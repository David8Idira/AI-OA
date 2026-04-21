# AI-OA 智能办公平台

基于Spring Boot 3.2 + JPA + MySQL的企业级智能办公系统。

## 模块说明

- **User模块** - 用户管理（CRUD）
- **Department模块** - 部门管理（树形结构支持）
- **Approval模块** - 审批流程（Process/Instance/Task三层架构）
- **AI-Chat模块** - 智能对话（会话管理/消息/提示词模板）

## 技术栈

- Spring Boot 3.2.4
- Spring Data JPA
- MySQL 8.0
- Lombok
- Validation

## 快速开始

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 测试
mvn test
```

## API基础路径

`http://localhost:8080/api`

### 用户接口
- `POST /users` - 创建用户
- `GET /users` - 获取所有用户
- `GET /users/{id}` - 获取用户详情

### 部门接口
- `POST /departments` - 创建部门
- `GET /departments` - 获取所有部门

### 审批接口
- `POST /approval/processes` - 创建审批流程
- `POST /approval/instances` - 创建审批实例
- `POST /approval/instances/{id}/submit` - 提交审批
- `POST /approval/instances/{id}/approve` - 执行审批

### AI对话接口
- `POST /chat/sessions` - 创建会话
- `POST /chat/messages` - 发送消息
- `GET /chat/messages/session/{sessionEntityId}` - 获取历史消息
