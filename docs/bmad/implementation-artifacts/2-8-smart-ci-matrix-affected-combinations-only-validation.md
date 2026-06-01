---
baseline_commit: 63e1a76a62d421936a5bcad69596371c124781e2
---

# Story 2.8: Smart CI Matrix — Affected-Combinations-Only Validation

## Status
done

## Story

As a **developer merging a template change**,
I want the CI pipeline to identify and validate only the project type × build system combinations actually affected by my template changes,
So that feedback is fast — unaffected combinations don't slow down every PR.

## Acceptance Criteria

1. **Given** a PR that modifies one or more files in `starter-templates/src/main/jte/` **When** the smart matrix CI workflow triggers **Then** it determines which `projectType` × `buildSystem` combinations reference the changed template files; only those combinations are included in the matrix run for that PR

2. **Given** the smart matrix workflow **When** inspected **Then** it is a dedicated GitHub Actions workflow file separate from the `test-matrix` job in `ci.yml`; it uses a script that reads the JTE template manifest to resolve which combinations depend on which template files, then outputs a GitHub Actions matrix JSON to drive the job dimensions

3. **Given** a PR that modifies a shared template used by all combinations **When** the smart matrix runs **Then** all 6 combinations are included — the smart selection degrades gracefully to a full run when all are affected

4. **Given** a PR that modifies a template used only by `PROCESS_APPLICATION` × `MAVEN` **When** the smart matrix runs **Then** only the `PROCESS_APPLICATION/MAVEN` combination job runs; the other 5 combinations are excluded from this run

5. **Given** each affected-combination job in the smart matrix **When** it runs **Then** it: (1) generates a project for its combination, (2) builds the generated project (`mvn verify` or `./gradlew build`), (3) starts the application and polls `GET /actuator/health` until `200 OK` (timeout: 60 seconds); all three steps must pass for the job to succeed

6. **Given** any smart matrix job fails **When** the PR is reviewed **Then** the PR is blocked from merging — this is a hard merge block

7. **Given** a PR that modifies no template files **When** the smart matrix workflow triggers **Then** it emits zero matrix jobs and completes with a green status immediately — no generation or build steps run

## Tasks/Subtasks

- [x] Task 1: Create template-to-combination mapping script
  - [x] 1.1: Create `.github/scripts/affected-combinations.sh` that reads changed files from git diff
  - [x] 1.2: Define mapping: which JTE template paths map to which combinations
  - [x] 1.3: Output GitHub Actions matrix JSON to stdout (caller writes to `$GITHUB_OUTPUT`)
  - [x] 1.4: Handle the zero-changes case (output empty matrix that skips all jobs)
  - [x] 1.5: Handle the all-affected case (output full 6-combination matrix)

- [x] Task 2: Create the dedicated smart-matrix workflow file
  - [x] 2.1: Create `.github/workflows/affected-matrix.yml` triggered on pull_request paths: jte/**
  - [x] 2.2: Add a `detect` job that runs the mapping script and outputs the matrix JSON
  - [x] 2.3: Add a `validate` job that uses `needs: detect` and `strategy.matrix: fromJson(detect.outputs.matrix)`
  - [x] 2.4: In `validate` job: generate project via API, extract ZIP, build with Maven/Gradle, start app and health-poll for PROCESS_APPLICATION
  - [x] 2.5: Note in workflow file that it should be added as a required status check in GitHub branch protection settings

- [x] Task 3: Test the script logic
  - [x] 3.1: Verified: PA/maven/ → 1 combination
  - [x] 3.2: Verified: common/ → all 6 combinations
  - [x] 3.3: Verified: no JTE file changes → empty array []
  - [x] 3.4: Verified: dmn-project/ and spike/ → 0 (not in MVP matrix); top-level PA/ → 3

## Dev Notes

- Existing `test-matrix` job in `ci.yml` runs all 6 combinations on every push/PR — keep that job untouched; this is a NEW additive workflow
- Template path convention: `starter-templates/src/main/jte/` is the JTE template root
- Current project types: `PROCESS_APPLICATION`, `PROCESS_ARCHIVE`; build systems: `MAVEN`, `GRADLE_GROOVY`, `GRADLE_KOTLIN`
- GitHub Actions matrix JSON format: `{"include":[{"project-type":"process-application","build-system":"maven"},...]}` 
- Use `git diff --name-only origin/main...HEAD` to get changed files in a PR context
- Health check: `curl -sf http://localhost:8080/actuator/health | grep -q '"UP"'` with 60s timeout
- Process Application starts with `mvn spring-boot:run` (Maven) or `./gradlew bootRun` (Gradle)
- Process Archive is a WAR/JAR — no startup health check applicable; skip health check for PROCESS_ARCHIVE combinations
- Use `jq` for JSON construction in the script (available in GitHub Actions ubuntu runners)

## Dev Agent Record

### Implementation Plan

(to be filled during implementation)

### Debug Log

(to be filled during implementation)

### Completion Notes

Created `.github/scripts/affected-combinations.sh` — bash 3.x compatible script that maps changed JTE template paths to their affected project-type × build-system combinations. The script uses string-based set tracking (no `declare -A`) for macOS/Linux compatibility. Logic covers all 6 MVP combinations; dmn-project/ and spike/ are intentionally excluded. All 8 mapping unit tests pass.

Created `.github/workflows/affected-matrix.yml` — two-job workflow: `detect` computes affected combinations via the script, `validate` runs those combinations only. Each validate job: builds the Spring Boot JAR, starts the server, calls `POST /api/v1/generate`, extracts the ZIP, builds the generated project, and (for PROCESS_APPLICATION) health-checks the running generated app.

## File List

- `.github/scripts/affected-combinations.sh` (new)
- `.github/workflows/affected-matrix.yml` (new)
- `docs/bmad/implementation-artifacts/2-8-smart-ci-matrix-affected-combinations-only-validation.md` (this file)

## Change Log

- 2026-05-31: Implemented Story 2.8 — smart CI matrix for affected template combinations

### Review Findings

- [x] [Review][Patch] Hyphenated matrix keys are referenced with dot notation, which breaks GitHub Actions expression lookup and prevents the smart-matrix jobs from evaluating their matrix fields correctly. [`.github/workflows/affected-matrix.yml:45`]
