export const formatDate = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

export const formatDateTime = (dateStr) => {
  if (!dateStr) return '—'
  return new Date(dateStr).toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

export const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '—'
  return new Intl.NumberFormat('en-US').format(amount)
}

export const portalTypeLabel = (type) => {
  const map = {
    SYSADMP: 'System Admin',
    SHPADMP: 'Shop Admin',
    BOTH: 'Both Portals',
  }
  return map[type] || type
}

export const approvalStatusLabel = (status) => {
  const map = { P: 'Pending', A: 'Approved', R: 'Rejected' }
  return map[status] || status
}

export const approvalStatusBadge = (status) => {
  const map = {
    P: 'badge-yellow',
    A: 'badge-green',
    R: 'badge-red',
  }
  return map[status] || 'badge-gray'
}

export const statusBadge = (status) => {
  return status === 1 || status === 'A' ? 'badge-green' : 'badge-red'
}

export const statusLabel = (status) => {
  if (status === 1) return 'Active'
  if (status === 0) return 'Inactive'
  if (status === 'A') return 'Active'
  if (status === 'I') return 'Inactive'
  return String(status)
}
