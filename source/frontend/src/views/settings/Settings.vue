<template>
  <div class="settings-container">
    <h2>系统设置</h2>

    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 基础设置 -->
      <el-tab-pane label="基础设置" name="basic">
        <el-card>
          <el-form :model="basicForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="平台名称">
              <el-input v-model="basicForm.appName" />
            </el-form-item>
            <el-form-item label="平台Logo">
              <el-upload action="#" :auto-upload="false" list-type="picture-card">
                <el-icon><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="系统公告">
              <el-input v-model="basicForm.notice" type="textarea" :rows="3" />
            </el-form-item>
            <el-form-item label="版权信息">
              <el-input v-model="basicForm.copyright" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- AI模型配置 -->
      <el-tab-pane label="AI模型配置" name="ai">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>AI模型列表</span>
              <el-button type="primary" size="small" @click="showModelDialog = true">添加模型</el-button>
            </div>
          </template>
          <el-table :data="modelList" style="width: 100%">
            <el-table-column prop="name" label="模型名称" width="150" />
            <el-table-column prop="capability" label="能力类型" width="100">
              <template #default="{ row }">
                <el-tag>{{ capabilityLabel(row.capability) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="provider" label="供应商" width="120" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
                  {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="quota" label="月度配额" width="120" />
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button size="small" @click="testModel(row)">测试</el-button>
                <el-button size="small" type="primary" @click="editModel(row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteModel(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card style="margin-top: 16px">
          <template #header>
            <span>功能模型分配</span>
          </template>
          <el-table :data="assignmentList" style="width: 100%">
            <el-table-column prop="functionModule" label="功能模块" width="150" />
            <el-table-column prop="capability" label="能力" width="100" />
            <el-table-column prop="modelName" label="使用模型" width="150" />
            <el-table-column prop="priority" label="优先级" width="80" />
            <el-table-column prop="failoverModelName" label="备用模型" width="150" />
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button size="small" type="primary">配置</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <el-card>
          <el-form :model="securityForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityForm.passwordMinLength" :min="6" :max="32" />
            </el-form-item>
            <el-form-item label="会话超时(分钟)">
              <el-input-number v-model="securityForm.sessionTimeout" :min="5" :max="480" />
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-switch v-model="securityForm.loginLockEnabled" />
              <span class="form-tip">连续失败5次锁定30分钟</span>
            </el-form-item>
            <el-form-item label="双因素认证">
              <el-switch v-model="securityForm.mfaEnabled" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSecurity">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 通知配置 -->
      <el-tab-pane label="通知配置" name="notification">
        <el-card>
          <el-form :model="notificationForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="邮件通知">
              <el-switch v-model="notificationForm.emailEnabled" />
            </el-form-item>
            <el-form-item label="SMTP服务器">
              <el-input v-model="notificationForm.smtpHost" placeholder="smtp.example.com" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="notificationForm.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="notificationForm.smtpUser" />
            </el-form-item>
            <el-form-item label="授权密码">
              <el-input v-model="notificationForm.smtpPass" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveNotification">保存设置</el-button>
              <el-button @click="testEmail">发送测试邮件</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 审批配置 -->
      <el-tab-pane label="审批配置" name="approval">
        <el-card>
          <el-form :model="approvalForm" label-width="120px" style="max-width: 600px">
            <el-form-item label="审批超时(小时)">
              <el-input-number v-model="approvalForm.approvalTimeout" :min="1" :max="168" />
              <span class="form-tip">超过此时间未审批将发送提醒</span>
            </el-form-item>
            <el-form-item label="自动通过金额">
              <el-input-number v-model="approvalForm.autoApproveAmount" :min="0" :step="1000" />
              <span class="form-tip">低于此金额且常规品类自动通过</span>
            </el-form-item>
            <el-form-item label="审批层级">
              <el-input-number v-model="approvalForm.maxApprovalLevel" :min="1" :max="5" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveApproval">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 缓存管理 -->
      <el-tab-pane label="缓存管理" name="cache">
        <el-card>
          <div class="cache-info">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="缓存命中率">{{ cacheStats.hitRate }}%</el-descriptions-item>
              <el-descriptions-item label="已用内存">{{ cacheStats.usedMemory }}</el-descriptions-item>
              <el-descriptions-item label="Key总数">{{ cacheStats.keyCount }}</el-descriptions-item>
              <el-descriptions-item label="最后清理">{{ cacheStats.lastCleared }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div class="cache-actions" style="margin-top: 16px">
            <el-button type="warning" @click="clearCache">清理缓存</el-button>
            <el-button @click="refreshCacheStats">刷新统计</el-button>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑模型对话框 -->
    <el-dialog v-model="showModelDialog" :title="editingModel ? '编辑模型' : '添加模型'" width="500px">
      <el-form :model="modelForm" label-width="100px">
        <el-form-item label="模型名称" required>
          <el-input v-model="modelForm.name" />
        </el-form-item>
        <el-form-item label="能力类型" required>
          <el-select v-model="modelForm.capability" style="width: 100%">
            <el-option label="生文" value="TEXT" />
            <el-option label="生图" value="IMAGE" />
            <el-option label="生视频" value="VIDEO" />
            <el-option label="语音" value="AUDIO" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商" required>
          <el-select v-model="modelForm.provider" style="width: 100%">
            <el-option label="OpenAI" value="OpenAI" />
            <el-option label="Anthropic" value="Anthropic" />
            <el-option label="智谱" value="Zhipu" />
            <el-option label="百度" value="Baidu" />
            <el-option label="阿里" value="Alibaba" />
            <el-option label="月之暗面" value="Moonshot" />
          </el-select>
        </el-form-item>
        <el-form-item label="API Endpoint">
          <el-input v-model="modelForm.endpoint" placeholder="可选，默认使用官方地址" />
        </el-form-item>
        <el-form-item label="API Key" required>
          <el-input v-model="modelForm.apiKey" type="password" show-password />
        </el-form-item>
        <el-form-item label="月度配额">
          <el-input-number v-model="modelForm.quota" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showModelDialog = false">取消</el-button>
        <el-button type="primary" @click="saveModel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { AIModelVO } from '@/api/ai'

const activeTab = ref('basic')

// 基础设置
const basicForm = reactive({
  appName: 'AI-OA 智能办公平台',
  notice: '',
  copyright: '© 2026 AI-OA Team'
})

// 安全设置
const securityForm = reactive({
  passwordMinLength: 8,
  sessionTimeout: 30,
  loginLockEnabled: true,
  mfaEnabled: false
})

// 通知配置
const notificationForm = reactive({
  emailEnabled: true,
  smtpHost: 'smtp.example.com',
  smtpPort: 465,
  smtpUser: '',
  smtpPass: ''
})

// 审批配置
const approvalForm = reactive({
  approvalTimeout: 24,
  autoApproveAmount: 5000,
  maxApprovalLevel: 3
})

// 缓存统计
const cacheStats = reactive({
  hitRate: 92.5,
  usedMemory: '256MB',
  keyCount: 15230,
  lastCleared: '2026-04-16 03:00'
})

// AI模型
const showModelDialog = ref(false)
const editingModel = ref(false)
const modelForm = reactive<Partial<AIModelVO>>({
  name: '',
  capability: 'TEXT',
  provider: '',
  endpoint: '',
  apiKey: '',
  quota: 10000
})

const modelList = ref<AIModelVO[]>([
  { id: '1', name: 'GPT-4o', capability: 'TEXT', provider: 'OpenAI', status: 'ENABLED', quota: 10000, usedQuota: 3200 },
  { id: '2', name: 'Claude 3.5', capability: 'TEXT', provider: 'Anthropic', status: 'ENABLED', quota: 8000, usedQuota: 1500 },
  { id: '3', name: 'DALL-E 3', capability: 'IMAGE', provider: 'OpenAI', status: 'ENABLED', quota: 500, usedQuota: 120 },
  { id: '4', name: 'Kimi', capability: 'TEXT', provider: 'Moonshot', status: 'DISABLED', quota: 5000, usedQuota: 0 }
])

const assignmentList = ref([
  { id: '1', functionModule: 'AI对话助手', capability: '生文', modelName: 'GPT-4o', priority: 1, failoverModelName: 'Claude 3.5' },
  { id: '2', functionModule: '智能报表', capability: '生文', modelName: 'GPT-4o', priority: 1, failoverModelName: 'Kimi' },
  { id: '3', functionModule: '期刊封面', capability: '生图', modelName: 'DALL-E 3', priority: 1, failoverModelName: '' },
  { id: '4', functionModule: '合同审查', capability: '生文', modelName: 'Kimi', priority: 1, failoverModelName: 'GPT-4o' }
])

const capabilityLabel = (cap: string) => {
  const map: Record<string, string> = { TEXT: '生文', IMAGE: '生图', VIDEO: '生视频', AUDIO: '语音' }
  return map[cap] || cap
}

const saveBasic = () => ElMessage.success('基础设置已保存')
const saveSecurity = () => ElMessage.success('安全设置已保存')
const saveNotification = () => ElMessage.success('通知配置已保存')
const saveApproval = () => ElMessage.success('审批配置已保存')

const testEmail = () => ElMessage.info('测试邮件已发送')

const clearCache = () => {
  ElMessage.success('缓存已清理')
  cacheStats.lastCleared = new Date().toLocaleString()
}

const refreshCacheStats = () => ElMessage.success('缓存统计已刷新')

const testModel = (row: AIModelVO) => ElMessage.info(`正在测试 ${row.name}...`)

const editModel = (row: AIModelVO) => {
  editingModel.value = true
  Object.assign(modelForm, row)
  showModelDialog.value = true
}

const deleteModel = (row: AIModelVO) => {
  modelList.value = modelList.value.filter(m => m.id !== row.id)
  ElMessage.success(`已删除 ${row.name}`)
}

const saveModel = () => {
  if (!modelForm.name || !modelForm.provider) {
    ElMessage.warning('请填写必填项')
    return
  }
  ElMessage.success('模型已保存')
  showModelDialog.value = false
  editingModel.value = false
  Object.assign(modelForm, { name: '', capability: 'TEXT', provider: '', endpoint: '', apiKey: '', quota: 10000 })
}
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 20px;

  h2 {
    margin-bottom: 20px;
  }

  .settings-tabs {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
  }

  .form-tip {
    margin-left: 12px;
    color: #999;
    font-size: 12px;
  }

  .cache-info {
    margin-bottom: 16px;
  }

  .cache-actions {
    display: flex;
    gap: 12px;
  }
}
</style>
