package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.operaton.dev.starter.server.model.Example;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExampleRegistry atomic swap behavior.
 */
class ExampleRegistryTest {

    @Test
    void registry_initialized_with_empty_snapshot() {
        var registry = new ExampleRegistry();
        var snapshot = registry.snapshot();
        assertNotNull(snapshot);
        assertTrue(snapshot.sources().isEmpty());
    }

    @Test
    void swap_updates_snapshot() {
        var registry = new ExampleRegistry();

        var example = new Example()
                .id("ex1")
                .title("Example 1")
                .path("examples/ex1")
                .shortDescription("Test");

        var sourceState = new ExampleSnapshot.SourceState("owner/repo", "success", List.of(example), "abc123", "2024-01-01T00:00:00Z");
        var newSnapshot = ExampleSnapshot.of(List.of(sourceState));

        registry.swap(newSnapshot);

        var current = registry.snapshot();
        assertEquals(1, current.sources().size());
        assertEquals("owner/repo", current.sources().get(0).source());
    }

    @Test
    void swap_is_atomic() throws InterruptedException {
        var registry = new ExampleRegistry();
        var snapshot1 = ExampleSnapshot.of(List.of(
                new ExampleSnapshot.SourceState("repo1", "success", List.of(), "sha1", "2024-01-01T00:00:00Z")
        ));
        var snapshot2 = ExampleSnapshot.of(List.of(
                new ExampleSnapshot.SourceState("repo2", "success", List.of(), "sha2", "2024-01-02T00:00:00Z")
        ));

        var latch = new CountDownLatch(2);
        var result1 = new AtomicReference<ExampleSnapshot>();
        var result2 = new AtomicReference<ExampleSnapshot>();

        Thread t1 = new Thread(() -> {
            registry.swap(snapshot1);
            result1.set(registry.snapshot());
            latch.countDown();
        });

        Thread t2 = new Thread(() -> {
            registry.swap(snapshot2);
            result2.set(registry.snapshot());
            latch.countDown();
        });

        t1.start();
        t2.start();
        latch.await();

        // Both threads should see the final state (no torn reads)
        var final1 = result1.get();
        var final2 = result2.get();

        assertNotNull(final1);
        assertNotNull(final2);
        // Both should see a complete snapshot (1 source with no null fields)
        assertEquals(1, final1.sources().size());
        assertEquals(1, final2.sources().size());
    }

    @Test
    void multiple_swaps_each_overwrites_previous() {
        var registry = new ExampleRegistry();

        for (int i = 0; i < 5; i++) {
            var snapshot = ExampleSnapshot.of(List.of(
                    new ExampleSnapshot.SourceState("repo" + i, "success", List.of(), "sha" + i, "2024-01-0" + (i+1) + "T00:00:00Z")
            ));
            registry.swap(snapshot);
        }

        var current = registry.snapshot();
        assertEquals(1, current.sources().size());
        assertEquals("repo4", current.sources().get(0).source());
    }
}
