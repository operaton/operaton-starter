---
status: done
---

# Story 13.3: Add REST API Documentation to Order Fulfillment README

## Status
done

## Story

As a developer evaluating Operaton,
I want the order fulfillment README to show REST API usage including process suspension,
So that I understand how ops teams can manage running process instances programmatically.

## Acceptance Criteria

1. **Given** the UC-04 `README.md.jte` **When** a "REST API" section is added **Then** it contains curl examples for start, list tasks, complete task, and suspend process instance

## Tasks/Subtasks

- [x] Task 1: Add REST API section to README
  - [x] 1.1: curl for `POST /engine-rest/process-definition/key/order-fulfillment/start`
  - [x] 1.2: curl for `GET /engine-rest/task?processDefinitionKey=order-fulfillment`
  - [x] 1.3: curl for `POST /engine-rest/task/{id}/complete` for pack-and-ship task
  - [x] 1.4: curl for `PUT /engine-rest/process-instance/{id}/suspended` with `{"suspended": true}` — highlight as ops pattern

- [x] Task 2: Verify README has no template errors

## Dev Notes

**Key file:** `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte`

Use `-u frank:frank` for process start (sales group). Pack & Ship task is claimable by `warehouse` group.

**Suspension REST example:**
```bash
curl -u admin:admin -X PUT \
  http://localhost:8080/engine-rest/process-instance/{processInstanceId}/suspended \
  -H 'Content-Type: application/json' \
  -d '{"suspended": true}'
```

## Dev Agent Record

Added "REST API" section to README.md.jte with four curl examples: start process, list tasks, complete pack-and-ship task, and suspend process instance (highlighted as ops pattern).

## File List

- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte`

## Change Log

- Added REST API section with 4 curl examples before the Running Tests section
