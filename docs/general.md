# EstateFlow — Real Estate CRM & Marketplace Platform

This project is a full-stack real estate platform designed for agencies, agents, property owners, buyers, and renters. It combines a public property marketplace with agency management tools, interactive map-based search, inquiries, viewing requests, dashboards, and basic subscription logic.

The platform helps real estate businesses manage property listings, agents, client inquiries, appointments, and listing visibility from one centralized system, while users can search, filter, save, and explore properties through a modern web interface.

---

## Table of Contents

- [Overview](#overview)
- [Main Features](#main-features)
- [User Roles](#user-roles)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Backend Features](#backend-features)
- [Frontend Features](#frontend-features)
- [Database Design](#database-design)
- [Environment Variables](#environment-variables)
- [How to Run the Project](#how-to-run-the-project)
- [Backend Setup](#backend-setup)
- [Frontend Setup](#frontend-setup)
- [Running with Docker](#running-with-docker)
- [Default Development Accounts](#default-development-accounts)
- [API Documentation](#api-documentation)
- [Business Model](#business-model)
- [Future Improvements](#future-improvements)

---

## Overview

Many real estate agencies still manage property listings, customer requests, appointments, and agent workflows through spreadsheets, social media pages, phone calls, and messaging apps. EstateFlow provides a structured digital system for managing this process.

EstateFlow includes:

- Property listing management
- Agency and agent management
- Public property marketplace
- Property search and filtering
- Interactive map-based property exploration
- Favorites system
- Inquiry management
- Viewing request management
- Role-based dashboards
- Basic subscription logic
- Admin moderation tools

---

## Main Features

### Public Features

- Home page with featured and latest properties
- Property listing page
- Property details page
- Property image gallery
- Search and filter system
- Interactive map page
- Agency profile page
- Contact page
- Login and registration pages

### Property Search Features

- Search by keyword
- Filter by city
- Filter by property type
- Filter by listing type: sale or rent
- Filter by price range
- Filter by number of rooms
- Sort by newest
- Sort by price
- View filtered properties on map

### Interactive Map Features

- Display property markers on the map
- Show property preview in marker popup
- Open property details from marker popup
- Filter map results
- Store latitude and longitude for each property
- Show property location on details page

### Property Management Features

- Create property listing
- Update property listing
- Delete property listing
- Upload property images
- Assign property to agent
- Set property status
- Manage property visibility
- Add property coordinates for map display

### Inquiry Features

- Client sends inquiry from property details page
- Agency or agent views inquiries
- Agency or agent updates inquiry status
- Inquiry status examples:
  - New
  - Contacted
  - Closed

### Viewing Request Features

- Client requests a property viewing
- Client selects preferred date and time
- Agent or agency admin approves or rejects request
- Viewing request statuses:
  - Pending
  - Approved
  - Rejected
  - Completed

### Favorites Features

- Client saves properties
- Client removes saved properties
- Client views favorite properties from dashboard

### Dashboard Features

- Super Admin dashboard
- Agency Admin dashboard
- Agent dashboard
- Client dashboard

### Subscription Features

- Admin creates subscription plans
- Agency has active or inactive subscription
- Agency can create property listings only when subscription is active
- Admin can manage featured listing status

---

## User Roles

### 1. Super Admin

The platform owner who manages the whole system.

Main permissions:

- Manage users
- Manage agencies
- Approve or reject property listings
- Manage subscription plans
- View platform statistics
- Activate or deactivate agencies
- Moderate platform content

### 2. Agency Admin

The real estate agency owner or manager.

Main permissions:

- Manage agency profile
- Add and manage agents
- Create and manage properties
- Assign properties to agents
- View inquiries
- Manage viewing requests
- View agency dashboard statistics

### 3. Agent

An agency employee responsible for assigned properties and client communication.

Main permissions:

- View assigned properties
- Update assigned property information
- View inquiries related to assigned properties
- Manage viewing requests
- Respond to clients

### 4. Client

A buyer or renter using the platform to find properties.

Main permissions:

- Browse properties
- Search and filter listings
- View properties on map
- Save favorite properties
- Send inquiries
- Request property viewings
- Manage own dashboard activity

---

## Tech Stack

### Backend

- Java
- Spring Boot
- Spring Security
- JWT Authentication
- PostgreSQL
- Spring Data JPA / Hibernate
- Liquibase
- Bean Validation
- MapStruct
- Lombok
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

### Database

- PostgreSQL

### Deployment

- Docker
- Docker Compose
- Nginx
- VPS or cloud hosting

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
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   └── util/
│   ├── src/main/resources/
│   │   ├── db/changelog/
│   │   └── application.properties
│   └── Dockerfile
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── constants/
│   │   ├── context/
│   │   ├── hooks/
│   │   ├── layouts/
│   │   ├── pages/
│   │   ├── routes/
│   │   ├── schemas/
│   │   ├── services/
│   │   ├── types/
│   │   ├── utils/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── Dockerfile
│   ├── package.json
│   └── vite.config.ts
│
├── docs/
│   ├── backend.md
│   ├── frontend.md
│   └── api.md
│
├── docker-compose.yml
└── README.md
```

---

## Backend Features

### Authentication & Authorization

- User registration
- Login
- Logout
- JWT access token
- Refresh token
- Password hashing
- Role-based access control
- Protected endpoints

### User Management

- Create users
- Update user profile
- Manage user status
- Assign roles
- View users by role

### Agency Management

- Create agency profile
- Update agency profile
- Upload agency logo
- Activate or deactivate agency
- Assign subscription plan
- View agency details

### Agent Management

- Create agent account
- Update agent profile
- Delete or deactivate agent
- Assign properties to agent
- View agent workload

### Property Management

- Create property
- Update property
- Delete property
- Upload property images
- Manage property status
- Assign agent to property
- Store latitude and longitude
- Search and filter properties
- Paginate property results

### Favorites

- Add property to favorites
- Remove property from favorites
- View client favorites

### Inquiries

- Create inquiry
- View property inquiries
- Update inquiry status
- Track inquiry history

### Viewing Requests

- Create viewing request
- Approve viewing request
- Reject viewing request
- Complete viewing request
- View upcoming viewings

### Subscription Plans

- Create subscription plan
- Update subscription plan
- Assign subscription to agency
- Validate agency subscription before allowing new listings

### Admin Moderation

- Approve property listings
- Reject property listings
- Manage agencies
- Manage users
- View platform statistics

---

## Frontend Features

### Public Pages

- Home page
- Properties page
- Property details page
- Map search page
- Agencies page
- About page
- Contact page
- Login page
- Register page

### Dashboard Pages

#### Super Admin

- Platform statistics
- User management
- Agency management
- Property moderation
- Subscription management

#### Agency Admin

- Agency statistics
- Property management
- Agent management
- Inquiry management
- Viewing request management

#### Agent

- Assigned properties
- Related inquiries
- Related viewing requests

#### Client

- Favorite properties
- Sent inquiries
- Viewing requests

### UI Components

- Navbar
- Footer
- Property card
- Property filters
- Property form
- Image gallery
- Map marker popup
- Dashboard sidebar
- Data table
- Modal
- Form inputs
- Loading skeletons
- Error messages

---

## Database Design

Main tables:

- users
- roles
- user_roles
- agencies
- agents
- properties
- property_images
- property_features
- favorites
- inquiries
- viewing_requests
- subscription_plans
- agency_subscriptions
- notifications

Important relationships:

- One agency has many agents
- One agency has many properties
- One agent can manage many properties
- One property can have many images
- One client can save many favorite properties
- One property can receive many inquiries
- One property can receive many viewing requests
- One agency can have one active subscription

---

## Environment Variables

### Backend `.env` or `application.yml`

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

### Frontend `.env`

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_MAP_PROVIDER=openstreetmap
VITE_MAPS_API_KEY=your_maps_api_key_if_needed
```

---

## How to Run the Project

You can run the project in two ways:

1. Manually run backend and frontend separately
2. Run all services using Docker Compose

---

## Backend Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/EstateFlow.git
cd EstateFlow
```

### 2. Go to backend folder

```bash
cd backend
```

### 3. Create PostgreSQL database

```sql
CREATE DATABASE estateflow_db;
```

### 4. Configure backend environment

Create or update:

```bash
src/main/resources/application.yml
```

Example configuration:

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
    properties:
      hibernate:
        format_sql: true
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.yaml

jwt:
  secret: change_this_secret_key
  access-expiration: 3600000
  refresh-expiration: 604800000
```

### 5. Install backend dependencies

If the project uses Maven:

```bash
mvn clean install
```

If the project uses Gradle:

```bash
./gradlew build
```

### 6. Run backend server

Using Maven:

```bash
mvn spring-boot:run
```

Using Gradle:

```bash
./gradlew bootRun
```

Backend server will run on:

```bash
http://localhost:8080
```

Swagger documentation will be available at:

```bash
http://localhost:8080/swagger-ui/index.html
```

---

## Frontend Setup

### 1. Go to frontend folder

From the project root:

```bash
cd frontend
```

### 2. Install dependencies

```bash
npm install
```

### 3. Create frontend environment file

Create `.env` inside the `frontend` folder:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_MAP_PROVIDER=openstreetmap
VITE_MAPS_API_KEY=
```

For OpenStreetMap with React Leaflet, a map API key is usually not required.

### 4. Run frontend development server

```bash
npm run dev
```

Frontend will run on:

```bash
http://localhost:5173
```

---

## Running with Docker

### 1. Make sure Docker is installed

Check Docker version:

```bash
docker --version
```

Check Docker Compose version:

```bash
docker compose version
```

### 2. Create `.env` file in project root

```env
POSTGRES_DB=estateflow_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/estateflow_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

VITE_API_BASE_URL=http://localhost:8080/api
```

### 3. Start containers

```bash
docker compose up --build
```

### 4. Stop containers

```bash
docker compose down
```

### 5. Stop containers and remove volumes

```bash
docker compose down -v
```

---

## Example Docker Compose

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:16-alpine
    container_name: estateflow_postgres
    restart: always
    environment:
      POSTGRES_DB: estateflow_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - estateflow_postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    container_name: estateflow_backend
    restart: always
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/estateflow_db
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      JWT_SECRET: change_this_secret_key

  frontend:
    build: ./frontend
    container_name: estateflow_frontend
    restart: always
    ports:
      - "5173:5173"
    depends_on:
      - backend
    environment:
      VITE_API_BASE_URL: http://localhost:8080/api

volumes:
  estateflow_postgres_data:
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

## API Documentation

The backend should expose REST APIs under:

```bash
/api
```

Main API groups:

```txt
/api/auth
/api/users
/api/admin
/api/agencies
/api/agents
/api/properties
/api/favorites
/api/inquiries
/api/viewings
/api/subscriptions
/api/dashboard
```

Swagger/OpenAPI documentation:

```bash
http://localhost:8080/swagger-ui/index.html
```

---

## Example API Endpoints

### Authentication

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh-token
POST /api/auth/logout
```

### Properties

```http
GET    /api/properties
GET    /api/properties/{id}
POST   /api/properties
PUT    /api/properties/{id}
DELETE /api/properties/{id}
GET    /api/properties/map
```

### Favorites

```http
GET    /api/favorites
POST   /api/favorites/{propertyId}
DELETE /api/favorites/{propertyId}
```

### Inquiries

```http
POST /api/inquiries
GET  /api/inquiries/my
GET  /api/inquiries/agency
PUT  /api/inquiries/{id}/status
```

### Viewing Requests

```http
POST /api/viewings
GET  /api/viewings/my
GET  /api/viewings/agency
PUT  /api/viewings/{id}/approve
PUT  /api/viewings/{id}/reject
PUT  /api/viewings/{id}/complete
```

---

## Business Model

EstateFlow can support several revenue models:

- Agency subscription plans
- Featured property listings
- Paid property promotions
- Developer advertising
- Lead generation
- Premium analytics for agencies

The first monetization logic can be simple subscription control:

```txt
If an agency does not have an active subscription, it cannot publish new property listings.
```

---

## Future Improvements

- Online payment gateway integration
- Invoice generation
- Advanced agency reports
- Saved searches
- Price alerts
- Property comparison
- AI property recommendations
- AI price estimation
- Real-time chat
- 360° virtual tours
- Mortgage calculator
- Bank integrations
- Property price history
- Neighborhood intelligence
- Mobile app with React Native

---

## License

This project is open for educational and portfolio use. Add a license file if you plan to publish it as an open-source project.
