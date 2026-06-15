package org.operaton.dev.starter.server.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * LRU cache service for example ZIP files.
 *
 * <p>Architecture A6: Cache key is {cacheDir}/{owner}/{repo}/{sha}/{exampleId}.zip.
 * SHA-keying gives natural invalidation: a refresh that bumps the SHA leaves old entries
 * to age out via LRU.
 *
 * <p>Concurrent writers cannot collide because each (sha, exampleId) write goes via a
 * unique .tmp then atomic rename.
 *
 * <p>A @Scheduled task every 10 minutes prunes oldest-by-last-access files until the
 * total cache size is ≤ cache.maxSizeMb. In-flight writes (the .tmp files) are excluded
 * from candidates.
 */
@Component
public class ExampleZipCache {
    private static final Logger log = LoggerFactory.getLogger(ExampleZipCache.class);

    private final ZipBuilder zipBuilder;
    private final Path cacheDir;
    private final long maxCacheSizeMb;
    private final long maxDownloadSizeMb;

    public ExampleZipCache(
            ZipBuilder zipBuilder,
            org.operaton.dev.starter.server.config.StarterProperties props
    ) {
        this.zipBuilder = zipBuilder;
        this.cacheDir = props.examples().cache().dir();
        this.maxCacheSizeMb = props.examples().cache().maxSizeMb();
        this.maxDownloadSizeMb = props.examples().maxDownloadSizeMb();

        log.info(
                "ExampleZipCache initialized: cacheDir={}, maxCacheSizeMb={}, maxDownloadSizeMb={}",
                cacheDir,
                maxCacheSizeMb,
                maxDownloadSizeMb
        );
    }

    /**
     * Gets a cached ZIP file or builds it if not present.
     *
     * <p>If the cache entry exists, returns it immediately.
     * If not, delegates to ZipBuilder to fetch and build, then caches the result.
     *
     * @param owner GitHub owner
     * @param repo GitHub repository name
     * @param sha commit SHA
     * @param exampleId example identifier
     * @param examplePath repository-relative path to the example folder
     * @return the cached or newly built ZIP File
     * @throws PathSafetyException if path traversal is detected during build
     * @throws SizeLimitExceededException if uncompressed size exceeds limit during build
     * @throws IOException for I/O errors
     */
    public File getOrBuild(
            String owner,
            String repo,
            String sha,
            String exampleId,
            String examplePath
    ) throws PathSafetyException, SizeLimitExceededException, IOException {
        // Construct cache key path
        Path cachePath = cacheDir.resolve(owner).resolve(repo).resolve(sha)
                .resolve(exampleId + ".zip");

        // Check if file already exists in cache
        if (Files.exists(cachePath)) {
            log.debug("Cache hit for {}/{}/{}/{}", owner, repo, sha, exampleId);
            // Update last-accessed time for LRU
            try {
                Files.setLastModifiedTime(cachePath, FileTime.fromMillis(System.currentTimeMillis()));
            } catch (IOException e) {
                log.warn("Failed to update last-modified time for cache entry: {}", cachePath, e);
            }
            return cachePath.toFile();
        }

        // Cache miss: build via ZipBuilder
        log.debug("Cache miss for {}/{}/{}/{}, building", owner, repo, sha, exampleId);
        return zipBuilder.build(
                owner,
                repo,
                sha,
                exampleId,
                examplePath,
                maxDownloadSizeMb,
                cachePath
        );
    }

    /**
     * Prunes the cache by deleting oldest-by-last-access files until total size ≤ maxCacheSizeMb.
     *
     * <p>Runs every 10 minutes via @Scheduled.
     * Excludes .tmp files from candidates.
     * Uses best-effort delete; missing files don't fail the operation.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes in milliseconds
    public void pruneCache() {
        log.debug("Starting cache pruning task");

        try {
            // Calculate current cache size
            long currentSizeBytes = calculateCacheSize();
            long maxSizeBytes = maxCacheSizeMb * 1024 * 1024;

            if (currentSizeBytes <= maxSizeBytes) {
                log.debug("Cache size {} MB is within limit {} MB, no pruning needed",
                        currentSizeBytes / (1024 * 1024),
                        maxCacheSizeMb);
                return;
            }

            log.info("Cache size {} MB exceeds limit {} MB, pruning oldest files",
                    currentSizeBytes / (1024 * 1024),
                    maxCacheSizeMb);

            // Get list of all .zip files (excluding .tmp files)
            List<CacheFile> cacheFiles = getCacheFiles();

            // Sort by last-modified time (oldest first)
            cacheFiles.sort(Comparator.comparing(CacheFile::lastModifiedTime));

            // Delete oldest files until size is under limit
            for (CacheFile cacheFile : cacheFiles) {
                if (currentSizeBytes <= maxSizeBytes) {
                    break;
                }

                try {
                    Files.delete(cacheFile.path());
                    currentSizeBytes -= cacheFile.sizeBytes();
                    log.debug("Deleted cache file: {}", cacheFile.path());
                } catch (IOException e) {
                    log.warn("Failed to delete cache file during pruning: {}", cacheFile.path(), e);
                }
            }

            log.info("Cache pruning completed. New size: {} MB",
                    currentSizeBytes / (1024 * 1024));
        } catch (IOException e) {
            log.error("Error during cache pruning", e);
        }
    }

    /**
     * Calculates the total size of the cache directory.
     *
     * @return total size in bytes
     * @throws IOException if reading the directory fails
     */
    private long calculateCacheSize() throws IOException {
        if (!Files.exists(cacheDir)) {
            return 0;
        }

        try (Stream<Path> paths = Files.walk(cacheDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().endsWith(".tmp"))
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            log.warn("Failed to get size of file: {}", path, e);
                            return 0;
                        }
                    })
                    .sum();
        }
    }

    /**
     * Gets a list of all cache files (excluding .tmp files).
     *
     * @return list of CacheFile records
     * @throws IOException if reading the directory fails
     */
    private List<CacheFile> getCacheFiles() throws IOException {
        List<CacheFile> files = new ArrayList<>();

        if (!Files.exists(cacheDir)) {
            return files;
        }

        try (Stream<Path> paths = Files.walk(cacheDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().endsWith(".tmp"))
                    .forEach(path -> {
                        try {
                            long sizeBytes = Files.size(path);
                            long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
                            files.add(new CacheFile(path, sizeBytes, lastModifiedTime));
                        } catch (IOException e) {
                            log.warn("Failed to get metadata for cache file: {}", path, e);
                        }
                    });
        }

        return files;
    }

    /**
     * Internal record for cache file metadata.
     */
    private record CacheFile(Path path, long sizeBytes, long lastModifiedTime) {}
}
