package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.Example;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExampleRepositoryLoaderTest {

    @Mock private GitHubManifestFetcher fetcher;
    @Mock private ExampleManifestParser parser;
    @Mock private ExampleRegistry registry;
    @Mock private StarterProperties properties;

    @InjectMocks
    private ExampleRepositoryLoader loader;

    private static ParsedManifest.Example parsedEx(String id, String path) {
        return new ParsedManifest.Example(id, "Title " + id, "Desc " + id, path,
                List.of(), null, null, null, null, null, null, null,
                List.of(), List.of(), null, List.of(), null, null, null, List.of(), null);
    }

    @Test
    void loader_handles_single_root_descriptor() throws Exception {
        var sourceToken = "owner/repo";
        var located = new LocatedFetchResult("yaml".getBytes(), "sha1", ".operaton-starter.yml");
        var manifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "examples/foo")), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located));
        when(parser.parse(any(), eq("owner/repo"), eq("sha1"))).thenReturn(manifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            // Root descriptor: path relative to root stays unchanged
            return examples.size() == 1 && "examples/foo".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_resolves_path_relative_to_descriptor_directory() throws Exception {
        var sourceToken = "owner/repo";
        var located = new LocatedFetchResult("yaml".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        // path: "bar" in a descriptor at "examples/foo/" → resolved "examples/foo/bar"
        var manifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "bar")), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located));
        when(parser.parse(any(), eq("owner/repo"), eq("sha1"))).thenReturn(manifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 1 && "examples/foo/bar".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_defaults_null_path_to_descriptor_directory() throws Exception {
        var sourceToken = "owner/repo";
        var located = new LocatedFetchResult("yaml".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        // path: null → resolved to "examples/foo"
        var manifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located));
        when(parser.parse(any(), eq("owner/repo"), eq("sha1"))).thenReturn(manifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 1 && "examples/foo".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_merges_examples_from_multiple_descriptors() throws Exception {
        var sourceToken = "owner/repo";
        var located1 = new LocatedFetchResult("yaml1".getBytes(), "sha1", ".operaton-starter.yml");
        var located2 = new LocatedFetchResult("yaml2".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        var manifest1 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "root-path")), "owner/repo", "sha1");
        var manifest2 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex2", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located1, located2));
        when(parser.parse(same(located1.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest1);
        when(parser.parse(same(located2.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest2);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 2;
        }));
    }

    @Test
    void loader_skips_duplicate_ids_across_descriptors() throws Exception {
        var sourceToken = "owner/repo";
        var located1 = new LocatedFetchResult("yaml1".getBytes(), "sha1", ".operaton-starter.yml");
        var located2 = new LocatedFetchResult("yaml2".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        // Both descriptors declare the same id "ex1"
        var manifest1 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", "path1")), "owner/repo", "sha1");
        var manifest2 = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located1, located2));
        when(parser.parse(same(located1.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest1);
        when(parser.parse(same(located2.yamlBytes()), eq("owner/repo"), eq("sha1"))).thenReturn(manifest2);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            // Duplicate id skipped: only one example survives
            return examples.size() == 1 && "path1".equals(examples.get(0).getPath());
        }));
    }

    @Test
    void loader_skips_on_fetch_failure() throws Exception {
        var sourceToken = "owner/repo";

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenThrow(new SourceUnavailable("timeout"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot ->
                snapshot.sources().stream().anyMatch(s -> s.outcome().contains("skipped"))));
    }

    @Test
    void loader_skips_source_when_no_descriptors_found() throws Exception {
        var sourceToken = "owner/repo";

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of());
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot ->
                snapshot.sources().stream().anyMatch(s -> s.outcome().contains("no-descriptors"))));
    }

    @Test
    void loader_isolates_per_descriptor_parse_failure() throws Exception {
        var sourceToken = "owner/repo";
        var located1 = new LocatedFetchResult("bad".getBytes(), "sha1", ".operaton-starter.yml");
        var located2 = new LocatedFetchResult("good".getBytes(), "sha1",
                "examples/foo/.operaton-starter.yml");
        var goodManifest = new ParsedManifest("operaton-starter/v1",
                List.of(parsedEx("ex1", null)), "owner/repo", "sha1");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(sourceToken), null, 50));
        when(fetcher.fetch(sourceToken)).thenReturn(List.of(located1, located2));
        when(parser.parse(same(located1.yamlBytes()), any(), any()))
                .thenThrow(new ManifestRejected("malformed-yaml"));
        when(parser.parse(same(located2.yamlBytes()), any(), any())).thenReturn(goodManifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        // Second descriptor still contributes its example despite first failing
        verify(registry).swap(argThat(snapshot -> {
            List<Example> examples = snapshot.sources().get(0).examples();
            return examples.size() == 1;
        }));
    }

    @Test
    void loader_handles_multiple_sources_in_parallel() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(sources, null, 50));
        when(fetcher.fetch(anyString())).thenReturn(List.of(
                new LocatedFetchResult("yaml".getBytes(), "sha1", ".operaton-starter.yml")));
        when(parser.parse(any(), anyString(), anyString())).thenReturn(
                new ParsedManifest("operaton-starter/v1", List.of(), "owner/repo1", "sha1"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(any());
    }

    @Test
    void loader_app_starts_even_if_all_sources_fail() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2");

        when(properties.examples()).thenReturn(new StarterProperties.Examples(sources, null, 50));
        when(fetcher.fetch(anyString())).thenThrow(new SourceUnavailable("timeout"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot ->
                snapshot.sources().size() == 2 &&
                snapshot.sources().stream().allMatch(s -> s.outcome().contains("skipped"))));
    }

    @Test
    void loader_handles_empty_source_list() throws Exception {
        when(properties.examples()).thenReturn(new StarterProperties.Examples(List.of(), null, 50));

        loader.onApplicationReady(mock(ApplicationReadyEvent.class));

        verify(registry).swap(argThat(snapshot -> snapshot.sources().isEmpty()));
    }
}
