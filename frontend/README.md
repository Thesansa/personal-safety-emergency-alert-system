# Frontend — Personal Safety Emergency Alert System

React frontend for the Auth module (Register, Login, Dashboard), built with Vite and styled with Tailwind CSS using the project's custom color palette (calm blue as primary, red reserved exclusively for SOS/emergency states).

## Status

- ✅ Register, Login, Dashboard pages — wired to the live backend API
- ✅ Dockerized (multi-stage build: Node → Nginx)
- ✅ Integrated into `docker-compose.yml` alongside backend + Postgres
- ⬜ Token refresh interceptor (currently tokens are stored in `localStorage` as a working placeholder — moving to a more secure handling approach is a known next step)
- ⬜ SOS trigger UI (next module)

## Tech Stack

- React 19 + Vite
- Tailwind CSS v4 (CSS-based `@theme` configuration, not the older `tailwind.config.js` approach)
- React Router (client-side routing)
- Axios (API calls)
- Lucide React (icons)

## Project Structure

```text
src/
├── api/
│   └── axios.js          # Axios instance, base URL configurable via VITE_API_BASE_URL
├── context/
│   └── AuthContext.jsx   # (in progress) centralized auth state
├── pages/
│   ├── Login.jsx
│   ├── Register.jsx
│   └── Dashboard.jsx
├── App.jsx               # Route definitions
├── main.jsx              # App entry point, wraps App in BrowserRouter
└── index.css             # Tailwind import + custom theme color definitions
```

## Environment Variables

Configured via Vite's `VITE_`-prefixed environment variables (only variables with this prefix are exposed to client-side code — a deliberate Vite security boundary).

| Variable | Purpose | Example |
|----------|---------|---------|
| `VITE_API_BASE_URL` | Base URL of the backend API | `https://sos-semali-backend.azurewebsites.net/api` |

Copy `.env.example` to `.env` and adjust if pointing at a different backend (e.g. local Docker Compose backend instead of the deployed Azure one).

> **Important distinction from the backend:** This value is baked in at **build time**, not read at container startup. Changing it requires rebuilding the image—there is no runtime `${VAR:default}` override mechanism here, unlike the Spring Boot backend.

## Running Locally (without Docker)

```bash
npm install
npm run dev
```

Visit `http://localhost:5173`.

## Running via Docker

See the root `docker/` folder—this frontend is one of three services orchestrated by `docker-compose.yml`, alongside `backend` and `postgres`.

```bash
cd docker
docker compose up --build
```

Visit `http://localhost:8081`.

The frontend's own `Dockerfile` uses a two-stage build:

1. **Build stage** (`node:20-alpine`) — installs dependencies and runs `npm run build`, producing static files.
2. **Serve stage** (`nginx:alpine`) — serves those static files. A custom `nginx.conf` is required specifically to support React Router's client-side routes (e.g. `/dashboard` loading correctly on a direct visit or page refresh, not just via in-app navigation).

## CI

`.github/workflows/frontend-ci.yml` runs on every push/PR to `main`, installing dependencies, linting, and building the app—using a path filter so it only actually executes for changes under `frontend/`, mirroring the same pattern used in the backend's CI workflow.

## Known Trade-offs

- **`npm install` used instead of `npm ci` in the Dockerfile**, despite `npm ci` being stricter and more reproducible. This was necessary due to a cross-platform lock file inconsistency (a native, platform-specific dependency pulled in by Tailwind's engine wasn't correctly recorded for Linux when the lock file was generated on Windows). Accepted as a pragmatic trade-off given the project timeline; worth revisiting if dependency drift becomes an issue.
- **Tokens currently stored in `localStorage`**, not `httpOnly` cookies. Functional, but a known security trade-off (readable by any script on the page)—documented in `deployment/decisions.md` alongside the other authentication trade-offs.