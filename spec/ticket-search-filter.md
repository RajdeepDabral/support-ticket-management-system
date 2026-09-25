# Ticket Search and Status Filter

## Objective

Enhance the existing ticket listing API to support:

1. Keyword search
2. Status filtering
3. Combined keyword search and status filtering

Endpoint:

`GET /api/v1/tickets`

The implementation must use PostgreSQL.

## Search

The optional `keyword` query parameter searches:

* ticket title
* ticket description

Search must be case-insensitive.

Example:

```http
GET /api/v1/tickets?keyword=login
```

A ticket matches when the keyword occurs in either:

```text
title
```

or:

```text
description
```

The search should use substring matching.

For example, searching for:

```text
login
```

may match:

```text
Login issue
Unable to login to application
Login failure after password reset
```

## Status Filter

The optional `status` query parameter filters tickets by exact status.

Example:

```http
GET /api/v1/tickets?status=OPEN
```

Supported values:

* OPEN
* IN_PROGRESS
* RESOLVED
* CLOSED
* CANCELLED

Invalid status values must return:

```text
HTTP 400 Bad Request
```

using the existing centralized error handling.

## Combined Search and Filter

Both parameters may be supplied together.

Example:

```http
GET /api/v1/tickets?keyword=login&status=OPEN
```

The result must satisfy BOTH conditions:

```text
keyword matches title OR description
AND
status matches requested status
```

## No Parameters

The existing endpoint behavior must remain unchanged.

```http
GET /api/v1/tickets
```

returns all tickets.

## Empty Results

If no ticket matches the criteria:

```text
HTTP 200 OK
```

with:

```json
[]
```

Do not return 404 for an empty search result.

## Keyword Behavior

Keyword matching must be case-insensitive.

For example:

```text
login
LOGIN
Login
LoGiN
```

should match the same relevant records.

Leading/trailing whitespace in the keyword should be handled sensibly, preferably by trimming it before querying.

An empty or whitespace-only keyword should not cause an error and should behave like no keyword filter.

## Repository

The repository/query layer is responsible for database querying.

The implementation should use PostgreSQL/JPA/Spring Data capabilities already present in the project.

Do not introduce Elasticsearch.

Do not introduce Redis.

Do not introduce another database.

## Architecture

Follow:

```text
Controller
    ↓
Service/Application Layer
    ↓
Repository
    ↓
PostgreSQL
```

The controller should not construct database queries.

The service should coordinate the query operation.

The repository should handle persistence/query concerns.

## Query Design

The implementation may use:

* Spring Data derived queries
* JPQL
* JPA Specification
* another simple Spring Data/JPA approach

Choose the simplest approach that supports:

* optional keyword
* optional status
* both filters together

Do not introduce unnecessary abstractions.

## Response

Return the existing `TicketResponse` representation.

Do not expose JPA entities.

The list response remains:

```json
[
  {
    "id": 1,
    "title": "Login issue",
    "description": "User cannot login",
    "priority": "HIGH",
    "status": "OPEN",
    "assignee": "rajdeep",
    "createdAt": "...",
    "updatedAt": "..."
  }
]
```

## Validation

The backend is authoritative.

Invalid status values must return HTTP 400.

Example:

```http
GET /api/v1/tickets?status=INVALID
```

Expected:

```text
400 Bad Request
```

## Tests

Add tests covering:

### Existing behavior

1. No parameters returns all tickets.
2. Empty database returns 200 with an empty list.

### Keyword search

3. Keyword matches title.
4. Keyword matches description.
5. Keyword does not match unrelated tickets.
6. Search is case-insensitive.
7. Partial/substring search works.
8. Empty keyword behaves like no keyword filter.
9. Whitespace around keyword is handled appropriately.

### Status filter

10. Filter by OPEN.
11. Filter by IN_PROGRESS.
12. Filter by RESOLVED.
13. Filter by CLOSED.
14. Filter by CANCELLED.
15. Invalid status returns 400.

### Combined filters

16. Keyword + status returns only tickets satisfying both conditions.
17. Keyword matching ticket with a different status is excluded.
18. Correct results are returned when multiple tickets match the keyword.

### Empty results

19. No keyword matches returns 200 with [].
20. No ticket matches the requested status returns 200 with [].
21. No ticket satisfies both filters returns 200 with [].

### API boundary

22. Response uses TicketResponse.
23. JPA entities are not returned directly.

## Performance

For this assignment, keep the query implementation simple and PostgreSQL-based.

Do not implement:

* Elasticsearch
* Redis
* caching
* pagination
* full-text search infrastructure
* search indexing infrastructure

If indexes are already defined by the data model, preserve them.

## Out of Scope

Do not implement:

* frontend search/filter UI
* pagination
* sorting
* Elasticsearch
* Redis
* Kafka
* RabbitMQ
* authentication
* authorization
* ticket deletion
* comment search
* new search infrastructure

## Definition of Done

* GET /api/v1/tickets supports keyword.
* GET /api/v1/tickets supports status.
* Keyword searches title and description.
* Keyword search is case-insensitive.
* Keyword and status can be combined.
* Invalid status returns 400.
* Empty results return 200 with [].
* Existing list behavior remains working.
* PostgreSQL is used for querying.
* No Elasticsearch introduced.
* No unnecessary abstraction introduced.
* TicketResponse remains the API response model.
* Tests pass.
* Full existing test suite passes.
