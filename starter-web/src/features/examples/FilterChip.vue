<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  label: string
  isActive: boolean
  category: string
  colorClasses?: string
}

const props = withDefaults(defineProps<Props>(), { colorClasses: '' })
const emit = defineEmits<{ toggle: [] }>()

const ariaPressedValue = computed(() => props.isActive)

function handleClick() {
  emit('toggle')
}

function handleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ' ') {
    event.preventDefault()
    emit('toggle')
  }
}
</script>

<template>
  <button
    type="button"
    :aria-pressed="ariaPressedValue"
    class="filter-chip"
    :class="[{ 'filter-chip-active': isActive }, !isActive && colorClasses ? colorClasses : '']"
    @click="handleClick"
    @keydown="handleKeydown"
  >
    {{ label }}
    <span v-if="isActive" class="ml-1" aria-hidden="true">×</span>
  </button>
</template>

<style scoped>
.filter-chip {
  background: rgb(255, 255, 255);
  color: rgb(0, 0, 0);
  border: 1px solid rgb(227, 212, 221);
  border-radius: 999px;
  padding: 0.25rem 0.75rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms ease;
}

.filter-chip:hover {
  border-color: rgb(24, 74, 239);
}

.filter-chip:focus {
  outline: 2px solid rgb(24, 74, 239);
  outline-offset: 2px;
}

.filter-chip-active {
  background: rgb(24, 74, 239);
  color: rgb(255, 255, 255);
  border-color: rgb(24, 74, 239);
}
</style>
