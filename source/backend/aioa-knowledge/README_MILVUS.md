# AI-OA 本地知识库建设指南

## 方案B：Milvus向量数据库集成

### 1. 架构概述

```
用户请求 → AI-OA应用 → 知识库服务 → Milvus向量搜索 → OpenAI嵌入 → 返回结果
```

### 2. 已实现功能

#### 2.1 核心组件
- ✅ **Milvus Java SDK集成**：版本2.3.6
- ✅ **向量服务接口**：生成、存储、搜索、更新、删除
- ✅ **OpenAI嵌入服务**：支持text-embedding-ada-002
- ✅ **本地嵌入服务**：支持本地模型（BGE-M3等）
- ✅ **RAG检索增强**：基于向量的语义搜索

#### 2.2 技术特性
- 向量维度：1536（OpenAI标准）
- 搜索算法：IVF_FLAT + IP（内积/余弦相似度）
- 批量处理：支持批量文档向量化
- 事务支持：数据库与向量存储的一致性

### 3. 安装与配置

#### 3.1 环境要求
- Java 17+
- Maven 3.8+
- Docker 20.10+
- MySQL 8.0+
- Redis 6.0+

#### 3.2 Milvus安装
```bash
# 进入docker目录
cd /root/workspace/AI-OA/source/backend/aioa-knowledge/docker

# 启动Milvus
docker-compose -f milvus-docker-compose.yml up -d

# 验证安装
curl http://localhost:19530/version
```

#### 3.3 应用配置
1. 复制配置文件：
```bash
cp src/main/resources/application-local.yml src/main/resources/application.yml
```

2. 更新配置：
```yaml
# OpenAI API密钥
ai:
  embedding:
    openai:
      api-key: "sk-xxx"

# 数据库配置
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ai_oa
    username: root
    password: root
```

### 4. API接口

#### 4.1 知识库管理
```
GET    /api/knowledge/search?keyword=xxx         # 关键词搜索
GET    /api/knowledge/semantic?query=xxx&topN=5  # 语义搜索
POST   /api/knowledge/doc                        # 创建文档
GET    /api/knowledge/doc/{id}                   # 获取文档
GET    /api/knowledge/categories                 # 分类列表
GET    /api/knowledge/stats                      # 统计信息
```

#### 4.2 向量功能
```
GET    /api/knowledge/vector-status              # 向量服务状态
GET    /api/knowledge/rag?query=xxx              # RAG检索
POST   /api/knowledge/batch-import               # 批量导入
```

### 5. 使用示例

#### 5.1 创建知识文档
```java
KnowledgeDoc doc = new KnowledgeDoc();
doc.setTitle("如何配置Spring Boot");
doc.setContent("Spring Boot配置需要application.yml文件...");
doc.setSummary("Spring Boot配置指南");
doc.setDocType("article");
doc.setStatus("published");

knowledgeService.createDoc(doc);  // 自动生成向量
```

#### 5.2 语义搜索
```java
// 向量搜索
List<KnowledgeDoc> results = knowledgeService.semanticSearch("Spring配置", 5);

// RAG检索
String context = knowledgeService.ragRetrieve("如何配置数据库连接");
```

#### 5.3 批量处理
```java
List<KnowledgeDoc> docs = // 从文件或数据库加载
for (KnowledgeDoc doc : docs) {
    knowledgeService.createDoc(doc);  // 自动生成并存储向量
}
```

### 6. 测试与验证

#### 6.1 单元测试
```bash
mvn test -Dtest=KnowledgeServiceTest
```

#### 6.2 集成测试
```bash
# 启动所有服务
docker-compose up -d
mvn spring-boot:run

# 测试API
curl http://localhost:8084/api/knowledge/vector-status
curl http://localhost:8084/api/knowledge/semantic?query=Spring&topN=3
```

#### 6.3 性能测试
```bash
# 批量导入测试
curl -X POST http://localhost:8084/api/knowledge/batch-import \
  -H "Content-Type: application/json" \
  -d @test-docs.json
```

### 7. 监控与维护

#### 7.1 健康检查
```bash
# Milvus健康
curl http://localhost:19530/health

# 应用健康
curl http://localhost:8084/actuator/health

# 向量服务状态
curl http://localhost:8084/api/knowledge/vector-status
```

#### 7.2 日志查看
```bash
# 应用日志
tail -f logs/aioa-knowledge.log

# Milvus日志
docker logs milvus-standalone -f

# 向量化日志
grep "向量" logs/aioa-knowledge.log
```

#### 7.3 备份恢复
```bash
# Milvus备份
docker exec milvus-standalone milvus backup

# 数据导出
curl -X GET http://localhost:8084/api/knowledge/export
```

### 8. 故障排除

#### 8.1 常见问题

**Q: Milvus连接失败**
```bash
# 检查服务状态
docker ps | grep milvus

# 检查端口
netstat -tlnp | grep 19530

# 重启服务
docker-compose down && docker-compose up -d
```

**Q: 向量生成失败**
```yaml
# 检查OpenAI配置
ai:
  embedding:
    openai:
      api-key: "正确密钥"
      base-url: "https://api.openai.com/v1"
```

**Q: 搜索性能差**
```java
// 调整搜索参数
SearchParam searchParam = SearchParam.newBuilder()
    .withTopK(10)  // 减少返回数量
    .withNProbe(10)  // 调整搜索精度
    .build();
```

#### 8.2 性能优化
1. **向量索引优化**：使用HNSW索引提高搜索速度
2. **批量处理**：使用batchGenerateEmbedding减少API调用
3. **缓存策略**：Redis缓存常用查询结果
4. **异步处理**：向量生成使用异步任务

### 9. 扩展开发

#### 9.1 添加新模型
```java
// 实现新的EmbeddingService
@Component
public class CustomEmbeddingService {
    public List<Float> generateEmbedding(String text) {
        // 调用自定义模型
    }
}

// 更新工厂类
EmbeddingServiceFactory.addService("custom", customService);
```

#### 9.2 支持多语言
```java
// 多语言向量生成
public List<Float> generateMultilingualEmbedding(String text, String language) {
    // 根据语言选择不同模型
}
```

#### 9.3 实时索引
```java
// 监听文档变化，实时更新向量
@EventListener
public void handleDocUpdate(DocUpdatedEvent event) {
    vectorService.updateVector(event.getVectorId(), event.getDoc());
}
```

### 10. 安全考虑

1. **API密钥管理**：使用环境变量或密钥管理服务
2. **访问控制**：实现基于角色的权限控制
3. **数据加密**：敏感数据加密存储
4. **审计日志**：记录所有向量操作

---

**版本**: 1.0.0  
**更新日期**: 2026-04-16  
**维护者**: A1 (战略规划与行业研究专家)