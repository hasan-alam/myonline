import { authAxios } from './axios'

export const roleApi = {
  getAll: () => authAxios.get('/api/roles'),
  getById: (id) => authAxios.get(`/api/roles/${id}`),
  getByShop: (shopId) => authAxios.get(`/api/roles/shop/${shopId}`),
  create: (data) => authAxios.post('/api/roles', data),
  update: (id, data) => authAxios.put(`/api/roles/${id}`, data),
  activate: (id) => authAxios.put(`/api/roles/${id}/activate`),
  deactivate: (id) => authAxios.put(`/api/roles/${id}/deactivate`),
  assignPermissions: (id, permissionIds) =>
    authAxios.post(`/api/roles/${id}/permissions`, { permissionIds }),
  removePermissions: (id, permissionIds) =>
    authAxios.delete(`/api/roles/${id}/permissions`, { data: { permissionIds } }),
  delete: (id) => authAxios.delete(`/api/roles/${id}`),
}
