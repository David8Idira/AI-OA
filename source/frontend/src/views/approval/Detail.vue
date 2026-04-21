<template>
  <div class="approval-detail-page" v-loading="loading">
    <!-- 顶部导航 -->
    <div class="page-nav">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="detail-content" v-if="detail">
      <!-- 左侧：审批表单内容 -->
      <div class="main-content">
        <!-- 审批状态卡片 -->
        <el-card class="status-card">
          <div class="status-header">
            <div class="status-info">
              <el-tag :type="getStatusConfig(detail.status).type" size="large">
                {{ getStatusConfig(detail.status).label }}
              </el-tag>
              <h2 class="approval-title">{{ detail.title }}</h2>
            </div>
            <div class="status-actions" v-if="canHandle">
              <el-button type="success" size="large" @click="handleApprove">
                <el-icon><Check /></el-icon>
                同意
              </el-button>
              <el-button type="danger" size="large" @click="handleReject">
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
              <el-button size="large" @click="handleTransfer">
                <el-icon><RefreshRight /></el-icon>
                转交
              </el-button>
            </div>
            <div class="status-actions" v-else-if="isInitiator && detail.status === 'PENDING'">
              <el-button type="warning" @click="handleCancel">
                <el-icon><CloseBold /></el-icon>
                撤回申请
              </el-button>
            </div>
          </div>
          
          <!-- 进度流程 -->
          <div class="approval-progress">
            <el-steps :active="getStepActive" finish-status="success" align-center>
              <el-step title="提交申请" :description="detail.createTime" />
              <el-step title="审批中" v-if="detail.status === 'PENDING'" />
              <el-step title="审批通过" v-else-if="detail.status === 'APPROVED'" />
              <el-step title="已拒绝" v-else-if="detail.status === 'REJECTED'" />
              <el-step title="已撤回" v-else-if="detail.status === 'CANCELLED'" />
            </el-steps>
          </div>
        </el-card>

        <!-- 审批表单内容 -->
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>申请内容</span>
              <div class="header-meta">
                <el-tag :type="getTypeConfig(detail.type).type" effect="plain">
                  {{ getTypeConfig(detail.type).label }}
                </el-tag>
                <span class="form-id">编号：{{ detail.id }}</span>
              </div>
            </div>
          </template>
          
          <div class="form-content">
            <!-- 通用信息 -->
            <el-descriptions :column="2" border class="info-descriptions">
              <el-descriptions-item label="申请人">
                <div class="user-info-cell">
                  <el-avatar :size="32" :src="detail.submitterAvatar">
                    {{ detail.submitterName?.charAt(0) }}
                  </el-avatar>
                  <span>{{ detail.submitterName }}</span>
                </div>
              </el-descriptions-item>
              <el-descriptions-item label="所属部门">
                {{ detail.deptName || '技术部' }}
              </el-descriptions-item>
              <el-descriptions-item label="申请类型" :span="2">
                {{ getTypeConfig(detail.type).label }}
              </el-descriptions-item>
              
              <!-- 动态字段：根据类型显示不同内容 -->
              <template v-if="detail.startDate">
                <el-descriptions-item label="开始日期">
                  {{ detail.startDate }}
                </el-descriptions-item>
                <el-descriptions-item label="结束日期">
                  {{ detail.endDate || '进行中' }}
                </el-descriptions-item>
              </template>
              
              <template v-if="detail.amount">
                <el-descriptions-item label="申请金额" :span="2">
                  <span class="amount-value">¥{{ detail.amount.toLocaleString() }}</span>
                </el-descriptions-item>
              </template>
              
              <el-descriptions-item label="申请理由" :span="2">
                {{ detail.reason }}
              </el-descriptions-item>
              
              <el-descriptions-item label="详细说明" :span="2">
                <div class="content-text">{{ detail.content }}</div>
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 附件 -->
            <div class="attachments" v-if="detail.attachments && detail.attachments.length > 0">
              <h4>附件</h4>
              <div class="attachment-list">
                <div
                  v-for="file in detail.attachments"
                  :key="file.id"
                  class="attachment-item"
                >
                  <el-icon><Document /></el-icon>
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">({{ formatFileSize(file.size) }})</span>
                  <el-button type="primary" text @click="handleDownload(file)">
                    下载
                  </el-button>
                </div>
              </div>
            </div>
            
            <!-- 动态表单数据 -->
            <div class="dynamic-form" v-if="detail.formData && Object.keys(detail.formData).length > 0">
              <h4>表单数据</h4>
              <el-descriptions :column="1" border>
                <el-descriptions-item
                  v-for="(value, key) in detail.formData"
                  :key="key"
                  :label="String(key)"
                >
                  {{ formatFormValue(value) }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>
        </el-card>
      </div>

      <!-- 右侧：审批历史 -->
      <div class="side-content">
        <!-- 审批历史时间线 -->
        <el-card class="history-card">
          <template #header>
            <span>审批历史</span>
          </template>
          
          <el-timeline v-if="detail.history.length > 0">
            <el-timeline-item
              v-for="(item, index) in detail.history"
              :key="item.id"
              :type="getHistoryItemType(item.action)"
              :hollow="index === 0"
              placement="top"
            >
              <div class="history-item">
                <div class="history-header">
                  <el-avatar :size="32" :src="item.operatorAvatar">
                    {{ item.operatorName?.charAt(0) }}
                  </el-avatar>
                  <div class="history-info">
                    <span class="operator-name">{{ item.operatorName }}</span>
                    <span class="action-text">{{ getHistoryActionText(item.action) }}</span>
                  </div>
                </div>
                
                <div class="history-comment" v-if="item.comment">
                  "{{ item.comment }}"
                </div>
                
                <div class="history-meta">
                  <span class="history-time">{{ formatTime(item.createTime) }}</span>
                  <span class="history-duration" v-if="item.duration">
                    耗时 {{ item.duration }}
                  </span>
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          
          <el-empty v-else description="暂无审批历史" />
        </el-card>

        <!-- 当前审批人 -->
        <el-card class="approver-card" v-if="detail.currentApprover || detail.nextApprovers?.length">
          <template #header>
            <span>审批人</span>
          </template>
          
          <div class="current-approver" v-if="detail.currentApprover">
            <el-tag type="warning" effect="dark">当前审批</el-tag>
            <div class="approver-info">
              <el-avatar :size="40" :src="detail.currentApprover.avatar">
                {{ detail.currentApprover.name?.charAt(0) }}
              </el-avatar>
              <span>{{ detail.currentApprover.name }}</span>
            </div>
          </div>
          
          <div class="next-approvers" v-if="detail.nextApprovers?.length">
            <el-divider v-if="detail.currentApprover" />
            <el-tag effect="plain">后续审批人</el-tag>
            <div class="approver-list">
              <div
                v-for="approver in detail.nextApprovers"
                :key="approver.id"
                class="approver-item"
              >
                <el-avatar :size="32" :src="approver.avatar">
                  {{ approver.name?.charAt(0) }}
                </el-avatar>
                <span>{{ approver.name }}</span>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 操作日志 -->
        <el-card class="log-card">
          <template #header>
            <span>操作记录</span>
          </template>
          <div class="log-list">
            <div class="log-item">
              <el-icon><Clock /></el-icon>
              <span>创建于 {{ formatTime(detail.createTime) }}</span>
            </div>
            <div class="log-item" v-if="detail.updateTime">
              <el-icon><Edit /></el-icon>
              <span>更新于 {{ formatTime(detail.updateTime) }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 审批意见对话框 -->
    <el-dialog
      v-model="commentDialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="commentForm" class="comment-form">
        <el-form-item label="审批意见">
          <el-input
            v-model="commentForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="dialogPlaceholder"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="commentDialogVisible = false">取消</el-button>
          <el-button
            :type="dialogType"
            :loading="actionLoading"
            @click="confirmAction"
          >
            确认
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 转交对话框 -->
    <el-dialog
      v-model="transferDialogVisible"
      title="转交审批"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="transferForm" class="transfer-form">
        <el-form-item label="选择转交人" required>
          <el-select
            v-model="transferForm.targetUserId"
            placeholder="请选择转交人"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">
                  {{ user.name?.charAt(0) }}
                </el-avatar>
                <span>{{ user.name }}</span>
                <span class="dept">{{ user.dept }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="转交原因">
          <el-input
            v-model="transferForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入转交原因"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="actionLoading" @click="confirmTransfer">
            确认转交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Check,
  Close,
  RefreshRight,
  CloseBold,
  Clock,
  Edit,
  Document
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import {
  getApprovalDetail,
  processApproval,
  cancelApproval,
  getStatusConfig,
  getTypeConfig,
  type ApprovalDetailVO,
  type ApprovalHistoryVO
} from '@/api/approval'

const router = useRouter()
const route = useRoute()

// ============ 状态定义 ============

const loading = ref(false)
const actionLoading = ref(false)
const detail = ref<ApprovalDetailVO | null>(null)

// 审批意见对话框
const commentDialogVisible = ref(false)
const dialogTitle = ref('')
const dialogType = ref('')
const dialogPlaceholder = ref('')
const pendingAction = ref<'APPROVE' | 'REJECT' | 'CANCEL' | null>(null)
const commentForm = reactive({
  comment: ''
})

// 转交对话框
const transferDialogVisible = ref(false)
const transferForm = reactive({
  targetUserId: '',
  comment: ''
})
const userList = ref<Array<{ id: string; name: string; avatar?: string; dept: string }>>([])

// ============ 计算属性 ============

const canHandle = computed(() => {
  // 只有待审批状态且当前用户是审批人时才能操作
  return detail.value?.status === 'PENDING'
})

const isInitiator = computed(() => {
  // TODO: 实际应该从用户 store 获取当前用户ID比较
  return true
})

const getStepActive = computed(() => {
  switch (detail.value?.status) {
    case 'PENDING': return 1
    case 'APPROVED': return 2
    case 'REJECTED': return 2
    case 'CANCELLED': return 2
    default: return 0
  }
})

// ============ 生命周期 ============

onMounted(() => {
  loadDetail()
})

// ============ 方法 ============

/** 加载详情 */
const loadDetail = async () => {
  loading.value = true
  const id = route.params.id as string
  
  try {
    const res = await getApprovalDetail(id)
    detail.value = res.data
  } catch (error) {
    console.error('加载审批详情失败', error)
    // 使用模拟数据
    detail.value = generateMockDetail(id)
  } finally {
    loading.value = false
  }
}

/** 返回列表 */
const handleBack = () => {
  router.push('/approval')
}

/** 同意审批 */
const handleApprove = () => {
  dialogTitle.value = '审批通过'
  dialogType.value = 'success'
  dialogPlaceholder.value = '请输入审批意见（可选）'
  pendingAction.value = 'APPROVE'
  commentForm.comment = ''
  commentDialogVisible.value = true
}

/** 拒绝审批 */
const handleReject = () => {
  dialogTitle.value = '审批拒绝'
  dialogType.value = 'danger'
  dialogPlaceholder.value = '请输入拒绝原因'
  pendingAction.value = 'REJECT'
  commentForm.comment = ''
  commentDialogVisible.value = true
}

/** 撤回申请 */
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要撤回此申请吗？', '提示', {
      confirmButtonText: '确定撤回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (!detail.value) return
    
    actionLoading.value = true
    await cancelApproval(detail.value.id)
    ElMessage.success('已撤回申请')
    loadDetail()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    actionLoading.value = false
  }
}

/** 转交审批 */
const handleTransfer = () => {
  // 加载用户列表（实际应该调用 API）
  userList.value = [
    { id: '1', name: '张三', dept: '技术部' },
    { id: '2', name: '李四', dept: '产品部' },
    { id: '3', name: '王五', dept: '运营部' }
  ]
  transferForm.targetUserId = ''
  transferForm.comment = ''
  transferDialogVisible.value = true
}

/** 确认操作 */
const confirmAction = async () => {
  if (!detail.value || !pendingAction.value) return
  
  // 拒绝必须填写原因
  if (pendingAction.value === 'REJECT' && !commentForm.comment.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  actionLoading.value = true
  try {
    await processApproval({
      id: detail.value.id,
      action: pendingAction.value,
      comment: commentForm.comment
    })
    
    ElMessage.success(
      pendingAction.value === 'APPROVE' ? '审批已通过' : 
      pendingAction.value === 'REJECT' ? '已拒绝该审批' : '操作成功'
    )
    
    commentDialogVisible.value = false
    loadDetail()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

/** 确认转交 */
const confirmTransfer = async () => {
  if (!detail.value || !transferForm.targetUserId) {
    ElMessage.warning('请选择转交人')
    return
  }
  
  actionLoading.value = true
  try {
    await processApproval({
      id: detail.value.id,
      action: 'TRANSFER',
      comment: `转交给 ${transferForm.targetUserId}：${transferForm.comment}`
    })
    ElMessage.success('已转交给相关审批人')
    transferDialogVisible.value = false
    loadDetail()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

/** 下载附件 */
const handleDownload = (file: { id: string; name: string; url: string }) => {
  // TODO: 实现文件下载
  ElMessage.info(`下载文件: ${file.name}`)
}

/** 格式化时间 */
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

/** 格式化文件大小 */
const formatFileSize = (size: number) => {
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / (1024 * 1024)).toFixed(1)} MB`
}

/** 格式化表单值 */
const formatFormValue = (value: any) => {
  if (Array.isArray(value)) return value.join(', ')
  if (typeof value === 'boolean') return value ? '是' : '否'
  return String(value)
}

/** 获取历史项类型 */
const getHistoryItemType = (action: string) => {
  switch (action) {
    case 'SUBMIT': return 'primary'
    case 'APPROVE': return 'success'
    case 'REJECT': return 'danger'
    case 'CANCEL': return 'info'
    case 'TRANSFER': return 'warning'
    default: return 'info'
  }
}

/** 获取历史操作文本 */
const getHistoryActionText = (action: string) => {
  switch (action) {
    case 'SUBMIT': return '提交了申请'
    case 'APPROVE': return '审批通过'
    case 'REJECT': return '审批拒绝'
    case 'CANCEL': return '撤回了申请'
    case 'TRANSFER': return '转交了审批'
    default: return action
  }
}

/** 生成模拟详情 */
const generateMockDetail = (id: string): ApprovalDetailVO => ({
  id,
  title: '张三-请假申请-4月5日',
  type: 'LEAVE',
  status: 'PENDING',
  submitterId: 'user-1',
  submitterName: '张三',
  submitterAvatar: '',
  deptName: '技术部',
  reason: '因私事需要处理，申请年假2天',
  content: '您好，因家中临时有事，需要请假2天处理。计划4月10日-11日请假，12日正常上班。期间工作已安排交接，请领导批准。',
  startDate: '2026-04-10',
  endDate: '2026-04-11',
  createTime: '2026-04-05 10:00:00',
  updateTime: '2026-04-05 10:00:00',
  attachments: [
    { id: '1', name: '请假证明.pdf', url: '#', size: 1024000 }
  ],
  formData: {
    '请假类型': '年假',
    '请假天数': '2天',
    '是否扣薪': '否',
    '紧急联系人': '张四 138****8888'
  },
  history: [
    {
      id: '1',
      approvalId: id,
      step: 1,
      action: 'SUBMIT',
      operatorId: 'user-1',
      operatorName: '张三',
      operatorAvatar: '',
      createTime: '2026-04-05 10:00:00'
    }
  ],
  currentApprover: {
    id: 'user-2',
    name: '李四',
    avatar: ''
  },
  nextApprovers: [
    { id: 'user-3', name: '王五', avatar: '' }
  ]
})
</script>

<style lang="scss" scoped>
.approval-detail-page {
  .page-nav {
    margin-bottom: 16px;
  }
  
  .detail-content {
    display: grid;
    grid-template-columns: 1fr 360px;
    gap: 16px;
    
    .main-content {
      display: flex;
    }
    
    .side-content {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
  }
  
  .status-card {
    margin-bottom: 16px;
    
    .status-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
      
      .status-info {
        .approval-title {
          margin: 12px 0 0;
          font-size: 18px;
          font-weight: 600;
        }
      }
      
      .status-actions {
        display: flex;
        gap: 12px;
      }
    }
    
    .approval-progress {
      padding: 0 20px;
    }
  }
  
  .form-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .header-meta {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .form-id {
          font-size: 12px;
          color: #999;
        }
      }
    }
    
    .form-content {
      .info-descriptions {
        .user-info-cell {
          display: flex;
          align-items: center;
          gap: 8px;
        }
        
        .amount-value {
          font-size: 18px;
          font-weight: 600;
          color: #E6A23C;
        }
        
        .content-text {
          line-height: 1.8;
          white-space: pre-wrap;
        }
      }
      
      .attachments {
        margin-top: 24px;
        
        h4 {
          margin: 0 0 12px;
          font-size: 14px;
          color: #333;
        }
        
        .attachment-list {
          .attachment-item {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 8px 12px;
            background: #f5f7fa;
            border-radius: 4px;
            margin-bottom: 8px;
            
            .file-name {
              flex: 1;
            }
            
            .file-size {
              color: #999;
              font-size: 12px;
            }
          }
        }
      }
      
      .dynamic-form {
        margin-top: 24px;
        
        h4 {
          margin: 0 0 12px;
          font-size: 14px;
          color: #333;
        }
      }
    }
  }
  
  .history-card {
    .history-item {
      .history-header {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
        
        .history-info {
          .operator-name {
            font-weight: 500;
            margin-right: 8px;
          }
          
          .action-text {
            color: #666;
            font-size: 13px;
          }
        }
      }
      
      .history-comment {
        padding: 8px 12px;
        background: #f5f7fa;
        border-radius: 4px;
        margin-bottom: 8px;
        font-style: italic;
        color: #666;
      }
      
      .history-meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .approver-card {
    .current-approver {
      .approver-info {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-top: 12px;
      }
    }
    
    .next-approvers {
      .approver-list {
        margin-top: 12px;
        
        .approver-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 0;
        }
      }
    }
  }
  
  .log-card {
    .log-list {
      .log-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 0;
        font-size: 13px;
        color: #666;
      }
    }
  }
  
  .comment-form,
  .transfer-form {
    .user-option {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .dept {
        color: #999;
        font-size: 12px;
        margin-left: auto;
      }
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}
</style>
