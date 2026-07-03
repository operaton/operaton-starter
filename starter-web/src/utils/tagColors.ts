import type { TagCategory } from '@/generated/types'

export function tagChipClasses(category: TagCategory | undefined): string {
  switch (category) {
    case 'BPMN_CONCEPT':
      return 'bg-blue-100 text-blue-800'
    case 'TECHNOLOGY':
      return 'bg-amber-100 text-amber-800'
    case 'PLATFORM':
      return 'bg-green-100 text-green-800'
    case 'STANDARD':
      return 'bg-purple-100 text-purple-800'
    default:
      return 'bg-neutral-100 text-neutral-600'
  }
}

/**
 * Returns classes for metadata badge styling (runtime, buildSystem, complexity).
 * These render in the monochrome metadata-badge lane per Architecture A10.
 */
export function metadataBadgeClasses(category: TagCategory | undefined): string {
  if (category === 'RUNTIME' || category === 'BUILD_SYSTEM' || category === 'COMPLEXITY') {
    return 'bg-neutral-50 text-neutral-900 border border-neutral-200'
  }
  return 'bg-neutral-50 text-neutral-900 border border-neutral-200'
}

export function integrationChipClasses(): string {
  return 'bg-amber-100 text-amber-800'
}

export function bpmnConceptChipClasses(): string {
  return 'bg-blue-100 text-blue-800'
}
