# Database and Persistence

## Objective

Implement the PostgreSQL persistence layer defined by `spec/data-model.md`.

The implementation must provide the database entities, repositories, and version-controlled database schema required for the Support Ticket Management System.

## References

Read before implementation:

* `spec/requirements.md`
* `spec/architecture.md`
* `spec/data-model.md`
* `spec/api-contract.md`
* `spec/state-machine.md`
* `spec/implementation-plan.md`
* `rules/java-springboot.md`
* `rules/testing.md`
* `rules/api-standards.md`

## Scope

Implement only:

* Ticket persistence model
* Comment persistence model
* Ticket repository
* Comment repository
* PostgreSQL database schema/migration
* Required JPA mappings
* Persistence tests

## Ticket

The Ticket table must contain:

* id
* title
* description
* priority
* status
* assignee
* created_at
* updated_at

## Comment

The Comment table must contain:

* id
* ticket_id
* content
* author
* created_at

## Relationship

One Ticket can have many Comments.

Each Comment belongs to exactly one Ticket.

The database must enforce the foreign-key relationship.

## Enums

Ticket status:

* OPEN
* IN_PROGRESS
* RESOLVED
* CLOSED
* CANCELLED

Ticket priority:

* LOW
* MEDIUM
* HIGH
* CRITICAL

Persist enum values explicitly as strings.

Do not use ordinal persistence.

## Database

PostgreSQL is mandatory.

Do not introduce H2.

Database schema must be version controlled using the migration mechanism selected for the project.

## IDs

IDs are backend/database generated.

Clients must not control database IDs.

## Timestamps

Use timezone-aware timestamps according to the approved data model.

`created_at` must be set when the record is created.

`updated_at` must represent the latest update.

## Constraints

Implement:

* primary keys
* required NOT NULL fields
* foreign key from comment to ticket
* appropriate column lengths
* appropriate indexes

At minimum consider indexes for:

* ticket.status
* comment.ticket_id

## JPA Rules

* Do not expose entities directly through REST APIs.
* Do not put business rules in entity setters.
* Avoid unnecessary bidirectional relationships.
* Avoid eager loading unless specifically justified.
* Prevent lazy-loading problems from leaking into API serialization.
* Use appropriate entity lifecycle handling for timestamps.

## Repository

Repositories should remain focused on persistence.

Do not implement ticket business rules in repositories.

Do not implement REST logic in repositories.

Do not implement state-machine logic in repositories.

## Testing

Add persistence-focused tests that verify:

1. Ticket can be persisted.
2. Ticket can be retrieved.
3. Comment can be persisted against a ticket.
4. Ticket/comment relationship works.
5. Required fields are enforced appropriately.
6. Status is persisted as its string representation.
7. Priority is persisted as its string representation.
8. Data survives repository operations.
9. Database schema is compatible with PostgreSQL.

Use PostgreSQL-compatible testing.

Do not use H2.

## Out of Scope

Do not implement:

* Ticket REST APIs
* Ticket service
* status transition service
* search service
* comment REST API
* frontend
* authentication
* authorization
* pagination
* Elasticsearch
* Redis
* Kafka
* microservices

## Completion Criteria

The task is complete when:

* PostgreSQL schema is created through version-controlled migration.
* Ticket entity is implemented.
* Comment entity is implemented.
* Repositories are implemented.
* Ticket/comment relationship is verified.
* Enum persistence is verified.
* Persistence tests pass.
* No H2 dependency is introduced.
* No secrets are committed.
* AI-generated code has been reviewed.
* Human review has been completed.

## Human Review

Before committing, manually verify:

* entity fields match `spec/data-model.md`
* database columns match the specification
* enum values are stored as strings
* foreign key is correct
* timestamps are correct
* indexes are reasonable
* no accidental business logic exists in entities
* no unnecessary dependencies were added
* no unrelated features were implemented
