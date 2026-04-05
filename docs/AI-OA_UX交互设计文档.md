# AI-OA UX交互设计文档 V1.0

> 文档版本：V1.0
> 更新日期：2026-04-05

---

## 目录

1. [用户角色分析](#1-用户角色分析)
2. [核心用户流程](#2-核心用户流程)
3. [交互模式库](#3-交互模式库)
4. [反馈系统](#4-反馈系统)
5. [错误处理](#5-错误处理)
6. [空状态设计](#6-空状态设计)
7. [加载状态](#7-加载状态)

---

## 1. 用户角色分析

### 1.1 角色矩阵

| 角色 | 占比 | 核心诉求 | 痛点 |
|------|------|----------|------|
| 普通员工 | 70% | 高效完成日常工作 | 操作复杂、找不到功能 |
| 部门经理 | 20% | 审批效率、团队管理 | 审批多、统计难 |
| 系统管理员 | 5% | 系统稳定、配置灵活 | 配置复杂、维护成本高 |
| 高层领导 | 5% | 数据洞察、决策支持 | 信息分散、汇总慢 |

### 1.2 角色卡片

```
┌─────────────────────────────────────────────────────────────┐
│  👤 普通员工                                               │
├─────────────────────────────────────────────────────────────┤
│  画像：                                                     │
│  • 每天使用OA系统1-2小时                                  │
│  • 主要操作：提交请假/报销/查看通知                        │
│  • 技术能力：一般，能接受新事物                            │
│                                                             │
│  目标：                                                     │
│  • 快速完成日常操作                                        │
│  • 了解审批进度                                            │
│  • 获取公司通知                                            │
│                                                             │
│  痛点：                                                     │
│  • 表单填写繁琐                                            │
│  • 审批状态不透明                                          │
│  • 不知道找谁问问题                                        │
└─────────────────────────────────────────────────────────────┘
```

### 1.3 角色优先级

```
高频用户优先 → 普通员工的操作效率是首位
└── 简化操作路径
└── 提供快捷操作
└── 智能默认值

审批用户优先 → 经理的审批效率很重要
└── 快速审批操作
└── 批量审批
└── 审批历史追溯
```

---

## 2. 核心用户流程

### 2.1 请假申请流程

```
用户旅程图：请假申请
═══════════════════════════════════════════════════════════════

🎯 目标：成功提交请假申请

📍 接触点：首页 → 审批 → 新建审批 → 填写表单 → 提交

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  阶段      发现          意图         行动          反馈   │
│                                                             │
│  员工     看到待办       想请假      点击请假      收到确认 │
│           首页入口                     入口             │
│              ↓                        ↓               ↓    │
│         [首页工作台] → [审批中心] → [新建请假] → [提交成功] │
│              ↓                        ↓               ↓    │
│         💡 快捷入口    📝 审批类型   ⏱️ 填写表单   ✅ 成功 │
│                                                             │
│  情感线   😊            🤔           😓           😄        │
│           ───────────────────────────────────────→         │
│                                                             │
│  机会点                                                      │
│  • 首页快捷入口 → 减少操作步骤                             │
│  • 智能日历 → 快速选择日期                                 │
│  • 历史请假 → 快速复用                                     │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 审批流程（经理视角）

```
审批流程优化
═══════════════════════════════════════════════════════════════

🎯 目标：快速完成审批决策

📍 当前流程（痛点）：
❌ 打开邮箱 → 复制信息 → 打开OA → 粘贴信息 → 审批
   约3-5分钟/单

📍 优化后流程：
✅ 收到通知 → 一键审批 → 完成
   约10秒/单

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  优化策略：                                                   │
│                                                             │
│  1️⃣ 消息推送即审批                                          │
│     └── 钉钉/企业微信推送审批详情                           │
│     └── 点击推送直接打开审批页面                            │
│                                                             │
│  2️⃣ 快捷审批操作                                           │
│     └── 左滑审批 → 同意/驳回                               │
│     └── 快捷备注 → 一键通过                                │
│                                                             │
│  3️⃣ 批量审批                                               │
│     └── 多选 → 批量同意                                    │
│     └── 批量驳回（需填写原因）                              │
│                                                             │
│  4️⃣ 审批建议                                               │
│     └── AI根据历史数据给出审批建议                          │
│     └── 参考：申请人历史记录/部门统计                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 OCR报销流程

```
OCR智能报销流程
═══════════════════════════════════════════════════════════════

┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  📱 员工操作                                                │
│                                                             │
│  步骤1：拍照/上传发票                                       │
│         ↓                                                  │
│  步骤2：AI自动识别发票信息                                  │
│         ↓                                                  │
│  步骤3：确认/修改信息                                       │
│         ↓                                                  │
│  步骤4：提交报销                                            │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  🤖 AI能力                                                 │
│                                                             │
│  • 自动识别发票类型                                         │
│  • 自动提取：金额/日期/发票代码/购买方                      │
│  • 自动校验：税额计算/信息一致性                            │
│  • 自动分类：交通/餐饮/办公/差旅                            │
│  • 异常预警：金额异常/日期异常/重复发票                     │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ⚠️ 置信度处理                                             │
│                                                             │
│  置信度≥85% → 绿色标识 → 可直接提交                       │
│  置信度<85% → 黄色高亮 → 需人工确认                        │
│  置信度<60% → 红色标记 → 需重新上传                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. 交互模式库

### 3.1 快捷操作模式

```vue
<!-- 模式1：快捷入口卡片 -->
<el-card class="quick-action">
  <div class="quick-actions">
    <div class="action-item">
      <el-icon><Plus /></el-icon>
      <span>新建审批</span>
    </div>
    <div class="action-item">
      <el-icon><Camera /></el-icon>
      <span>拍照识别</span>
    </div>
    <div class="action-item">
      <el-icon><ChatDotRound /></el-icon>
      <span>AI助手</span>
    </div>
  </div>
</el-card>

<!-- 模式2：悬浮快捷按钮 -->
<el-speed-dial
  :actions="quickActions"
  @command="handleQuickAction"
/>
```

### 3.2 批量操作模式

```vue
<!-- 批量选择 + 批量操作 -->
<template>
  <div class="batch-operations">
    <!-- 批量选择栏 -->
    <div v-if="selectedItems.length > 0" class="selection-bar">
      <span>已选择 {{ selectedItems.length }} 项</span>
      <el-button @click="clearSelection">清除</el-button>
      <el-button type="primary" @click="batchApprove">批量同意</el-button>
      <el-button type="danger" @click="batchReject">批量驳回</el-button>
    </div>
    
    <!-- 表格 -->
    <el-table @selection-change="handleSelection">
      <el-table-column type="selection" />
      ...
    </el-table>
  </div>
</template>
```

### 3.3 拖拽排序模式

```vue
<!-- 拖拽排序 -->
<template>
  <el-table :data="listData" row-key="id">
    <el-table-column width="50">
      <template #default>
        <el-icon class="drag-handle"><Rank /></el-icon>
      </template>
    </el-table-column>
    ...
  </el-table>
</template>

<!-- 使用 vuedraggable -->
<draggable 
  v-model="listData" 
  item-key="id"
  handle=".drag-handle"
  @end="onDragEnd"
>
  ...
</draggable>
```

### 3.4 无限滚动模式

```vue
<!-- 无限滚动加载 -->
<template>
  <div class="infinite-scroll" @scroll="handleScroll">
    <div class="content-list">
      <div 
        v-for="item in dataList" 
        :key="item.id"
        class="list-item"
      >
        {{ item.name }}
      </div>
    </div>
    
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>
    
    <div v-if="noMore" class="no-more">
      没有更多了
    </div>
  </div>
</template>

<script setup>
const loading = ref(false)
const page = ref(1)

const loadMore = async () => {
  loading.value = true
  const newData = await fetchData(page.value)
  dataList.value.push(...newData)
  page.value++
  loading.value = false
}
</script>
```

---

## 4. 反馈系统

### 4.1 反馈类型矩阵

| 场景 | 反馈类型 | 形式 | 示例 |
|------|----------|------|------|
| 操作成功 | 即时反馈 | Toast | "保存成功" |
| 操作失败 | 错误反馈 | Toast+弹窗 | "网络错误，请重试" |
| 状态变更 | 成功反馈 | Toast | "审批已通过" |
| 等待处理 | 进度反馈 | Loading/Spinner | 加载动画 |
| 需要确认 | 警告反馈 | Modal | "确认删除？" |

### 4.2 Toast反馈规范

```vue
<!-- 成功Toast -->
<el-message type="success" message="操作成功" show-close />

<!-- 错误Toast -->
<el-message type="error" message="操作失败，请重试" show-close />

<!-- 警告Toast -->
<el-message type="warning" message="数据已过期" show-close />

<!-- 信息Toast -->
<el-message type="info" message="有新版本可用" show-close />
```

### 4.3 操作反馈时间

| 操作类型 | 反馈时机 | 反馈形式 |
|----------|----------|----------|
| 点击按钮 | 立即 | Loading状态 |
| 提交表单 | 立即 | Loading遮罩 |
| 保存数据 | < 1秒 | Toast成功提示 |
| 删除数据 | 立即 | 确认对话框 |
| 长任务 | 实时 | 进度条 |

### 4.4 按钮状态反馈

```vue
<template>
  <el-button 
    :loading="saving"
    :disabled="submitting"
    @click="handleSubmit"
  >
    {{ saving ? '保存中...' : '保存' }}
  </el-button>
</template>
```

---

## 5. 错误处理

### 5.1 错误分类

```
┌─────────────────────────────────────────────────────────────┐
│                      错误分类体系                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1️⃣ 输入错误 (Input Error)                                  │
│     └── 用户输入不符合要求                                  │
│     └── 位置：表单内实时校验                                │
│     └── 反馈：红色提示+具体原因                            │
│                                                             │
│  2️⃣ 验证错误 (Validation Error)                            │
│     └── 提交前检查失败                                     │
│     └── 位置：表单顶部汇总提示                             │
│     └── 反馈：滚动到第一个错误                             │
│                                                             │
│  3️⃣ 网络错误 (Network Error)                                │
│     └── 请求发送失败                                       │
│     └── 位置：全局提示                                      │
│     └── 反馈：Toast+重试按钮                               │
│                                                             │
│  4️⃣ 权限错误 (Permission Error)                            │
│     └── 无权访问/操作                                      │
│     └── 位置：当前页面                                     │
│     └── 反馈：403页面/提示                                 │
│                                                             │
│  5️⃣ 系统错误 (System Error)                                │
│     └── 服务端异常                                         │
│     └── 位置：全局提示                                      │
│     └── 反馈：500页面+报告按钮                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 5.2 表单错误处理

```vue
<template>
  <el-form :model="form" :rules="rules" ref="formRef">
    <!-- 错误汇总 -->
    <el-alert
      v-if="errorSummary.length > 0"
      type="error"
      title="请修正以下问题："
      :closable="false"
    >
      <ul>
        <li v-for="err in errorSummary" :key="err.field">
          {{ err.message }}
        </li>
      </ul>
    </el-alert>
    
    <!-- 输入框错误 -->
    <el-form-item label="用户名" prop="username">
      <el-input 
        v-model="form.username"
        :class="{ 'is-error': errors.username }"
      />
      <span v-if="errors.username" class="error-tip">
        {{ errors.username }}
      </span>
    </el-form-item>
  </el-form>
</template>
```

### 5.3 网络错误处理

```vue
<script setup>
import { ElMessage } from 'element-plus'

const handleApiError = (error) => {
  if (error.code === 'NETWORK_ERROR') {
    ElMessage.error({
      message: '网络连接失败，请检查网络后重试',
      duration: 5000,
      showClose: true,
      action: {
        text: '重试',
        handler: () => retryRequest()
      }
    })
  } else if (error.code === 'TIMEOUT') {
    ElMessage.warning('请求超时，请稍后重试')
  } else {
    ElMessage.error(error.message || '操作失败')
  }
}
</script>
```

### 5.4 404/403处理

```vue
<!-- 404页面 -->
<template>
  <div class="error-page">
    <el-result
      icon="warning"
      title="404"
      sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>
```

---

## 6. 空状态设计

### 6.1 空状态类型

| 类型 | 场景 | 设计策略 |
|------|------|----------|
| 初始空 | 新用户/新模块 | 引导+示例 |
| 筛选空 | 搜索无结果 | 提示+调整建议 |
| 临时空 | 数据加载中 | Loading态 |
| 彻底空 | 无任何数据 | 引导创建 |

### 6.2 空状态模板

```vue
<!-- 完整空状态组件 -->
<template>
  <div class="empty-state">
    <div class="empty-icon">
      <el-icon :size="64"><Document /></el-icon>
    </div>
    
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-description">{{ description }}</p>
    
    <div v-if="$slots.actions" class="empty-actions">
      <slot name="actions" />
    </div>
    
    <div v-if="showHelp" class="empty-help">
      <el-link type="primary">查看帮助文档</el-link>
    </div>
  </div>
</template>

<!-- 使用示例 -->
<empty-state
  title="暂无审批记录"
  description="您还没有需要审批的单据"
  :show-help="true"
>
  <template #actions>
    <el-button type="primary">发起审批</el-button>
    <el-button>查看示例</el-button>
  </template>
</empty-state>
```

### 6.3 典型空状态设计

```
┌─────────────────────────────────────────────────────────────┐
│                                                             │
│  📋 审批空状态                                              │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                                                       │ │
│  │            📭 (空图标)                              │ │
│  │                                                       │ │
│  │            暂无待审批项                               │ │
│  │            您今天的工作已经完成啦！                      │ │
│  │                                                       │ │
│  │     [发起审批]                    [查看历史审批]      │ │
│  │                                                       │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                             │
│  📄 文档空状态                                              │
│  ┌─────────────────────────────────────────────────────┐ │
│  │                                                       │ │
│  │            📂 (空图标)                              │ │
│  │                                                       │ │
│  │            知识库为空                                │ │
│  │            还没有上传任何文档                         │ │
│  │                                                       │ │
│  │     [上传文档]                    [了解支持格式]       │ │
│  │                                                       │ │
│  └─────────────────────────────────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. 加载状态

### 7.1 加载类型

| 类型 | 使用场景 | 视觉形式 |
|------|----------|----------|
| 骨架屏 | 内容区 | 灰色占位块 |
| Spinner | 按钮/小区域 | 旋转图标 |
| Progress | 长任务 | 进度条 |
| Fullscreen | 全页加载 | Loading遮罩 |
| Inline | 内联加载 | 行内加载态 |

### 7.2 骨架屏规范

```vue
<!-- 骨架屏组件 -->
<template>
  <div class="skeleton">
    <div class="skeleton-header">
      <el-skeleton-item variant="circle" />
      <div class="skeleton-info">
        <el-skeleton-item variant="text" style="width: 50%" />
        <el-skeleton-item variant="text" style="width: 30%" />
      </div>
    </div>
    
    <div class="skeleton-body">
      <el-skeleton-item variant="image" />
      <el-skeleton-item variant="text" />
      <el-skeleton-item variant="text" />
      <el-skeleton-item variant="text" style="width: 60%" />
    </div>
  </div>
</template>
```

### 7.3 进度条规范

```vue
<!-- 线性进度条 -->
<el-progress 
  :percentage="percentage"
  :stroke-width="8"
  :format="format"
>
  <template #default>
    {{ percentage }}%
  </template>
</el-progress>

<!-- 环形进度 -->
<el-progress 
  type="circle" 
  :percentage="percentage"
  :stroke-width="10"
>
  <span class="percentage-value">{{ percentage }}%</span>
</el-progress>
```

### 7.4 渐进式加载

```vue
<!-- 内容渐进式展示 -->
<template>
  <div class="progressive-loading">
    <!-- 第一层：骨架屏 -->
    <div v-if="loading" class="skeleton-layer">
      <el-skeleton :rows="6" animated />
    </div>
    
    <!-- 第二层：占位图 -->
    <div v-else-if="!dataReady" class="placeholder-layer">
      <img src="/placeholder.svg" alt="加载中" />
    </div>
    
    <!-- 第三层：真实内容 -->
    <div v-else class="content-layer">
      <RealContent :data="content" />
    </div>
  </div>
</template>
```

---

## 附录A：交互自检清单

```markdown
## 交互设计自检清单

### 反馈
- [ ] 每个操作都有反馈吗？
- [ ] Loading状态清晰吗？
- [ ] 成功/失败反馈明确吗？

### 一致性
- [ ] 同类操作交互一致吗？
- [ ] 视觉风格统一吗？
- [ ] 术语一致吗？

### 错误处理
- [ ] 输入有实时校验吗？
- [ ] 错误提示具体吗？
- [ ] 错误可恢复吗？

### 效率
- [ ] 常用操作 < 3步吗？
- [ ] 支持快捷键吗？
- [ ] 支持批量操作吗？

### 空状态
- [ ] 有空状态设计吗？
- [ ] 空状态有引导吗？

### 加载
- [ ] 有骨架屏吗？
- [ ] 进度可见吗？
- [ ] 支持中断吗？
```

---

*文档版本：V1.0*
*更新日期：2026-04-05*
