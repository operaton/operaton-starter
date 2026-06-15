package org.operaton.dev.starter.server.examples.actuator;

import org.operaton.dev.starter.server.examples.ExampleRegistry;
import org.operaton.dev.starter.server.examples.ExampleSnapshot;
import org.operaton.dev.starter.server.examples.ExampleSourceStatus;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring Boot Actuator endpoint for example sources diagnostics.
 *
 * <p>Architecture A13: ExampleSourcesEndpoint is the canonical diagnostics surface.
 * Provides per-source load outcome, example counts, resolved SHAs, and fetch timestamps.
 *
 * <p>Architecture A7: Endpoint URL is GET /actuator/examples with actuator default auth.
 *
 * <p>Returns the same ExampleSourceStatus shape as the refresh endpoint,
 * but reflects the most recent load attempt (from the current ExampleRegistry snapshot).
 */
@Component
@Endpoint(id = "examples")
public class ExampleSourcesEndpoint {

    private final ExampleRegistry exampleRegistry;

    public ExampleSourcesEndpoint(ExampleRegistry exampleRegistry) {
        this.exampleRegistry = exampleRegistry;
    }

    /**
     * Returns the status of all example sources from the current snapshot.
     *
     * <p>GET /actuator/examples
     *
     * @return List of ExampleSourceStatus entries describing the most recent load attempt
     */
    @ReadOperation
    public List<ExampleSourceStatus> examples() {
        ExampleSnapshot snapshot = exampleRegistry.snapshot();
        return snapshot.statuses();
    }
}
