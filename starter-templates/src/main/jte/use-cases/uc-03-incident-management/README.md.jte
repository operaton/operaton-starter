@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton BPM example demonstrating timer boundary events and SLA-driven task escalation.

## The Scenario

**Frank** handles first-line support. When an incident is reported, he has **1 hour** to resolve it.

- If **Frank resolves** it in time, the ticket is closed via REST (WireMock-stubbed).
- If the **SLA timer fires**, the task escalates to **Grace** (second-line engineering) who investigates and sends a post-mortem notification.

## Getting Started in 5 Minutes

### Prerequisites

- Java ${config.javaVersion()}+
- Maven (or use the included `./mvnw`)
- Docker (for WireMock)

### 1. Start WireMock

```bash
docker compose up -d
```

This starts WireMock on port 8089, serving stubs for close-ticket and post-mortem endpoints.

### 2. Start the Application

```bash
./mvnw spring-boot:run
```

The application starts on port ${config.serverPort()}. Open:
- **Tasklist**: http://localhost:${config.serverPort()}/operaton/app/tasklist
- **Cockpit**: http://localhost:${config.serverPort()}/operaton/app/cockpit

### 3. Start an Incident

```bash
curl -s -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/incident-management/start \
  -H "Content-Type: application/json" \
  -d '{"variables": {"incidentId": {"value": "INC-001", "type": "String"}}}'
```

### 4. Resolve as Frank

Log into Tasklist as `frank` / `frank`. The **First-Line Triage** task appears. Complete it with `resolved = true` to close the ticket.

### 5. Test the Escalation Path

For a quick local demo, restart the app with a shorter SLA such as `--timer.escalation.duration=PT10S`, then wait 10 seconds. Check Cockpit — the task should appear in Grace's `second-line` inbox.

## Timer Override for Tests

`application-test.properties` overrides `timer.escalation.duration=PT5S`. Tests use `ClockUtil.setCurrentTime(...)` to advance Operaton's internal clock — no `Thread.sleep` needed.

## Running Tests

```bash
./mvnw verify
```

Tests use Testcontainers to start WireMock automatically — no Docker Compose needed for CI.

## Users

| Username | Password | Group       | Role                  |
|----------|----------|-------------|-----------------------|
| frank    | frank    | first-line  | First-line support    |
| grace    | grace    | second-line | Second-line engineer  |
