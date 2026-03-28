<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ProjectConfig } from '@/generated/types'

const props = defineProps<{ config: ProjectConfig; isValid: boolean }>()
const emit = defineEmits<{ generate: [] }>()

const copied = ref(false)
const baseUrl = import.meta.env.VITE_API_BASE_URL ?? window.location.origin

function buildEncodeUrl(): string {
  const params = new URLSearchParams()
  for (const [k, v] of Object.entries(props.config)) {
    if (v !== undefined && v !== null) params.set(k, String(v))
  }
  return `${baseUrl}/api/v1/generate?${params.toString()}`
}

const intellijUrl = computed(() => {
  return `idea://com.intellij.ide.starter?url=${encodeURIComponent(buildEncodeUrl())}`
})

const vscodeUrl = computed(() => {
  return `vscode://vscjava.vscode-spring-initializr/open?url=${encodeURIComponent(buildEncodeUrl())}`
})

const shareableUrl = computed(() => {
  const params = new URLSearchParams()
  for (const [k, v] of Object.entries(props.config)) {
    if (v !== undefined && v !== null) params.set(k, String(v))
  }
  return `${window.location.origin}/configure?${params.toString()}`
})

async function copyLink() {
  await navigator.clipboard.writeText(shareableUrl.value)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}
</script>

<template>
  <div class="space-y-4">
    <!-- IDE Deep Links -->
    <div>
      <h3 class="text-xs font-semibold text-neutral-500 uppercase tracking-wide mb-2">Open in IDE</h3>
      <div class="flex flex-col gap-2">
        <a :href="intellijUrl"
           class="flex items-center gap-2 px-3 py-2 border border-neutral-200 rounded-s text-sm text-neutral-900 hover:border-primary hover:text-primary transition-colors"
           :aria-label="`Open ${config.artifactId} in IntelliJ IDEA`">
          <span aria-hidden="true">🧩</span>
          Open in IntelliJ IDEA
        </a>
        <a :href="vscodeUrl"
           class="flex items-center gap-2 px-3 py-2 border border-neutral-200 rounded-s text-sm text-neutral-900 hover:border-primary hover:text-primary transition-colors"
           :aria-label="`Open ${config.artifactId} in VS Code`">
          <span aria-hidden="true">💻</span>
          Open in VS Code
        </a>
      </div>
    </div>

    <!-- Share -->
    <div>
      <h3 class="text-xs font-semibold text-neutral-500 uppercase tracking-wide mb-2">Share</h3>
      <button
        type="button"
        class="w-full flex items-center justify-center gap-2 px-3 py-2 border border-neutral-200 rounded-s text-sm text-neutral-500 hover:border-primary hover:text-primary transition-colors"
        :aria-label="copied ? 'Link copied!' : 'Copy shareable link'"
        @click="copyLink"
      >
        <span aria-hidden="true">{{ copied ? '✓' : '🔗' }}</span>
        {{ copied ? 'Link copied!' : 'Copy Shareable Link' }}
      </button>
    </div>
  </div>
</template>
