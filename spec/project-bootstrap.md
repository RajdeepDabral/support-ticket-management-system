# Project Bootstrap

## Objective

Bootstrap the Support Ticket Management System backend using Java 21, Spring Boot, Maven, and PostgreSQL.

This task establishes the foundation for future implementation without implementing ticket business functionality.

## References

The implementation must follow:

* `spec/requirements.md`
* `spec/architecture.md`
* `spec/data-model.md`
* `spec/implementation-plan.md`
* `rules/java-springboot.md`
* `rules/testing.md`
* `rules/api-standards.md`

## Scope

### Include

* Java 21
* Spring Boot
* Maven
* Spring Web
* Spring Data JPA
* PostgreSQL driver
* Bean Validation
* Actuator if justified
* Basic application configuration
* PostgreSQL Docker Compose service
* Backend Dockerfile
* Basic application startup test
* PostgreSQL connectivity verification
* `.gitignore`
* basic README setup information

### Do Not Include

* Ticket entity
* Comment entity
* Ticket repository
* Ticket service
* Ticket controller
* Ticket state machine
* Ticket REST APIs
* frontend functionality
* authentication
* authorization
* Kafka
* Redis
* Elasticsearch
* microservices
* unnecessary infrastructure

## Package Structure

Use a clean package structure that can evolve with the approved architecture.

Example:

```text
com.<company>.supportticket
├── SupportTicketApplication
├── config
├── common
│   ├── exception
│   └── response
├── ticket
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
└── comment
    ├── controller
    ├── dto
    ├── entity
    ├── repository
    └── service
```

Empty packages do not need to be created until their implementation is required.

## Database

Use PostgreSQL.

Do not use H2.

PostgreSQL configuration must be externalized through environment variables or application configuration.

Do not commit credentials.

## Verification

The following must work:

1. Backend starts successfully.
2. PostgreSQL starts successfully through Docker Compose.
3. Backend can establish a database connection.
4. Application context loads successfully.
5. Basic automated test passes.
6. No secrets are committed.

## Completion Criteria

Task is complete when:

* Java 21 is confirmed.
* Spring Boot application starts.
* PostgreSQL container starts.
* Backend connects to PostgreSQL.
* Test suite passes.
* Docker configuration is valid.
* No ticket functionality has been prematurely implemented.
* Changes have been manually reviewed.

## Human Review

Before committing:

* inspect generated dependencies
* inspect application configuration
* inspect Docker configuration
* verify PostgreSQL is actually being used
* verify no H2 dependency exists
* verify no unnecessary framework was introduced
* verify secrets are not committed
