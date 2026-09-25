# AI Prompt History

## Purpose

This document records significant AI-assisted engineering interactions used during development of the Support Ticket Management System.

The goal is to make the AI-assisted development process transparent and reviewable.

Prompt history should complement `.specstory/history/`.

---

## Recording Format

For significant development tasks, record:

### Date

YYYY-MM-DD

### Task

Short description of the engineering task.

### Specification

Relevant specification file(s).

### AI Tool

Cursor / GitHub Copilot / other approved tool.

### Prompt Intent

Short description of what was requested.

### AI Output Summary

Brief summary of the generated approach.

### Human Review

What was checked manually.

### Issue Found

Any incorrect, incomplete, risky, or unnecessary AI suggestion.

### Human Decision

What was accepted, modified, or rejected.

### Tests

Tests executed after the change.

### Commit

Related Git commit.

---

## Entry 1 — Initial Project Scaffolding

### Date

2026-09-25

### Task

Create initial repository structure and root project files.

### Specification

* Assignment baseline (pre-`spec/requirements.md`)

### AI Tool

Cursor

### Prompt Intent

Create mandatory root folders and files (`README.md`, `.gitignore`, `LICENSE`, `frontend/`, `spec/`, `docs/`, `.cursor/`, `.specstory/history/`).

### AI Output Summary

AI created root files and empty directories. A `backend/` Java package skeleton was also created locally.

### Human Review

Verified folder layout against assignment structure. Confirmed no application code was added.

### Issue Found

AI placed rules/commands under `.cursor/` in `README.md` and `spec/architecture.md`, but the later `spec/implementation-plan.md` defines them at repository root (`rules/`, `commands/`, `skills/`). Empty `.cursor/rules/` and `.cursor/commands/` directories were created while actual content was added at root level.

### Human Decision

Accepted root-level `rules/`, `commands/`, and `skills/` per `implementation-plan.md`. Updated `README.md` and `spec/architecture.md` §29 to document the canonical locations. Added `.cursor/README.md` as a pointer.

### Tests

None — documentation/scaffolding only.

### Commit

`33f782b`

---

## Entry 2 — Specification Phase

### Date

2026-09-25

### Task

Author the full specification set and implementation plan.

### Specification

* `spec/requirements.md`
* `spec/architecture.md`
* `spec/data-model.md`
* `spec/api-contract.md`
* `spec/state-machine.md`
* `spec/ui-flow.md`
* `spec/test-strategy.md`
* `spec/implementation-plan.md`

### AI Tool

Cursor

### Prompt Intent

Define requirements, architecture, data model, API contract, state machine, UI flow, test strategy, and phased implementation plan before any application code.

### AI Output Summary

AI produced eight specification documents following the SDD sequence: Requirement → Specification → Plan. State machine defined as explicit allow-list with 5 valid transitions and terminal states `CLOSED` and `CANCELLED`.

### Human Review

Cross-checked state transitions across `requirements.md`, `state-machine.md`, and `api-contract.md`. Verified API endpoints match UI flow. Confirmed PostgreSQL-only policy and no H2. Verified no authentication scope creep.

### Issue Found

`spec/requirements.md` footer still said "Ready for Architecture Specification" after all downstream specs were complete.

### Human Decision

Updated requirements status to "Specifications complete — ready for Phase 1". Left implementation and test traceability columns as `TBD` until code exists.

### Tests

None — specification documents only.

### Commit

`1a7d599` through `b0584f7`

---

## Entry 3 — AI Guidance Artifacts

### Date

2026-09-25

### Task

Create reusable AI rules, commands, skills, and prompt-history template.

### Specification

* `spec/architecture.md` §29
* `spec/implementation-plan.md` §26

### AI Tool

Cursor

### Prompt Intent

Populate `rules/`, `commands/`, `skills/documentation/`, `docs/prompt-history.md`, and `.specstory/history/README.md`.

### AI Output Summary

AI created `rules/java-springboot.md`, `rules/testing.md`, `rules/api-standards.md`, three command files, and a documentation skill. Content aligns with approved specifications.

### Human Review

Verified rules reference `spec/api-contract.md` and state-machine allow-list. Verified `generate-tests.md` prohibits H2. Verified no secrets in any file.

### Issue Found

`.gitignore` excluded `.specstory/history/*.jsonl`, which would prevent committing required session history evidence.

### Human Decision

Removed the `.specstory/history/*.jsonl` ignore rule. Session history should be committable; secrets remain excluded via general guidelines.

### Tests

None — AI guidance artifacts only.

### Commit

`d5170e3` (content finalized in follow-up correction)

---

## Entry 4 — Backend API Implementation

### Date

2026-09-25

### Task

Implement backend REST APIs, state machine, persistence, and integration tests (Phases 1–12).

### Specification

* `spec/create-ticket-api.md` through `spec/ticket-search-filter.md`
* `spec/state-machine.md`, `spec/database-persistence.md`

### AI Tool

Cursor

### Prompt Intent

Incremental backend tasks: domain model, Flyway migrations, ticket CRUD, status transitions, comments, search/filter, acceptance tests.

### AI Output Summary

Spring Boot 3.3.5 backend with `/api/v1/tickets`, `TicketStateTransitions` allow-list, Testcontainers PostgreSQL integration tests, `SupportTicketBackendAcceptanceTest`.

### Human Review

Verified state machine matches spec (5 valid transitions). Confirmed `UpdateTicketRequestDeserializer` blocks status field on PATCH. No H2 introduced.

### Issue Found

None blocking. API path `/api/v1` diverges from spec `/api` — documented in `docs/decisions/api-versioning.md`.

### Human Decision

Accepted `/api/v1` as stable implementation path. All tests and frontend aligned.

### Tests

`mvn test` (134 tests with Docker/Testcontainers).

### Commit

`ab8d0ae` through `22ade3a`

---

## Entry 5 — Frontend UI Implementation

### Date

2026-09-25

### Task

Implement React frontend: list, create, details/edit, comments, status transitions (Phases 13–17).

### Specification

* `spec/frontend-bootstrap.md`, `spec/ticket-list-ui.md`, `spec/create-ticket-ui.md`
* `spec/ticket-details-edit-ui.md`, `spec/ticket-comments-ui.md`, `spec/status-transition-ui.md`

### AI Tool

Cursor

### Prompt Intent

Bootstrap Vite/React app, implement ticket management screens with configurable `VITE_API_BASE_URL`.

### AI Output Summary

React 19 + TypeScript + Vite frontend with pages, API client, status action buttons, error handling. 87 Vitest unit tests.

### Human Review

Verified UI calls backend API; frontend validation is supplementary. Status transitions use dedicated endpoint.

### Issue Found

Vitest initially picked up Playwright E2E files in `frontend/e2e/`, causing test failures.

### Human Decision

Added `exclude: ['e2e/**']` to `vitest.config.ts`. E2E runs separately via `npm run test:e2e`.

### Tests

`npm test` (87/87), `npm run build`.

### Commit

`0492cdb` through `123a1f0`

---

## Entry 6 — E2E Acceptance Testing

### Date

2026-09-25

### Task

Add Playwright E2E tests for primary workflow, search/filter, validation, negative transitions, persistence.

### Specification

* `spec/e2e-acceptance-testing.md`, `spec/test-strategy.md`

### AI Tool

Cursor

### Prompt Intent

Single E2E framework (Playwright), real API against Docker backend, unique test data per run.

### AI Output Summary

7 E2E spec files, helpers, `playwright.config.ts` with `workers: 1`, global setup waiting for backend.

### Human Review

Confirmed E2E hits real backend (no mocking). Negative transitions assert 409 via Playwright `request` API.

### Issue Found

Some acceptance criteria (browser refresh, UI 409) not yet automated — documented in QA review.

### Human Decision

Accepted current E2E coverage; backend integration tests cover remaining gaps.

### Tests

`npm run test:e2e` (requires `docker compose up -d postgres backend`).

### Commit

`07d335a`

---

## Entry 7 — Full-Stack Dockerization

### Date

2026-09-25

### Task

Containerize frontend, extend Docker Compose, add nginx reverse proxy, verification script.

### Specification

* `spec/dockerize-full-stack.md`, `spec/architecture.md` §22

### AI Tool

Cursor

### Prompt Intent

Docker Compose with postgres + backend + frontend. Browser uses relative `/api/v1` proxied by nginx.

### AI Output Summary

`frontend/Dockerfile` (Node build + nginx), `nginx.conf`, updated `docker-compose.yml`, `scripts/verify-docker-stack.sh`.

### Human Review

Verified backend JDBC uses `postgres:5432` (not localhost). Frontend build arg `VITE_API_BASE_URL=/api/v1`.

### Issue Found

Backend marked unhealthy on first cold start — healthcheck `start_period: 40s` too short for Spring Boot + Flyway.

### Human Decision

Increased healthcheck `start_period` to 120s, retries to 15, added `restart: unless-stopped`.

### Tests

`docker compose config`, `docker compose up --build -d`, `./scripts/verify-docker-stack.sh`.

### Commit

`77d389f` and follow-up healthcheck fix

---

## Guidelines

Do not copy every trivial autocomplete interaction into this document.

Record significant AI-assisted engineering decisions, especially:

* architecture
* database
* API implementation
* state machine
* testing
* refactoring
* AI review
* defects discovered in AI-generated code

Never record:

* passwords
* API keys
* access tokens
* private credentials
* sensitive personal information
