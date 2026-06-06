package org.operaton.dev.starter.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.List;

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
    void metadata_returns_preview_content_for_template_entries() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"previewContent\":\""),
                "Expected metadata to include at least one non-null previewContent on a template entry");
    }

    @Test
    void metadata_returns_use_case_examples() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"useCaseExamples\""),
                "Expected metadata to include useCaseExamples array");
        assertTrue(response.body().contains("\"uc-01-leave-request\""),
                "Expected at least uc-01-leave-request use case example");
    }

    @Test
    void generate_with_useCaseId_produces_zip_with_leave_request_balance_support() throws Exception {
        String body = """
                {
                  "projectType": "PROCESS_APPLICATION",
                  "buildSystem": "MAVEN",
                  "groupId": "com.example",
                  "artifactId": "leave-request-example",
                  "projectName": "Leave Request Example",
                  "useCaseId": "uc-01-leave-request"
                }
                """;

        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/generate"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/zip")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());

        List<String> entries = new ArrayList<>();
        try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(response.body()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("src/main/resources/leave-request.bpmn"),
                "ZIP must contain leave-request.bpmn; got: " + entries);
        assertTrue(entries.contains("src/main/resources/schema.sql"),
                "ZIP must contain schema.sql; got: " + entries);
        assertTrue(entries.contains("src/main/java/com/example/leaverequestexample/DataInitializer.java"),
                "ZIP must contain DataInitializer.java; got: " + entries);
        assertTrue(entries.contains("src/main/java/com/example/leaverequestexample/VacationBalanceService.java"),
                "ZIP must contain VacationBalanceService.java; got: " + entries);
        assertTrue(entries.contains("pom.xml"),
                "ZIP must contain pom.xml; got: " + entries);
    }

    @Test
    void generate_with_useCaseId_uc02_produces_zip_with_loan_application_bpmn_and_dmn() throws Exception {
        String body = """
                {
                  "projectType": "PROCESS_APPLICATION",
                  "buildSystem": "MAVEN",
                  "groupId": "com.example",
                  "artifactId": "loan-application-example",
                  "projectName": "Loan Application Example",
                  "useCaseId": "uc-02-loan-application"
                }
                """;

        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/generate"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/zip")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());

        List<String> entries = new ArrayList<>();
        try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(response.body()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("src/main/resources/loan-application.bpmn"),
                "ZIP must contain loan-application.bpmn; got: " + entries);
        assertTrue(entries.contains("src/main/resources/dmn/risk-assessment.dmn"),
                "ZIP must contain risk-assessment.dmn; got: " + entries);
        assertTrue(entries.contains("src/main/resources/wiremock/mappings/credit-score-stub.json"),
                "ZIP must contain credit-score-stub.json; got: " + entries);
        assertTrue(entries.contains("docker-compose.yml"),
                "ZIP must contain docker-compose.yml; got: " + entries);
        assertTrue(entries.contains("pom.xml"),
                "ZIP must contain pom.xml; got: " + entries);
    }

    @Test
    void metadata_returns_uc02_loan_application_use_case() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/metadata"))
                        .header("Accept", "application/json")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"uc-02-loan-application\""),
                "Expected uc-02-loan-application use case in metadata");
    }

    @Test
    void generate_with_useCaseId_uc03_produces_zip_with_incident_management_bpmn() throws Exception {
        String body = """
                {
                  "projectType": "PROCESS_APPLICATION",
                  "buildSystem": "MAVEN",
                  "groupId": "com.example",
                  "artifactId": "incident-management-example",
                  "projectName": "Incident Management Example",
                  "useCaseId": "uc-03-incident-management"
                }
                """;

        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/generate"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/zip")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());

        List<String> entries = new ArrayList<>();
        try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(response.body()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("src/main/resources/incident-management.bpmn"),
                "ZIP must contain incident-management.bpmn; got: " + entries);
        assertTrue(entries.contains("src/main/resources/application-test.properties"),
                "ZIP must contain application-test.properties; got: " + entries);
        assertTrue(entries.contains("docker-compose.yml"),
                "ZIP must contain docker-compose.yml; got: " + entries);
    }

    @Test
    void generate_with_useCaseId_uc04_produces_zip_with_order_fulfillment_bpmn() throws Exception {
        String body = """
                {
                  "projectType": "PROCESS_APPLICATION",
                  "buildSystem": "MAVEN",
                  "groupId": "com.example",
                  "artifactId": "order-fulfillment-example",
                  "projectName": "Order Fulfillment Example",
                  "useCaseId": "uc-04-order-fulfillment"
                }
                """;

        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/generate"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/zip")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());

        List<String> entries = new ArrayList<>();
        try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(response.body()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("src/main/resources/order-fulfillment.bpmn"),
                "ZIP must contain order-fulfillment.bpmn; got: " + entries);
        assertTrue(entries.contains("src/main/resources/wiremock/mappings/inventory-in-stock.json"),
                "ZIP must contain inventory-in-stock.json; got: " + entries);
        assertTrue(entries.contains("docker-compose.yml"),
                "ZIP must contain docker-compose.yml; got: " + entries);
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

    @Test
    void generate_endpoint_supports_use_case_id_in_query_parameters() throws Exception {
        var response = httpClient.send(
                HttpRequest.newBuilder(URI.create(
                                "http://localhost:" + port +
                                        "/api/v1/generate?groupId=com.example" +
                                        "&artifactId=leave-request-example" +
                                        "&projectName=Leave%20Request%20Example" +
                                        "&projectType=PROCESS_APPLICATION" +
                                        "&buildSystem=MAVEN" +
                                        "&javaVersion=17" +
                                        "&useCaseId=uc-01-leave-request"))
                        .header("Accept", "application/zip")
                        .build(),
                HttpResponse.BodyHandlers.ofByteArray()
        );

        assertEquals(200, response.statusCode());

        List<String> entries = new ArrayList<>();
        try (var zis = new ZipInputStream(new java.io.ByteArrayInputStream(response.body()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
        }

        assertTrue(entries.contains("src/main/resources/leave-request.bpmn"),
                "ZIP must contain leave-request.bpmn when useCaseId is provided via query; got: " + entries);
        assertTrue(entries.contains("src/main/resources/schema.sql"),
                "ZIP must contain schema.sql when useCaseId is provided via query; got: " + entries);
        assertTrue(entries.contains("src/main/java/com/example/leaverequestexample/DataInitializer.java"),
                "ZIP must contain DataInitializer.java when useCaseId is provided via query; got: " + entries);
    }
}
