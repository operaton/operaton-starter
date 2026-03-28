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

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "starter.defaults.group-id=com.bank"
)
class ApiControllerTest {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @LocalServerPort
    private int port;

    @Test
    void metadata_exposes_configured_default_group_id() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"defaultGroupId\":\"com.bank\""));
    }

    @Test
    void generate_endpoint_supports_query_parameters_for_ide_deep_links() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create(
                                "http://localhost:" + port +
                                        "/api/v1/generate?groupId=com.example" +
                                        "&artifactId=my-process" +
                                        "&projectName=My%20Process" +
                                        "&projectType=PROCESS_APPLICATION" +
                                        "&buildSystem=MAVEN" +
                                        "&javaVersion=17"))
                        .header("Accept", "application/zip")
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());
        assertEquals("application/zip", response.headers().firstValue("content-type").orElseThrow());
        assertEquals("attachment; filename=\"my-process.zip\"",
                response.headers().firstValue("content-disposition").orElseThrow());
        assertTrue(response.body().length > 0);
    }
}
