package org.operaton.dev.starter.templates.model;

import java.util.Optional;

/**
 * Immutable value object holding all project generation parameters.
 *
 * <p>This is the single shared domain type used by the generation engine,
 * REST API, CLI, MCP, and all other channels. No channel defines its own
 * parallel representation.
 *
 * <p>Constructed via {@link ProjectConfig.Builder}.
 */
public record ProjectConfig(
        String groupId,
        String artifactId,
        String projectName,
        ProjectType projectType,
        BuildSystem buildSystem,
        int javaVersion,
        Optional<DeploymentTarget> deploymentTarget,
        DependencyUpdater dependencyUpdater,
        boolean dockerCompose,
        boolean githubActions
) {

    /** Default Java version used when not specified by the user. */
    public static final int DEFAULT_JAVA_VERSION = 17;

    /**
     * Derives the Java package from groupId and artifactId.
     * Hyphens in artifactId are replaced with underscores (valid Java identifier).
     */
    public String javaPackage() {
        return groupId + "." + artifactId.replace('-', '_');
    }

    /**
     * Derives the file-system path segment from groupId and artifactId.
     * Example: com.example / my-process → com/example/my_process
     */
    public String packagePath() {
        return javaPackage().replace('.', '/');
    }

    /** Builder for {@link ProjectConfig}. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String groupId;
        private String artifactId;
        private String projectName;
        private ProjectType projectType = ProjectType.PROCESS_APPLICATION;
        private BuildSystem buildSystem = BuildSystem.MAVEN;
        private int javaVersion = DEFAULT_JAVA_VERSION;
        private Optional<DeploymentTarget> deploymentTarget = Optional.empty();
        private DependencyUpdater dependencyUpdater = DependencyUpdater.RENOVATE;
        private boolean dockerCompose = false;
        private boolean githubActions = true;

        private Builder() {}

        public Builder groupId(String groupId) { this.groupId = groupId; return this; }
        public Builder artifactId(String artifactId) { this.artifactId = artifactId; return this; }
        public Builder projectName(String projectName) { this.projectName = projectName; return this; }
        public Builder projectType(ProjectType projectType) { this.projectType = projectType; return this; }
        public Builder buildSystem(BuildSystem buildSystem) { this.buildSystem = buildSystem; return this; }
        public Builder javaVersion(int javaVersion) { this.javaVersion = javaVersion; return this; }
        public Builder deploymentTarget(DeploymentTarget deploymentTarget) {
            this.deploymentTarget = Optional.ofNullable(deploymentTarget);
            return this;
        }
        public Builder dependencyUpdater(DependencyUpdater dependencyUpdater) {
            this.dependencyUpdater = dependencyUpdater;
            return this;
        }
        public Builder dockerCompose(boolean dockerCompose) { this.dockerCompose = dockerCompose; return this; }
        public Builder githubActions(boolean githubActions) { this.githubActions = githubActions; return this; }

        public ProjectConfig build() {
            if (groupId == null || groupId.isBlank()) {
                throw new IllegalStateException("groupId is required");
            }
            if (artifactId == null || artifactId.isBlank()) {
                throw new IllegalStateException("artifactId is required");
            }
            if (projectName == null || projectName.isBlank()) {
                throw new IllegalStateException("projectName is required");
            }
            if (projectType == ProjectType.PROCESS_ARCHIVE && deploymentTarget.isEmpty()) {
                throw new IllegalStateException("deploymentTarget is required for PROCESS_ARCHIVE");
            }
            return new ProjectConfig(
                    groupId, artifactId, projectName,
                    projectType, buildSystem, javaVersion,
                    deploymentTarget, dependencyUpdater,
                    dockerCompose, githubActions
            );
        }
    }
}
