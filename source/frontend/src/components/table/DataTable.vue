<template>
  <div class="data-table-container">
    <el-table
      v-loading="loading"
      :data="data"
      :row-key="rowKey"
      :stripe="stripe"
      :border="border"
      :size="size"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column v-if="showSelection" type="selection" width="55" fixed="left" />
      <el-table-column v-if="showIndex" type="index" label="序号" width="60" fixed="left" />

      <el-table-column
        v-for="col in columns"
        :key="col.prop"
        :prop="col.prop"
        :label="col.label"
        :width="col.width"
        :min-width="col.minWidth"
        :fixed="col.fixed"
        :sortable="col.sortable"
        :formatter="col.formatter"
        :align="col.align || 'left'"
      >
        <template v-if="col.slot" #default="scope">
          <slot :name="col.slot" :row="scope.row" :index="scope.$index" />
        </template>
        <template v-else-if="col.type === 'tag'">
          <el-tag :type="col.tagType ? col.tagType(scope.row) : 'info'">
            {{ col.tagLabel ? col.tagLabel(scope.row) : scope.row[col.prop] }}
          </el-tag>
        </template>
        <template v-else-if="col.type === 'status'">
          <el-tag :type="getStatusType(scope.row[col.prop])">
            {{ getStatusLabel(scope.row[col.prop]) }}
          </el-tag>
        </template>
        <template v-else-if="col.type === 'amount'">
          <span class="amount-text">¥{{ formatAmount(scope.row[col.prop]) }}</span>
        </template>
        <template v-else-if="col.type === 'datetime'">
          {{ formatDateTime(scope.row[col.prop]) }}
        </template>
        <template v-else-if="col.type === 'actions'">
          <div class="action-buttons">
            <slot name="actions" :row="scope.row" :index="scope.$index" />
          </div>
        </template>
      </el-table-column>

      <template #empty>
        <el-empty :description="emptyText" />
      </template>
    </el-table>

    <el-pagination
      v-if="showPagination"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      :layout="paginationLayout"
      class="pagination"
      @size-change="handleSizeChange"
      @current-change="handlePageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import dayjs from 'dayjs'

interface TableColumn {
  prop: string
  label: string
  width?: number | string
  minWidth?: number | string
  fixed?: boolean | 'left' | 'right'
  sortable?: boolean | 'custom'
  align?: 'left' | 'center' | 'right'
  slot?: string
  type?: 'tag' | 'status' | 'amount' | 'datetime' | 'actions'
  tagType?: (row: any) => string
  tagLabel?: (row: any) => string
  formatter?: (row: any, column: any, cellValue: any, index: number) => string
}

const props = withDefaults(defineProps<{
  data: any[]
  columns: TableColumn[]
  loading?: boolean
  rowKey?: string
  stripe?: boolean
  border?: boolean
  size?: 'large' | 'default' | 'small'
  showSelection?: boolean
  showIndex?: boolean
  showPagination?: boolean
  total?: number
  currentPage?: number
  pageSize?: number
  pageSizes?: number[]
  paginationLayout?: string
  emptyText?: string
  statusMap?: Record<string, { label: string; type: string }>
}>(), {
  rowKey: 'id',
  stripe: true,
  border: false,
  size: 'default',
  showSelection: false,
  showIndex: false,
  showPagination: true,
  total: 0,
  currentPage: 1,
  pageSize: 10,
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
  emptyText: '暂无数据',
  statusMap: () => ({})
})

const emit = defineEmits<{
  'selection-change': [selection: any[]]
  'sort-change': [sort: { prop: string; order: string }]
  'size-change': [size: number]
  'page-change': [page: number]
  'query': [params: { page: number; pageSize: number }]
}>()

const handleSelectionChange = (selection: any[]) => {
  emit('selection-change', selection)
}

const handleSortChange = ({ prop, order }: { prop: string; order: string }) => {
  emit('sort-change', { prop, order })
}

const handleSizeChange = (size: number) => {
  emit('size-change', size)
  emit('query', { page: 1, pageSize: size })
}

const handlePageChange = (page: number) => {
  emit('page-change', page)
  emit('query', { page, pageSize: props.pageSize })
}

const getStatusType = (status: string) => {
  return props.statusMap[status]?.type || 'info'
}

const getStatusLabel = (status: string) => {
  return props.statusMap[status]?.label || status
}

const formatAmount = (amount: number) => {
  if (amount == null) return '0.00'
  return Number(amount).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

const formatDateTime = (value: string) => {
  if (!value) return '-'
  return dayjs(value).format('YYYY-MM-DD HH:mm')
}
</script>

<style lang="scss" scoped>
.data-table-container {
  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }

  .amount-text {
    color: #f56c6c;
    font-weight: 500;
  }

  .action-buttons {
    display: flex;
    gap: 8px;
  }
}
</style>
