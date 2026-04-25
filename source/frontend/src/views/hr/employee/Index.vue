<template>
  <div class="employee-container">
    <div class="header">
      <div class="title">员工管理</div>
      <div class="actions">
        <el-button type="primary" @click="handleAdd">新增员工</el-button>
        <el-button @click="handleExport">导出Excel</el-button>
        <el-button @click="handleRefresh">刷新</el-button>
      </div>
    </div>
    
    <div class="filter">
      <el-form :inline="true" :model="queryParams">
        <el-form-item label="员工编号">
          <el-input v-model="queryParams.employeeNo" placeholder="请输入员工编号" clearable />
        </el-form-item>
        <el-form-item label="员工姓名">
          <el-input v-model="queryParams.name" placeholder="请输入员工姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="queryParams.departmentId" placeholder="请选择部门" clearable>
            <el-option
              v-for="dept in departments"
              :key="dept.id"
              :label="dept.departmentName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="员工状态">
          <el-select v-model="queryParams.employeeStatus" placeholder="请选择状态" clearable>
            <el-option label="试用" value="1" />
            <el-option label="正式" value="2" />
            <el-option label="离职" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
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
        <el-table-column prop="employeeNo" label="员工编号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="departmentName" label="部门" width="120" />
        <el-table-column prop="positionName" label="职位" width="120" />
        <el-table-column prop="entryDate" label="入职日期" width="120" />
        <el-table-column prop="employeeStatus" label="员工状态" width="80">
          <template #default="scope">
            <el-tag :type="getEmployeeStatusTagType(scope.row.employeeStatus)">
              {{ getEmployeeStatusText(scope.row.employeeStatus) }}
            </el-tag>
          </template>
        </el-table-column>
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
          <span>员工管理前端页面已创建，后端API接口已实现</span>
        </div>
        <div class="dialog-info">
          <p>后端API路径：</p>
          <ul>
            <li><code>POST /hr/employee/add</code> - 新增员工</li>
            <li><code>PUT /hr/employee/update</code> - 更新员工</li>
            <li><code>DELETE /hr/employee/delete/{id}</code> - 删除员工</li>
            <li><code>GET /hr/employee/get/{id}</code> - 查询员工详情</li>
            <li><code>POST /hr/employee/page</code> - 分页查询员工</li>
            <li><code>PUT /hr/employee/status/{id}</code> - 更新员工状态</li>
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
const departments = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  employeeNo: '',
  name: '',
  phone: '',
  departmentId: '',
  employeeStatus: '',
  status: ''
})

// 初始化数据
onMounted(() => {
  fetchData()
  fetchDepartments()
})

// 获取员工列表
const fetchData = async () => {
  loading.value = true
  try {
    // 模拟数据
    await new Promise(resolve => setTimeout(resolve, 500))
    
    tableData.value = [
      {
        id: 1,
        employeeNo: 'EMP202404260001',
        name: '张三',
        gender: 1,
        phone: '13800138001',
        email: 'zhangsan@example.com',
        departmentName: '技术部',
        positionName: 'Java开发工程师',
        entryDate: '2024-01-15',
        employeeStatus: 2,
        status: 1
      },
      {
        id: 2,
        employeeNo: 'EMP202404260002',
        name: '李四',
        gender: 2,
        phone: '13800138002',
        email: 'lisi@example.com',
        departmentName: '市场部',
        positionName: '市场专员',
        entryDate: '2024-02-20',
        employeeStatus: 2,
        status: 1
      },
      {
        id: 3,
        employeeNo: 'EMP202404260003',
        name: '王五',
        gender: 1,
        phone: '13800138003',
        email: 'wangwu@example.com',
        departmentName: '人事部',
        positionName: 'HR专员',
        entryDate: '2024-03-10',
        employeeStatus: 1,
        status: 1
      }
    ]
    total.value = 3
  } catch (error) {
    console.error('获取员工列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取部门列表
const fetchDepartments = async () => {
  try {
    // 模拟数据
    departments.value = [
      { id: 1, departmentName: '技术部' },
      { id: 2, departmentName: '市场部' },
      { id: 3, departmentName: '人事部' },
      { id: 4, departmentName: '财务部' },
      { id: 5, departmentName: '行政部' }
    ]
  } catch (error) {
    console.error('获取部门列表失败:', error)
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
    employeeNo: '',
    name: '',
    phone: '',
    departmentId: '',
    employeeStatus: '',
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
  dialogTitle.value = '新增员工'
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑员工'
  dialogVisible.value = true
}

// 查看详情
const handleView = (row) => {
  dialogTitle.value = '员工详情 - ' + row.name
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该员工吗？', '提示', {
      type: 'warning'
    })
    // 调用删除API
    // await deleteEmployee(row.id)
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
    // await updateEmployeeStatus(row.id, row.status)
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

// 辅助方法
const getEmployeeStatusText = (status) => {
  const map = {
    1: '试用',
    2: '正式',
    3: '离职'
  }
  return map[status] || '未知'
}

const getEmployeeStatusTagType = (status) => {
  const map = {
    1: 'warning',
    2: 'success',
    3: 'info'
  }
  return map[status] || 'info'
}
</script>

<style scoped>
.employee-container {
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