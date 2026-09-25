# Testing Engineering Rules

## Purpose

These rules define how tests must be generated, reviewed, and maintained for the Support Ticket Management System.

Tests must verify behavior rather than merely increase coverage numbers.

---

## Testing Principles

Tests must be:

* deterministic
* readable
* focused
* meaningful
* maintainable
* independent where practical

A passing test is not automatically a good test.

AI-generated tests must be reviewed by the developer.

---

## Approved Testing Stack

Use:

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc or equivalent
* Spring Data JPA testing
* PostgreSQL
* Testcontainers PostgreSQL where appropriate

Do not introduce H2.

---

## Unit Tests

Use unit tests for isolated business logic.

Examples:

* state transition validation
* service-level business rules
* validation-related logic
* mapping logic where meaningful

Mock dependencies only when isolation provides value.

Do not mock the class under test.

---

## State Machine Tests

The state machine is business-critical and must have explicit tests.

Test all valid transitions:

* OPEN → IN_PROGRESS
* OPEN → CANCELLED
* IN_PROGRESS → RESOLVED
* IN_PROGRESS → CANCELLED
* RESOLVED → CLOSED

Also test invalid transitions, including:

* CLOSED → OPEN
* CLOSED → IN_PROGRESS
* CANCELLED → OPEN
* RESOLVED → OPEN
* same-state transitions

Invalid transitions must not modify the ticket state.

---

## REST Integration Tests

REST integration tests should verify:

* HTTP status
* response body
* validation behavior
* error response
* persistence behavior
* state transition behavior
* search behavior
* filter behavior
* comment behavior

Do not verify only that an endpoint returns HTTP 200.

---

## PostgreSQL Integration

Database-dependent tests must use PostgreSQL-compatible behavior.

Prefer Testcontainers PostgreSQL for integration tests when appropriate.

Do not make tests pass by relying on H2-specific behavior.

---

## Test Quality

Every test should answer a meaningful question.

Weak test:

> Verify that a service method was called once.

Prefer behavior-focused verification:

> Verify that an invalid status transition returns a conflict and the persisted status remains unchanged.

Avoid tests that only duplicate implementation details.

---

## Negative Testing

For important functionality, test both success and failure paths.

Examples:

* missing required title
* blank description
* invalid priority
* invalid status
* unknown ticket ID
* invalid state transition
* invalid comment request
* search with no matches

---

## AI-Generated Tests

When AI generates tests:

1. Compare tests against `spec/test-strategy.md`.
2. Check whether important acceptance criteria are covered.
3. Check assertions for meaningful behavior.
4. Check negative cases.
5. Remove redundant tests.
6. Add missing cases manually.
7. Run the full relevant test suite.

Do not accept generated tests simply because they pass.

---

## Regression Protection

Whenever a bug is discovered:

1. Reproduce the issue.
2. Add or update a test that demonstrates the expected behavior.
3. Fix the implementation.
4. Run the regression test.
5. Run the broader relevant test suite.

---

## Definition of Test Completion

Testing is considered complete only when:

* critical business rules are covered
* state-machine transitions are covered
* invalid transitions are covered
* REST validation is covered
* persistence behavior is verified
* search/filter behavior is verified
* comments are verified
* important error paths are verified
* AI-generated tests have received human review
