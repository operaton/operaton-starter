export interface ProjectConfig {
  projectType: 'PROCESS_APPLICATION' | 'PROCESS_ARCHIVE'
  buildSystem: 'MAVEN' | 'GRADLE_GROOVY' | 'GRADLE_KOTLIN'
  groupId: string
  artifactId: string
  projectName: string
  javaVersion?: 17 | 21 | 25
  deploymentTarget?: 'TOMCAT' | 'STANDALONE_ENGINE'
  dependencyUpdater?: 'DEPENDABOT' | 'RENOVATE'
  dockerCompose?: boolean
  githubActions?: boolean
}

export interface Metadata {
  projectTypes: ProjectTypeInfo[]
  buildSystems: BuildSystemInfo[]
  globalOptions: GlobalOptions
}

export interface ProjectTypeInfo {
  id: string
  displayName: string
  description: string
  tags: string[]
  personaHint: string
  templateManifest: TemplateManifestEntry[]
}

export interface BuildSystemInfo {
  id: string
  displayName: string
}

export interface GlobalOptions {
  javaVersions: { options: number[]; default: number }
}

export interface TemplateManifestEntry {
  path: string
  condition?: string | null
  templateId: string
}

export interface ProblemDetail {
  type: string
  title: string
  status: number
  detail?: string
  instance?: string
}
