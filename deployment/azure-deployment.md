# Azure Deployment

## Overview

The Personal Safety Emergency Alert System is currently deployed on Microsoft Azure.

The deployment consists of:

- Azure App Service (Linux)
- Azure PostgreSQL Flexible Server
- GitHub Container Registry (GHCR)

The backend is fully deployed, while the frontend has been containerized and verified locally but has not yet been deployed to Azure.

---

# Deployment Architecture

```text
Internet
        │
        ▼
Azure App Service (Linux)
        │
        ▼
Docker Container
        ├───────────────┐
        ▼               │
Spring Boot Backend     │
        │               │
        ▼               │
Azure PostgreSQL        │
Flexible Server         │

Frontend (Docker + Nginx)
        │
        ▼
Currently deployed locally via Docker Compose
(Planned Azure deployment)
```

---

# Components

## Backend

- Spring Boot
- Docker container
- Hosted on Azure App Service (Linux)

## Frontend

- React + Vite
- Dockerized with Nginx
- Verified locally using Docker Compose
- Azure deployment planned

## Database

- Azure PostgreSQL Flexible Server
- Burstable (B1ms)
- 32 GB Storage

## Container Registry

- GitHub Container Registry (GHCR)

Backend Docker images are pushed to GHCR and pulled by Azure App Service during deployment.

---

# Environment Configuration

The backend uses Azure App Service Application Settings to inject sensitive configuration at runtime, including:

- Database credentials
- JWT signing secret

This allows the same backend Docker image to be reused across environments without rebuilding.

The frontend differs from the backend because Vite embeds environment variables at **build time**. Values such as `VITE_API_BASE_URL` must therefore be supplied during the Docker build, requiring the image to be rebuilt if they change.

---

# Networking

The backend connects to Azure PostgreSQL using the server hostname provided by Azure.

Firewall rules allow:

- Azure services
- Developer workstation for database administration

The backend is configured to accept requests from both the local development frontend (`http://localhost:5173` and `http://localhost:8081`). The frontend's production URL will be added once it is deployed.

---

# Current Deployment Status

## Completed

- Azure PostgreSQL deployment
- Dockerized Spring Boot backend
- Dockerized React frontend
- GitHub Container Registry integration
- Azure App Service backend deployment
- Local Docker Compose environment (`postgres` + `backend` + `frontend`)
- Authentication module
  - Register
  - Login
  - Refresh Token
  - Logout

## Planned

- Frontend deployment to Azure
- Automated CI/CD deployment
- Production monitoring
- HTTPS custom domain