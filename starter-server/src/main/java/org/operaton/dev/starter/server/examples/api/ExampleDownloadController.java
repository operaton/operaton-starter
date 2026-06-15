package org.operaton.dev.starter.server.examples.api;

import org.operaton.dev.starter.server.examples.ExampleRegistry;
import org.operaton.dev.starter.server.examples.ExampleSnapshot;
import org.operaton.dev.starter.server.examples.ExampleZipCache;
import org.operaton.dev.starter.server.examples.PathSafetyException;
import org.operaton.dev.starter.server.examples.SizeLimitExceededException;
import org.operaton.dev.starter.server.model.Example;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * HTTP endpoint for downloading example ZIPs.
 *
 * <p>Architecture A7: Endpoint is GET /api/v1/examples/{owner}/{repo}/{id}/download — no auth in v1.
 *
 * <p>Architecture A10: ExampleDownloadController never calls the commits API itself — SHA must
 * come from the in-memory ExampleRegistry snapshot.
 *
 * <p>Resolves (owner, repo, exampleId) against ExampleRegistry; returns 404 if not found
 * (no GitHub call). Delegates to ExampleZipCache.getOrBuild() using SHA from the registry snapshot.
 *
 * <p>Maps exceptions to appropriate HTTP status codes:
 * - SizeLimitExceededException → 413 Payload Too Large
 * - PathSafetyException → 502 Bad Gateway
 * - GitHub unreachable (IOException) → 502 Bad Gateway
 */
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleDownloadController {
    private static final Logger log = LoggerFactory.getLogger(ExampleDownloadController.class);
    private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
            .withZone(ZoneId.of("GMT"));

    private final ExampleRegistry exampleRegistry;
    private final ExampleZipCache exampleZipCache;

    public ExampleDownloadController(
            ExampleRegistry exampleRegistry,
            ExampleZipCache exampleZipCache
    ) {
        this.exampleRegistry = exampleRegistry;
        this.exampleZipCache = exampleZipCache;
    }

    /**
     * Downloads an example as a ZIP file.
     *
     * <p>GET /api/v1/examples/{owner}/{repo}/{exampleId}/download
     *
     * @param owner GitHub repository owner
     * @param repo GitHub repository name
     * @param exampleId example identifier
     * @return the ZIP file as a stream, or error response
     */
    @GetMapping("/{owner}/{repo}/{exampleId}/download")
    public ResponseEntity<?> downloadExample(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String exampleId
    ) {
        // Step 1: Resolve example from registry (no GitHub call)
        ExampleSnapshot snapshot = exampleRegistry.snapshot();
        Example example = findExample(snapshot, owner, repo, exampleId);

        if (example == null) {
            log.debug("Example not found: {}/{}/{}", owner, repo, exampleId);
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
            problem.setTitle("Example not found");
            problem.setDetail(String.format(
                    "Example %s not found in repository %s/%s",
                    exampleId,
                    owner,
                    repo
            ));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
        }

        // Get SHA from registry
        String sha = example.getSourceRepoSha();
        if (sha == null || sha.isEmpty()) {
            log.warn("Example {} has no SHA in registry", exampleId);
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
            problem.setTitle("Example SHA not available");
            problem.setDetail("The example does not have a resolved SHA");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
        }

        // Step 2: Get or build the ZIP via cache
        try {
            File zipFile = exampleZipCache.getOrBuild(
                    owner,
                    repo,
                    sha,
                    exampleId,
                    example.getPath()
            );

            // Step 3: Stream the response
            return buildDownloadResponse(zipFile, sha, exampleId);
        } catch (SizeLimitExceededException e) {
            log.warn("Size limit exceeded for {}/{}/{}: {} MB",
                    owner, repo, exampleId, e.getActualSizeMb(), e);
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
            problem.setTitle("Download size too large");
            problem.setDetail(String.format(
                    "The example download exceeds the maximum size of %d MB (actual: %d MB)",
                    e.getLimitMb(),
                    e.getActualSizeMb()
            ));
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(problem);
        } catch (PathSafetyException e) {
            log.warn("Path safety violation while building ZIP for {}/{}/{}: {}",
                    owner, repo, exampleId, e.getEntryPath(), e);
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
            problem.setTitle("Upstream archive failed path-safety check");
            problem.setDetail("The GitHub archive contains invalid paths that could escape the example folder");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
        } catch (IOException e) {
            log.error("Failed to build or retrieve ZIP for {}/{}/{}", owner, repo, exampleId, e);
            // Could be GitHub unreachable or other I/O error
            ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
            problem.setTitle("Failed to retrieve example");
            problem.setDetail("Could not fetch or build the example download. GitHub may be unreachable.");
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problem);
        }
    }

    /**
     * Finds an example in the snapshot by owner/repo/exampleId.
     *
     * @param snapshot the example snapshot
     * @param owner GitHub owner
     * @param repo GitHub repository name
     * @param exampleId example identifier
     * @return the Example, or null if not found
     */
    private Example findExample(
            ExampleSnapshot snapshot,
            String owner,
            String repo,
            String exampleId
    ) {
        String sourceRepo = owner + "/" + repo;

        for (ExampleSnapshot.SourceState source : snapshot.sources()) {
            if (!source.source().startsWith(sourceRepo)) {
                continue;
            }

            for (Example example : source.examples()) {
                if (exampleId.equals(example.getId())) {
                    return example;
                }
            }
        }

        return null;
    }

    /**
     * Builds the download response with appropriate headers.
     *
     * @param zipFile the ZIP file to stream
     * @param sha the commit SHA
     * @param exampleId the example identifier
     * @return a ResponseEntity streaming the file
     */
    private ResponseEntity<?> buildDownloadResponse(File zipFile, String sha, String exampleId) throws IOException {
        String shortSha = sha.substring(0, Math.min(7, sha.length()));
        String etag = String.format("W/\"sha-%s-%s\"", shortSha, exampleId);

        // Get file modification time for Last-Modified header
        long lastModifiedTime = Files.getLastModifiedTime(zipFile.toPath()).toMillis();
        String lastModified = HTTP_DATE_FORMATTER.format(Instant.ofEpochMilli(lastModifiedTime));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename(exampleId + ".zip")
                        .build()
        );
        headers.setETag(etag);
        headers.set(HttpHeaders.LAST_MODIFIED, lastModified);
        headers.setContentLength(zipFile.length());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(zipFile));
    }
}
