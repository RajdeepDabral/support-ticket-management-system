# ADR: API Version Prefix `/api/v1`

**Status:** Accepted  
**Date:** 2026-09-25

## Context

`spec/api-contract.md` and `rules/api-standards.md` originally defined the base path as `/api/tickets` with no version prefix.

During backend bootstrap (Phase 1), the implementation introduced `/api/v1/tickets` in `TicketController.java`.

## Decision

Keep `/api/v1` as the implemented and tested API path. Update documentation to reflect the actual implementation rather than changing working code.

## Consequences

- All backend controllers, integration tests, frontend API client, E2E tests, and Docker nginx proxy use `/api/v1`.
- Specification documents updated with an implementation note; behavioral contract unchanged.
- Future breaking API changes could introduce `/api/v2` without affecting v1 clients.

## Alternatives Considered

1. **Rename to `/api`** — rejected; would require changing all tests, frontend, and Docker config with no functional benefit.
2. **Silent spec-only change** — rejected; violates SDD principle that specs and code must align transparently.
