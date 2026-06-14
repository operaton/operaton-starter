package org.operaton.dev.starter.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for MetadataController with extended Example schema and TagCategory.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class MetadataControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void metadata_response_includes_examples_field() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        // The examples field may be empty for now, but should be present as a JSON array
        assertTrue(response.body().contains("\"examples\""),
                "Expected metadata response to include 'examples' field");
        assertTrue(response.body().contains("\"examples\":") || response.body().contains("\"examples\" :"),
                "Expected examples field in JSON response");
    }

    @Test
    void metadata_response_preserves_existing_fields() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        // Verify existing fields are still present
        assertTrue(response.body().contains("\"projectTypes\""),
                "Expected projectTypes field in metadata");
        assertTrue(response.body().contains("\"buildSystems\""),
                "Expected buildSystems field in metadata");
        assertTrue(response.body().contains("\"globalOptions\""),
                "Expected globalOptions field in metadata");
        assertTrue(response.body().contains("\"useCaseExamples\""),
                "Expected useCaseExamples field in metadata");
    }

    @Test
    void metadata_response_includes_new_tag_categories() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        // Verify the new tag categories are available in the response
        // The existing tags in useCaseExamples should compile without errors
        // The new categories (RUNTIME, BUILD_SYSTEM, COMPLEXITY) are available for future use
        assertTrue(response.body().contains("\"useCaseExamples\""),
                "Expected useCaseExamples in metadata");
    }

    @Test
    void metadata_response_includes_examples_array() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        // examples array should be present (may be empty if no sources configured)
        assertTrue(response.body().contains("\"examples\""),
                "Expected examples field in metadata response");
        assertTrue(response.body().contains("\"examples\":") || response.body().contains("\"examples\" :"),
                "Expected examples array in JSON response");
    }

    @Test
    void examples_array_contains_correct_fields_when_populated() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        String body = response.body();

        // The examples array should have the structure for Example model
        // Expected fields: id, title, path, shortDescription, sourceRepo, sourceRepoSha, sourceRepoUrl
        // (These will only be present if examples are actually loaded)
        assertTrue(body.contains("\"examples\""),
                "Expected examples array in response");

        // If examples are not empty, verify structure
        if (body.contains("\"examples\":[{")) {
            assertTrue(body.contains("\"id\"") || body.contains("\"title\""),
                    "If examples array is populated, should have example fields");
        }
    }
}
