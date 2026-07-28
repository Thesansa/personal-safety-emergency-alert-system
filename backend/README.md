# Backend Development Architecture

The backend will be developed using Spring Boot following a layered architecture.

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
- Keep the API contract independent of the database schema—entity fields (e.g., `passwordHash`) never leak directly into a response

DTOs are intentionally separate classes from entities, even when their fields overlap, so the database structure and the API contract can evolve independently.

---

## Security Layer

### Responsibilities

- Authenticate every incoming request via JWT (`JwtAuthFilter`), which runs before the request reaches any controller
- Decide which routes require authentication (`SecurityConfig`)—`/register`, `/login`, and `/refresh` are public; everything else requires a valid token
- Issue and validate JSON Web Tokens (`JwtUtil`)—short-lived access tokens (stateless, no database lookup required) paired with longer-lived, database-backed, revocable refresh tokens
- Hash passwords one-way using BCrypt (`PasswordEncoder`)—passwords are never stored or compared in plaintext

---

## Database

### Responsibilities

- Store application data
- Maintain relationships
- Preserve alert history
- Persist user information

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

Configuration (`application.properties`) uses `${VARIABLE_NAME:default}` placeholder syntax throughout, allowing the same image to run locally, via Docker Compose, or on Azure. Only the runtime environment variables change—no rebuild is required when switching environments.

For **local development**, `docker-compose.yml` orchestrates two containers:

- The backend, built from the project's `Dockerfile`
- PostgreSQL, using the official pre-built Docker image

Docker Compose automatically creates the network connecting both containers.

---

# CI/CD Pipeline (GitHub Actions)

A workflow (`.github/workflows/backend-ci.yml`) runs automatically on every push and pull request targeting the `main` branch.

## Workflow Steps

1. Check out the repository
2. Install JDK 21
3. Copy `application.properties.example` to `application.properties` so the application has a valid configuration without exposing secrets
4. Run `mvn clean package`, compiling the application and executing the unit test suite
5. Build the Docker image to verify that the `Dockerfile` itself is valid

A `paths-filter` step ensures the workflow always reports a status (required for branch protection) while skipping the Docker build whenever backend files were not modified. This keeps CI execution fast for unrelated changes.

---

# Testing Strategy

Unit tests for `AuthService` (`AuthServiceTest`) use Mockito to mock every dependency:

- `UserRepository`
- `RefreshTokenRepository`
- `PasswordEncoder`
- `JwtUtil`

Because all dependencies are mocked, no real database is required. These tests verify business logic in isolation, including:

- Duplicate email rejection
- Password validation
- Refresh token rotation
- Refresh token expiration and revocation
- Logout functionality

This approach ensures tests run quickly and consistently both locally and in CI.

## Removed Test

The default Spring Initializr test (`SosBackendApplicationTests`) was removed because it attempted to start the full Spring application context, including a real PostgreSQL connection.

Although this worked locally, it failed on GitHub Actions because the CI runner does not include a running database. Since the test only verified application startup and did not exercise business logic, it was replaced with meaningful unit tests.

Future modules will follow the same pattern:

- Mock the repository layer
- Test service-layer business logic
- Avoid requiring a live database during CI

---

# Deployment (Azure)

The backend is deployed to **Azure App Service** (Linux, F1 Free Tier) as a Docker container pulled from **GitHub Container Registry (GHCR)** instead of Azure Container Registry (ACR). GHCR was chosen to avoid the ongoing cost of ACR while still being fully supported by Azure App Service.

Application secrets, including:

- Database credentials
- JWT signing key

are provided through **Azure App Settings** at runtime. They are never stored inside the Docker image and use the same `${VARIABLE_NAME:default}` configuration mechanism used for local development.

The database is hosted on **Azure Database for PostgreSQL (Flexible Server, Burstable Tier)**. The rationale, regional considerations, and cost trade-offs are documented separately in `deployment/decisions.md`.

HTTPS is enforced on the deployed App Service. The complete API reference and production base URL are documented in `docs/api.md`.
