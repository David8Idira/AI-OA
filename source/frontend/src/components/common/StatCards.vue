<template>
  <div class="stat-cards">
    <el-card
      v-for="item in items"
      :key="item.title"
      shadow="hover"
      class="stat-card"
      :class="{ 'stat-card-clickable': item.onClick }"
      @click="item.onClick && item.onClick()"
    >
      <div class="stat-content">
        <div class="stat-icon" :style="{ background: item.color || '#667eea' }">
          <el-icon :size="24" color="#fff">
            <component :is="item.icon" />
          </el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-title">{{ item.title }}</div>
          <div class="stat-value" :style="{ color: item.color || '#667eea' }">
            {{ formatValue(item) }}
          </div>
          <div v-if="item.subtitle" class="stat-subtitle">{{ item.subtitle }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
interface StatCardItem {
  title: string
  value: number | string
  icon: string
  color?: string
  subtitle?: string
  onClick?: () => void
  prefix?: string
  suffix?: string
}

defineProps<{
  items: StatCardItem[]
}>()

const formatValue = (item: StatCardItem) => {
  let value = item.value
  if (typeof value === 'number') {
    value = value.toLocaleString()
  }
  if (item.prefix) value = item.prefix + value
  if (item.suffix) value = value + item.suffix
  return value
}
</script>

<style lang="scss" scoped>
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  &.stat-card-clickable {
    cursor: pointer;
    transition: transform 0.2s;

    &:hover {
      transform: translateY(-2px);
    }
  }

  .stat-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    flex: 1;

    .stat-title {
      font-size: 14px;
      color: #999;
      margin-bottom: 4px;
    }

    .stat-value {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 2px;
    }

    .stat-subtitle {
      font-size: 12px;
      color: #666;
    }
  }
}
</style>
