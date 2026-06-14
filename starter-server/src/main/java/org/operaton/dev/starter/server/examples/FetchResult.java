package org.operaton.dev.starter.server.examples;

/**
 * Result of fetching a manifest from a GitHub source.
 *
 * @param yamlBytes the raw YAML content as bytes
 * @param resolvedSha the resolved commit SHA (40-character hex string)
 */
public record FetchResult(
        byte[] yamlBytes,
        String resolvedSha
) {}
