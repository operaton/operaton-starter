---
baseline_commit: 48f7c1c
---

# Story 2.9: Build Tool Wrappers in Generated Projects

## Status
done

## Story

As a **developer who downloaded a generated Operaton project**,
I want the project archive to include the Maven or Gradle wrapper,
So that I can build the project immediately without installing Maven or Gradle globally.

## Acceptance Criteria

1. **Given** a project generated with `buildSystem=MAVEN` **When** the ZIP is extracted **Then** it contains `mvnw`, `mvnw.cmd`, and `.mvn/wrapper/maven-wrapper.properties`; running `./mvnw verify` succeeds without a globally installed Maven

2. **Given** a project generated with `buildSystem=GRADLE_GROOVY` or `buildSystem=GRADLE_KOTLIN` **When** the ZIP is extracted **Then** it contains `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.properties`, and `gradle/wrapper/gradle-wrapper.jar`; running `./gradlew build` succeeds without a globally installed Gradle

3. **Given** any generated project **When** the `README.md` references build commands **Then** all commands use the wrapper form (`./mvnw`, `./gradlew`) — not bare `mvn` or `gradle`

4. **Given** the CI test matrix **When** it validates generated project combinations **Then** each combination is built using the bundled wrapper, not a globally installed build tool

5. **Given** the Gradle wrapper **When** inspected **Then** it targets Gradle 8.14 (current pinned version per NFR14)

## Tasks/Subtasks

- [x] Task 1: Add Maven wrapper to Process Application and Process Archive Maven templates
  - [x] 1.1: Include `mvnw`, `mvnw.cmd` as executable scripts in the template
  - [x] 1.2: Include `.mvn/wrapper/maven-wrapper.properties` pinned to current Maven wrapper version

- [x] Task 2: Add Gradle wrapper to Process Application and Process Archive Gradle templates
  - [x] 2.1: Include `gradlew`, `gradlew.bat` as executable scripts
  - [x] 2.2: Include `gradle/wrapper/gradle-wrapper.properties` pinned to Gradle 8.14
  - [x] 2.3: Include `gradle/wrapper/gradle-wrapper.jar`

- [x] Task 3: Update README templates to use wrapper commands throughout

- [x] Task 4: Update CI test matrix jobs to invoke wrappers instead of bare build tools

## Dev Notes

- Wrapper files must be marked executable (`chmod +x`) in the ZIP entry — set the Unix file permissions in the ZIP metadata
- `gradle-wrapper.jar` is a binary; include it as a resource, not a text template
- Template engine condition: wrappers are unconditional — present in all generated projects regardless of project type or extras

## Change Log

- 2026-06-03: Story created to document FR61 implementation delivered in commit 48f7c1c (feat: Add Maven/Gradle wrapper)
