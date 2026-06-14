package org.operaton.dev.starter.server.api;

import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.examples.ExampleRegistry;
import org.operaton.dev.starter.server.model.*;
import org.operaton.dev.starter.server.model.Tag;
import org.operaton.dev.starter.server.model.TagCategory;
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
 *
 * <p>Includes examples loaded from configured sources via {@link ExampleRegistry}.
 * The examples field is additive and does not affect existing fields.
 */
@RestController
public class MetadataController {

    private static final Logger log = LoggerFactory.getLogger(MetadataController.class);

    private final StarterProperties properties;
    private final ResourceLoader resourceLoader;
    private final ExampleRegistry exampleRegistry;

    public MetadataController(StarterProperties properties, ResourceLoader resourceLoader, ExampleRegistry exampleRegistry) {
        this.properties = properties;
        this.resourceLoader = resourceLoader;
        this.exampleRegistry = exampleRegistry;
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

        // Include examples from registry (loaded from configured sources)
        var snapshot = exampleRegistry.snapshot();
        metadata.setExamples(snapshot.allExamples());

        return metadata;
    }

    private List<UseCaseExample> buildUseCaseExamples() {
        return List.of(
            new UseCaseExample()
                .useCaseId("uc-01-leave-request")
                .title("Leave Request")
                .description("Employee leave approval with automatic validation, persisted balances, manager review, and HR finalization.")
                .tags(List.of(
                    tag("multi-role", TagCategory.BPMN_CONCEPT),
                    tag("human-tasks", TagCategory.BPMN_CONCEPT),
                    tag("validation", TagCategory.BPMN_CONCEPT),
                    tag("PostgreSQL", TagCategory.PLATFORM),
                    tag("Docker Compose", TagCategory.TECHNOLOGY)
                ))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("leave-request")
                .defaultProjectName("Leave Request")
                .dockerCompose(true)
                .processSummary("An employee submits a leave request, which is validated against their remaining balance. A manager then approves or rejects it. If approved, HR finalizes the booking and an email confirmation is sent; if rejected, the employee receives a rejection notification.")
                .bpmnConcepts(List.of("User Task", "Exclusive Gateway", "Service Task", "Boundary Event"))
                .integrations(List.of("PostgreSQL (leave balance persistence)", "Jakarta Mail (email notifications)", "H2 (embedded test database)"))
                .learnings(List.of(
                    "How to model multi-role approval workflows with sequential human tasks",
                    "How to persist and query state (leave balances) alongside a running process",
                    "How to trigger email notifications from a service task",
                    "How to write an integration test that drives a process end-to-end"
                ))
                .templateManifest(uc01LeaveRequestManifest()),
            new UseCaseExample()
                .useCaseId("uc-02-loan-application")
                .title("Loan Application")
                .description("Credit scoring via DMN decision table with WireMock-stubbed external API — covers low/medium/high risk routing.")
                .tags(List.of(
                    tag("DMN", TagCategory.STANDARD),
                    tag("Service Tasks", TagCategory.BPMN_CONCEPT),
                    tag("WireMock", TagCategory.TECHNOLOGY),
                    tag("Docker Compose", TagCategory.TECHNOLOGY)
                ))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("loan-application")
                .defaultProjectName("Loan Application")
                .dockerCompose(true)
                .processSummary("A loan application is submitted and routed through a credit scoring step that calls an external API. The score feeds into a DMN decision table that classifies the applicant as low, medium, or high risk, and the process branches accordingly — approving, queuing for manual review, or rejecting.")
                .bpmnConcepts(List.of("Service Task", "DMN Business Rule Task", "Exclusive Gateway"))
                .integrations(List.of("WireMock (stubbed credit score API)", "DMN decision table (risk-assessment.dmn)", "H2 (embedded test database)"))
                .learnings(List.of(
                    "How to call an external REST API from a service task and pass the result downstream",
                    "How to connect a BPMN process to a DMN decision table using a Business Rule Task",
                    "How to model conditional routing based on a decision table output",
                    "How to stub external dependencies with WireMock for repeatable integration tests"
                ))
                .templateManifest(uc02LoanApplicationManifest()),
            new UseCaseExample()
                .useCaseId("uc-03-incident-management")
                .title("Incident Management")
                .description("SLA timer boundary event that escalates unresolved tickets from first-line to second-line engineering.")
                .tags(List.of(
                    tag("Timer", TagCategory.BPMN_CONCEPT),
                    tag("Boundary Event", TagCategory.BPMN_CONCEPT),
                    tag("Escalation", TagCategory.BPMN_CONCEPT),
                    tag("WireMock", TagCategory.TECHNOLOGY),
                    tag("Docker Compose", TagCategory.TECHNOLOGY)
                ))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("incident-management")
                .defaultProjectName("Incident Management")
                .dockerCompose(true)
                .processSummary("An incident ticket is opened and assigned to first-line engineering. If not resolved within the SLA window, a timer boundary event triggers automatic escalation to second-line. Once resolved, the ticket is closed and a post-mortem record is created via an external API call.")
                .bpmnConcepts(List.of("User Task", "Timer Boundary Event", "Escalation", "Service Task"))
                .integrations(List.of("WireMock (stubbed close-ticket and post-mortem APIs)", "H2 (embedded test database)"))
                .learnings(List.of(
                    "How to implement SLA enforcement with a timer boundary event",
                    "How to model escalation paths that activate automatically without user intervention",
                    "How to stub and verify multiple external service calls in a single integration test"
                ))
                .templateManifest(uc03IncidentManagementManifest()),
            new UseCaseExample()
                .useCaseId("uc-04-order-fulfillment")
                .title("Order Fulfillment")
                .description("Multi-service-task orchestration with inventory, payment, and notification REST calls stubbed via WireMock.")
                .tags(List.of(
                    tag("Service Tasks", TagCategory.BPMN_CONCEPT),
                    tag("REST", TagCategory.TECHNOLOGY),
                    tag("WireMock", TagCategory.TECHNOLOGY),
                    tag("Conditional Routing", TagCategory.BPMN_CONCEPT),
                    tag("Docker Compose", TagCategory.TECHNOLOGY)
                ))
                .projectType("PROCESS_APPLICATION")
                .buildSystem("MAVEN")
                .defaultArtifactId("order-fulfillment")
                .defaultProjectName("Order Fulfillment")
                .dockerCompose(true)
                .processSummary("An order is received and the process orchestrates three external systems: it checks inventory, processes payment, and sends a customer notification. If inventory is insufficient, the order is routed to a backorder path instead of proceeding to payment.")
                .bpmnConcepts(List.of("Service Task", "Exclusive Gateway", "Conditional Sequence Flow"))
                .integrations(List.of("WireMock (inventory, payment, notification, and backorder APIs)", "H2 (embedded test database)"))
                .learnings(List.of(
                    "How to orchestrate multiple external REST calls sequentially from a single process",
                    "How to model conditional routing based on the result of a service task",
                    "How to manage a WireMock stub set with multiple mappings for different scenarios",
                    "How to test both the happy path and an alternative path in the same test suite"
                ))
                .templateManifest(uc04OrderFulfillmentManifest())
        );
    }

    private List<TemplateManifestEntry> uc01LeaveRequestManifest() {
        String d = "use-cases/uc-01-leave-request/";
        List<TemplateManifestEntry> e = new ArrayList<>();
        e.add(entry("pom.xml", null, d + "maven/pom.xml.jte"));
        e.add(entry("src/main/java/{package}/Application.java", null, d + "Application.java.jte"));
        e.add(entry("src/main/java/{package}/VacationBalanceService.java", null, d + "VacationBalanceService.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/LeaveRequestValidationDelegate.java", null, d + "delegate/LeaveRequestValidationDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/FinalizeLeaveApprovalDelegate.java", null, d + "delegate/FinalizeLeaveApprovalDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/EscalationReminderDelegate.java", null, d + "delegate/EscalationReminderDelegate.java.jte"));
        e.add(entry("src/main/java/{package}/delegate/LeaveRejectionEmailDelegate.java", null, d + "delegate/LeaveRejectionEmailDelegate.java.jte"));
        e.add(entry("src/main/resources/leave-request.bpmn", null, d + "leave-request.bpmn.jte"));
        e.add(entry("src/main/resources/application.properties", null, d + "application.properties.jte"));
        e.add(entry("src/main/resources/application-h2.properties", null, d + "application-h2.properties.jte"));
        e.add(entry("src/main/resources/schema.sql", null, d + "schema.sql.jte"));
        e.add(entry("src/main/java/{package}/DataInitializer.java", null, d + "DataInitializer.java.jte"));
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
        e.add(entry("src/main/java/{package}/DataInitializer.java", null, d + "DataInitializer.java.jte"));
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
        e.add(entry("src/main/java/{package}/DataInitializer.java", null, d + "DataInitializer.java.jte"));
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
        e.add(entry("src/main/java/{package}/DataInitializer.java", null, d + "DataInitializer.java.jte"));
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
        var info = new ProjectTypeInfo(
                "PROCESS_APPLICATION",
                "Process Application",
                "A Spring Boot application with an embedded Operaton engine. Includes a skeleton BPMN process, a JavaDelegate implementation stub, and an end-to-end integration test that passes on first run.",
                List.of(
                        tag("Spring Boot", TagCategory.PLATFORM),
                        tag("Embedded Engine", TagCategory.PLATFORM),
                        tag("Java Delegate", TagCategory.BPMN_CONCEPT),
                        tag("JUnit 5", TagCategory.TECHNOLOGY)
                ),
                "Ideal for Camunda 7 migrators and developers starting fresh",
                processApplicationManifest()
        );
        info.setDetailedDescription(
                "A Process Application runs the Operaton engine embedded inside your Spring Boot application — " +
                "there is no separate engine server to install or manage. The engine starts with your app and lives in the same JVM, " +
                "giving you the tightest possible integration: process variables are plain Java objects, service tasks are Spring beans, " +
                "and you can debug the entire flow end-to-end in a single process.\n\n" +
                "Choose this when you are building a self-contained service that orchestrates its own business logic, " +
                "or when you are migrating from Camunda 7 and want to keep the same embedded-engine model you already know. " +
                "It is not the right fit if your organization runs a shared Operaton engine managed centrally — " +
                "for that deployment model, use a Process Archive instead."
        );
        return info;
    }

    private ProjectTypeInfo buildDmnProject() {
        var info = new ProjectTypeInfo(
                "DMN_PROJECT",
                "DMN Decision Project",
                "A Spring Boot application focused on DMN decision table evaluation. Includes a skeleton decision table (hit policy FIRST), an integration test that evaluates it, and full build tooling — ready to run on first checkout.",
                List.of(
                        tag("DMN", TagCategory.STANDARD),
                        tag("Decision Table", TagCategory.STANDARD),
                        tag("Spring Boot", TagCategory.PLATFORM),
                        tag("JUnit 5", TagCategory.TECHNOLOGY)
                ),
                "Ideal for teams building rules engines, approval logic, or pricing models",
                dmnProjectManifest()
        );
        info.setDetailedDescription(
                "A DMN Decision Project applies the Operaton decision engine to evaluate DMN decision tables — without any BPMN process. " +
                "The generated project is a Spring Boot application with a skeleton decision table using hit policy FIRST, " +
                "wired to an integration test that sends inputs and asserts the correct output.\n\n" +
                "This is the right starting point when your domain is rules-heavy: approval criteria, pricing tiers, risk scoring, " +
                "eligibility checks — anything that can be expressed as a table of conditions and conclusions. " +
                "You do not need a BPMN process to use DMN; the Operaton engine evaluates decision tables independently. " +
                "If you also need process orchestration around your decisions, combine this approach with a Process Application."
        );
        return info;
    }

    private ProjectTypeInfo buildProcessArchive() {
        var info = new ProjectTypeInfo(
                "PROCESS_ARCHIVE",
                "Process Archive",
                "A deployable WAR or JAR archive for a shared Operaton engine (Tomcat or Standalone). No embedded engine — process definitions are deployed to an existing engine via processes.xml.",
                List.of(
                        tag("Shared Engine", TagCategory.PLATFORM),
                        tag("WAR/JAR Deploy", TagCategory.PLATFORM),
                        tag("processes.xml", TagCategory.PLATFORM),
                        tag("Tomcat", TagCategory.PLATFORM),
                        tag("Standalone", TagCategory.PLATFORM)
                ),
                "Ideal for teams running a shared Operaton engine",
                processArchiveManifest()
        );
        info.setDetailedDescription(
                "A Process Archive is a deployable artifact — WAR or JAR — that contains process definitions and resources " +
                "to be registered on an already-running Operaton engine. The engine itself lives elsewhere; " +
                "your archive brings the BPMN files and a processes.xml descriptor that tells the engine what to deploy.\n\n" +
                "This is the right choice when your organization runs a shared Operaton engine on Tomcat or as a standalone server, " +
                "and multiple teams deploy their process definitions to it — keeping engine management centralized. " +
                "This template does not produce a runnable Spring Boot application. " +
                "If you need a self-contained application that runs its own engine, choose Process Application instead."
        );
        return info;
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

    private static Tag tag(String label, TagCategory category) {
        return new Tag().label(label).category(category);
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
