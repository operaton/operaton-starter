package org.operaton.dev.starter.templates.model;

/**
 * Deployment target for Process Archive projects.
 */
public enum DeploymentTarget {

    /** Apache Tomcat — WAR packaging, engine managed by the container */
    TOMCAT,

    /** Standalone Operaton engine (JBoss/WildFly or plain Java) — JAR packaging */
    STANDALONE_ENGINE
}
