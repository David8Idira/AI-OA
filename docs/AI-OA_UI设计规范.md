# AI-OA UI设计规范 V1.0

> 文档版本：V1.0
> 更新日期：2026-04-05
> 设计系统：Element UI + 自定义组件

---

## 目录

1. [设计原则](#1-设计原则)
2. [色彩系统](#2-色彩系统)
3. [字体系统](#3-字体系统)
4. [图标系统](#4-图标系统)
5. [组件规范](#5-组件规范)
6. [间距系统](#6-间距系统)
7. [圆角与阴影](#7-圆角与阴影)
8. [动效设计](#8-动效设计)
9. [响应式设计](#9-响应式设计)
10. [无障碍设计](#10-无障碍设计)

---

## 1. 设计原则

### 1.1 六大设计原则

```
┌─────────────────────────────────────────────────────────────┐
│                    六大设计原则                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1️⃣ 一致性 Consistency                                    │
│     └── 界面元素和交互行为保持一致                          │
│                                                             │
│  2️⃣ 反馈性 Feedback                                      │
│     └── 每个操作都有明确的视觉/状态反馈                      │
│                                                             │
│  3️⃣ 效率性 Efficiency                                    │
│     └── 减少用户操作步骤，提升效率                           │
│                                                             │
│  4️⃣ 可控性 Control                                       │
│     └── 用户始终掌控系统，支持撤销/重做                      │
│                                                             │
│  5️⃣ 容错性 Forgiveness                                    │
│     └── 预防错误，提供恢复机制                               │
│                                                             │
│  6️⃣ 简洁性 Simplicity                                    │
│     └── 界面简洁，隐藏复杂性，只展示必要信息                  │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 设计目标

| 目标 | 指标 | 说明 |
|------|------|------|
| 易学性 | 新用户上手时间 < 10分钟 | 符合用户认知习惯 |
| 效率性 | 常用操作 < 3步 | 减少操作路径 |
| 满意度 | NPS评分 > 60 | 用户体验满意度 |
| 可访问性 | WCAG 2.1 AA | 支持特殊用户群体 |

---

## 2. 色彩系统

### 2.1 主色（Primary Colors）

```css
:root {
  /* 主色 */
  --color-primary: #667EEA;          /* 主品牌色 */
  --color-primary-light: #8B9FEE;    /* 主色浅色 */
  --color-primary-dark: #4A63CC;     /* 主色深色 */
  --color-primary-50: rgba(102, 126, 234, 0.05);
  --color-primary-100: rgba(102, 126, 234, 0.1);
  --color-primary-200: rgba(102, 126, 234, 0.2);
  
  /* 辅助色 */
  --color-secondary: #A855F7;        /* 辅助品牌色 */
  --color-secondary-light: #C084FC;
  --color-secondary-dark: #9333EA;
}
```

### 2.2 功能色（Functional Colors）

```css
:root {
  /* 状态色 */
  --color-success: #10B981;          /* 成功 */
  --color-success-light: #34D399;
  --color-success-dark: #059669;
  
  --color-warning: #F59E0B;          /* 警告 */
  --color-warning-light: #FBBF24;
  --color-warning-dark: #D97706;
  
  --color-danger: #EF4444;           /* 危险/错误 */
  --color-danger-light: #F87171;
  --color-danger-dark: #DC2626;
  
  --color-info: #3B82F6;            /* 信息 */
  --color-info-light: #60A5FA;
  --color-info-dark: #2563EB;
  
  /* 中性色 */
  --color-gray-50: #F9FAFB;
  --color-gray-100: #F3F4F6;
  --color-gray-200: #E5E7EB;
  --color-gray-300: #D1D5DB;
  --color-gray-400: #9CA3AF;
  --color-gray-500: #6B7280;
  --color-gray-600: #4B5563;
  --color-gray-700: #374151;
  --color-gray-800: #1F2937;
  --color-gray-900: #111827;
}
```

### 2.3 色彩使用规范

| 使用场景 | 颜色 | 说明 |
|----------|------|------|
| 主要按钮 | `--color-primary` | 品牌主色 |
| 成功提示 | `--color-success` | 操作成功 |
| 警告提示 | `--color-warning` | 需要注意 |
| 错误提示 | `--color-danger` | 操作失败 |
| 信息提示 | `--color-info` | 一般信息 |
| 文字主色 | `--color-gray-900` | 标题/重要文字 |
| 文字正文 | `--color-gray-600` | 正文内容 |
| 文字辅助 | `--color-gray-400` | 辅助说明 |
| 边框 | `--color-gray-200` | 分割线/边框 |
| 背景 | `#FFFFFF` / `#F9FAFB` | 页面背景 |

---

## 3. 字体系统

### 3.1 字体家族

```css
:root {
  /* 中文优先使用系统字体 */
  --font-family-cn: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", "Helvetica Neue", sans-serif;
  
  /* 英文/数字使用Inter */
  --font-family-en: "Inter", -apple-system, BlinkMacSystemFont, sans-serif;
  
  /* 代码使用等宽字体 */
  --font-family-code: "JetBrains Mono", "Fira Code", "Consolas", monospace;
  
  /* 全局字体 */
  --font-family: var(--font-family-cn);
}
```

### 3.2 字体规格

| 级别 | 字体大小 | 行高 | 字重 | 用途 |
|------|----------|------|------|------|
| H1 | 32px | 1.2 | 700 | 页面标题 |
| H2 | 24px | 1.3 | 600 | 模块标题 |
| H3 | 20px | 1.4 | 600 | 卡片标题 |
| H4 | 16px | 1.5 | 600 | 段落标题 |
| Body | 14px | 1.6 | 400 | 正文内容 |
| Small | 12px | 1.5 | 400 | 辅助说明 |
| Tiny | 10px | 1.4 | 400 | 标签/徽章 |

### 3.3 字体使用规范

```css
/* 标题 */
h1, .h1 {
  font-size: 32px;
  line-height: 1.2;
  font-weight: 700;
  color: var(--color-gray-900);
}

h2, .h2 {
  font-size: 24px;
  line-height: 1.3;
  font-weight: 600;
  color: var(--color-gray-900);
}

h3, .h3 {
  font-size: 20px;
  line-height: 1.4;
  font-weight: 600;
  color: var(--color-gray-800);
}

/* 正文 */
p, .body {
  font-size: 14px;
  line-height: 1.6;
  font-weight: 400;
  color: var(--color-gray-600);
}
```

---

## 4. 图标系统

### 4.1 图标库

| 图标库 | 用途 | 加载方式 |
|--------|------|----------|
| Element Plus Icons | 基础图标 | 按需加载 |
| Remix Icon | 功能图标 | 全量加载 |
| 自定义图标 | 品牌图标 | SVG内联 |

### 4.2 图标使用规范

```vue
<!-- 正确示例 -->
<el-icon><Edit /></el-icon>  <!-- Element图标 -->
<i class="ri-home-line"></i>  <!-- Remix图标 -->

<!-- 图标与文字组合 -->
<el-button>
  <el-icon><Plus /></el-icon>
  新建
</el-button>
```

### 4.3 图标尺寸规范

| 尺寸 | 像素 | 用途 |
|------|------|------|
| XS | 12px | 紧凑列表 |
| SM | 14px | 表单标签 |
| MD | 16px | 正文/按钮 |
| LG | 20px | 导航/标题 |
| XL | 24px | 空状态 |
| XXL | 32px | 品牌图标 |

### 4.4 图标颜色规范

| 使用场景 | 颜色值 |
|----------|--------|
| 主要操作 | `--color-primary` |
| 成功状态 | `--color-success` |
| 警告状态 | `--color-warning` |
| 错误状态 | `--color-danger` |
| 辅助信息 | `--color-gray-400` |
| 禁用状态 | `--color-gray-300` |

---

## 5. 组件规范

### 5.1 按钮规范

#### 按钮类型

```vue
<!-- 主要按钮 -->
<el-button type="primary">主要操作</el-button>

<!-- 次要按钮 -->
<el-button>次要操作</el-button>

<!-- 文字按钮 -->
<el-button type="text">文字链接</el-button>

<!-- 危险按钮 -->
<el-button type="danger">删除</el-button>
```

#### 按钮尺寸

| 尺寸 | 高度 | 字号 | 内边距 | 用途 |
|------|------|------|--------|------|
| Large | 40px | 16px | 20px 24px | 主要操作 |
| Medium | 32px | 14px | 12px 16px | 默认尺寸 |
| Small | 24px | 12px | 8px 12px | 表格内操作 |
| Mini | 20px | 12px | 4px 8px | 紧凑布局 |

#### 按钮状态

```css
/* 正常 */
.btn { background: var(--color-primary); }

/* 悬停 */
.btn:hover { filter: brightness(1.1); }

/* 按下 */
.btn:active { filter: brightness(0.95); }

/* 聚焦 */
.btn:focus { outline: 2px solid var(--color-primary-200); outline-offset: 2px; }

/* 禁用 */
.btn:disabled { 
  opacity: 0.5; 
  cursor: not-allowed; 
}
```

### 5.2 表单规范

#### 输入框尺寸

| 尺寸 | 高度 | 字号 | 用途 |
|------|------|------|------|
| Large | 40px | 16px | 移动端/重要表单 |
| Medium | 32px | 14px | 默认尺寸 |
| Small | 28px | 12px | 紧凑表单 |

#### 表单布局

```vue
<!-- 标签对齐 -->
<el-form label-position="top">  <!-- 标签在输入框上方 -->
<el-form label-position="left">  <!-- 标签在输入框左侧 -->
<el-form label-position="right">  <!-- 标签在输入框右侧 -->

<!-- 标签宽度 -->
<el-form label-width="120px">

<!-- 栅格布局 -->
<el-row :gutter="20">
  <el-col :span="12">表单项1</el-col>
  <el-col :span="12">表单项2</el-col>
</el-row>
```

#### 必填标识

```vue
<!-- 必填字段 -->
<el-form-item label="用户名" required>
  <el-input v-model="form.username" />
</el-form-item>

<!-- 红色星号标识 -->
.required::before {
  content: "*";
  color: var(--color-danger);
  margin-right: 4px;
}
```

### 5.3 卡片规范

```vue
<el-card shadow="hover">  <!-- 可选: never/hover/always -->
  <template #header>
    <div class="card-header">
      <span>卡片标题</span>
      <el-button>操作</el-button>
    </div>
  </template>
  卡片内容
</el-card>
```

### 5.4 表格规范

```vue
<el-table :data="tableData" stripe border>
  <!-- 斑马纹 + 边框 -->
  
  <el-table-column prop="name" label="姓名" />
  <el-table-column prop="status" label="状态">
    <template #default="{ row }">
      <el-tag :type="getStatusType(row.status)">
        {{ row.status }}
      </el-tag>
    </template>
  </el-table-column>
</el-table>
```

### 5.5 对话框规范

```vue
<el-dialog
  v-model="dialogVisible"
  title="对话框标题"
  width="500px"
  :close-on-click-modal="false"
  :show-close="true"
>
  对话框内容
  
  <template #footer>
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button type="primary" @click="handleConfirm">确定</el-button>
  </template>
</el-dialog>
```

---

## 6. 间距系统

### 6.1 基础间距单位

```css
:root {
  --spacing-xs: 4px;   /* 紧凑间距 */
  --spacing-sm: 8px;    /* 小间距 */
  --spacing-md: 12px;  /* 中等间距 */
  --spacing-base: 16px; /* 基础间距 */
  --spacing-lg: 20px;  /* 大间距 */
  --spacing-xl: 24px;  /* 特大间距 */
  --spacing-2xl: 32px; /* 双倍间距 */
  --spacing-3xl: 40px; /* 三倍间距 */
  --spacing-4xl: 48px; /* 四倍间距 */
}
```

### 6.2 间距使用规范

| 使用场景 | 间距 | 说明 |
|----------|------|------|
| 组件内部元素 | `--spacing-sm` ~ `--spacing-md` | 紧凑 |
| 组件间距 | `--spacing-base` ~ `--spacing-lg` | 标准 |
| 区块间距 | `--spacing-xl` ~ `--spacing-2xl` | 分隔 |
| 页面边距 | `--spacing-2xl` ~ `--spacing-3xl` | 留白 |
| 大区块分隔 | `--spacing-3xl` ~ `--spacing-4xl` | 视觉分隔 |

### 6.3 栅格系统

```css
/* 12栏栅格 */
.row {
  display: flex;
  flex-wrap: wrap;
  margin-left: -12px;
  margin-right: -12px;
}

.col {
  padding-left: 12px;
  padding-right: 12px;
}

/* 栅格间距 */
.gutter-xs { gap: 8px; }
.gutter-sm { gap: 12px; }
.gutter-md { gap: 16px; }
.gutter-lg { gap: 24px; }
```

---

## 7. 圆角与阴影

### 7.1 圆角规范

```css
:root {
  /* 圆角尺寸 */
  --radius-none: 0;
  --radius-sm: 2px;    /* 微圆角 */
  --radius-base: 4px;  /* 标准圆角 */
  --radius-md: 6px;   /* 中等圆角 */
  --radius-lg: 8px;   /* 大圆角 */
  --radius-xl: 12px;  /* 特大圆角 */
  --radius-2xl: 16px; /* 双倍圆角 */
  --radius-full: 9999px; /* 全圆角/胶囊 */
}
```

### 7.2 圆角使用场景

| 元素 | 圆角 | 说明 |
|------|------|------|
| 按钮 | `--radius-md` | 6px |
| 输入框 | `--radius-md` | 6px |
| 卡片 | `--radius-lg` | 8px |
| 对话框 | `--radius-xl` | 12px |
| 下拉菜单 | `--radius-lg` | 8px |
| 标签/徽章 | `--radius-full` | 胶囊形 |
| 头像 | `--radius-full` | 圆形 |
| 图片 | `--radius-md` | 6px |

### 7.3 阴影规范

```css
:root {
  /* 阴影层级 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-base: 0 1px 3px rgba(0, 0, 0, 0.1), 0 1px 2px rgba(0, 0, 0, 0.06);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1), 0 2px 4px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1), 0 4px 6px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.1), 0 10px 10px rgba(0, 0, 0, 0.04);
  --shadow-2xl: 0 25px 50px rgba(0, 0, 0, 0.25);
}
```

### 7.4 阴影使用场景

| 场景 | 阴影 | 说明 |
|------|------|------|
| 卡片悬停 | `--shadow-md` | 悬停效果 |
| 下拉菜单 | `--shadow-lg` | 弹出层 |
| 对话框 | `--shadow-xl` | 模态框 |
| 按钮按下 | `--shadow-sm` | 按下效果 |
| 输入框聚焦 | `--shadow-base` | 聚焦效果 |

---

## 8. 动效设计

### 8.1 动效原则

```
┌─────────────────────────────────────────────────────────────┐
│                      动效设计四原则                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  1️⃣ 目的性 Purposeful                                    │
│     └── 动效应有意义，不为炫技                               │
│                                                             │
│  2️⃣ 轻量化 Lightweight                                    │
│     └── 动效应快速流畅，不造成负担                           │
│                                                             │
│  3️⃣ 自然性 Natural                                        │
│     └── 动效应符合物理世界的运动规律                         │
│                                                             │
│  4️⃣ 一致性 Consistent                                    │
│     └── 同类元素使用相同的动效模式                           │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 时长规范

```css
:root {
  /* 时长 */
  --duration-instant: 0ms;    /* 即时 */
  --duration-fast: 100ms;     /* 快速 */
  --duration-normal: 200ms;   /* 正常 */
  --duration-slow: 300ms;     /* 慢速 */
  --duration-slower: 500ms;    /* 更慢 */
}
```

| 场景 | 时长 | 说明 |
|------|------|------|
| 状态切换 | `--duration-fast` (100ms) | hover/active等 |
| 元素出现 | `--duration-normal` (200ms) | 淡入/滑入 |
| 页面过渡 | `--duration-slow` (300ms) | 路由切换 |
| 复杂动画 | `--duration-slower` (500ms) | 图表/3D |

### 8.3 缓动函数

```css
:root {
  /* 常用缓动 */
  --ease-default: cubic-bezier(0.4, 0, 0.2, 1);      /* 标准 */
  --ease-in: cubic-bezier(0.4, 0, 1, 1);             /* 进入 */
  --ease-out: cubic-bezier(0, 0, 0.2, 1);            /* 离开 */
  --ease-in-out: cubic-bezier(0.4, 0, 0.2, 1);      /* 双向 */
  
  /* 特殊缓动 */
  --ease-bounce: cubic-bezier(0.68, -0.55, 0.265, 1.55); /* 弹性 */
  --ease-elastic: cubic-bezier(0.5, 1.5, 0.5, 1);    /* 弹性2 */
}
```

### 8.4 动效示例

```css
/* 按钮悬停 */
.btn {
  transition: all var(--duration-fast) var(--ease-default);
}
.btn:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-md);
}

/* 淡入效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--duration-normal) var(--ease-default);
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 滑入效果 */
.slide-enter-active,
.slide-leave-active {
  transition: transform var(--duration-normal) var(--ease-out);
}
.slide-enter-from {
  transform: translateX(100%);
}
.slide-leave-to {
  transform: translateX(-100%);
}
```

---

## 9. 响应式设计

### 9.1 断点系统

```css
:root {
  /* 断点 */
  --breakpoint-xs: 480px;   /* 超小屏幕 */
  --breakpoint-sm: 640px;   /* 小屏幕 */
  --breakpoint-md: 768px;   /* 中等屏幕/平板 */
  --breakpoint-lg: 1024px;  /* 大屏幕/桌面 */
  --breakpoint-xl: 1280px;  /* 超大屏幕 */
  --breakpoint-2xl: 1536px; /* 极大屏幕 */
}
```

### 9.2 响应式策略

| 断点 | 设备 | 布局策略 |
|------|------|----------|
| < 480px | 手机竖屏 | 单列，隐藏侧边栏 |
| 480-768px | 手机横屏/小平板 | 双列，折叠导航 |
| 768-1024px | 平板 | 自适应，底部导航 |
| 1024-1280px | 小桌面 | 侧边栏收起 |
| > 1280px | 大桌面 | 完整布局 |

### 9.3 响应式布局

```vue
<template>
  <div class="layout">
    <!-- 桌面端：侧边栏 -->
    <el-aside v-if="isDesktop" width="200px">
      侧边导航
    </el-aside>
    
    <!-- 移动端：底部导航 -->
    <el-footer v-if="isMobile" height="60px">
      底部导航
    </el-footer>
  </div>
</template>

<style scoped>
/* 响应式显示 */
.desktop-only { display: block; }
.mobile-only { display: none; }

@media (max-width: 768px) {
  .desktop-only { display: none; }
  .mobile-only { display: block; }
}
</style>
```

---

## 10. 无障碍设计

### 10.1 无障碍标准

| 标准 | 级别 | 说明 |
|------|------|------|
| WCAG 2.1 | AA级 | 国际无障碍标准 |
| GB/T 37668-2019 | A级 | 中国无障碍标准 |

### 10.2 无障碍检查清单

```markdown
## 无障碍设计检查

### 颜色对比度
- [ ] 文字与背景对比度 ≥ 4.5:1
- [ ] 大文字(>18px)对比度 ≥ 3:1
- [ ] 不依赖颜色传递信息

### 键盘操作
- [ ] 所有功能可通过键盘操作
- [ ] 焦点状态可见
- [ ] Tab顺序合理

### 屏幕阅读器
- [ ] 图片有alt文本
- [ ] 表单有label关联
- [ ] 动态内容有ARIA标签

### 动效控制
- [ ] 提供减少动效选项
- [ ] 不使用闪烁频率 > 3Hz
```

### 10.3 ARIA使用规范

```vue
<!-- 按钮 -->
<el-button aria-label="关闭对话框">
  <Close />
</el-button>

<!-- 输入框 -->
<el-input
  v-model="value"
  aria-label="搜索内容"
  aria-describedby="search-hint"
/>
<span id="search-hint">按Enter键搜索</span>

<!-- 模态框 -->
<el-dialog
  aria-modal="true"
  aria-labelledby="dialog-title"
>
  <h2 id="dialog-title">对话框标题</h2>
</el-dialog>
```

---

## 附录A：设计Token

```css
/* 完整Design Token */
:root {
  /* 颜色 */
  --color-primary: #667EEA;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-danger: #EF4444;
  --color-info: #3B82F6;
  
  /* 文字 */
  --font-size-xs: 10px;
  --font-size-sm: 12px;
  --font-size-base: 14px;
  --font-size-lg: 16px;
  --font-size-xl: 20px;
  --font-size-2xl: 24px;
  --font-size-3xl: 32px;
  
  /* 间距 */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 12px;
  --spacing-base: 16px;
  --spacing-lg: 20px;
  --spacing-xl: 24px;
  --spacing-2xl: 32px;
  
  /* 圆角 */
  --radius-sm: 2px;
  --radius-base: 4px;
  --radius-md: 6px;
  --radius-lg: 8px;
  --radius-xl: 12px;
  --radius-full: 9999px;
  
  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-base: 0 1px 3px rgba(0, 0, 0, 0.1);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.1);
  
  /* 动效 */
  --duration-fast: 100ms;
  --duration-normal: 200ms;
  --duration-slow: 300ms;
  --ease-default: cubic-bezier(0.4, 0, 0.2, 1);
}
```

---

*文档版本：V1.0*
*更新日期：2026-04-05*
