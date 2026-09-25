# Create Ticket UI

## Objective

Implement the frontend workflow for creating a support ticket.

The user must be able to open the create-ticket page, enter the required ticket information, submit the form, receive meaningful validation/errors, and continue to the newly created ticket or ticket list.

The frontend must use the existing backend API contract.

---

## Route

Create:

```text
/tickets/new
```

The route must be accessible from the ticket list.

---

## API

Use the existing ticket creation endpoint:

```http
POST /api/v1/tickets
```

The frontend must use the existing API client and ticket API module.

Do not construct raw fetch calls directly inside the form component.

---

## Request

The create-ticket request must contain only fields permitted by the existing API contract.

Expected business fields are:

* title
* description
* priority
* assignee

The client must not attempt to control server-managed fields such as:

* ID
* status
* createdAt
* updatedAt

The backend remains responsible for generating those values.

---

## Form Fields

### Title

Required.

Must not be blank.

Display a clear validation message when invalid.

### Description

Required.

Must not be blank.

Display a clear validation message when invalid.

### Priority

Required.

Supported values must match the backend contract.

Expected values:

```text
LOW
MEDIUM
HIGH
CRITICAL
```

Do not invent additional priority values.

### Assignee

Required.

Must not be blank.

---

## Client-side Validation

The frontend should provide immediate, user-friendly validation.

At minimum:

* title cannot be blank
* description cannot be blank
* priority must be selected
* assignee cannot be blank

Do not duplicate complex backend validation rules unless those rules are explicitly defined in the API contract.

Frontend validation is for user experience.

Backend validation remains authoritative.

---

## Backend Validation

If the backend rejects the request with HTTP 400, the frontend must display meaningful validation feedback.

Examples:

```text
Title is required.
Description is required.
Invalid priority.
Assignee is required.
```

If field-level errors are returned by the backend, associate them with the appropriate fields where practical.

Do not expose raw JSON or stack traces as the primary user-facing error.

---

## Submission

When the user submits a valid form:

1. Prevent duplicate submissions.
2. Show a submitting/loading state.
3. Send POST request.
4. Wait for backend response.
5. Handle success or failure.

The submit button should be disabled while the request is in progress.

---

## Success

On successful creation:

* Display appropriate success feedback if useful.
* Navigate to the created ticket details route if the API returns the ticket ID.

Expected route:

```text
/tickets/:ticketId
```

If the API response does not contain a usable ticket ID, navigate back to:

```text
/tickets
```

without inventing an ID.

---

## Error Handling

Handle at least:

### Validation error

HTTP 400.

Display meaningful validation feedback.

### Unexpected server error

HTTP 500 or other unexpected error.

Display a general message such as:

```text
Unable to create ticket. Please try again.
```

### Network error

Display a meaningful connectivity message.

The user must be able to correct the form or retry.

---

## Cancel

Provide a Cancel action.

Cancel should return the user to:

```text
/tickets
```

without submitting the form.

---

## Accessibility

The form must have:

* labels associated with fields
* accessible error messages
* keyboard-accessible controls
* clear required-field indication

Do not rely only on placeholder text.

---

## User Experience

The form should clearly distinguish:

* editable fields
* validation errors
* submission state
* successful submission
* server/network errors

Do not reset the form before the create request succeeds.

---

## API Layer

Add/reuse a method such as:

```text
createTicket(request)
```

inside the ticket API module.

The form component should not contain raw HTTP implementation.

---

## Types

Use explicit TypeScript types for:

```text
CreateTicketRequest
TicketResponse
```

Do not use `any`.

---

## Tests

Add tests covering:

1. Create form renders.
2. Required fields are displayed.
3. Blank title is rejected.
4. Blank description is rejected.
5. Missing priority is rejected.
6. Blank assignee is rejected.
7. Valid form submits.
8. Correct POST request is generated.
9. Submit button prevents duplicate submission while loading.
10. Successful creation navigates correctly.
11. HTTP 400 displays validation feedback.
12. HTTP 500 displays meaningful error.
13. Network failure displays meaningful error.
14. Cancel returns to ticket list.
15. Server-managed fields are not sent by the frontend.

Tests should focus on observable behavior.

---

## Out of Scope

Do not implement:

* edit ticket
* comments
* status transitions
* ticket detail content
* attachments
* authentication
* user management
* pagination
* search changes
* filtering changes

---

## Acceptance Criteria

Step 23 is complete when:

* `/tickets/new` works.
* User can enter title.
* User can enter description.
* User can select priority.
* User can enter assignee.
* Client-side validation works.
* Backend validation errors are displayed meaningfully.
* Submit state works.
* Duplicate submissions are prevented.
* Successful creation works.
* Created ticket ID is handled correctly.
* Navigation after creation works.
* Cancel works.
* Network/server errors are handled.
* No server-managed fields are controlled by the client.
* Frontend tests pass.
* Production build passes.
* Existing ticket list functionality remains working.
* No unrelated backend changes are introduced.

## Definition of Done

* Implementation completed.
* Tests added and passing.
* Manual browser testing completed.
* Backend request verified.
* Error scenarios verified.
* AI review completed.
* Human diff review completed.
* Focused Git commit created.

Suggested commit:

```text
feat: add create ticket UI
```
