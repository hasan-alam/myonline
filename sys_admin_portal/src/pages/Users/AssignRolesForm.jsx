import { useState } from 'react'
import { userApi } from '../../api/userApi'
import toast from 'react-hot-toast'

export default function AssignRolesForm({ user, allRoles, onSuccess, onCancel }) {
  const assignedIds = new Set(user.roles?.map((r) => r.roleId) || [])
  const [selected, setSelected] = useState(new Set(assignedIds))
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

      if (toAdd.length > 0) await userApi.assignRoles(user.userId, toAdd)
      if (toRemove.length > 0) await userApi.removeRoles(user.userId, toRemove)

      toast.success('Roles updated successfully')
      onSuccess()
    } finally {
      setLoading(false)
    }
  }

  const sysRoles = allRoles.filter((r) => r.roleFor === 'SYSADMP' || r.roleFor === 'BOTH')
  const shopRoles = allRoles.filter((r) => r.roleFor === 'SHPADMP')

  const RoleGroup = ({ title, roles }) => (
    roles.length > 0 && (
      <div className="mb-4">
        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">{title}</p>
        <div className="space-y-1">
          {roles.map((role) => (
            <label
              key={role.roleId}
              className={`flex items-start gap-3 p-2.5 rounded-lg cursor-pointer transition-colors border ${
                selected.has(role.roleId)
                  ? 'bg-primary-50 border-primary-200'
                  : 'border-transparent hover:bg-gray-50'
              }`}
            >
              <input
                type="checkbox"
                checked={selected.has(role.roleId)}
                onChange={() => toggle(role.roleId)}
                className="mt-0.5 w-4 h-4 text-primary-600 rounded"
              />
              <div>
                <p className="text-sm font-medium text-gray-800">{role.roleName}</p>
                {role.roleDescription && (
                  <p className="text-xs text-gray-500">{role.roleDescription}</p>
                )}
              </div>
            </label>
          ))}
        </div>
      </div>
    )
  )

  return (
    <div>
      <div className="mb-4 p-3 bg-gray-50 rounded-lg">
        <p className="text-sm text-gray-600">
          User: <span className="font-medium text-gray-800">{user.name}</span> &mdash;{' '}
          <span className="text-gray-500">{user.email}</span>
        </p>
      </div>

      <RoleGroup title="System Admin Roles" roles={sysRoles} />
      <RoleGroup title="Shop Admin Roles" roles={shopRoles} />

      {allRoles.length === 0 && (
        <p className="text-sm text-gray-500 py-4 text-center">No roles available</p>
      )}

      <div className="flex justify-end gap-3 pt-2 border-t border-gray-100 mt-4">
        <button onClick={onCancel} className="btn-secondary">Cancel</button>
        <button onClick={handleSave} disabled={loading} className="btn-primary">
          {loading ? 'Saving...' : 'Save Changes'}
        </button>
      </div>
    </div>
  )
}
