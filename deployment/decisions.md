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
