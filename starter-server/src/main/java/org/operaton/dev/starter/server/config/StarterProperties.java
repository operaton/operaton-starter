package org.operaton.dev.starter.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Configuration properties for self-hosting defaults and CORS.
 *
 * <p>All values are overridable via environment variables using Spring's relaxed binding:
 * <ul>
 *   <li>{@code STARTER_DEFAULTS_GROUP_ID} → {@code starter.defaults.group-id}</li>
 *   <li>{@code STARTER_DEFAULTS_MAVEN_REGISTRY} → {@code starter.defaults.maven-registry}</li>
 *   <li>{@code STARTER_DEFAULTS_OPERATON_VERSION} → {@code starter.defaults.operaton-version}</li>
 *   <li>{@code STARTER_CORS_ALLOWED_ORIGINS} → {@code starter.cors.allowed-origins}</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "starter")
public record StarterProperties(
        @DefaultValue Defaults defaults,
        @DefaultValue Cors cors
) {

    public record Defaults(
            @DefaultValue("com.example") String groupId,
            @DefaultValue("") String mavenRegistry,
            @DefaultValue("") String operatonVersion
    ) {}

    public record Cors(
            @DefaultValue("https://start.operaton.org") List<String> allowedOrigins
    ) {}
}
