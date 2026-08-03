## Frontend Deployment to Azure

**Goal:** Host the React frontend on Azure App Service, reusing the same GHCR + App Service
pattern already proven with the backend, and reusing the existing App Service Plan.

### Steps taken

1. Confirmed backend and database were running before starting:
```bash
   az webapp show --resource-group sos-mvp-rg --name sos-semali-backend --query state -o tsv
   az postgres flexible-server show --resource-group sos-mvp-rg --name sos-semali-db --query state -o tsv
```

2. Built the frontend image locally, with the real backend URL baked in at build time
   (required, since Vite bakes environment variables into the compiled JS — unlike the
   backend's runtime `${VAR:default}` mechanism):
```bash
   docker build --build-arg VITE_API_BASE_URL=https://sos-semali-backend.azurewebsites.net/api \
     -t ghcr.io/thesansa/sos-frontend:latest .
```

3. Pushed to GHCR (same registry already used for the backend):
```bash
   docker push ghcr.io/thesansa/sos-frontend:latest
```

4. Created a new Web App on the **existing** `sos-mvp-plan` (no new plan needed — see
   "Quota lesson" below for why this doesn't increase risk):
```bash
   az webapp create --resource-group sos-mvp-rg --plan sos-mvp-plan \
     --name sos-semali-frontend \
     --deployment-container-image-name ghcr.io/thesansa/sos-frontend:latest
```

5. Authorized the Web App to pull the private GHCR image, using the same GitHub PAT already
   in use for the backend:
```bash
   az webapp config container set --resource-group sos-mvp-rg --name sos-semali-frontend \
     --container-image-name ghcr.io/thesansa/sos-frontend:latest \
     --container-registry-url https://ghcr.io \
     --container-registry-user Thesansa \
     --container-registry-password <token>
```

6. Set the correct container port — **80**, not 8080 (Nginx's default, unlike the backend's
   Spring Boot port):
```bash
   az webapp config appsettings set --resource-group sos-mvp-rg --name sos-semali-frontend \
     --settings WEBSITES_PORT=80
```

7. Enforced HTTPS, same as the backend:
```bash
   az webapp update --resource-group sos-mvp-rg --name sos-semali-frontend --https-only true
```

8. **CORS fix required.** The new frontend origin (`https://sos-semali-frontend.azurewebsites.net`)
   had to be added to the backend's `SecurityConfig` allowed origins list, followed by a full
   backend redeploy (rebuild image → push to GHCR → `az webapp restart`) — the same redeploy
   cycle now performed three times total (Vite dev server origin, Dockerized local origin, and
   this deployed frontend origin).

### Quota lesson learned (correcting earlier assumption)

Previously assumed F1 App Service required no cost-management attention since it's free
regardless of running state. This is only half true: **F1 apps have a real daily quota of 60
CPU-minutes**, resetting at midnight UTC — not a cost limit, but an availability one. Running
continuously for several days previously led to a `403 Quota Exceeded` state on the backend that
required deleting and recreating the app rather than simply waiting for a reset.

**Correction adopted:** both App Services (frontend and backend) are now stopped between work
sessions, the same discipline already used for the database — not for cost (F1 is free either
way) but specifically to avoid re-accumulating toward the daily CPU quota.

Confirmed via research: this quota is **per-app**, not shared across a plan — so hosting both
apps on the same `sos-mvp-plan` doesn't increase this risk. Filesystem/storage quota *is* shared
per plan, but is not a practical concern at this project's scale.

### Outcome
Full stack now live: `https://sos-semali-frontend.azurewebsites.net` (frontend) →
`https://sos-semali-backend.azurewebsites.net` (backend) → Azure PostgreSQL Flexible Server,
verified working end-to-end via a real register/login flow in a browser.