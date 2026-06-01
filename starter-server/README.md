# starter-server

## Role

Spring Boot REST API for operaton-starter. Exposes two endpoints:

- `POST /api/v1/generate` — accepts a `ProjectConfig` JSON body, delegates to `starter-templates`, returns a ZIP file
- `GET /api/v1/metadata` — returns available project types, build systems, Java versions, and defaults

Serves the Vue SPA (`starter-web`) as static assets in production.

## Prerequisites

- Java 21+
- Maven 3.9+

## Build in Isolation

```bash
mvn verify -pl starter-server -am
```

## Run Locally

```bash
mvn spring-boot:run -pl starter-server -am
```

Server starts on port `8080`. Health check: `http://localhost:8080/actuator/health`

API documentation (ReDoc): `http://localhost:8080/api-docs/`

## Example

Generate a Maven Process Application project and save it to a file:

```bash
curl -s -X POST http://localhost:8080/api/v1/generate \
  -H 'Content-Type: application/json' \
  -d '{
    "projectType": "PROCESS_APPLICATION",
    "buildSystem": "MAVEN",
    "groupId": "com.example",
    "artifactId": "my-app",
    "projectName": "My App",
    "javaVersion": 21,
    "dockerCompose": false,
    "githubActions": true
  }' \
  --output my-app.zip
```

For environment variable configuration (CORS, defaults), see the [root README](../README.md#self-hosting).
