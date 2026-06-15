import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ExamplesEmptyState from '../ExamplesEmptyState.vue'

describe('ExamplesEmptyState', () => {
  it('shows "no examples available" message when no active filters', () => {
    const wrapper = mount(ExamplesEmptyState, {
      props: {
        hasActiveFilters: false,
      },
    })
    expect(wrapper.text()).toContain('No examples are available right now')
  })

  it('shows "no examples match" message when active filters exist', () => {
    const wrapper = mount(ExamplesEmptyState, {
      props: {
        hasActiveFilters: true,
      },
    })
    expect(wrapper.text()).toContain('No examples match these filters')
  })

  it('shows "View format docs" button when no active filters', () => {
    const wrapper = mount(ExamplesEmptyState, {
      props: {
        hasActiveFilters: false,
      },
    })
    expect(wrapper.find('.docs-button').exists()).toBe(true)
    expect(wrapper.find('.clear-button').exists()).toBe(false)
  })

  it('shows "Clear filters" button when active filters exist', () => {
    const wrapper = mount(ExamplesEmptyState, {
      props: {
        hasActiveFilters: true,
      },
    })
    expect(wrapper.find('.clear-button').exists()).toBe(true)
    expect(wrapper.find('.docs-button').exists()).toBe(false)
  })

  it('emits clear-filters event when clear button clicked', async () => {
    const wrapper = mount(ExamplesEmptyState, {
      props: {
        hasActiveFilters: true,
      },
    })
    await wrapper.find('.clear-button').trigger('click')
    expect(wrapper.emitted('clear-filters')).toBeTruthy()
  })

  it('docs button links to format docs', () => {
    const wrapper = mount(ExamplesEmptyState, {
      props: {
        hasActiveFilters: false,
      },
    })
    const link = wrapper.find('.docs-button')
    expect(link.attributes('href')).toContain('examples-repository-format.md')
  })
})
