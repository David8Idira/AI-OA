<template>
  <div class="office-request-container">
    <div class="header">
      <div class="title">办公用品申请</div>
      <div class="actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交申请</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleSaveDraft">保存草稿</el-button>
      </div>
    </div>
    
    <div class="content">
      <el-card class="form-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="申请信息" name="info">
            <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="申请部门" prop="departmentId">
                    <el-select
                      v-model="formData.departmentId"
                      placeholder="请选择申请部门"
                      style="width: 100%"
                      @change="handleDepartmentChange"
                    >
                      <el-option
                        v-for="dept in departments"
                        :key="dept.id"
                        :label="dept.name"
                        :value="dept.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="申请人" prop="applicantId">
                    <el-select
                      v-model="formData.applicantId"
                      placeholder="请选择申请人"
                      filterable
                      style="width: 100%"
                    >
                      <el-option
                        v-for="user in departmentUsers"
                        :key="user.id"
                        :label="user.name"
                        :value="user.id"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="申请日期" prop="applyDate">
                <el-date-picker
                  v-model="formData.applyDate"
                  type="date"
                  placeholder="选择申请日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              
              <el-form-item label="期望领取日期" prop="expectedClaimDate">
                <el-date-picker
                  v-model="formData.expectedClaimDate"
                  type="date"
                  placeholder="选择期望领取日期"
                  style="width: 100%"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              
              <el-form-item label="申请事由" prop="reason">
                <el-input
                  v-model="formData.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入申请事由"
                />
              </el-form-item>
              
              <el-form-item label="紧急程度" prop="urgency">
                <el-radio-group v-model="formData.urgency">
                  <el-radio label="normal">普通</el-radio>
                  <el-radio label="urgent">紧急</el-radio>
                  <el-radio label="very_urgent">非常紧急</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="申请物品" name="items">
            <div class="items-section">
              <div class="section-header">
                <div class="section-title">申请物品清单</div>
                <el-button type="primary" @click="handleAddItem">添加物品</el-button>
              </div>
              
              <div class="items-table">
                <el-table :data="formData.items" border style="width: 100%">
                  <el-table-column prop="itemName" label="物品名称" width="150">
                    <template #default="scope">
                      <el-input
                        v-model="scope.row.itemName"
                        placeholder="请输入物品名称"
                        @change="handleItemChange(scope.row)"
                      />
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="specification" label="规格型号" width="120">
                    <template #default="scope">
                      <el-input
                        v-model="scope.row.specification"
                        placeholder="请输入规格"
                      />
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="unit" label="单位" width="80">
                    <template #default="scope">
                      <el-select v-model="scope.row.unit" placeholder="单位" style="width: 100%">
                        <el-option label="个" value="个" />
                        <el-option label="套" value="套" />
                        <el-option label="台" value="台" />
                        <el-option label="盒" value="盒" />
                        <el-option label="包" value="包" />
                        <el-option label="件" value="件" />
                        <el-option label="其他" value="其他" />
                      </el-select>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="quantity" label="数量" width="100">
                    <template #default="scope">
                      <el-input-number
                        v-model="scope.row.quantity"
                        :min="1"
                        style="width: 100%"
                      />
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="estimatedPrice" label="预估单价" width="120">
                    <template #default="scope">
                      <el-input-number
                        v-model="scope.row.estimatedPrice"
                        :min="0"
                        :precision="2"
                        style="width: 100%"
                      >
                        <template #prefix>¥</template>
                      </el-input-number>
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="totalPrice" label="预估总价" width="120">
                    <template #default="scope">
                      ¥{{ (scope.row.quantity * scope.row.estimatedPrice).toFixed(2) }}
                    </template>
                  </el-table-column>
                  
                  <el-table-column prop="purpose" label="用途说明" width="150">
                    <template #default="scope">
                      <el-input
                        v-model="scope.row.purpose"
                        placeholder="请输入用途"
                      />
                    </template>
                  </el-table-column>
                  
                  <el-table-column label="操作" width="80" fixed="right">
                    <template #default="scope">
                      <el-button
                        type="danger"
                        link
                        @click="handleRemoveItem(scope.$index)"
                      >
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <div class="items-summary">
                <div class="summary-item">
                  <span class="label">物品数量：</span>
                  <span class="value">{{ formData.items.length }} 种</span>
                </div>
                <div class="summary-item">
                  <span class="label">总数量：</span>
                  <span class="value">{{ totalQuantity }} 个</span>
                </div>
                <div class="summary-item">
                  <span class="label">预估总价：</span>
                  <span class="value">¥{{ totalEstimatedPrice.toFixed(2) }}</span>
                </div>
              </div>
              
              <div class="items-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>请准确填写物品信息，方便采购和审批</span>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="审批信息" name="approval">
            <div class="approval-section">
              <el-form :model="formData" :rules="rules" ref="approvalFormRef" label-width="120px">
                <el-form-item label="审批人" prop="approverId">
                  <el-select
                    v-model="formData.approverId"
                    placeholder="请选择审批人"
                    filterable
                    style="width: 100%"
                  >
                    <el-option
                      v-for="approver in approvers"
                      :key="approver.id"
                      :label="approver.name"
                      :value="approver.id"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="抄送人" prop="ccUsers">
                  <el-select
                    v-model="formData.ccUsers"
                    multiple
                    placeholder="请选择抄送人（可多选）"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="user in allUsers"
                      :key="user.id"
                      :label="user.name"
                      :value="user.id"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="附件" prop="attachments">
                  <el-upload
                    action="/api/upload"
                    :on-success="handleUploadSuccess"
                    :on-remove="handleRemove"
                    :file-list="fileList"
                    list-type="picture-card"
                    :limit="10"
                  >
                    <el-icon><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
                
                <el-form-item label="备注" prop="remark">
                  <el-input
                    v-model="formData.remark"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入备注信息"
                  />
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      
      <!-- 物品选择对话框 -->
      <el-dialog
        v-model="itemDialogVisible"
        title="选择办公用品"
        width="900px"
      >
        <ItemSelector
          v-if="itemDialogVisible"
          @select="handleItemSelect"
          @cancel="itemDialogVisible = false"
        />
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import { requestOfficeSupply } from '@/api/asset'
import ItemSelector from './components/ItemSelector.vue'

const formRef = ref()
const approvalFormRef = ref()
const submitting = ref(false)
const activeTab = ref('info')
const itemDialogVisible = ref(false)
const departments = ref([])
const departmentUsers = ref([])
const approvers = ref([])
const allUsers = ref([])
const fileList = ref([])

const formData = reactive({
  departmentId: '',
  applicantId: '',
  applyDate: new Date().toISOString().split('T')[0],
  expectedClaimDate: '',
  reason: '',
  urgency: 'normal',
  items: [
    {
      itemId: '',
      itemName: '',
      specification: '',
      unit: '个',
      quantity: 1,
      estimatedPrice: 0,
      purpose: ''
    }
  ],
  approverId: '',
  ccUsers: [],
  attachments: [],
  remark: ''
})

const rules = {
  departmentId: [
    { required: true, message: '请选择申请部门', trigger: 'change' }
  ],
  applicantId: [
    { required: true, message: '请选择申请人', trigger: 'change' }
  ],
  applyDate: [
    { required: true, message: '请选择申请日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' }
  ],
  urgency: [
    { required: true, message: '请选择紧急程度', trigger: 'change' }
  ],
  approverId: [
    { required: true, message: '请选择审批人', trigger: 'change' }
  ]
}

// 计算总数量
const totalQuantity = computed(() => {
  return formData.items.reduce((sum, item) => sum + (item.quantity || 0), 0)
})

// 计算预估总价
const totalEstimatedPrice = computed(() => {
  return formData.items.reduce((sum, item) => {
    return sum + (item.quantity || 0) * (item.estimatedPrice || 0)
  }, 0)
})

// 初始化数据
onMounted(async () => {
  await fetchDepartments()
  await fetchApprovers()
  await fetchAllUsers()
})

// 获取部门列表
const fetchDepartments = async () => {
  try {
    // 模拟数据
    departments.value = [
      { id: '1', name: '技术部' },
      { id: '2', name: '市场部' },
      { id: '3', name: '人事部' },
      { id: '4', name: '财务部' },
      { id: '5', name: '行政部' }
    ]
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

// 获取审批人列表
const fetchApprovers = async () => {
  try {
    // 模拟数据
    approvers.value = [
      { id: '1', name: '部门经理-张三' },
      { id: '2', name: '行政主管-李四' },
      { id: '3', name: '财务审批-王五' }
    ]
  } catch (error) {
    console.error('获取审批人列表失败:', error)
  }
}

// 获取所有用户
const fetchAllUsers = async () => {
  try {
    // 模拟数据
    allUsers.value = [
      { id: '1', name: '张三' },
      { id: '2', name: '李四' },
      { id: '3', name: '王五' },
      { id: '4', name: '赵六' },
      { id: '5', name: '钱七' }
    ]
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 部门选择变化
const handleDepartmentChange = (departmentId) => {
  // 根据部门加载用户
  departmentUsers.value = [
    { id: '1', name: '张三' },
    { id: '2', name: '李四' },
    { id: '3', name: '王五' }
  ]
  formData.applicantId = ''
}

// 添加物品
const handleAddItem = () => {
  formData.items.push({
    itemId: '',
    itemName: '',
    specification: '',
    unit: '个',
    quantity: 1,
    estimatedPrice: 0,
    purpose: ''
  })
}

// 删除物品
const handleRemoveItem = (index) => {
  if (formData.items.length > 1) {
    formData.items.splice(index, 1)
  } else {
    ElMessage.warning('至少需要保留一个物品')
  }
}

// 物品变化
const handleItemChange = (item) => {
  // 这里可以添加自动填充规格、单价等逻辑
  if (item.itemName && !item.specification) {
    // 根据物品名称自动填充规格（示例）
    const specMap = {
      'A4打印纸': '70g',
      '签字笔': '黑色0.5mm',
      '笔记本': 'A5 80页',
      '文件夹': 'A4 蓝色'
    }
    item.specification = specMap[item.itemName] || ''
  }
}

// 物品选择
const handleItemSelect = (selectedItems) => {
  selectedItems.forEach(item => {
    formData.items.push({
      itemId: item.id,
      itemName: item.name,
      specification: item.specification || '',
      unit: item.unit || '个',
      quantity: 1,
      estimatedPrice: item.price || 0,
      purpose: ''
    })
  })
  itemDialogVisible.value = false
}

// 文件上传成功
const handleUploadSuccess = (response, file, fileList) => {
  formData.attachments.push(response.data.url)
}

// 文件移除
const handleRemove = (file, fileList) => {
  const index = formData.attachments.findIndex(url => url === file.response?.data?.url)
  if (index > -1) {
    formData.attachments.splice(index, 1)
  }
}

// 提交申请
const handleSubmit = async () => {
  try {
    // 验证基本信息
    await formRef.value.validate()
    
    // 验证物品信息
    if (formData.items.length === 0) {
      ElMessage.warning('请至少添加一个物品')
      activeTab.value = 'items'
      return
    }
    
    for (const item of formData.items) {
      if (!item.itemName?.trim()) {
        ElMessage.warning('请填写物品名称')
        activeTab.value = 'items'
        return
      }
      if (!item.quantity || item.quantity <= 0) {
        ElMessage.warning('请填写正确的数量')
        activeTab.value = 'items'
        return
      }
    }
    
    // 验证审批信息
    await approvalFormRef.value.validate()
    
    submitting.value = true
    
    const submitData = {
      ...formData,
      items: formData.items.map(item => ({
        ...item,
        totalPrice: item.quantity * item.estimatedPrice
      })),
      totalQuantity,
      totalEstimatedPrice,
      attachments: formData.attachments.join(','),
      ccUsers: formData.ccUsers.join(',')
    }
    
    await requestOfficeSupply(submitData)
    ElMessage.success('办公用品申请提交成功')
    handleReset()
  } catch (error) {
    console.error('提交失败:', error)
    if (error.message?.includes('validation')) {
      ElMessage.error('请检查表单填写是否完整')
    } else {
      ElMessage.error('提交失败，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}

// 重置
const handleReset = () => {
  formRef.value?.resetFields()
  approvalFormRef.value?.resetFields()
  formData.items = [
    {
      itemId: '',
      itemName: '',
      specification: '',
      unit: '个',
      quantity: 1,
      estimatedPrice: 0,
      purpose: ''
    }
  ]
  formData.attachments = []
  formData.ccUsers = []
  fileList.value = []
  formData.applyDate = new Date().toISOString().split('T')[0]
  formData.urgency = 'normal'
  activeTab.value = 'info'
}

// 保存草稿
const handleSaveDraft = () => {
  try {
    const draftData = {
      ...formData,
      savedAt: new Date().toISOString()
    }
    localStorage.setItem('officeSupplyDraft', JSON.stringify(draftData))
    ElMessage.success('草稿保存成功')
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  }
}
</script>

<style scoped>
.office-request-container {
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

.content {
  min-height: 600px;
}

.form-card {
  width: 100%;
}

.items-section {
  padding: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.items-table {
  margin-bottom: 20px;
}

.items-summary {
  display: flex;
  justify-content: flex-end;
  gap: 30px;
  margin: 20px 0;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-item .label {
  color: #606266;
  font-size: 14px;
  margin-bottom: 5px;
}

.summary-item .value {
  color: #303133;
  font-size: 18px;
  font-weight: bold;
}

.items-hint {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 14px;
  padding: 10px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #d9ecff;
}

.items-hint .el-icon {
  margin-right: 8px;
}

.approval-section {
  padding: 20px 0;
}
</style>