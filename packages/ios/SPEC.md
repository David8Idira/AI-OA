# AI-OA iOS 技术规格文档

## 1. 项目概述

- **项目名称**: AI-OA
- **iOS Bundle ID**: `com.company.ai-oa`
- **核心功能**: 智能办公平台，提供审批流程、任务管理、公告通知、签到打卡等OA功能
- **目标用户**: 企业员工、行政管理人员
- **支持版本**: iOS 15.0+

## 2. 技术栈

### 框架与语言
- **语言**: Swift 5.9+
- **UI框架**: SwiftUI
- **最小部署版本**: iOS 15.0

### 关键依赖

| 库名 | 版本 | 用途 |
|------|------|------|
| Alamofire | ~> 5.9 | HTTP网络库 |
| SnapKit | ~> 5.7 | AutoLayout（用于UIKit混合开发） |
| KeychainAccess | ~> 4.2 | 安全存储 |
| Kingfisher | ~> 7.12 | 图片加载与缓存 |
| SwiftDate | ~> 7.0 | 日期处理 |

### 架构
- **模式**: MVVM (Model-View-ViewModel)
- **响应式**: Combine框架

## 3. 项目结构

```
ios/
├── AiOA/
│   ├── App/
│   │   ├── AiOAApp.swift      # App入口
│   │   └── ContentView.swift  # 根视图
│   ├── Models/
│   │   ├── User.swift         # 用户模型
│   │   └── Result.swift       # API结果封装
│   ├── Views/
│   │   ├── HomeView.swift     # 主工作台
│   │   ├── LoginView.swift    # 登录页
│   │   └── Theme.swift        # 主题配置
│   ├── Services/
│   │   ├── APIClient.swift    # 网络请求封装
│   │   └── NetworkService.swift # 业务服务层
│   └── Utils/
│       └── Constants.swift    # 常量定义
├── Resources/
│   └── Assets.xcassets        # 资源文件
├── project.yml                # XcodeGen配置
├── Podfile                    # CocoaPods依赖
└── SPEC.md                    # 本文档
```

## 4. 主要功能模块

### 4.1 认证模块
- 用户名/密码登录
- Token存储（Keychain）
- 自动登录

### 4.2 工作台
- 欢迎卡片（用户信息 + 统计数据）
- 快捷操作入口（发起流程、审批、签到等）
- 公告列表

### 4.3 任务管理
- 任务列表展示
- 任务状态筛选

### 4.4 消息中心
- 消息列表

### 4.5 个人中心
- 用户信息展示
- 设置入口
- 退出登录

## 5. 依赖管理

### CocoaPods
```bash
cd ios
pod install
```

### XcodeGen
```bash
cd ios
xcodegen generate
```

## 6. 构建与运行

### 前置条件
- Xcode 15.0+
- CocoaPods
- XcodeGen

### 构建步骤
```bash
cd ios
xcodegen generate
pod install
open AiOA.xcworkspace
```

或使用提供的构建脚本：
```bash
chmod +x scripts/build.sh
./scripts/build.sh
```

## 7. 配置说明

### API配置
在 `AiOA/Utils/Constants.swift` 中修改 `API.baseURL` 为实际接口地址。

### Bundle ID
在 `project.yml` 中修改 `PRODUCT_BUNDLE_IDENTIFIER`。

## 8. 注意事项

- 项目使用SwiftUI作为主要UI框架
- SnapKit用于UIKit混合开发场景
- 所有API请求通过Alamofire处理，包含自动Token注入和401处理
- 敏感信息存储在Keychain中
