package org.operaton.dev.starter.templates.model;

/**
 * Dependency update tool to configure in the generated project.
 */
public enum DependencyUpdater {

    /** No dependency updater — generates no configuration */
    NONE,

    /** GitHub Dependabot — generates .github/dependabot.yml */
    DEPENDABOT,

    /** Renovate Bot — generates renovate.json */
    RENOVATE
}
