# CI/CD Workflows

This repository uses two independent GitHub Actions workflows:

* `.github/workflows/backend-ci-cd.yml`
* `.github/workflows/frontend-ci-cd.yml`

Both follow the same high-level pipeline:

```text
Code Push
   ↓
Path Check
   ↓
CI: Build / Test / Lint
   ↓
Docker Image Build
   ↓
Push to GHCR
   ↓
Azure Deployment
```

Separating the workflows allows the backend and frontend to be built and deployed independently.

---

## Workflow Triggers

Both workflows run on pushes to `main` but only when relevant files change.

| Changed files            | Backend CI/CD | Frontend CI/CD |
| ------------------------ | ------------: | -------------: |
| `backend/sos-backend/**` |             ✅ |              ❌ |
| `frontend/**`            |             ❌ |              ✅ |
| `README.md`              |             ❌ |              ❌ |
| Documentation only       |             ❌ |              ❌ |

This avoids unnecessary CI runs and reduces GitHub Actions resource usage.

> **Note:** Top-level `paths` filters are appropriate for the current project because these workflows are not currently used as required branch-protection checks.

---

# Backend CI/CD

**Workflow:** `.github/workflows/backend-ci-cd.yml`

### CI

The backend uses **Spring Boot, Maven, and JDK 21**.

```text
Checkout
   ↓
JDK 21 + Maven Cache
   ↓
Create application.properties
   ↓
mvn clean package
   ↓
Docker Build
```

Key steps:

1. Checkout using `actions/checkout@v4`.
2. Set up JDK 21 using `actions/setup-java@v4`.
3. Cache Maven dependencies.
4. Copy `application.properties.example` to `application.properties` for a secret-free CI configuration.
5. Run:

```bash
mvn clean package
```

This compiles the application, runs the configured tests, and produces the JAR.

Current backend tests use mocked repositories, so a live PostgreSQL database is not required.

6. Validate the Docker image:

```bash
docker build -t nova-sos-backend .
```

### CD

After successful CI on `main`:

```text
Backend Docker Image
       ↓
GHCR
       ↓
Azure Webhook
       ↓
Azure Backend Deployment
```

The image is published as:

```text
ghcr.io/thesansa/sos-backend:latest
```

GitHub Actions authenticates with GHCR using `GITHUB_TOKEN`.

Deployment is triggered through:

```text
AZURE_BACKEND_CD_WEBHOOK
```

---

# Frontend CI/CD

**Workflow:** `.github/workflows/frontend-ci-cd.yml`

The frontend uses **React, Vite, Node.js 20, and npm**.

### CI

```text
Checkout
   ↓
Node.js 20 + npm Cache
   ↓
npm ci
   ↓
npm run lint
   ↓
npm run build
   ↓
Docker Build
```

Key steps:

1. Checkout using `actions/checkout@v4`.
2. Set up Node.js 20 using `actions/setup-node@v4`.
3. Cache npm dependencies using `frontend/package-lock.json`.
4. Install dependencies:

```bash
npm ci
```

5. Run linting:

```bash
npm run lint
```

6. Build the production frontend:

```bash
npm run build
```

The build receives `VITE_API_BASE_URL` so the generated application knows which backend API to communicate with.

7. Validate the frontend Docker image using the same `VITE_API_BASE_URL` as a Docker build argument.

### CD

After successful CI on `main`:

```text
Frontend Docker Image
       ↓
GHCR
       ↓
Azure Webhook
       ↓
Azure Frontend Deployment
```

The image is published as:

```text
ghcr.io/thesansa/sos-frontend:latest
```

Deployment is triggered through:

```text
AZURE_FRONTEND_CD_WEBHOOK
```

---

# Dependency Caching

Caching improves build speed by avoiding repeated dependency downloads.

| Workflow | Tool  | Cache              |
| -------- | ----- | ------------------ |
| Backend  | Maven | `~/.m2/repository` |
| Frontend | npm   | npm package cache  |

The frontend cache uses `frontend/package-lock.json` to determine dependency state.

Caching is only an optimization. The workflows still perform normal builds and dependency installation:

```bash
mvn clean package
npm ci
npm run build
```

---

# CI vs CD

**Continuous Integration (CI)** verifies that the application is buildable and passes its automated checks.

```text
Checkout
   ↓
Dependencies
   ↓
Tests / Lint
   ↓
Application Build
   ↓
Docker Build
```

**Continuous Deployment (CD)** publishes the validated image and deploys it.

```text
Successful CI
   ↓
Docker Image
   ↓
GHCR
   ↓
Azure Webhook
   ↓
Azure Deployment
```

---

# Testing Scope

| Component              | Current CI validation                |
| ---------------------- | ------------------------------------ |
| Backend                | Maven build + unit/service tests     |
| Frontend               | `npm ci` + ESLint + production build |
| Docker                 | Backend and frontend image builds    |
| PostgreSQL             | Not used during current unit tests   |
| Full-stack integration | Not currently automated              |

The current pipeline does **not** automatically test the complete:

```text
Frontend → Backend → Hibernate/JPA → PostgreSQL
```

flow.

---

# Overall Architecture

```text
                         GitHub Repository
                                │
                              Push
                                │
                ┌───────────────┴───────────────┐
                │                               │
         Backend changed?                Frontend changed?
                │                               │
               YES                             YES
                │                               │
                ▼                               ▼
       Backend CI/CD                    Frontend CI/CD
                │                               │
        Build + Test + Docker          Lint + Build + Docker
                │                               │
                ▼                               ▼
              GHCR                            GHCR
                │                               │
                ▼                               ▼
             Azure                            Azure
            Backend                          Frontend
```

---

# Current Architecture

| Area                     | Decision                         |
| ------------------------ | -------------------------------- |
| CI/CD                    | GitHub Actions                   |
| Backend                  | Spring Boot + JDK 21 + Maven     |
| Frontend                 | React + Vite + Node.js 20        |
| Containerization         | Docker                           |
| Registry                 | GitHub Container Registry (GHCR) |
| Backend deployment       | Azure                            |
| Frontend deployment      | Azure                            |
| Deployment trigger       | Azure Webhooks                   |
| Database                 | PostgreSQL                       |
| Backend cache            | Maven                            |
| Frontend cache           | npm                              |
| Full integration testing | Not currently automated          |
| Image tagging            | `latest`                         |

---

# Security

Secrets must never be committed to the repository.

Examples include:

* Database passwords
* JWT secrets
* Azure credentials
* Deployment webhook URLs

Backend secrets should be supplied through the deployment environment, such as Azure App Settings.

`VITE_API_BASE_URL` is different from a secret because frontend build-time values are ultimately exposed to the browser.

---

# Future Improvements

Potential improvements include:

* PostgreSQL integration tests in CI.
* Full frontend → backend → database testing.
* Post-deployment health checks and smoke tests.
* Automated deployment verification.
* Image versioning instead of relying only on `latest`.
* Deployment rollback strategy.
* Docker image security scanning.
* Dependency vulnerability scanning.
* Separate staging and production environments.
* Improved CI/CD observability.

---

