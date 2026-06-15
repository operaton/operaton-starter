package org.operaton.dev.starter.server.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.Example;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Loads example manifests from configured sources in parallel at application startup.
 *
 * <p>Architecture A5: Dispatches one parallel fetch+parse task per configured source.
 * Failures are recorded per-source (skipped:<reason>); app starts successfully even if
 * all sources fail. Results are atomically swapped into {@link ExampleRegistry}.
 *
 * <p>Listens for {@link ApplicationReadyEvent} and runs the load cycle asynchronously.
 */
@Service
public class ExampleRepositoryLoader {
    private static final Logger log = LoggerFactory.getLogger(ExampleRepositoryLoader.class);

    private final GitHubManifestFetcher fetcher;
    private final ExampleManifestParser parser;
    private final ExampleRegistry registry;
    private final StarterProperties properties;

    public ExampleRepositoryLoader(
            GitHubManifestFetcher fetcher,
            ExampleManifestParser parser,
            ExampleRegistry registry,
            StarterProperties properties
    ) {
        this.fetcher = fetcher;
        this.parser = parser;
        this.registry = registry;
        this.properties = properties;
    }

    /**
     * Triggered by ApplicationReadyEvent. Dispatches parallel per-source tasks.
     */
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        load();
    }

    /**
     * Main load cycle. Dispatches one parallel task per configured source,
     * collects results into a snapshot, and atomically stores via registry.swap().
     *
     * <p>On any source failure, records skipped:<reason> and continues.
     *
     * <p>Architecture A5: Failed source on refresh preserves its previous in-memory snapshot.
     * Per-source result merged into fresh snapshot builder; missing/failed entries filled from previous.
     *
     * @return List of ExampleSourceStatus entries describing the load outcome
     */
    public List<ExampleSourceStatus> load() {
        var sources = properties.examples().repositories();
        if (sources.isEmpty()) {
            log.info("No example sources configured");
            registry.swap(ExampleSnapshot.of(List.of()));
            return List.of();
        }

        log.info("Loading examples from {} source(s)", sources.size());
        var startTime = System.currentTimeMillis();

        // Get previous snapshot for preserve-previous-on-failure merge
        ExampleSnapshot previousSnapshot = registry.snapshot();

        // Dispatch one parallel task per source
        List<CompletableFuture<LoadSourceResult>> futures = new ArrayList<>();
        for (String sourceToken : sources) {
            var future = CompletableFuture.supplyAsync(() -> loadSourceWithStatus(sourceToken));
            futures.add(future);
        }

        // Wait for all futures to complete (ignore exceptions)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect results
        List<ExampleSnapshot.SourceState> sourceStates = new ArrayList<>();
        List<ExampleSourceStatus> statuses = new ArrayList<>();
        int successCount = 0;
        int exampleCount = 0;
        List<String> skippedReasons = new ArrayList<>();

        for (CompletableFuture<LoadSourceResult> future : futures) {
            try {
                var result = future.join();
                ExampleSourceStatus status = result.status();
                statuses.add(status);

                // Add source state, applying preserve-previous-on-failure
                ExampleSnapshot.SourceState sourceState = result.sourceState();
                if (!sourceState.outcome().equals("success")) {
                    // Try to find and preserve previous state for this source
                    ExampleSnapshot.SourceState previousState = findPreviousSourceState(previousSnapshot, sourceState.source());
                    if (previousState != null) {
                        log.debug("Preserving previous snapshot for failed source {}", sourceState.source());
                        sourceState = new ExampleSnapshot.SourceState(
                                sourceState.source(),
                                "stale:" + extractReasonFromOutcome(sourceState.outcome()),
                                previousState.examples(),
                                previousState.resolvedSha(),
                                Instant.now().toString()
                        );
                    }
                }
                sourceStates.add(sourceState);

                if (sourceState.outcome().equals("success")) {
                    successCount++;
                    exampleCount += sourceState.examples().size();
                } else {
                    skippedReasons.add(sourceState.source() + ": " + sourceState.outcome());
                }
            } catch (Exception e) {
                log.warn("Unexpected exception while collecting source results", e);
            }
        }

        // Atomic swap
        var snapshot = new ExampleSnapshot(sourceStates, statuses);
        registry.swap(snapshot);

        var elapsedMs = System.currentTimeMillis() - startTime;
        log.info("Examples loaded: {} sources, {} examples, {} skipped, {}ms",
                successCount, exampleCount, sources.size() - successCount, elapsedMs);

        if (!skippedReasons.isEmpty()) {
            log.warn("Skipped sources: {}", String.join("; ", skippedReasons));
        }

        return statuses;
    }

    /**
     * Loads a single source and captures status. Returns LoadSourceResult with both state and status.
     * Never throws — always returns a result (success or skipped).
     */
    private LoadSourceResult loadSourceWithStatus(String sourceToken) {
        try {
            // Step 1: Fetch manifest
            FetchResult fetchResult;
            try {
                fetchResult = fetcher.fetch(sourceToken);
            } catch (SourceUnavailable e) {
                var outcome = "skipped:" + e.getReason();
                var sourceState = new ExampleSnapshot.SourceState(
                        sourceToken,
                        outcome,
                        List.of(),
                        null,
                        Instant.now().toString()
                );
                var status = new ExampleSourceStatus(
                        sourceToken,
                        outcome,
                        0,
                        null,
                        Instant.now().toString(),
                        java.util.Optional.of(e.getReason())
                );
                return new LoadSourceResult(sourceState, status);
            }

            // Step 2: Parse manifest
            ParsedManifest parsedManifest;
            try {
                parsedManifest = parser.parse(
                        fetchResult.yamlBytes(),
                        extractRepoFromToken(sourceToken),
                        fetchResult.resolvedSha()
                );
            } catch (ManifestRejected e) {
                var outcome = "skipped:" + e.getReason();
                var sourceState = new ExampleSnapshot.SourceState(
                        sourceToken,
                        outcome,
                        List.of(),
                        null,
                        Instant.now().toString()
                );
                var status = new ExampleSourceStatus(
                        sourceToken,
                        outcome,
                        0,
                        null,
                        Instant.now().toString(),
                        java.util.Optional.of(e.getReason())
                );
                return new LoadSourceResult(sourceState, status);
            }

            // Step 3: Convert to Example model and annotate with source info
            List<Example> examples = new ArrayList<>();
            for (ParsedManifest.Example parsedEx : parsedManifest.examples()) {
                var example = new Example()
                        .id(parsedEx.name())
                        .title(parsedEx.name())
                        .path(parsedEx.path())
                        .shortDescription(parsedEx.description())
                        .sourceRepo(parsedManifest.sourceRepo())
                        .sourceRepoSha(parsedManifest.sourceRepoSha())
                        .sourceRepoUrl(buildSourceRepoUrl(parsedManifest.sourceRepo(), parsedManifest.sourceRepoSha(), parsedEx.path()));
                examples.add(example);
            }

            var sourceState = new ExampleSnapshot.SourceState(
                    sourceToken,
                    "success",
                    examples,
                    parsedManifest.sourceRepoSha(),
                    Instant.now().toString()
            );
            var status = new ExampleSourceStatus(
                    sourceToken,
                    "success",
                    examples.size(),
                    parsedManifest.sourceRepoSha(),
                    Instant.now().toString(),
                    java.util.Optional.empty()
            );
            return new LoadSourceResult(sourceState, status);

        } catch (Exception e) {
            log.warn("Unexpected exception loading source {}", sourceToken, e);
            var outcome = "skipped:error";
            var sourceState = new ExampleSnapshot.SourceState(
                    sourceToken,
                    outcome,
                    List.of(),
                    null,
                    Instant.now().toString()
            );
            var status = new ExampleSourceStatus(
                    sourceToken,
                    outcome,
                    0,
                    null,
                    Instant.now().toString(),
                    java.util.Optional.of("Unexpected error: " + e.getMessage())
            );
            return new LoadSourceResult(sourceState, status);
        }
    }

    /**
     * Finds the previous SourceState for a given source token from the prior snapshot.
     * Used to implement preserve-previous-on-failure.
     *
     * @param previousSnapshot the previous snapshot (may be empty)
     * @param sourceToken the source token to find
     * @return the previous SourceState, or null if not found
     */
    private ExampleSnapshot.SourceState findPreviousSourceState(ExampleSnapshot previousSnapshot, String sourceToken) {
        for (ExampleSnapshot.SourceState sourceState : previousSnapshot.sources()) {
            if (sourceState.source().equals(sourceToken)) {
                return sourceState;
            }
        }
        return null;
    }

    /**
     * Extracts the reason part from an outcome string.
     * E.g., "skipped:timeout" -> "timeout"
     *
     * @param outcome the outcome string
     * @return the reason part
     */
    private String extractReasonFromOutcome(String outcome) {
        int colonIndex = outcome.indexOf(':');
        if (colonIndex > 0 && colonIndex < outcome.length() - 1) {
            return outcome.substring(colonIndex + 1);
        }
        return outcome;
    }

    /**
     * Extracts the owner/repo portion from a source token (e.g., "owner/repo@ref" -> "owner/repo").
     */
    private String extractRepoFromToken(String sourceToken) {
        int atIndex = sourceToken.indexOf('@');
        if (atIndex > 0) {
            return sourceToken.substring(0, atIndex);
        }
        return sourceToken;
    }

    /**
     * Builds the GitHub HTML URL to the example folder at the pinned SHA.
     * Format: https://github.com/{owner}/{repo}/tree/{sha}/{examplePath}
     */
    private String buildSourceRepoUrl(String sourceRepo, String sha, String examplePath) {
        return "https://github.com/" + sourceRepo + "/tree/" + sha + "/" + examplePath;
    }

    /**
     * Internal record to carry both SourceState and ExampleSourceStatus from loadSourceWithStatus.
     */
    private record LoadSourceResult(
            ExampleSnapshot.SourceState sourceState,
            ExampleSourceStatus status
    ) {}
}
