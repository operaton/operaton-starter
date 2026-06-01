<script setup lang="ts">
import { useRouter } from 'vue-router'

export interface UseCase {
  title: string
  description: string
  icon: string
  tags: string[]
  query: Record<string, string>
}

const props = defineProps<{ useCase: UseCase }>()
const router = useRouter()

function handleSelect() {
  router.push({ path: '/configure', query: props.useCase.query })
}
</script>

<template>
  <article
    class="rounded-s border border-neutral-200 bg-neutral-0 p-6 hover:shadow-md hover:border-primary transition-all cursor-pointer flex flex-col"
    tabindex="0"
    :aria-label="useCase.title"
    @click="handleSelect"
    @keydown.enter="handleSelect"
  >
    <div class="text-2xl mb-3">{{ useCase.icon }}</div>
    <h3 class="text-base font-semibold text-neutral-900 mb-2">{{ useCase.title }}</h3>
    <p class="text-sm text-neutral-500 leading-relaxed mb-3 flex-1">{{ useCase.description }}</p>

    <div class="flex flex-wrap gap-2 mb-4">
      <span
        v-for="tag in useCase.tags"
        :key="tag"
        class="inline-flex bg-secondary/20 text-primary text-xs font-medium px-2 py-1 rounded-full"
      >
        {{ tag }}
      </span>
    </div>

    <button
      type="button"
      class="w-full bg-neutral-0 border border-primary text-primary py-2 rounded-s font-medium hover:bg-primary hover:text-white transition-colors text-sm mt-auto"
      :aria-label="`Use ${useCase.title} template`"
      @click.stop="handleSelect"
    >
      Use this template →
    </button>
  </article>
</template>
