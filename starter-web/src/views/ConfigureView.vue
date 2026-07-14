<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { useMetadata } from '@/composables/useMetadata'
import { useProjectForm } from '@/composables/useProjectForm'
import { useGenerate } from '@/composables/useGenerate'
import ErrorBanner from '@/components/ErrorBanner.vue'
import FileTreePreview from '@/components/FileTreePreview.vue'
import ActionPanel from '@/components/ActionPanel.vue'

const route = useRoute()
const { data: metadata, error: metadataError } = useMetadata()
const {
  form,
  errors,
  isValid,
  initFromQuery,
  isProjectTypeFromQuery,
  buildSystemCategory,
  gradleDsl,
  router,
} = useProjectForm()
const { isGenerating, error: generateError, generate } = useGenerate()

const successMsg = ref<string | null>(null)
const helpOpen = ref<Record<string, boolean>>({})
const hasGroupIdQuery = computed(
  () => typeof route.query.groupId === 'string' && route.query.groupId.trim().length > 0
)

function toggleHelp(field: string) {
  helpOpen.value[field] = !helpOpen.value[field]
}

// Initialize from query params.
// With a projectType param (card/use-case arrival): lock project type as read-only.
// Without projectType param (Configure Now, direct URL, bookmark): pre-select PROCESS_APPLICATION
// and leave project type editable.
onMounted(() => {
  initFromQuery(route.query as Record<string, string>)
})

// FR-2.4: if PROCESS_APPLICATION is absent from metadata, fall back to the first available type
watch(
  () => metadata.value?.projectTypes,
  (types) => {
    if (!types?.length || isProjectTypeFromQuery.value) return
    const valid = types.some((pt) => pt.id === form.projectType)
    if (!valid) {
      form.projectType = types[0].id as typeof form.projectType
    }
  },
  { immediate: true }
)

watch(
  () => metadata.value?.defaultGroupId,
  (defaultGroupId) => {
    if (!defaultGroupId || hasGroupIdQuery.value) {
      return
    }
    if (form.groupId === 'org.operaton.example' || form.groupId === 'com.example') {
      form.groupId = defaultGroupId
    }
  },
  { immediate: true }
)

// Selected project type manifest
const selectedTypeManifest = computed(() => {
  return metadata.value?.projectTypes.find((pt) => pt.id === form.projectType)?.templateManifest ?? []
})

// Display name for read-only project type banner
const projectTypeDisplayName = computed(() => {
  return (
    metadata.value?.projectTypes.find((pt) => pt.id === form.projectType)?.displayName ??
    form.projectType
  )
})

async function handleGenerate() {
  if (!isValid.value) return
  successMsg.value = null
  const ok = await generate({ ...form }, form.artifactId)
  if (ok) {
    successMsg.value = `Downloaded ${form.artifactId}.zip`
    setTimeout(() => {
      successMsg.value = null
    }, 4000)
  }
}

// Help text map
const helpText: Record<string, string> = {
  groupId:
    'The Maven group ID, typically your reverse domain name (e.g. com.example). Used as the Java package root.',
  artifactId:
    'The Maven artifact ID, used as the project folder name and Spring application name (e.g. my-process-app).',
  projectName: 'A human-readable name for your project, shown in the README and Maven POM.',
  projectType:
    'Process Application: embedded engine in Spring Boot. Process Archive: deploys to a shared engine (Tomcat, standalone).',
  buildSystem:
    'Maven: industry standard, widest tooling support. Gradle Groovy/Kotlin: faster builds, more expressive DSL.',
  javaVersion:
    'Java version to target. Java 17 is the current LTS; 21 is the next LTS with virtual threads.',
  deploymentTarget:
    'Tomcat: deploy as WAR to an application server. Standalone Engine: deploy to a standalone Operaton engine.',
  dependencyUpdater:
    'Renovate: flexible config, works across ecosystems. Dependabot: native GitHub integration, zero config.',
  dockerCompose: 'Generates a docker-compose.yml for running the application locally with Docker.',
  githubActions: 'Generates a GitHub Actions CI workflow that passes green on first push.'
}
</script>

<template>
  <div class="min-h-screen bg-neutral-50">
    <!-- Back nav -->
    <div class="bg-neutral-0 border-b border-neutral-200 px-6 md:px-8 py-3">
      <RouterLink to="/" class="text-sm text-neutral-500 hover:text-primary inline-flex items-center gap-1">
        ← Back to gallery
      </RouterLink>
    </div>

    <ErrorBanner :error="metadataError" />

    <div class="max-w-content mx-auto px-6 md:px-8 py-8 flex flex-col md:flex-row gap-8">
      <!-- Form Panel -->
      <div class="md:w-96 flex-shrink-0">
        <form novalidate @submit.prevent="handleGenerate">
          <!-- Identity -->
          <fieldset class="bg-neutral-0 border border-neutral-200 rounded-s p-5 mb-4">
            <legend class="text-xs font-semibold text-neutral-500 uppercase tracking-wide px-1">Project Identity</legend>

            <!-- Group ID -->
            <div class="mb-4">
              <div class="flex items-center gap-1 mb-1">
                <label for="groupId" class="text-sm font-medium text-neutral-900">Group ID</label>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Group ID'" :aria-expanded="helpOpen.groupId" @click="toggleHelp('groupId')">?</button>
              </div>
              <div v-show="helpOpen.groupId" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-1">{{ helpText.groupId }}</div>
              <input id="groupId" v-model="form.groupId" type="text" autocomplete="off" spellcheck="false"
                     class="w-full px-3 py-2 text-sm border rounded-s font-mono focus:outline-none focus:ring-2 focus:ring-primary/20"
                     :class="errors.groupId ? 'border-red-400' : 'border-neutral-200'"
                     :aria-describedby="errors.groupId ? 'groupId-error' : undefined" />
              <p v-if="errors.groupId" id="groupId-error" role="alert" class="text-xs text-red-600 mt-1">{{ errors.groupId }}</p>
            </div>

            <!-- Artifact ID -->
            <div class="mb-4">
              <div class="flex items-center gap-1 mb-1">
                <label for="artifactId" class="text-sm font-medium text-neutral-900">Artifact ID</label>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Artifact ID'" :aria-expanded="helpOpen.artifactId" @click="toggleHelp('artifactId')">?</button>
              </div>
              <div v-show="helpOpen.artifactId" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-1">{{ helpText.artifactId }}</div>
              <input id="artifactId" v-model="form.artifactId" type="text" autocomplete="off" spellcheck="false"
                     class="w-full px-3 py-2 text-sm border rounded-s font-mono focus:outline-none focus:ring-2 focus:ring-primary/20"
                     :class="errors.artifactId ? 'border-red-400' : 'border-neutral-200'"
                     :aria-describedby="errors.artifactId ? 'artifactId-error' : undefined" />
              <p v-if="errors.artifactId" id="artifactId-error" role="alert" class="text-xs text-red-600 mt-1">{{ errors.artifactId }}</p>
            </div>

            <!-- Project Name -->
            <div>
              <div class="flex items-center gap-1 mb-1">
                <label for="projectName" class="text-sm font-medium text-neutral-900">Project Name</label>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Project Name'" :aria-expanded="helpOpen.projectName" @click="toggleHelp('projectName')">?</button>
              </div>
              <div v-show="helpOpen.projectName" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-1">{{ helpText.projectName }}</div>
              <input id="projectName" v-model="form.projectName" type="text"
                     class="w-full px-3 py-2 text-sm border rounded-s focus:outline-none focus:ring-2 focus:ring-primary/20"
                     :class="errors.projectName ? 'border-red-400' : 'border-neutral-200'"
                     :aria-describedby="errors.projectName ? 'projectName-error' : undefined" />
              <p v-if="errors.projectName" id="projectName-error" role="alert" class="text-xs text-red-600 mt-1">{{ errors.projectName }}</p>
            </div>

            <!-- Version -->
            <div>
              <label for="version" class="text-sm font-medium text-neutral-900 block mb-1">Version</label>
              <input id="version" v-model="form.version" type="text" autocomplete="off"
                     class="w-full px-3 py-2 text-sm border rounded-s font-mono focus:outline-none focus:ring-2 focus:ring-primary/20"
                     :class="errors.version ? 'border-red-400' : 'border-neutral-200'"
                     :aria-describedby="errors.version ? 'version-error' : undefined" />
              <p v-if="errors.version" id="version-error" role="alert" class="text-xs text-red-600 mt-1">{{ errors.version }}</p>
            </div>
          </fieldset>

          <!-- Build Options -->
          <fieldset class="bg-neutral-0 border border-neutral-200 rounded-s p-5 mb-4">
            <legend class="text-xs font-semibold text-neutral-500 uppercase tracking-wide px-1">Build Options</legend>

            <!-- Project Type: read-only banner when arriving from gallery or shareable link -->
            <div v-if="isProjectTypeFromQuery" class="mb-4">
              <div class="flex items-center justify-between bg-primary/5 border border-primary/20 rounded-s px-4 py-3">
                <div>
                  <span class="text-xs font-semibold text-neutral-500 uppercase tracking-wide block mb-0.5">Project type</span>
                  <span class="text-sm font-semibold text-primary">{{ projectTypeDisplayName }}</span>
                </div>
                <RouterLink to="/" class="text-xs text-neutral-400 hover:text-primary transition-colors whitespace-nowrap ml-4">
                  ← Change
                </RouterLink>
              </div>
            </div>

            <!-- Project Type: editable selector (Practitioner direct-access path) -->
            <div v-else class="mb-4">
              <div class="flex items-center gap-1 mb-2">
                <span class="text-sm font-medium text-neutral-900">Project Type</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Project Type'" :aria-expanded="helpOpen.projectType" @click="toggleHelp('projectType')">?</button>
              </div>
              <div v-show="helpOpen.projectType" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-2">{{ helpText.projectType }}</div>
              <div class="space-y-2" role="radiogroup" aria-label="Project Type">
                <label v-for="pt in (metadata?.projectTypes ?? [])" :key="pt.id"
                       class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" :value="pt.id" v-model="form.projectType" class="accent-primary" />
                  {{ pt.displayName }}
                </label>
              </div>
            </div>

            <!-- Deployment Target (only for PROCESS_ARCHIVE) -->
            <div v-if="form.projectType === 'PROCESS_ARCHIVE'" class="mb-4">
              <div class="flex items-center gap-1 mb-2">
                <span class="text-sm font-medium text-neutral-900">Deployment Target</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Deployment Target'" :aria-expanded="helpOpen.deploymentTarget" @click="toggleHelp('deploymentTarget')">?</button>
              </div>
              <div v-show="helpOpen.deploymentTarget" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-2">{{ helpText.deploymentTarget }}</div>
              <div class="space-y-2" role="radiogroup" aria-label="Deployment Target">
                <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" value="TOMCAT" v-model="form.deploymentTarget" class="accent-primary" />
                  Tomcat
                </label>
                <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" value="STANDALONE_ENGINE" v-model="form.deploymentTarget" class="accent-primary" />
                  Standalone Engine
                </label>
              </div>
              <p v-if="errors.deploymentTarget" role="alert" class="text-xs text-red-600 mt-1">{{ errors.deploymentTarget }}</p>
            </div>

            <!-- Build System — two-step: Maven vs Gradle, then Gradle DSL (FR10) -->
            <div class="mb-4">
              <div class="flex items-center gap-1 mb-2">
                <span class="text-sm font-medium text-neutral-900">Build System</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Build System'" :aria-expanded="helpOpen.buildSystem" @click="toggleHelp('buildSystem')">?</button>
              </div>
              <div v-show="helpOpen.buildSystem" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-2">{{ helpText.buildSystem }}</div>
              <!-- Step 1: Maven vs Gradle -->
              <div class="space-y-2 mb-3" role="radiogroup" aria-label="Build Tool">
                <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" value="maven" v-model="buildSystemCategory" class="accent-primary"
                         @change="gradleDsl = null" />
                  Maven
                </label>
                <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" value="gradle" v-model="buildSystemCategory" class="accent-primary" />
                  Gradle
                </label>
              </div>
              <!-- Step 2: Gradle DSL (only shown when Gradle selected) -->
              <div v-if="buildSystemCategory === 'gradle'" class="ml-5 space-y-2" role="radiogroup" aria-label="Gradle DSL">
                <p class="text-xs text-neutral-500 mb-1">Choose DSL:</p>
                <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" value="GRADLE_GROOVY" v-model="gradleDsl" class="accent-primary" />
                  Groovy DSL
                </label>
                <label class="flex items-center gap-2 text-sm cursor-pointer">
                  <input type="radio" value="GRADLE_KOTLIN" v-model="gradleDsl" class="accent-primary" />
                  Kotlin DSL
                </label>
              </div>
              <p v-if="errors.buildSystem" role="alert" class="text-xs text-red-600 mt-1">{{ errors.buildSystem }}</p>
            </div>

            <!-- Java Version -->
            <div>
              <div class="flex items-center gap-1 mb-2">
                <span class="text-sm font-medium text-neutral-900">Java Version</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Java Version'" :aria-expanded="helpOpen.javaVersion" @click="toggleHelp('javaVersion')">?</button>
              </div>
              <div v-show="helpOpen.javaVersion" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mb-2">{{ helpText.javaVersion }}</div>
              <div class="flex gap-4" role="radiogroup" aria-label="Java Version">
                <label v-for="v in (metadata?.globalOptions.javaVersions.options ?? [17, 21, 25])" :key="v"
                       class="flex items-center gap-1 text-sm cursor-pointer">
                  <input type="radio" :value="v" v-model="form.javaVersion" class="accent-primary" />
                  {{ v }}
                </label>
              </div>
            </div>
          </fieldset>

          <!-- Extras -->
          <fieldset class="bg-neutral-0 border border-neutral-200 rounded-s p-5 mb-4">
            <legend class="text-xs font-semibold text-neutral-500 uppercase tracking-wide px-1">Extras</legend>

            <!-- GitHub Actions CI/CD (hidden for PROCESS_ARCHIVE — not applicable) -->
            <div v-if="form.projectType !== 'PROCESS_ARCHIVE'" class="mb-3">
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" v-model="form.githubActions" class="accent-primary w-4 h-4" />
                <span class="text-sm text-neutral-900">GitHub Actions CI/CD</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: GitHub Actions'" :aria-expanded="helpOpen.githubActions" @click.prevent="toggleHelp('githubActions')">?</button>
              </label>
              <div v-show="helpOpen.githubActions" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mt-1 ml-6">{{ helpText.githubActions }}</div>
            </div>

            <!-- Docker Compose -->
            <div class="mb-4">
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" v-model="form.dockerCompose" class="accent-primary w-4 h-4" />
                <span class="text-sm text-neutral-900">Docker Compose</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Docker Compose'" :aria-expanded="helpOpen.dockerCompose" @click.prevent="toggleHelp('dockerCompose')">?</button>
              </label>
              <div v-show="helpOpen.dockerCompose" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mt-1 ml-6">{{ helpText.dockerCompose }}</div>
            </div>

            <!-- Dependency Updates: opt-in checkbox + sub-option (FR13, FR56) -->
            <div>
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" class="accent-primary w-4 h-4"
                       :checked="!!form.dependencyUpdater"
                       @change="(e) => { form.dependencyUpdater = (e.target as HTMLInputElement).checked ? 'RENOVATE' : undefined }" />
                <span class="text-sm text-neutral-900">Dependency Updates</span>
                <button type="button" class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center hover:bg-primary hover:text-white transition-colors"
                        :aria-label="'Help: Dependency Updates'" :aria-expanded="helpOpen.dependencyUpdater" @click.prevent="toggleHelp('dependencyUpdater')">?</button>
              </label>
              <div v-show="helpOpen.dependencyUpdater" role="note" class="text-xs text-neutral-500 bg-neutral-50 p-2 rounded mt-1 ml-6">{{ helpText.dependencyUpdater }}</div>
              <div v-if="form.dependencyUpdater" class="ml-6 mt-2 flex gap-4" role="radiogroup" aria-label="Dependency Updater">
                <label class="flex items-center gap-1 text-sm cursor-pointer">
                  <input type="radio" value="RENOVATE" v-model="form.dependencyUpdater" class="accent-primary" />
                  Renovate
                </label>
                <label class="flex items-center gap-1 text-sm cursor-pointer">
                  <input type="radio" value="DEPENDABOT" v-model="form.dependencyUpdater" class="accent-primary" />
                  Dependabot
                </label>
              </div>
            </div>
          </fieldset>

          <!-- Generate button -->
          <button
            type="submit"
            :disabled="isGenerating || !isValid"
            class="w-full bg-primary text-white py-3 rounded-s font-semibold text-base hover:bg-primary-dark transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :aria-busy="isGenerating"
          >
            <span v-if="isGenerating">Generating...</span>
            <span v-else>Generate &amp; Download ↓</span>
          </button>

          <!-- Success / Error feedback -->
          <div v-if="successMsg" role="status" aria-live="polite"
               class="mt-3 text-sm text-green-700 bg-green-50 border border-green-200 rounded-s p-3 text-center">
            ✓ {{ successMsg }}
          </div>
          <ErrorBanner :error="generateError" />
        </form>
      </div>

      <!-- Preview + Action Panel -->
      <div class="flex-1 min-w-0">
        <!-- Mobile: collapsible details -->
        <details class="md:hidden mb-4 bg-neutral-0 border border-neutral-200 rounded-s">
          <summary class="text-sm font-medium text-neutral-700 p-4 cursor-pointer">File Structure Preview</summary>
          <div class="p-4 border-t border-neutral-200">
            <FileTreePreview :manifest="selectedTypeManifest" :config="form" />
          </div>
        </details>

        <!-- Desktop: always visible -->
        <div class="hidden md:block mb-6">
          <FileTreePreview :manifest="selectedTypeManifest" :config="form" />
        </div>

        <!-- Action Panel -->
        <div class="bg-neutral-0 border border-neutral-200 rounded-s p-5">
          <ActionPanel :config="form" :is-valid="isValid" @generate="handleGenerate" />
        </div>
      </div>
    </div>
  </div>
</template>
