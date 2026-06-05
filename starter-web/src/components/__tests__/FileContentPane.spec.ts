import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FileContentPane from '../FileContentPane.vue'
import type { TreeNode } from '@/utils/fileTreeBuilder'

function makeNode(overrides: Partial<TreeNode> = {}): TreeNode {
  return {
    name: 'Application.java',
    path: 'src/main/java/com/example/Application.java',
    isDir: false,
    ...overrides
  }
}

describe('FileContentPane', () => {
  it('renders file name as heading', () => {
    const node = makeNode({ entry: { path: 'Application.java', templateId: 'x.jte', previewContent: 'class App {}' } })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.text()).toContain('Application.java')
  })

  it('renders previewContent in monospace block', () => {
    const node = makeNode({ entry: { path: 'pom.xml', templateId: 'pom.xml.jte', previewContent: '<project/>' } })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.text()).toContain('<project/>')
  })

  it('shows placeholder when previewContent is null', () => {
    const node = makeNode({ entry: { path: 'x.bin', templateId: 'x.jte', previewContent: null } })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.text()).toContain('No preview available')
  })

  it('shows placeholder when entry is undefined', () => {
    const node = makeNode({ entry: undefined })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.text()).toContain('No preview available')
  })

  it('renders Copy button when content is present', () => {
    const node = makeNode({ entry: { path: 'f.java', templateId: 'f.jte', previewContent: 'hello' } })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.find('button').text()).toBe('Copy')
  })

  it('renders Copy button disabled when previewContent is null (AC10)', () => {
    const node = makeNode({ entry: { path: 'f.bin', templateId: 'f.jte', previewContent: null } })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.find('button').exists()).toBe(true)
    expect(wrapper.find('button').attributes('disabled')).toBeDefined()
  })

  it('renders nothing when node is null', () => {
    const wrapper = mount(FileContentPane, { props: { node: null } })
    expect(wrapper.find('section').exists()).toBe(false)
  })

  it('has aria-label on section for screen readers', () => {
    const node = makeNode({ entry: { path: 'f.java', templateId: 'f.jte', previewContent: 'code' } })
    const wrapper = mount(FileContentPane, { props: { node } })
    expect(wrapper.find('section').attributes('aria-label')).toBe('File content preview')
  })
})
