# Docker镜像仓库国内替代方案

## 问题描述
项目中可能使用了 `registry-docker.cn-com` 或其他国外Docker镜像仓库，导致国内环境下载镜像速度慢或失败。

## 国内镜像仓库推荐

### 1. 阿里云容器镜像服务（推荐）
```bash
# 阿里云镜像加速器
registry.cn-hangzhou.aliyuncs.com
```

### 2. 腾讯云容器镜像服务
```bash
# 腾讯云镜像仓库
ccr.ccs.tencentyun.com
```

### 3. 华为云SWR
```bash
# 华为云镜像仓库
swr.cn-north-1.myhuaweicloud.com
```

### 4. 网易云镜像中心
```bash
# 网易云镜像加速
hub-mirror.c.163.com
```

### 5. DaoCloud镜像中心
```bash
# DaoCloud镜像加速
docker.m.daocloud.io
```

## 配置方法

### 方法一：Docker配置文件替换（全局生效）
修改Docker配置文件 `/etc/docker/daemon.json`:

```json
{
  "registry-mirrors": [
    "https://registry.cn-hangzhou.aliyuncs.com",
    "https://docker.m.daocloud.io",
    "https://hub-mirror.c.163.com"
  ]
}
```

重启Docker服务：
```bash
sudo systemctl restart docker
```

### 方法二：Maven pom.xml配置修改
如果项目使用Maven插件构建Docker镜像，修改pom.xml中的镜像仓库配置：

```xml
<plugin>
    <groupId>com.spotify</groupId>
    <artifactId>docker-maven-plugin</artifactId>
    <version>1.2.2</version>
    <configuration>
        <!-- 替换为国内镜像仓库 -->
        <registryUrl>registry.cn-hangzhou.aliyuncs.com/your-namespace</registryUrl>
    </configuration>
</plugin>
```

### 方法三：Dockerfile中的基础镜像替换
修改Dockerfile中的FROM指令：

```dockerfile
# 原始（可能速度慢）
FROM openjdk:17-jdk-slim

# 替换为国内镜像
FROM registry.cn-hangzhou.aliyuncs.com/library/openjdk:17-jdk-slim
```

### 方法四：Maven镜像仓库配置
修改Maven配置文件 `~/.m2/settings.xml`:

```xml
<mirrors>
    <mirror>
        <id>aliyunmaven</id>
        <mirrorOf>*</mirrorOf>
        <name>阿里云公共仓库</name>
        <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
</mirrors>
```

## 针对本项目(AI-OA)的具体建议

### 1. 检查项目中的配置
```bash
# 查找所有可能的镜像仓库引用
find . -type f \( -name "*.xml" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" \) -exec grep -l "docker\|registry\|maven.*repo" {} \;
```

### 2. 如果使用Spring Boot Maven插件
检查是否有以下配置，并替换镜像仓库：

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <image>
            <name>registry.cn-hangzhou.aliyuncs.com/your-namespace/${project.artifactId}:${project.version}</name>
        </image>
    </configuration>
</plugin>
```

### 3. 构建脚本中的修改
如果有构建脚本（如build.sh、deploy.sh），检查其中的镜像推送命令：

```bash
# 原始命令
docker push registry-docker.cn-com/your-image:tag

# 替换为
docker push registry.cn-hangzhou.aliyuncs.com/your-namespace/your-image:tag
```

## 快速测试验证

### 1. 测试镜像拉取速度
```bash
# 测试国外镜像
time docker pull nginx:alpine

# 测试国内镜像
time docker pull registry.cn-hangzhou.aliyuncs.com/library/nginx:alpine
```

### 2. 查看当前镜像源配置
```bash
docker info | grep -i registry
```

## 常见问题解决

### 1. 镜像拉取超时
```bash
# 设置Docker超时时间
export DOCKER_CLIENT_TIMEOUT=120
export COMPOSE_HTTP_TIMEOUT=120
```

### 2. 认证问题
```bash
# 登录国内镜像仓库
docker login registry.cn-hangzhou.aliyuncs.com
# 输入阿里云账号密码
```

### 3. 镜像tag不存在
国内镜像仓库可能没有某些特定tag，可以使用：
- `:latest` 版本
- 特定版本号如 `:1.0.0`
- 使用官方镜像 + 镜像加速

## 最佳实践建议

1. **使用阿里云镜像加速器**（最稳定、速度最快）
2. **在CI/CD流水线中配置镜像仓库**
3. **使用企业私有的镜像仓库**（如有）
4. **定期同步关键基础镜像到国内仓库**
5. **编写镜像同步脚本**，确保镜像可用性

## 镜像同步脚本示例

```bash
#!/bin/bash
# sync-docker-images.sh

# 源镜像列表
IMAGES=(
    "openjdk:17-jdk-slim"
    "nginx:alpine"
    "redis:alpine"
    "mysql:8.0"
    "postgres:15-alpine"
)

# 目标镜像仓库
TARGET_REGISTRY="registry.cn-hangzhou.aliyuncs.com/your-namespace"

for IMAGE in "${IMAGES[@]}"; do
    # 拉取官方镜像
    docker pull $IMAGE
    
    # 打标签
    docker tag $IMAGE ${TARGET_REGISTRY}/${IMAGE}
    
    # 推送到国内仓库
    docker push ${TARGET_REGISTRY}/${IMAGE}
done

echo "镜像同步完成"
```

## 紧急情况处理

如果无法立即修改配置，可以使用临时解决方案：

```bash
# 使用代理
export http_proxy=http://your-proxy:port
export https_proxy=http://your-proxy:port

# 或者使用镜像下载后导入
docker save -o image.tar your-image:tag
docker load -i image.tar
```

## 总结
将`registry-docker.cn-com`替换为国内镜像仓库可以显著提升镜像下载速度，提高开发部署效率。建议优先使用阿里云容器镜像服务，并根据实际情况选择合适的方案。