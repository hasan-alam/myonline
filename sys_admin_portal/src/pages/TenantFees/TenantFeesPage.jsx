import { useState, useEffect, useCallback } from 'react'
import { Plus, Edit, Trash2, RefreshCw, Package } from 'lucide-react'
import { tenantFeesApi } from '../../api/tenantFeesApi'
import PageHeader from '../../components/common/PageHeader'
import Spinner from '../../components/common/Spinner'
import EmptyState from '../../components/common/EmptyState'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import Modal from '../../components/modal/Modal'
import TenantFeeForm from './TenantFeeForm'
import toast from 'react-hot-toast'
import { formatCurrency, formatDateTime } from '../../utils/formatters'

export default function TenantFeesPage() {
  const [packages, setPackages] = useState([])
  const [loading, setLoading] = useState(true)
  const [createOpen, setCreateOpen] = useState(false)
  const [editTarget, setEditTarget] = useState(null)
  const [confirm, setConfirm] = useState(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await tenantFeesApi.getAll()
      setPackages(res.data.data || [])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  const handleDelete = async () => {
    try {
      await tenantFeesApi.delete(confirm.packageCode)
      toast.success(`Package "${confirm.packageName}" deleted`)
      setConfirm(null)
      fetchData()
    } catch {}
  }

  return (
    <div>
      <PageHeader
        title="Subscription Packages"
        subtitle="Define pricing tiers and subscription packages for tenants"
        action={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            <Plus className="w-4 h-4" /> Add Package
          </button>
        }
      />

      {/* Package cards */}
      {loading ? (
        <div className="card flex justify-center py-16"><Spinner size="lg" /></div>
      ) : packages.length === 0 ? (
        <div className="card"><EmptyState title="No packages configured" /></div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
          {packages.map((pkg) => (
            <div key={pkg.packageCode} className="card p-5 flex flex-col">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-primary-100 rounded-xl flex items-center justify-center">
                    <Package className="w-5 h-5 text-primary-600" />
                  </div>
                  <div>
                    <p className="font-bold text-gray-800">{pkg.packageName}</p>
                    <span className="font-mono text-xs text-primary-600 bg-primary-50 px-2 py-0.5 rounded">
                      {pkg.packageCode}
                    </span>
                  </div>
                </div>
                <div className="flex gap-1">
                  <button
                    onClick={() => setEditTarget(pkg)}
                    className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                  >
                    <Edit className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setConfirm(pkg)}
                    className="p-1.5 rounded-lg text-red-600 hover:bg-red-50 transition-colors"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              <div className="space-y-2 flex-1">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Products range</span>
                  <span className="font-medium text-gray-800">
                    {pkg.productCountFrom.toLocaleString()} – {pkg.productCountTo.toLocaleString()}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Registration fee</span>
                  <span className="font-medium text-gray-800">৳ {formatCurrency(pkg.registrationFee)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-500">Monthly fee</span>
                  <span className="font-bold text-primary-700 text-base">৳ {formatCurrency(pkg.monthlyFee)}</span>
                </div>
              </div>

              <div className="mt-4 pt-3 border-t border-gray-100">
                <p className="text-xs text-gray-400">Updated {formatDateTime(pkg.updatedAt)}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Also show as table */}
      {packages.length > 0 && !loading && (
        <div className="card">
          <div className="px-4 py-3 border-b border-gray-100">
            <h3 className="font-semibold text-gray-700 text-sm">Packages Overview</h3>
          </div>
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Package Name</th>
                  <th>Product Range</th>
                  <th>Registration Fee</th>
                  <th>Monthly Fee</th>
                  <th>Last Updated</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {packages.map((pkg) => (
                  <tr key={pkg.packageCode}>
                    <td>
                      <span className="font-mono font-medium text-primary-600">{pkg.packageCode}</span>
                    </td>
                    <td className="font-medium text-gray-800">{pkg.packageName}</td>
                    <td className="text-gray-600">
                      {pkg.productCountFrom.toLocaleString()} – {pkg.productCountTo.toLocaleString()}
                    </td>
                    <td className="text-gray-700">৳ {formatCurrency(pkg.registrationFee)}</td>
                    <td className="text-gray-700 font-medium">৳ {formatCurrency(pkg.monthlyFee)}</td>
                    <td className="text-gray-500 text-xs">{formatDateTime(pkg.updatedAt)}</td>
                    <td>
                      <div className="flex items-center gap-1">
                        <button
                          onClick={() => setEditTarget(pkg)}
                          className="p-1.5 rounded-lg text-blue-600 hover:bg-blue-50 transition-colors"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => setConfirm(pkg)}
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
        </div>
      )}

      <div className="flex justify-end mt-3">
        <button onClick={fetchData} className="btn-secondary btn-sm">
          <RefreshCw className="w-3.5 h-3.5" /> Refresh
        </button>
      </div>

      <Modal isOpen={createOpen} onClose={() => setCreateOpen(false)} title="Add Subscription Package">
        <TenantFeeForm
          onSuccess={() => { setCreateOpen(false); fetchData() }}
          onCancel={() => setCreateOpen(false)}
        />
      </Modal>

      {editTarget && (
        <Modal isOpen={!!editTarget} onClose={() => setEditTarget(null)} title="Edit Subscription Package">
          <TenantFeeForm
            pkg={editTarget}
            onSuccess={() => { setEditTarget(null); fetchData() }}
            onCancel={() => setEditTarget(null)}
          />
        </Modal>
      )}

      <ConfirmDialog
        isOpen={!!confirm}
        title="Delete Package"
        message={`Delete package "${confirm?.packageName}"? Tenants currently using this package will not be affected, but new registrations won't be able to select it.`}
        onConfirm={handleDelete}
        onCancel={() => setConfirm(null)}
        danger
      />
    </div>
  )
}
