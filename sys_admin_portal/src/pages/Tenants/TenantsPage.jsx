import { useState, useEffect, useCallback } from 'react'
import { Search, Eye, CheckCircle, XCircle, RefreshCw, Filter } from 'lucide-react'
import { tenantApi } from '../../api/tenantApi'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import EmptyState from '../../components/common/EmptyState'
import Modal from '../../components/modal/Modal'
import RegistrationDetail from './RegistrationDetail'
import DecisionForm from './DecisionForm'
import toast from 'react-hot-toast'
import {
  formatDateTime,
  formatCurrency,
  approvalStatusLabel,
  approvalStatusBadge,
} from '../../utils/formatters'

const STATUS_FILTERS = [
  { value: '', label: 'All Status' },
  { value: 'P', label: 'Pending' },
  { value: 'A', label: 'Approved' },
  { value: 'R', label: 'Rejected' },
]

export default function TenantsPage() {
  const [registrations, setRegistrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [viewTarget, setViewTarget] = useState(null)
  const [decisionTarget, setDecisionTarget] = useState(null)
  const [decisionType, setDecisionType] = useState('approve')

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await tenantApi.getAllRegistrations()
      setRegistrations(res.data.data || [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  const handleSearch = async () => {
    setLoading(true)
    try {
      const params = {}
      if (search) params.tenantBusinessName = search
      if (statusFilter) params.approvalStatus = statusFilter
      const res = await tenantApi.searchRegistrations(params)
      setRegistrations(res.data.data || [])
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') handleSearch()
  }

  const openDecision = (reg, type) => {
    setDecisionTarget(reg)
    setDecisionType(type)
  }

  const pending = registrations.filter((r) => r.approvalStatus === 'P').length
  const approved = registrations.filter((r) => r.approvalStatus === 'A').length
  const rejected = registrations.filter((r) => r.approvalStatus === 'R').length

  return (
    <div>
      <PageHeader
        title="Tenant Registrations"
        subtitle="Review and process tenant registration requests"
      />

      {/* Stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-6">
        {[
          { label: 'Total', value: registrations.length, color: 'bg-blue-50 text-blue-700' },
          { label: 'Pending', value: pending, color: 'bg-yellow-50 text-yellow-700' },
          { label: 'Approved', value: approved, color: 'bg-green-50 text-green-700' },
          { label: 'Rejected', value: rejected, color: 'bg-red-50 text-red-700' },
        ].map((stat) => (
          <div key={stat.label} className={`card p-4 ${stat.color}`}>
            <p className="text-2xl font-bold">{stat.value}</p>
            <p className="text-sm font-medium mt-1">{stat.label}</p>
          </div>
        ))}
      </div>

      {/* Search & Filter */}
      <div className="card p-4 mb-4 flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-48">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Search by business name..."
            className="input pl-9"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="input w-auto min-w-36"
        >
          {STATUS_FILTERS.map((f) => (
            <option key={f.value} value={f.value}>{f.label}</option>
          ))}
        </select>
        <button onClick={handleSearch} className="btn-primary">
          <Filter className="w-4 h-4" /> Search
        </button>
        <button onClick={() => { setSearch(''); setStatusFilter(''); fetchData() }} className="btn-secondary">
          Clear
        </button>
      </div>

      {/* Table */}
      <div className="card">
        {loading ? (
          <div className="flex justify-center py-16"><Spinner size="lg" /></div>
        ) : registrations.length === 0 ? (
          <EmptyState title="No registrations found" />
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Business Name</th>
                  <th>Domain</th>
                  <th>Package</th>
                  <th>Reg. Fee</th>
                  <th>Contact</th>
                  <th>Status</th>
                  <th>Submitted</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {registrations.map((reg, idx) => (
                  <tr key={reg.id}>
                    <td className="text-gray-400 text-xs">{idx + 1}</td>
                    <td>
                      <div>
                        <p className="font-medium text-gray-800">{reg.tenantBusinessName}</p>
                        <p className="text-xs text-gray-500">{reg.emailAddress}</p>
                      </div>
                    </td>
                    <td>
                      <span className="font-mono text-sm text-primary-600">
                        {reg.domainPrefix}.myonline.com
                      </span>
                    </td>
                    <td>
                      <span className="badge badge-blue">{reg.packageCode}</span>
                    </td>
                    <td className="text-gray-700 font-medium">
                      {formatCurrency(reg.registrationFee)}
                    </td>
                    <td>
                      <div>
                        <p className="text-sm text-gray-700">{reg.contactPerson}</p>
                        <p className="text-xs text-gray-500">{reg.contactNumber1}</p>
                      </div>
                    </td>
                    <td>
                      <span className={approvalStatusBadge(reg.approvalStatus)}>
                        {approvalStatusLabel(reg.approvalStatus)}
                      </span>
                    </td>
                    <td className="text-gray-500 text-xs">{formatDateTime(reg.createdAt)}</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => setViewTarget(reg)}
                          title="View Details"
                          className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                        {reg.approvalStatus === 'P' && (
                          <>
                            <button
                              onClick={() => openDecision(reg, 'approve')}
                              title="Approve"
                              className="p-1.5 rounded-lg text-green-600 hover:bg-green-50 transition-colors"
                            >
                              <CheckCircle className="w-4 h-4" />
                            </button>
                            <button
                              onClick={() => openDecision(reg, 'reject')}
                              title="Reject"
                              className="p-1.5 rounded-lg text-red-600 hover:bg-red-50 transition-colors"
                            >
                              <XCircle className="w-4 h-4" />
                            </button>
                          </>
                        )}
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

      {/* View Detail Modal */}
      {viewTarget && (
        <Modal
          isOpen={!!viewTarget}
          onClose={() => setViewTarget(null)}
          title="Registration Details"
          size="xl"
        >
          <RegistrationDetail
            registration={viewTarget}
            onClose={() => setViewTarget(null)}
            onDecision={(type) => {
              setViewTarget(null)
              openDecision(viewTarget, type)
            }}
          />
        </Modal>
      )}

      {/* Decision Modal */}
      {decisionTarget && (
        <Modal
          isOpen={!!decisionTarget}
          onClose={() => setDecisionTarget(null)}
          title={decisionType === 'approve' ? 'Approve Registration' : 'Reject Registration'}
        >
          <DecisionForm
            registration={decisionTarget}
            type={decisionType}
            onSuccess={() => { setDecisionTarget(null); fetchData() }}
            onCancel={() => setDecisionTarget(null)}
          />
        </Modal>
      )}
    </div>
  )
}
