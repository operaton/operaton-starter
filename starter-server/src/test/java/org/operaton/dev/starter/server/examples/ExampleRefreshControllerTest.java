package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "starter.examples.repositories="
)
class ExampleRefreshControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void refresh_endpoint_returns_status_list() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/examples/refresh"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertNotNull(body);
        assertTrue(body.startsWith("["), "Response should be a JSON array");
    }

    @Test
    void refresh_endpoint_contains_required_fields_when_no_sources() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/examples/refresh"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        String body = response.body();
        // When no sources configured, should return empty array
        assertEquals("[]", body.trim());
    }
}
