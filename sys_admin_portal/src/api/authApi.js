import { authAxios } from './axios'

export const authApi = {
  login: (data) => authAxios.post('/api/auth/login', data),
  logout: () => authAxios.post('/api/auth/logout'),
  refreshToken: (refreshToken) => authAxios.post('/api/auth/refresh-token', { refreshToken }),
  changePassword: (data) => authAxios.put('/api/auth/change-password', data),
}
