import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Users, ShieldCheck, Key, Building2, DollarSign, Clock, CheckCircle, XCircle, ArrowRight, RefreshCw } from 'lucide-react'
import { userApi } from '../../api/userApi'
import { roleApi } from '../../api/roleApi'
import { permissionApi } from '../../api/permissionApi'
import { tenantApi } from '../../api/tenantApi'
import { tenantFeesApi } from '../../api/tenantFeesApi'
import { useAuth } from '../../context/AuthContext'
import Spinner from '../../components/common/Spinner'
import { formatDateTime, approvalStatusBadge, approvalStatusLabel } from '../../utils/formatters'

export default function DashboardPage() {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [recentRegistrations, setRecentRegistrations] = useState([])
  const [loading, setLoading] = useState(true)

  const fetchStats = async () => {
    setLoading(true)
    try {
      const [usersRes, rolesRes, permsRes, registrationsRes, feesRes] = await Promise.allSettled([
        userApi.getAll(),
        roleApi.getAll(),
        permissionApi.getAll(),
        tenantApi.getAllRegistrations(),
        tenantFeesApi.getAll(),
      ])

      const users = usersRes.status === 'fulfilled' ? usersRes.value.data.data || [] : []
      const roles = rolesRes.status === 'fulfilled' ? rolesRes.value.data.data || [] : []
      const perms = permsRes.status === 'fulfilled' ? permsRes.value.data.data || [] : []
      const registrations = registrationsRes.status === 'fulfilled'
        ? registrationsRes.value.data.data || [] : []
      const fees = feesRes.status === 'fulfilled' ? feesRes.value.data.data || [] : []

      setStats({
        users: users.length,
        activeUsers: users.filter((u) => u.userStatus === 1).length,
        roles: roles.length,
        permissions: perms.length,
        totalRegistrations: registrations.length,
        pendingRegistrations: registrations.filter((r) => r.approvalStatus === 'P').length,
        approvedRegistrations: registrations.filter((r) => r.approvalStatus === 'A').length,
        rejectedRegistrations: registrations.filter((r) => r.approvalStatus === 'R').length,
        packages: fees.length,
      })

      setRecentRegistrations(registrations.slice(0, 5))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { fetchStats() }, [])

  const StatCard = ({ title, value, sub, icon: Icon, color, to }) => (
    <Link to={to} className="card p-5 hover:shadow-md transition-shadow group">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm text-gray-500 font-medium">{title}</p>
          <p className="text-3xl font-bold text-gray-800 mt-1">{value ?? '—'}</p>
          {sub && <p className="text-xs text-gray-400 mt-1">{sub}</p>}
        </div>
        <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${color}`}>
          <Icon className="w-6 h-6" />
        </div>
      </div>
      <div className="flex items-center gap-1 mt-4 text-xs text-primary-600 font-medium opacity-0 group-hover:opacity-100 transition-opacity">
        View details <ArrowRight className="w-3 h-3" />
      </div>
    </Link>
  )

  return (
    <div>
      {/* Welcome */}
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">
          Good {getGreeting()}, {user?.name?.split(' ')[0]}!
        </h1>
        <p className="text-gray-500 mt-1">Here's what's happening on your platform.</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <Spinner size="xl" />
        </div>
      ) : (
        <>
          {/* Stats Grid */}
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              title="Total Users"
              value={stats?.users}
              sub={`${stats?.activeUsers} active`}
              icon={Users}
              color="bg-blue-100 text-blue-600"
              to="/users"
            />
            <StatCard
              title="Roles"
              value={stats?.roles}
              icon={ShieldCheck}
              color="bg-purple-100 text-purple-600"
              to="/roles"
            />
            <StatCard
              title="Permissions"
              value={stats?.permissions}
              icon={Key}
              color="bg-indigo-100 text-indigo-600"
              to="/permissions"
            />
            <StatCard
              title="Packages"
              value={stats?.packages}
              icon={DollarSign}
              color="bg-green-100 text-green-600"
              to="/tenant-fees"
            />
          </div>

          {/* Tenant Stats */}
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
            <div className="card p-4">
              <div className="flex items-center gap-3">
                <Building2 className="w-8 h-8 text-gray-400" />
                <div>
                  <p className="text-2xl font-bold text-gray-800">{stats?.totalRegistrations}</p>
                  <p className="text-xs text-gray-500">Total Registrations</p>
                </div>
              </div>
            </div>
            <div className="card p-4 bg-yellow-50 border border-yellow-100">
              <div className="flex items-center gap-3">
                <Clock className="w-8 h-8 text-yellow-500" />
                <div>
                  <p className="text-2xl font-bold text-yellow-700">{stats?.pendingRegistrations}</p>
                  <p className="text-xs text-yellow-600">Pending Review</p>
                </div>
              </div>
            </div>
            <div className="card p-4 bg-green-50 border border-green-100">
              <div className="flex items-center gap-3">
                <CheckCircle className="w-8 h-8 text-green-500" />
                <div>
                  <p className="text-2xl font-bold text-green-700">{stats?.approvedRegistrations}</p>
                  <p className="text-xs text-green-600">Approved Tenants</p>
                </div>
              </div>
            </div>
            <div className="card p-4 bg-red-50 border border-red-100">
              <div className="flex items-center gap-3">
                <XCircle className="w-8 h-8 text-red-400" />
                <div>
                  <p className="text-2xl font-bold text-red-700">{stats?.rejectedRegistrations}</p>
                  <p className="text-xs text-red-600">Rejected</p>
                </div>
              </div>
            </div>
          </div>

          {/* Recent Registrations */}
          {recentRegistrations.length > 0 && (
            <div className="card">
              <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
                <h2 className="font-semibold text-gray-800">Recent Tenant Registrations</h2>
                <Link to="/tenants" className="text-sm text-primary-600 hover:underline font-medium flex items-center gap-1">
                  View all <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
              <div className="table-container">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Business Name</th>
                      <th>Domain</th>
                      <th>Package</th>
                      <th>Status</th>
                      <th>Submitted</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentRegistrations.map((reg) => (
                      <tr key={reg.id}>
                        <td className="font-medium text-gray-800">{reg.tenantBusinessName}</td>
                        <td className="font-mono text-sm text-primary-600">
                          {reg.domainPrefix}.myonline.com
                        </td>
                        <td>
                          <span className="badge badge-blue">{reg.packageCode}</span>
                        </td>
                        <td>
                          <span className={approvalStatusBadge(reg.approvalStatus)}>
                            {approvalStatusLabel(reg.approvalStatus)}
                          </span>
                        </td>
                        <td className="text-gray-500 text-xs">{formatDateTime(reg.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          <div className="flex justify-end mt-4">
            <button onClick={fetchStats} className="btn-secondary btn-sm">
              <RefreshCw className="w-3.5 h-3.5" /> Refresh
            </button>
          </div>
        </>
      )}
    </div>
  )
}

function getGreeting() {
  const hour = new Date().getHours()
  if (hour < 12) return 'morning'
  if (hour < 17) return 'afternoon'
  return 'evening'
}
