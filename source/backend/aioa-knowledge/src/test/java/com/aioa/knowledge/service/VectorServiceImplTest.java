package com.aioa.knowledge.service;

import com.aioa.knowledge.service.impl.VectorServiceImpl;
import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.QueryResults;
import io.milvus.grpc.SearchResults;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * VectorServiceImpl单元测试
 * 
 * 测试知识库向量服务的核心功能：
 * 1. 向量集合管理
 * 2. 向量存储和索引
 * 3. 语义搜索功能
 * 4. RAG检索增强生成
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VectorServiceImpl 单元测试")
class VectorServiceImplTest {

    @Mock
    private MilvusServiceClient milvusClient;

    @Mock
    private EmbeddingService embeddingService;

    private VectorServiceImpl vectorService;

    @BeforeEach
    void setUp() {
        vectorService = new VectorServiceImpl();
        vectorService.setMilvusClient(milvusClient);
        vectorService.setEmbeddingService(embeddingService);
    }

    @AfterEach
    void tearDown() {
        reset(milvusClient, embeddingService);
    }

    @Test
    @DisplayName("测试创建向量集合 - 成功场景")
    void testCreateCollection_Success() {
        // 准备测试数据
        String collectionName = "test_collection";
        
        // 模拟Milvus响应
        R<Boolean> hasCollectionResponse = R.success(false);
        R<Void> createCollectionResponse = R.success(null);
        
        when(milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()))
                .thenReturn(hasCollectionResponse);
        
        when(milvusClient.createCollection(any(CreateCollectionParam.class)))
                .thenReturn(createCollectionResponse);

        // 执行创建集合
        boolean result = vectorService.createCollection(collectionName);

        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(milvusClient).hasCollection(eq(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()));
        
        verify(milvusClient).createCollection(argThat(param ->
                param.getCollectionName().equals(collectionName)
        ));
    }

    @Test
    @DisplayName("测试创建向量集合 - 集合已存在")
    void testCreateCollection_AlreadyExists() {
        // 准备测试数据
        String collectionName = "existing_collection";
        
        // 模拟集合已存在
        R<Boolean> hasCollectionResponse = R.success(true);
        
        when(milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()))
                .thenReturn(hasCollectionResponse);

        // 执行创建集合
        boolean result = vectorService.createCollection(collectionName);

        // 验证结果
        assertTrue(result); // 集合已存在，返回成功
        
        // 验证方法调用
        verify(milvusClient).hasCollection(eq(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()));
        
        verify(milvusClient, never()).createCollection(any());
    }

    @Test
    @DisplayName("测试添加文档向量 - 成功场景")
    void testAddDocumentVector_Success() {
        // 准备测试数据
        String collectionName = "test_collection";
        String docId = "doc-123";
        String content = "这是一个测试文档内容";
        float[] vector = new float[]{0.1f, 0.2f, 0.3f, 0.4f};
        
        // 模拟向量化响应
        when(embeddingService.generateEmbedding(content))
                .thenReturn(vector);
        
        // 模拟Milvus插入响应
        MutationResult mutationResult = MutationResult.newBuilder()
                .setInsertCnt(1)
                .build();
        
        R<MutationResult> insertResponse = R.success(mutationResult);
        
        when(milvusClient.insert(any(InsertParam.class)))
                .thenReturn(insertResponse);

        // 执行添加文档向量
        boolean result = vectorService.addDocumentVector(collectionName, docId, content);

        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(embeddingService).generateEmbedding(content);
        verify(milvusClient).insert(argThat(param ->
                param.getCollectionName().equals(collectionName)
        ));
    }

    @Test
    @DisplayName("测试添加文档向量 - 向量生成失败")
    void testAddDocumentVector_EmbeddingFailure() {
        // 准备测试数据
        String collectionName = "test_collection";
        String docId = "doc-123";
        String content = "这是一个测试文档内容";
        
        // 模拟向量化失败
        when(embeddingService.generateEmbedding(content))
                .thenReturn(null);

        // 执行添加文档向量
        boolean result = vectorService.addDocumentVector(collectionName, docId, content);

        // 验证结果
        assertFalse(result);
        
        // 验证方法调用
        verify(embeddingService).generateEmbedding(content);
        verify(milvusClient, never()).insert(any());
    }

    @Test
    @DisplayName("测试语义搜索 - 成功场景")
    void testSemanticSearch_Success() {
        // 准备测试数据
        String collectionName = "test_collection";
        String query = "测试查询";
        float[] queryVector = new float[]{0.5f, 0.6f, 0.7f, 0.8f};
        int topK = 5;
        
        // 模拟向量化响应
        when(embeddingService.generateEmbedding(query))
                .thenReturn(queryVector);
        
        // 模拟Milvus搜索响应
        List<SearchResults.SearchResult> searchResults = new ArrayList<>();
        SearchResults.SearchResult result1 = SearchResults.SearchResult.newBuilder()
                .setId("doc-1")
                .setScore(0.95f)
                .build();
        SearchResults.SearchResult result2 = SearchResults.SearchResult.newBuilder()
                .setId("doc-2")
                .setScore(0.85f)
                .build();
        
        searchResults.add(result1);
        searchResults.add(result2);
        
        SearchResults mockResults = SearchResults.newBuilder()
                .addAllResults(searchResults)
                .build();
        
        R<SearchResults> searchResponse = R.success(mockResults);
        
        when(milvusClient.search(any(SearchParam.class)))
                .thenReturn(searchResponse);

        // 执行语义搜索
        List<String> result = vectorService.semanticSearch(collectionName, query, topK);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("doc-1", result.get(0));
        assertEquals("doc-2", result.get(1));
        
        // 验证方法调用
        verify(embeddingService).generateEmbedding(query);
        verify(milvusClient).search(argThat(param ->
                param.getCollectionName().equals(collectionName) &&
                param.getMetricType().equals("IP") &&
                param.getTopK() == topK
        ));
    }

    @Test
    @DisplayName("测试语义搜索 - 查询向量生成失败")
    void testSemanticSearch_EmbeddingFailure() {
        // 准备测试数据
        String collectionName = "test_collection";
        String query = "测试查询";
        int topK = 5;
        
        // 模拟向量化失败
        when(embeddingService.generateEmbedding(query))
                .thenReturn(null);

        // 执行语义搜索
        List<String> result = vectorService.semanticSearch(collectionName, query, topK);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // 验证方法调用
        verify(embeddingService).generateEmbedding(query);
        verify(milvusClient, never()).search(any());
    }

    @Test
    @DisplayName("测试批量添加文档向量 - 成功场景")
    void testBatchAddDocumentVectors_Success() {
        // 准备测试数据
        String collectionName = "test_collection";
        List<String> docIds = Arrays.asList("doc-1", "doc-2", "doc-3");
        List<String> contents = Arrays.asList(
                "文档1内容",
                "文档2内容", 
                "文档3内容"
        );
        
        float[] vector1 = new float[]{0.1f, 0.2f, 0.3f};
        float[] vector2 = new float[]{0.4f, 0.5f, 0.6f};
        float[] vector3 = new float[]{0.7f, 0.8f, 0.9f};
        
        // 模拟向量化响应
        when(embeddingService.generateEmbedding("文档1内容")).thenReturn(vector1);
        when(embeddingService.generateEmbedding("文档2内容")).thenReturn(vector2);
        when(embeddingService.generateEmbedding("文档3内容")).thenReturn(vector3);
        
        // 模拟Milvus插入响应
        MutationResult mutationResult = MutationResult.newBuilder()
                .setInsertCnt(3)
                .build();
        
        R<MutationResult> insertResponse = R.success(mutationResult);
        
        when(milvusClient.insert(any(InsertParam.class)))
                .thenReturn(insertResponse);

        // 执行批量添加
        boolean result = vectorService.batchAddDocumentVectors(collectionName, docIds, contents);

        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(embeddingService, times(3)).generateEmbedding(anyString());
        verify(milvusClient).insert(argThat(param ->
                param.getCollectionName().equals(collectionName) &&
                param.getRows().size() == 3
        ));
    }

    @Test
    @DisplayName("测试批量添加文档向量 - 部分失败")
    void testBatchAddDocumentVectors_PartialFailure() {
        // 准备测试数据
        String collectionName = "test_collection";
        List<String> docIds = Arrays.asList("doc-1", "doc-2", "doc-3");
        List<String> contents = Arrays.asList(
                "文档1内容",
                "文档2内容", 
                "文档3内容"
        );
        
        // 模拟第二个文档向量化失败
        when(embeddingService.generateEmbedding("文档1内容"))
                .thenReturn(new float[]{0.1f, 0.2f});
        when(embeddingService.generateEmbedding("文档2内容"))
                .thenReturn(null);
        when(embeddingService.generateEmbedding("文档3内容"))
                .thenReturn(new float[]{0.7f, 0.8f});

        // 执行批量添加
        boolean result = vectorService.batchAddDocumentVectors(collectionName, docIds, contents);

        // 验证结果
        assertFalse(result); // 有失败，整体返回false
        
        // 验证方法调用
        verify(embeddingService, times(3)).generateEmbedding(anyString());
        verify(milvusClient).insert(argThat(param ->
                param.getRows().size() == 2 // 只有2个成功的文档
        ));
    }

    @Test
    @DisplayName("测试删除文档向量 - 成功场景")
    void testDeleteDocumentVector_Success() {
        // 准备测试数据
        String collectionName = "test_collection";
        String docId = "doc-123";
        
        // 模拟Milvus删除响应
        MutationResult mutationResult = MutationResult.newBuilder()
                .setDeleteCnt(1)
                .build();
        
        R<MutationResult> deleteResponse = R.success(mutationResult);
        
        when(milvusClient.delete(any()))
                .thenReturn(deleteResponse);

        // 执行删除文档向量
        boolean result = vectorService.deleteDocumentVector(collectionName, docId);

        // 验证结果
        assertTrue(result);
        
        // 验证方法调用
        verify(milvusClient).delete(argThat(param ->
                param.getCollectionName().equals(collectionName)
        ));
    }

    @Test
    @DisplayName("测试检查集合是否存在")
    void testCollectionExists() {
        // 准备测试数据
        String collectionName = "test_collection";
        
        // 模拟Milvus响应
        R<Boolean> hasCollectionResponse = R.success(true);
        
        when(milvusClient.hasCollection(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()))
                .thenReturn(hasCollectionResponse);

        // 执行检查
        boolean exists = vectorService.collectionExists(collectionName);

        // 验证结果
        assertTrue(exists);
        
        // 验证方法调用
        verify(milvusClient).hasCollection(eq(HasCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .build()));
    }

    @Test
    @DisplayName("测试RAG检索 - 成功场景")
    void testRagRetrieval_Success() {
        // 准备测试数据
        String collectionName = "test_collection";
        String query = "AI-OA项目是什么？";
        int topK = 3;
        
        // 模拟语义搜索返回文档ID
        List<String> docIds = Arrays.asList("doc-1", "doc-2");
        when(vectorService.semanticSearch(collectionName, query, topK))
                .thenReturn(docIds);

        // 执行RAG检索
        List<String> result = vectorService.ragRetrieval(collectionName, query, topK);

        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("doc-1", result.get(0));
        assertEquals("doc-2", result.get(1));
        
        // 验证方法调用
        verify(vectorService).semanticSearch(collectionName, query, topK);
    }

    @Test
    @DisplayName("测试获取向量维度")
    void testGetVectorDimension() {
        // 准备测试数据
        int expectedDimension = 1536; // OpenAI embedding维度
        
        // 模拟嵌入服务响应
        float[] sampleVector = new float[expectedDimension];
        for (int i = 0; i < expectedDimension; i++) {
            sampleVector[i] = i * 0.001f;
        }
        
        when(embeddingService.generateEmbedding("测试文本"))
                .thenReturn(sampleVector);

        // 执行获取维度
        int dimension = vectorService.getVectorDimension();

        // 验证结果
        assertEquals(expectedDimension, dimension);
        
        // 验证方法调用
        verify(embeddingService).generateEmbedding("测试文本");
    }

    @Test
    @DisplayName("测试异常处理 - Milvus连接失败")
    void testExceptionHandling_ConnectionFailure() {
        // 准备测试数据
        String collectionName = "test_collection";
        
        // 模拟连接失败
        when(milvusClient.hasCollection(any()))
                .thenThrow(new RuntimeException("连接超时"));

        // 执行操作并验证异常处理
        assertThrows(RuntimeException.class, () ->
                vectorService.collectionExists(collectionName)
        );
        
        // 验证方法调用
        verify(milvusClient).hasCollection(any());
    }

    @Test
    @DisplayName("测试空值边界情况")
    void testNullBoundaryCases() {
        // 测试空集合名称
        assertThrows(IllegalArgumentException.class, () ->
                vectorService.createCollection(null)
        );
        
        // 测试空文档ID
        assertThrows(IllegalArgumentException.class, () ->
                vectorService.addDocumentVector("test_collection", null, "内容")
        );
        
        // 测试空内容
        assertThrows(IllegalArgumentException.class, () ->
                vectorService.addDocumentVector("test_collection", "doc-1", null)
        );
    }
}