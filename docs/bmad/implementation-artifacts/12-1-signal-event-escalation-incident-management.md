---
status: done
---

# Story 12.1: Add Signal Event Escalation to Incident Management

## Status
done

## Story

As a developer evaluating Operaton,
I want an external signal to escalate an incident to second-line support in parallel with ongoing triage,
So that I understand how boundary signal events enable async external triggers without interrupting active tasks.

## Acceptance Criteria

1. **Given** `incident-management.bpmn.jte` **When** a non-interrupting boundary signal catch event named `EscalationSignal` is added to the "First-Line Triage" task **Then** it is wired to a sequence flow creating the "Second-Line Engineer" task in parallel

2. **Given** a process instance at "First-Line Triage" **When** `runtimeService.signalEventReceived("EscalationSignal")` is called **Then** a "Second-Line Engineer" task is created AND "First-Line Triage" task remains active

3. **Given** `IncidentManagementIT` **When** the signal is sent **Then** `taskService.createTaskQuery().taskDefinitionKey("Task_SecondLine").count()` returns 1 AND triage task count returns 1

4. **Given** `incidentPriority` status variable (set in Story 9.2) **When** escalation signal fires **Then** `execution.setVariable("incidentPriority", "HIGH")` is called on the escalation path

## Tasks/Subtasks

- [x] Task 1: Add boundary signal event to BPMN
  - [x] 1.1: In `incident-management.bpmn.jte`, add `<boundaryEvent id="Signal_Escalation" attachedToRef="Task_FirstLineTriage" cancelActivity="false"><signalEventDefinition signalRef="EscalationSignal"/></boundaryEvent>`
  - [x] 1.2: Add `<signal id="EscalationSignal" name="EscalationSignal"/>` to the `<definitions>` section
  - [x] 1.3: Add sequence flow from signal boundary event to `Task_SecondLine` via `Task_SetHighPriority`

- [x] Task 2: Create escalation service task for priority update
  - [x] 2.1: Added `Task_SetHighPriority` service task using `operaton:expression` to set `incidentPriority = HIGH`

- [x] Task 3: Add IT test for signal escalation
  - [x] 3.1: Added `signalEscalation_createsSecondLineTask_whileTriageRemainsActive` test method
  - [x] 3.2: Assert `Task_SecondLine` count equals 1
  - [x] 3.3: Assert `Task_FirstLineTriage` count equals 1
  - [x] 3.4: Assert `incidentPriority` equals `"HIGH"`

- [x] Task 4: Verify all tests pass

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/IncidentManagementIT.java.jte`

**Non-interrupting boundary signal BPMN pattern:**
```xml
<signal id="EscalationSignal" name="EscalationSignal"/>

<boundaryEvent id="Signal_Escalation" attachedToRef="Task_FirstLineTriage" cancelActivity="false">
  <signalEventDefinition signalRef="EscalationSignal"/>
</boundaryEvent>
```

**Signal sending in test:**
```java
runtimeService.signalEventReceived("EscalationSignal");
```

**Dependency note:** Story 9.2 sets `incidentPriority = LOW` at start. This story must update it to `HIGH` on the escalation path. Implement defensively — if 9.2 hasn't landed yet, set it here regardless.

## Dev Agent Record

### Implementation Plan

1. Added `<signal id="EscalationSignal"/>` in definitions section
2. Added non-interrupting `<boundaryEvent id="Signal_Escalation" cancelActivity="false">` on `Task_FirstLineTriage`
3. Added `Task_SetHighPriority` service task using `operaton:expression` to set variable
4. Wired: `Signal_Escalation → Task_SetHighPriority → Task_SecondLine`
5. Added BPMN DI shapes for all new elements
6. Added IT test method at Order(4) to verify parallel escalation behavior

### Completion Notes

All 42 unit tests pass. The non-interrupting signal boundary event creates a parallel token: `Task_FirstLineTriage` stays active while `Task_SecondLine` is created and `incidentPriority` is updated to `HIGH`.

## File List

- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/IncidentManagementIT.java.jte`

## Change Log

- Added `EscalationSignal` signal definition
- Added non-interrupting boundary signal event `Signal_Escalation` on `Task_FirstLineTriage`
- Added `Task_SetHighPriority` service task with inline UEL expression
- Added sequence flows `Flow_signal_to_priority` and `Flow_priority_to_secondline`
- Added `<incoming>Flow_priority_to_secondline</incoming>` to `Task_SecondLine`
- Added BPMN DI shapes and edges for all new elements
- Added `signalEscalation_createsSecondLineTask_whileTriageRemainsActive` test at Order(4)
