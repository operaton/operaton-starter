#!/usr/bin/env node
/**
 * operaton-starter-mcp
 *
 * MCP server exposing tools for AI assistants (Claude, GitHub Copilot, Cursor)
 * to generate and scaffold Operaton BPM projects iteratively.
 */
import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js'
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js'
import { z } from 'zod'

const DEFAULT_BASE_URL = process.env.OPERATON_STARTER_URL ?? 'https://start.operaton.org'

const server = new McpServer({
  name: 'operaton-starter-mcp',
  version: '0.2.0'
})

// ─── Tool: generate_project ───────────────────────────────────────────────────

server.tool(
  'generate_project',
  'Generate an Operaton BPM project archive (ZIP) with the specified configuration',
  {
    projectType: z
      .enum(['PROCESS_APPLICATION', 'PROCESS_ARCHIVE', 'DMN_PROJECT'])
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

// ─── Tool: scaffold_service_task ──────────────────────────────────────────────

server.tool(
  'scaffold_service_task',
  'Generate a BPMN service task XML snippet and a matching Java delegate class. ' +
    'Add the XML to your .bpmn file and the Java class to your delegates package.',
  {
    taskId: z.string().describe('Unique BPMN element ID (e.g. Task_approveOrder)'),
    taskName: z.string().describe('Human-readable task name (e.g. Approve Order)'),
    delegateClassName: z
      .string()
      .describe('Simple Java class name for the delegate (e.g. ApproveOrderDelegate)'),
    javaPackage: z
      .string()
      .optional()
      .describe('Java package for the delegate class (e.g. com.example.delegate)')
  },
  async ({ taskId, taskName, delegateClassName, javaPackage }) => {
    const beanName = delegateClassName.charAt(0).toLowerCase() + delegateClassName.slice(1)
    const pkg = javaPackage ?? 'com.example.delegate'

    const bpmnSnippet = `\
<!-- Service Task: ${taskName} -->
<!-- Add inside your <process> element and wire with sequence flows -->
<serviceTask id="${taskId}"
             name="${taskName}"
             operaton:delegateExpression="\${${beanName}}">
</serviceTask>`

    const javaSnippet = `\
package ${pkg};

import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class ${delegateClassName} implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // TODO: implement delegate logic
        // Access variables: execution.getVariable("myVar")
        // Set variables:    execution.setVariable("result", value)
    }
}
`

    return {
      content: [
        {
          type: 'text',
          text: [
            '## BPMN Service Task Snippet',
            '',
            '```xml',
            bpmnSnippet,
            '```',
            '',
            `## Java Delegate: \`${delegateClassName}.java\``,
            '',
            '```java',
            javaSnippet,
            '```',
            '',
            `Place the Java file at: \`src/main/java/${pkg.replace(/\./g, '/')}/${delegateClassName}.java\``
          ].join('\n')
        }
      ]
    }
  }
)

// ─── Tool: scaffold_user_task ─────────────────────────────────────────────────

server.tool(
  'scaffold_user_task',
  'Generate a BPMN user task XML snippet with optional assignee and candidate group configuration.',
  {
    taskId: z.string().describe('Unique BPMN element ID (e.g. Task_reviewRequest)'),
    taskName: z.string().describe('Human-readable task name (e.g. Review Request)'),
    assignee: z
      .string()
      .optional()
      .describe(
        'Static assignee or EL expression (e.g. john.doe or ${initiator}). Omit to use candidate groups only.'
      ),
    candidateGroups: z
      .string()
      .optional()
      .describe('Comma-separated candidate groups (e.g. managers,finance-team)'),
    formKey: z
      .string()
      .optional()
      .describe('Form key for embedded or external form (e.g. embedded:app:forms/review.html)')
  },
  async ({ taskId, taskName, assignee, candidateGroups, formKey }) => {
    const attrs: string[] = [`id="${taskId}"`, `name="${taskName}"`]
    if (assignee) attrs.push(`operaton:assignee="${assignee}"`)
    if (candidateGroups) attrs.push(`operaton:candidateGroups="${candidateGroups}"`)
    if (formKey) attrs.push(`operaton:formKey="${formKey}"`)

    const attrLines = attrs.map((a, i) => (i === 0 ? `<userTask ${a}` : `             ${a}`))
    const bpmnSnippet = `\
<!-- User Task: ${taskName} -->
<!-- Add inside your <process> element and wire with sequence flows -->
${attrLines.join('\n')}>
</userTask>`

    return {
      content: [
        {
          type: 'text',
          text: [
            '## BPMN User Task Snippet',
            '',
            '```xml',
            bpmnSnippet,
            '```',
            '',
            '**Next steps:**',
            '1. Add the snippet inside your `<process>` element',
            '2. Add `<sequenceFlow>` elements to wire it into your flow',
            candidateGroups
              ? `3. Ensure the groups \`${candidateGroups}\` exist in your identity service`
              : ''
          ]
            .filter(Boolean)
            .join('\n')
        }
      ]
    }
  }
)

// ─── Tool: scaffold_timer_boundary_event ──────────────────────────────────────

server.tool(
  'scaffold_timer_boundary_event',
  'Generate a BPMN timer boundary event XML snippet that attaches to an existing task. ' +
    'Use for SLA timeouts, escalations, or deadline handling.',
  {
    eventId: z
      .string()
      .describe('Unique BPMN element ID for the boundary event (e.g. Timer_approvalTimeout)'),
    attachedToRef: z
      .string()
      .describe('ID of the task this timer attaches to (e.g. Task_approveOrder)'),
    timerType: z
      .enum(['duration', 'date', 'cycle'])
      .default('duration')
      .describe(
        'Timer type: duration (PT1H), date (2025-01-01T00:00:00Z), or cycle (R3/PT10M)'
      ),
    timerValue: z
      .string()
      .default('PT1H')
      .describe('ISO 8601 timer value or EL expression (e.g. PT1H, ${slaDeadline})'),
    cancelActivity: z
      .boolean()
      .default(true)
      .describe('If true, cancels the attached task when timer fires (interrupting). If false, runs in parallel (non-interrupting).')
  },
  async ({ eventId, attachedToRef, timerType, timerValue, cancelActivity }) => {
    const timerElement =
      timerType === 'duration'
        ? `<timeDuration xsi:type="tFormalExpression">${timerValue}</timeDuration>`
        : timerType === 'date'
          ? `<timeDate xsi:type="tFormalExpression">${timerValue}</timeDate>`
          : `<timeCycle xsi:type="tFormalExpression">${timerValue}</timeCycle>`

    const escalationTaskId = `Task_after_${eventId}`
    const flowId = `Flow_${eventId}_to_escalation`

    const bpmnSnippet = `\
<!-- Timer Boundary Event: fires after ${timerValue} on ${attachedToRef} -->
<!-- Add inside your <process> element -->
<boundaryEvent id="${eventId}"
               attachedToRef="${attachedToRef}"
               cancelActivity="${cancelActivity}">
  <timerEventDefinition>
    ${timerElement}
  </timerEventDefinition>
</boundaryEvent>

<!-- Escalation path after timeout — replace with your actual next step -->
<sequenceFlow id="${flowId}" sourceRef="${eventId}" targetRef="${escalationTaskId}"/>
<userTask id="${escalationTaskId}" name="Handle Timeout"/>
<!-- TODO: add an end event or further flow after ${escalationTaskId} -->`

    const typeLabel = cancelActivity ? 'interrupting' : 'non-interrupting'

    return {
      content: [
        {
          type: 'text',
          text: [
            `## BPMN Timer Boundary Event Snippet (${typeLabel})`,
            '',
            '```xml',
            bpmnSnippet,
            '```',
            '',
            '**Note:** Make sure your `<definitions>` includes `xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"` for the `xsi:type` attribute.',
            '',
            '**Next steps:**',
            `1. Add the snippet inside your \`<process>\` element`,
            `2. Replace \`${escalationTaskId}\` with your actual escalation task/end event`,
            `3. Verify \`${attachedToRef}\` matches the exact ID of the task in your BPMN`
          ].join('\n')
        }
      ]
    }
  }
)

// ─── Bootstrap ────────────────────────────────────────────────────────────────

async function main() {
  const transport = new StdioServerTransport()
  await server.connect(transport)
  console.error('operaton-starter-mcp running on stdio')
}

main().catch((err) => {
  console.error('Fatal error:', err)
  process.exit(1)
})
