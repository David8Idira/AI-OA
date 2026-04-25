# Docker国内镜像源配置指南

## 问题描述
在AI-OA项目部署过程中，可能会遇到Docker镜像拉取速度慢或失败的问题，特别是当使用国外镜像仓库（如 `registry-docker.cn-com`）时。

## 解决方案

### 方案一：使用预配置的国内版docker-compose文件（推荐）

我们已经为您准备了使用国内镜像源的docker-compose配置文件：

```bash
# 使用国内镜像源的配置
docker-compose -f docker-compose-cn.yml up -d
```

这个配置文件已将以下官方镜像替换为阿里云镜像源：
- `nginx:alpine` → `registry.cn-hangzhou.aliyuncs.com/library/nginx:alpine`
- `mysql:8.0` → `registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0`
- `redis:7-alpine` → `registry.cn-hangzhou.aliyuncs.com/library/redis:7-alpine`
- 等等...

### 方案二：配置Docker镜像加速器（全局生效）

运行一键配置脚本：

```bash
# 给予执行权限
chmod +x setup-docker-mirrors.sh

# 执行配置脚本（需要sudo权限）
sudo ./setup-docker-mirrors.sh
```

脚本将：
1. 备份原有Docker配置
2. 让您选择镜像源（阿里云、腾讯云、网易云等）
3. 配置Docker daemon.json
4. 重启Docker服务
5. 测试镜像拉取速度

### 方案三：手动配置Docker镜像加速器

编辑Docker配置文件 `/etc/docker/daemon.json`：

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
# Ubuntu/Debian/CentOS/RHEL
sudo systemctl restart docker

# 或者
sudo service docker restart
```

## 验证配置

检查配置是否生效：

```bash
# 查看镜像源配置
docker info | grep -A 2 "Registry Mirrors"

# 测试拉取速度
time docker pull nginx:alpine
```

## 常见问题

### Q1: 镜像拉取仍然很慢
- 尝试更换其他镜像源（阿里云、腾讯云、网易云等）
- 检查网络连接，确保没有防火墙阻挡
- 尝试使用代理服务器

### Q2: 某些镜像在镜像源中找不到
有些特定版本或私有镜像可能不在公共镜像源中，可以：
- 使用官方镜像 + 镜像加速
- 自己构建镜像
- 使用其他可用的替代镜像

### Q3: 如何还原原始配置？
```bash
# 如果备份了原始配置
sudo cp /etc/docker/daemon.json.bak /etc/docker/daemon.json
sudo systemctl restart docker
```

## 推荐的国内镜像源

### 1. 阿里云容器镜像服务（推荐）
- 地址：`https://registry.cn-hangzhou.aliyuncs.com`
- 特点：稳定、速度快、覆盖全
- 注册：需要阿里云账号

### 2. 腾讯云镜像加速器
- 地址：`https://mirror.ccs.tencentyun.com`
- 特点：腾讯云用户专用，速度快

### 3. 网易云镜像中心
- 地址：`https://hub-mirror.c.163.com`
- 特点：免注册，开箱即用

### 4. DaoCloud镜像加速器
- 地址：`https://docker.m.daocloud.io`
- 特点：历史悠久，稳定性好

## 项目中的具体替换

### 原docker-compose.yml中的镜像替换对照表

| 原镜像 | 国内替换镜像 |
|--------|-------------|
| `n8nio/n8n:latest` | `registry.cn-hangzhou.aliyuncs.com/library/n8nio/n8n:latest` |
| `mysql:8.0` | `registry.cn-hangzhou.aliyuncs.com/library/mysql:8.0` |
| `redis:7-alpine` | `registry.cn-hangzhou.aliyuncs.com/library/redis:7-alpine` |
| `minio/minio:latest` | `registry.cn-hangzhou.aliyuncs.com/library/minio/minio:latest` |
| `nacos/nacos-server:v2.2.3` | `registry.cn-hangzhou.aliyuncs.com/library/nacos/nacos-server:v2.2.3` |
| `bitnami/kafka:3.6` | `registry.cn-hangzhou.aliyuncs.com/library/bitnami/kafka:3.6` |
| `rabbitmq:3.12-management-alpine` | `registry.cn-hangzhou.aliyuncs.com/library/rabbitmq:3.12-management-alpine` |

## 进阶使用

### 批量替换现有镜像标签
```bash
# 将已有镜像重新打标签为国内源
docker tag n8nio/n8n:latest registry.cn-hangzhou.aliyuncs.com/library/n8nio/n8n:latest

# 删除原镜像
docker rmi n8nio/n8n:latest
```

### 使用国内源构建镜像
在Dockerfile中，如果基础镜像来自国外，可以替换：

```dockerfile
# 原Dockerfile
FROM openjdk:17-jdk-slim

# 替换为
FROM registry.cn-hangzhou.aliyuncs.com/library/openjdk:17-jdk-slim
```

### 创建镜像同步脚本
```bash
#!/bin/bash
# sync-images-to-cn.sh

IMAGES=(
    "openjdk:17-jdk-slim"
    "nginx:alpine"
    "mysql:8.0"
)

TARGET_REGISTRY="registry.cn-hangzhou.aliyuncs.com/library"

for IMAGE in "${IMAGES[@]}"; do
    echo "同步镜像: $IMAGE"
    docker pull $IMAGE
    docker tag $IMAGE ${TARGET_REGISTRY}/${IMAGE}
    docker push ${TARGET_REGISTRY}/${IMAGE}
done
```

## 性能对比测试

### 测试命令
```bash
# 测试国外镜像拉取
time docker pull nginx:alpine

# 测试国内镜像拉取
time docker pull registry.cn-hangzhou.aliyuncs.com/library/nginx:alpine
```

### 预期结果
- 国外镜像：10-60秒（取决于网络状况）
- 国内镜像：1-10秒（稳定快速）

## 注意事项

1. **镜像版本一致性**：确保国内镜像版本与项目要求一致
2. **认证信息**：私有镜像仓库需要提前登录
3. **网络环境**：企业内网可能需要额外配置代理
4. **镜像更新**：定期检查国内镜像是否及时同步

## 紧急备用方案

如果所有镜像源都不可用，可以：

1. **使用离线包**：
```bash
# 在能访问外网的环境提前下载
docker save -o images.tar nginx:alpine mysql:8.0 redis:alpine

# 在目标环境加载
docker load -i images.tar
```

2. **使用代理服务器**：
```bash
export http_proxy=http://proxy-server:port
export https_proxy=http://proxy-server:port
```

## 联系支持

如果遇到任何问题，请：
1. 检查网络连接
2. 查看Docker日志：`sudo journalctl -u docker`
3. 查看配置脚本日志：`cat /tmp/docker-mirror-setup-*.log`

## 总结

通过使用国内镜像源，可以显著提升AI-OA项目的部署速度，解决因网络问题导致的镜像拉取失败。推荐使用方案一（国内版docker-compose）或方案二（一键配置脚本）快速解决问题。