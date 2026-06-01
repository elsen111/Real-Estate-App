# EstateFlow API Documentation

EstateFlow API is a RESTful backend service for a real estate CRM and marketplace platform. It supports authentication, role-based authorization, agency management, property listings, map search, favorites, inquiries, viewing requests, subscriptions, and admin operations.

---

## Table of Contents

- [Base URL](#base-url)
- [Authentication](#authentication)
  - [Authorization Header](#authorization-header)
  - [Public Endpoints](#public-endpoints)
  - [Protected Endpoints](#protected-endpoints)
  - [Roles](#roles)
- [Standard API Response Format](#standard-api-response-format)
- [1. Authentication API](#1-authentication-api)
  - [1.1 Register User](#11-register-user)
  - [1.2 Login](#12-login)
  - [1.3 Refresh Token](#13-refresh-token)
  - [1.4 Get Current User](#14-get-current-user)
  - [1.5 Logout](#15-logout)
- [2. User Management API](#2-user-management-api)
  - [2.1 Get All Users](#21-get-all-users)
  - [2.2 Enable or Disable User](#22-enable-or-disable-user)
- [3. Agency API](#3-agency-api)
  - [3.1 Create Agency Profile](#31-create-agency-profile)
  - [3.2 Get My Agency](#32-get-my-agency)
  - [3.3 Update Agency](#33-update-agency)
  - [3.4 Get Public Agencies](#34-get-public-agencies)
- [4. Agent API](#4-agent-api)
  - [4.1 Create Agent](#41-create-agent)
  - [4.2 Get Agency Agents](#42-get-agency-agents)
  - [4.3 Update Agent](#43-update-agent)
  - [4.4 Delete Agent](#44-delete-agent)
- [5. Property API](#5-property-api)
  - [5.1 Create Property](#51-create-property)
  - [5.2 Get Properties](#52-get-properties)
  - [5.3 Get Property Details](#53-get-property-details)
  - [5.4 Update Property](#54-update-property)
  - [5.5 Delete Property](#55-delete-property)
  - [5.6 Upload Property Images](#56-upload-property-images)
  - [5.7 Get Map Properties](#57-get-map-properties)
  - [5.8 Moderate Property Status](#58-moderate-property-status)
- [6. Favorites API](#6-favorites-api)
  - [6.1 Add Property to Favorites](#61-add-property-to-favorites)
  - [6.2 Remove Property from Favorites](#62-remove-property-from-favorites)
  - [6.3 Get My Favorites](#63-get-my-favorites)
- [7. Inquiry API](#7-inquiry-api)
  - [7.1 Create Inquiry](#71-create-inquiry)
  - [7.2 Get Agency Inquiries](#72-get-agency-inquiries)
  - [7.3 Update Inquiry Status](#73-update-inquiry-status)
- [8. Viewing Request API](#8-viewing-request-api)
  - [8.1 Create Viewing Request](#81-create-viewing-request)
  - [8.2 Get Agency Viewing Requests](#82-get-agency-viewing-requests)
  - [8.3 Update Viewing Status](#83-update-viewing-status)
- [9. Subscription API](#9-subscription-api)
  - [9.1 Create Subscription Plan](#91-create-subscription-plan)
  - [9.2 Get Subscription Plans](#92-get-subscription-plans)
  - [9.3 Assign Subscription to Agency](#93-assign-subscription-to-agency)
  - [9.4 Get My Agency Subscription](#94-get-my-agency-subscription)
- [10. Dashboard API](#10-dashboard-api)
  - [10.1 Super Admin Dashboard](#101-super-admin-dashboard)
  - [10.2 Agency Dashboard](#102-agency-dashboard)
  - [10.3 Agent Dashboard](#103-agent-dashboard)
  - [10.4 Client Dashboard](#104-client-dashboard)
- [11. Enum Values](#11-enum-values)
- [12. Suggested Endpoint Summary](#12-suggested-endpoint-summary)
- [13. Common HTTP Status Codes](#13-common-http-status-codes)
- [14. Security Notes](#14-security-notes)

## Base URL

```txt
http://localhost:8080/api
```

---

## Authentication

Most protected endpoints require a JWT access token.

### Authorization Header

```http
Authorization: Bearer <access_token>
```

### Public Endpoints

These endpoints do not require authentication:

```txt
POST /auth/register
POST /auth/login
POST /auth/refresh-token
GET  /properties
GET  /properties/{id}
GET  /properties/map
GET  /agencies/public
GET  /agencies/public/{id}
```

### Protected Endpoints

Protected endpoints require a valid JWT token and the correct role.

### Roles

```txt
SUPER_ADMIN
AGENCY_ADMIN
AGENT
CLIENT
```

---

## Standard API Response Format

All successful responses should follow this format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {}
}
```

For paginated responses:

```json
{
  "success": true,
  "message": "Data fetched successfully",
  "data": {
    "content": [],
    "page": 0,
    "size": 10,
    "totalElements": 45,
    "totalPages": 5,
    "last": false
  }
}
```

For validation or error responses:

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be valid",
    "password": "Password must contain at least 8 characters"
  }
}
```

---

# 1. Authentication API

## 1.1 Register User

```http
POST /auth/register
```

### Authorization

```txt
Public
```

### Request Body

```json
{
  "firstName": "Elshan",
  "lastName": "Hasanov",
  "email": "elshan@example.com",
  "password": "Password123!",
  "phoneNumber": "+994501234567",
  "role": "CLIENT"
}
```

### Validation Rules

```txt
firstName   required, min 2 characters
lastName    required, min 2 characters
email       required, valid email, unique
password    required, min 8 characters
phoneNumber required
role        required, allowed: CLIENT, AGENCY_ADMIN
```

### Response

```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "firstName": "Elshan",
    "lastName": "Hasanov",
    "email": "elshan@example.com",
    "phoneNumber": "+994501234567",
    "role": "CLIENT",
    "enabled": true
  }
}
```

---

## 1.2 Login

```http
POST /auth/login
```

### Authorization

```txt
Public
```

### Request Body

```json
{
  "email": "elshan@example.com",
  "password": "Password123!"
}
```

### Response

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "jwt-access-token",
    "refreshToken": "jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "user": {
      "id": 1,
      "firstName": "Elshan",
      "lastName": "Hasanov",
      "email": "elshan@example.com",
      "role": "CLIENT"
    }
  }
}
```

---

## 1.3 Refresh Token

```http
POST /auth/refresh-token
```

### Authorization

```txt
Public
```

### Request Body

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### Response

```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "new-jwt-access-token",
    "refreshToken": "new-jwt-refresh-token",
    "tokenType": "Bearer",
    "expiresIn": 86400000
  }
}
```

---

## 1.4 Get Current User

```http
GET /auth/me
```

### Authorization

```txt
Authenticated user
```

### Headers

```http
Authorization: Bearer <access_token>
```

### Response

```json
{
  "success": true,
  "message": "Current user fetched successfully",
  "data": {
    "id": 1,
    "firstName": "Elshan",
    "lastName": "Hasanov",
    "email": "elshan@example.com",
    "phoneNumber": "+994501234567",
    "role": "CLIENT",
    "enabled": true
  }
}
```

---

## 1.5 Logout

```http
POST /auth/logout
```

### Authorization

```txt
Authenticated user
```

### Headers

```http
Authorization: Bearer <access_token>
```

### Response

```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

---

# 2. User Management API

## 2.1 Get All Users

```http
GET /admin/users?page=0&size=10&role=CLIENT&keyword=elshan
```

### Authorization

```txt
SUPER_ADMIN only
```

### Headers

```http
Authorization: Bearer <access_token>
```

### Query Parameters

```txt
page     optional, default 0
size     optional, default 10
role     optional
keyword  optional
```

### Response

```json
{
  "success": true,
  "message": "Users fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "firstName": "Elshan",
        "lastName": "Hasanov",
        "email": "elshan@example.com",
        "phoneNumber": "+994501234567",
        "role": "CLIENT",
        "enabled": true,
        "createdAt": "2026-06-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 2.2 Enable or Disable User

```http
PATCH /admin/users/{userId}/status
```

### Authorization

```txt
SUPER_ADMIN only
```

### Request Body

```json
{
  "enabled": false
}
```

### Response

```json
{
  "success": true,
  "message": "User status updated successfully",
  "data": {
    "id": 1,
    "email": "elshan@example.com",
    "enabled": false
  }
}
```

---

# 3. Agency API

## 3.1 Create Agency Profile

```http
POST /agencies
```

### Authorization

```txt
AGENCY_ADMIN only
```

### Headers

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Request Body

```json
{
  "name": "Baku Premium Estate",
  "description": "Real estate agency in Baku",
  "phoneNumber": "+994501112233",
  "email": "agency@example.com",
  "website": "https://agency.az",
  "city": "Baku",
  "address": "Nizami Street 10"
}
```

### Response

```json
{
  "success": true,
  "message": "Agency created successfully",
  "data": {
    "id": 1,
    "name": "Baku Premium Estate",
    "description": "Real estate agency in Baku",
    "phoneNumber": "+994501112233",
    "email": "agency@example.com",
    "website": "https://agency.az",
    "city": "Baku",
    "address": "Nizami Street 10",
    "status": "ACTIVE",
    "ownerId": 2
  }
}
```

---

## 3.2 Get My Agency

```http
GET /agencies/me
```

### Authorization

```txt
AGENCY_ADMIN only
```

### Response

```json
{
  "success": true,
  "message": "Agency fetched successfully",
  "data": {
    "id": 1,
    "name": "Baku Premium Estate",
    "description": "Real estate agency in Baku",
    "phoneNumber": "+994501112233",
    "email": "agency@example.com",
    "website": "https://agency.az",
    "city": "Baku",
    "address": "Nizami Street 10",
    "status": "ACTIVE"
  }
}
```

---

## 3.3 Update Agency

```http
PUT /agencies/{agencyId}
```

### Authorization

```txt
AGENCY_ADMIN who owns agency, or SUPER_ADMIN
```

### Request Body

```json
{
  "name": "Baku Premium Estate Updated",
  "description": "Updated agency description",
  "phoneNumber": "+994501112244",
  "email": "agency-updated@example.com",
  "website": "https://updated-agency.az",
  "city": "Baku",
  "address": "28 May Street 20"
}
```

### Response

```json
{
  "success": true,
  "message": "Agency updated successfully",
  "data": {
    "id": 1,
    "name": "Baku Premium Estate Updated",
    "description": "Updated agency description",
    "phoneNumber": "+994501112244",
    "email": "agency-updated@example.com",
    "website": "https://updated-agency.az",
    "city": "Baku",
    "address": "28 May Street 20",
    "status": "ACTIVE"
  }
}
```

---

## 3.4 Get Public Agencies

```http
GET /agencies/public?page=0&size=10&city=Baku
```

### Authorization

```txt
Public
```

### Response

```json
{
  "success": true,
  "message": "Agencies fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Baku Premium Estate",
        "description": "Real estate agency in Baku",
        "phoneNumber": "+994501112233",
        "city": "Baku",
        "address": "Nizami Street 10",
        "activeListingsCount": 42
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

# 4. Agent API

## 4.1 Create Agent

```http
POST /agencies/{agencyId}/agents
```

### Authorization

```txt
AGENCY_ADMIN who owns agency
```

### Request Body

```json
{
  "firstName": "Nigar",
  "lastName": "Aliyeva",
  "email": "nigar.agent@example.com",
  "phoneNumber": "+994551234567",
  "password": "Agent123!",
  "position": "Senior Agent"
}
```

### Response

```json
{
  "success": true,
  "message": "Agent created successfully",
  "data": {
    "id": 5,
    "firstName": "Nigar",
    "lastName": "Aliyeva",
    "email": "nigar.agent@example.com",
    "phoneNumber": "+994551234567",
    "role": "AGENT",
    "position": "Senior Agent",
    "agencyId": 1,
    "enabled": true
  }
}
```

---

## 4.2 Get Agency Agents

```http
GET /agencies/{agencyId}/agents?page=0&size=10
```

### Authorization

```txt
AGENCY_ADMIN who owns agency, AGENT of the agency, or SUPER_ADMIN
```

### Response

```json
{
  "success": true,
  "message": "Agents fetched successfully",
  "data": {
    "content": [
      {
        "id": 5,
        "firstName": "Nigar",
        "lastName": "Aliyeva",
        "email": "nigar.agent@example.com",
        "phoneNumber": "+994551234567",
        "position": "Senior Agent",
        "assignedPropertiesCount": 8,
        "enabled": true
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 4.3 Update Agent

```http
PUT /agents/{agentId}
```

### Authorization

```txt
AGENCY_ADMIN who owns the agent's agency
```

### Request Body

```json
{
  "firstName": "Nigar",
  "lastName": "Aliyeva",
  "phoneNumber": "+994551234500",
  "position": "Lead Agent",
  "enabled": true
}
```

### Response

```json
{
  "success": true,
  "message": "Agent updated successfully",
  "data": {
    "id": 5,
    "firstName": "Nigar",
    "lastName": "Aliyeva",
    "email": "nigar.agent@example.com",
    "phoneNumber": "+994551234500",
    "position": "Lead Agent",
    "enabled": true
  }
}
```

---

## 4.4 Delete Agent

```http
DELETE /agents/{agentId}
```

### Authorization

```txt
AGENCY_ADMIN who owns the agent's agency
```

### Response

```json
{
  "success": true,
  "message": "Agent deleted successfully",
  "data": null
}
```

---

# 5. Property API

## 5.1 Create Property

```http
POST /properties
```

### Authorization

```txt
AGENCY_ADMIN or AGENT
```

### Business Rules

```txt
AGENCY_ADMIN can create property for own agency.
AGENT can create property only for assigned agency if allowed.
Agency subscription must be active.
New properties should be created with PENDING status by default.
SUPER_ADMIN can approve property later.
```

### Request Body

```json
{
  "title": "Modern 3-room Apartment Near Metro",
  "description": "Bright apartment with city view and renovated interior.",
  "price": 185000,
  "city": "Baku",
  "district": "Narimanov",
  "address": "Ataturk Avenue 15",
  "propertyType": "APARTMENT",
  "listingType": "SALE",
  "area": 95.5,
  "rooms": 3,
  "bathrooms": 2,
  "floor": 7,
  "totalFloors": 16,
  "latitude": 40.4093,
  "longitude": 49.8671,
  "agentId": 5,
  "features": ["ELEVATOR", "PARKING", "BALCONY", "HEATING"]
}
```

### Response

```json
{
  "success": true,
  "message": "Property created successfully",
  "data": {
    "id": 10,
    "title": "Modern 3-room Apartment Near Metro",
    "description": "Bright apartment with city view and renovated interior.",
    "price": 185000,
    "city": "Baku",
    "district": "Narimanov",
    "address": "Ataturk Avenue 15",
    "propertyType": "APARTMENT",
    "listingType": "SALE",
    "area": 95.5,
    "rooms": 3,
    "bathrooms": 2,
    "floor": 7,
    "totalFloors": 16,
    "latitude": 40.4093,
    "longitude": 49.8671,
    "status": "PENDING",
    "agencyId": 1,
    "agent": {
      "id": 5,
      "fullName": "Nigar Aliyeva",
      "phoneNumber": "+994551234567"
    },
    "features": ["ELEVATOR", "PARKING", "BALCONY", "HEATING"],
    "createdAt": "2026-06-01T10:30:00"
  }
}
```

---

## 5.2 Get Properties

```http
GET /properties?page=0&size=12&city=Baku&propertyType=APARTMENT&listingType=SALE&minPrice=100000&maxPrice=250000&rooms=3&sort=createdAt,desc
```

### Authorization

```txt
Public
```

### Query Parameters

```txt
page          optional, default 0
size          optional, default 12
keyword       optional
city          optional
district      optional
propertyType  optional: APARTMENT, HOUSE, VILLA, OFFICE, LAND
listingType   optional: SALE, RENT
minPrice      optional
maxPrice      optional
rooms         optional
minArea       optional
maxArea       optional
sort          optional, examples: createdAt,desc or price,asc
```

### Response

```json
{
  "success": true,
  "message": "Properties fetched successfully",
  "data": {
    "content": [
      {
        "id": 10,
        "title": "Modern 3-room Apartment Near Metro",
        "price": 185000,
        "city": "Baku",
        "district": "Narimanov",
        "propertyType": "APARTMENT",
        "listingType": "SALE",
        "area": 95.5,
        "rooms": 3,
        "mainImageUrl": "https://example.com/images/property-10.jpg",
        "status": "ACTIVE",
        "featured": false,
        "createdAt": "2026-06-01T10:30:00"
      }
    ],
    "page": 0,
    "size": 12,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 5.3 Get Property Details

```http
GET /properties/{propertyId}
```

### Authorization

```txt
Public
```

### Response

```json
{
  "success": true,
  "message": "Property fetched successfully",
  "data": {
    "id": 10,
    "title": "Modern 3-room Apartment Near Metro",
    "description": "Bright apartment with city view and renovated interior.",
    "price": 185000,
    "city": "Baku",
    "district": "Narimanov",
    "address": "Ataturk Avenue 15",
    "propertyType": "APARTMENT",
    "listingType": "SALE",
    "area": 95.5,
    "rooms": 3,
    "bathrooms": 2,
    "floor": 7,
    "totalFloors": 16,
    "latitude": 40.4093,
    "longitude": 49.8671,
    "status": "ACTIVE",
    "featured": false,
    "images": [
      {
        "id": 1,
        "url": "https://example.com/images/property-10-1.jpg",
        "main": true
      }
    ],
    "features": ["ELEVATOR", "PARKING", "BALCONY", "HEATING"],
    "agency": {
      "id": 1,
      "name": "Baku Premium Estate",
      "phoneNumber": "+994501112233"
    },
    "agent": {
      "id": 5,
      "fullName": "Nigar Aliyeva",
      "phoneNumber": "+994551234567"
    }
  }
}
```

---

## 5.4 Update Property

```http
PUT /properties/{propertyId}
```

### Authorization

```txt
AGENCY_ADMIN who owns property, assigned AGENT, or SUPER_ADMIN
```

### Request Body

```json
{
  "title": "Updated 3-room Apartment Near Metro",
  "description": "Updated description",
  "price": 178000,
  "city": "Baku",
  "district": "Narimanov",
  "address": "Ataturk Avenue 15",
  "propertyType": "APARTMENT",
  "listingType": "SALE",
  "area": 95.5,
  "rooms": 3,
  "bathrooms": 2,
  "floor": 7,
  "totalFloors": 16,
  "latitude": 40.4093,
  "longitude": 49.8671,
  "agentId": 5,
  "features": ["ELEVATOR", "PARKING", "BALCONY"]
}
```

### Response

```json
{
  "success": true,
  "message": "Property updated successfully",
  "data": {
    "id": 10,
    "title": "Updated 3-room Apartment Near Metro",
    "price": 178000,
    "status": "PENDING",
    "updatedAt": "2026-06-01T11:00:00"
  }
}
```

---

## 5.5 Delete Property

```http
DELETE /properties/{propertyId}
```

### Authorization

```txt
AGENCY_ADMIN who owns property, assigned AGENT, or SUPER_ADMIN
```

### Response

```json
{
  "success": true,
  "message": "Property deleted successfully",
  "data": null
}
```

---

## 5.6 Upload Property Images

```http
POST /properties/{propertyId}/images
```

### Authorization

```txt
AGENCY_ADMIN who owns property, assigned AGENT, or SUPER_ADMIN
```

### Headers

```http
Authorization: Bearer <access_token>
Content-Type: multipart/form-data
```

### Request Body

```txt
files: image1.jpg, image2.jpg
```

### Response

```json
{
  "success": true,
  "message": "Images uploaded successfully",
  "data": [
    {
      "id": 1,
      "url": "https://example.com/uploads/property-10/image1.jpg",
      "main": true
    },
    {
      "id": 2,
      "url": "https://example.com/uploads/property-10/image2.jpg",
      "main": false
    }
  ]
}
```

---

## 5.7 Get Map Properties

```http
GET /properties/map?city=Baku&listingType=SALE&minPrice=100000&maxPrice=250000
```

### Authorization

```txt
Public
```

### Response

```json
{
  "success": true,
  "message": "Map properties fetched successfully",
  "data": [
    {
      "id": 10,
      "title": "Modern 3-room Apartment Near Metro",
      "price": 185000,
      "listingType": "SALE",
      "propertyType": "APARTMENT",
      "latitude": 40.4093,
      "longitude": 49.8671,
      "mainImageUrl": "https://example.com/images/property-10.jpg"
    }
  ]
}
```

---

## 5.8 Moderate Property Status

```http
PATCH /admin/properties/{propertyId}/status
```

### Authorization

```txt
SUPER_ADMIN only
```

### Request Body

```json
{
  "status": "ACTIVE",
  "rejectionReason": null
}
```

### Response

```json
{
  "success": true,
  "message": "Property status updated successfully",
  "data": {
    "id": 10,
    "status": "ACTIVE",
    "rejectionReason": null
  }
}
```

---

# 6. Favorites API

## 6.1 Add Property to Favorites

```http
POST /favorites/{propertyId}
```

### Authorization

```txt
CLIENT only
```

### Response

```json
{
  "success": true,
  "message": "Property added to favorites",
  "data": {
    "id": 1,
    "propertyId": 10,
    "userId": 1,
    "createdAt": "2026-06-01T12:00:00"
  }
}
```

---

## 6.2 Remove Property from Favorites

```http
DELETE /favorites/{propertyId}
```

### Authorization

```txt
CLIENT only
```

### Response

```json
{
  "success": true,
  "message": "Property removed from favorites",
  "data": null
}
```

---

## 6.3 Get My Favorites

```http
GET /favorites/me?page=0&size=12
```

### Authorization

```txt
CLIENT only
```

### Response

```json
{
  "success": true,
  "message": "Favorites fetched successfully",
  "data": {
    "content": [
      {
        "id": 10,
        "title": "Modern 3-room Apartment Near Metro",
        "price": 185000,
        "city": "Baku",
        "mainImageUrl": "https://example.com/images/property-10.jpg"
      }
    ],
    "page": 0,
    "size": 12,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

# 7. Inquiry API

## 7.1 Create Inquiry

```http
POST /properties/{propertyId}/inquiries
```

### Authorization

```txt
CLIENT only
```

### Request Body

```json
{
  "message": "Hello, I am interested in this property. Is it still available?",
  "preferredContactMethod": "PHONE"
}
```

### Response

```json
{
  "success": true,
  "message": "Inquiry sent successfully",
  "data": {
    "id": 1,
    "propertyId": 10,
    "clientId": 1,
    "message": "Hello, I am interested in this property. Is it still available?",
    "preferredContactMethod": "PHONE",
    "status": "NEW",
    "createdAt": "2026-06-01T12:20:00"
  }
}
```

---

## 7.2 Get Agency Inquiries

```http
GET /agencies/{agencyId}/inquiries?page=0&size=10&status=NEW
```

### Authorization

```txt
AGENCY_ADMIN who owns agency, AGENT of agency, or SUPER_ADMIN
```

### Response

```json
{
  "success": true,
  "message": "Inquiries fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "propertyId": 10,
        "propertyTitle": "Modern 3-room Apartment Near Metro",
        "clientFullName": "Elshan Hasanov",
        "clientPhoneNumber": "+994501234567",
        "message": "Hello, I am interested in this property. Is it still available?",
        "preferredContactMethod": "PHONE",
        "status": "NEW",
        "createdAt": "2026-06-01T12:20:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 7.3 Update Inquiry Status

```http
PATCH /inquiries/{inquiryId}/status
```

### Authorization

```txt
AGENCY_ADMIN who owns inquiry's agency, assigned AGENT, or SUPER_ADMIN
```

### Request Body

```json
{
  "status": "CONTACTED"
}
```

### Response

```json
{
  "success": true,
  "message": "Inquiry status updated successfully",
  "data": {
    "id": 1,
    "status": "CONTACTED"
  }
}
```

---

# 8. Viewing Request API

## 8.1 Create Viewing Request

```http
POST /properties/{propertyId}/viewings
```

### Authorization

```txt
CLIENT only
```

### Request Body

```json
{
  "preferredDateTime": "2026-06-05T15:30:00",
  "note": "I would like to see the apartment after 3 PM."
}
```

### Response

```json
{
  "success": true,
  "message": "Viewing request created successfully",
  "data": {
    "id": 1,
    "propertyId": 10,
    "clientId": 1,
    "preferredDateTime": "2026-06-05T15:30:00",
    "note": "I would like to see the apartment after 3 PM.",
    "status": "PENDING",
    "createdAt": "2026-06-01T12:40:00"
  }
}
```

---

## 8.2 Get Agency Viewing Requests

```http
GET /agencies/{agencyId}/viewings?page=0&size=10&status=PENDING
```

### Authorization

```txt
AGENCY_ADMIN who owns agency, AGENT of agency, or SUPER_ADMIN
```

### Response

```json
{
  "success": true,
  "message": "Viewing requests fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "propertyId": 10,
        "propertyTitle": "Modern 3-room Apartment Near Metro",
        "clientFullName": "Elshan Hasanov",
        "clientPhoneNumber": "+994501234567",
        "preferredDateTime": "2026-06-05T15:30:00",
        "note": "I would like to see the apartment after 3 PM.",
        "status": "PENDING"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 8.3 Update Viewing Status

```http
PATCH /viewings/{viewingId}/status
```

### Authorization

```txt
AGENCY_ADMIN who owns viewing's agency, assigned AGENT, or SUPER_ADMIN
```

### Request Body

```json
{
  "status": "APPROVED",
  "responseNote": "Approved. Agent will contact you before the viewing."
}
```

### Response

```json
{
  "success": true,
  "message": "Viewing status updated successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "responseNote": "Approved. Agent will contact you before the viewing."
  }
}
```

---

# 9. Subscription API

## 9.1 Create Subscription Plan

```http
POST /admin/subscription-plans
```

### Authorization

```txt
SUPER_ADMIN only
```

### Request Body

```json
{
  "name": "Professional",
  "description": "For growing real estate agencies",
  "price": 79.99,
  "durationDays": 30,
  "maxListings": 200,
  "maxAgents": 20,
  "featuredListingsAllowed": true
}
```

### Response

```json
{
  "success": true,
  "message": "Subscription plan created successfully",
  "data": {
    "id": 1,
    "name": "Professional",
    "description": "For growing real estate agencies",
    "price": 79.99,
    "durationDays": 30,
    "maxListings": 200,
    "maxAgents": 20,
    "featuredListingsAllowed": true,
    "active": true
  }
}
```

---

## 9.2 Get Subscription Plans

```http
GET /subscription-plans
```

### Authorization

```txt
Public
```

### Response

```json
{
  "success": true,
  "message": "Subscription plans fetched successfully",
  "data": [
    {
      "id": 1,
      "name": "Professional",
      "description": "For growing real estate agencies",
      "price": 79.99,
      "durationDays": 30,
      "maxListings": 200,
      "maxAgents": 20,
      "featuredListingsAllowed": true
    }
  ]
}
```

---

## 9.3 Assign Subscription to Agency

```http
POST /admin/agencies/{agencyId}/subscription
```

### Authorization

```txt
SUPER_ADMIN only
```

### Request Body

```json
{
  "planId": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-07-01",
  "active": true
}
```

### Response

```json
{
  "success": true,
  "message": "Subscription assigned successfully",
  "data": {
    "id": 1,
    "agencyId": 1,
    "planId": 1,
    "planName": "Professional",
    "startDate": "2026-06-01",
    "endDate": "2026-07-01",
    "active": true
  }
}
```

---

## 9.4 Get My Agency Subscription

```http
GET /agencies/me/subscription
```

### Authorization

```txt
AGENCY_ADMIN only
```

### Response

```json
{
  "success": true,
  "message": "Subscription fetched successfully",
  "data": {
    "planName": "Professional",
    "startDate": "2026-06-01",
    "endDate": "2026-07-01",
    "active": true,
    "maxListings": 200,
    "usedListings": 42,
    "maxAgents": 20,
    "usedAgents": 5
  }
}
```

---

# 10. Dashboard API

## 10.1 Super Admin Dashboard

```http
GET /dashboard/admin
```

### Authorization

```txt
SUPER_ADMIN only
```

### Response

```json
{
  "success": true,
  "message": "Admin dashboard fetched successfully",
  "data": {
    "totalUsers": 120,
    "totalAgencies": 15,
    "totalAgents": 40,
    "totalProperties": 350,
    "pendingProperties": 18,
    "activeSubscriptions": 12,
    "latestProperties": [
      {
        "id": 10,
        "title": "Modern 3-room Apartment Near Metro",
        "status": "PENDING"
      }
    ]
  }
}
```

---

## 10.2 Agency Dashboard

```http
GET /dashboard/agency
```

### Authorization

```txt
AGENCY_ADMIN only
```

### Response

```json
{
  "success": true,
  "message": "Agency dashboard fetched successfully",
  "data": {
    "totalProperties": 42,
    "activeProperties": 35,
    "pendingProperties": 4,
    "soldProperties": 2,
    "rentedProperties": 1,
    "totalAgents": 5,
    "newInquiries": 8,
    "upcomingViewings": 6,
    "subscriptionActive": true
  }
}
```

---

## 10.3 Agent Dashboard

```http
GET /dashboard/agent
```

### Authorization

```txt
AGENT only
```

### Response

```json
{
  "success": true,
  "message": "Agent dashboard fetched successfully",
  "data": {
    "assignedProperties": 8,
    "activeProperties": 7,
    "newInquiries": 3,
    "upcomingViewings": 2
  }
}
```

---

## 10.4 Client Dashboard

```http
GET /dashboard/client
```

### Authorization

```txt
CLIENT only
```

### Response

```json
{
  "success": true,
  "message": "Client dashboard fetched successfully",
  "data": {
    "favoriteProperties": 6,
    "sentInquiries": 3,
    "viewingRequests": 2,
    "approvedViewings": 1
  }
}
```

---

# 11. Enum Values

## User Roles

```txt
SUPER_ADMIN
AGENCY_ADMIN
AGENT
CLIENT
```

## Property Type

```txt
APARTMENT
HOUSE
VILLA
OFFICE
LAND
```

## Listing Type

```txt
SALE
RENT
```

## Property Status

```txt
PENDING
ACTIVE
REJECTED
SOLD
RENTED
```

## Inquiry Status

```txt
NEW
CONTACTED
CLOSED
```

## Viewing Status

```txt
PENDING
APPROVED
REJECTED
COMPLETED
CANCELLED
```

## Preferred Contact Method

```txt
PHONE
EMAIL
WHATSAPP
```

---

# 12. Suggested Endpoint Summary

| Module | Method | Endpoint | Authorization |
|---|---:|---|---|
| Auth | POST | `/auth/register` | Public |
| Auth | POST | `/auth/login` | Public |
| Auth | POST | `/auth/refresh-token` | Public |
| Auth | GET | `/auth/me` | Authenticated |
| Users | GET | `/admin/users` | SUPER_ADMIN |
| Users | PATCH | `/admin/users/{userId}/status` | SUPER_ADMIN |
| Agencies | POST | `/agencies` | AGENCY_ADMIN |
| Agencies | GET | `/agencies/me` | AGENCY_ADMIN |
| Agencies | PUT | `/agencies/{agencyId}` | AGENCY_ADMIN / SUPER_ADMIN |
| Agencies | GET | `/agencies/public` | Public |
| Agents | POST | `/agencies/{agencyId}/agents` | AGENCY_ADMIN |
| Agents | GET | `/agencies/{agencyId}/agents` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Agents | PUT | `/agents/{agentId}` | AGENCY_ADMIN |
| Agents | DELETE | `/agents/{agentId}` | AGENCY_ADMIN |
| Properties | POST | `/properties` | AGENCY_ADMIN / AGENT |
| Properties | GET | `/properties` | Public |
| Properties | GET | `/properties/{propertyId}` | Public |
| Properties | PUT | `/properties/{propertyId}` | Owner Agency / Assigned Agent / SUPER_ADMIN |
| Properties | DELETE | `/properties/{propertyId}` | Owner Agency / Assigned Agent / SUPER_ADMIN |
| Properties | POST | `/properties/{propertyId}/images` | Owner Agency / Assigned Agent / SUPER_ADMIN |
| Map | GET | `/properties/map` | Public |
| Moderation | PATCH | `/admin/properties/{propertyId}/status` | SUPER_ADMIN |
| Favorites | POST | `/favorites/{propertyId}` | CLIENT |
| Favorites | DELETE | `/favorites/{propertyId}` | CLIENT |
| Favorites | GET | `/favorites/me` | CLIENT |
| Inquiries | POST | `/properties/{propertyId}/inquiries` | CLIENT |
| Inquiries | GET | `/agencies/{agencyId}/inquiries` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Inquiries | PATCH | `/inquiries/{inquiryId}/status` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Viewings | POST | `/properties/{propertyId}/viewings` | CLIENT |
| Viewings | GET | `/agencies/{agencyId}/viewings` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Viewings | PATCH | `/viewings/{viewingId}/status` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Plans | POST | `/admin/subscription-plans` | SUPER_ADMIN |
| Plans | GET | `/subscription-plans` | Public |
| Subscriptions | POST | `/admin/agencies/{agencyId}/subscription` | SUPER_ADMIN |
| Subscriptions | GET | `/agencies/me/subscription` | AGENCY_ADMIN |
| Dashboard | GET | `/dashboard/admin` | SUPER_ADMIN |
| Dashboard | GET | `/dashboard/agency` | AGENCY_ADMIN |
| Dashboard | GET | `/dashboard/agent` | AGENT |
| Dashboard | GET | `/dashboard/client` | CLIENT |

---

# 13. Common HTTP Status Codes

```txt
200 OK                  Request completed successfully
201 Created             Resource created successfully
400 Bad Request         Validation or request error
401 Unauthorized        Missing or invalid token
403 Forbidden           User does not have permission
404 Not Found           Resource not found
409 Conflict            Duplicate or business conflict
500 Internal Error      Unexpected server error
```

---

# 14. Security Notes

- Passwords must be stored using BCrypt hashing.
- JWT secret must be stored in environment variables, not hardcoded.
- Access tokens should be short-lived.
- Refresh tokens should be stored securely.
- Every protected endpoint must validate the user role.
- Agency users must not access another agency's private data.
- Agents must only manage assigned or agency-owned resources.
- Public property endpoints should only return ACTIVE listings.
- PENDING and REJECTED listings should only be visible to authorized agency users and admins.
