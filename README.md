# Support Ticket Management System

Specification-driven support ticket management application with a Spring Boot backend, React frontend, and PostgreSQL database.

## Technology Stack

- **Backend:** Java 21, Spring Boot 3.3.5, Maven, PostgreSQL, Flyway
- **Frontend:** React 19, TypeScript, Vite, React Router
- **API:** REST at `/api/v1/tickets`
- **Containerization:** Docker Compose (PostgreSQL + backend + frontend)

## Quick Start (Docker)

Run the complete application stack:

```bash
docker compose up --build -d
```

Open the UI at [http://localhost:3000](http://localhost:3000).

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

## Specifications

| Document | Path |
|----------|------|
| Requirements | `spec/requirements.md` |
| Architecture | `spec/architecture.md` |
| API Contract | `spec/api-contract.md` |
| State Machine | `spec/state-machine.md` |
| Docker | `spec/dockerize-full-stack.md` |
