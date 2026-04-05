# LRuoYi-OA 系统架构图 (Mermaid)

> 可在 GitHub / Typora / VSCode 等支持 Mermaid 的编辑器中渲染

## 1. 整体架构图

```mermaid
graph TB
    subgraph 用户层["用户访问层"]
        PC[PC Web]
        Mobile[Mobile H5]
        MiniApp[小程序]
        Ding[钉钉/企微]
        APP[App]
    end
    
    subgraph CDN["CDN / SLB 负载均衡"]
        CLB[阿里云CLB / Nginx]
    end
    
    subgraph 网关层["网关层 API Gateway"]
        Gateway[Spring Cloud Gateway]
        Auth[鉴权中心]
        RateLimit[限流熔断]
    end
    
    subgraph 服务集群["业务服务集群"]
        subgraph 核心服务
            System[系统服务]
            User[用户服务]
            Workflow[审批服务]
            Report[报表服务]
        end
        subgraph 集成服务
            Invoice[发票服务]
            Trip[行程单服务]
            AI[AI服务]
            Mail[邮箱服务]
        end
        subgraph 扩展服务
            Form[表单服务]
            Knowledge[知识库服务]
            Attend[考勤服务]
            Contract[合同服务]
        end
    end
    
    subgraph 数据层["数据访问层"]
        subgraph 缓存["Redis Cluster 缓存层"]
            Redis1[Master]
            Redis2[Slave1]
            Redis3[Slave2]
        end
        subgraph 数据库["MySQL Cluster 数据库层"]
            MySQL1[主库 Write]
            MySQL2[备库1 Read]
            MySQL3[备库2 Read]
        end
        subgraph 存储["向量库/文件存储"]
            Milvus[(Milvus)]
            MinIO[(MinIO)]
            OSS[(阿里OSS)]
        end
    end
    
    subgraph 消息层["消息队列/任务调度"]
        MQ[RocketMQ]
        Job[XXL-Job]
    end
    
    subgraph 基础设施["基础设施"]
        K8s[Kubernetes]
        Monitor[Prometheus/Grafana]
        Log[Loki/Kibana]
    end
    
    PC --> CLB
    Mobile --> CLB
    MiniApp --> CLB
    Ding --> CLB
    APP --> CLB
    CLB --> Gateway
    Gateway --> Auth
    Gateway --> RateLimit
    Gateway --> System
    Gateway --> User
    Gateway --> Workflow
    Gateway --> Report
    Gateway --> Invoice
    Gateway --> Trip
    Gateway --> AI
    Gateway --> Mail
    Gateway --> Form
    Gateway --> Knowledge
    Gateway --> Attend
    Gateway --> Contract
    
    System --> Redis1
    User --> Redis1
    Workflow --> Redis1
    AI --> Redis1
    
    System --> MySQL1
    User --> MySQL1
    Workflow --> MySQL1
    Invoice --> MySQL1
    
    MySQL1 -->|Binlog同步| MySQL2
    MySQL1 -->|Binlog同步| MySQL3
    
    AI --> Milvus
    Workflow --> MinIO
    Report --> OSS
    
    Workflow --> MQ
    Report --> MQ
    Invoice --> MQ
    
    Report --> Job
    Trip --> Job
    Attend --> Job
    
    K8s --> System
    K8s --> User
    K8s --> Workflow
    K8s --> AI
```

## 2. 数据库读写分离架构

```mermaid
graph LR
    subgraph 客户端
        App[应用服务]
    end
    
    subgraph 代理层
        Proxy[MySQL Router<br/>ProxySQL]
    end
    
    subgraph 写库
        Master[(主库<br/>Write)]
    end
    
    subgraph 读库
        Slave1[(备库1<br/>Read)]
        Slave2[(备库2<br/>Read)]
    end
    
    subgraph 分片层[可选 ShardingSphere]
        Shard[分库分表]
    end
    
    App --> Proxy
    Proxy -->|写| Master
    Proxy -->|读| Slave1
    Proxy -->|读| Slave2
    Master -->|Binlog| Slave1
    Master -->|Binlog| Slave2
    Shard -.->|按部门/时间| Master
```

## 3. Redis 缓存架构

```mermaid
graph TB
    subgraph 应用层
        Service[业务服务]
    end
    
    subgraph L1缓存["L1 本地缓存 (Caffeine)"]
        Local[本地缓存<br/>100MB<br/>TTL: 5min]
    end
    
    subgraph L2缓存["L2 分布式缓存 (Redis)"]
        RedisMaster[(Redis Master)]
        RedisSlave1[(Redis Slave1)]
        RedisSlave2[(Redis Slave2)]
    end
    
    Service -->|先查| Local
    Local -->|未命中| RedisMaster
    RedisMaster -->|主从同步| RedisSlave1
    RedisMaster -->|主从同步| RedisSlave2
    Service -->|读| RedisSlave1
    Service -->|读| RedisSlave2
    Service -->|写| RedisMaster
```

## 4. 微服务模块划分

```mermaid
graph TB
    subgraph 前端["前端应用"]
        Web[主后台 Web]
        Mobile[移动端 H5/App]
        AIChat[AI对话框<br/>侧边栏]
        ReportView[报表预览]
        FlowDesign[流程设计器]
    end
    
    subgraph BFF["BFF 层"]
        WebBFF[Web BFF]
        MobileBFF[Mobile BFF]
        AIBFF[AI BFF]
    end
    
    subgraph Gateway["API Gateway"]
        GW[Spring Cloud<br/>Gateway]
    end
    
    subgraph 系统域["系统域"]
        Sys[系统服务<br/>/system]
        User[用户服务<br/>/user]
    end
    
    subgraph 业务域["业务域"]
        Flow[审批服务<br/>/workflow]
        Inv[发票服务<br/>/invoice]
        Trip[行程单服务<br/>/trip]
        Rpt[报表服务<br/>/report]
    end
    
    subgraph 集成域["集成域"]
        AI[AI服务<br/>/ai]
        Mail[邮箱服务<br/>/mail]
    end
    
    subgraph 扩展域["扩展域"]
        Form[表单服务<br/>/form]
        KM[知识库<br/>/knowledge]
        Attend[考勤服务<br/>/attendance]
        Contract[合同服务<br/>/contract]
    end
    
    Web --> WebBFF
    Mobile --> MobileBFF
    AIChat --> AIBFF
    ReportView --> WebBFF
    FlowDesign --> WebBFF
    
    WebBFF --> GW
    MobileBFF --> GW
    AIBFF --> GW
    
    GW --> Sys
    GW --> User
    GW --> Flow
    GW --> Inv
    GW --> Trip
    GW --> Rpt
    GW --> AI
    GW --> Mail
    GW --> Form
    GW --> KM
    GW --> Attend
    GW --> Contract
```

## 5. 高并发部署架构

```mermaid
graph TB
    subgraph 用户["用户层"]
        U1[PC]
        U2[Mobile]
        U3[小程序]
    end
    
    subgraph 接入层["CDN / 负载均衡"]
        CDN[CDN]
        SLB[SLB 负载均衡]
    end
    
    subgraph K8s["Kubernetes 集群"]
        subgraph Master["Master 节点 × 3"]
            M1[API Server]
            M2[etcd]
            M3[Scheduler]
        end
        
        subgraph Worker1["Worker 节点 1"]
            S1_1[oa-system]
            S1_2[ai-service]
            S1_3[workflow]
        end
        
        subgraph Worker2["Worker 节点 2"]
            S2_1[oa-system]
            S2_2[ai-service]
            S2_3[invoice]
        end
        
        subgraph Worker3["Worker 节点 3"]
            S3_1[workflow]
            S3_2[n8n]
            S3_3[form-service]
        end
        
        subgraph Worker4["Worker 节点 4"]
            S4_1[report-service]
            S4_2[ai-service]
            S4_3[mail-service]
        end
    end
    
    subgraph 存储层["存储层"]
        MySQL[(MySQL<br/>Cluster)]
        Redis[(Redis<br/>Cluster)]
        MinIO[(MinIO<br/>Cluster)]
    end
    
    subgraph 监控层["监控/日志"]
        Prometheus[Prometheus]
        Grafana[Grafana]
        ELK[ELK Stack]
    end
    
    U1 --> CDN
    U2 --> SLB
    U3 --> SLB
    CDN --> SLB
    SLB --> S1_1
    SLB --> S2_1
    SLB --> S3_1
    SLB --> S4_1
    
    S1_1 --> MySQL
    S2_1 --> MySQL
    S3_1 --> MySQL
    S4_1 --> MySQL
    
    S1_2 --> Redis
    S2_2 --> Redis
    S3_2 --> Redis
    S4_2 --> Redis
    
    S1_3 --> MinIO
    S2_3 --> MinIO
    S3_3 --> MinIO
    
    S1_1 --> Prometheus
    S2_1 --> Prometheus
    S3_1 --> Prometheus
    Prometheus --> Grafana
    
    S1_1 --> ELK
    S2_1 --> ELK
    S3_1 --> ELK
```

## 6. 异步任务处理流程

```mermaid
sequenceDiagram
    participant Client as 客户端
    participant API as API网关
    participant Service as 业务服务
    participant MQ as RocketMQ
    participant Worker as 消费者
    participant DB as 数据库
    participant Notify as 通知服务
    
    Client->>API: 1. 发起请求
    API->>Service: 2. 同步处理
    Service->>DB: 3. 写主库
    Service->>MQ: 4. 发送消息
    API->>Client: 5. 立即返回任务ID
    
    MQ->>Worker: 6. 异步消费
    Worker->>DB: 7. 更新状态
    Worker->>Notify: 8. 发送通知
    Notify->>Client: 9. 推送结果
```

## 7. 安全架构

```mermaid
graph TB
    subgraph 边界层["边界安全"]
        WAF[WAF防火墙]
        DDoS[DDoS防护]
    end
    
    subgraph 网络层["网络安全"]
        VPC[VPC私有网络]
        TLS[TLS 1.3]
    end
    
    subgraph 认证层["认证授权"]
        JWT[JWT Token]
        OAuth[OAuth2]
        RBAC[RBAC权限]
    end
    
    subgraph 数据层["数据安全"]
        AES[AES加密]
        Mask[数据脱敏]
        Backup[备份恢复]
    end
    
    subgraph 应用层["应用安全"]
        SQL[SQL注入防护]
        XSS[XSS过滤]
        CSRF[CSRF Token]
    end
    
    subgraph 审计层["审计监控"]
        Audit[操作审计]
        Alert[告警通知]
        Log[日志留存]
    end
    
    WAF --> DDoS
    DDoS --> VPC
    VPC --> TLS
    TLS --> JWT
    TLS --> OAuth
    JWT --> RBAC
    RBAC --> AES
    AES --> Mask
    Mask --> Backup
    Backup --> SQL
    SQL --> XSS
    XSS --> CSRF
    CSRF --> Audit
    Audit --> Alert
    Alert --> Log
```

---

*使用说明：将代码块内容复制到支持 Mermaid 的编辑器中即可渲染*
