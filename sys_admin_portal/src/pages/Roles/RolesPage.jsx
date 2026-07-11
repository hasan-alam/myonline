import { useState, useEffect, useCallback } from 'react'
import { Plus, Search, ToggleLeft, ToggleRight, Trash2, Key, Edit, RefreshCw } from 'lucide-react'
import { roleApi } from '../../api/roleApi'
import { permissionApi } from '../../api/permissionApi'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import EmptyState from '../../components/common/EmptyState'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import Modal from '../../components/modal/Modal'
import RoleForm from './RoleForm'
import AssignPermissionsForm from './AssignPermissionsForm'
import toast from 'react-hot-toast'
import { formatDateTime, portalTypeLabel, statusBadge, statusLabel } from '../../utils/formatters'

export default function RolesPage() {
  const [roles, setRoles] = useState([])
  const [permissions, setPermissions] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [assignPermsTarget, setAssignPermsTarget] = useState(null)
  const [confirm, setConfirm] = useState(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const [rolesRes, permsRes] = await Promise.all([roleApi.getAll(), permissionApi.getAll()])
      setRoles(rolesRes.data.data || [])
      setPermissions(permsRes.data.data || [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  const filtered = roles.filter((r) =>
    [r.roleName, r.roleDescription].some((f) =>
      f?.toLowerCase().includes(search.toLowerCase()),
    ),
  )

  const handleToggleStatus = async (role) => {
    try {
      if (role.roleStatus === 1) {
        await roleApi.deactivate(role.roleId)
        toast.success(`"${role.roleName}" deactivated`)
      } else {
        await roleApi.activate(role.roleId)
        toast.success(`"${role.roleName}" activated`)
      }
      fetchData()
    } catch {}
  }

  const handleDelete = async () => {
    try {
      await roleApi.delete(confirm.roleId)
      toast.success('Role deleted')
      setConfirm(null)
      fetchData()
    } catch {}
  }

  return (
    <div>
      <PageHeader
        title="Role Management"
        subtitle="Define roles and assign permissions to control access"
        action={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            <Plus className="w-4 h-4" /> Add Role
          </button>
        }
      />

      <div className="card p-4 mb-4">
        <div className="relative max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search roles..."
            className="input pl-9"
          />
        </div>
      </div>

      <div className="card">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : filtered.length === 0 ? (
          <EmptyState title="No roles found" />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Role Name</th>
                  <th>Description</th>
                  <th>Portal</th>
                  <th>Status</th>
                  <th>Permissions</th>
                  <th>Created</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((role, idx) => (
                  <tr key={role.roleId}>
                    <td className="text-gray-400 text-xs">{idx + 1}</td>
                    <td className="font-medium text-gray-800">{role.roleName}</td>
                    <td className="text-gray-500 text-sm max-w-xs truncate">{role.roleDescription || '—'}</td>
                    <td>
                      <span className="badge badge-blue">{portalTypeLabel(role.roleFor)}</span>
                    </td>
                    <td>
                      <span className={statusBadge(role.roleStatus)}>{statusLabel(role.roleStatus)}</span>
                    </td>
                    <td>
                      <span className="text-sm text-gray-600 font-medium">
                        {role.permissions?.length || 0} permissions
                      </span>
                    </td>
                    <td className="text-gray-500 text-xs">{formatDateTime(role.createdAt)}</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => setEditTarget(role)}
                          title="Edit"
                          className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => setAssignPermsTarget(role)}
                          title="Manage Permissions"
                          className="p-1.5 rounded-lg text-purple-600 hover:bg-purple-50 transition-colors"
                        >
                          <Key className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleToggleStatus(role)}
                          title={role.roleStatus === 1 ? 'Deactivate' : 'Activate'}
                          className={`p-1.5 rounded-lg transition-colors ${
                            role.roleStatus === 1
                              ? 'text-yellow-600 hover:bg-yellow-50'
                              : 'text-green-600 hover:bg-green-50'
                          }`}
                        >
                          {role.roleStatus === 1 ? (
                            <ToggleRight className="w-4 h-4" />
                          ) : (
                            <ToggleLeft className="w-4 h-4" />
                          )}
                        </button>
                        <button
                          onClick={() => setConfirm(role)}
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

      <div className="flex justify-end mt-3">
        <button onClick={fetchData} className="btn-secondary btn-sm">
          <RefreshCw className="w-3.5 h-3.5" /> Refresh
        </button>
      </div>

      {/* Create Modal */}
      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Create New Role">
        <RoleForm
          onSuccess={() => { setCreateOpen(false); fetchData() }}
          onCancel={() => setCreateOpen(false)}
        />
      </Modal>

      {/* Edit Modal */}
      {editTarget && (
        <Modal isOpen={!!editTarget} onClose={() => setEditTarget(null)} title="Edit Role">
          <RoleForm
            role={editTarget}
            onSuccess={() => { setEditTarget(null); fetchData() }}
            onCancel={() => setEditTarget(null)}
          />
        </Modal>
      )}

      {/* Assign Permissions Modal */}
      {assignPermsTarget && (
        <Modal
          isOpen={!!assignPermsTarget}
          onClose={() => setAssignPermsTarget(null)}
          title={`Permissions — ${assignPermsTarget.roleName}`}
          size="xl"
        >
          <AssignPermissionsForm
            role={assignPermsTarget}
            allPermissions={permissions}
            onSuccess={() => { setAssignPermsTarget(null); fetchData() }}
            onCancel={() => setAssignPermsTarget(null)}
          />
        </Modal>
      )}

      {/* Delete Confirm */}
      <ConfirmDialog
        isOpen={!!confirm}
        title="Delete Role"
        message={`Delete role "${confirm?.roleName}"? This cannot be undone.`}
        onConfirm={handleDelete}
        onCancel={() => setConfirm(null)}
        danger
      />
    </div>
  )
}
