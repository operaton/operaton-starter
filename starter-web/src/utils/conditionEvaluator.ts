import type { ProjectConfig } from '@/generated/types'

/**
 * Evaluates a template manifest condition expression against the current form state.
 * Condition format: "field == 'value'" or "field == true"
 */
export function evaluateCondition(
  condition: string | null | undefined,
  config: ProjectConfig
): boolean {
  if (!condition) return true
  const parts = condition.trim().split(/\s+/)
  if (parts.length !== 3 || parts[1] !== '==') return true
  const [field, , rawValue] = parts
  const value = rawValue.replace(/^'|'$/g, '')
  const configValue = String((config as unknown as Record<string, unknown>)[field] ?? '')
  return configValue === value
}
