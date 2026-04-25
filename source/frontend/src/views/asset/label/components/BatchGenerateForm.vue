<template>
  <div class="batch-generate-form">
    <el-form :model="formData" :rules="rules" ref="formRef" label-width="120px">
      <el-form-item label="批量选择" prop="assetIds">
        <el-transfer
          v-model="formData.assetIds"
          :data="transferData"
          :titles="['可选资产', '已选资产']"
          :props="{
            key: 'id',
            label: 'display'
          }"
          filterable
          style="width: 100%; height: 400px;"
        />
      </el-form-item>
      
      <el-form-item label="每资产数量" prop="quantityPerAsset">
        <el-input-number
          v-model="formData.quantityPerAsset"
          :min="1"
          :max="50"
          style="width: 100%"
        />
      </el-form-item>
      
      <el-form-item label="标签模板" prop="template">
        <el-select v-model="formData.template" placeholder="请选择标签模板" style="width: 100%">
          <el-option label="标准模板" value="standard" />
          <el-option label="简约模板" value="simple" />
          <el-option label="带二维码" value="with_qrcode" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="输出格式" prop="outputFormat">
        <el-radio-group v-model="formData.outputFormat">
          <el-radio label="single">单个PDF文件</el-radio>
          <el-radio label="multiple">多个PDF文件（压缩包）</el-radio>
        </el-radio-group>
      </el-form-item>
      
      <el-form-item label="文件名前缀" prop="filenamePrefix">
        <el-input v-model="formData.filenamePrefix" placeholder="请输入文件名前缀" />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">生成</el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
    
    <div class="summary" v-if="formData.assetIds.length > 0">
      <div class="summary-title">生成摘要</div>
      <div class="summary-content">
        <div class="summary-item">
          <span class="label">选择的资产：</span>
          <span class="value">{{ formData.assetIds.length }} 个</span>
        </div>
        <div class="summary-item">
          <span class="label">总标签数量：</span>
          <span class="value">{{ formData.assetIds.length * formData.quantityPerAsset }} 张</span>
        </div>
        <div class="summary-item">
          <span class="label">输出格式：</span>
          <span class="value">{{ formData.outputFormat === 'single' ? '单个PDF文件' : '多个PDF文件（压缩包）' }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAssetList } from '@/api/asset'

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()
const submitting = ref(false)
const assets = ref([])

const formData = reactive({
  assetIds: [],
  quantityPerAsset: 1,
  template: 'standard',
  outputFormat: 'single',
  filenamePrefix: 'batch_labels'
})

const rules = {
  assetIds: [
    { required: true, message: '请选择至少一个资产', trigger: 'change' },
    { type: 'array', min: 1, message: '请选择至少一个资产', trigger: 'change' }
  ],
  quantityPerAsset: [
    { required: true, message: '请输入每资产数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  template: [
    { required: true, message: '请选择标签模板', trigger: 'change' }
  ],
  outputFormat: [
    { required: true, message: '请选择输出格式', trigger: 'change' }
  ]
}

// 计算Transfer组件的数据
const transferData = computed(() => {
  return assets.value.map(asset => ({
    id: asset.id,
    display: `${asset.assetName} (${asset.assetCode}) - 库存: ${asset.currentQuantity}`
  }))
})

// 初始化数据
onMounted(async () => {
  await fetchAssets()
})

// 获取资产列表
const fetchAssets = async () => {
  try {
    const res = await getAssetList({
      pageSize: 1000
    })
    assets.value = res.data.records
  } catch (error) {
    console.error('获取资产列表失败:', error)
  }
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    if (formData.assetIds.length === 0) {
      ElMessage.warning('请选择至少一个资产')
      return
    }
    
    submitting.value = true
    
    // 准备提交数据
    const submitData = {
      assetIds: formData.assetIds,
      quantityPerAsset: formData.quantityPerAsset,
      template: formData.template,
      outputFormat: formData.outputFormat,
      filenamePrefix: formData.filenamePrefix,
      totalLabels: formData.assetIds.length * formData.quantityPerAsset
    }
    
    emit('submit', submitData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    submitting.value = false
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.batch-generate-form {
  padding: 20px 0;
}

.summary {
  margin-top: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.summary-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.summary-item .label {
  color: #606266;
}

.summary-item .value {
  color: #303133;
  font-weight: 500;
}
</style>