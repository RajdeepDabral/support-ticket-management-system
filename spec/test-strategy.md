# Support Ticket Management System — Test Strategy

## 1. Purpose

This document defines the testing strategy for the Support Ticket Management System.

Testing will verify:

* Functional requirements
* Backend validation
* REST API behavior
* Ticket lifecycle/state transitions
* Persistence
* Search and filtering
* Comments
* Frontend behavior
* Error handling
* Integration between application layers

The test strategy is defined before implementation so that the implementation can be evaluated against explicit expected behavior.

---

# 2. Testing Principles

The project follows these principles:

1. Business-critical behavior must have automated tests.
2. Tests should verify observable behavior rather than implementation details.
3. Backend business rules must be tested independently of the frontend.
4. State-machine rules require dedicated tests.
5. REST API behavior must be tested through integration tests.
6. Persistence behavior must be tested against PostgreSQL.
7. Invalid inputs must be tested, not only happy paths.
8. Tests should remain deterministic and repeatable.
9. Tests should be readable enough to act as executable documentation.
10. AI-generated tests must be reviewed by a human engineer.

---

# 3. Test Pyramid

The project will use multiple testing levels.

```text
                    ┌─────────────────────┐
                    │   UI / Frontend     │
                    │      Tests           │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ REST / Integration  │
                    │       Tests         │
                    └──────────┬──────────┘
                               │
                 ┌─────────────▼─────────────┐
                 │     Service / Domain      │
                 │         Tests              │
                 └─────────────┬─────────────┘
                               │
                 ┌─────────────▼─────────────┐
                 │       Unit Tests           │
                 └─────────────────────────────┘
```

Most business logic should be covered by fast unit tests.

Critical workflows should additionally be covered by integration/API tests.

---

# 4. Testing Technology

## Backend

Recommended backend testing stack:

* JUnit 5
* Mockito
* Spring Boot Test
* Spring MockMvc or equivalent HTTP testing mechanism
* Spring Data JPA
* PostgreSQL

The exact testing annotations and test structure will be decided during implementation.

---

# 5. Database Testing Policy

The assignment explicitly uses PostgreSQL.

Therefore, persistence/integration tests should validate behavior against PostgreSQL rather than introducing H2 as a substitute database.

The test environment must remain compatible with the application's PostgreSQL behavior.

Possible approaches may include:

* dedicated PostgreSQL test database
* containerized PostgreSQL
* Testcontainers PostgreSQL

The final implementation choice will be made during the planning/implementation phase.

H2 must not be introduced merely for convenience.

---

# 6. Test Categories

The project will contain the following major test categories:

```text
1. Unit Tests
2. Service/Domain Tests
3. State Machine Tests
4. Repository/Persistence Tests
5. REST API Integration Tests
6. Validation Tests
7. Search and Filter Tests
8. Comment Tests
9. Frontend Tests
10. End-to-End/Acceptance Verification
```

Not every category requires a large number of tests.

Tests should provide meaningful coverage of business-critical behavior.

---

# 7. Unit Tests

Unit tests should verify isolated business behavior without requiring the full application stack.

Examples include:

* State transition validation
* Priority validation logic where applicable
* Service-level business rules
* Mapping logic where meaningful
* Error handling decisions

Unit tests should be:

* Fast
* Deterministic
* Independent
* Easy to understand

---

# 8. State Machine Tests

The state machine is one of the most important business rules in the application.

Every valid transition must be tested.

## Valid transitions

| Current     | Requested   | Expected |
| ----------- | ----------- | -------- |
| OPEN        | IN_PROGRESS | Allowed  |
| OPEN        | CANCELLED   | Allowed  |
| IN_PROGRESS | RESOLVED    | Allowed  |
| IN_PROGRESS | CANCELLED   | Allowed  |
| RESOLVED    | CLOSED      | Allowed  |

---

## Invalid transitions

At minimum:

| Current     | Requested   | Expected |
| ----------- | ----------- | -------- |
| OPEN        | RESOLVED    | Rejected |
| OPEN        | CLOSED      | Rejected |
| IN_PROGRESS | OPEN        | Rejected |
| IN_PROGRESS | CLOSED      | Rejected |
| RESOLVED    | OPEN        | Rejected |
| RESOLVED    | IN_PROGRESS | Rejected |
| RESOLVED    | CANCELLED   | Rejected |
| CLOSED      | OPEN        | Rejected |
| CLOSED      | IN_PROGRESS | Rejected |
| CLOSED      | RESOLVED    | Rejected |
| CANCELLED   | OPEN        | Rejected |
| CANCELLED   | IN_PROGRESS | Rejected |
| CANCELLED   | RESOLVED    | Rejected |
| CANCELLED   | CLOSED      | Rejected |

---

## Same-state transitions

These must also be rejected:

```text
OPEN → OPEN
IN_PROGRESS → IN_PROGRESS
RESOLVED → RESOLVED
CLOSED → CLOSED
CANCELLED → CANCELLED
```

---

# 9. State Machine Integration Tests

Unit tests alone are insufficient.

At least one integration test must verify that state-machine behavior is enforced through the actual API.

Example:

```text
Create ticket
      ↓
OPEN
      ↓
PATCH status → IN_PROGRESS
      ↓
Verify response
      ↓
Verify database
```

Invalid transition:

```text
Create ticket
      ↓
OPEN
      ↓
PATCH status → CLOSED
      ↓
Expect 409
      ↓
Verify ticket remains OPEN
```

This proves that the business rule is enforced at the application boundary.

---

# 10. Ticket Creation Tests

The following scenarios should be covered.

## Successful creation

Given valid:

* title
* description
* priority
* assignee

When the create API is called:

Expected:

```text
201 Created
```

And:

```text
status = OPEN
```

The ticket must be persisted.

---

## Missing title

Expected:

```text
400 Bad Request
```

The ticket must not be created.

---

## Blank title

Expected:

```text
400 Bad Request
```

---

## Title exceeding maximum length

Expected:

```text
400 Bad Request
```

---

## Missing description

Expected:

```text
400 Bad Request
```

---

## Blank description

Expected:

```text
400 Bad Request
```

---

## Missing priority

Expected:

```text
400 Bad Request
```

---

## Invalid priority

Example:

```json
{
  "priority": "UNKNOWN"
}
```

Expected:

```text
400 Bad Request
```

---

## Missing assignee

Expected:

```text
400 Bad Request
```

---

## Blank assignee

Expected:

```text
400 Bad Request
```

---

## Client-provided status

If a client attempts to provide a status during creation, the backend must not allow the client to override the initial state.

The resulting ticket must still start as:

```text
OPEN
```

The exact handling of an unknown/extra status field should follow the API implementation's configured request-binding behavior, but it must never override the server-controlled initial state.

---

# 11. Ticket Retrieval Tests

## List tickets

Verify:

```text
GET /api/tickets
```

returns:

```text
200 OK
```

and persisted tickets.

---

## Empty list

When no tickets exist:

Expected:

```text
200 OK
[]
```

---

## Get existing ticket

Expected:

```text
200 OK
```

and correct ticket information.

---

## Get non-existing ticket

Expected:

```text
404 Not Found
```

---

# 12. Search Tests

Search must cover both:

* title
* description

## Title search

Given a ticket:

```text
Title = "Login failure"
```

Search:

```text
keyword=login
```

Expected:

The ticket is returned.

---

## Description search

Given:

```text
Description = "Payment service is unavailable"
```

Search:

```text
keyword=payment
```

Expected:

The ticket is returned.

---

## Case-insensitive search

Given:

```text
Title = "Payment Failure"
```

Search:

```text
keyword=payment
```

and:

```text
keyword=PAYMENT
```

Both should find the ticket.

---

## No search result

Expected:

```text
200 OK
[]
```

---

# 13. Status Filter Tests

Test each supported status:

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

The API must return tickets matching the requested status.

Tickets with other statuses must not be returned.

---

# 14. Combined Search and Status Filter

Verify that:

```text
keyword
+
status
```

are applied together.

Example:

```http
GET /api/tickets?keyword=payment&status=OPEN
```

Expected:

Only tickets matching both criteria are returned.

---

# 15. Ticket Update Tests

The update API must support:

* title
* description
* priority
* assignee

## Update title

Expected:

```text
200 OK
```

and persisted title change.

## Update description

Expected:

```text
200 OK
```

and persisted description change.

## Update priority

Expected:

```text
200 OK
```

and persisted priority change.

## Update assignee

Expected:

```text
200 OK
```

and persisted assignee change.

---

# 16. Partial Update Tests

Because the API uses `PATCH`, partial updates must be tested.

Example:

```json
{
  "priority": "CRITICAL"
}
```

Expected:

* Priority changes.
* Title remains unchanged.
* Description remains unchanged.
* Assignee remains unchanged.
* Status remains unchanged.

---

# 17. Status Update Isolation

A normal ticket update must not modify status.

Example:

```http
PATCH /api/tickets/1001
```

with:

```json
{
  "title": "Updated title"
}
```

Expected:

The ticket title changes but its status remains unchanged.

Status modifications must use:

```http
PATCH /api/tickets/1001/status
```

---

# 18. Comment Tests

## Add comment

Given an existing ticket:

```http
POST /api/tickets/1001/comments
```

Expected:

```text
201 Created
```

The comment must be persisted and associated with the correct ticket.

---

## Comment content validation

Missing or blank content:

```text
400 Bad Request
```

---

## Comment author validation

Missing or blank author:

```text
400 Bad Request
```

---

## Comment for non-existing ticket

Expected:

```text
404 Not Found
```

The comment must not be persisted.

---

# 19. Ticket Details and Comments

When retrieving:

```http
GET /api/tickets/{ticketId}
```

the response should include the ticket's comments.

Tests should verify:

* Ticket information is correct.
* Comments belong to the requested ticket.
* Comments are not incorrectly returned for another ticket.

---

# 20. Persistence Tests

Persistence tests should verify:

### Create

Ticket is persisted.

### Update

Changes survive retrieval.

### Status transition

Updated status survives retrieval.

### Comment

Comment survives retrieval.

### Restart/persistence behavior

The Dockerized PostgreSQL setup should preserve persisted data when the application container is restarted.

The exact restart verification can be performed as part of acceptance testing.

---

# 21. Repository Tests

Repository-level tests should verify meaningful persistence behavior.

Examples:

* Save ticket
* Find ticket by ID
* Find tickets by status
* Search title
* Search description
* Search case-insensitively
* Combined keyword/status filtering
* Save and retrieve comments

Repository tests should not duplicate every service test.

They should focus on database/query behavior.

---

# 22. REST API Integration Tests

The integration suite should cover the API boundary.

At minimum:

```text
POST /api/tickets
GET /api/tickets
GET /api/tickets/{id}
PATCH /api/tickets/{id}
PATCH /api/tickets/{id}/status
POST /api/tickets/{id}/comments
```

Each should have both successful and important failure scenarios.

---

# 23. Error Contract Tests

Verify that API errors follow the documented structure.

For example:

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "...",
  "path": "...",
  "fieldErrors": {}
}
```

Tests should verify that internal stack traces are not returned to clients.

---

# 24. HTTP Status Code Tests

The integration suite should verify:

| Scenario                 | Expected |
| ------------------------ | -------: |
| Create ticket            |      201 |
| List tickets             |      200 |
| Get ticket               |      200 |
| Update ticket            |      200 |
| Valid status transition  |      200 |
| Add comment              |      201 |
| Validation failure       |      400 |
| Invalid status           |      400 |
| Ticket not found         |      404 |
| Invalid state transition |      409 |
| Unexpected server error  |      500 |

---

# 25. Transaction Tests

Business operations that modify data should not leave partially updated data.

For example, an invalid status transition:

```text
OPEN → CLOSED
```

must not result in:

```text
status = CLOSED
```

in the database.

The ticket must remain:

```text
OPEN
```

Tests should verify this behavior.

---

# 26. Timestamp Tests

Tests should verify:

### Creation

`createdAt` and `updatedAt` are populated.

### Update

`updatedAt` changes after a successful update.

### Status transition

`updatedAt` changes after a successful status transition.

### Comment

The comment receives a valid `createdAt`.

The tests should avoid relying on exact timestamp equality because timestamps may differ by small amounts.

---

# 27. Frontend Testing

Frontend tests should focus on user-visible behavior.

Important scenarios:

### Ticket list

* Tickets are displayed.
* Search can be performed.
* Status filter can be selected.
* Empty state is displayed.

### Create ticket

* Required fields are validated.
* Valid ticket can be submitted.
* Submission state is displayed.
* Backend errors are displayed meaningfully.

### Ticket details

* Ticket information is displayed.
* Valid status actions are displayed.
* Invalid status actions are not presented as available UI actions.
* Comments are displayed.
* Comment can be added.

### Error handling

* 404 displays ticket-not-found message.
* 409 displays status-transition error.
* 500 displays generic retryable error.

---

# 28. End-to-End Acceptance Scenarios

The following workflows should be manually or automatically verified.

## Scenario 1 — Create Ticket

```text
Open application
     ↓
Create Ticket
     ↓
Enter valid details
     ↓
Submit
     ↓
Ticket created
     ↓
Status = OPEN
```

---

## Scenario 2 — Search Ticket

```text
Create ticket containing "payment"
     ↓
Open ticket list
     ↓
Search "payment"
     ↓
Ticket appears
```

---

## Scenario 3 — Filter Ticket

```text
Create tickets with different statuses
     ↓
Select OPEN
     ↓
Only OPEN tickets appear
```

---

## Scenario 4 — Update Ticket

```text
Open ticket
     ↓
Edit title/description/priority/assignee
     ↓
Save
     ↓
Reload
     ↓
Changes remain persisted
```

---

## Scenario 5 — Add Comment

```text
Open ticket
     ↓
Enter comment
     ↓
Submit
     ↓
Comment appears
     ↓
Reload
     ↓
Comment remains
```

---

## Scenario 6 — Valid Lifecycle

```text
OPEN
 ↓
IN_PROGRESS
 ↓
RESOLVED
 ↓
CLOSED
```

Every transition succeeds.

---

## Scenario 7 — Cancellation

```text
OPEN
 ↓
CANCELLED
```

Transition succeeds.

---

## Scenario 8 — Invalid Transition

```text
OPEN
 ↓
CLOSED
```

Expected:

```text
409 Conflict
```

Status remains:

```text
OPEN
```

---

## Scenario 9 — Terminal State

```text
CLOSED
 ↓
OPEN
```

Expected:

```text
409 Conflict
```

Status remains:

```text
CLOSED
```

---

# 29. Test Naming

Tests should use descriptive names that explain behavior.

Prefer:

```text
shouldCreateTicketWithOpenStatus()
```

over:

```text
testCreate()
```

Prefer:

```text
shouldRejectTransitionFromOpenToClosed()
```

over:

```text
testStatus()
```

Test names should make failures understandable without opening the implementation.

---

# 30. Test Data

Tests should use predictable test data.

Example:

```text
Title:
Unable to login

Description:
User receives an authentication error.

Priority:
HIGH

Assignee:
john.doe
```

Test data should not contain:

* Real customer information
* Production credentials
* API keys
* Passwords
* Personal secrets

---

# 31. Test Isolation

Tests must not depend on execution order.

Each test should establish the state it requires.

Tests should clean up or isolate test data appropriately.

One test should not depend on a ticket created by another unrelated test.

---

# 32. No Production Dependencies

Automated tests must not depend on:

* Production databases
* Production credentials
* External customer systems
* Personal accounts
* Undocumented external services

The test environment must be reproducible locally.

---

# 33. Test Coverage Expectations

The goal is meaningful coverage rather than a specific arbitrary percentage.

High-priority areas requiring strong coverage:

```text
State machine
Validation
Ticket creation
Ticket update
Status transitions
Search/filter
Comments
REST error handling
Persistence
```

A high coverage percentage with weak assertions is not considered sufficient.

Tests should verify actual behavior.

---

# 34. Mutation/Quality Mindset

When reviewing tests, ask:

> If I intentionally broke this business rule, would the test fail?

Examples:

If:

```text
OPEN → CLOSED
```

were accidentally allowed, the invalid-transition integration test should fail.

If search stopped checking descriptions, the description-search test should fail.

If ticket creation stopped defaulting status to `OPEN`, the creation test should fail.

This is more important than simply increasing test count.

---

# 35. AI-Generated Test Review

AI may generate tests during implementation.

Every AI-generated test must be reviewed by a human engineer.

Review questions:

* Does the test actually test the requirement?
* Is the assertion meaningful?
* Could the test pass even if the implementation is broken?
* Is the test testing implementation details unnecessarily?
* Does the test cover an actual acceptance criterion?
* Does it accidentally encode an incorrect AI assumption?
* Does it depend on test execution order?
* Does it use realistic test data?

AI-generated tests must not automatically be considered correct.

---

# 36. Required Human Review Example

During development, if AI proposes a test such as:

```text
OPEN → CLOSED should succeed
```

the engineer must compare it against:

```text
spec/state-machine.md
```

and reject/correct the test because the documented state machine requires:

```text
OPEN → IN_PROGRESS → RESOLVED → CLOSED
```

This is an example of meaningful human validation of AI output.

---

# 37. Definition of Test Completion

Testing is considered sufficient when:

* [ ] All critical business rules have automated tests.
* [ ] Valid state transitions pass.
* [ ] Invalid state transitions fail.
* [ ] State-machine API integration tests pass.
* [ ] Ticket CRUD-like required operations are covered.
* [ ] Search works.
* [ ] Status filtering works.
* [ ] Combined search/filter works.
* [ ] Comments work.
* [ ] Backend validation is covered.
* [ ] Error responses are covered.
* [ ] Persistence behavior is covered.
* [ ] Frontend critical flows are verified.
* [ ] No secrets are present in tests.
* [ ] Tests are repeatable.
* [ ] Tests pass from a clean environment.

---

# 38. Requirement-to-Test Traceability

| Requirement                  | Test Coverage                     |
| ---------------------------- | --------------------------------- |
| Create ticket                | API + service + UI                |
| List tickets                 | API + repository + UI             |
| View details                 | API + integration + UI            |
| Update fields                | API + service + UI                |
| Change assignee              | API + persistence + UI            |
| Add comments                 | API + persistence + UI            |
| Search keyword               | Repository + API + UI             |
| Filter status                | Repository + API + UI             |
| Persist data                 | Integration + acceptance          |
| Backend validation           | API integration tests             |
| Meaningful UI errors         | Frontend tests                    |
| Valid state transitions      | State-machine + integration tests |
| Invalid transitions rejected | State-machine + integration tests |
| Data survives restart        | Docker/acceptance verification    |

---

# 39. Human Review Checklist

Before implementation begins:

* [ ] Test strategy covers every functional requirement.
* [ ] State-machine tests cover all valid transitions.
* [ ] State-machine tests cover important invalid transitions.
* [ ] Same-state transitions are covered.
* [ ] API validation is covered.
* [ ] Search is tested against title and description.
* [ ] Case-insensitive search is tested.
* [ ] Status filtering is tested.
* [ ] Combined filtering is tested.
* [ ] Comments are tested.
* [ ] Persistence is tested.
* [ ] PostgreSQL remains the database used for integration testing.
* [ ] H2 has not been introduced.
* [ ] Frontend error handling is tested.
* [ ] AI-generated tests will receive human review.
* [ ] Tests focus on behavior rather than arbitrary coverage numbers.

---

# 40. Specification Status

**Status:** Ready for human review

The test strategy should be reviewed against:

```text
spec/requirements.md
spec/architecture.md
spec/data-model.md
spec/api-contract.md
spec/state-machine.md
spec/ui-flow.md
```

After approval, the project has completed the core specification phase.

The next phase is planning the implementation tasks before writing production code.
