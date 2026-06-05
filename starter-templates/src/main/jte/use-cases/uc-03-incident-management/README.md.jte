@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton BPM example demonstrating timer boundary events and SLA-driven task escalation.

![Process Model](docs/process-model.png)

## The Scenario

**Henry** reports an incident. **Iris** is the second-line engineer who takes over if the SLA fires. When an incident is reported, the first-line team has **1 hour** to resolve it.

- If the first-line team resolves it in time, the ticket is closed via REST (WireMock-stubbed).
- If the **SLA timer fires**, the task escalates to **Iris** (second-line engineering) who investigates and sends a post-mortem notification.

## Getting Started in 5 Minutes

### Prerequisites

- Java ${config.javaVersion()}+
- Maven (or use the included `./mvnw`)
- Docker (for Postgres and WireMock)

### 1. Start services

```bash
docker compose up -d
```

This starts Postgres on port 5432 and WireMock on port 8089, serving stubs for close-ticket and post-mortem endpoints.

### 2. Start the Application

```bash
chmod +x mvnw && ./mvnw spring-boot:run
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

### 4. Resolve as first-line

Log into Tasklist as `frank` / `frank`. The **First-Line Triage** task appears. Complete it with `resolved = true` to close the ticket.

### 5. Test the Escalation Path

For a quick local demo, restart the app with a shorter SLA such as `--timer.escalation.duration=PT10S`, then wait 10 seconds. Check Cockpit — the task should appear in Iris's `second-line` inbox.

## Timer Override for Tests

`application-test.properties` overrides `timer.escalation.duration=PT5S`. Tests use `ClockUtil.setCurrentTime(...)` to advance Operaton's internal clock — no `Thread.sleep` needed.

## Running Tests

```bash
./mvnw verify
```

Tests use Testcontainers to start WireMock automatically — no Docker Compose needed for CI.

## Bootstrap Data

Users frank and iris (and grace) are seeded from `src/main/resources/data.sql` on startup.

## Users

| Username | Password | Group       | Role                  |
|----------|----------|-------------|-----------------------|
| frank    | frank    | first-line  | First-line support    |
| iris     | iris     | second-line | Second-line engineer  |
