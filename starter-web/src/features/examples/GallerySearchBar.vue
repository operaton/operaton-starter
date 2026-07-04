<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import FilterChip from './FilterChip.vue'

interface Props {
  filteredExamplesCount: number
  runtimes: string[]
  buildSystems: string[]
  complexities: string[]
  integrations: string[]
  bpmnConcepts: string[]
  activeFilters: {
    runtime: Set<string>
    buildSystem: Set<string>
    complexity: Set<string>
    integrations: Set<string>
    bpmnConcepts: Set<string>
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

const statusMessage = computed(() => {
  if (props.filteredExamplesCount === 0) {
    return 'No results found'
  }
  if (props.filteredExamplesCount === 1) {
    return '1 example found'
  }
  return `${props.filteredExamplesCount} examples found`
})

watch(searchInput, (value) => {
  if (debounceTimer.value !== null) {
    clearTimeout(debounceTimer.value)
  }
  debounceTimer.value = window.setTimeout(() => {
    emit('update:query', value)
  }, 200)
})

function toggleFilter(category: 'runtime' | 'buildSystem' | 'complexity' | 'integrations' | 'bpmnConcepts', value: string) {
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
          aria-label="Search examples"
          placeholder="Search examples…"
        />
      </div>

      <details class="filter-details">
        <summary class="filter-summary">Filters</summary>
        <div class="filter-groups">
          <div v-if="runtimes.length > 0" class="filter-group">
            <div role="toolbar" aria-label="Filter by runtime">
              <FilterChip
                v-for="runtime in runtimes"
                :key="`runtime-${runtime}`"
                :label="runtime"
                :is-active="activeFilters.runtime.has(runtime)"
                :category="'runtime'"
                color-classes="bg-neutral-50 text-neutral-900 border border-neutral-200"
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
                color-classes="bg-neutral-50 text-neutral-900 border border-neutral-200"
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
                color-classes="bg-neutral-50 text-neutral-900 border border-neutral-200"
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
                color-classes="bg-amber-100 text-amber-800"
                @toggle="toggleFilter('integrations', integration)"
              />
            </div>
          </div>

          <div v-if="bpmnConcepts.length > 0" class="filter-group">
            <div role="toolbar" aria-label="Filter by BPMN concept">
              <FilterChip
                v-for="concept in bpmnConcepts"
                :key="`bpmnConcept-${concept}`"
                :label="concept"
                :is-active="activeFilters.bpmnConcepts.has(concept)"
                :category="'bpmnConcepts'"
                color-classes="bg-blue-100 text-blue-800"
                @toggle="toggleFilter('bpmnConcepts', concept)"
              />
            </div>
          </div>
        </div>
      </details>

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

.filter-details {
  margin-top: 0.5rem;
}

.filter-summary {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: rgb(102, 102, 102);
  cursor: pointer;
  user-select: none;
  padding: 0.25rem 0;
  list-style: none;
}

.filter-summary::-webkit-details-marker { display: none; }

.filter-summary::before {
  content: '▶';
  font-size: 0.65rem;
  transition: transform 150ms ease;
}

.filter-details[open] .filter-summary::before {
  transform: rotate(90deg);
}

.filter-summary:hover {
  color: rgb(24, 74, 239);
}

.filter-groups {
  padding-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.filter-group {
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
