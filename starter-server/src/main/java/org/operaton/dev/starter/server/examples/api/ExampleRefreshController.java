package org.operaton.dev.starter.server.examples.api;

import org.operaton.dev.starter.server.examples.ExampleRepositoryLoader;
import org.operaton.dev.starter.server.examples.ExampleSourceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * HTTP endpoint for manual refresh of example sources.
 *
 * <p>Architecture A7: Endpoint is POST /api/v1/examples/refresh — no auth in v1.
 * Returns the list of ExampleSourceStatus entries describing the load outcome.
 *
 * <p>Architecture A9: Refresh endpoint unauthenticated in v1.
 * [NOTE FOR PM: This implements PRD Open Q-1 about unauthenticated access to the refresh endpoint.
 * Consider adding authentication/authorization in future versions.]
 *
 * <p>Invokes ExampleRepositoryLoader.load() to perform the refresh, then returns
 * the status list as JSON.
 */
@RestController
@RequestMapping("/api/v1/examples")
public class ExampleRefreshController {
    private static final Logger log = LoggerFactory.getLogger(ExampleRefreshController.class);

    private final ExampleRepositoryLoader exampleRepositoryLoader;

    public ExampleRefreshController(ExampleRepositoryLoader exampleRepositoryLoader) {
        this.exampleRepositoryLoader = exampleRepositoryLoader;
    }

    /**
     * Refreshes all example sources and returns their status.
     *
     * <p>POST /api/v1/examples/refresh
     *
     * @return 200 OK with List of ExampleSourceStatus entries (one per configured source)
     */
    @PostMapping("/refresh")
    public ResponseEntity<List<ExampleSourceStatus>> refresh() {
        log.info("Manual refresh requested via POST /api/v1/examples/refresh");
        List<ExampleSourceStatus> statuses = exampleRepositoryLoader.load();
        return ResponseEntity.ok(statuses);
    }
}
