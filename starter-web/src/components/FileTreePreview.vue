<script setup lang="ts">
import { computed } from 'vue'
import type { ProjectConfig, TemplateManifestEntry } from '@/generated/types'
import { buildFileTree, type TreeNode } from '@/utils/fileTreeBuilder'
import FileTreeNode from './FileTreeNode.vue'

const props = defineProps<{
  manifest: TemplateManifestEntry[]
  config: ProjectConfig
}>()

const tree = computed(() => buildFileTree(props.manifest, props.config))
</script>

<template>
  <section aria-label="File structure preview" aria-live="polite">
    <h2 class="text-xs font-semibold text-neutral-500 uppercase tracking-wide mb-3">
      File Structure Preview
    </h2>
    <div class="font-mono text-sm bg-neutral-50 rounded-s p-4 border border-neutral-200 overflow-auto">
      <ul role="tree" class="space-y-0.5">
        <FileTreeNode v-for="node in tree" :key="node.path" :node="node" :level="0" />
      </ul>
    </div>
  </section>
</template>
