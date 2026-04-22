package com.aioa.knowledge.service.impl;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.mapper.KnowledgeMapper;
import com.aioa.knowledge.service.VectorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * KnowledgeServiceImpl 批处理功能单元测试
 * 毛泽东思想指导：实事求是，测试批处理功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgeServiceImpl 批处理功能测试")
class KnowledgeServiceImplBatchTest {

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private VectorService vectorService;

    @InjectMocks
    private KnowledgeServiceImpl knowledgeService;

    private KnowledgeDoc createTestDoc(String id, String title) {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId(id);
        doc.setTitle(title);
        doc.setContent("这是" + title + "的内容");
        doc.setStatus("published");
        doc.setViewCount(0);
        doc.setLikeCount(0);
        return doc;
    }

    @Test
    @DisplayName("批量创建文档 - 正常场景")
    void batchCreateDocs_shouldReturnSuccessIds() {
        // given
        List<KnowledgeDoc> docs = Arrays.asList(
            createTestDoc("doc-1", "文档1"),
            createTestDoc("doc-2", "文档2"),
            createTestDoc("doc-3", "文档3")
        );

        when(knowledgeMapper.insert(any(KnowledgeDoc.class))).thenReturn(1);
        when(vectorService.storeVector(any(KnowledgeDoc.class))).thenReturn("vector-1", "vector-2", "vector-3");

        // when
        List<String> result = knowledgeService.batchCreateDocs(docs);

        // then
        assertThat(result).hasSize(3);
        verify(knowledgeMapper, times(3)).insert(any(KnowledgeDoc.class));
        verify(vectorService, times(3)).storeVector(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量创建文档 - 部分失败")
    void batchCreateDocs_withPartialFailure_shouldReturnSuccessIdsOnly() {
        // given
        List<KnowledgeDoc> docs = Arrays.asList(
            createTestDoc("doc-1", "文档1"),
            createTestDoc("doc-2", "文档2"),
            createTestDoc("doc-3", "文档3")
        );

        when(knowledgeMapper.insert(any(KnowledgeDoc.class)))
            .thenReturn(1)  // 第一个成功
            .thenThrow(new RuntimeException("插入失败"))  // 第二个失败
            .thenReturn(1);  // 第三个成功

        when(vectorService.storeVector(any(KnowledgeDoc.class)))
            .thenReturn("vector-1")
            .thenReturn("vector-3");

        // when
        List<String> result = knowledgeService.batchCreateDocs(docs);

        // then
        assertThat(result).hasSize(2); // 只返回成功的ID
        verify(knowledgeMapper, times(3)).insert(any(KnowledgeDoc.class));
        verify(vectorService, times(2)).storeVector(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量创建文档 - 空列表")
    void batchCreateDocs_withEmptyList_shouldReturnEmptyList() {
        // when
        List<String> result = knowledgeService.batchCreateDocs(Collections.emptyList());

        // then
        assertThat(result).isEmpty();
        verify(knowledgeMapper, never()).insert(any());
    }

    @Test
    @DisplayName("批量更新文档 - 正常场景")
    void batchUpdateDocs_shouldReturnSuccessCount() {
        // given
        List<KnowledgeDoc> docs = Arrays.asList(
            createTestDoc("doc-1", "文档1"),
            createTestDoc("doc-2", "文档2")
        );

        when(knowledgeMapper.updateById(any(KnowledgeDoc.class))).thenReturn(1);
        when(knowledgeMapper.selectById(any())).thenReturn(createTestDoc("doc-1", "原文档"));

        // when
        int result = knowledgeService.batchUpdateDocs(docs);

        // then
        assertThat(result).isEqualTo(2);
        verify(knowledgeMapper, times(2)).updateById(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量更新文档 - 部分失败")
    void batchUpdateDocs_withPartialFailure_shouldReturnSuccessCountOnly() {
        // given
        List<KnowledgeDoc> docs = Arrays.asList(
            createTestDoc("doc-1", "文档1"),
            createTestDoc("doc-2", "文档2"),
            createTestDoc("doc-3", "文档3")
        );

        when(knowledgeMapper.updateById(any(KnowledgeDoc.class)))
            .thenReturn(1)  // 第一个成功
            .thenReturn(0)  // 第二个失败
            .thenThrow(new RuntimeException("更新失败"));  // 第三个异常

        when(knowledgeMapper.selectById(any())).thenReturn(createTestDoc("doc-1", "原文档"));

        // when
        int result = knowledgeService.batchUpdateDocs(docs);

        // then
        assertThat(result).isEqualTo(1); // 只有第一个成功
        verify(knowledgeMapper, times(3)).updateById(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量删除文档 - 正常场景")
    void batchDeleteDocs_shouldReturnSuccessCount() {
        // given
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        KnowledgeDoc doc1 = createTestDoc("1", "文档1");
        doc1.setVectorId("vector-1");
        KnowledgeDoc doc2 = createTestDoc("2", "文档2");
        doc2.setVectorId("vector-2");
        KnowledgeDoc doc3 = createTestDoc("3", "文档3");
        doc3.setVectorId(null); // 没有向量ID

        when(knowledgeMapper.selectById(1L)).thenReturn(doc1);
        when(knowledgeMapper.selectById(2L)).thenReturn(doc2);
        when(knowledgeMapper.selectById(3L)).thenReturn(doc3);

        when(knowledgeMapper.deleteById(anyLong())).thenReturn(1);
        when(vectorService.deleteVector(anyString())).thenReturn(true);

        // when
        int result = knowledgeService.batchDeleteDocs(ids);

        // then
        assertThat(result).isEqualTo(3);
        verify(knowledgeMapper, times(3)).deleteById(anyLong());
        verify(vectorService, times(2)).deleteVector(anyString()); // 只有2个有向量ID
    }

    @Test
    @DisplayName("批量删除文档 - 部分文档不存在")
    void batchDeleteDocs_withNonExistentDocs_shouldHandleGracefully() {
        // given
        List<Long> ids = Arrays.asList(1L, 999L, 3L);

        KnowledgeDoc doc1 = createTestDoc("1", "文档1");
        doc1.setVectorId("vector-1");
        KnowledgeDoc doc3 = createTestDoc("3", "文档3");
        doc3.setVectorId("vector-3");

        when(knowledgeMapper.selectById(1L)).thenReturn(doc1);
        when(knowledgeMapper.selectById(999L)).thenReturn(null); // 文档不存在
        when(knowledgeMapper.selectById(3L)).thenReturn(doc3);

        when(knowledgeMapper.deleteById(anyLong())).thenReturn(1);
        when(vectorService.deleteVector(anyString())).thenReturn(true);

        // when
        int result = knowledgeService.batchDeleteDocs(ids);

        // then
        assertThat(result).isEqualTo(2); // 只删除了2个存在的文档
        verify(knowledgeMapper, times(2)).deleteById(anyLong());
        verify(vectorService, times(2)).deleteVector(anyString());
    }

    @Test
    @DisplayName("批量导入文档 - TXT格式")
    void batchImportDocs_txtFormat_shouldParseCorrectly() {
        // given
        String fileContent = "第一行文档内容\n第二行文档内容\n\n第三行文档内容";
        String fileType = "txt";
        Long categoryId = 1L;

        when(knowledgeMapper.insert(any(KnowledgeDoc.class))).thenReturn(1);
        when(vectorService.storeVector(any(KnowledgeDoc.class))).thenReturn("vector-1", "vector-2", "vector-3");

        // when
        Map<String, Object> result = knowledgeService.batchImportDocs(fileContent, fileType, categoryId);

        // then
        assertThat(result).containsKeys("taskId", "status", "totalDocs", "successCount");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("totalDocs")).isEqualTo(3); // 3行非空内容
        verify(knowledgeMapper, times(3)).insert(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量导入文档 - CSV格式")
    void batchImportDocs_csvFormat_shouldParseCorrectly() {
        // given
        String fileContent = "title,content,summary\n" +
                            "文档1,内容1,摘要1\n" +
                            "文档2,内容2,摘要2\n" +
                            "文档3,内容3,摘要3";
        String fileType = "csv";
        Long categoryId = 1L;

        when(knowledgeMapper.insert(any(KnowledgeDoc.class))).thenReturn(1);
        when(vectorService.storeVector(any(KnowledgeDoc.class))).thenReturn("vector-1", "vector-2", "vector-3");

        // when
        Map<String, Object> result = knowledgeService.batchImportDocs(fileContent, fileType, categoryId);

        // then
        assertThat(result).containsKeys("taskId", "status", "totalDocs", "successCount");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("totalDocs")).isEqualTo(3); // 3行数据（跳过表头）
        verify(knowledgeMapper, times(3)).insert(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量导入文档 - 未知格式")
    void batchImportDocs_unknownFormat_shouldHandleAsText() {
        // given
        String fileContent = "这是一个未知格式的文档内容";
        String fileType = "unknown";
        Long categoryId = 1L;

        when(knowledgeMapper.insert(any(KnowledgeDoc.class))).thenReturn(1);
        when(vectorService.storeVector(any(KnowledgeDoc.class))).thenReturn("vector-1");

        // when
        Map<String, Object> result = knowledgeService.batchImportDocs(fileContent, fileType, categoryId);

        // then
        assertThat(result).containsKeys("taskId", "status", "totalDocs", "successCount");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("totalDocs")).isEqualTo(1); // 整个内容作为一个文档
        verify(knowledgeMapper, times(1)).insert(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("批量导入文档 - 处理异常")
    void batchImportDocs_withException_shouldReturnErrorStatus() {
        // given
        String fileContent = "测试内容";
        String fileType = "txt";
        Long categoryId = 1L;

        when(knowledgeMapper.insert(any(KnowledgeDoc.class))).thenThrow(new RuntimeException("数据库错误"));

        // when
        Map<String, Object> result = knowledgeService.batchImportDocs(fileContent, fileType, categoryId);

        // then
        assertThat(result).containsKeys("taskId", "status", "error");
        assertThat(result.get("status")).isEqualTo("failed");
        assertThat(result.get("error")).isNotNull();
    }

    @Test
    @DisplayName("重建所有向量 - 正常场景")
    void rebuildAllVectors_shouldRebuildSuccessfully() {
        // given
        List<KnowledgeDoc> docs = Arrays.asList(
            createTestDoc("doc-1", "文档1"),
            createTestDoc("doc-2", "文档2")
        );

        when(knowledgeMapper.selectList(any())).thenReturn(docs);
        when(vectorService.storeVector(any(KnowledgeDoc.class))).thenReturn("new-vector-1", "new-vector-2");
        when(knowledgeMapper.updateById(any(KnowledgeDoc.class))).thenReturn(1);

        // when
        Map<String, Object> result = knowledgeService.rebuildAllVectors();

        // then
        assertThat(result).containsKeys("taskId", "status", "totalDocs", "successCount", "failCount");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("totalDocs")).isEqualTo(2);
        assertThat(result.get("successCount")).isEqualTo(2);
        assertThat(result.get("failCount")).isEqualTo(0);

        verify(vectorService, times(2)).storeVector(any(KnowledgeDoc.class));
        verify(knowledgeMapper, times(2)).updateById(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("重建所有向量 - 部分失败")
    void rebuildAllVectors_withPartialFailure_shouldReportCorrectly() {
        // given
        List<KnowledgeDoc> docs = Arrays.asList(
            createTestDoc("doc-1", "文档1"),
            createTestDoc("doc-2", "文档2"),
            createTestDoc("doc-3", "文档3")
        );

        when(knowledgeMapper.selectList(any())).thenReturn(docs);
        when(vectorService.storeVector(any(KnowledgeDoc.class)))
            .thenReturn("new-vector-1")
            .thenReturn(null)  // 第二个失败
            .thenThrow(new RuntimeException("向量生成失败"));  // 第三个异常

        when(knowledgeMapper.updateById(any(KnowledgeDoc.class))).thenReturn(1);

        // when
        Map<String, Object> result = knowledgeService.rebuildAllVectors();

        // then
        assertThat(result).containsKeys("taskId", "status", "totalDocs", "successCount", "failCount");
        assertThat(result.get("status")).isEqualTo("completed");
        assertThat(result.get("totalDocs")).isEqualTo(3);
        assertThat(result.get("successCount")).isEqualTo(1); // 只有第一个成功
        assertThat(result.get("failCount")).isEqualTo(2); // 两个失败

        verify(vectorService, times(3)).storeVector(any(KnowledgeDoc.class));
        verify(knowledgeMapper, times(1)).updateById(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("获取批处理任务状态")
    void getBatchTaskStatus_shouldReturnStatusInfo() {
        // given
        String taskId = "test-task-123";

        // when
        Map<String, Object> result = knowledgeService.getBatchTaskStatus(taskId);

        // then
        assertThat(result).containsKeys("taskId", "status", "timestamp");
        assertThat(result.get("taskId")).isEqualTo(taskId);
        assertThat(result.get("status")).isEqualTo("completed");
    }

    @Test
    @DisplayName("批量操作 - 空参数处理")
    void batchOperations_withNullParameters_shouldHandleGracefully() {
        // 测试空列表和null参数
        assertThat(knowledgeService.batchCreateDocs(null)).isEmpty();
        assertThat(knowledgeService.batchCreateDocs(Collections.emptyList())).isEmpty();
        
        assertThat(knowledgeService.batchUpdateDocs(null)).isEqualTo(0);
        assertThat(knowledgeService.batchUpdateDocs(Collections.emptyList())).isEqualTo(0);
        
        assertThat(knowledgeService.batchDeleteDocs(null)).isEqualTo(0);
        assertThat(knowledgeService.batchDeleteDocs(Collections.emptyList())).isEqualTo(0);
        
        assertThat(knowledgeService.batchImportDocs(null, "txt", 1L)).containsKey("status");
        assertThat(knowledgeService.batchImportDocs("", "txt", 1L)).containsKey("status");
    }

    @Test
    @DisplayName("批处理 - 大数据量性能测试")
    void batchOperations_withLargeDataset_shouldCompleteSuccessfully() {
        // given: 创建100个文档的测试数据
        List<KnowledgeDoc> largeDocs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeDocs.add(createTestDoc("doc-" + i, "文档" + i));
        }

        // 模拟所有操作成功
        when(knowledgeMapper.insert(any(KnowledgeDoc.class))).thenReturn(1);
        when(vectorService.storeVector(any(KnowledgeDoc.class))).thenReturn("vector-" + System.currentTimeMillis());

        // when: 执行批量创建
        long startTime = System.currentTimeMillis();
        List<String> result = knowledgeService.batchCreateDocs(largeDocs);
        long endTime = System.currentTimeMillis();

        // then: 验证结果
        assertThat(result).hasSize(100);
        assertThat(endTime - startTime).isLessThan(5000); // 5秒内完成
        
        // 验证调用次数
        verify(knowledgeMapper, times(100)).insert(any(KnowledgeDoc.class));
        verify(vectorService, times(100)).storeVector(any(KnowledgeDoc.class));
        
        // 记录性能信息
        System.out.printf("批处理性能: 处理 %d 个文档用时 %d 毫秒%n", 
            largeDocs.size(), endTime - startTime);
    }
}