package org.operaton.dev.starter.server.api;

import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for {@code GET /api/v1/metadata}.
 * Returns all supported configuration options and template manifests.
 * Served from in-memory — no database query.
 */
@RestController
public class MetadataController {

    private final StarterProperties properties;

    public MetadataController(StarterProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/api/v1/metadata", produces = MediaType.APPLICATION_JSON_VALUE)
    public Metadata getMetadata() {
        var metadata = new Metadata(
                List.of(buildProcessApplication(), buildProcessArchive()),
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
        return metadata;
    }

    private static ProjectTypeInfo buildProcessApplication() {
        return new ProjectTypeInfo(
                "PROCESS_APPLICATION",
                "Process Application",
                "A Spring Boot application with an embedded Operaton engine. Includes a skeleton BPMN process, a JavaDelegate implementation stub, and an end-to-end integration test that passes on first run.",
                List.of("Spring Boot", "Embedded Engine", "Java Delegate", "JUnit 5"),
                "Ideal for Camunda 7 migrators and developers starting fresh",
                processApplicationManifest()
        );
    }

    private static ProjectTypeInfo buildProcessArchive() {
        return new ProjectTypeInfo(
                "PROCESS_ARCHIVE",
                "Process Archive",
                "A deployable WAR or JAR archive for a shared Operaton engine (Tomcat or Standalone). No embedded engine — process definitions are deployed to an existing engine via processes.xml.",
                List.of("Shared Engine", "WAR/JAR Deploy", "processes.xml", "Tomcat", "Standalone"),
                "Ideal for teams running a shared Operaton engine",
                processArchiveManifest()
        );
    }

    private static List<TemplateManifestEntry> processApplicationManifest() {
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
        entries.add(entry("README.md", null, "common/README.md.jte"));
        entries.add(entry("renovate.json", "dependencyUpdater == 'RENOVATE'", "common/renovate.json.jte"));
        entries.add(entry(".github/dependabot.yml", "dependencyUpdater == 'DEPENDABOT'", "common/dependabot.yml.jte"));
        entries.add(entry(".github/workflows/ci.yml", "githubActions == true", "common/ci.yml.jte"));
        entries.add(entry("docker-compose.yml", "dockerCompose == true", "common/docker-compose.yml.jte"));
        return entries;
    }

    private static List<TemplateManifestEntry> processArchiveManifest() {
        List<TemplateManifestEntry> entries = new ArrayList<>();
        entries.add(entry("pom.xml", "buildSystem == 'MAVEN'", "process-archive/maven/pom.xml.jte"));
        entries.add(entry("build.gradle", "buildSystem == 'GRADLE_GROOVY'", "process-application/gradle-groovy/build.gradle.jte"));
        entries.add(entry("settings.gradle", "buildSystem == 'GRADLE_GROOVY'", "process-application/gradle-groovy/settings.gradle.jte"));
        entries.add(entry("build.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "process-application/gradle-kotlin/build.gradle.kts.jte"));
        entries.add(entry("settings.gradle.kts", "buildSystem == 'GRADLE_KOTLIN'", "process-application/gradle-kotlin/settings.gradle.kts.jte"));
        entries.add(entry("src/main/resources/META-INF/processes.xml", null, "process-archive/processes.xml.jte"));
        entries.add(entry("src/main/resources/{artifactId}.bpmn", null, "process-archive/process.bpmn.jte"));
        entries.add(entry("README.md", null, "common/README.md.jte"));
        entries.add(entry("renovate.json", "dependencyUpdater == 'RENOVATE'", "common/renovate.json.jte"));
        entries.add(entry(".github/dependabot.yml", "dependencyUpdater == 'DEPENDABOT'", "common/dependabot.yml.jte"));
        entries.add(entry(".github/workflows/ci.yml", "githubActions == true", "common/ci.yml.jte"));
        entries.add(entry("docker-compose.yml", "dockerCompose == true", "common/docker-compose.yml.jte"));
        return entries;
    }

    private static GlobalOptions buildGlobalOptions() {
        var javaVersions = new GlobalOptionsJavaVersions();
        javaVersions.setOptions(List.of(17, 21, 25));
        javaVersions.setDefault(17);
        var globalOptions = new GlobalOptions();
        globalOptions.setJavaVersions(javaVersions);
        return globalOptions;
    }

    private static TemplateManifestEntry entry(String path, String condition, String templateId) {
        var e = new TemplateManifestEntry(path, templateId);
        if (condition != null) {
            e.condition(condition);
        }
        return e;
    }
}
