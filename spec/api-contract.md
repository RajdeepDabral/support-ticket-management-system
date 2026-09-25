# Support Ticket Management System — API Contract

## 1. Purpose

This document defines the REST API contract for the Support Ticket Management System.

It specifies:

* API endpoints
* HTTP methods
* Request payloads
* Response payloads
* Validation rules
* Query parameters
* HTTP status codes
* Error response format
* State transition API behavior
* Comment API behavior

This contract acts as the boundary between the frontend and backend.

Implementation must follow this contract unless a specification change is explicitly reviewed and documented.

---

## 2. API Design Principles

The API follows these principles:

1. RESTful resource-oriented endpoints.
2. JSON request and response bodies.
3. Backend is authoritative for business rules.
4. Backend validation cannot be bypassed by frontend behavior.
5. Ticket status transitions are handled through a dedicated endpoint.
6. Normal ticket updates cannot directly modify status.
7. DTOs are used for API requests and responses.
8. Consistent error responses are returned for validation and business-rule failures.
9. No authentication or user-management API is included because it is outside the assignment scope.
10. API behavior should remain simple and appropriate for the assignment scope.

---

# 3. Base URL

The backend API uses:

```text
/api
```

Ticket resources are exposed under:

```text
/api/tickets
```

No API version prefix is required for this assignment.

### Implementation note (2026-09-25)

The implemented and tested API uses version prefix `/api/v1`:

```text
/api/v1/tickets
```

This is a deliberate, documented deviation. See `docs/decisions/api-versioning.md`. Request/response shapes, validation rules, and status codes in this contract apply unchanged; only the path prefix differs.

---

# 4. Ticket Resource

A ticket contains:

| Field       | Type               |                   Required | Description                   |
| ----------- | ------------------ | -------------------------: | ----------------------------- |
| id          | Long               |              Response only | Unique ticket identifier      |
| title       | String             |                        Yes | Ticket title                  |
| description | String             |                        Yes | Detailed ticket description   |
| priority    | String             |                        Yes | Ticket priority               |
| status      | String             | Response only / status API | Current ticket status         |
| assignee    | String             |                        Yes | Person assigned to the ticket |
| createdAt   | ISO-8601 timestamp |              Response only | Creation timestamp            |
| updatedAt   | ISO-8601 timestamp |              Response only | Last modification timestamp   |

Supported statuses:

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

The status transition rules are defined separately in:

```text
spec/state-machine.md
```

---

# 5. Priority Values

The API uses the following priority values:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Priority values are case-sensitive.

Example:

```json
{
  "priority": "HIGH"
}
```

An unsupported priority value must be rejected by backend validation.

---

# 6. Create Ticket

## Endpoint

```http
POST /api/tickets
```

## Purpose

Creates a new support ticket.

The initial status is always:

```text
OPEN
```

The client must not provide the initial status.

## Request

```json
{
  "title": "Unable to login",
  "description": "User receives an authentication error when attempting to login.",
  "priority": "HIGH",
  "assignee": "john.doe"
}
```

## Request Validation

### title

* Required
* Must not be blank
* Maximum length: 255 characters

### description

* Required
* Must not be blank

### priority

* Required
* Must be one of:

    * LOW
    * MEDIUM
    * HIGH
    * CRITICAL

### assignee

* Required
* Must not be blank
* Maximum length: 255 characters

### status

Status must not be accepted in the create request.

The backend determines the initial status as `OPEN`.

## Success Response

HTTP:

```text
201 Created
```

Example:

```json
{
  "id": 1001,
  "title": "Unable to login",
  "description": "User receives an authentication error when attempting to login.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "john.doe",
  "createdAt": "2026-09-25T08:00:00Z",
  "updatedAt": "2026-09-25T08:00:00Z"
}
```

---

# 7. List Tickets

## Endpoint

```http
GET /api/tickets
```

## Purpose

Returns tickets matching the supplied search/filter criteria.

## Query Parameters

### keyword

Optional.

Searches ticket:

* title
* description

Search must be case-insensitive.

Example:

```http
GET /api/tickets?keyword=login
```

### status

Optional.

Filters tickets by exact status.

Example:

```http
GET /api/tickets?status=IN_PROGRESS
```

### Combined Search and Filter

`keyword` and `status` may be used together.

Example:

```http
GET /api/tickets?keyword=payment&status=OPEN
```

The backend must apply both conditions.

## Success Response

HTTP:

```text
200 OK
```

Example:

```json
[
  {
    "id": 1001,
    "title": "Unable to login",
    "description": "User receives an authentication error when attempting to login.",
    "priority": "HIGH",
    "status": "OPEN",
    "assignee": "john.doe",
    "createdAt": "2026-09-25T08:00:00Z",
    "updatedAt": "2026-09-25T08:00:00Z"
  }
]
```

An empty search result returns:

```text
200 OK
```

with:

```json
[]
```

No `404 Not Found` is returned for an empty collection.

---

# 8. View Ticket Details

## Endpoint

```http
GET /api/tickets/{ticketId}
```

## Purpose

Returns a single ticket including its comments.

Example:

```http
GET /api/tickets/1001
```

## Success Response

HTTP:

```text
200 OK
```

Example:

```json
{
  "id": 1001,
  "title": "Unable to login",
  "description": "User receives an authentication error when attempting to login.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "john.doe",
  "createdAt": "2026-09-25T08:00:00Z",
  "updatedAt": "2026-09-25T08:15:00Z",
  "comments": [
    {
      "id": 501,
      "content": "Investigating the authentication issue.",
      "author": "jane.doe",
      "createdAt": "2026-09-25T08:15:00Z"
    }
  ]
}
```

## Not Found

If the ticket does not exist:

```text
404 Not Found
```

---

# 9. Update Ticket

## Endpoint

```http
PATCH /api/tickets/{ticketId}
```

## Purpose

Updates editable ticket information.

The following fields can be updated:

* title
* description
* priority
* assignee

The status cannot be changed using this endpoint.

Status changes must use the dedicated status endpoint.

## Request

The request is partial.

Example:

```json
{
  "title": "Unable to login after password reset",
  "priority": "CRITICAL"
}
```

Only supplied fields are changed.

Fields that are not supplied remain unchanged.

## Validation

If a supplied field is invalid, the entire request is rejected.

Examples:

* blank title
* blank description
* invalid priority
* blank assignee
* title longer than 255 characters

Explicit `null` values for editable fields are invalid.

## Status Field

A request such as:

```json
{
  "status": "RESOLVED"
}
```

must not be used to change the status.

Status changes are handled only through:

```http
PATCH /api/tickets/{ticketId}/status
```

## Success Response

HTTP:

```text
200 OK
```

Example:

```json
{
  "id": 1001,
  "title": "Unable to login after password reset",
  "description": "User receives an authentication error when attempting to login.",
  "priority": "CRITICAL",
  "status": "OPEN",
  "assignee": "john.doe",
  "createdAt": "2026-09-25T08:00:00Z",
  "updatedAt": "2026-09-25T08:30:00Z"
}
```

---

# 10. Update Ticket Status

## Endpoint

```http
PATCH /api/tickets/{ticketId}/status
```

## Purpose

Changes the status of an existing ticket.

The backend must enforce the state machine defined in:

```text
spec/state-machine.md
```

## Request

```json
{
  "status": "IN_PROGRESS"
}
```

## Valid Transition Example

Current:

```text
OPEN
```

Requested:

```text
IN_PROGRESS
```

The backend accepts the request.

## Invalid Transition Example

Current:

```text
CLOSED
```

Requested:

```text
OPEN
```

The backend rejects the request.

The frontend must not be relied upon to prevent invalid transitions.

## Success Response

HTTP:

```text
200 OK
```

Example:

```json
{
  "id": 1001,
  "title": "Unable to login",
  "description": "User receives an authentication error when attempting to login.",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignee": "john.doe",
  "createdAt": "2026-09-25T08:00:00Z",
  "updatedAt": "2026-09-25T08:45:00Z"
}
```

## Invalid Transition Response

HTTP:

```text
409 Conflict
```

Example:

```json
{
  "timestamp": "2026-09-25T08:45:00Z",
  "status": 409,
  "code": "INVALID_STATUS_TRANSITION",
  "message": "Ticket cannot transition from CLOSED to OPEN.",
  "path": "/api/tickets/1001/status"
}
```

---

# 11. Add Comment

## Endpoint

```http
POST /api/tickets/{ticketId}/comments
```

## Purpose

Adds a comment to an existing ticket.

## Request

```json
{
  "content": "The issue has been reproduced and is being investigated.",
  "author": "john.doe"
}
```

## Validation

### content

* Required
* Must not be blank

### author

* Required
* Must not be blank
* Maximum length: 255 characters

## Success Response

HTTP:

```text
201 Created
```

Example:

```json
{
  "id": 502,
  "content": "The issue has been reproduced and is being investigated.",
  "author": "john.doe",
  "createdAt": "2026-09-25T09:00:00Z"
}
```

## Ticket Not Found

If the ticket does not exist:

```text
404 Not Found
```

---

# 12. Error Response Contract

All API errors should follow a consistent structure.

Example:

```json
{
  "timestamp": "2026-09-25T09:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed.",
  "path": "/api/tickets",
  "fieldErrors": {
    "title": "Title must not be blank",
    "priority": "Priority must be one of LOW, MEDIUM, HIGH, CRITICAL"
  }
}
```

## Error Fields

| Field       | Description                            |
| ----------- | -------------------------------------- |
| timestamp   | Time at which the error occurred       |
| status      | HTTP status code                       |
| code        | Application-level error code           |
| message     | Human-readable error message           |
| path        | Requested API path                     |
| fieldErrors | Optional field-level validation errors |

`fieldErrors` is included when individual request fields can be identified.

---

# 13. HTTP Status Codes

| Situation                  | HTTP Status |
| -------------------------- | ----------: |
| Successful GET             |         200 |
| Successful PATCH           |         200 |
| Ticket created             |         201 |
| Comment created            |         201 |
| Invalid request validation |         400 |
| Invalid enum/value         |         400 |
| Ticket not found           |         404 |
| Invalid status transition  |         409 |
| Unexpected server error    |         500 |

The API should not expose internal stack traces to clients.

---

# 14. Content Type

Requests containing JSON must use:

```http
Content-Type: application/json
```

Successful JSON responses use:

```http
Content-Type: application/json
```

---

# 15. Endpoint Summary

| Method | Endpoint                           | Purpose                    |
| ------ | ---------------------------------- | -------------------------- |
| POST   | `/api/tickets`                     | Create ticket              |
| GET    | `/api/tickets`                     | List/search/filter tickets |
| GET    | `/api/tickets/{ticketId}`          | View ticket details        |
| PATCH  | `/api/tickets/{ticketId}`          | Update ticket fields       |
| PATCH  | `/api/tickets/{ticketId}/status`   | Change ticket status       |
| POST   | `/api/tickets/{ticketId}/comments` | Add comment                |

---

# 16. API Scope Deliberately Excluded

The following APIs are intentionally not included because they are outside the assignment requirements:

* Delete ticket
* User registration
* User management
* Authentication/login
* Role management
* Attachment upload
* Ticket history/audit API
* Pagination
* Notifications
* External integrations

These may be future enhancements but must not be added during the initial implementation unless the specification is explicitly changed.

---

# 17. Backend Authority

The backend is the final authority for:

* Required fields
* Field validation
* Priority validation
* Ticket existence
* Status transitions
* Persistence
* Business-rule enforcement

Frontend validation improves user experience but does not replace backend validation.

A client calling the REST API directly must receive the same business-rule enforcement as a user interacting through the UI.

---

# 18. API Contract Review Checklist

Before implementation begins, verify:

* [ ] All required ticket operations are represented.
* [ ] Create ticket starts with `OPEN`.
* [ ] Status cannot be changed through the normal update endpoint.
* [ ] Status has a dedicated endpoint.
* [ ] Backend enforces status transitions.
* [ ] Search covers title and description.
* [ ] Search is case-insensitive.
* [ ] Status filtering is supported.
* [ ] Keyword and status filters can be combined.
* [ ] Comments belong to a ticket.
* [ ] Backend validation is defined.
* [ ] Not-found behavior is defined.
* [ ] Invalid status transition behavior is defined.
* [ ] Error response structure is consistent.
* [ ] HTTP status codes are defined.
* [ ] No authentication/user-management scope has been introduced.
* [ ] No unnecessary pagination or delete functionality has been introduced.

---

# 19. Specification Status

**Status:** Ready for human review

**Next specification:** `spec/state-machine.md`

The API contract should be reviewed against:

```text
spec/requirements.md
spec/architecture.md
spec/data-model.md
```

before implementation begins.
