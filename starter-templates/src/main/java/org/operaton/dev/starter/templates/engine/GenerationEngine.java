package org.operaton.dev.starter.templates.engine;

import gg.jte.CodeResolver;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.TemplateOutput;
import gg.jte.output.StringOutput;
import gg.jte.resolve.DirectoryCodeResolver;
import org.operaton.dev.starter.templates.model.BuildSystem;
import org.operaton.dev.starter.templates.model.DependencyUpdater;
import org.operaton.dev.starter.templates.model.DeploymentTarget;
import org.operaton.dev.starter.templates.model.ProjectConfig;
import org.operaton.dev.starter.templates.model.ProjectType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Map;
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
            if (config.projectType() == ProjectType.PROCESS_APPLICATION) {
                generateProcessApplication(config, zos);
            } else {
                generateProcessArchive(config, zos);
            }
            generateCommonExtras(config, zos);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to generate project archive", e);
        }
        return baos.toByteArray();
    }

    private void generateProcessApplication(ProjectConfig config, ZipOutputStream zos) throws IOException {
        String buildSystem = config.buildSystem().name().toLowerCase().replace('_', '-');

        // Build files
        switch (config.buildSystem()) {
            case MAVEN -> {
                addTemplateEntry(zos, "pom.xml",
                        "process-application/maven/pom.xml.jte", config);
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

    private void generateProcessArchive(ProjectConfig config, ZipOutputStream zos) throws IOException {
        // Build files
        switch (config.buildSystem()) {
            case MAVEN -> {
                addTemplateEntry(zos, "pom.xml",
                        "process-archive/maven/pom.xml.jte", config);
            }
            case GRADLE_GROOVY -> {
                // Reuse process-application Groovy template structure
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

        // Resources
        addTemplateEntry(zos,
                "src/main/resources/META-INF/processes.xml",
                "process-archive/processes.xml.jte", config);
        addTemplateEntry(zos,
                "src/main/resources/" + config.artifactId() + ".bpmn",
                "process-archive/process.bpmn.jte", config);
    }

    private void generateCommonExtras(ProjectConfig config, ZipOutputStream zos) throws IOException {
        // README always present
        addTemplateEntry(zos, "README.md", "common/README.md.jte", config);

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

        // Optional Docker Compose
        if (config.dockerCompose()) {
            addTemplateEntry(zos, "docker-compose.yml", "common/docker-compose.yml.jte", config);
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
        // gradlew script
        addGradleWrapperResource(zos, "gradlew", "gradle-wrapper/gradlew");
        addGradleWrapperResource(zos, "gradlew.bat", "gradle-wrapper/gradlew.bat");
        addGradleWrapperResource(zos, "gradle/wrapper/gradle-wrapper.jar",
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

    private void addGradleWrapperResource(ZipOutputStream zos, String entryPath,
                                           String resourcePath) throws IOException {
        try (var is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is != null) {
                addEntry(zos, entryPath, is.readAllBytes());
            } else {
                // Placeholder — real Gradle wrapper jar is a binary resource bundled at build time
                addEntry(zos, entryPath,
                        ("# Gradle wrapper placeholder — see https://gradle.org/install/\n").getBytes());
            }
        }
    }
}
