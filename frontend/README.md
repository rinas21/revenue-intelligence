# Frontend

React 19 + TypeScript 6 + Vite 8.

## Commands

```bash
npm install
npm run dev        # dev server on http://localhost:5173
npm run build      # tsc -b (strict) then production build
npm run lint
npm run preview
```

## How it talks to the backend

`vite.config.ts` proxies `/api` and `/actuator` to `http://localhost:8080`, so the
browser stays on one origin and no CORS configuration is needed in development.
`src/api/http.ts` holds the single configured Axios instance; components must not
call `axios` directly.

## Structure

Directories (`pages/`, `components/`, `hooks/`, `types/`, `layouts/`, `routes/`)
are created as features land rather than in advance — see Phase 1 in the
[root README](../README.md#project-status).
