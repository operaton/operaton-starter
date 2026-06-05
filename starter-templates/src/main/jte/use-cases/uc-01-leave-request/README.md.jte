@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A pre-built Operaton use case example: **Leave Request HR Approval Workflow**.

Alice submits a leave request. Bob (her manager) reviews it and either approves or rejects it.
If approved, Carol from HR records the approved leave. No Docker, no external services — just run it.

## Getting Started in 5 Minutes

### Prerequisites

- Java ${config.javaVersion()} or later
- Maven wrapper included (`./mvnw`)

### Run the application

```bash
./mvnw spring-boot:run
```

The application starts on port ${config.serverPort()}.

### Try it in the Tasklist

1. Open **[http://localhost:${config.serverPort()}/operaton/app/tasklist](http://localhost:${config.serverPort()}/operaton/app/tasklist)**
2. Start a leave request process via the Cockpit or using the REST API:
   ```bash
   curl -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/leave-request/start \
     -H 'Content-Type: application/json' \
     -d '{"variables": {"requester": {"value": "alice", "type": "String"}}}'
   ```
3. Log in as **bob / bob** (manager). You will see the "Manager Reviews Request" task in your inbox.
4. Complete the task and set `approved` to `true` or `false`.
5. If approved, log in as **carol / carol** (HR) to complete the "HR Records Approved Leave" task.
6. If rejected, log in as **alice / alice** (employee) to see the rejection notification.

### Pre-seeded users

| Username | Password | Group     | Role              |
|----------|----------|-----------|-------------------|
| alice    | alice    | employees | Leave requester   |
| bob      | bob      | managers  | Approves/rejects  |
| carol    | carol    | hr        | Records approved leave |

### Run the tests

```bash
./mvnw test
```

Tests cover both the approval path and the rejection path end-to-end.

## Project Structure

```
src/
  main/
    java/${config.packagePath()}/
      Application.java          # Spring Boot entry point
    resources/
      leave-request.bpmn        # BPMN process definition
      application.properties    # Application configuration
      data.sql                  # Seeds users, groups, and memberships
  test/
    java/${config.packagePath()}/
      LeaveRequestIT.java       # Integration test (approval + rejection paths)
```

## Learn More

- [Operaton Documentation](https://docs.operaton.org)
- [BPMN User Tasks](https://docs.operaton.org/manual/latest/reference/bpmn20/tasks/user-task/)
- [Operaton Tasklist](https://docs.operaton.org/manual/latest/webapps/tasklist/)
