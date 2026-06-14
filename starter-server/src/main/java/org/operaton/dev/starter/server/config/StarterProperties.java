package org.operaton.dev.starter.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import jakarta.annotation.PostConstruct;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Configuration properties for self-hosting defaults, CORS, and examples.
 *
 * <p>All values are overridable via environment variables using Spring's relaxed binding:
 * <ul>
 *   <li>{@code STARTER_DEFAULTS_GROUP_ID} → {@code starter.defaults.group-id}</li>
 *   <li>{@code STARTER_DEFAULTS_MAVEN_REGISTRY} → {@code starter.defaults.maven-registry}</li>
 *   <li>{@code STARTER_DEFAULTS_OPERATON_VERSION} → {@code starter.defaults.operaton-version}</li>
 *   <li>{@code STARTER_CORS_ALLOWED_ORIGINS} → {@code starter.cors.allowed-origins}</li>
 *   <li>{@code STARTER_EXAMPLES_REPOSITORIES} → {@code starter.examples.repositories}</li>
 *   <li>{@code STARTER_EXAMPLES_CACHE_DIR} → {@code starter.examples.cache.dir}</li>
 *   <li>{@code STARTER_EXAMPLES_CACHE_MAXSIZEMB} → {@code starter.examples.cache.max-size-mb}</li>
 *   <li>{@code STARTER_EXAMPLES_MAXDOWNLOADSIZEMB} → {@code starter.examples.max-download-size-mb}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "starter")
public record StarterProperties(
        @DefaultValue Defaults defaults,
        @DefaultValue Cors cors,
        @DefaultValue Examples examples
) {
    private static final Logger log = LoggerFactory.getLogger(StarterProperties.class);
    private static final Pattern SOURCE_TOKEN_REGEX = Pattern.compile(
            "^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(@[A-Za-z0-9._/-]+)?$"
    );

    @PostConstruct
    public void validate() {
        if (examples != null && examples.repositories != null) {
            examples.repositories.removeIf(token -> {
                if (!SOURCE_TOKEN_REGEX.matcher(token).matches()) {
                    log.warn("Invalid source token dropped from starter.examples.repositories: '{}'. " +
                            "Token must match pattern: ^[A-Za-z0-9._-]+/[A-Za-z0-9._-]+(@[A-Za-z0-9._/-]+)?$",
                            token);
                    return true;
                }
                return false;
            });
        }
    }

    public record Defaults(
            @DefaultValue("org.operaton.example") String groupId,
            @DefaultValue("") String mavenRegistry,
            @DefaultValue("") String operatonVersion
    ) {}

    public record Cors(
            @DefaultValue("https://start.operaton.org") List<String> allowedOrigins
    ) {}

    public record Examples(
            @DefaultValue("kthoms/operaton-examples") List<String> repositories,
            @DefaultValue Cache cache,
            @DefaultValue("50") long maxDownloadSizeMb
    ) {
        public record Cache(
                Path dir,
                @DefaultValue("512") long maxSizeMb
        ) {
            public Cache(Path dir, long maxSizeMb) {
                this.dir = dir != null ? dir : Paths.get(
                        System.getProperty("java.io.tmpdir"),
                        "operaton-starter",
                        "examples-cache"
                );
                this.maxSizeMb = maxSizeMb;
            }
        }
    }
}
