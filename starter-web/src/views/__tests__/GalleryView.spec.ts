import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import { ref } from 'vue'
import type { Metadata } from '@/generated/types'

vi.mock('@/composables/useMetadata', () => ({
  useMetadata: () => ({
    data: ref({
        projectTypes: [],
        buildSystems: [],
        globalOptions: { javaVersions: { options: [17], default: 17 } },
        useCaseExamples: [
          {
            useCaseId: 'uc-01-leave-request',
            title: 'Leave Request',
            description: 'A manager approves employee leave.',
            tags: ['multi-role', 'human-tasks'],
            projectType: 'PROCESS_APPLICATION',
            buildSystem: 'MAVEN',
            defaultArtifactId: 'leave-request-example',
            defaultProjectName: 'Leave Request Example',
            dockerCompose: false,
          },
          {
            useCaseId: 'uc-02-loan-application',
            title: 'Loan Application',
            description: 'DMN credit scoring.',
            tags: ['dmn', 'docker-compose'],
            projectType: 'PROCESS_APPLICATION',
            buildSystem: 'MAVEN',
            defaultArtifactId: 'loan-application-example',
            defaultProjectName: 'Loan Application Example',
            dockerCompose: true,
          },
        ],
      } as Metadata),
    isLoading: ref(false),
    error: ref(null),
  }),
}))

import GalleryView from '../GalleryView.vue'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: GalleryView },
    { path: '/configure', component: { template: '<div/>' } },
  ],
})

describe('GalleryView — Use Case Examples section', () => {
  it('renders "Use Case Examples" heading when useCaseExamples present', async () => {
    const wrapper = mount(GalleryView, {
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('Use Case Examples')
  })

  it('renders all use case cards from metadata', async () => {
    const wrapper = mount(GalleryView, {
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('Leave Request')
    expect(wrapper.text()).toContain('Loan Application')
    expect(wrapper.text()).not.toContain('Start from a use case')
  })

  it('card click navigates to /configure with useCaseId', async () => {
    const wrapper = mount(GalleryView, {
      global: { plugins: [router] },
    })
    const articles = wrapper.findAll('article')
    // Find the Leave Request card
    const leaveRequestCard = articles.find((a) => a.text().includes('Leave Request'))
    expect(leaveRequestCard).toBeDefined()
    await leaveRequestCard!.trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query.useCaseId).toBe('uc-01-leave-request')
    expect(router.currentRoute.value.query.projectType).toBe('PROCESS_APPLICATION')
    expect(router.currentRoute.value.query.buildSystem).toBe('MAVEN')
    expect(router.currentRoute.value.query.projectName).toBe('Leave Request Example')
    expect(router.currentRoute.value.query.dockerCompose).toBe('false')
  })
})
