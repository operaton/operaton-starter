import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import AppHeader from '../AppHeader.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: '/', component: { template: '<div/>' } }]
})

describe('AppHeader', () => {
  it('renders brand name', async () => {
    const wrapper = mount(AppHeader, { global: { plugins: [router] } })
    await router.isReady()
    expect(wrapper.text()).toContain('Operaton')
    expect(wrapper.text()).toContain('Starter')
  })
})
