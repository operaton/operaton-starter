<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { ProjectConfig, TemplateManifestEntry } from '@/generated/types'
import { buildFileTree, type TreeNode } from '@/utils/fileTreeBuilder'
import FileTreeNode from './FileTreeNode.vue'
import FileContentPane from './FileContentPane.vue'

const props = defineProps<{
  manifest: TemplateManifestEntry[]
  config: ProjectConfig
}>()

const tree = computed(() => buildFileTree(props.manifest, props.config))
const selectedFile = ref<TreeNode | null>(null)

function handleSelect(node: TreeNode) {
  selectedFile.value = node
}

watch(() => props.manifest, () => { selectedFile.value = null })
</script>

<template>
  <section aria-label="File structure preview">
    <h2 class="text-xs font-semibold text-neutral-500 uppercase tracking-wide mb-3">
      File Structure Preview
    </h2>
    <div class="flex flex-col md:flex-row gap-4">
      <!-- File tree -->
      <div class="font-mono text-sm bg-neutral-50 rounded-s p-4 border border-neutral-200 overflow-auto md:w-64 shrink-0">
        <ul role="tree" class="space-y-0.5">
          <FileTreeNode
            v-for="node in tree"
            :key="node.path"
            :node="node"
            :level="0"
            :selected-path="selectedFile?.path ?? null"
            @select="handleSelect"
          />
        </ul>
      </div>
      <!-- Content pane — live region persists so screen readers announce content changes -->
      <div aria-live="polite" class="flex-1 min-w-0">
        <FileContentPane
          v-if="selectedFile"
          :node="selectedFile"
        />
      </div>
    </div>
  </section>
</template>
