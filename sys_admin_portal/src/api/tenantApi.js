import { tenantAxios } from './axios'

export const tenantApi = {
  // Tenant Registrations
  getAllRegistrations: () => tenantAxios.get('/api/tenant-registrations'),
  searchRegistrations: (params) =>
    tenantAxios.get('/api/tenant-registrations/search', { params }),
  getRegistrationById: (id) => tenantAxios.get(`/api/tenant-registrations/${id}`),
  checkDomain: (domainPrefix) =>
    tenantAxios.get('/api/tenant-registrations/check-domain', { params: { domainPrefix } }),
  submitRegistration: (data) => tenantAxios.post('/api/tenant-registrations', data),
  processDecision: (id, data) =>
    tenantAxios.put(`/api/tenant-registrations/${id}/decision`, data),
}
