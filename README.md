# Personal Safety Emergency Alert System

## Overview
A system designed to assist individuals in dangerous situations by enabling rapid SOS activation, real-time location sharing, and automatic alert escalation.

## Project Status
Phase 3 – Core MVP Complete

- ✅ System design (ERD, architecture, activity diagrams)
- ✅ Authentication module (register, login, JWT + refresh tokens)
- ✅ Trusted contacts module (ownership-scoped CRUD)
- ✅ Alert triggering, cancellation, and resolution
- ✅ Automatic alert escalation (scheduled background check)
- ✅ Location tracking (trigger location + live pings)
- ✅ Email notifications to trusted contacts (trigger + escalation)
- ✅ React frontend (Auth, Trusted Contacts, SOS Dashboard)
- ✅ Dockerized backend + frontend + PostgreSQL
- ✅ CI/CD pipeline (GitHub Actions, independent backend/frontend workflows)
- ✅ Azure deployment (backend + frontend, both live)
- ⬜ Kubernetes (local cluster) — in progress
- ⬜ Alert history view (frontend)

## Tech Stack
- **Backend:** Spring Boot 4, Java 21, Spring Security, JWT, Spring Mail
- **Database:** PostgreSQL (Azure Flexible Server)
- **Frontend:** React 19, Vite, Tailwind CSS v4, React Router, Axios
- **Containerization:** Docker, Docker Compose
- **CI/CD:** GitHub Actions, GitHub Container Registry (GHCR)
- **Hosting:** Azure App Service (Linux, container-based)

## Repository Structure
- `backend/sos-backend/` – Spring Boot backend
- `frontend/` – React frontend
- `docker/` – Dockerfile references and Docker Compose setup
- `docs/` – Documentation (API reference, design decisions)
- `diagrams/` – System diagrams (use case, activity, architecture)
- `research/` – Background and competitor research
- `deployment/` – Deployment decisions, Azure setup, CD process, environment variables
- `database/` – ERD, database flow
- `scripts/`

## Getting Started (Local, via Docker)

**Prerequisites:** Docker Desktop installed and running.

1. Clone the repo:
```bash
   git clone https://github.com/Thesansa/personal-safety-emergency-alert-system.git
   cd personal-safety-emergency-alert-system
```

2. Set up environment variables:
```bash
   cd docker
   cp .env.example .env
```
   Fill in real values for `POSTGRES_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`, and `MAIL_PASSWORD`
   (a Gmail address + [App Password](https://myaccount.google.com/apppasswords)). See
   [`deployment/environment-variables.md`](deployment/environment-variables.md) for details.

3. Build and run:
```bash
   docker compose up --build
```

4. The frontend is available at `http://localhost:8081`, the backend at `http://localhost:8080`.
   See [`docs/api.md`](docs/api.md) for available endpoints.

## Getting Started (Local, without Docker)

1. Ensure PostgreSQL is running locally, with a database created (default name: `sos_db`).
2. Copy the properties template:
```bash
   cd backend/sos-backend/src/main/resources
   cp application.properties.example application.properties
```
3. Fill in your real local database credentials, a generated JWT secret, and Gmail SMTP
   credentials in `application.properties`.
4. Run via your IDE or:
```bash
   cd backend/sos-backend
   ./mvnw spring-boot:run
```

## CI/CD

- ✅ Independent GitHub Actions workflows for backend and frontend (build, test/lint, Docker
  build, deploy)
- ✅ Continuous Deployment to Azure via GHCR + Azure container webhooks
- See [`.github/workflows/README.md`](.github/workflows/README.md) for full pipeline details

## Live Deployment
- **Frontend:** https://sos-semali-frontend.azurewebsites.net
- **Backend:** https://sos-semali-backend.azurewebsites.net

> The Azure PostgreSQL database and both App Services are stopped between work sessions to
> conserve Azure for Students credit. Need to infrom before testing deployed version in order to start the services, Please allow 1–2 minutes for them to come online if
> testing the live deployment.

## Documentation
- [API Reference](docs/api.md)
- [Design Decisions](deployment/decisions.md)
- [Azure Deployment](deployment/azure-deployment.md)
- [Frontend Deployment](deployment/frontend-deployment.md)
- [CD Process](deployment/CD-process.md)
- [Environment Variables](deployment/environment-variables.md)
- [Backend Architecture](backend/README.md)
- [Frontend Architecture](frontend/README.md)
- [Activity Diagrams](diagrams/activity-diagrams/)