package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;

class ExampleZipCacheTest {

    @TempDir
    Path tempCacheDir;

    private ExampleZipCache cache;
    private ZipBuilder mockZipBuilder;
    private org.operaton.dev.starter.server.config.StarterProperties mockProps;

    @BeforeEach
    void setUp() {
        mockZipBuilder = mock(ZipBuilder.class);
        mockProps = mock(org.operaton.dev.starter.server.config.StarterProperties.class);

        // Mock the properties chain
        var examples = mock(org.operaton.dev.starter.server.config.StarterProperties.Examples.class);
        var cacheConfig = mock(org.operaton.dev.starter.server.config.StarterProperties.Examples.Cache.class);

        when(cacheConfig.dir()).thenReturn(tempCacheDir);
        when(cacheConfig.maxSizeMb()).thenReturn(512L);
        when(examples.cache()).thenReturn(cacheConfig);
        when(examples.maxDownloadSizeMb()).thenReturn(50L);
        when(mockProps.examples()).thenReturn(examples);

        cache = new ExampleZipCache(mockZipBuilder, mockProps);
    }

    @Test
    void getOrBuild_returns_cached_file_without_calling_builder() throws Exception {
        // Arrange
        String owner = "owner";
        String repo = "repo";
        String sha = "abc123def456";
        String exampleId = "example-1";
        String examplePath = "examples/example-1";

        Path cachePath = tempCacheDir.resolve(owner).resolve(repo).resolve(sha)
                .resolve(exampleId + ".zip");

        // Create a dummy zip file in the cache
        File dummyZip = createDummyZip(cachePath);

        // Act
        File result = cache.getOrBuild(owner, repo, sha, exampleId, examplePath);

        // Assert
        assertEquals(dummyZip.getAbsolutePath(), result.getAbsolutePath());
        assertTrue(result.exists());
        // Verify ZipBuilder.build was never called
        verify(mockZipBuilder, never()).build(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyLong(), any(Path.class));
    }

    @Test
    void getOrBuild_builds_and_caches_on_miss() throws Exception {
        // Arrange
        String owner = "owner";
        String repo = "repo";
        String sha = "abc123def456";
        String exampleId = "example-1";
        String examplePath = "examples/example-1";

        Path cachePath = tempCacheDir.resolve(owner).resolve(repo).resolve(sha)
                .resolve(exampleId + ".zip");

        // Verify the file doesn't exist yet
        assertTrue(!Files.exists(cachePath));

        // Mock ZipBuilder to create a dummy file when called
        when(mockZipBuilder.build(owner, repo, sha, exampleId, examplePath, 50L, cachePath))
                .thenAnswer(invocation -> createDummyZip(cachePath));

        // Act
        File result = cache.getOrBuild(owner, repo, sha, exampleId, examplePath);

        // Assert
        assertTrue(result.exists());
        // Verify ZipBuilder.build was called exactly once
        verify(mockZipBuilder, times(1)).build(owner, repo, sha, exampleId, examplePath, 50L, cachePath);
    }

    @Test
    void getOrBuild_raises_PathSafetyException_on_builder_error() throws Exception {
        // Arrange
        String owner = "owner";
        String repo = "repo";
        String sha = "abc123def456";
        String exampleId = "example-1";
        String examplePath = "examples/example-1";

        Path cachePath = tempCacheDir.resolve(owner).resolve(repo).resolve(sha)
                .resolve(exampleId + ".zip");

        // Mock ZipBuilder to throw PathSafetyException
        when(mockZipBuilder.build(owner, repo, sha, exampleId, examplePath, 50L, cachePath))
                .thenThrow(new PathSafetyException("Invalid path", ".."));

        // Act & Assert
        try {
            cache.getOrBuild(owner, repo, sha, exampleId, examplePath);
            throw new AssertionError("Expected PathSafetyException");
        } catch (PathSafetyException e) {
            assertEquals("..", e.getEntryPath());
        }
    }

    @Test
    void getOrBuild_raises_SizeLimitExceededException_on_builder_error() throws Exception {
        // Arrange
        String owner = "owner";
        String repo = "repo";
        String sha = "abc123def456";
        String exampleId = "example-1";
        String examplePath = "examples/example-1";

        Path cachePath = tempCacheDir.resolve(owner).resolve(repo).resolve(sha)
                .resolve(exampleId + ".zip");

        // Mock ZipBuilder to throw SizeLimitExceededException
        when(mockZipBuilder.build(owner, repo, sha, exampleId, examplePath, 50L, cachePath))
                .thenThrow(new SizeLimitExceededException("Size exceeded", 50L, 100L, exampleId));

        // Act & Assert
        try {
            cache.getOrBuild(owner, repo, sha, exampleId, examplePath);
            throw new AssertionError("Expected SizeLimitExceededException");
        } catch (SizeLimitExceededException e) {
            assertEquals(100L, e.getActualSizeMb());
        }
    }

    @Test
    void pruneCache_does_not_crash_on_empty_cache() throws Exception {
        // Act
        cache.pruneCache();
        // Assert: no exceptions thrown
    }

    /**
     * Helper to create a dummy ZIP file for testing.
     */
    private File createDummyZip(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(path))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("test content".getBytes());
            zos.closeEntry();
        }
        return path.toFile();
    }
}
