package org.operaton.dev.starter.templates.model;

/**
 * Supported build systems for generated projects.
 */
public enum BuildSystem {

    /** Apache Maven — generates pom.xml */
    MAVEN,

    /** Gradle with Groovy DSL — generates build.gradle */
    GRADLE_GROOVY,

    /** Gradle with Kotlin DSL — generates build.gradle.kts */
    GRADLE_KOTLIN
}
