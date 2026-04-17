<template>
  <div class="search-form-container">
    <el-form
      :model="modelValue"
      :inline="inline"
      :label-width="labelWidth"
      class="search-form"
    >
      <el-row :gutter="16">
        <el-col v-for="field in fields" :key="field.prop" :span="field.span || 6">
          <el-form-item :label="field.label" :prop="field.prop">
            <!-- 文本输入 -->
            <el-input
              v-if="field.type === 'input'"
              v-model="modelValue[field.prop]"
              :placeholder="field.placeholder || `请输入${field.label}`"
              :clearable="field.clearable !== false"
              @keyup.enter="handleSearch"
            />

            <!-- 数字输入 -->
            <el-input-number
              v-else-if="field.type === 'number'"
              v-model="modelValue[field.prop]"
              :placeholder="field.placeholder"
              :min="field.min"
              :max="field.max"
              :precision="field.precision"
              style="width: 100%"
            />

            <!-- 下拉选择 -->
            <el-select
              v-else-if="field.type === 'select'"
              v-model="modelValue[field.prop]"
              :placeholder="field.placeholder || `请选择${field.label}`"
              :clearable="field.clearable !== false"
              :multiple="field.multiple"
              style="width: 100%"
            >
              <el-option
                v-for="option in field.options"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>

            <!-- 日期选择 -->
            <el-date-picker
              v-else-if="field.type === 'date'"
              v-model="modelValue[field.prop]"
              :type="field.dateType || 'date'"
              :placeholder="field.placeholder || `请选择${field.label}`"
              :format="field.format || 'YYYY-MM-DD'"
              :value-format="field.valueFormat || 'YYYY-MM-DD'"
              style="width: 100%"
            />

            <!-- 日期范围 -->
            <el-date-picker
              v-else-if="field.type === 'daterange'"
              v-model="modelValue[field.prop]"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              :format="field.format || 'YYYY-MM-DD'"
              :value-format="field.valueFormat || 'YYYY-MM-DD'"
              style="width: 100%"
            />

            <!-- 级联选择 -->
            <el-cascader
              v-else-if="field.type === 'cascader'"
              v-model="modelValue[field.prop]"
              :options="field.options"
              :props="field.cascaderProps"
              :placeholder="field.placeholder"
              :clearable="field.clearable !== false"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>

        <!-- 操作按钮 -->
        <el-col :span="6" class="search-actions">
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <slot name="extra-actions" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup lang="ts">
interface SearchField {
  prop: string
  label: string
  type: 'input' | 'number' | 'select' | 'date' | 'daterange' | 'cascader'
  span?: number
  placeholder?: string
  clearable?: boolean
  options?: Array<{ label: string; value: any }>
  multiple?: boolean
  dateType?: 'date' | 'datetime' | 'month' | 'year'
  format?: string
  valueFormat?: string
  min?: number
  max?: number
  precision?: number
  cascaderProps?: Record<string, any>
}

defineProps<{
  modelValue: Record<string, any>
  fields: SearchField[]
  inline?: boolean
  labelWidth?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
  search: []
  reset: []
}>()

const handleSearch = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}
</script>

<style lang="scss" scoped>
.search-form-container {
  margin-bottom: 16px;

  .search-form {
    .search-actions {
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
