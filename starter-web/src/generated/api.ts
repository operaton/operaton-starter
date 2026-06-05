import type { Metadata, ProjectConfig, ProblemDetail } from './types'

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

export async function getMetadata(): Promise<Metadata> {
  const response = await fetch(`${BASE_URL}/api/v1/metadata`, {
    headers: { Accept: 'application/json' }
  })
  if (!response.ok) {
    const problem: ProblemDetail = await response.json()
    throw problem
  }
  return response.json()
}

export async function generateProject(config: ProjectConfig): Promise<Blob> {
  const response = await fetch(`${BASE_URL}/api/v1/generate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/zip' },
    body: JSON.stringify(config)
  })
  if (!response.ok) {
    const problem: ProblemDetail = await response.json()
    throw problem
  }
  return response.blob()
}

export async function previewTemplate(templateId: string, config: ProjectConfig): Promise<string | null> {
  try {
    const response = await fetch(`${BASE_URL}/api/v1/preview`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ templateId, config })
    })
    if (!response.ok) return null
    const data = await response.json()
    return data.content ?? null
  } catch {
    return null
  }
}
