import { tenantAxios } from './axios'

export const tenantFeesApi = {
  getAll: () => tenantAxios.get('/api/tenant-fees'),
  getByCode: (packageCode) => tenantAxios.get(`/api/tenant-fees/${packageCode}`),
  search: (params) => tenantAxios.get('/api/tenant-fees/search', { params }),
  create: (data) => tenantAxios.post('/api/tenant-fees', data),
  update: (packageCode, data) => tenantAxios.put(`/api/tenant-fees/${packageCode}`, data),
  delete: (packageCode) => tenantAxios.delete(`/api/tenant-fees/${packageCode}`),
}
