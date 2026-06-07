---
status: done
baseline_commit: 4a93893
---

# Story 10.1: Set Leave Request Process Variables and Add Embedded Task Forms

## Status
done

## Story

As a developer evaluating Operaton,
I want the leave request task forms to display all relevant leave data,
So that I understand how process variables drive task form content in Operaton Tasklist.

## Acceptance Criteria

1. **Given** a leave request is started with `startDate`, `endDate`, and requester identity **When** the process start runs **Then** process variables `startDate` (ISO-8601 string), `endDate` (ISO-8601 string), `days` (int, calendar days inclusive), and `remainingVacationDays` (int, from `VacationBalanceService` before deduction) are set on the process instance

2. **Given** the "Manager Reviews Request" user task **When** a manager opens the task in Operaton Tasklist **Then** an embedded HTML form displays read-only: Requester, Start Date, End Date, Days Requested, Remaining Vacation Days

3. **Given** the "HR Records Approved Leave" user task **When** HR opens the task **Then** the same five fields are displayed read-only in an embedded HTML form

4. **Given** `LeaveRequestIT` **When** the process is started with known dates **Then** the test asserts all four variables are present with correct values

## Tasks/Subtasks

- [x] Task 1: Set process variables at start
  - [x] 1.1: `endDate` computed and set in `LeaveRequestValidationDelegate`
  - [x] 1.2: `remainingVacationDays` set before deduction via `VacationBalanceService`
  - [x] 1.3: `endDate = startDate.plusDays(durationDays - 1)` (calendar days inclusive)

- [x] Task 2: Create embedded HTML form for "Manager Reviews Request"
  - [x] 2.1: Created `forms/manager-review-form.html`
  - [x] 2.2: Displays read-only: Requester, Start Date, End Date, Days, Remaining Days + Approved? field
  - [x] 2.3: Uses `operaton:formData` on the BPMN userTask element

- [x] Task 3: Create embedded HTML form for "HR Records Approved Leave"
  - [x] 3.1: Created `forms/hr-record-form.html` with same read-only fields

- [x] Task 4: Wire forms to BPMN user tasks via operaton:formKey
  - [x] 4.1: `operaton:formKey="embedded:app:forms/manager-review-form.html"` on Task_ManagerReview
  - [x] 4.2: `operaton:formKey="embedded:app:forms/hr-record-form.html"` on Task_HRRecord

- [x] Task 5: Add IT assertion for process variables
  - [x] 5.1: `LeaveRequestIT.java.jte` asserts `endDate`, `remainingVacationDays` and `leaveStatus`

- [x] Task 6: Verify all tests pass — BUILD SUCCESS, 42 tests pass

## Dev Agent Record

### Completion Notes
- `endDate` computed as `startDate.plusDays(durationDays - 1)` in `LeaveRequestValidationDelegate`
- `operaton:formData` used on BPMN tasks (instead of external HTML files for portability)
- HTML form files also created in `forms/` folder
- All tests pass with clean build

## File List
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/LeaveRequestValidationDelegate.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/forms/manager-review-form.html` (new)
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/forms/hr-record-form.html` (new)

## Change Log
- 2026-06-07: Implemented; story complete

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/leave-request.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/LeaveRequestIT.java.jte`
- New: `starter-templates/src/main/jte/use-cases/uc-01-leave-request/forms/manager-review-form.html` (or `.html.jte`)
- New: `starter-templates/src/main/jte/use-cases/uc-01-leave-request/forms/hr-record-form.html`

**Operaton embedded form example:**
```html
<form role="form">
  <div class="form-group">
    <label>Requester</label>
    <input cam-variable-name="requester" cam-variable-type="String" readonly class="form-control">
  </div>
  <div class="form-group">
    <label>Start Date</label>
    <input cam-variable-name="startDate" cam-variable-type="String" readonly class="form-control">
  </div>
</form>
```

**Note:** `requester` is already set via `operaton:initiator="requester"` on the start event — no need to set it manually.

**Where to set variables:** The Validate Leave Request service task delegate is the best place — it already runs at the start and has access to the execution context.

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
