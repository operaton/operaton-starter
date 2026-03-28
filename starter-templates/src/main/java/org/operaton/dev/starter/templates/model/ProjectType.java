package org.operaton.dev.starter.templates.model;

/**
 * Supported Operaton project types.
 */
public enum ProjectType {

    /**
     * Spring Boot application with embedded Operaton engine.
     * Generates Application.java, JavaDelegate stub, BPMN, and end-to-end test.
     */
    PROCESS_APPLICATION,

    /**
     * Deployable archive for a shared Operaton engine (Tomcat or Standalone).
     * Generates processes.xml, BPMN, and packaging config. No embedded engine.
     */
    PROCESS_ARCHIVE
}
