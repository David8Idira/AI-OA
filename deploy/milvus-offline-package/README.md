# Milvus 离线部署包

## 概述
此离线部署包包含在无网络环境下部署Milvus向量数据库所需的所有依赖。

## 目录结构
```
milvus-offline-package/
├── README.md                    # 说明文档
├── INSTALL.sh                   # 安装脚本
├── docker-compose.yml           # Docker Compose配置
├── milvus-standalone-docker.tar # Milvus Docker镜像
├── milvus-sdk-java-2.3.6.jar    # Java SDK
├── pymilvus-2.3.6.tar.gz        # Python SDK
├── milvusctl                    # Milvus控制脚本
├── certs/                       # SSL证书（可选）
│   ├── ca.pem
│   ├── server.pem
│   └── server.key
└── config/                      # 配置文件
    ├── milvus.yaml              # Milvus配置
    └── system.yaml              # 系统配置
```

## 系统要求
- Linux系统（CentOS 7+ / Ubuntu 18.04+）
- Docker 20.10+
- Docker Compose 1.29+
- 至少4GB内存
- 至少10GB磁盘空间

## 安装步骤

### 1. 解压部署包
```bash
tar -xzvf milvus-offline-package.tar.gz
cd milvus-offline-package
```

### 2. 加载Docker镜像
```bash
# 加载Milvus镜像
docker load -i milvus-standalone-docker.tar
```

### 3. 启动Milvus服务
```bash
# 启动服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f milvus-standalone
```

### 4. 验证安装
```bash
# 检查服务是否运行
curl http://localhost:19530/health

# 使用Python验证
python3 verify_milvus.py
```

## 配置文件说明

### Docker Compose配置
```yaml
version: '3.5'

services:
  etcd:
    container_name: milvus-etcd
    image: quay.io/coreos/etcd:v3.5.5
    environment:
      - ETCD_AUTO_COMPACTION_MODE=revision
      - ETCD_AUTO_COMPACTION_RETENTION=1000
      - ETCD_QUOTA_BACKEND_BYTES=4294967296
      - ETCD_SNAPSHOT_COUNT=50000
    volumes:
      - ${DOCKER_VOLUME_DIRECTORY:-.}/volumes/etcd:/etcd
    command: etcd -advertise-client-urls=http://127.0.0.1:2379 -listen-client-urls http://0.0.0.0:2379 --data-dir /etcd

  minio:
    container_name: milvus-minio
    image: minio/minio:RELEASE.2023-03-20T20-16-18Z
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    volumes:
      - ${DOCKER_VOLUME_DIRECTORY:-.}/volumes/minio:/minio_data
    command: minio server /minio_data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

  standalone:
    container_name: milvus-standalone
    image: milvusdb/milvus:v2.3.6
    command: ["milvus", "run", "standalone"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - ${DOCKER_VOLUME_DIRECTORY:-.}/volumes/milvus:/var/lib/milvus
    ports:
      - "19530:19530"
      - "9091:9091"
    depends_on:
      - "etcd"
      - "minio"
```

### Milvus配置文件（milvus.yaml）
```yaml
# 基础配置
common:
  defaultPartitionName: _default
  defaultIndexName: _default_idx
  
etcd:
  endpoints:
    - localhost:2379
  rootPath: by-dev
  
minio:
  address: localhost:9000
  accessKeyID: minioadmin
  secretAccessKey: minioadmin
  bucketName: a-bucket
  useSSL: false
  
storage:
  path: /var/lib/milvus
  autoIndexConfig:
    enable: true
    extraParams:
      index_type: IVF_FLAT
      metric_type: L2
      params: '{"nlist": 1024}'
```

## 故障排除

### 1. 端口冲突
如果19530端口被占用，修改docker-compose.yml中的端口映射：
```yaml
ports:
  - "19531:19530"  # 将外部端口改为19531
```

### 2. 内存不足
编辑docker-compose.yml，增加内存限制：
```yaml
standalone:
  deploy:
    resources:
      limits:
        memory: 8G
```

### 3. 数据持久化
默认数据存储在`./volumes`目录，确保该目录有足够空间。

### 4. 连接失败
检查防火墙设置：
```bash
# 开放端口
sudo ufw allow 19530/tcp
sudo ufw allow 2379/tcp
sudo ufw allow 9000/tcp
```

## API使用示例

### Java SDK使用
```java
// 添加本地JAR依赖
// 将milvus-sdk-java-2.3.6.jar复制到项目lib目录

// 连接Milvus
MilvusServiceClient client = new MilvusServiceClient(
    ConnectParam.newBuilder()
        .withHost("localhost")
        .withPort(19530)
        .build()
);
```

### Python SDK使用
```python
# 安装本地SDK
pip install pymilvus-2.3.6.tar.gz

# 连接Milvus
from pymilvus import connections, Collection
connections.connect("default", host="localhost", port="19530")
```

## 监控和维护

### 健康检查
```bash
# 使用curl检查
curl http://localhost:19530/health

# 使用milvusctl
./milvusctl health
```

### 备份和恢复
```bash
# 备份数据
./milvusctl backup --path /backup/milvus

# 恢复数据
./milvusctl restore --path /backup/milvus
```

### 日志查看
```bash
# 查看实时日志
docker-compose logs -f milvus-standalone

# 查看错误日志
docker-compose logs milvus-standalone | grep ERROR
```

## 性能优化建议

### 1. 内存优化
```yaml
# 修改milvus.yaml
cache:
  cacheSize: 4GB
  insertBufferSize: 1GB
```

### 2. 索引优化
```java
// 创建优化索引
IndexType indexType = IndexType.IVF_FLAT;
String indexParam = "{\"nlist\":1024}";
```

### 3. 批量操作
```java
// 批量插入数据
List<InsertParam.Field> fields = new ArrayList<>();
// ... 添加字段
client.insert(InsertParam.newBuilder()
    .withCollectionName("knowledge")
    .withFields(fields)
    .build());
```

## 安全配置

### 1. 启用认证
```yaml
# 修改milvus.yaml
common:
  security:
    authorizationEnabled: true
    clusterRootPassword: "YourSecurePassword"
```

### 2. SSL/TLS加密
```bash
# 使用提供的证书
cp certs/* /path/to/milvus/certs/
```

### 3. 网络隔离
```yaml
# 使用内部网络
networks:
  milvus-net:
    driver: bridge
    internal: true
```

## 联系支持
如有问题，请联系：
- 邮箱：support@ai-oa.com
- 文档：https://docs.ai-oa.com/milvus