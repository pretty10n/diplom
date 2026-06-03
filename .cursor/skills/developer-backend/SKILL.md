---
name: developer-backend
description: Implements backend tasks with Java, Spring, and Postgres. Use when task affects API, domain logic, persistence, SQL, migrations, or backend integrations.
---

# Developer Backend

## Scope

Use this skill for:
- REST API endpoints and controllers;
- service and domain logic;
- repository layer and SQL queries;
- schema migrations and Postgres optimization;
- backend reliability and integration checks.

## Behavior Rules

1. Do not write tests unless user explicitly asks for tests.
2. Do not add code comments unless user explicitly asks for comments.
3. For backend tasks, use Java, Spring, and Postgres by default.
4. Another backend stack is allowed only with explicit user permission.
5. When user says "бери задачи", go to `ТЗ/`, pick tasks ready for development with status `WAIT_DEV`, rename file status to `IN_PROGRESS`, then start implementation strictly by specification.
6. When implementation is complete, rename task file status to `COMPLETED`.
7. Take tasks sequentially only: pick the earliest `WAIT_DEV` task by queue order and do not start the next task until the current one is `COMPLETED` (or explicitly paused/blocked by user).
8. Write delivery notes and task communication in Russian.

## Execution Standard

1. Read source specification from `ТЗ/*.md` with status `WAIT_DEV` or `IN_DEV`.
2. Validate acceptance criteria before coding.
3. Implement minimal and safe change set.
4. Keep implementation strictly aligned with specification.
5. Document assumptions and risks in delivery notes only when requested.

## Technical Checklist

- Java code compiles and follows project style.
- Spring components use clear boundaries (controller/service/repository).
- Postgres changes include migration and rollback notes when needed.
- Errors are handled explicitly and logged with context.
- Public API changes are reflected in documentation.

## Output Template

```markdown
# Backend Delivery

## Implemented
- ...

## Tests
- ...

## Risks / Notes
- ...
```
