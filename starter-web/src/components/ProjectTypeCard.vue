<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { ProjectTypeInfo } from '@/generated/types'

const props = defineProps<{ projectType: ProjectTypeInfo }>()
const router = useRouter()
const helpOpen = ref(false)

function handleSelect() {
  router.push({ path: '/configure', query: { projectType: props.projectType.id } })
}
</script>

<template>
  <article
    class="rounded-s border border-neutral-200 bg-neutral-0 p-6 hover:shadow-md hover:border-primary transition-all cursor-pointer flex flex-col"
    tabindex="0"
    :aria-label="projectType.displayName"
    @click="handleSelect"
    @keydown.enter="handleSelect"
  >
    <h3 class="text-xl font-semibold text-neutral-900 mb-2">{{ projectType.displayName }}</h3>
    <p class="text-sm text-neutral-500 leading-relaxed mb-3 flex-1">{{ projectType.description }}</p>

    <div class="flex flex-wrap gap-2 mb-3">
      <span v-for="tag in projectType.tags" :key="tag"
            class="inline-flex bg-secondary/20 text-primary text-xs font-medium px-2 py-1 rounded-full">
        {{ tag }}
      </span>
    </div>

    <p class="text-sm text-primary font-medium mb-3">{{ projectType.personaHint }}</p>

    <button
      type="button"
      class="text-sm text-neutral-500 hover:text-primary flex items-center gap-1 mb-2 w-fit"
      :aria-expanded="helpOpen"
      :aria-controls="`help-${projectType.id}`"
      @click.stop="helpOpen = !helpOpen"
    >
      <span class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center font-bold" aria-hidden="true">?</span>
      More about this project type
    </button>
    <div v-show="helpOpen" :id="`help-${projectType.id}`" role="note"
         class="text-sm text-neutral-500 border-t border-neutral-200 pt-2 mb-3">
      {{ projectType.description }}
    </div>

    <button
      type="button"
      class="w-full bg-primary text-white py-2 rounded-s font-medium hover:bg-primary-dark transition-colors text-sm mt-auto"
      :aria-label="`Configure ${projectType.displayName} project`"
      @click.stop="handleSelect"
    >
      Configure →
    </button>
  </article>
</template>
