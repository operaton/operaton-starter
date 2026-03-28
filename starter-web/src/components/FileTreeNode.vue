<script setup lang="ts">
import type { TreeNode } from '@/utils/fileTreeBuilder'

const props = defineProps<{ node: TreeNode; level: number }>()
const icon = props.node.isDir ? '📁' : '📄'
</script>

<template>
  <li role="treeitem" :aria-expanded="node.isDir ? true : undefined">
    <span class="flex items-center gap-1 text-neutral-900 hover:text-primary"
          :style="{ paddingLeft: `${level * 1.25}rem` }">
      <span aria-hidden="true">{{ icon }}</span>
      <span :class="node.isDir ? 'text-primary font-semibold' : ''">{{ node.name }}</span>
    </span>
    <ul v-if="node.children?.length" role="group">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :level="level + 1"
      />
    </ul>
  </li>
</template>
