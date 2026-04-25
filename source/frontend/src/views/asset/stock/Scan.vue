<template>
  <div class="scan-container">
    <div class="header">
      <div class="title">出入库扫码登记</div>
      <div class="actions">
        <el-button type="primary" @click="handleRegister" :loading="registering">登记</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button @click="handleSwitchCamera" v-if="hasCamera">切换摄像头</el-button>
        <el-button @click="handleManualInput">手动输入</el-button>
      </div>
    </div>
    
    <div class="content">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="scanner-card">
            <template #header>
              <div class="card-header">
                <span>扫码区域</span>
                <div class="scanner-status">
                  <el-tag :type="scannerStatus.type">{{ scannerStatus.text }}</el-tag>
                </div>
              </div>
            </template>
            
            <div class="scanner-area">
              <div class="camera-preview" v-if="isCameraActive">
                <!-- 摄像头预览占位 -->
                <div class="camera-placeholder">
                  <el-icon size="50" color="#409eff"><VideoCamera /></el-icon>
                  <div class="placeholder-text">摄像头预览</div>
                  <div class="scan-frame">
                    <div class="scan-line"></div>
                  </div>
                </div>
              </div>
              
              <div class="manual-input-area" v-else>
                <el-input
                  v-model="manualCode"
                  placeholder="请输入资产编码或扫描二维码"
                  @keyup.enter="handleManualSubmit"
                  style="margin-bottom: 20px;"
                >
                  <template #append>
                    <el-button @click="handleManualSubmit">确认</el-button>
                  </template>
                </el-input>
                
                <div class="scan-hint">
                  <el-icon><InfoFilled /></el-icon>
                  <span>支持输入资产编码、二维码内容</span>
                </div>
              </div>
              
              <div class="scanner-controls">
                <el-button
                  type="primary"
                  :icon="isCameraActive ? VideoCamera : Keyboard"
                  @click="toggleCamera"
                >
                  {{ isCameraActive ? '关闭摄像头' : '开启摄像头' }}
                </el-button>
                <el-button @click="handleTestScan">测试扫码</el-button>
              </div>
            </div>
          </el-card>
          
          <el-card class="recent-scans-card">
            <template #header>
              <div class="card-header">
                <span>最近扫描记录</span>
                <el-button type="primary" link @click="handleClearRecent">清空</el-button>
              </div>
            </template>
            
            <div class="recent-scans">
              <el-table :data="recentScans" height="200" style="width: 100%">
                <el-table-column prop="time" label="时间" width="120">
                  <template #default="scope">
                    {{ formatTime(scope.row.time) }}
                  </template>
                </el-table-column>
                <el-table-column prop="code" label="编码" width="150" />
                <el-table-column prop="assetName" label="资产名称" />
                <el-table-column prop="type" label="类型" width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.type === 'in' ? 'success' : 'warning'" size="small">
                      {{ scope.row.type === 'in' ? '入库' : '出库' }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="12">
          <el-card class="register-card">
            <template #header>
              <div class="card-header">
                <span>登记信息</span>
                <div class="operation-type">
                  <el-radio-group v-model="operationType">
                    <el-radio label="in">入库</el-radio>
                    <el-radio label="out">出库</el-radio>
                  </el-radio-group>
                </div>
              </div>
            </template>
            
            <div class="register-form">
              <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="100px">
                <el-form-item label="资产编码" prop="assetCode">
                  <el-input v-model="registerForm.assetCode" placeholder="扫描或输入资产编码" readonly />
                </el-form-item>
                
                <div class="asset-info" v-if="currentAsset">
                  <div class="info-item">
                    <span class="label">资产名称：</span>
                    <span class="value">{{ currentAsset.assetName }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">当前库存：</span>
                    <span class="value">{{ currentAsset.currentQuantity }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">型号规格：</span>
                    <span class="value">{{ currentAsset.model || '无' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="label">存放位置：</span>
                    <span class="value">{{ currentAsset.location || '无' }}</span>
                  </div>
                </div>
                
                <el-form-item label="数量" prop="quantity">
                  <el-input-number
                    v-model="registerForm.quantity"
                    :min="1"
                    :max="operationType === 'out' ? (currentAsset?.currentQuantity || 0) : 9999"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="操作日期" prop="operationDate">
                  <el-date-picker
                    v-model="registerForm.operationDate"
                    type="datetime"
                    placeholder="选择操作日期时间"
                    style="width: 100%"
                    value-format="YYYY-MM-DD HH:mm:ss"
                  />
                </el-form-item>
                
                <el-form-item label="操作人" prop="operator">
                  <el-input v-model="registerForm.operator" placeholder="请输入操作人姓名" />
                </el-form-item>
                
                <el-form-item label="仓库/位置" prop="warehouse">
                  <el-input v-model="registerForm.warehouse" placeholder="请输入仓库或位置" />
                </el-form-item>
                
                <el-form-item label="备注" prop="remark">
                  <el-input
                    v-model="registerForm.remark"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入备注信息"
                  />
                </el-form-item>
              </el-form>
              
              <div class="register-summary" v-if="currentAsset">
                <div class="summary-title">操作摘要</div>
                <div class="summary-content">
                  <div class="summary-item">
                    <span class="label">操作类型：</span>
                    <span class="value">{{ operationType === 'in' ? '入库' : '出库' }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="label">资产：</span>
                    <span class="value">{{ currentAsset.assetName }} ({{ currentAsset.assetCode }})</span>
                  </div>
                  <div class="summary-item">
                    <span class="label">操作后库存：</span>
                    <span class="value" :class="{
                      'warning-text': operationType === 'out' && (currentAsset.currentQuantity - registerForm.quantity) <= (currentAsset.warningQuantity || 0)
                    }">
                      {{ operationType === 'in' 
                        ? currentAsset.currentQuantity + registerForm.quantity 
                        : currentAsset.currentQuantity - registerForm.quantity }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoCamera, Keyboard, InfoFilled } from '@element-plus/icons-vue'
import { getAssetList } from '@/api/asset'
import { scanRegister } from '@/api/asset'

const registerFormRef = ref()
const registering = ref(false)
const isCameraActive = ref(false)
const hasCamera = ref(true) // 假设有摄像头
const manualCode = ref('')
const operationType = ref('in')
const currentAsset = ref(null)
const recentScans = ref([])

const registerForm = reactive({
  assetCode: '',
  quantity: 1,
  operationDate: new Date().toLocaleString('sv').replace('T', ' ').slice(0, 19),
  operator: '',
  warehouse: '',
  remark: ''
})

const registerRules = {
  assetCode: [
    { required: true, message: '请扫描或输入资产编码', trigger: 'blur' }
  ],
  quantity: [
    { required: true, message: '请输入数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
  ],
  operationDate: [
    { required: true, message: '请选择操作日期', trigger: 'change' }
  ],
  operator: [
    { required: true, message: '请输入操作人', trigger: 'blur' }
  ]
}

// 计算扫描器状态
const scannerStatus = computed(() => {
  if (isCameraActive.value) {
    return { type: 'success', text: '扫描中...' }
  } else if (manualCode.value) {
    return { type: 'warning', text: '手动输入模式' }
  } else {
    return { type: 'info', text: '等待扫描' }
  }
})

// 初始化数据
onMounted(() => {
  // 加载最近扫描记录
  loadRecentScans()
})

// 加载最近扫描记录
const loadRecentScans = () => {
  const saved = localStorage.getItem('recentAssetScans')
  if (saved) {
    recentScans.value = JSON.parse(saved)
  }
}

// 保存扫描记录
const saveScanRecord = (scan) => {
  recentScans.value.unshift({
    ...scan,
    time: new Date().getTime()
  })
  
  // 只保留最近20条记录
  if (recentScans.value.length > 20) {
    recentScans.value = recentScans.value.slice(0, 20)
  }
  
  localStorage.setItem('recentAssetScans', JSON.stringify(recentScans.value))
}

// 切换摄像头
const toggleCamera = () => {
  isCameraActive.value = !isCameraActive.value
  if (!isCameraActive.value) {
    manualCode.value = ''
  }
}

// 切换摄像头
const handleSwitchCamera = () => {
  ElMessage.info('切换摄像头功能待实现')
}

// 手动输入
const handleManualInput = () => {
  isCameraActive.value = false
}

// 手动提交
const handleManualSubmit = async () => {
  if (!manualCode.value.trim()) {
    ElMessage.warning('请输入资产编码')
    return
  }
  
  await processScannedCode(manualCode.value.trim())
  manualCode.value = ''
}

// 测试扫码
const handleTestScan = () => {
  const testCodes = ['ASSET-001', 'ASSET-002', 'ASSET-003']
  const randomCode = testCodes[Math.floor(Math.random() * testCodes.length)]
  processScannedCode(randomCode)
}

// 处理扫描到的编码
const processScannedCode = async (code) => {
  try {
    registerForm.assetCode = code
    
    // 根据编码查询资产信息
    const res = await getAssetList({
      assetCode: code,
      pageSize: 1
    })
    
    if (res.data.records.length > 0) {
      currentAsset.value = res.data.records[0]
      ElMessage.success(`识别到资产: ${currentAsset.value.assetName}`)
      
      // 保存扫描记录
      saveScanRecord({
        code,
        assetName: currentAsset.value.assetName,
        type: operationType.value
      })
    } else {
      currentAsset.value = null
      ElMessage.warning('未找到对应资产，请检查编码是否正确')
    }
  } catch (error) {
    console.error('查询资产失败:', error)
    ElMessage.error('查询资产失败')
  }
}

// 登记
const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    
    if (!currentAsset.value) {
      ElMessage.warning('请先扫描有效的资产编码')
      return
    }
    
    registering.value = true
    
    const registerData = {
      ...registerForm,
      operationType: operationType.value,
      assetId: currentAsset.value.id,
      assetName: currentAsset.value.assetName
    }
    
    await scanRegister(registerData)
    
    ElMessage.success(`${operationType.value === 'in' ? '入库' : '出库'}登记成功`)
    
    // 重置表单
    handleReset()
    
    // 刷新资产信息
    if (registerForm.assetCode) {
      await processScannedCode(registerForm.assetCode)
    }
  } catch (error) {
    console.error('登记失败:', error)
    ElMessage.error('登记失败')
  } finally {
    registering.value = false
  }
}

// 重置
const handleReset = () => {
  registerFormRef.value?.resetFields()
  currentAsset.value = null
  registerForm.operationDate = new Date().toLocaleString('sv').replace('T', ' ').slice(0, 19)
  registerForm.quantity = 1
}

// 清空最近记录
const handleClearRecent = () => {
  recentScans.value = []
  localStorage.removeItem('recentAssetScans')
  ElMessage.success('已清空最近扫描记录')
}

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.scan-container {
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

.scanner-card,
.register-card,
.recent-scans-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.scanner-status {
  display: flex;
  align-items: center;
}

.scanner-area {
  padding: 20px;
  text-align: center;
}

.camera-preview {
  position: relative;
  width: 100%;
  height: 300px;
  background: #000;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
}

.camera-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: white;
}

.placeholder-text {
  margin: 15px 0;
  font-size: 16px;
}

.scan-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border: 2px solid #409eff;
  border-radius: 8px;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: #409eff;
  animation: scan 2s linear infinite;
}

@keyframes scan {
  0% { top: 0; }
  100% { top: 100%; }
}

.manual-input-area {
  padding: 20px;
}

.scan-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
  margin-top: 15px;
}

.scan-hint .el-icon {
  margin-right: 8px;
}

.scanner-controls {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.recent-scans {
  max-height: 200px;
  overflow-y: auto;
}

.register-form {
  padding: 10px 0;
}

.asset-info {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}

.info-item .label {
  color: #606266;
  min-width: 80px;
}

.info-item .value {
  color: #303133;
  font-weight: 500;
}

.register-summary {
  margin-top: 30px;
  padding: 20px;
  background: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #d9ecff;
}

.summary-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #409eff;
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

.warning-text {
  color: #f56c6c;
  font-weight: bold;
}

.operation-type {
  display: flex;
  align-items: center;
}
</style>