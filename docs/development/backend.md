# EstateFlow Backend Documentation

## 1. Backend Goal

The backend is the core of EstateFlow. It should provide secure REST APIs for authentication, property management, agency CRM features, map-based search, inquiries, viewing requests, favorites, dashboards, and subscription rules.

The backend must be designed as a real-world Spring Boot application, not just simple CRUD.

## 2. Backend Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Liquibase
- JWT authentication
- Bean Validation
- MapStruct
- Lombok
- Swagger/OpenAPI
- Docker
- JUnit and Mockito

## 3. Main Backend Requirements

- RESTful API architecture
- N-layer structure
- Role-based authorization
- DTO-based request/response handling
- Global exception handling
- Standard API response wrapper
- Database migrations with Liquibase
- Pagination and filtering
- File upload support for property images
- Swagger API documentation
- Unit and controller tests

## 4. Required Roles

```text
SUPER_ADMIN
AGENCY_ADMIN
AGENT
CLIENT
```

## 5. Suggested Backend Package Structure

```bash
backend/src/main/java/com/estateflow/
├── EstateFlowApplication.java
├── config/
├── controller/
│   ├── auth/
│   ├── admin/
│   ├── agency/
│   ├── agent/
│   ├── property/
│   ├── favorite/
│   ├── inquiry/
│   ├── viewing/
│   ├── subscription/
│   └── dashboard/
├── dto/
│   ├── auth/
│   ├── user/
│   ├── agency/
│   ├── agent/
│   ├── property/
│   ├── favorite/
│   ├── inquiry/
│   ├── viewing/
│   ├── subscription/
│   └── common/
├── entity/
├── enums/
├── exception/
├── mapper/
├── repository/
├── security/
├── service/
│   ├── impl/
│   └── interfaces/
└── util/
```

## 6. N-Layer Architecture

EstateFlow should follow this flow:

```text
Controller → Service → Repository → Database
```

### Controller Layer

Responsible for receiving HTTP requests, validating request bodies, calling services, and returning responses.

### Service Layer

Contains business logic. Examples:

- Agency cannot create property without active subscription
- Agent can only manage assigned properties
- Client cannot approve viewing request
- Super Admin can approve or reject property listings

### Repository Layer

Communicates with the database using Spring Data JPA.

### DTO Layer

Used to separate API request/response models from database entities.

### Mapper Layer

Maps DTOs to entities and entities to DTOs.

## 7. Main Entities

### User

Fields:

- id
- firstName
- lastName
- email
- password
- phone
- role
- enabled
- createdAt
- updatedAt

### Agency

Fields:

- id
- name
- description
- email
- phone
- address
- website
- logoUrl
- status
- subscriptionStatus
- createdAt
- updatedAt

### Agent

Fields:

- id
- user
- agency
- specialization
- licenseNumber
- active
- createdAt
- updatedAt

### Property

Fields:

- id
- title
- description
- price
- city
- address
- propertyType
- listingType
- area
- rooms
- bathrooms
- floor
- totalFloors
- latitude
- longitude
- status
- agency
- agent
- owner
- createdAt
- updatedAt

### PropertyImage

Fields:

- id
- property
- imageUrl
- mainImage
- createdAt

### Favorite

Fields:

- id
- client
- property
- createdAt

### Inquiry

Fields:

- id
- property
- client
- agency
- agent
- message
- status
- createdAt
- updatedAt

### ViewingRequest

Fields:

- id
- property
- client
- agent
- requestedDateTime
- status
- note
- createdAt
- updatedAt

### SubscriptionPlan

Fields:

- id
- name
- price
- maxListings
- maxAgents
- active

### AgencySubscription

Fields:

- id
- agency
- plan
- startDate
- endDate
- status

## 8. Main Enums

```java
RoleName {
    SUPER_ADMIN,
    AGENCY_ADMIN,
    AGENT,
    CLIENT
}
```

```java
PropertyType {
    APARTMENT,
    HOUSE,
    VILLA,
    OFFICE,
    LAND
}
```

```java
ListingType {
    SALE,
    RENT
}
```

```java
PropertyStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    SOLD,
    RENTED
}
```

```java
InquiryStatus {
    NEW,
    CONTACTED,
    CLOSED
}
```

```java
ViewingStatus {
    PENDING,
    APPROVED,
    REJECTED,
    COMPLETED
}
```

```java
SubscriptionStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED
}
```

## 9. Business Rules

### Authentication Rules

- Users must login with email and password.
- JWT access token is required for protected endpoints.
- Refresh token is used to renew expired access token.

### Authorization Rules

- SUPER_ADMIN can manage all platform data.
- AGENCY_ADMIN can manage only own agency data.
- AGENT can manage only assigned properties and requests.
- CLIENT can only access own favorites, inquiries, and viewings.

### Property Rules

- New property should be created with PENDING status.
- Super Admin can approve or reject property.
- Only ACTIVE properties appear publicly.
- Agency must have active subscription to create new properties.
- Property must have latitude and longitude to appear on map.

### Inquiry Rules

- Client can send inquiry for active property.
- Agency Admin and assigned Agent can view inquiry.
- Inquiry status can be changed to NEW, CONTACTED, or CLOSED.

### Viewing Rules

- Client can request viewing for active property.
- Agent or Agency Admin can approve/reject request.
- Client can see own viewing requests.

### Favorite Rules

- Client can favorite active properties.
- Duplicate favorite for the same property should not be allowed.

## 10. Security Requirements

Use Spring Security with JWT.

Protected examples:

```text
/api/admin/**          → SUPER_ADMIN
/api/agency/**         → AGENCY_ADMIN
/api/agent/**          → AGENT, AGENCY_ADMIN
/api/client/**         → CLIENT
/api/properties/manage → AGENCY_ADMIN, AGENT
```

Public examples:

```text
/api/auth/**
/api/public/properties
/api/public/properties/{id}
/api/public/agencies
```

## 11. Standard API Response

Use a common response shape:

```json
{
  "success": true,
  "message": "Property created successfully",
  "data": {},
  "timestamp": "2026-06-01T12:00:00"
}
```

For validation errors:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "price": "Price must be positive"
  },
  "timestamp": "2026-06-01T12:00:00"
}
```

## 12. Validation Requirements

Examples:

- email: required and valid email format
- password: minimum 8 characters
- property title: required, min 5 characters
- price: positive number
- latitude: required for map
- longitude: required for map
- requested viewing date: future date

## 13. Liquibase Requirements

Use changelog files for database creation.

Suggested changelog order:

```bash
src/main/resources/db/changelog/
├── db.changelog-master.yaml
├── 001-create-users-table.yaml
├── 002-create-agencies-table.yaml
├── 003-create-agents-table.yaml
├── 004-create-properties-table.yaml
├── 005-create-property-images-table.yaml
├── 006-create-favorites-table.yaml
├── 007-create-inquiries-table.yaml
├── 008-create-viewing-requests-table.yaml
├── 009-create-subscription-plans-table.yaml
└── 010-create-agency-subscriptions-table.yaml
```

## 14. Backend Development Phases

### Phase 1 — Project Setup

- Create Spring Boot project
- Add dependencies
- Configure PostgreSQL
- Configure Liquibase
- Configure Swagger

### Phase 2 — Authentication

- User entity
- Role enum
- Register
- Login
- JWT generation
- Refresh token
- Security config

### Phase 3 — Agency CRM

- Agency entity
- Agency CRUD
- Agent entity
- Agent CRUD
- Agent assignment

### Phase 4 — Properties

- Property entity
- Property CRUD
- Property image upload
- Property status moderation
- Search/filter endpoints
- Map endpoint

### Phase 5 — Client Features

- Favorites
- Inquiries
- Viewing requests

### Phase 6 — Subscription Logic

- Plans
- Agency subscription status
- Listing creation restriction

### Phase 7 — Dashboards

- Admin statistics
- Agency statistics
- Agent statistics
- Client dashboard data

### Phase 8 — Testing and Documentation

- Unit tests
- Controller tests
- Swagger polish
- README update

## 15. Testing Requirements

Minimum tests:

- AuthService tests
- PropertyService tests
- InquiryService tests
- ViewingService tests
- Subscription business rule tests
- Controller tests for main endpoints

## 16. MVP Backend Completion Checklist

- [ ] Authentication completed
- [ ] Roles and permissions completed
- [ ] Agency module completed
- [ ] Agent module completed
- [ ] Property module completed
- [ ] Property image upload completed
- [ ] Search/filter completed
- [ ] Map endpoint completed
- [ ] Favorite module completed
- [ ] Inquiry module completed
- [ ] Viewing module completed
- [ ] Subscription logic completed
- [ ] Dashboards completed
- [ ] Swagger completed
- [ ] Basic tests completed
