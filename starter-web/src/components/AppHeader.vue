<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <header class="h-16 bg-neutral-0 border-b border-neutral-200 flex items-center px-6 md:px-8">
    <div class="flex items-center gap-3 flex-1">
      <RouterLink to="/" class="flex items-center gap-2 no-underline" aria-label="Operaton Starter home">
        <div class="w-8 h-8 bg-primary rounded-s flex items-center justify-center" aria-hidden="true">
          <span class="text-white text-xs font-bold">O</span>
        </div>
        <span class="text-lg font-bold text-neutral-900">
          Operaton <span class="font-normal text-neutral-500">Starter</span>
        </span>
      </RouterLink>
    </div>
    <nav aria-label="External links" class="flex items-center gap-6">
      <a href="https://docs.operaton.org" target="_blank" rel="noopener noreferrer"
         class="text-sm text-neutral-500 hover:text-primary transition-colors">Docs</a>
      <a href="https://operaton.org" target="_blank" rel="noopener noreferrer"
         class="text-sm text-neutral-500 hover:text-primary transition-colors">operaton.org</a>
      <a href="https://github.com/operaton/operaton-starter" target="_blank" rel="noopener noreferrer"
         class="text-sm text-neutral-500 hover:text-primary transition-colors">GitHub</a>
    </nav>
  </header>
</template>
