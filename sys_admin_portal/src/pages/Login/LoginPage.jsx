import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, EyeOff, Mail, Lock, Users, ShieldCheck, TrendingUp, Settings } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import toast from 'react-hot-toast'

const features = [
  {
    icon: Users,
    label: 'Multi-Tenant',
    desc: 'One Platform, Many Stores',
    color: 'text-blue-500',
    bg: 'bg-blue-50',
  },
  {
    icon: ShieldCheck,
    label: 'Secure',
    desc: 'Data Isolation & Role Management',
    color: 'text-green-500',
    bg: 'bg-green-50',
  },
  {
    icon: TrendingUp,
    label: 'Scalable',
    desc: 'Built to Grow With You',
    color: 'text-purple-500',
    bg: 'bg-purple-50',
  },
  {
    icon: Settings,
    label: 'Customizable',
    desc: 'Flexible & Easy to Customize',
    color: 'text-orange-500',
    bg: 'bg-orange-50',
  },
]

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})

  const validate = () => {
    const e = {}
    if (!form.email) e.email = 'Email is required'
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) e.email = 'Invalid email format'
    if (!form.password) e.password = 'Password is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      await login(form.email, form.password)
      toast.success('Login successful!')
      navigate('/')
    } catch (err) {
      const msg = err?.response?.data?.message || 'Login failed. Check your credentials.'
      toast.error(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-200 flex flex-col items-center justify-center p-4">
      {/* Main card */}
      <div className="w-full max-w-5xl bg-white rounded-3xl shadow-2xl overflow-hidden flex flex-col lg:flex-row">

        {/* ── Left Panel ── */}
        <div className="hidden lg:flex lg:w-[58%] flex-col items-center justify-between bg-gradient-to-br from-blue-50 via-indigo-50 to-slate-100 px-10 py-12 relative overflow-hidden">
          {/* Subtle decorative circles */}
          <div className="absolute -top-20 -left-20 w-64 h-64 rounded-full bg-blue-100/60 blur-3xl pointer-events-none" />
          <div className="absolute -bottom-20 -right-10 w-72 h-72 rounded-full bg-indigo-100/60 blur-3xl pointer-events-none" />

          {/* Logo + brand */}
          <div className="relative flex flex-col items-center text-center">
            <img
              src="/logo.jpeg"
              alt="MyOnline"
              className="w-52 h-52 object-contain drop-shadow-md"
            />
            <div className="flex items-center gap-3 mt-3 mb-1">
              <span className="h-px w-10 bg-blue-400" />
              <p className="text-gray-600 font-semibold text-sm tracking-wide">
                Multi-Tenant Ecommerce Website
              </p>
              <span className="h-px w-10 bg-blue-400" />
            </div>
            <p className="text-blue-600 font-semibold text-sm mt-0.5">
              One Platform. Multiple Stores. Unlimited Possibilities.
            </p>
          </div>

          {/* Feature grid */}
          <div className="relative grid grid-cols-2 gap-x-8 gap-y-6 w-full mt-10">
            {features.map(({ icon: Icon, label, desc, color, bg }) => (
              <div key={label} className="flex flex-col items-center text-center gap-2">
                <div className={`w-12 h-12 rounded-xl ${bg} flex items-center justify-center`}>
                  <Icon className={`w-6 h-6 ${color}`} />
                </div>
                <p className="font-semibold text-gray-800 text-sm">{label}</p>
                <p className="text-gray-500 text-xs leading-tight">{desc}</p>
              </div>
            ))}
          </div>

          {/* Bottom tagline */}
          <p className="relative text-xs text-gray-400 mt-10">
            Powering the future of multi-tenant commerce
          </p>
        </div>

        {/* ── Right Panel ── */}
        <div className="w-full lg:w-[42%] flex flex-col justify-center px-8 sm:px-12 py-12">
          {/* Mobile-only logo */}
          <div className="flex justify-center mb-8 lg:hidden">
            <img src="/logo.jpeg" alt="MyOnline" className="w-28 h-28 object-contain" />
          </div>

          <h1 className="text-2xl font-bold text-gray-900 mb-1">Welcome Back!</h1>
          <p className="text-sm text-gray-500 mb-8">Sign in to access your admin account</p>

          <form onSubmit={handleSubmit} className="space-y-5" noValidate>
            {/* Email */}
            <div>
              <label className="label">Email Address</label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <Mail className="w-4 h-4" />
                </span>
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setForm({ ...form, email: e.target.value })}
                  className={`input pl-9 ${errors.email ? 'input-error' : ''}`}
                  placeholder="Enter your email address"
                  autoComplete="email"
                />
              </div>
              {errors.email && (
                <p className="text-xs text-red-500 mt-1">{errors.email}</p>
              )}
            </div>

            {/* Password */}
            <div>
              <label className="label">Password</label>
              <div className="relative">
                <span className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
                  <Lock className="w-4 h-4" />
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })}
                  className={`input pl-9 pr-10 ${errors.password ? 'input-error' : ''}`}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
              {errors.password && (
                <p className="text-xs text-red-500 mt-1">{errors.password}</p>
              )}
            </div>

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="btn-primary w-full justify-center py-3 text-sm font-semibold mt-2"
            >
              {loading ? (
                <>
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Signing in...
                </>
              ) : (
                <>
                  <Lock className="w-4 h-4" />
                  Sign In
                </>
              )}
            </button>
          </form>

          <div className="mt-8 pt-6 border-t border-gray-100 space-y-2 text-center">
            <p className="text-xs text-gray-400">
              Don&apos;t have an account?{' '}
              <span className="text-primary-600 font-medium">Contact Administrator</span>
            </p>
            <p className="text-xs text-gray-400">Authorized personnel only.</p>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="mt-5 flex flex-col items-center gap-1">
        <div className="flex items-center gap-1.5 text-gray-500 text-xs">
          <ShieldCheck className="w-3.5 h-3.5" />
          <span>Your data is secure with enterprise-grade protection</span>
        </div>
        <p className="text-gray-400 text-xs">© 2025 myonline. All rights reserved.</p>
      </div>
    </div>
  )
}
