# Step Ticket Status Transition API

## Objective

Expose the ticket state-machine functionality through a REST API.

Endpoint:

`PATCH /api/v1/tickets/{ticketId}/status`

The REST layer must delegate status changes to the existing application/service/state-machine implementation.

The controller must not implement transition rules itself.

## Request

Example:

```json
{
  "status": "IN_PROGRESS"
}
```

The status must be one of:

* OPEN
* IN_PROGRESS
* RESOLVED
* CLOSED
* CANCELLED

## Valid Transitions

The backend state machine allows only:

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED

IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED

RESOLVED → CLOSED
```

`CLOSED` and `CANCELLED` are terminal states.

## Invalid Transitions

All transitions not explicitly listed above must be rejected.

Examples:

```text
OPEN → RESOLVED
OPEN → CLOSED
IN_PROGRESS → OPEN
IN_PROGRESS → CLOSED
RESOLVED → OPEN
RESOLVED → IN_PROGRESS
CLOSED → OPEN
CLOSED → IN_PROGRESS
CANCELLED → OPEN
```

An attempt to transition to the current status is also invalid.

## HTTP Behavior

### Successful Transition

Return:

```text
HTTP 200 OK
```

with the updated `TicketResponse`.

Example:

```json
{
  "id": 1,
  "title": "Login issue",
  "description": "User cannot login",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignee": "rajdeep",
  "createdAt": "2026-09-25T10:00:00Z",
  "updatedAt": "2026-09-25T10:30:00Z"
}
```

### Invalid Status Value

Example:

```json
{
  "status": "INVALID"
}
```

Return:

```text
HTTP 400 Bad Request
```

### Invalid State Transition

Return:

```text
HTTP 409 Conflict
```

The ticket status must remain unchanged.

### Ticket Not Found

Return:

```text
HTTP 404 Not Found
```

using the existing centralized error response.

## Business Logic

The REST controller must:

1. Receive the ticket ID.
2. Validate the request.
3. Convert the requested status to the domain enum if required.
4. Delegate to the existing ticket service/state-machine logic.
5. Return the updated TicketResponse.

The controller must not contain code such as:

```java
if (currentStatus == OPEN && newStatus == IN_PROGRESS) {
    ...
}
```

Transition rules belong to the state-machine/domain/application layer.

## Transaction Behavior

The transition must execute within the existing service transaction boundary.

For an invalid transition:

* no status update must be persisted
* `updatedAt` must not be incorrectly changed
* the API must return HTTP 409

## Tests

Add tests covering:

### Valid transitions

1. OPEN → IN_PROGRESS
2. OPEN → CANCELLED
3. IN_PROGRESS → RESOLVED
4. IN_PROGRESS → CANCELLED
5. RESOLVED → CLOSED

### Invalid transitions

Test representative invalid transitions including:

1. OPEN → RESOLVED
2. OPEN → CLOSED
3. IN_PROGRESS → OPEN
4. IN_PROGRESS → CLOSED
5. RESOLVED → OPEN
6. CLOSED → OPEN
7. CANCELLED → OPEN
8. same status → same status

### API validation

Test:

1. Invalid status value → 400
2. Missing ticket → 404
3. Invalid transition → 409
4. Successful transition → 200
5. Invalid transition does not modify persisted status
6. Successful transition updates `updatedAt`
7. `createdAt` remains unchanged

## Architecture Requirements

* Controller remains thin.
* Business rules remain outside the controller.
* Existing state-machine implementation must be reused.
* Do not duplicate transition rules.
* Do not expose JPA entities directly.
* Return TicketResponse.
* Reuse centralized exception handling.
* Do not introduce a second state-machine implementation.

## Out of Scope

Do not implement:

* ticket comments
* search
* filtering
* pagination
* frontend changes
* authentication
* authorization
* Elasticsearch
* Redis
* Kafka
* microservices
* database-level state transition triggers

## Definition of Done

* PATCH status endpoint implemented.
* Valid transitions work.
* Invalid transitions return 409.
* Invalid status values return 400.
* Missing tickets return 404.
* Invalid transitions do not modify persisted state.
* Existing Step 13 state-machine logic is reused.
* Controller contains no transition business rules.
* Integration/API tests pass.
* Existing tests remain green.
* No H2 database introduced.
* No unrelated functionality changed.
