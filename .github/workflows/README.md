# CI Workflows

Two independent GitHub Actions workflows validate this repository: `backend-ci.yml` and `frontend-ci.yml`. Both live in `.github/workflows/` and follow the same overall design pattern.

## Why Two Separate Workflows?

The backend and frontend use entirely different toolchains (Maven/JDK vs. npm/Node) and evolve independently. Keeping them separate means a frontend-only change does not incur the cost of a full Maven and Docker build, while backend-only changes do not trigger unnecessary Node.js jobs.

## The Shared Pattern: Always-Run, Conditionally-Executing Steps

Both workflows trigger on every `push` and `pull_request` targeting `main`, regardless of which files changed.

This is a deliberate design choice. GitHub branch protection rules can only require checks **by name**—they cannot express "require this check only when relevant." If a workflow were restricted using top-level `paths` filters, a pull request that did not modify those paths would never produce a status check. If that check were marked as required, the pull request could never be merged because GitHub would wait indefinitely for a check that never ran.

To avoid this, each workflow always starts and always reports a pass/fail result, satisfying branch protection. A `dorny/paths-filter` step then determines whether the expensive build steps should actually execute.

Example:

```yaml
- name: Check for backend changes
  uses: dorny/paths-filter@v3
  id: filter
  with:
    filters: |
      backend:
        - 'backend/sos-backend/**'
```

Subsequent steps are conditioned on the filter output:

```yaml
- name: Build with Maven
  if: steps.filter.outputs.backend == 'true'
  ...
```

As a result:

- Documentation-only pull requests complete both **Backend CI** and **Frontend CI** within seconds, with build steps skipped.
- Backend changes execute only the backend pipeline.
- Frontend changes execute only the frontend pipeline.

## `backend-ci.yml`

The backend workflow performs the following steps:

1. Checkout the repository.
2. Run a path filter against `backend/sos-backend/**`.
3. Set up JDK 21.
4. Copy `application.properties.example` into place so the project has a valid, secret-free configuration during the build.
5. Run:

   ```bash
   mvn clean package
   ```

   This compiles the application and executes the unit test suite (including `AuthServiceTest`), using mocked repositories without requiring a live database.

6. Build the Docker image to verify that the `Dockerfile` itself is valid and reproducible.

## `frontend-ci.yml`

The frontend workflow performs the following steps:

1. Checkout the repository.
2. Run a path filter against `frontend/**`.
3. Set up Node.js 20.
4. Install dependencies using:

   ```bash
   npm ci
   ```

   Unlike local Windows development (where a cross-platform lockfile inconsistency was encountered), CI always runs on a consistent Linux environment, making `npm ci` safe and deterministic.

5. Run:

   ```bash
   npm run lint
   ```

6. Build the production application:

   ```bash
   npm run build
   ```

## Current Limitations

The workflows intentionally focus on continuous integration rather than continuous deployment.

- **No automated deployment (CD).** Successful builds do not automatically deploy to Azure. Deployment is currently performed manually by building the Docker image, pushing it to GitHub Container Registry (GHCR), and restarting the Azure container so it pulls the latest image.
- **No integration tests.** The backend and frontend are validated independently. End-to-end verification of the complete frontend → backend → PostgreSQL flow is currently performed manually against the deployed Azure environment.