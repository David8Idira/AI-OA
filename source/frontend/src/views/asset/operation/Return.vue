<template>
  <div class="return-container">
    <div class="header">
      <div class="title">资产归还</div>
      <div class="actions">
        <el-button type="primary" @click="handleSubmit" :loading="submitting">提交归还</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleScan">扫码归还</el-button>
      </div>
    </div>
    
    <div class="content">
      <el-card class="form-card">
        <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="归还资产" prop="assetId">
                <el-select
                  v-model="formData.assetId"
                  placeholder="请选择要归还的资产"
                  filterable
                  style="width: 100%"
                  @change="handleAssetChange"
                >
                  <el-option
                    v-for="asset in borrowedAssets"
                    :key="asset.id"
                    :label="`${asset.assetName} (${asset.assetCode})`"
                    :value="asset.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="归还数量" prop="quantity">
                <el-input-number
                  v-model="formData.quantity"
                  :min="1"
                  :max="maxQuantity"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
          
          <el-form-item label="归还人" prop="returnerId">
            <el-select
              v-model="formData.returnerId"
              placeholder="请选择归还人"
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
          
          <el-form-item label="归还日期" prop="returnDate">
            <el-date-picker
              v-model="formData.returnDate"
              type="date"
              placeholder="选择归还日期"
              style="width: 100%"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
          
          <el-form-item label="资产状态" prop="assetStatus">
            <el-select v-model="formData.assetStatus" placeholder="请选择归还后状态" style="width: 100%">
              <el-option label="正常" value="1" />
              <el-option label="维修中" value="3" />
              <el-option label="报废" value="4" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="归还备注" prop="remark">
            <el-input
              v-model="formData.remark"
              type="textarea"
              :rows="3"
              placeholder="请输入归还备注"
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
            <span class="label">领用人：</span>
            <span class="value">{{ selectedAsset.borrowerName || '未知' }}</span>
          </div>
          <div class="info-item">
            <span class="label">领用日期：</span>
            <span class="value">{{ selectedAsset.borrowDate }}</span>
          </div>
          <div class="info-item">
            <span class="label">领用数量：</span>
            <span class="value">{{ selectedAsset.borrowQuantity }}</span>
          </div>
          <div class="info-item">
            <span class="label">领用事由：</span>
            <span class="value">{{ selectedAsset.borrowReason || '无' }}</span>
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
import { returnAsset } from '@/api/asset'

const formRef = ref()
const submitting = ref(false)
const borrowedAssets = ref([])
const users = ref([])
const selectedAsset = ref(null)
const fileList = ref([])
const maxQuantity = ref(0)

const formData = reactive({
  assetId: '',
  quantity: 1,
  returnerId: '',
  returnDate: '',
  assetStatus: '1',
  remark: '',
  attachments: []
})

const rules = {
  assetId: [
    { required: true, message: '请选择归还资产', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入归还数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  returnerId: [
    { required: true, message: '请选择归还人', trigger: 'change' }
  ],
  returnDate: [
    { required: true, message: '请选择归还日期', trigger: 'change' }
  ],
  assetStatus: [
    { required: true, message: '请选择资产状态', trigger: 'change' }
  ]
}

// 初始化数据
onMounted(async () => {
  await fetchBorrowedAssets()
  await fetchUsers()
})

// 获取已借出资产
const fetchBorrowedAssets = async () => {
  try {
    // 这里应该调用API获取已借出资产，暂时用模拟数据
    borrowedAssets.value = [
      {
        id: '1',
        assetName: '笔记本电脑',
        assetCode: 'ASSET-001',
        borrowerName: '张三',
        borrowDate: '2024-01-15',
        borrowQuantity: 2,
        borrowReason: '项目开发需要'
      },
      {
        id: '2',
        assetName: '投影仪',
        assetCode: 'ASSET-002',
        borrowerName: '李四',
        borrowDate: '2024-01-10',
        borrowQuantity: 1,
        borrowReason: '会议演示'
      },
      {
        id: '3',
        assetName: '打印机',
        assetCode: 'ASSET-003',
        borrowerName: '王五',
        borrowDate: '2024-01-05',
        borrowQuantity: 1,
        borrowReason: '日常办公'
      }
    ]
  } catch (error) {
    console.error('获取已借出资产失败:', error)
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    // 这里应该调用用户API，暂时用模拟数据
    users.value = [
      { id: '1', name: '张三' },
      { id: '2', name: '李四' },
      { id: '3', name: '王五' }
    ]
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 资产选择变化
const handleAssetChange = (assetId) => {
  const asset = borrowedAssets.value.find(item => item.id === assetId)
  if (asset) {
    selectedAsset.value = asset
    maxQuantity.value = asset.borrowQuantity
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

// 提交归还
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    const submitData = {
      ...formData,
      attachments: formData.attachments.join(',')
    }
    
    await returnAsset(submitData)
    ElMessage.success('归还成功')
    handleReset()
    await fetchBorrowedAssets() // 刷新列表
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
  formData.assetStatus = '1'
}

// 扫码归还
const handleScan = () => {
  ElMessage.info('扫码功能待实现')
}
</script>

<style scoped>
.return-container {
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