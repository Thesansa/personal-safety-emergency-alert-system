# Frontend — Personal Safety Emergency Alert System

React frontend for the Personal Safety Emergency Alert System, currently covering the Authentication module (Register, Login, Dashboard). Built with Vite and styled with Tailwind CSS using the project's custom color palette (calm blue as primary, red reserved exclusively for SOS/emergency states).

---

## Status
- ✅ Register, Login, Dashboard pages — wired to the backend API
- ✅ Authentication token handling
- ✅ Automatic access-token refresh using Axios interceptor, including concurrent-request queuing
  to prevent competing refresh calls
- ✅ Refresh-token rotation handling
- ✅ Trusted Contacts module — list, add, edit, delete, wired to the backend API
- ✅ Dockerized (multi-stage build: Node → Nginx)
- ✅ Integrated into `docker-compose.yml` alongside backend + PostgreSQL
- ⬜ SOS trigger UI — next application module

---

## Tech Stack

- React 19 + Vite
- Tailwind CSS v4 (CSS-based `@theme` configuration, not the older `tailwind.config.js` approach)
- React Router (client-side routing)
- Axios (API calls and authentication interceptors)
- Lucide React (icons)

---

## Project Structure

```text
src/
├── api/
│   ├── axios.js               # Axios instance, base URL, and auth interceptors
│   └── trustedContacts.js     # Trusted Contacts API calls
├── context/
│   └── AuthContext.jsx        # Centralized authentication state
├── pages/
│   ├── Login.jsx
│   ├── Register.jsx
│   ├── Dashboard.jsx
│   └── TrustedContacts.jsx    # List, add, edit, delete trusted contacts
├── App.jsx                    # Route definitions
├── main.jsx                   # App entry point, wraps App in BrowserRouter
└── index.css                  # Tailwind import + custom theme color definitions
```