package org.operaton.dev.starter.server.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Pattern;

/**
 * Fetches example manifests from GitHub repositories, resolving source tokens to SHAs.
 *
 * <p>Architecture A5: SHA resolution via {@code GET /repos/{o}/{r}/commits/{ref}} with
 * {@code Accept: application/vnd.github.sha}; raw manifest via
 * {@code raw.githubusercontent.com/{o}/{r}/{sha}/.operaton-starter.yml}.
 *
 * <p>Architecture A9: Outbound surface limited to {@code raw.githubusercontent.com} and
 * {@code api.github.com}. No credentials.
 *
 * <p>Architecture A11: Resolves {@code @ref} → SHA at each load for manifest/download consistency.
 *
 * <p>Key behaviors:
 * <ul>
 *   <li>5-second per-call timeout; throws {@code SourceUnavailable("timeout")} on timeout</li>
 *   <li>On any non-2xx HTTP, throws {@code SourceUnavailable("http-{status}")}</li>
 *   <li>Parses source token "owner/repo" or "owner/repo@ref"; default ref is "HEAD"</li>
 * </ul>
 */
public class GitHubManifestFetcher {
    private static final Logger log = LoggerFactory.getLogger(GitHubManifestFetcher.class);

    private static final String DEFAULT_GITHUB_API_BASE = "https://api.github.com";
    private static final String DEFAULT_RAW_GITHUB_BASE = "https://raw.githubusercontent.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String MANIFEST_FILENAME = ".operaton-starter.yml";

    // 40-character hex SHA pattern
    private static final Pattern SHA_PATTERN = Pattern.compile("^[a-f0-9]{40}$");

    private final HttpClient httpClient;
    private final String githubApiBase;
    private final String rawGithubBase;

    public GitHubManifestFetcher() {
        this(DEFAULT_GITHUB_API_BASE, DEFAULT_RAW_GITHUB_BASE);
    }

    /**
     * Constructor for testing with custom API endpoints.
     *
     * @param githubApiBase the base URL for GitHub API (e.g., "https://api.github.com")
     * @param rawGithubBase the base URL for raw GitHub content (e.g., "https://raw.githubusercontent.com")
     */
    protected GitHubManifestFetcher(String githubApiBase, String rawGithubBase) {
        this.httpClient = HttpClient.newHttpClient();
        this.githubApiBase = githubApiBase;
        this.rawGithubBase = rawGithubBase;
    }

    /**
     * Fetches a manifest from a GitHub source token.
     *
     * @param sourceToken the source token in format "owner/repo" or "owner/repo@ref"
     * @return a FetchResult containing the YAML bytes and resolved SHA
     * @throws SourceUnavailable if the fetch fails due to timeout or HTTP error
     */
    public FetchResult fetch(String sourceToken) throws SourceUnavailable {
        // Parse token
        String[] parts = sourceToken.split("@");
        String repo = parts[0]; // "owner/repo"
        String ref = parts.length > 1 ? parts[1] : "HEAD"; // default to HEAD

        // Validate repo format
        if (!repo.contains("/")) {
            throw new SourceUnavailable("invalid-token");
        }

        String[] repoParts = repo.split("/");
        if (repoParts.length != 2) {
            throw new SourceUnavailable("invalid-token");
        }
        String owner = repoParts[0];
        String repoName = repoParts[1];

        // Step 1: Resolve ref to SHA via GitHub commits API
        String sha = resolveSha(owner, repoName, ref);

        // Step 2: Fetch manifest from raw.githubusercontent.com
        byte[] yamlBytes = fetchManifest(owner, repoName, sha);

        return new FetchResult(yamlBytes, sha);
    }

    /**
     * Resolves a ref (branch, tag, or "HEAD") to a commit SHA.
     *
     * @param owner the repository owner
     * @param repo the repository name
     * @param ref the ref to resolve (e.g., "HEAD", "main", "v1.0.0")
     * @return the 40-character commit SHA
     * @throws SourceUnavailable if resolution fails
     */
    protected String resolveSha(String owner, String repo, String ref) throws SourceUnavailable {
        String url = githubApiBase + "/repos/" + owner + "/" + repo + "/commits/" + ref;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github.sha")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            log.debug("Resolving SHA for {}/{} ref={} from {}", owner, repo, ref, url);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new SourceUnavailable("http-" + response.statusCode());
            }

            String sha = response.body().trim();

            // Validate SHA format
            if (!SHA_PATTERN.matcher(sha).matches()) {
                throw new SourceUnavailable("invalid-sha");
            }

            log.debug("Resolved SHA for {}/{}: {}", owner, repo, sha);
            return sha;
        } catch (IOException e) {
            // Network error (including connection timeouts)
            throw new SourceUnavailable("timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SourceUnavailable("timeout", e);
        } catch (SourceUnavailable e) {
            throw e;
        } catch (Exception e) {
            throw new SourceUnavailable("fetch-error", e);
        }
    }

    /**
     * Fetches the manifest YAML from raw.githubusercontent.com.
     *
     * @param owner the repository owner
     * @param repo the repository name
     * @param sha the commit SHA
     * @return the YAML content as bytes
     * @throws SourceUnavailable if the fetch fails
     */
    protected byte[] fetchManifest(String owner, String repo, String sha) throws SourceUnavailable {
        String url = rawGithubBase + "/" + owner + "/" + repo + "/" + sha + "/" + MANIFEST_FILENAME;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();

            log.debug("Fetching manifest from {}", url);

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new SourceUnavailable("http-" + response.statusCode());
            }

            log.debug("Manifest fetched successfully ({} bytes)", response.body().length);
            return response.body();
        } catch (IOException e) {
            // Network error (including connection timeouts)
            throw new SourceUnavailable("timeout", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SourceUnavailable("timeout", e);
        } catch (SourceUnavailable e) {
            throw e;
        } catch (Exception e) {
            throw new SourceUnavailable("fetch-error", e);
        }
    }
}
