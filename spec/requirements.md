# Support Ticket Management System

## Requirements Specification

**Document:** Requirements Specification
**Version:** 1.0
**Status:** Approved for Implementation
**Primary Database:** PostgreSQL
**Backend:** Java 21 + Spring Boot
**Frontend:** React / Next.js or equivalent
**API:** REST
**Development Approach:** Spec Driven Development with Cursor and GitHub Copilot

---

# 1. Purpose

The Support Ticket Management System is a web-based application for managing support tickets throughout their lifecycle.

The system will allow users to create, view, update, search, filter, and comment on support tickets.

A key business requirement is that ticket status transitions must be controlled by the backend according to the defined state machine. Invalid transitions must never be accepted, even if a client attempts to call the REST API directly.

The application will be developed using a Spec Driven Development approach:

```text
Requirement
    ↓
Specification
    ↓
Plan / Tasks
    ↓
Implementation
    ↓
Testing
    ↓
Review
    ↓
Fix
```

The application requirements in this document are derived from the assignment and are treated as the baseline requirements for implementation.

---

# 2. Scope

## 2.1 Functional Scope

The application shall support:

1. Creating support tickets.
2. Listing tickets.
3. Viewing ticket details.
4. Updating ticket information.
5. Updating ticket priority.
6. Updating ticket assignee.
7. Adding comments.
8. Searching tickets by keyword.
9. Filtering tickets by status.
10. Persisting application data in PostgreSQL.
11. Backend input validation.
12. Meaningful error handling in the UI.
13. Backend enforcement of the ticket state machine.

## 2.2 Technology Scope

The solution shall use:

* Java 21
* Spring Boot
* PostgreSQL
* REST APIs
* React / Next.js or equivalent frontend
* Cursor
* GitHub Copilot
* Docker / Docker Compose for reproducible local execution

PostgreSQL will be the application's database for both development and the runnable assessment environment.

---

# 3. Functional Requirements

## FR-001 — Create a Ticket

The system shall allow a user to create a support ticket.

A ticket shall contain:

* Title
* Description
* Priority
* Assignee
* Status
* Created timestamp
* Updated timestamp

When a ticket is created, its initial status shall be:

```text
OPEN
```

The caller shall not be allowed to create a ticket directly in another status.

### Acceptance Criteria

* A ticket can be created from the UI.
* The backend accepts a valid create request.
* A unique ticket identifier is generated.
* The newly created ticket starts in `OPEN`.
* Ticket data is persisted in PostgreSQL.
* Invalid input is rejected by the backend.
* The UI displays a meaningful success or error message.

---

# 4. FR-002 — List Tickets

The system shall allow users to view a list of support tickets.

The ticket list shall provide sufficient information to identify each ticket, including:

* Ticket ID
* Title
* Priority
* Status
* Assignee
* Creation timestamp

### Acceptance Criteria

* Existing tickets can be retrieved.
* Tickets stored in PostgreSQL are returned.
* Tickets remain available after application restart.
* The UI displays the retrieved tickets.

---

# 5. FR-003 — View Ticket Details

The system shall allow a user to view the details of an individual ticket.

The detail view shall contain:

* Ticket ID
* Title
* Description
* Priority
* Status
* Assignee
* Created timestamp
* Updated timestamp
* Comments

### Acceptance Criteria

* An existing ticket can be retrieved using its identifier.
* Ticket details are displayed in the UI.
* Associated comments are displayed.
* A request for a non-existent ticket returns a meaningful error.

---

# 6. FR-004 — Update Ticket

The system shall allow the following ticket information to be updated:

* Title
* Description
* Priority
* Assignee

Ticket status transitions shall be handled separately according to the defined state machine.

An ordinary ticket update operation shall not be allowed to bypass the state-machine rules.

### Acceptance Criteria

* Title can be updated.
* Description can be updated.
* Priority can be updated.
* Assignee can be updated.
* Updated information is persisted in PostgreSQL.
* Invalid input is rejected by the backend.
* Status cannot be changed through an update mechanism that bypasses state-transition validation.

---

# 7. FR-005 — Update Assignee

The system shall allow the assignee of a ticket to be changed.

The assignment requirement does not require a separate user-management system.

For the scope of this assignment, the assignee can be represented using an appropriate application-level value.

### Acceptance Criteria

* A ticket can have an assignee.
* The assignee can be changed.
* The new assignee is persisted.
* The updated assignee is displayed when viewing the ticket.

---

# 8. FR-006 — Add Comments

The system shall allow users to add comments to an existing ticket.

A comment shall contain:

* Comment ID
* Ticket ID
* Comment content
* Author
* Created timestamp

### Acceptance Criteria

* A comment can be added to an existing ticket.
* Empty comments are rejected.
* Comments are persisted in PostgreSQL.
* Comments are displayed when viewing ticket details.
* Adding a comment does not change the ticket status.

---

# 9. FR-007 — Search Tickets by Keyword

The system shall allow users to search tickets using a keyword.

For this implementation, keyword search shall be performed against:

* Ticket title
* Ticket description

The search shall be case-insensitive.

Comments are not required to participate in ticket keyword search.

### Acceptance Criteria

* A user can search tickets using a keyword.
* Matching title values are returned.
* Matching description values are returned.
* Search is case-insensitive.
* Search is performed by the backend.
* The UI displays the matching tickets.

---

# 10. FR-008 — Filter Tickets by Status

The system shall allow users to filter tickets by status.

The supported statuses are:

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

### Acceptance Criteria

* Users can select a status filter.
* The backend returns tickets matching the selected status.
* Filtering does not modify ticket data.
* The UI displays the filtered results.
* Without a status filter, tickets across statuses can be retrieved.

---

# 11. FR-009 — Ticket State Machine

The backend shall enforce the following state machine:

```text
OPEN
  |
  +----> IN_PROGRESS
  |          |
  |          +----> RESOLVED
  |          |          |
  |          |          +----> CLOSED
  |          |
  |          +----> CANCELLED
  |
  +----> CANCELLED
```

## Allowed Transitions

| Current Status | Next Status |
| -------------- | ----------- |
| OPEN           | IN_PROGRESS |
| IN_PROGRESS    | RESOLVED    |
| RESOLVED       | CLOSED      |
| OPEN           | CANCELLED   |
| IN_PROGRESS    | CANCELLED   |

Only the transitions explicitly defined above are valid.

## Invalid Transitions

The following are examples of transitions that must be rejected:

| Current Status | Requested Status | Expected Result |
| -------------- | ---------------- | --------------- |
| CLOSED         | OPEN             | Rejected        |
| RESOLVED       | OPEN             | Rejected        |
| CANCELLED      | OPEN             | Rejected        |
| OPEN           | CLOSED           | Rejected        |
| OPEN           | RESOLVED         | Rejected        |
| CLOSED         | CANCELLED        | Rejected        |
| CANCELLED      | IN_PROGRESS      | Rejected        |

### Backend Enforcement

The state machine shall be enforced by the backend.

Frontend controls may prevent users from selecting invalid transitions, but the backend must independently validate every status-change request.

This ensures that invalid transitions cannot be introduced by directly calling the REST API.

### Acceptance Criteria

* Valid transitions succeed.
* Invalid transitions are rejected.
* The existing ticket status remains unchanged when an invalid transition is attempted.
* The API returns a meaningful error for an invalid transition.
* State-machine integration tests cover valid transitions.
* State-machine integration tests cover invalid transitions.

---

# 12. FR-010 — Backend Input Validation

The backend shall validate incoming API requests.

Validation shall include, where applicable:

* Required title validation.
* Required description validation.
* Valid priority validation.
* Required comment content validation.
* Ticket existence validation.
* Valid status validation.
* Valid state-transition validation.

Backend validation is authoritative.

Frontend validation shall improve user experience but shall not replace backend validation.

---

# 13. FR-011 — Meaningful Error Handling

The backend shall provide meaningful errors for invalid requests and business-rule violations.

The system shall handle scenarios including:

* Invalid input.
* Missing ticket.
* Invalid status.
* Invalid state transition.
* Invalid request data.

The frontend shall present meaningful messages to the user rather than exposing raw server exceptions or stack traces.

The detailed API error contract will be defined separately in:

```text
spec/api-contract.md
```

---

# 14. FR-012 — Data Persistence

The application shall persist ticket and comment data in PostgreSQL.

PostgreSQL is the required database for the application.

The application shall not depend on in-memory storage for ticket or comment persistence.

### Acceptance Criteria

* Created tickets are stored in PostgreSQL.
* Updated tickets are stored in PostgreSQL.
* Comments are stored in PostgreSQL.
* Data remains available after application restart.
* Application restart does not cause existing ticket data to be lost.

---

# 15. FR-013 — REST API

The backend shall expose REST APIs for the required ticket and comment operations.

The API shall provide functionality for:

* Creating tickets.
* Listing tickets.
* Viewing ticket details.
* Updating ticket fields.
* Updating assignee.
* Updating ticket status.
* Adding comments.
* Searching tickets.
* Filtering tickets.

The detailed API contract, request models, response models, and HTTP status codes will be defined separately in:

```text
spec/api-contract.md
```

---

# 16. FR-014 — Frontend

The frontend shall provide a usable interface for the required ticket-management functionality.

The UI shall allow users to:

* Create tickets.
* View ticket lists.
* View ticket details.
* Update ticket information.
* Change assignee.
* Perform valid status transitions.
* Add comments.
* Search tickets.
* Filter tickets by status.

The UI shall display meaningful validation and backend error messages.

---

# 17. Non-Functional Requirements

## NFR-001 — Technology

The solution shall use:

### Backend

* Java 21
* Spring Boot

### Database

* PostgreSQL

### Frontend

* React / Next.js or equivalent

### API

* REST

### AI-Assisted Development

* Cursor
* GitHub Copilot

### Containerization

* Docker
* Docker Compose

---

# 18. NFR-002 — Maintainability

The backend should maintain a clear separation of responsibilities.

The implementation should separate concerns such as:

* REST/API layer
* Service/business layer
* Persistence layer
* Domain/entity model
* DTOs
* Validation
* Exception handling
* State-transition logic

Business logic should not be unnecessarily placed inside controllers.

---

# 19. NFR-003 — Testability

The solution shall contain automated tests for important application behavior.

Testing shall cover:

* Ticket creation.
* Ticket retrieval.
* Ticket updates.
* Comments.
* Search.
* Status filtering.
* Backend validation.
* Valid state transitions.
* Invalid state transitions.
* Persistence/API behavior where appropriate.

The state machine must have integration-level test coverage.

---

# 20. NFR-004 — Containerization

The application shall be containerized to make the solution easy to run and review.

The repository shall provide Docker configuration that allows a reviewer to start the required application components with documented commands.

The expected runtime environment will include:

```text
Frontend
Backend
PostgreSQL
```

Docker Compose will be used to simplify startup of the application and database.

---

# 21. NFR-005 — Security and Secrets

No passwords, API keys, tokens, database credentials, or other secrets shall be committed to the public GitHub repository.

Environment-specific configuration shall be supplied through environment variables or appropriate external configuration.

Example configuration may be provided without containing real secrets.

---

# 22. NFR-006 — Error Safety

The application shall not expose internal implementation details or stack traces to end users through the API or UI.

Errors should provide enough information for the user to understand the problem while avoiding unnecessary internal details.

---

# 23. NFR-007 — Developer and Reviewer Experience

The project should be straightforward for another engineer to review and run.

A reviewer should be able to:

1. Clone the public GitHub repository.
2. Understand the requirements.
3. Review the specifications.
4. Understand the architecture.
5. Start the application using Docker.
6. Run the automated tests.
7. Review the Git history.
8. Review the AI prompt history.
9. Review AI-generated issues.
10. Review human engineering decisions.

---

# 24. AI-Assisted Development Requirement

The application shall be developed using the following workflow:

```text
Requirement
     ↓
Specification
     ↓
Plan / Tasks
     ↓
Implementation
     ↓
Testing
     ↓
Review
     ↓
Fix
```

AI tools shall be used as engineering assistants rather than as an unrestricted replacement for engineering decisions.

Reusable project instructions shall be maintained through the selected IDE's supported rule/steering mechanism.

The repository shall contain the required reusable AI guidance and prompt history.

---

# 25. Human Engineering Review

AI-generated suggestions and implementation shall be reviewed by the engineer before being considered complete.

Particular attention shall be given to:

* Business-rule correctness.
* State-machine enforcement.
* API contract correctness.
* Validation.
* Database behavior.
* Test coverage.
* Error handling.
* Maintainability.
* Security.
* Unnecessary complexity.

At least one meaningful incorrect AI suggestion or implementation issue shall be identified, documented, and corrected.

The purpose of this review is to demonstrate that AI-generated output was validated rather than blindly accepted.

The findings will be documented separately in:

```text
docs/ai-review.md
docs/human-review.md
```

---

# 26. Requirement Clarifications and Human Decisions

The following points clarify how the provided assignment requirements will be interpreted during implementation.

These clarifications do not remove or weaken any assignment requirement.

## 26.1 PostgreSQL

PostgreSQL will be used as the application's database.

Reason:

The assignment requires persistent data, and PostgreSQL provides a consistent relational database environment for development, testing, and reviewer execution.

---

## 26.2 Status Changes

Status will not be treated as an unrestricted field update.

Reason:

The assignment explicitly requires the backend state machine to reject invalid transitions.

Therefore, all status changes must pass through the backend transition validation.

---

## 26.3 Frontend vs Backend Validation

The frontend may perform validation for better user experience.

However, the backend remains authoritative.

Reason:

REST APIs can be called independently of the frontend, so business rules must be enforced server-side.

---

## 26.4 Search

Keyword search will cover ticket title and description.

Reason:

This provides a clear interpretation of "search tickets by keyword" while keeping the initial implementation focused on ticket data.

---

## 26.5 Authentication

Authentication and authorization are not part of the stated assignment requirements.

They will therefore not be introduced unless required by a later approved specification change.

Reason:

Introducing unrelated functionality would increase implementation complexity without contributing to the stated acceptance criteria.

---

## 26.6 Assignee

The assignment requires an assignee but does not require user management.

Therefore, the initial design will represent an assignee without introducing a complete authentication/user-management subsystem.

The exact representation will be finalized in:

```text
spec/data-model.md
```

---

# 27. Acceptance Criteria

The solution is considered complete when all of the following are satisfied:

* [ ] Ticket can be created from UI.
* [ ] Tickets can be listed.
* [ ] Ticket details can be viewed.
* [ ] Ticket fields can be updated.
* [ ] Assignee can be changed.
* [ ] Comments can be added.
* [ ] Search works.
* [ ] Status filter works.
* [ ] Valid status transitions work.
* [ ] Invalid status transitions are rejected by backend.
* [ ] Data survives application restart.
* [ ] Backend validation works.
* [ ] UI shows meaningful errors.
* [ ] State-machine integration tests pass.
* [ ] No secrets are committed.

In addition, the engineering submission will provide:

* [ ] Specification artefacts.
* [ ] Reusable AI steering instructions.
* [ ] Prompt history.
* [ ] Implementation plan/tasks.
* [ ] Automated tests.
* [ ] Docker configuration.
* [ ] AI review.
* [ ] Human engineering review.
* [ ] Git history demonstrating the development process.

---

# 28. Requirement Traceability

The following table will be progressively updated during implementation.

| Requirement                   | Specification                                   | Implementation | Test Evidence |
| ----------------------------- | ----------------------------------------------- | -------------- | ------------- |
| FR-001 Create ticket          | `spec/data-model.md`, `spec/api-contract.md`    | TBD            | TBD           |
| FR-002 List tickets           | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-003 Ticket details         | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-004 Update ticket          | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-005 Assignee               | `spec/data-model.md`                            | TBD            | TBD           |
| FR-006 Comments               | `spec/data-model.md`, `spec/api-contract.md`    | TBD            | TBD           |
| FR-007 Search                 | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-008 Status filter          | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-009 State machine          | `spec/state-machine.md`                         | TBD            | TBD           |
| FR-010 Validation             | `spec/api-contract.md`, `spec/test-strategy.md` | TBD            | TBD           |
| FR-011 Error handling         | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-012 PostgreSQL persistence | `spec/data-model.md`, `spec/architecture.md`    | TBD            | TBD           |
| FR-013 REST API               | `spec/api-contract.md`                          | TBD            | TBD           |
| FR-014 Frontend               | `spec/ui-flow.md`                               | TBD            | TBD           |

---

# 29. Definition of Done

The project will be considered complete when:

1. All assignment acceptance criteria are satisfied.
2. Backend business rules are enforced.
3. The ticket state machine is fully tested.
4. Data is persisted in PostgreSQL.
5. Frontend and backend functionality are integrated.
6. Automated tests pass.
7. Docker-based execution works.
8. No secrets are committed.
9. AI-generated implementation has been reviewed.
10. At least one meaningful AI issue has been identified and corrected.
11. Human engineering decisions are documented.
12. Prompt history is available.
13. Git history clearly demonstrates the Spec Driven Development workflow.
14. The README provides sufficient instructions for an independent reviewer to run and evaluate the application.

---

# 30. Specification Change Policy

Once implementation begins, the approved requirements should not be silently changed by an AI assistant.

If an ambiguity, conflict, or technical concern is discovered:

1. Identify the issue.
2. Document the issue.
3. Evaluate the impact.
4. Make a human engineering decision.
5. Update the relevant specification if required.
6. Update affected implementation tasks.
7. Update tests.
8. Record the decision in the appropriate documentation.

This keeps the specification, implementation, tests, and engineering decisions aligned throughout the project lifecycle.

---

# 31. Approval

This requirements document represents the baseline requirements for the Support Ticket Management System.

Implementation should begin only after the requirements and subsequent technical specifications have been reviewed.

**Status:** Ready for Architecture Specification
