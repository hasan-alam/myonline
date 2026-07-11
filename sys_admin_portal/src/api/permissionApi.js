import { authAxios } from './axios'

export const permissionApi = {
  getAll: () => authAxios.get('/api/permissions'),
  getById: (id) => authAxios.get(`/api/permissions/${id}`),
  getByPortal: (portalType) => authAxios.get(`/api/permissions/portal/${portalType}`),
  create: (data) => authAxios.post('/api/permissions', data),
  update: (id, data) => authAxios.put(`/api/permissions/${id}`, data),
  activate: (id) => authAxios.put(`/api/permissions/${id}/activate`),
  deactivate: (id) => authAxios.put(`/api/permissions/${id}/deactivate`),
  delete: (id) => authAxios.delete(`/api/permissions/${id}`),
}
