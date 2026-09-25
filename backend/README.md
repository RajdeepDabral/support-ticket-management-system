# Backend

Spring Boot backend for the Support Ticket Management System.

## Status

**Phase 1 — Project Bootstrap** complete.

Business functionality (tickets, comments, state machine, REST APIs) is not implemented yet.

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
├── config
├── controller
├── service
├── domain
├── repository
├── dto
├── exception
└── mapper
```
