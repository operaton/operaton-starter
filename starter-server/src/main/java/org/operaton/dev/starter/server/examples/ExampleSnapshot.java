package org.operaton.dev.starter.server.examples;

import org.operaton.dev.starter.server.model.Example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable snapshot of examples loaded from all configured sources.
 *
 * <p>Each source may succeed (examples loaded) or fail (outcome like "skipped:timeout").
 * The snapshot captures per-source state: source token, outcome, examples list,
 * resolved SHA, and last fetch timestamp.
 *
 * <p>Thread-safe: once created, snapshots are never mutated. New snapshots are created
 * and swapped atomically via {@link ExampleRegistry}.
 */
public record ExampleSnapshot(
        List<SourceState> sources
) {
    public ExampleSnapshot {
        // Defensive copy and unmodifiable list
        sources = Collections.unmodifiableList(new ArrayList<>(sources));
    }

    /**
     * Per-source state captured in the snapshot.
     *
     * @param source the source repository token (e.g., "owner/repo" or "owner/repo@ref")
     * @param outcome "success" or "skipped:<reason>" (e.g., "skipped:timeout", "skipped:http-404")
     * @param examples list of loaded examples (empty if source failed)
     * @param resolvedSha the resolved commit SHA if successful; null if skipped
     * @param lastFetchedAt ISO-8601 timestamp of the fetch attempt
     */
    public record SourceState(
            String source,
            String outcome,
            List<Example> examples,
            String resolvedSha,
            String lastFetchedAt
    ) {
        public SourceState {
            // Defensive copy and unmodifiable list
            examples = Collections.unmodifiableList(new ArrayList<>(examples));
        }
    }

    /**
     * Returns all examples from all sources in a single flat list.
     * Useful for the metadata response.
     */
    public List<Example> allExamples() {
        List<Example> all = new ArrayList<>();
        for (SourceState source : sources) {
            all.addAll(source.examples());
        }
        return all;
    }
}
