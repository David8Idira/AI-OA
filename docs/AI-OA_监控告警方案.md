# AI-OA 监控告警方案

> 文档版本：V1.0
> 更新日期：2026-04-05
> 监控系统：Prometheus + Grafana + AlertManager

---

## 目录

1. [监控架构](#1-监控架构)
2. [监控指标体系](#2-监控指标体系)
3. [告警规则配置](#3-告警规则配置)
4. [告警通知渠道](#4-告警通知渠道)
5. [告警处理流程](#5-告警处理流程)
6. [监控大盘](#6-监控大盘)
7. [容量规划](#7-容量规划)
8. [灾备方案](#8-灾备方案)

---

## 1. 监控架构

### 1.1 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                        监控层                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │ Prometheus  │  │ AlertManager │  │   Grafana   │        │
│  │   采集器    │  │    告警器   │  │   展示器    │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
└─────────┼─────────────────┼─────────────────┼────────────────┘
          │                 │                 │
┌─────────┼─────────────────┼─────────────────┼────────────────┐
│         │          数据层 │                 │                │
│  ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐        │
│  │ TimeSeriesDB│  │ Alert Store  │  │ DashboardDB │        │
│  │  (Prometheus│  │    (MySQL)  │  │  (Grafana)  │        │
│  │   Remote)   │  │              │  │             │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
          │
┌─────────┼───────────────────────────────────────────────────┐
│         │                    采集层                         │
│  ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐        │
│  │   主机监控   │  │  中间件监控  │  │   应用监控   │        │
│  │  node_exporter│ │  mysql/redis │  │ SpringBoot  │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
          │
┌─────────┼───────────────────────────────────────────────────┐
│         │                   目标主机                         │
│  ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐        │
│  │  应用服务器  │  │  数据库服务器 │  │  缓存/队列   │        │
│  │   6-10台    │  │     2-3台     │  │    3-6台     │        │
│  └─────────────┘  └─────────────┘  └─────────────┘        │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 组件清单

| 组件 | 数量 | 配置 | 职责 |
|------|------|------|------|
| Prometheus | 2 | 8核16G | 高可用采集 |
| AlertManager | 2 | 4核8G | 告警管理 |
| Grafana | 2 | 4核8G | 可视化展示 |
|Exporter| 每主机1 | 2核2G | 指标采集 |

---

## 2. 监控指标体系

### 2.1 指标分类

| 类别 | 指标数量 | 采集频率 | 保留时间 |
|------|----------|----------|----------|
| 主机指标 | 50+ | 15s | 30天 |
| 中间件指标 | 100+ | 15s | 30天 |
| 应用指标 | 200+ | 30s | 90天 |
| 业务指标 | 50+ | 60s | 180天 |

### 2.2 主机监控指标

| 指标分类 | 指标项 | 告警阈值 | 采集方式 |
|----------|--------|----------|----------|
| **CPU** | 使用率 | >80%警告，>90%严重 | node_exporter |
| **CPU** | Load1/5/15 | >CPU核心数警告 | node_exporter |
| **内存** | 使用率 | >85%警告，>95%严重 | node_exporter |
| **内存** | 可用内存 | <1GB警告 | node_exporter |
| **磁盘** | 使用率 | >90%警告，>95%严重 | node_exporter |
| **磁盘** | IO使用率 | >80%警告 | node_exporter |
| **网络** | 入/出带宽 | >80%警告 | node_exporter |
| **网络** | TCP连接数 | >10000警告 | node_exporter |
| **进程** | 进程数 | >500警告 | node_exporter |

### 2.3 MySQL监控指标

| 指标分类 | 指标项 | 告警阈值 | 说明 |
|----------|--------|----------|------|
| **连接** | 活跃连接数 | >80%max_connections | 最大连接数 |
| **连接** | 连接使用率 | >85%警告 | 当前/最大 |
| **查询** | QPS | >5000警告 | 每秒查询数 |
| **查询** | 慢查询数 | >100/分钟 | 超过1秒的查询 |
| **缓冲池** | 命中率 | <95%警告 | InnoDB缓冲池 |
| **缓冲池** | 使用率 | >90%警告 | 缓冲池使用 |
| **复制** | 从库延迟 | >10s警告 | 主从同步延迟 |
| **事务** | 回滚率 | >1%警告 | 事务回滚比例 |
| **表锁** | 锁等待 | >50/分钟 | 表锁等待次数 |

### 2.4 Redis监控指标

| 指标分类 | 指标项 | 告警阈值 | 说明 |
|----------|--------|----------|------|
| **内存** | 使用率 | >80%警告 | 内存使用 |
| **内存** | 最大内存 | >90%严重 | 到达上限 |
| **持久化** | RDB状态 | Down警告 | 持久化失败 |
| **复制** | 主从延迟 | >1s警告 | 同步延迟 |
| **连接** | 客户端数 | >10000警告 | 客户端连接 |
| **命令** | 慢命令 | >100/分钟 | 超过10ms |
| **过期** | Key过期数 | >10000/秒 | 过期Key速率 |

### 2.5 RabbitMQ监控指标

| 指标分类 | 指标项 | 告警阈值 | 说明 |
|----------|--------|----------|------|
| **队列** | 消息堆积 | >10000警告 | 未消费消息 |
| **队列** | 消费者数量 | =0警告 | 无消费者 |
| **连接** | 连接数 | >8000警告 | TCP连接 |
| **内存** | 内存使用 | >80%警告 | RabbitMQ内存 |
| **磁盘** | 磁盘空间 | <1GB警告 | 磁盘可用 |
| **节点** | 节点健康 | Down严重 | 集群节点 |

### 2.6 应用监控指标

| 指标分类 | 指标项 | 告警阈值 | 说明 |
|----------|--------|----------|------|
| **JVM** | 堆使用率 | >80%警告 | GC压力 |
| **JVM** | GC频率 | >10次/分 | 频繁GC |
| **JVM** | FGC次数 | >3次/小时 | Full GC |
| **线程** | 活跃线程 | >200警告 | 线程池满 |
| **HTTP** | QPS | >1000警告 | 接口限流 |
| **HTTP** | 响应时间P99 | >500ms警告 | 慢请求 |
| **HTTP** | 错误率 | >1%警告 | 5xx比例 |
| **数据库** | 连接池 | >90%警告 | 连接池满 |
| **熔断** | 熔断次数 | >10/分钟 | 熔断触发 |

### 2.7 业务监控指标

| 指标项 | 告警阈值 | 说明 |
|--------|----------|------|
| 用户登录失败率 | >10%/分钟 | 暴力破解检测 |
| 审批超时数 | >100/小时 | 审批超时 |
| OCR识别失败率 | >5% | 识别异常 |
| AI接口错误率 | >2% | AI服务异常 |
| 消息发送失败数 | >50/分钟 | 聊天服务异常 |
| 文件上传失败率 | >3% | 存储服务异常 |

---

## 3. 告警规则配置

### 3.1 Prometheus告警规则

```yaml
# prometheus/alerts.yml
groups:
  - name: host_alerts
    rules:
      - alert: HostHighCpu
        expr: 100 - (avg by(instance)(irate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "主机CPU使用率过高"
          description: "{{ $labels.instance }} CPU使用率超过80%，当前: {{ $value }}%"

      - alert: HostHighMemory
        expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100 > 85
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "主机内存使用率过高"
          description: "{{ $labels.instance }} 内存使用率超过85%，当前: {{ $value }}%"

      - alert: HostHighDisk
        expr: node_filesystem_files_free{fstype!~"tmpfs|fuse.lxcfs"} / node_filesystem_files_total * 100 < 10
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "主机磁盘空间不足"
          description: "{{ $labels.instance }} {{ $labels.mountpoint }} 磁盘剩余空间不足10%"

  - name: mysql_alerts
    rules:
      - alert: MySQLHighConnections
        expr: mysql_global_status_threads_connected / mysql_global_variables_max_connections * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "MySQL连接数过高"
          description: "{{ $labels.instance }} 连接数超过80%，当前: {{ $value }}%"

      - alert: MySQLSlowQueries
        expr: rate(mysql_global_status_slow_queries[5m]) > 0.1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "MySQL慢查询过多"
          description: "{{ $labels.instance }} 慢查询超过10个/分钟"

  - name: redis_alerts
    rules:
      - alert: RedisHighMemory
        expr: redis_memory_used_bytes / redis_memory_max_bytes * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis内存使用率过高"
          description: "{{ $labels.instance }} 内存使用率超过80%"

  - name: rabbitmq_alerts
    rules:
      - alert: RabbitMQMessageBacklog
        expr: rabbitmq_queue_messages > 10000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "RabbitMQ消息堆积"
          description: "{{ $labels.instance }} 队列 {{ $labels.queue }} 消息堆积超过10000"

  - name: jvm_alerts
    rules:
      - alert: JVMHighHeapUsage
        expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100 > 80
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "JVM堆内存使用率过高"
          description: "{{ $labels.instance }} {{ $labels.pool }} 堆内存使用率超过80%"

      - alert: JVMFullGC
        expr: increase(jvm_gc_pause_seconds_count{action="end of major GC"}[1h]) > 3
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "JVM Full GC频繁"
          description: "{{ $labels.instance }} Full GC超过3次/小时"

  - name: http_alerts
    rules:
      - alert: HTTPHighLatency
        expr: histogram_quantile(0.99, sum(rate(http_server_requests_seconds_bucket[5m])) by (uri, le)) > 0.5
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "HTTP响应时间过长"
          description: "{{ $labels.uri }} P99响应时间超过500ms，当前: {{ $value }}s"

      - alert: HTTPHighErrorRate
        expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) * 100 > 1
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "HTTP错误率过高"
          description: "{{ $labels.uri }} 5xx错误率超过1%，当前: {{ $value }}%"
```

### 3.2 AlertManager配置

```yaml
# alertmanager/config.yml
global:
  resolve_timeout: 5m
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alert@aioa.com'
  smtp_auth_username: 'alert@aioa.com'
  smtp_auth_password: 'password'

route:
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  receiver: 'default'
  routes:
    - match:
        severity: critical
      receiver: 'critical'
      group_wait: 10s
    - match:
        severity: warning
      receiver: 'warning'

receivers:
  - name: 'default'
    email_configs:
      - to: 'ops@aioa.com'
        send_resolved: true

  - name: 'critical'
    email_configs:
      - to: 'ops-critical@aioa.com'
        send_resolved: true
    webhook_configs:
      - url: 'http://dingtalk-hook:5000dingtalk'
        send_resolved: true
    pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_KEY'

  - name: 'warning'
    email_configs:
      - to: 'ops@aioa.com'
        send_resolved: true
```

---

## 4. 告警通知渠道

### 4.1 通知渠道配置

| 渠道 | 用途 | 优先级 | 配置 |
|------|------|--------|------|
| 邮件 | 正式通知 | 低 | SMTP配置 |
| 钉钉 | 团队通知 | 中 | Webhook |
| 电话 | 紧急告警 | 高 | PagerDuty |
| 短信 | 最高优先 | 最高 | 阿里云短信 |

### 4.2 钉钉机器人配置

```bash
# 在钉钉群中添加自定义机器人
# 获取Webhook地址后配置

# webhook地址格式
https://oapi.dingtalk.com/robot/send?access_token=xxx

# 加签密钥配置
secret: SECxxxxxxxxxxxxxxxxxxxxx
```

### 4.3 告警分级

| 级别 | 定义 | 通知方式 | 响应时间 | 处理时限 |
|------|------|----------|----------|----------|
| P0 | 系统宕机 | 电话+短信+钉钉+邮件 | 5分钟 | 1小时 |
| P1 | 核心功能不可用 | 钉钉+邮件 | 15分钟 | 4小时 |
| P2 | 非核心功能异常 | 钉钉 | 1小时 | 24小时 |
| P3 | 轻微问题 | 邮件 | 4小时 | 72小时 |

---

## 5. 告警处理流程

### 5.1 处理流程

```
┌─────────────┐
│   告警触发   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  告警分级   │
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────┐
│  P0/P1 → 电话+短信+钉钉+邮件        │
│  P2     → 钉钉通知                  │
│  P3     → 邮件通知                  │
└──────┬──────────────────────────────┘
       │
       ▼
┌─────────────┐
│  值班人员响应 │
└──────┬──────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│  能解决？    │──否─▶│  升级处理    │
└──────┬──────┘     └──────┬──────┘
       │是                  │
       ▼                   ▼
┌─────────────┐     ┌─────────────┐
│  解决问题    │     │  专家团队   │
└──────┬──────┘     └──────┬──────┘
       │                   │
       ▼                   ▼
┌─────────────┐     ┌─────────────┐
│  告警恢复    │◀────│  解决后恢复  │
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────┐
│  事件记录   │
└─────────────┘
```

### 5.2 值班安排

| 班次 | 时间 | 值班人 |
|------|------|--------|
| 早班 | 08:00-16:00 | 运维A |
| 中班 | 16:00-24:00 | 运维B |
| 夜班 | 00:00-08:00 | 值班电话 |

### 5.3 升级机制

| 级别 | 第一响应 | 15分钟未解决 | 30分钟未解决 |
|------|----------|--------------|--------------|
| P0 | 值班工程师 | 运维经理 | 技术总监 |
| P1 | 值班工程师 | 运维经理 | - |
| P2 | 值班工程师 | - | - |

---

## 6. 监控大盘

### 6.1 大盘清单

| 大盘名称 | 用途 | 刷新频率 |
|----------|------|----------|
| 系统总览 | 高层视图 | 1分钟 |
| 应用监控 | Java应用 | 30秒 |
| 数据库监控 | MySQL | 15秒 |
| 缓存监控 | Redis | 15秒 |
| 消息队列 | RabbitMQ | 15秒 |
| 业务监控 | 业务指标 | 1分钟 |
| 告警统计 | 告警分析 | 5分钟 |

### 6.2 系统总览大盘

```
┌────────────────────────────────────────────────────────────────────┐
│  AI-OA 系统总览  |  时间: 2026-04-05 15:00:00  |  刷新: 1分钟     │
├────────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐│
│  │   系统健康   │ │   在线用户   │ │  API请求量  │ │  错误率   ││
│  │     98%      │ │     256     │ │   1,234/s    │ │   0.1%    ││
│  │    正常 ✅    │ │   活跃中    │ │   正常 ✅    │ │   正常 ✅  ││
│  └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘│
│                                                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                      API响应时间趋势                         │  │
│  │  500ms ──────────────────────────────────────────────────── │  │
│  │       ╱╲                                                     │  │
│  │  ────╱──╲─────────────╱╲──────╱╲──────╱╲──────╱╲────────── │  │
│  │       ╲╱              ╲╱──────╲╱──────╲╱──────╲╱           │  │
│  │  200ms                                                       │  │
│  │  ─────────────────────────────────────────────────────────  │  │
│  │  50ms                                                        │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────┐│
│  │    CPU 使用率    │ │   内存使用率    │ │     磁盘使用率      ││
│  │    ████████░░    │ │   ████████░░    │ │    ████████░░░░    ││
│  │       78%        │ │      72%        │ │       65%           ││
│  └─────────────────┘ └─────────────────┘ └─────────────────────┘│
│                                                                    │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────────┐│
│  │   MySQL 连接     │ │  Redis 内存    │ │   RabbitMQ 队列     ││
│  │   ████████░░    │ │   ██████████    │ │   ████░░░░░░░░░    ││
│  │     65%          │ │      85%        │ │       23%           ││
│  └─────────────────┘ └─────────────────┘ └─────────────────────┘│
└────────────────────────────────────────────────────────────────────┘
```

---

## 7. 容量规划

### 7.1 容量评估模型

```
容量需求 = 基线容量 × 峰值倍数 × 安全系数 × 增长系数
```

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| 基线容量 | 正常运行容量 | 实测值 |
| 峰值倍数 | 业务高峰倍数 | 3-5倍 |
| 安全系数 | 预留余量 | 1.2-1.5 |
| 增长系数 | 未来6个月增长 | 1.3-1.5 |

### 7.2 分层扩容阈值

| 层级 | 指标 | 扩容阈值 | 扩容步长 |
|------|------|----------|----------|
| 应用层 | CPU > 70% | 70% | +2节点 |
| 应用层 | 响应时间P99 > 300ms | 300ms | +2节点 |
| 数据层 | 连接数 > 70% | 70% | +1从库 |
| 缓存层 | 内存 > 70% | 70% | +1节点 |
| 队列层 | 消息堆积 > 5000 | 5000 | +1节点 |

### 7.3 扩容预估表

| 用户规模 | 并发用户 | 应用服务器 | MySQL | Redis | RabbitMQ |
|----------|----------|------------|-------|-------|----------|
| 100人 | 20 | 2 | 1主1从 | 2 | 2 |
| 500人 | 100 | 4 | 1主1从 | 3 | 3 |
| 1000人 | 200 | 6 | 1主2从 | 3 | 3 |
| 5000人 | 500 | 10 | 1主2从 | 4 | 4 |
| 10000人 | 1000 | 15 | 1主3从 | 6 | 6 |

### 7.4 性能基线

| 指标 | 目标值 | 测量方法 |
|------|--------|----------|
| API响应时间P50 | <100ms | Prometheus histogram |
| API响应时间P99 | <500ms | Prometheus histogram |
| 系统可用性 | >99.5% | uptime监控 |
| 首页加载时间 | <2s | Browser Timing |
| 数据库QPS | <5000 | performance_schema |

---

## 8. 灾备方案

### 8.1 灾备架构

```
┌─────────────────────────────────────────────────────────────┐
│                        主数据中心                            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                      │
│  │  应用1  │  │  应用2  │  │  应用3  │                      │
│  └────┬────┘  └────┬────┘  └────┬────┘                      │
│       │            │            │                           │
│  ┌────┴────────────┴────────────┴────┐                      │
│  │           MySQL 主库              │                      │
│  └───────────────────────────────────┘                      │
└────────────────────────┬────────────────────────────────────┘
                         │ 异步复制 (RPO < 5分钟)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                        备数据中心                            │
│  ┌───────────────────────────────────┐                      │
│  │           MySQL 从库              │                      │
│  └───────────────────────────────────┘                      │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐                      │
│  │  应用1  │  │  应用2  │  │  应用3  │  (热备实例)          │
│  └─────────┘  └─────────┘  └─────────┘                      │
└─────────────────────────────────────────────────────────────┘
```

### 8.2 灾备指标

| 指标 | 目标 | 说明 |
|------|------|------|
| RPO | <5分钟 | 数据恢复点目标 |
| RTO | <30分钟 | 系统恢复时间目标 |
| 可用性 | 99.5% | 年度可用性 |

### 8.3 数据备份策略

| 备份类型 | 周期 | 时间 | 保留 | 存储位置 |
|----------|------|------|------|----------|
| 全量备份 | 每天 | 02:00 | 30天 | 本地+OSS |
| 增量备份 | 每6小时 | 08/14/20点 | 7天 | 本地 |
| 实时备份 | 实时 | - | - | 备库 |
| Binlog | 实时 | - | 7天 | 本地+OSS |
| 配置文件 | 变更时 | - | 90天 | 本地 |

### 8.4 故障切换流程

```bash
# 1. 故障检测
# - 自动检测：MySQL主库连接失败
# - 人工确认：检查是否真的故障

# 2. 切换决策
# - 评估故障影响
# - 决定是否切换

# 3. 执行切换
# - 停止主库写入
# - 等待从库同步完成
# - 提升从库为主库
# - 更新DNS/VIP

# 4. 验证
# - 检查应用连接
# - 检查数据完整性
# - 监控系统状态

# 5. 通知
# - 发送故障通知
# - 记录故障时间线
```

### 8.5 定期演练

| 演练项目 | 周期 | 执行时间 |
|----------|------|----------|
| 数据恢复演练 | 每季度 | 周末 |
| 故障切换演练 | 每半年 | 维护窗口 |
| 灾备切换演练 | 每年 | 年度维护日 |

---

## 附录A：监控组件安装

### A.1 node_exporter安装

```bash
# 下载
wget https://github.com/prometheus/node_exporter/releases/download/v1.7.0/node_exporter-1.7.0.linux-amd64.tar.gz
tar -xzf node_exporter-1.7.0.linux-amd64.tar.gz

# 启动
nohup ./node_exporter --web.listen-address=":9100" > node_exporter.log 2>&1 &

# 验证
curl http://localhost:9100/metrics
```

### A.2 MySQL exporter安装

```bash
# 安装
yum install -y mysqld_exporter

# 配置
cat > /etc/mysql_exporter.cnf << EOF
[client]
host=localhost
user=exporter
password=ExporterPass123!
EOF

# 启动
nohup ./mysqld_exporter --config.my-cnf=/etc/mysql_exporter.cnf > mysqld_exporter.log 2>&1 &
```

---

*文档版本：V1.0*
*更新日期：2026-04-05*
