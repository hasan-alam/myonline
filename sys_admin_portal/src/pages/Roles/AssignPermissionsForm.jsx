import { useState } from 'react'
import { roleApi } from '../../api/roleApi'
import toast from 'react-hot-toast'
import { Search } from 'lucide-react'

export default function AssignPermissionsForm({ role, allPermissions, onSuccess, onCancel }) {
  const assignedIds = new Set(role.permissions?.map((p) => p.id) || [])
  const [selected, setSelected] = useState(new Set(assignedIds))
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)

  const toggle = (id) => {
    const next = new Set(selected)
    if (next.has(id)) next.delete(id)
    else next.add(id)
    setSelected(next)
  }

  const handleSave = async () => {
    setLoading(true)
    try {
      const toAdd = [...selected].filter((id) => !assignedIds.has(id))
      const toRemove = [...assignedIds].filter((id) => !selected.has(id))
      if (toAdd.length > 0) await roleApi.assignPermissions(role.roleId, toAdd)
      if (toRemove.length > 0) await roleApi.removePermissions(role.roleId, toRemove)
      toast.success('Permissions updated')
      onSuccess()
    } finally {
      setLoading(false)
    }
  }

  const filtered = allPermissions.filter((p) =>
    p.permissionTitle?.toLowerCase().includes(search.toLowerCase()) ||
    p.permissionDescription?.toLowerCase().includes(search.toLowerCase()),
  )

  const groups = {
    SYSADMP: filtered.filter((p) => p.permissionFor === 'SYSADMP'),
    SHPADMP: filtered.filter((p) => p.permissionFor === 'SHPADMP'),
    BOTH: filtered.filter((p) => p.permissionFor === 'BOTH'),
  }

  const selectAll = (perms) => {
    const next = new Set(selected)
    perms.forEach((p) => next.add(p.id))
    setSelected(next)
  }

  const deselectAll = (perms) => {
    const next = new Set(selected)
    perms.forEach((p) => next.delete(p.id))
    setSelected(next)
  }

  const PermGroup = ({ title, perms }) =>
    perms.length === 0 ? null : (
      <div className="mb-6">
        <div className="flex items-center justify-between mb-2">
          <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">{title}</p>
          <div className="flex gap-2">
            <button type="button" onClick={() => selectAll(perms)} className="text-xs text-primary-600 hover:underline">Select all</button>
            <span className="text-gray-300">|</span>
            <button type="button" onClick={() => deselectAll(perms)} className="text-xs text-gray-500 hover:underline">Deselect all</button>
          </div>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-1">
          {perms.map((p) => (
            <label
              key={p.id}
              className={`flex items-start gap-2 p-2 rounded-lg cursor-pointer border transition-colors ${
                selected.has(p.id)
                  ? 'bg-primary-50 border-primary-200'
                  : 'border-transparent hover:bg-gray-50'
              }`}
            >
              <input
                type="checkbox"
                checked={selected.has(p.id)}
                onChange={() => toggle(p.id)}
                className="mt-0.5 w-3.5 h-3.5 text-primary-600"
              />
              <div>
                <p className="text-xs font-medium text-gray-800">{p.permissionTitle}</p>
                {p.permissionDescription && (
                  <p className="text-xs text-gray-500 leading-tight">{p.permissionDescription}</p>
                )}
              </div>
            </label>
          ))}
        </div>
      </div>
    )

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-sm text-gray-600">
          Role: <span className="font-semibold text-gray-800">{role.roleName}</span>
          <span className="ml-2 text-gray-400">({selected.size} selected)</span>
        </p>
        <div className="relative w-48">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-gray-400" />
          <input
            className="input pl-8 py-1.5 text-xs"
            placeholder="Filter permissions..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>

      <div className="max-h-[400px] overflow-y-auto pr-1">
        <PermGroup title="System Admin Permissions" perms={groups.SYSADMP} />
        <PermGroup title="Both Portals" perms={groups.BOTH} />
        <PermGroup title="Shop Admin Permissions" perms={groups.SHPADMP} />
      </div>

      <div className="flex justify-end gap-3 pt-4 border-t border-gray-100 mt-4">
        <button onClick={onCancel} className="btn-secondary">Cancel</button>
        <button onClick={handleSave} disabled={loading} className="btn-primary">
          {loading ? 'Saving...' : 'Save Permissions'}
        </button>
      </div>
    </div>
  )
}
