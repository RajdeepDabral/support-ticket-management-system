# Support Ticket Management System — State Machine Specification

## 1. Purpose

This document defines the lifecycle and allowed status transitions for support tickets.

The state machine is a backend business rule and must be enforced independently of the frontend.

The frontend may restrict the status options shown to a user, but the backend must reject any invalid transition even when the API is called directly.

---

# 2. Supported Ticket States

A ticket can have one of the following states:

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

These values are represented as a Java enum in the application.

The database stores the corresponding string values.

---

# 3. Initial State

Every newly created ticket starts in:

```text
OPEN
```

The client must not choose the initial status during ticket creation.

For example:

```http
POST /api/tickets
```

with:

```json
{
  "title": "Unable to login",
  "description": "User cannot login to the application.",
  "priority": "HIGH",
  "assignee": "john.doe"
}
```

creates a ticket whose status is automatically:

```text
OPEN
```

---

# 4. Allowed State Transitions

The supported transitions are:

```text
OPEN
 ├──→ IN_PROGRESS
 └──→ CANCELLED

IN_PROGRESS
 ├──→ RESOLVED
 └──→ CANCELLED

RESOLVED
 └──→ CLOSED

CLOSED
 └──→ no further transition

CANCELLED
 └──→ no further transition
```

Equivalent transition table:

| Current State | Allowed Next State |
| ------------- | ------------------ |
| OPEN          | IN_PROGRESS        |
| OPEN          | CANCELLED          |
| IN_PROGRESS   | RESOLVED           |
| IN_PROGRESS   | CANCELLED          |
| RESOLVED      | CLOSED             |
| CLOSED        | None               |
| CANCELLED     | None               |

---

# 5. Transition Rules

## 5.1 OPEN → IN_PROGRESS

Allowed.

Meaning:

The ticket has been accepted for active investigation or work.

---

## 5.2 OPEN → CANCELLED

Allowed.

Meaning:

The ticket is cancelled before work is completed.

---

## 5.3 IN_PROGRESS → RESOLVED

Allowed.

Meaning:

The reported issue has been addressed and is considered resolved.

---

## 5.4 IN_PROGRESS → CANCELLED

Allowed.

Meaning:

Work on the ticket is cancelled before resolution.

---

## 5.5 RESOLVED → CLOSED

Allowed.

Meaning:

The resolved ticket is formally closed.

---

# 6. Terminal States

The following states are terminal:

```text
CLOSED
CANCELLED
```

Once a ticket reaches either state, no further status transition is permitted.

Therefore:

```text
CLOSED → OPEN
```

is invalid.

And:

```text
CANCELLED → OPEN
```

is invalid.

---

# 7. Invalid Transitions

The backend must reject every transition that is not explicitly listed as allowed.

Examples include:

```text
OPEN → RESOLVED
OPEN → CLOSED

IN_PROGRESS → OPEN
IN_PROGRESS → CLOSED

RESOLVED → OPEN
RESOLVED → IN_PROGRESS
RESOLVED → CANCELLED

CLOSED → OPEN
CLOSED → IN_PROGRESS
CLOSED → RESOLVED
CLOSED → CANCELLED

CANCELLED → OPEN
CANCELLED → IN_PROGRESS
CANCELLED → RESOLVED
CANCELLED → CLOSED
```

The implementation should follow an allow-list approach rather than assuming that an unlisted transition is valid.

---

# 8. Same-State Transitions

A request attempting to transition a ticket to its current state is not a valid state transition.

Examples:

```text
OPEN → OPEN
IN_PROGRESS → IN_PROGRESS
RESOLVED → RESOLVED
CLOSED → CLOSED
CANCELLED → CANCELLED
```

These requests must be rejected.

The backend should return:

```text
409 Conflict
```

with an application error code such as:

```text
INVALID_STATUS_TRANSITION
```

Example:

```json
{
  "timestamp": "2026-09-25T10:00:00Z",
  "status": 409,
  "code": "INVALID_STATUS_TRANSITION",
  "message": "Ticket cannot transition from OPEN to OPEN.",
  "path": "/api/tickets/1001/status"
}
```

---

# 9. State Transition API

Status changes are performed through:

```http
PATCH /api/tickets/{ticketId}/status
```

Request:

```json
{
  "status": "IN_PROGRESS"
}
```

The backend performs the following conceptual process:

```text
Receive requested status
        ↓
Load ticket
        ↓
Check ticket exists
        ↓
Read current status
        ↓
Validate requested status
        ↓
Check state machine
        ↓
Allowed?
   ↙           ↘
 YES            NO
  ↓              ↓
Update        Reject
status        request
  ↓              ↓
Persist        409 Conflict
  ↓
Return updated ticket
```

---

# 10. State Machine Must Be Backend-Enforced

The frontend must not be treated as the source of truth.

For example, the UI may display only:

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED
```

when a ticket is currently `OPEN`.

However, a client could still manually call:

```http
PATCH /api/tickets/1001/status
```

with:

```json
{
  "status": "CLOSED"
}
```

The backend must reject the request because:

```text
OPEN → CLOSED
```

is not an allowed transition.

This protects the business rule regardless of the client.

---

# 11. State Machine Ownership

The state machine belongs to the application/domain business logic.

It must not depend on:

* React UI behavior
* Controller-specific conditions
* Database triggers
* Frontend dropdown restrictions

The backend should have one authoritative implementation of transition rules.

A dedicated component/class is preferred so that transition behavior is easy to:

* understand
* unit test
* review
* modify
* reuse from service operations

---

# 12. Recommended Implementation Model

The implementation should represent statuses using a Java enum:

```java
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}
```

Transition validation should be explicit.

Conceptually:

```text
OPEN:
    IN_PROGRESS
    CANCELLED

IN_PROGRESS:
    RESOLVED
    CANCELLED

RESOLVED:
    CLOSED

CLOSED:
    none

CANCELLED:
    none
```

The exact implementation technique is intentionally left to the implementation phase.

Possible implementation approaches can be evaluated during planning, but the behavior defined by this specification must remain unchanged.

---

# 13. State Transition Result

When a valid transition occurs:

1. The existing ticket is loaded.
2. The current state is read.
3. The requested state is validated.
4. The transition is accepted.
5. The ticket status is updated.
6. `updatedAt` is updated.
7. The updated ticket is persisted.
8. The updated ticket is returned.

Example:

```text
Before:

Ticket #1001
Status: OPEN

Request:

OPEN → IN_PROGRESS

After:

Ticket #1001
Status: IN_PROGRESS
```

---

# 14. Transaction Boundary

A status transition must execute within the service/application transaction boundary.

The status update and related timestamp update should be persisted as one logical operation.

If persistence fails, the transition must not be partially applied.

---

# 15. Ticket Not Found

If the requested ticket does not exist:

```http
PATCH /api/tickets/{ticketId}/status
```

must return:

```text
404 Not Found
```

The state machine must not be evaluated for a ticket that does not exist.

Example:

```json
{
  "timestamp": "2026-09-25T10:00:00Z",
  "status": 404,
  "code": "TICKET_NOT_FOUND",
  "message": "Ticket with id 9999 was not found.",
  "path": "/api/tickets/9999/status"
}
```

---

# 16. Invalid Status Value

If the client sends an unsupported status:

```json
{
  "status": "PENDING"
}
```

the backend must reject the request.

HTTP status:

```text
400 Bad Request
```

Example:

```json
{
  "timestamp": "2026-09-25T10:00:00Z",
  "status": 400,
  "code": "INVALID_STATUS",
  "message": "Unsupported ticket status: PENDING.",
  "path": "/api/tickets/1001/status"
}
```

This is different from an invalid transition.

### Invalid status

The requested status itself does not exist.

```text
OPEN → PENDING
```

Result:

```text
400 Bad Request
```

### Invalid transition

The requested status exists, but the transition is not allowed.

```text
OPEN → CLOSED
```

Result:

```text
409 Conflict
```

---

# 17. State Machine Test Matrix

The state machine must be covered by automated tests.

## Valid transitions

|  # | From        | To          | Expected |
| -: | ----------- | ----------- | -------- |
|  1 | OPEN        | IN_PROGRESS | Allowed  |
|  2 | OPEN        | CANCELLED   | Allowed  |
|  3 | IN_PROGRESS | RESOLVED    | Allowed  |
|  4 | IN_PROGRESS | CANCELLED   | Allowed  |
|  5 | RESOLVED    | CLOSED      | Allowed  |

## Invalid transitions

At minimum, the following must be tested:

|  # | From        | To          | Expected |
| -: | ----------- | ----------- | -------- |
|  1 | OPEN        | RESOLVED    | Rejected |
|  2 | OPEN        | CLOSED      | Rejected |
|  3 | IN_PROGRESS | OPEN        | Rejected |
|  4 | IN_PROGRESS | CLOSED      | Rejected |
|  5 | RESOLVED    | OPEN        | Rejected |
|  6 | RESOLVED    | IN_PROGRESS | Rejected |
|  7 | CLOSED      | OPEN        | Rejected |
|  8 | CLOSED      | RESOLVED    | Rejected |
|  9 | CANCELLED   | OPEN        | Rejected |
| 10 | CANCELLED   | CLOSED      | Rejected |

## Same-state transitions

The following must also be tested:

| Current     | Requested   | Expected |
| ----------- | ----------- | -------- |
| OPEN        | OPEN        | Rejected |
| IN_PROGRESS | IN_PROGRESS | Rejected |
| RESOLVED    | RESOLVED    | Rejected |
| CLOSED      | CLOSED      | Rejected |
| CANCELLED   | CANCELLED   | Rejected |

---

# 18. Integration Test Expectations

At least one integration test must verify that the state machine works through the actual REST API and persistence layer.

Example scenario:

```text
Create ticket
    ↓
Status = OPEN
    ↓
PATCH /status → IN_PROGRESS
    ↓
Verify status = IN_PROGRESS
    ↓
PATCH /status → RESOLVED
    ↓
Verify status = RESOLVED
    ↓
PATCH /status → CLOSED
    ↓
Verify status = CLOSED
```

An invalid transition must also be tested through the API.

Example:

```text
Create ticket
    ↓
OPEN
    ↓
PATCH /status → CLOSED
    ↓
Expect 409 Conflict
    ↓
Verify status remains OPEN
```

This test is important because it proves that the rule is enforced by the backend rather than only by a unit-tested helper.

---

# 19. State Machine Invariants

The following invariants must always hold:

### Invariant 1

A newly created ticket must always start in:

```text
OPEN
```

### Invariant 2

A ticket can only transition to a state explicitly allowed by this specification.

### Invariant 3

`CLOSED` is terminal.

### Invariant 4

`CANCELLED` is terminal.

### Invariant 5

Frontend behavior must not determine whether a transition is valid.

### Invariant 6

An invalid transition must not modify the persisted ticket status.

### Invariant 7

A successful transition updates the ticket's `updatedAt` timestamp.

---

# 20. Human Review Considerations

Before implementation, the engineer reviewing this specification should verify:

* [ ] The initial state is `OPEN`.
* [ ] All five statuses are defined.
* [ ] All five valid transitions are present.
* [ ] `CLOSED` is terminal.
* [ ] `CANCELLED` is terminal.
* [ ] Same-state transitions are rejected.
* [ ] Invalid transitions return `409 Conflict`.
* [ ] Invalid status values return `400 Bad Request`.
* [ ] Missing tickets return `404 Not Found`.
* [ ] Backend is authoritative.
* [ ] State transitions are transactional.
* [ ] Invalid transitions do not modify persisted state.
* [ ] REST integration tests cover valid transitions.
* [ ] REST integration tests cover invalid transitions.

---

# 21. Specification Status

**Status:** Ready for human review

**Next specification:** `spec/ui-flow.md`

This specification must be considered together with:

```text
spec/requirements.md
spec/architecture.md
spec/data-model.md
spec/api-contract.md
```

No implementation should begin until the state-machine behavior is understood and reviewed.
