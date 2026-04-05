# AI-OA 鸿蒙APP部署包

## 包含内容

```
harmonyos/
├── README.md                    # 本文档
├── VERSION                    # 版本信息
├── SPEC.md                    # 技术规格
├── assets/
│   └── app-icon.png          # 应用图标
├── scripts/
│   └── build.sh              # 构建脚本
├── config/
│   ├── api-config.ts         # API配置
│   └── env-config.ts         # 环境配置
├── docs/
│   ├── 编译指南.md            # 编译说明
│   ├── 发布指南.md            # 应用市场上架指南
│   └── 常见问题.md           # FAQ
└── links/
    ├── HarmonyOS开发者联盟.url
    ├── 华为应用市场.url
    └── DevEco Studio下载.url
```

## 技术规格

| 项目 | 规格 |
|------|------|
| 开发框架 | ArkTS + ArkUI |
| 最低版本 | HarmonyOS 2.0+ |
| SDK版本 | API 8+ |
| 包格式 | HAP |
| 签名 | 需要华为签名证书 |

## 快速构建

```bash
# 1. 安装DevEco Studio
# 下载地址：https://developer.huawei.com/consumer/cn/deveco-studio/

# 2. 导入项目
# File -> Open -> 选择本目录

# 3. 配置签名
# Project Structure -> Signing Configs -> 配置华为签名证书

# 4. 构建
# Build -> Build HAP(s) -> 选择模块

# 5. 输出目录
# build/outputs/hap/release/
```

## API配置

```typescript
// config/api-config.ts
export const API_BASE_URL = 'https://aioa.example.com/api';
export const WS_URL = 'wss://aioa.example.com/ws';

// 环境配置
export const ENV = {
  dev: {
    API_BASE_URL: 'http://192.168.1.100:8080/api',
    WS_URL: 'ws://192.168.1.100:8080/ws'
  },
  prod: {
    API_BASE_URL: 'https://aioa.example.com/api',
    WS_URL: 'wss://aioa.example.com/ws'
  }
};
```

## 应用市场发布

### 华为应用市场发布流程

1. **注册开发者账号**
   - https://developer.huawei.com/

2. **创建应用**
   - 登录华为开发者联盟
   - 进入应用市场服务
   - 创建新应用

3. **配置签名**
   - 申请签名证书
   - 配置AppGallery Connect

4. **上架审核**
   - 准备应用截图
   - 编写应用描述
   - 提交审核

### 发布检查清单

| 检查项 | 说明 |
|--------|------|
| 应用图标 | 1024x1024 PNG格式 |
| 应用截图 | 至少5张截图 |
| 应用描述 | 200字以上 |
| 隐私政策 | 必须提供URL |
| 权限说明 | 说明申请原因 |

## 功能模块

| 模块 | 说明 | 优先级 |
|------|------|--------|
| 首页工作台 | 仪表盘+快捷入口 | P0 |
| 审批中心 | 审批列表+审批详情 | P0 |
| 财务报销 | 发票上传+OCR识别 | P0 |
| AI助手 | 智能问答+知识库 | P1 |
| 企业聊天 | 即时消息+群聊 | P1 |
| 智能报表 | 报表查看+生成 | P2 |
| 考勤打卡 | 签到+签退 | P2 |
| 扫一扫 | 扫码功能 | P2 |

## 权限说明

| 权限 | 用途 |
|------|------|
| 网络权限 | API调用 |
| 相机权限 | 拍照/扫码 |
| 文件访问 | 附件上传下载 |
| 位置权限 | 考勤打卡 |
| 推送权限 | 消息通知 |

---

版本：1.0.0
更新日期：2026-04-05
