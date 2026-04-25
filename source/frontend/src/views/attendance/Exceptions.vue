<template>
  <div class="exceptions-container">
    <div class="header">
      <h2 class="title">考勤异常管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          申请异常
        </el-button>
      </div>
    </div>

    <!-- Filter Tabs -->
    <div class="filter-tabs">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="待审批" name="pending" />
        <el-tab-pane label="已批准" name="approved" />
        <el-tab-pane label="已驳回" name="rejected" />
        <el-tab-pane label="我的申请" name="my" />
      </el-tabs>
    </div>

    <!-- Search Form -->
    <div class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="申请人">
          <el-input
            v-model="searchForm.userName"
            placeholder="请输入申请人姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="异常类型">
          <el-select v-model="searchForm.type" placeholder="请选择异常类型" clearable>
            <el-option label="补卡" :value="0" />
            <el-option label="请假" :value="1" />
            <el-option label="出差" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="申请日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
          <el-button type="warning" @click="exportData" :loading="exportLoading">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        
        <el-table-column prop="userName" label="申请人" width="100" />
        
        <el-table-column label="异常类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)" size="small">
              {{ getTypeText(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="申请时间" width="180">
          <template #default="{ row }">
            <div>
              <div>{{ formatDate(row.startTime) }}</div>
              <div class="text-muted">至 {{ formatDate(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="reason" label="申请事由" min-width="200">
          <template #default="{ row }">
            <div class="reason-text">{{ row.reason || '--' }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="附件" width="80">
          <template #default="{ row }">
            <el-button
              v-if="hasAttachments(row.attachments)"
              type="primary"
              link
              @click="viewAttachments(row)"
            >
              查看
            </el-button>
            <span v-else class="text-muted">无</span>
          </template>
        </el-table-column>
        
        <el-table-column label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="审批信息" width="150">
          <template #default="{ row }">
            <div v-if="row.approverName">
              <div>{{ row.approverName }}</div>
              <div class="text-muted">{{ formatDate(row.approvalTime) }}</div>
              <div class="text-muted small" v-if="row.approvalComment">
                {{ truncateText(row.approvalComment, 15) }}
              </div>
            </div>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="remark" label="备注" min-width="120" />
        
        <el-table-column label="创建时间" width="140">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0 && hasPermission('approve')">
              <el-button type="primary" link @click="handleApprove(row, true)">
                批准
              </el-button>
              <el-button type="danger" link @click="handleApprove(row, false)">
                驳回
              </el-button>
            </template>
            
            <template v-if="row.status === 0 && row.userId === currentUserId">
              <el-button type="primary" link @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button type="danger" link @click="handleCancel(row)">
                取消
              </el-button>
            </template>
            
            <template v-if="row.status !== 0">
              <el-button type="primary" link @click="viewDetails(row)">
                查看
              </el-button>
              <el-button 
                v-if="hasPermission('delete')"
                type="danger" 
                link 
                @click="handleDelete(row)"
              >
                删除
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- Batch Actions -->
      <div class="batch-actions" v-if="selectedRows.length > 0">
        <el-space>
          <span>已选择 {{ selectedRows.length }} 项</span>
          <el-button v-if="hasPermission('approve')" type="primary" @click="batchApprove(true)">
            批量批准
          </el-button>
          <el-button v-if="hasPermission('approve')" type="danger" @click="batchApprove(false)">
            批量驳回
          </el-button>
          <el-button v-if="hasPermission('delete')" @click="batchDelete">
            批量删除
          </el-button>
          <el-button link @click="clearSelection">取消选择</el-button>
        </el-space>
      </div>

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

    <!-- Exception Dialog -->
    <el-dialog
      v-model="exceptionDialogVisible"
      :title="exceptionDialogTitle"
      width="700px"
    >
      <el-form
        ref="exceptionFormRef"
        :model="exceptionForm"
        :rules="exceptionRules"
        label-width="100px"
      >
        <el-form-item label="异常类型" prop="type">
          <el-select v-model="exceptionForm.type" placeholder="请选择异常类型">
            <el-option label="补卡" :value="0" />
            <el-option label="请假" :value="1" />
            <el-option label="出差" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>

        <el-form-item label="时间范围" prop="timeRange" required>
          <el-date-picker
            v-model="exceptionForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :default-time="defaultTime"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="申请事由" prop="reason">
          <el-input
            v-model="exceptionForm.reason"
            type="textarea"
            :rows="3"
            placeholder="请输入申请事由"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="附件">
          <el-upload
            class="upload-attachments"
            action="/api/upload"
            multiple
            :file-list="fileList"
            :on-success="handleUploadSuccess"
            :on-remove="handleUploadRemove"
            :on-error="handleUploadError"
            :limit="5"
            :on-exceed="handleExceed"
          >
            <el-button type="primary">
              <el-icon><Upload /></el-icon>
              上传附件
            </el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传图片、文档等文件，最多5个，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="exceptionForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exceptionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExceptionForm" :loading="exceptionLoading">
            提交申请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Approve Dialog -->
    <el-dialog
      v-model="approveDialogVisible"
      :title="approveDialogTitle"
      width="500px"
    >
      <el-form
        ref="approveFormRef"
        :model="approveForm"
        :rules="approveRules"
        label-width="80px"
      >
        <el-form-item label="审批意见" prop="comment">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitApproveForm" :loading="approveLoading">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Attachments Dialog -->
    <el-dialog
      v-model="attachmentsDialogVisible"
      title="附件列表"
      width="600px"
    >
      <div class="attachments-list" v-if="currentAttachments && currentAttachments.length">
        <div v-for="(attachment, index) in currentAttachments" :key="index" class="attachment-item">
          <el-row :gutter="10" align="middle">
            <el-col :span="2">
              <el-icon :size="20">
                <Document />
              </el-icon>
            </el-col>
            <el-col :span="16">
              <div class="attachment-name">{{ attachment.name }}</div>
              <div class="attachment-size">{{ formatFileSize(attachment.size) }}</div>
            </el-col>
            <el-col :span="6">
              <el-button type="primary" link @click="downloadAttachment(attachment)">
                下载
              </el-button>
              <el-button type="primary" link @click="previewAttachment(attachment)">
                预览
              </el-button>
            </el-col>
          </el-row>
        </div>
      </div>
      <div v-else class="empty-attachments">
        暂无附件
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Plus, Download, Upload, Document } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { 
  getExceptions,
  applyException,
  getException,
  approveException,
  deleteException,
  exportAttendanceRecords
} from '@/api/attendance'

// Reactive data
const loading = ref(false)
const exportLoading = ref(false)
const exceptionLoading = ref(false)
const approveLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const exceptionDialogVisible = ref(false)
const approveDialogVisible = ref(false)
const attachmentsDialogVisible = ref(false)
const exceptionFormRef = ref<FormInstance>()
const approveFormRef = ref<FormInstance>()
const activeTab = ref('all')
const dateRange = ref<string[]>([])
const fileList = ref<any[]>([])
const currentAttachments = ref<any[]>([])
const selectedRows = ref<any[]>([])

// Current user
const currentUserId = 'user1' // Mock user id

// Search form
const searchForm = reactive({
  pageNum: 1,
  pageSize: 20,
  userName: '',
  type: null as number | null,
  startDate: '',
  endDate: '',
  status: null as number | null,
  userId: null as string | null
})

// Exception form
const exceptionForm = reactive({
  id: null as number | null,
  type: 0,
  timeRange: [] as string[],
  startTime: '',
  endTime: '',
  reason: '',
  attachments: '[]',
  remark: ''
})

// Approve form
const approveForm = reactive({
  exceptionId: null as number | null,
  approved: true,
  comment: ''
})

// Default time for date picker
const defaultTime = ref([
  new Date(2000, 1, 1, 9, 0, 0),
  new Date(2000, 1, 1, 18, 0, 0)
])

// Current exception for approval
const currentException = ref<any>(null)

// Computed
const exceptionDialogTitle = computed(() => {
  return exceptionForm.id ? '编辑异常申请' : '申请异常'
})

const approveDialogTitle = computed(() => {
  return approveForm.approved ? '批准申请' : '驳回申请'
})

// Rules
const exceptionRules = {
  type: [
    { required: true, message: '请选择异常类型', trigger: 'change' }
  ],
  timeRange: [
    { required: true, message: '请选择时间范围', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' },
    { min: 5, message: '申请事由至少5个字符', trigger: 'blur' }
  ]
}

const approveRules = {
  comment: [
    { required: true, message: '请输入审批意见', trigger: 'blur' }
  ]
}

// Methods
const formatDateTime = (datetime: string) => {
  if (!datetime) return ''
  return format(new Date(datetime), 'yyyy-MM-dd HH:mm')
}

const formatDate = (datetime: string) => {
  if (!datetime) return ''
  return format(new Date(datetime), 'MM-dd HH:mm')
}

const getTypeText = (type: number) => {
  const map: Record<number, string> = {
    0: '补卡',
    1: '请假',
    2: '出差',
    3: '其他'
  }
  return map[type] || '未知'
}

const getTypeTagType = (type: number) => {
  const map: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'default'
  }
  return map[type] || 'info'
}

const getStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待审批',
    1: '已批准',
    2: '已驳回',
    3: '已取消'
  }
  return map[status] || '未知'
}

const getStatusTagType = (status: number) => {
  const map: Record<number, string> = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[status] || 'info'
}

const truncateText = (text: string, maxLength: number) => {
  if (!text) return ''
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text
}

const hasAttachments = (attachmentsJson: string) => {
  try {
    const attachments = JSON.parse(attachmentsJson || '[]')
    return attachments.length > 0
  } catch (error) {
    return false
  }
}

const hasPermission = (permission: string) => {
  // Mock permission check
  const permissions = ['view', 'edit', 'delete', 'approve']
  return permissions.includes(permission)
}

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const handleTabClick = (tab: any) => {
  searchForm.pageNum = 1
  
  switch (tab.paneName) {
    case 'pending':
      searchForm.status = 0
      searchForm.userId = null
      break
    case 'approved':
      searchForm.status = 1
      searchForm.userId = null
      break
    case 'rejected':
      searchForm.status = 2
      searchForm.userId = null
      break
    case 'my':
      searchForm.status = null
      searchForm.userId = currentUserId
      break
    default:
      searchForm.status = null
      searchForm.userId = null
  }
  
  loadTableData()
}

const handleSearch = () => {
  searchForm.pageNum = 1
  
  if (dateRange.value && dateRange.value.length === 2) {
    searchForm.startDate = dateRange.value[0]
    searchForm.endDate = dateRange.value[1]
  } else {
    searchForm.startDate = ''
    searchForm.endDate = ''
  }
  
  loadTableData()
}

const resetSearch = () => {
  searchForm.pageNum = 1
  searchForm.userName = ''
  searchForm.type = null
  dateRange.value = []
  searchForm.startDate = ''
  searchForm.endDate = ''
  loadTableData()
}

const exportData = async () => {
  try {
    exportLoading.value = true
    
    const params = { ...searchForm }
    delete params.pageNum
    delete params.pageSize
    
    const response = await exportAttendanceRecords(params)
    
    // Create download link
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `考勤异常记录_${format(new Date(), 'yyyyMMddHHmmss')}.xlsx`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
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
    
    const response = await getExceptions(searchForm)
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载异常记录失败:', error)
    ElMessage.error('加载异常记录失败')
  } finally {
    loading.value = false
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const clearSelection = () => {
  selectedRows.value = []
}

const handleCreate = () => {
  // Reset form
  Object.assign(exceptionForm, {
    id: null,
    type: 0,
    timeRange: [],
    startTime: '',
    endTime: '',
    reason: '',
    attachments: '[]',
    remark: ''
  })
  
  fileList.value = []
  
  exceptionDialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(exceptionForm, {
    id: row.id,
    type: row.type,
    timeRange: [row.startTime, row.endTime],
    startTime: row.startTime,
    endTime: row.endTime,
    reason: row.reason,
    attachments: row.attachments || '[]',
    remark: row.remark || ''
  })
  
  // Parse attachments
  try {
    const attachments = JSON.parse(row.attachments || '[]')
    fileList.value = attachments.map((att: any, index: number) => ({
      name: att.name || `附件${index + 1}`,
      url: att.url
    }))
  } catch (error) {
    fileList.value = []
  }
  
  exceptionDialogVisible.value = true
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定取消该申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: Call API to cancel exception
    ElMessage.success('取消成功')
    loadTableData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除该记录吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteException(row.id)
    
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

const handleApprove = (row: any, approved: boolean) => {
  currentException.value = row
  approveForm.exceptionId = row.id
  approveForm.approved = approved
  approveForm.comment = ''
  approveDialogVisible.value = true
}

const viewDetails = (row: any) => {
  // TODO: Show details dialog
  ElMessage.info('查看详情功能开发中...')
}

const viewAttachments = (row: any) => {
  try {
    const attachments = JSON.parse(row.attachments || '[]')
    currentAttachments.value = attachments
    attachmentsDialogVisible.value = true
  } catch (error) {
    ElMessage.error('附件数据格式错误')
  }
}

const downloadAttachment = (attachment: any) => {
  if (attachment.url) {
    window.open(attachment.url, '_blank')
  } else {
    ElMessage.warning('附件链接无效')
  }
}

const previewAttachment = (attachment: any) => {
  if (attachment.url) {
    window.open(attachment.url, '_blank')
  } else {
    ElMessage.warning('附件链接无效')
  }
}

const batchApprove = (approved: boolean) => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要处理的记录')
    return
  }
  
  const ids = selectedRows.value.map(row => row.id)
  ElMessageBox.confirm(
    `确定批量${approved ? '批准' : '驳回'}选中的 ${ids.length} 条申请吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // TODO: Call batch approve API
      ElMessage.success(`批量${approved ? '批准' : '驳回'}成功`)
      clearSelection()
      loadTableData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  })
}

const batchDelete = () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的记录')
    return
  }
  
  const ids = selectedRows.value.map(row => row.id)
  ElMessageBox.confirm(
    `确定批量删除选中的 ${ids.length} 条记录吗？删除后无法恢复。`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    }
  ).then(async () => {
    try {
      // TODO: Call batch delete API
      ElMessage.success('批量删除成功')
      clearSelection()
      loadTableData()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// Upload handlers
const handleUploadSuccess = (response: any, file: any) => {
  const fileInfo = {
    name: file.name,
    url: response.data?.url || file.url,
    size: file.size
  }
  
  try {
    const attachments = JSON.parse(exceptionForm.attachments || '[]')
    attachments.push(fileInfo)
    exceptionForm.attachments = JSON.stringify(attachments)
  } catch (error) {
    console.error('处理附件失败:', error)
  }
  
  ElMessage.success(`${file.name} 上传成功`)
}

const handleUploadRemove = (file: any) => {
  try {
    const attachments = JSON.parse(exceptionForm.attachments || '[]')
    const index = attachments.findIndex((att: any) => att.name === file.name)
    if (index > -1) {
      attachments.splice(index, 1)
      exceptionForm.attachments = JSON.stringify(attachments)
    }
  } catch (error) {
    console.error('删除附件失败:', error)
  }
}

const handleUploadError = (error: any, file: any) => {
  console.error('上传失败:', error)
  ElMessage.error(`${file.name} 上传失败`)
}

const handleExceed = (files: any, fileList: any) => {
  ElMessage.warning(`最多只能上传 ${fileList.length} 个文件`)
}

const submitExceptionForm = async () => {
  if (!exceptionFormRef.value) return
  
  try {
    await exceptionFormRef.value.validate()
    
    // Set time from range
    if (exceptionForm.timeRange && exceptionForm.timeRange.length === 2) {
      exceptionForm.startTime = exceptionForm.timeRange[0]
      exceptionForm.endTime = exceptionForm.timeRange[1]
    }
    
    exceptionLoading.value = true
    
    let response
    if (exceptionForm.id) {
      // TODO: Update exception API
      ElMessage.success('更新成功（待实现）')
    } else {
      response = await applyException(exceptionForm)
      
      if (response.code === 200) {
        ElMessage.success('申请提交成功')
        exceptionDialogVisible.value = false
        loadTableData()
      } else {
        ElMessage.error(response.message || '提交失败')
      }
    }
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    exceptionLoading.value = false
  }
}

const submitApproveForm = async () => {
  if (!approveFormRef.value) return
  
  try {
    await approveFormRef.value.validate()
    
    approveLoading.value = true
    
    if (!approveForm.exceptionId) {
      ElMessage.error('申请ID无效')
      return
    }
    
    const response = await approveException(
      approveForm.exceptionId,
      { approved: approveForm.approved, comment: approveForm.comment }
    )
    
    if (response.code === 200) {
      ElMessage.success(approveForm.approved ? '批准成功' : '驳回成功')
      approveDialogVisible.value = false
      loadTableData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败:', error)
  } finally {
    approveLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.exceptions-container {
  padding: 20px;
  max-width: 1800px;
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

.filter-tabs {
  margin-bottom: 20px;
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

.batch-actions {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.reason-text {
  word-break: break-word;
  line-height: 1.5;
}

.text-muted {
  color: #909399;
  font-style: italic;
}

.small {
  font-size: 12px;
}

.upload-attachments {
  width: 100%;
}

.attachments-list {
  max-height: 400px;
  overflow-y: auto;
}

.attachment-item {
  padding: 12px;
  margin-bottom: 8px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
}

.attachment-item:last-child {
  margin-bottom: 0;
}

.attachment-name {
  font-weight: 500;
  margin-bottom: 4px;
  word-break: break-all;
}

.attachment-size {
  font-size: 12px;
  color: #909399;
}

.empty-attachments {
  text-align: center;
  padding: 40px;
  color: #909399;
}

@media (max-width: 768px) {
  .exceptions-container {
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
  
  .attachment-item .el-row {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .attachment-item .el-col {
    width: 100%;
  }
}
</style>