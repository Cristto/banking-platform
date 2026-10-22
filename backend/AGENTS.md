# Project Agent Directives

## General
- Write all comments, documentation, and technical text in English.
- Follow Clean Architecture:
  - `domain`: models, enums, repository interfaces, domain exceptions.
  - `application`: use cases and application services.
  - `interfaceadapters`: controllers, DTOs, mappers, exception handlers.
  - `infrastructure`: JPA, Kafka, configuration, security, batch, external integrations.
- Use `BigDecimal` for money and constructor injection.
- Keep business transaction boundaries in the application layer.
- Avoid unnecessary interfaces, eager fetching, and in-memory filtering when database queries are appropriate.
- Handle concurrency for balance operations and idempotency for Kafka consumers.

## Implementation
- Before implementing a new use case, inspect an existing, completed use case and its tests.
- Follow its architecture, package structure, naming conventions, validation, exception handling, and testing patterns.
- Adapt the implementation to the new business rules; do not blindly copy code.
- Reuse existing components and avoid unnecessary abstractions.
- Modify only files required by the task. Preserve unrelated code and behavior.

## Git
- Check the working tree before starting.
- If uncommitted changes exist, stop and inform the user.
- Create one commit after completing each use case sub-case.
- Never use destructive Git commands without permission.

## Testing
- Use JUnit 5 and Mockito when needed.
- Follow existing test patterns and verify observable behavior.
- Use Testcontainers when real database or Kafka integration is required.
- Never run tests without explicit permission.
- If tests fail, report the cause before modifying production code.

## Agent Behavior
- Keep changes minimal and focused.
- Do not refactor, reorganize, or inspect unrelated code unnecessarily.
- Do not expand the requested scope.
- Ask before making changes that affect unrelated functionality.