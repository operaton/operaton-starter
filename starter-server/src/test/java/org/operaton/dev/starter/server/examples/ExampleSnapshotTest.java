package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.operaton.dev.starter.server.model.Example;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExampleSnapshot immutable record.
 */
class ExampleSnapshotTest {

    @Test
    void snapshot_with_single_source() {
        var example = new Example()
                .id("ex1")
                .title("Example 1")
                .path("examples/ex1")
                .shortDescription("Test example");

        var sourceState = new ExampleSnapshot.SourceState("owner/repo", "success", List.of(example), "abc123def456", "2024-01-01T00:00:00Z");
        var snapshot = new ExampleSnapshot(List.of(sourceState));

        assertEquals(1, snapshot.sources().size());
        assertEquals("owner/repo", snapshot.sources().get(0).source());
        assertEquals("success", snapshot.sources().get(0).outcome());
        assertEquals(1, snapshot.sources().get(0).examples().size());
    }

    @Test
    void snapshot_with_failed_source() {
        var sourceState = new ExampleSnapshot.SourceState("owner/repo", "skipped:timeout", List.of(), null, "2024-01-01T00:00:00Z");
        var snapshot = new ExampleSnapshot(List.of(sourceState));

        assertEquals(1, snapshot.sources().size());
        assertEquals("skipped:timeout", snapshot.sources().get(0).outcome());
        assertTrue(snapshot.sources().get(0).examples().isEmpty());
    }

    @Test
    void empty_snapshot() {
        var snapshot = new ExampleSnapshot(List.of());
        assertTrue(snapshot.sources().isEmpty());
    }

    @Test
    void snapshot_is_immutable() {
        var sourceState = new ExampleSnapshot.SourceState("owner/repo", "success", List.of(), "abc123", "2024-01-01T00:00:00Z");
        var snapshot = new ExampleSnapshot(List.of(sourceState));

        // Try to modify — this should fail at compile time or runtime
        assertThrows(Exception.class, () -> {
            snapshot.sources().add(new ExampleSnapshot.SourceState("other/repo", "success", List.of(), "def456", "2024-01-01T00:00:00Z"));
        });
    }

    @Test
    void snapshot_collects_all_examples() {
        var ex1 = new Example().id("ex1").title("Ex1").path("path1").shortDescription("Desc1");
        var ex2 = new Example().id("ex2").title("Ex2").path("path2").shortDescription("Desc2");
        var ex3 = new Example().id("ex3").title("Ex3").path("path3").shortDescription("Desc3");

        var source1 = new ExampleSnapshot.SourceState("owner/repo1", "success", List.of(ex1, ex2), "sha1", "2024-01-01T00:00:00Z");
        var source2 = new ExampleSnapshot.SourceState("owner/repo2", "success", List.of(ex3), "sha2", "2024-01-02T00:00:00Z");

        var snapshot = new ExampleSnapshot(List.of(source1, source2));
        var allExamples = snapshot.allExamples();

        assertEquals(3, allExamples.size());
        assertTrue(allExamples.stream().anyMatch(e -> "ex1".equals(e.getId())));
        assertTrue(allExamples.stream().anyMatch(e -> "ex2".equals(e.getId())));
        assertTrue(allExamples.stream().anyMatch(e -> "ex3".equals(e.getId())));
    }
}
