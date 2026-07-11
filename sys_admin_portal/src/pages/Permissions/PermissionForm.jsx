import { useState } from 'react'
import { permissionApi } from '../../api/permissionApi'
import toast from 'react-hot-toast'

const PORTAL_TYPES = [
  { value: 'SYSADMP', label: 'System Admin Portal' },
  { value: 'SHPADMP', label: 'Shop Admin Portal' },
  { value: 'BOTH', label: 'Both Portals' },
]

export default function PermissionForm({ permission, onSuccess, onCancel }) {
  const [form, setForm] = useState({
    permissionTitle: permission?.permissionTitle || '',
    permissionDescription: permission?.permissionDescription || '',
    permissionFor: permission?.permissionFor || 'SYSADMP',
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const set = (k, v) => setForm({ ...form, [k]: v })

  const validate = () => {
    const e = {}
    if (!form.permissionTitle.trim()) e.permissionTitle = 'Permission title is required'
    if (!form.permissionFor) e.permissionFor = 'Portal type is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      const payload = { ...form }
      if (!payload.permissionDescription) delete payload.permissionDescription
      if (permission) {
        await permissionApi.update(permission.id, payload)
        toast.success('Permission updated')
      } else {
        await permissionApi.create(payload)
        toast.success('Permission created')
      }
      onSuccess()
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label">Permission Title *</label>
        <input
          className={`input font-mono ${errors.permissionTitle ? 'input-error' : ''}`}
          value={form.permissionTitle}
          onChange={(e) => set('permissionTitle', e.target.value.toUpperCase())}
          placeholder="e.g. PRODUCT_CREATE"
        />
        {errors.permissionTitle && (
          <p className="text-xs text-red-500 mt-1">{errors.permissionTitle}</p>
        )}
        <p className="text-xs text-gray-400 mt-1">Use UPPER_SNAKE_CASE format</p>
      </div>

      <div>
        <label className="label">Description</label>
        <textarea
          className="input resize-none"
          rows={2}
          value={form.permissionDescription}
          onChange={(e) => set('permissionDescription', e.target.value)}
          placeholder="What does this permission allow?"
        />
      </div>

      <div>
        <label className="label">Portal Type *</label>
        <select
          className={`input ${errors.permissionFor ? 'input-error' : ''}`}
          value={form.permissionFor}
          onChange={(e) => set('permissionFor', e.target.value)}
        >
          {PORTAL_TYPES.map((p) => (
            <option key={p.value} value={p.value}>{p.label}</option>
          ))}
        </select>
        {errors.permissionFor && (
          <p className="text-xs text-red-500 mt-1">{errors.permissionFor}</p>
        )}
      </div>

      <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
        <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Saving...' : permission ? 'Update Permission' : 'Create Permission'}
        </button>
      </div>
    </form>
  )
}
