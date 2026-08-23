# Authentication Module API

Base URL:

```text
http://localhost:8080/api/auth
```

---

## 1. Register

**Endpoint**

```http
POST /register
```

**Full URL**

```text
http://localhost:8080/api/auth/register
```

**Headers**

```http
Content-Type: application/json
```

**Request Body**

```json
{
  "fullName": "Semali Perera",
  "email": "semali@example.com",
  "password": "SecurePass123",
  "gender": "female",
  "contactNo": "0771234567",
  "nic": "200012345678",
  "homeAddress": "Maharagama, Sri Lanka"
}
```

**Success Response**

```http
201 Created
```

---

## 2. Login

**Endpoint**

```http
POST /login
```

**Full URL**

```text
http://localhost:8080/api/auth/login
```

**Headers**

```http
Content-Type: application/json
```

**Request Body**

```json
{
  "email": "semali@example.com",
  "password": "SecurePass123"
}
```

**Success Response**

```http
200 OK
```

Returns an `AuthResponse` containing:

```json
{
  "accessToken": "<JWT_ACCESS_TOKEN>",
  "refreshToken": "<REFRESH_TOKEN>",
  "tokenType": "Bearer"
}
```

---

## 3. Refresh Access Token

**Endpoint**

```http
POST /refresh
```

**Full URL**

```text
http://localhost:8080/api/auth/refresh
```

**Headers**

```http
Content-Type: application/json
```

**Request Body**

```json
{
  "refreshToken": "9f8e7d6c...-...-...-..."
}
```

**Success Response**

```http
200 OK
```

Example Response

```json
{
  "accessToken": "<NEW_ACCESS_TOKEN>",
  "refreshToken": "<NEW_REFRESH_TOKEN>",
  "tokenType": "Bearer"
}
```

> **Note:** Refresh token rotation is enabled. After a successful refresh, the previous refresh token is revoked. Attempting to reuse it will result in a **401 Unauthorized** response.

---

## 4. Logout

**Endpoint**

```http
POST /logout
```

**Full URL**

```text
http://localhost:8080/api/auth/logout
```

**Headers**

```http
Authorization: Bearer <ACCESS_TOKEN>
```

**Success Response**

```http
204 No Content
```

---

## Response Status Codes

| Status Code | Description |
|-------------|-------------|
| **201 Created** | User registered successfully |
| **200 OK** | Login or token refresh successful |
| **204 No Content** | Logout successful |
| **400 Bad Request** | Invalid request data |
| **401 Unauthorized** | Invalid credentials or expired/revoked token |
| **409 Conflict** | User already exists |
| **500 Internal Server Error** | Unexpected server error |

## Deployed Environment (Azure)

Base URL:
```text
https://sos-semali-backend.azurewebsites.net/api/auth
```

All endpoints above work identically against this base URL.

**Before testing:** the PostgreSQL database is stopped between work sessions to conserve
Azure student credit. Please give a heads-up before testing so the database can be started
(takes 1–2 minutes to come online). The App Service itself (free F1 tier) is always available,
but may take a few extra seconds to respond on the very first request after a period of
inactivity (cold start).


# Trusted Contacts Module API

All endpoints require a valid access token.

Base URL:
```text
http://localhost:8080/api/trusted-contacts
```

---

## 1. Create Trusted Contact

**Endpoint**
```http
POST /
```
**Headers**
```http
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```
**Request Body**
```json
{
  "name": "Kamala Perera",
  "contactNo": "0771112233",
  "email": "kamala@example.com",
  "relation": "Mother",
  "priorityOrder": 1
}
```
**Success Response**
```http
201 Created
```

---

## 2. List Trusted Contacts

**Endpoint**
```http
GET /
```
**Headers**
```http
Authorization: Bearer <ACCESS_TOKEN>
```
**Success Response**
```http
200 OK
```
Returns a list of the authenticated user's contacts, ordered by `priorityOrder`.

---

## 3. Get a Single Trusted Contact

**Endpoint**
```http
GET /{id}
```
**Headers**
```http
Authorization: Bearer <ACCESS_TOKEN>
```
**Success Response**
```http
200 OK
```

---

## 4. Update Trusted Contact

**Endpoint**
```http
PUT /{id}
```
**Headers**
```http
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```
**Request Body**
```json
{
  "name": "Kamala Perera",
  "contactNo": "0771112233",
  "email": "kamala.new@example.com",
  "relation": "Mother",
  "priorityOrder": 1
}
```
**Success Response**
```http
200 OK
```

---

## 5. Delete Trusted Contact

**Endpoint**
```http
DELETE /{id}
```
**Headers**
```http
Authorization: Bearer <ACCESS_TOKEN>
```
**Success Response**
```http
204 No Content
```

---

## Response Status Codes

| Status Code | Description |
|---|---|
| **201 Created** | Contact created successfully |
| **200 OK** | List, get, or update successful |
| **204 No Content** | Delete successful |
| **400 Bad Request** | Invalid request data (e.g. missing name or contact number) |
| **401 Unauthorized** | Missing or invalid access token |
| **404 Not Found** | Contact does not exist, or does not belong to the authenticated user |

> **Note:** A `404` on a contact ID that genuinely belongs to another user is intentional — the
> API never confirms whether a given ID exists for someone else.

## Deployed Environment (Azure)

Base URL:
```text
https://sos-semali-backend.azurewebsites.net/api/trusted-contacts
```
Same endpoints and behavior as above.