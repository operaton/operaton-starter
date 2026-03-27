# Arc42 Section 7: Deployment View

## Production Deployment: start.operaton.org

```mermaid
flowchart TD
    subgraph internet["Internet"]
        BROWSER["Browser"]
        CLI["CLI / curl"]
        MCP_CLIENT["AI Assistant\n(MCP client)"]
    end

    subgraph operaton_infra["operaton.org infrastructure"]
        LB["Reverse Proxy / Load Balancer\n(HTTPS termination, X-Forwarded-For)"]
        subgraph docker["Docker container"]
            APP["starter-server\n(Spring Boot JAR)\nJRE: eclipse-temurin:25"]
        end
    end

    subgraph registries["External Registries (CI only)"]
        DOCKERHUB["docker.io/operaton/operaton-starter"]
        NPM["npm Registry\noperaton-starter-mcp\noperaton-starter"]
    end

    BROWSER -->|HTTPS 443| LB
    CLI -->|HTTPS 443| LB
    MCP_CLIENT -->|HTTPS 443| LB
    LB -->|HTTP 8080| APP
    DOCKERHUB -->|docker pull| docker
```

**Deployment characteristics:**
- Stateless: any number of container replicas; no sticky sessions
- Zero external dependencies at runtime (no DB, no Redis, no external API calls)
- Docker image published to `docker.io/operaton/operaton-starter` on every tagged release
- Reverse proxy handles HTTPS; application serves HTTP on port 8080

## Docker Image

**Base image:** `eclipse-temurin:25-jre-alpine`

**Layer structure (Spring Boot layered JAR):**
```dockerfile
FROM eclipse-temurin:25-jre-alpine AS builder
WORKDIR /app
COPY starter-server/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

Layer extraction maximizes Docker build cache efficiency — dependency layers (rarely change) are separated from application layers (change with every release).

**Network isolation test:** CI verifies the image starts and responds to `GET /actuator/health` with `--network none` (no outbound network access at runtime).

## CI/CD Pipeline

```mermaid
flowchart LR
    PR["PR / push to main"] --> BJ["build-java\nmvn verify"]
    PR --> TM["test-matrix\n6 parallel jobs\n(2 types × 3 build systems)"]
    PR --> CC["contract-check\nvalidate clients vs openapi.yaml\n⚠️ warning level"]
    PR --> LW["lint-web\nESLint + Vitest + axe-core"]

    TAG["on tag (release)"] --> BJ2["build-java"]
    TAG --> DP["docker-publish\npush eclipse-temurin:25 image\nto docker.io/operaton/operaton-starter"]
    TAG --> NP["npm-publish\noperaton-starter-mcp → npm\noperaton-starter → npm"]
```

### CI Jobs Detail

**`build-java`** (`ci.yml`)
- `mvn verify` — compiles, runs unit tests, ArchUnit zero-Spring enforcement
- Includes `ZeroSpringDependencyTest` for `starter-templates`
- Hard block on failure

**`test-matrix`** (`ci.yml`)
- 6 parallel shell jobs: `PROCESS_APPLICATION × MAVEN`, `PROCESS_APPLICATION × GRADLE_GROOVY`, `PROCESS_APPLICATION × GRADLE_KOTLIN`, `PROCESS_ARCHIVE × MAVEN`, `PROCESS_ARCHIVE × GRADLE_GROOVY`, `PROCESS_ARCHIVE × GRADLE_KOTLIN`
- Each job: call `POST /api/v1/generate` → extract ZIP → `cd` into project → run build command
- Smoke test: `mvn spring-boot:run` (or Gradle equivalent) → wait for `GET /actuator/health` to return 200 within 60s
- Pinned Java version (matches generated project Java floor)
- Hard block on failure

**`contract-check`** (`ci.yml`)
- Regenerates OpenAPI clients → diffs against committed versions
- Warning-level PR status (not merge block) in Phase 1
- Promoted to hard block in Phase 2 once spec is stable

**`lint-web`** (`ci.yml`)
- ESLint + Prettier check
- Vitest unit tests
- axe-core accessibility audit (WCAG 2.1 AA, hard block)
- Covers `starter-web`, `starter-mcp`, `starter-cli`

**`docker-publish`** (`release.yml`, on tag)
- Builds production Docker image
- Pushes to `docker.io/operaton/operaton-starter:${tag}` and `:latest`
- 30-second smoke test: `docker run --network none` + health check

**`npm-publish`** (`release.yml`, on tag)
- Publishes `operaton-starter-mcp` to npm
- Publishes `operaton-starter` (CLI) to npm

## Self-Hosted Deployment

Klaus persona deployment pattern:

```bash
docker run -d \
  -p 8080:8080 \
  -e DEFAULT_GROUP_ID=com.bank \
  -e MAVEN_REGISTRY=https://nexus.bank.internal/repository/maven-public \
  -e STARTER_DEFAULTS_OPERATON_VERSION=1.0.0 \
  docker.io/operaton/operaton-starter:latest
```

**Environment variables:**

| Variable | Scope | Description |
|----------|-------|-------------|
| `DEFAULT_GROUP_ID` | Self-hosted | Pre-fills Group ID field in web UI and metadata defaults |
| `MAVEN_REGISTRY` | Self-hosted | Maven repository URL injected into generated `pom.xml` / `build.gradle` |
| `STARTER_DEFAULTS_OPERATON_VERSION` | Self-hosted only | Pins Operaton version; public instance uses version baked at build time |
| `CORS_ALLOWED_ORIGINS` | Self-hosted | Additional CORS origins (default: `start.operaton.org`, `localhost`) |

**Key property:** The Docker image starts with zero external network calls. All configuration is via environment variables.

## Local Development Environment

`docker-compose.dev.yml` at project root (distinct from `docker-compose.yml` generated inside project archives):

```yaml
services:
  backend:
    build:
      context: .
      target: builder
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
```

Allows `starter-web` frontend developers to run the backend without a full Maven build:
```bash
docker compose -f docker-compose.dev.yml up backend
npm run dev  # starter-web Vite dev server proxies API to localhost:8080
```
