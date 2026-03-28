import { reactive, computed } from 'vue'
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

// Validation
const GROUP_ID_RE = /^[a-z][a-z0-9.]*$/
const ARTIFACT_ID_RE = /^[a-z][a-z0-9-]*$/

interface FormErrors {
  groupId?: string
  artifactId?: string
  projectName?: string
  deploymentTarget?: string
}

export function useProjectForm() {
  const router = useRouter()
  const route = useRoute()

  const form = reactive<ProjectConfig>({ ...DEFAULTS })

  // Initialize from URL query params
  function initFromQuery(query: Record<string, string>) {
    const valid = [
      'projectType',
      'buildSystem',
      'groupId',
      'artifactId',
      'projectName',
      'javaVersion',
      'deploymentTarget',
      'dependencyUpdater',
      'dockerCompose',
      'githubActions'
    ]
    for (const key of valid) {
      if (query[key] !== undefined) {
        try {
          const v = query[key]
          if (key === 'javaVersion') (form as Record<string, unknown>)[key] = Number(v)
          else if (key === 'dockerCompose' || key === 'githubActions')
            (form as Record<string, unknown>)[key] = v === 'true'
          else (form as Record<string, unknown>)[key] = v
        } catch {
          /* ignore invalid params */
        }
      }
    }
  }

  // Shareable URL
  const shareableUrl = computed(() => {
    const params = new URLSearchParams()
    for (const [k, v] of Object.entries(form)) {
      if (v !== undefined && v !== null) params.set(k, String(v))
    }
    const base = window.location.origin
    return `${base}/configure?${params.toString()}`
  })

  // Validation
  const errors = computed<FormErrors>(() => {
    const e: FormErrors = {}
    if (!GROUP_ID_RE.test(form.groupId)) e.groupId = 'Use lowercase letters, numbers, and dots only'
    if (!ARTIFACT_ID_RE.test(form.artifactId))
      e.artifactId = 'Use lowercase letters, numbers, and hyphens only'
    if (!form.projectName?.trim()) e.projectName = 'Project name is required'
    if (form.projectType === 'PROCESS_ARCHIVE' && !form.deploymentTarget) {
      e.deploymentTarget = 'Deployment target is required for Process Archive'
    }
    return e
  })

  const isValid = computed(() => Object.keys(errors.value).length === 0)

  return { form, errors, isValid, shareableUrl, initFromQuery, router, route }
}
