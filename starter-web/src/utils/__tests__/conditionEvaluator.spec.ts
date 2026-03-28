import { describe, it, expect } from 'vitest'
import { evaluateCondition } from '../conditionEvaluator'
import type { ProjectConfig } from '@/generated/types'

const config: ProjectConfig = {
  projectType: 'PROCESS_APPLICATION',
  buildSystem: 'MAVEN',
  groupId: 'com.example',
  artifactId: 'my-app',
  projectName: 'My App',
  javaVersion: 17,
  dependencyUpdater: 'RENOVATE',
  dockerCompose: false,
  githubActions: true
}

describe('evaluateCondition', () => {
  it('returns true for null condition', () => {
    expect(evaluateCondition(null, config)).toBe(true)
  })

  it('evaluates buildSystem == MAVEN as true', () => {
    expect(evaluateCondition("buildSystem == 'MAVEN'", config)).toBe(true)
  })

  it('evaluates buildSystem == GRADLE_GROOVY as false', () => {
    expect(evaluateCondition("buildSystem == 'GRADLE_GROOVY'", config)).toBe(false)
  })

  it('evaluates githubActions == true as true', () => {
    expect(evaluateCondition('githubActions == true', config)).toBe(true)
  })

  it('evaluates dockerCompose == true as false when dockerCompose is false', () => {
    expect(evaluateCondition('dockerCompose == true', config)).toBe(false)
  })

  it('evaluates dependencyUpdater == RENOVATE as true', () => {
    expect(evaluateCondition("dependencyUpdater == 'RENOVATE'", config)).toBe(true)
  })
})
