package org.operaton.dev.starter.server.examples;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Fetches example descriptor files from GitHub repositories by scanning the full repository tree.
 *
 * <p>Fetch flow per source token:
 * <ol>
 *   <li>Resolve {@code ref} → commit SHA via {@code GET /repos/{o}/{r}/commits/{ref}}</li>
 *   <li>Enumerate all files via {@code GET /repos/{o}/{r}/git/trees/{sha}?recursive=1}</li>
 *   <li>Filter for {@code .operaton-starter.yml} / {@code .operaton-starter.yaml} blobs</li>
 *   <li>Resolve .yml/.yaml collisions in the same directory (.yml wins)</li>
 *   <li>Fetch each descriptor via {@code raw.githubusercontent.com}</li>
 * </ol>
 *
 * <p>Key behaviors:
 * <ul>
 *   <li>5-second per-call timeout; throws {@code SourceUnavailable("timeout")} on timeout</li>
 *   <li>On any non-2xx HTTP, throws {@code SourceUnavailable("http-{status}")}</li>
 *   <li>If the tree response is truncated, logs a warning and processes visible descriptors</li>
 *   <li>Returns an empty list when no descriptor files are found (not an error)</li>
 * </ul>
 */
@Component
public class GitHubManifestFetcher {
    private static final Logger log = LoggerFactory.getLogger(GitHubManifestFetcher.class);

    private static final String DEFAULT_GITHUB_API_BASE = "https://api.github.com";
    private static final String DEFAULT_RAW_GITHUB_BASE = "https://raw.githubusercontent.com";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Set<String> DESCRIPTOR_FILENAMES =
            Set.of(".operaton-starter.yml", ".operaton-starter.yaml");

    private static final Pattern SHA_PATTERN = Pattern.compile("^[a-f0-9]{40}$");

    private final HttpClient httpClient;
    private final String githubApiBase;
    private final String rawGithubBase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GitHubManifestFetcher() {
        this(DEFAULT_GITHUB_API_BASE, DEFAULT_RAW_GITHUB_BASE);
    }

    protected GitHubManifestFetcher(String githubApiBase, String rawGithubBase) {
        this.httpClient = HttpClient.newHttpClient();
        this.githubApiBase = githubApiBase;
        this.rawGithubBase = rawGithubBase;
    }

    /**
     * Scans a repository for descriptor files and fetches each one.
     *
     * @param sourceToken "owner/repo" or "owner/repo@ref"
     * @return one {@link LocatedFetchResult} per descriptor found; empty if none exist
     * @throws SourceUnavailable on network error or non-2xx from SHA resolution or tree API
     */
    public List<LocatedFetchResult> fetch(String sourceToken) throws SourceUnavailable {
        String[] parts = sourceToken.split("@", 2);
        String repo = parts[0];
        String ref = parts.length > 1 ? parts[1] : "HEAD";

        if (!repo.contains("/")) throw new SourceUnavailable("invalid-token");
        String[] repoParts = repo.split("/", 2);
        if (repoParts.length != 2) throw new SourceUnavailable("invalid-token");
        String owner = repoParts[0];
        String repoName = repoParts[1];

        String sha = resolveSha(owner, repoName, ref);
        List<String> descriptorPaths = findDescriptorPaths(owner, repoName, sha);

List<LocatedFetchResult> results = new ArrayList<>();
SourceUnavailable lastError = null;
for (String descriptorPath : descriptorPaths) {
    try {
        byte[] yamlBytes = fetchDescriptor(owner, repoName, sha, descriptorPath);
        results.add(new LocatedFetchResult(yamlBytes, sha, descriptorPath));
    } catch (SourceUnavailable e) {
        lastError = e;
        log.warn("Skipping descriptor '{}' in {}/{} at {}: {}", descriptorPath, owner, repoName, sha, e.getReason());
    }
}
if (results.isEmpty() && lastError != null) {
    throw lastError;
}
return results;
    }

    /**
     * Resolves a ref to a 40-character commit SHA.
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
            log.debug("Resolving SHA for {}/{} ref={}", owner, repo, ref);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new SourceUnavailable("http-" + response.statusCode());
            String sha = response.body().trim();
            if (!SHA_PATTERN.matcher(sha).matches()) throw new SourceUnavailable("invalid-sha");
            log.debug("Resolved SHA for {}/{}: {}", owner, repo, sha);
            return sha;
        } catch (java.net.http.HttpTimeoutException e) {
            throw new SourceUnavailable("timeout", e);
        } catch (IOException e) {
            throw new SourceUnavailable("fetch-error", e);
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
     * Uses the GitHub Git Trees API to find all descriptor file paths in the repository.
     * Applies .yml/.yaml collision resolution (see FR-5.2: .yml wins, .yaml skipped with warning).
     */
    protected List<String> findDescriptorPaths(String owner, String repo, String sha)
            throws SourceUnavailable {
        String url = githubApiBase + "/repos/" + owner + "/" + repo + "/git/trees/" + sha + "?recursive=1";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/vnd.github+json")
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            log.debug("Scanning tree for {}/{} at {}", owner, repo, sha);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new SourceUnavailable("http-" + response.statusCode());

            JsonNode root = objectMapper.readTree(response.body());
            if (root.path("truncated").asBoolean(false)) {
                log.warn("Repository tree for {}/{} was truncated; some descriptors may be missed", owner, repo);
            }

            List<String> found = new ArrayList<>();
            for (JsonNode entry : root.path("tree")) {
                if (!"blob".equals(entry.path("type").asText())) continue;
                String entryPath = entry.path("path").asText();
                String filename = entryPath.contains("/")
                        ? entryPath.substring(entryPath.lastIndexOf('/') + 1)
                        : entryPath;
                if (DESCRIPTOR_FILENAMES.contains(filename)) {
                    found.add(entryPath);
                }
            }

            return resolveCollisions(found, owner, repo);
        } catch (java.net.http.HttpTimeoutException e) {
            throw new SourceUnavailable("timeout", e);
        } catch (IOException e) {
            throw new SourceUnavailable("fetch-error", e);
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
     * For each directory that has both .yml and .yaml, keeps .yml and logs a warning about .yaml.
     */
    private List<String> resolveCollisions(List<String> paths, String owner, String repo) {
        Map<String, List<String>> byDir = new LinkedHashMap<>();
        for (String p : paths) {
            String dir = p.contains("/") ? p.substring(0, p.lastIndexOf('/')) : "";
            byDir.computeIfAbsent(dir, k -> new ArrayList<>()).add(p);
        }

        List<String> resolved = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : byDir.entrySet()) {
            List<String> dirPaths = entry.getValue();
            if (dirPaths.size() == 1) {
                resolved.add(dirPaths.get(0));
            } else {
                Optional<String> yml = dirPaths.stream().filter(p -> p.endsWith(".yml")).findFirst();
                Optional<String> yaml = dirPaths.stream().filter(p -> p.endsWith(".yaml")).findFirst();
                if (yml.isPresent() && yaml.isPresent()) {
                    log.warn("Both .operaton-starter.yml and .operaton-starter.yaml found in '{}' of {}/{};"
                            + " using .yml, skipping .yaml", entry.getKey(), owner, repo);
                    resolved.add(yml.get());
                } else {
                    resolved.addAll(dirPaths);
                }
            }
        }
        return resolved;
    }

    /**
     * Fetches a single descriptor file from raw.githubusercontent.com.
     */
    protected byte[] fetchDescriptor(String owner, String repo, String sha, String descriptorPath)
            throws SourceUnavailable {
        String url = rawGithubBase + "/" + owner + "/" + repo + "/" + sha + "/" + descriptorPath;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(TIMEOUT)
                    .GET()
                    .build();
            log.debug("Fetching descriptor {} from {}", descriptorPath, url);
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new SourceUnavailable("http-" + response.statusCode());
            log.debug("Descriptor fetched ({} bytes): {}", response.body().length, descriptorPath);
            return response.body();
        } catch (java.net.http.HttpTimeoutException e) {
            throw new SourceUnavailable("timeout", e);
        } catch (IOException e) {
            throw new SourceUnavailable("fetch-error", e);
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
