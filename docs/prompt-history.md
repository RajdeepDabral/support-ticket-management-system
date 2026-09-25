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
