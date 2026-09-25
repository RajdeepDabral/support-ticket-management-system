# Task Ticket State Machine

## Objective

Implement the backend state-machine rules for Support Tickets.

The backend must enforce the approved ticket lifecycle and reject every invalid status transition.

The frontend must not be treated as the source of truth for transition rules.

## References

Read before implementation:

* `spec/requirements.md`
* `spec/architecture.md`
* `spec/data-model.md`
* `spec/state-machine.md`
* `spec/api-contract.md`
* `spec/test-strategy.md`
* `spec/tasks/12-ticket-domain-service.md`
* `rules/java-springboot.md`
* `rules/testing.md`
* `rules/api-standards.md`

## Valid States

The ticket status values are:

* OPEN
* IN_PROGRESS
* RESOLVED
* CLOSED
* CANCELLED

## Valid Transitions

Only the following transitions are allowed:

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED

IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED

RESOLVED → CLOSED
```

## Invalid Transitions

Every transition not explicitly listed above must be rejected.

Examples:

```text
CLOSED → OPEN
CLOSED → IN_PROGRESS
CLOSED → RESOLVED
CLOSED → CANCELLED

RESOLVED → OPEN
RESOLVED → IN_PROGRESS
RESOLVED → CANCELLED

IN_PROGRESS → OPEN
IN_PROGRESS → CLOSED

OPEN → RESOLVED
OPEN → CLOSED

CANCELLED → OPEN
CANCELLED → IN_PROGRESS
CANCELLED → RESOLVED
CANCELLED → CLOSED
```

## Business Rules

Transition validation must happen on the backend.

The frontend must not be relied upon to prevent invalid transitions.

A status transition operation must:

1. Load the ticket.
2. Verify that the requested target status is valid.
3. Reject the operation if the transition is invalid.
4. Update the ticket status if valid.
5. Persist the change atomically.

## Error Handling

An invalid transition must result in a controlled business exception.

The exception must contain enough information to identify:

* current status
* requested status

Do not expose raw database or framework exceptions.

The final HTTP representation will be implemented later with the REST API error-handling work.

## State Machine Design

Keep the implementation simple and readable.

Do not introduce a complex workflow engine or external state-machine framework.

The allowed transition rules should have a single authoritative source.

Avoid duplicating the transition matrix across:

* controllers
* services
* frontend
* tests

Tests may of course express the expected transitions.

## Integration With Ticket Service

The existing normal ticket update operation must not be used to bypass the state machine.

Status changes must go through the dedicated transition operation.

## Testing

Implement comprehensive state-machine tests.

At minimum verify every valid transition:

```text
OPEN → IN_PROGRESS
OPEN → CANCELLED
IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED
RESOLVED → CLOSED
```

Also verify representative invalid transitions.

Prefer parameterized tests where they improve readability and reduce duplicated test code.

Verify that:

* valid transitions persist the new status
* invalid transitions leave the original status unchanged
* missing ticket produces the existing not-found behavior
* transition operation is atomic

## Out of Scope

Do not implement:

* REST controller
* frontend status controls
* comments
* search
* filtering
* authentication
* authorization
* notifications
* Kafka
* Redis
* Elasticsearch
* workflow engines

## Completion Criteria

The task is complete when:

* State-machine rules have one authoritative implementation.
* All valid transitions work.
* Invalid transitions are rejected.
* Invalid transitions do not modify the ticket.
* Missing tickets are handled correctly.
* Tests cover valid and invalid transitions.
* Existing tests continue to pass.
* No REST endpoint is added yet.
* AI review is completed.
* Human review is completed.
