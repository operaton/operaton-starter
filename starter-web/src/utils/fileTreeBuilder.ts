import type { TemplateManifestEntry, ProjectConfig } from '@/generated/types'
import { evaluateCondition } from './conditionEvaluator'

export interface TreeNode {
  name: string
  path: string
  isDir: boolean
  children?: TreeNode[]
  entry?: TemplateManifestEntry
}

/**
 * Builds a tree structure from template manifest entries, evaluating conditions
 * and interpolating identity values (artifactId, groupId).
 * Leaf nodes carry a reference to their source TemplateManifestEntry for preview lookup.
 */
export function buildFileTree(
  manifest: TemplateManifestEntry[],
  config: ProjectConfig
): TreeNode[] {
  const visibleItems = manifest
    .filter((entry) => evaluateCondition(entry.condition, config))
    .map((entry) => ({ interpolatedPath: interpolatePath(entry.path, config), entry }))

  return pathsToTree(visibleItems)
}

function interpolatePath(path: string, config: ProjectConfig): string {
  const packagePath = `${config.groupId.replace(/\./g, '/')}/${config.artifactId}`
  return path
    .replace('{package}', packagePath)
    .replace('{artifactId}', config.artifactId)
    .replace('{groupId}', config.groupId.replace(/\./g, '/'))
}

function pathsToTree(
  items: Array<{ interpolatedPath: string; entry: TemplateManifestEntry }>
): TreeNode[] {
  const root: TreeNode[] = []

  for (const item of items) {
    const parts = item.interpolatedPath.split('/')
    let current = root

    for (let i = 0; i < parts.length; i++) {
      const name = parts[i]
      const isLast = i === parts.length - 1
      let node = current.find((n) => n.name === name)

      if (!node) {
        node = {
          name,
          path: parts.slice(0, i + 1).join('/'),
          isDir: !isLast,
          children: isLast ? undefined : [],
          entry: isLast ? item.entry : undefined
        }
        current.push(node)
      }

      if (!isLast && node.children) {
        current = node.children
      }
    }
  }

  return root
}
