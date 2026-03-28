import { reactive, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { ProjectConfig } from '@/generated/types'

const DEFAULTS: ProjectConfig = {
  projectType: 'PROCESS_APPLICATION',
  buildSystem: 'MAVEN',
  groupId: 'com.example',
  artifactId: 'my-process-app',
  projectName: 'My Process App',
  javaVersion: 17,
  dependencyUpdater: 'RENOVATE',
  dockerCompose: false,
  githubActions: true
}

const PROJECT_TYPES = ['PROCESS_APPLICATION', 'PROCESS_ARCHIVE'] as const
const BUILD_SYSTEMS = ['MAVEN', 'GRADLE_GROOVY', 'GRADLE_KOTLIN'] as const
const JAVA_VERSIONS = [17, 21, 25] as const
const DEPLOYMENT_TARGETS = ['TOMCAT', 'STANDALONE_ENGINE'] as const
const DEPENDENCY_UPDATERS = ['DEPENDABOT', 'RENOVATE'] as const

// Validation
const GROUP_ID_RE = /^[a-zA-Z][a-zA-Z0-9.-]*$/
const ARTIFACT_ID_RE = /^[a-zA-Z][a-zA-Z0-9-]*$/

interface FormErrors {
  groupId?: string
  artifactId?: string
  projectName?: string
  projectType?: string
  buildSystem?: string
  javaVersion?: string
  deploymentTarget?: string
  dependencyUpdater?: string
}

export function useProjectForm() {
  const router = useRouter()
  const route = useRoute()

  const form = reactive<ProjectConfig>({ ...DEFAULTS })

  function isOneOf<T extends readonly string[]>(value: string, options: T): value is T[number] {
    return options.includes(value as T[number])
  }

  function queryFromForm(): Record<string, string> {
    const query: Record<string, string> = {}
    for (const [key, value] of Object.entries(form)) {
      if (value !== undefined && value !== null) {
        query[key] = String(value)
      }
    }
    return query
  }

  // Initialize from URL query params
  function initFromQuery(query: Record<string, string>) {
    if (query.projectType && isOneOf(query.projectType, PROJECT_TYPES)) {
      form.projectType = query.projectType
    }
    if (query.buildSystem && isOneOf(query.buildSystem, BUILD_SYSTEMS)) {
      form.buildSystem = query.buildSystem
    }
    if (query.groupId && GROUP_ID_RE.test(query.groupId)) {
      form.groupId = query.groupId
    }
    if (query.artifactId && ARTIFACT_ID_RE.test(query.artifactId)) {
      form.artifactId = query.artifactId
    }
    if (query.projectName?.trim()) {
      form.projectName = query.projectName
    }
    if (query.javaVersion) {
      const javaVersion = Number(query.javaVersion)
      if (JAVA_VERSIONS.includes(javaVersion as (typeof JAVA_VERSIONS)[number])) {
        form.javaVersion = javaVersion as ProjectConfig['javaVersion']
      }
    }
    if (query.deploymentTarget && isOneOf(query.deploymentTarget, DEPLOYMENT_TARGETS)) {
      form.deploymentTarget = query.deploymentTarget
    }
    if (query.dependencyUpdater && isOneOf(query.dependencyUpdater, DEPENDENCY_UPDATERS)) {
      form.dependencyUpdater = query.dependencyUpdater
    }
    if (query.dockerCompose === 'true' || query.dockerCompose === 'false') {
      form.dockerCompose = query.dockerCompose === 'true'
    }
    if (query.githubActions === 'true' || query.githubActions === 'false') {
      form.githubActions = query.githubActions === 'true'
    }

    if (form.projectType !== 'PROCESS_ARCHIVE') {
      delete form.deploymentTarget
    }
  }

  // Shareable URL
  const shareableUrl = computed(() => {
    const params = new URLSearchParams(queryFromForm())
    const base = window.location.origin
    return `${base}/configure?${params.toString()}`
  })

  // Validation
  const errors = computed<FormErrors>(() => {
    const e: FormErrors = {}
    if (!GROUP_ID_RE.test(form.groupId))
      e.groupId = 'Use letters, numbers, dots, and hyphens only'
    if (!ARTIFACT_ID_RE.test(form.artifactId))
      e.artifactId = 'Use letters, numbers, and hyphens only'
    if (!form.projectName?.trim()) e.projectName = 'Project name is required'
    if (!isOneOf(form.projectType, PROJECT_TYPES)) {
      e.projectType = 'Select a supported project type'
    }
    if (!isOneOf(form.buildSystem, BUILD_SYSTEMS)) {
      e.buildSystem = 'Select a supported build system'
    }
    if (!JAVA_VERSIONS.includes(form.javaVersion as (typeof JAVA_VERSIONS)[number])) {
      e.javaVersion = 'Select Java 17, 21, or 25'
    }
    if (form.projectType === 'PROCESS_ARCHIVE' && !form.deploymentTarget) {
      e.deploymentTarget = 'Deployment target is required for Process Archive'
    }
    if (form.deploymentTarget && !isOneOf(form.deploymentTarget, DEPLOYMENT_TARGETS)) {
      e.deploymentTarget = 'Select a supported deployment target'
    }
    if (form.dependencyUpdater && !isOneOf(form.dependencyUpdater, DEPENDENCY_UPDATERS)) {
      e.dependencyUpdater = 'Select a supported dependency updater'
    }
    return e
  })

  const isValid = computed(() => Object.keys(errors.value).length === 0)

  watch(
    form,
    () => {
      void router.replace({ path: route.path, query: queryFromForm() })
    },
    { deep: true }
  )

  return { form, errors, isValid, shareableUrl, initFromQuery, router, route }
}
