# Update Ticket API

## Objective

Implement the REST API for partially updating an existing support ticket.

Endpoint:

`PATCH /api/v1/tickets/{ticketId}`

The endpoint must update only the fields explicitly supplied by the client.

## Scope

### Allowed fields

The following ticket fields may be updated:

* title
* description
* priority
* assignee

### Fields that must not be updated

The following fields must not be accepted as updateable fields:

* id
* status
* createdAt
* updatedAt

Status changes are handled separately by the ticket state-machine API.

## Functional Requirements

### FR-001 — Partial Update

The request may contain any combination of:

* title
* description
* priority
* assignee

Fields that are omitted must retain their existing values.

Example:

```json
{
  "priority": "HIGH"
}
```

Only the priority should change.

### FR-002 — Null Values

If an explicitly supplied update field is `null`, the request must fail validation with HTTP 400.

### FR-003 — Ticket Not Found

If the supplied ticket ID does not exist, return HTTP 404 using the application's standard error response.

### FR-004 — Validation

Backend validation must reject invalid values.

Examples:

* blank title
* blank description
* invalid priority
* blank assignee

The backend is authoritative for validation.

### FR-005 — Status Protection

The update endpoint must not modify ticket status.

A client must use the dedicated status-transition API to change status.

### FR-006 — Response

On successful update, return the updated ticket using `TicketResponse`.

Do not expose the JPA entity directly.

### FR-007 — Timestamp

`updatedAt` must represent the update operation.

`createdAt` must remain unchanged.

## API Contract

### Request

```http
PATCH /api/v1/tickets/{ticketId}
Content-Type: application/json
```

Example:

```json
{
  "title": "Updated login issue",
  "description": "Updated description",
  "priority": "HIGH",
  "assignee": "john.doe"
}
```

Partial example:

```json
{
  "priority": "CRITICAL"
}
```

### Successful Response

HTTP 200 OK

```json
{
  "id": 1,
  "title": "Updated login issue",
  "description": "Updated description",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "john.doe",
  "createdAt": "2026-09-25T10:00:00Z",
  "updatedAt": "2026-09-25T10:30:00Z"
}
```

## Error Handling

Use the existing centralized error response.

Expected status codes:

* `200` — successful update
* `400` — validation error / invalid request
* `404` — ticket not found
* `500` — unexpected server error

## Service Layer

The controller must delegate the update operation to the application/service layer.

The service must:

1. Load the ticket.
2. Return not-found if it does not exist.
3. Apply only supplied fields.
4. Validate business rules.
5. Preserve status.
6. Update `updatedAt`.
7. Persist the ticket.
8. Return the updated ticket.

Controllers must not contain business logic.

## Tests

Add tests covering at least:

1. Update title.
2. Update description.
3. Update priority.
4. Update assignee.
5. Update multiple fields together.
6. Partial update preserves omitted fields.
7. Explicit null value returns 400.
8. Blank title returns 400.
9. Blank description returns 400.
10. Invalid priority returns 400.
11. Missing ticket returns 404.
12. Status remains unchanged after update.
13. `createdAt` remains unchanged.
14. `updatedAt` changes after update.
15. Successful API returns `TicketResponse`.
16. JPA entity is not exposed directly.

## Out of Scope

Do not implement:

* status transition endpoint
* comments
* search
* filtering
* pagination
* sorting
* frontend changes
* authentication
* authorization
* Elasticsearch
* Redis
* Kafka
* microservices

## Definition of Done

* Update request DTO implemented.
* PATCH endpoint implemented.
* Partial update behavior works.
* Backend validation works.
* Status cannot be changed through this API.
* 404 handling works.
* Centralized error handling is reused.
* Tests pass.
* No existing retrieval/create functionality is broken.
* No H2 database is introduced.
* No unnecessary dependencies are added.
* Code follows the existing project architecture and coding rules.
