import { useState } from 'react'
import { CheckCircle, XCircle, AlertTriangle, Copy, KeyRound, UserCheck, User } from 'lucide-react'
import { tenantApi } from '../../api/tenantApi'
import toast from 'react-hot-toast'

export default function DecisionForm({ registration: r, type, onSuccess, onCancel }) {
  const [remarks, setRemarks] = useState('')
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)   // set after successful decision

  const isApprove = type === 'approve'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await tenantApi.processDecision(r.id, {
        approved: isApprove,
        remarks: remarks || undefined,
      })
      const data = res.data.data
      setResult(data)

      if (!isApprove) {
        toast.success(`"${r.tenantBusinessName}" registration rejected.`)
      }
    } catch {
      // Error toast is shown by the axios interceptor
    } finally {
      setLoading(false)
    }
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text).then(() => toast.success('Copied to clipboard'))
  }

  // ── Post-decision result screen ───────────────────────────────────────────
  if (result) {
    if (isApprove) {
      const password = result.generatedPassword
      const userCreated = result.userCreated

      return (
        <div className="space-y-4">
          <div className="p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-3">
            <CheckCircle className="w-5 h-5 text-green-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-green-800">{r.tenantBusinessName} — Approved</p>
              <p className="text-sm text-green-700 mt-1">
                Tenant account created. A notification has been queued for the applicant.
              </p>
            </div>
          </div>

          {/* User creation result */}
          <div className={`p-4 rounded-lg border ${userCreated ? 'bg-blue-50 border-blue-200' : 'bg-gray-50 border-gray-200'}`}>
            <div className="flex items-center gap-2 mb-2">
              {userCreated ? (
                <UserCheck className="w-4 h-4 text-blue-600" />
              ) : (
                <User className="w-4 h-4 text-gray-500" />
              )}
              <p className="text-sm font-semibold text-gray-800">
                {userCreated ? 'New SHOP_ADMIN user created' : 'Existing user account found'}
              </p>
            </div>
            <p className="text-xs text-gray-500 mb-1">Email</p>
            <p className="text-sm font-mono text-gray-800">{r.emailAddress}</p>
          </div>

          {/* Generated password — only shown when a new user was created */}
          {userCreated && password && (
            <div className="p-4 bg-yellow-50 border border-yellow-300 rounded-lg">
              <div className="flex items-center gap-2 mb-2">
                <KeyRound className="w-4 h-4 text-yellow-700" />
                <p className="text-sm font-semibold text-yellow-800">Generated Default Password</p>
              </div>
              <p className="text-xs text-yellow-700 mb-3">
                Share this password with the tenant. They should change it immediately after first login.
              </p>
              <div className="flex items-center gap-2">
                <code className="flex-1 bg-white border border-yellow-300 rounded-lg px-3 py-2 text-base font-mono font-bold text-gray-900 tracking-widest select-all">
                  {password}
                </code>
                <button
                  onClick={() => copyToClipboard(password)}
                  className="p-2 rounded-lg bg-yellow-100 hover:bg-yellow-200 border border-yellow-300 text-yellow-800 transition-colors shrink-0"
                  title="Copy password"
                >
                  <Copy className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}

          {!userCreated && (
            <div className="p-3 bg-gray-50 border border-gray-200 rounded-lg text-sm text-gray-600">
              The applicant already has an account. They can log in using their existing password and select the Business.
            </div>
          )}

          <div className="flex justify-end pt-2 border-t border-gray-100">
            <button onClick={onSuccess} className="btn-primary">
              <CheckCircle className="w-4 h-4" /> Done
            </button>
          </div>
        </div>
      )
    } else {
      // Rejection result
      return (
        <div className="space-y-4">
          <div className="p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
            <XCircle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
            <div>
              <p className="font-semibold text-red-800">{r.tenantBusinessName} — Rejected</p>
              <p className="text-sm text-red-700 mt-1">
                Registration rejected. A notification has been queued for the applicant.
              </p>
            </div>
          </div>
          <div className="flex justify-end pt-2 border-t border-gray-100">
            <button onClick={onSuccess} className="btn-secondary">Close</button>
          </div>
        </div>
      )
    }
  }

  // ── Decision form ─────────────────────────────────────────────────────────
  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Summary */}
      <div className={`p-4 rounded-lg border ${isApprove ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
        <div className="flex items-start gap-3">
          {isApprove ? (
            <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
          ) : (
            <XCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
          )}
          <div>
            <p className="font-semibold text-gray-800">{r.tenantBusinessName}</p>
            <p className="text-sm text-gray-600 mt-1">
              {isApprove
                ? 'Approving will create a tenant account, create/link a SHOP_ADMIN user, and queue a notification.'
                : 'Rejecting will mark this registration as rejected and queue a notification.'}
            </p>
          </div>
        </div>
      </div>

      {!isApprove && (
        <div className="flex items-center gap-2 p-3 bg-yellow-50 rounded-lg text-yellow-700 text-sm">
          <AlertTriangle className="w-4 h-4 flex-shrink-0" />
          This action is irreversible. The applicant will not be able to resubmit with the same domain.
        </div>
      )}

      <div>
        <label className="label">
          Admin Remarks {!isApprove && <span className="text-red-500">*</span>}
        </label>
        <textarea
          className="input resize-none"
          rows={3}
          value={remarks}
          onChange={(e) => setRemarks(e.target.value)}
          placeholder={isApprove ? 'Optional remarks...' : 'Reason for rejection (required)'}
          required={!isApprove}
        />
      </div>

      <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
        <button type="button" onClick={onCancel} className="btn-secondary">
          Cancel
        </button>
        <button
          type="submit"
          disabled={loading}
          className={isApprove ? 'btn-success' : 'btn-danger'}
        >
          {loading ? 'Processing...' : isApprove ? 'Confirm Approval' : 'Confirm Rejection'}
        </button>
      </div>
    </form>
  )
}
