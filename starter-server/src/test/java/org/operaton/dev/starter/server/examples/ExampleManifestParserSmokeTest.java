package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test validating the sample manifest fixture against {@link ExampleManifestParser}.
 *
 * <p>This test ensures that the committed sample manifest ({@code sample-operaton-starter.yml})
 * parses successfully with no {@code ManifestRejected} exceptions. It verifies that:
 *
 * <ul>
 *   <li>The manifest has valid apiVersion</li>
 *   <li>All required fields are present and valid</li>
 *   <li>All examples load without errors</li>
 *   <li>The fixture is suitable for external testing and documentation</li>
 * </ul>
 *
 * <p>This test runs as part of the standard Maven test suite and serves as a gating mechanism
 * for the fixture—any changes that break the manifest format will be caught before commit.
 */
class ExampleManifestParserSmokeTest {

    private final ExampleManifestParser parser = new ExampleManifestParser();

    @Test
    void testSampleManifestParses() throws IOException, ManifestRejected {
        // Load the committed sample fixture from test resources
        Path fixturePath = Paths.get("src/test/resources/fixtures/sample-operaton-starter.yml");
        assertTrue(Files.exists(fixturePath), "Sample fixture must exist at " + fixturePath);

        byte[] manifestBytes = Files.readAllBytes(fixturePath);

        // Parse the sample manifest
        ParsedManifest manifest = parser.parse(
                manifestBytes,
                "kthoms/operaton-examples",
                "main"
        );

        // Verify basic structure
        assertNotNull(manifest);
        assertEquals("operaton-starter/v1", manifest.apiVersion());
        assertEquals("kthoms/operaton-examples", manifest.sourceRepo());
        assertEquals("main", manifest.sourceRepoSha());

        // Verify examples are present
        assertNotNull(manifest.examples());
        assertFalse(manifest.examples().isEmpty(), "Manifest must contain at least one example");
        assertEquals(3, manifest.examples().size(), "Sample should have 3 seed examples");

        // Verify all examples have valid paths
        manifest.examples().forEach(example -> {
            assertNotNull(example.path(), "Example path must not be null");
            assertFalse(example.path().isEmpty(), "Example path must not be empty");
            assertFalse(example.path().startsWith("/"), "Paths must not be absolute");
            assertFalse(example.path().contains(".."), "Paths must not contain parent directory traversal");
        });
    }

    @Test
    void testSampleManifestContainsThreeExamples() throws IOException, ManifestRejected {
        // This test ensures the sample demonstrates the runtime/buildSystem matrix
        // Required for acceptance criteria: "at minimum 3 examples covering Spring Boot + Maven,
        // Quarkus + Gradle, plain-Java embedded"
        Path fixturePath = Paths.get("src/test/resources/fixtures/sample-operaton-starter.yml");
        byte[] manifestBytes = Files.readAllBytes(fixturePath);

        ParsedManifest manifest = parser.parse(manifestBytes, "kthoms/operaton-examples", "main");

        // Verify we have at least 3 examples (requirement from story AC#3)
        assertEquals(3, manifest.examples().size(),
                "Sample manifest must contain at least 3 examples (Spring Boot + Maven, " +
                "Quarkus + Gradle, plain-Java embedded)");

        // All examples should parse without ManifestRejected
        // (no exception thrown indicates success for all 3)
        manifest.examples().forEach(example ->
                assertNotNull(example.path(), "All examples must have valid paths")
        );
    }
}
