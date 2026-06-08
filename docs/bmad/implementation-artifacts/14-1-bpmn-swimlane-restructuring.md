---
baseline_commit: cd2a0cd
---

# Story 14-1: BPMN Swimlane Restructuring for All Use Cases

## Status
done

## Story

As a **developer evaluating Operaton**,
I want every use case BPMN to use collaboration pools with named swimlanes per actor group,
So that the diagram immediately communicates who does what without reading task labels or candidateGroups attributes.

## Acceptance Criteria

1. **Given** any of the four use case BPMN files is opened in Operaton Modeler or Cockpit **When** the diagram renders **Then** the process is wrapped in a collaboration with a single pool; each lane is named after its actor group; a `System` lane contains all automated elements (service tasks, send tasks, gateways, timer events, end events)

2. **Given** the BPMN XML is reviewed **When** every flow element is checked **Then** each element is placed in the correct lane via `<flowNodeRef>`; no task label contains the responsible actor's name

3. **Given** the UC-01 BPMN **When** it is reviewed **Then** the "Employee Notified of Rejection" user task is absent; a Send Task named "Send Rejection Email" is in the System lane using `delegateExpression="${leaveRejectionEmailDelegate}"`; both the invalid-validation path and the manager-rejection path route to this send task

4. **Given** the UC-01 "Review Request" user task **When** reviewed **Then** a non-interrupting timer boundary event with duration `${managerReviewTimeout}` is present; on firing it routes to an "Escalation Reminder" service task in the System lane (using `${escalationReminderDelegate}`) that sets `escalated = true`; the Review Request task remains active

5. **Given** the task rename table **When** BPMNs are updated **Then**: "Manager Reviews Request" → "Review Request"; "HR Records Approved Leave" → "Record Approved Leave"; "First-Line Triage" → "Triage"; "Second-Line Engineer" → "Handle Escalation"

6. **Given** all four BPMNs are updated **When** the commit lands **Then** swimlane restructuring and task renames land atomically in one commit

7. **Given** `mvn verify -pl starter-templates` **When** it runs **Then** all tests pass

## Tasks/Subtasks

- [x] Task 1: Restructure UC-01 leave-request.bpmn.jte with swimlanes
  - [x] 1.1: Add collaboration/pool/laneSet elements
  - [x] 1.2: Assign all flow nodes to correct lanes
  - [x] 1.3: Replace "Employee Notified of Rejection" user task with Send Task "Send Rejection Email"
  - [x] 1.4: Add non-interrupting timer boundary to "Review Request" → Escalation Reminder service task
  - [x] 1.5: Rename tasks (remove actor prefix)
  - [x] 1.6: Update BPMNDi shapes/edges with correct pool+lane coordinates

- [x] Task 2: Restructure UC-02 loan-application.bpmn.jte with swimlanes
  - [x] 2.1: Add collaboration/pool/laneSet; employees, underwriters, System lanes
  - [x] 2.2: Assign flow nodes; update BPMNDi

- [x] Task 3: Restructure UC-03 incident-management.bpmn.jte with swimlanes
  - [x] 3.1: Add collaboration/pool/laneSet; employees, first-line, second-line, System lanes
  - [x] 3.2: Assign flow nodes; rename "First-Line Triage" → "Triage", "Second-Line Engineer" → "Handle Escalation"; update BPMNDi

- [x] Task 4: Restructure UC-04 order-fulfillment.bpmn.jte with swimlanes
  - [x] 4.1: Add collaboration/pool/laneSet; sales, warehouse, System lanes
  - [x] 4.2: Assign flow nodes; update BPMNDi

- [x] Task 5: Run `mvn verify -pl starter-templates` — all tests green

## Dev Notes

- `BPMNPlane.bpmnElement` must reference the `collaboration.id`, NOT the `process.id`
- Pool shape: `isHorizontal="true"`; lane shapes also `isHorizontal="true"`
- `boundaryEvent` elements must NOT appear in `<lane><flowNodeRef>` — only regular flow nodes
- Pool label column: 30px (x=160-190); Lane label column: 30px (x=190-220); content from x=220+
- Timer boundary: non-interrupting means `cancelActivity="false"`
- UC-01 timer variable: `${managerReviewTimeout}` (process var, defaults PT72H in DataInitializer)

## File List

- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte`

## Review Findings

- [x] [Review][Decision] BPMN file state discrepancy — resolved: grep confirmed `Collaboration_LeaveRequest` IS present in working tree; acceptance auditor read error was a false negative
- [x] [Review][Decision] Timer duration expression — resolved: kept ternary with fixed double quotes `"PT72H"` for robustness
- [x] [Review][Patch] Timer fallback single-quoted `'PT72H'` — fixed to double-quoted `"PT72H"` [starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte]
- [x] [Review][Patch] `Task_EscalationReminder` does not set `escalated = true` — confirmed: `EscalationReminderDelegate.java.jte` already calls `execution.setVariable("escalated", true)` at runtime; no BPMN change needed
- [x] [Review][Defer] No test assertions cover `Task_SendRejectionEmail`, escalation path, or timer boundary in GenerationEngineTest — deferred, test coverage gap
- [x] [Review][Defer] Non-interrupting timer with duration fires once only — confirm `${managerReviewTimeout}` is duration (not cycle); currently safe but worth a comment — deferred, documentation gap

## Change Log

- 2026-06-08: Story created — FR-16 swimlane restructuring + FR-3b BPMN structural changes
- 2026-06-08: Code review — 2 decisions needed, 2 patches, 2 deferred
