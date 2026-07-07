# Repository-Wide Descriptor Discovery Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow `.operaton-starter.yml`/`.yaml` descriptor files to live anywhere in a repository tree; scan all of them, support multiple per repo, make `path` optional (default `.`), and resolve example paths relative to each descriptor's directory.

**Architecture:** Replace the single root-file fetch in `GitHubManifestFetcher` with a GitHub Trees API scan that finds all descriptor files, then fetches each individually. The loader merges all examples from all descriptors, resolving each example's path relative to its descriptor's directory. `FetchResult` is retired in favour of `LocatedFetchResult` which carries the descriptor's file path.

**Tech Stack:** Java 21, Spring Boot, Jackson (`ObjectMapper` — already on classpath via `spring-boot-starter-web`), SnakeYAML (unchanged), WireMock (tests), Mockito (tests).

## Global Constraints

- All source files are under `starter-server/src/main/java/org/operaton/dev/starter/server/examples/`
- All test files are under `starter-server/src/test/java/org/operaton/dev/starter/server/examples/`
- Run tests with: `./mvnw test -pl starter-server -Dtest=<ClassName>`
- Run all server tests with: `./mvnw test -pl starter-server`
- No Spring context needed for these unit/integration tests (no `@SpringBootTest`)
- Conventional commits: `feat(examples): ...`

---

## Task 1: Make `path` optional in `ExampleManifestParser`

**Files:**
- Modify: `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleManifestParser.java`
- Test: `starter-server/src/test/java/org/operaton/dev/starter/server/examples/ExampleManifestParserTest.java`

**Interfaces:**
- Produces: `ExampleManifestParser.parse()` now accepts YAML with no `path` field; stores null in `ParsedManifest.Example.path()`

- [ ] **Step 1: Write the failing tests**

Add these two tests to `ExampleManifestParserTest`:

```java
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
```

- [ ] **Step 2: Run tests to verify they fail**

```bash
./mvnw test -pl starter-server -Dtest=ExampleManifestParserTest#testParseExample_pathIsOptional_defaultsToNull+testParseExample_emptyPathIsRejected
```

Expected: `testParseExample_pathIsOptional_defaultsToNull` FAILS (throws `ManifestRejected` because current code rejects null/empty path), `testParseExample_emptyPathIsRejected` PASSES (already works).

- [ ] **Step 3: Update `ExampleManifestParser.parseExample()`**

In `ExampleManifestParser.java`, change lines 119 and 128–130 as follows.

Change this block:
```java
String path = getStringValue(exampleMap, "path", "");

// Validate required fields
if (id.isEmpty() || title.isEmpty() || shortDescription.isEmpty()) {
    log.warn("Skipping example in {} with missing required fields: id='{}', title='{}', shortDescription='{}'",
            sourceRepo, id, title, shortDescription.isEmpty() ? "" : "<present>");
    return null;
}

// Validate path
validatePath(path);
```

To:
```java
String path = getStringValue(exampleMap, "path", null);

// Validate required fields
if (id.isEmpty() || title.isEmpty() || shortDescription.isEmpty()) {
    log.warn("Skipping example in {} with missing required fields: id='{}', title='{}', shortDescription='{}'",
            sourceRepo, id, title, shortDescription.isEmpty() ? "" : "<present>");
    return null;
}

// Validate path only when explicitly provided
if (path != null) {
    validatePath(path);
}
```

Also update `validatePath` to reject an empty string explicitly (it currently rejects null-or-empty; keep that but the condition is now only reached when path is non-null):

```java
private void validatePath(String path) throws ManifestRejected {
    // path is guaranteed non-null here (caller checks)
    if (path.isBlank()) {
        throw new ManifestRejected("path-unsafe");
    }
    if (path.startsWith("/")) {
        throw new ManifestRejected("path-unsafe");
    }
    if (path.contains("..")) {
        throw new ManifestRejected("path-unsafe");
    }
    if (path.contains("\0")) {
        throw new ManifestRejected("path-unsafe");
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./mvnw test -pl starter-server -Dtest=ExampleManifestParserTest
```

Expected: All tests PASS, including both new tests.

- [ ] **Step 5: Commit**

```bash
git add starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleManifestParser.java \
        starter-server/src/test/java/org/operaton/dev/starter/server/examples/ExampleManifestParserTest.java
git commit -m "feat(examples): make path optional in manifest parser (defaults to null)"
```

---

## Task 2: Introduce `LocatedFetchResult`, rewrite `GitHubManifestFetcher` with Trees API

**Files:**
- Create: `starter-server/src/main/java/org/operaton/dev/starter/server/examples/LocatedFetchResult.java`
- Delete: `starter-server/src/main/java/org/operaton/dev/starter/server/examples/FetchResult.java` (replaced by `LocatedFetchResult`)
- Modify: `starter-server/src/main/java/org/operaton/dev/starter/server/examples/GitHubManifestFetcher.java`
- Test: `starter-server/src/test/java/org/operaton/dev/starter/server/examples/GitHubManifestFetcherTest.java`

**Interfaces:**
- Consumes: `SourceUnavailable` (unchanged)
- Produces: `GitHubManifestFetcher.fetch(sourceToken)` now returns `List<LocatedFetchResult>`; `LocatedFetchResult(byte[] yamlBytes, String resolvedSha, String descriptorPath)`

- [ ] **Step 1: Create `LocatedFetchResult.java`**

```java
package org.operaton.dev.starter.server.examples;

/**
 * Result of fetching a single descriptor file found during repository-wide scanning.
 *
 * @param yamlBytes      the raw YAML content of this descriptor
 * @param resolvedSha    the commit SHA the tree was scanned at (same for all descriptors in one fetch)
 * @param descriptorPath repository-relative path to this descriptor file, e.g., {@code ".operaton-starter.yml"}
 *                       or {@code "examples/foo/.operaton-starter.yml"}
 */
public record LocatedFetchResult(
        byte[] yamlBytes,
        String resolvedSha,
        String descriptorPath
) {}
```

- [ ] **Step 2: Write the failing tests for `GitHubManifestFetcher`**

Replace the entire content of `GitHubManifestFetcherTest.java` with:

```java
package org.operaton.dev.starter.server.examples;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class GitHubManifestFetcherTest {

    private WireMockServer wireMockServer;
    private GitHubManifestFetcher fetcher;

    private static final String TEST_SHA = "abcdef0123456789abcdef0123456789abcdef01";
    private static final String TEST_OWNER = "test-owner";
    private static final String TEST_REPO = "test-repo";
    private static final String TEST_MANIFEST_CONTENT = """
            apiVersion: operaton-starter/v1
            examples:
              - id: test-example
                title: Test
                shortDescription: Test example
                path: test-path
            """;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        String baseUrl = "http://localhost:" + wireMockServer.port();
        fetcher = new GitHubManifestFetcher(baseUrl, baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) wireMockServer.stop();
    }

    // --- helpers ---

    private void stubSha() {
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse().withStatus(200).withBody(TEST_SHA)));
    }

    private void stubTree(String treeJson) {
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/git/trees/" + TEST_SHA))
                .willReturn(aResponse().withStatus(200).withBody(treeJson)));
    }

    private void stubDescriptor(String path, String content) {
        stubFor(get(urlPathEqualTo("/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/" + path))
                .willReturn(aResponse().withStatus(200).withBody(content)));
    }

    // --- tests ---

    @Test
    void fetch_singleRootDescriptor_yml() throws SourceUnavailable {
        stubSha();
        stubTree("""
                {"tree":[{"path":".operaton-starter.yml","type":"blob"}],"truncated":false}
                """);
        stubDescriptor(".operaton-starter.yml", TEST_MANIFEST_CONTENT);

        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);

        assertEquals(1, results.size());
        assertEquals(TEST_SHA, results.get(0).resolvedSha());
        assertEquals(".operaton-starter.yml", results.get(0).descriptorPath());
        assertEquals(TEST_MANIFEST_CONTENT, new String(results.get(0).yamlBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void fetch_singleRootDescriptor_yaml_extension() throws SourceUnavailable {
        stubSha();
        stubTree("""
                {"tree":[{"path":".operaton-starter.yaml","type":"blob"}],"truncated":false}
                """);
        stubDescriptor(".operaton-starter.yaml", TEST_MANIFEST_CONTENT);

        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);

        assertEquals(1, results.size());
        assertEquals(".operaton-starter.yaml", results.get(0).descriptorPath());
    }

    @Test
    void fetch_multipleDescriptors_inDifferentDirectories() throws SourceUnavailable {
        String manifest2 = """
                apiVersion: operaton-starter/v1
                examples:
                  - id: foo-example
                    title: Foo
                    shortDescription: Foo example
                """;
        stubSha();
        stubTree("""
                {"tree":[
                  {"path":".operaton-starter.yml","type":"blob"},
                  {"path":"examples/foo/.operaton-starter.yml","type":"blob"}
                ],"truncated":false}
                """);
        stubDescriptor(".operaton-starter.yml", TEST_MANIFEST_CONTENT);
        stubDescriptor("examples/foo/.operaton-starter.yml", manifest2);

        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);

        assertEquals(2, results.size());
        assertEquals(".operaton-starter.yml", results.get(0).descriptorPath());
        assertEquals("examples/foo/.operaton-starter.yml", results.get(1).descriptorPath());
        // Both share same SHA
        assertEquals(TEST_SHA, results.get(0).resolvedSha());
        assertEquals(TEST_SHA, results.get(1).resolvedSha());
    }

    @Test
    void fetch_ymlAndYamlInSameDir_prefersYml_skipsYaml() throws SourceUnavailable {
        stubSha();
        stubTree("""
                {"tree":[
                  {"path":".operaton-starter.yml","type":"blob"},
                  {"path":".operaton-starter.yaml","type":"blob"}
                ],"truncated":false}
                """);
        stubDescriptor(".operaton-starter.yml", TEST_MANIFEST_CONTENT);

        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);

        assertEquals(1, results.size());
        assertEquals(".operaton-starter.yml", results.get(0).descriptorPath());
    }

    @Test
    void fetch_noDescriptors_returnsEmptyList() throws SourceUnavailable {
        stubSha();
        stubTree("""
                {"tree":[
                  {"path":"README.md","type":"blob"},
                  {"path":"pom.xml","type":"blob"}
                ],"truncated":false}
                """);

        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);

        assertTrue(results.isEmpty());
    }

    @Test
    void fetch_truncatedTree_logsWarning_returnsVisibleDescriptors() throws SourceUnavailable {
        stubSha();
        stubTree("""
                {"tree":[{"path":".operaton-starter.yml","type":"blob"}],"truncated":true}
                """);
        stubDescriptor(".operaton-starter.yml", TEST_MANIFEST_CONTENT);

        // Should succeed (warning logged, partial results returned)
        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);
        assertEquals(1, results.size());
    }

    @Test
    void fetch_nonDefaultBranchRef() throws SourceUnavailable {
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/develop"))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse().withStatus(200).withBody(TEST_SHA)));
        stubTree("""
                {"tree":[{"path":".operaton-starter.yml","type":"blob"}],"truncated":false}
                """);
        stubDescriptor(".operaton-starter.yml", TEST_MANIFEST_CONTENT);

        List<LocatedFetchResult> results = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO + "@develop");

        assertEquals(1, results.size());
        assertEquals(TEST_SHA, results.get(0).resolvedSha());
    }

    @Test
    void fetch_404OnCommitsApi_throwsSourceUnavailable() {
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .willReturn(aResponse().withStatus(404).withBody("Not Found")));

        SourceUnavailable ex = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));
        assertEquals("http-404", ex.getReason());
    }

    @Test
    void fetch_404OnTreesApi_throwsSourceUnavailable() {
        stubSha();
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/git/trees/" + TEST_SHA))
                .willReturn(aResponse().withStatus(404).withBody("Not Found")));

        SourceUnavailable ex = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));
        assertEquals("http-404", ex.getReason());
    }

    @Test
    void fetch_404OnDescriptorFetch_throwsSourceUnavailable() {
        stubSha();
        stubTree("""
                {"tree":[{"path":".operaton-starter.yml","type":"blob"}],"truncated":false}
                """);
        stubFor(get(urlPathEqualTo("/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/.operaton-starter.yml"))
                .willReturn(aResponse().withStatus(404)));

        SourceUnavailable ex = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));
        assertEquals("http-404", ex.getReason());
    }

    @Test
    void fetch_5xxFromCommitsApi_throwsSourceUnavailable() {
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        SourceUnavailable ex = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));
        assertEquals("http-500", ex.getReason());
    }

    @Test
    void fetch_timeoutOnCommitsApi_throwsSourceUnavailable() {
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(6000).withBody(TEST_SHA)));

        SourceUnavailable ex = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));
        assertEquals("timeout", ex.getReason());
    }
}
```

- [ ] **Step 3: Run tests to verify they fail**

```bash
./mvnw test -pl starter-server -Dtest=GitHubManifestFetcherTest
```

Expected: Compilation error or test failures — `LocatedFetchResult` doesn't exist yet and `fetch()` still returns `FetchResult`.

- [ ] **Step 4: Rewrite `GitHubManifestFetcher.java`**

Replace the entire file content with:

```java
package org.operaton.dev.starter.server.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        for (String descriptorPath : descriptorPaths) {
            byte[] yamlBytes = fetchDescriptor(owner, repoName, sha, descriptorPath);
            results.add(new LocatedFetchResult(yamlBytes, sha, descriptorPath));
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
        } catch (IOException e) {
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
        } catch (IOException e) {
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
        } catch (IOException e) {
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
```

- [ ] **Step 5: Delete `FetchResult.java`**

```bash
rm starter-server/src/main/java/org/operaton/dev/starter/server/examples/FetchResult.java
```

(The compiler will now point to remaining usages in `ExampleRepositoryLoader` and its test — those get fixed in Task 3.)

- [ ] **Step 6: Run only the fetcher tests to verify they pass**

```bash
./mvnw test -pl starter-server -Dtest=GitHubManifestFetcherTest
```

Expected: All tests PASS. (The loader tests will fail due to compilation errors — fix in Task 3.)

- [ ] **Step 7: Commit**

```bash
git add starter-server/src/main/java/org/operaton/dev/starter/server/examples/LocatedFetchResult.java \
        starter-server/src/main/java/org/operaton/dev/starter/server/examples/GitHubManifestFetcher.java \
        starter-server/src/test/java/org/operaton/dev/starter/server/examples/GitHubManifestFetcherTest.java
git rm starter-server/src/main/java/org/operaton/dev/starter/server/examples/FetchResult.java
git commit -m "feat(examples): scan whole repo tree for descriptor files; support .yaml extension"
```

---

## Task 3: Update `ExampleRepositoryLoader` for multi-descriptor path resolution

**Files:**
- Modify: `starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleRepositoryLoader.java`
- Test: `starter-server/src/test/java/org/operaton/dev/starter/server/examples/ExampleRepositoryLoaderTest.java`

**Interfaces:**
- Consumes: `GitHubManifestFetcher.fetch()` → `List<LocatedFetchResult>`; `ExampleManifestParser.parse()` → `ParsedManifest` (unchanged signature)
- Produces: examples whose `.path()` is now the fully resolved absolute repository path

- [ ] **Step 1: Write the new/updated tests**

Replace the entire content of `ExampleRepositoryLoaderTest.java` with:

```java
package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.Example;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleRepositoryLoaderTest {

    @Mock private GitHubManifestFetcher fetcher;
    @Mock private ExampleManifestParser parser;
    @Mock private ExampleRegistry registry;
    @Mock private StarterProperties properties;

    @InjectMocks
    private ExampleRepositoryLoader loader;

    private static ParsedManifest.Example parsedEx(String id, String path) {
        return new ParsedManifest.Example(id, "Title " + id, "Desc " + id, path,
                List.of(), null, null, null, null, null, null, null,
                List.of(), List.of(), null, List.of(), null, null, null, List.of(), null);
    }

    @Test
    void loader_handles_single_root_descriptor() throws Exception {
        var sourceToken = "owner/repo";
        var located = new LocatedFetchResult("yaml".getBytes(), "sha1", ".operaton-starter.yml");
        var manifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "examples/foo")), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located));
        when(parser.parse(any(), eq("owner/repo"), eq("sha1"))).thenReturn(manifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            // Root descriptor: path relative to root stays unchanged
            return examples.size() == 1 && "examples/foo".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_resolves_path_relative_to_descriptor_directory() throws Exception {
        var sourceToken = "owner/repo";
        var located = new LocatedFetchResult("yaml".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        // path: "bar" in a descriptor at "examples/foo/" → resolved "examples/foo/bar"
        var manifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "bar")), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located));
        when(parser.parse(any(), eq("owner/repo"), eq("sha1"))).thenReturn(manifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 1 && "examples/foo/bar".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_defaults_null_path_to_descriptor_directory() throws Exception {
        var sourceToken = "owner/repo";
        var located = new LocatedFetchResult("yaml".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        // path: null → resolved to "examples/foo"
        var manifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located));
        when(parser.parse(any(), eq("owner/repo"), eq("sha1"))).thenReturn(manifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 1 && "examples/foo".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_merges_examples_from_multiple_descriptors() throws Exception {
        var sourceToken = "owner/repo";
        var located1 = new LocatedFetchResult("yaml1".getBytes(), "sha1", ".operaton-starter.yml");
        var located2 = new LocatedFetchResult("yaml2".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        var manifest1 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "root-path")), "owner/repo", "sha1");
        var manifest2 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex2", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located1, located2));
        when(parser.parse(same(located1.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest1);
        when(parser.parse(same(located2.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest2);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 2;
        }));
    }

    @Test
    void loader_skips_duplicate_ids_across_descriptors() throws Exception {
        var sourceToken = "owner/repo";
        var located1 = new LocatedFetchResult("yaml1".getBytes(), "sha1", ".operaton-starter.yml");
        var located2 = new LocatedFetchResult("yaml2".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        // Both descriptors declare the same id "ex1"
        var manifest1 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "path1")), "owner/repo", "sha1");
        var manifest2 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located1, located2));
        when(parser.parse(same(located1.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest1);
        when(parser.parse(same(located2.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest2);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            // Duplicate id skipped: only one example survives
            return examples.size() == 1 && "path1".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_skips_on_fetch_failure() throws Exception {
        var sourceToken = "owner/repo";

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenThrow(new SourceUnavailable("timeout"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot ->
                snapshot.sources().stream().anyMatch(s -> s.outcome().contains("skipped"))));
    }

    @Test
    void loader_skips_source_when_no_descriptors_found() throws Exception {
        var sourceToken = "owner/repo";

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of());
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot ->
                snapshot.sources().stream().anyMatch(s -> s.outcome().contains("no-descriptors"))));
    }

    @Test
    void loader_isolates_per_descriptor_parse_failure() throws Exception {
        var sourceToken = "owner/repo";
        var located1 = new LocatedFetchResult("bad".getBytes(), "sha1", ".operaton-starter.yml");
        var located2 = new LocatedFetchResult("good".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        var goodManifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located1, located2));
        when(parser.parse(same(located1.yamlBytes()), any(), any()))
                .thenThrow(new ManifestRejected("malformed-yaml"));
        when(parser.parse(same(located2.yamlBytes()), any(), any())).thenReturn(goodManifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        // Second descriptor still contributes its example despite first failing
        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 1;
        }));
    }

    @Test
    void loader_handles_multiple_sources_in_parallel() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(sources, null, 50));
        when(fetcher.fetch(anyString())).thenReturn(List.of(
                new LocatedFetchResult("yaml".getBytes(), "sha1", ".operaton-starter.yml")));
        when(parser.parse(any(), anyString(), anyString())).thenReturn(
                new ParsedManifest("operaton-starter/v1", List.of(), "owner/repo1", "sha1"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(any());
    }

    @Test
    void loader_app_starts_even_if_all_sources_fail() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(sources, null, 50));
        when(fetcher.fetch(anyString())).thenThrow(new SourceUnavailable("timeout"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot ->
                snapshot.sources().size() == 2 &&
                snapshot.sources().stream().allMatch(s -> s.outcome().contains("skipped"))));
    }

    @Test
    void loader_handles_empty_source_list() throws Exception {
        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(), null, 50));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> snapshot.sources().isEmpty()));
    }
}
```

- [ ] **Step 2: Run tests to confirm they fail (compilation)**

```bash
./mvnw test -pl starter-server -Dtest=ExampleRepositoryLoaderTest
```

Expected: Compilation errors — `FetchResult` is gone and `LocatedFetchResult` is not yet used in the loader.

- [ ] **Step 3: Rewrite `ExampleRepositoryLoader.loadSourceWithStatus()`**

In `ExampleRepositoryLoader.java`, replace the entire `loadSourceWithStatus` method plus add two private helper methods. Also add `import java.util.LinkedHashSet;` and `import java.util.Set;` to the imports.

Replace the `loadSourceWithStatus` method:

```java
private LoadSourceResult loadSourceWithStatus(String sourceToken) {
    try {
        // Step 1: Fetch all descriptor locations
        List<LocatedFetchResult> locatedResults;
        try {
            locatedResults = fetcher.fetch(sourceToken);
        } catch (SourceUnavailable e) {
            return failedResult(sourceToken, "skipped:" + e.getReason(), e.getReason());
        }

        if (locatedResults.isEmpty()) {
            return failedResult(sourceToken, "skipped:no-descriptors", "no-descriptors");
        }

        String repo = sourceToken.contains("@")
                ? sourceToken.substring(0, sourceToken.indexOf('@')) : sourceToken;
        String resolvedSha = locatedResults.get(0).resolvedSha();

        // Step 2: Parse each descriptor, resolve paths, collect examples
        List<Example> allExamples = new ArrayList<>();
        Set<String> seenIds = new LinkedHashSet<>();

        for (LocatedFetchResult located : locatedResults) {
            ParsedManifest parsedManifest;
            try {
                parsedManifest = parser.parse(located.yamlBytes(), repo, located.resolvedSha());
            } catch (ManifestRejected e) {
                log.warn("Skipping descriptor '{}' in source {}: {}", located.descriptorPath(), sourceToken, e.getReason());
                continue;
            }

            String descriptorDir = descriptorDir(located.descriptorPath());

            for (ParsedManifest.Example parsedEx : parsedManifest.examples()) {
                if (seenIds.contains(parsedEx.id())) {
                    log.warn("Duplicate example id '{}' in descriptor '{}' of source {}; skipping",
                            parsedEx.id(), located.descriptorPath(), sourceToken);
                    continue;
                }
                seenIds.add(parsedEx.id());

                String resolvedPath = resolveExamplePath(descriptorDir, parsedEx.path());

                var example = new Example()
                        .id(parsedEx.id())
                        .title(parsedEx.title())
                        .path(resolvedPath)
                        .shortDescription(parsedEx.shortDescription())
                        .sourceRepo(parsedManifest.sourceRepo())
                        .sourceRepoSha(parsedManifest.sourceRepoSha())
                        .sourceRepoUrl("https://github.com/" + parsedManifest.sourceRepo()
                                + "/tree/" + parsedManifest.sourceRepoSha() + "/" + resolvedPath)
                        .icon(parsedEx.icon())
                        .longDescription(parsedEx.longDescription())
                        .operatonVersion(parsedEx.operatonVersion())
                        .javaVersion(parsedEx.javaVersion())
                        .integrations(parsedEx.integrations().isEmpty() ? null : parsedEx.integrations())
                        .bpmnConcepts(parsedEx.bpmnConcepts().isEmpty() ? null : parsedEx.bpmnConcepts())
                        .requires(parsedEx.requires())
                        .license(parsedEx.license())
                        .documentationUrl(parsedEx.documentationUrl())
                        .demoVideoUrl(parsedEx.demoVideoUrl())
                        .screenshots(parsedEx.screenshots().isEmpty() ? null : parsedEx.screenshots());

                if (parsedEx.buildSystem() != null) {
                    try { example.buildSystem(Example.BuildSystemEnum.fromValue(parsedEx.buildSystem())); }
                    catch (IllegalArgumentException e) { log.debug("Unknown buildSystem value: {}", parsedEx.buildSystem()); }
                }
                if (parsedEx.runtime() != null) {
                    try { example.runtime(Example.RuntimeEnum.fromValue(parsedEx.runtime())); }
                    catch (IllegalArgumentException e) { log.debug("Unknown runtime value: {}", parsedEx.runtime()); }
                }
                if (parsedEx.complexity() != null) {
                    try { example.complexity(Example.ComplexityEnum.fromValue(parsedEx.complexity())); }
                    catch (IllegalArgumentException e) { log.debug("Unknown complexity value: {}", parsedEx.complexity()); }
                }
                if (parsedEx.lastUpdated() != null) {
                    try { example.lastUpdated(java.time.LocalDate.parse(parsedEx.lastUpdated())); }
                    catch (Exception e) { log.debug("Could not parse lastUpdated date: {}", parsedEx.lastUpdated()); }
                }
                if (!parsedEx.tags().isEmpty()) {
                    List<org.operaton.dev.starter.server.model.Tag> modelTags = parsedEx.tags().stream()
                            .map(t -> new org.operaton.dev.starter.server.model.Tag()
                                    .label(t.label()).category(mapTagCategory(t.category())))
                            .toList();
                    example.tags(modelTags);
                }
                if (!parsedEx.authors().isEmpty()) {
                    List<org.operaton.dev.starter.server.model.Author> modelAuthors = parsedEx.authors().stream()
                            .map(a -> new org.operaton.dev.starter.server.model.Author().name(a.name()).url(a.url()))
                            .toList();
                    example.authors(modelAuthors);
                }

                allExamples.add(example);
            }
        }

        var sourceState = new ExampleSnapshot.SourceState(
                sourceToken, "success", allExamples, resolvedSha, Instant.now().toString());
        var status = new ExampleSourceStatus(
                sourceToken, "success", allExamples.size(), resolvedSha, Instant.now().toString(),
                java.util.Optional.empty());
        return new LoadSourceResult(sourceState, status);

    } catch (Exception e) {
        log.warn("Unexpected exception loading source {}", sourceToken, e);
        return failedResult(sourceToken, "skipped:error", "Unexpected error: " + e.getMessage());
    }
}

/** Returns the directory containing a descriptor, e.g., "examples/foo" for "examples/foo/.operaton-starter.yml". */
private static String descriptorDir(String descriptorPath) {
    int slash = descriptorPath.lastIndexOf('/');
    return slash < 0 ? "" : descriptorPath.substring(0, slash);
}

/**
 * Resolves an example's path relative to its descriptor's directory.
 *
 * <p>Resolution rules:
 * <ul>
 *   <li>null or empty path → defaults to "." → resolves to the descriptor dir (or "." for root)</li>
 *   <li>"." → same as above</li>
 *   <li>non-empty path → prepend descriptor dir (empty dir leaves path unchanged for backward compat)</li>
 * </ul>
 */
static String resolveExamplePath(String descriptorDir, String examplePath) {
    String effective = (examplePath == null || examplePath.isEmpty()) ? "." : examplePath;
    if (effective.equals(".")) {
        return descriptorDir.isEmpty() ? "." : descriptorDir;
    }
    return descriptorDir.isEmpty() ? effective : descriptorDir + "/" + effective;
}
```

Also update the `load()` method: change `fetcher.fetch(sourceToken)` from returning `FetchResult` — this is already done because the `loadSourceWithStatus` rewrite handles it. No change needed to `load()` itself.

Remove the old `FetchResult` import if it exists in the file imports.

- [ ] **Step 4: Run all loader tests**

```bash
./mvnw test -pl starter-server -Dtest=ExampleRepositoryLoaderTest
```

Expected: All tests PASS.

- [ ] **Step 5: Run all server tests**

```bash
./mvnw test -pl starter-server
```

Expected: All tests PASS (fetcher + parser + loader + smoke tests).

- [ ] **Step 6: Commit**

```bash
git add starter-server/src/main/java/org/operaton/dev/starter/server/examples/ExampleRepositoryLoader.java \
        starter-server/src/test/java/org/operaton/dev/starter/server/examples/ExampleRepositoryLoaderTest.java
git commit -m "feat(examples): resolve paths relative to descriptor dir; support multiple descriptors per repo"
```

---

## Task 4: Update documentation

**Files:**
- Modify: `docs/examples-repository-format.md`

- [ ] **Step 1: Apply documentation updates**

Make the following targeted changes to `docs/examples-repository-format.md`:

**1. Replace the "Repository Structure" section** to show both patterns:

```markdown
## Repository Structure

Descriptor files (`.operaton-starter.yml` or `.operaton-starter.yaml`) can be placed **anywhere** in the repository tree. The starter scans the full repository and loads every descriptor it finds.

### Pattern A: Central root manifest (original pattern, still supported)

```
example-repo/
├── .operaton-starter.yml         # lists all examples with explicit paths
├── examples/
│   ├── leave-request-spring-boot/
│   └── order-fulfillment-quarkus/
└── README.md
```

### Pattern B: Per-directory descriptors (new)

```
example-repo/
├── examples/
│   ├── leave-request-spring-boot/
│   │   ├── .operaton-starter.yml  # describes this example; path defaults to "."
│   │   ├── pom.xml
│   │   └── src/
│   └── order-fulfillment-quarkus/
│       ├── .operaton-starter.yml  # describes this example; path defaults to "."
│       ├── build.gradle
│       └── src/
└── README.md
```

Both patterns can coexist in the same repository.
```

**2. Update the `path` row in the Example Entry table:**

```
| `path` | no | path | Relative to the directory containing this descriptor. Omit (or use `.`) to mean "the same directory as this descriptor". No `..`, no leading `/`. |
```

**3. Add a note after the table:**

```markdown
> **Path resolution**: The `path` field is resolved relative to the directory where the descriptor lives. A root-level manifest (`/.operaton-starter.yml`) with `path: examples/foo` behaves identically to before. A descriptor at `examples/foo/.operaton-starter.yml` with no `path` (or `path: .`) means the example occupies `examples/foo/`.
```

**4. Update the "Extension" note** in Key rules:

```markdown
- Descriptor filename must be `.operaton-starter.yml` **or** `.operaton-starter.yaml`; if both exist in the same directory, `.yml` is used and `.yaml` is ignored (a warning is logged)
- Multiple descriptor files per repository are supported; example `id` values must be unique across all descriptors in a repository
```

**5. Update the "Registration Checklist"** — replace the path-related item:

```markdown
- [ ] At least one `.operaton-starter.yml` or `.operaton-starter.yaml` exists in the repository (at root or in any subdirectory)
- [ ] All `path` values (when provided) reference existing directories relative to their descriptor's location
```

- [ ] **Step 2: Verify the file reads well**

```bash
head -60 docs/examples-repository-format.md
```

- [ ] **Step 3: Commit**

```bash
git add docs/examples-repository-format.md
git commit -m "docs(examples): update repository format guide for per-directory descriptors and optional path"
```

---

## Self-Review

**Spec coverage check:**

| Requirement | Task |
|---|---|
| FR-1.1 Whole-repo scan (not just root) | Task 2 — `findDescriptorPaths()` |
| FR-1.2 GitHub Trees API (`recursive=1`) | Task 2 — Trees API call |
| FR-1.3 `.yml` and `.yaml` filenames | Task 2 — `DESCRIPTOR_FILENAMES` set |
| FR-1.4 Truncated tree warning | Task 2 — `truncated` JSON field check |
| FR-2.1–2.3 Multiple descriptors merged | Task 3 — loop over `locatedResults` |
| FR-2.4 Duplicate ID warning + skip | Task 3 — `seenIds` set |
| FR-3.1–3.3 Descriptor-relative path | Task 3 — `resolveExamplePath()` |
| FR-4.1–4.3 Optional path, default `.` | Task 1 + Task 3 |
| FR-5.1 Both extensions recognized | Task 2 |
| FR-5.2 `.yml` wins collision, `.yaml` skipped | Task 2 — `resolveCollisions()` |
| FR-6.1–6.2 Per-descriptor error isolation | Task 3 — `continue` on `ManifestRejected` |
| FR-7.1 Documentation update | Task 4 |

**Backward compatibility check:**
- Root manifest with `path: examples/foo` → `descriptorDir = ""`, `resolveExamplePath("", "examples/foo")` → `"examples/foo"` ✓ unchanged.
