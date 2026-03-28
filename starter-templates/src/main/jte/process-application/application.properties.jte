@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
spring.application.name=${config.projectName()}
server.port=8080

# H2 in-memory datasource — replace with a persistent DB for production
spring.datasource.url=jdbc:h2:mem:operaton;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Operaton configuration
operaton.bpm.auto-deployment-enabled=true
