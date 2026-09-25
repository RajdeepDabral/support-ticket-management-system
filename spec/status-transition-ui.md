# Status Transition UI + State-Machine Integration

## Objective

Implement status transition controls on the ticket details page while preserving the backend state-machine rules.

The frontend should make valid transitions easy to perform, but the backend must remain the authoritative source for whether a transition is allowed.

---

# State Machine

The supported state machine is:

```text
OPEN
→ IN_PROGRESS
→ RESOLVED
→ CLOSED
```

Additional allowed transitions:

```text
OPEN
→ CANCELLED

IN_PROGRESS
→ CANCELLED
```

No other transitions are allowed.

Examples of invalid transitions:

```text
CLOSED
→ OPEN

RESOLVED
→ OPEN

CANCELLED
→ OPEN

CLOSED
→ RESOLVED

OPEN
→ RESOLVED

OPEN
→ CLOSED
```

The backend must reject invalid transitions.

---

# Important Architecture Rule

The frontend must NOT be treated as the source of truth for transition validity.

The frontend may display the currently expected valid actions.

However, the backend must validate every requested transition.

The UI must correctly handle a backend rejection even if the frontend believes a transition should be allowed.

---

# Existing Backend Contract

Before implementation, inspect:

* spec/api-contract.md
* spec/state-machine.md
* existing backend state-machine implementation
* existing controller/service/API

Use the existing approved transition endpoint if available.

Do not invent a second state-transition mechanism.

---

# Transition API

Determine the exact existing endpoint and HTTP method from the backend implementation and approved API contract.

Do not assume an endpoint such as:

```text
PATCH /tickets/{id}/status
```

unless that is actually the approved contract.

The frontend must use the existing API.

---

# Current Status

The ticket details page must continue displaying the current status.

Example:

```text
Status: OPEN
```

---

# Available Actions

The frontend may display actions appropriate to the current state.

For example:

## OPEN

```text
[ Start Progress ]
[ Cancel Ticket ]
```

## IN_PROGRESS

```text
[ Resolve ]
[ Cancel Ticket ]
```

## RESOLVED

```text
[ Close ]
```

## CLOSED

No transition actions.

## CANCELLED

No transition actions.

The exact labels may follow the existing UI conventions.

---

# Backend Authority

Even if the frontend displays:

```text
[ Resolve ]
```

the backend must still validate:

```text
IN_PROGRESS → RESOLVED
```

before changing the ticket.

The frontend must not assume that displaying an action guarantees success.

---

# Transition Request

Use the exact request body required by the backend contract.

Do not add unnecessary fields.

Do not send a full ticket object unless the existing API explicitly requires it.

---

# Transition Success

After successful transition:

* Update the displayed ticket status.
* Update available transition actions.
* Remain on the ticket details page.
* Display meaningful success feedback where consistent with the UI.

Do not reload the entire browser unnecessarily.

---

# Transition Failure

If the backend rejects a transition:

* Keep the existing status unchanged.
* Display a meaningful error.
* Do not optimistically leave the ticket in the new state.
* Allow the user to retry where appropriate.

Example:

```text
Unable to change ticket status.
The requested transition is not allowed.
```

Do not expose raw exception messages.

---

# Important Race Condition Case

The UI may become stale.

Example:

```text
Browser A:
IN_PROGRESS

Browser B:
IN_PROGRESS
    ↓
resolves ticket
    ↓
RESOLVED
```

Then Browser A attempts:

```text
IN_PROGRESS → RESOLVED
```

The backend may reject or otherwise handle the request depending on its concurrency behavior.

The frontend must handle the backend response rather than assuming the transition succeeds.

---

# Loading State

When a transition request is being processed:

* Disable the relevant transition controls.
* Prevent duplicate requests.
* Display a transition-in-progress state.

Example:

```text
Updating status...
```

---

# Confirmation

If the existing UI conventions support confirmation for destructive transitions, confirmation may be used for:

```text
Cancel Ticket
```

Do not add unnecessary confirmation dialogs for normal transitions unless there is an existing design convention.

---

# Status Display

Status should remain visually distinct from normal editable fields.

The normal Edit Ticket form from Step 24 must continue to exclude status.

Status changes must happen through the dedicated transition mechanism.

---

# Error Handling

Handle at minimum:

### HTTP 400

Display the backend validation/state-machine error.

### HTTP 404

Display that the ticket no longer exists.

### HTTP 409

If the backend uses conflict semantics for invalid/stale transitions, display a meaningful conflict message.

### HTTP 500

Display:

```text
Unable to change ticket status.
Please try again.
```

### Network Failure

Display a meaningful network error.

Do not expose raw stack traces.

---

# Testing

Add behavior-focused tests for:

1. Current status is displayed.
2. OPEN shows allowed actions.
3. IN_PROGRESS shows allowed actions.
4. RESOLVED shows Close action.
5. CLOSED has no transition actions.
6. CANCELLED has no transition actions.
7. Valid transition request is sent.
8. Successful transition updates the UI.
9. Duplicate transition requests are prevented.
10. Transition controls are disabled during request.
11. Backend rejection is handled.
12. Backend rejection does not incorrectly change the displayed status.
13. 404 is handled.
14. 409 is handled if supported.
15. 500 is handled.
16. Network failure is handled.
17. Existing ticket editing still works.
18. Existing comments still work.

---

# Backend State-Machine Tests

The backend must continue to have integration tests covering:

```text
OPEN → IN_PROGRESS        allowed
IN_PROGRESS → RESOLVED    allowed
RESOLVED → CLOSED         allowed
OPEN → CANCELLED          allowed
IN_PROGRESS → CANCELLED   allowed
```

and invalid transitions:

```text
CLOSED → OPEN
RESOLVED → OPEN
CANCELLED → OPEN
OPEN → RESOLVED
OPEN → CLOSED
CLOSED → RESOLVED
```

The exact test implementation must follow the existing backend architecture.

---

# Critical Requirement

The backend must remain the authoritative state-machine enforcement point.

Do not weaken backend validation simply because the frontend currently hides invalid actions.

The following must remain true:

```text
Direct API request with invalid transition
                ↓
Backend rejects it
```

This is required even if the UI never presents the invalid action.

---

# Out of Scope

Do not implement:

* workflow history
* audit log
* notifications
* automatic transitions
* scheduled transitions
* role-based transition permissions
* authentication
* authorization
* bulk transitions
* drag-and-drop workflow

---

# Acceptance Criteria

Step 26 is complete when:

* Current ticket status is displayed.
* Valid transition actions are available.
* Invalid actions are not normally presented.
* Transition uses the approved backend API.
* Backend validates transitions.
* Valid transitions succeed.
* Invalid transitions are rejected by backend.
* UI handles backend rejection correctly.
* Status is not changed through normal ticket editing.
* Duplicate transition requests are prevented.
* Transition controls are disabled while processing.
* Successful transition updates the displayed status.
* Failed transition does not incorrectly update the UI.
* 404 is handled.
* 409 is handled if supported.
* 500 is handled.
* Network failures are handled.
* Backend state-machine integration tests pass.
* Frontend transition tests pass.
* Existing edit functionality still works.
* Existing comments still work.
* No unrelated functionality is changed.

---

# Definition of Done

* Specification created.
* Implementation complete.
* Backend contract verified.
* Frontend implementation complete.
* Backend integration tests pass.
* Frontend tests pass.
* Manual valid-transition testing complete.
* Manual invalid-transition testing complete.
* Direct API invalid-transition testing complete.
* AI review completed.
* Human code review completed.
* Git diff reviewed.
* Focused commit created.

Suggested commit:

```text
feat: add ticket status transition UI
```
