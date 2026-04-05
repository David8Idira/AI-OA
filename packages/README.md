# AI-OA 部署包总览

## 部署包列表

| 部署包 | 文件名 | 适用场景 | 服务器数量 |
|--------|--------|----------|------------|
| 单体部署 | AI-OA-standalone-v1.0.tar.gz | 中小企业 | 1-2台 |
| 微服务部署 | AI-OA-microservice-v1.0.tar.gz | 中大型企业 | 8-12台 |
| Docker部署 | AI-OA-docker-v1.0.tar.gz | 开发/测试 | 2-4台 |
| Kubernetes部署 | AI-OA-k8s-v1.0.tar.gz | 大型企业 | 15-20+台 |

## 选择指南

| 你的场景 | 推荐部署包 |
|----------|------------|
| 创业公司，<100人 | 单体部署 |
| 中小企业，100-500人 | 微服务部署 |
| 开发测试环境 | Docker部署 |
| 中大型企业，500-1000人 | Kubernetes部署 |
| 大型企业，1000+用户 | Kubernetes部署 |

## 通用部署流程

```bash
# 1. 选择适合的部署包
tar -xzvf AI-OA-<方案>-v1.0.tar.gz

# 2. 阅读README
cat README.md

# 3. 配置参数
vim config/*.yml

# 4. 执行部署
./scripts/deploy.sh

# 5. 验证部署
curl http://localhost:80
```

## 技术支持

- 文档：https://github.com/David8Idira/AI-OA
- 问题反馈：https://github.com/David8Idira/AI-OA/issues

---

版本：1.0.0
更新日期：2026-04-05
