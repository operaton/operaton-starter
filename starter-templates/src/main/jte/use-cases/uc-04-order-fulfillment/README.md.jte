@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton example for **order fulfillment orchestration** across inventory, payment, warehouse, and customer-notification steps.

## Scenario

Each order starts with an `orderId`. The process checks inventory first, then branches:

- in stock → charge payment, create a warehouse task, notify the customer
- out of stock → skip manual work and send a backorder notification

All integrations are stubbed with WireMock so the example stays local and deterministic.

## Actors

| Actor | Username | Group | Responsibility |
|------|----------|-------|----------------|
| Customer / caller | `frank` / `frank` | `sales` | Starts the order process |
| Warehouse operator | `dave` / `dave` | `warehouse` | Packs and ships in-stock orders |
| Automation | system | n/a | Calls inventory, payment, and notification endpoints |
| Admin | `admin` / `admin` | `operaton-admin` | Operaton administration only |

## Starting the Process

Log in as **frank / frank** (group: `sales`) in Tasklist to start this process. Only members of the `sales` group can place orders — this is enforced by `candidateStarterGroups` on the process definition.

## Main Flow

1. A caller starts `order-fulfillment` with `orderId`.
2. `InventoryDelegate` checks stock availability and stores `available`.
3. If `available == true`, `PaymentDelegate` charges the order.
4. Dave receives **Pack & Ship** in Tasklist.
5. When Dave completes the task, `NotifyCustomerDelegate` sends the shipment notification and the process ends.

## Alternate Paths

### Backorder path

If inventory is unavailable, the BPMN gateway skips payment and warehouse work entirely. `NotifyBackorderDelegate` sends the backorder notification and the process ends immediately.

### In-stock path with human handoff

This branch demonstrates orchestration plus a user task. Inventory and payment are automated, but warehouse fulfillment is intentionally modeled as human work.

## Routing Rules Reference

- Inventory is always checked before payment is attempted.
- Only in-stock orders reach the warehouse task.
- `orderId` prefixes are used by WireMock to simulate stock conditions locally.
- Customer notification is different for shipped orders versus backorders.

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

### In-stock order

```bash
curl -s -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/order-fulfillment/start \
  -H "Content-Type: application/json" \
  -d '{"variables": {"orderId": {"value": "ORD-001", "type": "String"}}}'
```

The default inventory stub returns `available: true`, so payment runs and Dave (`dave / dave`) receives **Pack & Ship**.

### Out-of-stock order

Start the process with an ID that begins with `out-of-stock-`, for example `out-of-stock-ORD-002`. The inventory stub returns `available: false`, and the process goes straight to the backorder notification branch with no warehouse task.

## Important Business Rules

- `available == true` is the gateway condition for payment and shipping
- `available == false` short-circuits the process to backorder notification
- Payment is never attempted for out-of-stock orders
- Warehouse work only exists for orders that passed both inventory and payment

## Project Structure

```
src/
  main/
    java/${config.packagePath()}/
      Application.java
      DataInitializer.java
      delegate/
        InventoryDelegate.java
        PaymentDelegate.java
        NotifyCustomerDelegate.java
        NotifyBackorderDelegate.java
    resources/
      order-fulfillment.bpmn
      wiremock/mappings/
  test/
    java/${config.packagePath()}/
      OrderFulfillmentIT.java
```

## REST API

### Start a process instance

```bash
curl -u frank:frank -X POST \
  http://localhost:8080/engine-rest/process-definition/key/order-fulfillment/start \
  -H 'Content-Type: application/json' \
  -d '{"variables": {"orderId": {"value": "ORD-001", "type": "String"}}}'
```

### List tasks for this process

```bash
curl -u frank:frank \
  http://localhost:8080/engine-rest/task?processDefinitionKey=order-fulfillment
```

### Complete the Pack & Ship task (warehouse group)

```bash
curl -u dave:dave -X POST \
  http://localhost:8080/engine-rest/task/{taskId}/complete \
  -H 'Content-Type: application/json' \
  -d '{}'
```

### Suspend a running process instance (ops pattern)

```bash
# Suspend a running process instance (ops pattern)
curl -u admin:admin -X PUT \
  http://localhost:8080/engine-rest/process-instance/{processInstanceId}/suspended \
  -H 'Content-Type: application/json' \
  -d '{"suspended": true}'
```

## Running Tests

```bash
./mvnw verify
```

`OrderFulfillmentIT` verifies both the in-stock warehouse path and the direct backorder path.
