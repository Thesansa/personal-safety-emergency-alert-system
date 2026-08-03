# Continuous Deployment (CD)

This covers how automated deployment to Azure was set up, including a genuine organizational
permission blocker hit along the way and the two pivots that led to the final, working approach.

## Goal
Every merge to `main` should automatically rebuild the affected image(s), push them to GHCR, and
trigger Azure to redeploy — replacing the fully manual `docker build` → `docker push` →
`az webapp restart` cycle that had been performed by hand up to this point (three times, for the
backend alone, across the CORS-origin updates).

## Attempt 1 — Azure Service Principal (blocked)

The standard approach for GitHub Actions to authenticate to Azure is a **Service Principal** — a
dedicated, scoped credential created via:
```bash
az ad sp create-for-rbac --name "github-actions-sos-deploy" --role contributor \
  --scopes /subscriptions/<id>/resourceGroups/sos-mvp-rg --json-auth
```

**Blocked with:** `Insufficient privileges to complete the operation.`

**Root cause:** this Azure for Students subscription is tied to the university's Azure AD tenant
(NSBM). Creating a Service Principal requires registering a new application in that tenant —
a permission commonly restricted to IT administrators in organizational tenants specifically to
prevent students/staff from creating arbitrary app registrations. This is a genuine, intentional
org-level policy, not a subscription limitation or a mistake in the command — and not something
worth pursuing a fix for given the project timeline (it would require going through university
IT, with no guarantee of approval).

## Attempt 2 — Publish Profiles + `azure/webapps-deploy` (partially blocked)

**Pivot:** Azure App Service supports a **Publish Profile** — an XML credential tied directly to
the App Service resource itself, generatable with existing account permissions, no Azure AD app
registration involved:
```bash
az webapp deployment list-publishing-profiles --resource-group sos-mvp-rg \
  --name sos-semali-backend --xml
```
Stored as GitHub Secrets (`AZURE_WEBAPP_PUBLISH_PROFILE_BACKEND` /
`_FRONTEND`), used with the official `azure/webapps-deploy@v3` action.

**Two further issues hit and resolved along the way:**

1. **GHCR push denied:** `denied: installation not allowed to Write organization package`.
   Even with repo-level "Read and write permissions" enabled for Actions, each GHCR package
   has its own independent access control list, separate from repo settings. Fixed by explicitly
   granting the repository **Write** access under each package's own
   Settings → **Manage Actions access**.

2. **`azure/webapps-deploy@v3` itself failed:** `Error: Failed to get app runtime OS` — occurred
   even with both App Services confirmed running (ruling out the F1 quota/stopped-app
   possibility first, before concluding this was the actual cause). This is a known,
   documented issue with this action specifically against container-based Linux Web Apps,
   where it cannot reliably query certain runtime metadata.

## Attempt 3 — Azure Container Webhook (working solution)

Rather than continue debugging a third-party action with a known reliability issue, switched to
Azure's own **built-in container CI/CD webhook** — arguably simpler and more directly suited to
this exact scenario than the action-based approach:

```bash
az webapp deployment container config --enable-cd true \
  --resource-group sos-mvp-rg --name sos-semali-backend
```

This returns a `CI_CD_URL` — a single authenticated endpoint that, when POSTed to, tells App
Service to pull the latest image and restart. No Service Principal, no publish profile, no
third-party action — just an HTTP call:

```yaml
- name: Trigger Azure deployment
  run: curl -X POST "${{ secrets.AZURE_BACKEND_CD_WEBHOOK }}"
```

Stored as `AZURE_BACKEND_CD_WEBHOOK` / `AZURE_FRONTEND_CD_WEBHOOK` GitHub Secrets — one per app,
generated the same way.

**One transcription bug hit and fixed:** an initial paste of the backend's webhook URL was
missing a hyphen (`sos-semalibackend` instead of `sos-semali-backend`), causing a DNS resolution
failure (`Could not resolve host`). Fixed by regenerating and re-copying the URL carefully, then
updating the secret.

## Final pipeline shape

Both `backend-ci.yml` and `frontend-ci.yml` now have a `deploy` job, gated to only run on an
actual merge to `main` (not on pull requests):
```yaml
if: github.event_name == 'push' && github.ref == 'refs/heads/main'
```
and depending on the `build` job succeeding first (`needs: build`) — broken code cannot trigger
a deployment.

Each `deploy` job:
1. Logs into GHCR using the automatic, built-in `secrets.GITHUB_TOKEN` — no personal access
   token needed for this step at all, an improvement over the manual process which relied on a
   personal PAT.
2. Rebuilds and pushes the image.
3. POSTs to the app's Azure container webhook, triggering an automatic pull-and-restart.

## Outcome
A merge to `main` now results in a fully automatic redeploy of the affected service(s) — frontend
and backend pipelines operate independently, each only deploying when its own relevant files
change (via the existing `paths-filter` step already in place from the CI setup). Verified
working end-to-end: a real code change, merged, resulted in the live Azure URLs serving the
updated version without any manual intervention.

**Operational note carried over from the F1 quota lesson:** both App Services must be in a
`Running` state for a deploy to succeed — a stopped app (per the cost-hygiene habit adopted
earlier) cannot be redeployed to until started again.

## Process note

The workflow file changes for this CD setup were pushed directly to `main`, rather than through
the usual feature-branch → PR → CI-check flow used for the rest of this project. Branch
protection was already temporarily disabled per earlier mentor guidance (while the repo
structure was still evolving), which is what made this possible without being blocked — but the
branch/PR habit itself was skipped here by choice/oversight, not by necessity. Worth returning to
the standard flow for subsequent changes, and worth re-enabling branch protection once the repo
structure is confirmed stable, per the original plan.