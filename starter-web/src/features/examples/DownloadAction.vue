<script setup lang="ts">
import { computed } from 'vue'
import type { DownloadStatus } from './useExampleDownload'

interface Props {
  status: DownloadStatus
  exampleId: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  download: []
  retry: []
}>()

const isDownloading = computed(() => props.status.state === 'downloading')
const isSuccess = computed(() => props.status.state === 'success')
const isError = computed(() => props.status.state === 'error')
</script>

<template>
  <div>
    <!-- Download button -->
    <button
      type="button"
      :disabled="isDownloading"
      class="download-button"
      @click="emit('download')"
    >
      <span v-if="isDownloading" class="inline-flex items-center gap-2">
        <svg
          class="animate-spin h-4 w-4"
          xmlns="http://www.w3.org/2000/svg"
          fill="none"
          viewBox="0 0 24 24"
        >
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
          <path
            class="opacity-75"
            fill="currentColor"
            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          />
        </svg>
        Downloading…
      </span>
      <span v-else>Download</span>
    </button>

    <!-- Success message -->
    <div v-if="isSuccess" class="success-message">
      Downloaded {{ exampleId }}.zip ✓
    </div>

    <!-- Error message -->
    <div v-if="isError" class="error-message">
      <p class="error-text">{{ props.status.error ?? 'Download failed' }}</p>
      <button
        type="button"
        class="retry-button"
        @click="emit('retry')"
      >
        Retry
      </button>
    </div>
  </div>
</template>

<style scoped>
.download-button {
  width: 100%;
  background: rgb(24, 74, 239);
  color: rgb(255, 255, 255);
  padding: 0.5rem 0.75rem;
  border-radius: 0.5em;
  font-weight: 500;
  font-size: 0.875rem;
  border: none;
  cursor: pointer;
  transition: background-color 150ms ease;
}

.download-button:hover:not(:disabled) {
  background: rgb(10, 45, 191);
}

.download-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.success-message {
  color: rgb(22, 163, 74);
  font-size: 0.875rem;
  font-weight: 500;
  margin-top: 0.5rem;
}

.error-message {
  background: rgb(254, 242, 242);
  border: 1px solid rgb(254, 202, 202);
  color: rgb(185, 28, 28);
  border-radius: 0.5em;
  padding: 0.5rem 0.75rem;
  font-size: 0.875rem;
  margin-top: 0.5rem;
  display: flex;
  items-align: center;
  gap: 0.5rem;
}

.error-text {
  flex: 1;
  margin: 0;
}

.retry-button {
  background: transparent;
  color: rgb(185, 28, 28);
  border: none;
  cursor: pointer;
  font-weight: 500;
  font-size: 0.875rem;
  padding: 0;
  text-decoration: underline;
}

.retry-button:hover {
  color: rgb(220, 38, 38);
}
</style>
