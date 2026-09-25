# How to Run the Application

Use this guide to start and test the **complete** Support Ticket Management System (PostgreSQL + backend + frontend).

## Prerequisites

- Docker and Docker Compose
- Git

Optional for local (non-Docker) development: Java 21, Maven 3.9+, Node.js 20+.

## Start the Full Stack (recommended)

From the **repository root**:

```bash
docker compose up --build -d
```

Wait until all services are healthy (first start may take 2–3 minutes):

```bash
docker compose ps
```

Expected:

| Container | Status |
|-----------|--------|
| `support-ticket-postgres` | healthy |
| `support-ticket-backend` | healthy |
| `support-ticket-frontend` | healthy |

## Application URLs

| What | URL |
|------|-----|
| **Web UI** | http://localhost:3000 |
| **REST API** | http://localhost:8080/api/v1/tickets |
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/api-docs |
| **Health check** | http://localhost:8080/actuator/health |

The UI proxies API calls through nginx at `/api/v1` on port 3000.

## Quick Smoke Test

```bash
# Backend health
curl -s http://localhost:8080/actuator/health

# List tickets (empty on fresh DB)
curl -s http://localhost:8080/api/v1/tickets

# Create a ticket
curl -s -X POST http://localhost:8080/api/v1/tickets \
  -H 'Content-Type: application/json' \
  -d '{"title":"Test ticket","description":"Smoke test","priority":"HIGH","assignee":"reviewer"}'

# Open UI in browser
# http://localhost:3000
```

## Automated Verification

Full workflow test (create, update, comment, transitions, search, persistence):

```bash
./scripts/verify-docker-stack.sh
```

## Run Tests

```bash
# Backend (requires Docker for Testcontainers)
cd backend && mvn test

# Frontend unit tests
cd frontend && npm test

# End-to-end (start backend first)
docker compose up -d postgres backend
cd frontend && npm run test:e2e
```

## Stop the Stack

```bash
# Stop containers, keep database data
docker compose stop

# Stop and remove containers, keep volume
docker compose down

# Stop and delete all data
docker compose down -v
```

## Troubleshooting

```bash
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
```

If backend is unhealthy after rebuild, wait 2–3 minutes or run:

```bash
docker compose down
docker compose up --build -d
```

See `README.md` for architecture details and `spec/dockerize-full-stack.md` for Docker design.
