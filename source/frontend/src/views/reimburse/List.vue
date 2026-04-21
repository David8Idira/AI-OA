<template>
  <div class="reimburse-container">
    <!-- 头部 -->
    <div class="reimburse-header">
      <h2>财务报销管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showOCRDialog = true">
          <el-icon><Camera /></el-icon>
          OCR识别发票
        </el-button>
        <el-button type="success" @click="showCreateDialog = true">
          <el-icon><Plus /></el-icon>
          新建报销单
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.pendingAmount }}</div>
          <div class="stat-label">待报销金额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.pendingCount }}</div>
          <div class="stat-label">待审批单据</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.monthTotal }}</div>
          <div class="stat-label">本月报销总额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-value">{{ stats.invoiceCount }}</div>
          <div class="stat-label">已识别发票</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 查询条件 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 150px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已打款" value="PAID" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="queryForm.keyword" placeholder="标题/申请人" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 报销单列表 -->
    <el-card>
      <el-table :data="reimburseList" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="报销标题" min-width="200" />
        <el-table-column prop="applicantName" label="申请人" width="120" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="totalAmount" label="报销金额" width="130">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发票/行程" width="120">
          <template #default="{ row }">
            {{ row.invoiceCount }}张发票 / {{ row.tripCount }}个行程
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">查看</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="success" @click="handleSubmit(row)">提交</el-button>
            <el-button v-if="row.status === 'DRAFT'" size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.page"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <!-- OCR识别对话框 -->
    <el-dialog v-model="showOCRDialog" title="OCR识别发票" width="600px">
      <el-upload
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        accept="image/*,.pdf"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">支持 JPG/PNG/PDF 格式，单文件不超过 10MB</div>
        </template>
      </el-upload>

      <div v-if="ocrResult" class="ocr-result">
        <h4>识别结果</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="发票类型">{{ ocrResult.invoiceType }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ ocrResult.confidence }}%</el-descriptions-item>
          <el-descriptions-item label="发票代码">{{ ocrResult.fields.code }}</el-descriptions-item>
          <el-descriptions-item label="发票号码">{{ ocrResult.fields.number }}</el-descriptions-item>
          <el-descriptions-item label="开票日期">{{ ocrResult.fields.date }}</el-descriptions-item>
          <el-descriptions-item label="价税合计">¥{{ ocrResult.fields.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="销售方">{{ ocrResult.fields.sellerName }}</el-descriptions-item>
          <el-descriptions-item label="购买方">{{ ocrResult.fields.buyerName }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="ocrResult.doubtfulFields.length > 0" class="doubtful-warning">
          <el-alert type="warning" :closable="false" show-icon>
            <template #title>以下字段置信度较低，请人工确认</template>
            <ul>
              <li v-for="field in ocrResult.doubtfulFields" :key="field.field">
                {{ field.field }}: {{ field.value }} (置信度: {{ field.confidence }}%)
              </li>
            </ul>
          </el-alert>
        </div>

        <div class="ocr-actions">
          <el-button @click="ocrResult = null">重新识别</el-button>
          <el-button type="primary" @click="confirmOCR">确认并保存</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 新建报销单对话框 -->
    <el-dialog v-model="showCreateDialog" :title="editId ? '编辑报销单' : '新建报销单'" width="600px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="报销标题" required>
          <el-input v-model="createForm.title" placeholder="请输入报销标题" />
        </el-form-item>
        <el-form-item label="选择发票">
          <el-select v-model="createForm.invoiceIds" multiple placeholder="请选择发票" style="width: 100%">
            <el-option v-for="inv in invoiceOptions" :key="inv.id" :label="`${inv.sellerName} - ¥${inv.totalAmount}`" :value="inv.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择行程">
          <el-select v-model="createForm.tripIds" multiple placeholder="请选择行程单" style="width: 100%">
            <el-option v-for="trip in tripOptions" :key="trip.id" :label="`${trip.departure} → ${trip.destination} - ¥${trip.amount}`" :value="trip.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="3" placeholder="备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog v-model="showDetailDialog" title="报销单详情" width="800px">
      <div v-if="currentDetail" class="detail-container">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报销标题">{{ currentDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(currentDetail.status)">{{ getStatusLabel(currentDetail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentDetail.applicantName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ currentDetail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="报销总额">
            <span class="amount-large">¥{{ currentDetail.totalAmount.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentDetail.createTime }}</el-descriptions-item>
        </el-descriptions>

        <h4>发票明细 ({{ currentDetail.invoices.length }})</h4>
        <el-table :data="currentDetail.invoices" style="width: 100%" size="small">
          <el-table-column prop="type" label="类型" width="100" />
          <el-table-column prop="number" label="发票号码" width="120" />
          <el-table-column prop="sellerName" label="销售方" min-width="150" />
          <el-table-column prop="totalAmount" label="金额" width="120">
            <template #default="{ row }">¥{{ row.totalAmount.toFixed(2) }}</template>
          </el-table-column>
        </el-table>

        <h4 style="margin-top: 16px">行程明细 ({{ currentDetail.trips.length }})</h4>
        <el-table :data="currentDetail.trips" style="width: 100%" size="small">
          <el-table-column prop="platform" label="平台" width="80" />
          <el-table-column prop="departure" label="出发地" min-width="150" />
          <el-table-column prop="destination" label="目的地" min-width="150" />
          <el-table-column prop="amount" label="金额" width="100">
            <template #default="{ row }">¥{{ row.amount.toFixed(2) }}</template>
          </el-table-column>
        </el-table>

        <div v-if="currentDetail.remark" class="detail-remark">
          <h4>备注</h4>
          <p>{{ currentDetail.remark }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getReimburseList,
  getStatusConfig,
  createReimburse,
  updateReimburse,
  submitReimburse,
  deleteReimburse,
  getInvoiceList,
  getTripList,
  uploadInvoice
} from '@/api/reimburse'
import type { ReimburseVO, ReimburseDetailVO, InvoiceVO, TripExpenseVO, OCRResultVO } from '@/api/reimburse'

const router = useRouter()
const loading = ref(false)
const total = ref(0)

const stats = ref({
  pendingAmount: '¥12,350',
  pendingCount: 5,
  monthTotal: '¥45,680',
  invoiceCount: 32
})

const queryForm = reactive({
  page: 1,
  pageSize: 10,
  status: '',
  keyword: '',
  dateRange: [] as string[]
})

const reimburseList = ref<ReimburseVO[]>([
  {
    id: '1',
    title: '张三-3月差旅报销',
    applicantId: 'u1',
    applicantName: '张三',
    deptName: '研发部',
    totalAmount: 3580.50,
    invoiceCount: 5,
    tripCount: 2,
    status: 'PENDING',
    createTime: '2026-04-10 10:30'
  },
  {
    id: '2',
    title: '李四-办公用品采购报销',
    applicantId: 'u2',
    applicantName: '李四',
    deptName: '运营部',
    totalAmount: 1250.00,
    invoiceCount: 3,
    tripCount: 0,
    status: 'APPROVED',
    createTime: '2026-04-08 14:20'
  },
  {
    id: '3',
    title: '王五-客户招待费用',
    applicantId: 'u3',
    applicantName: '王五',
    deptName: '销售部',
    totalAmount: 890.00,
    invoiceCount: 2,
    tripCount: 0,
    status: 'DRAFT',
    createTime: '2026-04-07 09:15'
  }
])

// OCR
const showOCRDialog = ref(false)
const ocrResult = ref<OCRResultVO | null>(null)
const ocrFileList = ref<any[]>([])

// 新建/编辑
const showCreateDialog = ref(false)
const editId = ref('')
const createForm = reactive({
  title: '',
  invoiceIds: [] as string[],
  tripIds: [] as string[],
  remark: ''
})

const invoiceOptions = ref<InvoiceVO[]>([
  { id: 'inv1', type: 'VAT_NORMAL', number: '12345678', sellerName: '某某科技公司', totalAmount: 1000, taxAmount: 60, amount: 940, status: 'CONFIRMED', createTime: '2026-04-01' },
  { id: 'inv2', type: 'TAXI', sellerName: '滴滴出行', totalAmount: 85.5, taxAmount: 0, amount: 85.5, status: 'CONFIRMED', createTime: '2026-04-05' }
])

const tripOptions = ref<TripExpenseVO[]>([
  { id: 'trip1', platform: '滴滴', departure: '北京西站', destination: '中关村', amount: 45.0, createTime: '2026-04-03' },
  { id: 'trip2', platform: '滴滴', departure: '首都机场', destination: '朝阳区', amount: 120.0, createTime: '2026-04-06' }
])

// 详情
const showDetailDialog = ref(false)
const currentDetail = ref<ReimburseDetailVO | null>(null)

const loadData = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 500)
}

const handleQuery = () => {
  queryForm.page = 1
  loadData()
}

const handleReset = () => {
  queryForm.status = ''
  queryForm.keyword = ''
  queryForm.dateRange = []
  handleQuery()
}

const getStatusType = (status: string) => {
  const config = getStatusConfig(status as any)
  return config.type
}

const getStatusLabel = (status: string) => {
  const config = getStatusConfig(status as any)
  return config.label
}

const handleFileChange = (file: any) => {
  ocrFileList.value = [file.raw]
  // 模拟OCR识别
  setTimeout(() => {
    ocrResult.value = {
      invoiceType: '增值税普票',
      fields: {
        code: '1100234560',
        number: '12345678',
        date: '2026-03-25',
        sellerName: '某某科技有限公司',
        buyerName: 'AI-OA科技有限公司',
        totalAmount: '1060.00'
      },
      confidence: 95.2,
      imageUrl: '',
      doubtfulFields: []
    }
  }, 1500)
}

const confirmOCR = () => {
  ElMessage.success('发票已保存')
  showOCRDialog.value = false
  ocrResult.value = null
}

const handleView = async (row: ReimburseVO) => {
  currentDetail.value = {
    ...row,
    invoices: invoiceOptions.value,
    trips: tripOptions.value,
    attachments: [],
    remark: '这是一笔差旅报销，包含交通和住宿费用。'
  }
  showDetailDialog.value = true
}

const handleEdit = (row: ReimburseVO) => {
  editId.value = row.id
  createForm.title = row.title
  showCreateDialog.value = true
}

const handleSubmit = async (row: ReimburseVO) => {
  try {
    await ElMessageBox.confirm('确定提交此报销单吗？', '提示', { type: 'warning' })
    ElMessage.success('提交成功')
    loadData()
  } catch {}
}

const handleDelete = async (row: ReimburseVO) => {
  try {
    await ElMessageBox.confirm(`确定删除 "${row.title}" 吗？`, '提示', { type: 'warning' })
    reimburseList.value = reimburseList.value.filter(r => r.id !== row.id)
    ElMessage.success('删除成功')
  } catch {}
}

const handleSave = async () => {
  if (!createForm.title) {
    ElMessage.warning('请输入报销标题')
    return
  }
  ElMessage.success(editId.value ? '更新成功' : '创建成功')
  showCreateDialog.value = false
  editId.value = ''
  createForm.title = ''
  createForm.invoiceIds = []
  createForm.tripIds = []
  createForm.remark = ''
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.reimburse-container {
  padding: 20px;

  .reimburse-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .stats-row {
    margin-bottom: 20px;

    .stat-card {
      text-align: center;

      .stat-value {
        font-size: 28px;
        font-weight: bold;
        color: #667eea;
      }

      .stat-label {
        font-size: 14px;
        color: #999;
        margin-top: 8px;
      }
    }
  }

  .filter-card {
    margin-bottom: 16px;
  }

  .amount {
    color: #f56c6c;
    font-weight: 500;
  }

  .amount-large {
    color: #f56c6c;
    font-size: 20px;
    font-weight: bold;
  }

  .pagination {
    margin-top: 16px;
    justify-content: flex-end;
  }

  .ocr-result {
    margin-top: 20px;

    h4 {
      margin-bottom: 12px;
    }
  }

  .doubtful-warning {
    margin-top: 12px;

    ul {
      margin: 8px 0 0;
      padding-left: 20px;
    }
  }

  .ocr-actions {
    margin-top: 16px;
    text-align: right;
  }

  .detail-container {
    h4 {
      margin: 16px 0 8px;
    }
  }

  .detail-remark {
    margin-top: 16px;

    p {
      color: #666;
      line-height: 1.6;
    }
  }
}
</style>
