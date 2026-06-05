<script setup lang="ts">
import { ref, computed } from 'vue'
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
}

const props = defineProps<{
  node: TreeNode | null
}>()

const copied = ref(false)
const copyError = ref(false)

const rawContent = computed(() => props.node?.entry?.previewContent || null)
const fileName = computed(() => props.node?.name ?? '')

const highlightedContent = computed(() => {
  if (!rawContent.value) return null
  const ext = fileName.value.split('.').pop()?.toLowerCase() ?? ''
  const lang = EXT_TO_LANG[ext] ?? 'plaintext'
  return hljs.highlight(rawContent.value, { language: lang }).value
})

async function copyContent() {
  if (!rawContent.value) return
  try {
    await navigator.clipboard.writeText(rawContent.value)
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
        :disabled="!rawContent"
        class="text-xs px-2 py-0.5 border rounded shrink-0 focus-visible:outline-2 focus-visible:outline-primary disabled:opacity-40 disabled:cursor-not-allowed"
        :class="copyError
          ? 'border-red-300 text-red-500'
          : 'border-neutral-200 text-neutral-500 hover:text-primary hover:border-primary'"
        @click="copyContent"
      >
        {{ copyError ? 'Failed!' : copied ? 'Copied!' : 'Copy' }}
      </button>
    </div>
    <pre
      v-if="highlightedContent"
      class="text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 overflow-auto flex-1 m-0"
    ><code class="hljs" v-html="highlightedContent" /></pre>
    <div
      v-else
      class="font-mono text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 text-neutral-500 italic"
    >
      No preview available
    </div>
  </section>
</template>
