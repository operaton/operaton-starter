#!/usr/bin/env node
/**
 * operaton-starter CLI
 *
 * Generates an Operaton BPM project archive via the REST API.
 * Usage: npx operaton-starter [flags]
 *
 * Modes:
 *   TTY stdout  → saves {artifactId}.zip to current directory
 *   Pipe stdout → writes raw ZIP bytes to stdout (all status to stderr)
 *   --output    → extracts ZIP into specified directory
 *   --extract   → extracts ZIP into ./{artifactId}/ directory
 */
import { Command } from 'commander'
import AdmZip from 'adm-zip'
import { writeFileSync, mkdirSync } from 'node:fs'
import { join } from 'node:path'
import { generateProject } from './generated/api.js'
import type { ProjectConfig } from './generated/types.js'

const BASE_URL = process.env.OPERATON_STARTER_URL ?? 'https://start.operaton.org'

function stderr(msg: string): void {
  process.stderr.write(msg + '\n')
}

function fatal(msg: string): never {
  stderr(`error: ${msg}`)
  process.exit(1)
}

async function main(): Promise<void> {
  const program = new Command()
    .name('operaton-starter')
    .description('Generate an Operaton BPM project archive')
    .requiredOption('--groupId <id>', 'Maven Group ID (e.g. com.example)')
    .requiredOption('--artifactId <id>', 'Maven Artifact ID (e.g. my-process)')
    .requiredOption('--projectName <name>', 'Human-readable project name')
    .requiredOption(
      '--projectType <type>',
      'Project type: PROCESS_APPLICATION | PROCESS_ARCHIVE',
      'PROCESS_APPLICATION'
    )
    .requiredOption(
      '--buildSystem <system>',
      'Build system: MAVEN | GRADLE_GROOVY | GRADLE_KOTLIN',
      'MAVEN'
    )
    .option('--javaVersion <version>', 'Java version: 17 | 21 | 25', '17')
    .option('--deploymentTarget <target>', 'Deployment target: TOMCAT | STANDALONE_ENGINE')
    .option('--dependencyUpdater <updater>', 'Dependency updater: DEPENDABOT | RENOVATE', 'RENOVATE')
    .option('--dockerCompose', 'Include Docker Compose file', false)
    .option('--githubActions', 'Include GitHub Actions CI skeleton', true)
    .option('--output <dir>', 'Extract ZIP into this directory (created if absent)')
    .option('--extract', 'Extract ZIP into ./{artifactId}/ in the current directory')

  program.parse(process.argv)
  const opts = program.opts<{
    groupId: string
    artifactId: string
    projectName: string
    projectType: string
    buildSystem: string
    javaVersion: string
    deploymentTarget?: string
    dependencyUpdater: string
    dockerCompose: boolean
    githubActions: boolean
    output?: string
    extract?: boolean
  }>()

  // Validate enum values
  const validProjectTypes = ['PROCESS_APPLICATION', 'PROCESS_ARCHIVE']
  const validBuildSystems = ['MAVEN', 'GRADLE_GROOVY', 'GRADLE_KOTLIN']
  const validDeploymentTargets = ['TOMCAT', 'STANDALONE_ENGINE']
  const validUpdaters = ['DEPENDABOT', 'RENOVATE']

  if (!validProjectTypes.includes(opts.projectType)) {
    fatal(`--projectType must be one of: ${validProjectTypes.join(', ')}`)
  }
  if (!validBuildSystems.includes(opts.buildSystem)) {
    fatal(`--buildSystem must be one of: ${validBuildSystems.join(', ')}`)
  }
  if (opts.deploymentTarget && !validDeploymentTargets.includes(opts.deploymentTarget)) {
    fatal(`--deploymentTarget must be one of: ${validDeploymentTargets.join(', ')}`)
  }
  if (!validUpdaters.includes(opts.dependencyUpdater)) {
    fatal(`--dependencyUpdater must be one of: ${validUpdaters.join(', ')}`)
  }

  const config: ProjectConfig = {
    projectType: opts.projectType as ProjectConfig['projectType'],
    buildSystem: opts.buildSystem as ProjectConfig['buildSystem'],
    groupId: opts.groupId,
    artifactId: opts.artifactId,
    projectName: opts.projectName,
    javaVersion: parseInt(opts.javaVersion, 10),
    deploymentTarget: opts.deploymentTarget as ProjectConfig['deploymentTarget'],
    dependencyUpdater: opts.dependencyUpdater as ProjectConfig['dependencyUpdater'],
    dockerCompose: opts.dockerCompose,
    githubActions: opts.githubActions
  }

  const isPiped = !process.stdout.isTTY

  if (!isPiped) {
    stderr(`Generating ${opts.artifactId} from ${BASE_URL}…`)
  }

  let zipBytes: ArrayBuffer
  try {
    zipBytes = await generateProject(BASE_URL, config)
  } catch (e) {
    fatal(e instanceof Error ? e.message : String(e))
  }

  const buf = Buffer.from(zipBytes)

  if (opts.output) {
    mkdirSync(opts.output, { recursive: true })
    new AdmZip(buf).extractAllTo(opts.output, true)
    const msg = `Extracted to ${opts.output}`
    isPiped ? stderr(msg) : process.stdout.write(msg + '\n')
  } else if (opts.extract) {
    const outDir = join(process.cwd(), opts.artifactId)
    mkdirSync(outDir, { recursive: true })
    new AdmZip(buf).extractAllTo(outDir, true)
    const msg = `Extracted to ${outDir}`
    isPiped ? stderr(msg) : process.stdout.write(msg + '\n')
  } else if (isPiped) {
    process.stdout.write(buf)
  } else {
    const filename = `${opts.artifactId}.zip`
    writeFileSync(filename, buf)
    process.stdout.write(`Downloaded ${filename}\n`)
  }
}

main().catch((e) => {
  process.stderr.write(`Fatal error: ${e instanceof Error ? e.message : String(e)}\n`)
  process.exit(1)
})
