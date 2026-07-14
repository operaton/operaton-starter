package org.operaton.dev.starter.templates.model;

import java.util.Optional;

/**
 * Immutable value object holding all project generation parameters.
 *
 * <p>This is the single shared domain type used by the generation engine,
 * REST API, CLI, and all other channels. No channel defines its own
 * parallel representation.
 *
 * <p>Constructed via {@link ProjectConfig.Builder}.
 */
public record ProjectConfig(
        String groupId,
        String artifactId,
        String projectName,
        String version,
        ProjectType projectType,
        BuildSystem buildSystem,
        int javaVersion,
        Optional<DeploymentTarget> deploymentTarget,
        DependencyUpdater dependencyUpdater,
        boolean dockerCompose,
        boolean githubActions,
        String operatonVersionOverride,
        String mavenRegistryUrl,
        int serverPort,
        String useCaseId
) {

    public static final String DEFAULT_PROJECT_VERSION = "1.0.0-SNAPSHOT";

    /** Default Java version used when not specified by the user. */
    public static final int DEFAULT_JAVA_VERSION = 17;

    /**
     * Derives the Java package from groupId and artifactId.
     * Hyphens in artifactId are removed (not valid in Java identifiers).
     * Example: com.example / my-process-app → com.example.myprocessapp
     */
    public String javaPackage() {
        return groupId + "." + artifactId.replace("-", "");
    }

    /**
     * Derives the file-system path segment from groupId and artifactId.
     * Example: com.example / my-process-app → com/example/myprocessapp
     */
    public String packagePath() {
        return javaPackage().replace('.', '/');
    }

    /**
     * Returns the effective Operaton version for generated build files.
     * Self-hosted instances can override the default stable version.
     */
    public String effectiveOperatonVersion() {
        return operatonVersionOverride == null || operatonVersionOverride.isBlank()
                ? VersionConstants.OPERATON_VERSION
                : operatonVersionOverride;
    }

    /**
     * Returns whether generated build files should point at a custom Maven registry.
     */
    public boolean hasCustomMavenRegistry() {
        return mavenRegistryUrl != null && !mavenRegistryUrl.isBlank();
    }

    /**
     * Applies self-hosted generation defaults without altering user-facing project inputs.
     */
    public ProjectConfig withGeneratorDefaults(String operatonVersionOverride, String mavenRegistryUrl) {
        return new ProjectConfig(
                groupId,
                artifactId,
                projectName,
                version,
                projectType,
                buildSystem,
                javaVersion,
                deploymentTarget,
                dependencyUpdater,
                dockerCompose,
                githubActions,
                normalizeBlank(operatonVersionOverride),
                normalizeBlank(mavenRegistryUrl),
                serverPort,
                useCaseId
        );
    }

    /**
     * Applies server-side defaults for a named use case example so generation stays consistent
     * even when callers omit or override incompatible fields.
     */
    public ProjectConfig withUseCaseDefaults(ProjectType projectType, BuildSystem buildSystem, boolean dockerCompose) {
        return new ProjectConfig(
                groupId,
                artifactId,
                projectName,
                version,
                projectType,
                buildSystem,
                javaVersion,
                deploymentTarget,
                dependencyUpdater,
                dockerCompose,
                githubActions,
                operatonVersionOverride,
                mavenRegistryUrl,
                serverPort,
                useCaseId
        );
    }

    /** Builder for {@link ProjectConfig}. */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String groupId;
        private String artifactId;
        private String projectName;
        private String version = DEFAULT_PROJECT_VERSION;
        private ProjectType projectType = ProjectType.PROCESS_APPLICATION;
        private BuildSystem buildSystem = BuildSystem.MAVEN;
        private int javaVersion = DEFAULT_JAVA_VERSION;
        private Optional<DeploymentTarget> deploymentTarget = Optional.empty();
        private DependencyUpdater dependencyUpdater = DependencyUpdater.NONE;
        private boolean dockerCompose = false;
        private boolean githubActions = true;
        private String operatonVersionOverride = "";
        private String mavenRegistryUrl = "";
        private int serverPort = 8080;
        private String useCaseId = null;

        private Builder() {}

        public Builder groupId(String groupId) { this.groupId = groupId; return this; }
        public Builder artifactId(String artifactId) { this.artifactId = artifactId; return this; }
        public Builder projectName(String projectName) { this.projectName = projectName; return this; }
        public Builder version(String version) { this.version = version; return this; }
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
        public Builder operatonVersionOverride(String operatonVersionOverride) {
            this.operatonVersionOverride = operatonVersionOverride;
            return this;
        }
        public Builder mavenRegistryUrl(String mavenRegistryUrl) {
            this.mavenRegistryUrl = mavenRegistryUrl;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder useCaseId(String useCaseId) {
            this.useCaseId = useCaseId;
            return this;
        }

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
                    version == null || version.isBlank() ? DEFAULT_PROJECT_VERSION : version,
                    projectType, buildSystem, javaVersion,
                    deploymentTarget, dependencyUpdater,
                    dockerCompose, githubActions,
                    normalizeBlank(operatonVersionOverride),
                    normalizeBlank(mavenRegistryUrl),
                    serverPort,
                    useCaseId
            );
        }
    }

    private static String normalizeBlank(String value) {
        return value == null ? "" : value.trim();
    }
}
