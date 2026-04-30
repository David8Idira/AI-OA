# AI-OA Kubernetes 部署说明

## 目录结构

```
k8s/
├── base/                    # 基础资源配置
│   ├── namespace.yaml       # 命名空间
│   ├── configmap.yaml       # 全局配置（环境变量）
│   ├── secret.yaml          # 敏感信息（密码、密钥）
│   └── mysql.yaml           # MySQL & Redis 有状态部署
├── services/                # 后端微服务
│   ├── gateway.yaml         # API 网关 (8080)
│   ├── system.yaml          # 系统服务 (8081)
│   ├── workflow.yaml        # 工作流服务 (8082)
│   ├── knowledge.yaml       # 知识库服务 (8083)
│   ├── ai.yaml              # AI 服务 (8084)
│   ├── asset.yaml           # 资产管理 (8085)
│   ├── attendance.yaml      # 考勤服务 (8086)
│   ├── hr.yaml              # 人力资源 (8087)
│   ├── license.yaml         # 许可证服务 (8088)
│   ├── ocr.yaml             # OCR 服务 (8089)
│   ├── reimburse.yaml       # 报销服务 (8090)
│   ├── report.yaml          # 报表服务 (8091)
│   └── im.yaml              # 即时通讯 (8092)
├── frontend/
│   └── deployment.yaml      # 前端 Vue SPA
├── ingress/
│   └── ingress.yaml          # Nginx Ingress
└── scripts/
    └── deploy.sh             # 一键部署脚本
```

## 快速部署

### 前置条件
- Kubernetes 1.19+
- kubectl 已配置集群
- Ingress Controller (如 nginx-ingress)

### 部署步骤

```bash
# 1. 进入 k8s 目录
cd /root/workspace/AI-OA/packages/k8s

# 2. 给脚本添加执行权限
chmod +x scripts/deploy.sh

# 3. 执行部署
./scripts/deploy.sh
```

### 手动部署

```bash
# 应用命名空间和基础资源
kubectl apply -f base/namespace.yaml
kubectl apply -f base/configmap.yaml
kubectl apply -f base/secret.yaml
kubectl apply -f base/mysql.yaml

# 等待 MySQL 就绪
kubectl wait --for=condition=available deployment/mysql -n ai-oa --timeout=300s

# 部署所有后端服务
for svc in gateway system workflow knowledge ai asset attendance hr license ocr reimburse report im; do
  kubectl apply -f services/$svc.yaml
done

# 部署前端
kubectl apply -f frontend/deployment.yaml

# 部署 Ingress
kubectl apply -f ingress/ingress.yaml
```

## 服务端口对照表

| 服务 | 端口 | HPA 范围 | CPU 请求 | 内存请求 |
|------|------|----------|----------|----------|
| gateway | 8080 | 2-10 | 200m | 512Mi |
| system | 8081 | 2-10 | 200m | 512Mi |
| workflow | 8082 | 2-10 | 200m | 512Mi |
| knowledge | 8083 | 2-10 | 200m | 512Mi |
| ai | 8084 | 2-10 | 300m | 1Gi |
| asset | 8085 | 2-10 | 200m | 512Mi |
| attendance | 8086 | 2-10 | 200m | 512Mi |
| hr | 8087 | 2-10 | 200m | 512Mi |
| license | 8088 | 2-10 | 200m | 512Mi |
| ocr | 8089 | 2-10 | 300m | 1Gi |
| reimburse | 8090 | 2-10 | 200m | 512Mi |
| report | 8091 | 2-10 | 200m | 512Mi |
| im | 8092 | 2-10 | 200m | 512Mi |
| frontend | 80 | 2-10 | 100m | 128Mi |
| mysql | 3306 | - | 250m | 512Mi |
| redis | 6379 | - | 100m | 128Mi |

## 常见操作

### 查看 Pod 状态
```bash
kubectl get pods -n ai-oa -w
```

### 查看日志
```bash
kubectl logs -n ai-oa -l app=aioa-gateway -f
```

### 进入 Pod
```bash
kubectl exec -it -n ai-oa deployment/aioa-gateway -- /bin/sh
```

### 扩缩容
```bash
kubectl scale deployment aioa-gateway -n ai-oa --replicas=5
```

### 更新镜像
```bash
kubectl set image deployment/aioa-gateway gateway=aioa-gateway:v2 -n ai-oa
```

### 删除所有资源
```bash
kubectl delete -f k8s/ --namespace=ai-oa
```

## 配置说明

### ConfigMap (configmap.yaml)
全局环境变量配置，包含数据库连接、Redis 配置等。

### Secret (secret.yaml)
敏感信息存储：
- `DB_PASSWORD`: 数据库密码
- `REDIS_PASSWORD`: Redis 密码
- `JWT_SECRET`: JWT 签名密钥
- `MIMI_API_KEY`, `OPENAI_API_KEY`, `CLAUDE_API_KEY`: AI 服务密钥

**生产环境请务必修改 secret.yaml 中的默认值！**

### HPA 配置
所有服务默认配置：
- 最小副本: 2
- 最大副本: 10
- CPU 目标利用率: 70%
- 内存目标利用率: 80%

### 健康检查
- **livenessProbe**: 启动 90s 后开始检测，检测失败 3 次重启容器
- **readinessProbe**: 启动 30s 后开始检测，检测失败 3 次停止流量

## Ingress 配置

部署后可通过 `ai-oa.example.com` 访问：
- `/` → 前端服务
- `/api` → API 网关

**请修改 ingress.yaml 中的 host 配置为实际域名。**