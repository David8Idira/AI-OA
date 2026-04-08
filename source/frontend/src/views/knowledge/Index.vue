<template>
  <div class="knowledge-container">
    <!-- 搜索区域 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索知识库..."
        class="search-input"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch">搜索</el-button>
        </template>
      </el-input>
      
      <el-input
        v-model="semanticQuery"
        placeholder="语义搜索（支持自然语言）..."
        class="semantic-input"
        @keyup.enter="handleSemanticSearch"
      >
        <template #append>
          <el-button :icon="MagicStick" @click="handleSemanticSearch">语义搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 搜索结果 -->
    <div v-if="searchResults.length > 0 || semanticResults.length > 0" class="results-section">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="关键词搜索" name="keyword">
          <div class="result-list">
            <el-card v-for="doc in searchResults" :key="doc.id" class="result-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="doc-title">{{ doc.title }}</span>
                  <el-tag :type="getDocTypeTag(doc.docType)">{{ doc.docType }}</el-tag>
                </div>
              </template>
              <p>{{ doc.summary || doc.content.substring(0, 200) }}</p>
              <div class="card-footer">
                <span>浏览: {{ doc.viewCount }}</span>
                <span>点赞: {{ doc.likeCount }}</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="语义搜索" name="semantic">
          <div class="result-list">
            <el-card v-for="doc in semanticResults" :key="doc.id" class="result-card semantic-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <span class="doc-title">{{ doc.title }}</span>
                  <el-tag type="success">语义匹配</el-tag>
                </div>
              </template>
              <p>{{ doc.summary || doc.content.substring(0, 200) }}</p>
              <div class="card-footer">
                <span>相关度: 高</span>
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 知识库统计 -->
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="文档总数" :value="stats.totalDocs" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总浏览量" :value="stats.totalViews" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总点赞数" :value="stats.totalLikes" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="分类数" :value="stats.categories" />
        </el-col>
      </el-row>
    </div>

    <!-- 分类浏览 -->
    <div class="categories-section">
      <h3>分类浏览</h3>
      <div class="category-tags">
        <el-tag
          v-for="cat in categories"
          :key="cat.id"
          size="large"
          class="category-tag"
          @click="handleCategoryClick(cat)"
        >
          {{ cat.name }} ({{ cat.docCount }})
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, MagicStick } from '@element-plus/icons-vue'
import { searchKnowledge, semanticSearch, getKnowledgeStats, getKnowledgeCategories } from '@/api/knowledge'
import { ElMessage } from 'element-plus'

const searchKeyword = ref('')
const semanticQuery = ref('')
const searchResults = ref<any[]>([])
const semanticResults = ref<any[]>([])
const activeTab = ref('keyword')
const stats = ref({ totalDocs: 0, totalViews: 0, totalLikes: 0, categories: 0 })
const categories = ref<any[]>([])

// 搜索
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return
  try {
    const res = await searchKnowledge(searchKeyword.value)
    searchResults.value = res.data || []
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

// 语义搜索
const handleSemanticSearch = async () => {
  if (!semanticQuery.value.trim()) return
  try {
    const res = await semanticSearch(semanticQuery.value, 10)
    semanticResults.value = res.data || []
    activeTab.value = 'semantic'
  } catch (e) {
    ElMessage.error('语义搜索失败')
  }
}

// 获取统计
const loadStats = async () => {
  try {
    const res = await getKnowledgeStats()
    stats.value = res.data || {}
  } catch (e) {
    console.error('获取统计失败', e)
  }
}

// 获取分类
const loadCategories = async () => {
  try {
    const res = await getKnowledgeCategories()
    categories.value = res.data || []
  } catch (e) {
    console.error('获取分类失败', e)
  }
}

const getDocTypeTag = (type: string) => {
  const map: Record<string, string> = {
    manual: 'primary',
    faq: 'success',
    notice: 'warning'
  }
  return map[type] || 'info'
}

const handleCategoryClick = (cat: any) => {
  searchKeyword.value = cat.name
  handleSearch()
}

onMounted(() => {
  loadStats()
  loadCategories()
})
</script>

<style scoped>
.knowledge-container {
  padding: 20px;
}

.search-section {
  margin-bottom: 24px;
}

.search-input {
  margin-bottom: 12px;
}

.semantic-input {
  margin-bottom: 12px;
}

.results-section {
  margin-bottom: 24px;
}

.result-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.result-card {
  margin-bottom: 12px;
}

.semantic-card {
  border-left: 3px solid #67c23a;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.doc-title {
  font-weight: bold;
}

.card-footer {
  margin-top: 12px;
  color: #909399;
  font-size: 12px;
  display: flex;
  gap: 16px;
}

.stats-section {
  margin-bottom: 24px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.categories-section h3 {
  margin-bottom: 12px;
}

.category-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.category-tag {
  cursor: pointer;
}
</style>