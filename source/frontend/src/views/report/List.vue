<template>
  <div class="report-container">
    <!-- 头部 -->
    <div class="report-header">
      <h2>📊 智能报表中心</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        创建报表
      </el-button>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月报销总额" :value="stats.monthTotal" prefix="¥" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="待审批" :value="stats.pendingCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="本月报销次数" :value="stats.reimburseCount" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="AI生成报表" :value="stats.aiReportCount" />
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 报表列表 -->
    <div class="report-section">
      <h3>我的报表</h3>
      <el-table :data="reportList" style="width: 100%">
        <el-table-column prop="name" label="报表名称" width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="period" label="周期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'completed' ? 'success' : 'warning'">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="viewReport(row)">查看</el-button>
            <el-button size="small" type="primary" @click="downloadReport(row)">下载</el-button>
            <el-button size="small" type="danger" @click="deleteReport(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- AI生成报表 -->
    <div class="ai-report-section">
      <h3>🤖 AI智能报表</h3>
      <el-card>
        <el-form :model="aiForm" label-width="120px">
          <el-form-item label="报表类型">
            <el-select v-model="aiForm.type" placeholder="选择类型">
              <el-option label="月度报销汇总" value="monthly_reimburse" />
              <el-option label="部门费用分析" value="department_cost" />
              <el-option label="审批效率分析" value="approval_efficiency" />
            </el-select>
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="aiForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
            />
          </el-form-item>
          <el-form-item label="AI生成描述">
            <el-input
              v-model="aiForm.prompt"
              type="textarea"
              :rows="3"
              placeholder="描述你需要的报表内容..."
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="aiLoading" @click="generateAIReport">
              生成AI报表
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- AI生成结果 -->
        <div v-if="aiResult" class="ai-result">
          <h4>AI生成的报表内容：</h4>
          <div class="result-content">{{ aiResult }}</div>
          <el-button type="success" @click="saveAIReport">保存为报表</el-button>
        </div>
      </el-card>
    </div>
    
    <!-- 创建对话框 -->
    <el-dialog v-model="showCreateDialog" title="创建报表" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="报表名称">
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="报表类型">
          <el-select v-model="createForm.type">
            <el-option label="报销统计" value="reimburse" />
            <el-option label="审批统计" value="approval" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="周期">
          <el-select v-model="createForm.period">
            <el-option label="日报" value="daily" />
            <el-option label="周报" value="weekly" />
            <el-option label="月报" value="monthly" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReports, deleteReport as deleteReportApi } from '@/api/report'

const showCreateDialog = ref(false)
const aiLoading = ref(false)
const aiResult = ref('')

const stats = ref({
  monthTotal: 156780,
  pendingCount: 12,
  reimburseCount: 45,
  aiReportCount: 8
})

const reportList = ref([
  { id: 1, name: '3月报销汇总', type: 'reimburse', period: 'monthly', status: 'completed', createTime: '2026-03-31 10:00' },
  { id: 2, name: 'Q1费用分析', type: 'cost', period: 'quarterly', status: 'completed', createTime: '2026-03-30 15:30' },
  { id: 3, name: '审批效率报告', type: 'approval', period: 'weekly', status: 'generating', createTime: '2026-04-08 09:00' }
])

const aiForm = ref({
  type: '',
  dateRange: [],
  prompt: ''
})

const createForm = ref({
  name: '',
  type: 'reimburse',
  period: 'monthly'
})

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    reimburse: 'primary',
    cost: 'success',
    approval: 'warning'
  }
  return map[type] || 'info'
}

const viewReport = (row: any) => {
  ElMessage.info(`查看报表: ${row.name}`)
}

const downloadReport = (row: any) => {
  ElMessage.success(`下载报表: ${row.name}`)
}

const deleteReport = (row: any) => {
  ElMessageBox.confirm(`确定删除报表 "${row.name}" 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    reportList.value = reportList.value.filter(r => r.id !== row.id)
    ElMessage.success('删除成功')
  })
}

const generateAIReport = async () => {
  if (!aiForm.value.type) {
    ElMessage.warning('请选择报表类型')
    return
  }
  
  aiLoading.value = true
  
  // 模拟AI生成
  setTimeout(() => {
    aiResult.value = `📊 ${aiForm.value.type === 'monthly_reimburse' ? '月度报销汇总' : '费用分析'}报告
    
【数据概览】
- 总报销金额: ¥156,780
- 报销次数: 45次
- 平均单笔: ¥3,484

【各部门占比】
- 研发部: 45% (¥70,551)
- 销售部: 30% (¥47,034)
- 运营部: 15% (¥23,517)
- 其他: 10% (¥15,678)

【趋势分析】
相比上月增长12%，主要来自差旅费用增加。

【建议】
1. 加强差旅审批管控
2. 优化部门预算分配`
    
    aiLoading.value = false
    ElMessage.success('AI报表生成完成')
  }, 2000)
}

const saveAIReport = () => {
  reportList.value.push({
    id: Date.now(),
    name: `AI报表-${new Date().toLocaleDateString()}`,
    type: 'ai_generated',
    period: 'custom',
    status: 'completed',
    createTime: new Date().toLocaleString()
  })
  aiResult.value = ''
  ElMessage.success('报表已保存')
}

const handleCreate = () => {
  if (!createForm.value.name) {
    ElMessage.warning('请输入报表名称')
    return
  }
  
  reportList.value.push({
    id: Date.now(),
    name: createForm.value.name,
    type: createForm.value.type,
    period: createForm.value.period,
    status: 'generating',
    createTime: new Date().toLocaleString()
  })
  
  showCreateDialog.value = false
  ElMessage.success('创建成功')
}
</script>

<style scoped>
.report-container {
  padding: 20px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.report-header h2 {
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.report-section, .ai-report-section {
  margin-bottom: 24px;
}

.report-section h3, .ai-report-section h3 {
  margin-bottom: 16px;
}

.ai-result {
  margin-top: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.ai-result h4 {
  margin-bottom: 12px;
}

.result-content {
  white-space: pre-wrap;
  line-height: 1.8;
  margin-bottom: 16px;
}
</style>