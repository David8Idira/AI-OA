# AI-OA Kubernetes 部署配置

## 目录结构

```
k8s/
├── namespace.yaml         # 命名空间
├── secret.yaml            # 密钥配置
├── configmap.yaml         # 配置
├── deployment-api.yaml    # API 微服务
├── deployment-web.yaml    # 前端
├── deployment-ai.yaml     # AI 服务
├── deployment-ocr.yaml    # OCR 服务
├── deployment-n8n.yaml    # n8n
├── service-api.yaml       # API 服务
├── service-web.yaml       # 前端服务
├── service-ai.yaml       # AI 服务
├── service-ocr.yaml      # OCR 服务
├── service-n8n.yaml      # n8n 服务
├── ingress.yaml           # 入口
├── hpa-api.yaml          # 自动扩缩容
├── pvc.yaml              # 持久化卷
└── mysql/
    ├── statefulset-mysql.yaml
    └── service-mysql.yaml
└── redis/
    ├── statefulset-redis.yaml
    └── service-redis.yaml
```

## 快速部署

```bash
# 1. 创建命名空间
kubectl apply -f namespace.yaml

# 2. 创建密钥
kubectl apply -f secret.yaml

# 3. 创建配置
kubectl apply -f configmap.yaml

# 4. 部署数据库和缓存
kubectl apply -f mysql/
kubectl apply -f redis/

# 5. 部署应用
kubectl apply -f deployment-api.yaml
kubectl apply -f deployment-web.yaml
kubectl apply -f deployment-ai.yaml
kubectl apply -f deployment-ocr.yaml
kubectl apply -f deployment-n8n.yaml

# 6. 部署服务
kubectl apply -f service-api.yaml
kubectl apply -f service-web.yaml
kubectl apply -f service-ai.yaml
kubectl apply -f service-ocr.yaml
kubectl apply -f service-n8n.yaml

# 7. 部署入口
kubectl apply -f ingress.yaml

# 8. 部署自动扩缩容
kubectl apply -f hpa-api.yaml

# 查看状态
kubectl get pods -n oa-system
kubectl get svc -n oa-system
kubectl get ingress -n oa-system
```

## 扩缩容

```bash
# 手动扩缩容
kubectl scale deployment oa-api-system --replicas=5 -n oa-system

# 查看 HPA
kubectl get hpa -n oa-system

# 查看 Pod 分布
kubectl top pods -n oa-system
```
