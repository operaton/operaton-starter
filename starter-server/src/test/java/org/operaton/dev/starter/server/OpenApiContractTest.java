package org.operaton.dev.starter.server;

import org.junit.jupiter.api.Test;
import org.operaton.dev.starter.server.model.Author;
import org.operaton.dev.starter.server.model.Example;
import org.operaton.dev.starter.server.model.Tag;
import org.operaton.dev.starter.server.model.TagCategory;

import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to verify the OpenAPI contract for Example and TagCategory enhancements.
 */
class OpenApiContractTest {

    @Test
    void tagCategory_includes_new_runtime_category() {
        assertDoesNotThrow(() -> TagCategory.RUNTIME);
        assertEquals("RUNTIME", TagCategory.RUNTIME.getValue());
    }

    @Test
    void tagCategory_includes_new_build_system_category() {
        assertDoesNotThrow(() -> TagCategory.BUILD_SYSTEM);
        assertEquals("BUILD_SYSTEM", TagCategory.BUILD_SYSTEM.getValue());
    }

    @Test
    void tagCategory_includes_new_complexity_category() {
        assertDoesNotThrow(() -> TagCategory.COMPLEXITY);
        assertEquals("COMPLEXITY", TagCategory.COMPLEXITY.getValue());
    }

    @Test
    void tagCategory_preserves_existing_categories() {
        assertEquals("BPMN_CONCEPT", TagCategory.BPMN_CONCEPT.getValue());
        assertEquals("TECHNOLOGY", TagCategory.TECHNOLOGY.getValue());
        assertEquals("PLATFORM", TagCategory.PLATFORM.getValue());
        assertEquals("STANDARD", TagCategory.STANDARD.getValue());
    }

    @Test
    void example_has_all_required_fields() {
        Example example = new Example();
        example.setId("test-example");
        example.setTitle("Test Example");
        example.setPath("examples/test");
        example.setShortDescription("A test example");

        assertEquals("test-example", example.getId());
        assertEquals("Test Example", example.getTitle());
        assertEquals("examples/test", example.getPath());
        assertEquals("A test example", example.getShortDescription());
    }

    @Test
    void example_has_all_optional_fields() {
        Example example = new Example();
        example.setIcon("📝");
        example.setLongDescription("Detailed description in markdown");
        example.setBuildSystem(Example.BuildSystemEnum.MAVEN);
        example.setRuntime(Example.RuntimeEnum.SPRING_BOOT);
        example.setOperatonVersion("1.0.0-beta-5");
        example.setJavaVersion("21");
        example.setComplexity(Example.ComplexityEnum.BEGINNER);
        example.setTags(Arrays.asList(
            new Tag().label("Approval").category(TagCategory.BPMN_CONCEPT)
        ));
        example.setIntegrations(Arrays.asList("rest", "dmn"));
        example.setBpmnConcepts(Arrays.asList("user-task", "exclusive-gateway"));
        example.setRequires("Java 21+");
        example.setAuthors(Arrays.asList(
            new Author().name("Test Author").url("https://github.com/test")
        ));
        example.setLicense("Apache-2.0");
        example.setDocumentationUrl("https://github.com/test/docs");
        example.setDemoVideoUrl("https://youtube.com/test");
        example.setScreenshots(Arrays.asList("docs/screenshot1.png", "docs/screenshot2.png"));
        example.setLastUpdated(LocalDate.of(2026, 6, 10));
        example.setSourceRepo("test/repo");
        example.setSourceRepoSha("abc123def456");
        example.setSourceRepoUrl("https://github.com/test/repo/tree/main/examples/test");

        assertEquals("📝", example.getIcon());
        assertEquals("Detailed description in markdown", example.getLongDescription());
        assertEquals(Example.BuildSystemEnum.MAVEN, example.getBuildSystem());
        assertEquals(Example.RuntimeEnum.SPRING_BOOT, example.getRuntime());
        assertEquals("1.0.0-beta-5", example.getOperatonVersion());
        assertEquals("21", example.getJavaVersion());
        assertEquals(Example.ComplexityEnum.BEGINNER, example.getComplexity());
        assertEquals(1, example.getTags().size());
        assertEquals(2, example.getIntegrations().size());
        assertEquals(2, example.getBpmnConcepts().size());
        assertEquals("Java 21+", example.getRequires());
        assertEquals(1, example.getAuthors().size());
        assertEquals("Apache-2.0", example.getLicense());
        assertEquals("https://github.com/test/docs", example.getDocumentationUrl());
        assertEquals("https://youtube.com/test", example.getDemoVideoUrl());
        assertEquals(2, example.getScreenshots().size());
        assertEquals(LocalDate.of(2026, 6, 10), example.getLastUpdated());
        assertEquals("test/repo", example.getSourceRepo());
        assertEquals("abc123def456", example.getSourceRepoSha());
        assertEquals("https://github.com/test/repo/tree/main/examples/test", example.getSourceRepoUrl());
    }

    @Test
    void example_buildSystem_enum_values() {
        assertEquals("maven", Example.BuildSystemEnum.MAVEN.getValue());
        assertEquals("gradle", Example.BuildSystemEnum.GRADLE.getValue());
    }

    @Test
    void example_runtime_enum_values() {
        assertEquals("spring-boot", Example.RuntimeEnum.SPRING_BOOT.getValue());
        assertEquals("quarkus", Example.RuntimeEnum.QUARKUS.getValue());
        assertEquals("plain-java", Example.RuntimeEnum.PLAIN_JAVA.getValue());
        assertEquals("other", Example.RuntimeEnum.OTHER.getValue());
    }

    @Test
    void example_complexity_enum_values() {
        assertEquals("beginner", Example.ComplexityEnum.BEGINNER.getValue());
        assertEquals("intermediate", Example.ComplexityEnum.INTERMEDIATE.getValue());
        assertEquals("advanced", Example.ComplexityEnum.ADVANCED.getValue());
    }

    @Test
    void author_has_required_fields() {
        Author author = new Author();
        author.setName("John Doe");
        author.setUrl("https://github.com/johndoe");

        assertEquals("John Doe", author.getName());
        assertEquals("https://github.com/johndoe", author.getUrl());
    }
}
