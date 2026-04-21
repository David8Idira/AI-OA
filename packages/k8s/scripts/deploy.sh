#!/bin/bash
#===============================================================================
# AI-OA Kubernetes 部署脚本
# 
# 适用场景: 大型企业，高可用，弹性伸缩 (1000+用户)
# 服务器数量: 15-20+台
# 
# 功能:
#   - K8s集群安装 (kubeadm)
#   - Helm Charts部署
#   - MySQL/Redis/MinIO/RabbitMQ/n8n/AI-OA K8s部署
#===============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

#-------------------------------------------------------------------------------
# 安装kubeadm
#-------------------------------------------------------------------------------
install_kubeadm() {
    info "安装Kubernetes组件..."
    
    case "$TARGET_OS" in
        centos|rhel)
            cat > /etc/yum.repos.d/kubernetes.repo << 'EOF'
[kubernetes]
name=Kubernetes
baseurl=https://packages.cloud.google.com/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=1
repo_gpgcheck=1
gpgkey=https://packages.cloud.google.com/yum/doc/yum-key.gpg https://packages.cloud.google.com/yum/doc/rpm-package-key.gpg
EOF
            yum install -y kubelet kubeadm kubectl >> "$INSTALL_LOG" 2>&1
            systemctl enable kubelet
            systemctl start kubelet
            ;;
        ubuntu|debian)
            curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | gpg --dearmor -o /etc/apt/keyrings/kubernetes-archive-keyring.gpg >> "$INSTALL_LOG" 2>&1
            echo "deb [signed-by=/etc/apt/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | tee /etc/apt/sources.list.d/kubernetes.list >> "$INSTALL_LOG" 2>&1
            apt-get update >> "$INSTALL_LOG" 2>&1
            apt-get install -y kubelet kubeadm kubectl >> "$INSTALL_LOG" 2>&1
            apt-mark hold kubelet kubeadm kubectl
            ;;
    esac
    
    success "Kubernetes组件安装完成"
}

#-------------------------------------------------------------------------------
# 初始化K8s集群
#-------------------------------------------------------------------------------
init_k8s_cluster() {
    info "初始化Kubernetes集群..."
    
    # 预检
    kubeadm init --dry-run >> "$INSTALL_LOG" 2>&1 || true
    
    # 初始化集群
    kubeadm init --pod-network-cidr=10.244.0.0/16 --service-cidr=10.96.0.0/12 >> "$INSTALL_LOG" 2>&1
    
    # 配置kubectl
    mkdir -p "$HOME/.kube"
    cp -i /etc/kubernetes/admin.conf "$HOME/.kube/config"
    chmod 600 "$HOME/.kube/config"
    
    # 安装网络插件 (Calico)
    kubectl apply -f https://docs.projectcalico.org/manifests/calico.yaml >> "$INSTALL_LOG" 2>&1
    
    success "Kubernetes集群初始化完成"
    
    # 获取join命令
    local join_cmd=$(kubeadm token create --print-join-command)
    info "其他节点加入命令: $join_cmd"
    echo "$join_cmd" > /root/k8s-join-command.sh
    chmod +x /root/k8s-join-command.sh
}

#-------------------------------------------------------------------------------
# 创建Namespace
#-------------------------------------------------------------------------------
create_namespaces() {
    info "创建Kubernetes命名空间..."
    
    kubectl create namespace aioa --dry-run=client -o yaml | kubectl apply -f -
    kubectl create namespace middleware --dry-run=client -o yaml | kubectl apply -f -
    
    success "命名空间创建完成"
}

#-------------------------------------------------------------------------------
# 创建Secret
#-------------------------------------------------------------------------------
create_secrets() {
    info "创建密钥配置..."
    
    # MySQL密码Secret
    kubectl create secret generic mysql-secret \
        --from-literal=root-password=ChangeMe123! \
        --from-literal=password=AioaPassword123! \
        -n aioa --dry-run=client -o yaml | kubectl apply -f -
    
    # Redis密码Secret
    kubectl create secret generic redis-secret \
        --from-literal=password=RedisPassword123 \
        -n aioa --dry-run=client -o yaml | kubectl apply -f -
    
    # MinIO配置Secret
    kubectl create secret generic minio-secret \
        --from-literal=access-key=aioaadmin \
        --from-literal=secret-key=MinioPassword123! \
        -n aioa --dry-run=client -o yaml | kubectl apply -f -
    
    # RabbitMQ Secret
    kubectl create secret generic rabbitmq-secret \
        --from-literal=username=aioa \
        --from-literal=password=AioaPassword123 \
        -n aioa --dry-run=client -o yaml | kubectl apply -f -
    
    success "密钥配置完成"
}

#-------------------------------------------------------------------------------
# 部署MySQL
#-------------------------------------------------------------------------------
deploy_mysql() {
    info "部署MySQL..."
    
    mkdir -p /opt/aioa/k8s/mysql
    cat > /opt/aioa/k8s/mysql/statefulset.yaml << 'EOF'
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: mysql
  namespace: aioa
spec:
  serviceName: mysql
  replicas: 1
  selector:
    matchLabels:
      app: mysql
  template:
    metadata:
      labels:
        app: mysql
    spec:
      containers:
      - name: mysql
        image: mysql:8.0
        ports:
        - containerPort: 3306
          name: mysql
        env:
        - name: MYSQL_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: root-password
        - name: MYSQL_DATABASE
          value: aioa
        - name: MYSQL_USER
          value: aioa
        - name: MYSQL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        volumeMounts:
        - name: mysql-data
          mountPath: /var/lib/mysql
        resources:
          requests:
            cpu: 500m
            memory: 2Gi
          limits:
            cpu: 2000m
            memory: 4Gi
  volumeClaimTemplates:
  - metadata:
      name: mysql-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 50Gi
EOF

    cat > /opt/aioa/k8s/mysql/service.yaml << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: mysql
  namespace: aioa
spec:
  ports:
  - port: 3306
    targetPort: 3306
  selector:
    app: mysql
EOF

    kubectl apply -f /opt/aioa/k8s/mysql/
    success "MySQL部署完成"
}

#-------------------------------------------------------------------------------
# 部署Redis
#-------------------------------------------------------------------------------
deploy_redis() {
    info "部署Redis..."
    
    mkdir -p /opt/aioa/k8s/redis
    cat > /opt/aioa/k8s/redis/statefulset.yaml << 'EOF'
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: redis
  namespace: aioa
spec:
  serviceName: redis
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        ports:
        - containerPort: 6379
          name: redis
        command: ["redis-server", "--requirepass", "$(REDIS_PASSWORD)"]
        env:
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: redis-secret
              key: password
        volumeMounts:
        - name: redis-data
          mountPath: /data
        resources:
          requests:
            cpu: 200m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 2Gi
  volumeClaimTemplates:
  - metadata:
      name: redis-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 10Gi
EOF

    cat > /opt/aioa/k8s/redis/service.yaml << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: redis
  namespace: aioa
spec:
  ports:
  - port: 6379
    targetPort: 6379
  selector:
    app: redis
EOF

    kubectl apply -f /opt/aioa/k8s/redis/
    success "Redis部署完成"
}

#-------------------------------------------------------------------------------
# 部署RabbitMQ
#-------------------------------------------------------------------------------
deploy_rabbitmq() {
    info "部署RabbitMQ..."
    
    mkdir -p /opt/aioa/k8s/rabbitmq
    cat > /opt/aioa/k8s/rabbitmq/statefulset.yaml << 'EOF'
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: rabbitmq
  namespace: aioa
spec:
  serviceName: rabbitmq
  replicas: 1
  selector:
    matchLabels:
      app: rabbitmq
  template:
    metadata:
      labels:
        app: rabbitmq
    spec:
      containers:
      - name: rabbitmq
        image: rabbitmq:3.12-management-alpine
        ports:
        - containerPort: 5672
          name: amqp
        - containerPort: 15672
          name: management
        env:
        - name: RABBITMQ_DEFAULT_USER
          valueFrom:
            secretKeyRef:
              name: rabbitmq-secret
              key: username
        - name: RABBITMQ_DEFAULT_PASS
          valueFrom:
            secretKeyRef:
              name: rabbitmq-secret
              key: password
        volumeMounts:
        - name: rabbitmq-data
          mountPath: /var/lib/rabbitmq
        resources:
          requests:
            cpu: 200m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 2Gi
  volumeClaimTemplates:
  - metadata:
      name: rabbitmq-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 20Gi
EOF

    cat > /opt/aioa/k8s/rabbitmq/service.yaml << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: rabbitmq
  namespace: aioa
spec:
  ports:
  - port: 5672
    targetPort: 5672
    name: amqp
  - port: 15672
    targetPort: 15672
    name: management
  selector:
    app: rabbitmq
EOF

    kubectl apply -f /opt/aioa/k8s/rabbitmq/
    success "RabbitMQ部署完成"
}

#-------------------------------------------------------------------------------
# 部署MinIO
#-------------------------------------------------------------------------------
deploy_minio() {
    info "部署MinIO..."
    
    mkdir -p /opt/aioa/k8s/minio
    cat > /opt/aioa/k8s/minio/statefulset.yaml << 'EOF'
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: minio
  namespace: aioa
spec:
  serviceName: minio
  replicas: 1
  selector:
    matchLabels:
      app: minio
  template:
    metadata:
      labels:
        app: minio
    spec:
      containers:
      - name: minio
        image: minio/minio:latest
        args:
        - server
        - /data
        - --console-address
        - ":9001"
        ports:
        - containerPort: 9000
          name: api
        - containerPort: 9001
          name: console
        env:
        - name: MINIO_ROOT_USER
          valueFrom:
            secretKeyRef:
              name: minio-secret
              key: access-key
        - name: MINIO_ROOT_PASSWORD
          valueFrom:
            secretKeyRef:
              name: minio-secret
              key: secret-key
        volumeMounts:
        - name: minio-data
          mountPath: /data
        resources:
          requests:
            cpu: 200m
            memory: 512Mi
          limits:
            cpu: 1000m
            memory: 2Gi
  volumeClaimTemplates:
  - metadata:
      name: minio-data
    spec:
      accessModes: ["ReadWriteOnce"]
      resources:
        requests:
          storage: 100Gi
EOF

    cat > /opt/aioa/k8s/minio/service.yaml << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: minio
  namespace: aioa
spec:
  ports:
  - port: 9000
    targetPort: 9000
    name: api
  - port: 9001
    targetPort: 9001
    name: console
  selector:
    app: minio
EOF

    kubectl apply -f /opt/aioa/k8s/minio/
    success "MinIO部署完成"
}

#-------------------------------------------------------------------------------
# 部署AI-OA应用
#-------------------------------------------------------------------------------
deploy_aioa() {
    info "部署AI-OA应用..."
    
    mkdir -p /opt/aioa/k8s/aioa
    cat > /opt/aioa/k8s/aioa/deployment.yaml << 'EOF'
apiVersion: apps/v1
kind: Deployment
metadata:
  name: aioa
  namespace: aioa
spec:
  replicas: 2
  selector:
    matchLabels:
      app: aioa
  template:
    metadata:
      labels:
        app: aioa
    spec:
      containers:
      - name: aioa
        image: aioa/app:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql:3306/aioa?useUnicode=true&characterEncoding=utf8"
        - name: SPRING_DATASOURCE_USERNAME
          value: aioa
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: mysql-secret
              key: password
        - name: SPRING_REDIS_HOST
          value: redis
        - name: SPRING_REDIS_PORT
          value: "6379"
        - name: SPRING_REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: redis-secret
              key: password
        resources:
          requests:
            cpu: 500m
            memory: 1Gi
          limits:
            cpu: 2000m
            memory: 4Gi
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
EOF

    cat > /opt/aioa/k8s/aioa/service.yaml << 'EOF'
apiVersion: v1
kind: Service
metadata:
  name: aioa
  namespace: aioa
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    name: http
  selector:
    app: aioa
EOF

    cat > /opt/aioa/k8s/aioa/hpa.yaml << 'EOF'
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: aioa-hpa
  namespace: aioa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: aioa
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
EOF

    kubectl apply -f /opt/aioa/k8s/aioa/
    success "AI-OA部署完成"
}

#-------------------------------------------------------------------------------
# 部署Ingress
#-------------------------------------------------------------------------------
deploy_ingress() {
    info "部署Ingress..."
    
    # 安装Ingress Controller
    kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.8.0/deploy/static/provider/cloud/deploy.yaml >> "$INSTALL_LOG" 2>&1
    
    mkdir -p /opt/aioa/k8s/ingress
    cat > /opt/aioa/k8s/ingress/ingress.yaml << 'EOF'
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: aioa-ingress
  namespace: aioa
  annotations:
    nginx.ingress.kubernetes.io/proxy-body-size: "100m"
spec:
  rules:
  - host: aioa.example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: aioa
            port:
              number: 80
EOF

    kubectl apply -f /opt/aioa/k8s/ingress/ingress.yaml
    success "Ingress部署完成"
}

#-------------------------------------------------------------------------------
# 验证部署
#-------------------------------------------------------------------------------
verify_k8s_deployment() {
    info "验证Kubernetes部署..."
    
    echo ""
    info "Pods状态:"
    kubectl get pods -n aioa
    
    echo ""
    info "Services状态:"
    kubectl get svc -n aioa
    
    echo ""
    info "Deployments状态:"
    kubectl get deployments -n aioa
    
    echo ""
    info "HPA状态:"
    kubectl get hpa -n aioa 2>/dev/null || echo "HPA未配置"
}

#-------------------------------------------------------------------------------
# 主函数
#-------------------------------------------------------------------------------
k8s_main() {
    info "========== AI-OA Kubernetes 部署 =========="
    
    install_kubeadm
    init_k8s_cluster
    create_namespaces
    create_secrets
    deploy_mysql
    deploy_redis
    deploy_rabbitmq
    deploy_minio
    deploy_aioa
    deploy_ingress
    verify_k8s_deployment
    
    show_k8s_info
}

show_k8s_info() {
    echo ""
    echo "========================================"
    echo "  AI-OA Kubernetes 部署完成"
    echo "========================================"
    echo ""
    echo "  管理命令:"
    echo "    kubectl get pods -n aioa"
    echo "    kubectl get svc -n aioa"
    echo "    kubectl logs -f <pod-name> -n aioa"
    echo ""
    echo "  访问地址:"
    echo "    应用:     http://aioa.example.com (需配置DNS)"
    echo "    MinIO:   http://minio.example.com:9001"
    echo "    RabbitMQ: http://rabbitmq.example.com:15672"
    echo ""
    echo "  其他节点加入:"
    echo "    /root/k8s-join-command.sh"
    echo ""
}

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    k8s_main "$@"
fi
