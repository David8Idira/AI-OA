<template>
  <div class="home-page">
    <!-- Header -->
    <div class="page-header">
      <div class="greeting">
        <h1>{{ greeting }}，{{ userStore.nickname }}</h1>
        <p>{{ currentDate }}</p>
      </div>
    </div>
    
    <!-- Quick Actions -->
    <el-row :gutter="16" class="quick-actions">
      <el-col :span="6">
        <div class="action-card" @click="$router.push('/approval')">
          <el-icon class="action-icon" color="#667eea"><Document /></el-icon>
          <span>提交审批</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="action-card" @click="$router.push('/reimburse?action=scan')">
          <el-icon class="action-icon" color="#10b981"><Camera /></el-icon>
          <span>拍照识别</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="action-card" @click="$router.push('/ai')">
          <el-icon class="action-icon" color="#a855f7"><ChatDotRound /></el-icon>
          <span>AI助手</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="action-card" @click="$router.push('/report')">
          <el-icon class="action-icon" color="#f59e0b"><DataLine /></el-icon>
          <span>生成报表</span>
        </div>
      </el-col>
    </el-row>
    
    <!-- Main Content -->
    <el-row :gutter="16">
      <!-- Left Column -->
      <el-col :span="16">
        <!-- Pending Tasks -->
        <el-card class="content-card">
          <template #header>
            <div class="card-header">
              <span>待办事项</span>
              <el-link type="primary" @click="$router.push('/approval')">查看更多</el-link>
            </div>
          </template>
          
          <div v-if="pendingList.length === 0" class="empty-state">
            <el-icon :size="48"><SuccessFilled /></el-icon>
            <p>太棒了！暂无待处理事项</p>
          </div>
          
          <div v-else class="task-list">
            <div v-for="item in pendingList" :key="item.id" class="task-item">
              <div class="task-type">
                <el-tag :type="getTypeColor(item.type)">{{ item.typeName }}</el-tag>
              </div>
              <div class="task-content">
                <span class="task-title">{{ item.title }}</span>
                <span class="task-submitter">{{ item.submitter }}</span>
              </div>
              <div class="task-time">{{ item.createTime }}</div>
              <el-button type="primary" size="small" @click="handleApprove(item)">
                立即审批
              </el-button>
            </div>
          </div>
        </el-card>
        
        <!-- Recent Activities -->
        <el-card class="content-card">
          <template #header>
            <span>最近动态</span>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="activity in activities"
              :key="activity.id"
              :timestamp="activity.createTime"
              placement="top"
            >
              <el-card>
                <h4>{{ activity.title }}</h4>
                <p>{{ activity.content }}</p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      
      <!-- Right Column -->
      <el-col :span="8">
        <!-- Statistics -->
        <el-card class="content-card">
          <template #header>
            <span>本月统计</span>
          </template>
          
          <div class="statistics">
            <div class="stat-item">
              <div class="stat-value">{{ stats.pendingApprovals }}</div>
              <div class="stat-label">待审批</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.pendingReimburses }}</div>
              <div class="stat-label">待报销</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalExpenses }}</div>
              <div class="stat-label">本月支出(元)</div>
            </div>
          </div>
        </el-card>
        
        <!-- AI Usage -->
        <el-card class="content-card">
          <template #header>
            <span>AI使用量</span>
          </template>
          
          <div class="ai-stats">
            <div class="ai-stat-item">
              <span class="label">今日调用</span>
              <span class="value">{{ aiStats.todayCalls }}</span>
            </div>
            <div class="ai-stat-item">
              <span class="label">本月调用</span>
              <span class="value">{{ aiStats.monthCalls }}</span>
            </div>
            <el-progress
              :percentage="aiStats.usagePercent"
              :stroke-width="8"
              :color="aiStats.usagePercent > 80 ? '#ef4444' : '#667eea'"
            />
            <span class="usage-hint">剩余 {{ aiStats.remainingCalls }} 次</span>
          </div>
        </el-card>
        
        <!-- Shortcuts -->
        <el-card class="content-card">
          <template #header>
            <span>常用功能</span>
          </template>
          
          <div class="shortcuts">
            <div class="shortcut-item" @click="$router.push('/knowledge')">
              <el-icon><Notebook /></el-icon>
              <span>知识库</span>
            </div>
            <div class="shortcut-item" @click="$router.push('/chat')">
              <el-icon><Message /></el-icon>
              <span>企业聊天</span>
            </div>
            <div class="shortcut-item" @click="$router.push('/settings')">
              <el-icon><Setting /></el-icon>
              <span>系统设置</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import dayjs from 'dayjs'

const userStore = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  return dayjs().format('dddd · YYYY年MM月DD日')
})

// Mock data
const pendingList = ref([
  {
    id: '1',
    type: 'LEAVE',
    typeName: '请假申请',
    title: '张三-请假申请-4月5日',
    submitter: '张三',
    createTime: '10:00'
  },
  {
    id: '2',
    type: 'EXPENSE',
    typeName: '差旅报销',
    title: '李四-杭州出差报销',
    submitter: '李四',
    createTime: '09:30'
  }
])

const activities = ref([
  {
    id: '1',
    title: '提交请假申请',
    content: '您提交了年假申请，等待审批',
    createTime: '2026-04-05 10:00'
  },
  {
    id: '2',
    title: '审批通过',
    content: '您的采购申请已通过审批',
    createTime: '2026-04-04 17:30'
  }
])

const stats = ref({
  pendingApprovals: 3,
  pendingReimburses: 2,
  totalExpenses: 12345
})

const aiStats = ref({
  todayCalls: 12,
  monthCalls: 156,
  usagePercent: 45,
  remainingCalls: 8444
})

const getTypeColor = (type: string) => {
  const map: Record<string, string> = {
    LEAVE: 'warning',
    EXPENSE: 'primary',
    PURCHASE: 'success',
    PAYMENT: 'danger'
  }
  return map[type] || 'info'
}

const handleApprove = (item: any) => {
  console.log('Approve', item)
}
</script>

<style lang="scss" scoped>
.home-page {
  .page-header {
    margin-bottom: 24px;
    
    .greeting {
      h1 {
        font-size: 24px;
        margin-bottom: 8px;
      }
      
      p {
        color: #999;
      }
    }
  }
  
  .quick-actions {
    margin-bottom: 24px;
    
    .action-card {
      height: 100px;
      background: #fff;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 12px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
      }
      
      .action-icon {
        font-size: 32px;
      }
      
      span {
        font-size: 14px;
        color: #333;
      }
    }
  }
  
  .content-card {
    margin-bottom: 16px;
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .empty-state {
      text-align: center;
      padding: 40px;
      color: #999;
      
      p {
        margin-top: 12px;
      }
    }
    
    .task-list {
      .task-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid #f0f0f0;
        
        &:last-child {
          border-bottom: none;
        }
        
        .task-type {
          margin-right: 12px;
        }
        
        .task-content {
          flex: 1;
          
          .task-title {
            display: block;
            font-weight: 500;
          }
          
          .task-submitter {
            font-size: 12px;
            color: #999;
          }
        }
        
        .task-time {
          margin-right: 12px;
          color: #999;
          font-size: 12px;
        }
      }
    }
    
    .statistics {
      display: flex;
      justify-content: space-around;
      
      .stat-item {
        text-align: center;
        
        .stat-value {
          font-size: 28px;
          font-weight: bold;
          color: #667eea;
        }
        
        .stat-label {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }
    }
    
    .ai-stats {
      .ai-stat-item {
        display: flex;
        justify-content: space-between;
        margin-bottom: 12px;
        
        .label {
          color: #999;
        }
        
        .value {
          font-weight: bold;
        }
      }
      
      .usage-hint {
        display: block;
        text-align: center;
        margin-top: 8px;
        color: #999;
        font-size: 12px;
      }
    }
    
    .shortcuts {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 12px;
      
      .shortcut-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px;
        background: #f5f7fa;
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #e8ecf1;
        }
        
        .el-icon {
          font-size: 24px;
          margin-bottom: 8px;
        }
        
        span {
          font-size: 12px;
          color: #666;
        }
      }
    }
  }
}
</style>
