package org.operaton.dev.starter.server.examples;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link GitHubManifestFetcher} using WireMock.
 *
 * <p>Covers:
 * <ul>
 *   <li>Happy path: 200 with valid SHA and manifest</li>
 *   <li>404 on commits API</li>
 *   <li>404 on raw content URL</li>
 *   <li>5xx error from commits API</li>
 *   <li>Network timeout</li>
 *   <li>Non-default branch ref</li>
 * </ul>
 */
class GitHubManifestFetcherTest {

    private WireMockServer wireMockServer;
    private GitHubManifestFetcher fetcher;

    // Test data
    private static final String TEST_SHA = "abcdef0123456789abcdef0123456789abcdef01";
    private static final String TEST_OWNER = "test-owner";
    private static final String TEST_REPO = "test-repo";
    private static final String TEST_MANIFEST_CONTENT = """
            apiVersion: operaton-starter/v1
            examples:
              - name: Test Example
                path: test-path
            """;

    @BeforeEach
    void setUp() {
        // Start WireMock server on a random port
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        // Create fetcher with custom URLs pointing to WireMock
        String baseUrl = "http://localhost:" + wireMockServer.port();
        fetcher = new GitHubManifestFetcher(baseUrl, baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testHappyPath_200WithValidShaAndManifest() throws SourceUnavailable {
        // Setup: commits API returns valid SHA
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_SHA)));

        // Setup: raw content API returns manifest
        stubFor(get(urlPathEqualTo(
                "/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/.operaton-starter.yml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_MANIFEST_CONTENT)));

        // Execute
        FetchResult result = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_SHA, result.resolvedSha());
        assertEquals(TEST_MANIFEST_CONTENT, new String(result.yamlBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void testHappyPath_WithNonDefaultBranchRef() throws SourceUnavailable {
        String testBranch = "develop";

        // Setup: commits API returns valid SHA for the branch
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/" + testBranch))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_SHA)));

        // Setup: raw content API returns manifest using the resolved SHA (not the branch name)
        stubFor(get(urlPathEqualTo(
                "/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/.operaton-starter.yml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_MANIFEST_CONTENT)));

        // Execute
        FetchResult result = fetcher.fetch(TEST_OWNER + "/" + TEST_REPO + "@" + testBranch);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_SHA, result.resolvedSha());
        assertEquals(TEST_MANIFEST_CONTENT, new String(result.yamlBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void testError_404OnCommitsApi() {
        // Setup: commits API returns 404
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Not Found")));

        // Execute & Assert
        SourceUnavailable exception = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));

        assertEquals("http-404", exception.getReason());
    }

    @Test
    void testError_404OnRawContentUrl() throws SourceUnavailable {
        // Setup: commits API returns valid SHA
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_SHA)));

        // Setup: raw content API returns 404
        stubFor(get(urlPathEqualTo(
                "/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/.operaton-starter.yml"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Manifest not found")));

        // Execute & Assert
        SourceUnavailable exception = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));

        assertEquals("http-404", exception.getReason());
    }

    @Test
    void testError_5xxFromCommitsApi() {
        // Setup: commits API returns 500
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Execute & Assert
        SourceUnavailable exception = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));

        assertEquals("http-500", exception.getReason());
    }

    @Test
    void testError_5xxFromRawContentApi() throws SourceUnavailable {
        // Setup: commits API returns valid SHA
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_SHA)));

        // Setup: raw content API returns 502
        stubFor(get(urlPathEqualTo(
                "/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/.operaton-starter.yml"))
                .willReturn(aResponse()
                        .withStatus(502)
                        .withBody("Bad Gateway")));

        // Execute & Assert
        SourceUnavailable exception = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));

        assertEquals("http-502", exception.getReason());
    }

    @Test
    void testError_NetworkTimeoutOnCommitsApi() {
        // Setup: commits API delays response beyond timeout
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(6000) // 6 seconds, exceeds 5-second timeout
                        .withBody(TEST_SHA)));

        // Execute & Assert
        SourceUnavailable exception = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));

        assertEquals("timeout", exception.getReason());
    }

    @Test
    void testError_NetworkTimeoutOnRawContentApi() throws SourceUnavailable {
        // Setup: commits API returns valid SHA quickly
        stubFor(get(urlPathEqualTo("/repos/" + TEST_OWNER + "/" + TEST_REPO + "/commits/HEAD"))
                .withHeader("Accept", equalTo("application/vnd.github.sha"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(TEST_SHA)));

        // Setup: raw content API delays response beyond timeout
        stubFor(get(urlPathEqualTo(
                "/" + TEST_OWNER + "/" + TEST_REPO + "/" + TEST_SHA + "/.operaton-starter.yml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(6000) // 6 seconds, exceeds 5-second timeout
                        .withBody(TEST_MANIFEST_CONTENT)));

        // Execute & Assert
        SourceUnavailable exception = assertThrows(SourceUnavailable.class,
                () -> fetcher.fetch(TEST_OWNER + "/" + TEST_REPO));

        assertEquals("timeout", exception.getReason());
    }
}
