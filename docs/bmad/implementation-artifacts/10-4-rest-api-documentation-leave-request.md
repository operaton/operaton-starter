---
status: done
baseline_commit: 4a93893
---

# Story 10.4: Add REST API Documentation to Leave Request README

## Status
ready-for-dev

## Story

As a developer evaluating Operaton,
I want the leave request README to show how to drive the process via REST,
So that I know I can automate and script Operaton without using the Tasklist UI.

## Acceptance Criteria

1. **Given** the leave request `README.md.jte` **When** a "REST API" section is added **Then** it contains working `curl` examples for starting, listing tasks, and completing a task

2. **Given** the curl examples **When** they are read **Then** they use alice's credentials, the correct process key `leave-request`, and a realistic variable payload

## Tasks/Subtasks

- [ ] Task 1: Add REST API section to `README.md.jte`
  - [ ] 1.1: Add a "## REST API" section after the existing Getting Started section
  - [ ] 1.2: Add curl example for starting process: `POST /engine-rest/process-definition/key/leave-request/start` with variables `startDate`, `endDate`, `days`
  - [ ] 1.3: Add curl example for listing tasks: `GET /engine-rest/task?processDefinitionKey=leave-request`
  - [ ] 1.4: Add curl example for completing manager task: `POST /engine-rest/task/{id}/complete` with `approved=true`
  - [ ] 1.5: All examples use `-u alice:alice` basic auth header

- [ ] Task 2: Verify README renders correctly (no JTE template errors)

## Dev Notes

**Key file:** `starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte`

**curl example format:**
```bash
# Start a leave request
curl -u alice:alice -X POST \
  http://localhost:8080/engine-rest/process-definition/key/leave-request/start \
  -H 'Content-Type: application/json' \
  -d '{
    "variables": {
      "startDate": {"value": "2024-07-01", "type": "String"},
      "endDate":   {"value": "2024-07-05", "type": "String"},
      "days":      {"value": 5, "type": "Integer"}
    }
  }'

# List active tasks
curl -u alice:alice \
  'http://localhost:8080/engine-rest/task?processDefinitionKey=leave-request'

# Complete the manager review task (substitute {taskId} from list response)
curl -u bob:bob -X POST \
  http://localhost:8080/engine-rest/task/{taskId}/complete \
  -H 'Content-Type: application/json' \
  -d '{"variables": {"approved": {"value": true, "type": "Boolean"}}}'
```

**Note:** Use `bob:bob` credentials for the manager task completion (bob is in the managers group).

## Dev Agent Record

### Implementation Plan
_To be filled by dev agent_

### Debug Log
_To be filled by dev agent_

### Completion Notes
_To be filled by dev agent_

## File List
_To be filled by dev agent_

## Change Log
_To be filled by dev agent_
