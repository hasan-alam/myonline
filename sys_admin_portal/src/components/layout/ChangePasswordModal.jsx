import { useState } from 'react'
import { Eye, EyeOff } from 'lucide-react'
import Modal from '../modal/Modal'
import { authApi } from '../../api/authApi'
import toast from 'react-hot-toast'
import { useAuth } from '../../context/AuthContext'

export default function ChangePasswordModal({ isOpen, onClose }) {
  const { logout } = useAuth()
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [show, setShow] = useState({ current: false, new: false, confirm: false })
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})

  const validate = () => {
    const e = {}
    if (!form.currentPassword) e.currentPassword = 'Current password is required'
    if (!form.newPassword || form.newPassword.length < 8) e.newPassword = 'Minimum 8 characters'
    if (form.newPassword !== form.confirmPassword) e.confirmPassword = 'Passwords do not match'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      await authApi.changePassword(form)
      toast.success('Password changed. Please login again.')
      onClose()
      setTimeout(() => logout(), 1500)
    } finally {
      setLoading(false)
    }
  }

  const field = (name, label, showKey) => (
    <div>
      <label className="label">{label}</label>
      <div className="relative">
        <input
          type={show[showKey] ? 'text' : 'password'}
          value={form[name]}
          onChange={(e) => setForm({ ...form, [name]: e.target.value })}
          className={`input pr-10 ${errors[name] ? 'input-error' : ''}`}
          placeholder={label}
        />
        <button
          type="button"
          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          onClick={() => setShow({ ...show, [showKey]: !show[showKey] })}
        >
          {show[showKey] ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
        </button>
      </div>
      {errors[name] && <p className="text-xs text-red-500 mt-1">{errors[name]}</p>}
    </div>
  )

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Change Password">
      <form onSubmit={handleSubmit} className="space-y-4">
        {field('currentPassword', 'Current Password', 'current')}
        {field('newPassword', 'New Password', 'new')}
        {field('confirmPassword', 'Confirm New Password', 'confirm')}
        <div className="flex justify-end gap-3 pt-2">
          <button type="button" onClick={onClose} className="btn-secondary">
            Cancel
          </button>
          <button type="submit" disabled={loading} className="btn-primary">
            {loading ? 'Updating...' : 'Update Password'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
