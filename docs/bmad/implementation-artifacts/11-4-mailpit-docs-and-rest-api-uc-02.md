---
status: done
---

# Story 11.4: Mailpit Documentation and REST API Examples for UC-02

## Status
done

## Story

As a developer evaluating Operaton,
I want the loan application README to explain email testing and show REST API usage,
So that I can explore the full use case without needing additional guidance.

## Acceptance Criteria

1. **Given** the UC-02 `README.md.jte` **When** an "Email Testing with Mailpit" section is added **Then** it states `http://localhost:8025`, lists all sample user emails, explains that high-risk applications trigger rejection email

2. **Given** the UC-02 `README.md.jte` **When** a "REST API" section is added **Then** it contains curl examples for start, list tasks, and complete (underwriter review)

## Tasks/Subtasks

- [x] Task 1: Add "Email Testing with Mailpit" section to README
  - [x] 1.1: State web UI URL `http://localhost:8025`
  - [x] 1.2: Explain that applicantEmail variable triggers rejection email
  - [x] 1.3: Explain trigger: high-risk applications → rejection email sent to applicantEmail

- [x] Task 2: Add REST API section to README
  - [x] 2.1: curl for `POST /engine-rest/process-definition/key/loan-application/start` with `applicantEmail`, `loanAmount`
  - [x] 2.2: curl for `GET /engine-rest/task?processDefinitionKey=loan-application`
  - [x] 2.3: curl for `POST /engine-rest/task/{id}/complete` for underwriter review

- [x] Task 3: Verify README has no template errors

## Dev Notes

**Key file:** `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte`

Use `-u eve:eve` for underwriter task completion (eve is in the underwriters group).

## Dev Agent Record

### Completion Notes
Added "Email Testing with Mailpit" and "REST API" sections to README.md.jte. REST API section shows curl examples for start, list tasks, and complete underwriter review.

## File List
- `starter-templates/src/main/jte/use-cases/uc-02-loan-application/README.md.jte`

## Change Log
- Added "Email Testing with Mailpit" section with Mailpit UI URL and usage guidance
- Added "REST API" section with curl examples for all three key operations
