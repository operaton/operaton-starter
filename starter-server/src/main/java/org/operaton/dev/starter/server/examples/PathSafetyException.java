package org.operaton.dev.starter.server.examples;

/**
 * Thrown when a tar entry's normalized path escapes the intended example subfolder.
 *
 * <p>Examples of violations:
 * <ul>
 *   <li>Path contains ".." component that would traverse upward</li>
 *   <li>Path is absolute (starts with /)</li>
 *   <li>Path contains null bytes</li>
 * </ul>
 *
 * <p>Architecture A9: Path traversal check on both manifest path: (done in 8.2)
 * and tarball entry names (done here). A path failure aborts build and returns 502.
 */
public class PathSafetyException extends Exception {
    private final String entryPath;

    public PathSafetyException(String message, String entryPath) {
        super(message);
        this.entryPath = entryPath;
    }

    public PathSafetyException(String message, String entryPath, Throwable cause) {
        super(message, cause);
        this.entryPath = entryPath;
    }

    public String getEntryPath() {
        return entryPath;
    }
}
