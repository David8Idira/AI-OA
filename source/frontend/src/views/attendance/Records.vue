<template>
  <div class="records-container">
    <div class="header">
      <h2 class="title">考勤记录查询</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportRecords" :loading="exportLoading">
          <el-icon><Download /></el-icon>
          导出记录
        </el-button>
      </div>
    </div>

    <!-- Search Form -->
    <div class="search-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="查询日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="handleDateRangeChange"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="迟到" :value="1" />
            <el-option label="早退" :value="2" />
            <el-option label="缺勤" :value="3" />
            <el-option label="加班" :value="4" />
            <el-option label="请假" :value="5" />
            <el-option label="出差" :value="6" />
          </el-select>
        </el-form-item>

        <el-form-item label="异常">
          <el-select v-model="queryParams.abnormal" placeholder="异常状态" clearable>
            <el-option label="正常" :value="0" />
            <el-option label="异常" :value="1" />
          </el-select>
        </el-form-item>

        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="姓名/地址/备注"
            clearable
          />
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

    <!-- Statistics -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" v-for="stat in quickStats" :key="stat.label">
          <div class="stat-card">
            <div class="stat-icon" :style="{ background: stat.color }">
              <component :is="stat.icon" />
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Data Table -->
    <div class="data-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="attendanceDate" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.attendanceDate) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="userName" label="姓名" width="100" />
        
        <el-table-column label="上班时间" width="120">
          <template #default="{ row }">
            <div v-if="row.checkinTime" class="time-cell">
              <div>{{ formatTime(row.checkinTime) }}</div>
              <div class="method-tag">
                <el-tag size="small" :type="getMethodTagType(row.checkinMethod)">
                  {{ getMethodText(row.checkinMethod) }}
                </el-tag>
              </div>
            </div>
            <span v-else class="empty-text">--:--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="下班时间" width="120">
          <template #default="{ row }">
            <div v-if="row.checkoutTime" class="time-cell">
              <div>{{ formatTime(row.checkoutTime) }}</div>
              <div class="method-tag">
                <el-tag size="small" :type="getMethodTagType(row.checkoutMethod)">
                  {{ getMethodText(row.checkoutMethod) }}
                </el-tag>
              </div>
            </div>
            <span v-else class="empty-text">--:--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="工作时长" width="100">
          <template #default="{ row }">
            <span v-if="row.workHours" class="hours-text">
              {{ row.workHours.toFixed(1) }}h
            </span>
            <span v-else class="empty-text">--</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="异常" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.abnormal === 1" type="danger" size="small">
              异常
            </el-tag>
            <span v-else class="normal-text">正常</span>
          </template>
        </el-table-column>
        
        <el-table-column label="迟到/早退" width="120">
          <template #default="{ row }">
            <div v-if="row.lateMinutes > 0" class="late-text">
              迟到 {{ row.lateMinutes }} 分钟
            </div>
            <div v-else-if="row.leaveEarlyMinutes > 0" class="early-text">
              早退 {{ row.leaveEarlyMinutes }} 分钟
            </div>
            <span v-else class="empty-text">--</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="checkinAddress" label="打卡地址" min-width="200">
          <template #default="{ row }">
            <div v-if="row.checkinAddress" class="address-cell">
              <el-icon><Location /></el-icon>
              <span class="address-text">{{ row.checkinAddress }}</span>
            </div>
            <span v-else class="empty-text">--</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="remark" label="备注" min-width="150" />
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="viewRecordDetail(row)"
            >
              详情
            </el-button>
            <el-button
              v-if="hasPermission('attendance:edit')"
              type="primary"
              link
              @click="editRecord(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
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

    <!-- Detail Dialog -->
    <el-dialog
      v-model="detailVisible"
      title="考勤记录详情"
      width="600px"
    >
      <div v-if="currentRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日期">
            {{ formatDate(currentRecord.attendanceDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="姓名">
            {{ currentRecord.userName }}
          </el-descriptions-item>
          <el-descriptions-item label="上班时间">
            {{ formatDateTime(currentRecord.checkinTime) || '--:--' }}
          </el-descriptions-item>
          <el-descriptions-item label="下班时间">
            {{ formatDateTime(currentRecord.checkoutTime) || '--:--' }}
          </el-descriptions-item>
          <el-descriptions-item label="上班方式">
            {{ getMethodText(currentRecord.checkinMethod) }}
          </el-descriptions-item>
          <el-descriptions-item label="下班方式">
            {{ getMethodText(currentRecord.checkoutMethod) }}
          </el-descriptions-item>
          <el-descriptions-item label="工作时长">
            {{ currentRecord.workHours?.toFixed(1) || '0' }} 小时
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="迟到分钟" v-if="currentRecord.lateMinutes > 0">
            <span class="late-text">{{ currentRecord.lateMinutes }} 分钟</span>
          </el-descriptions-item>
          <el-descriptions-item label="早退分钟" v-if="currentRecord.leaveEarlyMinutes > 0">
            <span class="early-text">{{ currentRecord.leaveEarlyMinutes }} 分钟</span>
          </el-descriptions-item>
          <el-descriptions-item label="加班分钟" v-if="currentRecord.overtimeMinutes > 0">
            <span class="overtime-text">{{ currentRecord.overtimeMinutes }} 分钟</span>
          </el-descriptions-item>
          <el-descriptions-item label="上班地址" :span="2">
            {{ currentRecord.checkinAddress || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="下班地址" :span="2">
            {{ currentRecord.checkoutAddress || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="设备ID" :span="2">
            上班: {{ currentRecord.checkinDeviceId || '--' }}
            下班: {{ currentRecord.checkoutDeviceId || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="IP地址" :span="2">
            上班: {{ currentRecord.checkinIp || '--' }}
            下班: {{ currentRecord.checkoutIp || '--' }}
          </el-descriptions-item>
          <el-descriptions-item label="异常原因" :span="2" v-if="currentRecord.abnormalReason">
            {{ currentRecord.abnormalReason }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ currentRecord.remark || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailVisible = false">关闭</el-button>
          <el-button
            v-if="hasPermission('attendance:edit')"
            type="primary"
            @click="editRecord(currentRecord)"
          >
            编辑
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog
      v-model="editVisible"
      :title="editTitle"
      width="500px"
    >
      <el-form
        v-if="editForm"
        ref="editFormRef"
        :model="editForm"
        :rules="editRules"
        label-width="100px"
      >
        <el-form-item label="上班时间" prop="checkinTime">
          <el-date-picker
            v-model="editForm.checkinTime"
            type="datetime"
            placeholder="选择上班时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="下班时间" prop="checkoutTime">
          <el-date-picker
            v-model="editForm.checkoutTime"
            type="datetime"
            placeholder="选择下班时间"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="editForm.status">
            <el-option label="正常" :value="0" />
            <el-option label="迟到" :value="1" />
            <el-option label="早退" :value="2" />
            <el-option label="缺勤" :value="3" />
            <el-option label="加班" :value="4" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="异常" prop="abnormal">
          <el-switch
            v-model="editForm.abnormal"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        
        <el-form-item label="异常原因" prop="abnormalReason">
          <el-input
            v-model="editForm.abnormalReason"
            type="textarea"
            :rows="2"
            placeholder="请输入异常原因"
          />
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit" :loading="editLoading">
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
import { 
  Search, Download, Location, Clock, User, Check, Warning 
} from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { 
  getAttendanceRecords,
  updateAttendanceRecord,
  exportAttendanceRecords 
} from '@/api/attendance'

// Reactive data
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])
const detailVisible = ref(false)
const editVisible = ref(false)
const editLoading = ref(false)
const currentRecord = ref<any>(null)

// Query parameters
const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  userId: '',
  startDate: '',
  endDate: '',
  status: null as number | null,
  abnormal: null as number | null,
  keyword: '',
  orderBy: 'attendanceDate desc, checkinTime desc'
})

// Edit form
const editForm = ref<any>(null)
const editFormRef = ref<FormInstance>()
const editTitle = computed(() => {
  return currentRecord.value ? `编辑考勤记录 - ${currentRecord.value.userName}` : '编辑考勤记录'
})

// Edit rules
const editRules = {
  checkinTime: [
    { required: false, message: '请选择上班时间', trigger: 'blur' }
  ],
  checkoutTime: [
    { required: false, message: '请选择下班时间', trigger: 'blur' }
  ]
}

// Status texts
const statusTexts = {
  0: '正常',
  1: '迟到',
  2: '早退',
  3: '缺勤',
  4: '加班',
  5: '请假',
  6: '出差'
}

const statusTagTypes = {
  0: 'success',
  1: 'warning',
  2: 'warning',
  3: 'danger',
  4: 'info',
  5: 'info',
  6: 'info'
}

// Method texts
const methodTexts = {
  0: 'GPS',
  1: 'WiFi',
  2: '手动',
  3: '远程'
}

const methodTagTypes = {
  0: 'success',
  1: 'info',
  2: 'warning',
  3: 'primary'
}

// Quick statistics
const quickStats = computed(() => {
  const stats = {
    totalDays: tableData.value.length,
    normalDays: tableData.value.filter(r => r.status === 0).length,
    lateDays: tableData.value.filter(r => r.status === 1).length,
    leaveEarlyDays: tableData.value.filter(r => r.status === 2).length,
    absentDays: tableData.value.filter(r => r.status === 3).length,
    overtimeDays: tableData.value.filter(r => r.status === 4).length,
    totalWorkHours: tableData.value.reduce((sum, r) => sum + (r.workHours || 0), 0).toFixed(1)
  }
  
  return [
    {
      icon: Clock,
      value: stats.totalDays,
      label: '总天数',
      color: '#409eff'
    },
    {
      icon: Check,
      value: stats.normalDays,
      label: '正常天数',
      color: '#67c23a'
    },
    {
      icon: Warning,
      value: stats.lateDays + stats.leaveEarlyDays,
      label: '异常天数',
      color: '#e6a23c'
    },
    {
      icon: User,
      value: stats.absentDays,
      label: '缺勤天数',
      color: '#f56c6c'
    }
  ]
})

// Methods
const formatDate = (date: string) => {
  return format(new Date(date), 'yyyy-MM-dd')
}

const formatTime = (time: string) => {
  if (!time) return '--:--'
  return format(new Date(time), 'HH:mm')
}

const formatDateTime = (datetime: string) => {
  if (!datetime) return null
  return format(new Date(datetime), 'yyyy-MM-dd HH:mm:ss')
}

const getStatusText = (status: number) => {
  return statusTexts[status as keyof typeof statusTexts] || '未知'
}

const getStatusTagType = (status: number) => {
  return statusTagTypes[status as keyof typeof statusTagTypes] || 'info'
}

const getMethodText = (method: number) => {
  return methodTexts[method as keyof typeof methodTexts] || '未知'
}

const getMethodTagType = (method: number) => {
  return methodTagTypes[method as keyof typeof methodTagTypes] || 'info'
}

const handleDateRangeChange = (range: string[]) => {
  if (range && range.length === 2) {
    queryParams.startDate = range[0]
    queryParams.endDate = range[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
}

const handleSearch = () => {
  queryParams.pageNum = 1
  loadTableData()
}

const resetSearch = () => {
  queryParams.pageNum = 1
  queryParams.startDate = ''
  queryParams.endDate = ''
  queryParams.status = null
  queryParams.abnormal = null
  queryParams.keyword = ''
  dateRange.value = []
  loadTableData()
}

const handleSizeChange = (size: number) => {
  queryParams.pageSize = size
  loadTableData()
}

const handleCurrentChange = (page: number) => {
  queryParams.pageNum = page
  loadTableData()
}

const loadTableData = async () => {
  try {
    loading.value = true
    
    // Set default date range if not specified
    if (!queryParams.startDate || !queryParams.endDate) {
      const endDate = new Date()
      const startDate = new Date()
      startDate.setDate(startDate.getDate() - 30)
      
      queryParams.startDate = format(startDate, 'yyyy-MM-dd')
      queryParams.endDate = format(endDate, 'yyyy-MM-dd')
      
      dateRange.value = [queryParams.startDate, queryParams.endDate]
    }
    
    // Set current user
    queryParams.userId = localStorage.getItem('userId') || 'test-user'
    
    const response = await getAttendanceRecords(queryParams)
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载考勤记录失败:', error)
    ElMessage.error('加载考勤记录失败')
  } finally {
    loading.value = false
  }
}

const viewRecordDetail = (record: any) => {
  currentRecord.value = record
  detailVisible.value = true
}

const editRecord = (record: any) => {
  currentRecord.value = record
  editForm.value = {
    id: record.id,
    checkinTime: record.checkinTime ? formatDateTime(record.checkinTime) : null,
    checkoutTime: record.checkoutTime ? formatDateTime(record.checkoutTime) : null,
    status: record.status,
    abnormal: record.abnormal,
    abnormalReason: record.abnormalReason,
    remark: record.remark
  }
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editFormRef.value) return
  
  try {
    await editFormRef.value.validate()
    editLoading.value = true
    
    const response = await updateAttendanceRecord(editForm.value)
    
    if (response.code === 200) {
      ElMessage.success('修改成功')
      editVisible.value = false
      loadTableData() // Refresh data
    } else {
      ElMessage.error(response.message || '修改失败')
    }
  } catch (error) {
    console.error('修改失败:', error)
  } finally {
    editLoading.value = false
  }
}

const exportRecords = async () => {
  try {
    exportLoading.value = true
    
    // Prepare export parameters
    const exportParams = {
      ...queryParams,
      exportFormat: 'excel'
    }
    
    const response = await exportAttendanceRecords(exportParams)
    
    if (response.code === 200) {
      // Create download link
      const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `考勤记录_${format(new Date(), 'yyyyMMddHHmmss')}.xlsx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const hasPermission = (permission: string) => {
  // Mock permission check
  const userRole = localStorage.getItem('userRole') || 'user'
  return userRole === 'admin'
}

// Lifecycle
onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.records-container {
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

.header-actions {
  display: flex;
  gap: 12px;
}

.search-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  height: 100%;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
  font-size: 28px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.data-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.time-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.method-tag {
  align-self: flex-start;
}

.hours-text {
  color: #409eff;
  font-weight: 500;
}

.late-text {
  color: #e6a23c;
  font-weight: 500;
}

.early-text {
  color: #f56c6c;
  font-weight: 500;
}

.overtime-text {
  color: #409eff;
  font-weight: 500;
}

.empty-text {
  color: #c0c4cc;
  font-style: italic;
}

.normal-text {
  color: #67c23a;
}

.address-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.address-text {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  max-height: 400px;
  overflow-y: auto;
  padding-right: 10px;
}

/* Custom scrollbar */
.detail-content::-webkit-scrollbar {
  width: 6px;
}

.detail-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.detail-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.detail-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .records-container {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .search-card .el-form {
    display: grid;
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .stat-card {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .stat-icon {
    margin-right: 0;
  }
}
</style>