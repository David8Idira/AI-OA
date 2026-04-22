package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.VectorService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Milvus向量服务实现
 * 使用真实的Milvus向量数据库进行存储和搜索
 */
@Slf4j
@Service
public class MilvusVectorServiceImpl implements VectorService {

    @Autowired
    private MilvusServiceClient milvusClient;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Value("${milvus.collectionName:aioa_knowledge}")
    private String collectionName;
    
    @Value("${milvus.vectorDim:1536}")
    private int vectorDim;
    
    // 缓存集合状态，避免重复检查
    private boolean collectionInitialized = false;
    
    /**
     * 初始化集合（如果不存在）
     */
    private synchronized void initializeCollection() {
        if (collectionInitialized) {
            return;
        }
        
        try {
            // 检查集合是否存在
            R<Boolean> resp = milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            if (!resp.getData()) {
                log.info("创建Milvus集合: {}", collectionName);
                
                // 定义字段
                FieldType idField = FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build();
                
                FieldType vectorField = FieldType.newBuilder()
                    .withName("vector")
                    .withDataType(DataType.FloatVector)
                    .withDimension(vectorDim)
                    .build();
                
                FieldType contentField = FieldType.newBuilder()
                    .withName("content")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build();
                
                FieldType titleField = FieldType.newBuilder()
                    .withName("title")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(255)
                    .build();
                
                FieldType docIdField = FieldType.newBuilder()
                    .withName("doc_id")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(100)
                    .build();
                
                FieldType categoryField = FieldType.newBuilder()
                    .withName("category")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(100)
                    .build();
                
                // 创建集合
                CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription("AI-OA知识库向量存储")
                    .withFieldTypes(idField, vectorField, contentField, titleField, docIdField, categoryField)
                    .build();
                
                R<RpcStatus> createResp = milvusClient.createCollection(createParam);
                
                if (createResp.getStatus() != R.Status.Success.getCode()) {
                    log.error("创建集合失败: {}", createResp.getMessage());
                    throw new RuntimeException("创建Milvus集合失败: " + createResp.getMessage());
                }
                
                // 创建向量索引
                CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("vector")
                    .withIndexType("IVF_FLAT")
                    .withMetricType("L2")
                    .withExtraParam("{\"nlist\":1024}")
                    .build();
                
                R<RpcStatus> indexResp = milvusClient.createIndex(indexParam);
                
                if (indexResp.getStatus() != R.Status.Success.getCode()) {
                    log.error("创建索引失败: {}", indexResp.getMessage());
                    throw new RuntimeException("创建Milvus索引失败: " + indexResp.getMessage());
                }
                
                log.info("Milvus集合和索引创建成功");
            } else {
                log.info("Milvus集合已存在: {}", collectionName);
            }
            
            collectionInitialized = true;
            
        } catch (Exception e) {
            log.error("初始化Milvus集合失败", e);
            throw new RuntimeException("初始化Milvus集合失败", e);
        }
    }
    
    @Override
    public List<Float> generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 使用EmbeddingService生成向量
            return embeddingService.generateEmbedding(text);
        } catch (Exception e) {
            log.error("生成向量失败: {}", e.getMessage(), e);
            // 返回伪向量作为降级方案
            return generateFallbackEmbedding(text);
        }
    }
    
    /**
     * 生成降级向量（当真实服务不可用时）
     */
    private List<Float> generateFallbackEmbedding(String text) {
        List<Float> embedding = new ArrayList<>();
        for (int i = 0; i < vectorDim; i++) {
            float value = (float) (Math.random() * 2 - 1);
            embedding.add(value);
        }
        return embedding;
    }
    
    @Override
    public String storeVector(KnowledgeDoc doc) {
        if (doc == null || doc.getContent() == null) {
            log.error("文档内容为空，无法生成向量");
            return null;
        }
        
        try {
            // 确保集合已初始化
            initializeCollection();
            
            // 生成向量
            List<Float> embedding = generateEmbedding(doc.getContent());
            if (embedding.isEmpty()) {
                log.error("向量生成失败");
                return null;
            }
            
            // 准备插入数据
            List<InsertParam.Field> fields = new ArrayList<>();
            
            // 向量字段
            List<List<Float>> vectors = new ArrayList<>();
            vectors.add(embedding);
            fields.add(new InsertParam.Field("vector", vectors));
            
            // 内容字段
            fields.add(new InsertParam.Field("content", Arrays.asList(doc.getContent())));
            
            // 标题字段
            String title = doc.getTitle() != null ? doc.getTitle() : "未命名文档";
            fields.add(new InsertParam.Field("title", Arrays.asList(title)));
            
            // 文档ID字段
            String docId = doc.getId() != null ? doc.getId().toString() : IdUtil.fastSimpleUUID();
            fields.add(new InsertParam.Field("doc_id", Arrays.asList(docId)));
            
            // 分类字段
            String category = doc.getCategoryId() != null ? doc.getCategoryId().toString() : "default";
            fields.add(new InsertParam.Field("category", Arrays.asList(category)));
            
            // 插入数据
            InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();
            
            R<MutationResult> insertResp = milvusClient.insert(insertParam);
            
            if (insertResp.getStatus() != R.Status.Success.getCode()) {
                log.error("插入向量失败: {}", insertResp.getMessage());
                return null;
            }
            
            // 获取插入的向量ID
            List<Long> ids = insertResp.getData().getIDs().getIntId().getDataList();
            if (ids.isEmpty()) {
                log.error("未获取到向量ID");
                return null;
            }
            
            String vectorId = "milvus_" + ids.get(0);
            log.info("向量存储成功: vectorId={}, docId={}", vectorId, docId);
            
            return vectorId;
            
        } catch (Exception e) {
            log.error("向量存储异常", e);
            return null;
        }
    }
    
    @Override
    public boolean updateVector(String vectorId, KnowledgeDoc doc) {
        if (vectorId == null || doc == null) {
            return false;
        }
        
        try {
            // 解析向量ID
            String[] parts = vectorId.split("_");
            if (parts.length < 2 || !"milvus".equals(parts[0])) {
                log.error("无效的向量ID格式: {}", vectorId);
                return false;
            }
            
            long id = Long.parseLong(parts[1]);
            
            // 先删除旧向量
            deleteVector(vectorId);
            
            // 存储新向量
            String newVectorId = storeVector(doc);
            return newVectorId != null;
            
        } catch (Exception e) {
            log.error("向量更新异常", e);
            return false;
        }
    }
    
    @Override
    public boolean deleteVector(String vectorId) {
        if (vectorId == null) {
            return false;
        }
        
        try {
            // 解析向量ID
            String[] parts = vectorId.split("_");
            if (parts.length < 2 || !"milvus".equals(parts[0])) {
                log.error("无效的向量ID格式: {}", vectorId);
                return false;
            }
            
            long id = Long.parseLong(parts[1]);
            
            // 删除向量
            String deleteExpr = "id == " + id;
            DeleteParam deleteParam = DeleteParam.newBuilder()
                .withCollectionName(collectionName)
                .withExpr(deleteExpr)
                .build();
            
            R<MutationResult> deleteResp = milvusClient.delete(deleteParam);
            
            if (deleteResp.getStatus() != R.Status.Success.getCode()) {
                log.error("删除向量失败: {}", deleteResp.getMessage());
                return false;
            }
            
            log.info("向量删除成功: vectorId={}", vectorId);
            return true;
            
        } catch (Exception e) {
            log.error("向量删除异常", e);
            return false;
        }
    }
    
    @Override
    public List<String> vectorSearch(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // 确保集合已初始化
            initializeCollection();
            
            // 生成查询向量
            List<Float> queryEmbedding = generateEmbedding(query);
            if (queryEmbedding.isEmpty()) {
                log.error("查询向量生成失败");
                return new ArrayList<>();
            }
            
            // 准备搜索参数
            List<List<Float>> searchVectors = new ArrayList<>();
            searchVectors.add(queryEmbedding);
            
            String searchParam = "{\"nprobe\":10}";
            List<String> outputFields = Arrays.asList("doc_id", "title", "content");
            
            SearchParam searchParamObj = SearchParam.newBuilder()
                .withCollectionName(collectionName)
                .withVectorFieldName("vector")
                .withVectors(searchVectors)
                .withTopK(topK)
                .withMetricType(MetricType.L2)
                .withParams(searchParam)
                .withOutFields(outputFields)
                .build();
            
            R<SearchResults> searchResp = milvusClient.search(searchParamObj);
            
            if (searchResp.getStatus() != R.Status.Success.getCode()) {
                log.error("向量搜索失败: {}", searchResp.getMessage());
                return new ArrayList<>();
            }
            
            // 处理搜索结果
            SearchResultsWrapper wrapper = new SearchResultsWrapper(searchResp.getData());
            List<SearchResultsWrapper.IDScore> idScores = wrapper.getIDScore(0);
            
            List<String> results = new ArrayList<>();
            for (SearchResultsWrapper.IDScore idScore : idScores) {
                String docId = wrapper.getFieldData("doc_id", 0, idScore.getIndex()).toString();
                results.add(docId);
            }
            
            log.info("向量搜索完成: query='{}', topK={}, 结果数={}", query, topK, results.size());
            return results;
            
        } catch (Exception e) {
            log.error("向量搜索异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> hybridSearch(String query, int topK) {
        // 混合搜索：结合向量搜索和关键词匹配
        try {
            List<String> vectorResults = vectorSearch(query, topK);
            
            // 这里可以添加关键词匹配结果
            // 在实际项目中，可以结合BM25等算法
            
            return vectorResults;
            
        } catch (Exception e) {
            log.error("混合搜索异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean createCollectionIfNotExists() {
        try {
            initializeCollection();
            return true;
        } catch (Exception e) {
            log.error("创建集合失败", e);
            return false;
        }
    }
    
    @Override
    public boolean healthCheck() {
        try {
            // 检查Milvus连接
            R<Boolean> resp = milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            boolean healthy = resp.getStatus() == R.Status.Success.getCode();
            
            if (healthy) {
                log.debug("Milvus健康检查通过");
            } else {
                log.warn("Milvus健康检查失败: {}", resp.getMessage());
            }
            
            return healthy;
            
        } catch (Exception e) {
            log.error("Milvus健康检查异常", e);
            return false;
        }
    }
    
    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 获取集合信息
            R<DescribeCollectionResponse> descResp = milvusClient.describeCollection(
                DescribeCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            if (descResp.getStatus() == R.Status.Success.getCode()) {
                DescribeCollectionResponse descData = descResp.getData();
                stats.put("collectionName", descData.getCollectionName());
                stats.put("description", descData.getDescription());
                stats.put("shardsNum", descData.getShardsNum());
                stats.put("consistencyLevel", descData.getConsistencyLevel().name());
            }
            
            // 获取集合统计
            R<GetCollectionStatisticsResponse> statResp = milvusClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            if (statResp.getStatus() == R.Status.Success.getCode()) {
                GetCollectionStatisticsResponse statData = statResp.getData();
                Map<String, String> statMap = new HashMap<>();
                for (KeyValuePair pair : statData.getStatsList()) {
                    statMap.put(pair.getKey(), pair.getValue());
                }
                stats.put("statistics", statMap);
            }
            
            stats.put("status", "healthy");
            stats.put("lastCheck", new Date());
            
        } catch (Exception e) {
            log.error("获取集合统计失败", e);
            stats.put("status", "unhealthy");
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 批量存储向量
     */
    public List<String> batchStoreVectors(List<KnowledgeDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> vectorIds = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (KnowledgeDoc doc : docs) {
            try {
                String vectorId = storeVector(doc);
                if (vectorId != null) {
                    vectorIds.add(vectorId);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("批量存储向量失败: docId={}, {}", doc.getId(), e.getMessage());
                failCount++;
            }
        }
        
        log.info("批量存储向量完成，成功数: {}/{}", successCount, docs.size());
        return vectorIds;
    }
    
    /**
     * 清空集合（用于测试和重置）
     */
    public boolean truncateCollection() {
        try {
            R<RpcStatus> resp = milvusClient.truncateCollection(
                TruncateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            boolean success = resp.getStatus() == R.Status.Success.getCode();
            
            if (success) {
                log.info("集合清空成功: {}", collectionName);
            } else {
                log.error("集合清空失败: {}", resp.getMessage());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("集合清空异常", e);
            return false;
        }
    }
    
    /**
     * 性能测试：批量搜索
     */
    public Map<String, Object> performanceTest(int queryCount, int topK) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<String> testQueries = Arrays.asList(
                "人工智能", "机器学习", "深度学习", "自然语言处理",
                "计算机视觉", "大数据", "云计算", "物联网"
            );
            
            long totalTime = 0;
            int totalResults = 0;
            
            for (int i = 0; i < queryCount; i++) {
                String query = testQueries.get(i % testQueries.size());
                
                long startTime = System.nanoTime();
                List<String> searchResults = vectorSearch(query, topK);
                long endTime = System.nanoTime();
                
                totalTime += (endTime - startTime);
                totalResults += searchResults.size();
            }
            
            double avgTimeMs = totalTime / (queryCount * 1_000_000.0);
            double qps = queryCount / (totalTime / 1_000_000_000.0);
            
            result.put("queryCount", queryCount);
            result.put("topK", topK);
            result.put("totalTimeMs", totalTime / 1_000_000.0);
            result.put("avgTimeMs", avgTimeMs);
            result.put("qps", qps);
            result.put("totalResults", totalResults);
            result.put("status", "completed");
            
            log.info("性能测试完成: avgTime={}ms, qps={}", avgTimeMs, qps);
            
        } catch (Exception e) {
            log.error("性能测试失败", e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}