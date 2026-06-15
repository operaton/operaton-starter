<script setup lang="ts">
interface Props {
  hasActiveFilters: boolean
}

defineProps<Props>()
defineEmits<{
  'clear-filters': []
}>()
</script>

<template>
  <div class="empty-state">
    <p v-if="!hasActiveFilters" class="empty-state-text">
      No examples are available right now. Check back soon for community-contributed examples.
    </p>
    <p v-else class="empty-state-text">
      No examples match these filters.
    </p>

    <button
      v-if="hasActiveFilters"
      type="button"
      class="clear-button"
      @click="$emit('clear-filters')"
    >
      Clear filters
    </button>
    <a
      v-else
      href="docs/examples-repository-format.md"
      class="docs-button"
    >
      View format docs →
    </a>
  </div>
</template>

<style scoped>
.empty-state {
  background: rgb(245, 240, 243);
  border: 1px dashed rgb(227, 212, 221);
  border-radius: 0.5em;
  padding: 2rem;
  text-align: center;
  color: rgb(102, 102, 102);
}

.empty-state-text {
  font-size: 0.875rem;
  margin-bottom: 1rem;
  line-height: 1.5;
}

.clear-button,
.docs-button {
  font-size: 0.875rem;
  font-weight: 500;
  padding: 0.5rem 1rem;
  border-radius: 0.5em;
  border: 1px solid rgb(102, 102, 102);
  background: transparent;
  color: rgb(102, 102, 102);
  cursor: pointer;
  transition: all 150ms ease;
  text-decoration: none;
  display: inline-block;
}

.clear-button:hover,
.docs-button:hover {
  color: rgb(24, 74, 239);
  border-color: rgb(24, 74, 239);
}

.clear-button:focus,
.docs-button:focus {
  outline: 2px solid rgb(24, 74, 239);
  outline-offset: 2px;
}
</style>
