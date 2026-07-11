import { useState } from 'react'
import { tenantFeesApi } from '../../api/tenantFeesApi'
import toast from 'react-hot-toast'

export default function TenantFeeForm({ pkg, onSuccess, onCancel }) {
  const [form, setForm] = useState({
    packageCode: pkg?.packageCode || '',
    packageName: pkg?.packageName || '',
    productCountFrom: pkg?.productCountFrom || '',
    productCountTo: pkg?.productCountTo || '',
    registrationFee: pkg?.registrationFee || '',
    monthlyFee: pkg?.monthlyFee || '',
  })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)

  const set = (k, v) => setForm({ ...form, [k]: v })

  const validate = () => {
    const e = {}
    if (!pkg && !form.packageCode.trim()) e.packageCode = 'Package code is required'
    if (!form.packageName.trim()) e.packageName = 'Package name is required'
    if (!form.productCountFrom || form.productCountFrom < 1)
      e.productCountFrom = 'Min products must be >= 1'
    if (!form.productCountTo || form.productCountTo < 1)
      e.productCountTo = 'Max products must be >= 1'
    if (Number(form.productCountFrom) >= Number(form.productCountTo))
      e.productCountTo = 'Must be greater than min products'
    if (form.registrationFee === '' || form.registrationFee < 0)
      e.registrationFee = 'Registration fee must be >= 0'
    if (form.monthlyFee === '' || form.monthlyFee < 0)
      e.monthlyFee = 'Monthly fee must be >= 0'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validate()) return
    setLoading(true)
    try {
      const payload = {
        packageName: form.packageName,
        productCountFrom: Number(form.productCountFrom),
        productCountTo: Number(form.productCountTo),
        registrationFee: Number(form.registrationFee),
        monthlyFee: Number(form.monthlyFee),
      }
      if (pkg) {
        await tenantFeesApi.update(pkg.packageCode, payload)
        toast.success('Package updated')
      } else {
        await tenantFeesApi.create({ ...payload, packageCode: form.packageCode })
        toast.success('Package created')
      }
      onSuccess()
    } finally {
      setLoading(false)
    }
  }

  const fc = (k) => `input ${errors[k] ? 'input-error' : ''}`

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="label">Package Code *</label>
          <input
            className={`${fc('packageCode')} font-mono uppercase`}
            value={form.packageCode}
            onChange={(e) => set('packageCode', e.target.value.toUpperCase())}
            placeholder="e.g. STARTER"
            disabled={!!pkg}
          />
          {errors.packageCode && <p className="text-xs text-red-500 mt-1">{errors.packageCode}</p>}
          {pkg && <p className="text-xs text-gray-400 mt-1">Package code cannot be changed</p>}
        </div>
        <div>
          <label className="label">Package Name *</label>
          <input
            className={fc('packageName')}
            value={form.packageName}
            onChange={(e) => set('packageName', e.target.value)}
            placeholder="e.g. Starter Package"
          />
          {errors.packageName && <p className="text-xs text-red-500 mt-1">{errors.packageName}</p>}
        </div>
      </div>

      <div>
        <label className="label">Product Count Range *</label>
        <div className="grid grid-cols-2 gap-3">
          <div>
            <input
              type="number"
              className={fc('productCountFrom')}
              value={form.productCountFrom}
              onChange={(e) => set('productCountFrom', e.target.value)}
              placeholder="Min (e.g. 1)"
              min={1}
            />
            {errors.productCountFrom && (
              <p className="text-xs text-red-500 mt-1">{errors.productCountFrom}</p>
            )}
          </div>
          <div>
            <input
              type="number"
              className={fc('productCountTo')}
              value={form.productCountTo}
              onChange={(e) => set('productCountTo', e.target.value)}
              placeholder="Max (e.g. 50)"
              min={1}
            />
            {errors.productCountTo && (
              <p className="text-xs text-red-500 mt-1">{errors.productCountTo}</p>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="label">Registration Fee (৳) *</label>
          <input
            type="number"
            className={fc('registrationFee')}
            value={form.registrationFee}
            onChange={(e) => set('registrationFee', e.target.value)}
            placeholder="e.g. 5000"
            min={0}
          />
          {errors.registrationFee && (
            <p className="text-xs text-red-500 mt-1">{errors.registrationFee}</p>
          )}
        </div>
        <div>
          <label className="label">Monthly Fee (৳) *</label>
          <input
            type="number"
            className={fc('monthlyFee')}
            value={form.monthlyFee}
            onChange={(e) => set('monthlyFee', e.target.value)}
            placeholder="e.g. 1000"
            min={0}
          />
          {errors.monthlyFee && <p className="text-xs text-red-500 mt-1">{errors.monthlyFee}</p>}
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
        <button type="button" onClick={onCancel} className="btn-secondary">Cancel</button>
        <button type="submit" disabled={loading} className="btn-primary">
          {loading ? 'Saving...' : pkg ? 'Update Package' : 'Create Package'}
        </button>
      </div>
    </form>
  )
}
