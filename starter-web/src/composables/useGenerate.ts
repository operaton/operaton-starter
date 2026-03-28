import { ref } from 'vue'
import { generateProject } from '@/generated/api'
import type { ProjectConfig, ProblemDetail } from '@/generated/types'

export function useGenerate() {
  const isGenerating = ref(false)
  const error = ref<ProblemDetail | null>(null)

  async function generate(config: ProjectConfig, artifactId: string) {
    isGenerating.value = true
    error.value = null
    try {
      const blob = await generateProject(config)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${artifactId}.zip`
      a.click()
      URL.revokeObjectURL(url)
      return true
    } catch (err) {
      error.value = err as ProblemDetail
      return false
    } finally {
      isGenerating.value = false
    }
  }

  return { isGenerating, error, generate }
}
