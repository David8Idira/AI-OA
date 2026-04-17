<template>
  <div class="empty-wrapper">
    <el-empty :description="description" :image-size="imageSize">
      <template #image>
        <el-icon v-if="icon" :size="imageSize / 2" color="#c0c4cc">
          <component :is="icon" />
        </el-icon>
      </template>
      <el-button v-if="actionText" type="primary" @click="$emit('action')">
        {{ actionText }}
      </el-button>
      <slot />
    </el-empty>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  imageSize?: number
  icon?: string
  actionText?: string
}>()

defineEmits<{
  action: []
}>()
</script>

<style lang="scss" scoped>
.empty-wrapper {
  padding: 40px 0;
}
</style>
