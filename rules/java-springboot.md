# Java 21 + Spring Boot Engineering Rules

## Purpose

These rules define the engineering standards for AI-assisted development of the Support Ticket Management System.

AI-generated code must follow the approved project specifications and architecture. AI suggestions are recommendations and must be reviewed by the developer before acceptance.

---

## Technology Standards

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate/JPA
* PostgreSQL
* Maven or Gradle according to the approved project setup
* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc or equivalent REST testing
* Testcontainers PostgreSQL where appropriate

Do not introduce alternative technologies without reviewing the relevant specification.

---

## Architecture

Follow the approved modular-monolith architecture.

Recommended dependency direction:

Controller
→ Service/Application
→ Domain/Business Rules
→ Repository
→ PostgreSQL

Controllers must remain thin.

Controllers should:

* receive HTTP requests
* validate/request-bind DTOs
* invoke application services
* return appropriate HTTP responses

Controllers must not contain business rules.

---

## DTO Boundary

Do not expose JPA entities directly through REST APIs.

Use request and response DTOs at API boundaries.

Do not allow API clients to directly control fields that are backend-owned.

Examples:

* Ticket ID is backend-generated.
* Ticket creation always starts with OPEN.
* Ticket status transitions are controlled by the state machine.
* Created/updated timestamps are backend-controlled.

---

## Business Rules

Business rules must be implemented in the appropriate service/domain layer.

The ticket state machine is a backend business rule.

Valid transitions are:

OPEN → IN_PROGRESS
OPEN → CANCELLED
IN_PROGRESS → RESOLVED
IN_PROGRESS → CANCELLED
RESOLVED → CLOSED

CLOSED and CANCELLED are terminal states.

The frontend may hide invalid actions, but the backend must independently reject invalid transitions.

Never rely on frontend validation for business-rule enforcement.

---

## Validation

Use Bean Validation or the project's approved validation mechanism.

Validate incoming API requests at the backend.

Validation must cover required fields, allowed enum values, and meaningful field constraints.

Do not duplicate business rules unnecessarily across controllers and services.

---

## Persistence

PostgreSQL is the authoritative persistence database.

Do not introduce H2.

Do not replace PostgreSQL with an in-memory database for production or integration behavior.

Use JPA/Hibernate appropriately.

Avoid exposing persistence implementation details through REST APIs.

Database schema changes must be version controlled using the project's selected migration mechanism.

---

## Transactions

Place transaction boundaries at the service/application layer where appropriate.

State transitions and related persistence changes must execute atomically.

Do not add transactions blindly to every method.

---

## Error Handling

Use centralized exception handling.

REST errors must follow the approved API error contract.

Do not return stack traces to clients.

Do not expose sensitive internal implementation details.

Expected business conflicts such as invalid state transitions should return the documented conflict response.

---

## Logging

Logs should provide useful diagnostic information without exposing:

* passwords
* credentials
* access tokens
* database secrets
* sensitive configuration values

Avoid excessive logging.

Use appropriate log levels.

---

## Code Quality

Prefer:

* clear naming
* small focused methods
* single responsibility
* immutable values where appropriate
* constructor injection
* explicit business rules
* meaningful exceptions
* readable code

Avoid:

* unnecessary abstractions
* speculative frameworks
* premature optimization
* generic utility classes without a clear purpose
* deeply nested conditionals
* duplicated business logic

Do not introduce microservices, Kafka, Redis, Elasticsearch, CQRS, event sourcing, or other infrastructure unless the specification is explicitly changed.

---

## AI-Assisted Development

AI-generated code must be reviewed before being committed.

For every significant AI-generated change:

1. Read the relevant specification.
2. Review the generated code.
3. Check architecture boundaries.
4. Check business rules.
5. Run relevant tests.
6. Review generated tests.
7. Fix issues identified during human review.
8. Commit the reviewed change.

Never accept AI output solely because it compiles.

---

## Human Engineering Judgment

The developer remains responsible for:

* architectural decisions
* business-rule correctness
* security
* database design
* test quality
* API behavior
* final code quality

When AI proposes multiple approaches, evaluate them against the approved specifications rather than automatically selecting one.

---

## Scope Control

Do not implement functionality that is not present in the approved specifications without first updating the specification and implementation plan.

The specification is the source of truth.
