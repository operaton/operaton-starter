import { describe, it, expect } from 'vitest'
import { ref } from 'vue'
import { useGalleryFilters } from '../useGalleryFilters'
import type { Example } from '@/generated/types'

function makeExample(overrides: Partial<Example> = {}): Example {
  return {
    id: 'example-1',
    title: 'Example',
    path: 'example',
    shortDescription: 'A short description',
    ...overrides,
  }
}

describe('useGalleryFilters', () => {
  it('returns all examples when no filters applied', () => {
    const examples = ref([
      makeExample({ id: 'ex1', title: 'Spring Boot' }),
      makeExample({ id: 'ex2', title: 'Quarkus' }),
    ])
    const { filteredExamples } = useGalleryFilters(examples)
    expect(filteredExamples.value.length).toBe(2)
  })

  it('filters by free-text query in title', () => {
    const examples = ref([
      makeExample({ id: 'ex1', title: 'Spring Boot Example' }),
      makeExample({ id: 'ex2', title: 'Quarkus Example' }),
    ])
    const { filters, filteredExamples, setQuery } = useGalleryFilters(examples)
    setQuery('Spring')
    expect(filteredExamples.value.length).toBe(1)
    expect(filteredExamples.value[0].id).toBe('ex1')
  })

  it('filters by free-text query in description', async () => {
    const examples = ref([
      makeExample({
        id: 'ex1',
        title: 'Example 1',
        shortDescription: 'Microservices architecture',
      }),
      makeExample({
        id: 'ex2',
        title: 'Example 2',
        shortDescription: 'Monolithic architecture',
      }),
    ])
    const { filters, filteredExamples, setQuery } = useGalleryFilters(examples)
    // Call setQuery directly (bypassing debounce in composable)
    filters.value.query = 'Microservices'
    expect(filteredExamples.value.length).toBe(1)
    expect(filteredExamples.value[0].id).toBe('ex1')
  })

  it('filters by runtime tag (OR within category)', () => {
    const examples = ref([
      makeExample({
        id: 'ex1',
        tags: [{ label: 'Spring Boot', category: 'RUNTIME' }],
      }),
      makeExample({
        id: 'ex2',
        tags: [{ label: 'Quarkus', category: 'RUNTIME' }],
      }),
      makeExample({
        id: 'ex3',
        tags: [{ label: 'Node.js', category: 'RUNTIME' }],
      }),
    ])
    const { toggleFilter, filteredExamples } = useGalleryFilters(examples)
    toggleFilter('runtime', 'Spring Boot')
    expect(filteredExamples.value.length).toBe(1)
    toggleFilter('runtime', 'Quarkus')
    expect(filteredExamples.value.length).toBe(2) // OR within category
  })

  it('filters across categories with AND logic', () => {
    const examples = ref([
      makeExample({
        id: 'ex1',
        tags: [
          { label: 'Spring Boot', category: 'RUNTIME' },
          { label: 'Maven', category: 'BUILD_SYSTEM' },
        ],
      }),
      makeExample({
        id: 'ex2',
        tags: [
          { label: 'Spring Boot', category: 'RUNTIME' },
          { label: 'Gradle', category: 'BUILD_SYSTEM' },
        ],
      }),
    ])
    const { toggleFilter, filteredExamples } = useGalleryFilters(examples)
    toggleFilter('runtime', 'Spring Boot')
    expect(filteredExamples.value.length).toBe(2)
    toggleFilter('buildSystem', 'Maven')
    expect(filteredExamples.value.length).toBe(1) // AND across categories
    expect(filteredExamples.value[0].id).toBe('ex1')
  })

  it('filters by integrations', () => {
    const examples = ref([
      makeExample({
        id: 'ex1',
        integrations: ['Kafka', 'REST'],
      }),
      makeExample({
        id: 'ex2',
        integrations: ['REST', 'Database'],
      }),
    ])
    const { toggleFilter, filteredExamples } = useGalleryFilters(examples)
    toggleFilter('integrations', 'Kafka')
    expect(filteredExamples.value.length).toBe(1)
    expect(filteredExamples.value[0].id).toBe('ex1')
  })

  it('clears all filters', () => {
    const examples = ref([
      makeExample({
        id: 'ex1',
        title: 'Spring Boot',
        tags: [{ label: 'Spring Boot', category: 'RUNTIME' }],
      }),
    ])
    const { toggleFilter, setQuery, clear, filteredExamples, hasActiveFilters } = useGalleryFilters(examples)
    setQuery('Spring')
    toggleFilter('runtime', 'Spring Boot')
    expect(filteredExamples.value.length).toBe(1)
    expect(hasActiveFilters.value).toBe(true)
    clear()
    expect(hasActiveFilters.value).toBe(false)
    expect(filteredExamples.value.length).toBe(1)
  })

  it('hasActiveFilters is true only when filters are applied', () => {
    const examples = ref([makeExample()])
    const { toggleFilter, hasActiveFilters } = useGalleryFilters(examples)
    expect(hasActiveFilters.value).toBe(false)
    toggleFilter('runtime', 'Spring Boot')
    expect(hasActiveFilters.value).toBe(true)
  })

  it('handles case-insensitive search', () => {
    const examples = ref([
      makeExample({ id: 'ex1', title: 'Spring Boot' }),
      makeExample({ id: 'ex2', title: 'QUARKUS' }),
    ])
    const { setQuery, filteredExamples } = useGalleryFilters(examples)
    setQuery('spring')
    expect(filteredExamples.value.length).toBe(1)
    expect(filteredExamples.value[0].id).toBe('ex1')
  })
})
