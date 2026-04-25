<template>
  <div class="rules-container">
    <div class="header">
      <h2 class="title">考勤规则管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建规则
        </el-button>
      </div>
    </div>

    <!-- Search Form -->
    <div class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="规则名称">
          <el-input
            v-model="searchForm.ruleName"
            placeholder="请输入规则名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Data Table -->
    <div class="data-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="ruleName" label="规则名称" width="200" />
        <el-table-column prop="ruleCode" label="规则编码" width="120" />
        
        <el-table-column label="上班时间" width="100">
          <template #default="{ row }">
            {{ row.workStartTime || '--:--' }}
          </template>
        </el-table-column>
        
        <el-table-column label="下班时间" width="100">
          <template #default="{ row }">
            {{ row.workEndTime || '--:--' }}
          </template>
        </el-table-column>
        
        <el-table-column label="允许迟到" width="80">
          <template #default="{ row }">
            {{ row.allowLateMinutes || 0 }}分钟
          </template>
        </el-table-column>
        
        <el-table-column label="允许早退" width="80">
          <template #default="{ row }">
            {{ row.allowLeaveEarlyMinutes || 0 }}分钟
          </template>
        </el-table-column>
        
        <el-table-column label="加班规则" width="100">
          <template #default="{ row }">
            {{ getOvertimeRuleText(row.overtimeRule) }}
          </template>
        </el-table-column>
        
        <el-table-column label="灵活考勤" width="100">
          <template #default="{ row }">
            <el-tag :type="row.flexibleWork === 1 ? 'success' : 'info'" size="small">
              {{ row.flexibleWork === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="remark" label="备注" min-width="150" />
        
        <el-table-column label="创建时间" width="140">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="toggleStatus(row)"
              :disabled="row.status === 0 && row.isDefault"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
              :disabled="row.isDefault"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="searchForm.pageNum"
          v-model:page-size="searchForm.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- Rule Dialog -->
    <el-dialog
      v-model="ruleDialogVisible"
      :title="ruleDialogTitle"
      width="800px"
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则名称" prop="ruleName">
              <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则编码" prop="ruleCode">
              <el-input v-model="ruleForm.ruleCode" placeholder="请输入规则编码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="上班时间" prop="workStartTime">
              <el-time-picker
                v-model="ruleForm.workStartTime"
                placeholder="选择上班时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="下班时间" prop="workEndTime">
              <el-time-picker
                v-model="ruleForm.workEndTime"
                placeholder="选择下班时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="允许迟到" prop="allowLateMinutes">
              <el-input-number
                v-model="ruleForm.allowLateMinutes"
                :min="0"
                :max="120"
                placeholder="分钟"
              />
              <span class="form-unit">分钟</span>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="允许早退" prop="allowLeaveEarlyMinutes">
              <el-input-number
                v-model="ruleForm.allowLeaveEarlyMinutes"
                :min="0"
                :max="120"
                placeholder="分钟"
              />
              <span class="form-unit">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="加班规则" prop="overtimeRule">
          <el-radio-group v-model="ruleForm.overtimeRule">
            <el-radio :label="0">不计算加班</el-radio>
            <el-radio :label="1">下班后计算</el-radio>
            <el-radio :label="2">固定时间计算</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-row :gutter="20" v-if="ruleForm.overtimeRule === 2">
          <el-col :span="12">
            <el-form-item label="加班开始时间" prop="overtimeStartTime">
              <el-time-picker
                v-model="ruleForm.overtimeStartTime"
                placeholder="选择加班开始时间"
                format="HH:mm"
                value-format="HH:mm"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小加班时长" prop="minOvertimeDuration">
              <el-input-number
                v-model="ruleForm.minOvertimeDuration"
                :min="0"
                :max="480"
                placeholder="分钟"
              />
              <span class="form-unit">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="灵活考勤" prop="flexibleWork">
          <el-switch
            v-model="ruleForm.flexibleWork"
            :active-value="1"
            :inactive-value="0"
            @change="handleFlexibleWorkChange"
          />
        </el-form-item>

        <el-row :gutter="20" v-if="ruleForm.flexibleWork === 1">
          <el-col :span="12">
            <el-form-item label="灵活工时" prop="flexibleWorkHours">
              <el-input-number
                v-model="ruleForm.flexibleWorkHours"
                :min="1"
                :max="24"
                :step="0.5"
                placeholder="小时"
              />
              <span class="form-unit">小时</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="包含休息日" prop="includeRestDays">
          <el-switch
            v-model="ruleForm.includeRestDays"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>

        <el-form-item label="适用工作日" prop="weekdays">
          <el-select
            v-model="weekdaySelection"
            multiple
            placeholder="选择适用工作日"
            style="width: 100%"
          >
            <el-option label="周一" :value="1" />
            <el-option label="周二" :value="2" />
            <el-option label="周三" :value="3" />
            <el-option label="周四" :value="4" />
            <el-option label="周五" :value="5" />
            <el-option label="周六" :value="6" />
            <el-option label="周日" :value="7" />
          </el-select>
        </el-form-item>

        <el-form-item label="适用部门" prop="deptIds">
          <el-select
            v-model="deptSelection"
            multiple
            filterable
            placeholder="选择适用部门"
            style="width: 100%"
          >
            <el-option
              v-for="dept in deptList"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="适用职位" prop="positionIds">
          <el-select
            v-model="positionSelection"
            multiple
            filterable
            placeholder="选择适用职位"
            style="width: 100%"
          >
            <el-option
              v-for="position in positionList"
              :key="position.id"
              :label="position.name"
              :value="position.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="时区" prop="timezone">
          <el-select v-model="ruleForm.timezone" placeholder="选择时区">
            <el-option label="Asia/Shanghai (中国标准时间)" value="Asia/Shanghai" />
            <el-option label="Asia/Tokyo (日本时间)" value="Asia/Tokyo" />
            <el-option label="America/New_York (美国东部时间)" value="America/New_York" />
            <el-option label="Europe/London (伦敦时间)" value="Europe/London" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="ruleForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="ruleForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="ruleDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRuleForm" :loading="ruleLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { 
  getAttendanceRules,
  createAttendanceRule,
  updateAttendanceRule,
  deleteAttendanceRule 
} from '@/api/attendance'

// Reactive data
const loading = ref(false)
const ruleLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const ruleDialogVisible = ref(false)
const ruleFormRef = ref<FormInstance>()

// Search form
const searchForm = reactive({
  pageNum: 1,
  pageSize: 20,
  ruleName: '',
  status: null as number | null
})

// Rule form
const ruleForm = reactive({
  id: null as number | null,
  ruleName: '',
  ruleCode: '',
  workStartTime: '09:00',
  workEndTime: '18:00',
  allowLateMinutes: 10,
  allowLeaveEarlyMinutes: 10,
  overtimeRule: 0,
  overtimeStartTime: '19:00',
  minOvertimeDuration: 60,
  flexibleWork: 0,
  flexibleWorkHours: 8.5,
  includeRestDays: 0,
  weekdays: '[]',
  excludeHolidays: '[]',
  specialWorkDays: '[]',
  deptIds: '[]',
  positionIds: '[]',
  timezone: 'Asia/Shanghai',
  status: 1,
  remark: ''
})

// Selections
const weekdaySelection = ref<number[]>([])
const deptSelection = ref<string[]>([])
const positionSelection = ref<string[]>([])

// Mock data
const deptList = ref([
  { id: 'dept1', name: '技术部' },
  { id: 'dept2', name: '市场部' },
  { id: 'dept3', name: '人事部' },
  { id: 'dept4', name: '财务部' }
])

const positionList = ref([
  { id: 'pos1', name: '工程师' },
  { id: 'pos2', name: '经理' },
  { id: 'pos3', name: '主管' },
  { id: 'pos4', name: '专员' }
])

// Computed
const ruleDialogTitle = computed(() => {
  return ruleForm.id ? '编辑考勤规则' : '新建考勤规则'
})

// Rules
const ruleRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' }
  ],
  ruleCode: [
    { required: true, message: '请输入规则编码', trigger: 'blur' }
  ],
  workStartTime: [
    { required: true, message: '请选择上班时间', trigger: 'change' }
  ],
  workEndTime: [
    { required: true, message: '请选择下班时间', trigger: 'change' }
  ]
}

// Methods
const formatDateTime = (datetime: string) => {
  if (!datetime) return ''
  return format(new Date(datetime), 'yyyy-MM-dd HH:mm')
}

const getOvertimeRuleText = (rule: number) => {
  const map: Record<number, string> = {
    0: '不计算',
    1: '下班后',
    2: '固定时间'
  }
  return map[rule] || '未知'
}

const handleSearch = () => {
  searchForm.pageNum = 1
  loadTableData()
}

const resetSearch = () => {
  searchForm.pageNum = 1
  searchForm.ruleName = ''
  searchForm.status = null
  loadTableData()
}

const handleSizeChange = (size: number) => {
  searchForm.pageSize = size
  loadTableData()
}

const handleCurrentChange = (page: number) => {
  searchForm.pageNum = page
  loadTableData()
}

const loadTableData = async () => {
  try {
    loading.value = true
    
    const response = await getAttendanceRules(searchForm)
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载规则失败:', error)
    ElMessage.error('加载规则失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  // Reset form
  Object.assign(ruleForm, {
    id: null,
    ruleName: '',
    ruleCode: '',
    workStartTime: '09:00',
    workEndTime: '18:00',
    allowLateMinutes: 10,
    allowLeaveEarlyMinutes: 10,
    overtimeRule: 0,
    overtimeStartTime: '19:00',
    minOvertimeDuration: 60,
    flexibleWork: 0,
    flexibleWorkHours: 8.5,
    includeRestDays: 0,
    weekdays: '[]',
    excludeHolidays: '[]',
    specialWorkDays: '[]',
    deptIds: '[]',
    positionIds: '[]',
    timezone: 'Asia/Shanghai',
    status: 1,
    remark: ''
  })
  
  // Reset selections
  weekdaySelection.value = [1, 2, 3, 4, 5]
  deptSelection.value = []
  positionSelection.value = []
  
  ruleDialogVisible.value = true
}

const handleEdit = (row: any) => {
  // Copy row data to form
  Object.assign(ruleForm, row)
  
  // Parse selections
  try {
    if (row.weekdays) {
      weekdaySelection.value = JSON.parse(row.weekdays)
    }
    if (row.deptIds) {
      deptSelection.value = JSON.parse(row.deptIds)
    }
    if (row.positionIds) {
      positionSelection.value = JSON.parse(row.positionIds)
    }
  } catch (error) {
    console.error('Parse JSON error:', error)
  }
  
  ruleDialogVisible.value = true
}

const toggleStatus = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const confirmMessage = newStatus === 1 ? '确定启用该规则吗？' : '确定停用该规则吗？'
    
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await updateAttendanceRule(row.id, { status: newStatus })
    
    if (response.code === 200) {
      ElMessage.success(newStatus === 1 ? '启用成功' : '停用成功')
      loadTableData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该规则吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteAttendanceRule(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadTableData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleFlexibleWorkChange = (value: number) => {
  if (value === 1) {
    // Enable flexible work
  } else {
    // Disable flexible work
  }
}

const submitRuleForm = async () => {
  if (!ruleFormRef.value) return
  
  try {
    await ruleFormRef.value.validate()
    
    // Prepare form data
    const formData = { ...ruleForm }
    
    // Convert selections to JSON strings
    formData.weekdays = JSON.stringify(weekdaySelection.value)
    formData.deptIds = JSON.stringify(deptSelection.value)
    formData.positionIds = JSON.stringify(positionSelection.value)
    
    ruleLoading.value = true
    
    let response
    if (formData.id) {
      response = await updateAttendanceRule(formData.id, formData)
    } else {
      response = await createAttendanceRule(formData)
    }
    
    if (response.code === 200) {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      ruleDialogVisible.value = false
      loadTableData()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    ruleLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.rules-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.search-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.data-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-unit {
  margin-left: 8px;
  color: #909399;
}

@media (max-width: 768px) {
  .rules-container {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .search-card .el-form {
    display: grid;
    grid-template-columns: 1fr;
    gap: 16px;
  }
}
</style>