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
    /**
     * Represents a tag with label and category from the manifest.
     */
    public record Tag(
            String label,
            String category
    ) {}

    public record Example(
            String id,
            String title,
            String shortDescription,
            String path,
            List<Tag> tags,
            String icon,
            String longDescription,
            String buildSystem,
            String runtime,
            String operatonVersion,
            String javaVersion,
            String complexity,
            List<String> integrations,
            List<String> bpmnConcepts,
            String requires,
            List<Author> authors,
            String license,
            String documentationUrl,
            String demoVideoUrl,
            List<String> screenshots,
            String lastUpdated
    ) {
        /**
         * Represents an author of an example.
         */
        public record Author(
                String name,
                String url
        ) {}
    }
}
