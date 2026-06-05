@import org.operaton.dev.starter.templates.model.ProjectConfig
@param ProjectConfig config
spring.application.name=${config.projectName()}
server.port=${config.serverPort()}

# H2 in-memory database — no external database needed for this example
spring.datasource.url=jdbc:h2:mem:operaton;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Load data.sql to seed users, groups, and memberships
spring.sql.init.mode=always

# Operaton configuration
operaton.bpm.auto-deployment-enabled=true
