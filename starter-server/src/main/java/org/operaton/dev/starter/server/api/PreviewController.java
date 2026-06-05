package org.operaton.dev.starter.server.api;

import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.ProjectConfig;
import org.operaton.dev.starter.templates.engine.GenerationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * REST controller for {@code POST /api/v1/preview}.
 * Renders a single template with the given config and returns the rendered content.
 * Used by the web UI to show actual file content (with interpolated values) in the preview pane.
 */
@RestController
public class PreviewController {

    private static final Logger log = LoggerFactory.getLogger(PreviewController.class);

    private final GenerationEngine engine;
    private final ProjectConfigMapper mapper;
    private final StarterProperties properties;
    private final ResourceLoader resourceLoader;

    public PreviewController(GenerationEngine engine, ProjectConfigMapper mapper,
                              StarterProperties properties, ResourceLoader resourceLoader) {
        this.engine = engine;
        this.mapper = mapper;
        this.properties = properties;
        this.resourceLoader = resourceLoader;
    }

    @PostMapping(
            value = "/api/v1/preview",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, String>> previewTemplate(
            @RequestBody PreviewRequest request) {
        if (request.templateId() == null || request.templateId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        String content;
        try {
            if (request.templateId().endsWith(".jte")) {
                var domainConfig = mapper.toDomain(request.config()).withGeneratorDefaults(
                        properties.defaults().operatonVersion(),
                        properties.defaults().mavenRegistry()
                );
                content = engine.renderSingleTemplate(request.templateId(), domainConfig);
            } else {
                // Plain static resource (e.g. banner.txt)
                var resource = resourceLoader.getResource("classpath:" + request.templateId());
                content = resource.getContentAsString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.warn("Failed to read static resource for preview: {}", request.templateId(), e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.warn("Failed to render template for preview: {}", request.templateId(), e);
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok(Map.of("content", content));
    }

    public record PreviewRequest(String templateId, ProjectConfig config) {}
}
