<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMetadata } from '@/composables/useMetadata'
import { useExamples } from '@/features/examples/useExamples'
import { useGalleryFilters } from '@/features/examples/useGalleryFilters'
import { useExampleDownload } from '@/features/examples/useExampleDownload'
import ProjectTypeCard from '@/components/ProjectTypeCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import GallerySearchBar from '@/features/examples/GallerySearchBar.vue'
import ExampleGalleryCard from '@/features/examples/ExampleGalleryCard.vue'
import ExamplesEmptyState from '@/features/examples/ExamplesEmptyState.vue'

const { data: metadata, isLoading, error } = useMetadata()
const router = useRouter()
const galleryRef = ref<HTMLElement | null>(null)
const examplesRef = ref<HTMLElement | null>(null)

// Composables for examples gallery
const { examples, runtimes, buildSystems, complexities, bpmnConcepts } = useExamples()
const { filters, filteredExamples, hasActiveFilters, toggleFilter, setQuery, clear } = useGalleryFilters(examples)
const { getStatus, download, retry } = useExampleDownload()

const integrations = computed(() => {
  const ints = new Set<string>()
  examples.value.forEach(ex => {
    ex.integrations?.forEach(int => ints.add(int))
  })
  return Array.from(ints).sort()
})

function scrollToGallery() {
  galleryRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function scrollToExamples() {
  examplesRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function goToConfigure() {
  router.push('/configure')
}

function handleExampleDownload(exampleId: string) {
  const example = examples.value.find(ex => ex.id === exampleId)
  if (example) {
    download(example)
  }
}
</script>

<template>
  <div>
    <!-- Hero Section -->
    <section class="bg-gradient-to-b from-neutral-0 to-neutral-50 border-b border-neutral-200 py-16 px-6 md:px-8 text-center">
      <h1 class="text-3xl md:text-4xl font-bold text-neutral-900 mb-3">
        Start your Operaton project
      </h1>
      <p class="text-lg text-neutral-500 max-w-xl mx-auto mb-8">
        Generate a ready-to-run Operaton project in seconds. No setup, no boilerplate — just download and build.
      </p>
      <div class="flex flex-col sm:flex-row gap-3 justify-center">
        <button
          type="button"
          class="bg-primary text-white px-6 py-3 rounded-s font-semibold text-base hover:bg-primary-dark transition-colors"
          @click="goToConfigure"
        >
          Configure Now →
        </button>
        <button
          type="button"
          class="border border-primary text-primary px-6 py-3 rounded-s font-semibold text-base hover:bg-primary/5 transition-colors"
          @click="scrollToGallery"
        >
          Project Types ↓
        </button>
        <button
          type="button"
          class="border border-primary text-primary px-6 py-3 rounded-s font-semibold text-base hover:bg-primary/5 transition-colors"
          @click="scrollToExamples"
        >
          Examples ↓
        </button>
      </div>
    </section>

    <!-- Gallery Section (Project Types) -->
    <section ref="galleryRef" class="py-12 px-6 md:px-8 max-w-content mx-auto border-b border-neutral-200">
      <div class="mb-8">
        <h2 class="text-2xl font-semibold text-neutral-900 mb-2">Project Types</h2>
        <p class="text-sm text-neutral-500">
          Choose the architecture that matches your needs. Each type generates a ready-to-build project.
        </p>
      </div>

      <ErrorBanner :error="error" />

      <!-- Loading skeletons -->
      <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <SkeletonCard v-for="n in 2" :key="n" />
      </div>

      <!-- Gallery cards -->
      <div v-else-if="metadata" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <ProjectTypeCard
          v-for="pt in metadata.projectTypes"
          :key="pt.id"
          :project-type="pt"
        />
      </div>
    </section>

    <!-- Examples Gallery Section -->
    <section
      v-if="examples.length > 0"
      ref="examplesRef"
      class="py-12 px-6 md:px-8 max-w-content mx-auto border-b border-neutral-200"
    >
      <div class="mb-8">
        <h2 class="text-2xl font-semibold text-neutral-900 mb-2">Examples</h2>
        <p class="text-sm text-neutral-500">
          Real-world runnable examples with source code. Download, build, and learn.
        </p>
      </div>

      <!-- Gallery Search Bar -->
      <GallerySearchBar
        :filtered-examples-count="filteredExamples.length"
        :runtimes="runtimes"
        :build-systems="buildSystems"
        :complexities="complexities"
        :integrations="integrations"
        :bpmn-concepts="bpmnConcepts"
        :active-filters="filters"
        @update:query="setQuery"
        @toggle-filter="(category, value) => toggleFilter(category as 'runtime' | 'buildSystem' | 'complexity' | 'integrations' | 'bpmnConcepts', value)"
        @clear-filters="clear"
      />

      <!-- Empty state or cards -->
      <div class="mt-6">
        <ExamplesEmptyState
          v-if="filteredExamples.length === 0"
          :has-active-filters="hasActiveFilters"
          @clear-filters="clear"
        />
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <ExampleGalleryCard
            v-for="example in filteredExamples"
            :key="example.id"
            :example="example"
            :download-status="getStatus(example.id ?? '')"
            @download="handleExampleDownload(example.id ?? '')"
            @retry="retry(example.id ?? '')"
          />
        </div>
      </div>
    </section>
  </div>
</template>
