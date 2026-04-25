<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    class="asset-form"
  >
    <el-form-item label="资产名称" prop="assetName">
      <el-input v-model="formData.assetName" placeholder="请输入资产名称" />
    </el-form-item>
    
    <el-form-item label="资产分类" prop="categoryId">
      <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
        <el-option
          v-for="item in categories"
          :key="item.id"
          :label="item.categoryName"
          :value="item.id"
        />
      </el-select>
    </el-form-item>
    
    <el-form-item label="资产编码" prop="assetCode">
      <el-input v-model="formData.assetCode" placeholder="自动生成或手动输入" :disabled="isEdit">
        <template #append>
          <el-button @click="generateAssetCode">生成</el-button>
        </template>
      </el-input>
    </el-form-item>
    
    <el-form-item label="型号规格" prop="model">
      <el-input v-model="formData.model" placeholder="请输入型号规格" />
    </el-form-item>
    
    <el-form-item label="品牌" prop="brand">
      <el-input v-model="formData.brand" placeholder="请输入品牌" />
    </el-form-item>
    
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="数量" prop="currentQuantity">
          <el-input-number
            v-model="formData.currentQuantity"
            :min="0"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="预警数量" prop="warningQuantity">
          <el-input-number
            v-model="formData.warningQuantity"
            :min="0"
            :precision="0"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>
    
    <el-form-item label="单价" prop="purchasePrice">
      <el-input-number
        v-model="formData.purchasePrice"
        :min="0"
        :precision="2"
        style="width: 100%"
      >
        <template #prefix>¥</template>
      </el-input-number>
    </el-form-item>
    
    <el-form-item label="购买日期" prop="purchaseDate">
      <el-date-picker
        v-model="formData.purchaseDate"
        type="date"
        placeholder="选择购买日期"
        style="width: 100%"
        value-format="YYYY-MM-DD"
      />
    </el-form-item>
    
    <el-form-item label="负责人" prop="responsiblePerson">
      <el-input v-model="formData.responsiblePerson" placeholder="请输入负责人姓名" />
    </el-form-item>
    
    <el-form-item label="存放位置" prop="location">
      <el-input v-model="formData.location" placeholder="请输入存放位置" />
    </el-form-item>
    
    <el-form-item label="资产状态" prop="assetStatus">
      <el-select v-model="formData.assetStatus" placeholder="请选择状态" style="width: 100%">
        <el-option label="正常" value="1" />
        <el-option label="领用中" value="2" />
        <el-option label="维修中" value="3" />
        <el-option label="报废" value="4" />
      </el-select>
    </el-form-item>
    
    <el-form-item label="备注" prop="remark">
      <el-input
        v-model="formData.remark"
        type="textarea"
        :rows="3"
        placeholder="请输入备注信息"
      />
    </el-form-item>
    
    <el-form-item>
      <el-button type="primary" @click="handleSubmit">保存</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  formData: {
    type: Object,
    default: () => ({})
  },
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['submit', 'cancel'])

const formRef = ref()
const formData = ref({ ...props.formData })

// 计算是否为编辑模式
const isEdit = computed(() => {
  return !!formData.value.id
})

// 表单验证规则
const rules = reactive({
  assetName: [
    { required: true, message: '请输入资产名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择资产分类', trigger: 'change' }
  ],
  currentQuantity: [
    { required: true, message: '请输入数量', trigger: 'blur' }
  ],
  purchasePrice: [
    { required: true, message: '请输入单价', trigger: 'blur' }
  ]
})

// 监听props变化
watch(() => props.formData, (newVal) => {
  formData.value = { ...newVal }
}, { immediate: true })

// 生成资产编码
const generateAssetCode = () => {
  const timestamp = Date.now().toString().slice(-6)
  const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  formData.value.assetCode = `ASSET-${timestamp}-${random}`
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    emit('submit', formData.value)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 取消
const handleCancel = () => {
  emit('cancel')
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
}
</script>

<style scoped>
.asset-form {
  padding: 20px 0;
}

.el-row {
  margin-bottom: 0;
}
</style>