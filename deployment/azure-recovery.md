## Azure Recovery (2026-07-31)

### Problem
- App Service entered QuotaExceeded state.
- Backend returned 503.
- Original App Service became unusable.

### Solution
- Deleted the faulty Web App.
- Recreated the App Service Plan.
- Recreated the Web App.
- Reconfigured GitHub Container Registry.
- Restored App Settings.
- Reconnected Azure PostgreSQL.
- Verified successful container startup.

### Result
- Spring Boot application started successfully.
- Registration and authentication worked.
- Frontend redirected users to the dashboard.
