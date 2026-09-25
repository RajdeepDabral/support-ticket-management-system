# Dockerize Full Stack — Implementation Specification

**Status:** Implemented  
**Date:** 2026-09-25

---

## 1. Objective

Containerize PostgreSQL, Spring Boot backend, and React frontend so a reviewer can run the complete application with Docker Compose without manual dependency installation.

---

## 2. Docker Architecture

```text
Browser → http://localhost:3000
              │
              ▼
┌─────────────────────────────┐
│  frontend (nginx:1.27)      │
│  - serves React dist/       │
│  - proxies /api/v1 → backend│
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  backend (Spring Boot J21)  │
│  - Flyway on startup        │
│  - REST API :8080           │
└──────────────┬──────────────┘
               │
               ▼
┌─────────────────────────────┐
│  postgres (16-alpine)        │
│  - volume: postgres_data    │
└─────────────────────────────┘
```

---

## 3. PostgreSQL Container

| Setting | Value |
|---------|-------|
| Image | `postgres:16-alpine` |
| Service name | `postgres` |
| Database | `support_ticket` |
| User/password | `support` / `support` (dev defaults, override via `.env`) |
| Host port | `5433` → container `5432` |
| Volume | `postgres_data` |
| Healthcheck | `pg_isready -U support -d support_ticket` |

---

## 4. Spring Boot Backend Container

| Setting | Value |
|---------|-------|
| Build | Multi-stage: `maven:3.9-eclipse-temurin-21` → `eclipse-temurin:21-jre-alpine` |
| Service name | `backend` |
| Port | `8080` |
| JDBC URL (internal) | `jdbc:postgresql://postgres:5432/support_ticket` |
| Flyway | Enabled; runs on startup before Hibernate validate |
| Healthcheck | `curl -sf http://127.0.0.1:8080/api/v1/tickets` |
| Start period | 120s (cold start includes Flyway + JVM) |
| Depends on | `postgres` healthy |

**Important:** Inside the backend container, `localhost` refers to the backend itself. Database hostname must be `postgres`, not `localhost`.

---

## 5. React Frontend Container

| Setting | Value |
|---------|-------|
| Build | Multi-stage: `node:20-alpine` → `nginx:1.27-alpine` |
| Service name | `frontend` |
| Build arg | `VITE_API_BASE_URL=/api/v1` |
| Port | Host `3000` → container `80` |
| Depends on | `backend` healthy |

---

## 6. Docker Networking

All services share the default Compose network. Internal DNS:

- Backend → `postgres:5432`
- Frontend nginx → `backend:8080`
- Browser → `localhost:3000` (never uses Docker service names)

---

## 7. Frontend-to-Backend Communication

| Context | API URL |
|---------|---------|
| Browser (Docker) | Relative `/api/v1` (proxied by nginx) |
| Browser (host dev) | `http://localhost:8080/api/v1` |
| Backend container | N/A (server-side) |

nginx configuration (`frontend/nginx.conf`):

```nginx
location /api/v1/ {
    proxy_pass http://backend:8080/api/v1/;
}
```

---

## 8. Flyway Migration Startup

1. Postgres becomes healthy (`pg_isready`)
2. Backend starts, connects to `postgres:5432`
3. Flyway applies `classpath:db/migration/V1__*.sql`
4. Hibernate validates schema (`ddl-auto: validate`)
5. Backend healthcheck passes on `GET /api/v1/tickets`

---

## 9. Health / Startup Dependencies

```text
postgres (healthy) → backend (healthy) → frontend (healthy)
```

---

## 10. Environment Configuration

See `.env.example`. Key variables:

| Variable | Purpose |
|----------|---------|
| `POSTGRES_DB/USER/PASSWORD` | Database credentials |
| `POSTGRES_PORT` | Host port (default 5433) |
| `SERVER_PORT` | Backend host port (default 8080) |
| `FRONTEND_PORT` | Frontend host port (default 3000) |

---

## 11. Secret Handling

- No real secrets in repository
- Dev defaults in `.env.example` and compose variable defaults
- Production would use external secret management (out of scope)

---

## 12. Verification Commands

```bash
# Start stack
sudo docker compose up --build -d

# Check health (wait up to 3 min on first cold start)
sudo docker compose ps

# Full functional verification
./scripts/verify-docker-stack.sh

# View backend logs if unhealthy
sudo docker compose logs backend
```

---

## 13. Failure Scenarios

| Symptom | Likely cause | Recovery |
|---------|--------------|----------|
| Backend unhealthy | Cold start > healthcheck window | Wait 2–3 min; healthcheck now 120s start_period |
| Backend crash loop | Flyway schema conflict | `docker compose down -v` then rebuild |
| Frontend 502 on API | Backend not healthy | Check `docker compose logs backend` |
| Port in use | Host conflict on 3000/8080/5433 | Change ports in `.env` |

---

## 14. API Path Note

Implementation uses `/api/v1` (see `docs/decisions/api-versioning.md`). nginx proxy targets `/api/v1/`, not `/api/`.

---

## 15. Scope Boundaries

Not included: Kubernetes, auth, H2, microservices, Spring Actuator.
