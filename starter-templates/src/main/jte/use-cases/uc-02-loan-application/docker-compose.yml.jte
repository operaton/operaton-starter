@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
services:
  wiremock:
    image: wiremock/wiremock:3.5.4
    ports:
      - "8089:8080"
    volumes:
      - ./src/main/resources/wiremock:/home/wiremock
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/__admin/mappings"]
      interval: 5s
      timeout: 3s
      retries: 10
      start_period: 5s
