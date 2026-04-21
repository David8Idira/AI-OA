<template>
  <div class="approval-list-page">
    <!-- 页面标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <h2>审批中心</h2>
        <el-tag v-if="pendingCount > 0" type="danger" size="small" class="pending-tag">
          {{ pendingCount }} 条待处理
        </el-tag>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建审批
        </el-button>
      </div>
    </div>

    <!-- Tabs 切换 -->
    <el-tabs v-model="activeTab" class="approval-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane name="pending">
        <template #label>
          <span>待审批</span>
          <el-badge v-if="stats.pending > 0" :value="stats.pending" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
      <el-tab-pane name="processed">
        <template #label>
          <span>已处理</span>
          <el-badge v-if="stats.approved + stats.rejected > 0" 
            :value="stats.approved + stats.rejected" 
            :max="99" 
            class="tab-badge"
            :type="stats.rejected > 0 ? 'danger' : 'primary'" 
          />
        </template>
      </el-tab-pane>
      <el-tab-pane name="initiated">
        <template #label>
          <span>我发起</span>
          <el-badge v-if="stats.initiated > 0" :value="stats.initiated" :max="99" class="tab-badge" />
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="审批类型">
          <el-select v-model="filterForm.type" placeholder="全部类型" clearable style="width: 140px">
            <el-option
              v-for="(item, key) in APPROVAL_TYPE_MAP"
              :key="key"
              :label="item.label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="activeTab === 'all' || activeTab === 'processed'" label="审批状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option
              v-for="(item, key) in APPROVAL_STATUS_MAP"
              :key="key"
              :label="item.label"
              :value="key"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="提交日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        
        <el-form-item label="关键词">
          <el-input
            v-model="filterForm.keyword"
            placeholder="搜索标题/申请人"
            clearable
            style="width: 180px"
            @keyup.enter="handleFilter"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 审批列表 -->
    <div class="approval-list" v-loading="loading">
      <!-- 空状态 -->
      <el-empty v-if="!loading && list.length === 0" description="暂无审批记录">
        <el-button type="primary" @click="handleCreate">新建审批</el-button>
      </el-empty>

      <!-- 审批卡片列表 -->
      <div v-else class="card-grid">
        <el-card
          v-for="item in list"
          :key="item.id"
          class="approval-card"
          shadow="hover"
          @click="handleDetail(item)"
        >
          <!-- 卡片头部 -->
          <div class="card-header">
            <div class="card-type">
              <el-icon :style="{ color: getTypeConfig(item.type).color }">
                <component :is="getTypeConfig(item.type).icon" />
              </el-icon>
              <span>{{ getTypeConfig(item.type).label }}</span>
            </div>
            <el-tag :type="getStatusConfig(item.status).type" size="small">
              {{ getStatusConfig(item.status).label }}
            </el-tag>
          </div>

          <!-- 卡片内容 -->
          <div class="card-body">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-reason">{{ item.reason }}</p>
            
            <!-- 日期范围或金额 -->
            <div class="card-meta" v-if="item.startDate || item.amount">
              <template v-if="item.startDate">
                <el-icon><Calendar /></el-icon>
                <span>{{ item.startDate }} ~ {{ item.endDate || '进行中' }}</span>
              </template>
              <template v-if="item.amount">
                <el-icon><Coin /></el-icon>
                <span class="amount">¥{{ item.amount.toLocaleString() }}</span>
              </template>
            </div>
          </div>

          <!-- 卡片底部 -->
          <div class="card-footer">
            <div class="submitter-info">
              <el-avatar :size="24" :src="item.submitterAvatar">
                {{ item.submitterName?.charAt(0) }}
              </el-avatar>
              <span class="submitter-name">{{ item.submitterName }}</span>
              <span class="dept-name" v-if="item.deptName">{{ item.deptName }}</span>
            </div>
            <div class="time-info">
              <el-icon><Clock /></el-icon>
              <span>{{ formatApprovalTime(item.createTime) }}</span>
            </div>
          </div>

          <!-- 进度指示 -->
          <div class="card-progress" v-if="item.currentStep && item.totalSteps">
            <el-progress
              :percentage="Math.round((item.currentStep / item.totalSteps) * 100)"
              :stroke-width="4"
              :show-text="false"
            />
            <span class="progress-text">第{{ item.currentStep }}级 / 共{{ item.totalSteps }}级</span>
          </div>
        </el-card>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 快捷审批对话框 -->
    <el-dialog
      v-model="quickActionVisible"
      title="快速审批"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="quick-action-content" v-if="currentApproval">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请人">
            {{ currentApproval.submitterName }}
          </el-descriptions-item>
          <el-descriptions-item label="审批类型">
            {{ getTypeConfig(currentApproval.type).label }}
          </el-descriptions-item>
          <el-descriptions-item label="申请标题">
            {{ currentApproval.title }}
          </el-descriptions-item>
          <el-descriptions-item label="申请理由">
            {{ currentApproval.reason }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-form class="action-form" :model="actionForm">
          <el-form-item label="审批意见">
            <el-input
              v-model="actionForm.comment"
              type="textarea"
              :rows="3"
              placeholder="请输入审批意见（可选）"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="quickActionVisible = false">取消</el-button>
          <el-button type="danger" @click="handleReject" :loading="actionLoading">
            拒绝
          </el-button>
          <el-button type="success" @click="handleApprove" :loading="actionLoading">
            同意
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Search,
  Refresh,
  Calendar,
  Clock,
  Coin
} from '@element-plus/icons-vue'
import {
  getApprovalList,
  getApprovalStats,
  processApproval,
  cancelApproval,
  formatApprovalTime,
  getStatusConfig,
  getTypeConfig,
  APPROVAL_STATUS_MAP,
  APPROVAL_TYPE_MAP,
  type ApprovalVO,
  type ApprovalQueryDTO,
  type ApprovalStatus,
  type ApprovalType
} from '@/api/approval'

const router = useRouter()

// ============ 状态定义 ============

const loading = ref(false)
const actionLoading = ref(false)
const activeTab = ref<'all' | 'pending' | 'processed' | 'initiated'>('all')
const list = ref<ApprovalVO[]>([])
const total = ref(0)
const pendingCount = ref(0)

const stats = ref({
  pending: 0,
  approved: 0,
  rejected: 0,
  initiated: 0
})

const pagination = reactive({
  page: 1,
  pageSize: 10
})

const filterForm = reactive({
  type: '' as ApprovalType | '',
  status: '' as ApprovalStatus | '',
  keyword: ''
})

const dateRange = ref<[string, string] | null>(null)

// 快速审批
const quickActionVisible = ref(false)
const currentApproval = ref<ApprovalVO | null>(null)
const actionForm = reactive({
  comment: ''
})

// ============ 生命周期 ============

onMounted(() => {
  loadStats()
  loadList()
  loadPendingCount()
})

// ============ 方法 ============

/** 加载统计数据 */
const loadStats = async () => {
  try {
    const res = await getApprovalStats()
    stats.value = res.data
  } catch (error) {
    console.error('加载统计失败', error)
  }
}

/** 加载待审批数量 */
const loadPendingCount = async () => {
  try {
    const res = await getPendingCount()
    pendingCount.value = res.data
  } catch (error) {
    console.error('加载待审批数量失败', error)
  }
}

/** 加载列表数据 */
const loadList = async () => {
  loading.value = true
  try {
    const params: ApprovalQueryDTO = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      tab: activeTab.value,
      keyword: filterForm.keyword || undefined,
      type: filterForm.type || undefined,
      status: filterForm.status || undefined,
      startDate: dateRange.value?.[0] || undefined,
      endDate: dateRange.value?.[1] || undefined
    }

    const res = await getApprovalList(params)
    list.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error('加载审批列表失败', error)
    // 使用模拟数据
    list.value = generateMockList()
    total.value = 25
  } finally {
    loading.value = false
  }
}

/** Tab 切换 */
const handleTabChange = () => {
  pagination.page = 1
  // 切换时清空状态筛选（已处理 tab 显示全部状态）
  if (activeTab.value !== 'processed' && activeTab.value !== 'all') {
    filterForm.status = ''
  }
  loadList()
}

/** 筛选 */
const handleFilter = () => {
  pagination.page = 1
  loadList()
}

/** 重置筛选 */
const handleReset = () => {
  filterForm.type = ''
  filterForm.status = ''
  filterForm.keyword = ''
  dateRange.value = null
  handleFilter()
}

/** 分页大小变化 */
const handleSizeChange = () => {
  pagination.page = 1
  loadList()
}

/** 页码变化 */
const handlePageChange = () => {
  loadList()
}

/** 查看详情 */
const handleDetail = (item: ApprovalVO) => {
  router.push(`/approval/detail/${item.id}`)
}

/** 新建审批 */
const handleCreate = () => {
  router.push('/approval/form')
}

/** 快捷审批 - 同意 */
const handleApprove = async () => {
  if (!currentApproval.value) return
  
  actionLoading.value = true
  try {
    await processApproval({
      id: currentApproval.value.id,
      action: 'APPROVE',
      comment: actionForm.comment
    })
    ElMessage.success('审批已通过')
    quickActionVisible.value = false
    loadList()
    loadStats()
    loadPendingCount()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    actionLoading.value = false
  }
}

/** 快捷审批 - 拒绝 */
const handleReject = async () => {
  if (!currentApproval.value) return
  
  try {
    await ElMessageBox.confirm('确定要拒绝此审批吗？', '提示', {
      confirmButtonText: '确定拒绝',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    actionLoading.value = true
    await processApproval({
      id: currentApproval.value.id,
      action: 'REJECT',
      comment: actionForm.comment
    })
    ElMessage.success('已拒绝该审批')
    quickActionVisible.value = false
    loadList()
    loadStats()
    loadPendingCount()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  } finally {
    actionLoading.value = false
  }
}

/** 生成模拟数据 */
const generateMockList = (): ApprovalVO[] => {
  const types: ApprovalType[] = ['LEAVE', 'EXPENSE', 'PURCHASE', 'TRAVEL', 'OVERTIME']
  const statuses: ApprovalStatus[] = ['PENDING', 'APPROVED', 'REJECTED', 'CANCELLED']
  const names = ['张三', '李四', '王五', '赵六', '钱七']
  const reasons = [
    '因私事需要处理，申请年假2天',
    '项目采购办公设备一批',
    '杭州出差，拜访客户',
    '周末加班处理紧急需求',
    '购买开发所需软件 license'
  ]
  
  return Array.from({ length: 10 }, (_, i) => ({
    id: `approval-${i + 1}`,
    title: `${names[i % names.length]}-${['请假', '报销', '采购', '差旅', '加班'][i % 5]}申请`,
    type: types[i % types.length],
    status: statuses[i % statuses.length],
    submitterId: `user-${i}`,
    submitterName: names[i % names.length],
    submitterAvatar: '',
    deptName: '技术部',
    reason: reasons[i % reasons.length],
    amount: i % 3 === 1 ? Math.floor(Math.random() * 10000) + 1000 : undefined,
    startDate: i % 2 === 0 ? '2026-04-10' : undefined,
    endDate: i % 2 === 0 ? '2026-04-12' : undefined,
    createTime: new Date(Date.now() - i * 3600000).toISOString(),
    commentCount: Math.floor(Math.random() * 5),
    currentStep: i % 2 === 0 ? 2 : undefined,
    totalSteps: i % 2 === 0 ? 3 : undefined
  }))
}
</script>

<style lang="scss" scoped>
.approval-list-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 12px;
      
      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
      }
      
      .pending-tag {
        animation: pulse 2s infinite;
      }
    }
  }
  
  .approval-tabs {
    margin-bottom: 16px;
    
    .tab-badge {
      margin-left: 4px;
    }
  }
  
  .filter-card {
    margin-bottom: 16px;
    
    .filter-form {
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }
  
  .approval-list {
    .card-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
      gap: 16px;
      margin-bottom: 16px;
    }
    
    .approval-card {
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }
      
      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 12px;
        
        .card-type {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          
          .el-icon {
            font-size: 18px;
          }
        }
      }
      
      .card-body {
        margin-bottom: 12px;
        
        .card-title {
          margin: 0 0 8px;
          font-size: 15px;
          font-weight: 500;
          color: #333;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        
        .card-reason {
          margin: 0;
          font-size: 13px;
          color: #666;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          line-height: 1.6;
        }
        
        .card-meta {
          display: flex;
          gap: 16px;
          margin-top: 12px;
          font-size: 12px;
          color: #999;
          
          .amount {
            color: #E6A23C;
            font-weight: 500;
          }
          
          .el-icon {
            margin-right: 4px;
          }
        }
      }
      
      .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding-top: 12px;
        border-top: 1px solid #f0f0f0;
        
        .submitter-info {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .submitter-name {
            font-size: 13px;
            color: #333;
          }
          
          .dept-name {
            font-size: 12px;
            color: #999;
            padding: 2px 6px;
            background: #f5f7fa;
            border-radius: 4px;
          }
        }
        
        .time-info {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #999;
        }
      }
      
      .card-progress {
        margin-top: 12px;
        
        .progress-text {
          display: block;
          text-align: right;
          font-size: 11px;
          color: #999;
          margin-top: 4px;
        }
      }
    }
    
    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }
  }
  
  .quick-action-content {
    .action-form {
      margin-top: 16px;
    }
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>
