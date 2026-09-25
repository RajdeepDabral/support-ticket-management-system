# REST API Standards

## Purpose

These rules define REST API implementation standards for the Support Ticket Management System.

The approved API contract in `spec/api-contract.md` is authoritative.

---

## Base Path

All APIs use:

`/api`

Ticket APIs use:

`/api/tickets`

---

## Ticket APIs

### Create

`POST /api/tickets`

The client provides:

* title
* description
* priority
* assignee

The backend:

* generates the ID
* sets status to OPEN
* manages timestamps

The client must not choose the initial status.

---

### List

`GET /api/tickets`

Optional query parameters:

* keyword
* status

Keyword searches:

* title
* description

Search must be case-insensitive.

Keyword and status filtering may be combined.

---

### Detail

`GET /api/tickets/{ticketId}`

Return ticket information and associated comments.

Unknown ticket IDs return 404.

---

### Update

`PATCH /api/tickets/{ticketId}`

Allowed fields:

* title
* description
* priority
* assignee

Status must not be changed through the general ticket update endpoint.

---

### Status Transition

`PATCH /api/tickets/{ticketId}/status`

Status changes must pass through the state-machine rules.

Invalid transitions return 409.

Invalid status values return 400.

---

### Comments

`POST /api/tickets/{ticketId}/comments`

Required fields:

* content
* author

Unknown ticket IDs return 404.

---

## HTTP Status Codes

Use the documented status codes:

* 200 — successful retrieval/update
* 201 — successful creation
* 400 — invalid request/validation
* 404 — resource not found
* 409 — business conflict/state transition conflict
* 500 — unexpected server error

Do not use HTTP status codes inconsistently.

---

## Error Response

Errors should follow:

```json
{
  "timestamp": "...",
  "status": 400,
  "code": "...",
  "message": "...",
  "path": "...",
  "fieldErrors": []
}
```

`fieldErrors` is optional where not applicable.

Never expose stack traces or internal implementation details.

---

## Request/Response DTOs

Use DTOs for API requests and responses.

Do not expose JPA entities directly.

Do not allow clients to modify backend-owned fields.

---

## Validation

Backend validation is authoritative.

Frontend validation may improve user experience but must not replace backend validation.

---

## API Consistency

Before implementing or changing an endpoint, check:

1. `spec/requirements.md`
2. `spec/api-contract.md`
3. `spec/data-model.md`
4. `spec/state-machine.md`
5. relevant test strategy

If the requested behavior conflicts with the specification, identify the conflict before implementing it.

---

## API Scope

Do not add:

* authentication
* user management
* DELETE ticket
* pagination
* API versioning
* bulk APIs
* WebSockets

unless the specification is explicitly updated.
