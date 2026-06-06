@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
spring.application.name=${config.projectName()}
server.port=${config.serverPort()}

# PostgreSQL database — start with: docker compose up -d
spring.datasource.url=jdbc:postgresql://localhost:5432/${config.artifactId()}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=postgres
spring.datasource.password=postgres

# H2 fallback: run with --spring.profiles.active=h2 (no Docker needed)


# Operaton configuration
operaton.bpm.auto-deployment-enabled=true

# External service URLs (override for tests via Testcontainers)
inventory.service.url=http://localhost:8089
payment.service.url=http://localhost:8089
notification.service.url=http://localhost:8089
