<script setup lang="ts">
import type { UseCaseExample } from '@/generated/types'

const props = defineProps<{ entry: UseCaseExample }>()
const emit = defineEmits<{ select: [entry: UseCaseExample] }>()

function handleSelect() {
  emit('select', props.entry)
}
</script>

<template>
  <article
    class="rounded-s border border-neutral-200 bg-neutral-0 p-6 hover:shadow-md hover:border-primary transition-all cursor-pointer flex flex-col"
    tabindex="0"
    :aria-label="entry.title"
    @click="handleSelect"
    @keydown.enter="handleSelect"
    @keydown.space.prevent="handleSelect"
  >
    <h3 class="text-base font-semibold text-neutral-900 mb-2">{{ entry.title }}</h3>
    <p class="text-sm text-neutral-500 leading-relaxed mb-3 flex-1">{{ entry.description }}</p>

    <div class="flex flex-wrap gap-2 mb-4">
      <span
        v-for="tag in entry.tags"
        :key="tag"
        :class="[
          'inline-flex text-xs font-medium px-2 py-1 rounded-full',
          tag === 'docker-compose'
            ? 'bg-amber-100 text-amber-700 border border-amber-300'
            : 'bg-secondary/20 text-primary',
        ]"
      >
        {{ tag }}
      </span>
    </div>

    <button
      type="button"
      class="w-full bg-neutral-0 border border-primary text-primary py-2 rounded-s font-medium hover:bg-primary hover:text-white transition-colors text-sm mt-auto"
      :aria-label="`Start ${entry.title} example`"
      @click.stop="handleSelect"
    >
      Start this example →
    </button>
  </article>
</template>
