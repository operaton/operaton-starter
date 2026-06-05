export interface ProjectConfig {
  projectType: 'PROCESS_APPLICATION' | 'PROCESS_ARCHIVE' | 'DMN_PROJECT'
  buildSystem: 'MAVEN' | 'GRADLE_GROOVY' | 'GRADLE_KOTLIN'
  groupId: string
  artifactId: string
  projectName: string
  javaVersion?: 17 | 21 | 25
  deploymentTarget?: 'TOMCAT' | 'STANDALONE_ENGINE'
  dependencyUpdater?: 'DEPENDABOT' | 'RENOVATE'
  dockerCompose?: boolean
  githubActions?: boolean
  useCaseId?: string
}

export interface UseCaseExample {
  useCaseId: string
  title: string
  description: string
  tags: string[]
  projectType: string
  buildSystem: 'MAVEN' | 'GRADLE_GROOVY' | 'GRADLE_KOTLIN'
  defaultArtifactId: string
  defaultProjectName: string
  dockerCompose: boolean
}

export interface Metadata {
  projectTypes: ProjectTypeInfo[]
  buildSystems: BuildSystemInfo[]
  globalOptions: GlobalOptions
  defaultGroupId?: string
  useCaseExamples?: UseCaseExample[]
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
  previewContent?: string | null
}

export interface ProblemDetail {
  type: string
  title: string
  status: number
  detail?: string
  instance?: string
}
