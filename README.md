# Support Ticket Management System

Specification-driven support ticket management application with a Spring Boot backend, React frontend, and PostgreSQL database.

> **Start here:** See **[RUN.md](RUN.md)** for step-by-step instructions to start the full stack with Docker Compose, test the app, and run verification scripts. Humans and AI agents should use `RUN.md` as the entry point.

## Technology Stack

- **Backend:** Java 21, Spring Boot 3.3.5, Maven, PostgreSQL, Flyway
- **Frontend:** React 19, TypeScript, Vite, React Router
- **API:** REST at `/api/v1/tickets`
- **Containerization:** Docker Compose (PostgreSQL + backend + frontend)
- **API docs:** Swagger UI at `/swagger-ui.html`
- **Ops:** Spring Boot Actuator at `/actuator/health`

## Quick Start (Docker)

Run the complete application stack from the repository root:

```bash
docker compose up --build -d
docker compose ps    # wait until all services are healthy
```

| Service | URL |
|---------|-----|
| Web UI | http://localhost:3000 |
| REST API | http://localhost:8080/api/v1/tickets |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |

Full instructions, smoke tests, and troubleshooting: **[RUN.md](RUN.md)**

The frontend nginx container serves the React app and proxies `/api/v1` to the backend service. The browser uses relative API URLs (`/api/v1`), so it never needs to resolve Docker-internal hostnames.

Stop the stack:

```bash
docker compose down
```

Stop without deleting persisted data (PostgreSQL volume is preserved):

```bash
docker compose stop
```

Remove containers and volumes (deletes database data):

```bash
docker compose down -v
```

Verify the full Dockerized workflow (health checks, API, persistence after restart):

```bash
./scripts/verify-docker-stack.sh
```

## Docker Architecture

```text
Browser → http://localhost:3000
              │
              ▼
┌─────────────────────────────┐
│  frontend (nginx)           │
│  - serves React static files│
│  - proxies /api/v1 → backend│
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  backend (Spring Boot)      │
│  - Flyway migrations        │
│  - REST API on :8080        │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  postgres (PostgreSQL 16) │
│  - persistent volume        │
└─────────────────────────────┘
```

### Ports

| Service  | Container port | Host port (default) |
|----------|----------------|---------------------|
| frontend | 80             | 3000                |
| backend  | 8080           | 8080                |
| postgres | 5432           | 5433                |

### Environment

Copy `.env.example` to `.env` to override defaults. See `frontend/.env.example` for host-based frontend development settings.

## Local Development (without Docker frontend)

### 1. Start PostgreSQL and backend

```bash
docker compose up -d postgres backend
```

### 2. Run backend on host (alternative)

```bash
docker compose up -d postgres
cd backend
mvn spring-boot:run
```

### 3. Run frontend on host

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

Set `VITE_API_BASE_URL=http://localhost:8080/api/v1` in `frontend/.env` when the backend is reachable on the host.

## Tests

### Backend

```bash
cd backend
mvn test
```

Requires Docker for Testcontainers PostgreSQL.

### Frontend

```bash
cd frontend
npm test
npm run build
```

### End-to-end (Playwright)

```bash
docker compose up -d postgres backend
cd frontend
npm run test:e2e
```

## Repository Structure

```
support-ticket-management-ai-sdd/
├── docker-compose.yml
├── backend/              # Spring Boot API
├── frontend/             # React + Vite UI
├── spec/                 # Specifications
└── rules/                # Engineering rules
```

## API Overview

Base path: `/api/v1` (see `docs/decisions/api-versioning.md`)

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/tickets` | Create ticket |
| GET | `/api/v1/tickets` | List, search (`keyword`), filter (`status`) |
| GET | `/api/v1/tickets/{id}` | Ticket details with comments |
| PATCH | `/api/v1/tickets/{id}` | Update fields (not status) |
| PATCH | `/api/v1/tickets/{id}/status` | Status transition |
| POST | `/api/v1/tickets/{id}/comments` | Add comment |

## State Machine

```text
OPEN ──────────→ IN_PROGRESS ──→ RESOLVED ──→ CLOSED
 │                    │
 └→ CANCELLED ←───────┘
```

Valid transitions: OPEN→IN_PROGRESS, OPEN→CANCELLED, IN_PROGRESS→RESOLVED, IN_PROGRESS→CANCELLED, RESOLVED→CLOSED. All others return HTTP 409.

## AI-Assisted Development

This project follows Spec Driven Development:

```text
Requirement → Specification → Plan → Implementation → Testing → Review → Fix
```

| Artifact | Location |
|----------|----------|
| Engineering rules | `rules/` |
| Review commands | `commands/` |
| Documentation skill | `skills/documentation/` |
| Prompt history | `docs/prompt-history.md` |
| Session history | `.specstory/history/` |
| AI review | `docs/ai-review.md` |
| Human review | `docs/human-review.md` |

### Known AI Corrections

1. Rules location: canonical at repo root, not `.cursor/` (see prompt-history Entry 1)
2. Vitest/E2E collision: excluded `e2e/**` from Vitest config
3. Docker healthcheck: increased `start_period` to 120s for Spring Boot cold start

## Docker Troubleshooting

If `support-ticket-backend` is **unhealthy** after `docker compose up`:

```bash
# 1. Check backend logs
sudo docker compose logs backend

# 2. Wait longer on first cold start (Flyway + JVM can take 2–3 minutes)
sudo docker compose ps

# 3. Rebuild and restart
sudo docker compose down
sudo docker compose up --build -d

# 4. If Flyway schema conflict from old volume
sudo docker compose down -v
sudo docker compose up --build -d
```

Verify full workflow:

```bash
./scripts/verify-docker-stack.sh
```

## Specifications

| Document | Path |
|----------|------|
| Requirements | `spec/requirements.md` |
| Architecture | `spec/architecture.md` |
| API Contract | `spec/api-contract.md` |
| State Machine | `spec/state-machine.md` |
| Docker | `spec/dockerize-full-stack.md` |
