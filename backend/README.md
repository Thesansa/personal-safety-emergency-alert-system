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

## Database

### Responsibilities

- Store application data
- Maintain relationships
- Preserve alert history
- Persist user information
- Store hashed refresh tokens and their expiration/revocation information

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
2. Set up JDK 21
3. Prepare the required application configuration without exposing secrets
4. Restore/cache Maven dependencies
5. Compile the backend
6. Run the unit test suite
7. Build the Docker image

The workflow is designed to keep CI execution efficient by avoiding unnecessary backend builds when only unrelated parts of the repository are changed.

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