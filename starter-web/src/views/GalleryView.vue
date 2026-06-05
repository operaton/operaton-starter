<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMetadata } from '@/composables/useMetadata'
import ProjectTypeCard from '@/components/ProjectTypeCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import ErrorBanner from '@/components/ErrorBanner.vue'
import UseCaseCard, { type UseCase } from '@/components/UseCaseCard.vue'

const { data: metadata, isLoading, error } = useMetadata()
const router = useRouter()
const galleryRef = ref<HTMLElement | null>(null)
const useCasesRef = ref<HTMLElement | null>(null)

const USE_CASES: UseCase[] = [
  {
    title: 'Expense Approval',
    description: 'Multi-level approval workflow for employee expense reports with manager and finance sign-off.',
    icon: '💰',
    tags: ['Approvals', 'HR', 'Spring Boot'],
    query: {
      projectType: 'PROCESS_APPLICATION',
      buildSystem: 'MAVEN',
      javaVersion: '17',
      artifactId: 'expense-approval',
      projectName: 'Expense Approval',
      githubActions: 'true',
      dockerCompose: 'true',
      dependencyUpdater: 'RENOVATE',
    },
  },
  {
    title: 'Employee Onboarding',
    description: 'Structured onboarding process coordinating IT provisioning, HR paperwork, and team introductions.',
    icon: '🧑‍💼',
    tags: ['HR', 'Onboarding', 'Spring Boot'],
    query: {
      projectType: 'PROCESS_APPLICATION',
      buildSystem: 'MAVEN',
      javaVersion: '21',
      artifactId: 'employee-onboarding',
      projectName: 'Employee Onboarding',
      githubActions: 'true',
      dockerCompose: 'false',
      dependencyUpdater: 'RENOVATE',
    },
  },
  {
    title: 'IT Incident Management',
    description: 'Escalation and resolution workflow for IT incidents with SLA tracking and stakeholder notifications.',
    icon: '🚨',
    tags: ['ITSM', 'Escalation', 'Gradle Kotlin'],
    query: {
      projectType: 'PROCESS_APPLICATION',
      buildSystem: 'GRADLE_KOTLIN',
      javaVersion: '21',
      artifactId: 'incident-management',
      projectName: 'IT Incident Management',
      githubActions: 'true',
      dockerCompose: 'true',
      dependencyUpdater: 'RENOVATE',
    },
  },
  {
    title: 'Customer Order Fulfillment',
    description: 'End-to-end order processing from placement through payment, fulfillment, and delivery confirmation.',
    icon: '📦',
    tags: ['E-Commerce', 'Integration', 'Spring Boot'],
    query: {
      projectType: 'PROCESS_APPLICATION',
      buildSystem: 'MAVEN',
      javaVersion: '17',
      artifactId: 'order-fulfillment',
      projectName: 'Customer Order Fulfillment',
      githubActions: 'true',
      dockerCompose: 'true',
      dependencyUpdater: 'DEPENDABOT',
    },
  },
  {
    title: 'Leave Request',
    description: 'Simple leave approval process deployable to a shared Operaton engine — ideal for enterprise environments.',
    icon: '🏖️',
    tags: ['HR', 'Approvals', 'Process Archive'],
    query: {
      projectType: 'PROCESS_ARCHIVE',
      buildSystem: 'MAVEN',
      javaVersion: '17',
      artifactId: 'leave-request',
      projectName: 'Leave Request',
      deploymentTarget: 'TOMCAT',
      githubActions: 'true',
      dockerCompose: 'false',
      dependencyUpdater: 'RENOVATE',
    },
  },
  {
    title: 'DMN Decision Table',
    description: 'Standalone DMN decision project with a skeleton decision table, Spring Boot evaluator, and passing tests out of the box.',
    icon: '📊',
    tags: ['DMN', 'Decision Table', 'Rules Engine'],
    query: {
      projectType: 'DMN_PROJECT',
      buildSystem: 'MAVEN',
      javaVersion: '17',
      artifactId: 'my-decisions',
      projectName: 'My Decisions',
      githubActions: 'true',
      dockerCompose: 'false',
      dependencyUpdater: 'RENOVATE',
    },
  },
  {
    title: 'Document Review & Sign-off',
    description: 'Collaborative document review workflow with parallel reviewer tracks and sequential approval gates.',
    icon: '📄',
    tags: ['Compliance', 'Documents', 'Gradle Kotlin'],
    query: {
      projectType: 'PROCESS_APPLICATION',
      buildSystem: 'GRADLE_KOTLIN',
      javaVersion: '21',
      artifactId: 'document-review',
      projectName: 'Document Review',
      githubActions: 'true',
      dockerCompose: 'false',
      dependencyUpdater: 'RENOVATE',
    },
  },
]

function scrollToGallery() {
  galleryRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function scrollToUseCases() {
  useCasesRef.value?.scrollIntoView({ behavior: 'smooth' })
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
          @click="scrollToUseCases"
        >
          Browse Use Cases ↓
        </button>
        <button
          type="button"
          class="border border-neutral-300 text-neutral-600 px-6 py-3 rounded-s font-semibold text-base hover:bg-neutral-100 transition-colors"
          @click="scrollToGallery"
        >
          Project Types ↓
        </button>
      </div>
    </section>

    <!-- Gallery Section -->
    <section ref="galleryRef" class="py-12 px-6 md:px-8 max-w-content mx-auto border-b border-neutral-200">
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

    <!-- Use Cases Section -->
    <section ref="useCasesRef" class="py-12 px-6 md:px-8 max-w-content mx-auto">
      <div class="mb-8">
        <h2 class="text-2xl font-semibold text-neutral-900 mb-2">Start from a use case</h2>
        <p class="text-sm text-neutral-500">Pre-configured templates for common business processes. Pick one and customize from there.</p>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <UseCaseCard v-for="uc in USE_CASES" :key="uc.title" :use-case="uc" />
      </div>
    </section>
  </div>
</template>
