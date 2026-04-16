package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aioa.knowledge.config.MilvusConfig;
import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.EmbeddingServiceFactory;
import com.aioa.knowledge.service.VectorService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 向量服务实现（Milvus集成）
 */
@Slf4j
@Service
public class VectorServiceImpl implements VectorService {
    
    @Autowired
    private MilvusServiceClient milvusClient;
    
    @Autowired
    private MilvusConfig milvusConfig;
    
    @Autowired
    private EmbeddingServiceFactory embeddingServiceFactory;
    
    // 向量字段名
    private static final String VECTOR_FIELD = "embedding";
    private static final String ID_FIELD = "id";
    private static final String DOC_ID_FIELD = "doc_id";
    private static final String TITLE_FIELD = "title";
    private static final String CONTENT_FIELD = "content";
    
    @Override
    public List<Float> generateEmbedding(String text) {
        return embeddingServiceFactory.getEmbeddingService().generateEmbedding(text);
    }
    
    @Override
    public String storeVector(KnowledgeDoc doc) {
        if (doc == null || doc.getContent() == null) {
            log.error("文档内容为空，无法生成向量");
            return null;
        }
        
        // 生成向量ID
        String vectorId = IdUtil.fastSimpleUUID();
        
        try {
            // 创建集合（如果不存在）
            createCollectionIfNotExists();
            
            // 生成文档向量
            List<Float> embedding = generateEmbedding(doc.getContent());
            if (embedding.isEmpty()) {
                log.error("向量生成失败，无法存储");
                return null;
            }
            
            // 准备插入数据
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field(ID_FIELD, DataType.Int64, List.of(Long.parseLong(vectorId))));
            fields.add(new InsertParam.Field(DOC_ID_FIELD, DataType.Int64, List.of(doc.getId())));
            fields.add(new InsertParam.Field(TITLE_FIELD, DataType.VarChar, List.of(doc.getTitle())));
            fields.add(new InsertParam.Field(CONTENT_FIELD, DataType.VarChar, List.of(doc.getContent())));
            fields.add(new InsertParam.Field(VECTOR_FIELD, DataType.FloatVector, List.of(embedding)));
            
            // 插入向量
            InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(milvusConfig.getCollectionName())
                .withFields(fields)
                .build();
            
            R<MutationResult> response = milvusClient.insert(insertParam);
            if (response.getStatus() == R.Status.Success.getCode()) {
                log.info("向量存储成功: vectorId={}, docId={}", vectorId, doc.getId());
                
                // 更新文档的向量ID
                doc.setVectorId(vectorId);
                
                return vectorId;
            } else {
                log.error("向量存储失败: {}", response.getMessage());
                return null;
            }
            
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
            // 先删除旧向量
            deleteVector(vectorId);
            
            // 再存储新向量
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
            // 构建删除条件
            String deleteExpr = String.format("%s == %s", ID_FIELD, vectorId);
            
            R<MutationResult> response = milvusClient.delete(
                DeleteParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .withExpr(deleteExpr)
                    .build()
            );
            
            if (response.getStatus() == R.Status.Success.getCode()) {
                log.info("向量删除成功: vectorId={}", vectorId);
                return true;
            } else {
                log.error("向量删除失败: {}", response.getMessage());
                return false;
            }
            
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
            // 生成查询向量
            List<Float> queryEmbedding = generateEmbedding(query);
            if (queryEmbedding.isEmpty()) {
                log.error("查询向量生成失败");
                return new ArrayList<>();
            }
            
            // 构建搜索参数
            List<String> outputFields = Arrays.asList(ID_FIELD, DOC_ID_FIELD, TITLE_FIELD);
            List<List<Float>> vectors = Arrays.asList(queryEmbedding);
            
            SearchParam searchParam = SearchParam.newBuilder()
                .withCollectionName(milvusConfig.getCollectionName())
                .withMetricType(MetricType.IP)  // 内积（余弦相似度）
                .withVectorFieldName(VECTOR_FIELD)
                .withVectors(vectors)
                .withTopK(topK)
                .withOutFields(outputFields)
                .withConsistencyLevel(ConsistencyLevelEnum.STRONG)
                .build();
            
            // 执行搜索
            R<SearchResults> response = milvusClient.search(searchParam);
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("向量搜索失败: {}", response.getMessage());
                return new ArrayList<>();
            }
            
            // 解析搜索结果
            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData());
            List<String> docIds = new ArrayList<>();
            
            for (int i = 0; i < vectors.size(); i++) {
                List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(i);
                for (SearchResultsWrapper.IDScore score : scores) {
                    Object docIdObj = score.get(TITLE_FIELD);
                    if (docIdObj != null) {
                        docIds.add(docIdObj.toString());
                    }
                }
            }
            
            log.info("向量搜索完成: query='{}', topK={}, 结果数={}", 
                query, topK, docIds.size());
            return docIds;
            
        } catch (Exception e) {
            log.error("向量搜索异常", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<String> hybridSearch(String query, int topK) {
        // 混合搜索：结合向量搜索和关键词搜索
        // 这里简化实现，只返回向量搜索结果
        return vectorSearch(query, topK);
    }
    
    @Override
    public boolean createCollectionIfNotExists() {
        try {
            // 检查集合是否存在
            R<DescribeCollectionResponse> describeResponse = milvusClient.describeCollection(
                DescribeCollectionParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .build()
            );
            
            if (describeResponse.getStatus() == R.Status.Success.getCode()) {
                log.info("集合已存在: {}", milvusConfig.getCollectionName());
                return true;
            }
            
            // 集合不存在，创建新集合
            log.info("创建新集合: {}", milvusConfig.getCollectionName());
            
            // 定义字段
            List<FieldType> fields = Arrays.asList(
                FieldType.newBuilder()
                    .withName(ID_FIELD)
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(false)
                    .build(),
                FieldType.newBuilder()
                    .withName(DOC_ID_FIELD)
                    .withDataType(DataType.Int64)
                    .build(),
                FieldType.newBuilder()
                    .withName(TITLE_FIELD)
                    .withDataType(DataType.VarChar)
                    .withMaxLength(255)
                    .build(),
                FieldType.newBuilder()
                    .withName(CONTENT_FIELD)
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build(),
                FieldType.newBuilder()
                    .withName(VECTOR_FIELD)
                    .withDataType(DataType.FloatVector)
                    .withDimension(milvusConfig.getVectorDim())
                    .build()
            );
            
            // 创建集合
            CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(milvusConfig.getCollectionName())
                .withFieldTypes(fields)
                .build();
            
            R<RpcStatus> createResponse = milvusClient.createCollection(createParam);
            if (createResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("集合创建失败: {}", createResponse.getMessage());
                return false;
            }
            
            // 创建索引
            CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(milvusConfig.getCollectionName())
                .withFieldName(VECTOR_FIELD)
                .withIndexType(IndexType.IVF_FLAT)
                .withMetricType(MetricType.IP)
                .withExtraParam("{\"nlist\":1024}")
                .build();
            
            R<RpcStatus> indexResponse = milvusClient.createIndex(indexParam);
            if (indexResponse.getStatus() != R.Status.Success.getCode()) {
                log.error("索引创建失败: {}", indexResponse.getMessage());
                // 继续执行，索引可以在后续创建
            }
            
            // 加载集合到内存
            milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .build()
            );
            
            log.info("集合创建并加载成功: {}", milvusConfig.getCollectionName());
            return true;
            
        } catch (Exception e) {
            log.error("集合创建异常", e);
            return false;
        }
    }
    
    @Override
    public boolean healthCheck() {
        try {
            // 检查Milvus连接
            R<GetVersionResponse> versionResponse = milvusClient.getVersion(
                GetVersionParam.newBuilder().build()
            );
            
            if (versionResponse.getStatus() == R.Status.Success.getCode()) {
                log.info("Milvus连接正常，版本: {}", versionResponse.getData().getVersion());
                return true;
            } else {
                log.error("Milvus连接失败: {}", versionResponse.getMessage());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Milvus健康检查异常", e);
            return false;
        }
    }
    
    /**
     * 批量存储向量
     */
    public List<String> batchStoreVectors(List<KnowledgeDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> vectorIds = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            String vectorId = storeVector(doc);
            if (vectorId != null) {
                vectorIds.add(vectorId);
            }
        }
        
        log.info("批量存储向量完成，成功数: {}/{}", vectorIds.size(), docs.size());
        return vectorIds;
    }
    
    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            R<GetCollectionStatisticsResponse> response = milvusClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                    .withCollectionName(milvusConfig.getCollectionName())
                    .build()
            );
            
            if (response.getStatus() == R.Status.Success.getCode()) {
                stats.put("rowCount", response.getData().getStats(0).getValue());
                stats.put("status", "healthy");
            } else {
                stats.put("rowCount", 0);
                stats.put("status", "unavailable");
                stats.put("error", response.getMessage());
            }
            
        } catch (Exception e) {
            stats.put("rowCount", 0);
            stats.put("status", "error");
            stats.put("error", e.getMessage());
        }
        
        stats.put("collectionName", milvusConfig.getCollectionName());
        stats.put("vectorDim", milvusConfig.getVectorDim());
        stats.put("embeddingService", embeddingServiceFactory.getEmbeddingService().getConfigInfo());
        
        return stats;
    }
}