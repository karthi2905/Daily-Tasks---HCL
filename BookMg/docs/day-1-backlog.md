# BookMg — Day 1 Product Backlog & Functional Requirements Analysis

## 1. Project Overview & Context

**BookMg** is an enterprise meeting room and shared resource management platform architected as a distributed microservices system (Spring Boot 3.3.5, Spring Cloud Gateway, PostgreSQL/H2, OpenFeign, JJWT, and React 18).

This document captures the **8 Functional Requirements (FRs)** derived directly from the `BookMg` system architecture, services, domain models, and API contracts, mapped into Agile User Stories with Fibonacci story point estimations and an enterprise Definition of Done (DoD).

---

## 2. Functional Requirements (Source-Derived)

The following 8 Functional Requirements represent the core capabilities of the BookMg system:

| FR | Functional Requirement | Scope & Architectural Source |
|---|---|---|
| **FR1** | **User Authentication & Role-Based Access Control (RBAC)** | `auth-service` — Secure user registration, credential verification with BCrypt, JJWT 0.12.6 token generation, and multi-tier role authorization (`ROLE_EMPLOYEE`, `ROLE_MANAGER`, `ROLE_ADMIN`) with department scoping. |
| **FR2** | **Resource Catalog & Asset Management** | `resource-service` — Administrative CRUD operations for physical assets (meeting rooms, training labs, AV equipment) specifying capacity, physical location, equipment amenities, and restricted access flags. |
| **FR3** | **Dynamic Resource Search & Multi-Criteria Filtering** | `resource-service` — Dynamic resource query engine using Spring Data JPA Specifications supporting multi-parameter filtering by resource type, minimum capacity, location, keyword search, and restriction status. |
| **FR4** | **Atomic Room Reservation & Overlap Conflict Detection** | `booking-service` — Single-slot reservation processing with continuous interval overlap detection ($S_1 < E_2 \text{ and } E_1 > S_2$), JPA optimistic locking (`@Version`), and cross-service resource validation via OpenFeign. |
| **FR5** | **Recurring Bookings Engine & Series Management** | `booking-service` — Strategy-driven generation of recurring meeting series (daily, weekly, bi-weekly, monthly) bounded by a strict 90-day (3-month) ceiling, atomic batch conflict validation, and cascade series cancellation via `recurrenceGroupId`. |
| **FR6** | **Multi-Tier Approval Policy Engine for Restricted Assets** | `booking-service` — Policy Factory workflow routing bookings for restricted assets (e.g., executive boardrooms, compute clusters) to department managers or global admins, managing `PENDING_APPROVAL`, `CONFIRMED`, and `REJECTED` states. |
| **FR7** | **Attendance Check-In & Automated No-Show Auto-Release** | `booking-service` — Anti-ghost-booking engine with a ±15 minute check-in window and a `@Scheduled` background worker automatically releasing unconfirmed rooms to `CANCELLED (AUTO_RELEASED_NO_SHOW)`. |
| **FR8** | **Real-Time Availability Grid & Space Utilization Analytics** | `booking-service` — Continuous time-slice availability calendar grid (08:00–20:00) and in-memory Java Stream analytics computing utilization percentage, peak usage hours, and department cancellation ratios. |

---

## 3. User Stories & Story Point Estimations

Each Functional Requirement is translated into an Agile User Story (`As a <role>, I want <function>, so that <benefit>`) and estimated using the Fibonacci scale (1, 2, 3, 5, 8, 13) based on implementation complexity, concurrency constraints, cross-service communication, and validation effort.

| FR | User Story ID | User Story | Story Points | Complexity Rationale |
|---|---|---|:---:|---|
| **FR1** | **US-01** | **As an Employee**, I want to securely register and log into the system with my credentials, **so that** I receive a cryptographically signed JWT token to access authorized booking services. | **3** | Moderate: BCrypt hashing, JWT minting/claims injection, Spring Security filters, and seed data initialization. |
| **FR2** | **US-02** | **As an Administrator**, I want to create, update, and manage room and resource records with defined capacities, amenities, and restriction rules, **so that** organizational physical assets are accurately maintained. | **3** | Moderate: Entity modeling, DTO validation, REST CRUD endpoints, and admin role enforcement. |
| **FR3** | **US-03** | **As an Employee**, I want to search and filter available rooms by type, minimum capacity, location, and amenities, **so that** I can rapidly locate a room suitable for my team size and event type. | **2** | Low-Moderate: Dynamic Spring Data JPA Specification predicates, pagination, and clean REST query parameters. |
| **FR4** | **US-04** | **As an Employee**, I want to book an available resource for a specified time slot with guaranteed overlap protection, **so that** two meetings are never double-booked in the same space at the same time. | **5** | High: Continuous interval overlap mathematics ($S_1 < E_2 \land E_1 > S_2$), OpenFeign remote validation, transaction isolation, and optimistic concurrency locking. |
| **FR5** | **US-05** | **As a Project Lead**, I want to schedule recurring meetings (daily, weekly, bi-weekly, monthly) up to a 90-day ceiling and cancel entire series in bulk, **so that** regular team syncs are scheduled without repetitive manual entry. | **8** | Very High: Strategy Pattern implementations, date-math slot generation, atomic batch overlap validation, 90-day hard limit, and cascading series deletion via UUID group ID. |
| **FR6** | **US-06** | **As a Department Manager or Admin**, I want to review, approve, or reject booking requests for restricted company assets, **so that** sensitive high-value rooms and equipment are allocated properly. | **5** | High: Policy Factory pattern, department matching logic, state machine transitions, audit history, and rejection notes. |
| **FR7** | **US-07** | **As an Organizer**, I want to confirm room check-in within ±15 minutes of the start time, and **as the System**, automatically cancel unconfirmed bookings after 15 minutes, **so that** abandoned reservations are freed for others. | **5** | High: Time-window calculation, Spring `@Scheduled` cron/fixedRate daemon, state transitions, and immediate slot release for ad-hoc bookings. |
| **FR8** | **US-08** | **As a Facilities Manager**, I want to inspect a live hourly availability grid and generate space utilization analytics, **so that** employees can spot open slots and leadership can optimize real estate usage. | **5** | High: Dynamic business-hour time slicing (08:00–20:00), Java Stream grouping/aggregation collectors, peak-hour calculations, and metric reporting. |

**Total Estimated Backlog Effort**: **36 Story Points**

---

## 4. Definition of Done (DoD)

A user story is considered **Done** and acceptable for production deployment when all of the following criteria are satisfied:

1. **Acceptance Criteria**: All business rules, functional criteria, and edge cases defined in the story are fully met.
2. **API & Data Contracts**: REST endpoints strictly adhere to defined request/response DTO schemas and appropriate HTTP status codes (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`, `409 Conflict`).
3. **Data Integrity & Concurrency**: Database updates maintain transactional consistency (`@Transactional`), avoid dirty reads, and handle race conditions via optimistic locking (`@Version`).
4. **Validation & Exception Handling**: Input boundaries, nullability, and formats are enforced with Bean Validation (`@Valid`, `@NotNull`), returning descriptive problem-details responses.
5. **Security & Authorization**: Proper role-based authorization (`ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_EMPLOYEE`) and gateway header identity propagation (`X-User-Id`, `X-User-Role`) are active.
6. **Automated Testing**: Unit tests and integration tests are written and passing (`mvn clean test`).
7. **Code Quality**: Code follows standard Java 17 and Spring Boot idioms, clean code principles, and compiles with zero compiler warnings.
8. **Documentation**: Code is documented with clear Javadoc where necessary; API endpoints are documented in Swagger/OpenAPI specifications.
9. **Version Control**: Changes are staged, verified, and committed with meaningful Git commit messages.
10. **Demo Ready**: The functionality can be demonstrated and verified end-to-end via REST clients (cURL, Postman) or web UI.

---

## 5. GitHub Projects / Jira Board Ready Cards

These card definitions are ready for immediate entry into a GitHub Projects board or Jira backlog:

---

### Card 1: US-01 — User Authentication & Role-Based Access Control
- **Title**: `US-01: User Authentication & Role-Based Access Control (RBAC)`
- **Source**: `FR1`
- **Story Points**: `3`
- **Description**:
  ```text
  As an Employee,
  I want to securely register and log into the system with my corporate email and password,
  So that I receive a cryptographically signed JWT token to access authorized booking services.
  ```
- **Acceptance Criteria**:
  - `POST /api/v1/auth/register` validates email uniqueness and stores password using BCrypt.
  - `POST /api/v1/auth/login` verifies credentials and returns HMAC-SHA256 signed JWT with user ID, email, and roles.
  - Pre-seeded users (`admin@bookmg.com`, `manager@bookmg.com`, `user@bookmg.com`) are available out of the box.

---

### Card 2: US-02 — Resource Catalog & Asset Management
- **Title**: `US-02: Resource Catalog & Asset Management`
- **Source**: `FR2`
- **Story Points**: `3`
- **Description**:
  ```text
  As an Administrator,
  I want to create, update, and manage room and resource records with defined capacities and amenities,
  So that organizational physical assets are accurately maintained in the system.
  ```
- **Acceptance Criteria**:
  - `POST /api/v1/resources` allows `ROLE_ADMIN` to add rooms/equipment with name, type, capacity, location, and restricted flag.
  - `PUT /api/v1/resources/{id}` and `DELETE /api/v1/resources/{id}` update and soft-delete resources.
  - Non-admin users attempting resource modifications receive `403 Forbidden`.

---

### Card 3: US-03 — Dynamic Resource Search & Multi-Criteria Filtering
- **Title**: `US-03: Dynamic Resource Search & Multi-Criteria Filtering`
- **Source**: `FR3`
- **Story Points**: `2`
- **Description**:
  ```text
  As an Employee,
  I want to search and filter available rooms by type, minimum capacity, location, and amenities,
  So that I can quickly find an appropriate space for my team's needs.
  ```
- **Acceptance Criteria**:
  - `GET /api/v1/resources` accepts query params `type`, `minCapacity`, `search`, and `restricted`.
  - Spring Data JPA Specifications dynamically combine non-null predicates without SQL concatenation.
  - Returns paginated results with resource amenities.

---

### Card 4: US-04 — Room Reservation & Atomic Conflict Detection
- **Title**: `US-04: Room Reservation & Atomic Conflict Detection`
- **Source**: `FR4`
- **Story Points**: `5`
- **Description**:
  ```text
  As an Employee,
  I want to book an available resource for a specific time slot with guaranteed overlap protection,
  So that two meetings are never double-booked in the same space at the same time.
  ```
- **Acceptance Criteria**:
  - `POST /api/v1/bookings` accepts resourceId, startTime, endTime, and meeting title.
  - Validates interval overlap ($S_1 < E_2 \land E_1 > S_2$). Back-to-back bookings ($E_1 = S_2$) are accepted.
  - Overlapping requests reject with `409 Conflict`.
  - OpenFeign validates resource existence against `resource-service`.

---

### Card 5: US-05 — Recurring Bookings Engine & Series Management
- **Title**: `US-05: Recurring Bookings Engine & Series Management`
- **Source**: `FR5`
- **Story Points**: `8`
- **Description**:
  ```text
  As a Project Lead,
  I want to schedule recurring meetings (daily, weekly, bi-weekly, monthly) up to a 90-day ceiling and cancel entire series in bulk,
  So that recurring team syncs are scheduled without repetitive manual entry.
  ```
- **Acceptance Criteria**:
  - Supports `DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY` recurrences using Strategy Pattern.
  - Enforces hard ceiling: `recurrenceUntil` cannot exceed 90 days from start date.
  - Atomic batch check: if even one future occurrence conflicts, the entire series is aborted with `409 Conflict`.
  - `DELETE /api/v1/bookings/series/{recurrenceGroupId}` cascades cancellation across all active occurrences.

---

### Card 6: US-06 — Multi-Tier Approval Workflow for Restricted Resources
- **Title**: `US-06: Multi-Tier Approval Workflow for Restricted Resources`
- **Source**: `FR6`
- **Story Points**: `5`
- **Description**:
  ```text
  As a Department Manager or Admin,
  I want to review, approve, or reject booking requests for restricted company assets,
  So that sensitive high-value resources are protected and allocated appropriately.
  ```
- **Acceptance Criteria**:
  - Bookings for restricted resources initialize in `PENDING_APPROVAL` status.
  - Managers can approve/reject requests from their own department (`ROLE_MANAGER`).
  - Admins can approve/reject any booking enterprise-wide (`ROLE_ADMIN`).
  - Approval moves booking to `CONFIRMED`; rejection moves booking to `REJECTED` with an audit reason.

---

### Card 7: US-07 — Attendance Check-In & Automated No-Show Auto-Release
- **Title**: `US-07: Attendance Check-In & Automated No-Show Auto-Release`
- **Source**: `FR7`
- **Story Points**: `5`
- **Description**:
  ```text
  As an Organizer,
  I want to check in to my room within ±15 minutes of the start time, and as the System, automatically release unattended rooms after 15 minutes,
  So that ghost bookings are eliminated and empty rooms become available for others.
  ```
- **Acceptance Criteria**:
  - `POST /api/v1/bookings/{id}/checkin` allows check-in between $[T_{\text{start}}-15\,\text{min}, T_{\text{start}}+15\,\text{min}]$.
  - Background `@Scheduled` job runs every 60 seconds.
  - Reservations where current time exceeds $T_{\text{start}} + 15\,\text{min}$ without check-in transition to `CANCELLED (AUTO_RELEASED_NO_SHOW)`.
  - Released room immediately appears as available on the calendar grid.

---

### Card 8: US-08 — Real-Time Availability Grid & Resource Utilization Analytics
- **Title**: `US-08: Real-Time Availability Grid & Resource Utilization Analytics`
- **Source**: `FR8`
- **Story Points**: `5`
- **Description**:
  ```text
  As a Facilities Manager,
  I want to inspect a live hourly availability grid and generate space utilization analytics,
  So that employees can spot open slots and leadership can optimize real estate usage.
  ```
- **Acceptance Criteria**:
  - `GET /api/v1/bookings/availability` projects operational hours (08:00–20:00) into 1-hour slots tagged `AVAILABLE` or `OCCUPIED`.
  - `GET /api/v1/reports/utilisation` calculates space utilization percentage using Java Streams.
  - Report highlights peak usage hours and department cancellation/no-show ratios.
