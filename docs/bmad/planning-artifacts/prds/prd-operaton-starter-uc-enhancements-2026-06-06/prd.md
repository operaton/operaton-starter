---
title: "Use Case Enhancements: Authorization, Email Notifications, and Task Data"
status: final
created: 2026-06-06
updated: 2026-06-07
project: operaton-starter
---

# PRD: Use Case Enhancements — Authorization, Email Notifications, and Task Data

## Overview

The four operaton-starter use cases (Leave Request, Loan Application, Incident Management, Order Fulfillment) are currently missing **process-start authorization** — any authenticated user can start processes. Additionally, the Loan Application use case needs email-based rejection notification (visible as a BPMN send event), a local Mailpit service for email testing, and the Leave Request task forms need to surface key leave variables for the reviewer and HR user.

---

## Problem Statement

1. **Authorization gap.** Start events carry no `candidateStarterGroups` restriction. A warehouse picker can start a loan application; an underwriter can start a leave request. This contradicts realistic role-based access control and teaches incorrect authorization patterns.

2. **Silent rejection.** In UC-02 Loan Application, high-risk rejections are handled by `Auto-Reject Notify` — a plain service task. Rejection deserves an explicit send email event as a business-critical step, making the notification intent visible to process modelers.

3. **No email test infrastructure.** The Docker Compose stacks include no local mail service, requiring external SMTP configuration to explore email flows. This friction slows adoption of the example.

4. **Sparse task forms.** In UC-01, when a manager reviews a leave request or HR records approval, the task form shows no contextual data. Reviewers must leave the task form to find requested dates, duration, or remaining balance, undermining task-centric design.

---

## Goals

- Every use case process can only be started by users in the designated starter group(s)
- UC-02 Loan Application rejection path uses a BPMN Send Email event, backed by a Spring Mail delegate
- UC-02 Docker Compose stack includes a Mailpit service; SMTP is routed to Mailpit in `application.properties`
- UC-02 README explains how to access Mailpit and view emails per user
- UC-01 "Manager Reviews Request" and "HR Records Approved Leave" task forms display: requester, start date, end date, number of days, remaining vacation days

## Non-Goals

- Email notifications in UC-01, UC-03, or UC-04 (UC-02 only in this iteration)
- Custom task form UI (uses Operaton Tasklist embedded forms)
- SMTP authentication or TLS for Mailpit (local dev only)
- Role management UI

---

## Functional Requirements

### FR-1 Process Start Authorization (all use cases)

**FR-1.1** Each `<process>` element in every use case BPMN must declare `operaton:candidateStarterGroups` matching the role that initiates the real-world process:

| Use Case | Starter Group | Sample User |
|----------|--------------|-------------|
| UC-01 Leave Request | `employees` | alice (existing) |
| UC-02 Loan Application | `employees` | alice (existing) |
| UC-03 Incident Management | `employees` | alice (existing) |
| UC-04 Order Fulfillment | `sales` (new) | frank (new) |

**FR-1.2** UC-04 `DataInitializer` must create the `sales` group and user `frank` (Frank, Sales, frank@example.com, password `frank`), assigned to `sales`.

**FR-1.3** Each use case README must document which user to log in as to start the process.

### FR-2 User Task Authorization Audit

**FR-2.1** All four use case BPMNs must be audited; every user task must carry `operaton:candidateGroups`. Current state — all tasks already have groups declared:

| Use Case | Task | Group |
|----------|------|-------|
| UC-01 | Manager Reviews Request | `managers` ✓ |
| UC-01 | HR Records Approved Leave | `hr` ✓ |
| UC-01 | Employee Notified of Rejection | `employees` ✓ |
| UC-02 | Underwriter Review | `underwriters` ✓ |
| UC-03 | First-Line Triage | `first-line` ✓ |
| UC-03 | Second-Line Engineer | `second-line` ✓ |
| UC-04 | Pack & Ship | `warehouse` ✓ |

Any tasks without groups found during the audit must be fixed before implementation is considered complete.

### FR-3 UC-02 Email Rejection via Send Event

**FR-3.1** Replace the `Auto-Reject Notify` service task on the high-risk rejection path with a BPMN **Send Task** named "Send Rejection Email".

**FR-3.2** The task must use a Java delegate (`RejectionEmailDelegate`) that:
- Reads applicant email from process variable `applicantEmail`
- Sends a plain-text email with subject "Loan Application — Decision" and body including applicant name and reason
- Uses Spring `JavaMailSender` (auto-configured via `spring.mail.*` properties)

**FR-3.3** `DataInitializer` must populate `applicantEmail` as a process variable at start, or resolve it from the identity service at runtime.

**FR-3.4** `application.properties` and `application-docker.properties` must include:
```
spring.mail.host=localhost
spring.mail.port=1025
```

### FR-4 Mailpit Service in Docker Compose (UC-02)

**FR-4.1** The UC-02 `docker-compose.yml` must include:

```yaml
mailpit:
  image: axllent/mailpit:latest
  ports:
    - "1025:1025"   # SMTP
    - "8025:8025"   # Web UI
  restart: unless-stopped
```

Acceptance: `docker compose up` succeeds and the Mailpit web UI is accessible at `http://localhost:8025`.

### FR-5 Mailpit Access Documentation (UC-02 README)

**FR-5.1** The UC-02 README must include an "Email Testing with Mailpit" section that:
- States the web UI URL: `http://localhost:8025`
- Lists the email addresses of all sample users so testers know which inbox to check
- Explains that high-risk loan applications trigger a rejection email to the applicant

### FR-6 Task Form Variable Display (UC-01)

**FR-6.1** When the leave request process starts, the following variables must be set as process variables:
- `startDate` (String ISO-8601)
- `endDate` (String ISO-8601)
- `days` (int — calendar days, inclusive of start and end date)
- `remainingVacationDays` (int — fetched from `VacationBalanceService` before any deduction)

**FR-6.2** The embedded task form for "Manager Reviews Request" must display (read-only):

| Field | Variable |
|-------|----------|
| Requester | `requester` (set via `operaton:initiator`) |
| Start Date | `startDate` |
| End Date | `endDate` |
| Days Requested | `days` |
| Remaining Vacation Days | `remainingVacationDays` |

**FR-6.3** The same five fields must appear on "HR Records Approved Leave".

**FR-6.4** Forms are implemented as embedded HTML files referenced via `operaton:formKey`. Both forms are new — no existing form key is present in the current BPMN.

### FR-7 Status Variables for Cockpit Visibility (all use cases)

**FR-7.1** Each use case must set at least one named status variable via `execution.setVariable()` at key transitions so Cockpit's variable view is readable without process internals knowledge:

| Use Case | Variable | Example Values |
|----------|----------|---------------|
| UC-01 | `leaveStatus` | `PENDING` → `APPROVED` / `REJECTED` |
| UC-02 | `loanDecision` | `PENDING` → `APPROVED` / `REJECTED` |
| UC-03 | `incidentPriority` | `LOW` → `HIGH` (on escalation) |
| UC-04 | `orderStatus` | `RECEIVED` → `FULFILLED` / `FAILED` |

**FR-7.2** The variable must reflect at least two distinct states across process milestones.

### FR-8 Timer Boundary Escalation (UC-01)

**FR-8.1** The "Manager Reviews Request" user task must carry a non-interrupting timer boundary event. Duration defaults to `PT72H`, overridable via process variable `managerReviewTimeout` to support short-circuit testing.

**FR-8.2** On expiry, the escalation path invokes a reminder service task delegate and sets process variable `escalated = true`. The manager task remains active (non-interrupting).

**FR-8.3** The integration test must set `managerReviewTimeout = PT1S` and assert the escalation path executes without real waiting.

### FR-9 Failure Path and Job Retry (UC-04)

**FR-9.1** `PaymentDelegate` must throw a `BpmnError` with code `PAYMENT_FAILED` when process variable `simulatePaymentFailure = true`.

**FR-9.2** The BPMN must handle `PAYMENT_FAILED` via a boundary error event on the payment task, routing to a "Notify Customer of Failure" end event.

**FR-9.3** The payment service task must declare `operaton:failedJobRetryTimeCycle="R3/PT10S"` so developers see retry configuration in the BPMN XML.

**FR-9.4** The integration test must assert the failure path is reached when `simulatePaymentFailure = true`.

### FR-10 Business Key (UC-02)

**FR-10.1** The loan application process must be started with a business key (e.g., `"LOAN-" + UUID`) passed to `RuntimeService.startProcessInstanceByKey(key, businessKey, variables)`.

**FR-10.2** The integration test must query by business key: `runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey)`.

**FR-10.3** The UC-02 README must explain what business keys are and why they matter for correlating external system IDs to process instances.

### FR-11 Signal Event Escalation (UC-03)

**FR-11.1** The "First-Line Triage" user task must carry a non-interrupting boundary signal catch event named `EscalationSignal`.

**FR-11.2** When the signal fires, the process routes to the "Second-Line Engineer" task in parallel with the ongoing triage task.

**FR-11.3** The integration test must call `runtimeService.signalEventReceived("EscalationSignal")` and assert the second-line task is created.

### FR-12 Process Suspension Demo (UC-04)

**FR-12.1** `OrderFulfillmentIT` must include a test case that:
1. Starts a process instance
2. Suspends it via `runtimeService.suspendProcessInstanceById(id)`
3. Asserts jobs do not execute while suspended
4. Reactivates via `runtimeService.activateProcessInstanceById(id)`
5. Asserts the process continues to completion

No BPMN change is required; this is a test-only demonstration of the suspension API.

### FR-13 Task-Local Variables (UC-01)

**FR-13.1** When the manager completes "Manager Reviews Request", the completion call must first set a task-local variable `approvalComment` via `taskService.setVariableLocal(taskId, "approvalComment", comment)`.

**FR-13.2** The integration test must retrieve the comment from history via `historyService.createHistoricVariableInstanceQuery().taskIdIn(taskId)` and assert it is present.

### FR-14 History API Assertion (UC-01)

**FR-14.1** `LeaveRequestIT` must assert after the HR step that `remainingVacationDays` was decremented: the balance queried via `HistoryService.createHistoricVariableInstanceQuery()` after approval must be lower than the balance recorded at process start.

---

## Documentation Requirements

### DR-15 REST API Examples (all use case READMEs)

**DR-15.1** Each use case README must include a "REST API" section with `curl` examples for:
- Starting a process instance (`POST /engine-rest/process-definition/key/{key}/start`)
- Listing active tasks (`GET /engine-rest/task`)
- Completing a task (`POST /engine-rest/task/{id}/complete`)

**DR-15.2** Examples must use the actual process keys and realistic variable payloads drawn from each use case's `DataInitializer`.

---

## Open Questions

All open questions resolved on 2026-06-06.

| # | Question | Resolution |
|---|----------|------------|
| OQ-1 | UC-02 starter group? | `employees` — any employee can apply for a loan |
| OQ-2 | UC-03 starter group? | `employees` — any employee can report an incident |
| OQ-3 | UC-04 start mechanism? | `sales` group (new) — frank starts orders via Tasklist |
| OQ-4 | `days` = calendar or working? | Calendar days |
| OQ-5 | Mailpit scope? | UC-02 only |
| OQ-6 | `remainingVacationDays` timing? | Before deduction — shows balance entering the request |

---

## Success Metrics

- Attempting to start a use case process as the wrong user role results in an authorization error in Operaton Tasklist
- A high-risk loan application generates a visible email in Mailpit within 5 seconds of process completion
- Manager and HR task forms in UC-01 display all five fields without leaving the task
- UC-01 timer boundary fires in the integration test when `managerReviewTimeout = PT1S`
- UC-04 payment failure path is reached and asserted when `simulatePaymentFailure = true`
- UC-03 signal escalation creates a second-line task when `runtimeService.signalEventReceived` is called in the test
- All four use case READMEs contain working `curl` examples copy-pasteable against a running instance
