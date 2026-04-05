# AI-OA UI组件库规格书 V1.0

> 文档版本：V1.0
> 更新日期：2026-04-05

---

## 目录

1. [组件分类](#1-组件分类)
2. [基础组件](#2-基础组件)
3. [业务组件](#3-业务组件)
4. [页面模板](#4-页面模板)

---

## 1. 组件分类

### 1.1 组件层级

```
┌─────────────────────────────────────────────────────────────┐
│                    组件层级结构                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Foundation Layer (基础层)                                  │
│  └── Colors, Typography, Spacing, Icons                    │
│                                                             │
│  Component Layer (组件层)                                   │
│  └── Button, Input, Card, Table, Modal                     │
│                                                             │
│  Pattern Layer (模式层)                                    │
│  └── Form, List, Search, Filter, Navigation                 │
│                                                             │
│  Page Layer (页面层)                                       │
│  └── Dashboard, Detail, List, Form                         │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 组件清单

#### 基础组件（18个）

| 组件 | 说明 | 状态 |
|------|------|------|
| AButton | 按钮 | ✅ |
| AInput | 输入框 | ✅ |
| ASelect | 选择器 | ✅ |
| ACascader | 级联选择 | ✅ |
| ADatePicker | 日期选择 | ✅ |
| ATimePicker | 时间选择 | ✅ |
| ASwitch | 开关 | ✅ |
| ASlider | 滑块 | ✅ |
| ARadio | 单选框 | ✅ |
| ACheckbox | 多选框 | ✅ |
| AInputNumber | 数字输入 | ✅ |
| ATextarea | 文本域 | ✅ |
| AUpload | 上传 | ✅ |
| ACard | 卡片 | ✅ |
| ATable | 表格 | ✅ |
| APagination | 分页 | ✅ |
| ATag | 标签 | ✅ |
| AProgress | 进度条 | ✅ |

#### 业务组件（12个）

| 组件 | 说明 | 状态 |
|------|------|------|
| ApprovalCard | 审批卡片 | ✅ |
| ReimburseItem | 报销项 | ✅ |
| ChatBubble | 聊天气泡 | ✅ |
| AIResponseCard | AI回复卡片 | ✅ |
| QuickAction | 快捷操作 | ✅ |
| WorkSummary | 工作汇总 | ✅ |
| InvoiceUploader | 发票上传器 | ✅ |
| ApprovalFlow | 审批流程图 | ✅ |
| ActivityTimeline | 活动时间线 | ✅ |
| StatisticsCard | 统计卡片 | ✅ |
| EmptyState | 空状态 | ✅ |
| ErrorBoundary | 错误边界 | ✅ |

---

## 2. 基础组件

### 2.1 AButton 按钮

```vue
<!-- 按钮类型 -->
<a-button type="primary">主要按钮</a-button>
<a-button>默认按钮</a-button>
<a-button type="success">成功按钮</a-button>
<a-button type="warning">警告按钮</a-button>
<a-button type="danger">危险按钮</a-button>
<a-button type="info">信息按钮</a-button>

<!-- 按钮尺寸 -->
<a-button size="large">大按钮</a-button>
<a-button size="medium">中按钮</a-button>
<a-button size="small">小按钮</a-button>

<!-- 按钮状态 -->
<a-button loading>加载中</a-button>
<a-button disabled>禁用</a-button>
<a-button icon="search">图标按钮</a-button>
```

**规格**：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| type | string | default | primary/success/warning/danger/info |
| size | string | medium | large/medium/small |
| loading | boolean | false | 加载状态 |
| disabled | boolean | false | 禁用状态 |
| icon | string | - | 图标名称 |

### 2.2 AInput 输入框

```vue
<!-- 基本用法 -->
<a-input v-model="value" placeholder="请输入" />

<!-- 前缀/后缀 -->
<a-input v-model="value" prefix="¥" suffix="元" />

<!-- 带图标 -->
<a-input v-model="value" prefix-icon="search" />
<a-input v-model="value" suffix-icon="close" />

<!-- 搜索框 -->
<a-input-search v-model="value" placeholder="搜索" />

<!-- 带字数统计 -->
<a-input v-model="value" :maxlength="100" show-word-limit />
```

**规格**：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| type | string | text | text/textarea/password |
| v-model | string | - | 绑定值 |
| placeholder | string | - | 占位文本 |
| maxlength | number | - | 最大长度 |
| show-word-limit | boolean | false | 显示字数统计 |
| prefix | string | - | 前缀文字 |
| suffix | string | - | 后缀文字 |
| prefix-icon | string | - | 前缀图标 |
| suffix-icon | string | - | 后缀图标 |
| clearable | boolean | false | 可清除 |
| disabled | boolean | false | 禁用 |

### 2.3 ASelect 选择器

```vue
<!-- 基本用法 -->
<a-select v-model="value" :options="options" placeholder="请选择" />

<!-- 多选 -->
<a-select v-model="value" multiple placeholder="请选择" />

<!-- 带搜索 -->
<a-select v-model="value" filterable placeholder="请选择" />

<!-- 分组 -->
<a-select v-model="value">
  <a-select-group label="分组1">
    <a-select-option value="1">选项1</a-select-option>
  </a-select-group>
</a-select>
```

**规格**：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| v-model | string/number/array | - | 绑定值 |
| options | array | [] | 选项列表 |
| multiple | boolean | false | 多选 |
| filterable | boolean | false | 可搜索 |
| clearable | boolean | false | 可清除 |
| disabled | boolean | false | 禁用 |
| placeholder | string | - | 占位文本 |

### 2.4 ACard 卡片

```vue
<!-- 基本卡片 -->
<a-card title="卡片标题">
  卡片内容
</a-card>

<!-- 带操作 -->
<a-card title="卡片标题">
  <template #extra>
    <a-button size="small">操作</a-button>
  </template>
  卡片内容
</a-card>

<!-- 带图片 -->
<a-card :image="imgUrl" title="图片卡片">
  卡片内容
</a-card>

<!-- 加载状态 -->
<a-card :loading="true">
  卡片内容
</a-card>
```

**规格**：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| title | string | - | 卡片标题 |
| extra | string/slot | - | 右上角操作 |
| image | string | - | 图片URL |
| shadow | string | hover | never/hover/always |

### 2.5 ATable 表格

```vue
<a-table
  :data="tableData"
  :columns="columns"
  :loading="loading"
  :pagination="pagination"
  :row-key="row => row.id"
  :selection="true"
  stripe
  border
  @selection-change="handleSelection"
>
  <!-- 自定义列 -->
  <template #status="{ row }">
    <a-tag :type="getStatusType(row.status)">
      {{ row.statusText }}
    </a-tag>
  </template>
  
  <!-- 操作列 -->
  <template #action="{ row }">
    <a-button size="small" @click="handleView(row)">查看</a-button>
    <a-button size="small" type="danger" @click="handleDelete(row)">删除</a-button>
  </template>
</a-table>
```

**规格**：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| data | array | [] | 表格数据 |
| columns | array | [] | 列配置 |
| loading | boolean | false | 加载状态 |
| pagination | object/boolean | true | 分页配置 |
| row-key | string/function | id | 行key |
| selection | boolean | false | 多选 |
| stripe | boolean | false | 斑马纹 |
| border | boolean | false | 边框 |

### 2.6 ATag 标签

```vue
<!-- 类型 -->
<a-tag>默认</a-tag>
<a-tag type="success">成功</a-tag>
<a-tag type="warning">警告</a-tag>
<a-tag type="danger">危险</a-tag>
<a-tag type="info">信息</a-tag>

<!-- 主题 -->
<a-tag theme="light">浅色</a-tag>
<a-tag theme="dark">深色</a-tag>
<a-tag theme="outline">描边</a-tag>

<!-- 可关闭 -->
<a-tag closable @close="handleClose">可关闭标签</a-tag>

<!-- 可选中 -->
<a-tag checkable :checked="checked">可选中标签</a-tag>
```

### 2.7 AProgress 进度条

```vue
<!-- 线性进度 -->
<a-progress :percentage="60" />
<a-progress :percentage="60" :stroke-width="20" />
<a-progress :percentage="60" status="success" />

<!-- 环形进度 -->
<a-progress type="circle" :percentage="60" :width="120" />

<!-- 阶梯进度 -->
<a-progress type="dashboard" :percentage="60" />

<!-- 带有动画 -->
<a-progress :percentage="60" :stroke-width="20" animated />
```

### 2.8 APagination 分页

```vue
<!-- 基本分页 -->
<a-pagination
  v-model:current="current"
  :total="1000"
  :page-size="20"
  @change="handleChange"
/>

<!-- 带有跳转 -->
<a-pagination
  v-model:current="current"
  :total="1000"
  show-quick-jumper
  show-total
/>

<!-- 简洁模式 -->
<a-pagination
  v-model:current="current"
  :total="1000"
  layout="prev, pager, next"
/>
```

---

## 3. 业务组件

### 3.1 ApprovalCard 审批卡片

```vue
<!-- 审批卡片组件 -->
<approval-card
  :approval="approvalData"
  @approve="handleApprove"
  @reject="handleReject"
  @transfer="handleTransfer"
/>

<!-- 或者使用插槽 -->
<approval-card :approval="approvalData">
  <template #header>
    <div class="approval-header">
      <span class="approval-type">{{ approval.typeName }}</span>
      <a-tag :type="getStatusType(approval.status)">
        {{ approval.statusName }}
      </a-tag>
    </div>
  </template>
  
  <template #body>
    <div class="approval-body">
      <div class="requester">
        <a-avatar :src="approval.requester.avatar" />
        <span>{{ approval.requester.name }}</span>
      </div>
      <div class="info">
        <div class="info-item">
          <span class="label">提交时间</span>
          <span class="value">{{ approval.createTime }}</span>
        </div>
      </div>
    </div>
  </template>
  
  <template #footer>
    <div class="approval-actions">
      <a-button type="text" @click="handleView">查看详情</a-button>
      <a-button type="primary" @click="handleApprove">同意</a-button>
      <a-button type="danger" @click="handleReject">驳回</a-button>
    </div>
  </template>
</approval-card>
```

**组件状态**：

| 状态 | 样式 |
|------|------|
| 待审批 | 蓝色边框，左侧蓝色标记 |
| 已通过 | 绿色边框，左侧绿色标记 |
| 已驳回 | 红色边框，左侧红色标记 |
| 已取消 | 灰色边框，禁用操作 |

### 3.2 InvoiceUploader 发票上传器

```vue
<!-- OCR发票上传组件 -->
<invoice-uploader
  v-model="invoiceList"
  :max-count="10"
  :max-size="5"
  accept=".jpg,.jpeg,.png,.pdf"
  @upload="handleUpload"
  @remove="handleRemove"
  @ocr-complete="handleOcrComplete"
/>

<!-- 上传结果 -->
<template #result="{ file, ocrResult }">
  <div class="invoice-result">
    <img :src="file.url" class="invoice-image" />
    <div class="invoice-info">
      <div class="confidence" :class="getConfidenceClass(ocrResult.confidence)">
        置信度: {{ (ocrResult.confidence * 100).toFixed(0) }}%
      </div>
      <div class="amount">
        金额: ¥{{ ocrResult.amount }}
      </div>
    </div>
  </div>
</template>
```

**规格**：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| v-model | array | [] | 已上传文件列表 |
| max-count | number | 10 | 最大上传数 |
| max-size | number | 5 | 单个文件大小限制(MB) |
| accept | string | .jpg,.png,.pdf | 接受的文件类型 |

**OCR结果**：

| 字段 | 类型 | 说明 |
|------|------|------|
| confidence | number | 识别置信度 0-1 |
| amount | number | 识别金额 |
| invoiceNo | string | 发票号码 |
| invoiceDate | string | 开票日期 |

### 3.3 AIResponseCard AI回复卡片

```vue
<!-- AI回复组件 -->
<ai-response-card
  :response="aiResponse"
  @copy="handleCopy"
  @regenerate="handleRegenerate"
  @feedback="handleFeedback"
/>

<!-- 组件内容 -->
<template>
  <div class="ai-response-card">
    <div class="response-header">
      <a-avatar icon="robot" />
      <span class="model-name">AI助手</span>
      <span class="model-tag">{{ response.model }}</span>
    </div>
    
    <div class="response-content">
      <div class="markdown-body" v-html="renderedContent" />
    </div>
    
    <!-- 相关链接 -->
    <div v-if="response.links?.length" class="response-links">
      <div class="links-title">相关链接</div>
      <a
        v-for="link in response.links"
        :key="link.url"
        :href="link.url"
        class="link-item"
      >
        <span class="link-icon">{{ getLinkIcon(link.type) }}</span>
        <span class="link-title">{{ link.title }}</span>
      </a>
    </div>
    
    <div class="response-actions">
      <a-button size="small" @click="$emit('copy')">
        <CopyIcon /> 复制
      </a-button>
      <a-button size="small" @click="$emit('regenerate')">
        <RefreshIcon /> 重新生成
      </a-button>
      <a-button size="small" @click="$emit('feedback', 'good')">
        <ThumbUpIcon /> 有帮助
      </a-button>
      <a-button size="small" @click="$emit('feedback', 'bad')">
        <ThumbDownIcon /> 没帮助
      </a-button>
    </div>
  </div>
</template>
```

### 3.4 QuickAction 快捷操作

```vue
<!-- 快捷操作组件 -->
<quick-action
  :actions="quickActions"
  @select="handleSelect"
/>

<!-- 配置 -->
<script setup>
const quickActions = [
  { key: 'approval', label: '提交审批', icon: 'document', color: '#667EEA' },
  { key: 'ocr', label: '拍照识别', icon: 'camera', color: '#10B981' },
  { key: 'ai', label: 'AI助手', icon: 'chat', color: '#A855F7' },
  { key: 'report', label: '生成报表', icon: 'chart', color: '#F59E0B' },
]
</script>
```

**布局规格**：

| 屏幕 | 列数 | 间距 |
|------|------|------|
| 手机 | 3列 | 12px |
| 平板 | 4列 | 16px |
| 桌面 | 6列 | 20px |

### 3.5 StatisticsCard 统计卡片

```vue
<!-- 统计卡片组件 -->
<statistics-card
  title="本月支出"
  :value="12345"
  prefix="¥"
  :trend="+12.5"
  :precision="2"
  icon="expense"
  theme="primary"
/>

<!-- 大数字展示 -->
<statistics-card
  title="审批效率"
  :value="98.5"
  suffix="%"
  :trend="-5.2"
  trend-type="down"
  theme="success"
/>
```

**主题色**：

| 主题 | 颜色 | 用途 |
|------|------|------|
| primary | #667EEA | 主品牌色 |
| success | #10B981 | 成功/增长 |
| warning | #F59E0B | 警告 |
| danger | #EF4444 | 危险/下降 |

### 3.6 EmptyState 空状态

```vue
<!-- 完整空状态 -->
<empty-state
  image="/images/empty/approval.svg"
  title="暂无审批记录"
  description="您还没有需要审批的单据"
>
  <template #actions>
    <a-button type="primary" @click="handleCreate">发起审批</a-button>
    <a-button @click="handleExample">查看示例</a-button>
  </template>
</empty-state>

<!-- 紧凑空状态 -->
<empty-state
  type="inline"
  description="暂无数据"
/>

<!-- 搜索结果为空 -->
<empty-state
  type="search"
  :keyword="searchKeyword"
  @retry="handleRetry"
/>
```

**空状态类型**：

| 类型 | 使用场景 |
|------|----------|
| list | 列表为空 |
| search | 搜索无结果 |
| filter | 筛选无结果 |
| inline | 内联空状态 |

### 3.7 ActivityTimeline 活动时间线

```vue
<!-- 活动时间线 -->
<activity-timeline :activities="activities">
  <template #item="{ activity }">
    <div class="activity-item">
      <div class="activity-header">
        <a-avatar :src="activity.user.avatar" size="small" />
        <span class="user-name">{{ activity.user.name }}</span>
        <span class="action-type">{{ activity.action }}</span>
      </div>
      <div class="activity-content">
        {{ activity.content }}
      </div>
      <div class="activity-time">
        {{ formatTime(activity.createTime) }}
      </div>
    </div>
  </template>
</activity-timeline>
```

### 3.8 ErrorBoundary 错误边界

```vue
<!-- 错误边界组件 -->
<error-boundary
  :fallback="ErrorFallback"
  @error-reported="handleErrorReport"
>
  <component-with-potential-error />
</error-boundary>

<!-- 自定义错误页面 -->
<template #fallback="{ error, reset }">
  <div class="error-page">
    <img src="/images/error/bug.svg" />
    <h2>抱歉，出现了一些问题</h2>
    <p>我们已经收到错误报告，会尽快处理</p>
    <a-button @click="reset">重试</a-button>
  </div>
</template>
```

---

## 4. 页面模板

### 4.1 列表页模板

```vue
<template>
  <div class="list-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">{{ pageTitle }}</h1>
      <div class="page-actions">
        <a-button type="primary" @click="handleCreate">
          <PlusIcon />新建
        </a-button>
      </div>
    </div>
    
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <a-input-search
        v-model="filters.keyword"
        placeholder="搜索..."
        @search="handleSearch"
      />
      <a-select
        v-model="filters.status"
        :options="statusOptions"
        placeholder="状态"
        clearable
      />
      <a-date-picker
        v-model="filters.dateRange"
        type="daterange"
        placeholder="时间范围"
      />
    </div>
    
    <!-- 批量操作栏 -->
    <div v-if="selectedCount > 0" class="batch-bar">
      <span>已选择 {{ selectedCount }} 项</span>
      <a-button @click="clearSelection">清除</a-button>
      <a-button type="danger" @click="handleBatchDelete">批量删除</a-button>
    </div>
    
    <!-- 数据表格 -->
    <a-table
      :data="dataList"
      :columns="columns"
      :loading="loading"
      :pagination="pagination"
      :selection="true"
      @selection-change="handleSelectionChange"
    >
      <!-- 自定义列插槽 -->
    </a-table>
  </div>
</template>
```

### 4.2 详情页模板

```vue
<template>
  <div class="detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <a-button @click="goBack">
        <ArrowLeftIcon />返回
      </a-button>
      <div class="page-actions">
        <a-button v-if="canEdit" type="primary" @click="handleEdit">
          编辑
        </a-button>
        <a-dropdown>
          <a-button>更多</a-button>
          <template #dropdown>
            <a-dropdown-menu>
              <a-dropdown-item @click="handleExport">导出</a-dropdown-item>
              <a-dropdown-item @click="handlePrint">打印</a-dropdown-item>
              <a-dropdown-item divided @click="handleDelete">删除</a-dropdown-item>
            </a-dropdown-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
    
    <!-- 详情内容 -->
    <a-card>
      <a-descriptions :column="2">
        <a-descriptions-item label="名称">
          {{ detail.name }}
        </a-descriptions-item>
        ...
      </a-descriptions>
    </a-card>
    
    <!-- 相关操作记录 -->
    <a-card title="操作记录">
      <activity-timeline :activities="activities" />
    </a-card>
  </div>
</template>
```

### 4.3 表单页模板

```vue
<template>
  <div class="form-page">
    <a-card>
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
      >
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          <a-row :gutter="24">
            <a-col :span="12">
              <el-form-item label="名称" prop="name">
                <a-input v-model="formData.name" />
              </el-form-item>
            </a-col>
            <a-col :span="12">
              <el-form-item label="类型" prop="type">
                <a-select v-model="formData.type" :options="typeOptions" />
              </el-form-item>
            </a-col>
          </a-row>
        </div>
        
        <!-- 其他字段... -->
        
        <!-- 附件 -->
        <div class="form-section">
          <h3 class="section-title">附件</h3>
          <a-upload />
        </div>
      </el-form>
      
      <!-- 表单操作 -->
      <div class="form-actions">
        <a-button @click="goBack">取消</a-button>
        <a-button @click="handleSaveDraft">保存草稿</a-button>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">
          提交
        </a-button>
      </div>
    </a-card>
  </div>
</template>
```

---

## 附录A：组件自检清单

```markdown
## 组件开发自检清单

### 功能性
- [ ] 组件功能完整
- [ ] Props定义合理
- [ ] Events定义完整
- [ ] Slots定义合理
- [ ] 支持v-model

### 样式
- [ ] 样式隔离（scoped）
- [ ] Design Token使用
- [ ] 响应式适配
- [ ] 暗色模式支持

### 无障碍
- [ ] 键盘导航支持
- [ ] ARIA标签
- [ ] 屏幕阅读器友好
- [ ] 焦点管理

### 文档
- [ ] Props文档完整
- [ ] Events文档完整
- [ ] Slots文档完整
- [ ] 示例代码完整
```

---

*文档版本：V1.0*
*更新日期：2026-04-05*
