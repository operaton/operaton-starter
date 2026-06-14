package org.operaton.dev.starter.server.examples;

/**
 * Exception thrown when a manifest fails validation.
 *
 * <p>Includes a {@code reason} field for diagnostics.
 */
public class ManifestRejected extends Exception {
    private final String reason;

    public ManifestRejected(String reason) {
        super("Manifest rejected: " + reason);
        this.reason = reason;
    }

    public ManifestRejected(String reason, Throwable cause) {
        super("Manifest rejected: " + reason, cause);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
