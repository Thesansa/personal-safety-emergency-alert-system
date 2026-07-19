# Database Flow

## Purpose

This document describes how data flows through the database during the complete lifecycle of an emergency alert, from user authentication to alert resolution.

---

# 1. User Login

The user authenticates using their email and password.

The backend verifies the password against the stored `password_hash` in the `users` table.

If authentication succeeds:

- A short-lived JWT access token is generated.
- A long-lived refresh token is generated.
- The refresh token hash is stored in the `refresh_tokens` table.
- Both tokens are returned to the client.

---

# 2. SOS Trigger

When the user presses the SOS button:

- A new record is created in the `alerts` table.
- The alert status is set to `ACTIVE`.
- The trigger timestamp is recorded.
- An initial entry is added to `alert_status_history`.

The browser immediately captures the user's current location.

The location is stored in `alert_locations`.

---

# 3. Initial Notifications

The system retrieves all trusted contacts ordered by their priority.

For each trusted contact:

- A notification record is created.
- A unique UUID access token (magic link) is generated.
- The notification is delivered via email.
- Delivery status is updated.

---

# 4. Escalation

If the alert remains unresolved after 45 seconds:

- The alert status changes to `ESCALATED`.
- The escalation timestamp is recorded.
- A new status history record is inserted.
- Escalation notifications are sent.

---

# 5. Continuous Location Tracking

While the alert remains ACTIVE or ESCALATED:

- The browser periodically sends location updates.
- Each location is stored as a new record in `alert_locations`.

---

# 6. Trusted Contact Access

Trusted contacts access the alert using the secure magic link.

The backend:

- Validates the access token.
- Confirms the alert is still active.
- Returns the latest location information.

Access activity may be logged for auditing purposes.

---

# 7. Alert Resolution

The alert may be resolved by:

- The user
- A trusted contact

The backend:

- Updates the alert status.
- Records the resolution timestamp.
- Creates a new status history entry.

Once resolved, all magic links become inactive.

---

# 8. Logout

When the user logs out:

- The refresh token is revoked.
- The access token naturally expires after its short lifetime.
