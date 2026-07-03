import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import GallerySearchBar from '../GallerySearchBar.vue'

describe('GallerySearchBar', () => {
  it('renders search input', () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 5,
        runtimes: [],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    const input = wrapper.find('input[type="search"]')
    expect(input.exists()).toBe(true)
    expect(input.attributes('aria-label')).toBe('Search examples')
  })

  it('emits update:query event with debounce', async () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 5,
        runtimes: [],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    const input = wrapper.find('input[type="search"]')
    await input.setValue('Spring')
    // Wait for debounce (200ms)
    await new Promise((resolve) => setTimeout(resolve, 300))
    expect(wrapper.emitted('update:query')).toBeTruthy()
    expect(wrapper.emitted('update:query')![0][0]).toBe('Spring')
  })

  it('renders filter chips for runtimes', () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 5,
        runtimes: ['Spring Boot', 'Quarkus'],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    expect(wrapper.text()).toContain('Spring Boot')
    expect(wrapper.text()).toContain('Quarkus')
  })

  it('renders status message with result count', async () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 2,
        runtimes: [],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    // Status is in sr-only, check it's there
    expect(wrapper.text()).toContain('2 examples found')
  })

  it('emits toggle-filter event', async () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 5,
        runtimes: ['Spring Boot'],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    const filterChips = wrapper.findAllComponents({ name: 'FilterChip' })
    expect(filterChips.length).toBeGreaterThan(0)
    await filterChips[0].vm.$emit('toggle')
    // The component should emit toggle-filter event
    expect(wrapper.emitted('toggle-filter')).toBeTruthy()
  })

  it('has sticky positioning', () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 5,
        runtimes: [],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    const stickyDiv = wrapper.find('.search-bar-sticky')
    expect(stickyDiv.exists()).toBe(true)
  })

  it('announces results via aria-live region', () => {
    const wrapper = mount(GallerySearchBar, {
      props: {
        filteredExamplesCount: 5,
        runtimes: [],
        buildSystems: [],
        complexities: [],
        integrations: [],
        bpmnConcepts: [],
        activeFilters: {
          runtime: new Set(),
          buildSystem: new Set(),
          complexity: new Set(),
          integrations: new Set(),
          bpmnConcepts: new Set(),
        },
      },
    })
    const liveRegion = wrapper.find('[role="status"]')
    expect(liveRegion.exists()).toBe(true)
    expect(liveRegion.attributes('aria-live')).toBe('polite')
  })
})
