package org.operaton.dev.starter.server.api;

import jakarta.validation.Valid;
import org.operaton.dev.starter.server.model.ProjectConfig;
import org.operaton.dev.starter.server.config.StarterProperties;
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
    private final StarterProperties properties;

    public GenerateController(GenerationEngine engine, ProjectConfigMapper mapper, StarterProperties properties) {
        this.engine = engine;
        this.mapper = mapper;
        this.properties = properties;
    }

    @PostMapping(
            value = "/api/v1/generate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/zip"
    )
    public ResponseEntity<byte[]> generateProject(@Valid @RequestBody ProjectConfig dto) {
        return generateProjectResponse(toEffectiveConfig(dto));
    }

    @GetMapping(value = "/api/v1/generate", produces = "application/zip")
    public ResponseEntity<byte[]> generateProjectFromQuery(
            @RequestParam String groupId,
            @RequestParam String artifactId,
            @RequestParam String projectName,
            @RequestParam(defaultValue = "PROCESS_APPLICATION") String projectType,
            @RequestParam(defaultValue = "MAVEN") String buildSystem,
            @RequestParam(defaultValue = "17") Integer javaVersion,
            @RequestParam(required = false) String deploymentTarget,
            @RequestParam(defaultValue = "RENOVATE") String dependencyUpdater,
            @RequestParam(defaultValue = "false") boolean dockerCompose,
            @RequestParam(defaultValue = "true") boolean githubActions
    ) {
        var dto = new ProjectConfig(
                ProjectConfig.ProjectTypeEnum.fromValue(projectType),
                ProjectConfig.BuildSystemEnum.fromValue(buildSystem),
                groupId,
                artifactId,
                projectName
        );
        dto.setJavaVersion(ProjectConfig.JavaVersionEnum.fromValue(javaVersion));
        if (deploymentTarget != null && !deploymentTarget.isBlank()) {
            dto.setDeploymentTarget(ProjectConfig.DeploymentTargetEnum.fromValue(deploymentTarget));
        }
        dto.setDependencyUpdater(ProjectConfig.DependencyUpdaterEnum.fromValue(dependencyUpdater));
        dto.setDockerCompose(dockerCompose);
        dto.setGithubActions(githubActions);

        return generateProjectResponse(toEffectiveConfig(dto));
    }

    private org.operaton.dev.starter.templates.model.ProjectConfig toEffectiveConfig(ProjectConfig dto) {
        return mapper.toDomain(dto).withGeneratorDefaults(
                properties.defaults().operatonVersion(),
                properties.defaults().mavenRegistry()
        );
    }

    private ResponseEntity<byte[]> generateProjectResponse(
            org.operaton.dev.starter.templates.model.ProjectConfig config
    ) {
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
