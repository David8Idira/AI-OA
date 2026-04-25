<template>
  <div class="statistics-container">
    <div class="header">
      <h2 class="title">考勤统计分析</h2>
      <div class="header-actions">
        <el-button type="primary" @click="exportReport" :loading="exportLoading">
          <el-icon><Download /></el-icon>
          导出报表
        </el-button>
      </div>
    </div>

    <!-- Filter Section -->
    <div class="filter-card">
      <el-form :model="filterParams" inline>
        <el-form-item label="统计周期">
          <el-select v-model="filterParams.period" @change="handlePeriodChange">
            <el-option label="本月" value="current_month" />
            <el-option label="上月" value="last_month" />
            <el-option label="本季度" value="current_quarter" />
            <el-option label="上季度" value="last_quarter" />
            <el-option label="本年" value="current_year" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>

        <el-form-item v-if="filterParams.period === 'custom'" label="开始日期">
          <el-date-picker
            v-model="filterParams.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item v-if="filterParams.period === 'custom'" label="结束日期">
          <el-date-picker
            v-model="filterParams.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="部门" v-if="hasPermission('attendance:view:department')">
          <el-select v-model="filterParams.deptId" placeholder="选择部门" clearable>
            <el-option label="技术部" value="tech" />
            <el-option label="市场部" value="marketing" />
            <el-option label="人事部" value="hr" />
            <el-option label="财务部" value="finance" />
            <el-option label="全部" value="" />
          </el-select>
        </el-form-item>

        <el-form-item label="员工" v-if="filterParams.deptId || !hasPermission('attendance:view:department')">
          <el-select
            v-model="filterParams.userId"
            placeholder="选择员工"
            filterable
            clearable
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadStatistics" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Summary Cards -->
    <div class="summary-cards">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6" v-for="card in summaryCards" :key="card.label">
          <div class="summary-card" :style="{ borderLeftColor: card.color }">
            <div class="card-icon" :style="{ background: card.color }">
              <component :is="card.icon" />
            </div>
            <div class="card-content">
              <div class="card-value">{{ card.value }}</div>
              <div class="card-label">{{ card.label }}</div>
              <div class="card-trend" v-if="card.trend !== undefined">
                <el-icon :class="card.trend >= 0 ? 'up' : 'down'">
                  <ArrowUp v-if="card.trend >= 0" />
                  <ArrowDown v-else />
                </el-icon>
                <span>{{ Math.abs(card.trend) }}%</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <el-row :gutter="20">
        <!-- Attendance Rate Chart -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">考勤率趋势</h3>
              <div class="chart-actions">
                <el-radio-group v-model="trendChartType" size="small">
                  <el-radio-button label="line">折线图</el-radio-button>
                  <el-radio-button label="bar">柱状图</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <div class="chart-container" ref="trendChartRef"></div>
          </div>
        </el-col>

        <!-- Status Distribution Chart -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">考勤状态分布</h3>
            </div>
            <div class="chart-container" ref="distributionChartRef"></div>
          </div>
        </el-col>

        <!-- Department Comparison -->
        <el-col :xs="24" :lg="12" v-if="hasPermission('attendance:view:department')">
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">部门考勤对比</h3>
            </div>
            <div class="chart-container" ref="departmentChartRef"></div>
          </div>
        </el-col>

        <!-- Top Performers -->
        <el-col :xs="24" :lg="12">
          <div class="chart-card">
            <div class="chart-header">
              <h3 class="chart-title">考勤标兵</h3>
              <div class="chart-actions">
                <el-select v-model="topRankType" size="small" style="width: 100px">
                  <el-option label="正常率" value="normal_rate" />
                  <el-option label="考勤分" value="attendance_score" />
                </el-select>
              </div>
            </div>
            <div class="top-list">
              <div
                v-for="(user, index) in topPerformers"
                :key="user.id"
                class="top-item"
              >
                <div class="rank">
                  <div class="rank-number" :class="getRankClass(index)">
                    {{ index + 1 }}
                  </div>
                </div>
                <div class="user-info">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-dept">{{ user.dept }}</div>
                </div>
                <div class="user-stats">
                  <div class="stat-item">
                    <span class="stat-label">正常率</span>
                    <span class="stat-value">{{ user.normalRate }}%</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">考勤分</span>
                    <span class="stat-value">{{ user.attendanceScore }}</span>
                  </div>
                </div>
                <div class="user-actions">
                  <el-button type="primary" link @click="viewUserDetail(user.id)">
                    详情
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- Detailed Statistics -->
    <div class="detail-stats">
      <div class="section-header">
        <h3 class="section-title">详细统计</h3>
        <div class="section-actions">
          <el-button type="primary" link @click="toggleDetailStats">
            {{ showDetailStats ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>
      
      <div v-if="showDetailStats" class="detail-content">
        <!-- Monthly Statistics Table -->
        <div class="monthly-table">
          <h4>月度统计表</h4>
          <el-table :data="monthlyStats" stripe style="width: 100%">
            <el-table-column prop="month" label="月份" width="100" />
            <el-table-column label="出勤天数" width="100">
              <template #default="{ row }">
                {{ row.totalDays - row.absentDays }}
              </template>
            </el-table-column>
            <el-table-column prop="normalDays" label="正常天数" width="100" />
            <el-table-column prop="lateDays" label="迟到天数" width="100" />
            <el-table-column prop="leaveEarlyDays" label="早退天数" width="100" />
            <el-table-column prop="absentDays" label="缺勤天数" width="100" />
            <el-table-column prop="overtimeDays" label="加班天数" width="100" />
            <el-table-column label="正常率" width="100">
              <template #default="{ row }">
                {{ ((row.normalDays / row.totalDays) * 100).toFixed(1) }}%
              </template>
            </el-table-column>
            <el-table-column label="总工时" width="100">
              <template #default="{ row }">
                {{ row.totalWorkHours.toFixed(1) }}h
              </template>
            </el-table-column>
            <el-table-column label="平均工时" width="100">
              <template #default="{ row }">
                {{ (row.totalWorkHours / row.workingDays).toFixed(1) }}h
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- Abnormal Analysis -->
        <div class="abnormal-analysis">
          <h4>异常分析</h4>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12">
              <div class="abnormal-card">
                <h5>迟到分析</h5>
                <div class="abnormal-stats">
                  <div class="stat-item">
                    <span class="stat-label">总次数</span>
                    <span class="stat-value">{{ abnormalStats.late.total }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">平均时长</span>
                    <span class="stat-value">{{ abnormalStats.late.avgMinutes }}分钟</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">最常时段</span>
                    <span class="stat-value">{{ abnormalStats.late.mostCommonTime }}</span>
                  </div>
                </div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12">
              <div class="abnormal-card">
                <h5>早退分析</h5>
                <div class="abnormal-stats">
                  <div class="stat-item">
                    <span class="stat-label">总次数</span>
                    <span class="stat-value">{{ abnormalStats.leaveEarly.total }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">平均时长</span>
                    <span class="stat-value">{{ abnormalStats.leaveEarly.avgMinutes }}分钟</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">最常时段</span>
                    <span class="stat-value">{{ abnormalStats.leaveEarly.mostCommonTime }}</span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- Overtime Analysis -->
        <div class="overtime-analysis">
          <h4>加班分析</h4>
          <el-row :gutter="20">
            <el-col :xs="24" :sm="8">
              <div class="overtime-card">
                <div class="overtime-value">{{ overtimeStats.totalHours.toFixed(1) }}</div>
                <div class="overtime-label">总加班时长(小时)</div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="overtime-card">
                <div class="overtime-value">{{ overtimeStats.avgHoursPerDay.toFixed(1) }}</div>
                <div class="overtime-label">日均加班</div>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="overtime-card">
                <div class="overtime-value">{{ overtimeStats.mostCommonDay }}</div>
                <div class="overtime-label">加班最多星期</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>

    <!-- Export Dialog -->
    <el-dialog
      v-model="exportDialogVisible"
      title="导出报表"
      width="400px"
    >
      <el-form :model="exportForm" label-width="100px">
        <el-form-item label="导出格式">
          <el-select v-model="exportForm.format">
            <el-option label="Excel" value="excel" />
            <el-option label="PDF" value="pdf" />
            <el-option label="CSV" value="csv" />
          </el-select>
        </el-form-item>
        <el-form-item label="包含图表">
          <el-switch v-model="exportForm.includeCharts" />
        </el-form-item>
        <el-form-item label="包含明细">
          <el-switch v-model="exportForm.includeDetails" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmExport" :loading="exportLoading">
            确认导出
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, Download, User, Clock, Check, Warning, 
  ArrowUp, ArrowDown 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { format, subMonths, startOfMonth, endOfMonth } from 'date-fns'
import { 
  getAttendanceStatistics,
  getDepartmentReport,
  exportStatisticsReport 
} from '@/api/attendance'

// Reactive data
const loading = ref(false)
const exportLoading = ref(false)
const showDetailStats = ref(false)
const exportDialogVisible = ref(false)

// Chart refs
const trendChartRef = ref<HTMLElement>()
const distributionChartRef = ref<HTMLElement>()
const departmentChartRef = ref<HTMLElement>()

// Chart instances
let trendChart: echarts.ECharts | null = null
let distributionChart: echarts.ECharts | null = null
let departmentChart: echarts.ECharts | null = null

// Filter parameters
const filterParams = reactive({
  period: 'current_month',
  startDate: '',
  endDate: '',
  deptId: '',
  userId: ''
})

// Export form
const exportForm = reactive({
  format: 'excel',
  includeCharts: true,
  includeDetails: true
})

// Chart types
const trendChartType = ref('line')
const topRankType = ref('normal_rate')

// Mock data
const userList = ref([
  { id: 'user1', name: '张三', dept: '技术部' },
  { id: 'user2', name: '李四', dept: '市场部' },
  { id: 'user3', name: '王五', dept: '人事部' },
  { id: 'user4', name: '赵六', dept: '财务部' }
])

// Summary statistics
const summaryStats = reactive({
  totalDays: 22,
  workingDays: 20,
  normalDays: 18,
  lateDays: 2,
  leaveEarlyDays: 1,
  absentDays: 1,
  overtimeDays: 5,
  totalWorkHours: 160.5,
  attendanceScore: 92.5,
  normalRate: 90.0
})

// Summary cards
const summaryCards = computed(() => [
  {
    icon: Check,
    value: `${summaryStats.normalRate}%`,
    label: '考勤正常率',
    color: '#67c23a',
    trend: 2.5
  },
  {
    icon: Clock,
    value: summaryStats.totalWorkHours,
    label: '总工作时长(小时)',
    color: '#409eff',
    trend: 5.2
  },
  {
    icon: Warning,
    value: summaryStats.lateDays + summaryStats.leaveEarlyDays,
    label: '异常天数',
    color: '#e6a23c',
    trend: -1.8
  },
  {
    icon: User,
    value: summaryStats.attendanceScore,
    label: '考勤得分',
    color: '#f56c6c',
    trend: 0.8
  }
])

// Top performers
const topPerformers = ref([
  { id: 'user1', name: '张三', dept: '技术部', normalRate: 98.5, attendanceScore: 99 },
  { id: 'user2', name: '李四', dept: '市场部', normalRate: 96.2, attendanceScore: 97 },
  { id: 'user3', name: '王五', dept: '人事部', normalRate: 95.8, attendanceScore: 96 },
  { id: 'user4', name: '赵六', dept: '财务部', normalRate: 94.3, attendanceScore: 95 },
  { id: 'user5', name: '钱七', dept: '技术部', normalRate: 93.7, attendanceScore: 94 }
])

// Monthly stats
const monthlyStats = ref([
  { month: '2024-01', totalDays: 22, workingDays: 20, normalDays: 18, lateDays: 2, leaveEarlyDays: 1, absentDays: 1, overtimeDays: 5, totalWorkHours: 160.5 },
  { month: '2024-02', totalDays: 20, workingDays: 18, normalDays: 17, lateDays: 1, leaveEarlyDays: 2, absentDays: 1, overtimeDays: 4, totalWorkHours: 144.0 },
  { month: '2024-03', totalDays: 22, workingDays: 21, normalDays: 20, lateDays: 1, leaveEarlyDays: 0, absentDays: 1, overtimeDays: 6, totalWorkHours: 168.0 }
])

// Abnormal stats
const abnormalStats = reactive({
  late: {
    total: 12,
    avgMinutes: 15,
    mostCommonTime: '09:10-09:20'
  },
  leaveEarly: {
    total: 8,
    avgMinutes: 25,
    mostCommonTime: '17:30-17:45'
  }
})

// Overtime stats
const overtimeStats = reactive({
  totalHours: 45.5,
  avgHoursPerDay: 2.3,
  mostCommonDay: '星期四'
})

// Methods
const getRankClass = (index: number) => {
  switch (index) {
    case 0: return 'rank-gold'
    case 1: return 'rank-silver'
    case 2: return 'rank-bronze'
    default: return 'rank-other'
  }
}

const handlePeriodChange = (period: string) => {
  const now = new Date()
  
  switch (period) {
    case 'current_month':
      filterParams.startDate = format(startOfMonth(now), 'yyyy-MM-dd')
      filterParams.endDate = format(endOfMonth(now), 'yyyy-MM-dd')
      break
    case 'last_month':
      const lastMonth = subMonths(now, 1)
      filterParams.startDate = format(startOfMonth(lastMonth), 'yyyy-MM-dd')
      filterParams.endDate = format(endOfMonth(lastMonth), 'yyyy-MM-dd')
      break
    case 'custom':
      filterParams.startDate = ''
      filterParams.endDate = ''
      break
    default:
      // For other periods, set default dates
      filterParams.startDate = format(subMonths(now, 3), 'yyyy-MM-dd')
      filterParams.endDate = format(now, 'yyyy-MM-dd')
  }
}

const loadStatistics = async () => {
  try {
    loading.value = true
    
    // Load statistics data
    if (filterParams.userId) {
      await loadUserStatistics()
    } else if (filterParams.deptId) {
      await loadDepartmentStatistics()
    } else {
      await loadOverallStatistics()
    }
    
    // Initialize charts
    setTimeout(() => {
      initCharts()
    }, 100)
    
  } catch (error) {
    console.error('加载统计失败:', error)
    ElMessage.error('加载统计失败')
  } finally {
    loading.value = false
  }
}

const loadUserStatistics = async () => {
  // Mock API call
  console.log('Loading user statistics for:', filterParams.userId)
  // In real implementation, call API
}

const loadDepartmentStatistics = async () => {
  // Mock API call
  console.log('Loading department statistics for:', filterParams.deptId)
  // In real implementation, call API
}

const loadOverallStatistics = async () => {
  // Mock API call
  console.log('Loading overall statistics')
  // In real implementation, call API
}

const resetFilter = () => {
  filterParams.period = 'current_month'
  filterParams.deptId = ''
  filterParams.userId = ''
  handlePeriodChange('current_month')
  loadStatistics()
}

const toggleDetailStats = () => {
  showDetailStats.value = !showDetailStats.value
}

const viewUserDetail = (userId: string) => {
  console.log('View user detail:', userId)
  // Navigate to user detail page
}

const exportReport = () => {
  exportDialogVisible.value = true
}

const confirmExport = async () => {
  try {
    exportLoading.value = true
    
    // Prepare export parameters
    const exportParams = {
      ...filterParams,
      ...exportForm
    }
    
    // Call export API
    const response = await exportStatisticsReport(exportParams)
    
    if (response.code === 200) {
      // Handle export file
      const blob = new Blob([response.data], { 
        type: exportForm.format === 'excel' 
          ? 'application/vnd.ms-excel' 
          : exportForm.format === 'pdf'
          ? 'application/pdf'
          : 'text/csv'
      })
      
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `考勤统计_${format(new Date(), 'yyyyMMddHHmmss')}.${exportForm.format}`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      
      ElMessage.success('导出成功')
      exportDialogVisible.value = false
    } else {
      ElMessage.error(response.message || '导出失败')
    }
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const hasPermission = (permission: string) => {
  // Mock permission check
  const userRole = localStorage.getItem('userRole') || 'user'
  return userRole === 'admin' || userRole === 'manager'
}

// Chart methods
const initCharts = () => {
  if (trendChartRef.value) {
    trendChart = echarts.init(trendChartRef.value)
    renderTrendChart()
  }
  
  if (distributionChartRef.value) {
    distributionChart = echarts.init(distributionChartRef.value)
    renderDistributionChart()
  }
  
  if (departmentChartRef.value && hasPermission('attendance:view:department')) {
    departmentChart = echarts.init(departmentChartRef.value)
    renderDepartmentChart()
  }
}

const renderTrendChart = () => {
  if (!trendChart) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}<br/>{a}: {c}%'
    },
    legend: {
      data: ['正常率', '考勤分']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}%'
      }
    },
    series: [
      {
        name: '正常率',
        type: trendChartType.value,
        data: [92.5, 93.2, 94.1, 93.8, 94.5, 95.2],
        smooth: true,
        lineStyle: {
          color: '#67c23a'
        },
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '考勤分',
        type: trendChartType.value,
        data: [91.5, 92.3, 93.0, 92.8, 93.5, 94.0],
        smooth: true,
        lineStyle: {
          color: '#409eff'
        },
        itemStyle: {
          color: '#409eff'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

const renderDistributionChart = () => {
  if (!distributionChart) return
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '考勤状态',
        type: 'pie',
        radius: '50%',
        data: [
          { value: summaryStats.normalDays, name: '正常', itemStyle: { color: '#67c23a' } },
          { value: summaryStats.lateDays, name: '迟到', itemStyle: { color: '#e6a23c' } },
          { value: summaryStats.leaveEarlyDays, name: '早退', itemStyle: { color: '#f56c6c' } },
          { value: summaryStats.absentDays, name: '缺勤', itemStyle: { color: '#909399' } },
          { value: summaryStats.overtimeDays, name: '加班', itemStyle: { color: '#409eff' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  distributionChart.setOption(option)
}

const renderDepartmentChart = () => {
  if (!departmentChart) return
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['正常率', '异常率']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      boundaryGap: [0, 0.01],
      axisLabel: {
        formatter: '{value}%'
      }
    },
    yAxis: {
      type: 'category',
      data: ['技术部', '市场部', '人事部', '财务部', '行政部']
    },
    series: [
      {
        name: '正常率',
        type: 'bar',
        data: [95.2, 92.8, 93.5, 94.1, 91.8],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '异常率',
        type: 'bar',
        data: [4.8, 7.2, 6.5, 5.9, 8.2],
        itemStyle: {
          color: '#f56c6c'
        }
      }
    ]
  }
  
  departmentChart.setOption(option)
}

// Watch chart type changes
watch(trendChartType, () => {
  if (trendChart) {
    renderTrendChart()
  }
})

// Lifecycle
onMounted(() => {
  handlePeriodChange('current_month')
  loadStatistics()
  
  // Add resize listener
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  
  // Dispose charts
  if (trendChart) trendChart.dispose()
  if (distributionChart) distributionChart.dispose()
  if (departmentChart) departmentChart.dispose()
})

const handleResize = () => {
  if (trendChart) trendChart.resize()
  if (distributionChart) distributionChart.resize()
  if (departmentChart) departmentChart.resize()
}
</script>

<style scoped>
.statistics-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.filter-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.summary-cards {
  margin-bottom: 20px;
}

.summary-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-left: 4px solid;
  height: 100%;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
  font-size: 24px;
}

.card-content {
  flex: 1;
}

.card-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.card-trend .up {
  color: #f56c6c;
}

.card-trend .down {
  color: #67c23a;
}

.charts-section {
  margin-bottom: 20px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.chart-container {
  flex: 1;
  min-height: 0;
}

.top-list {
  flex: 1;
  overflow-y: auto;
}

.top-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.top-item:last-child {
  border-bottom: none;
}

.rank {
  margin-right: 16px;
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: white;
}

.rank-gold {
  background: linear-gradient(135deg, #ffd700, #ffb700);
}

.rank-silver {
  background: linear-gradient(135deg, #c0c0c0, #a0a0a0);
}

.rank-bronze {
  background: linear-gradient(135deg, #cd7f32, #b36700);
}

.rank-other {
  background: #409eff;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.user-dept {
  font-size: 12px;
  color: #909399;
}

.user-stats {
  display: flex;
  gap: 24px;
  margin-right: 24px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.detail-stats {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.monthly-table h4,
.abnormal-analysis h4,
.overtime-analysis h4 {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 16px;
}

.abnormal-card {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  height: 100%;
}

.abnormal-card h5 {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 12px 0;
}

.abnormal-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.overtime-card {
  background: linear-gradient(135deg, #409eff, #79bbff);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  color: white;
}

.overtime-value {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
}

.overtime-label {
  font-size: 14px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .statistics-container {
    padding: 12px;
  }
  
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .filter-card .el-form {
    display: grid;
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .summary-card {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .card-icon {
    margin-right: 0;
  }
  
  .chart-card {
    height: 300px;
  }
  
  .top-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .user-stats {
    margin-right: 0;
  }
}
</style>