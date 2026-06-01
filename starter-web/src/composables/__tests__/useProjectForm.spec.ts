import { describe, it, expect, vi, beforeEach } from 'vitest'
import { nextTick } from 'vue'

// Mock vue-router before importing the composable
vi.mock('vue-router', () => ({
  useRouter: () => ({ replace: vi.fn() }),
  useRoute: () => ({ path: '/configure', query: {} }),
}))

import { useProjectForm } from '../useProjectForm'

describe('useProjectForm', () => {
  describe('isProjectTypeFromQuery', () => {
    it('is false by default (no query params)', () => {
      const { isProjectTypeFromQuery } = useProjectForm()
      expect(isProjectTypeFromQuery.value).toBe(false)
    })

    it('is true after initFromQuery with a valid projectType', () => {
      const { isProjectTypeFromQuery, initFromQuery } = useProjectForm()
      initFromQuery({ projectType: 'PROCESS_APPLICATION' })
      expect(isProjectTypeFromQuery.value).toBe(true)
    })

    it('remains false if query has no projectType', () => {
      const { isProjectTypeFromQuery, initFromQuery } = useProjectForm()
      initFromQuery({ groupId: 'com.example' })
      expect(isProjectTypeFromQuery.value).toBe(false)
    })

    it('remains false if projectType in query is invalid', () => {
      const { isProjectTypeFromQuery, initFromQuery } = useProjectForm()
      initFromQuery({ projectType: 'INVALID_TYPE' })
      expect(isProjectTypeFromQuery.value).toBe(false)
    })
  })

  describe('two-step build system (FR10)', () => {
    it('defaults to maven category and no gradleDsl', () => {
      const { buildSystemCategory, gradleDsl, form } = useProjectForm()
      expect(buildSystemCategory.value).toBe('maven')
      expect(gradleDsl.value).toBeNull()
      expect(form.buildSystem).toBe('MAVEN')
    })

    it('initFromQuery with MAVEN sets category to maven and clears gradleDsl', () => {
      const { buildSystemCategory, gradleDsl, initFromQuery } = useProjectForm()
      initFromQuery({ buildSystem: 'MAVEN' })
      expect(buildSystemCategory.value).toBe('maven')
      expect(gradleDsl.value).toBeNull()
    })

    it('initFromQuery with GRADLE_GROOVY sets category to gradle and gradleDsl to GRADLE_GROOVY', () => {
      const { buildSystemCategory, gradleDsl, initFromQuery } = useProjectForm()
      initFromQuery({ buildSystem: 'GRADLE_GROOVY' })
      expect(buildSystemCategory.value).toBe('gradle')
      expect(gradleDsl.value).toBe('GRADLE_GROOVY')
    })

    it('initFromQuery with GRADLE_KOTLIN sets category to gradle and gradleDsl to GRADLE_KOTLIN', () => {
      const { buildSystemCategory, gradleDsl, initFromQuery } = useProjectForm()
      initFromQuery({ buildSystem: 'GRADLE_KOTLIN' })
      expect(buildSystemCategory.value).toBe('gradle')
      expect(gradleDsl.value).toBe('GRADLE_KOTLIN')
    })

    it('switching buildSystemCategory to gradle without DSL makes form invalid (buildSystem error)', async () => {
      const { buildSystemCategory, gradleDsl, errors } = useProjectForm()
      buildSystemCategory.value = 'gradle'
      gradleDsl.value = null
      await nextTick()
      expect(errors.value.buildSystem).toBeTruthy()
    })

    it('switching to gradle and selecting GRADLE_KOTLIN clears build system error', async () => {
      const { buildSystemCategory, gradleDsl, errors } = useProjectForm()
      buildSystemCategory.value = 'gradle'
      gradleDsl.value = 'GRADLE_KOTLIN'
      await nextTick()
      expect(errors.value.buildSystem).toBeUndefined()
    })
  })

  describe('conditional rendering flags (FR45, FR46)', () => {
    it('PROCESS_APPLICATION: deploymentTarget error absent, githubActions no error', () => {
      const { form, errors, initFromQuery } = useProjectForm()
      initFromQuery({ projectType: 'PROCESS_APPLICATION' })
      expect(errors.value.deploymentTarget).toBeUndefined()
    })

    it('PROCESS_ARCHIVE without deploymentTarget produces a validation error', () => {
      const { form, errors } = useProjectForm()
      form.projectType = 'PROCESS_ARCHIVE'
      delete form.deploymentTarget
      expect(errors.value.deploymentTarget).toBeTruthy()
    })

    it('PROCESS_ARCHIVE with deploymentTarget clears deploymentTarget error', () => {
      const { form, errors } = useProjectForm()
      form.projectType = 'PROCESS_ARCHIVE'
      form.deploymentTarget = 'TOMCAT'
      expect(errors.value.deploymentTarget).toBeUndefined()
    })

    it('PROCESS_ARCHIVE forces githubActions off when the toggle is hidden', async () => {
      const { form } = useProjectForm()
      form.githubActions = true
      form.projectType = 'PROCESS_ARCHIVE'
      await nextTick()
      expect(form.githubActions).toBe(false)
    })

    it('restores the previous githubActions choice when switching back from PROCESS_ARCHIVE', async () => {
      const { form } = useProjectForm()
      form.githubActions = true
      form.projectType = 'PROCESS_ARCHIVE'
      await nextTick()
      form.projectType = 'PROCESS_APPLICATION'
      await nextTick()
      expect(form.githubActions).toBe(true)
    })
  })
})
