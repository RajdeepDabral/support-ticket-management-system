# Review Specification

## Objective

Review the project specifications for consistency before implementation.

## Specifications to Compare

* spec/requirements.md
* spec/architecture.md
* spec/data-model.md
* spec/api-contract.md
* spec/state-machine.md
* spec/ui-flow.md
* spec/test-strategy.md
* spec/implementation-plan.md

## Check For

* contradictions
* missing requirements
* inconsistent terminology
* API/data-model mismatch
* state-machine mismatch
* UI/API mismatch
* test-strategy gaps
* implementation-plan gaps
* accidental scope expansion
* unclear acceptance criteria
* unnecessary technical complexity

## Important

Do not modify the specifications automatically.

First report the issue and explain which documents conflict.

## Output

Use:

### Consistent Areas

List areas that are aligned.

### Issues

For each issue:

* document
* section
* problem
* related specification
* suggested resolution

### Scope Risks

Identify features that could accidentally expand the assignment.

### Recommended Actions

List only changes that are justified by the specifications.
