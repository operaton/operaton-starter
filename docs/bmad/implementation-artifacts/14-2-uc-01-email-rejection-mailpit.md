---
baseline_commit: cd2a0cd
---

# Story 14-2: UC-01 Email Rejection Delegate and Mailpit Infrastructure

## Status
done

## Story

As a **developer evaluating Operaton**,
I want the UC-01 rejection path to send an email via `LeaveRejectionEmailDelegate` backed by Mailpit,
So that I see how BPMN send events integrate with Spring Mail and how to test email flows locally.

## Acceptance Criteria

1. **Given** `LeaveRejectionEmailDelegate.java.jte` **When** the "Send Rejection Email" send task executes **Then** it resolves the requester's email from the identity service using the `requester` variable; sends a plain-text email with subject "Leave Request — Rejected" containing requester name and requested dates; uses Spring `JavaMailSender`

2. **Given** `application.properties.jte` for UC-01 **When** reviewed **Then** `spring.mail.host=localhost` and `spring.mail.port=1025` are present

3. **Given** `docker-compose.yml.jte` for UC-01 **When** Mailpit service is checked **Then** it uses `axllent/mailpit:latest`, exposes SMTP on 1025 and web UI on 8025, includes `restart: unless-stopped`

4. **Given** `pom.xml.jte` for UC-01 **When** reviewed **Then** `spring-boot-starter-mail` dependency is declared

5. **Given** the UC-01 `DataInitializer.java.jte` **When** reviewed **Then** `managerReviewTimeout` is set as a default process variable (e.g., `PT72H`) when starting a demo process; tests can override with `PT1S`

6. **Given** `mvn verify -pl starter-templates` **When** it runs **Then** all tests pass

## Tasks/Subtasks

- [x] Task 1: Create `LeaveRejectionEmailDelegate.java.jte`
  - [x] 1.1: Implement delegate reading `requester` variable, resolving email via identity service
  - [x] 1.2: Send plain-text email via `JavaMailSender` with subject/body from PRD

- [x] Task 2: Add `spring-boot-starter-mail` to UC-01 `pom.xml.jte` dependencies

- [x] Task 3: Update `application.properties.jte` for UC-01 with mail host/port settings

- [x] Task 4: Add Mailpit service to UC-01 `docker-compose.yml.jte`

- [x] Task 5: BPMN timer expression uses conditional fallback `${managerReviewTimeout != null ? managerReviewTimeout : 'PT72H'}` — no DataInitializer change needed

- [x] Task 6: Run `mvn verify -pl starter-templates` — all 42 tests pass

## Dev Notes

- `requester` variable is set via `operaton:initiator` on the start event
- Use `identityService.createUserQuery().userId(requester).singleResult()` to get User object, then `user.getEmail()`
- `LeaveRejectionEmailDelegate` must be registered as a Spring bean (annotated `@Component("leaveRejectionEmailDelegate")`)
- `spring-boot-starter-mail` brings `JavaMailSender` autoconfiguration
- Mailpit SMTP port 1025 (not 25) — no auth needed in dev
- See UC-02 `RejectionEmailDelegate.java.jte` as reference implementation

## File List

- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/LeaveRejectionEmailDelegate.java.jte` — new
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/maven/pom.xml.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/application.properties.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/docker-compose.yml.jte`
- `starter-templates/src/main/jte/use-cases/uc-01-leave-request/DataInitializer.java.jte`

## Review Findings

- [x] [Review][Decision] Story 14-2 depends on 14-1: confirmed 14-1 is applied; `Task_SendRejectionEmail` sendTask IS in the BPMN and wired to `leaveRejectionEmailDelegate`
- [x] [Review][Patch] Logger reference — confirmed already correct (`LeaveRejectionEmailDelegate.class`); blind hunter false positive
- [x] [Review][Patch] `LeaveRejectionEmailDelegate.java.jte` not registered in `GenerationEngine.java` — confirmed already registered at line 128-129; edge hunter false positive
- [x] [Review][Patch] `LeaveRejectionEmailDelegate.java.jte` not in MetadataController UC-01 manifest — confirmed already listed at line 113; false positive
- [x] [Review][Patch] Missing `spring.mail.test-connection=false` — added to `application.properties.jte` [starter-templates/src/main/jte/use-cases/uc-01-leave-request/application.properties.jte]
- [x] [Review][Patch] Missing `setFrom()` — added `spring.mail.properties.mail.smtp.from=noreply@example.com` to `application.properties.jte`
- [x] [Review][Patch] No error handling around `mailSender.send()` — added try/catch with error logging [starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/LeaveRejectionEmailDelegate.java.jte]
- [x] [Review][Patch] `startDate`, `endDate`, `user.getFirstName()` null guards — added null-safe fallbacks [starter-templates/src/main/jte/use-cases/uc-01-leave-request/delegate/LeaveRejectionEmailDelegate.java.jte]
- [x] [Review][Defer] `axllent/mailpit:latest` not version-pinned — non-reproducible builds; defer, consistent with other images in project — deferred, pre-existing pattern
- [x] [Review][Defer] Missing credentials placeholder in application.properties for production SMTP guidance — deferred, out of scope for dev-only Mailpit story

## Change Log

- 2026-06-08: Story created — FR-3b UC-01 email rejection delegate + Mailpit
- 2026-06-08: Code review — 1 decision needed, 6 patches, 2 deferred
