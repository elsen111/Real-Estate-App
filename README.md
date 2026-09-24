# EstateFlow — Real Estate CRM & Marketplace


<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white" alt="Docker"/>
</p>

A real estate platform backend for agencies, agents, landlords, and clients. It handles property listings, agency and agent management, inquiries, viewing appointments, reviews, favorites, categories, subscription plans, Stripe payments, and media storage via MinIO.

---

## 📑 Table of Contents

- [✨ Features](#-features)
- [👥 User Roles](#-user-roles)
- [🛠️ Tech Stack](#️-tech-stack)
- [📁 Project Structure](#-project-structure)
- [🧩 Main Modules](#-main-modules)
- [🗄️ Database](#️-database)
- [🔧 Environment Variables](#-environment-variables)
- [🖥️ Run Locally (without Docker)](#️-run-locally-without-docker)
- [🐳 Run with Docker](#-run-with-docker)
- [🗄️ Connecting DataGrip / a DB client](#️-connecting-datagrip--a-db-client)
- [🎨 Code Formatting](#-code-formatting)
- [🔮 Future Improvements](#-future-improvements)

---

## ✨ Features

* 🔐 JWT-based authentication (access + refresh tokens), login/logout, "me" endpoint
* 🛡️ Role-based access control (`SUPER_ADMIN`, `ADMIN`, `AGENCY_OWNER`, `AGENT`, `LANDLORD`, `CLIENT`)
* 📝 Separate registration flows for regular users and agency owners
* 📧 Password reset via email OTP
* 🏢 Agency management (profile, members, status, admin moderation)
* 👨‍💼 Agent management
* 🏠 Property listings: create/update/delete, media upload via MinIO, view tracking
* 🗂️ Categories, subscription plans, and agency subscriptions
* ❤️ Favorites
* 📩 Inquiries and viewing appointments
* ⭐ Reviews (property/agency), with admin moderation
* 💳 Stripe-based payments and subscription billing, with a webhook endpoint
* 🚦 Rate limiting (Bucket4j) on sensitive endpoints
* 📚 Swagger / OpenAPI documentation
* ⏰ Scheduled jobs for subscription expiration and notification emails


⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 👥 User Roles

Seeded via Liquibase (`db/changelog/2.0`):

- **SUPER_ADMIN** — full platform control
- **ADMIN** — assistant to the platform administrator
- **AGENCY_OWNER** — head of an agency, manages agents/properties/inquiries for that agency
- **AGENT** — manages assigned properties, inquiries, and appointments
- **LANDLORD** — private owner listing their own property directly
- **CLIENT** — searches properties, saves favorites, sends inquiries, requests viewings

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🛠️ Tech Stack

- Java 21
- Spring Boot 4 (Web, Security, Data JPA, Validation, Mail, Thymeleaf)
- Gradle (wrapper included — no local Gradle install required)
- PostgreSQL + Liquibase (SQL changesets, not XML)
- JWT (jjwt)
- MinIO (S3-compatible object storage) for property/agency/user media
- Stripe (`stripe-java`) for payments and subscriptions
- Bucket4j for rate limiting
- springdoc-openapi (Swagger UI)
- Spock (Groovy) + JUnit for tests
- Docker / Docker Compose
- Spotless (`google-java-format`) for code formatting

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 📁 Project Structure

```
Real-Estate-App/
├── backend/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── .dockerignore
│   ├── .env.example
│   ├── build.gradle
│   ├── gradlew / gradlew.bat
│   └── src/main/
│       ├── java/com/realestate/backend/
│       │   ├── config/
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── entity/
│       │   ├── enums/
│       │   ├── exception/
│       │   ├── mapper/
│       │   ├── payment/
│       │   ├── repository/
│       │   ├── scheduler/
│       │   ├── security/
│       │   ├── service/
│       │   ├── storage/
│       │   └── utils/
│       └── resources/
│           ├── application.yaml
│           ├── application-dev.yaml
│           ├── application-docker.yaml
│           └── db/changelog/
│               ├── 1.0/   # initial schema
│               └── 2.0/   # seed data + incremental changes
├── docs/
│   └── DOCKER_GUIDE.md
├── .github/workflows/
│   ├── lint-format.yml
│   └── deploy.yml
└── README.md
```

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🧩 Main Modules

Controllers currently implemented:

| Area | Controller |
|---|---|
| Auth | `AuthController` — register (user/agency owner), login, refresh, logout, me, change/forgot/reset password, reactivate/deactivate |
| Users | `UserController`, `AdminUserController` |
| Agencies | `AgencyController`, `AgencyMemberController`, `AdminAgencyController` |
| Agents | `AgentController` |
| Properties | `PropertyController`, `AdminPropertyController` |
| Categories | `CategoryController`, `AdminCategoryController` |
| Favorites | `FavoriteController` |
| Inquiries | `InquiryController` |
| Appointments (viewings) | `AppointmentController` |
| Reviews | `ReviewController`, `AdminReviewController` |
| Subscriptions | `SubscriptionPlanController`, `AdminSubscriptionController` |
| Payments | `PaymentController` (Stripe checkout + webhook) |

> ⚠️ **Email verification is not yet wired up.** Users are created with `emailVerified = false` at registration, but there's currently no endpoint or service that flips it to `true` — only the password-reset OTP flow exists so far.

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🗄️ Database

Managed entirely through Liquibase SQL changesets under `backend/src/main/resources/db/changelog/`. Key tables: `users`, `roles`, `user_roles`, `agencies`, `agency_members`, `agency_media`, `categories`, `properties`, `property_media`, `property_views`, `favorites`, `inquiries`, `appointments`, `reviews`, `subscription_plans`, `agency_subscriptions`, `subscription_notifications`, `refresh_tokens`, `password_reset_tokens`, `password_reset_otp`, and payment tables.

Never hand-edit an already-applied changeset — add a new one under `2.0/` (or a new version folder) instead.

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🔧 Environment Variables

All configuration is env-var driven — see `backend/.env.example` for the full list with placeholders:

```
SPRING_PROFILES_ACTIVE=docker   # or "dev" for local (non-Docker) runs

DB_HOST=postgres                # "localhost" for non-Docker local runs
DB_PORT=5432
DB_NAME=real_estate_db
DB_USERNAME=postgres
DB_PASSWORD=change-me
POSTGRES_HOST_PORT=5432

JWT_SECRET=replace-with-a-long-random-string

MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

MINIO_URL=http://minio:9000     # "http://localhost:9000" for non-Docker local runs
MINIO_USERNAME=minioadmin
MINIO_SECRET_KEY=minioadmin
MINIO_BUCKET_NAME=estateflow
MINIO_HOST_PORT=9000
MINIO_CONSOLE_PORT=9001

STRIPE_SECRET_KEY=sk_test_replace_me
STRIPE_WEBHOOK_SECRET=whsec_replace_me
STRIPE_CURRENCY=usd
STRIPE_SUCCESS_URL=http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}
STRIPE_CANCEL_URL=http://localhost:5173/payment/cancelled

BACKEND_HOST_PORT=8080
```

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🖥️ Run Locally (without Docker)

1. Create a local PostgreSQL database matching `DB_NAME` in your `.env`.
2. From `backend/`, copy the env template and fill in real values:
   ```powershell
   Copy-Item .env.example .env
   ```
   Set `SPRING_PROFILES_ACTIVE=dev`, `DB_HOST=localhost`, `MINIO_URL=http://localhost:9000`.
3. Run the app (Windows):
   ```powershell
   .\gradlew.bat bootRun --args='--spring.profiles.active=dev'
   ```
   macOS/Linux:
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```
4. Liquibase applies all changesets automatically on startup.

Backend: `http://localhost:8080`
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🐳 Run with Docker

Everything below runs from **`backend/`**, since that's where `docker-compose.yml` lives.

1. Copy the env template:
   ```powershell
   Copy-Item .env.example .env
   ```
   Fill in `DB_PASSWORD`, `JWT_SECRET`, `MAIL_USERNAME`/`MAIL_PASSWORD`, `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET`. Leave `DB_HOST=postgres` and `MINIO_URL=http://minio:9000` as-is — those are the in-network service names, not `localhost`.

2. Build and start everything:
   ```powershell
   docker compose up -d --build
   ```

3. Check status / logs:
   ```powershell
   docker compose ps
   docker compose logs -f backend
   ```

4. Stop everything (keeps data):
   ```powershell
   docker compose down
   ```
   Stop and wipe all data (Postgres + MinIO volumes):
   ```powershell
   docker compose down -v --remove-orphans
   ```

What starts:

| Service | Container | Host port(s) |
|---|---|---|
| postgres | estateflow-postgres | `POSTGRES_HOST_PORT` (default 5432) |
| minio | estateflow-minio | `MINIO_HOST_PORT` (9000), `MINIO_CONSOLE_PORT` (9001) |
| backend | estateflow-backend | `BACKEND_HOST_PORT` (8080) |

Backend: `http://localhost:8080/api`
Swagger UI: `http://localhost:8080/api/swagger-ui.html`
MinIO console: `http://localhost:9001` (login with `MINIO_USERNAME` / `MINIO_SECRET_KEY`)

> The `minio` service uses `bitnami/minio` — the official `minio/minio` image stopped publishing to Docker Hub after October 2025.

Full walkthrough, troubleshooting, and the DataGrip setup below live in [`docs/DOCKER_GUIDE.md`](docs/DOCKER_GUIDE.md).

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🗄️ Connecting DataGrip / a DB client

With the containers running:

- Host: `localhost`
- Port: value of `POSTGRES_HOST_PORT` in your `.env` (default `5432`)
- Database: value of `DB_NAME` (default `real_estate_db`)
- User / Password: `DB_USERNAME` / `DB_PASSWORD` from your `.env`

Data persists in a named Docker volume (`postgres_data`) across `docker compose down` / `up` cycles — only `down -v` clears it. See `docs/DOCKER_GUIDE.md` for migrating data from an existing local Postgres instance.

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🎨 Code Formatting

Formatting is enforced with Spotless (`google-java-format`) and checked in CI (`.github/workflows/lint-format.yml`). From `backend/`:

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat spotlessCheck
```

`spotlessApply` auto-formats the codebase; `spotlessCheck` only verifies formatting and is what CI runs.

⬆️ [Back to top](#estateflow--real-estate-crm--marketplace-backend)

---

## 🔮 Future Improvements

- Email verification flow (registration currently sets `emailVerified = false` but no confirmation endpoint exists yet)
- Frontend application
- Map-based property search
- Multi-language / multi-currency support
- Saved searches, price alerts
- Real-time chat
- Advanced reporting dashboards
