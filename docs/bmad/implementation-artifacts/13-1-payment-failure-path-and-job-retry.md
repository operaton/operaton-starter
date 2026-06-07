---
status: done
---

# Story 13.1: Add Payment Failure Path and Job Retry Configuration

## Status
done

## Story

As a developer evaluating Operaton,
I want to see a configurable payment failure route through a BPMN error boundary event with job retry,
So that I understand how Operaton handles service failures, error escalation, and retry policies declaratively.

## Acceptance Criteria

1. **Given** `PaymentDelegate.java.jte` **When** process variable `simulatePaymentFailure = true` **Then** the delegate throws `new BpmnError("PAYMENT_FAILED")`

2. **Given** `order-fulfillment.bpmn.jte` **When** a boundary error event catching `PAYMENT_FAILED` is added to the payment task **Then** it routes to a "Notify Customer of Failure" end event

3. **Given** the payment service task **When** `operaton:failedJobRetryTimeCycle="R3/PT10S"` is declared **Then** the retry config is visible in BPMN XML

4. **Given** `OrderFulfillmentIT` **When** started with `simulatePaymentFailure = true` **Then** the test asserts the process reaches the failure end event AND `orderStatus = FAILED`

## Tasks/Subtasks

- [x] Task 1: Update PaymentDelegate to support failure simulation
  - [x] 1.1: In `PaymentDelegate.java.jte`, check `execution.getVariable("simulatePaymentFailure")`; if `Boolean.TRUE.equals(value)`, throw `new BpmnError("PAYMENT_FAILED", "Payment failed due to simulation flag")`

- [x] Task 2: Update BPMN with error boundary event and retry config
  - [x] 2.1: In `order-fulfillment.bpmn.jte`, add `operaton:failedJobRetryTimeCycle="R3/PT10S"` (via extensionElements) plus `operaton:asyncBefore="true"` to the payment service task
  - [x] 2.2: Add `<boundaryEvent id="ErrorBoundary_Payment" attachedToRef="Task_ChargePayment"><errorEventDefinition errorRef="PaymentFailed"/></boundaryEvent>`
  - [x] 2.3: Add `<error id="PaymentFailed" errorCode="PAYMENT_FAILED" name="Payment Failed"/>` to definitions
  - [x] 2.4: Add "Notify Customer of Failure" end event and connect via sequence flow; set `orderStatus=FAILED` via executionListener on the flow

- [x] Task 3: Add IT test for failure path
  - [x] 3.1: In `OrderFulfillmentIT.java.jte`, add test `paymentFailurePath_endsAtFailedEvent` starting with `simulatePaymentFailure = true`
  - [x] 3.2: Execute async job, assert process is finished via `historyService.createHistoricProcessInstanceQuery().finished()`
  - [x] 3.3: Assert `orderStatus = "FAILED"` via historicVariableInstanceQuery

- [x] Task 4: Verify all tests pass

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/PaymentDelegate.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/OrderFulfillmentIT.java.jte`

**Error boundary BPMN pattern:**
```xml
<error id="PaymentFailed" errorCode="PAYMENT_FAILED" name="Payment Failed"/>

<boundaryEvent id="ErrorBoundary_Payment" attachedToRef="Task_Payment">
  <errorEventDefinition errorRef="PaymentFailed"/>
</boundaryEvent>
<sequenceFlow sourceRef="ErrorBoundary_Payment" targetRef="EndEvent_Failed"/>
<endEvent id="EndEvent_Failed" name="Notify Customer of Failure"/>
```

**Retry cycle on service task:**
```xml
<serviceTask id="Task_Payment" name="Process Payment"
             operaton:delegateExpression="${paymentDelegate}"
             operaton:failedJobRetryTimeCycle="R3/PT10S">
```

## Dev Agent Record

### Implementation Plan

1. Add `BpmnError` import and failure simulation check to PaymentDelegate
2. Add `<error>` to BPMN definitions, `asyncBefore` + `failedJobRetryTimeCycle` extensionElement to Task_ChargePayment, boundary error event, EndEvent_Failed, sequence flow with executionListener to set orderStatus=FAILED, and DI shapes
3. Add `paymentFailurePath_endsAtFailedEvent` test with async job execution

### Completion Notes

- Used `operaton:asyncBefore="true"` on Task_ChargePayment so failedJobRetryTimeCycle takes effect (requires async job)
- `failedJobRetryTimeCycle` placed inside `<extensionElements>` child element (not attribute, as the attribute form requires different XML)
- Exception class for suspended jobs is `SuspendedEntityInteractionException` (verified via jar inspection of operaton-engine-2.0.0.jar)

## File List

- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/PaymentDelegate.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/order-fulfillment.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-04-order-fulfillment/OrderFulfillmentIT.java.jte`

## Change Log

- PaymentDelegate: Added BpmnError import; added simulatePaymentFailure check before HTTP call
- BPMN: Added `<error>` definition, `asyncBefore`+`failedJobRetryTimeCycle` to Task_ChargePayment, ErrorBoundary_Payment, EndEvent_Failed, Flow_error_to_failed with executionListener, and DI shapes
- IT: Added HistoryService/ManagementService/Job/SuspendedEntityInteractionException imports; added paymentFailurePath_endsAtFailedEvent test (@Order 4); suspension test moved to @Order 5
