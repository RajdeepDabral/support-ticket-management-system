# Task List Tickets and View Ticket Details

## Objective

Implement the ticket retrieval APIs defined in the approved API contract.

The APIs must support:

* listing tickets
* retrieving a single ticket by ID

This task focuses only on read operations.

## References

Read before implementation:

* `spec/requirements.md`
* `spec/architecture.md`
* `spec/data-model.md`
* `spec/api-contract.md`
* `spec/state-machine.md`
* `spec/test-strategy.md`
* `spec/ticket-domain-service.md`
* `spec/create-ticket-api.md`
* `rules/java-springboot.md`
* `rules/testing.md`
* `rules/api-standards.md`

## Endpoints

Implement:

```text
GET /api/v1/tickets
GET /api/v1/tickets/{ticketId}
```

## List Tickets

The list endpoint must return persisted tickets.

For the current scope:

```text
GET /api/v1/tickets
```

returns all tickets available to the application.

The API contract currently supports these optional query parameters:

* `keyword`
* `status`

If search/filter functionality has not yet been implemented, do not partially implement or invent its behavior in this task.

Those capabilities will be handled by the dedicated search/filter task.

## Empty Results

If there are no tickets:

* return HTTP 200
* return an empty collection
* do not return 404

Expected conceptual response:

```json
[]
```

## Ticket Details

Implement:

```text
GET /api/v1/tickets/{ticketId}
```

The response should contain the ticket fields defined by the API contract.

If the ticket does not exist:

* return HTTP 404
* use the project's standard error response

## DTOs

Use response DTOs.

Do not expose JPA entities directly.

The list response and detail response should use the appropriate DTO representation.

## Service Layer

The controller must call the existing ticket service/application layer.

Do not access repositories directly from controllers.

The service remains responsible for:

* retrieval
* not-found handling
* mapping/business decisions where appropriate

## Comments

The API contract specifies that ticket details include comments.

If the current entity/service structure already supports safely retrieving comments, include them according to the contract.

Do not introduce a separate comments REST API in this task.

Do not implement comment creation yet.

## Error Handling

Unknown ticket IDs must produce the standard 404 response.

Do not expose:

* SQL errors
* Hibernate exceptions
* stack traces
* internal implementation details

## Performance

Do not introduce pagination unless the specification is explicitly updated.

Do not introduce caching.

Do not introduce Elasticsearch.

Do not introduce unnecessary query abstractions.

## Testing

Add tests for:

### List

* list with multiple tickets
* list with one ticket
* empty list

### Details

* existing ticket
* unknown ticket

### API

Verify:

* HTTP status
* response structure
* DTO mapping
* error response

If comments are included in the detail response, test the relationship and response mapping.

Use PostgreSQL-compatible testing.

Do not use H2.

## Out of Scope

Do not implement:

* ticket update
* status transition API
* comment creation
* search
* filtering
* frontend
* authentication
* authorization
* pagination
* caching
* Elasticsearch
* Redis
* Kafka

## Completion Criteria

The task is complete when:

* list endpoint works
* detail endpoint works
* empty list returns 200
* missing ticket returns 404
* response DTOs are used
* entities are not exposed
* existing ticket creation still works
* tests pass
* PostgreSQL is used
* AI review is completed
* human review is completed
