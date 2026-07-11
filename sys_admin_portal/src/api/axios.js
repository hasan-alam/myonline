import axios from 'axios'
import toast from 'react-hot-toast'

const AUTH_BASE_URL = import.meta.env.VITE_AUTH_SERVICE_URL ?? ''
const TENANT_BASE_URL = import.meta.env.VITE_TENANT_SERVICE_URL ?? ''

// Auth service instance
export const authAxios = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Tenant service instance
export const tenantAxios = axios.create({
  baseURL: TENANT_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Attach JWT to every request
const attachToken = (config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}

authAxios.interceptors.request.use(attachToken)
tenantAxios.interceptors.request.use(attachToken)

// Handle 401 — try refresh token once, then redirect to login
let isRefreshing = false
let refreshSubscribers = []

const onRefreshed = (token) => {
  refreshSubscribers.forEach((cb) => cb(token))
  refreshSubscribers = []
}

const addRefreshSubscriber = (cb) => {
  refreshSubscribers.push(cb)
}

const handleResponseError = (axiosInstance) => async (error) => {
  const originalRequest = error.config

  // Skip the refresh-token flow for auth endpoints (login, refresh-token)
  // so that login failures propagate normally to the caller.
  const isAuthEndpoint = originalRequest?.url?.includes('/api/auth/')

  if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
    if (isRefreshing) {
      return new Promise((resolve) => {
        addRefreshSubscriber((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`
          resolve(axiosInstance(originalRequest))
        })
      })
    }

    originalRequest._retry = true
    isRefreshing = true

    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) {
      isRefreshing = false
      localStorage.clear()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    try {
      const response = await axios.post(`${AUTH_BASE_URL}/api/auth/refresh-token`, {
        refreshToken,
      })
      const newToken = response.data.data.accessToken
      localStorage.setItem('accessToken', newToken)
      localStorage.setItem('refreshToken', response.data.data.refreshToken)
      onRefreshed(newToken)
      originalRequest.headers.Authorization = `Bearer ${newToken}`
      return axiosInstance(originalRequest)
    } catch {
      isRefreshing = false
      localStorage.clear()
      window.location.href = '/login'
      return Promise.reject(error)
    } finally {
      isRefreshing = false
    }
  }

  // Show error toast for non-401 errors.
  // For auth endpoints (login), skip the toast — the page handles its own message.
  if (error.response?.status !== 401 && !isAuthEndpoint) {
    const msg = error.response?.data?.message || 'An unexpected error occurred.'
    toast.error(msg)
  }

  return Promise.reject(error)
}

authAxios.interceptors.response.use((r) => r, handleResponseError(authAxios))
tenantAxios.interceptors.response.use((r) => r, handleResponseError(tenantAxios))
