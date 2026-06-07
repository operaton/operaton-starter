---
stepsCompleted: ['step-01-validate-prerequisites', 'step-02-design-epics', 'step-03-create-stories', 'step-04-final-validation']
workflowStatus: complete
completedAt: '2026-06-07'
inputDocuments:
  - 'docs/bmad/planning-artifacts/prds/prd-operaton-starter-uc-enhancements-2026-06-06/prd.md'
workflowStatus: in-progress
project_name: operaton-starter
---

# operaton-starter Use Case Enhancements - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for the operaton-starter use case enhancements, decomposing the requirements from the PRD into implementable stories.

## Requirements Inventory

### Functional Requirements

FR-1: Each use case BPMN must declare `operaton:candidateStarterGroups` — UC-01/02/03 use `employees`, UC-04 uses new `sales` group
FR-2: All user tasks across all four BPMNs must carry `operaton:candidateGroups`; audit and fill any gaps
FR-3.1: Replace UC-02 `Auto-Reject Notify` service task with a BPMN Send Task named "Send Rejection Email"
FR-3.2: Implement `RejectionEmailDelegate` using Spring `JavaMailSender`, reading `applicantEmail` process variable
FR-3.3: Populate `applicantEmail` process variable from DataInitializer or identity service at runtime
FR-3.4: Configure `spring.mail.host=localhost` and `spring.mail.port=1025` in UC-02 application properties
FR-4: Add Mailpit container to UC-02 `docker-compose.yml` (SMTP :1025, Web UI :8025)
FR-5: Add "Email Testing with Mailpit" section to UC-02 README with URL, user email addresses, and trigger explanation
FR-6.1: Set `startDate`, `endDate`, `days` (calendar), `remainingVacationDays` as process variables at UC-01 process start
FR-6.2/3: UC-01 embedded task forms for "Manager Reviews Request" and "HR Records Approved Leave" display all five fields read-only
FR-6.4: Implement forms as embedded HTML files referenced via `operaton:formKey`
FR-7: Each use case sets a named status variable (leaveStatus, loanDecision, incidentPriority, orderStatus) at key transitions via `execution.setVariable()`
FR-8.1: Add non-interrupting timer boundary event to UC-01 "Manager Reviews Request" task (default PT72H, overridable via `managerReviewTimeout` variable)
FR-8.2: Timer escalation path invokes reminder delegate and sets `escalated = true`
FR-8.3: Integration test asserts escalation fires with `managerReviewTimeout = PT1S`
FR-9.1: `PaymentDelegate` throws `BpmnError(PAYMENT_FAILED)` when `simulatePaymentFailure = true`
FR-9.2: UC-04 BPMN handles `PAYMENT_FAILED` via boundary error event routing to "Notify Customer of Failure"
FR-9.3: Payment service task declares `operaton:failedJobRetryTimeCycle="R3/PT10S"`
FR-9.4: Integration test asserts failure path reached when `simulatePaymentFailure = true`
FR-10.1: UC-02 process started with business key (`"LOAN-" + UUID`)
FR-10.2: Integration test demonstrates querying by business key
FR-10.3: UC-02 README explains business keys
FR-11.1: UC-03 "First-Line Triage" task carries non-interrupting boundary signal catch event `EscalationSignal`
FR-11.2: Signal fires escalation to "Second-Line Engineer" task in parallel
FR-11.3: Integration test calls `runtimeService.signalEventReceived("EscalationSignal")` and asserts second-line task created
FR-12: `OrderFulfillmentIT` includes suspend/activate test case (suspend → assert no job execution → activate → assert completion)
FR-13.1: Manager task completion sets task-local `approvalComment` via `taskService.setVariableLocal()`
FR-13.2: Integration test retrieves comment from history via `HistoryService`
FR-14: `LeaveRequestIT` asserts `remainingVacationDays` decremented after HR step via `HistoryService.createHistoricVariableInstanceQuery()`
DR-15: All four use case READMEs include REST API section with curl examples for start, list tasks, and complete task

### Non-Functional Requirements

NFR-1: Authorization enforced at the Operaton engine level via BPMN attributes — no application-layer workarounds
NFR-2: Email delivery from rejection path visible in Mailpit within 5 seconds of process completion
NFR-3: Timer boundary test must complete without real waiting (PT1S override)
NFR-4: All use case integration tests remain green after all changes
NFR-5: No new runtime dependencies beyond `spring-boot-starter-mail` (UC-02 only) and Mailpit as a Docker service

### Additional Requirements

- UC-04 DataInitializer must create `sales` group and user `frank` (frank@example.com, password `frank`)
- UC-02 and UC-03 reuse existing `employees` group — no new users needed for those use cases
- Embedded HTML task forms are new files; no existing `operaton:formKey` present in UC-01 BPMN
- `remainingVacationDays` on task forms reflects balance before deduction (pre-request state)
- `days` counts calendar days inclusive of start and end date

### UX Design Requirements

N/A — no UX document for this scope. Task form display (FR-6) is the only UI surface; requirements are fully specified in the PRD.

### FR Coverage Map

| FR | Epic | Description |
|----|------|-------------|
| FR-1 | Epic 1 | candidateStarterGroups on all 4 BPMNs + frank/sales (UC-04) |
| FR-2 | Epic 1 | User task candidateGroups audit |
| FR-7 | Epic 1 | Status variables for Cockpit visibility |
| FR-6 | Epic 2 | Task form variables + embedded HTML forms |
| FR-8 | Epic 2 | Timer boundary escalation on manager task |
| FR-13 | Epic 2 | Task-local approvalComment variable |
| FR-14 | Epic 2 | History API assertion in LeaveRequestIT |
| DR-15 (UC-01) | Epic 2 | REST API curl examples in leave request README |
| FR-3 | Epic 3 | Send Rejection Email task + RejectionEmailDelegate |
| FR-4 | Epic 3 | Mailpit in UC-02 docker-compose |
| FR-5 | Epic 3 | Mailpit README section |
| FR-10 | Epic 3 | Business key in UC-02 |
| DR-15 (UC-02) | Epic 3 | REST API curl examples in loan application README |
| FR-11 | Epic 4 | Signal event escalation in UC-03 |
| DR-15 (UC-03) | Epic 4 | REST API curl examples in incident management README |
| FR-9 | Epic 5 | PaymentDelegate failure + retry config |
| FR-12 | Epic 5 | Process suspension demo in OrderFulfillmentIT |
| DR-15 (UC-04) | Epic 5 | REST API curl examples in order fulfillment README |

## Epic List

### Epic 1: Authorization Foundation
Developers see realistic role-based access enforced by the engine, with process state visible in Cockpit without reading a BPMN. Stories must be sequenced: candidateStarterGroups first, then audit + status variables.
**FRs covered:** FR-1, FR-2, FR-7

### Epic 2: UC-01 Leave Request — Rich Task Experience
Developers experience a complete human workflow: task forms surface all relevant data, timer escalation handles non-response, and the History API shows how variable state is audited.
**FRs covered:** FR-6, FR-8, FR-13, FR-14, DR-15 (UC-01 README)

### Epic 3: UC-02 Loan Application — Email Integration & Business Keys
Developers see Operaton integrate with external services: rejection triggers a real email visible in Mailpit, and business keys show how to correlate external IDs to process instances.
**FRs covered:** FR-3, FR-4, FR-5, FR-10, DR-15 (UC-02 README)

### Epic 4: UC-03 Incident Management — Signal Escalation
Developers learn signal-based escalation: an external signal fires a parallel second-line task without interrupting the original triage.
**FRs covered:** FR-11, DR-15 (UC-03 README)

### Epic 5: UC-04 Order Fulfillment — Failure Patterns & Resilience
Developers see how Operaton handles failure: configurable error paths, job retry cycles, and process suspension/activation — all engine-level resilience with no application code needed.
**FRs covered:** FR-9, FR-12, DR-15 (UC-04 README)

---

## Epic 5: UC-04 Order Fulfillment — Failure Patterns & Resilience

Developers see how Operaton handles failure: configurable error paths, job retry cycles, and process suspension/activation — all engine-level resilience with no application code needed.

### Story 5.1: Add Payment Failure Path and Job Retry Configuration

As a developer evaluating Operaton,
I want to see a configurable payment failure route through a BPMN error boundary event with job retry,
So that I understand how Operaton handles service failures, error escalation, and retry policies declaratively.

**Acceptance Criteria:**

**Given** `PaymentDelegate.java.jte`
**When** process variable `simulatePaymentFailure = true` is set at process start
**Then** the delegate throws `new BpmnError("PAYMENT_FAILED")` instead of completing normally

**Given** `order-fulfillment.bpmn.jte`
**When** a boundary error event catching `PAYMENT_FAILED` is added to the payment service task
**Then** the error path routes to a "Notify Customer of Failure" end event

**Given** the payment service task in the BPMN
**When** `operaton:failedJobRetryTimeCycle="R3/PT10S"` is declared on the task
**Then** the retry configuration is visible in the BPMN XML and Cockpit shows retries remaining on job failure

**Given** `OrderFulfillmentIT`
**When** a process is started with `simulatePaymentFailure = true`
**Then** the test asserts the process reaches the "Notify Customer of Failure" end event
**And** the `orderStatus` variable equals `FAILED`

### Story 5.2: Demonstrate Process Suspension and Activation

As a developer evaluating Operaton,
I want to see a process instance suspended and reactivated in a test,
So that I understand how operators can pause and resume process execution at runtime.

**Acceptance Criteria:**

**Given** `OrderFulfillmentIT` starts a process instance
**When** `runtimeService.suspendProcessInstanceById(processInstanceId)` is called
**Then** `runtimeService.createProcessInstanceQuery().suspended().processInstanceId(id).count()` returns 1

**Given** the process instance is suspended
**When** `managementService.executeJob(jobId)` is attempted for any active job
**Then** a `SuspendedJobException` is thrown, confirming jobs do not execute while suspended

**Given** `runtimeService.activateProcessInstanceById(processInstanceId)` is called
**When** the process resumes
**Then** the process instance is no longer suspended and continues to completion

**Given** no BPMN changes are required for this story
**When** the story is implemented
**Then** all changes are confined to `OrderFulfillmentIT.java.jte`

### Story 5.3: Add REST API Documentation to Order Fulfillment README

As a developer evaluating Operaton,
I want the order fulfillment README to show REST API usage including process suspension,
So that I understand how ops teams can manage running process instances programmatically.

**Acceptance Criteria:**

**Given** the UC-04 `README.md.jte`
**When** a "REST API" section is added
**Then** it contains `curl` examples for:
- `POST /engine-rest/process-definition/key/order-fulfillment/start` with a realistic variable payload
- `GET /engine-rest/task` filtered by process definition key
- `POST /engine-rest/task/{id}/complete` for the pack-and-ship task
- `PUT /engine-rest/process-instance/{id}/suspended` with body `{"suspended": true}` to demonstrate suspension

---

## Epic 4: UC-03 Incident Management — Signal Escalation

Developers learn signal-based escalation: an external signal fires a parallel second-line task without interrupting the original triage.

### Story 4.1: Add Signal Event Escalation to Incident Management

As a developer evaluating Operaton,
I want an external signal to escalate an incident to second-line support in parallel with ongoing triage,
So that I understand how boundary signal events enable async external triggers without interrupting active tasks.

**Acceptance Criteria:**

**Given** `incident-management.bpmn.jte`
**When** a non-interrupting boundary signal catch event named `EscalationSignal` is added to the "First-Line Triage" user task
**Then** the signal event is wired to a sequence flow that creates the "Second-Line Engineer" user task in parallel

**Given** a process instance is active at the "First-Line Triage" task
**When** `runtimeService.signalEventReceived("EscalationSignal")` is called
**Then** a "Second-Line Engineer" task is created and the "First-Line Triage" task remains active and claimable

**Given** `IncidentManagementIT`
**When** the test sends `runtimeService.signalEventReceived("EscalationSignal")` after process start
**Then** `taskService.createTaskQuery().taskDefinitionKey("Task_SecondLine").count()` returns 1
**And** `taskService.createTaskQuery().taskDefinitionKey("Task_FirstLineTriage").count()` returns 1

**Given** the `incidentPriority` status variable is set on the process (per Epic 1 Story 1.2)
**When** the escalation signal fires
**Then** `execution.setVariable("incidentPriority", "HIGH")` is called on the escalation path

### Story 4.2: Add REST API Documentation to Incident Management README

As a developer evaluating Operaton,
I want the incident management README to show how to drive the process and send signals via REST,
So that I understand how external systems can trigger Operaton signal events programmatically.

**Acceptance Criteria:**

**Given** the UC-03 `README.md.jte`
**When** a "REST API" section is added
**Then** it contains `curl` examples for:
- `POST /engine-rest/process-definition/key/incident-management/start` with a realistic variable payload
- `GET /engine-rest/task` filtered by process definition key
- `POST /engine-rest/signal` with body `{"name": "EscalationSignal"}` to demonstrate signal sending
- `POST /engine-rest/task/{id}/complete` for the triage task

---

## Epic 3: UC-02 Loan Application — Email Integration & Business Keys

Developers see Operaton integrate with external services: rejection triggers a real email visible in Mailpit, and business keys show how to correlate external IDs to process instances.

### Story 3.1: Add Mailpit Infrastructure to UC-02

As a developer evaluating Operaton,
I want a local mail server ready when I run the UC-02 Docker Compose stack,
So that I can observe email notifications without configuring an external SMTP server.

**Acceptance Criteria:**

**Given** the UC-02 `docker-compose.yml.jte`
**When** a Mailpit service is added
**Then** it uses image `axllent/mailpit:latest`, exposes SMTP on port 1025 and web UI on port 8025, and includes `restart: unless-stopped`

**Given** `application.properties.jte` and `application-docker.properties.jte` for UC-02
**When** Spring Mail is configured
**Then** `spring.mail.host=localhost` and `spring.mail.port=1025` are present

**Given** `pom.xml.jte` (or Gradle equivalent) for UC-02
**When** the mail dependency is added
**Then** `spring-boot-starter-mail` is the only new dependency introduced

**Given** `docker compose up` is run for UC-02
**When** the stack starts
**Then** the Mailpit web UI is accessible at `http://localhost:8025` with no authentication required

### Story 3.2: Implement Email Rejection via BPMN Send Task

As a developer evaluating Operaton,
I want loan rejections to send an email via a BPMN Send Task backed by a Spring Mail delegate,
So that I see how Operaton models external notification as a first-class process element.

**Acceptance Criteria:**

**Given** `loan-application.bpmn.jte`
**When** the high-risk rejection path is updated
**Then** the `Auto-Reject Notify` service task is replaced with a Send Task named "Send Rejection Email" using delegate expression `${rejectionEmailDelegate}`

**Given** `RejectionEmailDelegate.java.jte` is implemented
**When** the Send Task executes
**Then** it reads `applicantEmail` from the process instance variables, sends a plain-text email with subject "Loan Application — Decision" containing the applicant name and reason, using `JavaMailSender`

**Given** `DataInitializer.java.jte` for UC-02
**When** a loan application process is started
**Then** `applicantEmail` is available as a process variable (populated from the start form or resolved from the identity service)

**Given** `LoanApplicationIT`
**When** a high-risk application completes the rejection path
**Then** the test verifies the Send Task executed and the process reaches the rejection end event without exception; mail sending verified via `JavaMailSender` mock or Mailpit integration

### Story 3.3: Add Business Key Pattern to Loan Application

As a developer evaluating Operaton,
I want the loan application process to be started with a business key,
So that I understand how to correlate an external application ID to a process instance.

**Acceptance Criteria:**

**Given** `LoanApplicationIT` starts a loan application
**When** `runtimeService.startProcessInstanceByKey(key, businessKey, variables)` is called
**Then** the business key follows the pattern `"LOAN-" + UUID` and is stored on the process instance

**Given** a running loan application process instance
**When** `runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult()` is called
**Then** the correct process instance is returned

**Given** the UC-02 README
**When** the business key section is read
**Then** it explains what business keys are, why they matter for external system correlation, and shows the query API example

### Story 3.4: Mailpit Documentation and REST API Examples for UC-02

As a developer evaluating Operaton,
I want the loan application README to explain email testing and show REST API usage,
So that I can explore the full use case without needing additional guidance.

**Acceptance Criteria:**

**Given** the UC-02 `README.md.jte`
**When** an "Email Testing with Mailpit" section is added
**Then** it states the web UI URL `http://localhost:8025`, lists all sample user email addresses from `DataInitializer`, and explains that high-risk applications trigger a rejection email to the applicant

**Given** the UC-02 `README.md.jte`
**When** a "REST API" section is added
**Then** it contains `curl` examples for:
- `POST /engine-rest/process-definition/key/loan-application/start` with a realistic variable payload including `applicantEmail`
- `GET /engine-rest/task` filtered by process definition key
- `POST /engine-rest/task/{id}/complete` for the underwriter review task

---

## Epic 2: UC-01 Leave Request — Rich Task Experience

Developers experience a complete human workflow: task forms surface all relevant data, timer escalation handles non-response, and the History API shows how variable state is audited.

### Story 2.1: Set Leave Request Process Variables and Add Embedded Task Forms

As a developer evaluating Operaton,
I want the leave request task forms to display all relevant leave data,
So that I understand how process variables drive task form content in Operaton Tasklist.

**Acceptance Criteria:**

**Given** a leave request is started with `startDate`, `endDate`, and requester identity
**When** the process start listener or service task runs
**Then** process variables `startDate` (ISO-8601 string), `endDate` (ISO-8601 string), `days` (int, calendar days inclusive), and `remainingVacationDays` (int, fetched from `VacationBalanceService` before any deduction) are set on the process instance

**Given** the "Manager Reviews Request" user task
**When** a manager opens the task in Operaton Tasklist
**Then** an embedded HTML form (referenced via `operaton:formKey`) displays read-only fields: Requester, Start Date, End Date, Days Requested, Remaining Vacation Days

**Given** the "HR Records Approved Leave" user task
**When** HR opens the task in Operaton Tasklist
**Then** the same five fields are displayed read-only in an embedded HTML form

**Given** `LeaveRequestIT`
**When** the process is started with known dates
**Then** the test asserts all four variables are present on the process instance with correct values

### Story 2.2: Add Timer Boundary Escalation to Manager Review Task

As a developer evaluating Operaton,
I want to see a non-responding manager task escalate automatically via a timer,
So that I understand how non-interrupting timer boundary events handle time-based escalation.

**Acceptance Criteria:**

**Given** the "Manager Reviews Request" user task in `leave-request.bpmn.jte`
**When** a non-interrupting timer boundary event is added with duration defaulting to `PT72H`
**Then** the timer duration is overridable via process variable `managerReviewTimeout` at process start

**Given** the timer fires
**When** the escalation path executes
**Then** a reminder service task delegate runs and sets process variable `escalated = true`; the manager task remains active and claimable

**Given** `LeaveRequestIT` sets `managerReviewTimeout = PT1S` at process start
**When** the test executes the timer job via `managementService.executeJob`
**Then** the test asserts `escalated = true` is set and the manager task is still active

### Story 2.3: Demonstrate Task-Local Variables and History API

As a developer evaluating Operaton,
I want to see how task-local variables are scoped and how the History API retrieves past variable state,
So that I understand variable scoping and process auditing patterns.

**Acceptance Criteria:**

**Given** the manager completes "Manager Reviews Request"
**When** `taskService.setVariableLocal(taskId, "approvalComment", comment)` is called before `taskService.complete(taskId)`
**Then** `approvalComment` is stored scoped to that task instance and does not appear as a process-level variable

**Given** `LeaveRequestIT` completes the manager task with a comment
**When** `historyService.createHistoricVariableInstanceQuery().taskIdIn(taskId).list()` is called
**Then** the query returns `approvalComment` with the value that was set

**Given** `LeaveRequestIT` runs the full approved path through to HR completion
**When** `historyService.createHistoricVariableInstanceQuery().variableName("remainingVacationDays").singleResult()` is called
**Then** the historic value of `remainingVacationDays` recorded at process start is greater than the balance after the `VacationBalanceService` deduction

### Story 2.4: Add REST API Documentation to Leave Request README

As a developer evaluating Operaton,
I want the leave request README to show how to drive the process via REST,
So that I know I can automate and script Operaton without using the Tasklist UI.

**Acceptance Criteria:**

**Given** the leave request `README.md.jte`
**When** a "REST API" section is added
**Then** it contains working `curl` examples for:
- `POST /engine-rest/process-definition/key/leave-request/start` with a realistic variable payload (startDate, endDate, days)
- `GET /engine-rest/task` filtered by process definition key
- `POST /engine-rest/task/{id}/complete` with an approval decision variable

**Given** the curl examples reference user credentials
**When** they are read
**Then** they use alice's credentials and the correct process key `leave-request`

---

## Epic 1: Authorization Foundation

Developers see realistic role-based access enforced by the engine, with process state visible in Cockpit without reading a BPMN. Story 1.1 must complete before 1.2.

### Story 1.1: Add Process Start Authorization to All Use Cases

As a developer evaluating Operaton,
I want each use case process to only be startable by the designated role,
So that I understand how `candidateStarterGroups` enforces role-based access at the engine level.

**Acceptance Criteria:**

**Given** the leave-request, loan-application, and incident-management BPMNs
**When** `operaton:candidateStarterGroups` is declared on each `<process>` element with value `employees`
**Then** only users in the `employees` group can start those three processes in Operaton Tasklist

**Given** the order-fulfillment BPMN
**When** `operaton:candidateStarterGroups="sales"` is declared and UC-04 `DataInitializer` creates group `sales` and user `frank` (frank@example.com, password `frank`) assigned to `sales`
**Then** only frank can start the order fulfillment process

**Given** a user not in the designated starter group (e.g. bob trying to start order fulfillment)
**When** they attempt to start the process via Tasklist
**Then** the process is not listed as startable for that user

**Given** each use case README
**When** the README is read
**Then** it documents which user to log in as to start the process

### Story 1.2: Audit User Task Authorization and Add Cockpit Status Variables

As a developer evaluating Operaton,
I want every user task to have a declared candidate group and every process to set a readable status variable,
So that I see complete authorization coverage and can read process state in Cockpit without opening the BPMN.

**Acceptance Criteria:**

**Given** all four use case BPMNs
**When** audited for `operaton:candidateGroups` on every `<userTask>`
**Then** all existing tasks carry the correct groups (audit confirms no gaps); any gap found must be filled before the story is complete

**Given** each use case reaches a key state transition
**When** `execution.setVariable()` is called in the appropriate delegate or listener
**Then** the status variable is visible in Cockpit's variable view with a human-readable value:
- UC-01: `leaveStatus` → `PENDING` on start, `APPROVED` or `REJECTED` on decision
- UC-02: `loanDecision` → `PENDING` on start, `APPROVED` or `REJECTED` on outcome
- UC-03: `incidentPriority` → `LOW` on start, `HIGH` on signal escalation
- UC-04: `orderStatus` → `RECEIVED` on start, `FULFILLED` or `FAILED` on outcome

**Given** an integration test for any one use case
**When** the process reaches its terminal state
**Then** the test asserts the status variable holds the expected final value
