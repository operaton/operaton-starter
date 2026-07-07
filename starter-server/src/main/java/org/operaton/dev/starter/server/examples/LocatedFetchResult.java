package org.operaton.dev.starter.server.examples;

/**
 * Result of fetching a single descriptor file found during repository-wide scanning.
 *
 * @param yamlBytes      the raw YAML content of this descriptor
 * @param resolvedSha    the commit SHA the tree was scanned at (same for all descriptors in one fetch)
 * @param descriptorPath repository-relative path to this descriptor file, e.g., {@code ".operaton-starter.yml"}
 *                       or {@code "examples/foo/.operaton-starter.yml"}
 */
public record LocatedFetchResult(
        byte[] yamlBytes,
        String resolvedSha,
        String descriptorPath
) {}
