# Architecture, Security & Deployment Decisions

This document records the key architectural, security, and deployment decisions made during the development of the Personal Safety Emergency Alert System MVP.

Each decision explains the reasoning behind the selected approach, its benefits, known MVP trade-offs, and possible future improvements.

---

# Decision 1 — Magic Link Authentication

## Decision

Trusted contacts will access active alerts using secure one-time magic links instead of requiring user accounts.

## Reason

During emergencies, trusted contacts should be able to access an active alert immediately without registration or authentication.

Reducing friction during a safety-critical situation is prioritised over requiring authenticated access.

## Security Measures

- Random UUID access token
- Token stored in the database
- Token linked to a specific notification
- Token becomes invalid once the alert is resolved
- Optional access logging

## Known MVP Trade-off

A trusted contact could forward the magic link to another person.

This risk is considered acceptable for the MVP to prioritise rapid emergency access.

Future versions may introduce authenticated trusted contact accounts.

---

# Decision 2 — JWT Authentication with Refresh Tokens

## Decision

The backend uses short-lived JWT Access Tokens together with Refresh Tokens.

## Reason

Using only JWT access tokens would require users to log in again every time the access token expires.

Refresh tokens provide a better user experience while maintaining security through short-lived access tokens.

## Implementation

- Short-lived JWT Access Token
- Long-lived Refresh Token
- Refresh Tokens stored as hashes in the database
- Refresh Token rotation on every refresh request
- Refresh Token revocation during logout
- Automatic access token renewal through a React Axios interceptor

## Benefits

- Improved user experience
- Better security than long-lived JWTs
- Session revocation support
- Supports multiple active sessions
- Stateless API authentication

## Trade-offs

- Additional authentication logic
- Refresh token storage and rotation
- Slightly increased implementation complexity

## Known Security Considerations

The `/api/auth/refresh` endpoint authenticates solely through possession of a valid refresh token.

Its security relies on:

- HTTPS in non-local environments
- Refresh Tokens stored in HttpOnly cookies (planned frontend implementation)
- Token rotation
- Token revocation on logout
- Refresh Tokens stored as hashes instead of plain text

Device binding and IP validation are not implemented in the MVP.

---

# Decision 3 — Database Schema Management

## Decision

Hibernate automatic schema generation is used during MVP development.

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Reason

The database schema is expected to change frequently during early development.

Automatically synchronising entity classes with the database significantly accelerates development.

## Benefits

- Faster prototyping
- Simplified development
- Reduced manual SQL maintenance
- Database schema stays synchronised with JPA entities

## Known MVP Trade-off

Hibernate schema generation is not appropriate for production environments because schema changes are not version-controlled.

Future versions should migrate to Flyway (or Liquibase) migrations.

---

# Decision 4 — Secrets Management

## Decision

Sensitive configuration values are excluded from version control.

## Protected Secrets

- JWT Secret
- Database Password
- Email Credentials
- API Keys
- Other sensitive configuration

## Implementation

Development secrets are stored locally using configuration files or environment variables.

Sensitive files are excluded using `.gitignore`.

Only example configuration files are committed.

## Benefits

- Prevents accidental credential exposure
- Supports multiple deployment environments
- Follows secure DevOps practices

---

# Decision 5 — Rate Limiting

## Decision

Rate limiting will not be implemented during the MVP.

## Reason

The current priority is completing the authentication workflow and emergency alert functionality.

Proper rate limiting requires additional infrastructure such as:

- Bucket4j
- Redis
- API Gateway
- Reverse Proxy configuration

These are intentionally deferred until after the MVP.

## Known MVP Trade-off

Authentication endpoints remain vulnerable to brute-force login attempts.

## Future Enhancement

Possible implementations include:

- IP-based request limiting
- User-based login throttling
- Temporary account lockout
- Distributed rate limiting using Redis

---

# Decision 6 — Dockerised Local Development

## Decision

The backend and PostgreSQL database are containerised using Docker Compose.

## Reason

Running application services inside containers provides a consistent development environment independent of the host operating system.

Docker Compose also simplifies service orchestration by starting all required services together.

## Implementation

Docker Compose is responsible for:

- Building the Spring Boot backend image
- Pulling the PostgreSQL image
- Creating an isolated Docker network
- Creating persistent database volumes
- Performing PostgreSQL health checks
- Starting the backend after PostgreSQL becomes healthy
- Allowing service-to-service communication using Docker DNS

The backend communicates with PostgreSQL using the service name:

```
postgres
```

instead of

```
localhost
```

## Benefits

- Consistent development environments
- Simplified project setup
- Environment isolation
- Persistent database storage
- Production-like networking
- Easy onboarding for contributors

## Known MVP Trade-off

The current Docker Compose configuration is intended for local development only.

Future production deployments will require:

- Environment-specific Compose files
- Secrets management
- Reverse proxy
- HTTPS
- Container orchestration

---

# Decision 7 — Layered Backend Architecture

## Decision

The backend follows Spring Boot's layered architecture.

```
Controller
      ↓
Service
      ↓
Repository
      ↓
PostgreSQL
```

## Reason

Separating responsibilities improves maintainability, readability, and testing.

Each layer has a single responsibility.

## Layer Responsibilities

### Controller

- REST API endpoints
- Request validation
- HTTP responses

### Service

- Business logic
- Authentication
- Alert lifecycle
- Notification coordination

### Repository

- Database access
- CRUD operations
- Custom queries

### Database

- Persistent application data
- Relationships
- Alert history
- User information

## Benefits

- Separation of concerns
- Easier testing
- Better maintainability
- Improved scalability
- Easier future refactoring
