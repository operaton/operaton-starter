package org.operaton.dev.starter.server.examples;

/**
 * Thrown when the uncompressed payload of a ZIP being built exceeds maxDownloadSizeMb.
 *
 * <p>This exception is caught by the controller and mapped to HTTP 413 Payload Too Large.
 *
 * <p>Contains both the limit and the size that was exceeded.
 */
public class SizeLimitExceededException extends Exception {
    private final long limitMb;
    private final long actualSizeMb;
    private final String exampleId;

    public SizeLimitExceededException(
            String message,
            long limitMb,
            long actualSizeMb,
            String exampleId
    ) {
        super(message);
        this.limitMb = limitMb;
        this.actualSizeMb = actualSizeMb;
        this.exampleId = exampleId;
    }

    public long getLimitMb() {
        return limitMb;
    }

    public long getActualSizeMb() {
        return actualSizeMb;
    }

    public String getExampleId() {
        return exampleId;
    }
}
