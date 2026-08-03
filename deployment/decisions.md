# Deployment Decisions

This document covers the full path from provisioning the database through deploying both the
backend and frontend, including the constraints hit along the way and why each choice was made.

## Why Azure at all
This project is part of an IEEE Young Protégé DevOps learning track with a mentor, using an
Azure for Students subscription provided for that purpose. Alternative platforms (Render,
Railway) were considered and are genuinely simpler for a plain student side project — but were
deliberately not used here, since the manual, hands-on Azure setup (resource groups, firewall
rules, App Service configuration) *is* the intended learning outcome for this specific program,
not overhead to avoid.

## Region selection — constrained, not chosen freely
**Attempted:** `eastus`, then `eastus2`, `centralus` — all rejected.
**Found:** Azure for Students subscriptions are restricted to a small, account-specific set of
allowed regions, surfaced via the Azure Portal's Policy → Authoring → Assignments →
"Allowed resource deployment regions" screen (not queryable via the standard "Allowed locations"
policy name).
**This account's allowed regions:** `indonesiacentral`, `centralindia`, `austriaeast`,
`malaysiawest`, `eastasia`.
**Decision:** `centralindia` — geographically closest to Sri Lanka among the allowed options.
All resources (database, both App Services) are provisioned here.

## Resource provider registration
New Azure subscriptions don't have every service namespace active by default. Creating the
Postgres server first failed with `MissingSubscriptionRegistration` until
`Microsoft.DBforPostgreSQL` was explicitly registered; `Microsoft.Web` and
`Microsoft.ContainerRegistry` were registered proactively at the same time. Registering a
provider is a permissions flag, not a resource — it draws no cost.

## Database
**Chosen:** PostgreSQL Flexible Server, Burstable tier (`Standard_B1ms`), 32 GB storage — the
current generation of Azure's Postgres offering, at its cheapest compute tier.
**Firewall:** two rules — `AllowAllAzureServicesAndResourcesWithinAzureIps` (lets the backend
App Service reach it) and `AllowMyIP` (direct local `psql` testing).
**Cost habit:** stopped between work sessions to conserve the $100 student credit, since the
server draws credit continuously while running regardless of load. Azure auto-restarts a
stopped server after 7 days regardless — expected, not a leak.

## Container registry — ACR vs. GHCR
**Rejected ACR:** no free tier at any size (Basic ≈ $0.167/day, continuous draw on credit).
**Chosen: GitHub Container Registry (GHCR)** — free at this project's scale, reuses existing
GitHub credentials. Confirmed Azure App Service (including F1) can pull from any registry, not
just ACR, before committing to this path.
**Trade-off accepted:** GHCR packages default to private, so both App Services need explicit
username + PAT credentials to pull — more manual than ACR's native Azure integration, but a
reasonable trade for the cost savings.

## Hosting tier and quota behavior
**Chosen:** App Service Plan, Linux, **F1 (Free)** tier — confirmed via Microsoft's own
documentation to support custom Docker containers before committing to this path.
**Real limitation, discovered (not merely theoretical):** F1 apps have a genuine daily quota of
**60 CPU-minutes**, resetting at midnight UTC — not a cost limit, but an availability one.
Running the backend continuously for several days led to a `403 Quota Exceeded` state requiring
the app to be deleted and recreated. **Correction adopted:** both App Services are now stopped
between work sessions, the same discipline as the database, specifically to avoid
re-accumulating toward this quota — not for cost, since F1 is free regardless of state.
**Plan sharing:** confirmed the CPU-minute quota is per-app, not pooled across a plan — hosting
both frontend and backend on one shared `sos-mvp-plan` does not increase this risk.

## Secrets management
No secrets are baked into either Docker image. Both ship with only placeholder/template
configuration (`application.properties.example` for the backend; Vite's build-time `ARG`/`ENV`
mechanism for the frontend). Real values — database credentials, JWT signing key — are injected
via **Azure App Service Application Settings** at runtime for the backend, and via
`--build-arg` at image-build time for the frontend (a necessary difference: Vite bakes
environment variables into the compiled JS at build time, not at container startup, so changing
the frontend's API URL requires rebuilding the image, not just changing a running container's
environment).

**Incidents handled:** a GHCR Personal Access Token was briefly exposed in plaintext during
backend setup — revoked and rotated immediately on discovery. Applied the same standard
consistently across the project (also done once for a local Postgres password).

## CORS
Spring Security requires an exact origin match, including port. Three origins are currently
allowed on the backend, added incrementally as each new frontend environment came online:
- `http://localhost:5173` — Vite dev server
- `http://localhost:8081` — Dockerized frontend, tested locally via Docker Compose
- `https://sos-semali-frontend.azurewebsites.net` — the deployed frontend

Each addition required a full backend redeploy (rebuild → push to GHCR → restart App Service),
performed three times total over the course of this project — a manual step that automating via
CD would remove going forward.

## Current state
- **Backend:** live at `https://sos-semali-backend.azurewebsites.net`, container pulled from a
  private GHCR image, connected to the managed Azure PostgreSQL instance.
- **Frontend:** live at `https://sos-semali-frontend.azurewebsites.net`, same GHCR + App Service
  pattern, sharing the backend's App Service Plan.
- **Full stack verified working end-to-end**, live, via a real register/login flow in a browser.
- **No CD yet** — every deployment (backend or frontend) is currently a manual
  `docker build` → `docker push` → Azure CLI restart/config sequence. This is the next planned
  step, per mentor guidance.
- **Cost:** minimal draw against the $100 student credit; a $5 budget alert configured as an
  early warning (not a hard stop — the subscription's own $100 spending limit is the actual hard
  stop, requiring no card on file).