package org.operaton.dev.starter.server.examples;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe holder for the current {@link ExampleSnapshot}.
 *
 * <p>Uses {@link AtomicReference} to allow atomic swap of snapshots,
 * ensuring readers never see torn state. Initialized with an empty snapshot
 * at bean creation to avoid NPE.
 *
 * <p>Readers call {@link #snapshot()} to get the current snapshot.
 * Writers call {@link #swap(ExampleSnapshot)} to atomically replace the snapshot.
 */
@Component
public class ExampleRegistry {
    private final AtomicReference<ExampleSnapshot> current;

    public ExampleRegistry() {
        // Initialize with empty snapshot to avoid NPE before load completes
        this.current = new AtomicReference<>(new ExampleSnapshot(List.of(), List.of()));
    }

    /**
     * Gets the current snapshot.
     *
     * @return the current ExampleSnapshot (never null)
     */
    public ExampleSnapshot snapshot() {
        return current.get();
    }

    /**
     * Atomically replaces the current snapshot.
     *
     * @param snapshot the new snapshot to store
     */
    public void swap(ExampleSnapshot snapshot) {
        current.set(snapshot);
    }
}
