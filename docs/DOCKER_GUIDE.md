# EstateFlow Backend — Docker & Dev Guide

This guide covers running the backend stack with Docker Compose, connecting DataGrip to
the Dockerized Postgres, and the code-formatting/CI workflow. Save this file as
`docs/DOCKER_GUIDE.md` in the repo.

---

## 0. Prerequisites

- Docker Desktop (or Docker Engine + Compose plugin) — `docker compose version` should print v2.x
- JDK 21 only needed if you plan to run Gradle *outside* Docker (formatting, tests)
- A free port for Postgres (default `5432`), MinIO (`9000`/`9001`), and the backend (`8080`)

---

## 1. First-time setup

```bash
cp .env.example .env
```

Open `.env` and fill in real values:
- `DB_PASSWORD` — pick anything for local dev
- `JWT_SECRET` — generate one, e.g. `openssl rand -hex 32`
- `MAIL_USERNAME` / `MAIL_PASSWORD` — a Gmail address + **App Password** (not your login password)
- `STRIPE_SECRET_KEY` / `STRIPE_WEBHOOK_SECRET` — from your Stripe test dashboard

> Because a `.env` with real secrets was found in the uploaded project, rotate the Gmail
> app password and JWT secret before reusing them, and don't commit `.env` (it's
> git-ignored already).

---

## 2. Running everything

```bash
make up          # docker compose up -d --build
make logs        # tail backend logs
make down        # stop everything
make clean       # stop + delete volumes (wipes DB & MinIO data)
```

What starts:

| Service  | Container name       | Host port(s)                  |
|----------|-----------------------|--------------------------------|
| postgres | estateflow-postgres    | `POSTGRES_HOST_PORT` (5432)   |
| minio    | estateflow-minio       | `MINIO_HOST_PORT` (9000), `MINIO_CONSOLE_PORT` (9001) |
| backend  | estateflow-backend     | `BACKEND_HOST_PORT` (8080)    |

API base URL: `http://localhost:8080/api`
Swagger UI: `http://localhost:8080/api/swagger-ui.html`
MinIO console: `http://localhost:9001` (login with `MINIO_USERNAME` / `MINIO_SECRET_KEY`)

---

## 3. Connecting DataGrip

You mentioned DataGrip already has some local data — here are the two ways to handle that.

### Option A — Point DataGrip at the new Docker Postgres (recommended)

The `postgres` service publishes to your host on `POSTGRES_HOST_PORT` (default `5432`),
so it's just a normal Postgres your existing tooling can reach.

1. In DataGrip: **New → Data Source → PostgreSQL**
2. Host: `localhost`
3. Port: value of `POSTGRES_HOST_PORT` from your `.env` (default `5432`)
4. Database: value of `DB_NAME` (default `real_estate_db`)
5. User / Password: `DB_USERNAME` / `DB_PASSWORD` from your `.env`
6. Test Connection → should succeed once `make up` has finished and the container is healthy

Data persists in a named Docker volume (`postgres_data`), so stopping/starting the
container (`make down` / `make up`) does **not** lose data — only `make clean` does
(`docker compose down -v`).

**If port 5432 is already taken** by an existing local Postgres install: change
`POSTGRES_HOST_PORT` in `.env` (e.g. `5433`), run `make restart`, and update the DataGrip
connection's port to match.

### Option B — Keep using your existing local Postgres (the one DataGrip already has data in)

If the "data" you want to keep is in a Postgres running directly on your machine (not in
Docker) and you don't want a second, empty database:

1. Remove/comment out the `postgres` service block in `docker-compose.yml`.
2. In `.env`, set `DB_HOST=host.docker.internal` (works on Docker Desktop for Mac/Windows;
   on Linux, add `extra_hosts: ["host.docker.internal:host-gateway"]` to the `backend`
   service in `docker-compose.yml`).
3. Point `DB_NAME`/`DB_USERNAME`/`DB_PASSWORD` at your existing local database — the same
   one DataGrip is already connected to.
4. `make up` — only `minio` and `backend` start in Docker; the backend talks to your
   existing host Postgres, and Liquibase will run its migrations against it on startup.

Either way, DataGrip's connection settings don't need to change once you pick an option —
they'll always point at `localhost:<port>`, whether that port belongs to a container or
your native install.

### Migrating existing data into the Docker container (if you choose Option A)

```bash
# Dump from your existing local Postgres
pg_dump -h localhost -p 5432 -U <old_user> -d <old_db> -F c -f dump.backup

# Restore into the new container (after `make up`)
docker compose exec -T postgres pg_restore -U $DB_USERNAME -d $DB_NAME --clean --if-exists < dump.backup
```

---

## 4. Code formatting & linting

Formatting uses **Spotless** with `google-java-format`, wired into Gradle.

```bash
make format   # auto-fix formatting across the codebase
make lint     # check only, fails if anything is unformatted (used in CI)
make test     # run the test suite (JUnit + Spock)
```

First run: the existing codebase hasn't been through Spotless yet, so run `make format`
once, review the diff, and commit it. After that, `make lint` (and CI) will stay green as
long as everyone runs `make format` before committing.

---

## 5. Database migrations (Liquibase)

```bash
make migrate   # ./gradlew update — applies pending changelogs to the DB in .env
```

Changelogs live in `backend/src/main/resources/db/changelog/`. New changesets should be
added under a new dated/numbered folder and registered in `changelog-master.yaml`,
following the existing `1.0` / `2.0` pattern.

---

## 6. CI/CD

- **`.github/workflows/lint-format.yml`** — runs on every push/PR to `main`/`develop`:
  Spotless check + compile. Keeps formatting enforced without needing Docker.
- **`.github/workflows/deploy.yml`** — on push to `main`: spins up a throwaway Postgres
  service container, runs the full test suite, then builds and pushes a Docker image to
  GitHub Container Registry (`ghcr.io`). The actual deploy step (SSH / Railway / Render /
  Kubernetes / etc.) is left as a commented placeholder — fill in your target.

---

## 7. Troubleshooting

| Symptom | Likely cause / fix |
|---|---|
| `port is already allocated` on `5432` | Another Postgres is using it — change `POSTGRES_HOST_PORT` in `.env` |
| Backend can't reach Postgres (`Connection refused`) | Make sure `SPRING_PROFILES_ACTIVE=docker` so `application-docker.yaml` is used (it resolves `DB_HOST=postgres`, not `localhost`) |
| Liquibase checksum mismatch | Someone edited an already-applied changelog — never modify a shipped changeset; add a new one instead |
| Mail sending fails (`535-5.7.8`) | Gmail rejected the credentials — regenerate an **App Password** (requires 2FA enabled on the Google account) |
| Stripe webhook not firing locally | Use the [Stripe CLI](https://stripe.com/docs/stripe-cli): `stripe listen --forward-to localhost:8080/api/webhooks/stripe`, and put the CLI's printed `whsec_...` into `STRIPE_WEBHOOK_SECRET` |
| MinIO bucket missing | The bucket in `MINIO_BUCKET_NAME` isn't auto-created — create it once via the console at `localhost:9001`, or add an init step if you want it automated |
