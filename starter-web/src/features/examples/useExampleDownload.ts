import { ref } from 'vue'
import { downloadExample } from '@/generated/api'
import type { Example } from '@/generated/types'

export type DownloadState = 'idle' | 'downloading' | 'success' | 'error'

export interface DownloadStatus {
  state: DownloadState
  error?: string
}

export function useExampleDownload() {
  // Map example ID to download status
  const statusMap = ref<Map<string, DownloadStatus>>(new Map())

  function getStatus(exampleId: string): DownloadStatus {
    return statusMap.value.get(exampleId) ?? { state: 'idle' }
  }

  function setStatus(exampleId: string, status: DownloadStatus) {
    statusMap.value.set(exampleId, status)
  }

  async function download(example: Example) {
    const exampleId = example.id ?? ''
    setStatus(exampleId, { state: 'downloading' })

    try {
      const [owner = '', repo = ''] = (example.sourceRepo ?? '').split('/')
      const blob = await downloadExample(owner, repo, exampleId)
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `${exampleId}.zip`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)

      setStatus(exampleId, { state: 'success' })

      // Clear success message after 3 seconds
      setTimeout(() => {
        setStatus(exampleId, { state: 'idle' })
      }, 3000)
    } catch (error) {
      setStatus(exampleId, {
        state: 'error',
        error: error instanceof Error ? error.message : 'Unknown error',
      })
    }
  }

  function retry(exampleId: string) {
    // Reset to idle to allow retry
    setStatus(exampleId, { state: 'idle' })
  }

  return {
    getStatus,
    setStatus,
    download,
    retry,
  }
}
