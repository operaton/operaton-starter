#!/usr/bin/env node
/**
 * operaton-starter-mcp
 *
 * MCP server exposing a generate_project tool for AI assistants
 * (Claude, GitHub Copilot, Cursor) to generate Operaton projects.
 */
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js'
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js'
import { z } from 'zod'

const DEFAULT_BASE_URL = process.env.OPERATON_STARTER_URL ?? 'https://start.operaton.org'

const server = new McpServer({
  name: 'operaton-starter-mcp',
  version: '0.1.0'
})

server.tool(
  'generate_project',
  'Generate an Operaton BPM project archive (ZIP) with the specified configuration',
  {
    projectType: z
      .enum(['PROCESS_APPLICATION', 'PROCESS_ARCHIVE'])
      .describe('Type of Operaton project to generate'),
    buildSystem: z
      .enum(['MAVEN', 'GRADLE_GROOVY', 'GRADLE_KOTLIN'])
      .describe('Build system to use'),
    groupId: z.string().describe('Maven Group ID (e.g. com.example)'),
    artifactId: z.string().describe('Maven Artifact ID (e.g. my-process)'),
    projectName: z.string().describe('Human-readable project name'),
    javaVersion: z.number().int().default(17).describe('Java version: 17, 21, or 25'),
    deploymentTarget: z
      .enum(['TOMCAT', 'STANDALONE_ENGINE'])
      .optional()
      .describe('Deployment target (required for PROCESS_ARCHIVE)'),
    dependencyUpdater: z
      .enum(['DEPENDABOT', 'RENOVATE'])
      .default('RENOVATE')
      .describe('Dependency update tool'),
    dockerCompose: z.boolean().default(false).describe('Include Docker Compose file'),
    githubActions: z.boolean().default(true).describe('Include GitHub Actions CI skeleton')
  },
  async (params) => {
    const response = await fetch(`${DEFAULT_BASE_URL}/api/v1/generate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/zip' },
      body: JSON.stringify(params)
    })

    if (!response.ok) {
      const errorText = await response.text()
      return {
        content: [
          {
            type: 'text',
            text: `Generation failed (HTTP ${response.status}): ${errorText}`
          }
        ],
        isError: true
      }
    }

    const bytes = await response.arrayBuffer()
    const base64 = Buffer.from(bytes).toString('base64')

    return {
      content: [
        {
          type: 'text',
          text: `Project generated successfully. ZIP archive (base64): ${base64}`
        }
      ]
    }
  }
)

async function main() {
  const transport = new StdioServerTransport()
  await server.connect(transport)
  console.error('operaton-starter-mcp running on stdio')
}

main().catch((err) => {
  console.error('Fatal error:', err)
  process.exit(1)
})
