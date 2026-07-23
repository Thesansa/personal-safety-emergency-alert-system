# Azure Deployment

## Overview

The Personal Safety Emergency Alert System backend is deployed on Microsoft Azure.

The deployment currently consists of:

- Azure App Service (Linux)
- Azure PostgreSQL Flexible Server
- GitHub Container Registry (GHCR)

The frontend has not yet been deployed and will be added after its implementation is completed.

---

# Deployment Architecture

```
Internet
        │
        ▼
Azure App Service (Linux)
        │
        ▼
Docker Container
        │
        ▼
Spring Boot Backend
        │
        ▼
Azure PostgreSQL Flexible Server
```

---

# Components

## Backend

- Spring Boot
- Docker container
- Hosted on Azure App Service (Linux)

## Database

- Azure PostgreSQL Flexible Server
- Burstable (B1ms)
- 32 GB Storage

## Container Registry

- GitHub Container Registry (GHCR)

Docker images are pushed to GHCR and pulled by Azure App Service during deployment.

---

# Environment Configuration

Application secrets are not stored inside the Docker image.

Sensitive configuration values such as:

- Database credentials
- JWT signing secret

are injected at runtime using Azure App Service Application Settings.

This allows the same Docker image to be used across different environments without modification.

---

# Networking

The backend communicates with Azure PostgreSQL using the server hostname provided by Azure.

Firewall rules allow:

- Azure services
- Developer workstation for database administration

---

# Current Deployment Status

## Completed

- Azure PostgreSQL deployment
- Dockerized Spring Boot backend
- GitHub Container Registry integration
- Azure App Service deployment
- Authentication module deployed
- Register
- Login
- Refresh Token
- Logout

## Planned

- Frontend deployment
- CI/CD with GitHub Actions
- Production monitoring
- HTTPS custom domain
