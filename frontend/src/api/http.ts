/**
 * The single place Axios is configured.
 *
 * Components must not call `axios` directly. Every API call goes through this
 * instance so that the base URL, timeouts and the response/error shape are
 * decided once instead of being repeated — and subtly diverging — across the
 * dashboard, the orders page and the import screen.
 */
import axios from 'axios'

/**
 * Relative by default so requests are same-origin and are proxied by the Vite
 * dev server to the backend. Override with VITE_API_BASE_URL only when the API
 * is genuinely served from a different origin (e.g. a deployed environment),
 * which also makes CORS relevant.
 */
const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api'

export const http = axios.create({
  baseURL,
  // Fail visibly rather than hanging: a 10s ceiling is long enough for the
  // analytics queries this app will issue and short enough that a stuck
  // request is visible to the user instead of silently spinning.
  timeout: 10_000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
  },
})
