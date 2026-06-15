import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import DownloadAction from '../DownloadAction.vue'
import type { DownloadStatus } from '../useExampleDownload'

describe('DownloadAction', () => {
  it('renders download button in idle state', () => {
    const status: DownloadStatus = { state: 'idle' }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'example-1',
      },
    })
    const button = wrapper.find('.download-button')
    expect(button.exists()).toBe(true)
    expect(button.text()).toContain('Download ZIP')
    expect(button.attributes('disabled')).toBeUndefined()
  })

  it('disables button and shows loading spinner in downloading state', () => {
    const status: DownloadStatus = { state: 'downloading' }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'example-1',
      },
    })
    const button = wrapper.find('.download-button')
    expect(button.attributes('disabled')).toBe('')
    expect(button.text()).toContain('Downloading…')
  })

  it('shows success message in success state', () => {
    const status: DownloadStatus = { state: 'success' }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'my-example',
      },
    })
    const successMessage = wrapper.find('.success-message')
    expect(successMessage.exists()).toBe(true)
    expect(successMessage.text()).toContain('Downloaded my-example.zip ✓')
  })

  it('shows error message and retry button in error state', () => {
    const status: DownloadStatus = {
      state: 'error',
      error: 'Network error',
    }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'example-1',
      },
    })
    const errorMessage = wrapper.find('.error-message')
    expect(errorMessage.exists()).toBe(true)
    expect(errorMessage.text()).toContain('Network error')
    const retryButton = wrapper.find('.retry-button')
    expect(retryButton.exists()).toBe(true)
    expect(retryButton.text()).toBe('Retry')
  })

  it('emits download event when button clicked', async () => {
    const status: DownloadStatus = { state: 'idle' }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'example-1',
      },
    })
    await wrapper.find('.download-button').trigger('click')
    expect(wrapper.emitted('download')).toBeTruthy()
  })

  it('emits retry event when retry button clicked', async () => {
    const status: DownloadStatus = {
      state: 'error',
      error: 'Network error',
    }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'example-1',
      },
    })
    await wrapper.find('.retry-button').trigger('click')
    expect(wrapper.emitted('retry')).toBeTruthy()
  })

  it('shows default error message when error is not provided', () => {
    const status: DownloadStatus = { state: 'error' }
    const wrapper = mount(DownloadAction, {
      props: {
        status,
        exampleId: 'example-1',
      },
    })
    expect(wrapper.find('.error-message').text()).toContain('Download failed')
  })
})
