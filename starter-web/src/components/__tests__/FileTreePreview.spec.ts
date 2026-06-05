import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import FileTreePreview from '../FileTreePreview.vue'
import type { ProjectConfig, TemplateManifestEntry } from '@/generated/types'

const baseConfig: ProjectConfig = {
  projectType: 'PROCESS_APPLICATION',
  buildSystem: 'MAVEN',
  groupId: 'com.example',
  artifactId: 'my-app',
  projectName: 'My App',
  javaVersion: 17,
  dependencyUpdater: 'RENOVATE',
  dockerCompose: false,
  githubActions: false
}

const manifest: TemplateManifestEntry[] = [
  {
    path: 'pom.xml',
    condition: "buildSystem == 'MAVEN'",
    templateId: 'pom.xml.jte',
    previewContent: '<project>pom content</project>'
  },
  {
    path: 'src/main/java/{package}/Application.java',
    condition: null,
    templateId: 'Application.java.jte',
    previewContent: 'class Application {}'
  }
]

describe('FileTreePreview', () => {
  it('renders file tree', () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    expect(wrapper.text()).toContain('pom.xml')
  })

  it('does not show content pane before any file is selected', () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    expect(wrapper.findComponent({ name: 'FileContentPane' }).exists()).toBe(false)
  })

  it('shows content pane after clicking a file node', async () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    // click pom.xml span
    const spans = wrapper.findAll('span')
    const pomSpan = spans.find((s) => s.text().includes('pom.xml'))
    await pomSpan!.trigger('click')
    expect(wrapper.findComponent({ name: 'FileContentPane' }).exists()).toBe(true)
  })

  it('displays selected file previewContent after click', async () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    const spans = wrapper.findAll('span')
    const pomSpan = spans.find((s) => s.text().includes('pom.xml'))
    await pomSpan!.trigger('click')
    expect(wrapper.text()).toContain('pom content')
  })

  it('updates content pane when a different file is selected', async () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    const spans = wrapper.findAll('span')
    const pomSpan = spans.find((s) => s.text().includes('pom.xml'))
    await pomSpan!.trigger('click')
    expect(wrapper.text()).toContain('pom content')
    // now click Application.java — content should switch
    const appSpan = wrapper.findAll('span').find((s) => s.text().includes('Application.java'))
    await appSpan!.trigger('click')
    expect(wrapper.text()).toContain('class Application {}')
    expect(wrapper.text()).not.toContain('pom content')
  })

  it('has persistent aria-live region so screen readers announce content changes', () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    expect(wrapper.find('[aria-live="polite"]').exists()).toBe(true)
  })

  it('selects file via Enter key', async () => {
    const wrapper = mount(FileTreePreview, { props: { manifest, config: baseConfig } })
    const spans = wrapper.findAll('span')
    const pomSpan = spans.find((s) => s.text().includes('pom.xml'))
    await pomSpan!.trigger('keydown', { key: 'Enter' })
    expect(wrapper.findComponent({ name: 'FileContentPane' }).exists()).toBe(true)
  })
})
