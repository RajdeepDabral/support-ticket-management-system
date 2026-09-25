# Generate Tests

## Objective

Generate focused tests for the selected implementation.

## Before Generating Tests

Read:

* relevant implementation
* spec/requirements.md
* spec/test-strategy.md
* relevant API contract
* relevant state-machine rules

## Requirements

Generate tests for behavior, not implementation details.

Include:

* happy paths
* validation failures
* business-rule failures
* not-found scenarios
* boundary cases
* state-machine cases where relevant

Do not introduce H2.

Use PostgreSQL-compatible integration testing where database behavior is required.

Do not modify production code unless explicitly requested.

## Test Quality

Every generated test must have:

* clear test name
* meaningful setup
* meaningful execution
* meaningful assertions

Avoid tests that only verify mocks were called.

After generating tests, identify any important scenarios that still require human review.

## Output

Provide:

1. Tests generated
2. Requirements covered
3. Important scenarios not covered
4. Potential weaknesses in generated tests
