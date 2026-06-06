---
title: 'Leave request balances and detailed use-case READMEs'
type: 'feature'
created: '2026-06-06T12:14:39.475+02:00'
baseline_commit: 'b405943'
status: 'done'
context: []
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** The leave-request example still starts with only a requester variable and has no vacation-balance rules, so it does not demonstrate realistic leave approval behavior. The use-case READMEs also explain setup, but they do not yet walk developers through each process in enough detail to serve as self-contained learning guides.

**Approach:** Extend the leave-request generated project with explicit start inputs, persistent per-employee vacation balances, automatic validation/rejection rules, and balance deduction on approval completion. Rewrite the four use-case README templates so each one explains the process flow, participants, key business rules, and how the generated project behaves during each path.

## Boundaries & Constraints

**Always:** Preserve the existing use-case generation flow and template structure under `starter-templates/src/main/jte/use-cases/`. Keep the leave-request example runnable with its current PostgreSQL default and H2 test profile. Persist remaining vacation days across multiple process instances for the same employee. Require two start inputs for leave requests: `startDate` and `durationDays`. Enforce `startDate >= today`, reject requests that exceed the employee’s remaining balance automatically, and reduce the employee’s persisted balance when an approved request is finalized. Update README templates for UC-01 through UC-04 with detailed process explanations, not just setup steps.

**Ask First:** Any change that would alter the number of supported use cases, replace Tasklist-driven interaction with custom UI, or require new infrastructure beyond what each example already uses.

**Never:** Do not add a bespoke frontend for generated examples. Do not fake vacation balances with request-local variables only. Do not remove the current manager/HR/employee approval flow from the leave-request BPMN. Do not leave README detail hardcoded outside the template files that generate each project.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| VALID_REQUEST | Alice starts a request with `startDate` today or later and `durationDays` <= her remaining balance | Process reaches Bob’s manager review task; request variables include the requested dates/duration and current balance context | N/A |
| OVER_BALANCE | Employee requests more days than their persisted remaining balance | Process skips manager approval and follows an automatic rejection path with a rejection reason visible to the employee | No silent fallback; store a clear rejection reason in process context |
| PAST_START_DATE | Employee starts a request with a date before today | Request is rejected automatically before manager review | Validation result must be explicit and testable |
| APPROVED_REQUEST | Bob approves and HR finalizes a valid request | Persisted remaining balance is reduced by `durationDays`; process ends cleanly | If balance update cannot be applied, fail explicitly rather than pretending success |
| README_GUIDE | Developer opens any generated use-case README | README explains participants, happy path, alternate paths, core business rules, and how to exercise the flow locally | N/A |

</frozen-after-approval>

## Code Map

- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte` -- leave-request workflow definition and start/form/process routing
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/Application.java.jte` -- Spring Boot wiring for any leave-request support beans
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/DataInitializer.java.jte` -- bootstrap identities and persistent vacation-balance seed data
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application.properties.jte` -- runtime configuration for the leave-request example
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application-h2.properties.jte` -- H2 profile support for tests/local fallback
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte` -- integration coverage for validation, rejection, approval, and balance deduction
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte` -- detailed generated guide for UC-01
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte` -- detailed generated guide for UC-02
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte` -- detailed generated guide for UC-03
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte` -- detailed generated guide for UC-04
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` -- ensure all new leave-request support files are emitted into generated projects

## Tasks & Acceptance

**Execution:**
- [x] `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte` -- add explicit start inputs plus automatic validation/rejection and approval finalization flow -- the BPMN must reflect the new leave rules
- [x] `starter-templates/src/main/jte/use-cases/uc-01-leave-request/Application.java.jte` and `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application*.jte` -- wire any required leave-request services/config needed for date validation and balance persistence -- generated apps must run in both default and H2 modes
- [x] `starter-templates/src/main/jte/use-cases/uc-01-leave-request/DataInitializer.java.jte` -- seed persistent 30-day vacation balances alongside identity bootstrap data -- leave balances must exist before the first request
- [x] `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte` -- cover valid approval, automatic rejection for past dates, automatic rejection for exceeded balance, and balance deduction across multiple requests -- the new business rules must stay regression-safe
- [x] `starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte` -- explain the full leave-request process, inputs, validation rules, auto-rejection behavior, and balance updates -- generated docs should teach the scenario, not just launch it
- [x] `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte`, `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte`, `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte` -- expand each README with detailed process-flow explanations and key decision/escalation/orchestration behavior -- every use case should read like a guided walkthrough
- [x] `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` -- emit any new leave-request support files created for this feature -- generated archives must remain complete

**Acceptance Criteria:**
- Given a generated leave-request project, when a developer starts a process instance, then the start contract exposes `startDate` and `durationDays` and the process stores both values for later tasks.
- Given a leave request whose `startDate` is before today, when the process starts, then it is rejected automatically before any manager task is created.
- Given an employee with fewer remaining vacation days than requested, when the process starts, then the request is rejected automatically and the rejection path is visible to the employee.
- Given two leave requests for the same employee, when the first valid request is approved and finalized, then the persisted remaining balance used by the second request reflects the deducted days.
- Given a generated README for any of the four use cases, when a developer reads it, then it explains the actors, main flow, alternate paths, and the important business/technical rules of that example in detail.

## Spec Change Log

## Design Notes

Use the generated project itself to own the leave-balance behavior instead of hiding it in generation-time data. A small application-level persistence helper plus BPMN service-task wiring keeps the example teachable: developers can inspect one table/source of truth for vacation balances and one process for orchestration.

For leave-request validation, prefer engine-visible process variables such as `remainingVacationDays`, `validationPassed`, and `rejectionReason` so tests and Tasklist/Cockpit inspection can verify the behavior directly.

## Verification

**Commands:**
- `cd starter-web && npm run test:unit` -- expected: existing frontend tests still pass after README/template type changes, if any are touched indirectly
- `mvn -q verify` -- expected: all repository tests pass, including updated generated-template coverage

## Suggested Review Order

**Workflow entry and routing**

- Start here: new contract, validation gate, and approval finalization are visible in one diagram.
  [`leave-request.bpmn.jte:12`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte#L12)

- Validation now loads balances, normalizes inputs, and turns invalid starts into explicit rejections.
  [`LeaveRequestValidationDelegate.java.jte:16`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/LeaveRequestValidationDelegate.java.jte#L16)

- HR finalization is the only place that mutates persisted balances.
  [`FinalizeLeaveApprovalDelegate.java.jte:11`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/FinalizeLeaveApprovalDelegate.java.jte#L11)

**Persistence and seed data**

- This service owns balance reads, seeding, and guarded deductions.
  [`VacationBalanceService.java.jte:12`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/VacationBalanceService.java.jte#L12)

- Identity bootstrap now seeds the initial employee balance alongside users and groups.
  [`DataInitializer.java.jte:13`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/DataInitializer.java.jte#L13)

- The generated schema adds the vacation-balance source of truth.
  [`schema.sql.jte:3`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/schema.sql.jte#L3)

**Generation surface**

- Archive generation now emits the new UC-01 support classes and schema.
  [`GenerationEngine.java:105`](../../../starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java#L105)

- API metadata and manifest now advertise the richer leave-request example correctly.
  [`MetadataController.java:55`](../../../starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java#L55)

**Regression coverage and docs**

- Integration coverage locks in valid, rejected, and cross-request balance scenarios.
  [`LeaveRequestIT.java.jte:50`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte#L50)

- Archive tests verify new generated files and README walkthrough sections.
  [`GenerationEngineTest.java:401`](../../../starter-templates/src/test/java/org/operaton/dev/starter/templates/GenerationEngineTest.java#L401)

- UC-01 README now explains actors, paths, rules, and local exercise steps.
  [`README.md.jte:16`](../../../starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte#L16)

- The other use-case templates now follow the same guided walkthrough structure.
  [`README.md.jte:11`](../../../starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte#L11)
