---
name: developer-frontend
description: Implements frontend tasks with React and TypeScript. Use when task affects UI components, state, routing, forms, validations, or frontend API integration.
---

# Developer Frontend

## Scope

Use this skill for:
- React components and page flows;
- state management and async UI behavior;
- forms, validation, and user feedback states;
- integration with backend APIs;
- accessibility and UX consistency.

## Behavior Rules

1. Do not write tests unless user explicitly asks for tests.
2. Do not add code comments unless user explicitly asks for comments.
3. For frontend tasks, use React and TypeScript by default.
4. Another frontend stack is allowed only with explicit user permission.
5. When user says "бери задачи", go to `ТЗ/`, pick tasks ready for development with status `WAIT_DEV`, rename file status to `IN_PROGRESS`, then start implementation strictly by specification.
6. When implementation is complete, rename task file status to `COMPLETED`.
7. Take tasks sequentially only: pick the earliest `WAIT_DEV` task by queue order and do not start the next task until the current one is `COMPLETED` (or explicitly paused/blocked by user).
8. Write delivery notes and task communication in Russian.

## Execution Standard

1. Read relevant specification from `ТЗ/*.md`.
2. Identify UX and acceptance criteria before edits.
3. Implement components with predictable state and explicit loading/error states.
4. Keep UI copy and behavior aligned strictly with specification.
5. Add tests only on explicit request.

## Technical Checklist

- Components are reusable and focused.
- Async calls handle loading, empty, and error states.
- Forms validate on clear rules with user-friendly messages.
- TypeScript types are strict for props and API models.
- Accessibility basics are respected (labels, keyboard, semantics).

## Output Template

```markdown
# Frontend Delivery

## Implemented
- ...

## Tests
- ...

## Risks / Notes
- ...
```
