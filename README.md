# operaton-starter

The open-source project generator for the [Operaton](https://operaton.org) BPM ecosystem — hosted at **[start.operaton.org](https://start.operaton.org)**.

Bootstrap Operaton-based projects as downloadable, ready-to-build, immediately runnable archives. Like Spring Initializr, but for Operaton.

## What It Generates

| Project Type | Description |
|-------------|-------------|
| **Process Application** | Spring Boot application embedding the Operaton engine; skeleton BPMN + Java delegate stub |
| **Process Archive** | Engine-agnostic WAR/JAR for deployment to a Standalone Engine or Tomcat |

**Build systems:** Maven · Gradle (Groovy DSL) · Gradle (Kotlin DSL)

Every generated project:
- Compiles and starts on first run — no manual configuration required
- Targets the current stable Operaton release (auto-updated via Renovate)
- Includes a GitHub Actions CI/CD skeleton that passes green on first push
- Propagates Group ID, Artifact ID, and project name into BPMN process IDs, Java packages, and `spring.application.name`
- Includes a purposeful README with next-step instructions and troubleshooting

## Access Channels

**Web UI** — `start.operaton.org`

Browser-based generator with a project gallery (Explorer path) and a configuration form (Practitioner path). Live file tree preview, IDE deep-links, shareable config URLs.

**CLI**
```bash
npx operaton-starter \
  --groupId com.example \
  --artifactId my-process \
  --projectName "My Process" \
  --projectType PROCESS_APPLICATION \
  --buildSystem GRADLE_KOTLIN \
  > my-process.zip
```

**curl**
```bash
curl -X POST https://start.operaton.org/api/v1/generate \
  -H "Content-Type: application/json" \
  -d '{"projectType":"PROCESS_APPLICATION","buildSystem":"MAVEN","groupId":"com.example","artifactId":"my-process","projectName":"My Process","javaVersion":17}' \
  -o my-process.zip
```

**MCP** — `operaton-starter-mcp` npm package

Exposes a `generate_project` tool callable by AI assistants (Claude, GitHub Copilot, Cursor) during development conversations.

## Self-Hosting

Run your own instance with Docker:

```bash
docker run -p 8080:8080 \
  -e STARTER_DEFAULTS_GROUP_ID=com.myorg \
  docker.io/operaton/operaton-starter:latest
```

The image starts with zero external network calls. All configuration is via environment variables.

| Variable | Description |
|----------|-------------|
| `STARTER_DEFAULTS_GROUP_ID` | Pre-fills the Group ID field |
| `STARTER_DEFAULTS_MAVEN_REGISTRY` | Maven repository URL for generated projects |
| `STARTER_DEFAULTS_OPERATON_VERSION` | Pin Operaton version (self-hosted only) |
| `STARTER_CORS_ALLOWED_ORIGINS` | Comma-separated CORS allowlist for `/api/**` |

## Architecture

operaton-starter is a 6-module Maven monorepo:

```
operaton-starter/
├── starter-templates/   Pure-Java generation engine (zero Spring dependency)
├── starter-server/      Spring Boot REST API
├── starter-archetypes/  GenerationClient interface + mvn archetype:generate integration
├── starter-web/         Vue 3 SPA
├── starter-mcp/         MCP npm package (operaton-starter-mcp)
└── starter-cli/         CLI npm package (operaton-starter)
```

All channels invoke the same `GenerationEngine.generate(ProjectConfig) → byte[]` — identical output guaranteed by a 6-combination CI test matrix (2 project types × 3 build systems).

See [`docs/arc42/`](docs/arc42/) for full architecture documentation.

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup instructions and contribution guidelines.

## License

Apache 2.0
