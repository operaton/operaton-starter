/**
 * TypeScript types generated from openapi.yaml — ProjectConfig schema.
 * Kept in sync with the OpenAPI contract manually; no hand-written HTTP client code below.
 */

export interface ProjectConfig {
  projectType: 'PROCESS_APPLICATION' | 'PROCESS_ARCHIVE'
  buildSystem: 'MAVEN' | 'GRADLE_GROOVY' | 'GRADLE_KOTLIN'
  groupId: string
  artifactId: string
  projectName: string
  javaVersion: number
  deploymentTarget?: 'TOMCAT' | 'STANDALONE_ENGINE'
  dependencyUpdater?: 'DEPENDABOT' | 'RENOVATE'
  dockerCompose?: boolean
  githubActions?: boolean
}

export interface ProblemDetail {
  type?: string
  title: string
  status: number
  detail?: string
  instance?: string
}
