@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: ${config.artifactId()}
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 3s
      retries: 10
      start_period: 5s
