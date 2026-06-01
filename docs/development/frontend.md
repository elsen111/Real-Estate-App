# EstateFlow Frontend Documentation

## 1. Frontend Goal

The frontend should provide a clean, modern, responsive interface for clients, agencies, agents, and admins. It must connect to the Spring Boot backend through REST APIs.

The frontend should clearly show that EstateFlow is both:

- a public real estate marketplace
- an agency CRM dashboard system

## 2. Frontend Tech Stack

- React
- TypeScript
- Vite
- React Router
- Axios
- Tailwind CSS
- React Hook Form
- Zod
- React Leaflet / OpenStreetMap
- Recharts
- Context API or Redux Toolkit

## 3. Suggested Frontend Structure

```bash
frontend/src/
├── assets/
├── components/
│   ├── common/
│   ├── layout/
│   ├── property/
│   ├── agency/
│   ├── dashboard/
│   ├── map/
│   └── forms/
├── constants/
├── context/
├── hooks/
├── pages/
│   ├── public/
│   ├── auth/
│   ├── admin/
│   ├── agency/
│   ├── agent/
│   └── client/
├── routes/
├── schemas/
├── services/
├── types/
├── utils/
├── App.tsx
└── main.tsx
```

## 4. Main Pages

## Public Pages

### Home Page

Sections:

- Hero search section
- Featured properties
- Latest properties
- Map preview
- Agency promotion section
- Footer

### Properties Page

Features:

- Property cards
- Search input
- Filter sidebar
- Sort dropdown
- Pagination
- Link to map view

### Property Details Page

Features:

- Image gallery
- Title and price
- Location information
- Features and specifications
- Agency and agent information
- Mini map
- Favorite button
- Inquiry form
- Viewing request button

### Map Search Page

Features:

- Full map view
- Property markers
- Marker popup
- Filters
- Link to property details

### Agencies Page

Features:

- Agency list
- Agency profile cards
- Link to agency details

### Agency Details Page

Features:

- Agency profile
- Contact information
- Agency properties
- Agents

### About Page

Explain the project and platform purpose.

### Contact Page

Contact form and company information.

## 5. Auth Pages

### Login Page

Fields:

- email
- password

Flow:

1. User submits form
2. Frontend calls `/api/auth/login`
3. Access token is stored safely
4. User is redirected by role

### Register Page

Possible registration types:

- Client registration
- Agency registration

For MVP, start with Client and Agency Admin registration.

## 6. Dashboards

## Super Admin Dashboard

Pages:

- Overview
- Manage users
- Manage agencies
- Pending properties
- Subscription plans

Statistics:

- Total users
- Total agencies
- Total properties
- Pending properties
- Active subscriptions

## Agency Admin Dashboard

Pages:

- Overview
- Agency profile
- Properties
- Agents
- Inquiries
- Viewing requests
- Subscription status

Statistics:

- Total properties
- Active listings
- Total agents
- New inquiries
- Upcoming viewings

## Agent Dashboard

Pages:

- Assigned properties
- Inquiries
- Viewing requests

## Client Dashboard

Pages:

- Favorite properties
- Sent inquiries
- Viewing requests

## 7. Core Components

### Layout Components

- Navbar
- Sidebar
- Footer
- DashboardLayout
- PublicLayout
- ProtectedRoute
- RoleBasedRoute

### Property Components

- PropertyCard
- PropertyGrid
- PropertyFilters
- PropertySearchBar
- PropertyGallery
- PropertyDetailsInfo
- PropertyForm
- PropertyStatusBadge

### Map Components

- PropertyMap
- PropertyMarker
- PropertyPopup
- MapFilters

### Dashboard Components

- StatCard
- DataTable
- DashboardHeader
- DashboardSidebar
- StatusBadge
- EmptyState

### Form Components

- Input
- Select
- Textarea
- FileUpload
- DateTimePicker
- FormError

## 8. Frontend Routing

```text
/                         → Home
/properties                → Public property list
/properties/:id            → Property details
/map                       → Map search
/agencies                  → Agencies list
/agencies/:id              → Agency details
/about                     → About
/contact                   → Contact
/login                     → Login
/register                  → Register

/dashboard/admin           → Super Admin dashboard
/dashboard/admin/users     → Manage users
/dashboard/admin/agencies  → Manage agencies
/dashboard/admin/properties/pending → Pending properties

/dashboard/agency          → Agency dashboard
/dashboard/agency/profile  → Agency profile
/dashboard/agency/properties → Agency properties
/dashboard/agency/agents   → Manage agents
/dashboard/agency/inquiries → Inquiries
/dashboard/agency/viewings → Viewing requests

/dashboard/agent           → Agent dashboard
/dashboard/agent/properties → Assigned properties
/dashboard/agent/inquiries → Agent inquiries
/dashboard/agent/viewings  → Agent viewings

/dashboard/client          → Client dashboard
/dashboard/client/favorites → Favorites
/dashboard/client/inquiries → Sent inquiries
/dashboard/client/viewings → Viewing requests
```

## 9. State Management

For MVP, you can use Context API for authentication and React Query or custom hooks for API fetching.

Recommended global states:

- authenticated user
- access token
- role
- selected language
- saved UI filters

If the app grows, use Redux Toolkit.

## 10. API Service Structure

```bash
services/
├── apiClient.ts
├── authService.ts
├── propertyService.ts
├── agencyService.ts
├── agentService.ts
├── favoriteService.ts
├── inquiryService.ts
├── viewingService.ts
├── subscriptionService.ts
└── dashboardService.ts
```

## 11. Axios API Client

Responsibilities:

- Set base URL
- Attach Authorization token
- Handle 401 errors
- Redirect to login when token expires

## 12. Forms and Validation

Use React Hook Form + Zod.

Required forms:

- Login form
- Register form
- Agency profile form
- Agent form
- Property form
- Inquiry form
- Viewing request form

## 13. Map Feature Requirements

Use React Leaflet with OpenStreetMap for free map functionality.

Required map behavior:

- Load active properties with latitude and longitude
- Show markers on map
- Click marker to show popup
- Popup includes property image, title, price, and details button
- Filters should update both list and map results

## 14. UI/UX Requirements

- Responsive design
- Clean dashboard layout
- Clear status badges
- Loading states
- Empty states
- Error messages
- Confirmation modals for delete actions
- Mobile-friendly property cards

## 15. Frontend Development Phases

### Phase 1 — Setup

- Create Vite React TypeScript project
- Configure Tailwind
- Configure routing
- Create layouts

### Phase 2 — Auth

- Login
- Register
- Protected routes
- Role-based redirect

### Phase 3 — Public Marketplace

- Home page
- Properties page
- Property details page
- Search and filters

### Phase 4 — Map

- Map page
- Property markers
- Marker popups
- Filter integration

### Phase 5 — Dashboards

- Admin dashboard
- Agency dashboard
- Agent dashboard
- Client dashboard

### Phase 6 — Forms and Management

- Property CRUD
- Agent CRUD
- Agency profile
- Inquiry management
- Viewing management

### Phase 7 — Polish

- Loading states
- Error handling
- Responsive design
- Final presentation UI

## 16. Frontend MVP Checklist

- [ ] Public layout completed
- [ ] Dashboard layout completed
- [ ] Login/register completed
- [ ] Protected routes completed
- [ ] Properties page completed
- [ ] Property details completed
- [ ] Map page completed
- [ ] Agency dashboard completed
- [ ] Agent dashboard completed
- [ ] Client dashboard completed
- [ ] Admin dashboard completed
- [ ] Forms completed
- [ ] API integration completed
- [ ] Responsive design completed
