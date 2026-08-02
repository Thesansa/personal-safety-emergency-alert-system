# Docker — Local Development Environment

This folder orchestrates the full application stack locally using Docker Compose: three containers, networked together automatically by Compose.

## Services

| Service | Image Source | Purpose |
|----------|--------------|---------|
| `postgres` | Pre-built official image (`postgres:16-alpine`) | Local database, separate from the Azure-hosted one used in production |
| `backend` | Built from `../backend/sos-backend/Dockerfile` | Spring Boot API |
| `frontend` | Built from `../frontend/Dockerfile` | React app, served via Nginx |

No `Dockerfile` is needed for `postgres`—it's an existing, official image. Only genuinely custom code (`backend` and `frontend`) needs its own build instructions.

## First-time Setup

```bash
cp .env.example .env
```

Then fill in real values in `.env`:

- `POSTGRES_PASSWORD` — any password for the local Postgres container (independent of the real Azure database credentials)
- `JWT_SECRET` — any random string for local testing (independent of the deployed secret)
- `VITE_API_BASE_URL` — determines which backend the frontend should call. Two realistic options:
  - **Local containerized backend** — not directly reachable from inside another container via `localhost`; would require pointing at the `backend` service name instead, with additional Nginx/proxy configuration that is not currently set up.
  - **Deployed Azure backend** — the simplest option for now, and the current configuration. The local frontend container communicates with the live Azure API rather than the local backend.

## Running the Full Stack

```bash
docker compose up --build
```

Services will be available at:

- **Frontend:** `http://localhost:8081`
- **Backend:** `http://localhost:8080`
- **Postgres:** `localhost:5432` (reachable directly from tools such as `psql` or DBeaver)

## Shutting Down

```bash
docker compose down
```

This removes the containers while preserving the `postgres_data` named volume, allowing local test data to persist between sessions.

To completely remove the database volume and start with a fresh database:

```bash
docker compose down -v
```

## Notes

- The local Postgres instance is entirely separate from the Azure-hosted PostgreSQL Flexible Server used in production. Registering a user locally does **not** create that user in the production database, and vice versa.
- Backend CORS is configured to allow both:
  - `http://localhost:5173` — Vite development server (used with `npm run dev`)
  - `http://localhost:8081` — Dockerized frontend served by Nginx