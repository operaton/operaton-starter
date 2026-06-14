package org.operaton.dev.starter.server.examples;

/**
 * Exception thrown when a source is unavailable or unreachable.
 *
 * <p>Includes a {@code reason} field for diagnostics, such as:
 * <ul>
 *   <li>"timeout" — HTTP call exceeded the 5-second limit</li>
 *   <li>"http-404" — HTTP 404 Not Found</li>
 *   <li>"http-500" — HTTP 500 Internal Server Error</li>
 * </ul>
 */
public class SourceUnavailable extends Exception {
    private final String reason;

    public SourceUnavailable(String reason) {
        super("Source unavailable: " + reason);
        this.reason = reason;
    }

    public SourceUnavailable(String reason, Throwable cause) {
        super("Source unavailable: " + reason, cause);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
