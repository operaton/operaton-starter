@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton example for **incident management with SLA escalation** using timer boundary events and service-task integrations.

## Scenario

The process starts when a caller reports an `incidentId`. First-line support gets an initial chance to resolve the issue. If the SLA expires, an interrupting timer boundary event escalates the work to second-line engineering and triggers a post-mortem notification path.

## Actors

| Actor | Username | Group | Responsibility |
|------|----------|-------|----------------|
| Incident reporter | `alice` / `alice` | `employees` | Starts the incident process |
| First-line support | `frank` / `frank` | `first-line` | Performs initial triage and can resolve the incident before the SLA fires |
| Second-line engineer | `grace` / `grace` | `second-line` | Handles escalated incidents after the timer interrupts first-line work |
| Automation | system | n/a | Calls ticket-close and post-mortem notification endpoints |

## Starting the Process

Log in as **alice / alice** (group: `employees`) in Tasklist to start this process. Only members of the `employees` group can report an incident — this is enforced by `candidateStarterGroups` on the process definition.

## Main Flow

1. A caller starts `incident-management` with `incidentId`.
2. **First-Line Triage** is created for the `first-line` group.
3. At the same time, an interrupting boundary timer is scheduled using `timer.escalation.duration`.
4. If Frank completes the task with `resolved = true` before the timer fires, the process calls `CloseTicketDelegate` and ends.
5. If the timer fires first, the first-line task is interrupted and the process moves to **Second-Line Engineer**.
6. Grace completes the second-line task and `PostMortemDelegate` sends the follow-up notification before the process ends.

## Alternate Paths

### Resolved before SLA breach

This is the fastest happy path. First-line support resolves the issue, the ticket-close service is called, and no escalation or post-mortem path is needed.

### Escalated after SLA breach

If the timer fires, Operaton cancels the original first-line task automatically because the boundary event is interrupting. A new task appears for the `second-line` group and the post-mortem notification is sent after completion.

## SLA Notes

- The SLA is modeled in BPMN, not in Java polling code.
- `timer.escalation.duration` controls how long first-line support has before escalation.
- The boundary timer is interrupting, so only one active human task exists after escalation.
- The generated `application-test.properties` shortens the timer to keep automated tests fast.

## Getting Started

### Prerequisites

- Java ${config.javaVersion()}+
- Maven wrapper included (`./mvnw`)
- Docker (PostgreSQL + WireMock in the default profile)

### 1. Start dependencies

```bash
docker compose up -d
```

### 2. Run the application

```bash
chmod +x mvnw && ./mvnw spring-boot:run
```

### 3. Open the web apps

- Tasklist: http://localhost:${config.serverPort()}/operaton/app/tasklist
- Cockpit: http://localhost:${config.serverPort()}/operaton/app/cockpit

## Local Walkthrough

### Resolved path

1. Start an instance:
   ```bash
   curl -s -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/incident-management/start \
     -H "Content-Type: application/json" \
     -d '{"variables": {"incidentId": {"value": "INC-001", "type": "String"}}}'
   ```
2. Log in as `frank / frank`.
3. Claim **First-Line Triage** and complete it with `resolved = true`.
4. The workflow calls the close-ticket stub and ends.

### Escalation path

Run the app with a shorter timer for a quick demo:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--timer.escalation.duration=PT10S
```

Start a new incident and wait for the timer to fire. The first-line task disappears and Grace (`grace / grace`) receives **Second-Line Engineer**.

## Important Business Rules

- First-line resolution requires `resolved = true`
- Any unresolved or timed-out incident escalates
- Escalation also triggers a post-mortem notification step
- Tests advance Operaton's internal clock instead of using `Thread.sleep`

## Project Structure

```
src/
  main/
    java/${config.packagePath()}/
      Application.java
      DataInitializer.java
      delegate/
        CloseTicketDelegate.java
        PostMortemDelegate.java
    resources/
      incident-management.bpmn
      application-test.properties
      wiremock/mappings/
  test/
    java/${config.packagePath()}/
      IncidentManagementIT.java
```

## REST API

All process interactions are available through the Operaton REST API. The examples below use the default port `8080`.

### Start a process instance

```bash
curl -u alice:alice -X POST \
  http://localhost:8080/engine-rest/process-definition/key/incident-management/start \
  -H 'Content-Type: application/json' \
  -d '{"variables": {"incidentId": {"value": "INC-001", "type": "String"}}}'
```

### List active tasks

```bash
curl -u alice:alice \
  'http://localhost:8080/engine-rest/task?processDefinitionKey=incident-management'
```

### Send a signal to escalate in parallel (key pattern)

The `EscalationSignal` triggers the non-interrupting boundary signal event on the First-Line Triage task. The triage task **remains active** while a Second-Line Engineer task is created concurrently and `incidentPriority` is set to `HIGH`.

```bash
curl -u alice:alice -X POST \
  http://localhost:8080/engine-rest/signal \
  -H 'Content-Type: application/json' \
  -d '{"name": "EscalationSignal"}'
```

### Complete the first-line triage task

Replace `{taskId}` with the ID returned by the task list call above.

```bash
curl -u frank:frank -X POST \
  http://localhost:8080/engine-rest/task/{taskId}/complete \
  -H 'Content-Type: application/json' \
  -d '{"variables": {"resolved": {"value": true, "type": "Boolean"}}}'
```

## Running Tests

```bash
./mvnw verify
```

`IncidentManagementIT` verifies deployment, the resolved path, timer-driven escalation to second-line support, and signal-driven parallel escalation.
