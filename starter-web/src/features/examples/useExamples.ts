import { computed, type Ref } from 'vue'
import { useMetadata } from '@/composables/useMetadata'
import type { Example } from '@/generated/types'

export function useExamples(): {
  examples: Ref<Example[]>
  allTags: Ref<string[]>
  allIntegrations: Ref<string[]>
  runtimes: Ref<string[]>
  buildSystems: Ref<string[]>
  complexities: Ref<string[]>
} {
  const { data: metadata } = useMetadata()

  const examples = computed(() => metadata.value?.examples ?? [])

  const allTags = computed(() => {
    const tags = new Set<string>()
    examples.value.forEach(ex => {
      ex.tags?.forEach(tag => tags.add(tag.label))
    })
    return Array.from(tags).sort()
  })

  const allIntegrations = computed(() => {
    const integrations = new Set<string>()
    examples.value.forEach(ex => {
      ex.integrations?.forEach(int => integrations.add(int))
    })
    return Array.from(integrations).sort()
  })

  const runtimes = computed(() => {
    const runtimes = new Set<string>()
    examples.value.forEach(ex => {
      ex.tags?.forEach(tag => {
        if (tag.category === 'RUNTIME') runtimes.add(tag.label)
      })
    })
    return Array.from(runtimes).sort()
  })

  const buildSystems = computed(() => {
    const buildSystems = new Set<string>()
    examples.value.forEach(ex => {
      ex.tags?.forEach(tag => {
        if (tag.category === 'BUILD_SYSTEM') buildSystems.add(tag.label)
      })
    })
    return Array.from(buildSystems).sort()
  })

  const complexities = computed(() => {
    const complexities = new Set<string>()
    examples.value.forEach(ex => {
      ex.tags?.forEach(tag => {
        if (tag.category === 'COMPLEXITY') complexities.add(tag.label)
      })
    })
    return Array.from(complexities).sort()
  })

  return {
    examples,
    allTags,
    allIntegrations,
    runtimes,
    buildSystems,
    complexities,
  }
}
