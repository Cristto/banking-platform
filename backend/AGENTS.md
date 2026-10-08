# Project Agent Directives

- All comments, documentation, and generated technical text must be written in English.
- Use Clean Architecture with these package layers:
  - `domain`: models, enums, repository interfaces, domain exceptions
  - `application`: use cases and application services
  - `interfaceadapters`: controllers, request/response DTOs, API mappers, exception handlers
  - `infrastructure`: JPA implementations, Kafka, configuration, security, batch, external integrations
- Do not create unnecessary interfaces between controllers and application use cases.
- Do not expose JPA entities directly from controllers.
- Use `BigDecimal` for monetary values.
- Prefer constructor injection.
- Keep transaction boundaries in the application layer when they represent complete business operations.
- Use database queries properly when needed: JOIN, GROUP BY, projections, pagination, JPQL, Specifications, EntityGraph, etc. Do not replace database work with `findAll()` plus in-memory filtering when the query belongs in the database.
- Avoid `FetchType.EAGER` unless clearly justified.
- Consider concurrency and locking for balance-changing operations.
- Kafka must be used for asynchronous events, not as a replacement for the PostgreSQL transaction that changes balances.
- Kafka consumers must tolerate duplicate messages when required.

## Existing code protection

- Never delete, rename, move, replace, or substantially modify existing code unless required by the requested task.
- Modify only files directly related to the current task.
- Do not perform unrelated refactoring, cleanup, formatting, or package reorganization.
- Before modifying shared files, inspect their current contents and preserve unrelated behavior.
- If a required change would affect unrelated code, explain it before making the change.
- Never use destructive Git or filesystem commands unless explicitly requested.

## Task workflow

- Before implementing a new use case, inspect the Git working tree.
- A new use case must start from a committed state.
- If there are uncommitted changes, stop and inform the user before starting.
- When the requested use case is finished, use the project's commit skill to create the final commit.
- Do not create intermediate commits unless explicitly requested.

## Testing

- Use JUnit 5.
- Use Mockito only when dependencies need isolation.
- Unit tests should verify observable behavior, not implementation details.
- Integration tests must not call real third-party APIs.
- Use Testcontainers when real PostgreSQL or Kafka integration is relevant.
- Never run tests without explicit user permission.
- If an authorized test fails, analyze and report the cause first.
- If the failure reveals a production bug, explain the bug and proposed fix, then wait for explicit approval before modifying production code.

## Comments

- Comment only non-obvious decisions or important logic.
- Do not comment trivial imports, annotations, getters, setters, or self-explanatory code.
- Useful comments include transaction behavior, concurrency, locking, Kafka/idempotency, complex queries, Batch logic, and important test behavior.

## Agent behavior

- Keep changes small and focused.
- Do not inspect unrelated files without a concrete reason.
- Do not fix unrelated warnings or errors.
- Avoid repeated searches or exploration loops.
- Do not expand the task beyond what the user requested.