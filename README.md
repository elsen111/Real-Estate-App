# EstateFlow — Real Estate CRM & Marketplace

This is a full-stack real estate platform for agencies, agents, property owners, buyers, and renters. It combines property listings, map-based search, agency management, inquiries, viewing requests, dashboards, and basic subscription logic in one system.

---

## Features

- User authentication with JWT
- Role-based access control
- Agency and agent management
- Property listing management
- Property image upload
- Property search and filters
- Interactive map with property markers
- Property details page
- Favorites system
- Inquiry management
- Viewing request management
- Admin, agency, agent, and client dashboards
- Basic agency subscription control

---

## User Roles

- **Super Admin** — manages users, agencies, properties, subscriptions, and platform statistics.
- **Agency Admin** — manages agency profile, agents, properties, inquiries, and viewing requests.
- **Agent** — manages assigned properties, inquiries, and viewing requests.
- **Client** — searches properties, saves favorites, sends inquiries, and requests viewings.

---

## Tech Stack

### Backend

- Java
- Spring Boot
- Spring Security
- JWT
- PostgreSQL
- Spring Data JPA / Hibernate
- Liquibase
- Bean Validation
- Swagger / OpenAPI
- Docker

### Frontend

- React
- TypeScript
- Vite
- React Router
- Axios
- Tailwind CSS
- React Helmet Async
- React Leaflet or Google Maps
- Recharts
- i18n (Planned for the future)
- React Hook Form (Planned for the future)
- Zod (Planned for the future)

---

## Project Structure

```bash
EstateFlow/
├── backend/
│   ├── src/main/java/com/estateflow/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── security/
│   │   └── service/
│   └── src/main/resources/
│       ├── db/changelog/
│       └── application.properties
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── hooks/
│   │   ├── layouts/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── services/
│   │   ├── types/
│   │   └── utils/
│   └── package.json
│
├── docs/
├── docker-compose.yml
└── README.md
```

---

## Main Modules

### Authentication

- Register
- Login
- Logout
- Refresh token
- Protected endpoints
- Protected frontend routes

### Properties

- Create, update, delete, and view properties
- Upload property images
- Add price, city, address, rooms, area, floor, description, latitude, and longitude
- Set listing type: sale or rent
- Set property type: apartment, house, villa, office, or land
- Set property status: pending, active, sold, or rented

### Search & Map

- Search by keyword
- Filter by city, property type, listing type, price range, and rooms
- Sort by newest or price
- Show properties on map
- Open property details from map marker popup

### Inquiries & Viewings

- Client sends inquiry for a property
- Agent or agency admin updates inquiry status
- Client requests property viewing
- Agent or agency admin approves, rejects, or completes viewing request

### Dashboards

- Super Admin dashboard
- Agency Admin dashboard
- Agent dashboard
- Client dashboard

---

## Database Tables

Main tables:

- users
- roles
- user_roles
- agencies
- agency_members
- appointments
- categories
- agents
- properties
- media_files
- reviews
- favorites
- inquiries
- viewing_requests
- subscription_plans
- agency_subscriptions

---

## Environment Variables

### Backend

```env
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=estateflow_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=change_this_secret_key
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
FILE_UPLOAD_DIR=uploads
```

### Frontend

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_MAP_PROVIDER=openstreetmap
VITE_MAPS_API_KEY=
```

---

## How to Run

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/EstateFlow.git
cd EstateFlow
```

---

## Backend Setup

### 1. Create PostgreSQL Database

```sql
CREATE DATABASE estateflow_db;
```

### 2. Configure Backend

Create or update `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/estateflow_db
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml

jwt:
  secret: change_this_secret_key
  access-expiration: 3600000
  refresh-expiration: 604800000
```

### 3. Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on:

```bash
http://localhost:8080
```

Swagger runs on:

```bash
http://localhost:8080/swagger-ui/index.html
```

---

## Frontend Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Create `.env`

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_MAP_PROVIDER=openstreetmap
VITE_MAPS_API_KEY=
```

### 3. Run Frontend

```bash
npm run dev
```

Frontend runs on:

```bash
http://localhost:5173
```

---

## Run with Docker

Create `.env` in the project root:

```env
POSTGRES_DB=estateflow_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/estateflow_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
VITE_API_BASE_URL=http://localhost:8080/api
```

Start containers:

```bash
docker compose up --build
```

Stop containers:

```bash
docker compose down
```

---

## Default Development Accounts

```txt
Super Admin
Email: admin@estateflow.com
Password: Admin123!

Agency Admin
Email: agency@estateflow.com
Password: Agency123!

Agent
Email: agent@estateflow.com
Password: Agent123!

Client
Email: client@estateflow.com
Password: Client123!
```

---

## Future Improvements

- Online payment integration
- Featured listing payment flow
- Advanced reports
- Saved searches
- Price alerts
- Property comparison
- AI property recommendations
- AI price estimation
- Real-time chat
- 360° virtual tours
- Mortgage calculator
- React Native mobile app
