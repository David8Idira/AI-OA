# AI-OA RabbitMQ单队列架构

> 分支：rabbitmq-only
> 
> 适用场景：中小企业/中型企业，追求架构简洁，统一消息队列

---

## 一、架构设计

### 1.1 设计理念

| 理念 | 说明 |
|------|------|
| **简单优先** | 减少系统复杂度，降低运维成本 |
| **够用就好** | RabbitMQ完全满足OA场景性能需求 |
| **统一队列** | 统一消息队列，减少学习成本 |

### 1.2 架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        负载均衡层 (Nginx)                         │
└────────────────────────────────┬────────────────────────────────┘
                                 │
┌────────────────────────────────┴────────────────────────────────┐
│                         应用服务层                                │
│  ┌────────┬────────┬────────┬────────┬────────┬────────┐       │
│  │用户服务│审批服务│报表服务│AI服务  │聊天服务│网关服务│       │
│  └────┬───┴────┬───┴────┬───┴────┬───┴────┬───┘       │
│       │         │         │         │                          │
│       └─────────┴─────────┼┴─────────┘                          │
│                           │                                       │
│                    ┌──────┴──────┐                              │
│                    │  RabbitMQ   │                              │
│                    │   集群(3节点) │                             │
│                    └──────┬──────┘                              │
│                           │                                       │
│       ┌──────────────────┼──────────────────┐                  │
│       │                  │                  │                   │
│  ┌────┴────┐     ┌─────┴────┐     ┌─────┴────┐            │
│  │ 邮件通知  │     │ 审批事件  │     │ 聊天消息  │            │
│  │(可靠队列) │     │ (持久化)  │     │ (高吞吐)  │            │
│  └──────────┘     └──────────┘     └──────────┘            │
└─────────────────────────────────────────────────────────────────┘
```

---

## 二、RabbitMQ 集群设计

### 2.1 集群架构

| 节点 | 主机名 | IP | 角色 |
|------|--------|-----|------|
| 节点1 | aioa-rabbit-01 | 192.168.1.141 | master |
| 节点2 | aioa-rabbit-02 | 192.168.1.142 | slave |
| 节点3 | aioa-rabbit-03 | 192.168.1.143 | slave |

### 2.2 队列设计

| 队列名称 | 类型 | 说明 | 消费者数量 |
|----------|------|------|------------|
| aioa.approval | 持久化 | 审批流程事件 | 3-5 |
| aioa.chat | 镜像队列 | 聊天消息 | 5-10 |
| aioa.mail | 可靠队列 | 邮件发送（确认机制） | 2-3 |
| aioa.notify | 广播 | 系统通知推送 | 3-5 |
| aioa.report | 延迟队列 | 报表生成任务 | 2 |
| aioa.ai | 持久化 | AI服务请求 | 3-5 |

### 2.3 交换机设计

| 交换机名称 | 类型 | 说明 |
|------------|------|------|
| aioa.topic | topic | 主题交换机，路由审批/通知 |
| aioa.chat | fanout | 聊天消息广播 |
| aioa.mail | direct | 邮件直接投递 |
| aioa.dlx | direct | 死信队列 |

---

## 三、性能指标

### 3.1 RabbitMQ 性能数据

| 场景 | 消息量/秒 | 延迟 | 备注 |
|------|-----------|------|------|
| 审批流程 | 500-1000 | <10ms | 足够 |
| 企业聊天 | 2000-5000 | <50ms | 足够 |
| 邮件通知 | 100-500 | <100ms | 可靠投递 |
| 报表触发 | 10-50 | <1s | 异步任务 |

### 3.2 对比：RabbitMQ vs Kafka

| 维度 | RabbitMQ | Kafka | OA场景结论 |
|------|-----------|-------|------------|
| **适用场景** | 企业级消息 | 大数据流处理 | OA两者皆可 |
| **吞吐量** | 万级/秒 | 百万级/秒 | RabbitMQ足够 |
| **延迟** | 微秒级 | 毫秒级 | RabbitMQ更低 |
| **可靠性** | 消息确认 | 持久化+ACK | 两者皆优 |
| **运维复杂度** | 中等 | 高 | RabbitMQ更简单 |
| **功能** | 消息路由丰富 | 流处理强 | RabbitMQ功能更贴合 |

### 3.3 结论

对于OA系统（用户量<10万，并发<1000），**RabbitMQ单集群完全满足需求**：
- ✅ 足够吞吐量
- ✅ 更好的延迟
- ✅ 更简单的运维
- ✅ 更低的资源消耗

---

## 四、部署配置

### 4.1 RabbitMQ 集群配置

```bash
# 1. 安装Erlang
yum install -y erlang

# 2. 安装RabbitMQ
wget https://github.com/rabbitmq/rabbitmq-server/releases/download/v3.12.6/rabbitmq-server-3.12.6-1.el8.noarch.rpm
rpm --import https://github.com/rabbitmq/signing-keys/releases/download/3.0/rabbitmq-release-signing-key.asc
yum install -y rabbitmq-server-3.12.6-1.el8.noarch.rpm

# 3. 配置Cookie（所有节点相同）
echo "COOKIE=aioa_rabbitmq_cluster_cookie" > /var/lib/rabbitmq/.erlang.cookie

# 4. 配置集群（节点1）
cat > /etc/rabbitmq/rabbitmq.conf << 'EOF'
listeners.tcp.default = 5672
management.tcp.port = 15672
cluster_formation.peer_discovery_backend = rabbit_peer_discovery_classic_config
cluster_formation.classic_config.nodes.1 = rabbit@aioa-rabbit-01
cluster_formation.classic_config.nodes.2 = rabbit@aioa-rabbit-02
cluster_formation.classic_config.nodes.3 = rabbit@aioa-rabbit-03
EOF

# 5. 启动服务
systemctl start rabbitmq-server
systemctl enable rabbitmq-server

# 6. 创建用户和VHost
rabbitmqctl add_user aioa AioaPassword123
rabbitmqctl set_permissions -p / aioa ".*" ".*" ".*"
rabbitmqctl set_user_tags aioa administrator

# 7. 启用管理插件
rabbitmq-plugins enable rabbitmq_management
```

### 4.2 镜像队列配置

```bash
# 创建高可用队列
rabbitmqctl set_policy ha-all "^aioa\." '{"ha-mode":"all","ha-sync-mode":"automatic"}'
```

### 4.3 应用配置

```yaml
# application.yml
spring:
  rabbitmq:
    host: 192.168.1.141,192.168.1.142,192.168.1.143
    port: 5672
    username: aioa
    password: AioaPassword123
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10
    template:
      mandatory: true
```

---

## 五、监控与运维

### 5.1 监控指标

| 指标 | 告警阈值 | 说明 |
|------|----------|------|
| 队列消息数 | >10000 | 消息堆积 |
| 消费者数量 | =0 | 消费者断开 |
| 内存使用 | >80% | 内存泄漏 |
| 磁盘空间 | <20% | 磁盘不足 |

### 5.2 常用命令

```bash
# 查看队列状态
rabbitmqctl list_queues name messages consumers

# 查看交换机
rabbitmqctl list_exchanges

# 查看连接
rabbitmqctl list_connections

# 监控队列
rabbitmqmq-management  # Web UI

# 清理队列
rabbitmqctl purge_queue aioa.report
```

---

## 六、故障处理

### 6.1 单节点故障

| 故障 | 影响 | 处理 |
|------|------|------|
| 节点1宕机 | 消息路由到其他节点 | 自动切换，无人工介入 |
| 节点2宕机 | 同上 | 自动切换 |
| 节点3宕机 | 同上 | 自动切换 |

### 6.2 网络分区

```bash
# 手动停止集群
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl start_app

# 重新加入集群
rabbitmqctl stop_app
rabbitmqctl join_cluster rabbit@aioa-rabbit-01
rabbitmqctl start_app
```

---

*文档版本：V1.0*
*分支：rabbitmq-only*
*创建时间：2026-04-05*
