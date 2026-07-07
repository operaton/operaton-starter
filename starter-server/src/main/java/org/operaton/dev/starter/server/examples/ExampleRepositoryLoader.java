package org.operaton.dev.starter.server.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.Author;
import org.operaton.dev.starter.server.model.Example;
import org.operaton.dev.starter.server.model.Tag;
import org.operaton.dev.starter.server.model.TagCategory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
                    var sourceToken = sourceState.source();
                    var previousState = previousSnapshot.sources().stream()
                            .filter(s -> s.source().equals(sourceToken))
                            .findFirst().orElse(null);
                    if (previousState != null) {
                        log.debug("Preserving previous snapshot for failed source {}", sourceState.source());
                        var outcome = sourceState.outcome();
                        var reason = outcome.contains(":") ? outcome.substring(outcome.indexOf(':') + 1) : outcome;
                        sourceState = new ExampleSnapshot.SourceState(
                                sourceState.source(),
                                "stale:" + reason,
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

    private LoadSourceResult failedResult(String sourceToken, String outcome, String reason) {
        var now = Instant.now().toString();
        return new LoadSourceResult(
                new ExampleSnapshot.SourceState(sourceToken, outcome, List.of(), null, now),
                new ExampleSourceStatus(sourceToken, outcome, 0, null, now, java.util.Optional.of(reason))
        );
    }

    /**
     * Loads a single source and captures status. Returns LoadSourceResult with both state and status.
     * Never throws — always returns a result (success or skipped).
     *
     * <p>Iterates all {@link LocatedFetchResult}s returned by the fetcher (one per descriptor file
     * found in the repository). Each descriptor is parsed independently; parse failures are logged
     * and skipped without aborting the remaining descriptors. Duplicate example IDs across
     * descriptors are also logged and skipped (first occurrence wins).
     */
    private LoadSourceResult loadSourceWithStatus(String sourceToken) {
        try {
            // Step 1: Fetch all descriptor locations
            List<LocatedFetchResult> locatedResults;
            try {
                locatedResults = fetcher.fetch(sourceToken);
            } catch (SourceUnavailable e) {
                return failedResult(sourceToken, "skipped:" + e.getReason(), e.getReason());
            }

            if (locatedResults.isEmpty()) {
                return failedResult(sourceToken, "skipped:no-descriptors", "no-descriptors");
            }

            String repo = sourceToken.contains("@")
                    ? sourceToken.substring(0, sourceToken.indexOf('@')) : sourceToken;
            String resolvedSha = locatedResults.get(0).resolvedSha();

            // Step 2: Parse each descriptor, resolve paths, collect examples
            List<Example> allExamples = new ArrayList<>();
            Set<String> seenIds = new LinkedHashSet<>();

            for (LocatedFetchResult located : locatedResults) {
                ParsedManifest parsedManifest;
                try {
                    parsedManifest = parser.parse(located.yamlBytes(), repo, located.resolvedSha());
                } catch (ManifestRejected e) {
                    log.warn("Skipping descriptor '{}' in source {}: {}", located.descriptorPath(), sourceToken, e.getReason());
                    continue;
                }

                String descriptorDir = descriptorDir(located.descriptorPath());

                for (ParsedManifest.Example parsedEx : parsedManifest.examples()) {
                    if (seenIds.contains(parsedEx.id())) {
                        log.warn("Duplicate example id '{}' in descriptor '{}' of source {}; skipping",
                                parsedEx.id(), located.descriptorPath(), sourceToken);
                        continue;
                    }
                    seenIds.add(parsedEx.id());

                    String resolvedPath = resolveExamplePath(descriptorDir, parsedEx.path());

                    var example = new Example()
                            .id(parsedEx.id())
                            .title(parsedEx.title())
                            .path(resolvedPath)
                            .shortDescription(parsedEx.shortDescription())
                            .sourceRepo(parsedManifest.sourceRepo())
                            .sourceRepoSha(parsedManifest.sourceRepoSha())
                            .sourceRepoUrl("https://github.com/" + parsedManifest.sourceRepo()
                                    + "/tree/" + parsedManifest.sourceRepoSha() + "/" + resolvedPath)
                            .icon(parsedEx.icon())
                            .longDescription(parsedEx.longDescription())
                            .operatonVersion(parsedEx.operatonVersion())
                            .javaVersion(parsedEx.javaVersion())
                            .integrations(parsedEx.integrations().isEmpty() ? null : parsedEx.integrations())
                            .bpmnConcepts(parsedEx.bpmnConcepts().isEmpty() ? null : parsedEx.bpmnConcepts())
                            .requires(parsedEx.requires())
                            .license(parsedEx.license())
                            .documentationUrl(parsedEx.documentationUrl())
                            .demoVideoUrl(parsedEx.demoVideoUrl())
                            .screenshots(parsedEx.screenshots().isEmpty() ? null : parsedEx.screenshots());

                    if (parsedEx.buildSystem() != null) {
                        try { example.buildSystem(Example.BuildSystemEnum.fromValue(parsedEx.buildSystem())); }
                        catch (IllegalArgumentException e) { log.debug("Unknown buildSystem value: {}", parsedEx.buildSystem()); }
                    }
                    if (parsedEx.runtime() != null) {
                        try { example.runtime(Example.RuntimeEnum.fromValue(parsedEx.runtime())); }
                        catch (IllegalArgumentException e) { log.debug("Unknown runtime value: {}", parsedEx.runtime()); }
                    }
                    if (parsedEx.complexity() != null) {
                        try { example.complexity(Example.ComplexityEnum.fromValue(parsedEx.complexity())); }
                        catch (IllegalArgumentException e) { log.debug("Unknown complexity value: {}", parsedEx.complexity()); }
                    }
                    if (parsedEx.lastUpdated() != null) {
                        try { example.lastUpdated(java.time.LocalDate.parse(parsedEx.lastUpdated())); }
                        catch (Exception e) { log.debug("Could not parse lastUpdated date: {}", parsedEx.lastUpdated()); }
                    }
                    if (!parsedEx.tags().isEmpty()) {
                        List<Tag> modelTags = parsedEx.tags().stream()
                                .map(t -> new Tag().label(t.label()).category(mapTagCategory(t.category())))
                                .toList();
                        example.tags(modelTags);
                    }
                    if (!parsedEx.authors().isEmpty()) {
                        List<Author> modelAuthors = parsedEx.authors().stream()
                                .map(a -> new Author().name(a.name()).url(a.url()))
                                .toList();
                        example.authors(modelAuthors);
                    }

                    allExamples.add(example);
                }
            }

            var sourceState = new ExampleSnapshot.SourceState(
                    sourceToken, "success", allExamples, resolvedSha, Instant.now().toString());
            var status = new ExampleSourceStatus(
                    sourceToken, "success", allExamples.size(), resolvedSha, Instant.now().toString(),
                    java.util.Optional.empty());
            return new LoadSourceResult(sourceState, status);

        } catch (Exception e) {
            log.warn("Unexpected exception loading source {}", sourceToken, e);
            return failedResult(sourceToken, "skipped:error", "Unexpected error: " + e.getMessage());
        }
    }

    /** Returns the directory containing a descriptor, e.g., "examples/foo" for "examples/foo/.operaton-starter.yml". */
    private static String descriptorDir(String descriptorPath) {
        int slash = descriptorPath.lastIndexOf('/');
        return slash < 0 ? "" : descriptorPath.substring(0, slash);
    }

    /**
     * Resolves an example's path relative to its descriptor's directory.
     *
     * <p>Resolution rules:
     * <ul>
     *   <li>null or empty path → defaults to "." → resolves to the descriptor dir (or "." for root)</li>
     *   <li>"." → same as above</li>
     *   <li>non-empty path → prepend descriptor dir (empty dir leaves path unchanged for backward compat)</li>
     * </ul>
     */
    static String resolveExamplePath(String descriptorDir, String examplePath) {
        String effective = (examplePath == null || examplePath.isEmpty()) ? "." : examplePath;
        if (effective.equals(".")) {
            return descriptorDir.isEmpty() ? "." : descriptorDir;
        }
        return descriptorDir.isEmpty() ? effective : descriptorDir + "/" + effective;
    }

    /**
     * Maps a manifest tag category string (lowercase) to a TagCategory enum value.
     * Unknown categories fall back to TECHNOLOGY.
     */
    private TagCategory mapTagCategory(String category) {
        if (category == null) {
            return TagCategory.TECHNOLOGY;
        }
        return switch (category.toLowerCase()) {
            case "concept", "bpmn_concept", "bpmn-concept" -> TagCategory.BPMN_CONCEPT;
            case "runtime" -> TagCategory.RUNTIME;
            case "integration", "technology" -> TagCategory.TECHNOLOGY;
            case "platform" -> TagCategory.PLATFORM;
            case "standard" -> TagCategory.STANDARD;
            case "build_system", "build-system", "buildsystem" -> TagCategory.BUILD_SYSTEM;
            case "complexity" -> TagCategory.COMPLEXITY;
            default -> TagCategory.TECHNOLOGY;
        };
    }

    /**
     * Internal record to carry both SourceState and ExampleSourceStatus from loadSourceWithStatus.
     */
    private record LoadSourceResult(
            ExampleSnapshot.SourceState sourceState,
            ExampleSourceStatus status
    ) {}
}
