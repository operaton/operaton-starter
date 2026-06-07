---
status: done
---

# Story 11.1: Add Mailpit Infrastructure to UC-02

## Status
done

## Story

As a developer evaluating Operaton,
I want a local mail server ready when I run the UC-02 Docker Compose stack,
So that I can observe email notifications without configuring an external SMTP server.

## Acceptance Criteria

1. **Given** the UC-02 `docker-compose.yml.jte` **When** a Mailpit service is added **Then** it uses `axllent/mailpit:latest`, exposes SMTP on port 1025 and web UI on port 8025, with `restart: unless-stopped`

2. **Given** `application.properties.jte` and `application-docker.properties.jte` for UC-02 **When** Spring Mail is configured **Then** `spring.mail.host=localhost` and `spring.mail.port=1025` are present

3. **Given** `pom.xml.jte` for UC-02 **When** the mail dependency is added **Then** `spring-boot-starter-mail` is the only new dependency

4. **Given** `docker compose up` **When** the stack starts **Then** Mailpit web UI is accessible at `http://localhost:8025`

## Tasks/Subtasks

- [x] Task 1: Add Mailpit to UC-02 docker-compose.yml.jte
  - [x] 1.1: In `starter-templates/src/main/jte/use-cases/uc-02-loan-application/docker-compose.yml.jte`, add Mailpit service block after existing services

- [x] Task 2: Add Spring Mail configuration to application properties
  - [x] 2.1: In `uc-02-loan-application/application.properties.jte`, add `spring.mail.host=localhost` and `spring.mail.port=1025`
  - [x] 2.2: Added `spring.mail.test-connection=false` to `application-h2.properties.jte` for IT tests

- [x] Task 3: Add spring-boot-starter-mail dependency to Maven pom.xml.jte
  - [x] 3.1: `spring-boot-starter-mail` added after `spring-boot-starter-web`

- [x] Task 4: Verify tests pass (mail config should not break existing tests with a mock/noop sender)

## Dev Notes

**Key files:**
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/docker-compose.yml.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/application.properties.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/maven/pom.xml.jte`

**Mailpit docker-compose block:**
```yaml
  mailpit:
    image: axllent/mailpit:latest
    ports:
      - "1025:1025"
      - "8025:8025"
    restart: unless-stopped
```

**IT test concern:** Tests that use H2 profile will also need mail config. For IT tests, Spring Boot auto-configures a `JavaMailSender` if the properties are present. To avoid real SMTP calls in tests, either:
- Use `spring.mail.host=localhost` and start Mailpit in tests (integration), OR
- Add `spring.mail.test-connection=false` to test properties

## Dev Agent Record

### Completion Notes
Mailpit added to docker-compose. Spring mail properties added to application.properties. `spring.mail.test-connection=false` added to H2 profile to prevent test failures. `spring-boot-starter-mail` dependency added to pom.xml.jte.

## File List
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/docker-compose.yml.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/application.properties.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/application-h2.properties.jte`
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/maven/pom.xml.jte`

## Change Log
- Added Mailpit service to docker-compose.yml.jte
- Added spring.mail.* properties to application.properties.jte
- Added spring.mail.test-connection=false to application-h2.properties.jte
- Added spring-boot-starter-mail dependency to maven/pom.xml.jte
