import { describe, it, expect } from 'vitest'
import { buildFileTree } from '../fileTreeBuilder'
import type { ProjectConfig, TemplateManifestEntry } from '@/generated/types'

const baseConfig: ProjectConfig = {
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

const manifest: TemplateManifestEntry[] = [
  { path: 'pom.xml', condition: "buildSystem == 'MAVEN'", templateId: 'pom.xml.jte' },
  { path: 'build.gradle', condition: "buildSystem == 'GRADLE_GROOVY'", templateId: 'build.gradle.jte' },
  { path: '.github/workflows/ci.yml', condition: 'githubActions == true', templateId: 'ci.yml.jte' },
  { path: 'docker-compose.yml', condition: 'dockerCompose == true', templateId: 'docker-compose.yml.jte' },
  {
    path: 'src/main/java/{package}/Application.java',
    condition: null,
    templateId: 'Application.java.jte'
  },
  { path: '{artifactId}.bpmn', condition: null, templateId: 'process.bpmn.jte' },
  { path: 'renovate.json', condition: "dependencyUpdater == 'RENOVATE'", templateId: 'renovate.json.jte' }
]

describe('buildFileTree', () => {
  it('includes pom.xml for Maven, excludes build.gradle', () => {
    const tree = buildFileTree(manifest, baseConfig)
    const names = tree.map((n) => n.name)
    expect(names).toContain('pom.xml')
    expect(names).not.toContain('build.gradle')
  })

  it('includes ci.yml when githubActions is true', () => {
    const tree = buildFileTree(manifest, baseConfig)
    const github = tree.find((n) => n.name === '.github')
    expect(github).toBeDefined()
  })

  it('excludes docker-compose.yml when dockerCompose is false', () => {
    const tree = buildFileTree(manifest, baseConfig)
    const names = tree.map((n) => n.name)
    expect(names).not.toContain('docker-compose.yml')
  })

  it('includes docker-compose.yml when dockerCompose is true', () => {
    const tree = buildFileTree(manifest, { ...baseConfig, dockerCompose: true })
    const names = tree.map((n) => n.name)
    expect(names).toContain('docker-compose.yml')
  })

  it('interpolates artifactId in filenames', () => {
    const tree = buildFileTree(manifest, baseConfig)
    const names = tree.map((n) => n.name)
    expect(names).toContain('my-app.bpmn')
    expect(names).not.toContain('{artifactId}.bpmn')
  })

  it('includes renovate.json for RENOVATE, excludes for DEPENDABOT', () => {
    const treeRenovate = buildFileTree(manifest, baseConfig)
    const treeDependabot = buildFileTree(manifest, { ...baseConfig, dependencyUpdater: 'DEPENDABOT' })

    expect(treeRenovate.map((n) => n.name)).toContain('renovate.json')
    expect(treeDependabot.map((n) => n.name)).not.toContain('renovate.json')
  })

  it('switches build file when buildSystem changes', () => {
    const gradleTree = buildFileTree(manifest, { ...baseConfig, buildSystem: 'GRADLE_GROOVY' })
    const names = gradleTree.map((n) => n.name)
    expect(names).toContain('build.gradle')
    expect(names).not.toContain('pom.xml')
  })
})
