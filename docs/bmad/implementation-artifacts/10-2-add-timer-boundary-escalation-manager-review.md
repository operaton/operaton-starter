---
status: done
baseline_commit: 4a93893
---

# Story 10.2: Add Timer Boundary Escalation to Manager Review Task

## Status
ready-for-dev

## Story

As a developer evaluating Operaton,
I want to see a non-responding manager task escalate automatically via a timer,
So that I understand how non-interrupting timer boundary events handle time-based escalation.

## Acceptance Criteria

1. **Given** the "Manager Reviews Request" user task in `leave-request.bpmn.jte` **When** a non-interrupting timer boundary event is added with duration defaulting to `PT72H` **Then** the timer duration is overridable via process variable `managerReviewTimeout` at process start

2. **Given** the timer fires **When** the escalation path executes **Then** a reminder service task delegate runs and sets process variable `escalated = true`; the manager task remains active and claimable

3. **Given** `LeaveRequestIT` sets `managerReviewTimeout = PT1S` at process start **When** the test executes the timer job via `managementService.executeJob` **Then** the test asserts `escalated = true` is set and the manager task is still active

## Tasks/Subtasks

- [ ] Task 1: Add non-interrupting timer boundary event to BPMN
  - [ ] 1.1: In `leave-request.bpmn.jte`, add a `<boundaryEvent id="TimerBoundary_Manager" attachedToRef="Task_ManagerReview" cancelActivity="false">` with `<timerEventDefinition><timeDuration>${managerReviewTimeout != null ? managerReviewTimeout : 'PT72H'}</timeDuration></timerEventDefinition>`
  - [ ] 1.2: Add sequence flow from timer boundary to an escalation service task
  - [ ] 1.3: Add `<serviceTask id="Task_EscalateReminder" name="Send Escalation Reminder" operaton:delegateExpression="${escalationReminderDelegate}">` that routes to an end event

- [ ] Task 2: Create EscalationReminderDelegate
  - [ ] 2.1: Create `starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/EscalationReminderDelegate.java.jte`
  - [ ] 2.2: Implement `JavaDelegate` that sets `execution.setVariable("escalated", true)` and logs a reminder message

- [ ] Task 3: Add IT test for timer escalation
  - [ ] 3.1: In `LeaveRequestIT.java.jte`, add test method that starts process with `managerReviewTimeout = "PT1S"` variable
  - [ ] 3.2: Fetch timer job: `managementService.createJobQuery().timers().singleResult()`
  - [ ] 3.3: Execute job: `managementService.executeJob(job.getId())`
  - [ ] 3.4: Assert `runtimeService.getVariable(pid, "escalated")` equals `true`
  - [ ] 3.5: Assert manager task still exists: `taskService.createTaskQuery().taskDefinitionKey("Task_ManagerReview").count()` equals 1

- [ ] Task 4: Verify all tests pass

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte`
- New: `starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/EscalationReminderDelegate.java.jte`

**Non-interrupting timer boundary BPMN pattern:**
```xml
<boundaryEvent id="TimerBoundary_Manager" attachedToRef="Task_ManagerReview" cancelActivity="false">
  <timerEventDefinition>
    <timeDuration>${managerReviewTimeout != null ? managerReviewTimeout : 'PT72H'}</timeDuration>
  </timerEventDefinition>
</boundaryEvent>
```

**Timer testing:** In IT tests, use `@Deployment` and the process engine's job executor. With `managementService.executeJob()` you can fire timers synchronously without waiting real time.

**Note:** The delegate folder already exists in UC-01 — check `uc-01-leave-request/delegate/` for existing delegates to follow their pattern.

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
