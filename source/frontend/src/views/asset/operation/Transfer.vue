<template>
  <div class="transfer-container">
    <div class="header">
      <div class="title">资产调拨</div>
      <div class="actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交调拨</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>
    
    <div class="content">
      <el-card class="form-card">
        <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="调拨资产" prop="assetId">
                <el-select
                  v-model="formData.assetId"
                  placeholder="请选择要调拨的资产"
                  filterable
                  style="width: 100%"
                  @change="handleAssetChange"
                >
                  <el-option
                    v-for="asset in availableAssets"
                    :key="asset.id"
                    :label="`${asset.assetName} (${asset.assetCode})`"
                    :value="asset.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="调拨数量" prop="quantity">
                <el-input-number
                  v-model="formData.quantity"
                  :min="1"
                  :max="maxQuantity"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="原负责人" prop="sourceResponsiblePerson">
                <el-input
                  v-model="formData.sourceResponsiblePerson"
                  placeholder="自动填充"
                  disabled
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="原存放位置" prop="sourceLocation">
                <el-input
                  v-model="formData.sourceLocation"
                  placeholder="自动填充"
                  disabled
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="新负责人" prop="targetResponsiblePerson">
                <el-select
                  v-model="formData.targetResponsiblePerson"
                  placeholder="请选择新负责人"
                  filterable
                  style="width: 100%"
                >
                  <el-option
                    v-for="user in users"
                    :key="user.id"
                    :label="user.name"
                    :value="user.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="新存放位置" prop="targetLocation">
                <el-input
                  v-model="formData.targetLocation"
                  placeholder="请输入新存放位置"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="调拨部门" prop="targetDepartmentId">
            <el-select
              v-model="formData.targetDepartmentId"
              placeholder="请选择调拨目标部门"
              style="width: 100%"
            >
              <el-option
                v-for="dept in departments"
                :key="dept.id"
                :label="dept.name"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="调拨日期" prop="transferDate">
            <el-date-picker
              v-model="formData.transferDate"
              type="date"
              placeholder="选择调拨日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="调拨事由" prop="reason">
            <el-input
              v-model="formData.reason"
              type="textarea"
              :rows="3"
              placeholder="请输入调拨事由"
            />
          </el-form-item>
          
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注信息"
            />
          </el-form-item>
          
          <el-form-item label="附件" prop="attachments">
            <el-upload
              action="/api/upload"
              :on-success="handleUploadSuccess"
              :on-remove="handleRemove"
              :file-list="fileList"
              list-type="picture-card"
              :limit="5"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 资产信息卡片 -->
      <el-card class="info-card" v-if="selectedAsset">
        <template #header>
          <div class="card-header">
            <span>资产信息</span>
          </div>
        </template>
        <div class="asset-info">
          <div class="info-item">
            <span class="label">资产名称：</span>
            <span class="value">{{ selectedAsset.assetName }}</span>
          </div>
          <div class="info-item">
            <span class="label">资产编码：</span>
            <span class="value">{{ selectedAsset.assetCode }}</span>
          </div>
          <div class="info-item">
            <span class="label">型号规格：</span>
            <span class="value">{{ selectedAsset.model || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前库存：</span>
            <span class="value">{{ selectedAsset.currentQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">原负责人：</span>
            <span class="value">{{ selectedAsset.responsiblePerson || '无' }}</span>
          </div>
          <div class="info-item">
            <span class="label">原存放位置：</span>
            <span class="value">{{ selectedAsset.location || '无' }}</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAssetList } from '@/api/asset'
import { transferAsset } from '@/api/asset'

const formRef = ref()
const submitting = ref(false)
const availableAssets = ref([])
const users = ref([])
const departments = ref([])
const selectedAsset = ref(null)
const fileList = ref([])
const maxQuantity = ref(0)

const formData = reactive({
  assetId: '',
  quantity: 1,
  sourceResponsiblePerson: '',
  sourceLocation: '',
  targetResponsiblePerson: '',
  targetLocation: '',
  targetDepartmentId: '',
  transferDate: '',
  reason: '',
  remark: '',
  attachments: []
})

const rules = {
  assetId: [
    { required: true, message: '请选择调拨资产', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入调拨数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  targetResponsiblePerson: [
    { required: true, message: '请选择新负责人', trigger: 'change' }
  ],
  targetLocation: [
    { required: true, message: '请输入新存放位置', trigger: 'blur' }
  ],
  targetDepartmentId: [
    { required: true, message: '请选择调拨部门', trigger: 'change' }
  ],
  transferDate: [
    { required: true, message: '请选择调拨日期', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入调拨事由', trigger: 'blur' }
  ]
}

// 初始化数据
onMounted(async () => {
  await fetchAvailableAssets()
  await fetchUsers()
  await fetchDepartments()
})

// 获取可调拨资产
const fetchAvailableAssets = async () => {
  try {
    const res = await getAssetList({
      assetStatus: '1', // 正常状态的资产
      status: 1,
      pageSize: 1000
    })
    availableAssets.value = res.data.records.filter(asset => asset.currentQuantity > 0)
  } catch (error) {
    console.error('获取资产列表失败:', error)
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    // 这里应该调用用户API，暂时用模拟数据
    users.value = [
      { id: '1', name: '张三' },
      { id: '2', name: '李四' },
      { id: '3', name: '王五' },
      { id: '4', name: '赵六' }
    ]
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 获取部门列表
const fetchDepartments = async () => {
  try {
    // 这里应该调用部门API，暂时用模拟数据
    departments.value = [
      { id: '1', name: '技术部' },
      { id: '2', name: '市场部' },
      { id: '3', name: '人事部' },
      { id: '4', name: '财务部' },
      { id: '5', name: '运维部' }
    ]
  } catch (error) {
    console.error('获取部门列表失败:', error)
  }
}

// 资产选择变化
const handleAssetChange = (assetId) => {
  const asset = availableAssets.value.find(item => item.id === assetId)
  if (asset) {
    selectedAsset.value = asset
    maxQuantity.value = asset.currentQuantity
    formData.sourceResponsiblePerson = asset.responsiblePerson || ''
    formData.sourceLocation = asset.location || ''
    
    if (formData.quantity > maxQuantity.value) {
      formData.quantity = maxQuantity.value
    }
  } else {
    selectedAsset.value = null
    maxQuantity.value = 0
    formData.sourceResponsiblePerson = ''
    formData.sourceLocation = ''
  }
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

// 提交调拨
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = {
      ...formData,
      attachments: formData.attachments.join(',')
    }
    
    await transferAsset(submitData)
    ElMessage.success('调拨申请提交成功')
    handleReset()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败，请检查表单')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const handleReset = () => {
  formRef.value?.resetFields()
  selectedAsset.value = null
  fileList.value = []
  formData.attachments = []
  maxQuantity.value = 0
  formData.quantity = 1
  formData.sourceResponsiblePerson = ''
  formData.sourceLocation = ''
}
</script>

<style scoped>
.transfer-container {
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
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-card,
.info-card {
  width: 100%;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.asset-info {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .label {
  font-weight: bold;
  color: #666;
  min-width: 100px;
}

.info-item .value {
  color: #333;
}
</style>