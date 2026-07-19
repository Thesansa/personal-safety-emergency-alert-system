# Backend Modularization

## Purpose

The SOS backend is divided into independent modules.

Each module represents a business capability and can be developed, tested, and maintained independently while depending only on previously completed modules.

---

## Module Development Order

### 1. Authentication

Responsibilities

- User registration
- Login
- JWT authentication
- Password hashing
- Refresh token management

---

### 2. Trusted Contacts

Responsibilities

- Add trusted contacts
- Update contacts
- Delete contacts
- Retrieve contact list

Depends on:

- Authentication

---

### 3. Alert Triggering

Responsibilities

- SOS activation
- Alert creation
- Initial location capture
- ACTIVE status

Depends on:

- Authentication
- Trusted Contacts

---

### 4. Escalation

Responsibilities

- 45-second scheduler
- Status transition
- Escalation logging

Depends on:

- Alert Triggering

---

### 5. Notifications

Responsibilities

- Email notifications
- Notification tracking
- Magic link generation

Depends on:

- Escalation

---

### 6. Location Tracking

Responsibilities

- Receive browser location updates
- Store location history
- Provide latest location

Depends on:

- Alert Triggering

---

### 7. Resolution

Responsibilities

- Resolve alerts
- Cancel alerts
- Close magic links
- Update history

Depends on:

- Notifications
- Location Tracking

---

### 8. Alert History

Responsibilities

- Display alert lifecycle
- View status changes
- Audit trail

Depends on:

- All previous modules
