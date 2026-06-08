<script setup lang="ts">
const rawVersion = import.meta.env.VITE_APP_VERSION as string | undefined
const version = rawVersion ? rawVersion.replace(/-.*$/, '') : ''
</script>

<template>
  <footer class="border-t border-neutral-200 bg-neutral-0 py-6 px-6 md:px-8 text-center">
    <p class="text-sm text-neutral-500">
      <a href="https://github.com/operaton/operaton-starter" target="_blank" rel="noopener noreferrer"
         class="hover:text-primary">operaton-starter<template v-if="version"> {{ version }}</template></a>
      · Apache 2.0 ·
      <a href="https://operaton.org" target="_blank" rel="noopener noreferrer"
         class="hover:text-primary">operaton.org</a>
    </p>
  </footer>
</template>
