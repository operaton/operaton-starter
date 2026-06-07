@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton example for a realistic **leave request approval workflow** with persisted vacation balances.

## Scenario

An employee starts a leave request with two explicit business inputs:

- `startDate` — the first requested day off
- `durationDays` — how many vacation days should be consumed

The process validates those inputs immediately, routes valid requests to the manager, lets HR finalize approved leave, and keeps the employee informed when a request is rejected automatically or manually.

## Actors

| Actor | Username | Group | Responsibility |
|------|----------|-------|----------------|
| Employee | `alice` / `alice` | `employees` | Starts leave requests and receives rejection notifications |
| Manager | `bob` / `bob` | `managers` | Approves or rejects valid leave requests |
| HR | `carol` / `carol` | `hr` | Finalizes approved leave so the persisted balance is reduced |
| Admin | `admin` / `admin` | `operaton-admin` | Operaton administration only |

## Starting the Process

Log in as **alice / alice** (group: `employees`) in Tasklist to start this process. Members of the `employees` group can initiate a leave request, and the seeded `admin / admin` account can also start it directly because `candidateStarterGroups` includes `operaton-admin`.

## Main Flow

1. Alice starts the **leave-request** process while authenticated in Tasklist or Cockpit.
2. The start contract captures `startDate` and `durationDays`.
3. `LeaveRequestValidationDelegate` loads Alice's persisted balance from `employee_vacation_balance`.
4. If the request is valid, Bob receives **Manager Reviews Request**.
5. If Bob approves, Carol receives **HR Records Approved Leave**.
6. When Carol completes the HR task, `FinalizeLeaveApprovalDelegate` deducts `durationDays` from Alice's persisted balance.
7. The process ends with **Leave Approved** and the updated balance is visible for the next request.

## Alternate Paths

### Automatic rejection — start date is in the past

If `startDate` is earlier than today, the process never creates a manager task. Instead:

1. Validation sets `validationPassed = false`
2. `rejectionReason` is set to `Start date must be today or later.`
3. Alice receives **Employee Notified of Rejection**

### Automatic rejection — request exceeds remaining balance

If `durationDays` is greater than the employee's persisted `remainingVacationDays`, the process also skips manager review and routes directly to the rejection notification task. The rejection reason includes both the requested duration and the available balance so the failure is visible in Cockpit, Tasklist variables, and automated tests.

### Manual rejection — manager declines a valid request

If Bob completes the manager task with `approved = false`, Alice still receives **Employee Notified of Rejection**, but the persisted vacation balance does not change because HR finalization never happens.

## Important Business Rules

- Every seeded employee starts with **30 vacation days** persisted in `employee_vacation_balance`.
- Validation writes engine-visible variables: `remainingVacationDays`, `validationPassed`, and `rejectionReason`.
- A request is only balance-impacting after HR finalization, not when the manager approves.
- Balance deduction is persisted in the database, so a second leave request for the same employee sees the reduced balance immediately.
- Missing balance data or a failed deduction raises an explicit error instead of pretending success.

## Getting Started

### Prerequisites

- Java ${config.javaVersion()} or later
- Maven wrapper included (`./mvnw`)
- Docker (for PostgreSQL in the default profile)

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Run the application

```bash
chmod +x mvnw && ./mvnw spring-boot:run
```

The application starts on port ${config.serverPort()}.

### 3. Open the web apps

- Tasklist: http://localhost:${config.serverPort()}/operaton/app/tasklist
- Cockpit: http://localhost:${config.serverPort()}/operaton/app/cockpit

## Local Walkthrough

### Happy path

1. Log in as `alice / alice`.
2. Start **Leave Request Approval** and enter:
   - `startDate` = today or later
   - `durationDays` = any value up to 30
3. Log in as `bob / bob`, claim **Manager Reviews Request**, and complete it with `approved = true`.
4. Log in as `carol / carol`, claim **HR Records Approved Leave**, and complete it.
5. Inspect the persisted balance or start a second request — Alice's available vacation days are now reduced for the next request.

### Past-date rejection

Start the process with `startDate` before today. No manager task is created. Alice immediately sees **Employee Notified of Rejection** with `rejectionReason = Start date must be today or later.`

### Over-balance rejection

After approving one valid request, start another request for Alice with `durationDays` greater than the updated balance. The second instance is rejected automatically before manager review because the persisted balance was already reduced by the first approved request.

## REST API

You can drive the entire leave request process via the Operaton REST API without using the Tasklist UI.

### Start a leave request (as Alice)

```bash
curl -u alice:alice -X POST \
  http://localhost:${config.serverPort()}/engine-rest/process-definition/key/leave-request/start \
  -H 'Content-Type: application/json' \
  -d '{
    "variables": {
      "startDate": {"value": "2030-07-01", "type": "String"},
      "durationDays": {"value": 5, "type": "Integer"}
    }
  }'
```

### List active tasks for the leave-request process

```bash
curl -u alice:alice \
  'http://localhost:${config.serverPort()}/engine-rest/task?processDefinitionKey=leave-request'
```

### Complete the manager review task (approve, as Bob)

Replace `{taskId}` with the task ID returned by the list tasks call:

```bash
curl -u bob:bob -X POST \
  http://localhost:${config.serverPort()}/engine-rest/task/{taskId}/complete \
  -H 'Content-Type: application/json' \
  -d '{"variables": {"approved": {"value": true, "type": "Boolean"}}}'
```

### Complete the HR records task (as Carol)

```bash
curl -u carol:carol -X POST \
  http://localhost:${config.serverPort()}/engine-rest/task/{taskId}/complete \
  -H 'Content-Type: application/json' \
  -d '{}'
```

> **Note:** Process start requires Alice (`employees` group). Manager tasks are accessible to Bob (`managers` group). HR tasks require Carol (`hr` group).

## API Example

If you want to start a request via REST instead of the UI, authenticate as Alice so the process initiator is stored in `requester`:

```bash
curl -u alice:alice -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/leave-request/start \
  -H 'Content-Type: application/json' \
  -d '{"variables": {"startDate": {"value": "2030-06-01", "type": "String"}, "durationDays": {"value": 5, "type": "Integer"}}}'
```

## Persistence Model

- `schema.sql` creates the `employee_vacation_balance` table.
- `DataInitializer` seeds users, groups, memberships, and Alice's initial 30-day balance.
- `VacationBalanceService` is the single source of truth for reading and deducting balances.

## Project Structure

```
src/
  main/
    java/${config.packagePath()}/
      Application.java
      DataInitializer.java
      VacationBalanceService.java
      delegate/
        LeaveRequestValidationDelegate.java
        FinalizeLeaveApprovalDelegate.java
    resources/
      leave-request.bpmn
      schema.sql
      application.properties
      application-h2.properties
  test/
    java/${config.packagePath()}/
      LeaveRequestIT.java
```

## Running Tests

```bash
./mvnw test
```

`LeaveRequestIT` covers start-form inputs, past-date rejection, balance-limit rejection, manager rejection, approval finalization, and persisted deduction across multiple requests.
