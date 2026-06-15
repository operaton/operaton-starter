package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.operaton.dev.starter.server.config.StarterProperties;
import org.operaton.dev.starter.server.model.Example;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ExampleRepositoryLoader startup loading behavior.
 */
@ExtendWith(MockitoExtension.class)
class ExampleRepositoryLoaderTest {

    @Mock
    private GitHubManifestFetcher fetcher;

    @Mock
    private ExampleManifestParser parser;

    @Mock
    private ExampleRegistry registry;

    @Mock
    private StarterProperties properties;

    @InjectMocks
    private ExampleRepositoryLoader loader;

    @Test
    void loader_handles_single_source() throws Exception {
        var sourceToken = "owner/repo";
        var example = new Example().id("ex1").title("Ex1").path("path1").shortDescription("Desc1");
        var parsedManifest = new ParsedManifest("operaton-starter/v1", List.of(
                new ParsedManifest.Example("ex1", "Desc1", "path1", List.of())
        ), "owner/repo", "abc123def456");

        when(properties.examples()).thenReturn(
                new StarterProperties.Examples(List.of(sourceToken), null, 50)
        );
        when(fetcher.fetch(sourceToken)).thenReturn(new FetchResult("yaml content".getBytes(), "abc123def456"));
        when(parser.parse(any(), eq("owner/repo"), eq("abc123def456"))).thenReturn(parsedManifest);
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        var event = mock(ApplicationReadyEvent.class);
        loader.onApplicationReady(event);

        verify(registry, times(1)).swap(any());
    }

    @Test
    void loader_skips_on_fetch_failure() throws Exception {
        var sourceToken = "owner/repo";

        when(properties.examples()).thenReturn(
                new StarterProperties.Examples(List.of(sourceToken), null, 50)
        );
        when(fetcher.fetch(sourceToken)).thenThrow(new SourceUnavailable("timeout"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        var event = mock(ApplicationReadyEvent.class);
        loader.onApplicationReady(event);

        // Should still call swap, but with skipped source
        verify(registry, times(1)).swap(argThat(snapshot ->
                snapshot.sources().stream().anyMatch(s -> s.outcome().contains("skipped"))
        ));
    }

    @Test
    void loader_handles_multiple_sources_in_parallel() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2", "owner/repo3");

        when(properties.examples()).thenReturn(
                new StarterProperties.Examples(sources, null, 50)
        );
        when(fetcher.fetch(anyString())).thenReturn(new FetchResult("yaml".getBytes(), "sha123"));
        when(parser.parse(any(), anyString(), anyString())).thenReturn(
                new ParsedManifest("operaton-starter/v1", List.of(
                        new ParsedManifest.Example("ex1", "Desc", "path", List.of())
                ), "owner/repo1", "sha123")
        );
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        var event = mock(ApplicationReadyEvent.class);
        loader.onApplicationReady(event);

        verify(registry, times(1)).swap(any());
    }

    @Test
    void loader_continues_on_parse_failure() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2");

        when(properties.examples()).thenReturn(
                new StarterProperties.Examples(sources, null, 50)
        );
        when(fetcher.fetch("owner/repo1")).thenReturn(new FetchResult("yaml".getBytes(), "sha1"));
        when(fetcher.fetch("owner/repo2")).thenReturn(new FetchResult("yaml".getBytes(), "sha2"));
        when(parser.parse(any(), eq("owner/repo1"), eq("sha1"))).thenThrow(new ManifestRejected("malformed-yaml"));
        when(parser.parse(any(), eq("owner/repo2"), eq("sha2"))).thenReturn(
                new ParsedManifest("operaton-starter/v1", List.of(), "owner/repo2", "sha2")
        );
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        var event = mock(ApplicationReadyEvent.class);
        loader.onApplicationReady(event);

        verify(registry, times(1)).swap(argThat(snapshot ->
                snapshot.sources().size() == 2 &&
                snapshot.sources().stream().anyMatch(s -> s.outcome().contains("skipped"))
        ));
    }

    @Test
    void loader_app_starts_even_if_all_sources_fail() throws Exception {
        var sources = List.of("owner/repo1", "owner/repo2");

        when(properties.examples()).thenReturn(
                new StarterProperties.Examples(sources, null, 50)
        );
        when(fetcher.fetch(anyString())).thenThrow(new SourceUnavailable("timeout"));
        when(registry.snapshot()).thenReturn(ExampleSnapshot.of(List.of()));

        var event = mock(ApplicationReadyEvent.class);
        loader.onApplicationReady(event);

        verify(registry, times(1)).swap(argThat(snapshot ->
                snapshot.sources().size() == 2 &&
                snapshot.sources().stream().allMatch(s -> s.outcome().contains("skipped"))
        ));
    }

    @Test
    void loader_handles_empty_source_list() throws Exception {
        when(properties.examples()).thenReturn(
                new StarterProperties.Examples(List.of(), null, 50)
        );

        var event = mock(ApplicationReadyEvent.class);
        loader.onApplicationReady(event);

        verify(registry, times(1)).swap(argThat(snapshot -> snapshot.sources().isEmpty()));
    }
}
