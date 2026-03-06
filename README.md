# hofmann-elimination-example

A standalone Dropwizard application demonstrating [hofmann-elimination](https://github.com/codeheadsystems/hofmann-elimination) (OPAQUE password-authenticated key exchange) with a simple notes CRUD app.

## Features

- User registration and login via OPAQUE (RFC 9807 — server never sees the password)
- JWT-protected notes REST API
- Single-page web UI using the `hofmann-typescript` browser client
- Dagger 2 dependency injection
- SQL persistence (H2 + JDBI3) for credentials and notes

## Requirements

- Java 21+
- Node.js 20+
- Gradle wrapper included

## Quick Start

```bash
# Build fat jar (also compiles and bundles the frontend)
./gradlew shadowJar

# Run
java -jar build/libs/hofmann-elimination-example-1.0.0-SNAPSHOT-all.jar server src/main/resources/example.yml

# Open
open http://localhost:8080
```

## Frontend Development (hot reload)

```bash
# Terminal 1 — backend
java -jar build/libs/hofmann-elimination-example-1.0.0-SNAPSHOT-all.jar server src/main/resources/example.yml

# Terminal 2 — Vite dev server (proxies /api to :8080)
cd frontend
npm install
npm run dev
# open http://localhost:5173
```

## Installation Setup

**Do not run this application in production with the demo secrets in `example.yml`.** The demo values are public and shared by everyone who downloads this code. Anyone who knows them can forge tokens, derive the same OPRF outputs, and impersonate users.

Before deploying, generate your own values for each secret:

```bash
openssl rand -hex 32
```

Run that command once per secret and set the result in `example.yml`:

### `serverKeySeedHex`

Derives the server's long-term OPAQUE keypair. If this changes, all existing user registrations become invalid — clients will get an authentication error on login and must re-register. Keep it fixed across restarts and back it up securely.

### `oprfSeedHex`

Derives the per-user OPRF keys used during registration and login. If this changes, all existing registrations become invalid for the same reason as above. Must stay fixed across restarts.

### `oprfMasterKeyHex`

Signs the standalone OPRF responses at `/api/oprf`. If this changes, any OPRF tokens issued before the change can no longer be verified. For the notes demo this endpoint is unused, but generate a unique value anyway.

### `jwtSecretHex`

Signs the JWT bearer tokens issued after successful login. If this changes, all currently-logged-in users will be logged out (their tokens become invalid). A random secret is generated on each startup if this field is omitted, which means every restart logs everyone out.

### `context`

A plain-text string bound into every OPAQUE registration. Client and server must agree on the same value, and changing it invalidates all existing registrations (same effect as changing the seeds). Choose something unique to your deployment, e.g. `myapp-prod-v1`. Never reuse a context string after re-generating your seeds.

### Summary

| Field | What breaks if changed | Must survive restart? |
|-------|------------------------|-----------------------|
| `serverKeySeedHex` | All registrations | Yes |
| `oprfSeedHex` | All registrations | Yes |
| `oprfMasterKeyHex` | Standalone OPRF tokens | Yes |
| `jwtSecretHex` | Active sessions (users logged out) | Recommended |
| `context` | All registrations | Yes |

### Production checklist

- [ ] Replace all five demo hex values with freshly generated ones (`openssl rand -hex 32`)
- [ ] Set a strong `databasePassword` (or use a production database instead of H2)
- [ ] Back up the `serverKeySeedHex`, `oprfSeedHex`, and `context` values — losing them means all users must re-register
- [ ] Back up the `./data/` directory — it contains the H2 database with credentials and notes
- [ ] Do not commit `example.yml` with real secrets to version control

## API Endpoints

All JAX-RS resources are mounted at `/api` (`server.rootPath: /api/*`).

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | /api/opaque/config | — | OPAQUE server config (cipher suite, KSF params) |
| POST | /api/opaque/registration/start | — | Registration step 1 |
| POST | /api/opaque/registration/finish | — | Registration step 2 |
| POST | /api/opaque/auth/start | — | Login step 1 |
| POST | /api/opaque/auth/finish | — | Login step 2 — returns JWT |
| DELETE | /api/opaque/registration | Bearer | Delete account |
| GET | /api/notes | Bearer | List your notes |
| POST | /api/notes | Bearer | Create a note |
| DELETE | /api/notes/{id} | Bearer | Delete a note |

## Moving this app

When moving out of the `hofmann-elimination` parent directory, update `frontend/package.json` to point to the published npm package instead of the local file reference:

```json
"hofmann-typescript": "0.1.0"
```

(Once `hofmann-typescript` is published to npm.)

## Configuration Reference

All Hofmann configuration is in `example.yml`. Key fields:

| Field | Default | Notes |
|-------|---------|-------|
| `argon2MemoryKib` | `65536` | 64 MiB — tune for your hardware |
| `argon2Iterations` | `3` | |
| `argon2Parallelism` | `1` | |
| `context` | `hofmann-demo-v1` | Must match client; unique per deployment |
| `serverKeySeedHex` | _(demo value)_ | Generate your own — see Installation Setup |
| `oprfSeedHex` | _(demo value)_ | Generate your own — see Installation Setup |
| `oprfMasterKeyHex` | _(demo value)_ | Generate your own — see Installation Setup |
| `jwtSecretHex` | _(demo value)_ | Generate your own — see Installation Setup |
| `databaseUrl` | `jdbc:h2:file:./data/hofmann-example` | H2 file-mode; data persists in `./data/` |
