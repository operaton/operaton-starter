<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMetadata } from '@/composables/useMetadata'
import ProjectTypeCard from '@/components/ProjectTypeCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'

const { data: metadata, isLoading, error } = useMetadata()
const router = useRouter()
const galleryRef = ref<HTMLElement | null>(null)

function scrollToGallery() {
  galleryRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function goToConfigure() {
  router.push('/configure')
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
          Browse Project Types ↓
        </button>
      </div>
    </section>

    <!-- Gallery Section -->
    <section ref="galleryRef" class="py-12 px-6 md:px-8 max-w-content mx-auto">
      <h2 class="text-2xl font-semibold text-neutral-900 mb-8">Choose a project type</h2>

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
  </div>
</template>
