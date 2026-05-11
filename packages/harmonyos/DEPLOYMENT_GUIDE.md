# AI-OA HarmonyOS 移动端部署指南

## 一、现状概览

| 模块 | 状态 | 说明 |
|------|------|------|
| 页面组件 | ✅ 完整 | 67个 .ets 文件，18个主页面 |
| 服务层 | ✅ 完整 | ApiClient 已封装，支持 Token 认证 |
| API 端点 | ⚠️ 待配置 | 使用 mock URL，需根据部署环境修改 |
| iOS/Android | ❌ 缺失 | 仅有压缩包外壳，无源码 |

## 二、HarmonyOS 源码结构

```
packages/harmonyos/
├── entry/                    # 应用入口模块
│   └── src/main/
│       ├── App.ets
│       ├── MainPage.ets
│       ├── entryability/EntryAbility.ets
│       └── resources/base/profile/main_pages.json  # 页面路由配置
├── commons/                  # 公共模块（HAR）
│   └── src/main/ets/
│       ├── data/
│       │   └── api/ApiClient.ets   # HTTP 客户端（核心）
│       ├── model/
│       ├── ui/
│       │   ├── home/HomeIndex.ets  # 首页（已集成API）
│       │   ├── login/LoginPage.ets # 登录页（已集成API）
│       │   ├── approval/           # 审批模块
│       │   ├── attendance/         # 考勤模块
│       │   ├── message/            # 消息模块
│       │   └── ...                # 其他模块
│       └── services/              # 业务服务
├── config/
│   └── api-config.ts         # API 配置（需修改）
├── contract/                 # HAR 导出接口
└── SPEC.md                   # 技术规格文档
```

## 三、API 端点对照表

移动端使用的前端 BASE URL 需要与后端网关对应。后端网关路由：

| 后端服务 | 端口 | 路由前缀 | 对应移动端 API |
|----------|------|----------|----------------|
| Gateway | 8080 | /api/* | 统一入口 |
| aioa-system | 8081 | /api/system/* | 用户、权限 |
| aioa-workflow | 8082 | /api/workflow/* | 审批流 |
| aioa-ai | 8083 | /api/ai/* | AI 对话 |
| aioa-im | 8084 | /api/im/* | 即时通讯 |
| aioa-ocr | 8085 | /api/ocr/* | OCR 识别 |
| aioa-report | 8084(?) | /api/report/* | 报表 |

**关键接口（已与后端对齐）：**

| 功能 | 移动端调用 | 后端接口 |
|------|-----------|----------|
| 登录 | `POST /api/v1/users/login` | ✅ 已对齐 |
| 获取当前用户 | `GET /api/v1/users/current` | ✅ 已对齐 |
| 考勤打卡 | `POST /api/v1/attendance/checkin` | ✅ 已对齐 |
| 考勤记录 | `GET /api/v1/attendance/records` | ✅ 已对齐 |
| 审批列表 | `GET /api/v1/approval/approvals` | ✅ 已对齐 |
| 消息列表 | `GET /api/v1/im/conversations` | ✅ 已对齐 |

## 四、部署配置

### 4.1 开发环境（Docker Compose）

修改 `config/api-config.ts`：

```typescript
export const devConfig: ApiConfig = {
  baseUrl: 'http://localhost:30080',  // Nginx 反向代理
  timeout: 30000,
  enableLog: true
}
```

### 4.2 生产环境

```typescript
export const prodConfig: ApiConfig = {
  baseUrl: 'https://your-domain.com',  // 实际域名
  timeout: 30000,
  enableLog: false
}
```

### 4.3 设置运行环境

在应用启动时设置环境：

```typescript
AppStorage.setOrCreate('api_env', 'dev')  // dev/test/prod
```

## 五、iOS/Android 源码缺失问题

当前 `AI-OA-ios-v1.0.tar.gz` 和 `AI-OA-android-v1.0.tar.gz` 仅包含 VERSION 和 README 文件，**无实际源码**。

### 解决方案

1. **React Native 方案**：使用前端代码打包为 React Native 应用
   - 优势：代码复用率高
   - 需要：安装 react-native-cli，配置 iOS/Android 工程

2. **Flutter 方案**：将 ArkUI 代码迁移至 Flutter
   - 挑战：需要完全重写 UI 层

3. **uni-app 方案**：使用前端代码打包为多端应用
   - 优势：支持 iOS/Android/H5/小程序
   - 推荐指数：⭐⭐⭐⭐⭐

## 六、下一步工作

1. **HarmonyOS**：
   - [x] API 端点与后端对齐
   - [x] 首页数据从真实接口获取
   - [ ] 完善各页面的 API 集成
   - [ ] 集成华为 HMS Core（如需要地图定位）

2. **iOS/Android**：
   - [ ] 确定跨平台框架方案
   - [ ] 搭建 iOS/Android 工程
   - [ ] 封装与 HarmonyOS 同款的 ApiClient

3. **后端**：
   - [ ] 启动后端服务验证接口
   - [ ] 补充缺失的 API 文档

---

_践行毛选思想：实事求是，群众路线，持续改进_
