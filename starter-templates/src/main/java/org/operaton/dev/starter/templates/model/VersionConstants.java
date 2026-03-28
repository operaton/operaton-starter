package org.operaton.dev.starter.templates.model;

/**
 * Current stable Operaton and Spring Boot version constants.
 *
 * <p>These are bumped automatically by Renovate when a new Operaton release is published.
 * No user-selectable version exists — generated projects always target the current stable release.
 */
public final class VersionConstants {

    /** Current stable Operaton BPM version. */
    public static final String OPERATON_VERSION = "2.0.0";

    /** Spring Boot version bundled in the current Operaton BOM. */
    public static final String SPRING_BOOT_VERSION = "4.0.4";

    /** Gradle wrapper version used in generated Gradle projects. */
    public static final String GRADLE_VERSION = "8.14";

    private VersionConstants() {}
}
