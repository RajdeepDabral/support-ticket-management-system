# Backend Integration and Acceptance Test Hardening

## Objective

Validate the complete Support Ticket Management System backend through integration and acceptance-level tests.

The tests must exercise the real application flow:

HTTP → Controller → Service → Repository → PostgreSQL

## Database

Integration tests must use PostgreSQL.

Do not introduce H2.

Testcontainers PostgreSQL may be used if it is not already configured.

The tests must verify real persistence behavior rather than mocking the repository.

## Scope

Validate the complete backend functionality:

1. Create ticket
2. Retrieve ticket list
3. Retrieve ticket details
4. Update ticket
5. Status transitions
6. Comments
7. Keyword search
8. Status filtering
9. Combined search + status filtering
10. Validation
11. Error handling
12. Persistence

## Acceptance Scenario 1 — Create Ticket

Create a ticket through the HTTP API.

Verify:

* HTTP 201
* generated ID
* title persisted
* description persisted
* priority persisted
* assignee persisted
* initial status is OPEN
* createdAt generated
* updatedAt generated

The client must not control:

* ID
* status
* timestamps

## Acceptance Scenario 2 — Retrieve Ticket

Retrieve the created ticket.

Verify:

* HTTP 200
* correct ID
* correct fields
* correct status
* correct timestamps

## Acceptance Scenario 3 — Update Ticket

Update:

* title
* description
* priority
* assignee

Verify:

* HTTP 200
* updated values returned
* status remains unchanged
* createdAt remains unchanged
* updatedAt changes

Also verify partial update behavior.

## Acceptance Scenario 4 — State Machine

Verify all valid transitions:

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED

IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED

RESOLVED → CLOSED
```

Verify invalid transitions return HTTP 409.

Verify invalid transitions do not modify persisted state.

Verify CLOSED and CANCELLED are terminal.

## Acceptance Scenario 5 — Comments

Create a comment for an existing ticket.

Verify:

* HTTP 201
* comment persisted
* correct ticket relationship
* generated ID
* generated createdAt

Retrieve the ticket and verify the comment is present if required by the API contract.

## Acceptance Scenario 6 — Search

Create tickets with different titles and descriptions.

Verify keyword search:

* title matching
* description matching
* case-insensitive matching
* substring matching
* no-result behavior

No-result behavior:

```text
HTTP 200
[]
```

## Acceptance Scenario 7 — Status Filter

Verify filtering for:

* OPEN
* IN_PROGRESS
* RESOLVED
* CLOSED
* CANCELLED

Invalid status values must return HTTP 400.

## Acceptance Scenario 8 — Combined Search and Filter

Verify:

```text
keyword match
AND
status match
```

Tickets matching only the keyword but having the wrong status must not be returned.

## Acceptance Scenario 9 — Validation

Verify HTTP 400 for invalid requests including:

* blank title
* blank description
* invalid priority
* blank assignee
* invalid status
* blank comment content
* blank comment author

## Acceptance Scenario 10 — Not Found

Verify HTTP 404 for:

* unknown ticket ID during retrieval
* unknown ticket ID during update
* unknown ticket ID during status transition
* unknown ticket ID when adding a comment

## Acceptance Scenario 11 — Error Contract

Verify error responses follow the existing centralized error format.

The response should contain the fields defined by the API contract, such as:

* timestamp
* status
* code
* message
* path
* fieldErrors when applicable

## Acceptance Scenario 12 — Persistence

Verify that data survives the request lifecycle.

For example:

1. Create ticket.
2. Update ticket.
3. Add comment.
4. Retrieve ticket.
5. Verify all changes are persisted.

Where practical, verify persistence using a fresh transaction or application context rather than relying only on in-memory objects.

## Test Quality

Tests must validate observable behavior.

Avoid tests that merely verify:

```text
method X was called
```

without proving the API/database behavior.

Repository mocking should not replace integration tests.

## Test Isolation

Tests must not depend on execution order.

Each test should create the required data or use appropriate database cleanup/isolation.

## Existing Tests

All existing unit, service, state-machine, repository, and API tests must continue to pass.

## Out of Scope

Do not implement:

* frontend
* Docker production deployment changes
* authentication
* authorization
* pagination
* Elasticsearch
* Redis
* Kafka
* microservices
* performance/load testing

## Definition of Done

* PostgreSQL integration testing works.
* No H2 introduced.
* End-to-end backend flows are covered.
* State-machine API is covered.
* Search/filter behavior is covered.
* Comments are covered.
* Validation is covered.
* Error handling is covered.
* Persistence is covered.
* Existing tests remain green.
* Full test suite passes.
