package org.operaton.dev.starter.server.examples;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Builds ZIP files from GitHub tarball archives.
 *
 * <p>Architecture A6: Fetches https://codeload.github.com/{owner}/{repo}/tar.gz/{sha} as a stream,
 * walks tar entries, filters to those under the example's path:, validates each path for
 * traversal attacks, tracks uncompressed size, and re-packs into a ZIP file.
 *
 * <p>Architecture A9: Per-entry path-safety checks prevent directory traversal.
 *
 * <p>Atomic rename ensures the final cache file is always complete and valid.
 */
@Component
public class ZipBuilder {
    private static final Logger log = LoggerFactory.getLogger(ZipBuilder.class);
    private static final String GITHUB_CODELOAD_BASE = "https://codeload.github.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final int BUFFER_SIZE = 8192;

    private final HttpClient httpClient;

    public ZipBuilder() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Builds a ZIP file from a GitHub tarball, filtering to entries under the example path.
     *
     * <p>Process:
     * 1. Fetch tarball from GitHub as stream
     * 2. Walk tar entries, filtering to those under examplePath
     * 3. Validate each path for traversal attacks (no .., no absolute paths, no nulls)
     * 4. Track running uncompressed size; abort if exceeds maxSizeMb
     * 5. Re-pack into ZipOutputStream backed by a .tmp file
     * 6. Atomic rename .tmp → finalPath
     *
     * @param owner GitHub owner
     * @param repo GitHub repository name
     * @param sha commit SHA
     * @param exampleId example identifier
     * @param examplePath repository-relative path to the example folder (e.g., "examples/leave-request")
     * @param maxDownloadSizeMb maximum uncompressed size in MB
     * @param finalPath final cache file path where the ZIP should be written
     * @return the finalPath File if successful
     * @throws PathSafetyException if a tar entry path traversal is detected
     * @throws SizeLimitExceededException if uncompressed size exceeds maxDownloadSizeMb
     * @throws IOException for I/O errors during fetch/build
     */
    public File build(
            String owner,
            String repo,
            String sha,
            String exampleId,
            String examplePath,
            long maxDownloadSizeMb,
            Path finalPath
    ) throws PathSafetyException, SizeLimitExceededException, IOException {
        // Create parent directories if needed
        Files.createDirectories(finalPath.getParent());

        // Create a temp file in the same directory as the final path for atomic rename
        Path tmpPath = finalPath.resolveSibling(finalPath.getFileName() + ".tmp");

        try {
            // Fetch tarball from GitHub
            String tarballUrl = String.format(
                    "%s/%s/%s/tar.gz/%s",
                    GITHUB_CODELOAD_BASE,
                    owner,
                    repo,
                    sha
            );

            log.debug("Fetching tarball from {}", tarballUrl);
            InputStream tarInputStream = fetchTarball(tarballUrl);

            // Build the ZIP
            long totalSizeBytes = buildZipFromTar(
                    tarInputStream,
                    examplePath,
                    maxDownloadSizeMb,
                    exampleId,
                    tmpPath
            );

            log.debug("Built ZIP for {}/{}/{}: {} bytes", owner, repo, exampleId, totalSizeBytes);

            // Atomic rename tmp → final
            atomicRename(tmpPath, finalPath);

            return finalPath.toFile();
        } catch (PathSafetyException | SizeLimitExceededException e) {
            // Clean up temp file on error
            try {
                Files.deleteIfExists(tmpPath);
            } catch (IOException ioe) {
                log.warn("Failed to delete temp file after exception: {}", tmpPath, ioe);
            }
            throw e;
        } catch (IOException e) {
            // Clean up temp file on error
            try {
                Files.deleteIfExists(tmpPath);
            } catch (IOException ioe) {
                log.warn("Failed to delete temp file after exception: {}", tmpPath, ioe);
            }
            throw e;
        }
    }

    /**
     * Fetches the tarball from GitHub's codeload service.
     *
     * @param tarballUrl the URL to fetch
     * @return an InputStream of the tarball (gzip-compressed)
     * @throws IOException if the fetch fails
     */
    private InputStream fetchTarball(String tarballUrl) throws IOException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tarballUrl))
                .timeout(TIMEOUT)
                .GET()
                .build();

        try {
            HttpResponse<InputStream> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofInputStream()
            );

            if (response.statusCode() != 200) {
                throw new IOException(
                        String.format(
                                "GitHub codeload returned %d for %s",
                                response.statusCode(),
                                tarballUrl
                        )
                );
            }

            return response.body();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while fetching tarball", e);
        }
    }

    /**
     * Builds the ZIP from the tar stream, filtering to entries under examplePath.
     *
     * @param tarInputStream the compressed tarball input stream
     * @param examplePath the repository-relative path to filter to
     * @param maxDownloadSizeMb maximum uncompressed size in MB
     * @param exampleId the example identifier
     * @param tmpPath the temporary file path to write to
     * @return total uncompressed size in bytes
     * @throws PathSafetyException if path traversal is detected
     * @throws SizeLimitExceededException if size limit is exceeded
     * @throws IOException for I/O errors
     */
    private long buildZipFromTar(
            InputStream tarInputStream,
            String examplePath,
            long maxDownloadSizeMb,
            String exampleId,
            Path tmpPath
    ) throws PathSafetyException, SizeLimitExceededException, IOException {
        long maxSizeBytes = maxDownloadSizeMb * 1024 * 1024;
        long totalSizeBytes = 0;

        try (TarArchiveInputStream tar = new TarArchiveInputStream(
                new BufferedInputStream(tarInputStream));
             FileOutputStream fos = new FileOutputStream(tmpPath.toFile());
             ZipOutputStream zip = new ZipOutputStream(fos)) {

            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                // GitHub tarballs are wrapped in a single root directory (owner-repo-sha/)
                // We need to strip that and filter to examplePath entries
                String entryName = entry.getName();

                // Strip the root directory (e.g., "operaton-examples-abc123/")
                String relativePath = stripTarballRoot(entryName);

                if (relativePath == null) {
                    // This is the root directory entry; skip
                    continue;
                }

                // Check if this entry is under the examplePath
                if (!relativePath.startsWith(examplePath + "/") && !relativePath.equals(examplePath)) {
                    continue;
                }

                // Get the path relative to the example subfolder
                String zipEntryPath = getPathUnderExample(relativePath, examplePath);
                if (zipEntryPath == null) {
                    // This is the example directory itself; skip
                    continue;
                }

                // Validate path safety
                validatePathSafety(zipEntryPath);

                // Add to ZIP (skip directories, only add files)
                if (!entry.isDirectory()) {
                    long entrySize = entry.getSize();
                    totalSizeBytes += entrySize;

                    if (totalSizeBytes > maxSizeBytes) {
                        throw new SizeLimitExceededException(
                                String.format(
                                        "Uncompressed size %d MB exceeds limit of %d MB for example %s",
                                        totalSizeBytes / (1024 * 1024),
                                        maxDownloadSizeMb,
                                        exampleId
                                ),
                                maxDownloadSizeMb,
                                totalSizeBytes / (1024 * 1024),
                                exampleId
                        );
                    }

                    ZipEntry zipEntry = new ZipEntry(zipEntryPath);
                    zip.putNextEntry(zipEntry);

                    // Copy data
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int bytesRead;
                    while ((bytesRead = tar.read(buffer)) != -1) {
                        zip.write(buffer, 0, bytesRead);
                    }
                    zip.closeEntry();
                }
            }
        }

        return totalSizeBytes;
    }

    /**
     * Strips the tarball root directory from an entry name.
     * GitHub tarballs wrap content in owner-repo-sha/, we need to remove that.
     *
     * @param entryName the full entry name from the tar
     * @return the path after stripping the root, or null if it's the root
     */
    private String stripTarballRoot(String entryName) {
        // Find the first slash
        int slashIndex = entryName.indexOf('/');
        if (slashIndex == -1) {
            // Root directory
            return null;
        }
        return entryName.substring(slashIndex + 1);
    }

    /**
     * Gets the path relative to the example folder.
     *
     * @param relativePath the path relative to tarball root (e.g., "examples/leave-request/README.md")
     * @param examplePath the example folder path (e.g., "examples/leave-request")
     * @return the path within the example (e.g., "README.md"), or null if this is the example dir itself
     */
    private String getPathUnderExample(String relativePath, String examplePath) {
        if (relativePath.equals(examplePath)) {
            return null;
        }
        if (relativePath.startsWith(examplePath + "/")) {
            return relativePath.substring(examplePath.length() + 1);
        }
        return null;
    }

    /**
     * Validates that a path is safe (no traversal attempts).
     *
     * @param path the path to validate
     * @throws PathSafetyException if the path contains traversal attempts
     */
    private void validatePathSafety(String path) throws PathSafetyException {
        // Check for null bytes
        if (path.contains("\0")) {
            throw new PathSafetyException("Path contains null byte", path);
        }

        // Check for absolute paths
        if (path.startsWith("/")) {
            throw new PathSafetyException("Path is absolute", path);
        }

        // Check for directory traversal
        if (path.contains("..")) {
            throw new PathSafetyException("Path contains directory traversal (..) component", path);
        }

        // Normalize and check again
        try {
            // Use the path normalization to detect attempts to escape
            String[] parts = path.split("/");
            for (String part : parts) {
                if ("..".equals(part)) {
                    throw new PathSafetyException("Path contains .. component after normalization", path);
                }
            }
        } catch (Exception e) {
            throw new PathSafetyException("Path validation failed: " + e.getMessage(), path, e);
        }
    }

    /**
     * Atomically renames the temp file to the final path.
     * Handles the case where atomic rename is not supported by retrying with fallback.
     *
     * @param tmpPath the temporary file path
     * @param finalPath the final file path
     * @throws IOException if the rename fails
     */
    private void atomicRename(Path tmpPath, Path finalPath) throws IOException {
        try {
            Files.move(
                    tmpPath,
                    finalPath,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            log.debug("Atomic move not supported, falling back to regular move", e);
            Files.move(
                    tmpPath,
                    finalPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}
