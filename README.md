# 🤖 AI-OA 智能化OA系统

> 基于 RuoYi + OCR + n8n + AI 的新一代智能化OA系统

[![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Stars](https://img.shields.io/github/stars/David8Idira/AI-OA?style=social)](https://github.com/David8Idira/AI-OA/stargazers)

---

## 📖 项目简介

AI-OA 是一款基于 RuoYi 开源框架开发的智能化OA系统，整合了 **OCR识别**、**n8n流程引擎**、**AI大模型** 能力，实现财务审批自动化和智能报表生成。

### 核心特性

| 模块 | 功能 | 技术亮点 |
|------|------|----------|
| 🔥 **财务审批** | OCR发票识别 + 行程单对接 + n8n审批流 | 置信度<85%自动邮件通知审批人+提交人 |
| 📊 **智能报表** | 周刊/月刊/年刊 + AI生图/视频 | 多模型按功能分配 |
| 🤖 **AI助手** | 侧边栏对话 + 知识库RAG + **自主学习** | 回复包含文档/审批跳转链接 |
| 💬 **企业聊天** | 即时消息 + 群聊 + WebSocket | Kafka高吞吐 |
| ⚙️ **系统配置** | AI模型配置 + 网关限流 + 缓存管理 | 可视化配置界面 |

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3 + Element UI)           │
├─────────────────────────────────────────────────────────────┤
│  网关层 (Spring Cloud Gateway + Sentinel 限流熔断)          │
├─────────┬─────────┬─────────┬─────────┬─────────┬─────────┤
│  用户   │  财务   │  报表   │  AI     │  聊天   │  流程   │
│  服务   │  审批   │  服务   │  服务   │  服务   │  中心   │
├─────────┴─────────┴─────────┴─────────┴─────────┴─────────┤
│                    n8n 工作流引擎                           │
├─────────────────────────────────────────────────────────────┤
│  Kafka (高吞吐)          │          RabbitMQ (可靠消息)    │
├─────────────────────────────────────────────────────────────┤
│  MySQL (主备库+读写分离) │  Redis Cluster (L1+L2缓存)     │
│  Milvus (向量数据库)     │          MinIO (S3存储)        │
└─────────────────────────────────────────────────────────────┘
```

---

## 📂 项目结构

```
AI-OA/
├── 业务分析/                    # 业务分析报告
│   └── AI-OA_业务分析报告_V1.0.md
├── 需求设计/                    # 需求规格说明书 & 架构设计
│   ├── AI-OA_需求规格说明书_V1.6.md    # ⭐ 最新需求
│   ├── AI-OA_完整架构总览_V2.0.md       # 系统全貌
│   ├── AI-OA_系统架构设计文档_V1.0.md
│   ├── AI-OA_容器化微服务架构文档_V1.2.md
│   └── AI-OA_架构图_Mermaid.md
├── 项目管理/                    # 项目管理计划
│   └── AI-OA_项目管理计划_V1.0.md
├── ui-prototype/               # UI原型 (可直接浏览器预览)
│   ├── 1-工作台.html
│   ├── 2-财务报销-OCR识别.html
│   ├── 3-AI助手-知识库问答.html
│   ├── 4-企业聊天.html
│   ├── 5-智能报表生成.html
│   ├── 6-系统配置-AI模型.html
│   ├── 7-移动端-鸿蒙APP.html       # 鸿蒙原生APP
│   ├── 8-移动端-iOS-APP.html      # iOS原生APP
│   └── 9-移动端-Android-APP.html   # Android原生APP
├── deploy/                     # 部署配置
│   ├── docker/                 # Docker Compose
│   ├── k8s/                    # Kubernetes
│   └── helm/                   # Helm Charts
└── docs/                       # 文档资源
    └── AI-OA-需求架构总览.xmind  # 思维导图
```

---

## 🛠️ 技术栈

| 层级 | 技术选型 |
|------|----------|
| **后端** | Java 17+ / Spring Boot 3.x / Spring Cloud |
| **前端** | Vue 3 + Element UI |
| **移动端** | 鸿蒙APP (ArkTS) · iOS APP (Swift) · Android APP (Kotlin) |
| **数据库** | MySQL 8.0 (主备库+读写分离) |
| **缓存** | Redis Cluster ( Caffeine L1 + Redis L2 ) |
| **消息队列** | Apache Kafka + RabbitMQ |
| **文件存储** | MinIO (S3兼容) |
| **AI能力** | GPT-4o / Claude 3.5 / Kimi / DALL-E 3 / Sora |
| **OCR** | PaddleOCR + 阿里云OCR |
| **工作流** | n8n (可视化流程编辑) |
| **部署** | Docker + Kubernetes + GitLab CI + ArgoCD |

---

## 📈 高并发指标

| 指标 | 目标值 |
|------|--------|
| 并发用户 | 1000+ |
| 注册用户 | 10万+ |
| 系统可用性 | 99.5%+ |
| 聊天消息延迟 | ≤100ms |
| API响应时间 | ≤200ms (P95) |

---

## 🔧 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/David8Idira/AI-OA.git
cd AI-OA
```

### 2. 查看文档

```bash
# 需求规格说明书
cat 需求设计/AI-OA_需求规格说明书_V1.6.md

# 架构设计
cat 需求设计/AI-OA_完整架构总览_V2.0.md

# 思维导图 (使用 XMind 打开)
open docs/AI-OA-需求架构总览.xmind
```

### 3. 预览UI原型

```bash
# 直接在浏览器打开
open ui-prototype/1-工作台.html
```

### 4. 部署

```bash
# Docker部署
cd deploy/docker
docker-compose up -d

# Kubernetes部署
cd deploy/k8s
kubectl apply -f .
```

---

## 📋 功能模块

| 模块 | 功能 | 状态 |
|------|------|------|
| F1 | 基础管理 + 企业邮箱 + 系统配置界面 | ✅ 完成 |
| F2 | 财务审批自动化（OCR + n8n审批） | ✅ 完成 |
| F3 | 智能报表生成（周刊/月刊/年刊） | ✅ 完成 |
| F4 | AI对话助手（知识库 + 自主学习） | ✅ 完成 |
| F5 | 流程管理中心（n8n可视化编辑） | ✅ 完成 |
| F6 | 增强功能（低代码/移动端/考勤/合同） | ✅ 完成 |
| F7 | 企业内部聊天（即时消息） | ✅ 完成 |
| **移动端** | **鸿蒙APP + iOS APP + Android APP** | ✅ 完成 |

---

## 👥 团队

| 角色 | 职责 |
|------|------|
| Master | 项目经理 - 统筹协调 |
| Analyst | 分析师 - 市场研究 |
| Designer | 设计师 - 架构设计 |
| Coder | 编码 - 开发实现 |
| Tester | 测试 - 质量保障 |

---

## 📄 License

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 🙏 致谢

- [RuoYi](https://gitee.com/y_project/RuoYi) - 基于若依框架
- [n8n](https://n8n.io/) - 工作流自动化
- [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR) - OCR识别

---

<p align="center">
  <strong>Made with ❤️ by AI-OA Team</strong>
</p>
