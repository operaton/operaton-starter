package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ExampleManifestParser}.
 */
class ExampleManifestParserTest {

    private final ExampleManifestParser parser = new ExampleManifestParser();

    @Test
    void testParseValidManifest() throws ManifestRejected {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: my-example
                    title: My Example
                    shortDescription: A test example
                    path: examples/basic
                    tags:
                      - beginner
                """;

        ParsedManifest manifest = parser.parse(
                yaml.getBytes(StandardCharsets.UTF_8),
                "owner/repo",
                "abc123"
        );

        assertEquals("operaton-starter/v1", manifest.apiVersion());
        assertEquals("owner/repo", manifest.sourceRepo());
        assertEquals("abc123", manifest.sourceRepoSha());
        assertEquals(1, manifest.examples().size());

        ParsedManifest.Example example = manifest.examples().getFirst();
        assertEquals("my-example", example.id());
        assertEquals("My Example", example.title());
        assertEquals("A test example", example.shortDescription());
        assertEquals("examples/basic", example.path());
        assertEquals(1, example.tags().size());
        assertEquals("beginner", example.tags().getFirst().label());
    }

    @Test
    void testParseManifestWithMultipleExamples() throws ManifestRejected {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: example-1
                    title: Example 1
                    shortDescription: First example
                    path: ex1
                  - id: example-2
                    title: Example 2
                    shortDescription: Second example
                    path: ex2
                    tags:
                      - advanced
                """;

        ParsedManifest manifest = parser.parse(
                yaml.getBytes(StandardCharsets.UTF_8),
                "user/examples",
                "def456"
        );

        assertEquals(2, manifest.examples().size());
        assertEquals("Example 1", manifest.examples().get(0).title());
        assertEquals("Example 2", manifest.examples().get(1).title());
    }

    @Test
    void testParseManifestWithIgnoredUnknownFields() throws ManifestRejected {
        String yaml = """
                apiVersion: operaton-starter/v1
                unknownField: should be ignored
                examples:
                  - id: example
                    title: Example
                    shortDescription: Test
                    path: test
                    unknownExampleField: ignored
                """;

        ParsedManifest manifest = parser.parse(
                yaml.getBytes(StandardCharsets.UTF_8),
                "owner/repo",
                "sha123"
        );

        assertEquals(1, manifest.examples().size());
        // No exception thrown; unknown fields are silently ignored
    }

    @Test
    void testParseManifestTooLarge() {
        byte[] largeContent = new byte[256 * 1024 + 1];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = 'a';
        }

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(largeContent, "owner/repo", "sha")
        );
        assertEquals("manifest-too-large", ex.getReason());
    }

    @Test
    void testParseMissingApiVersion() {
        String yaml = """
                examples:
                  - name: Example
                    path: test
                """;

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals("api-version", ex.getReason());
    }

    @Test
    void testParseBadApiVersion() {
        String yaml = """
                apiVersion: operaton-starter/v2
                examples: []
                """;

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals("api-version", ex.getReason());
    }

    @Test
    void testParsePathWithParentTraversal() {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: bad-example
                    title: Bad Example
                    shortDescription: test
                    path: ../../etc/passwd
                """;

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals("path-unsafe", ex.getReason());
    }

    @Test
    void testParsePathWithAbsolutePath() {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: bad-example
                    title: Bad Example
                    shortDescription: test
                    path: /etc/passwd
                """;

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals("path-unsafe", ex.getReason());
    }

    @Test
    void testParsePathWithSpaces() {
        // Paths with spaces are valid
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: example
                    title: Example
                    shortDescription: test
                    path: test file with spaces
                """;

        ParsedManifest manifest = assertDoesNotThrow(() ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals(1, manifest.examples().size());
        assertEquals("test file with spaces", manifest.examples().getFirst().path());
    }

    @Test
    void testParseMalformedYaml() {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - name: Example
                    path: test
                  invalid yaml content { [ ]
                """;

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals("malformed-yaml", ex.getReason());
    }

    @Test
    void testParseEmptyPath() {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: example
                    title: Example
                    shortDescription: test
                    path: ''
                """;

        ManifestRejected ex = assertThrows(ManifestRejected.class, () ->
                parser.parse(
                        yaml.getBytes(StandardCharsets.UTF_8),
                        "owner/repo",
                        "sha"
                )
        );
        assertEquals("path-unsafe", ex.getReason());
    }

    @Test
    void testParseSkipsExamplesMissingRequiredFields() throws ManifestRejected {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - path: minimal
                """;

        ParsedManifest manifest = parser.parse(
                yaml.getBytes(StandardCharsets.UTF_8),
                "owner/repo",
                "sha"
        );

        // Example missing id/title/shortDescription is skipped, not added
        assertEquals(0, manifest.examples().size());
    }

    @Test
    void testParseExample_pathIsOptional_defaultsToNull() throws ManifestRejected {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: my-example
                    title: My Example
                    shortDescription: A test example
                """;

        ParsedManifest manifest = parser.parse(
                yaml.getBytes(StandardCharsets.UTF_8), "owner/repo", "abc123");

        ParsedManifest.Example example = manifest.examples().getFirst();
        assertNull(example.path());
    }

    @Test
    void testParseExample_emptyPathIsRejected() {
        String yaml = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: my-example
                    title: My Example
                    shortDescription: A test example
                    path: ""
                """;

        assertThrows(ManifestRejected.class, () ->
                parser.parse(yaml.getBytes(StandardCharsets.UTF_8), "owner/repo", "abc123"));
    }
}
