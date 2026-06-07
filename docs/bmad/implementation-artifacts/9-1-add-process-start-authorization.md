---
status: done
baseline_commit: 4a93893
---

# Story 9.1: Add Process Start Authorization to All Use Cases

## Status
done

## Story

As a developer evaluating Operaton,
I want each use case process to only be startable by the designated role,
So that I understand how `candidateStarterGroups` enforces role-based access at the engine level.

## Acceptance Criteria

1. **Given** the leave-request, loan-application, and incident-management BPMNs **When** `operaton:candidateStarterGroups` is declared on each `<process>` element with value `employees` **Then** only users in the `employees` group can start those three processes in Operaton Tasklist

2. **Given** the order-fulfillment BPMN **When** `operaton:candidateStarterGroups="sales"` is declared and UC-04 `DataInitializer` creates group `sales` and user `frank` (frank@example.com, password `frank`) assigned to `sales` **Then** only frank can start the order fulfillment process

3. **Given** a user not in the designated starter group **When** they attempt to start the process via Tasklist **Then** the process is not listed as startable for that user

4. **Given** each use case README **When** the README is read **Then** it documents which user to log in as to start the process

## Tasks/Subtasks

- [x] Task 1: Add candidateStarterGroups to UC-01, UC-02, UC-03 BPMNs
  - [x] 1.1: In `leave-request.bpmn.jte` add `operaton:candidateStarterGroups="employees"` to the `<process>` element
  - [x] 1.2: In `loan-application.bpmn.jte` add `operaton:candidateStarterGroups="employees"` to the `<process>` element
  - [x] 1.3: In `incident-management.bpmn.jte` add `operaton:candidateStarterGroups="employees"` to the `<process>` element

- [x] Task 2: Add candidateStarterGroups to UC-04 BPMN and create sales group/user
  - [x] 2.1: In `order-fulfillment.bpmn.jte` add `operaton:candidateStarterGroups="sales"` to the `<process>` element
  - [x] 2.2: In `uc-04-order-fulfillment/DataInitializer.java.jte` add `createGroupIfAbsent("sales", "Sales", "WORKFLOW")`, `createUserIfAbsent("frank", "Frank", "Sales", "frank@example.com", "frank")`, and `createMembershipIfAbsent("frank", "sales")`

- [x] Task 3: Update READMEs with login instructions
  - [x] 3.1: Add a "Starting the Process" note to UC-01 README: log in as alice (employees group)
  - [x] 3.2: Add a "Starting the Process" note to UC-02 README: log in as alice (employees group)
  - [x] 3.3: Add a "Starting the Process" note to UC-03 README: log in as alice (employees group)
  - [x] 3.4: Add a "Starting the Process" note to UC-04 README: log in as frank (sales group)

- [x] Task 4: Verify all existing integration tests still pass
  - [x] 4.1: Run `./mvnw verify` or `./gradlew test` for each use case; all tests must pass

## Dev Notes

**Key files to modify:**
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/DataInitializer.java.jte`
- All four `README.md.jte` files

**Pattern:** The `candidateStarterGroups` attribute goes on the `<process>` element, not the start event. Example:
```xml
<process id="leave-request" name="Leave Request Approval" isExecutable="true"
         operaton:candidateStarterGroups="employees">
```

**UC-04 DataInitializer pattern** — follow the exact same helper methods already used in the file (`createGroupIfAbsent`, `createUserIfAbsent`, `createMembershipIfAbsent`).

**These are JTE template files** — the generated output is what matters. No logic changes, just attribute/data additions.

## Dev Agent Record

### Completion Notes
- UC-01 already had candidateStarterGroups; UC-02, UC-03, UC-04 added
- UC-02 and UC-03 DataInitializers updated to include `employees` group and `alice` user
- UC-04 DataInitializer updated with `sales` group and `frank` user
- All four READMEs updated with "Starting the Process" section and corrected Actors table
- All template tests pass

## File List
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/incident-management.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/DataInitializer.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/DataInitializer.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/DataInitializer.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/README.md.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte`
- `starter-templates/src/main/jte/use-cases/uc-03-incident-management/README.md.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/README.md.jte`

## Change Log
- 2026-06-07: Implemented all tasks; story complete
