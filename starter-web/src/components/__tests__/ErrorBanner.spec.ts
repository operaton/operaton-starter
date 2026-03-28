import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import ErrorBanner from '../ErrorBanner.vue'
import type { ProblemDetail } from '@/generated/types'

describe('ErrorBanner', () => {
  it('renders nothing when error is null', () => {
    const wrapper = mount(ErrorBanner, { props: { error: null } })
    expect(wrapper.find('[role="alert"]').exists()).toBe(false)
  })

  it('renders error title and detail', () => {
    const error: ProblemDetail = {
      type: 'test',
      title: 'Test Error',
      status: 429,
      detail: 'Too many requests'
    }
    const wrapper = mount(ErrorBanner, { props: { error } })
    expect(wrapper.find('[role="alert"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('Test Error')
    expect(wrapper.text()).toContain('Too many requests')
  })
})
