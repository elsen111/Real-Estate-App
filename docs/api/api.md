# EstateFlow API Documentation

> **Version:** 2.0.0 | **Base URL:** `http://localhost:8080/api`

EstateFlow is a RESTful backend for a real estate CRM and marketplace platform. This document covers all endpoints, request/response DTOs, validation rules, business rules, role authorization, and enum values.

---

## Table of Contents

- [Base URL & Auth](#base-url--auth)
- [Roles](#roles)
- [Standard Response Format](#standard-response-format)
- [Global DTOs](#global-dtos)
- [1. Authentication API](#1-authentication-api)
- [2. User Profile API](#2-user-profile-api)
- [3. Admin — User Management API](#3-admin--user-management-api)
- [4. Admin — Agency Management API](#4-admin--agency-management-api)
- [5. Admin — Property Moderation API](#5-admin--property-moderation-api)
- [6. Admin — Subscription Management API](#6-admin--subscription-management-api)
- [7. Agency API](#7-agency-api)
- [8. Agent API](#8-agent-api)
- [9. Property API](#9-property-api)
- [10. Media API](#10-media-api)
- [11. Favorites API](#11-favorites-api)
- [12. Inquiry API](#12-inquiry-api)
- [13. Viewing Request API](#13-viewing-request-api)
- [14. Subscription Plan API](#14-subscription-plan-api)
- [15. Notification API](#15-notification-api)
- [16. Dashboard API](#16-dashboard-api)
- [17. Enum Values](#17-enum-values)
- [18. Controller Map](#18-controller-map)
- [19. HTTP Status Codes](#19-http-status-codes)
- [20. Security Notes](#20-security-notes)

---

## Base URL & Auth

```
http://localhost:8080/api
```

### Authorization Header

```http
Authorization: Bearer <access_token>
```

### Public Endpoints (no token required)

```
POST /auth/register
POST /auth/login
POST /auth/refresh-token
POST /auth/forgot-password
POST /auth/reset-password
GET  /properties
GET  /properties/{id}
GET  /properties/map
GET  /properties/featured
GET  /properties/recent
GET  /properties/search/suggestions
GET  /agencies/public
GET  /agencies/public/{id}
GET  /agencies/public/{id}/properties
GET  /subscription-plans
```

---

## Roles

```
SUPER_ADMIN     — platform administrator
AGENCY_ADMIN    — owns and manages an agency
AGENT           — works under an agency
CLIENT          — end user browsing/inquiring
```

---

## Standard Response Format

### Success

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {}
}
```

### Paginated

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

### Validation Error

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

### Business / General Error

```json
{
  "success": false,
  "message": "Agency subscription is not active",
  "errors": null
}
```

---

## Global DTOs

These DTOs are reused across multiple endpoints.

### UserSummaryDTO

```json
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
```

### AgencySummaryDTO

```json
{
  "id": 1,
  "name": "Baku Premium Estate",
  "city": "Baku",
  "phoneNumber": "+994501112233",
  "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg"
}
```

### PropertySummaryDTO

```json
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
  "mainImageUrl": "https://example.com/uploads/properties/10/main.jpg",
  "status": "ACTIVE",
  "featured": false,
  "createdAt": "2026-06-01T10:30:00"
}
```

### MediaFileDTO

```json
{
  "id": "uuid",
  "fileUrl": "https://example.com/uploads/properties/10/image1.jpg",
  "fileName": "image1.jpg",
  "fileType": "image/jpeg",
  "fileSize": 204800,
  "mediaPurpose": "PROPERTY_IMAGE",
  "isMain": true,
  "sortOrder": 0
}
```

### PageRequestDTO (query params)

```
page     integer, default 0
size     integer, default 10 or 12
sort     string, e.g. createdAt,desc or price,asc
keyword  string, optional full-text search
```

---

# 1. Authentication API

**Controller:** `AuthController` | **Prefix:** `/auth`

---

## 1.1 Register User

```http
POST /auth/register
```

**Authorization:** Public

### Request DTO — `RegisterRequestDTO`

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

| Field | Rules |
|---|---|
| firstName | required, min 2 chars, max 50 chars |
| lastName | required, min 2 chars, max 50 chars |
| email | required, valid email format, unique |
| password | required, min 8 chars, at least 1 uppercase, 1 digit, 1 special char |
| phoneNumber | required, valid phone format |
| role | required, only `CLIENT` or `AGENCY_ADMIN` allowed |

### Response DTO — `UserResponseDTO`

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
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

---

## 1.2 Login

```http
POST /auth/login
```

**Authorization:** Public

### Request DTO — `LoginRequestDTO`

```json
{
  "email": "elshan@example.com",
  "password": "Password123!"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| email | required, valid email |
| password | required |

### Response DTO — `AuthResponseDTO`

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

### Business Rules

```
- Return 401 if credentials are invalid
- Return 403 if account is disabled (enabled = false)
```

---

## 1.3 Refresh Token

```http
POST /auth/refresh-token
```

**Authorization:** Public

### Request DTO — `RefreshTokenRequestDTO`

```json
{
  "refreshToken": "jwt-refresh-token"
}
```

### Response DTO — `TokenResponseDTO`

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

## 1.4 Forgot Password

```http
POST /auth/forgot-password
```

**Authorization:** Public

### Request DTO — `ForgotPasswordRequestDTO`

```json
{
  "email": "elshan@example.com"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| email | required, valid email |

### Response

```json
{
  "success": true,
  "message": "If this email is registered, a password reset link has been sent.",
  "data": null
}
```

### Business Rules

```
- Always return 200 regardless of whether email exists (prevents enumeration attacks)
- Generate a secure random token, store hashed with expiry (e.g. 15 minutes)
- Send reset link: https://yourfrontend.com/reset-password?token=<token>
```

---

## 1.5 Reset Password

```http
POST /auth/reset-password
```

**Authorization:** Public

### Request DTO — `ResetPasswordRequestDTO`

```json
{
  "token": "secure-reset-token",
  "newPassword": "NewPassword123!",
  "confirmPassword": "NewPassword123!"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| token | required |
| newPassword | required, min 8 chars, at least 1 uppercase, 1 digit, 1 special char |
| confirmPassword | required, must match newPassword |

### Response

```json
{
  "success": true,
  "message": "Password reset successfully",
  "data": null
}
```

### Business Rules

```
- Return 400 if token is invalid or expired
- Invalidate token after successful use
- Invalidate all existing refresh tokens for the user
```

---

## 1.6 Change Password

```http
PATCH /auth/change-password
```

**Authorization:** Authenticated (any role)

### Request DTO — `ChangePasswordRequestDTO`

```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword456!",
  "confirmPassword": "NewPassword456!"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| currentPassword | required |
| newPassword | required, min 8 chars, at least 1 uppercase, 1 digit, 1 special char |
| confirmPassword | required, must match newPassword |

### Response

```json
{
  "success": true,
  "message": "Password changed successfully",
  "data": null
}
```

### Business Rules

```
- Return 400 if currentPassword does not match stored BCrypt hash
- newPassword must differ from currentPassword
- Invalidate all existing refresh tokens after change
```

---

## 1.7 Get Current User

```http
GET /auth/me
```

**Authorization:** Authenticated (any role)

### Response DTO — `UserResponseDTO`

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
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

---

## 1.8 Logout

```http
POST /auth/logout
```

**Authorization:** Authenticated (any role)

### Response

```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

### Business Rules

```
- Invalidate / blacklist the current refresh token server-side
```

---

# 2. User Profile API

**Controller:** `UserProfileController` | **Prefix:** `/users`

---

## 2.1 Update My Profile

```http
PATCH /users/me/profile
```

**Authorization:** Authenticated (any role)

### Request DTO — `UpdateProfileRequestDTO`

```json
{
  "firstName": "Elshan",
  "lastName": "Mammadov",
  "phoneNumber": "+994501234599"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| firstName | optional, min 2 chars, max 50 chars |
| lastName | optional, min 2 chars, max 50 chars |
| phoneNumber | optional, valid phone format |

### Response DTO — `UserResponseDTO`

```json
{
  "success": true,
  "message": "Profile updated successfully",
  "data": {
    "id": 1,
    "firstName": "Elshan",
    "lastName": "Mammadov",
    "email": "elshan@example.com",
    "phoneNumber": "+994501234599",
    "role": "CLIENT",
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

---

## 2.2 Upload Profile Photo

```http
POST /users/me/photo
```

**Authorization:** Authenticated (any role)

**Content-Type:** `multipart/form-data`

### Request

```
file: profile.jpg   (JPEG/PNG, max 5MB)
```

### Response

```json
{
  "success": true,
  "message": "Profile photo uploaded successfully",
  "data": {
    "photoUrl": "https://example.com/uploads/users/1/avatar.jpg"
  }
}
```

---

## 2.3 Delete My Account

```http
DELETE /users/me
```

**Authorization:** Authenticated (any role)

### Request DTO — `DeleteAccountRequestDTO`

```json
{
  "password": "Password123!"
}
```

### Response

```json
{
  "success": true,
  "message": "Account deleted successfully",
  "data": null
}
```

### Business Rules

```
- Verify password before deletion
- Soft-delete: set enabled = false and anonymize PII (email, phone)
- AGENCY_ADMIN cannot delete account if agency has active listings
- Invalidate all tokens
```

---

# 3. Admin — User Management API

**Controller:** `AdminUserController` | **Prefix:** `/admin/users`

**Authorization:** `SUPER_ADMIN` only (all endpoints)

---

## 3.1 Get All Users

```http
GET /admin/users
```

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 10 | Page size |
| role | string | — | Filter by role |
| keyword | string | — | Search by name/email |
| enabled | boolean | — | Filter by status |

### Response DTO — Paginated `UserResponseDTO`

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
    "totalElements": 120,
    "totalPages": 12,
    "last": false
  }
}
```

---

## 3.2 Get User by ID

```http
GET /admin/users/{userId}
```

### Response DTO — `UserResponseDTO`

```json
{
  "success": true,
  "message": "User fetched successfully",
  "data": {
    "id": 1,
    "firstName": "Elshan",
    "lastName": "Hasanov",
    "email": "elshan@example.com",
    "phoneNumber": "+994501234567",
    "role": "CLIENT",
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

---

## 3.3 Enable or Disable User

```http
PATCH /admin/users/{userId}/status
```

### Request DTO — `UpdateUserStatusRequestDTO`

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

### Business Rules

```
- SUPER_ADMIN cannot disable their own account
```

---

## 3.4 Delete User

```http
DELETE /admin/users/{userId}
```

### Response

```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null
}
```

### Business Rules

```
- Soft-delete only; anonymize PII
- Cannot delete SUPER_ADMIN accounts
- Cannot delete AGENCY_ADMIN if agency has active listings
```

---

# 4. Admin — Agency Management API

**Controller:** `AdminAgencyController` | **Prefix:** `/admin/agencies`

**Authorization:** `SUPER_ADMIN` only (all endpoints)

---

## 4.1 Get All Agencies

```http
GET /admin/agencies
```

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 10 | Page size |
| city | string | — | Filter by city |
| status | string | — | Filter by status |
| keyword | string | — | Search by name |

### Response DTO — Paginated `AgencyAdminResponseDTO`

```json
{
  "success": true,
  "message": "Agencies fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Baku Premium Estate",
        "email": "agency@example.com",
        "phoneNumber": "+994501112233",
        "city": "Baku",
        "status": "ACTIVE",
        "ownerId": 2,
        "ownerEmail": "owner@example.com",
        "activeListingsCount": 42,
        "subscriptionActive": true,
        "createdAt": "2026-05-01T09:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2,
    "last": false
  }
}
```

---

## 4.2 Get Agency by ID (Admin)

```http
GET /admin/agencies/{agencyId}
```

### Response DTO — `AgencyDetailAdminResponseDTO`

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
    "status": "ACTIVE",
    "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg",
    "coverUrl": "https://example.com/uploads/agencies/1/cover.jpg",
    "ownerId": 2,
    "ownerEmail": "owner@example.com",
    "totalAgents": 5,
    "totalProperties": 42,
    "activeListingsCount": 35,
    "subscription": {
      "planName": "Professional",
      "startDate": "2026-06-01",
      "endDate": "2026-07-01",
      "active": true
    },
    "createdAt": "2026-05-01T09:00:00"
  }
}
```

---

## 4.3 Update Agency Status

```http
PATCH /admin/agencies/{agencyId}/status
```

### Request DTO — `UpdateAgencyStatusRequestDTO`

```json
{
  "status": "SUSPENDED",
  "reason": "Violation of platform terms"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| status | required, one of: `ACTIVE`, `SUSPENDED`, `INACTIVE` |
| reason | optional |

### Response

```json
{
  "success": true,
  "message": "Agency status updated successfully",
  "data": {
    "id": 1,
    "status": "SUSPENDED"
  }
}
```

---

## 4.4 Delete Agency

```http
DELETE /admin/agencies/{agencyId}
```

### Response

```json
{
  "success": true,
  "message": "Agency deleted successfully",
  "data": null
}
```

### Business Rules

```
- Soft-delete agency
- Cascade: deactivate all agents and listings under the agency
- Notify agency owner via notification
```

---

# 5. Admin — Property Moderation API

**Controller:** `AdminPropertyController` | **Prefix:** `/admin/properties`

**Authorization:** `SUPER_ADMIN` only (all endpoints)

---

## 5.1 Get All Properties (Admin)

```http
GET /admin/properties
```

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 12 | Page size |
| status | string | — | Filter by status |
| city | string | — | Filter by city |
| agencyId | integer | — | Filter by agency |
| keyword | string | — | Search by title |

### Response DTO — Paginated `PropertySummaryDTO`

```json
{
  "success": true,
  "message": "Properties fetched successfully",
  "data": {
    "content": [ /* PropertySummaryDTO objects */ ],
    "page": 0,
    "size": 12,
    "totalElements": 350,
    "totalPages": 30,
    "last": false
  }
}
```

---

## 5.2 Moderate Property Status

```http
PATCH /admin/properties/{propertyId}/status
```

### Request DTO — `ModeratePropertyRequestDTO`

```json
{
  "status": "ACTIVE",
  "rejectionReason": null
}
```

### Validation Rules

| Field | Rules |
|---|---|
| status | required, one of: `ACTIVE`, `REJECTED` |
| rejectionReason | required when status is `REJECTED`, max 500 chars |

### Response DTO — `PropertyModerationResponseDTO`

```json
{
  "success": true,
  "message": "Property status updated successfully",
  "data": {
    "id": 10,
    "title": "Modern 3-room Apartment Near Metro",
    "status": "ACTIVE",
    "rejectionReason": null,
    "moderatedAt": "2026-06-02T09:00:00"
  }
}
```

### Business Rules

```
- Notify agency via notification when property is ACTIVE or REJECTED
- Include rejectionReason in notification when REJECTED
```

---

# 6. Admin — Subscription Management API

**Controller:** `AdminSubscriptionController` | **Prefix:** `/admin`

**Authorization:** `SUPER_ADMIN` only (all endpoints)

---

## 6.1 Create Subscription Plan

```http
POST /admin/subscription-plans
```

### Request DTO — `CreateSubscriptionPlanRequestDTO`

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

### Validation Rules

| Field | Rules |
|---|---|
| name | required, unique, max 100 chars |
| description | optional, max 500 chars |
| price | required, min 0 |
| durationDays | required, min 1 |
| maxListings | required, min 1 |
| maxAgents | required, min 1 |
| featuredListingsAllowed | required, boolean |

### Response DTO — `SubscriptionPlanResponseDTO`

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
    "active": true,
    "createdAt": "2026-06-01T08:00:00"
  }
}
```

---

## 6.2 Get All Subscription Plans (Admin)

```http
GET /admin/subscription-plans
```

### Query Parameters

| Param | Type | Description |
|---|---|---|
| active | boolean | Filter by active status |

### Response DTO — List of `SubscriptionPlanResponseDTO`

```json
{
  "success": true,
  "message": "Subscription plans fetched successfully",
  "data": [ /* SubscriptionPlanResponseDTO objects including inactive */ ]
}
```

---

## 6.3 Get Subscription Plan by ID (Admin)

```http
GET /admin/subscription-plans/{planId}
```

### Response DTO — `SubscriptionPlanResponseDTO`

```json
{
  "success": true,
  "message": "Subscription plan fetched successfully",
  "data": {
    "id": 1,
    "name": "Professional",
    "description": "For growing real estate agencies",
    "price": 79.99,
    "durationDays": 30,
    "maxListings": 200,
    "maxAgents": 20,
    "featuredListingsAllowed": true,
    "active": true,
    "createdAt": "2026-06-01T08:00:00"
  }
}
```

---

## 6.4 Update Subscription Plan

```http
PUT /admin/subscription-plans/{planId}
```

### Request DTO — `UpdateSubscriptionPlanRequestDTO`

```json
{
  "name": "Professional Plus",
  "description": "Updated description",
  "price": 89.99,
  "durationDays": 30,
  "maxListings": 250,
  "maxAgents": 25,
  "featuredListingsAllowed": true
}
```

### Response DTO — `SubscriptionPlanResponseDTO`

```json
{
  "success": true,
  "message": "Subscription plan updated successfully",
  "data": { /* SubscriptionPlanResponseDTO */ }
}
```

### Business Rules

```
- Cannot update a plan that has active agency subscriptions attached
```

---

## 6.5 Toggle Subscription Plan Status

```http
PATCH /admin/subscription-plans/{planId}/status
```

### Request DTO — `TogglePlanStatusRequestDTO`

```json
{
  "active": false
}
```

### Response

```json
{
  "success": true,
  "message": "Subscription plan status updated successfully",
  "data": {
    "id": 1,
    "active": false
  }
}
```

---

## 6.6 Delete Subscription Plan

```http
DELETE /admin/subscription-plans/{planId}
```

### Response

```json
{
  "success": true,
  "message": "Subscription plan deleted successfully",
  "data": null
}
```

### Business Rules

```
- Cannot delete a plan currently assigned to any agency
```

---

## 6.7 Assign Subscription to Agency

```http
POST /admin/agencies/{agencyId}/subscription
```

### Request DTO — `AssignSubscriptionRequestDTO`

```json
{
  "planId": 1,
  "startDate": "2026-06-01",
  "endDate": "2026-07-01",
  "active": true
}
```

### Validation Rules

| Field | Rules |
|---|---|
| planId | required, must exist |
| startDate | required, valid date |
| endDate | required, must be after startDate |
| active | required, boolean |

### Response DTO — `AgencySubscriptionResponseDTO`

```json
{
  "success": true,
  "message": "Subscription assigned successfully",
  "data": {
    "id": 1,
    "agencyId": 1,
    "agencyName": "Baku Premium Estate",
    "planId": 1,
    "planName": "Professional",
    "startDate": "2026-06-01",
    "endDate": "2026-07-01",
    "active": true,
    "maxListings": 200,
    "maxAgents": 20,
    "featuredListingsAllowed": true
  }
}
```

---

## 6.8 Get Agency Subscription (Admin)

```http
GET /admin/agencies/{agencyId}/subscription
```

### Response DTO — `AgencySubscriptionResponseDTO`

```json
{
  "success": true,
  "message": "Subscription fetched successfully",
  "data": {
    "id": 1,
    "agencyId": 1,
    "agencyName": "Baku Premium Estate",
    "planId": 1,
    "planName": "Professional",
    "startDate": "2026-06-01",
    "endDate": "2026-07-01",
    "active": true,
    "maxListings": 200,
    "usedListings": 42,
    "maxAgents": 20,
    "usedAgents": 5,
    "featuredListingsAllowed": true
  }
}
```

---

# 7. Agency API

**Controller:** `AgencyController` | **Prefix:** `/agencies`

---

## 7.1 Create Agency Profile

```http
POST /agencies
```

**Authorization:** `AGENCY_ADMIN`

### Request DTO — `CreateAgencyRequestDTO`

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

### Validation Rules

| Field | Rules |
|---|---|
| name | required, unique, min 2, max 100 chars |
| description | optional, max 1000 chars |
| phoneNumber | required, valid phone format |
| email | required, valid email |
| website | optional, valid URL |
| city | required, max 100 chars |
| address | required, max 255 chars |

### Response DTO — `AgencyResponseDTO`

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
    "logoUrl": null,
    "coverUrl": null,
    "ownerId": 2,
    "createdAt": "2026-06-01T09:00:00"
  }
}
```

### Business Rules

```
- One AGENCY_ADMIN can only own one agency
- Agency is ACTIVE by default upon creation
```

---

## 7.2 Get My Agency

```http
GET /agencies/me
```

**Authorization:** `AGENCY_ADMIN`

### Response DTO — `AgencyResponseDTO`

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
    "status": "ACTIVE",
    "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg",
    "coverUrl": "https://example.com/uploads/agencies/1/cover.jpg",
    "ownerId": 2,
    "createdAt": "2026-06-01T09:00:00"
  }
}
```

---

## 7.3 Update Agency

```http
PUT /agencies/{agencyId}
```

**Authorization:** `AGENCY_ADMIN` who owns the agency, or `SUPER_ADMIN`

### Request DTO — `UpdateAgencyRequestDTO`

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

### Response DTO — `AgencyResponseDTO`

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
    "status": "ACTIVE",
    "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg",
    "coverUrl": "https://example.com/uploads/agencies/1/cover.jpg",
    "ownerId": 2,
    "createdAt": "2026-06-01T09:00:00"
  }
}
```

---

## 7.4 Get My Agency Subscription

```http
GET /agencies/me/subscription
```

**Authorization:** `AGENCY_ADMIN`

### Response DTO — `AgencySubscriptionResponseDTO`

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
    "usedAgents": 5,
    "featuredListingsAllowed": true
  }
}
```

---

## 7.5 Get My Agency Properties

```http
GET /agencies/me/properties
```

**Authorization:** `AGENCY_ADMIN`

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 12 | Page size |
| status | string | — | Filter by status (all statuses visible) |
| keyword | string | — | Search by title |

### Response DTO — Paginated `PropertySummaryDTO`

```json
{
  "success": true,
  "message": "Properties fetched successfully",
  "data": {
    "content": [ /* PropertySummaryDTO objects including PENDING/REJECTED */ ],
    "page": 0,
    "size": 12,
    "totalElements": 42,
    "totalPages": 4,
    "last": false
  }
}
```

---

## 7.6 Get Public Agencies

```http
GET /agencies/public
```

**Authorization:** Public

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 10 | Page size |
| city | string | — | Filter by city |
| keyword | string | — | Search by name |

### Response DTO — Paginated `PublicAgencySummaryDTO`

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
        "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg",
        "activeListingsCount": 42
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 15,
    "totalPages": 2,
    "last": false
  }
}
```

---

## 7.7 Get Public Agency Detail

```http
GET /agencies/public/{agencyId}
```

**Authorization:** Public

### Response DTO — `PublicAgencyDetailResponseDTO`

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
    "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg",
    "coverUrl": "https://example.com/uploads/agencies/1/cover.jpg",
    "activeListingsCount": 42,
    "totalAgents": 5
  }
}
```

---

## 7.8 Get Public Agency Listings

```http
GET /agencies/public/{agencyId}/properties
```

**Authorization:** Public

### Query Parameters — same as `GET /properties`

### Response DTO — Paginated `PropertySummaryDTO` (only ACTIVE listings)

```json
{
  "success": true,
  "message": "Agency properties fetched successfully",
  "data": {
    "content": [ /* PropertySummaryDTO objects, ACTIVE only */ ],
    "page": 0,
    "size": 12,
    "totalElements": 42,
    "totalPages": 4,
    "last": false
  }
}
```

---

# 8. Agent API

**Controller:** `AgentController` | **Prefix:** `/agencies/{agencyId}/agents`, `/agents`

---

## 8.1 Create Agent

```http
POST /agencies/{agencyId}/agents
```

**Authorization:** `AGENCY_ADMIN` who owns the agency

### Request DTO — `CreateAgentRequestDTO`

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

### Validation Rules

| Field | Rules |
|---|---|
| firstName | required, min 2, max 50 chars |
| lastName | required, min 2, max 50 chars |
| email | required, valid email, unique |
| phoneNumber | required, valid phone format |
| password | required, min 8 chars, at least 1 uppercase, 1 digit, 1 special char |
| position | optional, max 100 chars |

### Response DTO — `AgentResponseDTO`

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
    "agencyName": "Baku Premium Estate",
    "assignedPropertiesCount": 0,
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

### Business Rules

```
- Check subscription maxAgents limit before creating
- Return 409 if email already registered
```

---

## 8.2 Get Agency Agents

```http
GET /agencies/{agencyId}/agents
```

**Authorization:** `AGENCY_ADMIN` who owns agency, `AGENT` of the agency, or `SUPER_ADMIN`

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 10 | Page size |
| keyword | string | — | Search by name |
| enabled | boolean | — | Filter by status |

### Response DTO — Paginated `AgentResponseDTO`

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
        "agencyId": 1,
        "agencyName": "Baku Premium Estate",
        "assignedPropertiesCount": 8,
        "enabled": true,
        "createdAt": "2026-06-01T10:00:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 5,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 8.3 Get Agent by ID

```http
GET /agents/{agentId}
```

**Authorization:** `AGENCY_ADMIN` who owns agent's agency, or `SUPER_ADMIN`

### Response DTO — `AgentResponseDTO`

```json
{
  "success": true,
  "message": "Agent fetched successfully",
  "data": {
    "id": 5,
    "firstName": "Nigar",
    "lastName": "Aliyeva",
    "email": "nigar.agent@example.com",
    "phoneNumber": "+994551234567",
    "position": "Senior Agent",
    "agencyId": 1,
    "agencyName": "Baku Premium Estate",
    "assignedPropertiesCount": 8,
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

---

## 8.4 Update Agent

```http
PUT /agents/{agentId}
```

**Authorization:** `AGENCY_ADMIN` who owns the agent's agency

### Request DTO — `UpdateAgentRequestDTO`

```json
{
  "firstName": "Nigar",
  "lastName": "Aliyeva",
  "phoneNumber": "+994551234500",
  "position": "Lead Agent",
  "enabled": true
}
```

### Validation Rules

| Field | Rules |
|---|---|
| firstName | optional, min 2, max 50 chars |
| lastName | optional, min 2, max 50 chars |
| phoneNumber | optional, valid phone format |
| position | optional, max 100 chars |
| enabled | optional, boolean |

### Response DTO — `AgentResponseDTO`

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
    "agencyId": 1,
    "agencyName": "Baku Premium Estate",
    "assignedPropertiesCount": 8,
    "enabled": true,
    "createdAt": "2026-06-01T10:00:00"
  }
}
```

---

## 8.5 Get Agent Assigned Properties

```http
GET /agents/{agentId}/properties
```

**Authorization:** `AGENCY_ADMIN` who owns agent's agency, the `AGENT` themselves, or `SUPER_ADMIN`

### Query Parameters — `page`, `size`, `status`

### Response DTO — Paginated `PropertySummaryDTO`

```json
{
  "success": true,
  "message": "Agent properties fetched successfully",
  "data": {
    "content": [ /* PropertySummaryDTO objects */ ],
    "page": 0,
    "size": 12,
    "totalElements": 8,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 8.6 Delete Agent

```http
DELETE /agents/{agentId}
```

**Authorization:** `AGENCY_ADMIN` who owns the agent's agency

### Response

```json
{
  "success": true,
  "message": "Agent deleted successfully",
  "data": null
}
```

### Business Rules

```
- Unassign agent from all properties before deletion
- Invalidate agent's tokens
```

---

# 9. Property API

**Controller:** `PropertyController` | **Prefix:** `/properties`

---

## 9.1 Create Property

```http
POST /properties
```

**Authorization:** `AGENCY_ADMIN` or `AGENT`

### Request DTO — `CreatePropertyRequestDTO`

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

### Validation Rules

| Field | Rules |
|---|---|
| title | required, min 5, max 200 chars |
| description | optional, max 3000 chars |
| price | required, min 0 |
| city | required, max 100 chars |
| district | optional, max 100 chars |
| address | required, max 255 chars |
| propertyType | required, valid enum |
| listingType | required, valid enum |
| area | required, min 1 |
| rooms | optional, min 0 |
| bathrooms | optional, min 0 |
| floor | optional |
| totalFloors | optional, must be >= floor if both provided |
| latitude | optional, -90 to 90 |
| longitude | optional, -180 to 180 |
| agentId | optional, must belong to same agency |
| features | optional, valid enum values |

### Response DTO — `PropertyDetailResponseDTO`

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
    "featured": false,
    "images": [],
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
    },
    "createdAt": "2026-06-01T10:30:00",
    "updatedAt": "2026-06-01T10:30:00"
  }
}
```

### Business Rules

```
- Agency subscription must be active
- Check subscription maxListings limit before creating
- New properties default to PENDING status; require SUPER_ADMIN approval to go ACTIVE
- AGENT can only create property under their own agency
- AGENCY_ADMIN can optionally assign an agentId from their agency
```

---

## 9.2 Get Properties (Public Listing)

```http
GET /properties
```

**Authorization:** Public

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 12 | Page size |
| keyword | string | — | Full-text search on title/description |
| city | string | — | Filter by city |
| district | string | — | Filter by district |
| propertyType | string | — | APARTMENT, HOUSE, VILLA, OFFICE, LAND |
| listingType | string | — | SALE, RENT |
| minPrice | number | — | Minimum price |
| maxPrice | number | — | Maximum price |
| rooms | integer | — | Number of rooms |
| minArea | number | — | Minimum area in m² |
| maxArea | number | — | Maximum area in m² |
| features | string[] | — | Feature filters |
| sort | string | createdAt,desc | Sort field and direction |

### Response DTO — Paginated `PropertySummaryDTO`

```json
{
  "success": true,
  "message": "Properties fetched successfully",
  "data": {
    "content": [ /* PropertySummaryDTO objects, ACTIVE only */ ],
    "page": 0,
    "size": 12,
    "totalElements": 240,
    "totalPages": 20,
    "last": false
  }
}
```

### Business Rules

```
- Only returns ACTIVE listings
- Featured listings should appear first within sort order
```

---

## 9.3 Get Property Details

```http
GET /properties/{propertyId}
```

**Authorization:** Public (for ACTIVE); AGENCY_ADMIN/AGENT/SUPER_ADMIN (for PENDING/REJECTED)

### Response DTO — `PropertyDetailResponseDTO`

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
        "id": "uuid",
        "fileUrl": "https://example.com/uploads/properties/10/main.jpg",
        "isMain": true,
        "sortOrder": 0
      }
    ],
    "features": ["ELEVATOR", "PARKING", "BALCONY", "HEATING"],
    "agency": {
      "id": 1,
      "name": "Baku Premium Estate",
      "phoneNumber": "+994501112233",
      "logoUrl": "https://example.com/uploads/agencies/1/logo.jpg"
    },
    "agent": {
      "id": 5,
      "fullName": "Nigar Aliyeva",
      "phoneNumber": "+994551234567"
    },
    "createdAt": "2026-06-01T10:30:00",
    "updatedAt": "2026-06-01T10:30:00"
  }
}
```

---

## 9.4 Update Property

```http
PUT /properties/{propertyId}
```

**Authorization:** `AGENCY_ADMIN` who owns property, assigned `AGENT`, or `SUPER_ADMIN`

### Request DTO — `UpdatePropertyRequestDTO` (same fields as Create, all optional)

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

### Response DTO — `PropertyDetailResponseDTO`

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

### Business Rules

```
- Property is reset to PENDING after any update, requiring re-approval
- SUPER_ADMIN updates do not reset status
```

---

## 9.5 Update Property Status (Agency)

```http
PATCH /properties/{propertyId}/status
```

**Authorization:** `AGENCY_ADMIN` who owns property, or assigned `AGENT`

### Request DTO — `UpdatePropertyStatusRequestDTO`

```json
{
  "status": "SOLD"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| status | required, only `SOLD` or `RENTED` allowed for agencies |

### Response DTO

```json
{
  "success": true,
  "message": "Property status updated successfully",
  "data": {
    "id": 10,
    "status": "SOLD",
    "updatedAt": "2026-06-15T09:00:00"
  }
}
```

---

## 9.6 Toggle Featured

```http
PATCH /properties/{propertyId}/featured
```

**Authorization:** `AGENCY_ADMIN` who owns property, or `SUPER_ADMIN`

### Request DTO — `ToggleFeaturedRequestDTO`

```json
{
  "featured": true
}
```

### Response

```json
{
  "success": true,
  "message": "Property featured status updated",
  "data": {
    "id": 10,
    "featured": true
  }
}
```

### Business Rules

```
- Agency subscription must have featuredListingsAllowed = true
- SUPER_ADMIN can always toggle regardless of subscription
```

---

## 9.7 Delete Property

```http
DELETE /properties/{propertyId}
```

**Authorization:** `AGENCY_ADMIN` who owns property, assigned `AGENT`, or `SUPER_ADMIN`

### Response

```json
{
  "success": true,
  "message": "Property deleted successfully",
  "data": null
}
```

### Business Rules

```
- Soft-delete; cascade delete associated media files from storage
- Cascade delete favorites, inquiries, and viewing requests
```

---

## 9.8 Get Featured Properties

```http
GET /properties/featured
```

**Authorization:** Public

### Query Parameters — `page`, `size`, `city`, `listingType`

### Response DTO — Paginated `PropertySummaryDTO` (featured + ACTIVE only)

```json
{
  "success": true,
  "message": "Featured properties fetched successfully",
  "data": {
    "content": [ /* PropertySummaryDTO objects */ ],
    "page": 0,
    "size": 8,
    "totalElements": 12,
    "totalPages": 2,
    "last": false
  }
}
```

---

## 9.9 Get Recent Properties

```http
GET /properties/recent
```

**Authorization:** Public

### Query Parameters — `size` (default 8), `city`

### Response DTO — List `PropertySummaryDTO` (ACTIVE, sorted by createdAt desc)

```json
{
  "success": true,
  "message": "Recent properties fetched successfully",
  "data": [ /* PropertySummaryDTO objects */ ]
}
```

---

## 9.10 Get Similar Properties

```http
GET /properties/{propertyId}/similar
```

**Authorization:** Public

### Query Parameters — `size` (default 6)

### Response DTO — List `PropertySummaryDTO`

```json
{
  "success": true,
  "message": "Similar properties fetched successfully",
  "data": [ /* PropertySummaryDTO — same city + propertyType, ACTIVE only */ ]
}
```

---

## 9.11 Search Suggestions (Autocomplete)

```http
GET /properties/search/suggestions
```

**Authorization:** Public

### Query Parameters

| Param | Type | Required | Description |
|---|---|---|---|
| keyword | string | yes | Min 2 chars |

### Response DTO — `SearchSuggestionsResponseDTO`

```json
{
  "success": true,
  "message": "Suggestions fetched successfully",
  "data": {
    "properties": [
      { "id": 10, "title": "Modern 3-room Apartment Near Metro" }
    ],
    "cities": ["Baku", "Bakıxanov"],
    "districts": ["Narimanov", "Nasimi"]
  }
}
```

---

## 9.12 Get Map Properties

```http
GET /properties/map
```

**Authorization:** Public

### Query Parameters

| Param | Type | Description |
|---|---|---|
| city | string | Filter by city |
| listingType | string | SALE or RENT |
| propertyType | string | Property type filter |
| minPrice | number | Min price |
| maxPrice | number | Max price |

### Response DTO — `MapPropertyResponseDTO`

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
      "mainImageUrl": "https://example.com/uploads/properties/10/main.jpg"
    }
  ]
}
```

### Business Rules

```
- Only returns ACTIVE listings
- Cap results at 500 for performance
```

---

# 10. Media API

**Controller:** `MediaController` | **Prefix:** `/properties/{id}/images`, `/agencies/{id}/media`, `/media`

---

## 10.1 Upload Property Images

```http
POST /properties/{propertyId}/images
```

**Authorization:** `AGENCY_ADMIN` who owns property, assigned `AGENT`, or `SUPER_ADMIN`

**Content-Type:** `multipart/form-data`

### Request

```
files: image1.jpg, image2.jpg  (JPEG/PNG/WEBP, max 10MB each, max 20 files)
```

### Response DTO — List `MediaFileDTO`

```json
{
  "success": true,
  "message": "Images uploaded successfully",
  "data": [
    {
      "id": "uuid-1",
      "fileUrl": "https://example.com/uploads/properties/10/image1.jpg",
      "fileName": "image1.jpg",
      "fileType": "image/jpeg",
      "fileSize": 204800,
      "mediaPurpose": "PROPERTY_IMAGE",
      "isMain": true,
      "sortOrder": 0
    },
    {
      "id": "uuid-2",
      "fileUrl": "https://example.com/uploads/properties/10/image2.jpg",
      "fileName": "image2.jpg",
      "fileType": "image/jpeg",
      "fileSize": 185000,
      "mediaPurpose": "PROPERTY_IMAGE",
      "isMain": false,
      "sortOrder": 1
    }
  ]
}
```

### Business Rules

```
- First uploaded image becomes isMain = true if no main image exists
- Accepted types: image/jpeg, image/png, image/webp
- Max 10MB per file; max 20 images per property
```

---

## 10.2 Get Property Images

```http
GET /properties/{propertyId}/images
```

**Authorization:** Public

### Response DTO — List `MediaFileDTO`

```json
{
  "success": true,
  "message": "Images fetched successfully",
  "data": [
    /* MediaFileDTO objects sorted by sortOrder */
  ]
}
```

---

## 10.3 Set Main Image

```http
PATCH /media/{mediaId}/main
```

**Authorization:** `AGENCY_ADMIN` who owns property, assigned `AGENT`, or `SUPER_ADMIN`

### Response

```json
{
  "success": true,
  "message": "Main image updated successfully",
  "data": {
    "id": "uuid-2",
    "isMain": true
  }
}
```

### Business Rules

```
- Unset isMain on the previous main image
```

---

## 10.4 Delete Media File

```http
DELETE /media/{mediaId}
```

**Authorization:** `AGENCY_ADMIN` who owns the resource, assigned `AGENT`, or `SUPER_ADMIN`

### Response

```json
{
  "success": true,
  "message": "Media file deleted successfully",
  "data": null
}
```

### Business Rules

```
- Delete from storage (S3/local) as well as DB
- If deleted file was isMain, promote next image (lowest sortOrder) to main
```

---

## 10.6 Upload Agency Logo

```http
POST /agencies/{agencyId}/logo
```

**Authorization:** `AGENCY_ADMIN` who owns agency, or `SUPER_ADMIN`

**Content-Type:** `multipart/form-data`

### Request

```
file: logo.jpg  (JPEG/PNG/WEBP, max 5MB)
```

### Response

```json
{
  "success": true,
  "message": "Agency logo uploaded successfully",
  "data": {
    "id": "uuid",
    "fileUrl": "https://example.com/uploads/agencies/1/logo.jpg",
    "mediaPurpose": "AGENCY_LOGO",
    "isMain": true
  }
}
```

### Business Rules

```
- Replaces previous logo; delete old file from storage
```

---

# 11. Favorites API

**Controller:** `FavoriteController` | **Prefix:** `/favorites`

**Authorization:** `CLIENT` only (all endpoints)

---

## 11.1 Add Property to Favorites

```http
POST /favorites/{propertyId}
```

### Response DTO — `FavoriteResponseDTO`

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

### Business Rules

```
- Property must be ACTIVE
- Return 409 if already favorited
```

---

## 11.2 Remove Property from Favorites

```http
DELETE /favorites/{propertyId}
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

## 11.3 Get My Favorites

```http
GET /favorites/me
```

### Query Parameters — `page`, `size`

### Response DTO — Paginated `PropertySummaryDTO`

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
        "district": "Narimanov",
        "propertyType": "APARTMENT",
        "listingType": "SALE",
        "area": 95.5,
        "rooms": 3,
        "mainImageUrl": "https://example.com/uploads/properties/10/main.jpg",
        "status": "ACTIVE",
        "featured": false,
        "createdAt": "2026-06-01T10:30:00"
      }
    ],
    "page": 0,
    "size": 12,
    "totalElements": 6,
    "totalPages": 1,
    "last": true
  }
}
```

---

# 12. Inquiry API

**Controller:** `InquiryController` | **Prefix:** `/properties/{id}/inquiries`, `/inquiries`, `/agencies/{id}/inquiries`

---

## 12.1 Create Inquiry

```http
POST /properties/{propertyId}/inquiries
```

**Authorization:** `CLIENT`

### Request DTO — `CreateInquiryRequestDTO`

```json
{
  "message": "Hello, I am interested in this property. Is it still available?",
  "preferredContactMethod": "PHONE"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| message | required, min 10, max 1000 chars |
| preferredContactMethod | required, one of: PHONE, EMAIL, WHATSAPP |

### Response DTO — `InquiryResponseDTO`

```json
{
  "success": true,
  "message": "Inquiry sent successfully",
  "data": {
    "id": 1,
    "propertyId": 10,
    "propertyTitle": "Modern 3-room Apartment Near Metro",
    "clientId": 1,
    "clientFullName": "Elshan Hasanov",
    "message": "Hello, I am interested in this property. Is it still available?",
    "preferredContactMethod": "PHONE",
    "status": "NEW",
    "createdAt": "2026-06-01T12:20:00"
  }
}
```

### Business Rules

```
- Property must be ACTIVE
- Client cannot send duplicate open inquiries for the same property
- Notify agency via notification
```

---

## 12.2 Get My Inquiries (Client)

```http
GET /inquiries/me
```

**Authorization:** `CLIENT`

### Query Parameters — `page`, `size`, `status`

### Response DTO — Paginated `InquiryResponseDTO`

```json
{
  "success": true,
  "message": "Inquiries fetched successfully",
  "data": {
    "content": [ /* InquiryResponseDTO objects */ ],
    "page": 0,
    "size": 10,
    "totalElements": 3,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 12.3 Get Agency Inquiries

```http
GET /agencies/{agencyId}/inquiries
```

**Authorization:** `AGENCY_ADMIN` who owns agency, `AGENT` of agency, or `SUPER_ADMIN`

### Query Parameters

| Param | Type | Description |
|---|---|---|
| page | integer | Page number |
| size | integer | Page size |
| status | string | NEW, CONTACTED, CLOSED |
| propertyId | integer | Filter by specific property |

### Response DTO — Paginated `InquiryResponseDTO`

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
        "clientId": 1,
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

## 12.4 Get Inquiry by ID

```http
GET /inquiries/{inquiryId}
```

**Authorization:** `CLIENT` who created it, `AGENCY_ADMIN`/`AGENT` of the property's agency, or `SUPER_ADMIN`

### Response DTO — `InquiryResponseDTO`

```json
{
  "success": true,
  "message": "Inquiry fetched successfully",
  "data": { /* InquiryResponseDTO */ }
}
```

---

## 12.5 Update Inquiry Status

```http
PATCH /inquiries/{inquiryId}/status
```

**Authorization:** `AGENCY_ADMIN` who owns inquiry's agency, assigned `AGENT`, or `SUPER_ADMIN`

### Request DTO — `UpdateInquiryStatusRequestDTO`

```json
{
  "status": "CONTACTED"
}
```

### Validation Rules

| Field | Rules |
|---|---|
| status | required, one of: CONTACTED, CLOSED |

### Response DTO

```json
{
  "success": true,
  "message": "Inquiry status updated successfully",
  "data": {
    "id": 1,
    "status": "CONTACTED",
    "updatedAt": "2026-06-02T10:00:00"
  }
}
```

---

# 13. Viewing Request API

**Controller:** `ViewingController` | **Prefix:** `/properties/{id}/viewings`, `/viewings`, `/agencies/{id}/viewings`

---

## 13.1 Create Viewing Request

```http
POST /properties/{propertyId}/viewings
```

**Authorization:** `CLIENT`

### Request DTO — `CreateViewingRequestDTO`

```json
{
  "preferredDateTime": "2026-06-05T15:30:00",
  "note": "I would like to see the apartment after 3 PM."
}
```

### Validation Rules

| Field | Rules |
|---|---|
| preferredDateTime | required, must be a future datetime |
| note | optional, max 500 chars |

### Response DTO — `ViewingResponseDTO`

```json
{
  "success": true,
  "message": "Viewing request created successfully",
  "data": {
    "id": 1,
    "propertyId": 10,
    "propertyTitle": "Modern 3-room Apartment Near Metro",
    "clientId": 1,
    "clientFullName": "Elshan Hasanov",
    "preferredDateTime": "2026-06-05T15:30:00",
    "note": "I would like to see the apartment after 3 PM.",
    "status": "PENDING",
    "responseNote": null,
    "createdAt": "2026-06-01T12:40:00"
  }
}
```

### Business Rules

```
- Property must be ACTIVE
- Client cannot have more than one PENDING viewing per property
- Notify agency via notification
```

---

## 13.2 Get My Viewing Requests (Client)

```http
GET /viewings/me
```

**Authorization:** `CLIENT`

### Query Parameters — `page`, `size`, `status`

### Response DTO — Paginated `ViewingResponseDTO`

```json
{
  "success": true,
  "message": "Viewing requests fetched successfully",
  "data": {
    "content": [ /* ViewingResponseDTO objects */ ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 13.3 Cancel Viewing Request (Client)

```http
PATCH /viewings/{viewingId}/cancel
```

**Authorization:** `CLIENT` who created the request

### Response

```json
{
  "success": true,
  "message": "Viewing request cancelled successfully",
  "data": {
    "id": 1,
    "status": "CANCELLED"
  }
}
```

### Business Rules

```
- Only PENDING or APPROVED viewings can be cancelled
- Notify agency via notification
```

---

## 13.4 Get Agency Viewing Requests

```http
GET /agencies/{agencyId}/viewings
```

**Authorization:** `AGENCY_ADMIN` who owns agency, `AGENT` of agency, or `SUPER_ADMIN`

### Query Parameters

| Param | Type | Description |
|---|---|---|
| page | integer | Page number |
| size | integer | Page size |
| status | string | PENDING, APPROVED, REJECTED, COMPLETED, CANCELLED |
| propertyId | integer | Filter by specific property |

### Response DTO — Paginated `ViewingResponseDTO`

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
        "clientId": 1,
        "clientFullName": "Elshan Hasanov",
        "clientPhoneNumber": "+994501234567",
        "preferredDateTime": "2026-06-05T15:30:00",
        "note": "I would like to see the apartment after 3 PM.",
        "status": "PENDING",
        "responseNote": null,
        "createdAt": "2026-06-01T12:40:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 6,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 13.5 Update Viewing Status (Agency)

```http
PATCH /viewings/{viewingId}/status
```

**Authorization:** `AGENCY_ADMIN` who owns viewing's agency, assigned `AGENT`, or `SUPER_ADMIN`

### Request DTO — `UpdateViewingStatusRequestDTO`

```json
{
  "status": "APPROVED",
  "responseNote": "Approved. Agent will contact you before the viewing."
}
```

### Validation Rules

| Field | Rules |
|---|---|
| status | required, one of: APPROVED, REJECTED, COMPLETED |
| responseNote | optional, max 500 chars |

### Response DTO

```json
{
  "success": true,
  "message": "Viewing status updated successfully",
  "data": {
    "id": 1,
    "status": "APPROVED",
    "responseNote": "Approved. Agent will contact you before the viewing.",
    "updatedAt": "2026-06-02T09:00:00"
  }
}
```

### Business Rules

```
- Notify client via notification when status changes
```

---

# 14. Subscription Plan API

**Controller:** `SubscriptionPlanController` | **Prefix:** `/subscription-plans`

---

## 14.1 Get Public Subscription Plans

```http
GET /subscription-plans
```

**Authorization:** Public

### Response DTO — List `SubscriptionPlanPublicResponseDTO`

```json
{
  "success": true,
  "message": "Subscription plans fetched successfully",
  "data": [
    {
      "id": 1,
      "name": "Starter",
      "description": "For small agencies getting started",
      "price": 29.99,
      "durationDays": 30,
      "maxListings": 50,
      "maxAgents": 5,
      "featuredListingsAllowed": false
    },
    {
      "id": 2,
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

### Business Rules

```
- Only returns active = true plans
```

---

# 15. Notification API

**Controller:** `NotificationController` | **Prefix:** `/notifications`

**Authorization:** Authenticated (any role, own notifications only)

---

## 15.1 Get My Notifications

```http
GET /notifications
```

### Query Parameters

| Param | Type | Default | Description |
|---|---|---|---|
| page | integer | 0 | Page number |
| size | integer | 20 | Page size |
| read | boolean | — | Filter by read status |

### Response DTO — Paginated `NotificationResponseDTO`

```json
{
  "success": true,
  "message": "Notifications fetched successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Property Approved",
        "message": "Your property 'Modern 3-room Apartment Near Metro' has been approved.",
        "type": "PROPERTY_APPROVED",
        "referenceId": 10,
        "referenceType": "PROPERTY",
        "read": false,
        "createdAt": "2026-06-02T09:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 5,
    "totalPages": 1,
    "last": true
  }
}
```

---

## 15.2 Get Unread Count

```http
GET /notifications/unread-count
```

### Response

```json
{
  "success": true,
  "message": "Unread count fetched successfully",
  "data": {
    "unreadCount": 3
  }
}
```

---

## 15.3 Mark Notification as Read

```http
PATCH /notifications/{notificationId}/read
```

### Response

```json
{
  "success": true,
  "message": "Notification marked as read",
  "data": {
    "id": 1,
    "read": true
  }
}
```

---

## 15.4 Mark All as Read

```http
PATCH /notifications/read-all
```

### Response

```json
{
  "success": true,
  "message": "All notifications marked as read",
  "data": null
}
```

---

## 15.5 Delete Notification

```http
DELETE /notifications/{notificationId}
```

### Response

```json
{
  "success": true,
  "message": "Notification deleted successfully",
  "data": null
}
```

---

### Notification Types (Internal Reference)

```
PROPERTY_APPROVED       — sent to AGENCY_ADMIN when property approved
PROPERTY_REJECTED       — sent to AGENCY_ADMIN when property rejected
NEW_INQUIRY             — sent to AGENCY_ADMIN/AGENT on new inquiry
NEW_VIEWING_REQUEST     — sent to AGENCY_ADMIN/AGENT on new viewing request
VIEWING_APPROVED        — sent to CLIENT when viewing approved
VIEWING_REJECTED        — sent to CLIENT when viewing rejected
SUBSCRIPTION_EXPIRING   — sent to AGENCY_ADMIN 7 days before expiry
SUBSCRIPTION_EXPIRED    — sent to AGENCY_ADMIN when expired
AGENCY_SUSPENDED        — sent to AGENCY_ADMIN when agency suspended
```

---

# 16. Dashboard API

**Controller:** `DashboardController` | **Prefix:** `/dashboard`

---

## 16.1 Super Admin Dashboard

```http
GET /dashboard/admin
```

**Authorization:** `SUPER_ADMIN`

### Response DTO — `AdminDashboardResponseDTO`

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
    "revenueThisMonth": 959.88,
    "newUsersThisWeek": 14,
    "latestProperties": [
      {
        "id": 10,
        "title": "Modern 3-room Apartment Near Metro",
        "status": "PENDING",
        "agencyName": "Baku Premium Estate",
        "createdAt": "2026-06-01T10:30:00"
      }
    ],
    "latestAgencies": [
      {
        "id": 1,
        "name": "Baku Premium Estate",
        "city": "Baku",
        "createdAt": "2026-05-01T09:00:00"
      }
    ]
  }
}
```

---

## 16.2 Agency Dashboard

```http
GET /dashboard/agency
```

**Authorization:** `AGENCY_ADMIN`

### Response DTO — `AgencyDashboardResponseDTO`

```json
{
  "success": true,
  "message": "Agency dashboard fetched successfully",
  "data": {
    "totalProperties": 42,
    "activeProperties": 35,
    "pendingProperties": 4,
    "rejectedProperties": 1,
    "soldProperties": 2,
    "rentedProperties": 1,
    "totalAgents": 5,
    "newInquiries": 8,
    "upcomingViewings": 6,
    "subscriptionActive": true,
    "subscriptionExpiresAt": "2026-07-01",
    "usedListings": 42,
    "maxListings": 200
  }
}
```

---

## 16.3 Agent Dashboard

```http
GET /dashboard/agent
```

**Authorization:** `AGENT`

### Response DTO — `AgentDashboardResponseDTO`

```json
{
  "success": true,
  "message": "Agent dashboard fetched successfully",
  "data": {
    "assignedProperties": 8,
    "activeProperties": 7,
    "pendingProperties": 1,
    "newInquiries": 3,
    "upcomingViewings": 2,
    "agencyName": "Baku Premium Estate"
  }
}
```

---

## 16.4 Client Dashboard

```http
GET /dashboard/client
```

**Authorization:** `CLIENT`

### Response DTO — `ClientDashboardResponseDTO`

```json
{
  "success": true,
  "message": "Client dashboard fetched successfully",
  "data": {
    "favoriteProperties": 6,
    "sentInquiries": 3,
    "viewingRequests": 2,
    "approvedViewings": 1,
    "pendingViewings": 1
  }
}
```

---

# 17. Enum Values

## User Roles

| Value | Description |
|---|---|
| `SUPER_ADMIN` | Platform administrator |
| `AGENCY_ADMIN` | Agency owner |
| `AGENT` | Agency employee |
| `CLIENT` | End user |

## Property Type

| Value |
|---|
| `APARTMENT` |
| `HOUSE` |
| `VILLA` |
| `OFFICE` |
| `LAND` |

## Listing Type

| Value |
|---|
| `SALE` |
| `RENT` |

## Property Status

| Value | Set By |
|---|---|
| `PENDING` | Default on create/update |
| `ACTIVE` | SUPER_ADMIN approval |
| `REJECTED` | SUPER_ADMIN moderation |
| `SOLD` | AGENCY_ADMIN / AGENT |
| `RENTED` | AGENCY_ADMIN / AGENT |

## Agency Status

| Value |
|---|
| `ACTIVE` |
| `SUSPENDED` |
| `INACTIVE` |

## Inquiry Status

| Value |
|---|
| `NEW` |
| `CONTACTED` |
| `CLOSED` |

## Viewing Status

| Value |
|---|
| `PENDING` |
| `APPROVED` |
| `REJECTED` |
| `COMPLETED` |
| `CANCELLED` |

## Preferred Contact Method

| Value |
|---|
| `PHONE` |
| `EMAIL` |
| `WHATSAPP` |

## Property Features

| Value |
|---|
| `ELEVATOR` |
| `PARKING` |
| `BALCONY` |
| `HEATING` |
| `AIR_CONDITIONING` |
| `FURNISHED` |
| `POOL` |
| `SECURITY` |
| `GARDEN` |
| `STORAGE` |
| `GAS` |
| `INTERNET` |

## Media Purpose

| Value | Used For |
|---|---|
| `PROPERTY_IMAGE` | Property gallery images |
| `AGENCY_LOGO` | Agency profile logo |
| `AGENCY_COVER` | Agency cover/banner photo |
| `USER_AVATAR` | User profile photo |

## Notification Type

| Value |
|---|
| `PROPERTY_APPROVED` |
| `PROPERTY_REJECTED` |
| `NEW_INQUIRY` |
| `NEW_VIEWING_REQUEST` |
| `VIEWING_APPROVED` |
| `VIEWING_REJECTED` |
| `SUBSCRIPTION_EXPIRING` |
| `SUBSCRIPTION_EXPIRED` |
| `AGENCY_SUSPENDED` |

---

# 18. Controller Map

| Controller | Prefix | Roles |
|---|---|---|
| `AuthController` | `/auth` | Mixed |
| `UserProfileController` | `/users/me` | Authenticated |
| `AdminUserController` | `/admin/users` | SUPER_ADMIN |
| `AdminAgencyController` | `/admin/agencies` | SUPER_ADMIN |
| `AdminPropertyController` | `/admin/properties` | SUPER_ADMIN |
| `AdminSubscriptionController` | `/admin/subscription-plans`, `/admin/agencies/{id}/subscription` | SUPER_ADMIN |
| `AgencyController` | `/agencies` | AGENCY_ADMIN / Public |
| `AgentController` | `/agencies/{id}/agents`, `/agents` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| `PropertyController` | `/properties` | Mixed |
| `MediaController` | `/properties/{id}/images`, `/agencies/{id}/logo`, `/agencies/{id}/cover`, `/media` | Mixed |
| `FavoriteController` | `/favorites` | CLIENT |
| `InquiryController` | `/properties/{id}/inquiries`, `/inquiries`, `/agencies/{id}/inquiries` | Mixed |
| `ViewingController` | `/properties/{id}/viewings`, `/viewings`, `/agencies/{id}/viewings` | Mixed |
| `SubscriptionPlanController` | `/subscription-plans` | Public |
| `NotificationController` | `/notifications` | Authenticated |
| `DashboardController` | `/dashboard` | Role-based |

---

# 19. Complete Endpoint Summary

| Module | Method | Endpoint | Authorization |
|---|---|---|---|
| Auth | POST | `/auth/register` | Public |
| Auth | POST | `/auth/login` | Public |
| Auth | POST | `/auth/refresh-token` | Public |
| Auth | POST | `/auth/forgot-password` | Public |
| Auth | POST | `/auth/reset-password` | Public |
| Auth | PATCH | `/auth/change-password` | Authenticated |
| Auth | GET | `/auth/me` | Authenticated |
| Auth | POST | `/auth/logout` | Authenticated |
| Profile | PATCH | `/users/me/profile` | Authenticated |
| Profile | POST | `/users/me/photo` | Authenticated |
| Profile | DELETE | `/users/me` | Authenticated |
| Admin Users | GET | `/admin/users` | SUPER_ADMIN |
| Admin Users | GET | `/admin/users/{userId}` | SUPER_ADMIN |
| Admin Users | PATCH | `/admin/users/{userId}/status` | SUPER_ADMIN |
| Admin Users | DELETE | `/admin/users/{userId}` | SUPER_ADMIN |
| Admin Agencies | GET | `/admin/agencies` | SUPER_ADMIN |
| Admin Agencies | GET | `/admin/agencies/{agencyId}` | SUPER_ADMIN |
| Admin Agencies | PATCH | `/admin/agencies/{agencyId}/status` | SUPER_ADMIN |
| Admin Agencies | DELETE | `/admin/agencies/{agencyId}` | SUPER_ADMIN |
| Admin Properties | GET | `/admin/properties` | SUPER_ADMIN |
| Admin Properties | PATCH | `/admin/properties/{propertyId}/status` | SUPER_ADMIN |
| Admin Plans | POST | `/admin/subscription-plans` | SUPER_ADMIN |
| Admin Plans | GET | `/admin/subscription-plans` | SUPER_ADMIN |
| Admin Plans | GET | `/admin/subscription-plans/{planId}` | SUPER_ADMIN |
| Admin Plans | PUT | `/admin/subscription-plans/{planId}` | SUPER_ADMIN |
| Admin Plans | PATCH | `/admin/subscription-plans/{planId}/status` | SUPER_ADMIN |
| Admin Plans | DELETE | `/admin/subscription-plans/{planId}` | SUPER_ADMIN |
| Admin Subscriptions | POST | `/admin/agencies/{agencyId}/subscription` | SUPER_ADMIN |
| Admin Subscriptions | GET | `/admin/agencies/{agencyId}/subscription` | SUPER_ADMIN |
| Agencies | POST | `/agencies` | AGENCY_ADMIN |
| Agencies | GET | `/agencies/me` | AGENCY_ADMIN |
| Agencies | PUT | `/agencies/{agencyId}` | AGENCY_ADMIN / SUPER_ADMIN |
| Agencies | GET | `/agencies/me/subscription` | AGENCY_ADMIN |
| Agencies | GET | `/agencies/me/properties` | AGENCY_ADMIN |
| Agencies | GET | `/agencies/public` | Public |
| Agencies | GET | `/agencies/public/{agencyId}` | Public |
| Agencies | GET | `/agencies/public/{agencyId}/properties` | Public |
| Agents | POST | `/agencies/{agencyId}/agents` | AGENCY_ADMIN |
| Agents | GET | `/agencies/{agencyId}/agents` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Agents | GET | `/agents/{agentId}` | AGENCY_ADMIN / SUPER_ADMIN |
| Agents | PUT | `/agents/{agentId}` | AGENCY_ADMIN |
| Agents | GET | `/agents/{agentId}/properties` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Agents | DELETE | `/agents/{agentId}` | AGENCY_ADMIN |
| Properties | POST | `/properties` | AGENCY_ADMIN / AGENT |
| Properties | GET | `/properties` | Public |
| Properties | GET | `/properties/{propertyId}` | Public / Authorized |
| Properties | PUT | `/properties/{propertyId}` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Properties | PATCH | `/properties/{propertyId}/status` | AGENCY_ADMIN / AGENT |
| Properties | PATCH | `/properties/{propertyId}/featured` | AGENCY_ADMIN / SUPER_ADMIN |
| Properties | DELETE | `/properties/{propertyId}` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Properties | GET | `/properties/featured` | Public |
| Properties | GET | `/properties/recent` | Public |
| Properties | GET | `/properties/{propertyId}/similar` | Public |
| Properties | GET | `/properties/search/suggestions` | Public |
| Properties | GET | `/properties/map` | Public |
| Media | POST | `/properties/{propertyId}/images` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Media | GET | `/properties/{propertyId}/images` | Public |
| Media | PATCH | `/media/{mediaId}/main` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Media | PATCH | `/properties/{propertyId}/images/reorder` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Media | DELETE | `/media/{mediaId}` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Media | POST | `/agencies/{agencyId}/logo` | AGENCY_ADMIN / SUPER_ADMIN |
| Media | POST | `/agencies/{agencyId}/cover` | AGENCY_ADMIN / SUPER_ADMIN |
| Favorites | POST | `/favorites/{propertyId}` | CLIENT |
| Favorites | DELETE | `/favorites/{propertyId}` | CLIENT |
| Favorites | GET | `/favorites/me` | CLIENT |
| Inquiries | POST | `/properties/{propertyId}/inquiries` | CLIENT |
| Inquiries | GET | `/inquiries/me` | CLIENT |
| Inquiries | GET | `/agencies/{agencyId}/inquiries` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Inquiries | GET | `/inquiries/{inquiryId}` | CLIENT / AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Inquiries | PATCH | `/inquiries/{inquiryId}/status` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Viewings | POST | `/properties/{propertyId}/viewings` | CLIENT |
| Viewings | GET | `/viewings/me` | CLIENT |
| Viewings | PATCH | `/viewings/{viewingId}/cancel` | CLIENT |
| Viewings | GET | `/agencies/{agencyId}/viewings` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Viewings | PATCH | `/viewings/{viewingId}/status` | AGENCY_ADMIN / AGENT / SUPER_ADMIN |
| Plans | GET | `/subscription-plans` | Public |
| Notifications | GET | `/notifications` | Authenticated |
| Notifications | GET | `/notifications/unread-count` | Authenticated |
| Notifications | PATCH | `/notifications/{notificationId}/read` | Authenticated |
| Notifications | PATCH | `/notifications/read-all` | Authenticated |
| Notifications | DELETE | `/notifications/{notificationId}` | Authenticated |
| Dashboard | GET | `/dashboard/admin` | SUPER_ADMIN |
| Dashboard | GET | `/dashboard/agency` | AGENCY_ADMIN |
| Dashboard | GET | `/dashboard/agent` | AGENT |
| Dashboard | GET | `/dashboard/client` | CLIENT |

---

# 20. HTTP Status Codes

| Code | Meaning |
|---|---|
| `200 OK` | Request completed successfully |
| `201 Created` | Resource created successfully |
| `400 Bad Request` | Validation or request error |
| `401 Unauthorized` | Missing or invalid token |
| `403 Forbidden` | Valid token but insufficient permissions |
| `404 Not Found` | Resource not found |
| `409 Conflict` | Duplicate resource or business conflict |
| `413 Payload Too Large` | File upload exceeds size limit |
| `415 Unsupported Media Type` | Unsupported file type |
| `422 Unprocessable Entity` | Business rule violation |
| `500 Internal Server Error` | Unexpected server error |

---

# 21. Security Notes

```
- Passwords stored using BCrypt (min cost factor 12)
- JWT secret stored in environment variables, never hardcoded
- Access tokens: short-lived (e.g. 15 minutes)
- Refresh tokens: longer-lived (e.g. 7 days), stored server-side hashed for validation
- Password reset tokens: single-use, expire in 15 minutes, stored hashed
- All protected endpoints validate JWT signature and expiry
- Role-based access enforced at controller and service level
- Agency users cannot access another agency's private data
- Agents can only manage resources belonging to their own agency
- Public property endpoints only return ACTIVE listings
- PENDING and REJECTED listings only visible to authorized agency users and SUPER_ADMIN
- File uploads validated by content type (magic bytes), not just extension
- Rate limiting on auth endpoints (register, login, forgot-password)
- PII anonymized on soft-delete
- Paginated results capped to prevent over-fetching (max size: 100)
```
