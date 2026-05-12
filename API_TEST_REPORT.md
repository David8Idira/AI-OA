# AI-OA 后端 API 接口测试报告

> **任务**：检查 `/root/workspace/AI-OA/source/backend/` 下主要 Controller 接口，生成 API 测试清单。
> **基准路径**：`/root/workspace/AI-OA/source/backend/`
> **生成时间**：2026-05-12
> **指导思想**：实事求是，质量第一

---

## 一、API 响应统一规范

### 1.1 统一响应包装器 `Result<T>`

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1747065600000
}
```

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1xxxx | 业务错误（如 10001 用户名密码错误） |

### 1.2 分页响应 `PageResult<T>`

```json
{
  "total": 100,
  "pageNum": 1,
  "pageSize": 10,
  "totalPages": 10,
  "hasNext": true,
  "hasPrev": false,
  "records": [...]
}
```

---

## 二、aioa-system 模块

### 2.1 SysUserController — 用户管理

**基础路径**：`/api/v1/users`

| # | Method | Path | 功能 | 优先级 | 认证 | 请求体 | 响应 |
|---|--------|------|------|--------|------|--------|------|
| 1 | POST | `/login` | 用户登录 | **P0** | 否 | `LoginDTO` | `UserVO` |
| 2 | POST | `/register` | 用户注册 | **P0** | 否 | `RegisterDTO` | `SysUser` |
| 3 | GET | `/current` | 获取当前用户信息 | **P0** | `@Login` | — | `UserVO` |
| 4 | PUT | `/password` | 修改密码 | **P0** | `@Login` | `UpdatePasswordDTO` | `Void` |
| 5 | GET | `/permissions` | 获取用户权限 | **P1** | `@Login` | — | `List<String>` |
| 6 | GET | `/menus` | 获取用户菜单 | **P1** | `@Login` | — | `List<?>` |

**LoginDTO**：
```json
{ "username": "string", "password": "string" }
```

**RegisterDTO**：
```json
{ "username": "string", "password": "string", "nickname": "string" }
```

**UpdatePasswordDTO**：
```json
{ "oldPassword": "string", "newPassword": "string" }
```

---

### 2.2 SysDepartmentController — 部门管理

**基础路径**：`/api/v1/departments`

| # | Method | Path | 功能 | 优先级 | 请求体 | 响应 |
|---|--------|------|------|--------|--------|------|
| 1 | GET | `/tree` | 获取部门树 | **P0** | — | `List<SysDepartment>` |
| 2 | GET | `/` | 部门列表 | **P1** | — | `List<SysDepartment>` |
| 3 | GET | `/{id}` | 部门详情 | **P1** | — | `SysDepartment` |
| 4 | POST | `/` | 创建部门 | **P0** | `SysDepartment` | `String` (ID) |
| 5 | PUT | `/{id}` | 更新部门 | **P0** | `SysDepartment` | `Void` |
| 6 | DELETE | `/{id}` | 删除部门 | **P0** | — | `Void` |
| 7 | GET | `/{id}/children` | 获取子部门ID列表 | **P1** | — | `List<String>` |

> ⚠️ **注意**：部门 Controller 未使用 `@Login` 注解，与 User/Role/Menu Controller 不一致，需要确认是否为设计缺陷。

---

### 2.3 SysRoleController — 角色管理

**基础路径**：`/api/v1/roles`

| # | Method | Path | 功能 | 优先级 | 请求体 | 响应 |
|---|--------|------|------|--------|--------|------|
| 1 | GET | `/` | 角色列表 | **P0** | — | `List<SysRole>` |
| 2 | GET | `/{id}` | 角色详情 | **P1** | — | `SysRole` |
| 3 | POST | `/` | 创建角色 | **P0** | `SysRole` | `String` (ID) |
| 4 | PUT | `/{id}` | 更新角色 | **P0** | `SysRole` | `Void` |
| 5 | DELETE | `/{id}` | 删除角色 | **P0** | — | `Void` |
| 6 | GET | `/tree` | 角色树 | **P1** | — | `List<SysRole>` |
| 7 | GET | `/user/{userId}` | 获取用户角色 | **P1** | — | `List<SysRole>` |
| 8 | POST | `/user/{userId}/assign` | 分配角色 | **P0** | `List<String>` | `Void` |
| 9 | GET | `/knowledge-config` | 知识库权限配置列表 | **P1** | — | `List<Map>` |
| 10 | GET | `/{id}/knowledge-access` | 知识库访问详情 | **P2** | — | `Map<String,Object>` |
| 11 | PUT | `/{id}/knowledge-access` | 更新知识库访问配置 | **P2** | `@RequestParam` | `Void` |

> ⚠️ **注意**：该 Controller 未使用 `@Login` 注解。

---

### 2.4 SysMenuController — 菜单管理

**基础路径**：`/api/v1/menus`

| # | Method | Path | 功能 | 优先级 | 请求体 | 响应 |
|---|--------|------|------|--------|--------|------|
| 1 | GET | `/tree` | 菜单树 | **P0** | — | `List<SysMenu>` |
| 2 | GET | `/user/{userId}` | 用户菜单树 | **P1** | — | `List<SysMenu>` |
| 3 | GET | `/user/{userId}/router` | 路由菜单 | **P1** | — | `List<Object>` |
| 4 | GET | `/user/{userId}/permissions` | 用户权限标识 | **P1** | — | `List<String>` |
| 5 | GET | `/` | 菜单列表 | **P1** | — | `List<SysMenu>` |
| 6 | GET | `/{id}` | 菜单详情 | **P2** | — | `SysMenu` |
| 7 | POST | `/` | 创建菜单 | **P0** | `SysMenu` | `String` (ID) |
| 8 | PUT | `/{id}` | 更新菜单 | **P0** | `SysMenu` | `Void` |
| 9 | DELETE | `/{id}` | 删除菜单 | **P0** | — | `Void` |

> ⚠️ **注意**：该 Controller 未使用 `@Login` 注解。

---

## 三、aioa-reimburse 模块（报销审批）

**基础路径**：`/api/v1/reimburse`

| # | Method | Path | 功能 | 优先级 | 认证 | 请求体/参数 | 响应 |
|---|--------|------|------|--------|------|------------|------|
| 1 | POST | `/` | 提交报销 | **P0** | `@Login` | `CreateReimburseDTO` | `ReimburseVO` |
| 2 | GET | `/` | 查询报销列表 | **P0** | `@Login` | `ReimburseQueryDTO` (Query) | `PageResult<ReimburseVO>` |
| 3 | GET | `/{id}` | 报销详情 | **P0** | `@Login` | — | `ReimburseVO` |
| 4 | DELETE | `/{id}` | 删除（撤回）报销 | **P0** | `@Login` | — | `Boolean` |
| 5 | POST | `/{id}/action` | 审批操作 | **P0** | `@Login` | `ReimburseActionDTO` | `ReimburseVO` |
| 6 | POST | `/ocr-auto-fill` | OCR自动填单预览 | **P1** | `@Login` | `OcrAutoFillDTO` | `OcrAutoFillVO` |
| 7 | POST | `/ocr-auto-fill/create` | OCR预览并创建报销 | **P1** | `@Login` | `OcrAutoFillDTO` | `ReimburseVO` |
| 8 | GET | `/pending` | 待我审批列表 | **P0** | `@Login` | `pageNum, pageSize` (Query) | `PageResult<ReimburseVO>` |
| 9 | GET | `/my` | 我提交的报销 | **P0** | `@Login` | `pageNum, pageSize` (Query) | `PageResult<ReimburseVO>` |
| 10 | GET | `/pending/count` | 待审批数量 | **P1** | `@Login` | — | `Long` |
| 11 | GET | `/{id}/invoices` | 报销发票列表 | **P1** | `@Login` | — | `List<Invoice>` |
| 12 | POST | `/invoices/{invoiceId}/verify` | 发票核验 | **P2** | `@Login` | `verified, remark` (Query) | `Boolean` |
| 13 | GET | `/statistics` | 报销统计 | **P1** | `@Login` | — | `Map<String,Object>` |

### 3.1 CreateReimburseDTO

```json
{
  "title": "string [必填, ≤200]",
  "type": "string [必填]",
  "currency": "CNY",
  "priority": "integer [必填, 0-3]",
  "approverId": "string [必填]",
  "reimburseDate": "LocalDateTime [必填]",
  "expectPayDate": "LocalDateTime",
  "payMethod": "string",
  "bankAccount": "string",
  "bankName": "string",
  "ccUsers": "string",
  "description": "string [≤2000]",
  "attachments": "string",
  "ocrAutoFill": "integer",
  "items": "[List<ReimburseItemDTO> 必填, ≥1项]"
}
```

### 3.2 ReimburseActionDTO

```json
{
  "actionType": "string [必填, APPROVE|REJECT|CANCEL|REQUEST_EXTRA]",
  "comment": "string [≤500]",
  "reason": "string",
  "nextApproverId": "string"
}
```

### 3.3 ReimburseQueryDTO（Query参数）

```
GET /api/v1/reimburse?keyword=&type=&status=&mode=MY_APPLY&pageNum=1&pageSize=10&...
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| pageNum | Integer | 1 | 页码 |
| pageSize | Integer | 10 | 每页数量 |
| keyword | String | — | 标题/描述关键词 |
| type | String | — | 报销类型 |
| status | Integer | — | 状态：0草稿 1待审批 2已通过 3已拒绝 4已撤回 5已支付 |
| mode | String | MY_APPLY | MY_APPLY/MY_APPROVE/ALL |
| startDate | LocalDateTime | — | 创建时间起 |
| endDate | LocalDateTime | — | 创建时间止 |
| reimburseDateStart | LocalDateTime | — | 报销日期起 |
| reimburseDateEnd | LocalDateTime | — | 报销日期止 |
| minAmount | BigDecimal | — | 最小金额 |
| maxAmount | BigDecimal | — | 最大金额 |

---

## 四、aioa-attendance 模块（考勤打卡）

### 4.1 AttendanceController

**基础路径**：`/api/attendance`

| # | Method | Path | 功能 | 优先级 | 认证 | 请求体/参数 | 响应 |
|---|--------|------|------|--------|------|------------|------|
| 1 | POST | `/checkin` | 打卡/签退 | **P0** | 否 | `CheckinDTO` | `AttendanceRecord` |
| 2 | GET | `/today` | 今日考勤记录 | **P0** | 否 | `userId` (Query) | `AttendanceRecord` |
| 3 | GET | `/hasCheckedIn` | 今日是否已打卡 | **P1** | 否 | `userId` (Query) | `Boolean` |
| 4 | POST | `/list` | 查询考勤记录 | **P0** | 否 | `AttendanceQueryDTO` | `PageResult<AttendanceRecord>` |
| 5 | GET | `/summary` | 考勤汇总 | **P1** | 否 | `userId, startDate, endDate` (Query) | `Map<String,Object>` |
| 6 | GET | `/monthlyReport` | 月度考勤报告 | **P1** | 否 | `userId, year, month` (Query) | `Map<String,Object>` |
| 7 | GET | `/departmentReport` | 部门考勤报告 | **P1** | 否 | `deptId, startDate, endDate` (Query) | `Map<String,Object>` |
| 8 | POST | `/autoCheckout` | 自动签退（管理员） | **P2** | 否 | — | `Void` |
| 9 | GET | `/calculateDistance` | 计算GPS距离 | **P2** | 否 | `lat1, lon1, lat2, lon2` (Query) | `Double` |

**CheckinDTO**：
```json
{
  "userId": "string [必填]",
  "checkinType": "integer [0-2]",
  "latitude": "BigDecimal",
  "longitude": "BigDecimal",
  "address": "string",
  "wifiMac": "string",
  "deviceId": "string",
  "ip": "string",
  "method": "integer [0-3, 默认0]",
  "remark": "string",
  "photoUrl": "string",
  "locationId": "Long"
}
```

> ⚠️ **严重问题**：考勤模块所有接口 **均未使用 `@Login` 注解**，即无需认证即可打卡和查询任何人考勤记录，**存在严重安全风险**。

---

### 4.2 AttendanceRuleController

**基础路径**：`/api/attendance/rules`

| # | Method | Path | 功能 | 优先级 | 认证 | 请求体/参数 | 响应 |
|---|--------|------|------|--------|------|------------|------|
| 1 | POST | `/` | 创建考勤规则 | **P0** | 否 | `AttendanceRuleDTO` | `AttendanceRule` |
| 2 | PUT | `/{id}` | 更新考勤规则 | **P0** | 否 | `AttendanceRuleDTO` | `AttendanceRule` |
| 3 | DELETE | `/{id}` | 删除考勤规则 | **P0** | 否 | — | `Boolean` |
| 4 | GET | `/{id}` | 获取规则详情 | **P1** | 否 | — | `AttendanceRule` |
| 5 | GET | `/code/{ruleCode}` | 按编码获取规则 | **P1** | 否 | — | `AttendanceRule` |
| 6 | GET | `/list` | 规则列表（分页） | **P1** | 否 | `pageNum, pageSize, keyword, status` (Query) | `PageResult<AttendanceRule>` |
| 7 | GET | `/applicable` | 获取适用规则 | **P1** | 否 | `userId, deptId, positionId` (Query) | `List<AttendanceRule>` |
| 8 | PUT | `/{id}/status` | 启用/禁用规则 | **P0** | 否 | `status` (Query) | `Boolean` |

> ⚠️ **严重问题**：同上，**所有接口无认证保护**。

---

## 五、aioa-workflow 模块（流程审批）

### 5.1 ApprovalController

**基础路径**：`/api/v1/approvals`

| # | Method | Path | 功能 | 优先级 | 认证 | 请求体/参数 | 响应 |
|---|--------|------|------|--------|------|------------|------|
| 1 | GET | `/` | 查询审批列表 | **P0** | `@Login` | `ApprovalQueryDTO` (Query) | `PageResult<ApprovalVO>` |
| 2 | GET | `/{id}` | 审批详情 | **P0** | `@Login` | — | `ApprovalVO` |
| 3 | POST | `/` | 创建审批 | **P0** | `@Login` | `CreateApprovalDTO` | `ApprovalVO` |
| 4 | POST | `/{id}/action` | 审批操作 | **P0** | `@Login` | `ApprovalActionDTO` | `ApprovalVO` |
| 5 | POST | `/{id}/cancel` | 撤回审批 | **P0** | `@Login` | `reason` (Query) | `Void` |
| 6 | GET | `/pending` | 待我审批列表 | **P0** | `@Login` | `pageNum, pageSize` (Query) | `PageResult<ApprovalVO>` |
| 7 | GET | `/my` | 我发起的审批 | **P0** | `@Login` | `pageNum, pageSize` (Query) | `PageResult<ApprovalVO>` |
| 8 | GET | `/statistics` | 审批统计 | **P1** | `@Login` | — | `Map<String,Object>` |
| 9 | GET | `/pending/count` | 待审批数量 | **P1** | `@Login` | — | `Long` |
| 10 | POST | `/{id}/reassign` | 转交审批人 | **P1** | `@Login` | `newApproverId, reason` (Query) | `ApprovalVO` |

### 5.2 ApprovalActionDTO

```json
{
  "actionType": "integer [必填, 1-Approve 2-Reject 3-Transfer 4-Cancel]",
  "comment": "string [≤500]",
  "transferToId": "string",
  "attachments": "string",
  "nextApproverId": "string"
}
```

### 5.3 ApprovalQueryDTO（Query参数）

```
GET /api/v1/approvals?keyword=&type=&status=&mode=&pageNum=1&pageSize=10&...
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| pageNum | Integer | 1 | 页码 |
| pageSize | Integer | 10 | 每页数量 |
| type | String | — | 审批类型 |
| status | Integer | — | 状态 |
| priority | Integer | — | 优先级 |
| applicantId | String | — | 申请人ID |
| approverId | String | — | 当前审批人ID |
| keyword | String | — | 标题关键词 |
| startDate | String | — | 创建时间起（yyyy-MM-dd） |
| endDate | String | — | 创建时间止 |
| mode | String | — | MY_APPLY/MY_APPROVE |

---

### 5.4 N8nWorkflowController

**基础路径**：`/api/workflow/n8n`

| # | Method | Path | 功能 | 优先级 | 认证 | 请求体 |
|---|--------|------|------|--------|------|--------|
| 1 | POST | `/register` | 注册工作流 | **P2** | 否 | `N8nWorkflowDTO` |
| 2 | POST | `/trigger/{workflowId}` | 触发工作流 | **P2** | 否 | Object |
| 3 | GET | `/status/{workflowId}` | 获取工作流状态 | **P2** | 否 | — |
| 4 | POST | `/webhook/test` | 测试Webhook | **P2** | 否 | Object |

---

## 六、其他模块 Controller（概览）

| 模块 | Controller | 路径 | API数量 |
|------|-----------|------|--------|
| aioa-hr | DepartmentController | `/api/v1/hr/departments` | — |
| aioa-hr | EmployeeController | `/api/v1/hr/employees` | — |
| aioa-asset | AssetInfoController | `/api/v1/assets` | — |
| aioa-asset | AssetCategoryController | `/api/v1/asset-categories` | — |
| aioa-asset | OfficeSupplyController | `/api/v1/office-supplies` | — |
| aioa-asset | StockController | `/api/v1/stocks` | — |
| aioa-asset | LabelController | `/api/v1/labels` | — |
| aioa-ai | AiController | `/api/v1/ai` | — |
| aioa-ai | AiModelConfigController | `/api/v1/ai/model-config` | — |
| aioa-ai | AiQuotaController | `/api/v1/ai/quota` | — |
| aioa-ai | AiUsageHistoryController | `/api/v1/ai/usage-history` | — |
| aioa-knowledge | KnowledgeController | `/api/v1/knowledge` | — |
| aioa-knowledge | HealthController | `/api/v1/health` | — |
| aioa-ocr | OcrController | `/api/v1/ocr` | — |
| aioa-report | ReportController | `/api/v1/reports` | — |
| aioa-im | ImController | `/api/v1/im` | — |

---

## 七、关键发现与风险清单

### 🔴 高风险（必须修复）

| ID | 模块 | 问题 | 影响 |
|----|------|------|------|
| **R-01** | aioa-attendance | 所有考勤接口（AttendanceController + AttendanceRuleController）**均无 `@Login` 认证注解**，任何人可查询/打卡任意用户考勤 | 考勤数据泄露、伪造打卡记录 |
| **R-02** | aioa-system | SysDepartmentController、SysRoleController、SysMenuController **均无 `@Login` 认证注解** | 部门/角色/菜单数据可被任意操作 |

### 🟡 中风险（需确认）

| ID | 模块 | 问题 | 影响 |
|----|------|------|------|
| **Y-01** | aioa-system | UserController 使用 `@Login` 但部门/角色/菜单 Controller 不使用，**认证策略不一致** | 同一系统两套安全模型，容易混淆 |
| **Y-02** | aioa-reimburse | 所有 `@RequestAttribute("userId")` 均依赖调用方传入，存在**横向越权风险**——用户可修改请求体 userId 操他人数据 | 需在 Service 层严格校验 userId 与资源归属关系 |
| **Y-03** | aioa-workflow | 同报销模块，`userId` 通过 `@RequestAttribute` 传入，存在**横向越权风险** | 同上 |

### 🟢 设计建议

| ID | 模块 | 建议 |
|----|------|------|
| **G-01** | 全局 | 建议统一 API 响应 `code` 枚举，与 HTTP Status Code 对齐（200/400/401/403/404/500） |
| **G-02** | aioa-attendance | 打卡接口建议增加**设备签名/指纹校验**，防伪造 GPS 坐标打卡 |
| **G-03** | aioa-workflow | `/reassign` 接口缺少 `reason` 的必填校验，`reason` 为 required=false |
| **G-04** | aioa-reimburse | `ocrAutoFillAndCreate` 接口在 OCR 识别失败时返回内容需明确指定 |
| **G-05** | 全局 | 部分 Query 参数使用 `@ModelAttribute`，建议统一 QueryDTO 避免遗漏字段 |

---

## 八、测试优先级矩阵

### P0 — 核心流程（必须通过）

| 模块 | 接口 | 测试要点 |
|------|------|----------|
| system | POST `/users/login` | 正常登录、密码错误、账户禁用 |
| system | POST `/users/register` | 正常注册、用户名重复 |
| system | GET `/users/current` | Token 有效/无效/过期 |
| system | POST `/departments` | 正常创建、部门名重复 |
| system | PUT `/departments/{id}` | 正常更新、部门不存在 |
| system | DELETE `/departments/{id}` | 正常删除、有关联用户时删除 |
| reimburse | POST `/reimburse` | 正常提交、字段校验、items 不能为空 |
| reimburse | GET `/reimburse` | 分页、mode=MY_APPLY/MY_APPROVE |
| reimburse | POST `/{id}/action` | APPROVE/REJECT/CANCEL 状态校验 |
| reimburse | GET `/pending` | 待审批列表分页 |
| attendance | POST `/checkin` | 正常打卡、重复打卡、同一天两次打卡 |
| attendance | POST `/checkin` (checkinType=1) | 签退、超过上班时间未签退 |
| workflow | POST `/approvals` | 正常创建 |
| workflow | POST `/{id}/action` | 审批操作状态校验 |
| workflow | POST `/{id}/cancel` | 申请人撤回、非申请人撤回 |

### P1 — 重要功能（应该通过）

| 模块 | 接口 | 测试要点 |
|------|------|----------|
| system | PUT `/users/password` | 旧密码正确/错误 |
| reimburse | POST `/ocr-auto-fill` | OCR 记录存在/不存在 |
| reimburse | POST `/ocr-auto-fill/create` | OCR 自动创建报销草稿 |
| reimburse | DELETE `/{id}` | 草稿删除、待审批删除（非申请人）、已通过删除 |
| reimburse | GET `/statistics` | 统计数据完整性 |
| attendance | GET `/today` | 查询今日考勤 |
| attendance | GET `/summary` | 考勤汇总统计 |
| attendance | GET `/monthlyReport` | 月度报告 |
| workflow | GET `/statistics` | 审批统计 |
| workflow | GET `/pending/count` | 待审批数量 |

### P2 — 辅助功能（最好通过）

| 模块 | 接口 | 测试要点 |
|------|------|----------|
| reimburse | POST `/invoices/{invoiceId}/verify` | 发票核验通过/拒绝 |
| attendance | POST `/autoCheckout` | 管理员自动签退 |
| attendance | GET `/departmentReport` | 部门考勤报告 |
| workflow | POST `/{id}/reassign` | 转交审批人 |
| workflow | POST `/workflow/n8n/trigger/{workflowId}` | 触发 n8n 工作流 |

---

*报告基于源码静态分析生成，未实际调用接口。测试数据需准备真实测试账号、部门和考勤规则。*
