/**
 * OpenAPI-derived API client for POST /api/v1/generate.
 * Returns raw ZIP bytes as an ArrayBuffer.
 */
import type { ProjectConfig, ProblemDetail } from './types.js'

export async function generateProject(
  baseUrl: string,
  config: ProjectConfig
): Promise<ArrayBuffer> {
  const response = await fetch(`${baseUrl}/api/v1/generate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/zip'
    },
    body: JSON.stringify(config)
  })

  if (!response.ok) {
    let detail: ProblemDetail
    try {
      detail = (await response.json()) as ProblemDetail
    } catch {
      detail = { title: 'Unknown error', status: response.status, detail: await response.text() }
    }
    const message = detail.detail ?? detail.title
    throw new Error(`API error ${response.status}: ${message}`)
  }

  return response.arrayBuffer()
}
