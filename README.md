# Personal Safety Emergency Alert System

## Overview
A system designed to assist individuals in dangerous situations by enabling rapid SOS activation, real-time location sharing, and automatic alert escalation.

## Project Status
Phase 2 – Implementation

- ✅ System design (ERD, architecture, activity diagrams)
- ✅ Authentication module (register, login, JWT + refresh tokens)
- ✅ Dockerized backend + PostgreSQL
- ⬜ Trusted contacts module
- ⬜ Alert triggering & escalation module
- ⬜ Location tracking module
- ⬜ Notifications module
- ⬜ Frontend
- ⬜ CI/CD pipeline
- ⬜ Azure deployment

## Tech Stack
- **Backend:** Spring Boot 4, Java 21, Spring Security, JWT
- **Database:** PostgreSQL
- **Frontend:** React *(planned)*
- **Containerization:** Docker, Docker Compose
- **Hosting:** Azure *(planned)*

## Repository Structure
- `backend/sos-backend/` – Spring Boot backend
- `docker/` – Dockerfile references and Docker Compose setup
- `docs/` – Documentation (API reference, design decisions)
- `diagrams/` – System diagrams (use case diagram, activity diagrams, system architecture diagram)
- `research/` – Background and competitor research
- `deployment/` – Deployment and infrastructure decisions
- `frontend/` – React frontend *(planned)*
- `scripts/`
- `database/` - ERD, database flow

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
   Then open `.env` and fill in real values for `POSTGRES_PASSWORD` and `JWT_SECRET`.

3. Build and run:
```bash
   docker compose up --build
```

4. The backend will be available at `http://localhost:8080`. See [`docs/api.md`](docs/api.md) for available endpoints.

## Getting Started (Local, without Docker)

1. Ensure PostgreSQL is running locally, with a database created (default name: `sos_db`).
2. Copy the properties template:
```bash
   cd backend/sos-backend/src/main/resources
   cp application.properties.example application.properties
```
3. Fill in your real local database credentials and a generated JWT secret in `application.properties`.
4. Run via your IDE or:
```bash
   cd backend/sos-backend
   ./mvnw spring-boot:run
```

## Documentation
- [API Reference](docs/api.md)
- [Design Decisions](deployment/decisions.md)
- [Activity Diagrams](diagrams/activity-diagrams/)
  - [SOS Trigger and Escalation](diagrams/activity-diagrams/sos-trigger-escalation-activity-diagram.drawio.png)
  - [Alert Resolution by Trusted Contact](diagrams/activity-diagrams/trusted-contact-resolution-alert-and-user-confirmation.drawio.png)
