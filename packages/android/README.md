# AI-OA Android APP部署包

## 包含内容

```
android/
├── README.md                    # 本文档
├── VERSION                    # 版本信息
├── SPEC.md                    # 技术规格
├── app/
│   └── src/
│       └── main/
│           └── java/
│               └── com/aioa/app/
├── scripts/
│   ├── build.sh              # 构建脚本
│   └── sign.sh               # 签名脚本
├── config/
│   ├── ApiConfig.kt         # API配置
│   └── BuildConfig.kt        # 构建配置
├── docs/
│   ├── 编译指南.md            # 编译说明
│   ├── 应用市场上架指南.md    # 各渠道发布
│   └── 常见问题.md           # FAQ
└── links/
    ├── Android开发者中心.url
    ├── 应用宝.url
    ├── 华为应用市场.url
    └── 小米应用商店.url
```

## 技术规格

| 项目 | 规格 |
|------|------|
| 开发语言 | Kotlin 1.9+ |
| 开发框架 | Jetpack Compose |
| 最低版本 | Android 6.0 (API 23) |
| 目标版本 | Android 14 (API 34) |
| Gradle版本 | 8.2+ |
| 包格式 | APK / AAB |

## 快速构建

### Android Studio构建

```bash
# 1. 安装Android Studio
# 下载地址：https://developer.android.com/studio

# 2. 导入项目
# File -> Open -> 选择本目录

# 3. 配置签名
# Build -> Generate Signed Bundle/APK -> 选择Android App Bundle

# 4. 选择构建类型
# Release版本需要配置签名

# 5. 构建
# Build -> Build Bundle(s) / APK(s)
```

### 命令行构建

```bash
# 调试版本
./gradlew assembleDebug

# 发布版本
./gradlew assembleRelease

# 输出目录
# app/build/outputs/apk/release/
```

## API配置

```kotlin
// config/ApiConfig.kt
object ApiConfig {
    const val DEV_BASE_URL = "http://192.168.1.100:8080"
    const val PROD_BASE_URL = "https://aioa.example.com"
    
    const val DEV_WS_URL = "ws://192.168.1.100:8080/ws"
    const val PROD_WS_URL = "wss://aioa.example.com/ws"
}
```

## 多渠道构建

### 配置签名

```properties
# keystore.properties (不提交到git)
storePassword=你的密码
keyPassword=你的密码
keyAlias=你的别名
storeFile=keystore路径
```

### Gradle多渠道

```kotlin
android {
    flavorDimensions += "version"
    productFlavors {
        create("huawei") {
            dimension = "version"
            applicationIdSuffix = ".huawei"
            resValue("string", "app_name", "AI-OA华为")
        }
        create("xiaomi") {
            dimension = "version"
            applicationIdSuffix = ".xiaomi"
            resValue("string", "app_name", "AI-OA小米")
        }
        create("oppo") {
            dimension = "version"
            applicationIdSuffix = ".oppo"
            resValue("string", "app_name", "AI-OAOPPO")
        }
        create("vivo") {
            dimension = "version"
            applicationIdSuffix = ".vivo"
            resValue("string", "app_name", "AI-OAvivo")
        }
        create("official") {
            dimension = "version"
            // 官方版本
        }
    }
}
```

## 应用市场上架

### 1. 华为应用市场

1. 注册华为开发者账号
2. 创建应用
3. 上传签名证书指纹
4. 提交审核

### 2. 应用宝（腾讯）

1. 注册腾讯开发者账号
2. 创建应用
3. 上传APK/AAB
4. 提交审核

### 3. 小米应用商店

1. 注册小米开发者账号
2. 创建应用
3. 上传APK
4. 提交审核

## 发布检查清单

| 检查项 | 要求 |
|--------|------|
| 应用图标 | 192x192 PNG |
| 应用截图 | 至少6张 |
| 应用描述 | 100字以上 |
| 隐私政策 | 必须提供URL |
| 权限说明 | 说明申请原因 |
| 签名证书 | 正式证书 |

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

| 权限 | 用途 | Android版本 |
|------|------|------------|
| INTERNET | 网络访问 | 所有版本 |
| CAMERA | 拍照/扫码 | 所有版本 |
| READ_EXTERNAL_STORAGE | 读取文件 | < Android 13 |
| READ_MEDIA_IMAGES | 选择图片 | Android 13+ |
| ACCESS_FINE_LOCATION | 精确定位 | 考勤用 |
| POST_NOTIFICATIONS | 推送通知 | Android 13+ |

## 推送配置

### 华为 HMS Push

```gradle
// build.gradle
implementation 'com.huawei.hms:push:6.10.0.300'
```

### 小米 Push

```gradle
// build.gradle
implementation 'com.xiaomi.push:xiaomi-push:10.5.0'
```

---

版本：1.0.0
更新日期：2026-04-05
