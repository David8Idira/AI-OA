# AI-OA iOS APP部署包

## 包含内容

```
ios/
├── README.md                    # 本文档
├── VERSION                    # 版本信息
├── SPEC.md                    # 技术规格
├── assets/
│   └── AppIcon.appiconset/   # 应用图标
├── scripts/
│   └── build.sh              # 构建脚本
├── config/
│   ├── APIConfig.swift       # API配置
│   └── Environment.swift     # 环境配置
├── docs/
│   ├── 编译指南.md            # 编译说明
│   ├── AppStore发布指南.md   # App Store上架
│   └── 常见问题.md           # FAQ
└── links/
    ├── Apple开发者中心.url
    └── App Store Connect.url
```

## 技术规格

| 项目 | 规格 |
|------|------|
| 开发语言 | Swift 5.9+ |
| 开发框架 | SwiftUI / UIKit |
| 最低版本 | iOS 15.0+ |
| Xcode版本 | 15.0+ |
| 包格式 | IPA |
| 签名 | 需要Apple开发者证书 |

## 快速构建

```bash
# 1. 安装Xcode
# App Store搜索Xcode或从开发者网站下载

# 2. 打开项目
open ios/AIOA.xcodeproj

# 3. 配置签名
# Signing & Capabilities -> Team -> 选择你的开发者账号

# 4. 配置Bundle Identifier
# 一般格式：com.公司.aioa

# 5. 构建
# Product -> Build (Cmd+B)
# Product -> Archive -> 发布

# 6. 导出IPA
# Window -> Organizer -> 选择Archive -> Distribute
```

## API配置

```swift
// config/APIConfig.swift
enum Environment {
    case dev
    case prod
    
    var baseURL: String {
        switch self {
        case .dev:
            return "http://192.168.1.100:8080"
        case .prod:
            return "https://aioa.example.com"
        }
    }
    
    var wsURL: String {
        switch self {
        case .dev:
            return "ws://192.168.1.100:8080/ws"
        case .prod:
            return "wss://aioa.example.com/ws"
        }
    }
}
```

## App Store发布流程

### 1. 创建App Store Connect应用

1. 登录 [App Store Connect](https://appstoreconnect.apple.com/)
2. 点击「我的App」->「+」->「新建App」
3. 填写应用信息：
   - 平台：iOS
   - 名称：AI-OA
   - 主要语言：简体中文
   - Bundle ID：com.公司.aioa
   - SKU：aioa-ios

### 2. 准备应用资源

| 资源 | 要求 |
|------|------|
| 应用图标 | 1024x1024 PNG |
| 应用截图 | iPhone 6.7"/6.5"/5.5"各至少1张 |
| 预览视频 | 可选，30秒内 |
| 应用描述 | 170字以上 |
| 关键词 | 100字符以内 |
| 支持URL | 隐私政策页面 |

### 3. 构建与上传

```bash
# 使用Xcode Cloud或Fastlane
# Xcode Cloud配置见 docs/xcode-cloud.yml
```

### 4. 提交审核

- 填写价格与销售范围
- 提交审核
- 等待Apple审核（通常1-3天）

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

| 权限 | 用途 | Info.plist Key |
|------|------|----------------|
| 网络 | API调用 | NSAppTransportSecurity |
| 相机 | 拍照/扫码 | NSCameraUsageDescription |
| 相册 | 选择图片 | NSPhotoLibraryUsageDescription |
| 位置 | 考勤打卡 | NSLocationWhenInUseUsageDescription |
| 推送 | 消息通知 | UIBackgroundModes - remote-notification |
| Face ID | 生物认证 | NSFaceIDUsageDescription |

## 推送配置

```swift
// AppDelegate.swift
func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
    // 发送token到服务器
}
```

---

版本：1.0.0
更新日期：2026-04-05
