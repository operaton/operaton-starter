import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    outDir: '../starter-server/src/main/resources/static',
    emptyOutDir: false
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
