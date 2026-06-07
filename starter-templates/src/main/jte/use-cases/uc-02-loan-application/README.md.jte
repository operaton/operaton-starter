@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton example for **loan application triage** that combines a credit-score service call, DMN evaluation, and conditional user-task routing.

## Scenario

Each loan application collects a requested `loanAmount`. The workflow enriches that request with a credit score from WireMock, evaluates the DMN decision table, and then routes the application based on the computed `riskLevel`.

## Actors

| Actor | Username | Group | Responsibility |
|------|----------|-------|----------------|
| Applicant | `alice` / `alice` | `employees` | Starts the process with a `loanAmount` |
| Underwriter | `eve` / `eve` | `underwriters` | Reviews only medium-risk applications |
| Decision automation | system | n/a | Calls the credit-score API and evaluates the DMN table |

## Starting the Process

Log in as **alice / alice** (group: `employees`) in Tasklist to start this process. Only members of the `employees` group can initiate a loan application — this is enforced by `candidateStarterGroups` on the process definition.

## Main Flow

1. A caller starts `loan-application` with `loanAmount`.
2. `CreditScoreDelegate` calls the WireMock credit-score endpoint and stores `creditScore`.
3. The BPMN business-rule task evaluates `dmn/risk-assessment.dmn` and writes `riskLevel`.
4. The exclusive gateway routes by risk:
   - `low` → auto-approve notification
   - `medium` → **Underwriter Review**
   - `high` → auto-reject notification
5. Medium-risk applications end only after Eve claims and completes the underwriter task.

## Alternate Paths

### Low-risk path

The application never waits on a human. A favorable score and amount combination drives the process straight to the approval notification task and then to completion.

### Medium-risk path

The workflow pauses in Tasklist for the `underwriters` group. This is the only human-in-the-loop branch and is the best path for learning how DMN outputs influence BPMN routing.

### High-risk path

If the DMN decision returns `high`, the process skips the user task completely and sends the rejection notification automatically.

## Risk Rules Reference

- The decision table, not handwritten Java branching, defines the risk thresholds.
- `creditScore` is supplied by the external API delegate before DMN evaluation.
- `riskLevel` is a process variable, so you can inspect the decision outcome directly in Cockpit.
- WireMock is the only external dependency for the example; no real credit bureau integration is required.

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

### Default low-risk example

```bash
curl -s -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/loan-application/start \
  -H "Content-Type: application/json" \
  -d '{"variables": {"loanAmount": {"value": 100000, "type": "Integer"}}}'
```

With the default WireMock response, the delegate returns a score of 750 and the DMN table routes the instance to the low-risk auto-approval path.

### Medium-risk example

Edit `src/main/resources/wiremock/mappings/credit-score-stub.json` to return a score around 650, restart WireMock, and start another application. Eve (`eve / eve`) will receive **Underwriter Review** in Tasklist.

### High-risk example

Return a score below 600 from the WireMock stub. The process will skip the user task and finish on the automatic rejection branch.

## Important Business Rules

- `creditScore >= 700` and moderate amount → `low`
- `600 <= creditScore <= 699` and moderate amount → `medium`
- `creditScore < 600` → `high`
- Medium risk is the only branch that requires manual review

## Project Structure

```
src/
  main/
    java/${config.packagePath()}/
      Application.java
      DataInitializer.java
      delegate/
        CreditScoreDelegate.java
        NotificationDelegate.java
    resources/
      loan-application.bpmn
      dmn/risk-assessment.dmn
      wiremock/mappings/credit-score-stub.json
  test/
    java/${config.packagePath()}/
      LoanApplicationIT.java
```

## Email Testing with Mailpit

When running with Docker Compose, Mailpit captures all outgoing emails locally.

Access the Mailpit web UI at: http://localhost:8025

To see rejection emails, start processes with high-risk loan amounts that trigger auto-rejection.
Users that may receive emails: applicants whose `applicantEmail` variable is set when starting the process.

## REST API

### Start a loan application (as alice)

```bash
curl -s -u alice:alice -X POST \
  http://localhost:${config.serverPort()}/engine-rest/process-definition/key/loan-application/start \
  -H "Content-Type: application/json" \
  -d '{
    "variables": {
      "loanAmount":      {"value": 50000,             "type": "Integer"},
      "applicantEmail":  {"value": "alice@example.com","type": "String"}
    }
  }'
```

### List open underwriter tasks

```bash
curl -s http://localhost:${config.serverPort()}/engine-rest/task?processDefinitionKey=loan-application
```

### Complete an underwriter review task

```bash
TASK_ID=<id from previous response>
curl -s -X POST \
  http://localhost:${config.serverPort()}/engine-rest/task/${'$'}{TASK_ID}/complete \
  -H "Content-Type: application/json" \
  -d '{"variables": {}}'
```

## Running Tests

```bash
./mvnw verify
```

`LoanApplicationIT` covers DMN deployment, isolated DMN evaluation, business-key querying, and all three process branches.
