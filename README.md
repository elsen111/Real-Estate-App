# EstateFlow — Real Estate CRM & Marketplace

<p align="left">
  <img src="https://komarev.com/ghpvc/?username=estateflow&label=Profile%20Views&color=0e75b6&style=flat" alt="views" />
  <img src="https://img.shields.io/github/stars/your-username/EstateFlow?style=flat&color=yellow" alt="stars" />
  <img src="https://img.shields.io/github/forks/your-username/EstateFlow?style=flat&color=blue" alt="forks" />
  <img src="https://img.shields.io/github/issues/your-username/EstateFlow?style=flat&color=red" alt="issues" />
  <img src="https://img.shields.io/github/last-commit/your-username/EstateFlow?style=flat&color=green" alt="last commit" />
  <img src="https://img.shields.io/github/license/your-username/EstateFlow?style=flat&color=lightgrey" alt="license" />
</p>

This is a full-stack real estate platform for agencies, agents, property owners, buyers, and renters. It combines property listings, map-based search, agency management, inquiries, viewing requests, dashboards, and basic subscription logic in one system.

---

## 📑 Table of Contents

- [✨ Features](#-features)
- [👥 User Roles](#-user-roles)
- [🛠️ Tech Stack](#️-tech-stack)
  - [⚙️ Backend](#️-backend)
  - [🎨 Frontend](#-frontend)
- [📁 Project Structure](#-project-structure)
- [🧩 Main Modules](#-main-modules)
  - [🔐 Authentication](#-authentication)
  - [🏠 Properties](#-properties)
  - [🗺️ Search & Map](#️-search--map)
  - [📩 Inquiries & Viewings](#-inquiries--viewings)
  - [📊 Dashboards](#-dashboards)
- [🗄️ Database Tables](#️-database-tables)
- [🔧 Environment Variables](#-environment-variables)
- [🚀 How to Run](#-how-to-run)
- [🖥️ Backend Setup](#️-backend-setup)
- [💻 Frontend Setup](#-frontend-setup)
- [🐳 Run with Docker](#-run-with-docker)
- [🔑 Default Development Accounts](#-default-development-accounts)
- [🔮 Future Improvements](#-future-improvements)

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## ✨ Features

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 👥 User Roles

- **Super Admin** — manages users, agencies, properties, subscriptions, and platform statistics.
- **Agency Admin** — manages agency profile, agents, properties, inquiries, and viewing requests.
- **Agent** — manages assigned properties, inquiries, and viewing requests.
- **Client** — searches properties, saves favorites, sends inquiries, and requests viewings.

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🛠️ Tech Stack

### ⚙️ Backend

<p align="left">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security" />
  <img src="https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" alt="JWT" />
  <img src="https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL" />
  <img src="https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate" />
  <img src="https://img.shields.io/badge/Liquibase-2962FF?style=for-the-badge&logo=liquibase&logoColor=white" alt="Liquibase" />
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger" />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
</p>

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

### 🎨 Frontend

<p align="left">
  <img src="https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React" />
  <img src="https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white" alt="TypeScript" />
  <img src="https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white" alt="Vite" />
  <img src="https://img.shields.io/badge/React%20Router-CA4245?style=for-the-badge&logo=reactrouter&logoColor=white" alt="React Router" />
  <img src="https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white" alt="Axios" />
  <img src="https://img.shields.io/badge/Tailwind%20CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="Tailwind CSS" />
  <img src="https://img.shields.io/badge/Leaflet-199900?style=for-the-badge&logo=leaflet&logoColor=white" alt="Leaflet" />
  <img src="https://img.shields.io/badge/Google%20Maps-4285F4?style=for-the-badge&logo=googlemaps&logoColor=white" alt="Google Maps" />
  <img src="https://img.shields.io/badge/Recharts-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white" alt="Recharts" />
</p>

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 📁 Project Structure

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🧩 Main Modules

### 🔐 Authentication

- Register
- Login
- Logout
- Refresh token
- Protected endpoints
- Protected frontend routes

### 🏠 Properties

- Create, update, delete, and view properties
- Upload property images
- Add price, city, address, rooms, area, floor, description, latitude, and longitude
- Set listing type: sale or rent
- Set property type: apartment, house, villa, office, or land
- Set property status: pending, active, sold, or rented

### 🗺️ Search & Map

- Search by keyword
- Filter by city, property type, listing type, price range, and rooms
- Sort by newest or price
- Show properties on map
- Open property details from map marker popup

### 📩 Inquiries & Viewings

- Client sends inquiry for a property
- Agent or agency admin updates inquiry status
- Client requests property viewing
- Agent or agency admin approves, rejects, or completes viewing request

### 📊 Dashboards

- Super Admin dashboard
- Agency Admin dashboard
- Agent dashboard
- Client dashboard

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🗄️ Database Tables

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🔧 Environment Variables

### ⚙️ Backend

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

### 🎨 Frontend

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_MAP_PROVIDER=openstreetmap
VITE_MAPS_API_KEY=
```

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🚀 How to Run

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/EstateFlow.git
cd EstateFlow
```

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🖥️ Backend Setup

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
http://localhost:8080/api/swagger-ui.html
```

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 💻 Frontend Setup

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🐳 Run with Docker

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🔑 Default Development Accounts

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

<p align="right"><a href="#estateflow--real-estate-crm--marketplace">⬆️ Back to top</a></p>

---

## 🔮 Future Improvements

- Multilanguage support
- Currencies
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
