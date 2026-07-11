import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'
import { userApi } from '../../api/userApi'
import toast from 'react-hot-toast'

const PORTAL_TYPES = [
  { value: 'SYSADMP', label: 'System Admin Portal' },
  { value: 'SHPADMP', label: 'Shop Admin Portal' },
  { value: 'BOTH', label: 'Both Portals' },
]

export default function CreateUserForm({ roles, onSuccess, onCancel }) {
  const [form, setForm] = useState({
    name: '',
    mobile: '',
    email: '',
    password: '',
    userFor: 'SYSADMP',
    shopId: '',
  })
  const [showPwd, setShowPwd] = useState(false)
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const set = (k, v) => setForm({ ...form, [k]: v })

  const validate = () => {
    const e = {}
    if (!form.name.trim()) e.name = 'Name is required'
    if (!form.mobile.trim()) e.mobile = 'Mobile is required'
    if (!form.email.trim()) e.email = 'Email is required'
    if (!form.password || form.password.length < 8) e.password = 'Minimum 8 characters'
    if (!form.userFor) e.userFor = 'Portal type is required'
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
      await userApi.create(payload)
      toast.success('User created successfully')
      onSuccess()
    } finally {
      setLoading(false)
    }
  }

  const fieldClass = (k) => `input ${errors[k] ? 'input-error' : ''}`

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="label">Full Name *</label>
          <input
            className={fieldClass('name')}
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
            placeholder="John Doe"
          />
          {errors.name && <p className="text-xs text-red-500 mt-1">{errors.name}</p>}
        </div>
        <div>
          <label className="label">Mobile *</label>
          <input
            className={fieldClass('mobile')}
            value={form.mobile}
            onChange={(e) => set('mobile', e.target.value)}
            placeholder="+880 1XXX XXXXXX"
          />
          {errors.mobile && <p className="text-xs text-red-500 mt-1">{errors.mobile}</p>}
        </div>
      </div>

      <div>
        <label className="label">Email Address *</label>
        <input
          type="email"
          className={fieldClass('email')}
          value={form.email}
          onChange={(e) => set('email', e.target.value)}
          placeholder="user@example.com"
        />
        {errors.email && <p className="text-xs text-red-500 mt-1">{errors.email}</p>}
      </div>

      <div>
        <label className="label">Password *</label>
        <div className="relative">
          <input
            type={showPwd ? 'text' : 'password'}
            className={`${fieldClass('password')} pr-10`}
            value={form.password}
            onChange={(e) => set('password', e.target.value)}
            placeholder="Min. 8 characters"
          />
          <button
            type="button"
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            onClick={() => setShowPwd(!showPwd)}
          >
            {showPwd ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
          </button>
        </div>
        {errors.password && <p className="text-xs text-red-500 mt-1">{errors.password}</p>}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="label">Portal Type *</label>
          <select
            className={fieldClass('userFor')}
            value={form.userFor}
            onChange={(e) => set('userFor', e.target.value)}
          >
            {PORTAL_TYPES.map((p) => (
              <option key={p.value} value={p.value}>{p.label}</option>
            ))}
          </select>
          {errors.userFor && <p className="text-xs text-red-500 mt-1">{errors.userFor}</p>}
        </div>
        {form.userFor !== 'SYSADMP' && (
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
        <button type="button" onClick={onCancel} className="btn-secondary">
          Cancel
        </button>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Creating...' : 'Create User'}
        </button>
      </div>
    </form>
  )
}
