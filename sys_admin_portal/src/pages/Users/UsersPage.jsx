import { useState, useEffect, useCallback } from 'react'
import { Plus, Search, UserCheck, UserX, Trash2, Shield, RefreshCw } from 'lucide-react'
import { userApi } from '../../api/userApi'
import { roleApi } from '../../api/roleApi'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import EmptyState from '../../components/common/EmptyState'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import Modal from '../../components/modal/Modal'
import CreateUserForm from './CreateUserForm'
import AssignRolesForm from './AssignRolesForm'
import toast from 'react-hot-toast'
import { formatDateTime, portalTypeLabel, statusBadge, statusLabel } from '../../utils/formatters'

export default function UsersPage() {
  const [users, setUsers] = useState([])
  const [roles, setRoles] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [assignRolesTarget, setAssignRolesTarget] = useState(null)
  const [confirm, setConfirm] = useState(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const [usersRes, rolesRes] = await Promise.all([userApi.getAll(), roleApi.getAll()])
      setUsers(usersRes.data.data || [])
      setRoles(rolesRes.data.data || [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  const filtered = users.filter((u) =>
    [u.name, u.email, u.mobile].some((f) =>
      f?.toLowerCase().includes(search.toLowerCase()),
    ),
  )

  const handleToggleStatus = async (user) => {
    try {
      if (user.userStatus === 1) {
        await userApi.deactivate(user.userId)
        toast.success(`${user.name} deactivated`)
      } else {
        await userApi.activate(user.userId)
        toast.success(`${user.name} activated`)
      }
      fetchData()
    } catch {}
  }

  const handleDelete = async () => {
    try {
      await userApi.delete(confirm.userId)
      toast.success('User deleted')
      setConfirm(null)
      fetchData()
    } catch {}
  }

  return (
    <div>
      <PageHeader
        title="User Management"
        subtitle="Manage system admin users, their roles and access"
        action={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            <Plus className="w-4 h-4" /> Add User
          </button>
        }
      />

      {/* Search bar */}
      <div className="card p-4 mb-4">
        <div className="relative max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by name, email, mobile..."
            className="input pl-9"
          />
        </div>
      </div>

      {/* Table */}
      <div className="card">
        {loading ? (
          <div className="flex justify-center py-16">
            <Spinner size="lg" />
          </div>
        ) : filtered.length === 0 ? (
          <EmptyState title="No users found" description="Try adjusting your search or create a new user." />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Mobile</th>
                  <th>Portal</th>
                  <th>Status</th>
                  <th>Roles</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((user, idx) => (
                  <tr key={user.userId}>
                    <td className="text-gray-400 text-xs">{idx + 1}</td>
                    <td>
                      <div className="flex items-center gap-2">
                        <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center flex-shrink-0">
                          <span className="text-primary-700 text-xs font-semibold">
                            {user.name?.charAt(0)?.toUpperCase()}
                          </span>
                        </div>
                        <span className="font-medium text-gray-800">{user.name}</span>
                      </div>
                    </td>
                    <td className="text-gray-600">{user.email}</td>
                    <td className="text-gray-600">{user.mobile}</td>
                    <td>
                      <span className="badge badge-blue">{portalTypeLabel(user.userFor)}</span>
                    </td>
                    <td>
                      <span className={statusBadge(user.userStatus)}>
                        {statusLabel(user.userStatus)}
                      </span>
                    </td>
                    <td>
                      <div className="flex flex-wrap gap-1">
                        {user.roles?.length > 0 ? (
                          user.roles.map((r) => (
                            <span key={r.roleId} className="badge badge-gray text-xs">
                              {r.roleName}
                            </span>
                          ))
                        ) : (
                          <span className="text-gray-400 text-xs">No roles</span>
                        )}
                      </div>
                    </td>
                    <td className="text-gray-500 text-xs">{formatDateTime(user.createdAt)}</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => setAssignRolesTarget(user)}
                          title="Manage Roles"
                          className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                        >
                          <Shield className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleToggleStatus(user)}
                          title={user.userStatus === 1 ? 'Deactivate' : 'Activate'}
                          className={`p-1.5 rounded-lg transition-colors ${
                            user.userStatus === 1
                              ? 'text-yellow-600 hover:bg-yellow-50'
                              : 'text-green-600 hover:bg-green-50'
                          }`}
                        >
                          {user.userStatus === 1 ? (
                            <UserX className="w-4 h-4" />
                          ) : (
                            <UserCheck className="w-4 h-4" />
                          )}
                        </button>
                        <button
                          onClick={() => setConfirm(user)}
                          title="Delete"
                          className="p-1.5 rounded-lg text-red-600 hover:bg-red-50 transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Refresh */}
      <div className="flex justify-end mt-3">
        <button onClick={fetchData} className="btn-secondary btn-sm">
          <RefreshCw className="w-3.5 h-3.5" /> Refresh
        </button>
      </div>

      {/* Create User Modal */}
      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Create New User" size="lg">
        <CreateUserForm
          roles={roles}
          onSuccess={() => { setCreateOpen(false); fetchData() }}
          onCancel={() => setCreateOpen(false)}
        />
      </Modal>

      {/* Assign Roles Modal */}
      {assignRolesTarget && (
        <Modal
          isOpen={!!assignRolesTarget}
          onClose={() => setAssignRolesTarget(null)}
          title={`Manage Roles — ${assignRolesTarget.name}`}
          size="lg"
        >
          <AssignRolesForm
            user={assignRolesTarget}
            allRoles={roles}
            onSuccess={() => { setAssignRolesTarget(null); fetchData() }}
            onCancel={() => setAssignRolesTarget(null)}
          />
        </Modal>
      )}

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!confirm}
        title="Delete User"
        message={`Are you sure you want to permanently delete "${confirm?.name}"? This action cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setConfirm(null)}
        danger
      />
    </div>
  )
}
