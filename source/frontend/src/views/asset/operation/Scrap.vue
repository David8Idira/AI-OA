<template>
  <div class="scrap-container">
    <div class="header">
      <div class="title">资产报废</div>
      <div class="actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交报废</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>
    
    <div class="content">
      <el-card class="form-card">
        <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="报废资产" prop="assetId">
                <el-select
                  v-model="formData.assetId"
                  placeholder="请选择要报废的资产"
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
              <el-form-item label="报废数量" prop="quantity">
                <el-input-number
                  v-model="formData.quantity"
                  :min="1"
                  :max="maxQuantity"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="报废原因" prop="reason">
            <el-select v-model="formData.reason" placeholder="请选择报废原因" style="width: 100%">
              <el-option label="自然损坏" value="自然损坏" />
              <el-option label="人为损坏" value="人为损坏" />
              <el-option label="技术淘汰" value="技术淘汰" />
              <el-option label="使用年限到期" value="使用年限到期" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="详细说明" prop="description" v-if="formData.reason === '其他'">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="3"
              placeholder="请详细说明报废原因"
            />
          </el-form-item>
          
          <el-form-item label="报废日期" prop="scrapDate">
            <el-date-picker
              v-model="formData.scrapDate"
              type="date"
              placeholder="选择报废日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="处置方式" prop="disposalMethod">
            <el-select v-model="formData.disposalMethod" placeholder="请选择处置方式" style="width: 100%">
              <el-option label="变卖" value="变卖" />
              <el-option label="捐赠" value="捐赠" />
              <el-option label="回收" value="回收" />
              <el-option label="销毁" value="销毁" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="预计残值" prop="estimatedResidualValue">
            <el-input-number
              v-model="formData.estimatedResidualValue"
              :min="0"
              :precision="2"
              style="width: 100%"
            >
              <template #prefix>¥</template>
            </el-input-number>
          </el-form-item>
          
          <el-form-item label="审批人" prop="approverId">
            <el-select
              v-model="formData.approverId"
              placeholder="请选择审批人"
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="user in approvers"
                :key="user.id"
                :label="user.name"
                :value="user.id"
              />
            </el-select>
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
            <span class="label">购买价格：</span>
            <span class="value">¥{{ selectedAsset.purchasePrice?.toFixed(2) || '0.00' }}</span>
          </div>
          <div class="info-item">
            <span class="label">购买日期：</span>
            <span class="value">{{ selectedAsset.purchaseDate || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前库存：</span>
            <span class="value">{{ selectedAsset.currentQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">使用年限：</span>
            <span class="value">{{ selectedAsset.serviceLife || '未知' }}年</span>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAssetList } from '@/api/asset'
import { scrapAsset } from '@/api/asset'

const formRef = ref()
const submitting = ref(false)
const availableAssets = ref([])
const approvers = ref([])
const selectedAsset = ref(null)
const fileList = ref([])
const maxQuantity = ref(0)

const formData = reactive({
  assetId: '',
  quantity: 1,
  reason: '',
  description: '',
  scrapDate: '',
  disposalMethod: '',
  estimatedResidualValue: 0,
  approverId: '',
  remark: '',
  attachments: []
})

const rules = {
  assetId: [
    { required: true, message: '请选择报废资产', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入报废数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请选择报废原因', trigger: 'change' }
  ],
  scrapDate: [
    { required: true, message: '请选择报废日期', trigger: 'change' }
  ],
  disposalMethod: [
    { required: true, message: '请选择处置方式', trigger: 'change' }
  ],
  approverId: [
    { required: true, message: '请选择审批人', trigger: 'change' }
  ]
}

// 监听报废原因变化
watch(() => formData.reason, (newVal) => {
  if (newVal !== '其他') {
    formData.description = ''
  }
})

// 初始化数据
onMounted(async () => {
  await fetchAvailableAssets()
  await fetchApprovers()
})

// 获取可报废资产
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

// 获取审批人列表
const fetchApprovers = async () => {
  try {
    // 这里应该调用用户API获取有审批权限的用户，暂时用模拟数据
    approvers.value = [
      { id: '1', name: '部门经理-张三' },
      { id: '2', name: '资产管理员-李四' },
      { id: '3', name: '财务总监-王五' }
    ]
  } catch (error) {
    console.error('获取审批人列表失败:', error)
  }
}

// 资产选择变化
const handleAssetChange = (assetId) => {
  const asset = availableAssets.value.find(item => item.id === assetId)
  if (asset) {
    selectedAsset.value = asset
    maxQuantity.value = asset.currentQuantity
    if (formData.quantity > maxQuantity.value) {
      formData.quantity = maxQuantity.value
    }
  } else {
    selectedAsset.value = null
    maxQuantity.value = 0
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

// 提交报废
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = {
      ...formData,
      attachments: formData.attachments.join(',')
    }
    
    await scrapAsset(submitData)
    ElMessage.success('报废申请提交成功，等待审批')
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
  formData.estimatedResidualValue = 0
}
</script>

<style scoped>
.scrap-container {
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