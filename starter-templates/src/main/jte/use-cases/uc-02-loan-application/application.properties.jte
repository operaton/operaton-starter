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

# WireMock credit score service URL (override for tests or local dev)
credit.score.service.url=http://localhost:8089

# Spring Mail — Mailpit SMTP (start with: docker compose up -d)
spring.mail.host=localhost
spring.mail.port=1025
spring.mail.protocol=smtp
