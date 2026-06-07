---
status: done
---

# Story 12.2: Add REST API Documentation to Incident Management README

## Status
done

## Story

As a developer evaluating Operaton,
I want the incident management README to show how to drive the process and send signals via REST,
So that I understand how external systems can trigger Operaton signal events programmatically.

## Acceptance Criteria

1. **Given** the UC-03 `README.md.jte` **When** a "REST API" section is added **Then** it contains curl examples for start, list tasks, send signal, and complete task

## Tasks/Subtasks

- [x] Task 1: Add REST API section to README
  - [x] 1.1: curl for `POST /engine-rest/process-definition/key/incident-management/start`
  - [x] 1.2: curl for `GET /engine-rest/task?processDefinitionKey=incident-management`
  - [x] 1.3: curl for `POST /engine-rest/signal` with body `{"name": "EscalationSignal"}` — highlight this as the key pattern
  - [x] 1.4: curl for `POST /engine-rest/task/{id}/complete` for triage task

- [x] Task 2: Verify README has no template errors

## Dev Notes

**Key file:** `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte`

Use `-u alice:alice` for process start (employees group). Triage task is claimable by `first-line` group.

**Signal REST example:**
```bash
curl -u alice:alice -X POST \
  http://localhost:8080/engine-rest/signal \
  -H 'Content-Type: application/json' \
  -d '{"name": "EscalationSignal"}'
```

## Dev Agent Record

Added a "REST API" section to `README.md.jte` with four curl examples covering process start, task listing, signal sending, and task completion. All templates compile and tests pass.

## File List

- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte`

## Change Log

- Added REST API section with curl examples for start, list tasks, send EscalationSignal, and complete triage task
