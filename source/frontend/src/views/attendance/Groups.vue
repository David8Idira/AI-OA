<template>
  <div class="groups-container">
    <div class="header">
      <h2 class="title">考勤组管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建考勤组
        </el-button>
      </div>
    </div>

    <!-- Search Form -->
    <div class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="考勤组名称">
          <el-input
            v-model="searchForm.groupName"
            placeholder="请输入考勤组名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item label="规则">
          <el-select v-model="searchForm.ruleId" placeholder="请选择考勤规则" clearable filterable>
            <el-option
              v-for="rule in ruleList"
              :key="rule.id"
              :label="rule.ruleName"
              :value="rule.id"
            />
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
        <el-table-column prop="groupName" label="考勤组名称" width="200" />
        <el-table-column prop="groupCode" label="组编码" width="120" />
        
        <el-table-column label="考勤规则" width="150">
          <template #default="{ row }">
            <span v-if="row.ruleName">{{ row.ruleName }}</span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        
        <el-table-column label="排班类型" width="100">
          <template #default="{ row }">
            {{ getScheduleTypeText(row.scheduleType) }}
          </template>
        </el-table-column>
        
        <el-table-column label="允许远程打卡" width="110">
          <template #default="{ row }">
            <el-tag :type="row.allowRemote === 1 ? 'success' : 'info'" size="small">
              {{ row.allowRemote === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="最大远程距离" width="120">
          <template #default="{ row }">
            <span v-if="row.allowRemote === 1">{{ row.maxRemoteDistance || 1000 }}米</span>
            <span v-else class="text-muted">--</span>
          </template>
        </el-table-column>
        
        <el-table-column label="考勤地点">
          <template #default="{ row }">
            <el-tooltip 
              :content="getCheckinLocationsText(row.checkinLocations)" 
              placement="top"
            >
              <span class="locations-text">
                {{ getCheckinLocationsText(row.checkinLocations, 30) }}
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column label="成员数量" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.memberCount > 0" type="info" size="small">
              {{ row.memberCount }}人
            </el-tag>
            <span v-else class="text-muted">0人</span>
          </template>
        </el-table-column>
        
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
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
            <el-button type="primary" link @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="manageMembers(row)">
              成员
            </el-button>
            <el-button 
              type="primary" 
              link 
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button 
              type="danger" 
              link 
              @click="handleDelete(row)"
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

    <!-- Group Dialog -->
    <el-dialog
      v-model="groupDialogVisible"
      :title="groupDialogTitle"
      width="900px"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考勤组名称" prop="groupName">
              <el-input v-model="groupForm.groupName" placeholder="请输入考勤组名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组编码" prop="groupCode">
              <el-input v-model="groupForm.groupCode" placeholder="请输入组编码" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="考勤规则" prop="ruleId">
              <el-select 
                v-model="groupForm.ruleId" 
                placeholder="请选择考勤规则"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="rule in ruleList"
                  :key="rule.id"
                  :label="rule.ruleName"
                  :value="rule.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排班类型" prop="scheduleType">
              <el-select v-model="groupForm.scheduleType" placeholder="请选择排班类型">
                <el-option label="固定班制" :value="0" />
                <el-option label="轮班制" :value="1" />
                <el-option label="弹性班制" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="groupForm.scheduleType === 1">
          <el-col :span="24">
            <el-form-item label="排班数据" prop="scheduleData">
              <el-input
                v-model="groupForm.scheduleData"
                type="textarea"
                :rows="3"
                placeholder="请输入排班数据（JSON格式）"
                :autosize="{ minRows: 3, maxRows: 6 }"
              />
              <div class="form-help">
                格式示例：{"shiftA": {"name": "早班", "time": "08:00-17:00"}, "shiftB": {"name": "中班", "time": "13:00-22:00"}}
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="managerId">
              <el-select
                v-model="managerSelection"
                placeholder="请选择负责人"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="user in userList"
                  :key="user.id"
                  :label="user.name"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="允许远程打卡" prop="allowRemote">
          <el-switch
            v-model="groupForm.allowRemote"
            :active-value="1"
            :inactive-value="0"
            @change="handleAllowRemoteChange"
          />
        </el-form-item>

        <el-row :gutter="20" v-if="groupForm.allowRemote === 1">
          <el-col :span="12">
            <el-form-item label="最大远程距离" prop="maxRemoteDistance">
              <el-input-number
                v-model="groupForm.maxRemoteDistance"
                :min="0"
                :max="10000"
                placeholder="米"
              />
              <span class="form-unit">米</span>
              <div class="form-help">设置为0表示不限制距离</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="考勤地点" prop="checkinLocations">
          <div class="locations-container">
            <div v-for="(location, index) in locationList" :key="index" class="location-item">
              <el-row :gutter="10" align="middle">
                <el-col :span="8">
                  <el-input v-model="location.name" placeholder="地点名称" />
                </el-col>
                <el-col :span="8">
                  <el-input v-model="location.address" placeholder="详细地址" />
                </el-col>
                <el-col :span="4">
                  <el-input-number
                    v-model="location.radius"
                    :min="0"
                    :max="1000"
                    placeholder="半径"
                  />
                  <span class="form-unit">米</span>
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="danger"
                    link
                    @click="removeLocation(index)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-col>
              </el-row>
            </div>
            
            <el-button type="primary" link @click="addLocation">
              <el-icon><Plus /></el-icon>
              添加考勤地点
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="WiFi列表" prop="wifiList">
          <div class="wifi-container">
            <div v-for="(wifi, index) in wifiList" :key="index" class="wifi-item">
              <el-row :gutter="10" align="middle">
                <el-col :span="10">
                  <el-input v-model="wifi.name" placeholder="WiFi名称" />
                </el-col>
                <el-col :span="10">
                  <el-input v-model="wifi.mac" placeholder="MAC地址" />
                </el-col>
                <el-col :span="4">
                  <el-button
                    type="danger"
                    link
                    @click="removeWifi(index)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-col>
              </el-row>
            </div>
            
            <el-button type="primary" link @click="addWifi">
              <el-icon><Plus /></el-icon>
              添加WiFi
            </el-button>
          </div>
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

        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="groupForm.status"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="groupForm.remark"
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
          <el-button @click="groupDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitGroupForm" :loading="groupLoading">
            保存
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- Members Dialog -->
    <el-dialog
      v-model="membersDialogVisible"
      title="考勤组成员管理"
      width="800px"
    >
      <div class="members-dialog">
        <div class="dialog-header">
          <h3>{{ currentGroup?.groupName }} - 成员管理</h3>
          <el-button type="primary" size="small" @click="showAddMemberDialog">
            添加成员
          </el-button>
        </div>

        <el-table
          :data="groupMembers"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column prop="position" label="职位" width="120" />
          <el-table-column prop="department" label="部门" width="150" />
          <el-table-column prop="joinDate" label="加入时间" width="120" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '正常' : '已移除' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="danger" 
                link 
                @click="removeMember(row)"
                v-if="row.status === 1"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, FormInstance } from 'element-plus'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import { 
  getAttendanceGroups,
  createAttendanceGroup,
  updateAttendanceGroup,
  deleteAttendanceGroup 
} from '@/api/attendance'

// Reactive data
const loading = ref(false)
const groupLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const groupDialogVisible = ref(false)
const membersDialogVisible = ref(false)
const groupFormRef = ref<FormInstance>()

// Search form
const searchForm = reactive({
  pageNum: 1,
  pageSize: 20,
  groupName: '',
  status: null as number | null,
  ruleId: null as number | null
})

// Group form
const groupForm = reactive({
  id: null as number | null,
  groupName: '',
  groupCode: '',
  status: 1,
  ruleId: null as number | null,
  scheduleType: 0,
  scheduleData: '',
  managerId: '',
  managerName: '',
  deptIds: '[]',
  positionIds: '[]',
  userIds: '[]',
  checkinLocations: '[]',
  wifiList: '[]',
  allowRemote: 0,
  maxRemoteDistance: 1000,
  remark: ''
})

// Selections
const managerSelection = ref<string>('')
const deptSelection = ref<string[]>([])
const positionSelection = ref<string[]>([])
const locationList = ref<any[]>([
  { name: '公司总部', address: '上海市浦东新区', radius: 100 }
])
const wifiList = ref<any[]>([
  { name: 'Office-WiFi', mac: 'AA:BB:CC:DD:EE:FF' }
])

// Mock data
const ruleList = ref([
  { id: 1, ruleName: '标准考勤规则' },
  { id: 2, ruleName: '弹性考勤规则' },
  { id: 3, ruleName: '轮班考勤规则' }
])

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

const userList = ref([
  { id: 'user1', name: '张三' },
  { id: 'user2', name: '李四' },
  { id: 'user3', name: '王五' },
  { id: 'user4', name: '赵六' }
])

const currentGroup = ref<any>(null)
const groupMembers = ref<any[]>([
  { id: 1, name: '张三', position: '工程师', department: '技术部', joinDate: '2023-01-01', status: 1 },
  { id: 2, name: '李四', position: '设计师', department: '设计部', joinDate: '2023-02-01', status: 1 },
  { id: 3, name: '王五', position: '产品经理', department: '产品部', joinDate: '2023-03-01', status: 1 }
])

// Computed
const groupDialogTitle = computed(() => {
  return groupForm.id ? '编辑考勤组' : '新建考勤组'
})

// Rules
const groupRules = {
  groupName: [
    { required: true, message: '请输入考勤组名称', trigger: 'blur' }
  ],
  groupCode: [
    { required: true, message: '请输入组编码', trigger: 'blur' }
  ],
  ruleId: [
    { required: true, message: '请选择考勤规则', trigger: 'change' }
  ]
}

// Methods
const formatDateTime = (datetime: string) => {
  if (!datetime) return ''
  return format(new Date(datetime), 'yyyy-MM-dd HH:mm')
}

const getScheduleTypeText = (type: number) => {
  const map: Record<number, string> = {
    0: '固定班制',
    1: '轮班制',
    2: '弹性班制'
  }
  return map[type] || '未知'
}

const getCheckinLocationsText = (locationsJson: string, maxLength: number = 0) => {
  try {
    const locations = JSON.parse(locationsJson || '[]')
    if (!locations.length) return '未设置'
    
    const text = locations.map((loc: any) => loc.name || '未命名').join('、')
    if (maxLength > 0 && text.length > maxLength) {
      return text.substring(0, maxLength) + '...'
    }
    return text
  } catch (error) {
    return '格式错误'
  }
}

const handleSearch = () => {
  searchForm.pageNum = 1
  loadTableData()
}

const resetSearch = () => {
  searchForm.pageNum = 1
  searchForm.groupName = ''
  searchForm.status = null
  searchForm.ruleId = null
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
    
    const response = await getAttendanceGroups(searchForm)
    
    if (response.code === 200) {
      tableData.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载考勤组失败:', error)
    ElMessage.error('加载考勤组失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  // Reset form
  Object.assign(groupForm, {
    id: null,
    groupName: '',
    groupCode: '',
    status: 1,
    ruleId: null,
    scheduleType: 0,
    scheduleData: '',
    managerId: '',
    managerName: '',
    deptIds: '[]',
    positionIds: '[]',
    userIds: '[]',
    checkinLocations: '[]',
    wifiList: '[]',
    allowRemote: 0,
    maxRemoteDistance: 1000,
    remark: ''
  })
  
  // Reset selections
  managerSelection.value = ''
  deptSelection.value = []
  positionSelection.value = []
  locationList.value = [{ name: '公司总部', address: '上海市浦东新区', radius: 100 }]
  wifiList.value = [{ name: 'Office-WiFi', mac: 'AA:BB:CC:DD:EE:FF' }]
  
  groupDialogVisible.value = true
}

const handleEdit = (row: any) => {
  // Copy row data to form
  Object.assign(groupForm, row)
  
  // Set manager
  managerSelection.value = row.managerId || ''
  
  // Parse selections
  try {
    if (row.deptIds) {
      deptSelection.value = JSON.parse(row.deptIds)
    }
    if (row.positionIds) {
      positionSelection.value = JSON.parse(row.positionIds)
    }
    if (row.checkinLocations) {
      const locations = JSON.parse(row.checkinLocations)
      locationList.value = locations.length ? locations : [{ name: '', address: '', radius: 100 }]
    }
    if (row.wifiList) {
      const wifi = JSON.parse(row.wifiList)
      wifiList.value = wifi.length ? wifi : [{ name: '', mac: '' }]
    }
  } catch (error) {
    console.error('Parse JSON error:', error)
  }
  
  groupDialogVisible.value = true
}

const toggleStatus = async (row: any) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    const confirmMessage = newStatus === 1 ? '确定启用该考勤组吗？' : '确定停用该考勤组吗？'
    
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await updateAttendanceGroup(row.id, { status: newStatus })
    
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
    await ElMessageBox.confirm('确定删除该考勤组吗？删除后无法恢复。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const response = await deleteAttendanceGroup(row.id)
    
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

const manageMembers = (row: any) => {
  currentGroup.value = row
  membersDialogVisible.value = true
}

const showAddMemberDialog = () => {
  ElMessage.info('添加成员功能开发中...')
}

const removeMember = async (member: any) => {
  try {
    await ElMessageBox.confirm(`确定移除成员 ${member.name} 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // TODO: Call API to remove member
    ElMessage.success('移除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('移除失败')
    }
  }
}

const handleAllowRemoteChange = (value: number) => {
  if (value === 0) {
    groupForm.maxRemoteDistance = 1000
  }
}

const addLocation = () => {
  locationList.value.push({ name: '', address: '', radius: 100 })
}

const removeLocation = (index: number) => {
  if (locationList.value.length > 1) {
    locationList.value.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个考勤地点')
  }
}

const addWifi = () => {
  wifiList.value.push({ name: '', mac: '' })
}

const removeWifi = (index: number) => {
  if (wifiList.value.length > 1) {
    wifiList.value.splice(index, 1)
  } else {
    ElMessage.warning('至少保留一个WiFi')
  }
}

const submitGroupForm = async () => {
  if (!groupFormRef.value) return
  
  try {
    await groupFormRef.value.validate()
    
    // Prepare form data
    const formData = { ...groupForm }
    
    // Set manager info
    if (managerSelection.value) {
      formData.managerId = managerSelection.value
      const manager = userList.value.find(u => u.id === managerSelection.value)
      formData.managerName = manager ? manager.name : ''
    }
    
    // Convert selections to JSON strings
    formData.deptIds = JSON.stringify(deptSelection.value)
    formData.positionIds = JSON.stringify(positionSelection.value)
    formData.checkinLocations = JSON.stringify(locationList.value.filter(loc => loc.name && loc.address))
    formData.wifiList = JSON.stringify(wifiList.value.filter(wifi => wifi.name && wifi.mac))
    
    groupLoading.value = true
    
    let response
    if (formData.id) {
      response = await updateAttendanceGroup(formData.id, formData)
    } else {
      response = await createAttendanceGroup(formData)
    }
    
    if (response.code === 200) {
      ElMessage.success(formData.id ? '更新成功' : '创建成功')
      groupDialogVisible.value = false
      loadTableData()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    groupLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  loadTableData()
})
</script>

<style scoped>
.groups-container {
  padding: 20px;
  max-width: 1600px;
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

.form-help {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.locations-container,
.wifi-container {
  background: #f5f7fa;
  border-radius: 4px;
  padding: 16px;
  border: 1px solid #dcdfe6;
}

.location-item,
.wifi-item {
  margin-bottom: 12px;
  padding: 8px;
  background: white;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.location-item:last-child,
.wifi-item:last-child {
  margin-bottom: 16px;
}

.locations-text {
  cursor: pointer;
  color: #409eff;
}

.text-muted {
  color: #909399;
  font-style: italic;
}

.members-dialog .dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .groups-container {
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
  
  .location-item .el-row,
  .wifi-item .el-row {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .location-item .el-col,
  .wifi-item .el-col {
    width: 100%;
  }
}
</style>