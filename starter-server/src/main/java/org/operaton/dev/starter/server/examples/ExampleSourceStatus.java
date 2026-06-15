package org.operaton.dev.starter.server.examples;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Optional;

/**
 * Per-source load status captured during an example refresh operation.
 *
 * <p>Records outcome ("success" or "stale:<reason>"), number of examples loaded,
 * resolved SHA, fetch timestamp, and optional error details.
 *
 * <p>Returned by `POST /api/v1/examples/refresh` and exposed via
 * `GET /actuator/examples` for diagnostics.
 *
 * @param source the source repository token (e.g., "owner/repo" or "owner/repo@ref")
 * @param outcome "success" or "stale:<reason>" (e.g., "stale:timeout", "stale:http-404")
 * @param examplesLoaded number of examples successfully loaded from this source
 * @param resolvedSha the resolved commit SHA (40-character hex) if successful; null if skipped
 * @param lastFetchedAt ISO-8601 timestamp of the most recent fetch attempt
 * @param error optional error message (present when outcome is stale or on failure)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExampleSourceStatus(
        String source,
        String outcome,
        int examplesLoaded,
        String resolvedSha,
        String lastFetchedAt,
        Optional<String> error
) {
    public ExampleSourceStatus {
        if (error == null) {
            error = Optional.empty();
        }
    }
}
