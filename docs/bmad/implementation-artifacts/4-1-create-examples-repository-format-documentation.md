---
baseline_commit:
---

# Story 4-1: Create Examples Repository Format Documentation

## Status
ready-for-dev

## Story

As a **content author**,
I want **clear documentation on how to write a `.operaton-starter.yml` manifest and publish examples**,
So that **I can contribute examples to the registry without guessing the format**.

## Acceptance Criteria

**Given** the PRD specifies manifest schema (FR-A1 through A7)  
**When** I create `docs/examples-repository-format.md` (or similar) in the operaton-starter project  
**Then** the documentation includes:
1. **Overview section** (100 words max): What manifests are, why they matter, how they make examples discoverable
2. **Schema reference** with:
   - `apiVersion` — must be `operaton-starter/v1`
   - Required fields: `id`, `title`, `shortDescription`, `path` with descriptions and constraints
   - Optional fields (grouped by category):
     - **Descriptive:** longDescription, tags, integrations, bpmnConcepts, authors, license, documentationUrl, demoVideoUrl
     - **Technical:** buildSystem, runtime, requires, operatonVersion, javaVersion, complexity
     - **Media:** icon (emoji or URL), screenshots
     - **Metadata:** lastUpdated (ISO 8601)
   - Field-level constraints: max lengths, allowed enums (buildSystem, runtime, complexity), URL format
3. **Manifest example** with realistic values and inline comments:
   ```yaml
   apiVersion: operaton-starter/v1
   repository:
     name: Example Repository
     maintainer: Jane Doe
   examples:
     - id: kafka-saga
       title: Kafka-based Saga Pattern
       shortDescription: Demonstrates distributed saga pattern using Kafka events
       path: examples/kafka-saga
       # ... optional fields
   ```
4. **Repository layout** guide showing the expected directory structure:
   ```
   operaton-examples/
   ├── .operaton-starter.yml    # Manifest at root
   ├── examples/
   │   ├── kafka-saga/          # Example subdirectory
   │   │   ├── pom.xml          # Example's own build files
   │   │   └── src/
   │   └── saga-with-jdbc/      # Another example
   ```
5. **How to register** — brief guidance on contacting maintainers to register a repo in `starter.examples.repositories`
6. **Forward-compatibility** section explaining that unknown fields are ignored; authors should not worry about future schema versions
7. **Validation checklist:**
   - [ ] Each example has a unique `id`
   - [ ] `path` points to existing subdirectory
   - [ ] `shortDescription` is ≤ 200 characters
   - [ ] manifest ≤ 256 KB (if defined)
   - [ ] YAML is valid (use a linter)
**Location:** `docs/examples-repository-format.md` (discoverable from README)  
**Word count:** 600–800 words (concise, scannable)  
**Review criteria:** A new contributor should understand the schema in under 10 minutes
**Unit test:** None required (documentation task)
---

## Tasks/Subtasks

- [ ] Implementation tasks to be defined based on story requirements

## Dev Notes

- See epic-level documentation in epics-examples-gallery-v1.md for full context
- All stories should follow the patterns established in Examples Gallery v1 PRD

## Dev Agent Record

### Completion Notes
(To be filled during implementation)

### File List
(To be filled during implementation)

### Change Log
(To be filled during implementation)
