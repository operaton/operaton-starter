package org.operaton.dev.starter.server.examples;

import java.util.List;

/**
 * Represents a parsed manifest from an examples repository.
 *
 * <p>Includes computed fields {@code sourceRepo} and {@code sourceRepoSha} that are
 * derived from the parser's input parameters.
 */
public record ParsedManifest(
        String apiVersion,
        List<Example> examples,
        String sourceRepo,
        String sourceRepoSha
) {

    /**
     * Represents a single example within a manifest.
     */
    public record Example(
            String id,
            String title,
            String shortDescription,
            String path,
            List<String> tags
    ) {}
}
