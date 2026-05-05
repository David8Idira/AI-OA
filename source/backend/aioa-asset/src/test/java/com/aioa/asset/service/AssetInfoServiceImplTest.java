package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetOperation;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.service.impl.AssetInfoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AssetInfoServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试资产信息服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetInfoServiceImplTest 单元测试")
class AssetInfoServiceImplTest {

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Mock
    private com.aioa.asset.service.AssetOperationService assetOperationService;

    private AssetInfoServiceImpl assetInfoService;

    @BeforeEach
    void setUp() {
        assetInfoService = new AssetInfoServiceImpl();
        ReflectionTestUtils.setField(assetInfoService, "baseMapper", assetInfoMapper);
        ReflectionTestUtils.setField(assetInfoService, "assetOperationService", assetOperationService);
        ReflectionTestUtils.setField(assetInfoService, "assetOperationMapper", null);
    }

    private AssetInfo createTestAsset(Long id, int quantity) {
        AssetInfo asset = new AssetInfo();
        asset.setId(id);
        asset.setAssetCode("ASSET-" + id);
        asset.setAssetName("测试资产" + id);
        asset.setCurrentQuantity(quantity);
        asset.setWarningQuantity(5);
        asset.setAssetStatus(1);
        asset.setStatus(1);
        return asset;
    }

    @Test
    @DisplayName("分页查询资产 - 正常场景")
    void pageAssets_shouldReturnPagedResults() {
        // given
        AssetInfo query = new AssetInfo();
        query.setAssetName("测试");
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AssetInfo> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        
        when(assetInfoMapper.selectPage(any(), any())).thenReturn(page);

        // when
        var result = assetInfoService.pageAssets(page, query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCurrent()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("分页查询资产 - 带分类筛选")
    void pageAssets_withCategoryFilter_shouldWork() {
        // given
        AssetInfo query = new AssetInfo();
        query.setCategoryId(1L);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AssetInfo> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
        
        when(assetInfoMapper.selectPage(any(), any())).thenReturn(page);

        // when
        var result = assetInfoService.pageAssets(page, query);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取预警资产 - 正常场景")
    void getWarningAssets_shouldReturnWarningList() {
        // given
        List<AssetInfo> warningAssets = List.of(
            createTestAsset(1L, 2),
            createTestAsset(2L, 3)
        );
        when(assetInfoMapper.selectWarningAssets()).thenReturn(warningAssets);

        // when
        var result = assetInfoService.getWarningAssets();

        // then
        assertThat(result).hasSize(2);
        verify(assetInfoMapper, times(1)).selectWarningAssets();
    }

    @Test
    @DisplayName("获取预警资产 - 无预警资产")
    void getWarningAssets_withNoWarnings_shouldReturnEmptyList() {
        // given
        when(assetInfoMapper.selectWarningAssets()).thenReturn(List.of());

        // when
        var result = assetInfoService.getWarningAssets();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("领用资产 - 正常场景")
    void borrowAsset_withValidInput_shouldSucceed() {
        // given
        AssetInfo asset = createTestAsset(1L, 10);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        boolean result = assetInfoService.borrowAsset(1L, 3, "张三", "user-001", "项目使用");

        // then
        assertThat(result).isTrue();
        verify(assetInfoMapper, times(1)).updateById(any());
        verify(assetOperationService, times(1)).save(any());
    }

    @Test
    @DisplayName("领用资产 - 资产不存在")
    void borrowAsset_withNonExistingAsset_shouldThrowException() {
        // given
        when(assetInfoMapper.selectById(999L)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> assetInfoService.borrowAsset(999L, 1, "张三", "user-001", "测试"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("资产不存在");
    }

    @Test
    @DisplayName("领用资产 - 库存不足")
    void borrowAsset_withInsufficientStock_shouldThrowException() {
        // given
        AssetInfo asset = createTestAsset(1L, 2);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);

        // when/then
        assertThatThrownBy(() -> assetInfoService.borrowAsset(1L, 5, "张三", "user-001", "测试"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("库存不足");
    }

    @Test
    @DisplayName("领用资产 - 数量为0")
    void borrowAsset_withZeroQuantity_shouldWork() {
        // given
        AssetInfo asset = createTestAsset(1L, 10);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        boolean result = assetInfoService.borrowAsset(1L, 0, "张三", "user-001", "测试");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("归还资产 - 正常场景")
    void returnAsset_withValidInput_shouldSucceed() {
        // given
        AssetInfo asset = createTestAsset(1L, 7);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        boolean result = assetInfoService.returnAsset(1L, 3, "张三");

        // then
        assertThat(result).isTrue();
        verify(assetInfoMapper, times(1)).updateById(any());
        verify(assetOperationService, times(1)).save(any());
    }

    @Test
    @DisplayName("归还资产 - 资产不存在")
    void returnAsset_withNonExistingAsset_shouldThrowException() {
        // given
        when(assetInfoMapper.selectById(999L)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> assetInfoService.returnAsset(999L, 1, "张三"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("资产不存在");
    }

    @Test
    @DisplayName("调拨资产 - 正常场景")
    void transferAsset_withValidInput_shouldSucceed() {
        // given
        AssetInfo asset = createTestAsset(1L, 10);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        boolean result = assetInfoService.transferAsset(1L, 2, "张三", "user-001", "技术部", "部门调整");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("调拨资产 - 库存不足")
    void transferAsset_withInsufficientStock_shouldThrowException() {
        // given
        AssetInfo asset = createTestAsset(1L, 2);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);

        // when/then
        assertThatThrownBy(() -> assetInfoService.transferAsset(1L, 5, "张三", "user-001", "技术部", "测试"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("库存不足");
    }

    @Test
    @DisplayName("报废资产 - 正常场景")
    void scrapAsset_withValidInput_shouldSucceed() {
        // given
        AssetInfo asset = createTestAsset(1L, 10);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        boolean result = assetInfoService.scrapAsset(1L, 2, "张三", "user-001", "设备老化");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("报废资产 - 资产不存在")
    void scrapAsset_withNonExistingAsset_shouldThrowException() {
        // given
        when(assetInfoMapper.selectById(999L)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> assetInfoService.scrapAsset(999L, 1, "张三", "user-001", "测试"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("资产不存在");
    }

    @Test
    @DisplayName("报废资产 - 库存不足")
    void scrapAsset_withInsufficientStock_shouldThrowException() {
        // given
        AssetInfo asset = createTestAsset(1L, 2);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);

        // when/then
        assertThatThrownBy(() -> assetInfoService.scrapAsset(1L, 5, "张三", "user-001", "测试"))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("库存不足");
    }

    @Test
    @DisplayName("统计资产数据 - 正常场景")
    void getAssetStatistics_shouldReturnStatistics() {
        // given
        when(assetInfoMapper.selectCount(any())).thenReturn(100L);
        when(assetInfoMapper.selectWarningAssets()).thenReturn(List.of(createTestAsset(1L, 2)));
        when(assetInfoMapper.groupByCategory()).thenReturn(List.of(Map.of("category", "办公设备", "count", 50)));

        // when
        Map<String, Object> result = assetInfoService.getAssetStatistics();

        // then
        assertThat(result).isNotNull();
        assertThat(result).containsKeys("totalAssets", "warningCount", "categoryStats");
        assertThat(result.get("totalAssets")).isEqualTo(100L);
        assertThat(result.get("warningCount")).isEqualTo(1);
    }

    @Test
    @DisplayName("统计资产数据 - 无数据")
    void getAssetStatistics_withNoData_shouldReturnEmptyStats() {
        // given
        when(assetInfoMapper.selectCount(any())).thenReturn(0L);
        when(assetInfoMapper.selectWarningAssets()).thenReturn(List.of());
        when(assetInfoMapper.groupByCategory()).thenReturn(List.of());

        // when
        Map<String, Object> result = assetInfoService.getAssetStatistics();

        // then
        assertThat(result).isNotNull();
        assertThat(result.get("totalAssets")).isEqualTo(0L);
        assertThat(result.get("warningCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("领用资产 - 记录操作日志")
    void borrowAsset_shouldRecordOperationLog() {
        // given
        AssetInfo asset = createTestAsset(1L, 10);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any(AssetOperation.class))).thenReturn(true);

        // when
        assetInfoService.borrowAsset(1L, 5, "张三", "user-001", "项目使用");

        // then
        verify(assetOperationService).save(argThat(op -> {
            assertThat(op.getAssetId()).isEqualTo(1L);
            assertThat(op.getOperationType()).isEqualTo(2); // 领用
            assertThat(op.getOperationQuantity()).isEqualTo(5);
            assertThat(op.getBeforeQuantity()).isEqualTo(10);
            assertThat(op.getAfterQuantity()).isEqualTo(5);
            assertThat(op.getOperator()).isEqualTo("张三");
            return true;
        }));
    }

    @Test
    @DisplayName("归还资产 - 状态恢复")
    void returnAsset_shouldRestoreAssetStatus() {
        // given
        AssetInfo asset = createTestAsset(1L, 7);
        asset.setAssetStatus(2); // 领用中状态
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        assetInfoService.returnAsset(1L, 3, "张三");

        // then
        verify(assetInfoMapper).updateById(argThat(a -> a.getAssetStatus() == 1)); // 恢复正常状态
    }

    @Test
    @DisplayName("领用全部库存")
    void borrowAsset_withAllQuantity_shouldSucceed() {
        // given
        AssetInfo asset = createTestAsset(1L, 5);
        when(assetInfoMapper.selectById(1L)).thenReturn(asset);
        when(assetInfoMapper.updateById(any())).thenReturn(1);
        when(assetOperationService.save(any())).thenReturn(true);

        // when
        boolean result = assetInfoService.borrowAsset(1L, 5, "张三", "user-001", "全部领用");

        // then
        assertThat(result).isTrue();
    }
}