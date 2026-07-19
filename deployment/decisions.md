# Deployment & Security Decisions

This document records important architectural decisions made during the development of the SOS MVP.

---

# Decision 1 — Magic Link Authentication

## Decision

Trusted contacts will access active alerts using secure one-time magic links instead of requiring user accounts.

## Reason

During emergencies, trusted contacts should access the alert immediately without registration or login.

Reducing friction is more important than requiring authentication.

## Security Measures

- Random UUID access token
- Token stored in the database
- Token linked to a specific notification
- Token becomes invalid once the alert is resolved
- Optional access logging

## Known MVP Limitation

A trusted contact could forward the link to another person.

This trade-off is accepted for the MVP to prioritize rapid emergency access.

Future versions may introduce authenticated trusted contact accounts.

---

# Decision 2 — JWT Authentication

## Decision

Users authenticate using JWT access tokens with refresh tokens.

## Reason

JWT provides stateless authentication while refresh tokens allow long-lived user sessions without requiring repeated logins.

## Implementation

- Short-lived access token
- Long-lived refresh token
- Refresh token stored as a hash
- Refresh token revocation on logout

## Benefits

- Better security
- Reduced server state
- Improved user experience
- Supports scalable deployments

# Decision 3 – Database Schema Management

## Decision

The project will use Hibernate's automatic schema generation during MVP development by configuring:

```properties
spring.jpa.hibernate.ddl-auto=update
```

## Reason

Automatic schema generation allows the database schema to be created and updated directly from the application's entity classes. This enables rapid development without manually writing SQL migration scripts.

Since the project is an MVP and the database design is expected to change frequently, this approach allows faster iteration while learning Spring Boot.

## Benefits

- Faster development and prototyping
- No manual SQL migrations during early development
- Database schema stays synchronized with JPA entity classes
- Simplifies learning and experimentation

## Known MVP Trade-off

Hibernate schema generation is not recommended for production environments because schema changes are not version-controlled or reviewable.

Future versions of the project should migrate to **Flyway** (or Liquibase), where database changes are managed through version-controlled SQL migration scripts.

---

# Decision 4 – Secrets Management

## Decision

Sensitive application configuration will not be committed to the Git repository.

This includes:

- JWT signing secret
- Database password
- Email credentials
- API keys
- Other sensitive configuration values

## Reason

Secrets committed to Git remain in the repository history even if they are deleted later, creating a permanent security risk.

Separating configuration from source code follows secure development practices and makes deployment to different environments much easier.

## Implementation

During development:

- Sensitive values will be stored in a local configuration file (such as `application-local.properties`) or provided through environment variables.
- Local configuration files containing secrets will be ignored using `.gitignore`.
- Only placeholder or example configuration files will be committed to the repository.

## Benefits

- Prevents accidental exposure of credentials
- Supports different configurations for development and production
- Aligns with secure DevOps practices

---

# Decision 5 – Rate Limiting

## Decision

Rate limiting will not be implemented during the MVP.

## Reason

The current focus is to complete the authentication flow and core emergency alert functionality before introducing infrastructure-level security controls.

Implementing rate limiting correctly typically requires additional technologies such as:

- Bucket4j
- Redis
- API Gateway
- Reverse proxy configuration

These technologies introduce additional complexity that is outside the scope of the MVP.

## Known MVP Trade-off

Without rate limiting, authentication endpoints remain vulnerable to brute-force login attempts.

This limitation is accepted for the MVP to prioritize delivering the complete emergency alert workflow.

## Future Enhancement

Future versions of the system may implement:

- IP-based request limiting
- User-based login throttling
- Temporary account lockout after repeated failed login attempts
- Distributed rate limiting using Redis or an API Gateway
