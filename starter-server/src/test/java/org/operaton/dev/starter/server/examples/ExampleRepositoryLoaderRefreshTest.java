package org.operaton.dev.starter.server.examples;

import org.junit.jupiter.api.Test;
import org.operaton.dev.starter.server.model.Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "starter.examples.repositories="
})
class ExampleRepositoryLoaderRefreshTest {

    @Autowired
    private ExampleRegistry exampleRegistry;

    @Autowired
    private ExampleRepositoryLoader exampleRepositoryLoader;

    @Test
    void concurrency_test_no_torn_snapshot() throws InterruptedException {
        // Set up initial snapshot with some examples
        var initialSource = new ExampleSnapshot.SourceState(
                "owner/repo1",
                "success",
                List.of(createExample("example1")),
                "abc123",
                "2024-01-01T00:00:00Z"
        );
        exampleRegistry.swap(ExampleSnapshot.of(List.of(initialSource)));

        AtomicBoolean observedTornSnapshot = new AtomicBoolean(false);
        int numReaders = 10;
        int numRefreshes = 5;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numReaders + numRefreshes);

        // Start reader threads
        for (int i = 0; i < numReaders; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < 100; j++) {
                        ExampleSnapshot snapshot = exampleRegistry.snapshot();
                        assertNotNull(snapshot);
                        assertNotNull(snapshot.sources());
                        // Check for consistent state
                        for (ExampleSnapshot.SourceState sourceState : snapshot.sources()) {
                            assertNotNull(sourceState.source());
                            assertNotNull(sourceState.outcome());
                            assertNotNull(sourceState.examples());
                        }
                    }
                } catch (Exception e) {
                    observedTornSnapshot.set(true);
                } finally {
                    completionLatch.countDown();
                }
            }).start();
        }

        // Start refresh threads
        for (int i = 0; i < numRefreshes; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    exampleRepositoryLoader.load();
                } catch (Exception e) {
                    observedTornSnapshot.set(true);
                } finally {
                    completionLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(completionLatch.await(30, java.util.concurrent.TimeUnit.SECONDS),
                "Threads did not complete in time");
        assertFalse(observedTornSnapshot.get(), "Observed torn snapshot during concurrent access");
    }

    private Example createExample(String id) {
        var example = new Example();
        example.setId(id);
        example.setTitle(id);
        example.setPath("/" + id);
        example.setShortDescription("Test example");
        example.setSourceRepo("owner/repo1");
        example.setSourceRepoSha("abc123");
        example.setSourceRepoUrl("https://github.com/owner/repo1/tree/abc123/" + id);
        return example;
    }
}
