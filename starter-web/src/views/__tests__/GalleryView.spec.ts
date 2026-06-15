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
        examples: [],
      } as Metadata),
    isLoading: ref(false),
    error: ref(null),
  }),
}))

vi.mock('@/features/examples/useExamples', () => ({
  useExamples: () => ({
    examples: ref([]),
    allTags: ref([]),
    allIntegrations: ref([]),
    runtimes: ref([]),
    buildSystems: ref([]),
    complexities: ref([]),
  }),
}))

vi.mock('@/features/examples/useGalleryFilters', () => ({
  useGalleryFilters: () => ({
    filters: ref({
      query: '',
      runtime: new Set(),
      buildSystem: new Set(),
      complexity: new Set(),
      integrations: new Set(),
    }),
    filteredExamples: ref([]),
    hasActiveFilters: ref(false),
    toggleFilter: vi.fn(),
    setQuery: vi.fn(),
    clear: vi.fn(),
  }),
}))

vi.mock('@/features/examples/useExampleDownload', () => ({
  useExampleDownload: () => ({
    getStatus: vi.fn(() => ({ state: 'idle' })),
    download: vi.fn(),
    retry: vi.fn(),
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

describe('GalleryView', () => {
  it('renders Hero section with call-to-action buttons', async () => {
    const wrapper = mount(GalleryView, {
      global: { plugins: [router] },
    })
    expect(wrapper.text()).toContain('Start your Operaton project')
    expect(wrapper.text()).toContain('Configure Now')
  })
})
