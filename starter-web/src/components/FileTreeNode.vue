<script setup lang="ts">
import type { TreeNode } from '@/utils/fileTreeBuilder'

const props = defineProps<{
  node: TreeNode
  level: number
  selectedPath?: string | null
}>()

const emit = defineEmits<{ select: [node: TreeNode] }>()

const icon = props.node.isDir ? '📁' : '📄'

function handleSelect() {
  if (!props.node.isDir) {
    emit('select', props.node)
  }
}
</script>

<template>
  <li role="treeitem" :aria-expanded="node.isDir ? true : undefined" :aria-selected="!node.isDir && selectedPath === node.path">
    <span
      class="flex items-center gap-1 hover:text-primary cursor-pointer"
      :class="[
        node.isDir ? 'text-primary font-semibold' : 'text-neutral-900',
        !node.isDir && selectedPath === node.path ? 'bg-primary/10 rounded' : ''
      ]"
      :style="{ paddingLeft: `${level * 1.25}rem` }"
      :tabindex="node.isDir ? -1 : 0"
      @click="handleSelect"
      @keydown.enter.prevent="handleSelect"
    >
      <span aria-hidden="true">{{ icon }}</span>
      <span>{{ node.name }}</span>
    </span>
    <ul v-if="node.children?.length" role="group">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :level="level + 1"
        :selected-path="selectedPath"
        @select="emit('select', $event)"
      />
    </ul>
  </li>
</template>
