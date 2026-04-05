# AI-OA Kubernetes部署包

## 包含内容

```
k8s/
├── namespace.yaml            # 命名空间定义
├── secrets/                 # 密钥配置
│   ├── mysql-secret.yaml
│   ├── redis-secret.yaml
│   ├── minio-secret.yaml
│   └── rabbitmq-secret.yaml
├── configmap/              # 配置字典
│   ├── mysql-config.yaml
│   ├── redis-config.yaml
│   └── app-config.yaml
├── mysql/                  # MySQL部署
│   ├── statefulset.yaml
│   ├── service.yaml
│   └── pvc.yaml
├── redis/                  # Redis部署
│   ├── statefulset.yaml
│   ├── service.yaml
│   └── pvc.yaml
├── rabbitmq/               # RabbitMQ部署
│   ├── statefulset.yaml
│   └── service.yaml
├── minio/                  # MinIO部署
│   ├── statefulset.yaml
│   └── service.yaml
├── aioa/                   # AI-OA应用
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── hpa.yaml
│   └── ingress.yaml
├── n8n/                    # n8n工作流
│   ├── deployment.yaml
│   └── service.yaml
├── ingress/                # Ingress配置
│   ├── nginx-ingress.yaml
│   └── aioa-ingress.yaml
├── monitoring/             # 监控配置
│   ├── prometheus.yaml
│   └── grafana.yaml
├── scripts/
│   ├── deploy.sh            # 一键部署
│   ├── uninstall.sh         # 卸载
│   └── backup.sh           # 备份
├── helm/                    # Helm Charts
│   └── aioa/
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
└── README.md
```

## 快速部署

### 方式一：kubectl部署

```bash
# 1. 解压
tar -xzvf AI-OA-k8s-v1.0.tar.gz
cd AI-OA-k8s-v1.0

# 2. 修改密码
vim config/secret.yaml

# 3. 一键部署
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 4. 查看状态
kubectl get pods -n aioa
kubectl get svc -n aioa
```

### 方式二：Helm部署

```bash
# 1. 解压
tar -xzvf AI-OA-k8s-v1.0.tar.gz
cd AI-OA-k8s-v1.0

# 2. 安装Helm仓库
helm repo add aioa ./helm/aioa
helm repo update

# 3. 安装
helm install aioa aioa/aioa -n aioa --create-namespace

# 4. 升级
helm upgrade aioa aioa/aioa -n aioa

# 5. 卸载
helm uninstall aioa -n aioa
```

## Kubernetes版本要求

- Kubernetes: 1.24+
- Helm: 3.10+
- 推荐: 1.27+

## 节点规划

| 节点类型 | 数量 | CPU | 内存 | 磁盘 |
|----------|------|-----|------|------|
| Master | 3 | 8核 | 16GB | 100GB SSD |
| Worker | 6-10 | 16核 | 32GB | 200GB SSD |
| Storage | 4 | 8核 | 16GB | 2TB HDD |

## Pod资源规划

| Pod | 副本 | CPU Request | Memory Request |
|-----|------|-------------|----------------|
| mysql | 1 | 500m | 2Gi |
| redis | 1 | 200m | 512Mi |
| rabbitmq | 1 | 200m | 512Mi |
| minio | 1 | 200m | 512Mi |
| aioa | 2-10 | 500m | 1Gi |

## 服务访问

| 服务 | ClusterIP | NodePort | Ingress |
|------|-----------|----------|---------|
| aioa | Yes | 8080 | aioa.example.com |
| minio | Yes | 9000, 9001 | minio.example.com |
| rabbitmq | Yes | 5672, 15672 | rabbitmq.example.com |
| n8n | Yes | 5678 | n8n.example.com |

## 管理命令

```bash
# 查看所有资源
kubectl get all -n aioa

# 查看Pod日志
kubectl logs -f deployment/aioa -n aioa

# 进入容器
kubectl exec -it deployment/aioa -n aioa -- /bin/bash

# 扩缩容
kubectl scale deployment/aioa --replicas=5 -n aioa

# 更新配置
kubectl apply -f configmap/app-config.yaml -n aioa

# 卸载
./scripts/uninstall.sh
```

## HPA自动扩缩容

```bash
# 查看HPA
kubectl get hpa -n aioa

# 手动扩缩容
kubectl autoscale deployment/aioa -n aioa --min=2 --max=10 --cpu-percent=70
```

---

版本：1.0.0
更新日期：2026-04-05
