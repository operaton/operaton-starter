---
stepsCompleted: [1, 2]
inputDocuments: []
session_topic: 'Operaton Starter — a web-based project initializer for Operaton BPMN projects (process applications, engine plugins, connectors)'
session_goals: 'Generate ideas for project types, UX patterns, features, architecture, and differentiators vs. existing tools (start.spring.io, code.quarkus.io, start.camunda.com)'
selected_approach: 'ai-recommended'
techniques_used: ['Cross-Pollination', 'SCAMPER Method', 'What If Scenarios', 'Six Thinking Hats']
ideas_generated: []
context_file: ''
---

# Brainstorming Session Results

**Facilitator:** Karsten
**Date:** 2026-03-25

## Session Overview

**Topic:** Operaton Starter — a web-based project initializer for Operaton BPMN projects (process applications, engine plugins, connectors)
**Goals:** Generate ideas for project types, UX patterns, features, architecture, and differentiators vs. existing tools (start.spring.io, code.quarkus.io, start.camunda.com)

### Context Guidance

Operaton is an open-source BPM platform supporting BPMN 2.0, DMN 1.3, and CMMN 1.1. Spring Boot starters available: operaton-bpm-spring-boot-starter, -rest, -webapp. Reference tools researched: Spring Initializr, code.quarkus.io, micronaut.io/launch, start.camunda.com.

### Session Setup

Goal is to design a developer-facing web tool that bootstraps Operaton projects — similar to start.spring.io but tailored for the Operaton ecosystem. Scope includes process applications, engine plugins, and connectors.

## Technique Selection

**Approach:** AI-Recommended Techniques
**Analysis Context:** Product design challenge with known reference points — risk of uninspired clone, opportunity for genuine differentiation.

**Recommended Techniques:**

- **Cross-Pollination:** Mine patterns from unexpected domains outside the BPM/Java ecosystem
- **SCAMPER Method:** 7 lenses applied to the Spring Initializr model to generate concrete feature ideas
- **What If Scenarios:** Push past the obvious into breakthrough territory
- **Six Thinking Hats:** Multi-perspective evaluation of the generated idea pool

**AI Rationale:** Three generative phases (broad → systematic → bold) followed by a structured evaluation phase ensures both quantity and quality, avoiding an uninspired clone of start.spring.io.

---

## Product Vision

> **Operaton Starter** is the official first-party developer onboarding tool for the Operaton ecosystem — a stateless, fast, beautiful project generator at `start.operaton.org` that serves both Practitioners (zero friction, curl-ready) and Explorers (guided, visual, educational) — and doubles as the top of the Operaton adoption funnel.

## User Personas

**Practitioner** — Knows Operaton, knows their project type, wants zero friction. Uses curl, shareable links, IDE deep-link. Never wants to answer "have you used Operaton before?"

**Explorer** — Knows BPMN concepts, possibly migrating from Camunda 7, needs guardrails but not hand-holding. Benefits from gallery, capability tags, inline help, and purposeful README.

> Today's Explorer is tomorrow's Practitioner — the starter's onboarding quality directly drives ecosystem retention.

---

## Design Principles (Emerging)

- **Stateless and simple:** No user profiles, no API keys, no authentication for MVP. As frictionless as possible.
- **Latest version always:** No version dropdown — always generate against current stable. Dependabot/Renovate keep generated projects current.
- **REST API first:** The web UI is a consumer of the generation API, not the other way around.
- **No web modeler integration:** No reliable Operaton web modeler available at this time.
- **Open templates:** Generation templates are open-source and forkable.
- **Camunda migration:** Leverage existing https://github.com/operaton/migrate-from-camunda-recipe tooling.

---

## Phase 1: Cross-Pollination Results

### Project Type Taxonomy
**[Domain #1]: Project Type Taxonomy**
*Concept:* Two distinct process project types — Process Application (engine embedded, e.g. Spring Boot) and Process Archive (engine-agnostic deployable, targets a Standalone Engine runtime). Standalone Engine is a deployment target, not a project type.
*Novelty:* start.camunda.com conflates these; Operaton Starter could be the first initializer to model this distinction explicitly.

### UX Ideas
**[UX #1]: The Skeleton Process**
*Concept:* Generated project includes a minimal but valid BPMN file — a start event, one service task placeholder, and an end event. Runnable but not overwhelming.
*Novelty:* Competing tools generate zero process artifacts. Bridges the blank-page problem for newcomers.

**[UX #2]: Identity-Aware Scaffolding**
*Concept:* The entered Group ID, Artifact ID, and project name flow through into the generated BPMN process ID, Java package names, and Spring application.name — everything coherent from the start.
*Novelty:* start.camunda.com generates generic placeholders; nothing feels "yours."

**[UX #3]: Deployment-Target-Aware Generation**
*Concept:* When a user selects Process Archive, the tool asks for the target runtime (Tomcat, WildFly, JBoss EAP) and adjusts packaging (WAR vs JAR), descriptor files, and classloader configuration accordingly.
*Novelty:* No current BPM initializer models the deploy-target dimension at generation time.

**[UX #4]: Compatibility-Aware Dependency Picker**
*Concept:* Each selectable dependency/module shows a compatibility badge — "Requires Operaton 1.x", "Spring Boot 3.x only", "Java 17+". Incompatible combinations are visually flagged or blocked before generation.
*Novelty:* Surfaces version matrix problems before mvn install fails.

**[UX #5]: Capability Tags on Modules**
*Concept:* Modules/extensions are tagged with capability keywords — tasklist, rest-api, webapps, decision-engine, history — so developers can search by what they want to do rather than knowing Maven artifact names.
*Novelty:* Lowers entry barrier for developers new to the Operaton ecosystem.

**[UX #6]: Ecosystem Signals on Dependencies**
*Concept:* Each Operaton module/extension shows live signals — GitHub stars, last release date, number of known issues — so developers can assess maintenance health before adding a dependency.
*Novelty:* No BPM initializer surfaces health signals. Especially powerful for an open-source ecosystem.

**[UX #7]: "Used Together" Suggestions**
*Concept:* When a user adds operaton-bpm-spring-boot-starter-rest, the tool suggests co-dependencies — npm-style hints based on what other developers commonly add together.
*Novelty:* Reduces the "what did I forget?" anxiety after project generation.

**[UX #8]: Live Project Preview Panel**
*Concept:* As the user selects project type, dependencies, and options, a right-hand panel shows a live file tree preview — pom.xml, processes.xml, Application.java, process.bpmn — updating in real time.
*Novelty:* start.spring.io shows nothing until download. The preview builds confidence and teaches project structure simultaneously.

**[UX #9]: Estimated Footprint Indicator**
*Concept:* A lightweight indicator shows approximate JAR size, startup time class (lean/standard/full), and database requirements. "This project needs a datasource" surfaces as a warning if no DB dependency is selected.
*Novelty:* Prevents common "why does my app fail to start?" when developers forget H2 or datasource config.

**[UX #10]: Visual Project Gallery Landing Page**
*Concept:* The starter opens with a visual card gallery — each card showing a BPMN diagram thumbnail and one-liner. Click a card to pre-fill all configuration. Outcome-first rather than form-first.
*Novelty:* Completely flips the initializer UX. Developers choose by recognizing a scenario rather than constructing a configuration.

**[UX #11]: Version Diff Awareness**
*Concept:* When Operaton releases a new version, the starter shows "What changed in 1.1.0" tooltips — breaking changes, new starters, deprecated modules — inline in the dependency picker.
*Novelty:* Turns the generator into a lightweight changelog interface for the Operaton release cycle.

**[UX #12]: Time-to-First-Run Optimization**
*Concept:* Generated README.md is personalized to the user's chosen stack — exact mvn spring-boot:run command, H2 console URL, link to the process in Operaton Cockpit. Personalized, not generic.
*Novelty:* A README that says "your project loan-approval is ready — run this command" gets followed; generic READMEs get ignored.

**[UX #13]: Shareable Configuration Links**
*Concept:* The current configuration is encoded in the URL. Share the link and a colleague gets the exact same pre-filled form — essential for team standardization.
*Novelty:* start.spring.io does this well; start.camunda.com does not.

### Archetype & Scaffolding Ideas
**[ARCH #14]: Community Archetype Registry**
*Concept:* The web app is a friendly face on top of a versioned archetype registry. Third parties can contribute archetypes (community templates, ISV blueprints) that appear in the gallery without Operaton core team involvement.
*Novelty:* Turns the starter into an ecosystem hub rather than a single-vendor tool.

**[ARCH #15]: Archetype-as-Source-of-Truth**
*Concept:* The starter generates projects by invoking real Maven archetypes under the hood. mvn archetype:generate works identically to the web UI — CLI, CI pipelines, and IDE plugins all share the same generation engine with zero drift.
*Novelty:* First initializer where web UI and mvn archetype:generate are perfectly in sync.

**[ARCH #16]: Archetype Version Pinning**
*Concept:* Users can pin to a specific archetype version for reproducible scaffolding across a long project lifetime — critical for enterprise teams needing consistency between Q1 and Q4 onboarding.
*Novelty:* No current BPM initializer supports generation-time version pinning.

**[ARCH #17]: Composable Sub-generators**
*Concept:* Beyond initial project generation, the starter exposes sub-generators: "Add a new BPMN process", "Add a Java Delegate wired to a task", "Add a connector configuration" — working inside already-created projects.
*Novelty:* All current BPM initializers are one-shot tools. Yeoman proved generators have a lifecycle beyond project creation.

**[ARCH #18]: Interactive CLI Mirror**
*Concept:* A first-class operaton-starter CLI (npm or Java-based) mirroring the web UI — fully interactive prompts, same options, same output. npx create-operaton-app my-loan-app or mvn operaton:init.
*Novelty:* Terminal-first developers never need to open a browser for project setup.

**[ARCH #19]: Blueprint / Profile System**
*Concept:* Curated "Blueprints" — opinionated full-stack configurations endorsed by the Operaton team. "Production Blueprint" adds datasource pooling, history cleanup, health endpoints. "Cloud Blueprint" adds Docker/K8s manifests and Helm chart skeleton.
*Novelty:* Moves the starter from "dependency picker" to "architecture accelerator" encoding production knowledge.

**[ARCH #20]: Docker Compose Generation**
*Concept:* Generated projects optionally include a docker-compose.yml — Operaton engine container, PostgreSQL or H2, and the application itself — ready for docker compose up immediately.
*Novelty:* JHipster proved developers love this. No BPM initializer currently generates container orchestration files.

**[ARCH #21]: CI/CD Pipeline Skeleton**
*Concept:* Optionally generate a GitHub Actions / GitLab CI / Jenkins pipeline that builds, tests, and packages the project — including process deployment validation and Operaton REST API smoke tests.
*Novelty:* A generated pipeline that validates deployment from day one prevents entire categories of runtime failures.

**[ARCH #22]: Multi-Module Project Generation**
*Concept:* For teams building multiple process archives on a shared engine, generate a Maven multi-module project — parent POM, individual process archive modules, shared domain library, standalone engine module — coherent from the start.
*Novelty:* Enterprise Operaton projects almost always end up multi-module. Generating the structure upfront avoids painful restructuring.

**[ARCH #23]: Existing Project Integration Mode**
*Concept:* "Add to existing project" mode — given a Group ID and detected project structure, generates only the incremental files needed to add Operaton to an existing Maven project. Brownfield-first.
*Novelty:* Most real-world adoption is brownfield. A starter that works on existing projects is dramatically more useful.

**[ARCH #24]: Vertical Slice Generation**
*Concept:* When generating a Process Application with a service task, generates the full vertical slice: BPMN with service task wired to a Java delegate, the JavaDelegate implementation stub, and a JUnit test stub that deploys and asserts task completion. Everything connected, nothing orphaned.
*Novelty:* Spring Initializr generates horizontal layers (dependencies). Rails proved vertical slices are more immediately useful — runnable tests on day zero.

---

## Phase 2: SCAMPER Results (Refined)

### S — Substitute

**[SCAMPER #25]: ~~MCP-Backed Live Metadata~~ — DROPPED**
*Reason:* Costs tokens and is slow. Static metadata is the right approach.

**[SCAMPER #26]: Git Repository Init as Output — POST-MVP**
*Concept:* Optional push of generated project directly to GitHub/GitLab/Gitea. Violates stateless principle — requires user profiles and API keys. Revisit after MVP.
*Novelty:* Eliminates unzip-rename-init-git ceremony. Good future feature.

**[SCAMPER #27]: IDE Deep-Link Output — KEEP**
*Concept:* "Open in IntelliJ IDEA" / "Open in VS Code" button triggering the IDE's project import protocol directly — like code.quarkus.io does with IntelliJ.
*Novelty:* Reduces time-to-coding from ~2 minutes to ~10 seconds.

### C — Combine

**[SCAMPER #28]: ~~Starter + Web Modeler~~ — DROPPED**
*Reason:* No reliable Operaton web modeler available at this time.

**[SCAMPER #29]: Starter + Purposeful README — MODIFIED**
*Concept:* Generated README.md with the basic project structure, a "what to do next" checklist (e.g. "1. Run the app, 2. Open Cockpit at localhost:8080, 3. Deploy your first process"), and links to relevant Operaton docs. No ADR templates.
*Novelty:* A README that is a genuine next-steps guide, not boilerplate filler.

**[SCAMPER #30]: Dependency Update Bot Choice — KEEP**
*Concept:* Generated projects include a pre-configured Dependabot or Renovate config (user's choice at generation time) watching specifically for Operaton version updates.
*Novelty:* Keeps generated projects current automatically. Aligns with "always latest" principle — Dependabot/Renovate is how versions stay updated after generation.

**[SCAMPER #31]: REST API + MCP Module — KEEP + EXTENDED**
*Concept:* Expose a full REST API for project generation (POST /starter.zip). Additionally, provide an MCP module that wraps this REST API — enabling AI assistants and IDE agents to generate Operaton projects programmatically via MCP tool calls.
*Novelty:* The web UI, CLI, IDE plugins, and AI agents all share the same generation API. The MCP module makes Operaton Starter natively accessible from Claude, Copilot, and other AI tools.

**[SCAMPER #32]: Formula / Recipe System — POST-MVP**
*Concept:* Community-maintained named recipes combining project type + dependencies + blueprints into a shareable versioned unit. Interesting but out of scope for MVP.
*Novelty:* Consulting firms and platform teams could maintain org-standard recipes.

### A — Adapt

**[SCAMPER #31-API]: REST API (detail)**
*Concept:* Full REST API identical to start.spring.io's API model — powers IDE plugins (IntelliJ, Eclipse, VS Code), CLI tools, and CI scripts. MCP module is a thin wrapper on top.

**[SCAMPER #35]: Open-Source Generation Templates — KEEP**
*Concept:* Generation templates (Mustache/Freemarker/Qute) are open-source in a public repo — teams can fork, add conventions, and self-host a customized Operaton Starter.
*Novelty:* Template transparency builds community trust and enables enterprise customization.

### M — Modify / Magnify

**[SCAMPER #33, #34]: ~~Rich Type Graph / Validate Before Generate~~ — DROPPED**
*Reason:* Over-engineered for MVP scope.

### P — Put to Other Uses

**[SCAMPER #36]: Camunda 7 Migration Mode — KEEP**
*Concept:* "Migrate from Camunda 7" project type using https://github.com/operaton/migrate-from-camunda-recipe — generates a migration scaffold with OpenRewrite recipe integration, dependency substitutions, and a checklist of manual migration steps.
*Novelty:* Operaton's #1 adoption path is Camunda 7 migration. First-class support in the starter directly addresses the biggest real-world use case.

**[SCAMPER #37]: Backstage Plugin — KEEP**
*Concept:* Package Operaton Starter as a Backstage Software Template plugin — enterprise developers generating Operaton projects from within their existing IDP, with org-specific defaults.
*Novelty:* Enterprise adoption happens through IDPs. Being a first-class Backstage plugin means appearing where enterprise developers already work.

**[SCAMPER #38]: Learning Path Entry Point — KEEP**
*Concept:* Generated project files include inline links to relevant Operaton documentation — processes.xml links to deployment descriptor docs, JavaDelegate stub links to service task guide. The project is also a learning map.
*Novelty:* Turns a one-time generation tool into an ongoing onboarding resource.

### E — Eliminate

**[SCAMPER #39]: Eliminate the Version Dropdown — KEEP**
*Concept:* No manual Operaton version selection — always generate against current stable release. Post-generation, Dependabot or Renovate (chosen at generation time, #30) keeps the project current automatically.
*Novelty:* Eliminates the confusion of snapshot/milestone/GA version choices. "Latest" is always right for new projects.

**[SCAMPER #40]: Eliminate Boilerplate Config Classes — KEEP**
*Concept:* Generated projects rely entirely on Spring Boot autoconfiguration and application.properties — zero explicit @Configuration classes for engine setup. If a config class is needed, the starter is incomplete.
*Novelty:* Enforces modern Spring Boot idioms; avoids the verbose config patterns from older Camunda getting-started guides.

### R — Reverse

**[SCAMPER #41, #42]: ~~Process-First Generation / Diagnostic Question Mode~~ — DROPPED**
*Reason:* Out of scope.

---

## Phase 3: What If Scenarios Results

**[WI #43]: Starter as Official Onboarding Gateway**
*Concept:* operaton.org "Get Started" button deep-links to the starter with a pre-filled "Hello World Process Application" — one click, download, run. Replaces all "copy this pom.xml" sections in docs.
*Impact:* Every doc page's setup section becomes a single button. 20 minutes → 2 minutes to first running process.

**[WI #44]: Community Configuration Gallery — POST-MVP**
*Concept:* Browsable gallery of named, community-submitted starter configurations. One click to pre-fill the form with any community config.

**[WI #45]: Pure curl/wget Generation**
*Concept:* `curl https://start.operaton.org/starter.zip -d type=process-application -d artifact=my-app -o my-app.zip` — single curl command, no browser, no auth. Documented with copy-paste examples for every project type.
*Impact:* CI pipelines, onboarding scripts, dotfiles — all can generate fresh Operaton projects without human interaction.

**[WI #46]: Green on First Push**
*Concept:* Generated project guarantee — skeleton compiles, tests pass, CI pipeline succeeds out of the box. First thing a developer sees in GitHub Actions is a green checkmark.
*Impact:* Sets "this project works, I can only improve it" as the baseline. Eliminates demoralizing setup failures.

**[WI #47]: AI-Native Generation Flow**
*Concept:* MCP module documented and published so AI assistants (Claude, GitHub Copilot, Cursor) can call it as a tool during development conversations — detecting intent and triggering generation without visiting the website.
*Impact:* Positions Operaton Starter as AI-native from day one.

**[WI #48]: Tested Stack Snapshots — POST-MVP**
*Concept:* Curated "stack compatibility snapshots" — Operaton + Spring Boot + Java combinations that have been integration-tested together. Choose a snapshot, not individual versions.

**[WI #49]: Upgrade-Aware Generation — POST-MVP**
*Concept:* Maven plugin applying archetype diffs to existing projects — shows what the latest archetype would change, asks for confirmation, applies.

**[WI #50]: Self-Hostable Enterprise Edition**
*Concept:* Docker image configurable via environment variables — default group ID, internal Maven registry, custom archetype registry, corporate branding. `docker run -p 8080:8080 operaton/starter`.
*Impact:* Enterprise teams get the full starter experience without using public SaaS tools.

---

## Phase 4: Six Thinking Hats + Party Mode Results

### New Ideas from Hats & Party Mode

**[HAT #51]: Persona-Adaptive Split Landing (Sally's refinement)**
*Concept:* Single viewport — clean form on the left, visual project gallery on the right. Practitioners click the form instinctively; Explorers are drawn to the gallery cards. No routing question asked — pure visual self-selection.
*Novelty:* Resolves the dual-persona tension without condescending to either.

**[HAT #52]: Inline "Explain This" Help**
*Concept:* Every dependency, option, and field has a collapsible "What is this?" tooltip for Explorers — collapsed by default so Practitioners never see them.
*Novelty:* Inline contextual education without polluting the Practitioner experience.

**[HAT #53]: Social Proof Usage Stats — POST-MVP**
*Concept:* Anonymized usage stats inline — "87% of Process Application projects include the REST starter" — as soft guidance for Explorers. Requires real usage data first.

**[ARCH #54]: start.operaton.org Infrastructure Model**
*Concept:* Stateless Spring Boot app deployed as Docker container at start.operaton.org. Single public instance + self-hostable image. Same binary — zero divergence between public and self-hosted.
*Novelty:* First-party ownership means the starter is updated on the same day as every Operaton release.

**[ARCH #55]: Anonymous Usage Telemetry (Opt-in) — POST-MVP**
*Concept:* Opt-in anonymous telemetry — project types, module selections, Operaton version. Zero personal data, purely aggregate. Feeds social proof stats (#53) and roadmap decisions.

**[UX #56]: Unified Design System**
*Concept:* start.operaton.org adopts the same design system, color palette, and nav as operaton.org and docs.operaton.org — seamless visual handoff across the developer journey.

**[UX #57]: "What's New" Release Banner**
*Concept:* Slim dismissible banner when a new Operaton version drops — "Operaton 1.1 is out! Projects now generate with the latest stable." Links to changelog.

**[CORE #58]: Maven & Gradle Build System Choice**
*Concept:* At generation time, user selects Maven (default) or Gradle (Groovy DSL or Kotlin DSL). Separate template sets per build system. All project types support both.
*Novelty:* start.camunda.com is Maven-only. Supporting Gradle signals ecosystem maturity.

---

## MVP Scope (Final)

### 🟢 MVP — v1.0

**Core Generation Engine**
- Project types: Process Application, Process Archive, Engine Plugin, Connector, Camunda Migration
- Build system: Maven or Gradle (Groovy / Kotlin DSL) [#58]
- Identity-aware scaffolding [#2]
- Minimal skeleton BPMN [#1]
- Deployment-target selector for Process Archive [#3]
- Zero boilerplate config classes [#40]
- Always latest Operaton version — no dropdown [#39]
- Dependabot or Renovate — user's choice [#30]
- Purposeful README with next-steps checklist [#29]
- Docker Compose generation — optional toggle [#20]
- GitHub Actions CI/CD skeleton [#46]
- Camunda 7 migration mode — migrate-from-camunda-recipe [#36]

**UX**
- Split landing: form (left) + gallery (right) [#HAT51]
- Live project preview panel — real-time file tree [#8]
- Capability tags on modules [#5]
- Shareable config links — URL encoding [#13]
- IDE deep-link — IntelliJ + VS Code [#27]
- Inline "explain this" collapsible help [#HAT52]

**API & Ecosystem**
- Full REST API — POST /starter.zip [#31]
- MCP module wrapping the REST API [#31]
- Pure curl/wget support — documented examples [#45]

**Infrastructure**
- Deployed at start.operaton.org [#54]
- Self-hostable Docker image [#50]
- Open-source generation templates [#35]
- Archetype-as-source-of-truth — web UI and mvn archetype:generate in sync [#15]

### 🟡 Post-MVP — v1.x
- Live project preview panel [#8] ← moved to MVP
- Social proof / usage stats [#53] — needs real data
- Opt-in telemetry [#55]
- "What's New" release banner [#57]
- Inline doc links in generated files [#38]
- Tested stack snapshots [#48]
- Interactive CLI mirror — npx create-operaton-app [#18]
- Multi-module project generation [#22]
- Backstage plugin [#37]
- Community gallery [#44]
- Formula/recipe system [#32]
- Git repo push [#26]
- Upgrade-aware Maven plugin [#49]

---

## Proposed Module Structure

```
operaton-starter/
├── starter-server/        # Spring Boot app, REST API
├── starter-web/           # Frontend (the UI)
├── starter-mcp/           # MCP module wrapping the API
├── starter-templates/     # Open-source generation templates
└── starter-archetypes/    # Maven archetypes
```

---

## Next Steps

1. **PRD** — Product Requirements Document (personas, user journeys, functional requirements, success metrics)
2. **Architecture** — Technical design session (template engine, API contract, live preview data flow, MCP module, monorepo structure)
3. **Epics & Stories** — Sprint-ready backlog from MVP scope
