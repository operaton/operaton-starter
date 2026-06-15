<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import FilterChip from './FilterChip.vue'
import type { Example } from '@/generated/types'

interface Props {
  filteredExamplesCount: number
  filteredUseCasesCount: number
  runtimes: string[]
  buildSystems: string[]
  complexities: string[]
  integrations: string[]
  activeFilters: {
    runtime: Set<string>
    buildSystem: Set<string>
    complexity: Set<string>
    integrations: Set<string>
  }
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:query': [value: string]
  'toggle-filter': [category: string, value: string]
  'clear-filters': []
}>()

const searchInput = ref('')
const debounceTimer = ref<number | null>(null)

const totalResults = computed(
  () => props.filteredExamplesCount + props.filteredUseCasesCount
)

const statusMessage = computed(() => {
  if (props.filteredExamplesCount === 0 && props.filteredUseCasesCount === 0) {
    return 'No results found'
  }
  if (props.filteredExamplesCount === 1 && props.filteredUseCasesCount === 0) {
    return '1 example found'
  }
  if (props.filteredExamplesCount !== 1 && props.filteredUseCasesCount === 0) {
    return `${props.filteredExamplesCount} examples found`
  }
  return `${totalResults.value} results found`
})

watch(searchInput, (value) => {
  if (debounceTimer.value !== null) {
    clearTimeout(debounceTimer.value)
  }
  debounceTimer.value = window.setTimeout(() => {
    emit('update:query', value)
  }, 200)
})

function toggleFilter(category: 'runtime' | 'buildSystem' | 'complexity' | 'integrations', value: string) {
  emit('toggle-filter', category, value)
}

function clearFilters() {
  emit('clear-filters')
}
</script>

<template>
  <div class="search-bar-sticky">
    <div class="search-bar-content">
      <div class="search-input-wrapper">
        <input
          v-model="searchInput"
          type="search"
          class="search-input"
          aria-label="Search examples and use cases"
          placeholder="Search examples and use cases…"
        />
      </div>

      <div v-if="runtimes.length > 0" class="filter-group">
        <div role="toolbar" aria-label="Filter by runtime">
          <FilterChip
            v-for="runtime in runtimes"
            :key="`runtime-${runtime}`"
            :label="runtime"
            :is-active="activeFilters.runtime.has(runtime)"
            :category="'runtime'"
            @toggle="toggleFilter('runtime', runtime)"
          />
        </div>
      </div>

      <div v-if="buildSystems.length > 0" class="filter-group">
        <div role="toolbar" aria-label="Filter by build system">
          <FilterChip
            v-for="buildSystem in buildSystems"
            :key="`buildSystem-${buildSystem}`"
            :label="buildSystem"
            :is-active="activeFilters.buildSystem.has(buildSystem)"
            :category="'buildSystem'"
            @toggle="toggleFilter('buildSystem', buildSystem)"
          />
        </div>
      </div>

      <div v-if="complexities.length > 0" class="filter-group">
        <div role="toolbar" aria-label="Filter by complexity">
          <FilterChip
            v-for="complexity in complexities"
            :key="`complexity-${complexity}`"
            :label="complexity"
            :is-active="activeFilters.complexity.has(complexity)"
            :category="'complexity'"
            @toggle="toggleFilter('complexity', complexity)"
          />
        </div>
      </div>

      <div v-if="integrations.length > 0" class="filter-group">
        <div role="toolbar" aria-label="Filter by integration">
          <FilterChip
            v-for="integration in integrations"
            :key="`integration-${integration}`"
            :label="integration"
            :is-active="activeFilters.integrations.has(integration)"
            :category="'integrations'"
            @toggle="toggleFilter('integrations', integration)"
          />
        </div>
      </div>

      <span role="status" aria-live="polite" class="sr-only">
        {{ statusMessage }}
      </span>
    </div>
  </div>
</template>

<style scoped>
.search-bar-sticky {
  position: sticky;
  top: 4rem;
  background: rgb(255, 255, 255);
  border-bottom: 1px solid rgb(227, 212, 221);
  padding: 1rem 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  z-index: 40;
}

.search-bar-content {
  max-width: 80rem;
  margin: 0 auto;
  padding: 0 1.5rem;
}

.search-input-wrapper {
  margin-bottom: 1rem;
}

.search-input {
  width: 100%;
  background: rgb(255, 255, 255);
  border: 1px solid rgb(227, 212, 221);
  border-radius: 0.5em;
  padding: 0.75rem;
  font-size: 0.875rem;
  transition: border-color 150ms ease;
}

.search-input:focus {
  outline: 2px solid rgb(24, 74, 239);
  outline-offset: 2px;
  border-color: rgb(24, 74, 239);
}

.filter-group {
  margin-bottom: 0.5rem;
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.filter-group [role="toolbar"] {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>
