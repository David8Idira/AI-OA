<template>
  <div class="asset-info-container">
    <div class="header">
      <div class="title">资产管理</div>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">新增资产</el-button>
        <el-button @click="handleExport">导出Excel</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>
    
    <div class="filter">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="资产名称">
          <el-input v-model="queryParams.assetName" placeholder="请输入资产名称" clearable />
        </el-form-item>
        <el-form-item label="资产分类">
          <el-select v-model="queryParams.categoryId" placeholder="请选择分类" clearable>
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.categoryName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="资产状态">
          <el-select v-model="queryParams.assetStatus" placeholder="请选择状态" clearable>
            <el-option label="正常" value="1" />
            <el-option label="领用中" value="2" />
            <el-option label="维修中" value="3" />
            <el-option label="报废" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="content">
      <el-table :data="tableData" v-loading="loading" border style="width: 100%">
        <el-table-column prop="assetCode" label="资产编码" width="120" />
        <el-table-column prop="assetName" label="资产名称" width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="model" label="型号" width="120" />
        <el-table-column prop="currentQuantity" label="当前数量" width="80" />
        <el-table-column prop="warningQuantity" label="预警数量" width="80">
          <template #default="scope">
            <span :class="{ 'warning-text': scope.row.currentQuantity <= scope.row.warningQuantity }">
              {{ scope.row.warningQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="purchasePrice" label="购买价格" width="100">
          <template #default="scope">
            {{ formatCurrency(scope.row.purchasePrice) }}
          </template>
        </el-table-column>
        <el-table-column prop="responsiblePerson" label="负责人" width="100" />
        <el-table-column prop="location" label="存放位置" width="120" />
        <el-table-column prop="assetStatus" label="资产状态" width="80">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.assetStatus)">
              {{ getStatusText(scope.row.assetStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" link @click="handleBorrow(scope.row)">领用</el-button>
            <el-button type="warning" link @click="handleReturn(scope.row)">归还</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <AssetForm
        v-if="dialogVisible"
        ref="formRef"
        :formData="formData"
        :categories="categories"
        @submit="handleSubmit"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import AssetForm from './components/AssetForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAssetList, updateAssetStatus, deleteAsset } from '@/api/asset'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  assetName: '',
  categoryId: '',
  assetStatus: '',
  status: ''
})

// 初始化数据
onMounted(() => {
  fetchData()
  fetchCategories()
})

// 获取资产列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAssetList(queryParams)
    tableData.value = res.data.records
    total.value = res.data.total
  } catch (error) {
    console.error('获取资产列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const res = await getCategories()
    categories.value = res.data
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    assetName: '',
    categoryId: '',
    assetStatus: '',
    status: ''
  })
  fetchData()
}

// 分页大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchData()
}

// 当前页变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增资产'
  formData.value = {}
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑资产'
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该资产吗？', '提示', {
      type: 'warning'
    })
    await deleteAsset(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 状态切换
const handleStatusChange = async (row) => {
  try {
    await updateAssetStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 领用
const handleBorrow = (row) => {
  // 实现领用逻辑
  console.log('领用资产:', row)
}

// 归还
const handleReturn = (row) => {
  // 实现归还逻辑
  console.log('归还资产:', row)
}

// 导出
const handleExport = () => {
  // 实现导出逻辑
  console.log('导出资产')
}

// 刷新
const handleRefresh = () => {
  fetchData()
}

// 提交表单
const handleSubmit = async (formData) => {
  try {
    // 调用API保存数据
    await saveAsset(formData)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('保存失败:', error)
  }
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetForm()
}

// 辅助方法
const formatCurrency = (value) => {
  if (!value) return '¥0.00'
  return '¥' + parseFloat(value).toFixed(2)
}

const getStatusText = (status) => {
  const map = {
    1: '正常',
    2: '领用中',
    3: '维修中',
    4: '报废'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status) => {
  const map = {
    1: 'success',
    2: 'primary',
    3: 'warning',
    4: 'danger'
  }
  return map[status] || 'info'
}
</script>

<style scoped>
.asset-info-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header .title {
  font-size: 18px;
  font-weight: bold;
}

.filter {
  margin-bottom: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 4px;
}

.content {
  background: #fff;
  padding: 20px;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.warning-text {
  color: #f56c6c;
  font-weight: bold;
}
</style>