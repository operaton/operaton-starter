<script setup lang="ts">
import { ref, computed } from 'vue'
import { marked } from 'marked'
import type { Example } from '@/generated/types'
import { tagChipClasses, metadataBadgeClasses, integrationChipClasses, bpmnConceptChipClasses } from '@/utils/tagColors'
import DownloadAction from './DownloadAction.vue'
import type { DownloadStatus } from './useExampleDownload'

interface Props {
  example: Example
  downloadStatus: DownloadStatus
}

const props = defineProps<Props>()
const emit = defineEmits<{
  download: []
  retry: []
}>()

const detailsOpen = ref(false)

const hasIcon = computed(() => {
  return props.example.icon && props.example.icon.length > 0
})

const isEmojiIcon = computed(() => {
  if (!props.example.icon) return false
  // Simple heuristic: if it's a single character or emoji, treat as emoji
  return /^[\p{Emoji}\p{L}]$/u.test(props.example.icon)
})

const iconUrl = computed(() => {
  if (!props.example.icon) return null
  // If it looks like a path (starts with / or contains /), construct GitHub raw URL
  if (props.example.icon.startsWith('/') || props.example.icon.includes('/')) {
    return `https://raw.githubusercontent.com/${props.example.owner}/${props.example.repo}/${props.example.sourceRepoSha}${props.example.icon}`
  }
  return null
})

const shortSha = computed(() => {
  if (!props.example.sourceRepoSha) return ''
  return props.example.sourceRepoSha.substring(0, 7)
})

const screenshotUrls = computed(() =>
  (props.example.screenshots ?? []).map(path =>
    `https://raw.githubusercontent.com/${props.example.owner}/${props.example.repo}/${props.example.sourceRepoSha}/${path}`
  )
)

const hasDetails = computed(() => {
  return !!(
    props.example.longDescription ||
    props.example.bpmnConcepts?.length ||
    props.example.integrations?.length ||
    props.example.requires ||
    props.example.authors?.length ||
    props.example.license ||
    props.example.lastUpdated ||
    screenshotUrls.value.length
  )
})

function toggleDetails() {
  detailsOpen.value = !detailsOpen.value
}

function handleEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    detailsOpen.value = false
  }
}
</script>

<template>
  <article class="gallery-card">
    <!-- Icon and basic info -->
    <div class="card-header">
      <div v-if="hasIcon" class="icon-container">
        <span v-if="isEmojiIcon" class="emoji-icon">{{ example.icon }}</span>
        <img
          v-else-if="iconUrl"
          :src="iconUrl"
          :alt="example.title"
          class="image-icon"
          @error="(e: Event) => (e.target as HTMLImageElement).style.display = 'none'"
        />
        <svg v-else class="fallback-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2" />
          <circle cx="8.5" cy="8.5" r="1.5" />
          <polyline points="21 15 16 10 5 21" />
        </svg>
      </div>

      <div class="card-title-desc">
        <h3 class="card-title">
          {{ example.title }}
          <a
            v-if="example.documentationUrl"
            :href="example.documentationUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="doc-link"
            aria-label="Documentation"
          >📄</a>
        </h3>
        <p class="card-description">{{ example.shortDescription }}</p>
      </div>
    </div>

    <!-- Chips: runtime, integrations, bpmnConcepts, tags (excluding BPMN_CONCEPT) -->
    <div
      v-if="example.runtime || example.integrations?.length || example.bpmnConcepts?.length || example.tags?.length"
      class="tags-section"
    >
      <div class="tags-row">
        <span v-if="example.runtime" :class="['tag-badge', metadataBadgeClasses('RUNTIME')]">
          {{ example.runtime }}
        </span>
        <span
          v-for="integration in example.integrations"
          :key="`int-${integration}`"
          :class="['tag-badge', integrationChipClasses()]"
        >
          {{ integration }}
        </span>
        <span
          v-for="concept in example.bpmnConcepts"
          :key="`bpmn-${concept}`"
          :class="['tag-badge', bpmnConceptChipClasses()]"
        >
          {{ concept }}
        </span>
        <span
          v-for="tag in example.tags?.filter(t => t.category !== 'BPMN_CONCEPT')"
          :key="tag.label"
          :class="[
            'tag-badge',
            tag.category === 'RUNTIME' || tag.category === 'BUILD_SYSTEM' || tag.category === 'COMPLEXITY'
              ? metadataBadgeClasses(tag.category)
              : tagChipClasses(tag.category)
          ]"
        >
          {{ tag.label }}
        </span>
      </div>
    </div>

    <!-- Details disclosure -->
    <button
      v-if="hasDetails"
      type="button"
      class="details-button"
      :aria-expanded="detailsOpen"
      :aria-controls="`details-${example.id}`"
      @click="toggleDetails"
      @keydown="handleEscape"
    >
      <span class="w-4 h-4 rounded-full bg-neutral-200 text-neutral-500 text-xs inline-flex items-center justify-center font-bold mr-1">?</span>
      More about this example
    </button>

    <!-- Details panel -->
    <div
      v-if="hasDetails && detailsOpen"
      :id="`details-${example.id}`"
      role="note"
      class="details-panel"
      @keydown="handleEscape"
    >
      <!-- Long description -->
      <!-- ponytail: marked parses trusted content from our own GitHub repos -->
      <div v-if="example.longDescription" class="detail-section long-description" v-html="marked.parse(example.longDescription)" />

      <!-- Screenshots -->
      <div v-if="screenshotUrls.length" class="detail-section">
        <p class="detail-label">Screenshots</p>
        <div class="screenshots-row">
          <span
            v-for="(url, i) in screenshotUrls"
            :key="i"
            class="screenshot-thumb-wrapper"
          >
            <img :src="url" :alt="`Screenshot ${i + 1}`" class="screenshot-thumb" />
            <img :src="url" :alt="`Screenshot ${i + 1} preview`" class="screenshot-preview" />
          </span>
        </div>
      </div>

      <!-- Requires -->
      <div v-if="example.requires" class="detail-section">
        <p class="detail-label">Requires</p>
        <p class="detail-text">{{ example.requires }}</p>
      </div>

      <!-- Authors -->
      <div v-if="example.authors?.length" class="detail-section">
        <p class="detail-label">Authors</p>
        <ul class="authors-list">
          <li v-for="author in example.authors" :key="author.name">
            <a v-if="author.url" :href="author.url" target="_blank" rel="noopener noreferrer">
              {{ author.name }}
            </a>
            <span v-else>{{ author.name }}</span>
          </li>
        </ul>
      </div>

      <!-- License -->
      <div v-if="example.license" class="detail-section">
        <p class="detail-label">License</p>
        <p class="detail-text">{{ example.license }}</p>
      </div>

      <!-- Last updated & SHA -->
      <div v-if="example.lastUpdated || example.sourceRepoSha" class="detail-section">
        <p v-if="example.lastUpdated" class="detail-text">
          Last updated: {{ example.lastUpdated }}
        </p>
        <p v-if="example.sourceRepoSha" class="sha-text">
          {{ shortSha }}
        </p>
      </div>
    </div>

    <!-- Download action -->
    <div class="action-row">
      <DownloadAction
        :status="downloadStatus"
        :example-id="example.id ?? ''"
        @download="emit('download')"
        @retry="emit('retry')"
      />
      <a
        v-if="example.sourceRepoUrl"
        :href="example.sourceRepoUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="github-link"
      >
        View on GitHub
      </a>
    </div>
  </article>
</template>

<style scoped>
.gallery-card {
  border: 1px solid rgb(227, 212, 221);
  background: rgb(255, 255, 255);
  border-radius: 0.5em;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition: all 150ms ease;
}

.gallery-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: rgb(24, 74, 239);
}

.card-header {
  display: flex;
  gap: 1rem;
  align-items: flex-start;
}

.icon-container {
  width: 2.5rem;
  height: 2.5rem;
  min-width: 2.5rem;
  min-height: 2.5rem;
  background: rgb(245, 240, 243);
  border-radius: 0.5em;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  line-height: 1;
}

.emoji-icon {
  display: block;
  font-size: 1.5rem;
  line-height: 1;
}

.image-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 0.5em;
}

.fallback-icon {
  width: 1.5rem;
  height: 1.5rem;
  color: rgb(102, 102, 102);
}

.card-title-desc {
  flex: 1;
}

.card-title {
  font-size: 1rem;
  font-weight: 600;
  color: rgb(0, 0, 0);
  margin: 0 0 0.25rem 0;
}

.card-description {
  font-size: 0.875rem;
  color: rgb(102, 102, 102);
  margin: 0;
  line-height: 1.5;
}

.tags-section {
  margin-bottom: 0.5rem;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-badge {
  display: inline-flex;
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25em;
}

.details-button {
  align-self: flex-start;
  background: transparent;
  border: none;
  color: rgb(102, 102, 102);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  padding: 0;
  text-decoration: underline;
  transition: color 150ms ease;
}

.details-button:hover {
  color: rgb(24, 74, 239);
}

.details-button:focus {
  outline: 2px solid rgb(24, 74, 239);
  outline-offset: 2px;
}

.details-panel {
  border-top: 1px solid rgb(227, 212, 221);
  padding-top: 1rem;
  space-y: 1rem;
  font-size: 0.875rem;
  color: rgb(102, 102, 102);
}

.details-panel > * + * {
  margin-top: 1rem;
}

.detail-section {
  margin: 0;
}

.detail-label {
  font-weight: 600;
  color: rgb(0, 0, 0);
  margin: 0 0 0.5rem 0;
  font-size: 0.875rem;
}

.detail-text {
  margin: 0;
  line-height: 1.5;
}

.long-description :deep(p) { margin: 0 0 0.5rem 0; line-height: 1.5; }
.long-description :deep(ul) { margin: 0 0 0.5rem 1.25rem; padding: 0; list-style: disc; }
.long-description :deep(li) { margin-bottom: 0.25rem; line-height: 1.5; }
.long-description :deep(strong) { font-weight: 600; }
.long-description :deep(code) { font-family: monospace; font-size: 0.85em; background: rgba(0,0,0,0.06); padding: 0.1em 0.3em; border-radius: 3px; }

.detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.detail-tag {
  display: inline-block;
  background: rgb(245, 240, 243);
  color: rgb(102, 102, 102);
  border: 1px solid rgb(227, 212, 221);
  font-size: 0.75rem;
  font-weight: 500;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25em;
}

.authors-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.authors-list li {
  margin-bottom: 0.25rem;
}

.authors-list a {
  color: rgb(24, 74, 239);
  text-decoration: none;
}

.authors-list a:hover {
  text-decoration: underline;
}

.sha-text {
  font-family: JetBrains Mono, Fira Code, monospace;
  font-size: 0.75rem;
  color: rgb(102, 102, 102);
  margin: 0;
  word-break: break-all;
}

.action-row {
  margin-top: auto;
  padding-top: 1rem;
  border-top: 1px solid rgb(227, 212, 221);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.github-link {
  font-size: 0.875rem;
  color: rgb(102, 102, 102);
  text-decoration: none;
  align-self: flex-start;
}

.github-link:hover {
  color: rgb(24, 74, 239);
  text-decoration: underline;
}

.doc-link {
  margin-left: 0.4rem;
  text-decoration: none;
  font-size: 1rem;
  vertical-align: middle;
  opacity: 0.75;
  transition: opacity 150ms ease;
}

.doc-link:hover {
  opacity: 1;
}

.screenshots-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.screenshot-thumb-wrapper {
  position: relative;
  display: inline-block;
}

.screenshot-thumb {
  width: 3rem;
  height: 3rem;
  object-fit: cover;
  border-radius: 0.25em;
  border: 1px solid rgb(227, 212, 221);
  cursor: pointer;
}

.screenshot-preview {
  display: none;
  position: absolute;
  bottom: calc(100% + 0.5rem);
  left: 0;
  max-width: 20rem;
  max-height: 15rem;
  object-fit: contain;
  border-radius: 0.5em;
  border: 1px solid rgb(227, 212, 221);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  z-index: 50;
  background: white;
}

.screenshot-thumb-wrapper:hover .screenshot-preview {
  display: block;
}
</style>
