# AI-OA 单消息队列方案 (RabbitMQ Only)

## 方案说明

本分支采用**单一消息队列架构**，仅使用 RabbitMQ 作为消息中间件，适用于对消息可靠性要求高、并发量适中的场景。

### 架构特点

- **消息队列**: RabbitMQ 3.12 (支持消息确认、延迟队列、优先级队列)
- **部署方式**: Docker Compose 一键部署
- **适用场景**: 中小型企业、创业公司、验证性项目

### 与双队列方案对比

| 特性 | 单队列方案 (RabbitMQ) | 双队列方案 (Kafka + RabbitMQ) |
|------|---------------------|------------------------------|
| 消息吞吐量 | 中等 (< 10万/秒) | 高 (> 50万/秒) |
| 消息可靠性 | ✅ 完善 (确认机制) | ✅ 完善 |
| 延迟消息 | ✅ 支持 | ✅ 支持 |
| 部署复杂度 | 低 | 中 |
| 资源消耗 | 低 | 中 |
| 成本 | 低 | 中 |

### 快速启动

```bash
# 启动所有服务
docker-compose -f docker-compose-rabbitmq.yml up -d

# 查看服务状态
docker-compose -f docker-compose-rabbitmq.yml ps

# 查看日志
docker-compose -f docker-compose-rabbitmq.yml logs -f

# 停止服务
docker-compose -f docker-compose-rabbitmq.yml down
```

### 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 80 | OA系统Web界面 |
| API | 8080 | 后端REST API |
| AI服务 | 8000 | AI对话服务 |
| OCR服务 | 8001 | OCR识别服务 |
| n8n | 5678 | 工作流引擎 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| MinIO | 9000/9001 | 文件存储 |
| Nacos | 8848 | 配置中心 |
| RabbitMQ | 5672/15672 | 消息队列 (AMQP/Web管理) |

### 消息队列配置

RabbitMQ 默认账号: `guest` / `guest`
Web管理界面: http://localhost:15672

### 适用场景

✅ 适合的场景:
- 消息可靠性要求 > 吞吐量要求
- 团队规模较小，运维能力有限
- 预算有限，需要控制成本
- 快速验证 MVP

❌ 不适合的场景:
- 超高并发场景 (> 10万/秒)
- 事件溯源架构
- 大规模流处理
