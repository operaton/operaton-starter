import type { TemplateManifestEntry, ProjectConfig } from '@/generated/types'
import { evaluateCondition } from './conditionEvaluator'

export interface TreeNode {
  name: string
  path: string
  isDir: boolean
  children?: TreeNode[]
}

/**
 * Builds a tree structure from template manifest entries, evaluating conditions
 * and interpolating identity values (artifactId, groupId).
 */
export function buildFileTree(
  manifest: TemplateManifestEntry[],
  config: ProjectConfig
): TreeNode[] {
  const visiblePaths = manifest
    .filter((entry) => evaluateCondition(entry.condition, config))
    .map((entry) => interpolatePath(entry.path, config))

  return pathsToTree(visiblePaths)
}

function interpolatePath(path: string, config: ProjectConfig): string {
  const packagePath = `${config.groupId.replace(/\./g, '/')}/${config.artifactId}`
  return path
    .replace('{package}', packagePath)
    .replace('{artifactId}', config.artifactId)
    .replace('{groupId}', config.groupId.replace(/\./g, '/'))
}

function pathsToTree(paths: string[]): TreeNode[] {
  const root: TreeNode[] = []

  for (const path of paths) {
    const parts = path.split('/')
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
          children: isLast ? undefined : []
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
