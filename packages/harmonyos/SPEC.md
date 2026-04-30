# AI-OA HarmonyOS 应用技术规格文档

## 1. 项目概述

**项目名称**: AI-OA  
**项目类型**: HarmonyOS 移动应用  
**核心功能**: 智能办公平台，提供审批流程、日程管理、考勤打卡、通讯录等功能  
**目标用户**: 企业员工、行政管理人员

## 2. 技术栈

### 2.1 框架与语言
- **语言**: ArkTS (TypeScript的超集)
- **框架**: ArkUI
- **最低SDK版本**: API 9

### 2.2 核心依赖
| 依赖 | 用途 | 版本 |
|------|------|------|
| @ohos.net.http | HTTP网络请求 | 系统内置 |
| @ohos.router | 页面路由管理 | 系统内置 |
| @ohos.app.ability.Want | 应用能力 | 系统内置 |
| @ohos.storage | 本地存储 | 系统内置 |

### 2.3 状态管理
- **AppStorage**: 全局状态存储，用于登录状态、用户信息等
- **LocalStorage**: 页面级状态存储，用于页面内部状态管理

### 2.4 网络请求
- 使用 `ohos.net.http` 模块封装HTTP请求
- 支持Token自动注入
- 支持统一错误处理

## 3. 项目结构

```
harmonyos/
├── entry/                      # 主模块
│   └── src/main/
│       ├── App.ets             # 应用入口
│       ├── MainPage.ets        # 主页面(Tab容器)
│       └── module.json5        # 模块配置
├── commons/                    # 公共模块
│   └── src/main/ets/
│       ├── data/
│       │   ├── api/
│       │   │   └── ApiClient.ets   # HTTP客户端封装
│       │   └── model/
│       │       └── User.ets        # 用户数据模型
│       ├── ui/
│       │   ├── home/
│       │   │   └── HomeIndex.ets   # 首页工作台
│       │   └── login/
│       │       └── LoginPage.ets   # 登录页面
│       └── utils/
│           └── Constants.ets       # 常量定义
├── contract/                    # HAR导出接口
│   └── src/main/ets/har/
│       └── index.ts             # 公共API导出
├── scripts/
│   └── build.sh                # 构建脚本
├── config/
│   └── api-config.ts           # API配置
└── SPEC.md                     # 本文档
```

## 4. 模块说明

### 4.1 ApiClient (网络请求封装)
**功能特性**:
- 单例模式，全局复用
- 统一的基础URL配置
- Token自动注入(从AppStorage读取)
- 统一的错误处理(ApiError)
- 支持GET/POST/PUT/DELETE方法

**使用示例**:
```typescript
import { ApiClient } from '../commons/src/main/ets/data/api/ApiClient'

// 初始化
ApiClient.init({
  baseUrl: 'https://api.example.com',
  timeout: 30000
})

// GET请求
const response = await ApiClient.get('/api/user/info')

// POST请求
const response = await ApiClient.post('/api/auth/login', {
  username: 'admin',
  password: '123456'
})
```

### 4.2 HomeIndex (首页工作台)
**功能特性**:
- 顶部欢迎区域
- 统计卡片(今日任务、待审批、未读消息)
- 快捷操作网格(发起审批、我的审批、日程、通讯录、考勤打卡等)
- 最新动态列表

### 4.3 LoginPage (登录页面)
**功能特性**:
- 用户名/密码输入
- 记住密码选项
- 登录表单验证
- 错误提示
- Token存储

## 5. 路由配置

| 路径 | 页面 | 说明 |
|------|------|------|
| pages/Login | LoginPage | 登录页 |
| pages/Main | MainPage | 主页面(Tab容器) |
| pages/Home | HomeIndex | 首页工作台 |
| pages/Approvals | ApprovalsPage | 审批列表 |
| pages/Messages | MessagesPage | 消息列表 |
| pages/Profile | ProfilePage | 个人中心 |

## 6. 存储设计

### 6.1 AppStorage Keys
| Key | 类型 | 说明 |
|-----|------|------|
| auth_token | string | 认证Token |
| user_id | string | 用户ID |
| username | string | 用户名 |

### 6.2 本地存储
使用 `userinfo` 数据库存储:
- 用户详细信息
- 应用配置
- 缓存数据

## 7. API规范

### 7.1 统一响应格式
```typescript
interface ApiResponse<T> {
  code: number      // 业务状态码，0或200表示成功
  message: string   // 响应消息
  data: T          // 响应数据
}
```

### 7.2 错误处理
```typescript
class ApiError extends Error {
  code: number      // HTTP或业务错误码
  message: string  // 错误消息
  response?: any   // 原始响应
}
```

## 8. 构建配置

### 8.1 环境配置
| 环境 | API地址 | 说明 |
|------|---------|------|
| dev | https://dev-api.example.com | 开发环境 |
| test | https://test-api.example.com | 测试环境 |
| prod | https://api.example.com | 生产环境 |

### 8.2 构建命令
```bash
# 开发环境构建
npm run build:dev

# 测试环境构建
npm run build:test

# 生产环境构建
npm run build:prod

# 使用构建脚本
./scripts/build.sh dev
./scripts/build.sh test
./scripts/build.sh prod
```

## 9. 后续开发计划

- [ ] 审批详情页
- [ ] 日程管理页
- [ ] 考勤打卡功能
- [ ] 消息通知
- [ ] 通讯录
- [ ] 个人设置

## 10. 注意事项

1. 所有页面路由需在 `module.json5` 中配置
2. 网络请求必须在 EntryAbility 中初始化 ApiClient
3. Token过期时需自动跳转登录页
4. 敏感信息(如密码)不要存储在AppStorage中

---

**文档版本**: 1.0.0  
**创建日期**: 2026-04-30  
**最后更新**: 2026-04-30
