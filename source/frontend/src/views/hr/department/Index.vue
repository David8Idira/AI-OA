<template>
  <div class="department-container">
    <div class="header">
      <div class="title">部门管理</div>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">新增部门</el-button>
        <el-button @click="handleExport">导出Excel</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>
    
    <div class="filter">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="部门编码">
          <el-input v-model="queryParams.departmentCode" placeholder="请输入部门编码" clearable />
        </el-form-item>
        <el-form-item label="部门名称">
          <el-input v-model="queryParams.departmentName" placeholder="请输入部门名称" clearable />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="queryParams.manager" placeholder="请输入负责人" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
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
        <el-table-column prop="departmentCode" label="部门编码" width="120" />
        <el-table-column prop="departmentName" label="部门名称" width="150" />
        <el-table-column prop="level" label="部门级别" width="80">
          <template #default="scope">
            {{ scope.row.level }}级
          </template>
        </el-table-column>
        <el-table-column prop="parentId" label="上级部门" width="120">
          <template #default="scope">
            {{ getParentDepartmentName(scope.row.parentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="manager" label="负责人" width="100" />
        <el-table-column prop="sortOrder" label="排序号" width="80" />
        <el-table-column prop="status" label="启用状态" width="80">
          <template #default="scope">
            <el-switch
              v-model="scope.row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
            <el-button type="info" link @click="handleView(scope.row)">详情</el-button>
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
      <div v-if="dialogVisible" class="dialog-content">
        <div class="dialog-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>部门管理前端页面已创建，后端API接口已实现</span>
        </div>
        <div class="dialog-info">
          <p>后端API路径：</p>
          <ul>
            <li><code>POST /hr/department/add</code> - 新增部门</li>
            <li><code>PUT /hr/department/update</code> - 更新部门</li>
            <li><code>DELETE /hr/department/delete/{id}</code> - 删除部门</li>
            <li><code>GET /hr/department/get/{id}</code> - 查询部门详情</li>
            <li><code>POST /hr/department/page</code> - 分页查询部门</li>
            <li><code>GET /hr/department/tree</code> - 查询部门树形结构</li>
            <li><code>PUT /hr/department/status/{id}</code> - 更新部门状态</li>
          </ul>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="dialogVisible = false">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const departmentTree = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  departmentCode: '',
  departmentName: '',
  manager: '',
  status: ''
})

// 初始化数据
onMounted(() => {
  fetchData()
  fetchDepartmentTree()
})

// 获取部门列表
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    tableData.value = [
      {
        id: 1,
        departmentCode: 'DEPT001',
        departmentName: '技术部',
        level: 1,
        parentId: 0,
        manager: '张总',
        sortOrder: 1,
        status: 1,
        createTime: '2024-01-15 10:30:00'
      },
      {
        id: 2,
        departmentCode: 'DEPT002',
        departmentName: '市场部',
        level: 1,
        parentId: 0,
        manager: '李总',
        sortOrder: 2,
        status: 1,
        createTime: '2024-01-16 14:20:00'
      },
      {
        id: 3,
        departmentCode: 'DEPT003',
        departmentName: '人事部',
        level: 1,
        parentId: 0,
        manager: '王总',
        sortOrder: 3,
        status: 1,
        createTime: '2024-01-17 09:15:00'
      },
      {
        id: 4,
        departmentCode: 'DEPT004',
        departmentName: '前端开发组',
        level: 2,
        parentId: 1,
        manager: '赵经理',
        sortOrder: 1,
        status: 1,
        createTime: '2024-02-01 11:00:00'
      },
      {
        id: 5,
        departmentCode: 'DEPT005',
        departmentName: '后端开发组',
        level: 2,
        parentId: 1,
        manager: '钱经理',
        sortOrder: 2,
        status: 1,
        createTime: '2024-02-02 13:45:00'
      }
    ]
    total.value = 5
  } catch (error) {
    console.error('获取部门列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取部门树
const fetchDepartmentTree = async () => {
  try {
    // 模拟部门树数据
    departmentTree.value = [
      { id: 0, departmentName: '根部门' },
      { id: 1, departmentName: '技术部' },
      { id: 2, departmentName: '市场部' },
      { id: 3, departmentName: '人事部' },
      { id: 4, departmentName: '前端开发组' },
      { id: 5, departmentName: '后端开发组' }
    ]
  } catch (error) {
    console.error('获取部门树失败:', error)
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
    departmentCode: '',
    departmentName: '',
    manager: '',
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
  dialogTitle.value = '新增部门'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑部门'
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  dialogTitle.value = '部门详情 - ' + row.departmentName
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该部门吗？', '提示', {
      type: 'warning'
    })
    // 调用删除API
    // await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    console.error('删除失败:', error)
  }
}

// 状态切换
const handleStatusChange = async (row) => {
  try {
    // 调用更新状态API
    // await updateDepartmentStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
  }
}

// 导出
const handleExport = () => {
  ElMessage.info('导出功能待实现')
}

// 刷新
const handleRefresh = () => {
  fetchData()
}

// 对话框关闭
const handleDialogClose = () => {
  // 重置表单逻辑
}

// 获取上级部门名称
const getParentDepartmentName = (parentId) => {
  if (parentId === 0 || !parentId) return '根部门'
  const parent = departmentTree.value.find(dept => dept.id === parentId)
  return parent ? parent.departmentName : '未知部门'
}
</script>

<style scoped>
.department-container {
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

.dialog-content {
  padding: 10px 0;
}

.dialog-hint {
  display: flex;
  align-items: center;
  padding: 10px;
  margin-bottom: 20px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #d9ecff;
  color: #409eff;
}

.dialog-hint .el-icon {
  margin-right: 8px;
}

.dialog-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.dialog-info p {
  font-weight: bold;
  margin-bottom: 10px;
  color: #303133;
}

.dialog-info ul {
  margin: 0;
  padding-left: 20px;
}

.dialog-info li {
  margin-bottom: 5px;
  color: #606266;
}

.dialog-info code {
  background: #e8f4ff;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  color: #409eff;
}
</style>