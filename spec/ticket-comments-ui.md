# Ticket Comments UI

## Objective

Implement the ticket comments experience on the existing ticket details page.

Users must be able to:

* View comments associated with a ticket.
* Add a new comment.
* Receive meaningful validation feedback.
* Receive meaningful API/network errors.
* See loading and empty states.

The implementation must reuse the existing frontend architecture and backend API contract.

---

## Location

Comments must be displayed on:

```text
/tickets/:ticketId
```

The existing ticket details page from Step 24 must remain functional.

---

## Backend API

Before implementation, inspect the approved API contract and existing backend implementation.

Use the existing comment endpoints.

Do not invent a new endpoint.

The frontend must use the existing API abstraction rather than placing raw HTTP calls directly inside UI components.

---

## Comment List

Display existing comments associated with the ticket.

Each comment should display, where provided by the API:

* Comment content
* Author
* Created timestamp

Do not display fields that are not part of the approved API contract.

---

## Loading State

While comments are being retrieved, display a meaningful loading state.

Example:

```text
Loading comments...
```

The loading state must not be confused with an empty comment list.

---

## Empty State

If the ticket has no comments, display a meaningful message.

Example:

```text
No comments yet.
```

This state should only be displayed after a successful API response containing zero comments.

---

## Error State

If comments cannot be retrieved, display a meaningful error.

Example:

```text
Unable to load comments.
Please try again.
```

Provide a retry action where practical.

Do not expose raw exception messages or stack traces.

---

# Add Comment

Provide a comment input area.

A simple multiline textarea is sufficient.

Example:

```text
Add a comment...

[ Add Comment ]
```

Do not introduce a rich text editor unless it already exists in the project.

---

## Validation

A comment must not be blank.

Whitespace-only comments must also be rejected.

Example:

```text
Comment cannot be empty.
```

Client-side validation is for user experience.

The backend remains authoritative.

Do not invent additional comment validation rules unless they already exist in the approved contract.

---

## Submit

When the user submits a valid comment:

1. Validate the comment.
2. Prevent duplicate submission.
3. Display submitting state.
4. Send the backend request.
5. Process the response.
6. Display the new comment.

The Add Comment button must be disabled while the request is in progress.

---

## API Request

Use the exact endpoint, HTTP method, and request structure already defined by the backend.

Do not invent request fields.

Do not send:

* ticket status
* ticket priority
* ticket title
* ticket description
* ticket timestamps

unless explicitly required by the existing comment API contract.

The comment API should be responsible only for comment creation.

---

## Successful Comment

After successful creation:

* The new comment should become visible.
* The input should be cleared only after successful creation.
* The user should remain on the same ticket page.
* Existing ticket details should remain intact.

Do not reload the entire browser page unnecessarily.

Prefer updating the comments state using the API response.

---

## Error Handling

Handle at least:

### HTTP 400

Display meaningful validation feedback.

### HTTP 404

If the ticket no longer exists, communicate that clearly.

### HTTP 500

Display:

```text
Unable to add comment.
Please try again.
```

### Network Failure

Display a meaningful error and allow retry.

Do not expose raw technical exceptions to the user.

---

## Comment Ordering

Use the ordering provided by the backend/API contract.

Do not invent client-side ordering rules if the backend already defines the ordering.

If the API does not define an ordering, document the frontend assumption before implementing it.

---

## API Layer

Reuse or extend the existing ticket/comment API abstraction.

Conceptually, methods may look like:

```text
getComments(ticketId)
addComment(ticketId, request)
```

The exact implementation must follow the existing project architecture.

Do not create duplicate API clients.

---

## Types

Use explicit TypeScript types.

For example:

```text
Comment
CreateCommentRequest
```

based on the actual backend response/request contract.

Do not use:

```typescript
any
```

for comment data.

---

## Component Structure

Reuse the existing ticket details page.

A reasonable structure may be:

```text
TicketDetailsPage
    ├── TicketDetails
    ├── EditTicket
    └── Comments
          ├── CommentList
          ├── CommentItem
          └── AddCommentForm
```

The exact structure may differ according to the existing implementation.

Do not create unnecessary components.

---

## Accessibility

The comment input must have:

* An accessible label.
* Keyboard accessibility.
* An accessible validation error.
* A clear submit button.

Do not rely only on placeholder text.

---

## Tests

Add behavior-focused tests covering:

1. Comments section renders.
2. Comments are retrieved for the ticket.
3. Existing comments are displayed.
4. Author is displayed when provided.
5. Timestamp is displayed when provided.
6. Loading state is displayed.
7. Empty state is displayed.
8. API error is displayed.
9. Retry works.
10. Comment input is displayed.
11. Empty comment is rejected.
12. Whitespace-only comment is rejected.
13. Valid comment submits.
14. Correct API endpoint/request is used.
15. Submit button prevents duplicate requests.
16. Successful comment appears in the UI.
17. Input clears only after successful creation.
18. Failed submission preserves the user's comment.
19. Backend validation error is displayed.
20. Existing ticket details remain functional.

Tests should focus on observable behavior rather than implementation details.

---

## Out of Scope

Do not implement:

* Comment editing
* Comment deletion
* Rich text
* Attachments
* Mentions
* Notifications
* Authentication
* Authorization
* Status transitions
* Ticket reassignment
* Pagination
* Real-time comments

---

## Acceptance Criteria

Step 25 is complete when:

* Comments are displayed on the ticket details page.
* Existing comments are loaded from the backend.
* Author is displayed where available.
* Timestamp is displayed where available.
* Empty state works.
* Loading state works.
* Error state works.
* Retry works.
* User can enter a comment.
* Empty comments are rejected.
* Whitespace-only comments are rejected.
* Valid comments can be submitted.
* Duplicate submissions are prevented.
* Correct backend API is used.
* New comments appear after successful creation.
* Input clears only after successful creation.
* Failed submissions do not lose user input.
* Backend validation errors are handled.
* Existing ticket details continue working.
* Frontend tests pass.
* Production build passes.
* No unrelated functionality is changed.

---

## Definition of Done

* Implementation complete.
* Tests added.
* Tests pass.
* Manual browser testing complete.
* API requests verified.
* Error scenarios verified.
* AI review complete.
* Human diff review complete.
* Focused Git commit created.

Suggested commit:

```text
feat: add ticket comments UI
```
