@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# Stage 1: Build and verify with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn verify

# Stage 2: Runtime image
FROM eclipse-temurin:${config.javaVersion()}
WORKDIR /app
COPY --from=builder /app/target/${config.artifactId()}-*.jar ${config.artifactId()}.jar
EXPOSE ${config.serverPort()}
ENTRYPOINT ["java", "-jar", "${config.artifactId()}.jar"]
