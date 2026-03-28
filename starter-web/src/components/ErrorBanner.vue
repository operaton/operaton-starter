<script setup lang="ts">
import type { ProblemDetail } from '@/generated/types'
defineProps<{ error: ProblemDetail | null }>()
</script>

<template>
  <div v-if="error" role="alert" aria-live="assertive"
       class="rounded-s border border-red-200 bg-red-50 p-4 flex gap-3 items-start">
    <span class="text-red-600 text-xl" aria-hidden="true">⚠</span>
    <div>
      <p class="font-semibold text-red-700">{{ error.title }}</p>
      <p v-if="error.detail" class="text-sm text-red-600 mt-1">{{ error.detail }}</p>
    </div>
  </div>
</template>
