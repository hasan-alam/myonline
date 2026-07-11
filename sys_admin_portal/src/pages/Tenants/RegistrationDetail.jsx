import { useState, useEffect } from 'react'
import { formatDateTime, formatCurrency, approvalStatusLabel, approvalStatusBadge } from '../../utils/formatters'
import { CheckCircle, XCircle, MapPin, Phone, Mail, Globe, Package, CreditCard, Image, Loader2, ZoomIn } from 'lucide-react'
import { tenantApi } from '../../api/tenantApi'
import Spinner from '../../components/common/Spinner'

export default function RegistrationDetail({ registration: initialReg, onClose, onDecision }) {
  const [r, setR] = useState(initialReg)
  const [loading, setLoading] = useState(true)
  const [receiptZoomed, setReceiptZoomed] = useState(false)

  // Fetch full registration (includes receipt base64) when modal opens
  useEffect(() => {
    let cancelled = false
    tenantApi.getRegistrationById(initialReg.id)
      .then((res) => {
        if (!cancelled) setR(res.data.data)
      })
      .catch(() => {
        // Fall back to the list data already available
        if (!cancelled) setR(initialReg)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => { cancelled = true }
  }, [initialReg.id])

  const Section = ({ title, icon: Icon, children }) => (
    <div className="mb-5">
      <div className="flex items-center gap-2 mb-3 pb-2 border-b border-gray-100">
        <Icon className="w-4 h-4 text-primary-600" />
        <h3 className="text-sm font-semibold text-gray-700">{title}</h3>
      </div>
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">{children}</div>
    </div>
  )

  const Field = ({ label, value, mono = false }) => (
    <div>
      <p className="text-xs text-gray-500 mb-0.5">{label}</p>
      <p className={`text-sm text-gray-800 font-medium ${mono ? 'font-mono' : ''}`}>
        {value || '—'}
      </p>
    </div>
  )

  if (loading) {
    return (
      <div className="flex justify-center items-center py-16">
        <Spinner size="lg" />
      </div>
    )
  }

  return (
    <div>
      {/* Status Banner */}
      <div className={`flex items-center justify-between p-3 rounded-lg mb-5 ${
        r.approvalStatus === 'A' ? 'bg-green-50 border border-green-200' :
        r.approvalStatus === 'R' ? 'bg-red-50 border border-red-200' :
        'bg-yellow-50 border border-yellow-200'
      }`}>
        <div>
          <p className="text-xs text-gray-500">Registration ID</p>
          <p className="font-bold text-gray-800">#{r.id}</p>
        </div>
        <span className={`${approvalStatusBadge(r.approvalStatus)} text-sm px-3 py-1`}>
          {approvalStatusLabel(r.approvalStatus)}
        </span>
      </div>

      <Section title="Business Information" icon={Globe}>
        <Field label="Business Name" value={r.tenantBusinessName} />
        <Field label="Domain" value={`${r.domainPrefix}.myonline.com`} mono />
        <div className="sm:col-span-2">
          <p className="text-xs text-gray-500 mb-0.5">Mailing Address</p>
          <p className="text-sm text-gray-800 font-medium">{r.mailingAddress1}</p>
          {r.mailingAddress2 && <p className="text-sm text-gray-600">{r.mailingAddress2}</p>}
        </div>
      </Section>

      <Section title="Contact Details" icon={Phone}>
        <Field label="Contact Person" value={r.contactPerson} />
        <Field label="Email" value={r.emailAddress} />
        <Field label="Primary Phone" value={r.contactNumber1} />
        <Field label="Secondary Phone" value={r.contactNumber2} />
      </Section>

      <Section title="Subscription Package" icon={Package}>
        <Field label="Package Code" value={r.packageCode} />
        <Field label="Max Inventory Items" value={r.maxInventoryItems?.toLocaleString()} />
        <Field label="Registration Fee" value={`৳ ${formatCurrency(r.registrationFee)}`} />
        <Field label="Monthly Fee" value={`৳ ${formatCurrency(r.monthlyPayment)}`} />
      </Section>

      <Section title="Payment Details" icon={CreditCard}>
        <Field label="Payment Channel" value={r.registrationFeePmtChannel} />
        <Field label="Payment Reference" value={r.registrationFeePmtRef} mono />
        <Field label="Submitted" value={formatDateTime(r.createdAt)} />
        <Field label="Last Updated" value={formatDateTime(r.updatedAt)} />
      </Section>

      {/* Payment Receipt Image */}
      <div className="mb-5">
        <div className="flex items-center gap-2 mb-3 pb-2 border-b border-gray-100">
          <Image className="w-4 h-4 text-primary-600" />
          <h3 className="text-sm font-semibold text-gray-700">Payment Receipt</h3>
        </div>
        {r.registrationFeePmtReceiptBase64 ? (
          <div className="relative">
            <img
              src={`data:image/jpeg;base64,${r.registrationFeePmtReceiptBase64}`}
              alt="Payment receipt"
              className={`rounded-lg border border-gray-200 cursor-zoom-in transition-all duration-200 ${
                receiptZoomed
                  ? 'w-full max-h-none'
                  : 'max-h-48 object-contain w-full object-left-top'
              }`}
              onClick={() => setReceiptZoomed(!receiptZoomed)}
            />
            <button
              onClick={() => setReceiptZoomed(!receiptZoomed)}
              className="absolute top-2 right-2 bg-white/80 backdrop-blur-sm border border-gray-200 rounded-lg p-1.5 text-gray-600 hover:bg-white shadow-sm transition-colors"
              title={receiptZoomed ? 'Collapse' : 'Expand'}
            >
              <ZoomIn className="w-3.5 h-3.5" />
            </button>
            <p className="text-xs text-gray-400 mt-1.5">Click image to {receiptZoomed ? 'collapse' : 'expand'}</p>
          </div>
        ) : (
          <p className="text-sm text-gray-400 italic">No receipt uploaded</p>
        )}
      </div>

      {r.adminRemarks && (
        <div className="mb-4 p-3 bg-gray-50 rounded-lg">
          <p className="text-xs text-gray-500 mb-1">Admin Remarks</p>
          <p className="text-sm text-gray-700">{r.adminRemarks}</p>
        </div>
      )}

      {/* Actions */}
      <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
        <button onClick={onClose} className="btn-secondary">Close</button>
        {r.approvalStatus === 'P' && (
          <>
            <button onClick={() => onDecision('reject')} className="btn-danger">
              <XCircle className="w-4 h-4" /> Reject
            </button>
            <button onClick={() => onDecision('approve')} className="btn-success">
              <CheckCircle className="w-4 h-4" /> Approve
            </button>
          </>
        )}
      </div>
    </div>
  )
}
