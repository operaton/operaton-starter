@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton BPM example combining DMN business rule evaluation with service task orchestration.

![Process Model](docs/process-model.png)

## The Scenario

**Jack** applies for a loan. **Kate** is the underwriter who reviews medium-risk applications. When a loan application is submitted, the system:

1. Calls an external credit scoring API (stubbed with WireMock)
2. Evaluates a **DMN decision table** to determine risk level (`low` / `medium` / `high`)
3. Routes the application:
   - **Low risk** → automatically approved
   - **Medium risk** → Kate reviews it in the Tasklist
   - **High risk** → automatically rejected

## Getting Started in 5 Minutes

### Prerequisites

- Java ${config.javaVersion()}+
- Maven (or use the included `./mvnw`)
- Docker (for Postgres and WireMock)

### 1. Start services

```bash
docker compose up -d
```

This starts Postgres on port 5432 and WireMock on port 8089 with the credit-score stub loaded from `src/main/resources/wiremock/mappings/`.

### 2. Start the Application

```bash
chmod +x mvnw && ./mvnw spring-boot:run
```

The application starts on port ${config.serverPort()}. Open:
- **Tasklist**: http://localhost:${config.serverPort()}/operaton/app/tasklist
- **Cockpit**: http://localhost:${config.serverPort()}/operaton/app/cockpit

### 3. Start a Loan Application

Using the Tasklist or curl:

```bash
curl -s -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/loan-application/start \
  -H "Content-Type: application/json" \
  -d '{"variables": {"loanAmount": {"value": 100000, "type": "Integer"}}}'
```

The credit score service (WireMock) returns a score of 750, which the DMN table evaluates as **low risk** → auto-approved.

### 4. Try the Medium-Risk Path

Lower the stub score by editing `src/main/resources/wiremock/mappings/credit-score-stub.json` to return `{"score": 650}`, then restart WireMock and start a new process instance.

A task will appear in the Tasklist for **Kate** (login: `kate` / password: `kate`) to review.

## Project Structure

```
src/
  main/
    java/...
      Application.java              # Spring Boot entry point
      delegate/
        CreditScoreDelegate.java    # Calls WireMock credit-score API
        NotificationDelegate.java   # Logs approval/rejection
    resources/
      loan-application.bpmn         # BPMN process definition
      dmn/
        risk-assessment.dmn         # DMN decision table
      wiremock/
        mappings/
          credit-score-stub.json    # WireMock stub for credit score API
      application.properties
      data.sql                      # Seeds eve/underwriters identity
  test/
    java/.../LoanApplicationIT.java # Integration test (Testcontainers WireMock)
docker-compose.yml                  # WireMock for local development
```

## DMN Decision Table

| Credit Score | Loan Amount | Risk Level |
|-------------|-------------|------------|
| >= 700      | <= 500000   | low        |
| 600 – 699   | <= 500000   | medium     |
| < 600       | any         | high       |

## Running Tests

Tests use **Testcontainers** to start WireMock automatically — no Docker Compose needed for CI:

```bash
./mvnw verify
```

## Bootstrap Data

Users jack and kate are seeded from `src/main/resources/data.sql` on startup.

## Users

| Username | Password | Group        | Role              |
|----------|----------|--------------|-------------------|
| jack     | jack     | applicants   | Loan applicant    |
| kate     | kate     | underwriters | Loan underwriter  |
