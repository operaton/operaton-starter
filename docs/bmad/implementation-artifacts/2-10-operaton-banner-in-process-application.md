---
baseline_commit: 0568d7f
---

# Story 2.10: Operaton Banner in Generated Process Application Projects

## Status
todo

## Story

As a **developer who just generated and started an Operaton Process Application**,
I want the Operaton ASCII banner to display in the console on startup,
So that the application visually identifies itself as an Operaton project with version information at a glance.

## Acceptance Criteria

1. **Given** a generated Process Application project (Maven or Gradle, any Java version) **When** `src/main/resources/banner.txt` is inspected **Then** it contains the Operaton ASCII art logo and the following version placeholders exactly: `${spring-boot.formatted-version}`, `${operaton.bpm.formatted-version}`, and `@project.version@`

2. **Given** the application is started with `./mvnw spring-boot:run` (or `./gradlew bootRun`) **When** the console output is observed at startup **Then** the banner is displayed before the Spring Boot startup log lines; the Operaton version and Spring Boot version are resolved and printed (not the literal placeholder strings)

3. **Given** no additional configuration in `application.properties` **When** the application starts **Then** the banner appears automatically — Spring Boot's default banner mechanism reads `src/main/resources/banner.txt` from the classpath with no explicit opt-in required

4. **Given** a Process Archive project is generated **When** inspected **Then** no `banner.txt` is included — the banner is specific to embedded Spring Boot Process Applications only

## Tasks/Subtasks

- [ ] Task 1: Add banner.txt to Process Application template
  - [ ] 1.1: Create `starter-templates/src/main/jte/process-application/banner.txt.jte` (or as a plain static resource with no JTE templating — banner.txt has no per-project variable substitution) containing the Operaton ASCII art logo sourced from `operaton/operaton` Spring Boot Starter (`spring-boot-starter/starter/src/main/resources/banner.txt`)
  - [ ] 1.2: Register `banner.txt` in the `process-application` template manifest so it appears in the file tree preview on the web UI and is included in generated ZIPs

- [ ] Task 2: Verify banner renders at startup
  - [ ] 2.1: Add an assertion to the Process Application integration test (from Story 2.3 `ProcessIT.java`) that captures startup output and confirms the banner is printed — or add a `@SpringBootTest` smoke test that verifies `banner.txt` is on the classpath
  - [ ] 2.2: Alternatively, verify in Story 2.7 CI matrix job that startup output contains "Operaton" before the first INFO log line

- [ ] Task 3: Exclude banner from Process Archive template
  - [ ] 3.1: Confirm `banner.txt` is not in the `process-archive` template manifest; no action needed if it was never added

- [ ] Task 4: Run `mvn verify` — all tests green

## Dev Notes

- **Source**: The banner content is taken verbatim from `https://raw.githubusercontent.com/operaton/operaton/main/spring-boot-starter/starter/src/main/resources/banner.txt`. It contains:
  ```
       _/_/                                              _/
    _/    _/  _/_/_/      _/_/    _/  _/_/    _/_/_/  _/_/_/_/    _/_/    _/_/_/
   _/    _/  _/    _/  _/_/_/_/  _/_/      _/    _/    _/      _/    _/  _/    _/
  _/    _/  _/    _/  _/        _/        _/    _/    _/      _/    _/  _/    _/
   _/_/    _/_/_/      _/_/_/  _/          _/_/_/      _/_/    _/_/    _/    _/
          _/
         _/

    Spring-Boot: ${spring-boot.formatted-version}
    Operaton: ${operaton.bpm.formatted-version}
    Operaton Spring Boot Starter: (v@project.version@)
  ```
- **No JTE templating needed**: `banner.txt` has no per-project placeholders — the Spring Boot and Operaton version placeholders are resolved by Spring Boot at runtime from the classpath, not by JTE at generation time. The file can be a plain static resource (`.txt`, not `.jte`) if the generation engine supports static file pass-through, or a trivial `.jte` that outputs the content verbatim.
- **Template manifest**: The manifest entry should have `condition: null` (always included for Process Application) and `templateId` matching the banner template.
- **Process Archive exclusion**: `banner.txt` is meaningless for a Process Archive (no embedded Spring Boot), so it must not be in the `process-archive` template manifest.
- **`@project.version@` Maven filtering**: This placeholder is resolved by Maven resource filtering at build time of the _generated_ project. The generated `pom.xml` must have `<resources><resource><directory>src/main/resources</directory><filtering>true</filtering></resource></resources>` or Spring Boot's default resource filtering must be active (it is by default in spring-boot-maven-plugin projects).

## File List

- `starter-templates/src/main/jte/process-application/banner.txt` (or `banner.txt.jte`) — new
- `starter-templates/src/main/java/org/operaton/dev/starter/templates/engine/GenerationEngine.java` — include banner.txt in process-application file list
- `starter-server/src/main/java/org/operaton/dev/starter/server/api/MetadataController.java` — add banner.txt to PROCESS_APPLICATION templateManifest entries

## Change Log

- 2026-06-05: Story created for FR75 — Operaton banner.txt in generated Process Application projects
