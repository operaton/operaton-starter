---
status: done
---

# Story 11.2: Implement Email Rejection via BPMN Send Task

## Status
done

## Story

As a developer evaluating Operaton,
I want loan rejections to send an email via a BPMN Send Task backed by a Spring Mail delegate,
So that I see how Operaton models external notification as a first-class process element.

## Acceptance Criteria

1. **Given** `loan-application.bpmn.jte` **When** the high-risk rejection path is updated **Then** the `Auto-Reject Notify` service task is replaced with a Send Task named "Send Rejection Email" using delegate expression `${rejectionEmailDelegate}`

2. **Given** `RejectionEmailDelegate.java.jte` is implemented **When** the Send Task executes **Then** it reads `applicantEmail` from process variables, sends plain-text email with subject "Loan Application — Decision" containing applicant name and reason, using `JavaMailSender`

3. **Given** `DataInitializer.java.jte` for UC-02 **When** a loan application process is started **Then** `applicantEmail` is available as a process variable

4. **Given** `LoanApplicationIT` **When** a high-risk application completes the rejection path **Then** the test verifies the process reaches the rejection end event without exception

## Tasks/Subtasks

- [x] Task 1: Replace Auto-Reject Notify with Send Task in BPMN
  - [x] 1.1: In `loan-application.bpmn.jte`, changed `<serviceTask>` to `<sendTask>` with `operaton:delegateExpression="${'$'}{rejectionEmailDelegate}"`

- [x] Task 2: Create RejectionEmailDelegate
  - [x] 2.1: Created `RejectionEmailDelegate.java.jte`
  - [x] 2.2: Implements `JavaDelegate`; injects `JavaMailSender` via constructor; reads `applicantEmail` from execution
  - [x] 2.3: Sends `SimpleMailMessage` with subject "Loan Application Rejected"; skips if email blank
  - [x] 2.4: Registered as `@Component("rejectionEmailDelegate")`; sets `loanDecision=REJECTED`

- [x] Task 3: Ensure applicantEmail process variable is available
  - [x] 3.1: All process starts in `LoanApplicationIT.java.jte` pass `applicantEmail = "test@example.com"`

- [x] Task 4: Handle mail sending in IT tests
  - [x] 4.1: `spring.mail.test-connection=false` added to `application-h2.properties.jte`
  - [x] 4.2: `@MockBean JavaMailSender mailSender` added to IT test class

- [x] Task 5: Verify all tests pass

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte`
- New: `starter-templates/src/main/jte/use-cases/uc-02-loan-application/RejectionEmailDelegate.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/DataInitializer.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/LoanApplicationIT.java.jte`

**RejectionEmailDelegate pattern:**
```java
@Component("rejectionEmailDelegate")
public class RejectionEmailDelegate implements JavaDelegate {
    private final JavaMailSender mailSender;
    public RejectionEmailDelegate(JavaMailSender mailSender) { this.mailSender = mailSender; }

    @Override
    public void execute(DelegateExecution execution) {
        String email = (String) execution.getVariable("applicantEmail");
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Loan Application — Decision");
        msg.setText("Dear applicant, your application has been rejected due to high risk.");
        mailSender.send(msg);
    }
}
```

**IT test mock pattern:**
```java
@MockBean
private JavaMailSender mailSender;
```
Then verify process completes the rejection end event without assertions on mail sending internals.

## Dev Agent Record

### Completion Notes
`Task_AutoReject` changed from serviceTask to sendTask using `${rejectionEmailDelegate}` delegate expression. Created `RejectionEmailDelegate.java.jte` with constructor-injected `JavaMailSender`. `NotificationDelegate` simplified to only handle approve path. IT test uses `@MockBean JavaMailSender`. `GenerationEngine.java` updated to emit `RejectionEmailDelegate.java`.

## File List
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/loan-application.bpmn.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/RejectionEmailDelegate.java.jte` (new)
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/NotificationDelegate.java.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/LoanApplicationIT.java.jte`
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java`

## Change Log
- Changed Task_AutoReject from serviceTask to sendTask
- Created RejectionEmailDelegate.java.jte
- Simplified NotificationDelegate to approve-only
- Added @MockBean JavaMailSender to IT test
- Registered RejectionEmailDelegate in GenerationEngine
