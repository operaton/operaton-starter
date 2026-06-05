<script setup lang="ts">
import { ref, computed, watch, onBeforeUnmount, nextTick } from 'vue'
import hljs from 'highlight.js/lib/core'
import xml from 'highlight.js/lib/languages/xml'
import java from 'highlight.js/lib/languages/java'
import yaml from 'highlight.js/lib/languages/yaml'
import groovy from 'highlight.js/lib/languages/groovy'
import kotlin from 'highlight.js/lib/languages/kotlin'
import properties from 'highlight.js/lib/languages/properties'
import plaintext from 'highlight.js/lib/languages/plaintext'
import 'highlight.js/styles/github.css'
import type { TreeNode } from '@/utils/fileTreeBuilder'
import type { ProjectConfig } from '@/generated/types'
import { previewTemplate } from '@/generated/api'

hljs.registerLanguage('xml', xml)
hljs.registerLanguage('java', java)
hljs.registerLanguage('yaml', yaml)
hljs.registerLanguage('groovy', groovy)
hljs.registerLanguage('kotlin', kotlin)
hljs.registerLanguage('properties', properties)
hljs.registerLanguage('plaintext', plaintext)

const EXT_TO_LANG: Record<string, string> = {
  java: 'java',
  xml: 'xml',
  yaml: 'yaml',
  yml: 'yaml',
  gradle: 'groovy',
  kt: 'kotlin',
  kts: 'kotlin',
  properties: 'properties',
  sql: 'plaintext',
  txt: 'plaintext',
  md: 'plaintext',
}

const props = defineProps<{
  node: TreeNode | null
  config?: ProjectConfig
}>()

const copied = ref(false)
const copyError = ref(false)
const renderedContent = ref<string | null>(null)
const loading = ref(false)

const fileName = computed(() => props.node?.name ?? '')
const templateId = computed(() => props.node?.entry?.templateId ?? null)
const isBpmn = computed(() => fileName.value.endsWith('.bpmn'))

// BPMN viewer
const bpmnContainer = ref<HTMLElement | null>(null)
let bpmnViewer: any = null

async function destroyBpmnViewer() {
  if (bpmnViewer) {
    bpmnViewer.destroy()
    bpmnViewer = null
  }
}

async function renderBpmn(xml: string) {
  await nextTick()
  if (!bpmnContainer.value) return
  const { default: BpmnViewer } = await import('bpmn-js/lib/Viewer')
  if (!bpmnViewer) {
    bpmnViewer = new BpmnViewer({ container: bpmnContainer.value })
  }
  try {
    await bpmnViewer.importXML(xml)
    bpmnViewer.get('canvas').zoom('fit-viewport')
  } catch {
    // fall back to source view if XML is invalid
  }
}

// When the selected file changes, show previewContent immediately (raw template source),
// then asynchronously replace it with the rendered version from the API when config is available.
watch(
  [() => props.node, () => props.config],
  async ([node]) => {
    // Set content synchronously before any await so the UI updates immediately
    if (!node || !node.entry) {
      renderedContent.value = null
      return
    }
    renderedContent.value = node.entry.previewContent ?? null

    // Destroy previous BPMN viewer (async, but happens before we render new BPMN)
    await destroyBpmnViewer()

    // Fetch rendered version (with interpolated values) if config is provided
    if (templateId.value && props.config) {
      loading.value = true
      try {
        const fetched = await previewTemplate(templateId.value, props.config)
        if (fetched !== null) {
          renderedContent.value = fetched
        }
      } finally {
        loading.value = false
      }
    }

    if (isBpmn.value && renderedContent.value) {
      await renderBpmn(renderedContent.value)
    }
  },
  { immediate: true, deep: true }
)

onBeforeUnmount(destroyBpmnViewer)

const highlightedContent = computed(() => {
  if (isBpmn.value || !renderedContent.value) return null
  const ext = fileName.value.split('.').pop()?.toLowerCase() ?? ''
  const lang = EXT_TO_LANG[ext] ?? 'plaintext'
  return hljs.highlight(renderedContent.value, { language: lang }).value
})

async function copyContent() {
  if (!renderedContent.value) return
  try {
    await navigator.clipboard.writeText(renderedContent.value)
    copied.value = true
    copyError.value = false
    setTimeout(() => { copied.value = false }, 1500)
  } catch {
    copyError.value = true
    setTimeout(() => { copyError.value = false }, 2000)
  }
}
</script>

<template>
  <section
    v-if="node"
    aria-label="File content preview"
    class="flex flex-col min-w-0"
  >
    <div class="flex items-center justify-between mb-2 gap-2">
      <h3 class="text-xs font-semibold text-neutral-500 uppercase tracking-wide truncate" :title="fileName">
        {{ fileName }}
      </h3>
      <button
        type="button"
        :disabled="!renderedContent"
        class="text-xs px-2 py-0.5 border rounded shrink-0 focus-visible:outline-2 focus-visible:outline-primary disabled:opacity-40 disabled:cursor-not-allowed"
        :class="copyError
          ? 'border-red-300 text-red-500'
          : 'border-neutral-200 text-neutral-500 hover:text-primary hover:border-primary'"
        @click="copyContent"
      >
        {{ copyError ? 'Failed!' : copied ? 'Copied!' : 'Copy' }}
      </button>
    </div>

    <!-- Loading (only shown when no content available yet) -->
    <div v-if="loading && !renderedContent" class="font-mono text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 text-neutral-400 italic">
      Loading…
    </div>

    <!-- BPMN diagram -->
    <div
      v-else-if="isBpmn"
      ref="bpmnContainer"
      class="bg-white rounded-s border border-neutral-200 overflow-hidden"
      style="height: 420px; width: 100%"
    />

    <!-- Syntax-highlighted source -->
    <pre
      v-else-if="highlightedContent"
      class="text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 overflow-auto flex-1 m-0"
    ><code class="hljs" v-html="highlightedContent" /></pre>

    <!-- No preview -->
    <div
      v-else
      class="font-mono text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 text-neutral-500 italic"
    >
      No preview available
    </div>
  </section>
</template>
