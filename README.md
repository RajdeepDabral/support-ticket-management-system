# Support Ticket Management AI SDD

Specification-driven development project for AI-powered support ticket management.

## Current Phase

The project has completed the **specification and AI-guidance phase**. Backend implementation, frontend implementation, automated tests, and Docker setup have **not** started yet.

Next step per `spec/implementation-plan.md`: **Phase 1 — Backend Project Bootstrap**.

## SDD Workflow Status

| Phase | Status |
|-------|--------|
| Requirement | Complete — `spec/requirements.md` |
| Specification | Complete — `spec/architecture.md`, `data-model.md`, `api-contract.md`, `state-machine.md`, `ui-flow.md`, `test-strategy.md` |
| Plan / Tasks | Complete — `spec/implementation-plan.md` |
| Implementation | Not started |
| Testing | Not started |
| Review | Not started |
| Fix | Not started |

## Repository Structure

```
support-ticket-management-ai-sdd/
├── README.md
├── LICENSE
├── .gitignore
├── spec/                 # Approved specifications
├── rules/                # Cursor AI rules (Java/Spring Boot, testing, API standards)
├── commands/             # Cursor commands (review-code, review-spec, generate-tests)
├── skills/               # Cursor skills (documentation)
├── docs/                 # Prompt history and review documentation
├── .specstory/history/   # AI session history (Specstory)
├── .cursor/              # Cursor IDE pointer to project AI guidance
├── backend/              # Scaffold only — implementation not started
└── frontend/             # Scaffold only — implementation not started
```

## Specifications

| Document | Path |
|----------|------|
| Requirements | `spec/requirements.md` |
| Architecture | `spec/architecture.md` |
| Data Model | `spec/data-model.md` |
| API Contract | `spec/api-contract.md` |
| State Machine | `spec/state-machine.md` |
| UI Flow | `spec/ui-flow.md` |
| Test Strategy | `spec/test-strategy.md` |
| Implementation Plan | `spec/implementation-plan.md` |

## AI-Assisted Development Artifacts

| Artifact | Location |
|----------|----------|
| Engineering rules | `rules/java-springboot.md`, `rules/testing.md`, `rules/api-standards.md` |
| Review commands | `commands/review-code.md`, `commands/review-spec.md`, `commands/generate-tests.md` |
| Documentation skill | `skills/documentation/SKILL.md` |
| Prompt history | `docs/prompt-history.md` |
| Session history | `.specstory/history/` |

## Technology Stack (Planned)

- **Backend:** Java 21, Spring Boot, PostgreSQL
- **Frontend:** React / Next.js or equivalent
- **API:** REST
- **Containerization:** Docker Compose (not yet configured)
