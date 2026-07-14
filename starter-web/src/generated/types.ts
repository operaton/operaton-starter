export type TagCategory = 'BPMN_CONCEPT' | 'TECHNOLOGY' | 'PLATFORM' | 'STANDARD' | 'RUNTIME' | 'BUILD_SYSTEM' | 'COMPLEXITY'

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
}

export interface Author {
  name: string
  url?: string
}

export interface Example {
  id: string
  title: string
  icon?: string
  path: string
  shortDescription: string
  longDescription?: string
  buildSystem?: string
  runtime?: string
  operatonVersion?: string
  javaVersion?: number
  complexity?: string
  tags?: Tag[]
  integrations?: string[]
  bpmnConcepts?: string[]
  requires?: string[]
  authors?: Author[]
  license?: string
  documentationUrl?: string
  demoVideoUrl?: string
  screenshots?: string[]
  lastUpdated?: string
  sourceRepo?: string
  sourceRepoSha?: string
  sourceRepoUrl?: string
  owner?: string
  repo?: string
}

export interface Metadata {
  projectTypes: ProjectTypeInfo[]
  buildSystems: BuildSystemInfo[]
  globalOptions: GlobalOptions
  defaultGroupId?: string
  examples?: Example[]
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
