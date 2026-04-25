<template>
  <div class="label-print-container">
    <div class="header">
      <div class="title">物料标签打印</div>
      <div class="actions">
        <el-button type="primary" @click="handleGenerate" :loading="generating">生成标签</el-button>
        <el-button @click="handleBatchGenerate" :loading="batchGenerating">批量生成</el-button>
        <el-button @click="handlePrint">打印</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>
    
    <div class="content">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="config-card">
            <template #header>
              <div class="card-header">
                <span>标签配置</span>
              </div>
            </template>
            
            <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="120px">
              <el-form-item label="选择资产" prop="assetId">
                <el-select
                  v-model="configForm.assetId"
                  placeholder="请选择资产"
                  filterable
                  style="width: 100%"
                  @change="handleAssetChange"
                >
                  <el-option
                    v-for="asset in assets"
                    :key="asset.id"
                    :label="`${asset.assetName} (${asset.assetCode})`"
                    :value="asset.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="打印数量" prop="quantity">
                <el-input-number
                  v-model="configForm.quantity"
                  :min="1"
                  :max="100"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="标签尺寸" prop="labelSize">
                <el-select v-model="configForm.labelSize" placeholder="请选择标签尺寸" style="width: 100%">
                  <el-option label="小 (50x30mm)" value="small" />
                  <el-option label="中 (70x40mm)" value="medium" />
                  <el-option label="大 (100x60mm)" value="large" />
                  <el-option label="自定义" value="custom" />
                </el-select>
              </el-form-item>
              
              <div v-if="configForm.labelSize === 'custom'" class="custom-size">
                <el-form-item label="宽度(mm)" prop="customWidth">
                  <el-input-number
                    v-model="configForm.customWidth"
                    :min="10"
                    :max="200"
                    style="width: 100%"
                  />
                </el-form-item>
                <el-form-item label="高度(mm)" prop="customHeight">
                  <el-input-number
                    v-model="configForm.customHeight"
                    :min="10"
                    :max="200"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              
              <el-form-item label="标签模板" prop="template">
                <el-select v-model="configForm.template" placeholder="请选择标签模板" style="width: 100%">
                  <el-option label="标准模板" value="standard" />
                  <el-option label="简约模板" value="simple" />
                  <el-option label="带二维码" value="with_qrcode" />
                  <el-option label="带条形码" value="with_barcode" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="包含信息" prop="fields">
                <el-checkbox-group v-model="configForm.fields">
                  <el-checkbox label="assetCode">资产编码</el-checkbox>
                  <el-checkbox label="assetName">资产名称</el-checkbox>
                  <el-checkbox label="model">型号规格</el-checkbox>
                  <el-checkbox label="brand">品牌</el-checkbox>
                  <el-checkbox label="purchaseDate">购买日期</el-checkbox>
                  <el-checkbox label="responsiblePerson">负责人</el-checkbox>
                  <el-checkbox label="location">存放位置</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="打印方向" prop="orientation">
                <el-radio-group v-model="configForm.orientation">
                  <el-radio label="portrait">纵向</el-radio>
                  <el-radio label="landscape">横向</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="边距(mm)" prop="margin">
                <el-input-number
                  v-model="configForm.margin"
                  :min="0"
                  :max="20"
                  style="width: 100%"
                />
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="preview-card">
            <template #header>
              <div class="card-header">
                <span>标签预览</span>
                <el-button type="primary" link @click="handleRefreshPreview">刷新预览</el-button>
              </div>
            </template>
            
            <div class="preview-container">
              <div
                class="label-preview"
                :style="previewStyle"
                v-if="selectedAsset"
              >
                <div class="label-header">
                  <div class="label-title">资产标签</div>
                  <div class="label-qrcode" v-if="configForm.template.includes('qrcode')">
                    <!-- 二维码占位 -->
                    <div class="qrcode-placeholder">QR</div>
                  </div>
                </div>
                
                <div class="label-content">
                  <div class="label-field" v-if="configForm.fields.includes('assetCode')">
                    <span class="field-label">编码：</span>
                    <span class="field-value">{{ selectedAsset.assetCode }}</span>
                  </div>
                  <div class="label-field" v-if="configForm.fields.includes('assetName')">
                    <span class="field-label">名称：</span>
                    <span class="field-value">{{ selectedAsset.assetName }}</span>
                  </div>
                  <div class="label-field" v-if="configForm.fields.includes('model') && selectedAsset.model">
                    <span class="field-label">型号：</span>
                    <span class="field-value">{{ selectedAsset.model }}</span>
                  </div>
                  <div class="label-field" v-if="configForm.fields.includes('brand') && selectedAsset.brand">
                    <span class="field-label">品牌：</span>
                    <span class="field-value">{{ selectedAsset.brand }}</span>
                  </div>
                  <div class="label-field" v-if="configForm.fields.includes('purchaseDate') && selectedAsset.purchaseDate">
                    <span class="field-label">购买日期：</span>
                    <span class="field-value">{{ selectedAsset.purchaseDate }}</span>
                  </div>
                  <div class="label-field" v-if="configForm.fields.includes('responsiblePerson') && selectedAsset.responsiblePerson">
                    <span class="field-label">负责人：</span>
                    <span class="field-value">{{ selectedAsset.responsiblePerson }}</span>
                  </div>
                  <div class="label-field" v-if="configForm.fields.includes('location') && selectedAsset.location">
                    <span class="field-label">位置：</span>
                    <span class="field-value">{{ selectedAsset.location }}</span>
                  </div>
                </div>
                
                <div class="label-footer" v-if="configForm.template.includes('barcode')">
                  <!-- 条形码占位 -->
                  <div class="barcode-placeholder">|||| ||| |||| |||| |||</div>
                </div>
                
                <div class="label-generated">
                  生成时间：{{ currentTime }}
                </div>
              </div>
              
              <div class="empty-preview" v-else>
                <el-empty description="请选择资产进行预览" />
              </div>
              
              <div class="preview-info" v-if="selectedAsset">
                <div class="info-item">
                  <span class="label">标签尺寸：</span>
                  <span class="value">{{ getLabelSizeText() }}</span>
                </div>
                <div class="info-item">
                  <span class="label">打印数量：</span>
                  <span class="value">{{ configForm.quantity }} 张</span>
                </div>
                <div class="info-item">
                  <span class="label">包含字段：</span>
                  <span class="value">{{ configForm.fields.length }} 个</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 批量生成对话框 -->
      <el-dialog
        v-model="batchDialogVisible"
        title="批量生成标签"
        width="800px"
      >
        <BatchGenerateForm
          v-if="batchDialogVisible"
          @submit="handleBatchSubmit"
          @cancel="batchDialogVisible = false"
        />
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getAssetList } from '@/api/asset'
import { generateLabel, batchGenerateLabel } from '@/api/asset'
import BatchGenerateForm from './components/BatchGenerateForm.vue'

const configFormRef = ref()
const generating = ref(false)
const batchGenerating = ref(false)
const batchDialogVisible = ref(false)
const assets = ref([])
const selectedAsset = ref(null)
const currentTime = ref(new Date().toLocaleString())

const configForm = reactive({
  assetId: '',
  quantity: 1,
  labelSize: 'medium',
  customWidth: 100,
  customHeight: 60,
  template: 'standard',
  fields: ['assetCode', 'assetName', 'model', 'brand'],
  orientation: 'portrait',
  margin: 5
})

const configRules = {
  assetId: [
    { required: true, message: '请选择资产', trigger: 'change' }
  ],
  quantity: [
    { required: true, message: '请输入打印数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  labelSize: [
    { required: true, message: '请选择标签尺寸', trigger: 'change' }
  ],
  template: [
    { required: true, message: '请选择标签模板', trigger: 'change' }
  ]
}

// 计算预览样式
const previewStyle = computed(() => {
  const sizeMap = {
    small: { width: '50mm', height: '30mm' },
    medium: { width: '70mm', height: '40mm' },
    large: { width: '100mm', height: '60mm' },
    custom: { width: `${configForm.customWidth}mm`, height: `${configForm.customHeight}mm` }
  }
  
  const size = sizeMap[configForm.labelSize] || sizeMap.medium
  return {
    width: size.width,
    height: size.height,
    transform: configForm.orientation === 'landscape' ? 'rotate(90deg)' : 'none',
    margin: `${configForm.margin}mm`
  }
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

// 资产选择变化
const handleAssetChange = (assetId) => {
  const asset = assets.value.find(item => item.id === assetId)
  selectedAsset.value = asset || null
}

// 获取标签尺寸文本
const getLabelSizeText = () => {
  if (configForm.labelSize === 'custom') {
    return `${configForm.customWidth}×${configForm.customHeight}mm`
  }
  const sizeMap = {
    small: '50×30mm',
    medium: '70×40mm',
    large: '100×60mm'
  }
  return sizeMap[configForm.labelSize] || '70×40mm'
}

// 生成标签
const handleGenerate = async () => {
  try {
    await configFormRef.value.validate()
    
    if (!selectedAsset.value) {
      ElMessage.warning('请选择资产')
      return
    }
    
    generating.value = true
    
    const params = {
      ...configForm,
      assetInfo: selectedAsset.value
    }
    
    // 调用API生成标签
    const response = await generateLabel(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `label_${selectedAsset.value.assetCode}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    
    ElMessage.success('标签生成成功')
  } catch (error) {
    console.error('生成标签失败:', error)
    ElMessage.error('生成标签失败')
  } finally {
    generating.value = false
  }
}

// 批量生成
const handleBatchGenerate = () => {
  batchDialogVisible.value = true
}

// 批量提交
const handleBatchSubmit = async (batchData) => {
  try {
    batchGenerating.value = true
    batchDialogVisible.value = false
    
    // 调用批量生成API
    const response = await batchGenerateLabel(batchData)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(new Blob([response]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', `batch_labels_${new Date().getTime()}.pdf`)
    document.body.appendChild(link)
    link.click()
    link.remove()
    
    ElMessage.success('批量标签生成成功')
  } catch (error) {
    console.error('批量生成失败:', error)
    ElMessage.error('批量生成失败')
  } finally {
    batchGenerating.value = false
  }
}

// 打印
const handlePrint = () => {
  ElMessage.info('打印功能待实现（需要连接打印机）')
}

// 刷新预览
const handleRefreshPreview = () => {
  currentTime.value = new Date().toLocaleString()
}

// 重置
const handleReset = () => {
  configFormRef.value?.resetFields()
  selectedAsset.value = null
  configForm.quantity = 1
  configForm.labelSize = 'medium'
  configForm.template = 'standard'
  configForm.fields = ['assetCode', 'assetName', 'model', 'brand']
  configForm.orientation = 'portrait'
  configForm.margin = 5
}
</script>

<style scoped>
.label-print-container {
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

.config-card,
.preview-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.custom-size {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.label-preview {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  background: white;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  font-family: 'Microsoft YaHei', sans-serif;
}

.label-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 5px;
  margin-bottom: 10px;
}

.label-title {
  font-size: 14px;
  font-weight: bold;
  color: #409eff;
}

.label-qrcode {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px dashed #c0c4cc;
}

.qrcode-placeholder {
  font-size: 12px;
  color: #909399;
}

.label-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label-field {
  display: flex;
  font-size: 12px;
  line-height: 1.4;
}

.field-label {
  color: #606266;
  min-width: 50px;
}

.field-value {
  color: #303133;
  font-weight: 500;
  flex: 1;
}

.label-footer {
  margin-top: 10px;
  padding-top: 5px;
  border-top: 1px solid #e4e7ed;
  text-align: center;
}

.barcode-placeholder {
  font-family: monospace;
  letter-spacing: 2px;
  color: #303133;
  font-size: 14px;
}

.label-generated {
  margin-top: 5px;
  font-size: 10px;
  color: #909399;
  text-align: center;
}

.empty-preview {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
  width: 100%;
}

.preview-info {
  width: 100%;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 4px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #606266;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}
</style>