package org.operaton.dev.starter.server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.operaton.dev.starter.server.examples.api.ExampleDownloadController;
import org.operaton.dev.starter.server.examples.ExampleRegistry;
import org.operaton.dev.starter.server.examples.ExampleSnapshot;
import org.operaton.dev.starter.server.examples.ExampleZipCache;
import org.operaton.dev.starter.server.examples.PathSafetyException;
import org.operaton.dev.starter.server.examples.SizeLimitExceededException;
import org.operaton.dev.starter.server.model.Example;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ExampleDownloadControllerTest {

    private ExampleDownloadController controller;
    private ExampleRegistry mockRegistry;
    private ExampleZipCache mockZipCache;

    @BeforeEach
    void setUp() {
        mockRegistry = mock(ExampleRegistry.class);
        mockZipCache = mock(ExampleZipCache.class);
        controller = new ExampleDownloadController(mockRegistry, mockZipCache);

        // Default: empty snapshot
        ExampleSnapshot emptySnapshot = createEmptySnapshot();
        when(mockRegistry.snapshot()).thenReturn(emptySnapshot);
    }

    @Test
    void downloadExample_returns_404_for_unknown_example() {
        // Act
        ResponseEntity<?> response = controller.downloadExample("unknown-owner", "unknown-repo", "unknown-example");

        // Assert
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void downloadExample_returns_404_when_example_sha_is_missing() {
        // Arrange
        String owner = "test-owner";
        String repo = "test-repo";
        String exampleId = "test-example";
        String examplePath = "examples/test-example";

        Example example = mock(Example.class);
        when(example.getId()).thenReturn(exampleId);
        when(example.getPath()).thenReturn(examplePath);
        when(example.getSourceRepoSha()).thenReturn(null); // No SHA

        ExampleSnapshot snapshot = createSnapshotWithExample(owner, repo, example);
        when(mockRegistry.snapshot()).thenReturn(snapshot);

        // Act
        ResponseEntity<?> response = controller.downloadExample(owner, repo, exampleId);

        // Assert
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void downloadExample_returns_200_with_valid_example() throws Exception {
        // Arrange
        String owner = "test-owner";
        String repo = "test-repo";
        String exampleId = "test-example";
        String sha = "abc123def456";
        String examplePath = "examples/test-example";

        Example example = createMockExample(exampleId, sha, examplePath);
        ExampleSnapshot snapshot = createSnapshotWithExample(owner, repo, example);
        when(mockRegistry.snapshot()).thenReturn(snapshot);

        // Create a dummy ZIP file
        Path tmpZip = Files.createTempFile("test", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(tmpZip))) {
            ZipEntry entry = new ZipEntry("README.md");
            zos.putNextEntry(entry);
            zos.write("# Test Example".getBytes());
            zos.closeEntry();
        }

        when(mockZipCache.getOrBuild(owner, repo, sha, exampleId, examplePath))
                .thenReturn(tmpZip.toFile());

        // Act
        ResponseEntity<?> response = controller.downloadExample(owner, repo, exampleId);

        // Assert
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getHeaders().get("Content-Type"));
        assertNotNull(response.getHeaders().get("Content-Disposition"));
        assertNotNull(response.getHeaders().get("ETag"));
        assertNotNull(response.getHeaders().get("Last-Modified"));

        // Cleanup
        Files.delete(tmpZip);
    }

    @Test
    void downloadExample_returns_502_on_size_limit_exceeded() throws Exception {
        // Arrange
        String owner = "test-owner";
        String repo = "test-repo";
        String exampleId = "test-example";
        String sha = "abc123def456";
        String examplePath = "examples/test-example";

        Example example = createMockExample(exampleId, sha, examplePath);
        ExampleSnapshot snapshot = createSnapshotWithExample(owner, repo, example);
        when(mockRegistry.snapshot()).thenReturn(snapshot);

        when(mockZipCache.getOrBuild(owner, repo, sha, exampleId, examplePath))
                .thenThrow(new SizeLimitExceededException("Size exceeded", 50L, 100L, exampleId));

        // Act
        ResponseEntity<?> response = controller.downloadExample(owner, repo, exampleId);

        // Assert
        assertEquals(413, response.getStatusCode().value());
    }

    @Test
    void downloadExample_returns_502_on_path_safety_exception() throws Exception {
        // Arrange
        String owner = "test-owner";
        String repo = "test-repo";
        String exampleId = "test-example";
        String sha = "abc123def456";
        String examplePath = "examples/test-example";

        Example example = createMockExample(exampleId, sha, examplePath);
        ExampleSnapshot snapshot = createSnapshotWithExample(owner, repo, example);
        when(mockRegistry.snapshot()).thenReturn(snapshot);

        when(mockZipCache.getOrBuild(owner, repo, sha, exampleId, examplePath))
                .thenThrow(new PathSafetyException("Invalid path", ".."));

        // Act
        ResponseEntity<?> response = controller.downloadExample(owner, repo, exampleId);

        // Assert
        assertEquals(502, response.getStatusCode().value());
    }

    @Test
    void downloadExample_returns_502_on_io_exception() throws Exception {
        // Arrange
        String owner = "test-owner";
        String repo = "test-repo";
        String exampleId = "test-example";
        String sha = "abc123def456";
        String examplePath = "examples/test-example";

        Example example = createMockExample(exampleId, sha, examplePath);
        ExampleSnapshot snapshot = createSnapshotWithExample(owner, repo, example);
        when(mockRegistry.snapshot()).thenReturn(snapshot);

        when(mockZipCache.getOrBuild(owner, repo, sha, exampleId, examplePath))
                .thenThrow(new java.io.IOException("GitHub unreachable"));

        // Act
        ResponseEntity<?> response = controller.downloadExample(owner, repo, exampleId);

        // Assert
        assertEquals(502, response.getStatusCode().value());
    }

    // ============================================ Helper Methods ============================================

    /**
     * Creates an empty snapshot with no examples.
     */
    private ExampleSnapshot createEmptySnapshot() {
        return new ExampleSnapshot(new ArrayList<>());
    }

    /**
     * Creates a snapshot containing a single example.
     */
    private ExampleSnapshot createSnapshotWithExample(String owner, String repo, Example example) {
        List<ExampleSnapshot.SourceState> sources = new ArrayList<>();

        List<Example> examples = new ArrayList<>();
        examples.add(example);

        String sourceRepo = owner + "/" + repo;
        String sha = example.getSourceRepoSha();
        sources.add(new ExampleSnapshot.SourceState(sourceRepo, "success", examples, sha, "2026-06-15T00:00:00Z"));

        return new ExampleSnapshot(sources);
    }

    /**
     * Creates a mock Example with the given properties.
     */
    private Example createMockExample(String exampleId, String sha, String examplePath) {
        Example example = mock(Example.class);
        when(example.getId()).thenReturn(exampleId);
        when(example.getSourceRepoSha()).thenReturn(sha);
        when(example.getPath()).thenReturn(examplePath);
        return example;
    }
}
