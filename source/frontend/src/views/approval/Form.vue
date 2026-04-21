<template>
  <div class="approval-form-page">
    <!-- 顶部导航 -->
    <div class="page-nav">
      <el-button @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <el-button type="primary" @click="handleSaveDraft" :loading="draftLoading">
        <el-icon><Folder /></el-icon>
        保存草稿
      </el-button>
    </div>

    <div class="form-content">
      <!-- 左侧：表单 -->
      <div class="main-form">
        <el-card>
          <template #header>
            <span>{{ isEdit ? '编辑审批' : '新建审批' }}</span>
          </template>
          
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            :disabled="loading"
            label-width="100px"
            class="approval-form"
          >
            <!-- 审批类型选择 -->
            <el-form-item label="审批类型" prop="type">
              <el-radio-group v-model="form.type" @change="handleTypeChange">
                <el-radio-button
                  v-for="(item, key) in APPROVAL_TYPE_MAP"
                  :key="key"
                  :value="key"
                >
                  <div class="type-option">
                    <el-icon>
                      <component :is="item.icon" />
                    </el-icon>
                    <span>{{ item.label }}</span>
                  </div>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>
            
            <!-- 申请标题 -->
            <el-form-item label="申请标题" prop="title">
              <el-input
                v-model="form.title"
                placeholder="请输入审批标题"
                maxlength="100"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 请假申请 -->
            <template v-if="form.type === 'LEAVE'">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="请假类型" prop="formData.leaveType">
                    <el-select v-model="form.formData.leaveType" placeholder="请选择" style="width: 100%">
                      <el-option label="年假" value="ANNUAL" />
                      <el-option label="病假" value="SICK" />
                      <el-option label="事假" value="PERSONAL" />
                      <el-option label="婚假" value="MARRIAGE" />
                      <el-option label="产假" value="MATERNITY" />
                      <el-option label="丧假" value="FUNERAL" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="请假天数" prop="formData.days">
                    <el-input-number
                      v-model="form.formData.days"
                      :min="1"
                      :max="30"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="开始日期" prop="startDate">
                    <el-date-picker
                      v-model="form.startDate"
                      type="date"
                      placeholder="选择开始日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期" prop="endDate">
                    <el-date-picker
                      v-model="form.endDate"
                      type="date"
                      placeholder="选择结束日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                      :disabled="form.formData.days && form.formData.days <= 1"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="请假原因" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请详细说明请假原因"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </template>
            
            <!-- 费用报销 -->
            <template v-else-if="form.type === 'EXPENSE' || form.type === 'REIMBURSE'">
              <el-form-item label="报销金额" prop="amount">
                <el-input-number
                  v-model="form.amount"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 200px"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
              
              <el-form-item label="费用类型" prop="formData.expenseType">
                <el-checkbox-group v-model="form.formData.expenseTypes">
                  <el-checkbox label="TRAVEL">差旅费</el-checkbox>
                  <el-checkbox label="MEAL">餐饮费</el-checkbox>
                  <el-checkbox label="ACCOMMODATION">住宿费</el-checkbox>
                  <el-checkbox label="TRANSPORT">交通费</el-checkbox>
                  <el-checkbox label="OFFICE">办公费</el-checkbox>
                  <el-checkbox label="OTHER">其他</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="费用明细" prop="formData.items">
                <div class="expense-items">
                  <div
                    v-for="(item, index) in form.formData.items"
                    :key="index"
                    class="expense-item"
                  >
                    <el-input
                      v-model="item.description"
                      placeholder="费用说明"
                      style="flex: 1"
                    />
                    <el-input-number
                      v-model="item.amount"
                      :min="0"
                      :precision="2"
                      :controls="false"
                      placeholder="金额"
                      style="width: 120px"
                    />
                    <el-button
                      type="danger"
                      :icon="Delete"
                      circle
                      @click="removeExpenseItem(index)"
                      :disabled="form.formData.items.length <= 1"
                    />
                  </div>
                  <el-button type="primary" plain @click="addExpenseItem">
                    <el-icon><Plus /></el-icon>
                    添加明细
                  </el-button>
                </div>
              </el-form-item>
              
              <el-form-item label="报销事由" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请详细说明报销事由"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              
              <el-form-item label="发票凭证">
                <el-upload
                  v-model:file-list="fileList"
                  action="#"
                  :auto-upload="false"
                  :limit="10"
                  accept=".jpg,.jpeg,.png,.pdf"
                  list-type="picture-card"
                >
                  <el-icon><Plus /></el-icon>
                  <template #tip>
                    <div class="el-upload__tip">支持 JPG、PNG、PDF 格式，最多上传 10 张</div>
                  </template>
                </el-upload>
              </el-form-item>
            </template>
            
            <!-- 差旅申请 -->
            <template v-else-if="form.type === 'TRAVEL'">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="目的地" prop="formData.destination">
                    <el-input
                      v-model="form.formData.destination"
                      placeholder="请输入出差目的地"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="出行方式" prop="formData.travelType">
                    <el-select v-model="form.formData.travelType" placeholder="请选择" style="width: 100%">
                      <el-option label="飞机" value="PLANE" />
                      <el-option label="高铁" value="TRAIN" />
                      <el-option label="自驾" value="SELF_DRIVING" />
                      <el-option label="其他" value="OTHER" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="开始日期" prop="startDate">
                    <el-date-picker
                      v-model="form.startDate"
                      type="date"
                      placeholder="选择开始日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期" prop="endDate">
                    <el-date-picker
                      v-model="form.endDate"
                      type="date"
                      placeholder="选择结束日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="出差预算" prop="amount">
                <el-input-number
                  v-model="form.amount"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 200px"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
              
              <el-form-item label="出差事由" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请详细说明出差目的和计划"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </template>
            
            <!-- 加班申请 -->
            <template v-else-if="form.type === 'OVERTIME'">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="加班日期" prop="startDate">
                    <el-date-picker
                      v-model="form.startDate"
                      type="date"
                      placeholder="选择加班日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="加班时长" prop="formData.hours">
                    <el-input-number
                      v-model="form.formData.hours"
                      :min="1"
                      :max="24"
                      style="width: 100%"
                    />
                    <span class="unit">小时</span>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="加班类型" prop="formData.overtimeType">
                <el-radio-group v-model="form.formData.overtimeType">
                  <el-radio label="WORKDAY">工作日加班</el-radio>
                  <el-radio label="WEEKEND">周末加班</el-radio>
                  <el-radio label="HOLIDAY">节假日加班</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="加班原因" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请说明加班原因和工作内容"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </template>
            
            <!-- 采购申请 -->
            <template v-else-if="form.type === 'PURCHASE'">
              <el-form-item label="采购物品" prop="formData.items">
                <div class="purchase-items">
                  <div
                    v-for="(item, index) in form.formData.items"
                    :key="index"
                    class="purchase-item"
                  >
                    <el-input
                      v-model="item.name"
                      placeholder="物品名称"
                      style="flex: 2"
                    />
                    <el-input-number
                      v-model="item.quantity"
                      :min="1"
                      placeholder="数量"
                      style="width: 100px"
                    />
                    <el-input-number
                      v-model="item.price"
                      :min="0"
                      :precision="2"
                      :controls="false"
                      placeholder="单价"
                      style="width: 120px"
                    />
                    <el-button
                      type="danger"
                      :icon="Delete"
                      circle
                      @click="removePurchaseItem(index)"
                      :disabled="form.formData.items.length <= 1"
                    />
                  </div>
                  <el-button type="primary" plain @click="addPurchaseItem">
                    <el-icon><Plus /></el-icon>
                    添加物品
                  </el-button>
                </div>
              </el-form-item>
              
              <el-form-item label="采购总金额" prop="amount">
                <el-input-number
                  v-model="form.amount"
                  :min="0"
                  :precision="2"
                  :controls="false"
                  style="width: 200px"
                >
                  <template #prefix>¥</template>
                </el-input-number>
              </el-form-item>
              
              <el-form-item label="供应商" prop="formData.supplier">
                <el-input
                  v-model="form.formData.supplier"
                  placeholder="请输入供应商名称"
                />
              </el-form-item>
              
              <el-form-item label="采购原因" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请说明采购原因和用途"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </template>
            
            <!-- 居家办公 -->
            <template v-else-if="form.type === 'WORK_FROM_HOME'">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="开始日期" prop="startDate">
                    <el-date-picker
                      v-model="form.startDate"
                      type="date"
                      placeholder="选择开始日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="结束日期" prop="endDate">
                    <el-date-picker
                      v-model="form.endDate"
                      type="date"
                      placeholder="选择结束日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="申请原因" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="3"
                  placeholder="请说明居家办公的原因"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </template>
            
            <!-- 离职申请 -->
            <template v-else-if="form.type === 'RESIGN'">
              <el-row :gutter="24">
                <el-col :span="12">
                  <el-form-item label="预计离职日" prop="endDate">
                    <el-date-picker
                      v-model="form.endDate"
                      type="date"
                      placeholder="选择预计离职日期"
                      value-format="YYYY-MM-DD"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="工作交接人" prop="formData.handoverTo">
                    <el-input
                      v-model="form.formData.handoverTo"
                      placeholder="请输入交接人姓名"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="离职原因" prop="reason">
                <el-input
                  v-model="form.reason"
                  type="textarea"
                  :rows="4"
                  placeholder="请详细说明离职原因"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </template>
            
            <!-- 通用说明 -->
            <el-form-item label="详细说明" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="4"
                placeholder="请输入详细的申请说明"
                maxlength="2000"
                show-word-limit
              />
            </el-form-item>
            
            <!-- 附件上传 -->
            <el-form-item label="附件">
              <el-upload
                v-model:file-list="fileList"
                action="#"
                :auto-upload="false"
                :limit="10"
                accept=".jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx"
                multiple
              >
                <el-button>
                  <el-icon><Upload /></el-icon>
                  点击上传
                </el-button>
                <template #tip>
                  <div class="el-upload__tip">支持 JPG、PNG、PDF、DOC、DOCX、XLS、XLSX 格式，最多上传 10 个文件</div>
                </template>
              </el-upload>
            </el-form-item>
            
            <!-- 提交按钮 -->
            <el-form-item>
              <div class="form-actions">
                <el-button @click="handleBack">取消</el-button>
                <el-button @click="handleSaveDraft" :loading="draftLoading">保存草稿</el-button>
                <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
                  <el-icon><Position /></el-icon>
                  提交审批
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
      
      <!-- 右侧：提示信息 -->
      <div class="side-tips">
        <el-card class="tips-card">
          <template #header>
            <span>填写提示</span>
          </template>
          
          <div class="tips-content">
            <div class="tip-item">
              <el-icon color="#409EFF"><InfoFilled /></el-icon>
              <div>
                <h4>标题规范</h4>
                <p>建议格式：姓名-类型-简要描述，如"张三-请假申请-4月5日"</p>
              </div>
            </div>
            
            <div class="tip-item">
              <el-icon color="#67C23A"><InfoFilled /></el-icon>
              <div>
                <h4>审批流程</h4>
                <p>提交后将按照预设流程依次审批，请确保信息准确</p>
              </div>
            </div>
            
            <div class="tip-item">
              <el-icon color="#E6A23C"><InfoFilled /></el-icon>
              <div>
                <h4>附件要求</h4>
                <p>如有相关证明材料，建议一并上传以加快审批速度</p>
              </div>
            </div>
            
            <div class="tip-item">
              <el-icon color="#F56C6C"><InfoFilled /></el-icon>
              <div>
                <h4>撤销说明</h4>
                <p>审批通过前可随时撤回申请，审批通过后如需变更请重新发起</p>
              </div>
            </div>
          </div>
        </el-card>
        
        <el-card class="approval-flow-card">
          <template #header>
            <span>审批流程</span>
          </template>
          
          <el-steps direction="vertical" :space="60" :active="3">
            <el-step title="提交申请" description="您提交申请表单" />
            <el-step title="直属上级审批" description="部门经理/主管审批" />
            <el-step title="HR复核" description="人力资源部复核" />
            <el-step title="审批完成" description="系统通知结果" />
          </el-steps>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Plus,
  Delete,
  Upload,
  Folder,
  Position,
  InfoFilled
} from '@element-plus/icons-vue'
import type { FormInstance, FormRules, UploadUserFile } from 'element-plus'
import {
  submitApproval,
  APPROVAL_TYPE_MAP,
  type ApprovalType,
  type ApprovalFormDTO
} from '@/api/approval'

const router = useRouter()
const route = useRoute()

// ============ 状态定义 ============

const formRef = ref<FormInstance>()
const loading = ref(false)
const submitLoading = ref(false)
const draftLoading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const isEdit = ref(false)

const form = reactive<{
  type: ApprovalType
  title: string
  content: string
  reason: string
  amount?: number
  startDate?: string
  endDate?: string
  formData: Record<string, any>
}>({
  type: 'LEAVE',
  title: '',
  content: '',
  reason: '',
  amount: undefined,
  startDate: '',
  endDate: '',
  formData: {
    leaveType: '',
    days: 1,
    expenseTypes: [],
    items: [{ description: '', amount: 0 }],
    overtimeType: '',
    hours: 4,
    purchaseItems: [{ name: '', quantity: 1, price: 0 }],
    supplier: '',
    destination: '',
    travelType: '',
    handoverTo: ''
  }
})

// 表单验证规则
const rules: FormRules = {
  type: [
    { required: true, message: '请选择审批类型', trigger: 'change' }
  ],
  title: [
    { required: true, message: '请输入申请标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度在 5 到 100 个字符', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请原因', trigger: 'blur' },
    { min: 10, message: '原因说明至少10个字符', trigger: 'blur' }
  ]
}

// ============ 生命周期 ============

onMounted(() => {
  // 检查是否有草稿或编辑
  const draftId = route.query.draft
  if (draftId) {
    loadDraft(draftId as string)
  }
  
  // 根据类型初始化表单数据
  initFormDataByType()
})

// ============ 方法 ============

/** 根据类型初始化表单数据 */
const handleTypeChange = () => {
  initFormDataByType()
  // 清除验证状态
  formRef.value?.clearValidate()
}

/** 初始化表单数据 */
const initFormDataByType = () => {
  switch (form.type) {
    case 'EXPENSE':
    case 'REIMBURSE':
      form.formData = {
        expenseTypes: [],
        items: [{ description: '', amount: 0 }]
      }
      break
    case 'PURCHASE':
      form.formData = {
        items: [{ name: '', quantity: 1, price: 0 }],
        supplier: ''
      }
      break
    case 'OVERTIME':
      form.formData = {
        overtimeType: 'WORKDAY',
        hours: 4
      }
      break
    case 'LEAVE':
      form.formData = {
        leaveType: '',
        days: 1
      }
      break
    default:
      form.formData = {}
  }
}

/** 添加费用明细 */
const addExpenseItem = () => {
  form.formData.items.push({ description: '', amount: 0 })
}

/** 删除费用明细 */
const removeExpenseItem = (index: number) => {
  if (form.formData.items.length > 1) {
    form.formData.items.splice(index, 1)
  }
}

/** 添加采购物品 */
const addPurchaseItem = () => {
  form.formData.items.push({ name: '', quantity: 1, price: 0 })
}

/** 删除采购物品 */
const removePurchaseItem = (index: number) => {
  if (form.formData.items.length > 1) {
    form.formData.items.splice(index, 1)
  }
}

/** 加载草稿 */
const loadDraft = (id: string) => {
  // TODO: 实际应该调用 API 获取草稿
  console.log('加载草稿:', id)
  isEdit.value = true
}

/** 保存草稿 */
const handleSaveDraft = async () => {
  draftLoading.value = true
  try {
    // TODO: 调用保存草稿 API
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('草稿已保存')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    draftLoading.value = false
  }
}

/** 提交审批 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    // 构建提交数据
    const submitData: ApprovalFormDTO = {
      type: form.type,
      title: form.title,
      content: form.content,
      reason: form.reason,
      amount: form.amount,
      startDate: form.startDate,
      endDate: form.endDate,
      attachments: fileList.value.map(f => f.name),
      formData: form.formData
    }
    
    submitLoading.value = true
    const res = await submitApproval(submitData)
    
    if (res.code === 200) {
      ElMessage.success('提交成功，审批流程已启动')
      router.push('/approval')
    }
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error(error.message || '请完善表单信息')
    }
  } finally {
    submitLoading.value = false
  }
}

/** 返回列表 */
const handleBack = () => {
  router.push('/approval')
}
</script>

<style lang="scss" scoped>
.approval-form-page {
  .page-nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
  }
  
  .form-content {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 16px;
    
    .main-form {
      .approval-form {
        .type-option {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 4px 0;
          
          .el-icon {
            font-size: 18px;
          }
        }
        
        .unit {
          margin-left: 8px;
          color: #666;
        }
        
        .expense-items,
        .purchase-items {
          width: 100%;
          
          .expense-item,
          .purchase-item {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 12px;
          }
        }
        
        .form-actions {
          display: flex;
          justify-content: center;
          gap: 16px;
          padding-top: 24px;
          border-top: 1px solid #f0f0f0;
        }
      }
    }
    
    .side-tips {
      display: flex;
      flex-direction: column;
      gap: 16px;
      
      .tips-card {
        .tips-content {
          .tip-item {
            display: flex;
            gap: 12px;
            margin-bottom: 16px;
            
            &:last-child {
              margin-bottom: 0;
            }
            
            .el-icon {
              font-size: 20px;
              margin-top: 4px;
            }
            
            h4 {
              margin: 0 0 4px;
              font-size: 14px;
              color: #333;
            }
            
            p {
              margin: 0;
              font-size: 12px;
              color: #666;
              line-height: 1.5;
            }
          }
        }
      }
      
      .approval-flow-card {
        .el-steps {
          padding-left: 20px;
        }
      }
    }
  }
}
</style>
