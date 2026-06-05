@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
# H2 in-memory datasource — no Docker needed
# Activate: ./mvnw spring-boot:run --spring.profiles.active=h2
spring.datasource.url=jdbc:h2:mem:${config.artifactId()};DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.platform=h2
operaton.bpm.database.type=h2
