# Ticket List UI

## Objective

Implement the ticket listing screen for the Support Ticket Management System.

The screen must consume the existing backend ticket listing API and provide users with a simple way to:

* View tickets
* Search tickets by keyword
* Filter tickets by status
* Navigate to a ticket's details

The implementation must use the existing frontend architecture established.

---

## Route

The ticket list must be available at:

```text
/tickets
```

The existing application shell and routing foundation must be reused.

---

## API

Use the existing backend API contract.

The ticket list API supports:

```http
GET /api/v1/tickets
```

Optional query parameters:

```text
keyword
status
```

Examples:

```http
GET /api/v1/tickets

GET /api/v1/tickets?keyword=login

GET /api/v1/tickets?status=OPEN

GET /api/v1/tickets?keyword=login&status=OPEN
```

Do not invent a new backend endpoint.

Do not duplicate backend search/filter logic in the frontend.

---

## Ticket List

Display the available tickets in a readable structure.

Each ticket should expose at least:

* Ticket ID
* Title
* Priority
* Status
* Assignee
* Updated date/time

The existing backend response contract must be used.

Do not expose fields that are not returned by the API unless they are already part of the approved contract.

---

## Search

Provide a search input.

Example:

```text
Search tickets...
```

Searching must use the backend keyword parameter.

Example:

```http
GET /api/v1/tickets?keyword=login
```

Do not download all tickets and filter them entirely in JavaScript.

The backend remains responsible for search behavior.

---

## Status Filter

Provide a status filter.

Supported statuses:

```text
All
OPEN
IN_PROGRESS
RESOLVED
CLOSED
CANCELLED
```

When a status is selected, call the backend with:

```text
status=<selected status>
```

Selecting `All` should remove the status parameter.

---

## Combined Search and Filter

Search and status filtering must work together.

Example:

```text
keyword = login
status = OPEN
```

The frontend should request:

```http
GET /api/v1/tickets?keyword=login&status=OPEN
```

The frontend must not independently implement the AND/OR matching rules.

---

## Loading State

While the ticket API request is in progress, display a meaningful loading state.

Examples:

```text
Loading tickets...
```

or an appropriate loading indicator.

Avoid displaying stale results as though they represent the current request.

---

## Empty State

When the API successfully returns no tickets, display a meaningful empty state.

For example:

```text
No tickets found.
```

If filters are active, the message may indicate that no tickets match the current search/filter.

Do not display this state while the request is still loading.

---

## Error State

If the backend request fails, display a meaningful error message.

Example:

```text
Unable to load tickets.
Please try again.
```

Provide a retry action where practical.

Do not display raw stack traces or technical exception objects to the user.

---

## Navigation

Each ticket must provide a way to open its details.

For example:

```text
View
```

or clicking the ticket row.

Navigate to:

```text
/tickets/:ticketId
```

The actual ticket details page will be implemented in a later task.

The route does not need to contain the details implementation yet.

---

## Search Interaction

Keep the interaction simple.

The implementation may:

* search on submit
* search on input with a sensible debounce

Do not introduce unnecessary complexity.

If search is performed on every keystroke, avoid generating an excessive number of API requests.

---

## Filter Interaction

Changing the status should trigger a new API request.

The selected search/filter state should be clear to the user.

---

## URL State

If the existing frontend routing structure supports it cleanly, search/filter state may be represented in the URL.

This is optional for this task.

Do not introduce a routing abstraction solely for query-state management.

---

## API Layer

Ticket API calls must remain outside UI components where practical.

Create/reuse a ticket API module such as:

```text
src/api/tickets.ts
```

The UI component should not construct raw fetch URLs throughout the component.

---

## TypeScript

Use explicit types for ticket data.

Do not use:

```typescript
any
```

for the ticket response unless there is a documented and unavoidable reason.

The frontend types should reflect the backend API contract.

---

## Component Structure

Keep components focused.

A reasonable structure may include:

```text
src/
├── api/
│   └── tickets.ts
├── components/
│   ├── TicketList.tsx
│   ├── TicketRow.tsx
│   └── TicketFilters.tsx
├── pages/
│   └── TicketsPage.tsx
└── types/
    └── ticket.ts
```

The exact structure may differ based on the existing frontend architecture.

Do not create components only for the sake of creating more files.

---

## Accessibility

The following should be accessible:

* Search input
* Status selector
* Buttons
* Ticket navigation

Use labels or accessible names rather than relying only on placeholder text.

---

## Responsive Behavior

The ticket list should remain usable on smaller screens.

Do not spend significant effort on advanced responsive design.

A simple responsive layout is sufficient.

---

## Tests

Add frontend tests covering at least:

1. Ticket list renders successfully.
2. Loading state is displayed.
3. Tickets returned from API are displayed.
4. Empty result displays empty state.
5. API failure displays error state.
6. Status filter triggers the correct API request.
7. Search triggers the correct API request.
8. Search + status filter sends both parameters.
9. Ticket navigation points to the correct ticket ID.

Tests should verify user-visible behavior rather than implementation details.

---

## Out of Scope

Do not implement:

* ticket creation
* ticket editing
* ticket details content
* comments
* status transition actions
* authentication
* pagination
* sorting
* advanced dashboard functionality
* real-time updates

---

## Acceptance Criteria

Step 22 is complete when:

* `/tickets` displays successfully.
* Tickets are retrieved from the backend.
* Ticket ID is displayed.
* Ticket title is displayed.
* Priority is displayed.
* Status is displayed.
* Assignee is displayed.
* Updated timestamp is displayed.
* Search works through the backend API.
* Status filter works through the backend API.
* Search + status filter works together.
* Loading state works.
* Empty state works.
* API error state works.
* Retry is available where appropriate.
* Ticket navigation is available.
* No ticket business logic is duplicated in the frontend.
* Frontend tests pass.
* Production build passes.
* No unrelated backend functionality is changed.

## Definition of Done

* Implementation completed.
* Tests added.
* Tests pass.
* Manual browser testing completed.
* API calls verified.
* Git diff reviewed.
* AI review completed.
* Changes committed with a focused commit.

Suggested commit:

```text
feat: add ticket list UI
```
