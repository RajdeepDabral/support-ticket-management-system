# AI Review — Support Ticket Management System

**Date:** 2026-09-25  
**Reviewer:** AI-assisted review (Cursor) + human validation  
**Scope:** Architecture, specifications, API, state machine, tests, security, Docker

---

## 1. Architecture Compliance

| Check | Result | Evidence |
|-------|--------|----------|
| Layered monolith (controller → service → repository) | **Pass** | `backend/src/main/java/com/supportticket/` package structure |
| PostgreSQL only, no H2 | **Pass** | `pom.xml`, `AbstractPostgreSQLContainerTest.java` |
| Flyway migrations, Hibernate validate | **Pass** | `application.yml`, `V1__create_ticket_and_comment_tables.sql` |
| DTOs at API boundary | **Pass** | `dto/` package, `TicketMapper` |
| Business rules in service layer | **Pass** | `TicketServiceImpl`, `CommentServiceImpl` |
| Docker Compose (frontend + backend + postgres) | **Pass** | `docker-compose.yml`, `frontend/Dockerfile`, `backend/Dockerfile` |
| nginx reverse proxy for browser API access | **Pass** | `frontend/nginx.conf` proxies `/api/v1/` → `backend:8080` |

**Finding:** No microservices, auth, or unnecessary infrastructure introduced.

---

## 2. Specification Compliance

| Area | Result | Notes |
|------|--------|-------|
| Ticket CRUD | **Pass** | All endpoints implemented |
| Comments | **Pass** | `POST /api/v1/tickets/{id}/comments` |
| Search + status filter | **Pass** | `GET /api/v1/tickets?keyword=&status=` |
| State machine | **Pass** | Matches `spec/state-machine.md` allow-list |
| No authentication | **Pass** | Not implemented (per scope) |
| API path `/api/v1` vs spec `/api` | **Documented deviation** | See `docs/decisions/api-versioning.md` |

---

## 3. API Review

| Endpoint | Method | Status |
|----------|--------|--------|
| `/api/v1/tickets` | POST | Pass — 201, validation |
| `/api/v1/tickets` | GET | Pass — list, search, filter |
| `/api/v1/tickets/{id}` | GET | Pass — 404 on missing |
| `/api/v1/tickets/{id}` | PATCH | Pass — status field blocked via deserializer |
| `/api/v1/tickets/{id}/status` | PATCH | Pass — dedicated transition endpoint |
| `/api/v1/tickets/{id}/comments` | POST | Pass — validation |

**Error format:** Consistent `ApiErrorResponse` via `GlobalExceptionHandler` (400, 404, 409, 500).

---

## 4. State Machine Review

**Authoritative implementation:** `TicketStateTransitions.java`

| Valid transition | Enforced |
|------------------|----------|
| OPEN → IN_PROGRESS | Yes |
| OPEN → CANCELLED | Yes |
| IN_PROGRESS → RESOLVED | Yes |
| IN_PROGRESS → CANCELLED | Yes |
| RESOLVED → CLOSED | Yes |

**Invalid transitions:** Rejected with HTTP 409 `INVALID_STATUS_TRANSITION`; status unchanged in DB.

**Status bypass prevention:** `UpdateTicketRequestDeserializer` rejects `status` field on PATCH update.

**Test coverage:**
- `TicketStateTransitionValidatorTest` — 25 cases
- `TicketStatusTransitionIntegrationTest` — integration with PostgreSQL
- `SupportTicketBackendAcceptanceTest.StateMachine` — acceptance matrix
- `e2e/negative-transitions.spec.ts` — API-level E2E negatives

---

## 5. Test Quality Review

| Layer | Count | Assessment |
|-------|-------|------------|
| Backend unit/controller | 124+ | Focused, behavior-driven |
| Backend integration (Testcontainers) | 10 classes | Real PostgreSQL, no H2 |
| Frontend Vitest | 87 | Mocked API, error scenarios covered |
| Playwright E2E | 7 specs | Primary workflow, search, validation, persistence |

**Known E2E gaps (non-blocking):** browser refresh, UI-level 409, all 5 status filters in E2E (backend acceptance covers filters).

---

## 6. Security Findings

| Check | Result |
|-------|--------|
| No secrets in source control | Pass — `.env` gitignored |
| Dev credentials in `.env.example` only | Pass — documented defaults |
| No stack traces exposed to users | Pass — structured error responses |
| No auth bypass vectors | Pass — N/A (no auth) |
| CORS | Note — not configured; Docker uses same-origin nginx proxy |

---

## 7. AI-Generated Issues Identified and Corrected

### Issue 1 — Rules location mismatch (pre-implementation)

- **AI suggestion:** Place rules under `.cursor/`
- **Problem:** `implementation-plan.md` defines root-level `rules/`, `commands/`, `skills/`
- **Fix:** Canonical content at repo root; `.cursor/README.md` as pointer
- **Evidence:** `docs/prompt-history.md` Entry 1

### Issue 2 — `.gitignore` blocked Specstory history

- **AI suggestion:** Exclude `.specstory/history/*.jsonl`
- **Problem:** Assignment requires committable session history
- **Fix:** Removed jsonl exclusion from `.gitignore`
- **Evidence:** `docs/prompt-history.md` Entry 3

### Issue 3 — Vitest picked up Playwright E2E files

- **AI implementation:** E2E specs co-located under `frontend/e2e/`
- **Problem:** `vitest` attempted to run Playwright tests, causing failures
- **Fix:** Added `exclude: ['e2e/**']` in `frontend/vitest.config.ts`
- **Verification:** 87/87 frontend unit tests pass

### Issue 4 — Docker backend healthcheck too aggressive

- **AI implementation:** `start_period: 40s`, 10 retries
- **Problem:** Spring Boot + Flyway cold start exceeds 40s on first container boot; backend marked unhealthy
- **Fix:** Increased `start_period` to 120s, retries to 15, added `restart: unless-stopped`
- **Verification:** See Docker verification section in `README.md`

---

## 8. Docker Verification

Run after starting the stack:

```bash
sudo docker compose up --build -d
./scripts/verify-docker-stack.sh
```

If backend is unhealthy, check logs:

```bash
sudo docker compose logs backend
```

Common recovery:

```bash
sudo docker compose down
sudo docker compose up --build -d
# wait up to 3 minutes for first cold start
sudo docker compose ps
```

If Flyway schema conflict from old volume:

```bash
sudo docker compose down -v   # deletes DB data
sudo docker compose up --build -d
```

---

## 9. Review Conclusion

The implementation is **specification-compliant** with one documented API versioning deviation (`/api/v1`). State machine enforcement is correct and well-tested. Docker architecture is sound; healthcheck timing was adjusted for cold-start reliability. No business logic changes were required for compliance.

**Recommended human follow-up:** Run `./scripts/verify-docker-stack.sh` and attach output to submission evidence.
