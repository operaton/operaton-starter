package org.operaton.dev.starter.templates.engine;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.operaton.dev.starter.templates.model.BuildSystem;
import org.operaton.dev.starter.templates.model.DependencyUpdater;
import org.operaton.dev.starter.templates.model.ProjectConfig;
import org.operaton.dev.starter.templates.model.ProjectType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Core generation engine for Operaton project archives.
 *
 * <p>Takes a {@link ProjectConfig} and returns a ZIP byte array.
 * All generation logic lives here — no per-channel duplication.
 *
 * <p>JTE templates are precompiled at build time by the {@code jte-maven-plugin}.
 * At runtime, templates are loaded from precompiled classes — zero file-system access.
 */
public class GenerationEngine {

    private final TemplateEngine jte;

    /**
     * Creates a GenerationEngine using precompiled JTE templates from classpath.
     * This is the production constructor — no file-system access required.
     */
    public GenerationEngine() {
        this.jte = TemplateEngine.createPrecompiled(ContentType.Plain);
    }

    /**
     * Creates a GenerationEngine using JTE templates from the specified directory.
     * Used for development and testing when templates may not yet be precompiled.
     */
    public GenerationEngine(Path templateRoot) {
        CodeResolver resolver = new DirectoryCodeResolver(templateRoot);
        this.jte = TemplateEngine.create(resolver, ContentType.Plain);
    }

    /**
     * Generates a project archive ZIP for the given configuration.
     *
     * @param config fully specified project configuration
     * @return ZIP archive as a byte array
     */
    public byte[] generate(ProjectConfig config) {
        var baos = new ByteArrayOutputStream();
        try (var zos = new ZipOutputStream(baos)) {
            ProjectConfig effectiveConfig = applyUseCaseDefaults(config);
            String useCaseId = effectiveConfig.useCaseId();
            if (useCaseId != null && !useCaseId.isBlank()) {
                generateUseCaseExample(useCaseId, effectiveConfig, zos);
            } else if (effectiveConfig.projectType() == ProjectType.PROCESS_APPLICATION) {
                generateProcessApplication(effectiveConfig, zos);
            } else if (effectiveConfig.projectType() == ProjectType.DMN_PROJECT) {
                generateDmnProject(effectiveConfig, zos);
            } else {
                generateProcessArchive(effectiveConfig, zos);
            }
            generateCommonExtras(effectiveConfig, zos);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate project archive", e);
        }
        return baos.toByteArray();
    }

    private ProjectConfig applyUseCaseDefaults(ProjectConfig config) {
        String useCaseId = config.useCaseId();
        if (useCaseId == null || useCaseId.isBlank()) {
            return config;
        }

        return switch (useCaseId) {
            case "uc-01-leave-request" ->
                    config.withUseCaseDefaults(ProjectType.PROCESS_APPLICATION, BuildSystem.MAVEN, false);
            case "uc-02-loan-application",
                 "uc-03-incident-management",
                 "uc-04-order-fulfillment" ->
                    config.withUseCaseDefaults(ProjectType.PROCESS_APPLICATION, BuildSystem.MAVEN, true);
            default -> throw new IllegalArgumentException("Unknown use case: " + useCaseId);
        };
    }

    private void generateUseCaseExample(String useCaseId, ProjectConfig config,
                                         ZipOutputStream zos) throws IOException {
        switch (useCaseId) {
            case "uc-01-leave-request" -> generateUC01LeaveRequest(config, zos);
            case "uc-02-loan-application" -> generateUC02LoanApplication(config, zos);
            case "uc-03-incident-management" -> generateUC03IncidentManagement(config, zos);
            case "uc-04-order-fulfillment" -> generateUC04OrderFulfillment(config, zos);
            default -> throw new IllegalArgumentException("Unknown use case: " + useCaseId);
        }
    }

    private void generateUC01LeaveRequest(ProjectConfig config, ZipOutputStream zos) throws IOException {
        String templateDir = "use-cases/uc-01-leave-request/";
        String pkgPath = config.packagePath();

        addTemplateEntry(zos, "pom.xml", templateDir + "maven/pom.xml.jte", config);
        addMavenWrapper(zos);

        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/Application.java",
                templateDir + "Application.java.jte", config);

        addTemplateEntry(zos,
                "src/main/resources/application.properties",
                templateDir + "application.properties.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/leave-request.bpmn",
                templateDir + "leave-request.bpmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/data.sql",
                templateDir + "data.sql.jte", config);

        addTemplateEntry(zos,
                "src/test/java/" + pkgPath + "/LeaveRequestIT.java",
                templateDir + "LeaveRequestIT.java.jte", config);

        addTemplateEntry(zos, "README.md", templateDir + "README.md.jte", config);
    }

    private void generateUC02LoanApplication(ProjectConfig config, ZipOutputStream zos) throws IOException {
        String templateDir = "use-cases/uc-02-loan-application/";
        String pkgPath = config.packagePath();

        addTemplateEntry(zos, "pom.xml", templateDir + "maven/pom.xml.jte", config);
        addMavenWrapper(zos);

        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/Application.java",
                templateDir + "Application.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/CreditScoreDelegate.java",
                templateDir + "CreditScoreDelegate.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/NotificationDelegate.java",
                templateDir + "NotificationDelegate.java.jte", config);

        addTemplateEntry(zos,
                "src/main/resources/application.properties",
                templateDir + "application.properties.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/loan-application.bpmn",
                templateDir + "loan-application.bpmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/dmn/risk-assessment.dmn",
                templateDir + "risk-assessment.dmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/data.sql",
                templateDir + "data.sql.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/credit-score-stub.json",
                templateDir + "wiremock/mappings/credit-score-stub.json.jte", config);

        addTemplateEntry(zos,
                "src/test/java/" + pkgPath + "/LoanApplicationIT.java",
                templateDir + "LoanApplicationIT.java.jte", config);

        addTemplateEntry(zos, "docker-compose.yml", templateDir + "docker-compose.yml.jte", config);
        addTemplateEntry(zos, "README.md", templateDir + "README.md.jte", config);
    }

    private void generateUC03IncidentManagement(ProjectConfig config, ZipOutputStream zos) throws IOException {
        String templateDir = "use-cases/uc-03-incident-management/";
        String pkgPath = config.packagePath();

        addTemplateEntry(zos, "pom.xml", templateDir + "maven/pom.xml.jte", config);
        addMavenWrapper(zos);

        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/Application.java",
                templateDir + "Application.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/CloseTicketDelegate.java",
                templateDir + "CloseTicketDelegate.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/PostMortemDelegate.java",
                templateDir + "PostMortemDelegate.java.jte", config);

        addTemplateEntry(zos,
                "src/main/resources/application.properties",
                templateDir + "application.properties.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/application-test.properties",
                templateDir + "application-test.properties.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/incident-management.bpmn",
                templateDir + "incident-management.bpmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/data.sql",
                templateDir + "data.sql.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/close-ticket-stub.json",
                templateDir + "wiremock/mappings/close-ticket-stub.json.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/post-mortem-stub.json",
                templateDir + "wiremock/mappings/post-mortem-stub.json.jte", config);

        addTemplateEntry(zos,
                "src/test/java/" + pkgPath + "/IncidentManagementIT.java",
                templateDir + "IncidentManagementIT.java.jte", config);

        addTemplateEntry(zos, "docker-compose.yml", templateDir + "docker-compose.yml.jte", config);
        addTemplateEntry(zos, "README.md", templateDir + "README.md.jte", config);
    }

    private void generateUC04OrderFulfillment(ProjectConfig config, ZipOutputStream zos) throws IOException {
        String templateDir = "use-cases/uc-04-order-fulfillment/";
        String pkgPath = config.packagePath();

        addTemplateEntry(zos, "pom.xml", templateDir + "maven/pom.xml.jte", config);
        addMavenWrapper(zos);

        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/Application.java",
                templateDir + "Application.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/InventoryDelegate.java",
                templateDir + "InventoryDelegate.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/PaymentDelegate.java",
                templateDir + "PaymentDelegate.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/NotifyCustomerDelegate.java",
                templateDir + "NotifyCustomerDelegate.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/NotifyBackorderDelegate.java",
                templateDir + "NotifyBackorderDelegate.java.jte", config);

        addTemplateEntry(zos,
                "src/main/resources/application.properties",
                templateDir + "application.properties.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/order-fulfillment.bpmn",
                templateDir + "order-fulfillment.bpmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/data.sql",
                templateDir + "data.sql.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/inventory-in-stock.json",
                templateDir + "wiremock/mappings/inventory-in-stock.json.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/inventory-out-of-stock.json",
                templateDir + "wiremock/mappings/inventory-out-of-stock.json.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/payment-success.json",
                templateDir + "wiremock/mappings/payment-success.json.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/notify-customer.json",
                templateDir + "wiremock/mappings/notify-customer.json.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/wiremock/mappings/notify-backorder.json",
                templateDir + "wiremock/mappings/notify-backorder.json.jte", config);

        addTemplateEntry(zos,
                "src/test/java/" + pkgPath + "/OrderFulfillmentIT.java",
                templateDir + "OrderFulfillmentIT.java.jte", config);

        addTemplateEntry(zos, "docker-compose.yml", templateDir + "docker-compose.yml.jte", config);
        addTemplateEntry(zos, "README.md", templateDir + "README.md.jte", config);
    }

    private void generateProcessApplication(ProjectConfig config, ZipOutputStream zos) throws IOException {
        // Build files
        switch (config.buildSystem()) {
            case MAVEN -> {
                addTemplateEntry(zos, "pom.xml",
                        "process-application/maven/pom.xml.jte", config);
                addMavenWrapper(zos);
            }
            case GRADLE_GROOVY -> {
                addTemplateEntry(zos, "build.gradle",
                        "process-application/gradle-groovy/build.gradle.jte", config);
                addTemplateEntry(zos, "settings.gradle",
                        "process-application/gradle-groovy/settings.gradle.jte", config);
                addGradleWrapper(zos);
            }
            case GRADLE_KOTLIN -> {
                addTemplateEntry(zos, "build.gradle.kts",
                        "process-application/gradle-kotlin/build.gradle.kts.jte", config);
                addTemplateEntry(zos, "settings.gradle.kts",
                        "process-application/gradle-kotlin/settings.gradle.kts.jte", config);
                addGradleWrapper(zos);
            }
        }

        String pkgPath = config.packagePath();

        // Java sources
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/Application.java",
                "process-application/Application.java.jte", config);
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/delegate/SkeletonDelegate.java",
                "process-application/delegate/SkeletonDelegate.java.jte", config);

        // Resources
        addTemplateEntry(zos,
                "src/main/resources/" + config.artifactId() + ".bpmn",
                "process-application/process.bpmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/application.properties",
                "process-application/application.properties.jte", config);

        // Test
        addTemplateEntry(zos,
                "src/test/java/" + pkgPath + "/ProcessIT.java",
                "process-application/ProcessIT.java.jte", config);
    }

    private void generateDmnProject(ProjectConfig config, ZipOutputStream zos) throws IOException {
        // Build files
        switch (config.buildSystem()) {
            case MAVEN -> {
                addTemplateEntry(zos, "pom.xml",
                        "dmn-project/maven/pom.xml.jte", config);
            }
            case GRADLE_GROOVY -> {
                addTemplateEntry(zos, "build.gradle",
                        "dmn-project/gradle-groovy/build.gradle.jte", config);
                addTemplateEntry(zos, "settings.gradle",
                        "dmn-project/gradle-groovy/settings.gradle.jte", config);
                addGradleWrapper(zos);
            }
            case GRADLE_KOTLIN -> {
                addTemplateEntry(zos, "build.gradle.kts",
                        "dmn-project/gradle-kotlin/build.gradle.kts.jte", config);
                addTemplateEntry(zos, "settings.gradle.kts",
                        "dmn-project/gradle-kotlin/settings.gradle.kts.jte", config);
                addGradleWrapper(zos);
            }
        }

        String pkgPath = config.packagePath();

        // Java sources
        addTemplateEntry(zos,
                "src/main/java/" + pkgPath + "/Application.java",
                "dmn-project/Application.java.jte", config);

        // Resources
        addTemplateEntry(zos,
                "src/main/resources/skeleton-decision.dmn",
                "dmn-project/skeleton-decision.dmn.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/application.properties",
                "dmn-project/application.properties.jte", config);

        // Test
        addTemplateEntry(zos,
                "src/test/java/" + pkgPath + "/DecisionIT.java",
                "dmn-project/DecisionIT.java.jte", config);
    }

    private void generateProcessArchive(ProjectConfig config, ZipOutputStream zos) throws IOException {
        // Build files
        switch (config.buildSystem()) {
            case MAVEN -> {
                addTemplateEntry(zos, "pom.xml",
                        "process-archive/maven/pom.xml.jte", config);
                addMavenWrapper(zos);
            }
            case GRADLE_GROOVY -> {
                addTemplateEntry(zos, "build.gradle",
                        "process-archive/gradle-groovy/build.gradle.jte", config);
                addTemplateEntry(zos, "settings.gradle",
                        "process-archive/gradle-groovy/settings.gradle.jte", config);
                addGradleWrapper(zos);
            }
            case GRADLE_KOTLIN -> {
                addTemplateEntry(zos, "build.gradle.kts",
                        "process-archive/gradle-kotlin/build.gradle.kts.jte", config);
                addTemplateEntry(zos, "settings.gradle.kts",
                        "process-archive/gradle-kotlin/settings.gradle.kts.jte", config);
                addGradleWrapper(zos);
            }
        }

        // Resources
        addTemplateEntry(zos,
                "src/main/resources/META-INF/processes.xml",
                "process-archive/processes.xml.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/" + config.artifactId() + ".bpmn",
                "process-archive/process.bpmn.jte", config);
    }

    private void generateCommonExtras(ProjectConfig config, ZipOutputStream zos) throws IOException {
        boolean useCaseGeneration = config.useCaseId() != null && !config.useCaseId().isBlank();

        // Use case examples include their own README; skip for those
        if (!useCaseGeneration) {
            addTemplateEntry(zos, "README.md", "common/README.md.jte", config);
        }

        // Dependency updater — mutually exclusive
        if (config.dependencyUpdater() == DependencyUpdater.RENOVATE) {
            addTemplateEntry(zos, "renovate.json", "common/renovate.json.jte", config);
        } else {
            addTemplateEntry(zos, ".github/dependabot.yml", "common/dependabot.yml.jte", config);
        }

        // Optional GitHub Actions CI
        if (config.githubActions()) {
            addTemplateEntry(zos, ".github/workflows/ci.yml", "common/ci.yml.jte", config);
        }

        // Use case examples own their docker-compose setup when needed.
        if (!useCaseGeneration && config.dockerCompose()) {
            addTemplateEntry(zos, "docker-compose.yml", "common/docker-compose.yml.jte", config);
            addTemplateEntry(zos, "Dockerfile", "common/dockerfile.jte", config);
        }
    }

    private void addTemplateEntry(ZipOutputStream zos, String entryPath, String template,
                                   ProjectConfig config) throws IOException {
        String content = renderTemplate(template, config);
        addEntry(zos, entryPath, content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private String renderTemplate(String templateName, ProjectConfig config) {
        StringOutput output = new StringOutput();
        jte.render(templateName, config, output);
        return output.toString();
    }

    private void addEntry(ZipOutputStream zos, String path, byte[] content) throws IOException {
        zos.putNextEntry(new ZipEntry(path));
        zos.write(content);
        zos.closeEntry();
    }

    private void addGradleWrapper(ZipOutputStream zos) throws IOException {
        addClasspathResource(zos, "gradlew", "gradle-wrapper/gradlew");
        addClasspathResource(zos, "gradlew.bat", "gradle-wrapper/gradlew.bat");
        addClasspathResource(zos, "gradle/wrapper/gradle-wrapper.jar",
                "gradle-wrapper/gradle-wrapper.jar");

        // gradle-wrapper.properties is always generated with the pinned version constant
        String wrapperProps =
                "distributionBase=GRADLE_USER_HOME\n" +
                "distributionPath=wrapper/dists\n" +
                "distributionUrl=https\\://services.gradle.org/distributions/gradle-" +
                org.operaton.dev.starter.templates.model.VersionConstants.GRADLE_VERSION + "-bin.zip\n" +
                "zipStoreBase=GRADLE_USER_HOME\n" +
                "zipStorePath=wrapper/dists\n";
        addEntry(zos, "gradle/wrapper/gradle-wrapper.properties",
                wrapperProps.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private void addMavenWrapper(ZipOutputStream zos) throws IOException {
        addClasspathResource(zos, "mvnw", "maven-wrapper/mvnw");
        addClasspathResource(zos, "mvnw.cmd", "maven-wrapper/mvnw.cmd");
        addClasspathResource(zos, ".mvn/wrapper/maven-wrapper.properties",
                "maven-wrapper/.mvn/wrapper/maven-wrapper.properties");
    }

    private void addClasspathResource(ZipOutputStream zos, String entryPath,
                                       String resourcePath) throws IOException {
        try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                addEntry(zos, entryPath, is.readAllBytes());
            } else {
                addEntry(zos, entryPath, new byte[0]);
            }
        }
    }
}
