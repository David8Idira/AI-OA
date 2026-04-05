# AI-OA API接口设计文档

> 文档版本：V1.0
> 更新日期：2026-04-05
> API版本：V1.0
> 基础路径：/api/v1

---

## 目录

1. [接口概述](#1-接口概述)
2. [认证授权](#2-认证授权)
3. [用户管理](#3-用户管理)
4. [审批中心](#4-审批中心)
5. [财务报销](#5-财务报销)
6. [AI助手](#6-ai助手)
7. [企业聊天](#7-企业聊天)
8. [智能报表](#8-智能报表)
9. [知识库](#9-知识库)
10. [文件管理](#10-文件管理)
11. [系统设置](#11-系统设置)
12. [错误码定义](#12-错误码定义)

---

## 1. 接口概述

### 1.1 基本规范

| 规范 | 说明 |
|------|------|
| 协议 | HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 请求方法 | GET/POST/PUT/DELETE |
| 认证方式 | Bearer Token (JWT) |

### 1.2 基础路径

```
测试环境：https://api-test.aioa.com/api/v1
生产环境：https://api.aioa.com/api/v1
```

### 1.3 请求头

```http
Content-Type: application/json
Authorization: Bearer <token>
X-Request-ID: <uuid>
X-Timestamp: <unix_timestamp>
X-Signature: <签名>
```

### 1.4 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { },
  "timestamp": 1709712000000,
  "requestId": "uuid"
}
```

### 1.5 分页格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [ ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

---

## 2. 认证授权

### 2.1 登录

```http
POST /auth/login
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | ✅ | 用户名/手机号/邮箱 |
| password | string | ✅ | 密码（MD5加密传输） |
| captcha | string | ❌ | 验证码 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 7200,
    "user": {
      "id": "10001",
      "username": "admin",
      "nickname": "管理员",
      "avatar": "https://cdn.aioa.com/avatar/10001.jpg",
      "deptId": "D001",
      "deptName": "技术部",
      "roles": ["ADMIN"]
    }
  }
}
```

### 2.2 刷新Token

```http
POST /auth/refresh
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| refreshToken | string | ✅ | 刷新令牌 |

### 2.3 登出

```http
POST /auth/logout
```

### 2.4 获取当前用户

```http
GET /auth/currentUser
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "10001",
    "username": "admin",
    "nickname": "管理员",
    "avatar": "https://cdn.aioa.com/avatar/10001.jpg",
    "email": "admin@aioa.com",
    "mobile": "13800138000",
    "deptId": "D001",
    "deptName": "技术部",
    "position": "项目经理",
    "roles": ["ADMIN"],
    "permissions": ["*:*:*"]
  }
}
```

---

## 3. 用户管理

### 3.1 用户列表

```http
GET /users
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | ❌ | 页码，默认1 |
| pageSize | int | ❌ | 每页数量，默认20 |
| keyword | string | ❌ | 搜索关键字 |
| deptId | string | ❌ | 部门ID |
| status | string | ❌ | 状态：ENABLED/DISABLED |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "10001",
        "username": "admin",
        "nickname": "管理员",
        "avatar": "https://cdn.aioa.com/avatar/10001.jpg",
        "email": "admin@aioa.com",
        "mobile": "13800138000",
        "deptId": "D001",
        "deptName": "技术部",
        "position": "项目经理",
        "status": "ENABLED",
        "createTime": "2026-01-01 10:00:00",
        "lastLoginTime": "2026-04-05 09:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 100,
      "totalPages": 5
    }
  }
}
```

### 3.2 创建用户

```http
POST /users
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | string | ✅ | 用户名 |
| password | string | ✅ | 密码 |
| nickname | string | ✅ | 昵称 |
| email | string | ✅ | 邮箱 |
| mobile | string | ❌ | 手机号 |
| deptId | string | ✅ | 部门ID |
| position | string | ❌ | 职位 |
| roleIds | string[] | ✅ | 角色ID列表 |

### 3.3 更新用户

```http
PUT /users/{id}
```

### 3.4 删除用户

```http
DELETE /users/{id}
```

### 3.5 重置密码

```http
POST /users/{id}/resetPassword
```

### 3.6 修改密码

```http
PUT /users/password
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | string | ✅ | 原密码 |
| newPassword | string | ✅ | 新密码 |

---

## 4. 审批中心

### 4.1 审批列表

```http
GET /approvals
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | ❌ | 页码 |
| pageSize | int | ❌ | 每页数量 |
| type | string | ❌ | 类型：LEAVE/PURCHASE/PAYMENT/CONTRACT |
| status | string | ❌ | 状态：PENDING/APPROVED/REJECTED |
| keyword | string | ❌ | 搜索关键字 |
| startDate | string | ❌ | 开始日期 |
| endDate | string | ❌ | 结束日期 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "A001",
        "type": "LEAVE",
        "typeName": "请假申请",
        "title": "张三-请假申请-4月5日",
        "status": "PENDING",
        "statusName": "待审批",
        "requester": {
          "id": "10002",
          "name": "张三",
          "avatar": "https://cdn.aioa.com/avatar/10002.jpg",
          "deptName": "销售部"
        },
        "currentApprover": {
          "id": "10003",
          "name": "李四",
          "avatar": "https://cdn.aioa.com/avatar/10003.jpg"
        },
        "amount": null,
        "createTime": "2026-04-05 10:00:00",
        "urgent": false
      }
    ],
    "pagination": {
      "page": 1,
      "pageSize": 20,
      "total": 50,
      "totalPages": 3
    }
  }
}
```

### 4.2 审批详情

```http
GET /approvals/{id}
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "A001",
    "type": "LEAVE",
    "typeName": "请假申请",
    "title": "张三-请假申请-4月5日",
    "status": "PENDING",
    "requester": {
      "id": "10002",
      "name": "张三",
      "avatar": "https://cdn.aioa.com/avatar/10002.jpg",
      "deptName": "销售部",
      "mobile": "13800138001"
    },
    "formData": {
      "leaveType": "年假",
      "startTime": "2026-04-05 14:00",
      "endTime": "2026-04-05 18:00",
      "duration": 4,
      "reason": "个人事务"
    },
    "attachments": [
      {
        "id": "F001",
        "name": "病假证明.pdf",
        "url": "https://cdn.aioa.com/attachments/F001.pdf",
        "size": 1024000
      }
    ],
    "approvalHistory": [
      {
        "step": 1,
        "approver": {
          "id": "10003",
          "name": "李四"
        },
        "action": "APPROVED",
        "comment": "同意",
        "actionTime": "2026-04-05 11:00:00"
      }
    ],
    "currentStep": 1,
    "totalSteps": 2,
    "createTime": "2026-04-05 10:00:00"
  }
}
```

### 4.3 创建审批

```http
POST /approvals
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | string | ✅ | 审批类型 |
| title | string | ✅ | 标题 |
| formData | object | ✅ | 表单数据 |
| attachmentIds | string[] | ❌ | 附件ID列表 |

### 4.4 审批操作

```http
POST /approvals/{id}/action
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| action | string | ✅ | APPROVE/REJECT/TRANSFER |
| comment | string | ❌ | 审批意见 |
| transferTo | string | ❌ | 转交人ID（当action=TRANSFER时必填） |

### 4.5 我的申请

```http
GET /approvals/my/requests
```

### 4.6 待我审批

```http
GET /approvals/my/tasks
```

---

## 5. 财务报销

### 5.1 OCR识别

```http
POST /finance/ocr/recognize
Content-Type: multipart/form-data
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | ✅ | 发票图片/ PDF |
| type | string | ✅ | INVOICE/RECEIPT/TRAIN/AIRLINE/TAXI |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "OCR001",
    "type": "INVOICE",
    "confidence": 0.92,
    "confidenceLevel": "HIGH",
    "data": {
      "invoiceNo": "NO12345678",
      "invoiceCode": "1100211000",
      "invoiceDate": "2026-04-01",
      "taxAmount": 13.00,
      "totalAmount": 113.00,
      "不含税金额": 100.00,
      "税额": 13.00,
      "购买方": "XX科技有限公司",
      "销售方": "XX办公用品店",
      "商品明细": [
        {
          "name": "办公用品",
          "quantity": 1,
          "price": 100.00,
          "amount": 100.00
        }
      ]
    },
    "rawText": "原始OCR识别文本...",
    "processedAt": "2026-04-05 10:00:00"
  }
}
```

### 5.2 提交报销

```http
POST /finance/reimburse
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | ✅ | 报销标题 |
| items | object[] | ✅ | 报销明细 |
| totalAmount | decimal | ✅ | 总金额 |
| bankAccount | string | ❌ | 银行账号 |
| bankName | string | ❌ | 开户行 |
| remark | string | ❌ | 备注 |

**items项结构**

| 参数名 | 类型 | 说明 |
|--------|------|------|
| ocrId | string | OCR识别记录ID |
| type | string | 类型：交通/餐饮/住宿/办公 |
| amount | decimal | 金额 |
| date | string | 日期 |
| description | string | 说明 |

### 5.3 报销列表

```http
GET /finance/reimburse
```

### 5.4 报销详情

```http
GET /finance/reimburse/{id}
```

### 5.5 发票管理

```http
GET /finance/invoices
```

### 5.6 删除发票

```http
DELETE /finance/invoices/{id}
```

---

## 6. AI助手

### 6.1 智能问答

```http
POST /ai/chat
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| message | string | ✅ | 用户问题 |
| sessionId | string | ❌ | 会话ID（新会话可不传） |
| model | string | ❌ | 模型：gpt-4o/claude-3.5/kimi-pro |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "sessionId": "S001",
    "messageId": "M001",
    "answer": "根据公司制度，年假计算方式如下：...",
    "links": [
      {
        "type": "DOC",
        "title": "员工手册-假期管理",
        "url": "/knowledge/doc/K001",
        "snippet": "年假计算：工作满1年享5天..."
      },
      {
        "type": "APPROVAL",
        "title": "请假申请流程",
        "url": "/approval/process/LEAVE"
      }
    ],
    "model": "gpt-4o",
    "tokens": 1234,
    "latency": 1200
  }
}
```

### 6.2 知识库检索

```http
POST /ai/search
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| query | string | ✅ | 搜索query |
| category | string | ❌ | 知识分类 |
| limit | int | ❌ | 返回数量，默认5 |

### 6.3 报表生成

```http
POST /ai/report/generate
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | string | ✅ | WEEKLY/MONTHLY/QUARTERLY/YEARLY |
| startDate | string | ✅ | 开始日期 |
| endDate | string | ✅ | 结束日期 |
| model | string | ❌ | 生成模型 |
| includeCharts | boolean | ❌ | 是否包含图表 |
| style | string | ❌ | 风格：SIMPLE/DETAILED |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "reportId": "R001",
    "title": "2026年第14周工作周报",
    "content": "## 本周工作总结\n\n1. 完成...",
    "attachments": [
      {
        "type": "IMAGE",
        "url": "https://cdn.aioa.com/reports/R001_chart.png"
      }
    ],
    "wordUrl": "https://cdn.aioa.com/reports/R001.docx",
    "status": "GENERATED",
    "generateTime": "2026-04-05 10:00:00"
  }
}
```

### 6.4 AI生图

```http
POST /ai/image/generate
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| prompt | string | ✅ | 图像描述 |
| size | string | ❌ | 尺寸：1024x1024/1920x1080 |
| style | string | ❌ | 风格：REALISTIC/CARTOON |

---

## 7. 企业聊天

### 7.1 会话列表

```http
GET /im/conversations
```

**响应示例**

```json
{
  "code": 200,
  "data": [
    {
      "id": "C001",
      "type": "PRIVATE",
      "name": null,
      "avatar": null,
      "members": [
        {
          "id": "10001",
          "name": "张三",
          "avatar": "https://cdn.aioa.com/avatar/10001.jpg"
        },
        {
          "id": "10002",
          "name": "李四",
          "avatar": "https://cdn.aioa.com/avatar/10002.jpg"
        }
      ],
      "lastMessage": {
        "id": "M001",
        "content": "你好！",
        "senderId": "10001",
        "sendTime": "2026-04-05 10:00:00"
      },
      "unreadCount": 5
    }
  ]
}
```

### 7.2 发送消息

```http
POST /im/messages
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| conversationId | string | ✅ | 会话ID |
| content | string | ✅ | 消息内容 |
| type | string | ✅ | TEXT/IMAGE/FILE/AUDIO |
| attachmentId | string | ❌ | 附件ID |

### 7.3 消息列表

```http
GET /im/conversations/{conversationId}/messages
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | int | ❌ | 页码 |
| pageSize | int | ❌ | 每页数量 |
| before | string | ❌ | 之前某条消息ID（翻页用） |

### 7.4 已读确认

```http
POST /im/conversations/{conversationId}/read
```

### 7.5 创建群聊

```http
POST /im/groups
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | string | ✅ | 群名称 |
| memberIds | string[] | ✅ | 成员ID列表 |
| avatar | string | ❌ | 群头像 |
| announcement | string | ❌ | 群公告 |

### 7.6 群管理

```http
PUT /im/groups/{groupId}
DELETE /im/groups/{groupId}
POST /im/groups/{groupId}/members/{userId}
DELETE /im/groups/{groupId}/members/{userId}
```

---

## 8. 智能报表

### 8.1 报表列表

```http
GET /reports
```

### 8.2 报表详情

```http
GET /reports/{id}
```

### 8.3 报表预览

```http
GET /reports/{id}/preview
```

### 8.4 下载报表

```http
GET /reports/{id}/download?format=PDF|WORD|HTML
```

### 8.5 删除报表

```http
DELETE /reports/{id}
```

---

## 9. 知识库

### 9.1 知识分类

```http
GET /knowledge/categories
```

### 9.2 知识列表

```http
GET /knowledge/docs
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | string | ❌ | 分类ID |
| keyword | string | ❌ | 搜索关键字 |
| page | int | ❌ | 页码 |
| pageSize | int | ❌ | 每页数量 |

### 9.3 知识详情

```http
GET /knowledge/docs/{id}
```

### 9.4 创建知识

```http
POST /knowledge/docs
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | string | ✅ | 标题 |
| content | string | ✅ | 内容（支持Markdown） |
| categoryId | string | ✅ | 分类ID |
| tags | string[] | ❌ | 标签 |
| visibility | string | ❌ | PUBLIC/PRIVATE |

### 9.5 更新知识

```http
PUT /knowledge/docs/{id}
```

### 9.6 删除知识

```http
DELETE /knowledge/docs/{id}
```

---

## 10. 文件管理

### 10.1 上传文件

```http
POST /files/upload
Content-Type: multipart/form-data
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | ✅ | 文件 |
| category | string | ❌ | 分类：DOC/IMAGE/AUDIO/VIDEO/OTHER |
| folderId | string | ❌ | 文件夹ID |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": "F001",
    "name": "document.pdf",
    "url": "https://cdn.aioa.com/files/F001.pdf",
    "size": 2048000,
    "mimeType": "application/pdf",
    "uploadTime": "2026-04-05 10:00:00"
  }
}
```

### 10.2 文件列表

```http
GET /files
```

### 10.3 文件预览

```http
GET /files/{id}/preview
```

### 10.4 下载文件

```http
GET /files/{id}/download
```

### 10.5 删除文件

```http
DELETE /files/{id}
```

---

## 11. 系统设置

### 11.1 获取配置

```http
GET /system/configs
```

### 11.2 更新配置

```http
PUT /system/configs
```

**请求参数**

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| configs | object | ✅ | 配置项 |

### 11.3 AI模型配置

```http
GET /system/ai/models
PUT /system/ai/models
```

**请求参数示例**

```json
{
  "models": [
    {
      "id": "gpt-4o",
      "name": "GPT-4o",
      "apiKey": "sk-***",
      "endpoint": "https://api.openai.com/v1",
      "defaultFor": ["CHAT", "REPORT"],
      "enabled": true,
      "dailyLimit": 10000
    }
  ]
}
```

### 11.4 审批流程配置

```http
GET /system/approval/workflows
POST /system/approval/workflows
PUT /system/approval/workflows/{id}
DELETE /system/approval/workflows/{id}
```

---

## 12. 错误码定义

### 12.1 错误码表

| 错误码 | 说明 | HTTP Status |
|--------|------|-------------|
| 200 | 成功 | 200 |
| 400 | 请求参数错误 | 400 |
| 401 | 未授权 | 401 |
| 403 | 禁止访问 | 403 |
| 404 | 资源不存在 | 404 |
| 409 | 资源冲突 | 409 |
| 429 | 请求过于频繁 | 429 |
| 500 | 服务器内部错误 | 500 |
| 502 | 网关错误 | 502 |
| 503 | 服务不可用 | 503 |

### 12.2 业务错误码

| 错误码 | 说明 |
|--------|------|
| 10001 | 用户名或密码错误 |
| 10002 | Token已过期 |
| 10003 | 账户已被禁用 |
| 20001 | 审批不存在 |
| 20002 | 无权审批此单 |
| 20003 | 审批状态不允许操作 |
| 30001 | OCR识别失败 |
| 30002 | 发票信息有疑问 |
| 40001 | AI服务不可用 |
| 40002 | AI模型配额不足 |
| 50001 | 文件上传失败 |
| 50002 | 文件大小超限 |
| 50003 | 文件类型不支持 |

### 12.3 错误响应示例

```json
{
  "code": 400,
  "message": "请求参数错误",
  "error": {
    "field": "username",
    "message": "用户名不能为空"
  },
  "timestamp": 1709712000000,
  "requestId": "uuid"
}
```

---

## 附录A：WebSocket接口

### A.1 消息推送

```javascript
// 连接地址
wss://api.aioa.com/ws?token=<token>

// 消息格式
{
  "type": "MESSAGE|NOTIFICATION|SYSTEM",
  "data": { }
}
```

### A.2 订阅主题

| 主题 | 说明 |
|------|------|
| /topic/im/{userId} | 个人消息 |
| /topic/approval/{userId} | 审批通知 |
| /topic/system | 系统通知 |

---

*文档版本：V1.0*
*更新日期：2026-04-05*
