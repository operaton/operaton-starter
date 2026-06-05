<script setup lang="ts">
import { ref, computed } from 'vue'
import type { TreeNode } from '@/utils/fileTreeBuilder'

const props = defineProps<{
  node: TreeNode | null
}>()

const copied = ref(false)

const content = computed(() => props.node?.entry?.previewContent ?? null)
const fileName = computed(() => props.node?.name ?? '')

async function copyContent() {
  if (!content.value) return
  await navigator.clipboard.writeText(content.value)
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 1500)
}
</script>

<template>
  <section
    v-if="node"
    aria-label="File content preview"
    class="flex flex-col min-w-0"
  >
    <div class="flex items-center justify-between mb-2 gap-2">
      <h3 class="text-xs font-semibold text-neutral-500 uppercase tracking-wide truncate">
        {{ fileName }}
      </h3>
      <button
        v-if="content"
        type="button"
        class="text-xs px-2 py-0.5 border border-neutral-200 rounded text-neutral-500 hover:text-primary hover:border-primary shrink-0 focus-visible:outline-2 focus-visible:outline-primary"
        @click="copyContent"
      >
        {{ copied ? 'Copied!' : 'Copy' }}
      </button>
    </div>
    <div
      v-if="content"
      class="font-mono text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 overflow-auto flex-1 whitespace-pre"
    >{{ content }}</div>
    <div
      v-else
      class="font-mono text-xs bg-neutral-50 rounded-s p-4 border border-neutral-200 text-neutral-500 italic"
    >
      No preview available
    </div>
  </section>
</template>
