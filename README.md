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

## Examples Gallery

The web UI includes a curated **Examples Gallery** showcasing runnable Operaton examples from trusted repositories. Each example is a complete, ready-to-build project demonstrating specific patterns (approval workflows, event-driven processing, embedded engines) across different runtimes (Spring Boot, Quarkus, plain Java) and build systems (Maven, Gradle).

Users can browse, filter, and download examples directly from the gallery, jumpstarting their Operaton journey.

**→ [Publish your own examples](docs/examples-repository-format.md)**

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

### Self-Hosting with MCP

Connect the `operaton-starter-mcp` AI tool to your private instance so AI assistants generate projects against your internal deployment.

**Build and start the server:**

```bash
# 1. Build the fat JAR (required before docker build)
./mvnw verify -pl starter-templates,starter-server -am

# 2. Build the Docker image
docker build -t operaton-starter:local .

# 3. Start the server
docker run -p 8080:8080 operaton-starter:local
```

Or with docker compose (development):

```bash
./mvnw verify -pl starter-templates,starter-server -am
docker compose -f docker-compose.dev.yml up
```

**Configure Claude Desktop** (`~/Library/Application Support/Claude/claude_desktop_config.json`):

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

**Configure VS Code / GitHub Copilot** (`.vscode/mcp.json` or user settings):

```json
{
  "servers": {
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

The `OPERATON_STARTER_URL` variable tells the MCP package which backend to call. Omitting it falls back to `https://start.operaton.org`.

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

## Contributing Examples

Want to share your Operaton example with the community?

1. **Author a new repository** with your example(s) and a `.operaton-starter.yml` manifest
2. **Read the format guide**: [`docs/examples-repository-format.md`](docs/examples-repository-format.md) documents the schema, repository structure, and registration process
3. **Register your repository** either via environment variable (self-hosted) or by opening a PR to add it to the default configuration
4. **Your examples appear** in the public Examples Gallery once deployed

See the [seed repository](https://github.com/kthoms/operaton-examples) for a complete example.

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup instructions and contribution guidelines.

## Releasing

See [`docs/release.md`](docs/release.md) for the release procedure, required GitHub Actions secrets, and troubleshooting guide.

## License

Apache 2.0
