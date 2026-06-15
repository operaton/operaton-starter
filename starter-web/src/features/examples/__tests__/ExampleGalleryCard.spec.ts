import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ExampleGalleryCard from '../ExampleGalleryCard.vue'
import type { Example } from '@/generated/types'

function makeExample(overrides: Partial<Example> = {}): Example {
  return {
    id: 'leave-request-spring-boot',
    title: 'Leave Request (Spring Boot)',
    path: 'leave-request-spring-boot',
    shortDescription: 'A multi-role approval workflow.',
    owner: 'operaton',
    repo: 'examples',
    sourceRepoSha: 'abc123def456',
    ...overrides,
  }
}

describe('ExampleGalleryCard', () => {
  it('renders title and short description', () => {
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example: makeExample(),
        downloadStatus: { state: 'idle' },
      },
    })
    expect(wrapper.text()).toContain('Leave Request (Spring Boot)')
    expect(wrapper.text()).toContain('A multi-role approval workflow.')
  })

  it('renders metadata badge tags (runtime, buildSystem, complexity) with monochrome styling', () => {
    const example = makeExample({
      tags: [
        { label: 'Spring Boot', category: 'RUNTIME' },
        { label: 'Maven', category: 'BUILD_SYSTEM' },
        { label: 'Beginner', category: 'COMPLEXITY' },
      ],
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    // Metadata badges should have monochrome styling
    const badges = wrapper.findAll('.tag-badge')
    expect(badges.length).toBe(3)
    badges.forEach((badge) => {
      expect(badge.classes()).toContain('bg-neutral-50')
    })
  })

  it('renders accent tags (BPMN_CONCEPT) with colored styling', () => {
    const example = makeExample({
      tags: [
        { label: 'Multi-role', category: 'BPMN_CONCEPT' },
      ],
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const badge = wrapper.find('.tag-badge')
    expect(badge.text()).toContain('Multi-role')
    expect(badge.classes()).toContain('bg-blue-100')
  })

  it('does not show details button when no details available', () => {
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example: makeExample(),
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    expect(detailsButton.exists()).toBe(false)
  })

  it('shows details button when longDescription exists', () => {
    const example = makeExample({
      longDescription: 'This is a detailed description.',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    expect(detailsButton.exists()).toBe(true)
  })

  it('toggles details panel on button click', async () => {
    const example = makeExample({
      longDescription: 'This is a detailed description.',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    expect(detailsButton.attributes('aria-expanded')).toBe('false')

    await detailsButton.trigger('click')
    expect(detailsButton.attributes('aria-expanded')).toBe('true')
    expect(wrapper.find('.details-panel').exists()).toBe(true)

    await detailsButton.trigger('click')
    expect(detailsButton.attributes('aria-expanded')).toBe('false')
  })

  it('closes details panel on Escape key', async () => {
    const example = makeExample({
      longDescription: 'This is a detailed description.',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    await detailsButton.trigger('click')
    expect(detailsButton.attributes('aria-expanded')).toBe('true')

    await wrapper.find('.details-panel').trigger('keydown', { key: 'Escape' })
    expect(detailsButton.attributes('aria-expanded')).toBe('false')
  })

  it('shows emoji icon when icon is emoji', () => {
    const example = makeExample({
      icon: '🚀',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    expect(wrapper.find('.emoji-icon').text()).toBe('🚀')
  })

  it('shows short SHA when sourceRepoSha exists', async () => {
    const example = makeExample({
      longDescription: 'A detailed description.',
      sourceRepoSha: 'abc123def456xyz789',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    await wrapper.find('.details-button').trigger('click')
    expect(wrapper.text()).toContain('abc123d')
  })

  it('emits download event', async () => {
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example: makeExample(),
        downloadStatus: { state: 'idle' },
      },
      global: {
        stubs: {
          DownloadAction: true,
        },
      },
    })
    await wrapper.findComponent({ name: 'DownloadAction' }).vm.$emit('download')
    expect(wrapper.emitted('download')).toBeTruthy()
  })

  it('displays long description in details panel', async () => {
    const example = makeExample({
      longDescription: 'This is a very detailed and comprehensive description of the example.',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    await detailsButton.trigger('click')
    // Need to wait for DOM update
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.details-panel').text()).toContain('This is a very detailed')
  })

  it('displays authors in details panel', async () => {
    const example = makeExample({
      longDescription: 'A description.',
      authors: [
        { name: 'Alice Author', url: 'https://example.com' },
        { name: 'Bob Builder' },
      ],
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    await detailsButton.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.details-panel').text()).toContain('Alice Author')
    expect(wrapper.find('.details-panel').text()).toContain('Bob Builder')
  })

  it('displays BPMN concepts in details panel', async () => {
    const example = makeExample({
      longDescription: 'A description.',
      bpmnConcepts: ['Multi-instance Task', 'Exclusive Gateway'],
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    await detailsButton.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.details-panel').text()).toContain('Multi-instance Task')
    expect(wrapper.find('.details-panel').text()).toContain('Exclusive Gateway')
  })

  it('displays integrations in details panel', async () => {
    const example = makeExample({
      longDescription: 'A description.',
      integrations: ['Kafka', 'REST API'],
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    await detailsButton.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.details-panel').text()).toContain('Kafka')
    expect(wrapper.find('.details-panel').text()).toContain('REST API')
  })

  it('displays license in details panel', async () => {
    const example = makeExample({
      longDescription: 'A description.',
      license: 'Apache 2.0',
    })
    const wrapper = mount(ExampleGalleryCard, {
      props: {
        example,
        downloadStatus: { state: 'idle' },
      },
    })
    const detailsButton = wrapper.find('.details-button')
    await detailsButton.trigger('click')
    await wrapper.vm.$nextTick()
    expect(wrapper.find('.details-panel').text()).toContain('Apache 2.0')
  })
})
