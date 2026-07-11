import { useState, useEffect, useCallback } from 'react'
import { Plus, Search, ToggleLeft, ToggleRight, Trash2, Edit, RefreshCw } from 'lucide-react'
import { permissionApi } from '../../api/permissionApi'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import EmptyState from '../../components/common/EmptyState'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import Modal from '../../components/modal/Modal'
import PermissionForm from './PermissionForm'
import toast from 'react-hot-toast'
import { formatDateTime, portalTypeLabel, statusBadge, statusLabel } from '../../utils/formatters'

const PORTAL_FILTERS = [
  { value: '', label: 'All Portals' },
  { value: 'SYSADMP', label: 'System Admin' },
  { value: 'SHPADMP', label: 'Shop Admin' },
  { value: 'BOTH', label: 'Both' },
]

export default function PermissionsPage() {
  const [permissions, setPermissions] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [portalFilter, setPortalFilter] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [confirm, setConfirm] = useState(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await permissionApi.getAll()
      setPermissions(res.data.data || [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  const filtered = permissions.filter((p) => {
    const matchSearch = [p.permissionTitle, p.permissionDescription].some((f) =>
      f?.toLowerCase().includes(search.toLowerCase()),
    )
    const matchPortal = !portalFilter || p.permissionFor === portalFilter
    return matchSearch && matchPortal
  })

  const handleToggleStatus = async (perm) => {
    try {
      if (perm.permissionStatus === 1) {
        await permissionApi.deactivate(perm.id)
        toast.success(`"${perm.permissionTitle}" deactivated`)
      } else {
        await permissionApi.activate(perm.id)
        toast.success(`"${perm.permissionTitle}" activated`)
      }
      fetchData()
    } catch {}
  }

  const handleDelete = async () => {
    try {
      await permissionApi.delete(confirm.id)
      toast.success('Permission deleted')
      setConfirm(null)
      fetchData()
    } catch {}
  }

  // Group by portal
  const grouped = {
    SYSADMP: filtered.filter((p) => p.permissionFor === 'SYSADMP'),
    BOTH: filtered.filter((p) => p.permissionFor === 'BOTH'),
    SHPADMP: filtered.filter((p) => p.permissionFor === 'SHPADMP'),
  }

  const PermTable = ({ perms }) =>
    perms.length === 0 ? null : (
      <div className="table-container mb-4">
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Permission Title</th>
              <th>Description</th>
              <th>Portal</th>
              <th>Status</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {perms.map((perm, idx) => (
              <tr key={perm.id}>
                <td className="text-gray-400 text-xs">{idx + 1}</td>
                <td>
                  <span className="font-mono text-sm font-medium text-gray-800">
                    {perm.permissionTitle}
                  </span>
                </td>
                <td className="text-gray-500 text-sm">{perm.permissionDescription || '—'}</td>
                <td>
                  <span className="badge badge-blue">{portalTypeLabel(perm.permissionFor)}</span>
                </td>
                <td>
                  <span className={statusBadge(perm.permissionStatus)}>
                    {statusLabel(perm.permissionStatus)}
                  </span>
                </td>
                <td className="text-gray-500 text-xs">{formatDateTime(perm.createdAt)}</td>
                <td>
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => setEditTarget(perm)}
                      title="Edit"
                      className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                    >
                      <Edit className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleToggleStatus(perm)}
                      title={perm.permissionStatus === 1 ? 'Deactivate' : 'Activate'}
                      className={`p-1.5 rounded-lg transition-colors ${
                        perm.permissionStatus === 1
                          ? 'text-yellow-600 hover:bg-yellow-50'
                          : 'text-green-600 hover:bg-green-50'
                      }`}
                    >
                      {perm.permissionStatus === 1 ? (
                        <ToggleRight className="w-4 h-4" />
                      ) : (
                        <ToggleLeft className="w-4 h-4" />
                      )}
                    </button>
                    <button
                      onClick={() => setConfirm(perm)}
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
    )

  return (
    <div>
      <PageHeader
        title="Permission Management"
        subtitle={`${permissions.length} permissions across all portals`}
        action={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            <Plus className="w-4 h-4" /> Add Permission
          </button>
        }
      />

      {/* Filters */}
      <div className="card p-4 mb-4 flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-48">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search permissions..."
            className="input pl-9"
          />
        </div>
        <select
          value={portalFilter}
          onChange={(e) => setPortalFilter(e.target.value)}
          className="input w-auto min-w-36"
        >
          {PORTAL_FILTERS.map((f) => (
            <option key={f.value} value={f.value}>{f.label}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <div className="card flex justify-center py-16"><Spinner size="lg" /></div>
      ) : filtered.length === 0 ? (
        <div className="card"><EmptyState title="No permissions found" /></div>
      ) : (
        <div>
          {grouped.SYSADMP.length > 0 && (
            <div className="card p-4 mb-4">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-blue-500 inline-block" />
                System Admin Portal
                <span className="badge badge-blue ml-1">{grouped.SYSADMP.length}</span>
              </h3>
              <PermTable perms={grouped.SYSADMP} />
            </div>
          )}
          {grouped.BOTH.length > 0 && (
            <div className="card p-4 mb-4">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-purple-500 inline-block" />
                Both Portals
                <span className="badge badge-blue ml-1">{grouped.BOTH.length}</span>
              </h3>
              <PermTable perms={grouped.BOTH} />
            </div>
          )}
          {grouped.SHPADMP.length > 0 && (
            <div className="card p-4 mb-4">
              <h3 className="font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <span className="w-2.5 h-2.5 rounded-full bg-green-500 inline-block" />
                Shop Admin Portal
                <span className="badge badge-green ml-1">{grouped.SHPADMP.length}</span>
              </h3>
              <PermTable perms={grouped.SHPADMP} />
            </div>
          )}
        </div>
      )}

      <div className="flex justify-end mt-3">
        <button onClick={fetchData} className="btn-secondary btn-sm">
          <RefreshCw className="w-3.5 h-3.5" /> Refresh
        </button>
      </div>

      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Add Permission">
        <PermissionForm
          onSuccess={() => { setCreateOpen(false); fetchData() }}
          onCancel={() => setCreateOpen(false)}
        />
      </Modal>

      {editTarget && (
        <Modal isOpen={!!editTarget} onClose={() => setEditTarget(null)} title="Edit Permission">
          <PermissionForm
            permission={editTarget}
            onSuccess={() => { setEditTarget(null); fetchData() }}
            onCancel={() => setEditTarget(null)}
          />
        </Modal>
      )}

      <ConfirmDialog
        isOpen={!!confirm}
        title="Delete Permission"
        message={`Delete permission "${confirm?.permissionTitle}"? This may affect roles that use it.`}
        onConfirm={handleDelete}
        onCancel={() => setConfirm(null)}
        danger
      />
    </div>
  )
}
