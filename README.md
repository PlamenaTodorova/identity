# Identity

Central authentication and account service for [Adjutant](../adjutant). Handles user registration, login, JWT issuance, and per-user app preferences (which Adjutant apps are enabled).

## Why this service exists

Adjutant is a frontend hub for multiple backend apps (Bingable, Glutton, Bookworm, and others). Those backends currently trust a manually entered `X-User-Id` header. Identity replaces that with real accounts and signed access tokens.

Responsibilities:

- **Authentication** — register, login, refresh, logout
- **Identification** — issue JWT access tokens with the authenticated user id
- **User preferences** — store which Adjutant apps each user has enabled

Other backends can continue accepting `X-User-Id` during migration, but should eventually validate JWTs issued by this service.

## Tech stack

- Java 21
- Spring Boot 3.5
- Spring Security (stateless JWT)
- MySQL
- Gradle multi-module layout (`core` + `api`), matching the Bingable project structure

## Getting started

### 1. Create the database

```bash
mysql -u root -p < schema.sql
```

Or let Spring create the database automatically and run the SQL manually if tables are missing.

Create a dedicated MySQL user if you prefer:

```sql
CREATE USER 'identity'@'localhost' IDENTIFIED BY 'identity';
GRANT ALL PRIVILEGES ON identity.* TO 'identity'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure JWT secret

Set a strong secret in production (minimum 32 characters for HS256):

```bash
export IDENTITY_JWT_SECRET="replace-with-a-long-random-secret-at-least-32-chars"
```

### 3. Run the API

```bash
./gradlew :api:bootRun
```

The service listens on `http://localhost:8082`.

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- Health: `http://localhost:8082/actuator/health`

## API overview

### Public auth endpoints

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/auth/register` | Create account and return tokens |
| `POST` | `/auth/login` | Authenticate and return tokens |
| `POST` | `/auth/refresh` | Rotate refresh token and return new access token |
| `POST` | `/auth/logout` | Revoke refresh token |

### Protected user endpoints

Send `Authorization: Bearer <accessToken>`.

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/users/me` | Current user profile |
| `GET` | `/users/me/preferences` | Enabled/disabled Adjutant apps |
| `PUT` | `/users/me/preferences` | Update app preferences |

### Example: register

```bash
curl -s http://localhost:8082/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "alice@example.com",
    "password": "password123",
    "displayName": "Alice"
  }'
```

### Example: update preferences

```bash
curl -s http://localhost:8082/users/me/preferences \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "apps": [
      { "appId": "bingable", "enabled": true },
      { "appId": "glutton", "enabled": false }
    ]
  }'
```

Known app ids are configured in `application.yml` under `identity.apps.known`.

## Integration with Adjutant

Recommended next steps for the frontend:

1. Add a login/register screen that calls `/auth/login` and `/auth/register`.
2. Store the access and refresh tokens securely (memory + httpOnly cookie, or secure storage for local dev).
3. Replace the manual User ID field in the sidebar with the authenticated user from `/users/me`.
4. Send `Authorization: Bearer <token>` to Identity and continue sending `X-User-Id: <user.id>` to existing backends during migration.
5. Load `/users/me/preferences` to filter the sidebar apps list.

Suggested Adjutant dev proxy:

```ts
'/api/identity': {
  target: 'http://localhost:8082',
  changeOrigin: true,
  rewrite: (path) => path.replace(/^\/api\/identity/, ''),
}
```

## Integration with backend services

Short term (compatible with current Bingable/Glutton code):

- Adjutant reads `user.id` from the JWT payload or `/users/me`.
- Adjutant sends that id as `X-User-Id` to downstream APIs.

Long term (more secure):

- Each backend validates the JWT using the shared `IDENTITY_JWT_SECRET` and issuer `adjutant-identity`.
- Extract `sub` (user id) from the token instead of trusting a raw header.

Future upgrade path: switch from HS256 shared secret to RS256 with a JWKS endpoint so backends only need the public key.

## Project structure

```
identity/
  core/     Domain models, repositories, auth and preference services
  api/      REST controllers, JWT filter, Spring Security config
  schema.sql
```

## Scripts

- `./gradlew test` — run unit tests
- `./gradlew :api:bootRun` — start the API locally
- `./gradlew :api:bootJar` — build runnable jar
