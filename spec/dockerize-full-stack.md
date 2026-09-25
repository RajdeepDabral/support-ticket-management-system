Create `dockerize-full-stack.md` for the Support Ticket Management System.

Do NOT modify application code yet.

Read and inspect:

* `spec/requirements.md`
* `spec/architecture.md`
* `spec/data-model.md`
* `spec/api-contract.md`
* `spec/implementation-plan.md`
* existing backend configuration
* existing frontend configuration
* existing Docker files, if any
* Gradle configuration
* frontend package configuration
* Flyway configuration
* current API base URL configuration
* `.gitignore`
* existing environment/configuration files

Create a concise implementation specification for containerizing the complete application.

The specification must cover:

1. Objective
2. Docker architecture
3. PostgreSQL container
4. Spring Boot backend container
5. React frontend container
6. Docker networking
7. PostgreSQL persistent volume
8. Backend database configuration
9. Frontend-to-backend API communication
10. Flyway migration startup
11. Health/startup dependencies
12. Environment configuration
13. Secret handling
14. Docker build strategy
15. Frontend production serving strategy
16. API proxy/routing strategy if applicable
17. Local development compatibility
18. Verification commands
19. Dockerized acceptance tests
20. Failure scenarios
21. Security considerations
22. Scope boundaries

Important constraints:

* Java 21
* Spring Boot 3.x
* PostgreSQL only
* No H2
* Frontend remains React/Next.js/equivalent based on the existing implementation
* Backend remains the authority for business rules and state transitions
* Do not introduce unnecessary infrastructure
* Do not introduce Kubernetes
* Do not introduce microservices
* Do not introduce authentication unless already implemented
* Do not hardcode secrets
* Docker Compose should be sufficient for local/full-stack execution

Pay special attention to the difference between:

* `localhost` from inside a container
* Docker service names
* `localhost` from the user's browser

The specification must explicitly explain how the browser reaches the backend API.

Also identify any existing `/api` vs `/api/v1` inconsistency between the API specification and current implementation and flag it for deliberate resolution rather than silently changing it.

Do not modify source code, Dockerfiles, compose files, or configuration files yet.
