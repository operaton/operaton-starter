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

# Load data.sql to seed users, groups, and memberships
spring.sql.init.mode=always

# Operaton configuration
operaton.bpm.auto-deployment-enabled=true
