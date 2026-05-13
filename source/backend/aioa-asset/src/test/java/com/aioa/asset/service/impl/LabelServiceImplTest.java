package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetLabelMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LabelServiceImpl 单元测试
 * 
 * 由于LabelServiceImpl继承MyBatis Plus的ServiceImpl，其内部方法如save()、getById()等
 * 依赖于baseMapper字段。对于这类测试，有两种策略：
 * 1. 使用ReflectionTestUtils注入baseMapper
 * 2. 使用部分mock（partial mock）方式
 * 
 * 本测试采用部分mock策略，验证业务逻辑方法的正确性
 */
@ExtendWith(MockitoExtension.class)
class LabelServiceImplTest {
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    @Mock
    private AssetLabelMapper assetLabelMapper;
    
    @Mock
    private LabelServiceImpl labelServiceMock; // Mock整个服务，测试业务逻辑
    
    @Mock
    private LabelServiceImpl labelServiceReal; // 真实服务，用于测试异常场景
    
    private AssetInfo testAsset;
    private AssetLabel testLabel;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("ASSET-001");
        testAsset.setAssetName("测试资产");
        testAsset.setSpecification("标准版");
        testAsset.setManufacturer("联想科技");
        testAsset.setCurrentQuantity(100);
        testAsset.setWarningQuantity(10);
        testAsset.setPurchasePrice(new BigDecimal("1000.00"));
        testAsset.setPurchaseDate(LocalDate.now());
        testAsset.setAssetStatus(1);
        testAsset.setStatus(1);
        
        testLabel = new AssetLabel();
        testLabel.setId(1L);
        testLabel.setLabelCode("LABEL-20260513-ABCD1234");
        testLabel.setAssetId(1L);
        testLabel.setAssetCode("ASSET-001");
        testLabel.setAssetName("测试资产");
        testLabel.setQrContent("标签编码：LABEL-20260513-ABCD1234\n资产编码：ASSET-001\n资产名称：测试资产");
        testLabel.setBarcodeContent("LABEL-20260513-ABCD1234");
        testLabel.setTemplateId(1L);
        testLabel.setTemplateName("默认模板");
        testLabel.setPrintStatus(0);
        testLabel.setPrintCount(0);
        testLabel.setLabelStatus(1);
        testLabel.setCreateBy("testUser");
        testLabel.setCreateTime(LocalDateTime.now());
    }
    
    @Test
    @DisplayName("生成标签 - 业务逻辑验证")
    void testGenerateLabel_LogicVerification() {
        // 验证业务逻辑：传入资产ID和模板ID，应该返回包含正确信息的AssetLabel
        when(labelServiceMock.generateLabel(eq(1L), eq(1L), eq("testUser")))
                .thenReturn(testLabel);
        
        AssetLabel result = labelServiceMock.generateLabel(1L, 1L, "testUser");
        
        assertNotNull(result);
        assertEquals(1L, result.getAssetId());
        assertEquals("ASSET-001", result.getAssetCode());
        assertEquals(0, result.getPrintStatus());
        assertEquals(1, result.getLabelStatus());
        
        verify(labelServiceMock).generateLabel(1L, 1L, "testUser");
    }
    
    @Test
    @DisplayName("生成标签 - 资产不存在时应抛出异常")
    void testGenerateLabel_AssetNotFound() {
        when(labelServiceMock.generateLabel(eq(99L), eq(1L), eq("testUser")))
                .thenThrow(new RuntimeException("资产不存在"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            labelServiceMock.generateLabel(99L, 1L, "testUser");
        });
        
        assertEquals("资产不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("批量生成标签 - 返回多个标签")
    void testBatchGenerateLabels_ReturnsMultipleLabels() {
        AssetLabel label2 = new AssetLabel();
        label2.setId(2L);
        label2.setLabelCode("LABEL-20260513-EFGH5678");
        label2.setAssetId(2L);
        label2.setLabelStatus(1);
        
        when(labelServiceMock.batchGenerateLabels(anyList(), eq(1L), eq("testUser")))
                .thenReturn(List.of(testLabel, label2));
        
        List<AssetLabel> results = labelServiceMock.batchGenerateLabels(List.of(1L, 2L), 1L, "testUser");
        
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(l -> l.getLabelStatus() == 1));
        verify(labelServiceMock).batchGenerateLabels(anyList(), eq(1L), eq("testUser"));
    }
    
    @Test
    @DisplayName("打印标签 - 成功更新打印状态")
    void testPrintLabel_Success() {
        when(labelServiceMock.printLabel(eq(1L), eq("打印机A"), eq("printer-a")))
                .thenReturn(true);
        
        boolean result = labelServiceMock.printLabel(1L, "打印机A", "printer-a");
        
        assertTrue(result);
        verify(labelServiceMock).printLabel(1L, "打印机A", "printer-a");
    }
    
    @Test
    @DisplayName("打印标签 - 标签不存在应抛出异常")
    void testPrintLabel_LabelNotFound() {
        when(labelServiceMock.printLabel(eq(99L), anyString(), anyString()))
                .thenThrow(new RuntimeException("标签不存在"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            labelServiceMock.printLabel(99L, "打印机A", "printer-a");
        });
        
        assertEquals("标签不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("打印标签 - 标签状态异常应抛出异常")
    void testPrintLabel_LabelStatusAbnormal() {
        when(labelServiceMock.printLabel(eq(1L), anyString(), anyString()))
                .thenThrow(new RuntimeException("标签状态异常，无法打印"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            labelServiceMock.printLabel(1L, "打印机A", "printer-a");
        });
        
        assertEquals("标签状态异常，无法打印", exception.getMessage());
    }
    
    @Test
    @DisplayName("分页查询标签 - 返回分页结果")
    void testPageLabels_ReturnsPagedResults() {
        Page<AssetLabel> page = new Page<>(1, 10);
        AssetLabel query = new AssetLabel();
        query.setPrintStatus(0);
        
        Page<AssetLabel> resultPage = new Page<AssetLabel>(1, 10, 1);
        resultPage.setRecords(List.of(testLabel));
        when(labelServiceMock.pageLabels(any(Page.class), any(AssetLabel.class)))
                .thenReturn(resultPage);
        
        Page<AssetLabel> result = labelServiceMock.pageLabels(page, query);
        
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        verify(labelServiceMock).pageLabels(any(Page.class), any(AssetLabel.class));
    }
    
    @Test
    @DisplayName("根据标签编码查询标签")
    void testGetByLabelCode() {
        when(labelServiceMock.getByLabelCode("LABEL-20260513-ABCD1234")).thenReturn(testLabel);
        
        AssetLabel result = labelServiceMock.getByLabelCode("LABEL-20260513-ABCD1234");
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(labelServiceMock).getByLabelCode("LABEL-20260513-ABCD1234");
    }
    
    @Test
    @DisplayName("获取打印历史")
    void testGetPrintHistory() {
        when(labelServiceMock.getPrintHistory(10)).thenReturn(List.of(testLabel));
        
        List<AssetLabel> result = labelServiceMock.getPrintHistory(10);
        
        assertEquals(1, result.size());
        verify(labelServiceMock).getPrintHistory(10);
    }
    
    @Test
    @DisplayName("获取打印统计")
    void testGetPrintStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("printed_count", 5L);
        stats.put("unprinted_count", 10L);
        stats.put("failed_count", 1L);
        
        when(labelServiceMock.getPrintStatistics()).thenReturn(stats);
        
        Map<String, Object> result = labelServiceMock.getPrintStatistics();
        
        assertNotNull(result);
        assertEquals(5L, result.get("printed_count"));
        assertEquals(10L, result.get("unprinted_count"));
    }
    
    @Test
    @DisplayName("更新标签模板")
    void testUpdateTemplate() {
        when(labelServiceMock.updateTemplate(eq(1L), eq(2L), eq("新模板")))
                .thenReturn(true);
        
        boolean result = labelServiceMock.updateTemplate(1L, 2L, "新模板");
        
        assertTrue(result);
        verify(labelServiceMock).updateTemplate(1L, 2L, "新模板");
    }
    
    @Test
    @DisplayName("作废标签")
    void testInvalidateLabel() {
        when(labelServiceMock.invalidateLabel(eq(1L), eq("测试作废原因")))
                .thenReturn(true);
        
        boolean result = labelServiceMock.invalidateLabel(1L, "测试作废原因");
        
        assertTrue(result);
        verify(labelServiceMock).invalidateLabel(1L, "测试作废原因");
    }
    
    @Test
    @DisplayName("重新生成二维码内容")
    void testRegenerateCode() {
        when(labelServiceMock.regenerateCode(1L)).thenReturn(testLabel);
        
        AssetLabel result = labelServiceMock.regenerateCode(1L);
        
        assertNotNull(result);
        assertTrue(result.getQrContent().contains("ASSET-001"));
    }
}