import { computed, ref, type Ref } from 'vue'
import type { Example } from '@/generated/types'

export interface FilterState {
  query: string
  runtime: Set<string>
  buildSystem: Set<string>
  complexity: Set<string>
  integrations: Set<string>
}

export function useGalleryFilters(examples: Ref<Example[]>) {
  const filters = ref<FilterState>({
    query: '',
    runtime: new Set(),
    buildSystem: new Set(),
    complexity: new Set(),
    integrations: new Set(),
  })

  function toggleFilter(category: 'runtime' | 'buildSystem' | 'complexity' | 'integrations', value: string) {
    const filterSet = filters.value[category]
    if (filterSet.has(value)) {
      filterSet.delete(value)
    } else {
      filterSet.add(value)
    }
  }

  function setQuery(query: string) {
    filters.value.query = query
  }

  function clear() {
    filters.value.query = ''
    filters.value.runtime.clear()
    filters.value.buildSystem.clear()
    filters.value.complexity.clear()
    filters.value.integrations.clear()
  }

  const filteredExamples = computed(() => {
    let result = examples.value

    // Free-text search
    if (filters.value.query) {
      const q = filters.value.query.toLowerCase()
      result = result.filter(ex => {
        return (
          ex.title?.toLowerCase().includes(q) ||
          ex.shortDescription?.toLowerCase().includes(q) ||
          ex.tags?.some(tag => tag.label.toLowerCase().includes(q))
        )
      })
    }

    // Apply filters: AND across categories, OR within category
    if (filters.value.runtime.size > 0) {
      result = result.filter(ex =>
        ex.tags?.some(tag =>
          tag.category === 'RUNTIME' && filters.value.runtime.has(tag.label)
        )
      )
    }

    if (filters.value.buildSystem.size > 0) {
      result = result.filter(ex =>
        ex.tags?.some(tag =>
          tag.category === 'BUILD_SYSTEM' && filters.value.buildSystem.has(tag.label)
        )
      )
    }

    if (filters.value.complexity.size > 0) {
      result = result.filter(ex =>
        ex.tags?.some(tag =>
          tag.category === 'COMPLEXITY' && filters.value.complexity.has(tag.label)
        )
      )
    }

    if (filters.value.integrations.size > 0) {
      result = result.filter(ex =>
        ex.integrations?.some(int => filters.value.integrations.has(int))
      )
    }

    return result
  })

  const hasActiveFilters = computed(() =>
    filters.value.runtime.size > 0 ||
    filters.value.buildSystem.size > 0 ||
    filters.value.complexity.size > 0 ||
    filters.value.integrations.size > 0
  )

  return {
    filters,
    filteredExamples,
    hasActiveFilters,
    toggleFilter,
    setQuery,
    clear,
  }
}
