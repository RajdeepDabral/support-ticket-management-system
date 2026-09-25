# Documentation Skill

## Purpose

Create and maintain clear technical documentation for the Support Ticket Management System.

## Documentation Principles

Documentation must be:

* accurate
* concise
* based on the actual implementation
* consistent with specifications
* easy for another engineer to follow

Never claim that a feature exists unless it has been verified in the implementation.

---

## README Documentation

The project README should eventually contain:

1. Project overview
2. Architecture
3. Technology stack
4. Project structure
5. Prerequisites
6. Local setup
7. PostgreSQL setup
8. Configuration
9. Running backend
10. Running frontend
11. Running tests
12. Docker setup
13. API overview
14. State machine
15. AI-assisted development workflow
16. Human review process
17. Known AI mistakes and corrections
18. Submission verification

---

## AI Development Documentation

Document meaningful AI-assisted decisions.

For important AI-generated changes record:

* task
* prompt intent
* generated approach
* human review
* issue identified
* final decision
* tests performed

Do not record secrets, credentials, tokens, or private information.

---

## Architecture Documentation

Keep architecture documentation aligned with:

`spec/architecture.md`

If implementation intentionally differs from the specification, document why and update the appropriate specification.

---

## Review Findings

AI review findings must not be hidden.

Meaningful defects discovered through AI or human review should be documented when relevant to the engineering process.

---

## Accuracy Rule

Documentation must describe the implemented system, not an imagined future system.
