---
status: done
baseline_commit: 4a93893
---

# Story 10.3: Demonstrate Task-Local Variables and History API

## Status
ready-for-dev

## Story

As a developer evaluating Operaton,
I want to see how task-local variables are scoped and how the History API retrieves past variable state,
So that I understand variable scoping and process auditing patterns.

## Acceptance Criteria

1. **Given** the manager completes "Manager Reviews Request" **When** `taskService.setVariableLocal(taskId, "approvalComment", comment)` is called before `taskService.complete(taskId)` **Then** `approvalComment` is stored scoped to that task instance and does not appear as a process-level variable

2. **Given** `LeaveRequestIT` completes the manager task with a comment **When** `historyService.createHistoricVariableInstanceQuery().taskIdIn(taskId).list()` is called **Then** the query returns `approvalComment` with the value that was set

3. **Given** `LeaveRequestIT` runs the full approved path through to HR completion **When** `historyService.createHistoricVariableInstanceQuery().variableName("remainingVacationDays").singleResult()` is called **Then** the historic value recorded at process start is greater than the balance after `VacationBalanceService` deduction

## Tasks/Subtasks

- [ ] Task 1: Add approvalComment task-local variable in IT test
  - [ ] 1.1: In `LeaveRequestIT.java.jte`, when completing the manager task on the approved path, call `taskService.setVariableLocal(taskId, "approvalComment", "Approved for annual leave")` before `taskService.complete(taskId, vars)`
  - [ ] 1.2: Assert `runtimeService.getVariables(pid)` does NOT contain key `approvalComment` (confirming task-local scope)
  - [ ] 1.3: Assert `historyService.createHistoricVariableInstanceQuery().taskIdIn(taskId).variableName("approvalComment").singleResult()` is not null and has correct value

- [ ] Task 2: Add remainingVacationDays history assertion
  - [ ] 2.1: In `LeaveRequestIT.java.jte`, after the full approved path (including HR step), query `historyService.createHistoricVariableInstanceQuery().processInstanceId(pid).variableName("remainingVacationDays").singleResult()`
  - [ ] 2.2: Assert the historic value (captured at start) is greater than `vacationBalanceService.getBalance(requesterId)` (post-deduction balance)

- [ ] Task 3: Verify all tests pass

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte`

**No BPMN changes required** — this story is purely test additions demonstrating the History API and task-local variable APIs.

**Task-local variable pattern:**
```java
String taskId = taskService.createTaskQuery().taskDefinitionKey("Task_ManagerReview").singleResult().getId();
taskService.setVariableLocal(taskId, "approvalComment", "Looks good!");
taskService.complete(taskId, Map.of("approved", true));
```

**History API query pattern:**
```java
HistoricVariableInstance hvi = historyService
    .createHistoricVariableInstanceQuery()
    .taskIdIn(taskId)
    .variableName("approvalComment")
    .singleResult();
assertThat(hvi).isNotNull();
assertThat(hvi.getValue()).isEqualTo("Looks good!");
```

**Note:** History queries require `history.level=full` or `audit` in test properties. Check `application-h2.properties` — if not set, add `operaton.bpm.history-level=full`.

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
