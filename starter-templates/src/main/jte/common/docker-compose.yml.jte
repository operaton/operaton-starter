@import org.operaton.dev.starter.templates.model.ProjectConfig
@import org.operaton.dev.starter.templates.model.ProjectType
@param ProjectConfig config
version: "3.9"

services:
  app:
    build: .
    ports:
      - "${config.serverPort()}:${config.serverPort()}"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
@if(config.projectType() == ProjectType.PROCESS_APPLICATION)
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:${config.serverPort()}/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
@endif
