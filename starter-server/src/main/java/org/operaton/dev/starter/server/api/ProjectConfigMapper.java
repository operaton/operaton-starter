package org.operaton.dev.starter.server.api;

import org.operaton.dev.starter.server.model.ProjectConfig;
import org.operaton.dev.starter.templates.model.BuildSystem;
import org.operaton.dev.starter.templates.model.DependencyUpdater;
import org.operaton.dev.starter.templates.model.DeploymentTarget;
import org.operaton.dev.starter.templates.model.ProjectType;
import org.springframework.stereotype.Component;

/**
 * Maps the OpenAPI-generated {@link ProjectConfig} DTO to the domain
 * {@link org.operaton.dev.starter.templates.model.ProjectConfig} record.
 *
 * <p>No mapping logic exists in the controller itself.
 */
@Component
public class ProjectConfigMapper {

    public org.operaton.dev.starter.templates.model.ProjectConfig toDomain(ProjectConfig dto) {
        var builder = org.operaton.dev.starter.templates.model.ProjectConfig.builder()
                .groupId(dto.getGroupId())
                .artifactId(dto.getArtifactId())
                .projectName(dto.getProjectName())
                .projectType(mapProjectType(dto.getProjectType()))
                .buildSystem(mapBuildSystem(dto.getBuildSystem()))
                .javaVersion(dto.getJavaVersion() != null ? dto.getJavaVersion().getValue()
                        : org.operaton.dev.starter.templates.model.ProjectConfig.DEFAULT_JAVA_VERSION)
                .dependencyUpdater(mapDependencyUpdater(dto.getDependencyUpdater()))
                .dockerCompose(Boolean.TRUE.equals(dto.getDockerCompose()))
                .githubActions(!Boolean.FALSE.equals(dto.getGithubActions()));

        if (dto.getDeploymentTarget() != null) {
            builder.deploymentTarget(mapDeploymentTarget(dto.getDeploymentTarget()));
        }

        return builder.build();
    }

    private ProjectType mapProjectType(org.operaton.dev.starter.server.model.ProjectConfig.ProjectTypeEnum dto) {
        if (dto == null) return ProjectType.PROCESS_APPLICATION;
        return switch (dto) {
            case APPLICATION -> ProjectType.PROCESS_APPLICATION;
            case ARCHIVE -> ProjectType.PROCESS_ARCHIVE;
        };
    }

    private BuildSystem mapBuildSystem(org.operaton.dev.starter.server.model.ProjectConfig.BuildSystemEnum dto) {
        if (dto == null) return BuildSystem.MAVEN;
        return switch (dto) {
            case MAVEN -> BuildSystem.MAVEN;
            case GRADLE_GROOVY -> BuildSystem.GRADLE_GROOVY;
            case GRADLE_KOTLIN -> BuildSystem.GRADLE_KOTLIN;
        };
    }

    private DeploymentTarget mapDeploymentTarget(
            org.operaton.dev.starter.server.model.ProjectConfig.DeploymentTargetEnum dto) {
        if (dto == null) return null;
        return switch (dto) {
            case TOMCAT -> DeploymentTarget.TOMCAT;
            case STANDALONE_ENGINE -> DeploymentTarget.STANDALONE_ENGINE;
        };
    }

    private DependencyUpdater mapDependencyUpdater(
            org.operaton.dev.starter.server.model.ProjectConfig.DependencyUpdaterEnum dto) {
        if (dto == null) return DependencyUpdater.RENOVATE;
        return switch (dto) {
            case DEPENDABOT -> DependencyUpdater.DEPENDABOT;
            case RENOVATE -> DependencyUpdater.RENOVATE;
        };
    }
}
