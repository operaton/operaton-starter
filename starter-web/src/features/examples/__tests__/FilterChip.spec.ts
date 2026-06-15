import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FilterChip from '../FilterChip.vue'

describe('FilterChip', () => {
  it('renders label', () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: false,
        category: 'runtime',
      },
    })
    expect(wrapper.text()).toContain('Spring Boot')
  })

  it('inactive chip has aria-pressed false', () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: false,
        category: 'runtime',
      },
    })
    expect(wrapper.find('button').attributes('aria-pressed')).toBe('false')
  })

  it('active chip has aria-pressed true', () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: true,
        category: 'runtime',
      },
    })
    expect(wrapper.find('button').attributes('aria-pressed')).toBe('true')
  })

  it('active chip displays × glyph', () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: true,
        category: 'runtime',
      },
    })
    expect(wrapper.text()).toContain('×')
  })

  it('inactive chip does not display × glyph', () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: false,
        category: 'runtime',
      },
    })
    expect(wrapper.text()).not.toContain('×')
  })

  it('active chip has active styles', () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: true,
        category: 'runtime',
      },
    })
    expect(wrapper.find('button').classes()).toContain('filter-chip-active')
  })

  it('click emits toggle event', async () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: false,
        category: 'runtime',
      },
    })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('toggle')).toBeTruthy()
  })

  it('Space key emits toggle event', async () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: false,
        category: 'runtime',
      },
    })
    await wrapper.find('button').trigger('keydown', { key: ' ' })
    expect(wrapper.emitted('toggle')).toBeTruthy()
  })

  it('Enter key emits toggle event', async () => {
    const wrapper = mount(FilterChip, {
      props: {
        label: 'Spring Boot',
        isActive: false,
        category: 'runtime',
      },
    })
    await wrapper.find('button').trigger('keydown', { key: 'Enter' })
    expect(wrapper.emitted('toggle')).toBeTruthy()
  })
})
