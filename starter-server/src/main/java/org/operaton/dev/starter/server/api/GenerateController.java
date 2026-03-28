package org.operaton.dev.starter.server.api;

import org.operaton.dev.starter.server.model.ProjectConfig;
import org.operaton.dev.starter.templates.engine.GenerationEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for {@code POST /api/v1/generate}.
 * Implements the OpenAPI-generated {@code DefaultApi} interface.
 * All mapping logic is delegated to {@link ProjectConfigMapper}.
 */
@RestController
public class GenerateController {

    private static final Logger log = LoggerFactory.getLogger(GenerateController.class);

    private final GenerationEngine engine;
    private final ProjectConfigMapper mapper;

    public GenerateController(GenerationEngine engine, ProjectConfigMapper mapper) {
        this.engine = engine;
        this.mapper = mapper;
    }

    @PostMapping(
            value = "/api/v1/generate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/zip"
    )
    public ResponseEntity<byte[]> generateProject(@RequestBody ProjectConfig dto) {
        var config = mapper.toDomain(dto);

        log.info("Generating project: projectType={} buildSystem={} javaVersion={}",
                config.projectType(), config.buildSystem(), config.javaVersion());

        byte[] zip = engine.generate(config);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + config.artifactId() + ".zip\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zip);
    }
}
