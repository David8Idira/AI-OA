<template>
  <div class="page-header">
    <div class="page-header-left">
      <h2 v-if="title">{{ title }}</h2>
      <slot name="title" />
      <p v-if="description" class="page-description">{{ description }}</p>
    </div>
    <div class="page-header-right">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;

  h2 {
    margin: 0 0 4px;
    font-size: 20px;
    color: #333;
  }

  .page-description {
    margin: 0;
    color: #999;
    font-size: 14px;
  }
}
</style>
