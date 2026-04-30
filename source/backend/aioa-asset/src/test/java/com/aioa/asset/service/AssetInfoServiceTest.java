package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetOperation;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetOperationMapper;
import com.aioa.asset.service.impl.AssetInfoServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 资产信息服务单元测试 - Mockito版本
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssetInfoServiceTest {
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    @Mock
    private AssetOperationMapper assetOperationMapper;
    
    @Mock
    private AssetOperationService assetOperationService;
    
    private AssetInfoServiceImpl assetInfoService;
    
    private AssetInfo testAsset;
    
    @BeforeEach
    void setUp() throws Exception {
        // 手动构造service并注入baseMapper（ServiceImpl使用baseMapper而不是mapper字段）
        assetInfoService = new AssetInfoServiceImpl();
        
        // 通过反射设置baseMapper（来自ServiceImpl）
        Field baseMapperField = assetInfoService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(assetInfoService, assetInfoMapper);
        
        // 通过反射设置其他@Autowired字段
        Field opServiceField = assetInfoService.getClass().getDeclaredField("assetOperationService");
        opServiceField.setAccessible(true);
        opServiceField.set(assetInfoService, assetOperationService);
        
        Field opMapperField = assetInfoService.getClass().getDeclaredField("assetOperationMapper");
        opMapperField.setAccessible(true);
        opMapperField.set(assetInfoService, assetOperationMapper);
        
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("ASSET-001");
        testAsset.setAssetName("笔记本电脑");
        testAsset.setCategoryId(1L);
        testAsset.setModel("ThinkPad X1 Carbon");
        testAsset.setCurrentQuantity(10);
        testAsset.setWarningQuantity(3);
        testAsset.setPurchasePrice(new BigDecimal("12999.00"));
        testAsset.setPurchaseDate(LocalDate.now());
        testAsset.setAssetStatus(1);
        testAsset.setStatus(1);
        testAsset.setCreateBy("testUser");
        testAsset.setCreateTime(LocalDateTime.now());
    }
    
    @Test
    void testGetById() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        AssetInfo result = assetInfoService.getById(1L);
        
        assertNotNull(result);
        assertEquals("笔记本电脑", result.getAssetName());
        assertEquals(10, result.getCurrentQuantity());
        verify(assetInfoMapper, times(1)).selectById(1L);
    }
    
    @Test
    void testBorrowAsset() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(assetOperationService.save(any(AssetOperation.class))).thenReturn(true);
        
        boolean result = assetInfoService.borrowAsset(1L, 3, "操作员", "operator1", "借用测试");
        
        assertTrue(result);
        verify(assetInfoMapper, times(1)).selectById(1L);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
        verify(assetOperationService, times(1)).save(any(AssetOperation.class));
    }
    
    @Test
    void testBorrowAsset_InsufficientStock() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assetInfoService.borrowAsset(1L, 15, "操作员", "operator1", "借用测试"));
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    void testBorrowAsset_AssetNotFound() {
        when(assetInfoMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assetInfoService.borrowAsset(99L, 1, "操作员", "operator1", "借用测试"));
        
        assertTrue(exception.getMessage().contains("资产不存在"));
    }
    
    @Test
    void testReturnAsset() {
        testAsset.setAssetStatus(2); // 领用中状态
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(assetOperationService.save(any(AssetOperation.class))).thenReturn(true);
        
        boolean result = assetInfoService.returnAsset(1L, 3, "操作员");
        
        assertTrue(result);
        verify(assetInfoMapper, times(1)).selectById(1L);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
        verify(assetOperationService, times(1)).save(any(AssetOperation.class));
    }
    
    @Test
    void testTransferAsset() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(assetOperationService.save(any(AssetOperation.class))).thenReturn(true);
        
        boolean result = assetInfoService.transferAsset(1L, 2, "操作员", "operator1", "新部门", "调拨测试");
        
        assertTrue(result);
        verify(assetInfoMapper, times(1)).selectById(1L);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
        verify(assetOperationService, times(1)).save(any(AssetOperation.class));
    }
    
    @Test
    void testTransferAsset_InsufficientStock() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assetInfoService.transferAsset(1L, 15, "操作员", "operator1", "新部门", "调拨测试"));
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    void testScrapAsset() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(assetOperationService.save(any(AssetOperation.class))).thenReturn(true);
        
        boolean result = assetInfoService.scrapAsset(1L, 2, "操作员", "operator1", "报废测试");
        
        assertTrue(result);
        verify(assetInfoMapper, times(1)).selectById(1L);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
        verify(assetOperationService, times(1)).save(any(AssetOperation.class));
    }
    
    @Test
    void testScrapAsset_InsufficientStock() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> assetInfoService.scrapAsset(1L, 15, "操作员", "operator1", "报废测试"));
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    void testGetWarningAssets() {
        AssetInfo lowStockAsset = new AssetInfo();
        lowStockAsset.setId(2L);
        lowStockAsset.setAssetCode("ASSET-002");
        lowStockAsset.setAssetName("打印纸");
        lowStockAsset.setCurrentQuantity(2);
        lowStockAsset.setWarningQuantity(5);
        
        when(assetInfoMapper.selectWarningAssets()).thenReturn(Collections.singletonList(lowStockAsset));
        
        List<AssetInfo> result = assetInfoService.getWarningAssets();
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("打印纸", result.get(0).getAssetName());
    }
    
    @Test
    void testPageAssets() {
        Page<AssetInfo> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Collections.singletonList(testAsset));
        resultPage.setTotal(1);
        
        when(assetInfoMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(resultPage);
        
        AssetInfo query = new AssetInfo();
        query.setAssetName("笔记本");
        
        Page<AssetInfo> result = assetInfoService.pageAssets(new Page<>(1, 10), query);
        
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }
    
    @Test
    void testGetAssetStatistics() {
        List<AssetInfo> warningAssets = new ArrayList<>();
        warningAssets.add(testAsset);
        
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        Map<String, Object> stat = new HashMap<>();
        stat.put("category_name", "固定资产");
        stat.put("count", 10L);
        categoryStats.add(stat);
        
        when(assetInfoMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);
        when(assetInfoMapper.selectWarningAssets()).thenReturn(warningAssets);
        when(assetInfoMapper.groupByCategory()).thenReturn(categoryStats);
        
        Map<String, Object> result = assetInfoService.getAssetStatistics();
        
        assertNotNull(result);
        assertEquals(5L, result.get("totalAssets"));
        assertEquals(1, result.get("warningCount"));
        assertNotNull(result.get("categoryStats"));
    }
}
