export type TagCategory = 'BPMN_CONCEPT' | 'TECHNOLOGY' | 'PLATFORM' | 'STANDARD'

export interface Tag {
  label: string
  category: TagCategory
}

export interface ProjectConfig {
  projectType: 'PROCESS_APPLICATION' | 'PROCESS_ARCHIVE' | 'DMN_PROJECT'
  buildSystem: 'MAVEN' | 'GRADLE_GROOVY' | 'GRADLE_KOTLIN'
  groupId: string
  artifactId: string
  projectName: string
  version?: string
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
  tags: Tag[]
  projectType: string
  buildSystem: 'MAVEN' | 'GRADLE_GROOVY' | 'GRADLE_KOTLIN'
  defaultArtifactId: string
  defaultProjectName: string
  dockerCompose: boolean
  processSummary?: string
  bpmnConcepts?: string[]
  integrations?: string[]
  learnings?: string[]
  templateManifest?: TemplateManifestEntry[]
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
  detailedDescription?: string
  tags: Tag[]
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
