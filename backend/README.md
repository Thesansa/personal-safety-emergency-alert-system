# Backend Development Architecture

The backend will be developed using Spring Boot following a layered architecture.

# Layer Structure


---

## Controller Layer

Responsibilities

- Expose REST APIs
- Validate incoming requests
- Return HTTP responses

The controller contains no business logic.

---

## Service Layer

Responsibilities

- Business logic
- Alert lifecycle
- Authentication
- Notification coordination

This layer acts as the core of the application.

---

## Repository Layer

Responsibilities

- Database access
- CRUD operations
- Custom database queries

Repositories interact with PostgreSQL using Spring Data JPA.

---

## Database

Responsibilities

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
