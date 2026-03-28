import { ref, type Ref } from 'vue'
import { getMetadata } from '@/generated/api'
import type { Metadata, ProblemDetail } from '@/generated/types'

export function useMetadata(): {
  data: Ref<Metadata | null>
  isLoading: Ref<boolean>
  error: Ref<ProblemDetail | null>
} {
  const data = ref<Metadata | null>(null)
  const isLoading = ref(true)
  const error = ref<ProblemDetail | null>(null)

  getMetadata()
    .then((result: Metadata) => {
      data.value = result
    })
    .catch((err: ProblemDetail) => {
      error.value = err
    })
    .finally(() => {
      isLoading.value = false
    })

  return { data, isLoading, error }
}
