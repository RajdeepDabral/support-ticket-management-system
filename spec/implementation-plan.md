# Support Ticket Management System — Implementation Plan

## 1. Purpose

This document converts the approved specifications into small, reviewable implementation tasks.

The implementation will follow:

```text
Requirements
    ↓
Specifications
    ↓
Implementation Plan
    ↓
Small Implementation Tasks
    ↓
Tests
    ↓
AI Review
    ↓
Human Review
    ↓
Fixes
```

The implementation will be performed incrementally.

The project must not be generated as one large AI task.

---

# 2. Approved Specifications

Implementation must follow these specifications:

```text
spec/requirements.md
spec/architecture.md
spec/data-model.md
spec/api-contract.md
spec/state-machine.md
spec/ui-flow.md
spec/test-strategy.md
```

If implementation conflicts with these specifications, the specification must be reviewed before changing the code.

---

# 3. Implementation Principles

The implementation should follow these principles:

1. Java 21.
2. Spring Boot.
3. PostgreSQL only.
4. REST API.
5. React/Next.js or equivalent frontend.
6. Layered modular monolith architecture.
7. DTOs at API boundaries.
8. Business rules belong in the backend.
9. State transitions have one authoritative implementation.
10. Controllers remain thin.
11. Service/application layer owns use cases.
12. Repository layer owns persistence access.
13. Validation is enforced on the backend.
14. Frontend validation improves user experience but does not replace backend validation.
15. Tests are implemented alongside production behavior.
16. No unnecessary functionality should be introduced.
17. Configuration and secrets must remain externalized.
18. Every meaningful AI-generated change receives human review.

---

# 4. Implementation Phases

The implementation will be divided into:

```text
Phase 1  Project Bootstrap
Phase 2  Database and Persistence
Phase 3  Domain Model
Phase 4  State Machine
Phase 5  Ticket Creation
Phase 6  Ticket Retrieval
Phase 7  Ticket Update
Phase 8  Ticket Status Transitions
Phase 9  Comments
Phase 10 Search and Filtering
Phase 11 Error Handling and Validation
Phase 12 Backend Integration Testing
Phase 13 Frontend Bootstrap
Phase 14 Ticket List UI
Phase 15 Ticket Creation UI
Phase 16 Ticket Details UI
Phase 17 Frontend Error/Loading States
Phase 18 End-to-End Verification
Phase 19 Dockerization
Phase 20 AI Review and Human Review
Phase 21 Documentation and Submission
```

Each phase should be implemented and reviewed independently.

---

# 5. Phase 1 — Project Bootstrap

## Goal

Create the basic backend project structure without implementing business functionality.

### Tasks

* Create Spring Boot project.
* Configure Java 21.
* Configure Gradle or Maven.
* Add required Spring dependencies.
* Add PostgreSQL driver.
* Add JPA/Spring Data dependency.
* Add validation dependency.
* Add testing dependencies.
* Configure application profiles if needed.
* Create basic package structure.
* Verify application starts successfully.

### Expected package structure

The exact package name will depend on the project name, but the structure should follow:

```text
src/main/java
└── <base-package>
    ├── controller
    ├── service
    ├── domain
    ├── repository
    ├── dto
    ├── exception
    ├── config
    └── mapper
```

The implementation should not create unnecessary packages.

### Verification

```text
Application starts successfully.
```

No ticket functionality is required yet.

---

# 6. Phase 2 — Database and Persistence

## Goal

Establish PostgreSQL persistence.

### Tasks

* Configure PostgreSQL connection.
* Configure JPA/Hibernate.
* Configure database migrations.
* Create ticket table.
* Create comment table.
* Create required constraints.
* Create required indexes.
* Verify database connectivity.
* Verify application can start against PostgreSQL.

### Requirements

The database must follow:

```text
spec/data-model.md
```

H2 must not be introduced.

### Verification

Application successfully connects to PostgreSQL.

---

# 7. Phase 3 — Domain Model

## Goal

Create the core domain representation.

### Tasks

Implement:

```text
Ticket
Comment
TicketStatus
TicketPriority
```

### Ticket

Ticket must represent:

```text
id
title
description
priority
status
assignee
createdAt
updatedAt
```

### Comment

Comment must represent:

```text
id
ticketId / ticket relationship
content
author
createdAt
```

### Domain Rules

* New ticket starts as `OPEN`.
* Supported status values follow the state-machine specification.
* Supported priority values follow the API contract.
* Required fields cannot contain invalid values.

---

# 8. Phase 4 — State Machine

## Goal

Implement the authoritative ticket state-transition logic.

### Tasks

* Create state-transition component.
* Implement allowed transitions.
* Reject invalid transitions.
* Reject same-state transitions.
* Produce appropriate domain/business exception.
* Unit test all valid transitions.
* Unit test important invalid transitions.

### Required transitions

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED

IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED

RESOLVED → CLOSED
```

### Terminal states

```text
CLOSED
CANCELLED
```

No further transition is allowed.

### Verification

All state-machine unit tests pass.

---

# 9. Phase 5 — Ticket Creation

## Goal

Implement ticket creation.

### Tasks

* Create create-ticket request DTO.
* Create ticket response DTO.
* Implement validation.
* Implement service operation.
* Persist ticket.
* Default status to `OPEN`.
* Set timestamps.
* Implement `POST /api/tickets`.
* Add unit tests.
* Add API integration tests.

### Verification

Valid ticket:

```text
POST /api/tickets
```

returns:

```text
201 Created
```

and status:

```text
OPEN
```

---

# 10. Phase 6 — Ticket Retrieval

## Goal

Implement ticket listing and details.

### Tasks

* Implement `GET /api/tickets`.
* Implement `GET /api/tickets/{ticketId}`.
* Create list response mapping.
* Create detail response mapping.
* Load comments for ticket details.
* Handle ticket-not-found behavior.
* Add tests.

### Verification

Existing tickets can be:

* listed
* retrieved individually
* retrieved with comments

---

# 11. Phase 7 — Ticket Update

## Goal

Implement editing of ticket fields.

### Supported fields

```text
title
description
priority
assignee
```

### Tasks

* Create update request DTO.
* Implement partial update behavior.
* Validate supplied fields.
* Persist changes.
* Update `updatedAt`.
* Ensure status cannot be changed through this endpoint.
* Add unit tests.
* Add integration tests.

### Verification

Example:

```http
PATCH /api/tickets/1001
```

with:

```json
{
  "priority": "CRITICAL"
}
```

updates priority without modifying status.

---

# 12. Phase 8 — Ticket Status Transitions

## Goal

Expose the state machine through the REST API.

### Endpoint

```text
PATCH /api/tickets/{ticketId}/status
```

### Tasks

* Create status update request DTO.
* Load ticket.
* Validate requested status.
* Invoke state-transition component.
* Persist new status.
* Update `updatedAt`.
* Return updated ticket.
* Handle invalid transitions.
* Handle ticket-not-found.
* Add integration tests.

### Expected errors

Invalid status:

```text
400 Bad Request
```

Invalid transition:

```text
409 Conflict
```

Missing ticket:

```text
404 Not Found
```

### Critical integration test

```text
OPEN → CLOSED
```

must return:

```text
409 Conflict
```

and database status must remain:

```text
OPEN
```

---

# 13. Phase 9 — Comments

## Goal

Allow comments to be added to tickets.

### Tasks

* Create comment request DTO.
* Create comment response DTO.
* Implement comment service.
* Persist comment.
* Associate comment with ticket.
* Implement:

```text
POST /api/tickets/{ticketId}/comments
```

* Include comments in ticket details.
* Add validation.
* Add tests.

### Verification

A comment:

* is created successfully
* belongs to the correct ticket
* is returned in ticket details
* survives application restart

---

# 14. Phase 10 — Search and Filtering

## Goal

Implement server-side ticket search and status filtering.

### Search

Search:

```text
title
description
```

Search must be case-insensitive.

### Filter

Support:

```text
status
```

### Combined query

Support:

```text
keyword + status
```

Example:

```http
GET /api/tickets?keyword=payment&status=OPEN
```

### Tasks

* Implement repository query.
* Implement service behavior.
* Implement query parameters.
* Add repository tests.
* Add API integration tests.

### Verification

Search and filtering happen in the backend.

The frontend must not load every ticket and filter locally.

---

# 15. Phase 11 — Error Handling and Validation

## Goal

Provide consistent API behavior.

### Tasks

* Add request validation.
* Create application exceptions.
* Create ticket-not-found exception.
* Create invalid-transition exception.
* Create invalid-status handling.
* Implement global exception handling.
* Implement standard error response.
* Add field-level validation errors.
* Prevent stack traces from reaching clients.
* Add tests.

### Error format

Follow:

```text
spec/api-contract.md
```

Example:

```json
{
  "timestamp": "2026-09-25T10:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "path": "/api/tickets",
  "fieldErrors": {
    "title": "Title must not be blank"
  }
}
```

---

# 16. Phase 12 — Backend Integration Testing

## Goal

Verify the backend as a complete system.

### Tasks

Test:

* ticket creation
* ticket listing
* ticket details
* ticket updates
* status transitions
* invalid transitions
* comments
* search
* filtering
* combined filtering
* validation
* error responses
* persistence

### Database

Integration tests must use PostgreSQL-compatible behavior.

H2 must not be introduced.

### Critical workflow

```text
Create
 ↓
Retrieve
 ↓
Update
 ↓
Status transition
 ↓
Comment
 ↓
Retrieve again
```

All changes must remain persisted.

---

# 17. Phase 13 — Frontend Bootstrap

## Goal

Create the frontend foundation.

### Tasks

* Initialize React/Next.js application.
* Configure development environment.
* Create application layout.
* Configure API base URL through environment configuration.
* Create basic navigation.
* Add reusable API client mechanism.
* Add basic error handling mechanism.

No business-heavy UI is required yet.

---

# 18. Phase 14 — Ticket List UI

## Goal

Implement ticket listing.

### Tasks

* Create ticket list screen.
* Load tickets from API.
* Display ticket fields.
* Add keyword search.
* Add status filter.
* Support combined search/filter.
* Add loading state.
* Add empty state.
* Add API error state.
* Add navigation to details.
* Add create-ticket navigation.

### Verification

User can view, search, and filter tickets.

---

# 19. Phase 15 — Ticket Creation UI

## Goal

Implement ticket creation flow.

### Tasks

* Create ticket form.
* Add client-side validation.
* Connect to create API.
* Display submitting state.
* Prevent duplicate submission.
* Display backend validation errors.
* Display success feedback.
* Navigate to created ticket.

### Verification

A user can create a ticket entirely through the UI.

---

# 20. Phase 16 — Ticket Details UI

## Goal

Implement ticket details and editing.

### Tasks

* Display ticket information.
* Display comments.
* Add edit functionality.
* Update title.
* Update description.
* Update priority.
* Update assignee.
* Add status actions.
* Connect status endpoint.
* Add comment form.

### Verification

The complete ticket workflow is available through the UI.

---

# 21. Phase 17 — Frontend Error and Loading States

## Goal

Provide meaningful user feedback.

### Tasks

Handle:

```text
400
404
409
500
```

Provide:

* loading indicators
* empty states
* validation messages
* retry actions
* status-transition errors
* ticket-not-found behavior
* comment errors
* update errors

The frontend must not display:

* stack traces
* SQL errors
* Java class names
* internal server implementation details

---

# 22. Phase 18 — End-to-End Verification

## Goal

Verify the complete application against the acceptance criteria.

### Workflow

```text
Open application
    ↓
Create ticket
    ↓
Verify OPEN
    ↓
Search ticket
    ↓
Filter ticket
    ↓
Open details
    ↓
Update ticket
    ↓
Add comment
    ↓
OPEN → IN_PROGRESS
    ↓
IN_PROGRESS → RESOLVED
    ↓
RESOLVED → CLOSED
```

Also verify:

```text
OPEN → CLOSED
```

is rejected.

---

# 23. Phase 19 — Dockerization

## Goal

Make the project easy to run and evaluate.

### Services

The initial Docker setup should support:

```text
frontend
backend
postgres
```

### Tasks

* Create backend Dockerfile.
* Create frontend Dockerfile.
* Create Docker Compose configuration.
* Configure PostgreSQL.
* Configure persistent PostgreSQL volume.
* Configure service networking.
* Externalize configuration.
* Document startup commands.
* Verify clean startup.

### Expected command

The exact command will depend on the implementation, but the final project should provide a simple documented command such as:

```bash
docker compose up --build
```

---

# 24. Phase 20 — AI Review and Human Review

This phase is mandatory for the assessment.

The AI development process must include:

```text
Implementation
     ↓
AI Review
     ↓
Human Review
     ↓
Fix
     ↓
Tests
```

### AI review should inspect:

* Architecture compliance
* Specification compliance
* API correctness
* Validation
* State-machine correctness
* Error handling
* Test quality
* Security/configuration issues
* Code duplication
* Maintainability

### Human review must verify:

* AI recommendations are actually correct.
* Recommendations do not contradict specifications.
* Unnecessary complexity has not been introduced.
* Tests test meaningful behavior.
* No requirement has been silently changed.

---

# 25. AI Mistake Requirement

At least one meaningful AI mistake or incorrect suggestion must be identified during development.

The mistake must be genuine.

Examples of potential categories:

* Incorrect state transition
* Incorrect API behavior
* Incorrect validation
* Incorrect database query
* Incorrect Spring configuration
* Weak/incorrect test
* Incorrect Docker configuration
* Unnecessary architectural complexity

Do not manufacture a fake mistake merely for documentation.

When an actual issue is discovered, record:

```text
Date
Task
AI suggestion/implementation
Why it was incorrect
Human reasoning
Correction
Test proving the correction
```

Example structure:

```text
AI Review Finding #1

Problem:
AI implementation allowed OPEN → CLOSED.

Expected:
OPEN → IN_PROGRESS → RESOLVED → CLOSED.

Human decision:
Reject the implementation because it violates spec/state-machine.md.

Fix:
Changed transition validation.

Verification:
Added integration test proving OPEN → CLOSED returns 409.
```

---

# 26. Phase 21 — Documentation and Submission

## Goal

Prepare the repository for architectural review.

The final repository should contain:

```text
README.md

spec/
├── requirements.md
├── architecture.md
├── data-model.md
├── api-contract.md
├── state-machine.md
├── ui-flow.md
├── test-strategy.md
└── implementation-plan.md
```

Also include:

```text
rules/
commands/
skills/
docs/
.specstory/
```

where applicable according to the chosen Cursor workflow.

---

# 27. README Requirements

The README should explain:

* Project purpose
* Architecture
* Technology stack
* Requirements
* Local setup
* PostgreSQL setup
* Docker setup
* How to run backend
* How to run frontend
* How to run tests
* API overview
* State machine
* AI-assisted development workflow
* Human review approach
* Known AI findings/corrections
* Repository structure

The README must allow an architect to understand and run the project without reading the entire source code first.

---

# 28. Git Commit Strategy

Commits should represent meaningful development steps.

Avoid one final commit containing the entire application.

Recommended progression:

```text
docs: define requirements
docs: define architecture
docs: define PostgreSQL data model
docs: define REST API contract
docs: define ticket state machine
docs: define frontend UI flow
docs: define test strategy
docs: define implementation plan
```

Then implementation commits should remain focused.

Examples:

```text
feat: bootstrap spring boot backend
feat: add PostgreSQL persistence
feat: implement ticket creation
test: add ticket creation integration tests
feat: implement ticket retrieval
feat: implement ticket updates
feat: implement ticket status transitions
test: add state machine integration tests
feat: implement ticket comments
feat: implement ticket search and filtering
feat: add frontend ticket list
feat: add ticket creation form
feat: add ticket details flow
```

The exact commit sequence may change during development.

---

# 29. Task Execution Rule

Each implementation task should follow:

```text
1. Read relevant specification
2. Define small task
3. Ask AI for implementation
4. Review generated changes
5. Run tests
6. Review test quality
7. Fix issues
8. Commit
```

Do not give AI a broad request such as:

```text
Build the complete Support Ticket Management System.
```

Instead use focused tasks such as:

```text
Implement the TicketStatus enum and the state-transition component according to spec/state-machine.md. Do not modify controllers, repositories, or frontend code.
```

---

# 30. Scope Control

During implementation, do not introduce functionality merely because the AI suggests it.

Examples of scope that should not be added without specification review:

* Authentication
* JWT
* User management
* Kafka
* Redis
* Elasticsearch
* Microservices
* Event sourcing
* CQRS
* Kubernetes
* Notification systems
* Complex caching
* Advanced observability platforms

The assignment does not require these capabilities.

Additional technology must have a clear engineering reason and must not unnecessarily increase complexity.

---

# 31. Definition of Implementation Completion

Implementation is complete when:

* [x] Backend starts successfully.
* [x] PostgreSQL starts successfully.
* [x] Frontend starts successfully.
* [x] Tickets can be created.
* [x] Tickets can be listed.
* [x] Ticket details can be viewed.
* [x] Ticket fields can be updated.
* [x] Assignee can be changed.
* [x] Comments can be added.
* [x] Search works.
* [x] Status filtering works.
* [x] Valid state transitions work.
* [x] Invalid state transitions are rejected.
* [x] Backend validation works.
* [x] UI displays meaningful errors.
* [x] Data persists.
* [x] Tests pass.
* [x] Docker setup works.
* [x] No secrets are committed.
* [x] AI review has been performed.
* [x] Human review has been performed.
* [x] At least one genuine AI issue has been identified and corrected if encountered.
* [x] README is complete.

---

# 32. Implementation Readiness Checklist

Before starting production implementation:

* [ ] Requirements approved.
* [ ] Architecture approved.
* [ ] Data model approved.
* [ ] API contract approved.
* [ ] State machine approved.
* [ ] UI flow approved.
* [ ] Test strategy approved.
* [ ] Implementation plan approved.
* [ ] Cursor rules available.
* [ ] Testing rules available.
* [ ] API standards available.
* [ ] Review commands available.
* [ ] Prompt history enabled.
* [ ] Git repository clean.
* [ ] Initial specification commits pushed.

---

# 33. Specification Status

**Status:** Ready for implementation.

The project can now move from specification into controlled implementation.

The first implementation task should be **backend project bootstrap**, not complete application generation.
