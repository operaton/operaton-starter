import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import UseCaseGalleryCard from '../UseCaseGalleryCard.vue'
import type { UseCaseExample } from '@/generated/types'

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', component: { template: '<div/>' } },
    { path: '/configure', component: { template: '<div/>' } },
  ],
})

function makeEntry(overrides: Partial<UseCaseExample> = {}): UseCaseExample {
  return {
    useCaseId: 'uc-01-leave-request',
    title: 'Leave Request',
    description: 'A manager approves employee leave.',
    tags: ['multi-role', 'human-tasks'],
    projectType: 'PROCESS_APPLICATION',
    defaultArtifactId: 'leave-request-example',
    ...overrides,
  }
}

describe('UseCaseGalleryCard', () => {
  it('renders title', () => {
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry: makeEntry() },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('Leave Request')
  })

  it('renders description', () => {
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry: makeEntry() },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('A manager approves employee leave.')
  })

  it('renders capability tags', () => {
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry: makeEntry() },
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('multi-role')
    expect(wrapper.text()).toContain('human-tasks')
  })

  it('docker-compose tag has distinct amber class', () => {
    const entry = makeEntry({ tags: ['multi-role', 'docker-compose'] })
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry },
      global: { plugins: [router] },
    })
    const tags = wrapper.findAll('[class*="rounded-full"]')
    const dockerTag = tags.find((t) => t.text() === 'docker-compose')
    expect(dockerTag).toBeDefined()
    expect(dockerTag!.classes()).toContain('bg-amber-100')
    expect(dockerTag!.classes()).toContain('text-amber-700')
  })

  it('non-docker-compose tags use primary colour class', () => {
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry: makeEntry() },
      global: { plugins: [router] },
    })
    const tags = wrapper.findAll('[class*="rounded-full"]')
    const regularTag = tags.find((t) => t.text() === 'multi-role')
    expect(regularTag).toBeDefined()
    expect(regularTag!.classes()).toContain('text-primary')
    expect(regularTag!.classes()).not.toContain('bg-amber-100')
  })

  it('click emits select event', async () => {
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry: makeEntry() },
      global: { plugins: [router] },
    })
    await wrapper.trigger('click')
    expect(wrapper.emitted('select')).toBeTruthy()
    expect(wrapper.emitted('select')![0][0]).toMatchObject({ useCaseId: 'uc-01-leave-request' })
  })

  it('is keyboard accessible with tabindex', () => {
    const wrapper = mount(UseCaseGalleryCard, {
      props: { entry: makeEntry() },
      global: { plugins: [router] },
    })
    const article = wrapper.find('article')
    expect(article.attributes('tabindex')).toBe('0')
  })
})
