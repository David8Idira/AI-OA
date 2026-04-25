<template>
  <div class="checkin-container">
    <div class="checkin-card">
      <!-- Header -->
      <div class="checkin-header">
        <h2 class="checkin-title">考勤打卡</h2>
        <div class="date-time">
          <div class="date">{{ currentDate }}</div>
          <div class="time">{{ currentTime }}</div>
        </div>
      </div>

      <!-- Location Info -->
      <div class="location-info" v-if="location">
        <div class="location-icon">
          <el-icon><Location /></el-icon>
        </div>
        <div class="location-details">
          <div class="location-address">{{ location.address || '正在获取位置...' }}</div>
          <div class="location-accuracy" v-if="location.accuracy">
            精度: {{ location.accuracy }}米
          </div>
        </div>
      </div>

      <!-- Checkin Status -->
      <div class="status-info">
        <div class="status-item">
          <div class="status-label">上班时间</div>
          <div class="status-value">{{ workStartTime || '--:--' }}</div>
        </div>
        <div class="status-item">
          <div class="status-label">下班时间</div>
          <div class="status-value">{{ workEndTime || '--:--' }}</div>
        </div>
        <div class="status-item">
          <div class="status-label">考勤组</div>
          <div class="status-value">{{ attendanceGroup || '未分配' }}</div>
        </div>
      </div>

      <!-- Today's Record -->
      <div class="today-record" v-if="todayRecord">
        <div class="record-title">今日记录</div>
        <div class="record-details">
          <div class="record-item">
            <span class="record-label">上班打卡:</span>
            <span class="record-value">{{ formatTime(todayRecord.checkinTime) || '未打卡' }}</span>
          </div>
          <div class="record-item">
            <span class="record-label">下班打卡:</span>
            <span class="record-value">{{ formatTime(todayRecord.checkoutTime) || '未打卡' }}</span>
          </div>
          <div class="record-item">
            <span class="record-label">状态:</span>
            <el-tag :type="getStatusTagType(todayRecord.status)">
              {{ getStatusText(todayRecord.status) }}
            </el-tag>
          </div>
        </div>
      </div>

      <!-- Checkin Buttons -->
      <div class="checkin-buttons">
        <el-button
          type="primary"
          size="large"
          :loading="checkinLoading"
          :disabled="!canCheckIn || checkinLoading"
          @click="handleCheckIn"
          class="checkin-btn"
        >
          <template v-if="!hasCheckedInToday">上班打卡</template>
          <template v-else>下班打卡</template>
        </el-button>
        
        <el-button
          type="info"
          size="large"
          :disabled="checkinLoading"
          @click="refreshLocation"
          class="refresh-btn"
        >
          刷新位置
        </el-button>
      </div>

      <!-- Checkin Methods -->
      <div class="checkin-methods">
        <div class="methods-title">打卡方式</div>
        <div class="methods-list">
          <el-radio-group v-model="checkinMethod">
            <el-radio :label="0">GPS定位</el-radio>
            <el-radio :label="1">WiFi打卡</el-radio>
            <el-radio :label="2">手动打卡</el-radio>
          </el-radio-group>
        </div>
      </div>

      <!-- WiFi Info (if WiFi method selected) -->
      <div class="wifi-info" v-if="checkinMethod === 1">
        <el-input
          v-model="wifiMac"
          placeholder="请输入WiFi MAC地址"
          clearable
        >
          <template #prepend>
            <el-icon><Connection /></el-icon>
          </template>
        </el-input>
        <div class="wifi-hint" v-if="availableWifis.length > 0">
          可用WiFi: {{ availableWifis.join(', ') }}
        </div>
      </div>

      <!-- Remark -->
      <div class="remark-section">
        <el-input
          v-model="remark"
          type="textarea"
          :rows="2"
          placeholder="备注 (可选)"
          maxlength="200"
          show-word-limit
        />
      </div>

      <!-- Recent Records -->
      <div class="recent-records" v-if="recentRecords.length > 0">
        <div class="records-title">最近打卡记录</div>
        <div class="records-list">
          <div
            v-for="record in recentRecords"
            :key="record.id"
            class="record-card"
          >
            <div class="record-date">{{ formatDate(record.attendanceDate) }}</div>
            <div class="record-times">
              <span class="record-time">上班: {{ formatTime(record.checkinTime) || '--:--' }}</span>
              <span class="record-time">下班: {{ formatTime(record.checkoutTime) || '--:--' }}</span>
            </div>
            <el-tag size="small" :type="getStatusTagType(record.status)">
              {{ getStatusText(record.status) }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- Statistics Card -->
    <div class="stats-card">
      <div class="stats-title">本月统计</div>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ monthlyStats.normalDays || 0 }}</div>
          <div class="stat-label">正常天数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ monthlyStats.lateDays || 0 }}</div>
          <div class="stat-label">迟到</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ monthlyStats.leaveEarlyDays || 0 }}</div>
          <div class="stat-label">早退</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ monthlyStats.overtimeDays || 0 }}</div>
          <div class="stat-label">加班</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ monthlyStats.totalWorkHours || 0 }}</div>
          <div class="stat-label">总工时(小时)</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ monthlyStats.attendanceScore || 100 }}</div>
          <div class="stat-label">考勤分</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Connection } from '@element-plus/icons-vue'
import { format } from 'date-fns'
import zhCN from 'date-fns/locale/zh-CN'
import { 
  getTodayAttendance,
  checkin,
  getAttendanceRecords,
  getMonthlyReport 
} from '@/api/attendance'

// Reactive data
const currentTime = ref('')
const currentDate = ref('')
const location = ref<any>(null)
const todayRecord = ref<any>(null)
const hasCheckedInToday = ref(false)
const checkinLoading = ref(false)
const checkinMethod = ref(0) // 0-GPS, 1-WiFi, 2-Manual
const wifiMac = ref('')
const remark = ref('')
const recentRecords = ref<any[]>([])
const monthlyStats = ref<any>({})
const availableWifis = ref<string[]>([])
const attendanceGroup = ref('')
const workStartTime = ref('')
const workEndTime = ref('')

// Status texts
const statusTexts = {
  0: '正常',
  1: '迟到',
  2: '早退',
  3: '缺勤',
  4: '加班',
  5: '请假',
  6: '出差'
}

const statusTagTypes = {
  0: 'success',
  1: 'warning',
  2: 'warning',
  3: 'danger',
  4: 'info',
  5: 'info',
  6: 'info'
}

// Computed
const canCheckIn = computed(() => {
  if (checkinMethod.value === 0 && !location.value) return false
  if (checkinMethod.value === 1 && !wifiMac.value) return false
  return true
})

// Methods
const updateDateTime = () => {
  const now = new Date()
  currentTime.value = format(now, 'HH:mm:ss')
  currentDate.value = format(now, 'yyyy年MM月dd日 EEEE', { locale: zhCN })
}

const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.warning('浏览器不支持地理位置定位')
    return
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const { latitude, longitude, accuracy } = position.coords
      location.value = {
        latitude,
        longitude,
        accuracy: Math.round(accuracy),
        address: '正在解析地址...'
      }
      
      // Reverse geocoding would go here
      // For now, we'll just show coordinates
      location.value.address = `纬度: ${latitude.toFixed(6)}, 经度: ${longitude.toFixed(6)}`
    },
    (error) => {
      console.error('获取位置失败:', error)
      ElMessage.error(`获取位置失败: ${error.message}`)
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

const refreshLocation = () => {
  location.value = null
  getCurrentLocation()
}

const formatTime = (time: string | null) => {
  if (!time) return null
  return format(new Date(time), 'HH:mm')
}

const formatDate = (date: string) => {
  return format(new Date(date), 'MM/dd')
}

const getStatusText = (status: number) => {
  return statusTexts[status as keyof typeof statusTexts] || '未知'
}

const getStatusTagType = (status: number) => {
  return statusTagTypes[status as keyof typeof statusTagTypes] || 'info'
}

const loadTodayAttendance = async () => {
  try {
    const userId = localStorage.getItem('userId') || 'test-user'
    const response = await getTodayAttendance(userId)
    if (response.code === 200 && response.data) {
      todayRecord.value = response.data
      hasCheckedInToday.value = !!response.data.checkinTime
    }
  } catch (error) {
    console.error('加载今日考勤失败:', error)
  }
}

const loadRecentRecords = async () => {
  try {
    const userId = localStorage.getItem('userId') || 'test-user'
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(startDate.getDate() - 7)
    
    const response = await getAttendanceRecords({
      userId,
      startDate: format(startDate, 'yyyy-MM-dd'),
      endDate: format(endDate, 'yyyy-MM-dd'),
      pageNum: 1,
      pageSize: 5,
      orderBy: 'attendanceDate desc'
    })
    
    if (response.code === 200) {
      recentRecords.value = response.data.list || []
    }
  } catch (error) {
    console.error('加载最近记录失败:', error)
  }
}

const loadMonthlyStats = async () => {
  try {
    const userId = localStorage.getItem('userId') || 'test-user'
    const now = new Date()
    const response = await getMonthlyReport(userId, now.getFullYear(), now.getMonth() + 1)
    
    if (response.code === 200) {
      monthlyStats.value = response.data
    }
  } catch (error) {
    console.error('加载月度统计失败:', error)
  }
}

const handleCheckIn = async () => {
  try {
    checkinLoading.value = true
    
    const userId = localStorage.getItem('userId') || 'test-user'
    const checkinType = hasCheckedInToday.value ? 1 : 0 // 0-checkin, 1-checkout
    
    const dto = {
      userId,
      checkinType,
      method: checkinMethod.value,
      latitude: location.value?.latitude || null,
      longitude: location.value?.longitude || null,
      address: location.value?.address || null,
      wifiMac: checkinMethod.value === 1 ? wifiMac.value : null,
      deviceId: 'web-browser',
      ip: '127.0.0.1',
      remark: remark.value
    }
    
    const response = await checkin(dto)
    
    if (response.code === 200) {
      ElMessage.success(hasCheckedInToday.value ? '下班打卡成功' : '上班打卡成功')
      
      // Reload data
      await loadTodayAttendance()
      await loadRecentRecords()
      await loadMonthlyStats()
      
      // Reset form
      if (checkinType === 1) {
        remark.value = ''
      }
    } else {
      ElMessage.error(response.message || '打卡失败')
    }
  } catch (error: any) {
    console.error('打卡失败:', error)
    ElMessage.error(error.message || '打卡失败')
  } finally {
    checkinLoading.value = false
  }
}

// Lifecycle
onMounted(() => {
  // Start clock
  updateDateTime()
  const timer = setInterval(updateDateTime, 1000)
  
  // Get location
  getCurrentLocation()
  
  // Load data
  loadTodayAttendance()
  loadRecentRecords()
  loadMonthlyStats()
  
  // Mock data for demo
  attendanceGroup.value = '默认考勤组'
  workStartTime.value = '09:00'
  workEndTime.value = '18:00'
  availableWifis.value = ['00:11:22:33:44:55', 'AA:BB:CC:DD:EE:FF']
  
  return () => clearInterval(timer)
})
</script>

<style scoped>
.checkin-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.checkin-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.checkin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.checkin-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.date-time {
  text-align: right;
}

.date-time .date {
  font-size: 16px;
  color: #606266;
  margin-bottom: 4px;
}

.date-time .time {
  font-size: 32px;
  font-weight: 500;
  color: #409eff;
  font-family: 'Courier New', monospace;
}

.location-info {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.location-icon {
  margin-right: 12px;
  color: #409eff;
  font-size: 24px;
}

.location-details {
  flex: 1;
}

.location-address {
  font-size: 16px;
  color: #303133;
  margin-bottom: 4px;
}

.location-accuracy {
  font-size: 14px;
  color: #909399;
}

.status-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.status-item {
  text-align: center;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.status-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.status-value {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
}

.today-record {
  margin-bottom: 24px;
}

.record-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

.record-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.record-item {
  text-align: center;
}

.record-label {
  display: block;
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.record-value {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.checkin-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.checkin-btn {
  flex: 1;
  height: 56px;
  font-size: 18px;
}

.refresh-btn {
  height: 56px;
}

.checkin-methods {
  margin-bottom: 16px;
}

.methods-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.methods-list {
  display: flex;
  gap: 24px;
}

.wifi-info {
  margin-bottom: 16px;
}

.wifi-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.remark-section {
  margin-bottom: 24px;
}

.recent-records {
  margin-top: 24px;
}

.records-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 12px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.record-date {
  font-size: 14px;
  color: #303133;
  min-width: 60px;
}

.record-times {
  flex: 1;
  margin-left: 16px;
}

.record-time {
  margin-right: 24px;
  font-size: 14px;
  color: #606266;
}

.stats-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stats-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: linear-gradient(135deg, #409eff, #79bbff);
  border-radius: 8px;
  color: white;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

@media (max-width: 768px) {
  .checkin-container {
    padding: 12px;
  }
  
  .checkin-card {
    padding: 16px;
  }
  
  .checkin-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .date-time {
    margin-top: 12px;
    align-self: flex-end;
  }
  
  .status-info,
  .record-details {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .record-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .record-times {
    margin-left: 0;
  }
  
  .record-time {
    display: block;
    margin-right: 0;
    margin-bottom: 4px;
  }
}
</style>