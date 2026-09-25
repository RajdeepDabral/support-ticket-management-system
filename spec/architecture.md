# Support Ticket Management System

## Architecture Specification

**Document:** Architecture Specification
**Version:** 1.0
**Status:** Approved for Implementation
**Architecture Style:** Layered Modular Architecture
**Backend:** Java 21 + Spring Boot
**Database:** PostgreSQL
**Frontend:** React / Next.js or equivalent
**API:** REST
**Containerization:** Docker + Docker Compose

---

# 1. Architecture Goals

The architecture is designed to satisfy the functional requirements while keeping the implementation:

* Maintainable
* Testable
* Easy to understand
* Easy to run locally
* Suitable for AI-assisted development
* Easy for another engineer to review

The solution intentionally avoids unnecessary distributed-system complexity because the assignment does not require microservices.

The application will initially be implemented as a modular monolith.

---

# 2. High-Level Architecture

The system consists of three primary runtime components:

```text
┌──────────────────────────────────────────┐
│              React Frontend              │
│                                          │
│  Ticket List                             │
│  Ticket Details                          │
│  Create / Update                         │
│  Comments                                │
│  Search / Filter                         │
└────────────────────┬─────────────────────┘
                     │
                     │ HTTP / JSON
                     ▼
┌──────────────────────────────────────────┐
│          Spring Boot Backend             │
│                                          │
│  REST API                                │
│      │                                   │
│      ▼                                   │
│  Application / Service Layer             │
│      │                                   │
│      ├── Ticket Management               │
│      ├── State Transition                │
│      └── Comment Management              │
│      │                                   │
│      ▼                                   │
│  Repository / Persistence Layer          │
└────────────────────┬─────────────────────┘
                     │
                     │ JDBC / JPA
                     ▼
┌──────────────────────────────────────────┐
│               PostgreSQL                 │
│                                          │
│  Tickets                                 │
│  Comments                                │
└──────────────────────────────────────────┘
```

---

# 3. Architectural Style

The backend will use a **layered modular architecture**.

The primary layers are:

```text
API / Controller
       ↓
Application / Service
       ↓
Domain / Business Rules
       ↓
Persistence / Repository
       ↓
PostgreSQL
```

The frontend communicates only with the REST API.

The frontend must not access PostgreSQL directly.

---

# 4. Why a Modular Monolith

A modular monolith is selected instead of microservices.

### Reason

The assignment contains a relatively small bounded scope and does not require:

* independent deployment
* distributed scaling
* service-to-service communication
* event-driven architecture
* service discovery
* distributed transactions

Introducing microservices would add infrastructure and operational complexity without providing a clear benefit for the stated requirements.

The code will still maintain clear module boundaries so that the business logic remains separated and testable.

---

# 5. Backend Module Structure

The backend will be organized around clear responsibilities.

A proposed structure is:

```text
backend/
└── src/
    ├── main/
    │   └── java/
    │       └── <base-package>/
    │           │
    │           ├── controller/
    │           ├── service/
    │           ├── repository/
    │           ├── entity/
    │           ├── dto/
    │           ├── mapper/
    │           ├── validation/
    │           ├── exception/
    │           ├── state/
    │           └── config/
    │
    └── test/
```

The exact package naming will be finalized during project bootstrap.

---

# 6. Controller Layer

Controllers are responsible for:

* Receiving HTTP requests.
* Validating request DTOs through the configured validation mechanism.
* Calling application/service operations.
* Returning appropriate HTTP responses.
* Translating application results into API responses.

Controllers should remain thin.

Controllers must not contain:

* Database access.
* Complex business rules.
* State-machine logic.
* Significant data transformation logic.

Example responsibility:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Response
```

---

# 7. Service Layer

The service layer owns application-level business operations.

Examples include:

* Create ticket.
* Get ticket.
* List tickets.
* Update ticket.
* Change ticket status.
* Add comment.
* Search tickets.
* Filter tickets.

Business rules shall be enforced here or in dedicated domain/business components.

The service layer shall not expose persistence entities directly as public API responses.

---

# 8. State Transition Component

Ticket status transitions are a critical business rule.

The state machine shall be represented explicitly rather than relying on arbitrary status mutation.

Conceptually:

```text
Ticket
  │
  │ requested transition
  ▼
State Transition Validator
  │
  ├── valid ───────→ update status
  │
  └── invalid ─────→ business exception
```

The following transitions are allowed:

```text
OPEN → IN_PROGRESS
IN_PROGRESS → RESOLVED
RESOLVED → CLOSED
OPEN → CANCELLED
IN_PROGRESS → CANCELLED
```

Any other transition must be rejected.

---

# 9. State Machine Design Principle

The implementation must not rely only on frontend restrictions.

For example, hiding `CLOSED → OPEN` in the UI is not sufficient.

A direct API request such as:

```text
PATCH /api/tickets/{id}/status
```

must still be rejected by the backend if the transition is invalid.

This protects the business rule regardless of the client.

---

# 10. Repository Layer

The repository layer is responsible for persistence operations.

It should provide access to PostgreSQL through the selected Spring persistence technology.

The repository layer should not contain application-level business rules such as:

```text
OPEN → IN_PROGRESS allowed
CLOSED → OPEN rejected
```

Those rules belong to the application/domain logic.

---

# 11. Database

PostgreSQL is the application's persistent database.

The initial domain model contains:

```text
Ticket
Comment
```

The high-level relationship is:

```text
Ticket 1 ─────────── * Comment
```

A ticket can have zero or more comments.

A comment belongs to exactly one ticket.

The detailed database model will be defined separately in:

```text
spec/data-model.md
```

---

# 12. Data Access Strategy

The application will use Spring's standard relational persistence capabilities.

The final implementation will use:

* JPA/Hibernate for entity persistence.
* Spring Data repositories where appropriate.
* PostgreSQL as the runtime database.

The persistence model should remain separate from the API model.

Database entities should not be returned directly from REST controllers.

---

# 13. DTO Strategy

The REST API will use DTOs for external communication.

Conceptually:

```text
HTTP Request
     ↓
Request DTO
     ↓
Service
     ↓
Entity
     ↓
Repository
     ↓
PostgreSQL
```

For responses:

```text
PostgreSQL
     ↓
Entity
     ↓
Service
     ↓
Response DTO
     ↓
HTTP Response
```

This prevents the persistence model from becoming tightly coupled to the external API contract.

---

# 14. API Boundary

The frontend communicates with the backend using REST/JSON.

The frontend must not depend on:

* Java classes
* JPA entities
* database structures
* internal service classes

The frontend depends only on the documented REST API contract.

The detailed API specification will be defined in:

```text
spec/api-contract.md
```

---

# 15. Error Handling Architecture

The backend will use centralized exception handling.

The architecture will support meaningful errors for:

* Validation failures.
* Ticket not found.
* Invalid status.
* Invalid state transition.
* Invalid request data.
* Other expected business/application errors.

Conceptually:

```text
Controller
    ↓
Service
    ↓
Exception
    ↓
Global Exception Handler
    ↓
Consistent Error Response
    ↓
Frontend
```

The API should not expose stack traces or internal implementation details.

The exact error response format will be defined in:

```text
spec/api-contract.md
```

---

# 16. Validation Architecture

Validation occurs at multiple levels.

## Frontend

Frontend validation provides immediate feedback to the user.

## Backend

Backend validation is authoritative.

```text
Frontend validation
       +
Backend validation
       +
Business-rule validation
```

The backend must reject invalid requests even if frontend validation is bypassed.

---

# 17. Transaction Management

Database operations involving multiple related changes should be performed within appropriate transaction boundaries.

For example:

```text
Add Comment
    ↓
Persist Comment
```

and operations that modify ticket state shall ensure that the state change is persisted consistently.

Transaction boundaries will be placed at the service/application layer rather than the controller layer.

The implementation should avoid unnecessarily large transactions.

---

# 18. Concurrency Consideration

The application should prevent inconsistent status updates caused by concurrent requests where practical.

For example, two clients should not be able to bypass the state-machine rules by concurrently updating the same ticket.

The implementation approach for concurrency control will be finalized during the data-model and implementation design.

The solution should use the simplest mechanism appropriate for the assignment rather than introducing unnecessary distributed locking.

---

# 19. Search and Filtering

Search and filtering will be implemented in the backend.

The frontend will send the requested criteria to the REST API.

Example:

```text
GET /api/tickets?keyword=payment&status=OPEN
```

The backend will determine the matching records.

This avoids depending on the frontend to load all records before filtering.

---

# 20. Frontend Architecture

The frontend will be implemented using React, Next.js, or an equivalent React-based solution.

The frontend will contain logical areas for:

```text
Ticket List
Ticket Creation
Ticket Details
Ticket Editing
Comments
Search
Status Filtering
Error Handling
```

The frontend will communicate with the backend through HTTP APIs.

The frontend should not contain authoritative business rules.

For example, the UI may disable an invalid transition, but the backend remains responsible for rejecting it.

---

# 21. Frontend Error Handling

The frontend shall distinguish between expected application errors and unexpected failures.

Examples:

```text
Validation error
Ticket not found
Invalid status transition
Network/server error
```

The user should receive a meaningful message.

Raw Java stack traces or internal server details must never be displayed.

---

# 22. Docker Architecture

The project will provide a Docker-based runtime environment.

The expected components are:

```text
┌─────────────────┐
│    Frontend     │
│    Container    │
└────────┬────────┘
         │
         │ HTTP
         ▼
┌─────────────────┐
│     Backend     │
│ Java 21 /       │
│ Spring Boot     │
└────────┬────────┘
         │
         │ PostgreSQL
         ▼
┌─────────────────┐
│   PostgreSQL    │
│    Container    │
└─────────────────┘
```

Docker Compose will orchestrate the required services.

The goal is to allow a reviewer to run the application without manually installing PostgreSQL.

The exact Docker configuration will be implemented after the application architecture and configuration are finalized.

---

# 23. Configuration Management

Configuration shall be externalized where appropriate.

Examples include:

* Database URL.
* Database username.
* Database password.
* Backend port.
* Frontend API URL.

Sensitive configuration must not be committed to GitHub.

The repository may provide safe example configuration.

---

# 24. Testing Architecture

The testing strategy will contain multiple levels.

## Unit Tests

Used for isolated business behavior.

Examples:

```text
TicketService
StateTransitionValidator
Validation logic
```

## Integration Tests

Used to validate multiple application layers together.

Examples:

```text
REST API
Service
Repository
PostgreSQL
```

## State Machine Integration Tests

The complete status transition behavior must be verified.

Both valid and invalid transitions will be tested.

## Frontend Tests

Important user-facing functionality and error handling should be tested where practical.

The detailed test strategy will be documented in:

```text
spec/test-strategy.md
```

---

# 25. Test Database Strategy

PostgreSQL will be the database used by the application.

Automated tests should use an isolated database environment rather than relying on a developer's local database.

The final test setup will be selected during implementation based on the project requirements and Docker/test infrastructure.

The important requirement is that persistence-related tests exercise behavior representative of PostgreSQL.

---

# 26. Logging

The backend should log useful operational information such as:

* Application startup.
* Important business operation failures.
* Unexpected exceptions.
* Database/application errors where appropriate.

Logs must not contain:

* passwords
* tokens
* credentials
* sensitive user information unnecessarily

Logging should support debugging without creating unnecessary noise.

---

# 27. Security Boundaries

Although authentication is outside the current assignment scope, the application shall still follow basic security principles.

The implementation shall:

* Validate incoming data.
* Avoid SQL injection through parameterized persistence mechanisms.
* Avoid exposing stack traces.
* Avoid committing secrets.
* Avoid logging credentials.
* Avoid trusting frontend business rules.

Authentication and authorization will not be introduced unless the approved requirements are changed.

---

# 28. AI-Assisted Engineering Architecture

AI tools are part of the development process but do not replace the architecture specification.

The relationship is:

```text
Human Requirements
        ↓
Human-reviewed Specification
        ↓
AI-assisted Planning
        ↓
AI-assisted Implementation
        ↓
Automated Tests
        ↓
AI Review
        ↓
Human Review
        ↓
Approved Implementation
```

AI-generated implementation must follow the approved specifications.

If AI proposes an architectural change, the change must be reviewed before implementation.

---

# 29. AI Context Management

Reusable engineering guidance will be maintained in the repository.

The expected structure is:

```text
.cursor/
├── rules/
│   ├── java-springboot.md
│   ├── testing.md
│   ├── api-standards.md
│   └── documentation.md
│
└── commands/
    ├── review-code.md
    ├── review-spec.md
    └── generate-tests.md
```

These files provide reusable project context to the AI assistant.

They are not a replacement for the formal specifications.

---

# 30. Specification Hierarchy

When implementing the system, the following hierarchy should be respected:

```text
Assignment Requirements
        ↓
spec/requirements.md
        ↓
Architecture Specification
        ↓
API / Data / State Specifications
        ↓
Implementation Tasks
        ↓
Code
```

If generated code conflicts with an approved specification, the code must be corrected rather than silently changing the requirement.

---

# 31. Architecture Decision Principles

The following principles will guide implementation:

### Principle 1 — Prefer simplicity

Do not introduce technology or architecture that is not justified by the requirements.

### Principle 2 — Backend owns business rules

Business-critical rules must be enforced server-side.

### Principle 3 — Explicit state transitions

Ticket lifecycle transitions must be explicit and testable.

### Principle 4 — API boundaries use DTOs

Persistence entities should not become the public API contract.

### Principle 5 — Test business-critical behavior

State transitions, validation, persistence, and API behavior require automated verification.

### Principle 6 — Human approval remains required

AI-generated code is considered a proposal until reviewed and tested.

### Principle 7 — Specification before implementation

Implementation should follow the approved specification rather than defining requirements implicitly through code.

---

# 32. Architecture Trade-offs

## Modular Monolith vs Microservices

**Decision:** Modular monolith.

**Reason:** The assignment does not require distributed deployment or independent service scaling. A modular monolith provides sufficient separation while keeping the system easier to develop, test, run, and review.

---

## PostgreSQL vs In-Memory Database

**Decision:** PostgreSQL.

**Reason:** The requirements explicitly require persistent data that survives application restart.

---

## Direct Entity Exposure vs DTOs

**Decision:** DTOs.

**Reason:** DTOs provide a stable API boundary and prevent database entities from becoming coupled to external clients.

---

## Frontend State Validation vs Backend State Validation

**Decision:** Both, with backend authoritative.

**Reason:** Frontend validation improves usability, while backend validation protects the business rule from direct or malicious API requests.

---

## Full Authentication vs No Authentication

**Decision:** No authentication in the current scope.

**Reason:** Authentication is not part of the stated assignment requirements. Adding it would increase complexity without improving the evaluation of the core ticket-management requirements.

---

# 33. Architecture Review Checklist

Before implementation begins, the following shall be reviewed:

* [ ] Layer responsibilities are clear.
* [ ] PostgreSQL is the persistent database.
* [ ] REST API boundary is defined.
* [ ] DTO strategy is defined.
* [ ] State-machine responsibility is defined.
* [ ] Backend validation is authoritative.
* [ ] Error handling approach is defined.
* [ ] Transaction boundaries are defined.
* [ ] Docker architecture is defined.
* [ ] Testing approach is defined.
* [ ] AI context strategy is defined.
* [ ] Architecture does not introduce unnecessary complexity.

---

# 34. Approval

This architecture specification provides the technical direction for implementation.

Detailed specifications for the database model, REST API, state machine, UI flow, and testing will be created before implementation begins.

**Status:** Ready for Detailed Specifications
