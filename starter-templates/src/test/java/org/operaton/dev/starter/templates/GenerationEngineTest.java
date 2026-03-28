package org.operaton.dev.starter.templates;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;
import org.operaton.dev.starter.templates.engine.GenerationEngine;
import org.operaton.dev.starter.templates.model.*;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Story 2.2 + 2.3 + 2.4 + 2.5: Tests all ProjectType × BuildSystem combinations.
 *
 * <p>Uses DirectoryCodeResolver so tests pass without precompiling templates.
 */
class GenerationEngineTest {

    private static final Path JTE_ROOT = Path.of("src/main/jte");
    private final GenerationEngine engine = new GenerationEngine(JTE_ROOT);

    static Stream<Arguments> allCombinations() {
        List<Arguments> args = new ArrayList<>();
        for (var buildSystem : BuildSystem.values()) {
            // Process Application combinations
            args.add(Arguments.of(
                    ProjectType.PROCESS_APPLICATION, buildSystem, null
            ));
            // Process Archive combinations (need deploymentTarget)
            for (var deploymentTarget : DeploymentTarget.values()) {
                args.add(Arguments.of(
                        ProjectType.PROCESS_ARCHIVE, buildSystem, deploymentTarget
                ));
            }
        }
        return args.stream();
    }

    @ParameterizedTest(name = "{0} x {1} x {2}")
    @MethodSource("allCombinations")
    void generate_returns_valid_zip(ProjectType projectType, BuildSystem buildSystem,
                                    DeploymentTarget deploymentTarget) throws Exception {
        var configBuilder = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("my-process")
                .projectName("My Process")
                .projectType(projectType)
                .buildSystem(buildSystem);
        if (deploymentTarget != null) {
            configBuilder.deploymentTarget(deploymentTarget);
        }
        var config = configBuilder.build();

        byte[] zip = engine.generate(config);

        assertNotNull(zip);
        assertTrue(zip.length > 0, "ZIP should not be empty");

        // Verify valid ZIP format
        var entries = listZipEntries(zip);
        assertFalse(entries.isEmpty(), "ZIP should contain at least one file");
    }

    @ParameterizedTest(name = "ProcessApp {1} — identity propagation")
    @MethodSource("processApplicationCombinations")
    void process_application_identity_propagates(BuildSystem buildSystem) throws Exception {
        var config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("my-process")
                .projectName("My Process")
                .projectType(ProjectType.PROCESS_APPLICATION)
                .buildSystem(buildSystem)
                .build();

        byte[] zip = engine.generate(config);
        var entries = listZipEntries(zip);

        // Assert build file present (not pom.xml for Gradle)
        switch (buildSystem) {
            case MAVEN -> assertTrue(entries.contains("pom.xml"), "Should contain pom.xml");
            case GRADLE_GROOVY -> {
                assertTrue(entries.contains("build.gradle"), "Should contain build.gradle");
                assertFalse(entries.contains("pom.xml"), "Should NOT contain pom.xml");
            }
            case GRADLE_KOTLIN -> {
                assertTrue(entries.contains("build.gradle.kts"), "Should contain build.gradle.kts");
                assertFalse(entries.contains("pom.xml"), "Should NOT contain pom.xml");
            }
        }

        // Core Java sources
        String pkgPath = "com/example/my_process";
        assertTrue(entries.contains("src/main/java/" + pkgPath + "/Application.java"));
        assertTrue(entries.contains("src/main/java/" + pkgPath + "/delegate/SkeletonDelegate.java"));
        assertTrue(entries.contains("src/test/java/" + pkgPath + "/ProcessIT.java"));

        // BPMN with artifact ID
        assertTrue(entries.contains("src/main/resources/my-process.bpmn"),
                "Should contain my-process.bpmn, got: " + entries);

        // README always present
        assertTrue(entries.contains("README.md"));
    }

    static Stream<Arguments> processApplicationCombinations() {
        return Stream.of(BuildSystem.values()).map(Arguments::of);
    }

    @Test
    void process_archive_maven_tomcat_contains_processes_xml() throws Exception {
        var config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("my-archive")
                .projectName("My Archive")
                .projectType(ProjectType.PROCESS_ARCHIVE)
                .buildSystem(BuildSystem.MAVEN)
                .deploymentTarget(DeploymentTarget.TOMCAT)
                .build();

        byte[] zip = engine.generate(config);
        var entries = listZipEntries(zip);

        assertTrue(entries.contains("pom.xml"));
        assertTrue(entries.contains("src/main/resources/META-INF/processes.xml"));
        assertTrue(entries.contains("src/main/resources/my-archive.bpmn"));

        // Process Archive should NOT have embedded engine classes
        assertFalse(entries.stream().anyMatch(e -> e.contains("Application.java")));
        assertFalse(entries.stream().anyMatch(e -> e.contains("SkeletonDelegate.java")));
        assertFalse(entries.stream().anyMatch(e -> e.contains("ProcessIT.java")));
    }

    @Test
    void renovate_and_dependabot_are_mutually_exclusive() throws Exception {
        var renovateConfig = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-project")
                .projectName("Test Project")
                .dependencyUpdater(DependencyUpdater.RENOVATE)
                .build();
        var dependabotConfig = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-project")
                .projectName("Test Project")
                .dependencyUpdater(DependencyUpdater.DEPENDABOT)
                .build();

        var renovateEntries = listZipEntries(engine.generate(renovateConfig));
        var dependabotEntries = listZipEntries(engine.generate(dependabotConfig));

        assertTrue(renovateEntries.contains("renovate.json"));
        assertFalse(renovateEntries.contains(".github/dependabot.yml"));

        assertTrue(dependabotEntries.contains(".github/dependabot.yml"));
        assertFalse(dependabotEntries.contains("renovate.json"));
    }

    @Test
    void githubActions_true_includes_ci_yml() throws Exception {
        var config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-project")
                .projectName("Test Project")
                .githubActions(true)
                .build();
        var entries = listZipEntries(engine.generate(config));
        assertTrue(entries.contains(".github/workflows/ci.yml"));
    }

    @Test
    void githubActions_false_excludes_ci_yml() throws Exception {
        var config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-project")
                .projectName("Test Project")
                .githubActions(false)
                .build();
        var entries = listZipEntries(engine.generate(config));
        assertFalse(entries.stream().anyMatch(e -> e.contains(".github/workflows")));
    }

    @Test
    void dockerCompose_true_includes_docker_compose_yml() throws Exception {
        var config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-project")
                .projectName("Test Project")
                .dockerCompose(true)
                .build();
        var entries = listZipEntries(engine.generate(config));
        assertTrue(entries.contains("docker-compose.yml"));
    }

    @Test
    void dockerCompose_false_excludes_docker_compose_yml() throws Exception {
        var config = ProjectConfig.builder()
                .groupId("com.example")
                .artifactId("test-project")
                .projectName("Test Project")
                .dockerCompose(false)
                .build();
        var entries = listZipEntries(engine.generate(config));
        assertFalse(entries.contains("docker-compose.yml"));
    }

    private static List<String> listZipEntries(byte[] zip) throws Exception {
        var entries = new ArrayList<String>();
        try (var zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            var entry = zis.getNextEntry();
            while (entry != null) {
                entries.add(entry.getName());
                entry = zis.getNextEntry();
            }
        }
        return entries;
    }
}
