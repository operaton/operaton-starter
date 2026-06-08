<script setup lang="ts">
import { ref, computed } from 'vue'
import type { UseCaseExample } from '@/generated/types'

const props = defineProps<{ entry: UseCaseExample }>()
const emit = defineEmits<{ select: [entry: UseCaseExample] }>()
const detailsOpen = ref(false)

const hasDetails = computed(() =>
  !!(props.entry.processSummary || props.entry.bpmnConcepts?.length || props.entry.integrations?.length || props.entry.learnings?.length)
)

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
      v-if="hasDetails"
      type="button"
      class="text-sm text-neutral-500 hover:text-primary flex items-center gap-1 mb-2 w-fit"
      :aria-expanded="detailsOpen"
      :aria-controls="`details-${entry.useCaseId}`"
      @click.stop="detailsOpen = !detailsOpen"
    >
      <span class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center font-bold" aria-hidden="true">?</span>
      More about this use case
    </button>

    <div
      v-if="hasDetails"
      v-show="detailsOpen"
      :id="`details-${entry.useCaseId}`"
      role="note"
      class="text-sm text-neutral-600 border-t border-neutral-200 pt-3 mb-3 space-y-3"
      @click.stop
    >
      <p v-if="entry.processSummary" class="leading-relaxed">{{ entry.processSummary }}</p>

      <div v-if="entry.bpmnConcepts?.length">
        <p class="font-medium text-neutral-700 mb-1">BPMN concepts</p>
        <div class="flex flex-wrap gap-1">
          <span
            v-for="concept in entry.bpmnConcepts"
            :key="concept"
            class="inline-flex bg-blue-50 text-blue-700 border border-blue-200 text-xs font-medium px-2 py-0.5 rounded-full"
          >{{ concept }}</span>
        </div>
      </div>

      <div v-if="entry.integrations?.length">
        <p class="font-medium text-neutral-700 mb-1">Integrations</p>
        <div class="flex flex-wrap gap-1">
          <span
            v-for="integration in entry.integrations"
            :key="integration"
            class="inline-flex bg-neutral-100 text-neutral-600 border border-neutral-200 text-xs font-medium px-2 py-0.5 rounded-full"
          >{{ integration }}</span>
        </div>
      </div>

      <div v-if="entry.learnings?.length">
        <p class="font-medium text-neutral-700 mb-1">What you'll learn</p>
        <ul class="list-disc list-inside space-y-0.5 text-neutral-600">
          <li v-for="learning in entry.learnings" :key="learning">{{ learning }}</li>
        </ul>
      </div>
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
