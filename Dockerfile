# ─── Stage 1: Extract layered JAR ────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS builder
WORKDIR /build
ARG JAR_FILE=starter-server/target/starter-server-*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ─── Stage 2: Runtime image ──────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy layers in dependency-stable order for maximum cache reuse
COPY --from=builder /build/dependencies/ ./
COPY --from=builder /build/spring-boot-loader/ ./
COPY --from=builder /build/snapshot-dependencies/ ./
COPY --from=builder /build/application/ ./

EXPOSE 8080

# No secrets or env-specific values baked in — configure via environment variables:
#   STARTER_DEFAULTS_GROUP_ID        Default groupId pre-filled in the UI
#   STARTER_DEFAULTS_MAVEN_REGISTRY  Custom Maven registry URL for generated projects
#   STARTER_DEFAULTS_OPERATON_VERSION  Pin Operaton version for generated projects
#   STARTER_CORS_ALLOWED_ORIGINS     Comma-separated list of allowed CORS origins

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
