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
