package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetLabelMapper;
import com.aioa.asset.service.impl.LabelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 标签服务单元测试 - Mockito版本
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LabelServiceTest {
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    @Mock
    private AssetLabelMapper assetLabelMapper;
    
    private LabelServiceImpl labelService;
    
    private AssetInfo testAsset;
    
    @BeforeEach
    void setUp() throws Exception {
        // 手动构造service并注入baseMapper，使用spy以便stub
        labelService = spy(new LabelServiceImpl(assetInfoMapper, assetLabelMapper));
        
        // 通过反射设置baseMapper（来自ServiceImpl）
        Field baseMapperField = labelService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(labelService, assetLabelMapper);
        
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("ASSET-LABEL-001");
        testAsset.setAssetName("测试资产-标签");
        testAsset.setCategoryId(1L);
        testAsset.setModel("测试型号");
        testAsset.setCurrentQuantity(100);
        testAsset.setWarningQuantity(10);
        testAsset.setPurchasePrice(new BigDecimal("1000.00"));
        testAsset.setPurchaseDate(LocalDate.now());
        testAsset.setAssetStatus(1);
        testAsset.setStatus(1);
    }
    
    @Test
    void testGenerateLabel() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        // stub save() on the service spy to simulate DB-generated ID
        doAnswer(invocation -> {
            AssetLabel l = invocation.getArgument(0);
            l.setId(1L);
            return true;
        }).when(labelService).save(any(AssetLabel.class));
        
        AssetLabel label = labelService.generateLabel(1L, 1L, "testUser");
        
        assertNotNull(label);
        assertNotNull(label.getId());
        assertEquals(testAsset.getId(), label.getAssetId());
        assertEquals(testAsset.getAssetCode(), label.getAssetCode());
        assertEquals(testAsset.getAssetName(), label.getAssetName());
        assertNotNull(label.getLabelCode());
        assertTrue(label.getQrContent().contains(testAsset.getAssetCode()));
        assertEquals(0, label.getPrintStatus());
        assertEquals(1, label.getLabelStatus());
        
        verify(assetInfoMapper, times(1)).selectById(1L);
    }
    
    @Test
    void testGenerateLabel_AssetNotFound() {
        when(assetInfoMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> labelService.generateLabel(99L, 1L, "testUser"));
        
        assertTrue(exception.getMessage().contains("资产不存在"));
    }
    
    @Test
    void testBatchGenerateLabels() {
        AssetInfo asset2 = new AssetInfo();
        asset2.setId(2L);
        asset2.setAssetCode("ASSET-LABEL-002");
        asset2.setAssetName("测试资产2");
        
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.selectById(2L)).thenReturn(asset2);
        // stub save() for batch
        doAnswer(invocation -> {
            AssetLabel l = invocation.getArgument(0);
            l.setId(1L);
            return true;
        }).when(labelService).save(any(AssetLabel.class));
        
        List<AssetLabel> labels = labelService.batchGenerateLabels(
                Arrays.asList(1L, 2L), 1L, "testUser");
        
        assertEquals(2, labels.size());
        assertTrue(labels.stream().allMatch(label -> label.getLabelStatus() == 1));
    }
    
    @Test
    void testPrintLabel() {
        AssetLabel label = new AssetLabel();
        label.setId(1L);
        label.setAssetId(1L);
        label.setLabelCode("LABEL-TEST-001");
        label.setLabelStatus(1);
        label.setPrintStatus(0);
        label.setPrintCount(0);
        
        when(assetLabelMapper.selectById(1L)).thenReturn(label);
        when(assetLabelMapper.updateById(any(AssetLabel.class))).thenReturn(1);
        
        boolean result = labelService.printLabel(1L, "打印机1", "printer1");
        
        assertTrue(result);
        verify(assetLabelMapper, times(1)).selectById(1L);
        verify(assetLabelMapper, times(1)).updateById(any(AssetLabel.class));
    }
    
    @Test
    void testPrintLabel_LabelNotFound() {
        when(assetLabelMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> labelService.printLabel(99L, "打印机1", "printer1"));
        
        assertTrue(exception.getMessage().contains("标签不存在"));
    }
    
    @Test
    void testPrintLabel_InvalidStatus() {
        AssetLabel label = new AssetLabel();
        label.setId(1L);
        label.setLabelStatus(2); // 已作废
        
        when(assetLabelMapper.selectById(1L)).thenReturn(label);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> labelService.printLabel(1L, "打印机1", "printer1"));
        
        assertTrue(exception.getMessage().contains("状态异常"));
    }
    
    @Test
    void testGetByLabelCode() {
        AssetLabel label = new AssetLabel();
        label.setId(1L);
        label.setLabelCode("LABEL-TEST-001");
        
        when(assetLabelMapper.selectByLabelCode("LABEL-TEST-001")).thenReturn(label);
        
        AssetLabel result = labelService.getByLabelCode("LABEL-TEST-001");
        
        assertNotNull(result);
        assertEquals("LABEL-TEST-001", result.getLabelCode());
    }
    
    @Test
    void testInvalidateLabel() {
        AssetLabel label = new AssetLabel();
        label.setId(1L);
        label.setLabelStatus(1);
        
        when(assetLabelMapper.selectById(1L)).thenReturn(label);
        when(assetLabelMapper.updateById(any(AssetLabel.class))).thenReturn(1);
        
        boolean result = labelService.invalidateLabel(1L, "测试作废");
        
        assertTrue(result);
        verify(assetLabelMapper, times(1)).updateById(any(AssetLabel.class));
    }
    
    @Test
    void testRegenerateCode() {
        AssetLabel label = new AssetLabel();
        label.setId(1L);
        label.setAssetId(1L);
        label.setLabelCode("LABEL-TEST-001");
        label.setQrContent("OLD_QR_CONTENT");
        
        when(assetLabelMapper.selectById(1L)).thenReturn(label);
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetLabelMapper.updateById(any(AssetLabel.class))).thenReturn(1);
        
        AssetLabel result = labelService.regenerateCode(1L);
        
        assertNotNull(result);
        assertTrue(result.getQrContent().contains(label.getLabelCode()));
        assertTrue(result.getQrContent().contains(testAsset.getAssetCode()));
    }
    
    @Test
    void testGetPrintStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("printed_count", 10);
        stats.put("unprinted_count", 5);
        when(assetLabelMapper.countPrintStatus()).thenReturn(stats);
        
        Map<String, Object> result = labelService.getPrintStatistics();
        
        assertNotNull(result);
        assertEquals(10, result.get("printed_count"));
        assertEquals(5, result.get("unprinted_count"));
    }
}
