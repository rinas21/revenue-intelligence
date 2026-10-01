import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')

  // The backend address is only needed by the dev server, so it is read as a
  // plain (non VITE_-prefixed) variable and never leaks into the client bundle.
  const backendUrl = env.BACKEND_URL ?? 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: Number(env.FRONTEND_PORT ?? 5173),
      // The frontend calls same-origin paths such as `/api/v1/orders` and Vite
      // forwards them to the backend. That keeps the browser on one origin in
      // development, so no CORS configuration is needed here or in the API.
      proxy: {
        '/api': { target: backendUrl, changeOrigin: true },
        '/actuator': { target: backendUrl, changeOrigin: true },
      },
    },
  }
})
