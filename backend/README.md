# Backend

Spring Boot backend for the Support Ticket Management System.

## Status

**Phase 1 — Project Bootstrap** complete.
**Phase 2 — Database and Persistence** complete.
**Ticket Domain and Business Service** complete.
**Ticket State Machine** complete.
**REST API Foundation — Create Ticket** complete (`POST /api/v1/tickets`).
**List Tickets and View Ticket Details** complete (`GET /api/v1/tickets`, `GET /api/v1/tickets/{ticketId}`).
**Update Ticket** complete (`PATCH /api/v1/tickets/{ticketId}`).

**Status Transition** complete (`PATCH /api/v1/tickets/{ticketId}/status`).

Comment creation, search, filtering, and frontend are not implemented yet.

## Technology

- Java 21
- Spring Boot 3.3.5
- Maven
- PostgreSQL

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (for PostgreSQL and integration tests)

## Local Development

### 1. Start PostgreSQL

From the repository root:

```bash
docker compose up -d postgres
```

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

Configuration is loaded from `src/main/resources/application.yml` and can be overridden with environment variables (see `../.env.example`).

### 3. Run tests

```bash
cd backend
mvn test
```

Integration tests use Testcontainers with PostgreSQL. Docker must be available.

## Docker

Build and run backend + PostgreSQL:

```bash
docker compose up --build
```

## Package Structure

```text
com.supportticket
├── domain          # Ticket, Comment, TicketStatus, TicketPriority
├── repository      # TicketRepository, CommentRepository
├── service         # TicketService (create, get, update, status transition)
├── state           # TicketStateTransitions, TicketStateTransitionValidator
├── dto             # CreateTicketRequest, UpdateTicketCommand, TicketResponse
├── mapper          # TicketMapper
├── exception       # TicketNotFoundException, InvalidRequestException, InvalidStatusTransitionException
├── config
├── controller      # TicketController (POST /api/v1/tickets)
├── dto
├── exception
└── mapper
```

## Database Migrations

Flyway migrations live in `src/main/resources/db/migration/`.

Current schema:

* `ticket` — support tickets
* `comment` — comments linked to tickets via `ticket_id` foreign key
