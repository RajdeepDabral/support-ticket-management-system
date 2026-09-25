# End-to-End & Acceptance Testing

## Objective

Validate the complete Support Ticket Management System through realistic end-to-end user workflows.

This step verifies that the implemented frontend, backend, database, API contracts, validation, comments, search, filtering, and state-machine behavior work together as an integrated application.

The focus is on user-visible behavior and acceptance criteria rather than implementation details.

---

# Scope

The end-to-end validation must cover:

* Ticket creation
* Ticket listing
* Ticket details
* Ticket update
* Assignee update
* Comments
* Status transitions
* Search
* Status filtering
* Combined search and filtering
* Backend validation
* Error handling
* State-machine enforcement
* Persistence
* UI loading/empty/error states
* Application restart behavior

---

# Primary Acceptance Workflow

Execute the following complete workflow.

## Step 1 — Create Ticket

From the UI create a ticket with:

* Title
* Description
* Priority
* Assignee

Do not allow the user to specify the initial status.

Expected:

* Ticket is created successfully.
* HTTP/API response is successful.
* Ticket receives an ID.
* Initial status is OPEN.
* Ticket appears in the ticket list.

---

# Step 2 — Open Ticket

Open the newly created ticket.

Verify:

* ID
* Title
* Description
* Priority
* Status
* Assignee
* Created timestamp
* Updated timestamp

are displayed correctly.

Status must be:

```text
OPEN
```

---

# Step 3 — Edit Ticket

Edit:

* Title
* Description
* Priority
* Assignee

Save the changes.

Verify:

* Updated values are displayed.
* Status remains OPEN.
* Created timestamp does not unexpectedly change.
* Updated timestamp changes appropriately.

---

# Step 4 — Add Comment

Add a meaningful comment.

Verify:

* Comment is submitted.
* Comment appears in the ticket.
* Author is displayed.
* Timestamp is displayed.
* Comment input is cleared after successful creation.

---

# Step 5 — Start Progress

Transition:

```text
OPEN → IN_PROGRESS
```

Verify:

* Backend accepts the transition.
* UI displays IN_PROGRESS.
* Available actions change accordingly.

---

# Step 6 — Resolve Ticket

Transition:

```text
IN_PROGRESS → RESOLVED
```

Verify:

* Backend accepts the transition.
* UI displays RESOLVED.
* Close action becomes available.

---

# Step 7 — Close Ticket

Transition:

```text
RESOLVED → CLOSED
```

Verify:

* Backend accepts the transition.
* UI displays CLOSED.
* No further transition action is available.

---

# Search Acceptance

Create tickets with distinguishable words in:

* title
* description

Search by a keyword from the title.

Verify the matching ticket appears.

Search by a keyword from the description.

Verify the matching ticket appears.

Search must be case-insensitive.

Example:

```text
login
LOGIN
Login
```

must produce equivalent matching behavior.

---

# Status Filter Acceptance

Verify each supported status filter:

```text
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

The result set must contain only tickets with the selected status.

---

# Combined Search + Filter

Use:

```text
keyword + status
```

Verify the result satisfies both conditions:

```text
(title matches OR description matches)
AND
(status matches)
```

---

# Negative State-Machine Testing

The frontend should not normally expose invalid transitions.

However, the backend must independently reject them.

Test direct API calls for invalid transitions such as:

```text
CLOSED → OPEN
RESOLVED → OPEN
CANCELLED → OPEN
OPEN → RESOLVED
OPEN → CLOSED
```

Expected:

* Request is rejected.
* Ticket status remains unchanged.
* Appropriate error response is returned.

---

# Validation Testing

Verify backend validation through API/UI workflows.

Test:

* Blank title
* Blank description
* Invalid priority
* Blank assignee
* Invalid status
* Blank comment
* Whitespace-only comment

The application must return meaningful validation feedback.

---

# Not Found Testing

Test:

```text
GET /api/v1/tickets/{unknownId}
```

Expected:

```text
404
```

The UI must display a meaningful ticket-not-found message.

Also test comment and status operations against an unknown ticket where applicable.

---

# Error Handling

Verify meaningful handling of:

* 400
* 404
* 409 where applicable
* 500
* Network failure

The UI must not display raw stack traces.

---

# Persistence Testing

Create/update a ticket and add a comment.

Stop the application.

Restart the application.

Verify:

* Ticket still exists.
* Updated values remain.
* Comment remains.
* Status remains unchanged.
* Database data was persisted correctly.

---

# Browser Refresh

After creating/updating a ticket:

Refresh the ticket details page.

Verify:

* Ticket data is loaded from the backend.
* Comments remain.
* Status remains correct.
* No data depends only on frontend memory.

---

# Concurrent/Stale UI Scenario

Where practical:

1. Open the same ticket in two browser tabs.
2. Change the ticket state in one tab.
3. Attempt an operation from the other tab using stale information.

Verify the backend remains authoritative.

The frontend must correctly handle any rejected or stale operation response.

---

# Acceptance Matrix

The following capabilities must be verified:

| Capability              | Expected         |
| ----------------------- | ---------------- |
| Create ticket           | Works            |
| Initial status          | OPEN             |
| List tickets            | Works            |
| Ticket details          | Works            |
| Edit title              | Works            |
| Edit description        | Works            |
| Edit priority           | Works            |
| Edit assignee           | Works            |
| Add comment             | Works            |
| Search title            | Works            |
| Search description      | Works            |
| Case-insensitive search | Works            |
| Status filter           | Works            |
| Combined search/filter  | Works            |
| OPEN → IN_PROGRESS      | Allowed          |
| IN_PROGRESS → RESOLVED  | Allowed          |
| RESOLVED → CLOSED       | Allowed          |
| OPEN → CANCELLED        | Allowed          |
| IN_PROGRESS → CANCELLED | Allowed          |
| Invalid transitions     | Rejected         |
| Backend validation      | Enforced         |
| Not found               | 404              |
| Persistence             | Survives restart |
| UI errors               | Meaningful       |
| No secrets committed    | Verified         |

---

# Test Evidence

Record meaningful evidence for the final submission.

Evidence may include:

* automated test output
* screenshots
* API request/response examples
* browser workflow
* database persistence verification
* invalid transition response
* Git commit history

Do not create unnecessary screenshots for every minor action.

Capture evidence for important acceptance criteria.

---

# Test Data

Use realistic but non-sensitive test data.

Do not use:

* real customer information
* passwords
* API keys
* production credentials
* personal confidential data

---

# Automation

If an E2E framework already exists, extend it.

If not, use a lightweight browser automation approach appropriate to the frontend stack.

Do not introduce a large testing framework unnecessarily.

Possible tools include:

* Playwright
* Cypress

Choose one only if needed.

Do not introduce both.

---

# Test Isolation

Automated E2E tests should avoid relying on test execution order where practical.

Tests should create their own required data or use a controlled test environment.

Do not depend on manually created production-like data.

---

# Out of Scope

Do not implement:

* new product features
* authentication
* authorization
* performance testing
* load testing
* production monitoring
* analytics
* notifications
* attachments
* real-time collaboration

---

# Definition of Done

Step 27 is complete when:

* Complete happy-path workflow passes.
* Search tests pass.
* Filter tests pass.
* Combined search/filter tests pass.
* Validation scenarios pass.
* Invalid state transitions are rejected.
* Persistence after restart is verified.
* Browser refresh is verified.
* Error scenarios are verified.
* E2E tests pass where implemented.
* Backend tests pass.
* Frontend tests pass.
* Production frontend build passes.
* No secrets are committed.
* Test evidence is captured.
* AI review is complete.
* Human review is complete.
* Git changes are reviewed.
* Focused commit is created.

Suggested commit:

```text
test: add end-to-end acceptance coverage
```
