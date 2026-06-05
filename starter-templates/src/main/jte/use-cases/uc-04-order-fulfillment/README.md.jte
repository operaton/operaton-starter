@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# ${config.projectName()}

A self-contained Operaton BPM example demonstrating multi-service-task orchestration with conditional routing.

![Process Model](docs/process-model.png)

## The Scenario

When a customer places an order:

1. **Inventory check** — calls a REST API to verify stock availability
2. **Routing** — two paths based on availability:
   - **In stock**: Charge payment → **Dave** (warehouse) packs and ships → Customer notified
   - **Out of stock**: Customer notified of backorder

All external calls are stubbed with WireMock — no real services needed.

## Getting Started in 5 Minutes

### Prerequisites

- Java ${config.javaVersion()}+
- Maven (or use the included `./mvnw`)
- Docker (for Postgres and WireMock)

### 1. Start services

```bash
docker compose up -d
```

This starts Postgres on port 5432 and WireMock on port 8089, serving inventory, payment, and notification stubs.

### 2. Start the Application

```bash
chmod +x mvnw && ./mvnw spring-boot:run
```

The application starts on port ${config.serverPort()}. Open:
- **Tasklist**: http://localhost:${config.serverPort()}/operaton/app/tasklist
- **Cockpit**: http://localhost:${config.serverPort()}/operaton/app/cockpit

### 3. Start an Order

```bash
curl -s -X POST http://localhost:${config.serverPort()}/engine-rest/process-definition/key/order-fulfillment/start \
  -H "Content-Type: application/json" \
  -d '{"variables": {"orderId": {"value": "ORD-001", "type": "String"}}}'
```

### 4. Pack and Ship as Dave

Log into Tasklist as `dave` / `dave`. The **Pack & Ship** task appears after inventory check and payment succeeds. Complete it to trigger the customer notification.

### 5. Try the Out-of-Stock Path

Use an orderId starting with `out-of-stock-` (e.g. `out-of-stock-ORD-002`). The WireMock stub matches this pattern and returns `available: false`, routing directly to backorder notification.

## WireMock Stubs

Stubs are in `src/main/resources/wiremock/mappings/`:

| File | Endpoint | Response |
|------|----------|----------|
| `inventory-in-stock.json` | GET /inventory/* | `{"available": true}` |
| `inventory-out-of-stock.json` | GET /inventory/out-of-stock-* | `{"available": false}` |
| `payment-success.json` | POST /payments | `{"status": "approved"}` |
| `notify-customer.json` | POST /notifications/customer | 200 OK |
| `notify-backorder.json` | POST /notifications/backorder | 200 OK |

## Running Tests

```bash
./mvnw verify
```

Tests use Testcontainers to start WireMock automatically — no Docker Compose needed for CI.

## Bootstrap Data

Dave (warehouse) and the admin user are seeded from `src/main/resources/data.sql` on startup.

## Users

| Username | Password | Group          | Role               |
|----------|----------|----------------|--------------------|
| dave     | dave     | warehouse      | Warehouse operator |
| admin    | admin    | operaton-admin | Operaton admin     |
