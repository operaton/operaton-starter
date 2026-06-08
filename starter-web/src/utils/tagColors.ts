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
