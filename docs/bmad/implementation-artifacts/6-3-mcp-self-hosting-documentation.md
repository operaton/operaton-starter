---
baseline_commit: 63e1a76a62d421936a5bcad69596371c124781e2
---

# Story 6.3: MCP Self-Hosting Documentation

## Status
done

## Story

As a **developer running a self-hosted Operaton Starter instance**,
I want clear documentation on how to connect the `operaton-starter-mcp` npm package to my private instance,
So that AI assistants in my team can generate projects against our internal deployment without pointing at the public instance.

## Acceptance Criteria

1. **Given** the root `README.md` **When** inspected **Then** it contains a "Self-Hosting with MCP" section that explains: (1) how to start the Docker image, (2) how to set `OPERATON_STARTER_URL` when registering `operaton-starter-mcp` in an AI assistant's MCP config, (3) a complete working example MCP config JSON snippet that a developer can copy-paste

2. **Given** the self-hosting documentation section **When** inspected **Then** it documents the Docker build prerequisite explicitly: `mvn verify` must complete before `docker build` is run; a one-liner command sequence is provided

3. **Given** `docker-compose.dev.yml` **When** expanded in this story **Then** it includes a commented-out service entry or environment variable showing how the MCP package connects to the backend via `OPERATON_STARTER_URL`

4. **Given** the documentation **When** a developer follows it from zero **Then** they can have a locally running self-hosted instance accessible from their AI assistant's MCP client in under 5 minutes — this is the manual acceptance gate

## Tasks/Subtasks

- [x] Task 1: Add "Self-Hosting with MCP" section to root README.md
  - [x] 1.1: Add section with build prerequisite sequence: `mvn verify` then `docker build`
  - [x] 1.2: Add Docker run command with `OPERATON_STARTER_URL` environment variable explanation
  - [x] 1.3: Add Claude Desktop MCP config JSON snippet (complete, copy-paste ready)
  - [x] 1.4: Add VS Code / Copilot MCP config snippet (settings.json format)
  - [x] 1.5: Keep section concise — under 50 lines including code blocks

- [x] Task 2: Update docker-compose.dev.yml with MCP connection comment
  - [x] 2.1: Add a comment block showing how to override `OPERATON_STARTER_URL` in the MCP package to point at the local service
  - [x] 2.2: Ensure the existing service definition is unchanged

- [x] Task 3: Verify documentation accuracy
  - [x] 3.1: Confirm `OPERATON_STARTER_URL` env var name matches `starter-mcp/src/index.ts` implementation
  - [x] 3.2: Confirm MCP config JSON structure matches MCP SDK expectations
  - [x] 3.3: Verify the Claude Desktop config path example is correct for macOS

## Dev Notes

- Root README.md already has a "Self-Hosting" section — add "Self-Hosting with MCP" as a subsection or extend it
- The `starter-mcp` module uses `OPERATON_STARTER_URL` (check `starter-mcp/src/index.ts` to confirm env var name)
- Claude Desktop MCP config lives at `~/Library/Application Support/Claude/claude_desktop_config.json` on macOS
- Claude Desktop MCP config format:
  ```json
  {
    "mcpServers": {
      "operaton-starter": {
        "command": "npx",
        "args": ["-y", "operaton-starter-mcp"],
        "env": {
          "OPERATON_STARTER_URL": "http://localhost:8080"
        }
      }
    }
  }
  ```
- The Dockerfile already exists at project root; docker-compose.dev.yml already runs the service on port 8080
- Keep the documentation factual and minimal — no marketing language; developer audience

## Dev Agent Record

### Implementation Plan

1. Added "Self-Hosting with MCP" subsection to README.md after the env var table in the "Self-Hosting" section. Covers: build prerequisites (mvnw verify then docker build), docker run and docker compose options, Claude Desktop config JSON, VS Code/Copilot config JSON, and OPERATON_STARTER_URL explanation.
2. Added MCP connection comment to docker-compose.dev.yml explaining OPERATON_STARTER_URL.
3. Verified OPERATON_STARTER_URL in starter-mcp/src/index.ts line 12 — confirmed exact match.

### Debug Log

(none)

### Completion Notes

All 3 tasks complete. Documentation verified accurate. Section is 46 lines (under 50 limit). Ready for review.

## File List

- `README.md` — modified: added "Self-Hosting with MCP" subsection
- `docker-compose.dev.yml` — modified: added MCP connection comment

## Change Log

- 2026-05-31: Story implemented by Dev Agent. All tasks complete, status → review.
