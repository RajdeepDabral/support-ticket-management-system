# Support Ticket Management System — UI Flow Specification

## 1. Purpose

This document defines the user interface flow for the Support Ticket Management System.

It describes:

* Application screens
* User actions
* Form behavior
* Ticket listing
* Search and filtering
* Ticket details
* Status transitions
* Comments
* Loading states
* Empty states
* Validation
* Backend error handling

The frontend must consume the REST API defined in:

```text
spec/api-contract.md
```

The frontend is responsible for usability and client-side feedback.

The backend remains authoritative for validation and business rules.

---

# 2. Frontend Scope

The application requires the following primary screens:

1. Ticket List
2. Create Ticket
3. Ticket Details

The UI should remain intentionally simple and focused on the assignment requirements.

No authentication, dashboard analytics, user administration, notifications, or unrelated functionality should be introduced.

---

# 3. Application Navigation

The application should provide simple navigation between the main areas.

Recommended navigation:

```text
Support Ticket Management
│
├── Tickets
│   └── Ticket List
│
└── Create Ticket
```

From the ticket list, selecting a ticket opens:

```text
Ticket Details
```

The user should be able to return to the ticket list from the details screen.

---

# 4. Ticket List Screen

## Purpose

The Ticket List screen allows users to:

* View tickets
* Search tickets by keyword
* Filter tickets by status
* Open ticket details
* Navigate to ticket creation

---

## 4.1 Layout

Recommended structure:

```text
----------------------------------------------------
 Support Ticket Management

 [ + Create Ticket ]

 Search: [____________________]

 Status: [ All ▼ ]

----------------------------------------------------
 ID | Title | Priority | Status | Assignee
----------------------------------------------------
 1  | Login | HIGH     | OPEN   | John
 2  | API   | MEDIUM   | RESOLVED | Jane
----------------------------------------------------
```

The exact visual styling is an implementation decision.

The UI should prioritize clarity over visual complexity.

---

# 5. Ticket Search

The list screen contains a keyword search field.

The search applies to:

* Ticket title
* Ticket description

Search is case-insensitive.

Example:

```text
Search: payment
```

The frontend calls:

```http
GET /api/tickets?keyword=payment
```

---

# 6. Status Filter

The ticket list provides a status filter.

Suggested options:

```text
All
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

Selecting a specific status calls:

```http
GET /api/tickets?status=OPEN
```

Selecting `All` does not send a status filter.

---

# 7. Combined Search and Filter

Keyword and status can be used simultaneously.

Example:

```text
Keyword: login
Status: OPEN
```

The frontend calls:

```http
GET /api/tickets?keyword=login&status=OPEN
```

The backend performs the actual filtering.

The frontend must not retrieve all tickets and perform business filtering locally.

---

# 8. Search Interaction

The frontend may implement search using either:

* Search button
* Enter key
* Controlled search interaction

The implementation should avoid unnecessary API requests for every keystroke unless there is a clear reason to do so.

For this assignment, a simple explicit search action is preferred.

Example:

```text
User enters:
payment

User clicks:
Search

Frontend:
GET /api/tickets?keyword=payment
```

---

# 9. Empty Ticket List

If no tickets exist:

```text
No tickets found.
```

The screen should provide a clear action:

```text
[ Create Ticket ]
```

---

# 10. Empty Search Result

If filters/search produce no results:

```text
No tickets match your search criteria.
```

The user should be able to:

* Clear the search
* Change the status filter
* Create a new ticket

The UI should not treat an empty result as a backend error.

---

# 11. Create Ticket Screen

## Purpose

Allows a user to create a new ticket.

Fields:

```text
Title
Description
Priority
Assignee
```

Status is not displayed as an editable field.

The backend automatically creates the ticket with:

```text
OPEN
```

---

# 12. Create Ticket Form

Recommended form:

```text
Create Ticket

Title
[________________________________]

Description
[________________________________]
[________________________________]
[________________________________]

Priority
[ MEDIUM ▼ ]

Assignee
[________________________________]

[ Cancel ]       [ Create Ticket ]
```

---

# 13. Create Ticket Client Validation

The frontend should provide immediate validation for obvious invalid input.

### Title

* Required
* Must not be blank
* Maximum 255 characters

### Description

* Required
* Must not be blank

### Priority

* Required
* Must be one of:

    * LOW
    * MEDIUM
    * HIGH
    * CRITICAL

### Assignee

* Required
* Must not be blank
* Maximum 255 characters

Client validation improves usability.

However, backend validation remains authoritative.

---

# 14. Create Ticket Submission

When the user submits the form:

```http
POST /api/tickets
```

Example request:

```json
{
  "title": "Unable to login",
  "description": "User cannot login to the application.",
  "priority": "HIGH",
  "assignee": "john.doe"
}
```

The frontend should show a submission/loading state.

Example:

```text
[ Creating... ]
```

The user should not accidentally submit the same form multiple times while the request is in progress.

---

# 15. Successful Ticket Creation

After successful creation:

```text
POST /api/tickets
→ 201 Created
```

The UI should provide clear confirmation.

Recommended flow:

```text
Create Ticket
     ↓
API success
     ↓
Show success message
     ↓
Open created ticket details
```

Example message:

```text
Ticket created successfully.
```

The exact navigation approach can be finalized during implementation.

---

# 16. Create Ticket Backend Validation Error

If the backend returns:

```text
400 Bad Request
```

the frontend should display meaningful validation feedback.

Example:

```text
Title must not be blank.
Priority must be one of LOW, MEDIUM, HIGH, CRITICAL.
```

Errors should be displayed close to the relevant fields where possible.

The UI should not display raw Java exceptions, stack traces, or internal backend details.

---

# 17. Ticket Details Screen

## Purpose

The Ticket Details screen displays:

* Ticket information
* Current status
* Available status actions
* Comments
* Add comment form

Recommended structure:

```text
----------------------------------------------------
 Ticket #1001

 Title
 Unable to login

 Description
 User cannot login to the application.

 Priority: HIGH
 Status: OPEN
 Assignee: john.doe

 Created: ...
 Updated: ...

 Status Actions
 [ Start Progress ] [ Cancel ]

----------------------------------------------------
 Comments

 john.doe
 Investigating the issue.
 ...

 [ Add comment ]

----------------------------------------------------

 [ Back to Tickets ]
```

---

# 18. Ticket Information

The details screen should display:

* ID
* Title
* Description
* Priority
* Status
* Assignee
* Created timestamp
* Updated timestamp

The frontend should format timestamps in a human-readable manner while preserving the actual API timestamp semantics.

---

# 19. Update Ticket Fields

The user must be able to update:

* Title
* Description
* Priority
* Assignee

The frontend may provide an edit form or inline editing.

The implementation should use:

```http
PATCH /api/tickets/{ticketId}
```

Example:

```json
{
  "priority": "CRITICAL",
  "assignee": "jane.doe"
}
```

Only changed fields need to be sent.

---

# 20. Status Update UI

Status changes must use:

```http
PATCH /api/tickets/{ticketId}/status
```

The UI should show only transitions that are valid for the current state.

---

# 21. Status Actions

For an `OPEN` ticket:

```text
[ Start Progress ]
[ Cancel ]
```

For an `IN_PROGRESS` ticket:

```text
[ Resolve ]
[ Cancel ]
```

For a `RESOLVED` ticket:

```text
[ Close ]
```

For a `CLOSED` ticket:

```text
No status actions
```

For a `CANCELLED` ticket:

```text
No status actions
```

The UI must derive these available actions from the state-machine rules.

---

# 22. Backend Protection for Status Changes

Hiding invalid actions in the UI is only a usability feature.

It is not a security or business-rule mechanism.

For example, if the UI shows no `OPEN` option for a `CLOSED` ticket, the backend must still reject:

```http
PATCH /api/tickets/1001/status
```

with:

```json
{
  "status": "OPEN"
}
```

The backend returns:

```text
409 Conflict
```

The frontend must handle this response gracefully.

---

# 23. Invalid Status Transition UI

If a status transition fails because of a backend business rule:

```text
Unable to change ticket status.
The requested status transition is not allowed.
```

The frontend should refresh the ticket details if appropriate so the displayed status reflects the backend's actual state.

---

# 24. Add Comment

The ticket details page contains a comment form.

Example:

```text
Add Comment

[________________________________]
[________________________________]

Author
[ john.doe ]

[ Add Comment ]
```

The frontend calls:

```http
POST /api/tickets/{ticketId}/comments
```

Example:

```json
{
  "content": "The issue has been reproduced.",
  "author": "john.doe"
}
```

---

# 25. Comment Validation

The frontend should validate:

### Content

* Required
* Must not be blank

### Author

* Required
* Must not be blank
* Maximum 255 characters

Backend validation remains authoritative.

---

# 26. Successful Comment

After:

```text
201 Created
```

the newly created comment should become visible in the ticket details.

Recommended behavior:

```text
Add Comment
    ↓
API success
    ↓
Clear comment form
    ↓
Display new comment
```

A full page reload is not required if the frontend can update its local state reliably.

---

# 27. Ticket Not Found

If:

```http
GET /api/tickets/{ticketId}
```

returns:

```text
404 Not Found
```

the UI should display:

```text
Ticket not found.
```

The user should have an option to return to the ticket list.

---

# 28. General API Error Handling

The frontend should handle at least:

```text
400 Bad Request
404 Not Found
409 Conflict
500 Internal Server Error
```

### 400

Display validation or invalid request information.

### 404

Display that the requested ticket does not exist.

### 409

Display the business-rule conflict, particularly invalid status transitions.

### 500

Display a user-friendly message:

```text
Something went wrong while processing your request.
Please try again.
```

Do not display:

* stack traces
* database errors
* SQL statements
* internal class names
* server implementation details

---

# 29. Loading States

API operations should provide visible loading feedback.

Examples:

### Ticket List

```text
Loading tickets...
```

### Ticket Details

```text
Loading ticket...
```

### Create Ticket

```text
Creating...
```

### Update Ticket

```text
Saving...
```

### Status Update

```text
Updating status...
```

### Add Comment

```text
Adding comment...
```

Buttons involved in an active request should be disabled where appropriate to prevent duplicate submissions.

---

# 30. Error Recovery

The UI should allow the user to recover from common errors.

Examples:

```text
Failed to load tickets.
[ Retry ]
```

```text
Failed to save changes.
[ Try Again ]
```

For a status conflict:

```text
Unable to change status because the transition is no longer valid.

[ Refresh Ticket ]
```

The frontend should avoid leaving the user with a dead-end error screen.

---

# 31. Navigation After Errors

Errors should not unnecessarily navigate the user away from their current context.

Examples:

* Validation error → remain on form
* Update error → remain on details
* Comment error → remain on details
* Ticket not found → allow navigation back to list

---

# 32. Frontend and Backend Responsibilities

| Responsibility              |      Frontend      |    Backend    |
| --------------------------- | :----------------: | :-----------: |
| Required field feedback     |         Yes        |      Yes      |
| String length validation    |         Yes        |      Yes      |
| Priority validation         |         Yes        |      Yes      |
| Ticket existence            |         No         |      Yes      |
| State transition validation |    Display only    |      Yes      |
| Search/filter execution     | Request parameters |      Yes      |
| Persistence                 |         No         |      Yes      |
| Business rules              |  Display behavior  | Authoritative |

The backend is always the final authority.

---

# 33. UI State Model

The frontend should maintain enough state to represent:

```text
Tickets
Search keyword
Selected status filter
Selected ticket
Loading state
Submitting state
Error state
Success message
```

The exact state-management mechanism is an implementation decision.

A dedicated global state library is not required for this assignment unless implementation complexity justifies it.

---

# 34. Accessibility and Usability

The UI should follow basic accessibility practices:

* Form fields should have labels.
* Buttons should have meaningful text.
* Errors should be understandable.
* Interactive elements should be keyboard accessible.
* Loading states should be visible.
* Color should not be the only way status is communicated.

Status should be displayed using both text and optional visual styling.

Example:

```text
Status: OPEN
```

rather than relying only on a colored badge.

---

# 35. Responsive Behavior

The UI should remain usable on:

* Desktop
* Tablet
* Smaller browser widths

The assignment does not require a complex mobile application.

Responsive layout should focus on maintaining usable forms, tables/lists, and ticket details.

---

# 36. UI-to-API Traceability

| UI Action       | API                                       |
| --------------- | ----------------------------------------- |
| Create ticket   | `POST /api/tickets`                       |
| Load tickets    | `GET /api/tickets`                        |
| Search tickets  | `GET /api/tickets?keyword=...`            |
| Filter tickets  | `GET /api/tickets?status=...`             |
| Search + filter | `GET /api/tickets?keyword=...&status=...` |
| View ticket     | `GET /api/tickets/{id}`                   |
| Update ticket   | `PATCH /api/tickets/{id}`                 |
| Change status   | `PATCH /api/tickets/{id}/status`          |
| Add comment     | `POST /api/tickets/{id}/comments`         |

---

# 37. UI Acceptance Checklist

The implementation is complete when:

* [ ] User can create a ticket.
* [ ] New ticket appears with `OPEN` status.
* [ ] User can see the ticket list.
* [ ] User can search by title/description keyword.
* [ ] Search is case-insensitive through backend behavior.
* [ ] User can filter by status.
* [ ] User can combine search and status filter.
* [ ] User can open ticket details.
* [ ] User can update title.
* [ ] User can update description.
* [ ] User can update priority.
* [ ] User can change assignee.
* [ ] User can add comments.
* [ ] User can perform valid status transitions.
* [ ] Invalid status transitions cannot be performed successfully.
* [ ] Backend errors are presented meaningfully.
* [ ] Loading states are visible.
* [ ] Empty results are handled clearly.
* [ ] Ticket-not-found behavior is handled.
* [ ] No raw backend stack traces are shown.
* [ ] UI does not implement business rules as the only protection.

---

# 38. Deliberate Scope Boundaries

The UI will not include:

* Login
* User registration
* User administration
* Role management
* Notifications
* Attachments
* Dashboard analytics
* Advanced reporting
* Ticket deletion
* Complex state-management frameworks without need

These are outside the current assignment scope.

---

# 39. Human Review Checklist

Before implementation, review:

* [ ] Every requirement has a corresponding UI flow.
* [ ] Every UI operation maps to an API endpoint.
* [ ] Status actions match `spec/state-machine.md`.
* [ ] Frontend does not become the source of truth for business rules.
* [ ] Backend errors have meaningful UI handling.
* [ ] No unnecessary functionality has been added.
* [ ] Forms contain appropriate validation.
* [ ] Loading and empty states are defined.
* [ ] The design remains simple enough for the assignment.
* [ ] The API contract and UI flow are consistent.

---

# 40. Specification Status

**Status:** Ready for human review

The UI specification should be reviewed against:

```text
spec/requirements.md
spec/architecture.md
spec/data-model.md
spec/api-contract.md
spec/state-machine.md
```

No frontend implementation should begin until this specification has been reviewed.
