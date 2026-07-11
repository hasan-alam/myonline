import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/api/auth': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/users': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/roles': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/permissions': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/tenant': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
})
