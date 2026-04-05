# AI-OA 容器化微服务架构设计文档

> 项目名称：AI-OA
> 文档版本：V1.2（消息队列Kafka/RabbitMQ + MinIO文档管理）
> 编制日期：2026-04-05
> 状态：生产级架构

---

## 一、架构设计目标

| 目标 | 说明 | 量化指标 |
|------|------|----------|
| **容器化部署** | 100%容器化，所有服务Docker化 | Dockerfile覆盖率 100% |
| **微服务架构** | 服务独立部署，独立扩缩容 | 服务数量 15+ |
| **横向扩展** | 根据负载自动扩缩容 | 扩展时间 < 60s |
| **高可用** | 99.5%+ 可用性，故障自愈 | RTO < 5min |
| **多环境支持** | dev/test/staging/prod | 一键部署 |

---

## 二、容器化架构总览

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                           AI-OA 容器化微服务架构                                │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          GitOps 流水线 (ArgoCD / GitLab CI)                  │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                         │                                            │
│  ┌──────────────────────────────────────▼──────────────────────────────────────┐   │
│  │                           Harbor 镜像仓库                                     │   │
│  │   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐       │   │
│  │   │ oa-api │ │ oa-web │ │ai-svc  │ │ ocr-svc│ │ n8n    │ │ nginx  │       │   │
│  │   └────────┘ └────────┘ └────────┘ └────────┘ └────────┘ └────────┘       │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                         │                                            │
│  ┌──────────────────────────────────────▼──────────────────────────────────────┐   │
│  │                    Kubernetes Cluster (K8s)                                   │   │
│  │                                                                             │   │
│  │   ┌─────────────────────────────────────────────────────────────────────┐  │   │
│  │   │                     System Namespace                                 │  │   │
│  │   │   ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐          │  │   │
│  │   │   │ingress │ │cert-mgr│ │metrics │ │logging │ │monitor │          │  │   │
│  │   │   └────────┘ └────────┘ └────────┘ └────────┘ └────────┘          │  │   │
│  │   └─────────────────────────────────────────────────────────────────────┘  │   │
│  │                                                                             │   │
│  │   ┌─────────────────────────────────────────────────────────────────────┐  │   │
│  │   │                     oa-system Namespace                             │  │   │
│  │   │                                                                     │  │   │
│  │   │   ┌─────────────────────────────────────────────────────────────┐   │  │   │
│  │   │   │  Deployment: oa-api (Spring Boot 微服务)                    │   │  │   │
│  │   │   │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐              │   │  │   │
│  │   │   │  │ pod-1  │ │ pod-2  │ │ pod-3  │ │ pod-N  │  ← HPA     │   │  │   │
│  │   │   │  └────────┘ └────────┘ └────────┘ └────────┘              │   │  │   │
│  │   │   └─────────────────────────────────────────────────────────────┘   │  │   │
│  │   │                                                                     │  │   │
│  │   └─────────────────────────────────────────────────────────────────────┘  │   │
│  │                                                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                         │                                            │
│  ┌──────────────────────────────────────▼──────────────────────────────────────┐   │
│  │                           基础设施层 (IaaS / CaaS)                           │   │
│  │   ┌────────────────┐  ┌────────────────┐  ┌────────────────┐             │   │
│  │   │  阿里云 ACK    │  │  AWS EKS        │  │  自建 K8s      │             │   │
│  │   └────────────────┘  └────────────────┘  └────────────────┘             │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 三、Dockerfile 规范

### 3.1 后端服务 Dockerfile

```dockerfile
# === Java / Spring Boot 服务 ===
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["/entrypoint.sh"]
EXPOSE 8080
```

### 3.2 前端服务 Dockerfile

```dockerfile
# === Vue3 前端 ===
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install -g pnpm && pnpm install
COPY . .
RUN pnpm build

FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/conf.d/default.conf

# 健康检查
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -q --spider http://localhost/health || exit 1

EXPOSE 80
```

### 3.3 AI/OCR 服务 Dockerfile

```dockerfile
# === Python / FastAPI 服务 ===
FROM python:3.11-slim
WORKDIR /app

# 依赖
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# OCR
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-chi-sim \
    poppler-utils \
    && rm -rf /var/lib/apt/lists/*

COPY . .

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8000/health || exit 1

EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 3.4 n8n 服务 Dockerfile

```dockerfile
# === n8n 工作流引擎 ===
FROM n8nio/n8n:latest
WORKDIR /home/node

# 挂载数据卷
VOLUME ["/home/node/.n8n"]

# 环境变量
ENV N8N_PROTOCOL=http
ENV N8N_PORT=5678
ENV EXECUTIONS_MODE=queue

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
  CMD wget -q --spider http://localhost:5678/healthz || exit 1

EXPOSE 5678
```

---

## 四、Kubernetes 部署配置

### 4.1 API 微服务 Deployment

```yaml
# deployment-api.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: oa-api-system
  namespace: oa-system
  labels:
    app: oa-api
    tier: backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: oa-api
  template:
    metadata:
      labels:
        app: oa-api
        tier: backend
    spec:
      containers:
      - name: oa-api
        image: harbor.example.com/oa/oa-api:v1.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: MYSQL_HOST
          value: "mysql-master.oa-db.svc.cluster.local"
        - name: REDIS_HOST
          value: "redis.oa-cache.svc.cluster.local"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 4.2 HPA 自动扩缩容配置

```yaml
# hpa-api.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: oa-api-hpa
  namespace: oa-system
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: oa-api-system
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 0
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 4
        periodSeconds: 15
      selectPolicy: Max
```

### 4.3 Service 配置

```yaml
# service-api.yaml
apiVersion: v1
kind: Service
metadata:
  name: oa-api-system-svc
  namespace: oa-system
spec:
  selector:
    app: oa-api
  ports:
  - name: http
    port: 8080
    targetPort: 8080
  type: ClusterIP
---
apiVersion: v1
kind: Endpoints
metadata:
  name: oa-api-system-svc
  namespace: oa-system
subsets:
- addresses:
  - ip: 10.244.1.10
  - ip: 10.244.1.11
  - ip: 10.244.1.12
  ports:
  - name: http
    port: 8080
```

### 4.4 Ingress 配置

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: oa-ingress
  namespace: oa-system
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/proxy-body-size: "100m"
    nginx.ingress.kubernetes.io/proxy-read-timeout: "300"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
spec:
  tls:
  - hosts:
    - oa.example.com
    secretName: oa-tls-cert
  rules:
  - host: oa.example.com
    http:
      paths:
      - path: /api/system
        pathType: Prefix
        backend:
          service:
            name: oa-api-system-svc
            port:
              number: 8080
      - path: /api/user
        pathType: Prefix
        backend:
          service:
            name: oa-api-user-svc
            port:
              number: 8080
      - path: /api/workflow
        pathType: Prefix
        backend:
          service:
            name: oa-api-workflow-svc
            port:
              number: 8080
      - path: /api/ai
        pathType: Prefix
        backend:
          service:
            name: oa-api-ai-svc
            port:
              number: 8080
      - path: /
        pathType: Prefix
        backend:
          service:
            name: oa-web-svc
            port:
              number: 80
```

---

## 五、微服务模块划分

### 5.1 服务清单

| 服务名 | 技术栈 | 副本数 | 内存 | CPU | 端口 |
|--------|--------|--------|------|-----|------|
| **核心服务** |
| oa-api-gateway | Spring Cloud | 3-10 | 1Gi | 500m | 8080 |
| oa-api-system | Spring Boot | 3-10 | 1Gi | 500m | 8080 |
| oa-api-user | Spring Boot | 3-10 | 1Gi | 500m | 8080 |
| oa-api-workflow | Spring Boot | 3-10 | 1Gi | 500m | 8080 |
| oa-api-invoice | Spring Boot | 3-10 | 1Gi | 500m | 8080 |
| oa-api-report | Spring Boot | 2-8 | 1Gi | 500m | 8080 |
| **集成服务** |
| oa-api-ai | FastAPI | 2-10 | 2Gi | 1000m | 8000 |
| oa-api-ocr | FastAPI | 2-10 | 2Gi | 1000m | 8000 |
| oa-api-mail | Spring Boot | 2-5 | 512Mi | 250m | 8080 |
| oa-api-trip | Spring Boot | 2-5 | 512Mi | 250m | 8080 |
| **扩展服务** |
| oa-api-form | Spring Boot | 2-5 | 1Gi | 500m | 8080 |
| oa-api-knowledge | Spring Boot | 2-5 | 1Gi | 500m | 8080 |
| oa-api-attendance | Spring Boot | 2-5 | 512Mi | 250m | 8080 |
| oa-api-contract | Spring Boot | 2-5 | 512Mi | 250m | 8080 |
| **前端服务** |
| oa-web | Vue3/Nginx | 3-10 | 256Mi | 100m | 80 |
| **基础设施** |
| n8n | Node.js | 2-5 | 1Gi | 500m | 5678 |
| redis | Redis | 3 | 2Gi | 1 | 6379 |
| mysql | MySQL | 3 | 8Gi | 2 | 3306 |

### 5.2 服务通信拓扑

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              微服务通信拓扑                                          │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│                           ┌──────────────────┐                                      │
│                           │  oa-api-gateway  │                                      │
│                           │  (Spring Cloud)  │                                      │
│                           └────────┬─────────┘                                      │
│                                    │                                                │
│         ┌──────────────────────────┼──────────────────────────┐                    │
│         │                          │                          │                    │
│    ┌────▼────┐              ┌─────▼─────┐              ┌─────▼─────┐             │
│    │system   │              │ workflow   │              │   ai      │             │
│    │service  │              │ service    │              │  service  │             │
│    └────┬────┘              └─────┬─────┘              └─────┬─────┘             │
│         │                         │                          │                    │
│    ┌────▼────┐              ┌─────▼─────┐              ┌─────▼─────┐             │
│    │user     │              │ invoice    │              │   ocr     │             │
│    │service  │              │ service    │              │  service  │             │
│    └────┬────┘              └─────┬─────┘              └─────┬─────┘             │
│         │                         │                          │                    │
│         └─────────────────────────┼──────────────────────────┘                    │
│                                   │                                                │
│                           ┌───────▼───────┐                                        │
│                           │     MQ        │                                        │
│                           │ (Kafka/RabbitMQ) │                                    │
│                           └───────────────┘                                        │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                           服务间通信方式                                     │   │
│  │                                                                              │   │
│  │  同步通信: HTTP/REST (Spring Cloud OpenFeign)                               │   │
│  │  异步消息: Kafka/RabbitMQ (高并发/可靠消息)                                  │   │
│  │  服务发现: Nacos (注册中心 + 配置中心)                                       │   │
│  │  负载均衡: Spring Cloud LoadBalancer (轮询/权重/最小连接)                     │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                           文件存储: MinIO (分布式对象存储)                     │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 六、Helm Chart 部署

### 6.1 项目结构

```
lrUoyi-oa/
├── Chart.yaml
├── values.yaml
├── values-prod.yaml
├── values-dev.yaml
├── templates/
│   ├── _helpers.tpl
│   ├── deployment-api.yaml
│   ├── deployment-web.yaml
│   ├── service-api.yaml
│   ├── service-web.yaml
│   ├── ingress.yaml
│   ├── hpa-api.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   └── pvc.yaml
└── charts/
    ├── mysql-10.0.0.tgz
    ├── redis-17.0.0.tgz
    └── n8n-2.0.0.tgz
```

### 6.2 values.yaml

```yaml
# values.yaml
global:
  imageRegistry: harbor.example.com
  imagePullSecrets: regcred
  storageClass: "alicloud-disk-ssd"

namespace: oa-system

# API 微服务配置
api:
  gateway:
    enabled: true
    replicaCount: 3
    image: oa/oa-api-gateway
    tag: v1.0
    service:
      type: ClusterIP
      port: 8080
    resources:
      requests:
        memory: "512Mi"
        cpu: "250m"
      limits:
        memory: "2Gi"
        cpu: "1000m"
    autoscaling:
      enabled: true
      minReplicas: 2
      maxReplicas: 10
      targetCPUUtilizationPercentage: 70
      targetMemoryUtilizationPercentage: 80
  
  system:
    enabled: true
    replicaCount: 3
    image: oa/oa-api-system
    tag: v1.0
    # ... 类似配置
    
  workflow:
    enabled: true
    replicaCount: 3
    image: oa/oa-api-workflow
    tag: v1.0
    # ... 类似配置

# 前端
web:
  enabled: true
  replicaCount: 3
  image: oa/oa-web
  tag: v1.0
  service:
    type: ClusterIP
    port: 80
  ingress:
    enabled: true
    host: oa.example.com
    path: /

# 基础设施
mysql:
  enabled: true
  architecture: replication
  auth:
    database: oa_system
    username: oa_user
  primary:
    persistence:
      enabled: true
      size: 100Gi
    resources:
      requests:
        memory: "4Gi"
        cpu: "1000m"
      limits:
        memory: "8Gi"
        cpu: "2000m"
  secondary:
    replicaCount: 2
    persistence:
      enabled: true
      size: 100Gi

redis:
  enabled: true
  architecture: replication
  auth:
    enabled: true
  master:
    persistence:
      enabled: true
      size: 20Gi
    resources:
      requests:
        memory: "1Gi"
        cpu: "500m"
  replica:
    replicaCount: 2
    persistence:
      enabled: true
      size: 20Gi

# n8n 工作流引擎
n8n:
  enabled: true
  replicaCount: 2
  image: n8nio/n8n
  tag: latest
  service:
    type: ClusterIP
    port: 5678
  persistence:
    enabled: true
    size: 10Gi
```

### 6.3 一键部署命令

```bash
# 添加 Helm 仓库
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update

# 安装 AI-OA
helm install lruoyi-oa ./charts/lruoyi-oa \
  -n oa-system \
  --create-namespace \
  -f values-prod.yaml

# 升级
helm upgrade lruoyi-oa ./charts/lruoyi-oa \
  -n oa-system \
  -f values-prod.yaml

# 查看状态
helm status lruoyi-oa -n oa-system
kubectl get pods -n oa-system
```

---

## 七、CI/CD 流水线

### 7.1 GitLab CI 配置

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - push
  - deploy

variables:
  REGISTRY: harbor.example.com
  K8S_CLUSTER: prod-ack
  NAMESPACE: oa-system

# 构建镜像
build:api:
  stage: build
  image: maven:3.9-eclipse-temurin-21
  script:
    - cd oa-api
    - mvn clean package -DskipTests
    - docker build -t $REGISTRY/oa/oa-api:$CI_COMMIT_SHA .
    - docker push $REGISTRY/oa/oa-api:$CI_COMMIT_SHA
  tags:
    - build
  only:
    - main

build:web:
  stage: build
  image: node:20-alpine
  script:
    - cd oa-web
    - npm install -g pnpm
    - pnpm install
    - pnpm build
    - docker build -t $REGISTRY/oa/oa-web:$CI_COMMIT_SHA .
    - docker push $REGISTRY/oa/oa-web:$CI_COMMIT_SHA
  tags:
    - build
  only:
    - main

# 单元测试
test:unit:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn test -f oa-api/pom.xml
    - mvn test -f oa-common/pom.xml
  coverage: '/Total:.*?([0-9]{1,3})%/'
  tags:
    - build

# 推送镜像
push:
  stage: push
  image: docker:24
  services:
    - docker:24-dind
  script:
    - docker login $REGISTRY -u $REGISTRY_USER -p $REGISTRY_PASS
    - docker tag $REGISTRY/oa/oa-api:$CI_COMMIT_SHA $REGISTRY/oa/oa-api:latest
    - docker push $REGISTRY/oa/oa-api:latest
  only:
    - main
  dependencies:
    - build:api

# 部署到 K8s
deploy:prod:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl set image deployment/oa-api-system oa-api=$REGISTRY/oa/oa-api:$CI_COMMIT_SHA -n $NAMESPACE
    - kubectl rollout status deployment/oa-api-system -n $NAMESPACE --timeout=300s
    - kubectl annotate deployment/oa-api-system kubernetes.io/change-cause="Deploy $CI_COMMIT_SHA" -n $NAMESPACE
  environment:
    name: prod
    url: https://oa.example.com
  only:
    - main
  tags:
    - deploy
```

### 7.2 ArgoCD 应用配置

```yaml
# argocd-application.yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: lruoyi-oa
  namespace: argocd
spec:
  project: default
  source:
    repoURL: https://github.com/David8Idira/AI-OA.git
    targetRevision: HEAD
    path: deploy/helm/lruoyi-oa
    helm:
      valueFiles:
        - values-prod.yaml
  destination:
    server: https://kubernetes.default.svc
    namespace: oa-system
  syncPolicy:
    automated:
      prune: true
      selfHeal: true
      allowEmpty: false
    syncOptions:
      - CreateNamespace=true
    retry:
      limit: 5
      backoff:
        duration: 5s
        factor: 2
        maxDuration: 3m
```

---

## 八、横向扩展策略

### 8.1 扩展维度

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              横向扩展策略                                            │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          维度一：Pod 水平扩展 (HPA)                          │   │
│  │                                                                              │   │
│  │   触发条件: CPU > 70% 持续 1min │ Memory > 80% 持续 1min                    │   │
│  │   扩展速度: 15s 内新增 Pod，最多 10 个副本                                   │   │
│  │   收缩速度: 5min 稳定后开始收缩，每次减少 10%                               │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          维度二：节点扩展 (Cluster Autoscaler)              │   │
│  │                                                                              │   │
│  │   触发条件: Pod 无法调度 (资源不足)                                          │   │
│  │   扩展速度: 3-5min 内新增节点                                              │   │
│  │   收缩速度: 10min 无负载后开始收缩                                          │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          维度三：数据库扩展 (读写分离/分库分表)               │   │
│  │                                                                              │   │
│  │   读扩展: 增加 MySQL Slave 节点，ShardingSphere 自动分发读请求              │   │
│  │   写扩展: 按业务分库 (按部门/按租户)                                        │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐   │
│  │                          维度四：缓存扩展 (Redis Cluster)                     │   │
│  │                                                                              │   │
│  │   扩展方式: 增加 Redis 节点，Slot 自动迁移                                   │   │
│  │   缓存预热: 应用启动时预加载热点数据                                         │   │
│  └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

### 8.2 扩展流程图

```
                    ┌──────────────────┐
                    │   负载监控      │
                    │ (Prometheus)    │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │   指标判断      │
                    │ CPU>70%?        │
                    │ Memory>80%?     │
                    └────────┬─────────┘
                             │
              ┌──────────────┴──────────────┐
              │                              │
         ┌────▼────┐                   ┌────▼────┐
         │  是     │                   │  否     │
         └────┬────┘                   └────┬────┘
              │                              │
    ┌─────────▼─────────┐                    │
    │   HPA 扩展检查    │                    │
    │ 当前副本 < 最大?  │                    │
    └─────────┬─────────┘                    │
              │                              │
    ┌─────────▼─────────┐                    │
    │     扩容         │                    │
    │ +1 Pod (15s)     │                    │
    └─────────┬─────────┘                    │
              │                              │
    ┌─────────▼─────────┐                    │
    │  调度到节点       │                    │
    │ Pod正常启动?      │                    │
    └─────────┬─────────┘                    │
              │                              │
    ┌─────────▼─────────┐                    │
    │  就绪探针通过     │                    │
    │  服务开始接收流量 │                    │
    └───────────────────┘                    │
```

### 8.3 扩缩容配置矩阵

| 服务类型 | 最小副本 | 最大副本 | 扩容阈值 | 收缩延迟 |
|----------|----------|----------|----------|----------|
| API Gateway | 3 | 20 | CPU 70% | 5min |
| 系统服务 | 3 | 15 | CPU 70%, Memory 80% | 5min |
| 审批服务 | 2 | 15 | CPU 70% | 5min |
| AI 服务 | 2 | 10 | CPU 70% | 10min |
| OCR 服务 | 2 | 10 | Queue > 10 | 10min |
| 前端 | 3 | 10 | CPU 70% | 5min |
| n8n | 2 | 5 | Queue > 20 | 10min |

---

## 九、多环境部署

### 9.1 环境矩阵

| 环境 | 用途 | K8s集群 | 资源配置 | 副本数 |
|------|------|---------|----------|--------|
| dev | 开发测试 | Dev Cluster | 2C4G × 3节点 | 1-2 |
| test | 功能测试 | Dev Cluster | 4C8G × 3节点 | 1-2 |
| staging | 预发布 | Staging Cluster | 8C16G × 5节点 | 2-5 |
| prod | 生产 | Prod Cluster | 8C16G × 10+节点 | 3-20 |

### 9.2 环境隔离策略

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              多环境隔离架构                                          │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             │
│   │    dev      │  │    test     │  │   staging   │  │    prod     │             │
│   │  Namespace  │  │  Namespace  │  │  Namespace  │  │  Namespace  │             │
│   │   dev-db    │  │   test-db   │  │ stage-db    │  │   prod-db   │             │
│   │   dev-redis │  │  test-redis │  │ stage-redis │  │  prod-redis │             │
│   └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘             │
│                                                                                     │
│   ┌─────────────────────────────────────────────────────────────────────────────┐   │
│   │                           网络策略 (NetworkPolicy)                          │   │
│   │                                                                              │   │
│   │   dev → test → staging → prod (单向，测试通过后 promotions)               │   │
│   │   禁止 prod 访问 dev/test 环境                                              │   │
│   └─────────────────────────────────────────────────────────────────────────────┘   │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 十、容器化最佳实践

### 10.1 镜像优化

| 实践 | 说明 |
|------|------|
| 多阶段构建 | 减少镜像体积 80% |
| Alpine 基础镜像 | 减少攻击面 |
| 非 root 运行 | 安全加固 |
| 最小化层数 | 合并 RUN 指令 |
| 缓存友好 | COPY 依赖文件在前 |

### 10.2 健康检查配置

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
  failureThreshold: 3

startupProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  failureThreshold: 30
  periodSeconds: 10
```

### 10.3 资源限制

```yaml
resources:
  requests:
    memory: "512Mi"    # Guaranteed
    cpu: "250m"       # 0.25 cores
  limits:
    memory: "2Gi"     # Max memory
    cpu: "1000m"      # Max 1 core
```

### 10.4 Pod 反亲和性

```yaml
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app
            operator: In
            values:
            - oa-api
        topologyKey: kubernetes.io/hostname
```

---

## 十一、运维监控

### 11.1 监控体系

| 组件 | 工具 | 监控内容 |
|------|------|----------|
| 基础设施 | Prometheus Node Exporter | CPU/内存/磁盘/网络 |
| 应用 | Prometheus Java Client | JVM/GC/线程池 |
| K8s | kube-state-metrics | Pod/Deployment/Service |
| 中间件 | Prometheus Exporters | MySQL/Redis/Kafka/RabbitMQ |
| 日志 | Loki + Promtail | 应用日志聚合 |
| 链路 | Jaeger | 分布式追踪 |
| 告警 | AlertManager | 钉钉/企微/邮件 |

### 11.2 关键告警

| 告警 | 条件 | 级别 |
|------|------|------|
| Pod CPU 高 | CPU > 90% 持续 5min | P1 |
| Pod 重启 | 重启次数 > 3 | P2 |
| Pod 不健康 | 连续 3 次健康检查失败 | P1 |
| HPA 扩容中 | 副本数 > 最大值 80% | P2 |
| 磁盘空间低 | 使用率 > 85% | P2 |
| API 延迟高 | P99 > 2s | P1 |

---

*文档版本：V1.2*
*更新内容：容器化部署 + 微服务架构 + 横向扩展策略 + 消息队列Kafka/RabbitMQ + MinIO文档管理*

---

## 十二、消息队列架构（Kafka/RabbitMQ）

### 12.1 技术选型对比

| 特性 | Apache Kafka | RabbitMQ | 推荐场景 |
|------|--------------|----------|----------|
| **吞吐量** | 百万级/秒 | 万级/秒 | 高并发选Kafka |
| **延迟** | 毫秒级 | 毫秒级 | 相当 |
| **消息持久化** | 支持 | 支持 | 相当 |
| **消息回溯** | 支持（按offset） | 不支持 | 日志分析选Kafka |
| **死信队列** | 支持 | 支持 | 相当 |
| **延迟消息** | 支持（需插件） | 原生支持 | 定时任务选RabbitMQ |
| **事务消息** | 支持 | 支持 | 相当 |
| **优先级队列** | 不支持 | 支持 | 优先级选RabbitMQ |
| **集群部署** | Zookeeper/KRaft | 镜像队列 | Kafka更成熟 |
| **运维复杂度** | 高 | 中 | RabbitMQ更简单 |

### 12.2 AI-OA 消息队列选型

**推荐方案：RabbitMQ集群 双队列架构**

| 场景 | 队列 | 原因 |
|------|------|------|
| **高并发消息** | Kafka | 审批事件、聊天消息、报表生成 |
| **可靠消息** | RabbitMQ | 邮件发送、支付回调、系统通知 |
| **延迟任务** | RabbitMQ | 定时提醒、过期处理 |

### 12.3 Kafka 配置

```yaml
# Kafka Producer 配置
spring:
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      acks: all
      retries: 3
      batch-size: 16384
      buffer-memory: 33554432
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5

# Kafka Consumer 配置
    consumer:
      group-id: lruoyi-oa
      auto-offset-reset: earliest
      enable-auto-commit: false
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      max-poll-records: 500
```

### 12.4 RabbitMQ 配置

```yaml
# RabbitMQ 配置
spring:
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    publisher-confirm-type: correlated
    publisher-returns: true
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 10
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
```

### 12.5 Topic/Queue 设计

**Kafka Topics**：

| Topic | 分区数 | 用途 | 消费者 |
|-------|--------|------|--------|
| workflow-events | 12 | 审批流程事件 | 审批服务、通知服务 |
| chat-messages | 24 | 聊天消息 | 消息服务、推送服务 |
| report-tasks | 6 | 报表生成任务 | 报表服务 |
| ocr-tasks | 8 | OCR识别任务 | OCR服务 |

**RabbitMQ Queues**：

| Queue | 用途 | TTL |
|-------|------|-----|
| mail-queue | 邮件发送 | - |
| notification-queue | 系统通知 | - |
| delay-reminder-queue | 延迟提醒 | 5min-24h |
| dead-letter-queue | 死信队列 | 7天 |

### 12.6 消息流程图

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                              消息队列架构                                          │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│   ┌─────────────┐                                                                  │
│   │  业务服务   │                                                                  │
│   └──────┬──────┘                                                                  │
│          │ Producer                                                                  │
│          ▼                                                                          │
│   ┌─────────────────────────────────────────────────────────────────────────────┐  │
│   │                        Kafka (高吞吐量)                                     │  │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │  │
│   │   │workflow  │  │  chat    │  │  report  │  │   ocr    │                 │  │
│   │   │-events   │  │ -messages │  │  -tasks  │  │  -tasks  │                 │  │
│   │   └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘                 │  │
│   │        │              │              │              │                        │  │
│   │        ▼              ▼              ▼              ▼                        │  │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │  │
│   │   │消费者组A  │  │消费者组B  │  │消费者组C  │  │消费者组D  │                 │  │
│   │   └──────────┘  └──────────┘  └──────────┘  └──────────┘                 │  │
│   └─────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                     │
│   ┌─────────────────────────────────────────────────────────────────────────────┐  │
│   │                       RabbitMQ (可靠消息)                                    │  │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │  │
│   │   │  mail    │  │   notif  │  │  delay   │  │   DLX    │                 │  │
│   │   │ -queue   │  │  -queue  │  │  -queue  │  │  (死信)  │                 │  │
│   │   └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────────┘                 │  │
│   │        │              │              │                                       │  │
│   │        ▼              ▼              ▼                                       │  │
│   │   ┌──────────┐  ┌──────────┐  ┌──────────┐                                │  │
│   │   │ 邮件服务  │  │ 通知服务  │  │ 延迟队列  │                                │  │
│   │   └──────────┘  └──────────┘  └──────────┘                                │  │
│   └─────────────────────────────────────────────────────────────────────────────┘  │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 十三、MinIO 文档管理架构

### 13.1 MinIO 选型理由

| 特性 | MinIO | 阿里云OSS | 腾讯COS |
|------|-------|-----------|---------|
| **部署方式** | 私有部署 | 云服务 | 云服务 |
| **S3兼容** | 原生支持 | 支持 | 支持 |
| **性能** | 高性能 | 高性能 | 高性能 |
| **运维** | 需自行维护 | 托管 | 托管 |
| **成本** | 服务器成本 | 按量付费 | 按量付费 |
| **数据控制** | 完全自有 | 在云厂商 | 在云厂商 |
| **适用场景** | 私有化部署 | SaaS云服务 | SaaS云服务 |

### 13.2 Bucket 设计

| Bucket | 用途 | 存储类型 | 生命周期 |
|--------|------|----------|----------|
| `oa-invoice` | 发票扫描件 | 标准存储 | 永久 |
| `oa-document` | 合同文档 | 标准存储 | 永久 |
| `oa-chat` | 聊天文件 | 标准存储 | 30天后删除 |
| `oa-report` | 报表生成 | 低频存储 | 1年后归档 |
| `oa-attachment` | 审批附件 | 标准存储 | 永久 |
| `oa-avatar` | 用户头像 | 标准存储 | 永久 |

### 13.3 文件组织结构

```
oa-system/
├── invoice/
│   ├── {year}/
│   │   ├── {month}/
│   │   │   ├── {invoice_id}.pdf
│   │   │   └── {invoice_id}_thumb.jpg
├── document/
│   ├── contract/
│   │   ├── {contract_id}/
│   │   │   ├── draft.pdf
│   │   │   └── final.pdf
│   └── template/
│       ├── report_weekly.xlsx
│       └── report_monthly.xlsx
├── chat/
│   ├── {user_id}/
│   │   ├── image/
│   │   ├── voice/
│   │   └── file/
├── report/
│   ├── {year}/
│   │   ├── weekly/
│   │   ├── monthly/
│   │   └── annual/
└── avatar/
    └── {user_id}.jpg
```

### 13.4 MinIO 配置

```yaml
# MinIO Client (mc) 配置
mc alias set myminio http://minio:9000 minioadmin minioadmin123

# 创建 Bucket
mc mb myminio/oa-invoice
mc mb myminio/oa-document
mc mb myminio/oa-chat
mc mb myminio/oa-report

# 设置 Bucket Policy (公开只读)
mc anonymous set download myminio/oa-invoice
mc anonymous set download myminio/oa-avatar

# 设置 Lifecycle (自动删除/归档)
mc ilm add --days 30 --delete myminio/oa-chat
mc ilm add --days 365 --transition-to "cold-storage" myminio/oa-report
```

### 13.5 Spring Boot MinIO 集成

```java
// application.yml
spring:
  minio:
    endpoint: http://minio:9000
    access-key: minioadmin
    secret-key: minioadmin123
    bucket:
      invoice: oa-invoice
      document: oa-document
      chat: oa-chat
      report: oa-report
      avatar: oa-avatar
```

### 13.6 文件访问策略

```java
// Presigned URL (带签名临时访问)
@GetMapping("/download/{bucket}/{objectName}")
public String getPresignedUrl(@PathVariable String bucket, @PathVariable String objectName) {
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .expiry(3600) // 1小时
            .build()
    );
}

// 文件上传
@PostMapping("/upload/{bucket}")
public String upload(@PathVariable String bucket, @RequestParam("file") MultipartFile file) {
    String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();
    minioClient.putObject(
        PutObjectArgs.builder()
            .bucket(bucket)
            .object(objectName)
            .stream(file.getInputStream(), file.getSize(), -1)
            .contentType(file.getContentType())
            .build()
    );
    return objectName;
}
```

### 13.7 文档管理功能

| 功能 | 说明 | 实现 |
|------|------|------|
| 上传下载 | 文件上传下载 | Presigned URL |
| 预览 | Office/PDF在线预览 | WOPI协议 |
| 分享 | 生成分享链接 | Presigned URL + 过期时间 |
| 版本控制 | 文件版本管理 | MinIO Versioning |
| 增量备份 | 增量同步到OSS | mc mirror |
| 权限控制 | Bucket/Object策略 | IAM Policy |

---

*文档版本：V1.2*
*更新内容：消息队列Kafka/RabbitMQ选型 + MinIO文档管理架构*

