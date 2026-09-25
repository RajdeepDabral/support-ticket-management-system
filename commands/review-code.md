# Review Code

## Objective

Review the selected code against the approved project specifications and engineering rules.

## Instructions

Before reviewing:

1. Read the relevant specification.
2. Read the relevant architecture rules.
3. Understand the changed code.
4. Check related tests.

Review for:

* correctness
* specification compliance
* architecture violations
* business-rule violations
* state-machine correctness
* validation
* API contract compliance
* persistence issues
* transaction issues
* error handling
* security concerns
* logging concerns
* unnecessary complexity
* duplicated logic
* test quality

Do not modify code during the review unless explicitly requested.

## Output

Report findings using:

### Critical

Issues that can cause incorrect behavior, data corruption, security problems, or major specification violations.

### Major

Important correctness, architecture, API, persistence, or testing issues.

### Minor

Maintainability or code-quality improvements.

### Suggestions

Optional improvements that do not represent defects.

For each finding include:

* file
* relevant code
* issue
* why it matters
* recommended fix

Do not invent problems.

If no issue is found, explicitly state that no significant issue was identified and mention what was reviewed.
