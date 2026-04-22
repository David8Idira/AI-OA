#!/bin/bash

# AI-OA 部署监控系统安装脚本
# 毛泽东思想指导：实事求是，全面监控

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_step "检查系统依赖..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    # 检查系统资源
    local mem_kb=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    local mem_gb=$((mem_kb / 1024 / 1024))
    
    if [ $mem_gb -lt 8 ]; then
        log_warn "系统内存可能不足（当前: ${mem_gb}GB，建议: 8GB+）"
    fi
    
    # 检查磁盘空间
    local disk_gb=$(df -BG . | awk 'NR==2 {print $4}' | sed 's/G//')
    if [ $disk_gb -lt 20 ]; then
        log_warn "磁盘空间可能不足（当前: ${disk_gb}GB，建议: 20GB+）"
    fi
    
    log_info "依赖检查通过"
}

# 创建目录结构
create_directories() {
    log_step "创建监控系统目录结构..."
    
    mkdir -p data/prometheus
    mkdir -p data/grafana
    mkdir -p data/alertmanager
    mkdir -p data/loki
    mkdir -p data/promtail
    mkdir -p config/prometheus
    mkdir -p config/grafana
    mkdir -p config/alertmanager
    mkdir -p config/loki
    mkdir -p dashboards
    mkdir -p alerts
    mkdir -p logs
    
    log_info "目录创建完成"
}

# 设置权限
set_permissions() {
    log_step "设置目录权限..."
    
    chmod 755 data config dashboards alerts logs
    chmod 777 data/prometheus data/grafana data/alertmanager data/loki data/promtail
    
    log_info "权限设置完成"
}

# 创建配置文件
create_config_files() {
    log_step "创建监控配置文件..."
    
    # Prometheus配置
    cat > config/prometheus/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  scrape_timeout: 10s

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

rule_files:
  - "/etc/prometheus/rules/*.yml"

scrape_configs:
  # 监控Prometheus自身
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # 监控Docker容器
  - job_name: 'docker'
    static_configs:
      - targets: ['docker-exporter:9323']

  # 监控AI-OA知识库服务
  - job_name: 'ai-oa-knowledge'
    metrics_path: '/api/knowledge/monitor/metrics/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'ai-oa-knowledge'

  # 监控节点（系统级）
  - job_name: 'node'
    static_configs:
      - targets: ['node-exporter:9100']

  # 监控cAdvisor（容器资源）
  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']

  # 监控Redis
  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  # 监控MySQL
  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']

  # 监控Nginx
  - job_name: 'nginx'
    static_configs:
      - targets: ['nginx-exporter:9113']
EOF

    # Alertmanager配置
    cat > config/alertmanager/alertmanager.yml << 'EOF'
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'alertmanager@ai-oa.com'
  smtp_auth_username: 'your-email@gmail.com'
  smtp_auth_password: 'your-password'
  smtp_require_tls: true

route:
  group_by: ['alertname', 'cluster', 'service']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 1h
  receiver: 'team-email'

receivers:
  - name: 'team-email'
    email_configs:
      - to: 'devops@ai-oa.com'
        send_resolved: true
    webhook_configs:
      - url: 'http://webhook-server:5000/webhook'
        send_resolved: true

inhibit_rules:
  - source_match:
      severity: 'critical'
    target_match:
      severity: 'warning'
    equal: ['alertname', 'cluster', 'service']
EOF

    # Loki配置
    cat > config/loki/loki.yml << 'EOF'
auth_enabled: false

server:
  http_listen_port: 3100
  grpc_listen_port: 9096

common:
  path_prefix: /tmp/loki
  storage:
    filesystem:
      chunks_directory: /tmp/loki/chunks
      rules_directory: /tmp/loki/rules
  replication_factor: 1
  ring:
    instance_addr: 127.0.0.1
    kvstore:
      store: inmemory

schema_config:
  configs:
    - from: 2020-10-24
      store: boltdb-shipper
      object_store: filesystem
      schema: v11
      index:
        prefix: index_
        period: 24h

ruler:
  alertmanager_url: http://alertmanager:9093
EOF

    # Promtail配置
    cat > config/promtail/promtail.yml << 'EOF'
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: system
    static_configs:
      - targets:
          - localhost
        labels:
          job: varlogs
          __path__: /var/log/*log

  - job_name: ai-oa
    static_configs:
      - targets:
          - localhost
        labels:
          job: ai-oa-logs
          __path__: /logs/ai-oa-*.log
EOF

    # 告警规则
    cat > alerts/ai-oa-alerts.yml << 'EOF'
groups:
  - name: ai-oa-alerts
    rules:
      # 服务不可用告警
      - alert: AIOAKnowledgeServiceDown
        expr: up{job="ai-oa-knowledge"} == 0
        for: 1m
        labels:
          severity: critical
          service: knowledge
        annotations:
          summary: "AI-OA知识库服务不可用"
          description: "知识库服务已经宕机超过1分钟"

      # 高错误率告警
      - alert: HighErrorRate
        expr: rate(knowledge_errors_total[5m]) > 0.1
        for: 2m
        labels:
          severity: warning
          service: knowledge
        annotations:
          summary: "知识库错误率过高"
          description: "过去5分钟错误率超过10%"

      # 搜索延迟告警
      - alert: HighSearchLatency
        expr: histogram_quantile(0.95, rate(knowledge_search_duration_seconds_bucket[5m])) > 2
        for: 5m
        labels:
          severity: warning
          service: knowledge
        annotations:
          summary: "知识库搜索延迟过高"
          description: "95%的搜索请求延迟超过2秒"

      # 内存使用告警
      - alert: HighMemoryUsage
        expr: (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes > 0.8
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "内存使用率过高"
          description: "内存使用率超过80%"

      # CPU使用告警
      - alert: HighCPUUsage
        expr: 100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "CPU使用率过高"
          description: "CPU使用率超过80%"

      # 磁盘空间告警
      - alert: LowDiskSpace
        expr: (node_filesystem_avail_bytes{mountpoint="/"} / node_filesystem_size_bytes{mountpoint="/"}) < 0.2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "磁盘空间不足"
          description: "根分区可用空间低于20%"

      # 缓存命中率告警
      - alert: LowCacheHitRate
        expr: knowledge_cache_hit_rate < 0.7
        for: 10m
        labels:
          severity: warning
          service: knowledge
        annotations:
          summary: "缓存命中率过低"
          description: "知识库缓存命中率低于70%"

      # Milvus健康告警
      - alert: MilvusUnhealthy
        expr: milvus_health_status == 0
        for: 2m
        labels:
          severity: critical
          service: milvus
        annotations:
          summary: "Milvus服务异常"
          description: "Milvus向量数据库服务异常"

      # 高并发告警
      - alert: HighConcurrentSearches
        expr: knowledge_search_active > 100
        for: 2m
        labels:
          severity: warning
          service: knowledge
        annotations:
          summary: "高并发搜索"
          description: "当前并发搜索数超过100"
EOF

    # Grafana数据源配置
    cat > config/grafana/datasources.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    editable: true
EOF

    # Grafana仪表盘配置
    cat > dashboards/knowledge-dashboard.json << 'EOF'
{
  "dashboard": {
    "title": "AI-OA知识库监控",
    "tags": ["ai-oa", "knowledge", "monitoring"],
    "timezone": "browser",
    "panels": [
      {
        "title": "服务状态",
        "type": "stat",
        "targets": [{"expr": "up{job=\"ai-oa-knowledge\"}", "legendFormat": "知识库服务"}],
        "gridPos": {"h": 4, "w": 6, "x": 0, "y": 0}
      },
      {
        "title": "搜索请求率",
        "type": "graph",
        "targets": [{"expr": "rate(knowledge_search_total[5m])", "legendFormat": "搜索请求率"}],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 4}
      },
      {
        "title": "搜索延迟分布",
        "type": "graph",
        "targets": [
          {"expr": "histogram_quantile(0.5, rate(knowledge_search_duration_seconds_bucket[5m]))", "legendFormat": "p50"},
          {"expr": "histogram_quantile(0.95, rate(knowledge_search_duration_seconds_bucket[5m]))", "legendFormat": "p95"},
          {"expr": "histogram_quantile(0.99, rate(knowledge_search_duration_seconds_bucket[5m]))", "legendFormat": "p99"}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 4}
      },
      {
        "title": "缓存命中率",
        "type": "gauge",
        "targets": [{"expr": "knowledge_cache_hit_rate * 100", "legendFormat": "命中率"}],
        "gridPos": {"h": 6, "w": 6, "x": 18, "y": 0}
      },
      {
        "title": "活跃文档数",
        "type": "stat",
        "targets": [{"expr": "knowledge_docs_active", "legendFormat": "活跃文档"}],
        "gridPos": {"h": 4, "w": 6, "x": 6, "y": 0}
      },
      {
        "title": "错误率",
        "type": "graph",
        "targets": [{"expr": "rate(knowledge_errors_total[5m])", "legendFormat": "错误率"}],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 12}
      },
      {
        "title": "向量操作统计",
        "type": "table",
        "targets": [{"expr": "knowledge_vectors_stored", "legendFormat": "向量存储数"}],
        "gridPos": {"h": 8, "w": 12, "x": 12, "y": 12}
      },
      {
        "title": "系统资源使用",
        "type": "graph",
        "targets": [
          {"expr": "100 - (avg by(instance) (rate(node_cpu_seconds_total{mode=\"idle\"}[5m])) * 100)", "legendFormat": "CPU使用率"},
          {"expr": "(node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100", "legendFormat": "内存使用率"}
        ],
        "gridPos": {"h": 8, "w": 12, "x": 0, "y": 20}
      }
    ],
    "time": {"from": "now-1h", "to": "now"}
  }
}
EOF

    log_info "配置文件创建完成"
}

# 创建Docker Compose文件
create_docker_compose() {
    log_step "创建Docker Compose配置..."
    
    cat > docker-compose.yml << 'EOF'
version: '3.8'

networks:
  monitoring:
    driver: bridge

volumes:
  prometheus_data: {}
  grafana_data: {}
  loki_data: {}
  alertmanager_data: {}

services:
  # Prometheus - 指标收集
  prometheus:
    image: prom/prometheus:v2.45.0
    container_name: prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=30d'
      - '--web.enable-lifecycle'
    volumes:
      - ./config/prometheus:/etc/prometheus
      - prometheus_data:/prometheus
      - ./alerts:/etc/prometheus/rules
    networks:
      - monitoring

  # Grafana - 数据可视化
  grafana:
    image: grafana/grafana:10.0.0
    container_name: grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_INSTALL_PLUGINS=grafana-piechart-panel
    volumes:
      - grafana_data:/var/lib/grafana
      - ./config/grafana:/etc/grafana/provisioning
      - ./dashboards:/var/lib/grafana/dashboards
    networks:
      - monitoring

  # Alertmanager - 告警管理
  alertmanager:
    image: prom/alertmanager:v0.25.0
    container_name: alertmanager
    restart: unless-stopped
    ports:
      - "9093:9093"
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
      - '--storage.path=/alertmanager'
    volumes:
      - ./config/alertmanager:/etc/alertmanager
      - alertmanager_data:/alertmanager
    networks:
      - monitoring

  # Loki - 日志收集
  loki:
    image: grafana/loki:2.8.2
    container_name: loki
    restart: unless-stopped
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/loki.yml
    volumes:
      - ./config/loki:/etc/loki
      - loki_data:/tmp/loki
    networks:
      - monitoring

  # Promtail - 日志采集器
  promtail:
    image: grafana/promtail:2.8.2
    container_name: promtail
    restart: unless-stopped
    volumes:
      - ./config/promtail:/etc/promtail
      - /var/log:/var/log
      - ./logs:/logs
    command: -config.file=/etc/promtail/promtail.yml
    networks:
      - monitoring

  # Node Exporter - 系统指标
  node-exporter:
    image: prom/node-exporter:v1.6.0
    container_name: node-exporter
    restart: unless-stopped
    ports:
      - "9100:9100"
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    command:
      - '--path.procfs=/host/proc'
      - '--path.rootfs=/rootfs'
      - '--path.sysfs=/host/sys'
      - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'
    networks:
      - monitoring

  # cAdvisor - 容器监控
  cadvisor:
    image: gcr.io/cadvisor/cadvisor:v0.47.2
    container_name: cadvisor
    restart: unless-stopped
    ports:
      - "8080:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:ro
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
    privileged: true
    devices:
      - /dev/kmsg
    networks:
      - monitoring

  # Redis Exporter
  redis-exporter:
    image: oliver006/redis_exporter:v1.52.0
    container_name: redis-exporter
    restart: unless-stopped
    ports:
      - "9121:9121"
    environment:
      - REDIS_ADDR=redis://redis:6379
    networks:
      - monitoring

  # MySQL Exporter
  mysql-exporter:
    image: prom/mysqld-exporter:v0.14.0
    container_name: mysql-exporter
    restart: unless-stopped
    ports:
      - "9104:9104"
    environment:
      - DATA_SOURCE_NAME=exporter:password@(mysql:3306)/
    networks:
      - monitoring

  # Nginx Exporter
  nginx-exporter:
    image: nginx/nginx-prometheus-exporter:0.11.0
    container_name: nginx-exporter
    restart: unless-stopped
    ports:
      - "9113:9113"
    command:
      - '-nginx.scrape-uri=http://nginx:80/status'
    networks:
      - monitoring

  # Docker Exporter
  docker-exporter:
    image: prometheuscommunity/docker-exporter:v0.1.0
    container_name: docker-exporter
    restart: unless-stopped
    ports:
      - "9323:9323"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    networks:
      - monitoring
EOF

    log_info "Docker Compose配置创建完成"
}

# 创建启动脚本
create_startup_scripts() {
    log_step "创建管理脚本..."
    
    # 启动脚本
    cat > start-monitoring.sh << 'EOF'
#!/bin/bash
# 启动监控系统

echo "启动AI-OA监控系统..."
echo "========================"

cd "$(dirname "$0")"

# 启动服务
docker-compose up -d

echo "等待服务启动..."
sleep 10

# 检查服务状态
echo "服务状态："
docker-compose ps

echo ""
echo "监控系统访问地址："
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana:    http://localhost:3000 (admin/admin)"
echo "  - Alertmanager: http://localhost:9093"
echo "  - Loki:       http://localhost:3100"
echo ""
echo "AI-OA知识库监控仪表板："
echo "  http://localhost:3000/dashboards"
EOF

    # 停止脚本
    cat > stop-monitoring.sh << 'EOF'
#!/bin/bash
# 停止监控系统

echo "停止AI-OA监控系统..."
cd "$(dirname "$0")"
docker-compose down
echo "监控系统已停止"
EOF

    # 重启脚本
    cat > restart-monitoring.sh << 'EOF'
#!/bin/bash
# 重启监控系统

echo "重启AI-OA监控系统..."
cd "$(dirname "$0")"
docker-compose restart
echo "监控系统已重启"
EOF

    # 状态检查脚本
    cat > status-monitoring.sh << 'EOF'
#!/bin/bash
# 检查监控系统状态

echo "AI-OA监控系统状态"
echo "========================"
cd "$(dirname "$0")"

echo "容器状态："
docker-compose ps

echo ""
echo "服务健康检查："
echo "1. Prometheus:"
curl -s http://localhost:9090/-/healthy || echo "  ✗ 不可用"
echo "2. Grafana:"
curl -s http://localhost:3000/api/health || echo "  ✗ 不可用"
echo "3. Alertmanager:"
curl -s http://localhost:9093/-/healthy || echo "  ✗ 不可用"
echo "4. Loki:"
curl -s http://localhost:3100/ready || echo "  ✗ 不可用"

echo ""
echo "监控指标："
echo "  文档数量：运行以下命令查看"
echo "  curl http://localhost:9090/api/v1/query?query=knowledge_docs_active"
EOF

    # 备份脚本
    cat > backup-monitoring.sh << 'EOF'
#!/bin/bash
# 备份监控数据

cd "$(dirname "$0")"
BACKUP_DIR="backup/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

echo "开始备份监控数据..."
echo "备份目录: $BACKUP_DIR"

# 备份配置文件
cp -r config "$BACKUP_DIR/"
cp -r alerts "$BACKUP_DIR/"
cp -r dashboards "$BACKUP_DIR/"
cp docker-compose.yml "$BACKUP_DIR/"
cp *.sh "$BACKUP_DIR/"

echo "备份完成！"
echo "备份文件保存在: $BACKUP_DIR"
EOF

    # 设置执行权限
    chmod +x *.sh
    
    log_info "管理脚本创建完成"
}

# 创建集成指南
create_integration_guide() {
    log_step "创建集成指南..."
    
    cat > INTEGRATION.md << 'EOF'
# AI-OA监控系统集成指南

## 概述
本文档介绍如何将AI-OA知识库服务与监控系统集成。

## 监控指标

### 知识库服务指标
AI-OA知识库服务提供以下Prometheus指标：

1. **知识库文档指标**
   - `knowledge_docs_active`: 活跃文档数
   - `knowledge_docs_create_total`: 文档创建总数
   - `knowledge_docs_update_total`: 文档更新总数
   - `knowledge_docs_delete_total`: 文档删除总数
   - `knowledge_docs_view_total`: 文档查看总数

2. **搜索指标**
   - `knowledge_search_total`: 关键词搜索总数
   - `knowledge_search_semantic_total`: 语义搜索总数
   - `knowledge_search_duration_seconds`: 搜索延迟分布
   - `knowledge_search_active`: 活跃搜索数

3. **向量操作指标**
   - `knowledge_vector_store_total`: 向量存储总数
   - `knowledge_vector_search_total`: 向量搜索总数
   - `knowledge_vector_store_duration_seconds`: 向量存储延迟
   - `knowledge_vector_search_duration_seconds`: 向量搜索延迟

4. **缓存指标**
   - `knowledge_cache_hit_rate`: 缓存命中率

5. **错误指标**
   - `knowledge_errors_total`: 错误总数
   - `knowledge_errors_by_type`: 按类型统计的错误

6. **批处理指标**
   - `knowledge_batch_create_total`: 批量创建总数
   - `knowledge_batch_update_total`: 批量更新总数
   - `knowledge_batch_delete_total`: 批量删除总数
   - `knowledge_batch_operation_duration_seconds`: 批处理延迟

7. **RAG指标**
   - `knowledge_rag_retrieve_total`: RAG检索总数
   - `knowledge_rag_retrieve_duration_seconds`: RAG检索延迟

### 访问监控指标
知识库服务的监控指标可通过以下端点访问：
```
GET /api/knowledge/monitor/metrics/prometheus
```

## 配置AI-OA服务监控

### 1. 在Prometheus中添加配置
编辑 `config/prometheus/prometheus.yml`，添加以下配置：

```yaml
scrape_configs:
  - job_name: 'ai-oa-knowledge'
    metrics_path: '/api/knowledge/monitor/metrics/prometheus'
    static_configs:
      - targets: ['your-ai-oa-host:8080']
        labels:
          service: 'ai-oa-knowledge'
          environment: 'production'
```

### 2. 配置Grafana数据源
1. 登录Grafana (http://localhost:3000，默认账号：admin/admin)
2. 进入 Configuration -> Data Sources
3. 点击 Add data source
4. 选择 Prometheus
5. 设置 URL: http://prometheus:9090
6. 点击 Save & Test

### 3. 导入仪表板
1. 在Grafana中，点击 + -> Import
2. 上传 `dashboards/knowledge-dashboard.json`
3. 选择 Prometheus 数据源
4. 点击 Import

## 告警配置

### 1. 告警规则
编辑 `alerts/ai-oa-alerts.yml` 配置告警规则：

```yaml
groups:
  - name: ai-oa-alerts
    rules:
      - alert: AIOAKnowledgeServiceDown
        expr: up{job="ai-oa-knowledge"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "知识库服务不可用"
          description: "服务已经宕机超过1分钟"
```

### 2. 告警接收器
编辑 `config/alertmanager/alertmanager.yml` 配置告警接收方式：

```yaml
receivers:
  - name: 'email-alerts'
    email_configs:
      - to: 'devops@your-company.com'
        from: 'alertmanager@ai-oa.com'
        smarthost: 'smtp.gmail.com:587'
        auth_username: 'your-email@gmail.com'
        auth_password: 'your-password'
        send_resolved: true
```

## 日志监控

### 1. 配置日志收集
AI-OA服务应输出结构化日志到指定位置：

```java
// Spring Boot配置
logging:
  file:
    name: /logs/ai-oa-knowledge.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. 在Promtail中添加配置
编辑 `config/promtail/promtail.yml`：

```yaml
scrape_configs:
  - job_name: ai-oa-logs
    static_configs:
      - targets:
          - localhost
        labels:
          job: ai-oa
          __path__: /logs/ai-oa-*.log
```

## 性能优化建议

### 1. 监控数据保留策略
```yaml
# Prometheus配置
storage:
  tsdb:
    retention:
      time: 30d  # 保留30天数据
```

### 2. 采样频率
```yaml
global:
  scrape_interval: 15s  # 每15秒采集一次
  evaluation_interval: 15s  # 每15秒评估一次告警规则
```

### 3. 资源限制
```yaml
# Docker Compose配置
services:
  prometheus:
    deploy:
      resources:
        limits:
          memory: 4G
        reservations:
          memory: 2G
```

## 故障排除

### 1. 监控指标不可用
检查项：
- AI-OA服务是否运行
- 监控端点是否可访问：`curl http://your-host:8080/api/knowledge/monitor/metrics/prometheus`
- Prometheus配置是否正确
- 防火墙是否开放端口

### 2. Grafana无数据
检查项：
- Grafana数据源配置
- 时间范围设置
- Prometheus查询语法

### 3. 告警不触发
检查项：
- 告警规则配置
- Alertmanager配置
- 网络连接

### 4. 日志无法收集
检查项：
- 日志文件路径
- Promtail配置
- Loki服务状态

## 扩展监控

### 1. 业务指标监控
除了系统指标，还可以监控业务指标：
- 用户活跃度
- 文档增长趋势
- 搜索热门关键词
- 向量检索准确率

### 2. 自定义仪表板
根据业务需求创建自定义仪表板：
1. 在Grafana中创建新仪表板
2. 添加业务指标图表
3. 设置告警阈值
4. 导出为JSON文件

### 3. 集成第三方监控
可集成以下第三方监控：
- 应用性能监控（APM）
- 用户行为分析
- 安全监控
- 成本监控

## 最佳实践

1. **分级监控**：区分基础设施监控和应用监控
2. **告警分级**：根据严重程度设置不同告警级别
3. **自动化响应**：配置自动化修复脚本
4. **定期审查**：定期审查监控配置和告警规则
5. **容量规划**：基于监控数据进行容量规划

## 支持
如有问题，请联系：
- 邮箱：support@ai-oa.com
- 文档：https://docs.ai-oa.com/monitoring
```

    log_info "集成指南创建完成"
}

# 主安装流程
main() {
    echo ""
    echo "=========================================="
    echo "    AI-OA部署监控系统安装程序"
    echo "=========================================="
    
    # 检查依赖
    check_dependencies
    
    # 创建目录
    create_directories
    
    # 设置权限
    set_permissions
    
    # 创建配置文件
    create_config_files
    
    # 创建Docker Compose
    create_docker_compose
    
    # 创建启动脚本
    create_startup_scripts
    
    # 创建集成指南
    create_integration_guide
    
    echo ""
    echo "=========================================="
    echo "     监控系统安装完成！"
    echo "=========================================="
    echo ""
    echo "下一步："
    echo "1. 启动监控系统：./start-monitoring.sh"
    echo "2. 访问Grafana：http://localhost:3000 (admin/admin)"
    echo "3. 导入仪表板：上传 dashboards/knowledge-dashboard.json"
    echo "4. 配置AI-OA服务监控"
    echo ""
    echo "详细集成指南请查看 INTEGRATION.md"
    echo "=========================================="
}

# 执行主函数
main "$@"