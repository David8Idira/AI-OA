<template>
  <div class="item-selector">
    <div class="selector-header">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索办公用品"
        style="width: 300px; margin-right: 15px;"
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-button type="primary" @click="handleConfirm">确认选择</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </div>
    
    <div class="selector-content">
      <div class="categories">
        <div class="category-list">
          <div
            v-for="category in categories"
            :key="category.id"
            class="category-item"
            :class="{ active: activeCategory === category.id }"
            @click="activeCategory = category.id"
          >
            {{ category.name }}
            <span class="item-count">({{ category.itemCount }})</span>
          </div>
        </div>
      </div>
      
      <div class="items-grid">
        <div class="items-container">
          <div
            v-for="item in filteredItems"
            :key="item.id"
            class="item-card"
            :class="{ selected: selectedItems.some(selected => selected.id === item.id) }"
            @click="handleItemClick(item)"
          >
            <div class="item-image">
              <el-icon v-if="!item.image" size="40" color="#909399"><Box /></el-icon>
              <img v-else :src="item.image" :alt="item.name" />
            </div>
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-spec">{{ item.specification }}</div>
              <div class="item-price">¥{{ item.price?.toFixed(2) || '0.00' }}</div>
              <div class="item-stock">库存: {{ item.stock || 0 }} {{ item.unit || '个' }}</div>
            </div>
            <div class="item-select">
              <el-checkbox
                :model-value="selectedItems.some(selected => selected.id === item.id)"
                @click.stop
                @change="(checked) => handleCheckboxChange(checked, item)"
              />
            </div>
          </div>
        </div>
        
        <div class="empty-state" v-if="filteredItems.length === 0">
          <el-empty description="未找到相关办公用品" />
        </div>
      </div>
    </div>
    
    <div class="selector-footer">
      <div class="selected-summary">
        <div class="summary-title">已选择 {{ selectedItems.length }} 个物品</div>
        <div class="selected-list">
          <el-tag
            v-for="item in selectedItems"
            :key="item.id"
            closable
            @close="handleRemoveSelected(item.id)"
          >
            {{ item.name }}
          </el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Box } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['select', 'cancel'])

const searchKeyword = ref('')
const activeCategory = ref('all')
const items = ref([])
const selectedItems = ref([])

// 模拟分类数据
const categories = ref([
  { id: 'all', name: '全部', itemCount: 32 },
  { id: 'stationery', name: '文具', itemCount: 12 },
  { id: 'paper', name: '纸张', itemCount: 5 },
  { id: 'computer', name: '电脑耗材', itemCount: 8 },
  { id: 'office', name: '办公设备', itemCount: 4 },
  { id: 'cleaning', name: '清洁用品', itemCount: 3 }
])

// 模拟办公用品数据
const mockItems = [
  { id: '1', name: 'A4打印纸', specification: '70g', price: 25.00, unit: '包', stock: 50, category: 'paper' },
  { id: '2', name: '签字笔', specification: '黑色0.5mm', price: 2.50, unit: '支', stock: 200, category: 'stationery' },
  { id: '3', name: '笔记本', specification: 'A5 80页', price: 8.00, unit: '本', stock: 100, category: 'stationery' },
  { id: '4', name: '文件夹', specification: 'A4 蓝色', price: 5.00, unit: '个', stock: 80, category: 'stationery' },
  { id: '5', name: '订书机', specification: '标准型', price: 15.00, unit: '个', stock: 30, category: 'stationery' },
  { id: '6', name: '计算器', specification: '12位', price: 35.00, unit: '个', stock: 20, category: 'office' },
  { id: '7', name: '鼠标', specification: '有线USB', price: 25.00, unit: '个', stock: 40, category: 'computer' },
  { id: '8', name: '键盘', specification: '有线USB', price: 45.00, unit: '个', stock: 25, category: 'computer' },
  { id: '9', name: 'U盘', specification: '32GB USB3.0', price: 55.00, unit: '个', stock: 60, category: 'computer' },
  { id: '10', name: '墨盒', specification: 'HP 305A', price: 120.00, unit: '个', stock: 15, category: 'computer' },
  { id: '11', name: '纸巾', specification: '抽纸', price: 12.00, unit: '包', stock: 80, category: 'cleaning' },
  { id: '12', name: '洗手液', specification: '500ml', price: 18.00, unit: '瓶', stock: 40, category: 'cleaning' }
]

// 过滤后的物品
const filteredItems = computed(() => {
  let result = items.value
  
  // 按分类过滤
  if (activeCategory.value !== 'all') {
    result = result.filter(item => item.category === activeCategory.value)
  }
  
  // 按关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(item => 
      item.name.toLowerCase().includes(keyword) ||
      item.specification?.toLowerCase().includes(keyword)
    )
  }
  
  return result
})

// 初始化数据
onMounted(() => {
  items.value = mockItems
})

// 搜索
const handleSearch = () => {
  // 搜索逻辑已经在computed中实现
}

// 物品点击
const handleItemClick = (item) => {
  const index = selectedItems.value.findIndex(selected => selected.id === item.id)
  if (index > -1) {
    selectedItems.value.splice(index, 1)
  } else {
    selectedItems.value.push(item)
  }
}

// 复选框变化
const handleCheckboxChange = (checked, item) => {
  if (checked) {
    if (!selectedItems.value.some(selected => selected.id === item.id)) {
      selectedItems.value.push(item)
    }
  } else {
    const index = selectedItems.value.findIndex(selected => selected.id === item.id)
    if (index > -1) {
      selectedItems.value.splice(index, 1)
    }
  }
}

// 移除已选物品
const handleRemoveSelected = (itemId) => {
  const index = selectedItems.value.findIndex(item => item.id === itemId)
  if (index > -1) {
    selectedItems.value.splice(index, 1)
  }
}

// 确认选择
const handleConfirm = () => {
  if (selectedItems.value.length === 0) {
    ElMessage.warning('请至少选择一个物品')
    return
  }
  emit('select', selectedItems.value)
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.item-selector {
  display: flex;
  flex-direction: column;
  height: 600px;
}

.selector-header {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 20px;
}

.selector-content {
  display: flex;
  flex: 1;
  gap: 20px;
  overflow: hidden;
}

.categories {
  width: 200px;
  border-right: 1px solid #e4e7ed;
  padding-right: 20px;
  overflow-y: auto;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  padding: 12px 15px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.category-item:hover {
  background: #f5f7fa;
}

.category-item.active {
  background: #ecf5ff;
  color: #409eff;
  font-weight: 500;
}

.item-count {
  font-size: 12px;
  color: #909399;
}

.category-item.active .item-count {
  color: #409eff;
}

.items-grid {
  flex: 1;
  overflow-y: auto;
}

.items-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
  padding: 5px;
}

.item-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
  background: white;
}

.item-card:hover {
  border-color: #409eff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.item-card.selected {
  border-color: #409eff;
  background: #ecf5ff;
}

.item-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 80px;
  margin-bottom: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}

.item-image img {
  max-width: 100%;
  max-height: 80px;
  object-fit: contain;
}

.item-info {
  text-align: center;
}

.item-name {
  font-weight: bold;
  margin-bottom: 5px;
  color: #303133;
  font-size: 14px;
}

.item-spec {
  color: #606266;
  font-size: 12px;
  margin-bottom: 5px;
  min-height: 18px;
}

.item-price {
  color: #f56c6c;
  font-weight: bold;
  font-size: 14px;
  margin-bottom: 5px;
}

.item-stock {
  color: #67c23a;
  font-size: 12px;
}

.item-select {
  position: absolute;
  top: 10px;
  right: 10px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  width: 100%;
}

.selector-footer {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.selected-summary {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-title {
  font-weight: bold;
  color: #303133;
  font-size: 14px;
}

.selected-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 80px;
  overflow-y: auto;
}
</style>