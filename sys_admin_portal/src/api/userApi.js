import { authAxios } from './axios'

export const userApi = {
  // Public — no auth required. Pass { email } or { mobile } in params.
  checkCount: (params) => authAxios.get('/api/users/count', { params }),

  getAll: () => authAxios.get('/api/users'),
  getById: (id) => authAxios.get(`/api/users/${id}`),
  getByShop: (shopId) => authAxios.get(`/api/users/shop/${shopId}`),
  create: (data) => authAxios.post('/api/users', data),
  activate: (id) => authAxios.put(`/api/users/${id}/activate`),
  deactivate: (id) => authAxios.put(`/api/users/${id}/deactivate`),
  assignRoles: (id, roleIds) => authAxios.post(`/api/users/${id}/roles`, { roleIds }),
  removeRoles: (id, roleIds) => authAxios.delete(`/api/users/${id}/roles`, { data: { roleIds } }),
  delete: (id) => authAxios.delete(`/api/users/${id}`),
}
