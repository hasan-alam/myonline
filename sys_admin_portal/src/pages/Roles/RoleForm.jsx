import { useState } from 'react'
import { roleApi } from '../../api/roleApi'
import toast from 'react-hot-toast'

const PORTAL_TYPES = [
  { value: 'SYSADMP', label: 'System Admin Portal' },
  { value: 'SHPADMP', label: 'Shop Admin Portal' },
  { value: 'BOTH', label: 'Both Portals' },
]

export default function RoleForm({ role, onSuccess, onCancel }) {
  const [form, setForm] = useState({
    roleName: role?.roleName || '',
    roleDescription: role?.roleDescription || '',
    roleFor: role?.roleFor || 'SYSADMP',
    shopId: role?.shopId || '',
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const set = (k, v) => setForm({ ...form, [k]: v })

  const validate = () => {
    const e = {}
    if (!form.roleName.trim()) e.roleName = 'Role name is required'
    if (!form.roleFor) e.roleFor = 'Portal type is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      const payload = { ...form }
      if (!payload.shopId) delete payload.shopId
      else payload.shopId = Number(payload.shopId)
      if (!payload.roleDescription) delete payload.roleDescription

      if (role) {
        await roleApi.update(role.roleId, payload)
        toast.success('Role updated')
      } else {
        await roleApi.create(payload)
        toast.success('Role created')
      }
      onSuccess()
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label">Role Name *</label>
        <input
          className={`input ${errors.roleName ? 'input-error' : ''}`}
          value={form.roleName}
          onChange={(e) => set('roleName', e.target.value)}
          placeholder="e.g. SHOP_MANAGER"
        />
        {errors.roleName && <p className="text-xs text-red-500 mt-1">{errors.roleName}</p>}
      </div>

      <div>
        <label className="label">Description</label>
        <textarea
          className="input resize-none"
          rows={2}
          value={form.roleDescription}
          onChange={(e) => set('roleDescription', e.target.value)}
          placeholder="Brief description of this role"
        />
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="label">Portal Type *</label>
          <select
            className={`input ${errors.roleFor ? 'input-error' : ''}`}
            value={form.roleFor}
            onChange={(e) => set('roleFor', e.target.value)}
          >
            {PORTAL_TYPES.map((p) => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
          {errors.roleFor && <p className="text-xs text-red-500 mt-1">{errors.roleFor}</p>}
        </div>
        {form.roleFor !== 'SYSADMP' && (
          <div>
            <label className="label">Shop ID</label>
            <input
              type="number"
              className="input"
              value={form.shopId}
              onChange={(e) => set('shopId', e.target.value)}
              placeholder="Tenant shop ID"
            />
          </div>
        )}
      </div>

      <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
        <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Saving...' : role ? 'Update Role' : 'Create Role'}
        </button>
      </div>
    </form>
  )
}
