package org.operaton.dev.starter.server.api;

import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for {@code GET /api/v1/metadata}.
 * Returns all supported configuration options and template manifests.
 * Served from in-memory — no database query.
 */
@RestController
public class MetadataController {

    private static final Logger log = LoggerFactory.getLogger(MetadataController.class);

    private final StarterProperties properties;
    private final ResourceLoader resourceLoader;

    public MetadataController(StarterProperties properties, ResourceLoader resourceLoader) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @GetMapping(value = "/api/v1/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public Metadata getMetadata() {
        var metadata = new Metadata(
                List.of(buildProcessApplication(), buildProcessArchive(), buildDmnProject()),
                List.of(
                        new BuildSystemInfo("MAVEN", "Maven"),
                        new BuildSystemInfo("GRADLE_GROOVY", "Gradle (Groovy DSL)"),
                        new BuildSystemInfo("GRADLE_KOTLIN", "Gradle (Kotlin DSL)")
                ),
                buildGlobalOptions()
        );
        String defaultGroupId = properties.defaults().groupId();
        if (defaultGroupId != null && !defaultGroupId.isBlank()) {
            metadata.setDefaultGroupId(defaultGroupId);
        }
        metadata.setUseCaseExamples(buildUseCaseExamples());
        return metadata;
    }

    private List<UseCaseExample> buildUseCaseExamples() {
        return List.of(
            new UseCaseExample()
                .useCaseId("uc-01-leave-request")
                .title("Leave Request")
                .description("A manager approves employee leave — two roles, one process, zero infrastructure overhead.")
                .tags(List.of("multi-role", "human-tasks"))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("leave-request-example")
                .defaultProjectName("Leave Request Example")
                .dockerCompose(false)
                .templateManifest(uc01LeaveRequestManifest()),
            new UseCaseExample()
                .useCaseId("uc-02-loan-application")
                .title("Loan Application")
                .description("Credit scoring via DMN decision table with WireMock-stubbed external API — covers low/medium/high risk routing.")
                .tags(List.of("DMN", "service-tasks", "wiremock", "docker-compose"))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("loan-application-example")
                .defaultProjectName("Loan Application Example")
                .dockerCompose(true)
                .templateManifest(uc02LoanApplicationManifest()),
            new UseCaseExample()
                .useCaseId("uc-03-incident-management")
                .title("Incident Management")
                .description("SLA timer boundary event that escalates unresolved tickets from first-line to second-line engineering.")
                .tags(List.of("timer", "boundary-event", "escalation", "wiremock", "docker-compose"))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("incident-management-example")
                .defaultProjectName("Incident Management Example")
                .dockerCompose(true)
                .templateManifest(uc03IncidentManagementManifest()),
            new UseCaseExample()
                .useCaseId("uc-04-order-fulfillment")
                .title("Order Fulfillment")
                .description("Multi-service-task orchestration with inventory, payment, and notification REST calls stubbed via WireMock.")
                .tags(List.of("service-tasks", "rest", "wiremock", "conditional-routing", "docker-compose"))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("order-fulfillment-example")
                .defaultProjectName("Order Fulfillment Example")
                .dockerCompose(true)
                .templateManifest(uc04OrderFulfillmentManifest())
        );
    }

    private List<TemplateManifestEntry> uc01LeaveRequestManifest() {
        String d = "use-cases/uc-01-leave-request/";
        List<TemplateManifestEntry> e = new ArrayList<>();
        e.add(entry("pom.xml", null, d + "maven/pom.xml.jte"));
        e.add(entry("src/main/java/{package}/Application.java", null, d + "Application.java.jte"));
        e.add(entry("src/main/resources/leave-request.bpmn", null, d + "leave-request.bpmn.jte"));
        e.add(entry("src/main/resources/application.properties", null, d + "application.properties.jte"));
        e.add(entry("src/main/resources/application-h2.properties", null, d + "application-h2.properties.jte"));
        e.add(entry("src/main/resources/data.sql", null, d + "data.sql.jte"));
        e.add(entry("src/test/java/{package}/LeaveRequestIT.java", null, d + "LeaveRequestIT.java.jte"));
        e.add(entry("README.md", null, d + "README.md.jte"));
        return e;
    }

    private List<TemplateManifestEntry> uc02LoanApplicationManifest() {
        String d = "use-cases/uc-02-loan-application/";
        List<TemplateManifestEntry> e = new ArrayList<>();
        e.add(entry("pom.xml", null, d + "maven/pom.xml.jte"));
        e.add(entry("src/main/java/{package}/Application.java", null, d + "Application.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/CreditScoreDelegate.java", null, d + "CreditScoreDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/NotificationDelegate.java", null, d + "NotificationDelegate.java.jte"));
        e.add(entry("src/main/resources/loan-application.bpmn", null, d + "loan-application.bpmn.jte"));
        e.add(entry("src/main/resources/dmn/risk-assessment.dmn", null, d + "risk-assessment.dmn.jte"));
        e.add(entry("src/main/resources/application.properties", null, d + "application.properties.jte"));
        e.add(entry("src/main/resources/application-h2.properties", null, d + "application-h2.properties.jte"));
        e.add(entry("src/main/resources/data.sql", null, d + "data.sql.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/credit-score-stub.json", null, d + "wiremock/mappings/credit-score-stub.json.jte"));
        e.add(entry("src/test/java/{package}/LoanApplicationIT.java", null, d + "LoanApplicationIT.java.jte"));
        e.add(entry("docker-compose.yml", null, d + "docker-compose.yml.jte"));
        e.add(entry("README.md", null, d + "README.md.jte"));
        return e;
    }

    private List<TemplateManifestEntry> uc03IncidentManagementManifest() {
        String d = "use-cases/uc-03-incident-management/";
        List<TemplateManifestEntry> e = new ArrayList<>();
        e.add(entry("pom.xml", null, d + "maven/pom.xml.jte"));
        e.add(entry("src/main/java/{package}/Application.java", null, d + "Application.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/CloseTicketDelegate.java", null, d + "CloseTicketDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/PostMortemDelegate.java", null, d + "PostMortemDelegate.java.jte"));
        e.add(entry("src/main/resources/incident-management.bpmn", null, d + "incident-management.bpmn.jte"));
        e.add(entry("src/main/resources/application.properties", null, d + "application.properties.jte"));
        e.add(entry("src/main/resources/application-h2.properties", null, d + "application-h2.properties.jte"));
        e.add(entry("src/main/resources/application-test.properties", null, d + "application-test.properties.jte"));
        e.add(entry("src/main/resources/data.sql", null, d + "data.sql.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/close-ticket-stub.json", null, d + "wiremock/mappings/close-ticket-stub.json.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/post-mortem-stub.json", null, d + "wiremock/mappings/post-mortem-stub.json.jte"));
        e.add(entry("src/test/java/{package}/IncidentManagementIT.java", null, d + "IncidentManagementIT.java.jte"));
        e.add(entry("docker-compose.yml", null, d + "docker-compose.yml.jte"));
        e.add(entry("README.md", null, d + "README.md.jte"));
        return e;
    }

    private List<TemplateManifestEntry> uc04OrderFulfillmentManifest() {
        String d = "use-cases/uc-04-order-fulfillment/";
        List<TemplateManifestEntry> e = new ArrayList<>();
        e.add(entry("pom.xml", null, d + "maven/pom.xml.jte"));
        e.add(entry("src/main/java/{package}/Application.java", null, d + "Application.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/InventoryDelegate.java", null, d + "InventoryDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/PaymentDelegate.java", null, d + "PaymentDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/NotifyCustomerDelegate.java", null, d + "NotifyCustomerDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/NotifyBackorderDelegate.java", null, d + "NotifyBackorderDelegate.java.jte"));
        e.add(entry("src/main/resources/order-fulfillment.bpmn", null, d + "order-fulfillment.bpmn.jte"));
        e.add(entry("src/main/resources/application.properties", null, d + "application.properties.jte"));
        e.add(entry("src/main/resources/application-h2.properties", null, d + "application-h2.properties.jte"));
        e.add(entry("src/main/resources/data.sql", null, d + "data.sql.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/inventory-in-stock.json", null, d + "wiremock/mappings/inventory-in-stock.json.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/inventory-out-of-stock.json", null, d + "wiremock/mappings/inventory-out-of-stock.json.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/payment-success.json", null, d + "wiremock/mappings/payment-success.json.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/notify-customer.json", null, d + "wiremock/mappings/notify-customer.json.jte"));
        e.add(entry("src/main/resources/wiremock/mappings/notify-backorder.json", null, d + "wiremock/mappings/notify-backorder.json.jte"));
        e.add(entry("src/test/java/{package}/OrderFulfillmentIT.java", null, d + "OrderFulfillmentIT.java.jte"));
        e.add(entry("docker-compose.yml", null, d + "docker-compose.yml.jte"));
        e.add(entry("README.md", null, d + "README.md.jte"));
        return e;
    }

    private ProjectTypeInfo buildProcessApplication() {
        return new ProjectTypeInfo(
                "PROCESS_APPLICATION",
                "Process Application",
                "A Spring Boot application with an embedded Operaton engine. Includes a skeleton BPMN process, a JavaDelegate implementation stub, and an end-to-end integration test that passes on first run.",
                List.of("Spring Boot", "Embedded Engine", "Java Delegate", "JUnit 5"),
                "Ideal for Camunda 7 migrators and developers starting fresh",
                processApplicationManifest()
        );
    }

    private ProjectTypeInfo buildDmnProject() {
        return new ProjectTypeInfo(
                "DMN_PROJECT",
                "DMN Decision Project",
                "A Spring Boot application focused on DMN decision table evaluation. Includes a skeleton decision table (hit policy FIRST), an integration test that evaluates it, and full build tooling — ready to run on first checkout.",
                List.of("DMN", "Decision Table", "Spring Boot", "JUnit 5"),
                "Ideal for teams building rules engines, approval logic, or pricing models",
                dmnProjectManifest()
        );
    }

    private ProjectTypeInfo buildProcessArchive() {
        return new ProjectTypeInfo(
                "PROCESS_ARCHIVE",
                "Process Archive",
                "A deployable WAR or JAR archive for a shared Operaton engine (Tomcat or Standalone). No embedded engine — process definitions are deployed to an existing engine via processes.xml.",
                List.of("Shared Engine", "WAR/JAR Deploy", "processes.xml", "Tomcat", "Standalone"),
                "Ideal for teams running a shared Operaton engine",
                processArchiveManifest()
        );
    }

    private List<TemplateManifestEntry> dmnProjectManifest() {
        List<TemplateManifestEntry> entries = new ArrayList<>();
        entries.add(entry("pom.xml", "buildSystem == 'MAVEN'", "dmn-project/maven/pom.xml.jte"));
        entries.add(entry("build.gradle", "buildSystem == 'GRADLE_GROOVY'", "dmn-project/gradle-groovy/build.gradle.jte"));
        entries.add(entry("settings.gradle", "buildSystem == 'GRADLE_GROOVY'", "dmn-project/gradle-groovy/settings.gradle.jte"));
        entries.add(entry("build.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "dmn-project/gradle-kotlin/build.gradle.kts.jte"));
        entries.add(entry("settings.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "dmn-project/gradle-kotlin/settings.gradle.kts.jte"));
        entries.add(entry("src/main/java/{package}/Application.java", null, "dmn-project/Application.java.jte"));
        entries.add(entry("src/main/resources/skeleton-decision.dmn", null, "dmn-project/skeleton-decision.dmn.jte"));
        entries.add(entry("src/main/resources/application.properties", null, "dmn-project/application.properties.jte"));
        entries.add(entry("src/test/java/{package}/DecisionIT.java", null, "dmn-project/DecisionIT.java.jte"));
        entries.add(entry("README.md", null, "common/README.md.jte"));
        entries.add(entry("renovate.json", "dependencyUpdater == 'RENOVATE'", "common/renovate.json.jte"));
        entries.add(entry(".github/dependabot.yml", "dependencyUpdater == 'DEPENDABOT'", "common/dependabot.yml.jte"));
        entries.add(entry(".github/workflows/ci.yml", "githubActions == true", "common/ci.yml.jte"));
        entries.add(entry("docker-compose.yml", "dockerCompose == true", "common/docker-compose.yml.jte"));
        return entries;
    }

    private List<TemplateManifestEntry> processApplicationManifest() {
        List<TemplateManifestEntry> entries = new ArrayList<>();
        entries.add(entry("pom.xml", "buildSystem == 'MAVEN'", "process-application/maven/pom.xml.jte"));
        entries.add(entry("build.gradle", "buildSystem == 'GRADLE_GROOVY'", "process-application/gradle-groovy/build.gradle.jte"));
        entries.add(entry("settings.gradle", "buildSystem == 'GRADLE_GROOVY'", "process-application/gradle-groovy/settings.gradle.jte"));
        entries.add(entry("build.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "process-application/gradle-kotlin/build.gradle.kts.jte"));
        entries.add(entry("settings.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "process-application/gradle-kotlin/settings.gradle.kts.jte"));
        entries.add(entry("src/main/java/{package}/Application.java", null, "process-application/Application.java.jte"));
        entries.add(entry("src/main/java/{package}/delegate/SkeletonDelegate.java", null, "process-application/delegate/SkeletonDelegate.java.jte"));
        entries.add(entry("src/main/resources/{artifactId}.bpmn", null, "process-application/process.bpmn.jte"));
        entries.add(entry("src/main/resources/application.properties", null, "process-application/application.properties.jte"));
        entries.add(entry("src/test/java/{package}/ProcessIT.java", null, "process-application/ProcessIT.java.jte"));
        entries.add(entry("src/main/resources/banner.txt", null, "process-application/banner.txt"));
        entries.add(entry("README.md", null, "common/README.md.jte"));
        entries.add(entry("renovate.json", "dependencyUpdater == 'RENOVATE'", "common/renovate.json.jte"));
        entries.add(entry(".github/dependabot.yml", "dependencyUpdater == 'DEPENDABOT'", "common/dependabot.yml.jte"));
        entries.add(entry(".github/workflows/ci.yml", "githubActions == true", "common/ci.yml.jte"));
        entries.add(entry("docker-compose.yml", "dockerCompose == true", "common/docker-compose.yml.jte"));
        return entries;
    }

    private List<TemplateManifestEntry> processArchiveManifest() {
        List<TemplateManifestEntry> entries = new ArrayList<>();
        entries.add(entry("pom.xml", "buildSystem == 'MAVEN'", "process-archive/maven/pom.xml.jte"));
        entries.add(entry("build.gradle", "buildSystem == 'GRADLE_GROOVY'", "process-archive/gradle-groovy/build.gradle.jte"));
        entries.add(entry("settings.gradle", "buildSystem == 'GRADLE_GROOVY'", "process-archive/gradle-groovy/settings.gradle.jte"));
        entries.add(entry("build.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "process-archive/gradle-kotlin/build.gradle.kts.jte"));
        entries.add(entry("settings.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "process-archive/gradle-kotlin/settings.gradle.kts.jte"));
        entries.add(entry("src/main/resources/META-INF/processes.xml", null, "process-archive/processes.xml.jte"));
        entries.add(entry("src/main/resources/{artifactId}.bpmn", null, "process-archive/process.bpmn.jte"));
        entries.add(entry("README.md", null, "common/README.md.jte"));
        entries.add(entry("renovate.json", "dependencyUpdater == 'RENOVATE'", "common/renovate.json.jte"));
        entries.add(entry(".github/dependabot.yml", "dependencyUpdater == 'DEPENDABOT'", "common/dependabot.yml.jte"));
        entries.add(entry(".github/workflows/ci.yml", "githubActions == true", "common/ci.yml.jte"));
        entries.add(entry("docker-compose.yml", "dockerCompose == true", "common/docker-compose.yml.jte"));
        return entries;
    }

    private static GlobalOptions buildGlobalOptions() {  // intentionally static — no instance state needed
        var javaVersions = new GlobalOptionsJavaVersions();
        javaVersions.setOptions(List.of(17, 21, 25));
        javaVersions.setDefault(17);
        var globalOptions = new GlobalOptions();
        globalOptions.setJavaVersions(javaVersions);
        return globalOptions;
    }

    private TemplateManifestEntry entry(String path, String condition, String templateId) {
        var e = new TemplateManifestEntry(path, templateId);
        if (condition != null) {
            e.condition(condition);
        }
        String preview = loadPreviewContent(templateId);
        if (preview != null) {
            e.previewContent(preview);
        }
        return e;
    }

    private String loadPreviewContent(String templateId) {
        try {
            Resource resource = resourceLoader.getResource("classpath:jte-sources/" + templateId);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Template preview not found for {}: {}", templateId, e.getMessage());
            return null;
        }
    }
}
