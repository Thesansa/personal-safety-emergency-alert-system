# Backend Development Architecture

The backend is developed using Spring Boot following a layered architecture.

---

# Layer Structure

## Controller Layer

### Responsibilities

- Expose REST APIs
- Validate incoming requests
- Return HTTP responses

The controller contains no business logic.

---

## Service Layer

### Responsibilities

- Business logic
- Alert lifecycle
- Authentication
- Notification coordination

This layer acts as the core of the application.

---

## Repository Layer

### Responsibilities

- Database access
- CRUD operations
- Custom database queries

Repositories interact with PostgreSQL using Spring Data JPA.

---

## DTO Layer

### Responsibilities

- Define the exact shape of data crossing the HTTP boundary (requests and responses)
- Carry validation rules (`@NotBlank`, `@Email`, `@Size`, etc.)
- Keep the API contract independent of the database schema — entity fields (e.g., `passwordHash`) never leak directly into a response

DTOs are intentionally separate classes from entities, even when their fields overlap, so the database structure and the API contract can evolve independently.

---

## Security Layer

### Responsibilities

- Authenticate incoming requests via JWT (`JwtAuthFilter`), which runs before the request reaches any controller
- Decide which routes require authentication (`SecurityConfig`) — `/register`, `/login`, and `/refresh` are public; everything else requires a valid token
- Issue and validate JSON Web Tokens (`JwtUtil`)
- Use short-lived, stateless access tokens
- Use longer-lived, database-backed and revocable refresh tokens
- Rotate refresh tokens when they are successfully used
- Revoke refresh tokens when required, including during logout
- Hash passwords one-way using BCrypt (`PasswordEncoder`) — passwords are never stored or compared in plaintext

---

## Trusted Contacts Module

### Responsibilities

- Allow an authenticated user to manage the people who should be notified in an emergency
- Enforce strict per-user ownership — a user can only view, edit, or delete their own trusted
  contacts, never another user's

### Data Model

trusted_contacts
├── id (PK)
├── user_id (FK → users)
├── name
├── contact_no
├── email (optional)
├── relation (optional)
└── priority_order


`priority_order` is user-supplied and determines the order trusted contacts are notified in
during an alert.

### Ownership Enforcement

Every read, update, and delete operation queries by both the record's ID **and** the
authenticated user's ID in a single repository call (`findByIdAndUserId`), rather than fetching
by ID alone and checking ownership afterward. If a contact exists but belongs to a different
user, the query returns nothing, and the API responds with `404 Not Found` — deliberately
indistinguishable from the contact genuinely not existing.

---

## Alert Module

### Responsibilities

- Trigger, cancel, and resolve emergency alerts
- Automatically escalate an alert if it remains unresolved past a configurable window
- Track a location trail for the duration of an active alert
- Notify trusted contacts by email on trigger and on escalation
- Log every status transition for audit purposes

### Data Model

alerts
├── id (PK)
├── user_id (FK → users)
├── status (ACTIVE, ESCALATED, RESOLVED, CANCELLED)
├── triggered_at / escalated_at / resolved_at / cancelled_at
└── resolved_by (nullable)

alert_status_history
├── id (PK)
├── alert_id (FK → alerts)
├── previous_status / new_status
├── changed_by (USER / SYSTEM)
├── changed_at
└── note

alert_locations
├── id (PK)
├── alert_id (FK → alerts)
├── latitude / longitude
└── captured_at

alert_notifications
├── id (PK)
├── alert_id (FK → alerts)
├── trusted_contact_id (FK → trusted_contacts)
├── notification_type (INITIAL / ESCALATION)
├── delivery_status (SENT / FAILED)
└── notified_at


`alert_locations` is a single continuous trail, not one row per lifecycle event — the trigger
location is simply the first entry. Cancelling or resolving an alert doesn't need its own
location field, since the trail combined with the alert's own timestamps already answers "where
was this alert at moment X."

### Escalation

A `@Scheduled` background task (`EscalationScheduler`) runs every 5 seconds, checking for
`ACTIVE` alerts older than a configurable window (`alert.escalation-window-seconds`, default 45s)
and escalating them automatically. Escalation is re-verified against the alert's *current* status
at the moment it runs (not just the scheduler's initial query), guarding against a race condition
where a user resolves or cancels an alert in the gap between the scheduler's query and the actual
escalation call.

### Notifications

Sent via a `NotificationService` interface, with `EmailNotificationService` (Gmail SMTP) as the
current implementation — this separation allows additional channels (e.g. SMS) to be added later
without changing `AlertService`. Each contact is notified independently; one failed send does not
block notifications to the others, and delivery success/failure is recorded per contact in
`alert_notifications`.

### Ownership Enforcement

Same pattern as Trusted Contacts — every user-facing alert operation uses `findByIdAndUserId`.
The one exception is the escalation scheduler itself, which acts on the system's behalf (no
`Authentication` context exists for a background task) and uses a plain `findById`.

### Known Trade-off

Notification sending currently happens synchronously, inside the same `@Transactional` boundary
as the alert's database writes. A slow SMTP response could delay the API response and hold a
database connection open for that duration. Acceptable at this project's scale; a production
version would move notification dispatch to an asynchronous/queued job.

## Database

### Responsibilities

- Store application data
- Maintain relationships
- Preserve alert history
- Persist user information
- Store hashed refresh tokens and their expiration/revocation information
- Store trusted contacts, alert lifecycle records, location trails, and notification
  delivery records
---

## Benefits

- Separation of concerns
- Easier testing
- Better maintainability
- Improved scalability

---

# Containerization (Docker)

The backend is packaged as a Docker image using a **multi-stage build**:

- **Build stage** — Uses a full Maven + JDK 21 image to compile the source code and package the application into a `.jar`.
- **Runtime stage** — Starts from a minimal JRE-only image and copies in only the finished `.jar`, discarding the build tools. This keeps the final image significantly smaller.

Configuration (`application.properties`) uses `${VARIABLE_NAME:default}` placeholder syntax throughout, allowing the same image to run locally, via Docker Compose, or on Azure. Only the runtime environment variables change — no rebuild is required when switching environments.

For **local development**, `docker-compose.yml` orchestrates two containers:

- The backend, built from the project's `Dockerfile`
- PostgreSQL, using the official pre-built Docker image

Docker Compose automatically creates the network connecting both containers.

---

# CI/CD Pipeline (GitHub Actions)

The backend uses a GitHub Actions workflow to validate and build the backend when relevant backend files are changed.

The workflow uses path-based conditions so unrelated repository changes do not unnecessarily trigger the backend workflow.

## CI Workflow Steps

1. Check out the repository
2. Set up JDK 21 (with Maven dependency caching)
3. Prepare the required application configuration without exposing secrets
4. Compile the backend
5. Run the unit test suite — covers `AuthService`, `TrustedContactService`, and `AlertService`,
   all using mocked repositories and a mocked `NotificationService`, so no live database or real
   email sending occurs during CI
6. Build the Docker image

## Continuous Deployment

The current CD process builds the backend Docker image, pushes it to GitHub Container Registry (GHCR), and triggers the Azure deployment process.

The current deployment scope is:

```text
Backend changes
      ↓
CI
      ↓
Docker image build
      ↓
Push image to GHCR
      ↓
Trigger Azure deployment