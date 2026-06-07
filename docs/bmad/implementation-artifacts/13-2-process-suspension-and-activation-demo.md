---
status: done
---

# Story 13.2: Demonstrate Process Suspension and Activation

## Status
done

## Story

As a developer evaluating Operaton,
I want to see a process instance suspended and reactivated in a test,
So that I understand how operators can pause and resume process execution at runtime.

## Acceptance Criteria

1. **Given** `OrderFulfillmentIT` starts a process instance **When** `runtimeService.suspendProcessInstanceById(id)` is called **Then** the instance is suspended

2. **Given** the instance is suspended **When** `managementService.executeJob(jobId)` is attempted **Then** a `SuspendedJobException` is thrown

3. **Given** `runtimeService.activateProcessInstanceById(id)` is called **When** process resumes **Then** it continues to completion

4. **Given** no BPMN changes are required **When** story is implemented **Then** all changes are in `OrderFulfillmentIT.java.jte` only

## Tasks/Subtasks

- [x] Task 1: Add suspend/activate test to OrderFulfillmentIT
  - [x] 1.1: Start a process instance (use `simulatePaymentFailure = false` for happy path)
  - [x] 1.2: Call `runtimeService.suspendProcessInstanceById(pi.getId())`
  - [x] 1.3: Assert `runtimeService.createProcessInstanceQuery().suspended().count()` equals 1
  - [x] 1.4: Attempt to execute any active job and assert `SuspendedJobException` is thrown (wrap in `assertThrows`)
  - [x] 1.5: Call `runtimeService.activateProcessInstanceById(pi.getId())`
  - [x] 1.6: Assert instance is no longer suspended: `runtimeService.createProcessInstanceQuery().active().processInstanceId(pi.getId()).count()` equals 1

- [x] Task 2: Verify all tests pass

## Dev Notes

**Key file:** `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/OrderFulfillmentIT.java.jte`

**No BPMN changes required.**

**Suspension test pattern:**
```java
ProcessInstance pi = runtimeService.startProcessInstanceByKey("order-fulfillment", vars);
runtimeService.suspendProcessInstanceById(pi.getId());

assertThat(runtimeService.createProcessInstanceQuery().suspended()
    .processInstanceId(pi.getId()).count()).isEqualTo(1);

Job job = managementService.createJobQuery().processInstanceId(pi.getId()).singleResult();
if (job != null) {
    assertThrows(SuspendedJobException.class, () -> managementService.executeJob(job.getId()));
}

runtimeService.activateProcessInstanceById(pi.getId());
assertThat(runtimeService.createProcessInstanceQuery().active()
    .processInstanceId(pi.getId()).count()).isEqualTo(1);
```

## Dev Agent Record

Added `suspendAndActivateProcessInstance` test (@Order(5)) to OrderFulfillmentIT. Added `ManagementService`, `HistoryService` autowired fields. Used `SuspendedEntityInteractionException` (the actual exception class in Operaton, found via jar inspection). Test cleans up by completing the warehouse task.

## File List

- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/OrderFulfillmentIT.java.jte`

## Change Log

- Added `ManagementService` and `HistoryService` autowired fields
- Added imports for `ManagementService`, `HistoryService`, `SuspendedEntityInteractionException`, `Job`
- Added `suspendAndActivateProcessInstance` test method
