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
     */
    public void load() {
        var sources = properties.examples().repositories();
        if (sources.isEmpty()) {
            log.info("No example sources configured");
            registry.swap(new ExampleSnapshot(List.of()));
            return;
        }

        log.info("Loading examples from {} source(s)", sources.size());
        var startTime = System.currentTimeMillis();

        // Dispatch one parallel task per source
        List<CompletableFuture<ExampleSnapshot.SourceState>> futures = new ArrayList<>();
        for (String sourceToken : sources) {
            var future = CompletableFuture.supplyAsync(() -> loadSource(sourceToken));
            futures.add(future);
        }

        // Wait for all futures to complete (ignore exceptions)
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Collect results
        List<ExampleSnapshot.SourceState> sourceStates = new ArrayList<>();
        int successCount = 0;
        int exampleCount = 0;
        List<String> skippedReasons = new ArrayList<>();

        for (CompletableFuture<ExampleSnapshot.SourceState> future : futures) {
            try {
                var sourceState = future.join();
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
        var snapshot = new ExampleSnapshot(sourceStates);
        registry.swap(snapshot);

        var elapsedMs = System.currentTimeMillis() - startTime;
        log.info("Examples loaded: {} sources, {} examples, {} skipped, {}ms",
                successCount, exampleCount, sources.size() - successCount, elapsedMs);

        if (!skippedReasons.isEmpty()) {
            log.warn("Skipped sources: {}", String.join("; ", skippedReasons));
        }
    }

    /**
     * Loads a single source. Returns a SourceState with outcome and examples.
     * Never throws — always returns a state (success or skipped).
     */
    private ExampleSnapshot.SourceState loadSource(String sourceToken) {
        try {
            // Step 1: Fetch manifest
            FetchResult fetchResult;
            try {
                fetchResult = fetcher.fetch(sourceToken);
            } catch (SourceUnavailable e) {
                return new ExampleSnapshot.SourceState(
                        sourceToken,
                        "skipped:" + e.getReason(),
                        List.of(),
                        null,
                        Instant.now().toString()
                );
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
                return new ExampleSnapshot.SourceState(
                        sourceToken,
                        "skipped:" + e.getReason(),
                        List.of(),
                        null,
                        Instant.now().toString()
                );
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

            return new ExampleSnapshot.SourceState(
                    sourceToken,
                    "success",
                    examples,
                    parsedManifest.sourceRepoSha(),
                    Instant.now().toString()
            );

        } catch (Exception e) {
            log.warn("Unexpected exception loading source {}", sourceToken, e);
            return new ExampleSnapshot.SourceState(
                    sourceToken,
                    "skipped:error",
                    List.of(),
                    null,
                    Instant.now().toString()
            );
        }
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
}
