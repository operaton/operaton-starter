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
