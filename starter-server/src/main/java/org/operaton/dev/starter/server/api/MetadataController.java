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

        // Include examples from registry (loaded from configured sources)
        var snapshot = exampleRegistry.snapshot();
        metadata.setExamples(snapshot.allExamples());

        return metadata;
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
