package com.aioa.asset.mapper;

import com.aioa.asset.entity.AssetInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * AssetInfoMapper 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetInfoMapper 资产信息Mapper测试")
class AssetInfoMapperTest {

    @Mock
    private AssetInfoMapper assetInfoMapper;

    @Test
    @DisplayName("selectWarningAssets 应返回预警资产列表")
    void selectWarningAssets_shouldReturnWarningAssets() {
        AssetInfo warningAsset = new AssetInfo();
        warningAsset.setId(1L);
        warningAsset.setAssetName("低库存资产");
        warningAsset.setCurrentQuantity(2);
        warningAsset.setWarningQuantity(5);

        when(assetInfoMapper.selectWarningAssets()).thenReturn(List.of(warningAsset));

        List<AssetInfo> result = assetInfoMapper.selectWarningAssets();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssetName()).isEqualTo("低库存资产");
    }

    @Test
    @DisplayName("selectWarningAssets 无预警资产时应返回空列表")
    void selectWarningAssets_withNoWarnings_shouldReturnEmptyList() {
        when(assetInfoMapper.selectWarningAssets()).thenReturn(List.of());

        List<AssetInfo> result = assetInfoMapper.selectWarningAssets();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("groupByCategory 应返回按分类统计的结果")
    void groupByCategory_shouldReturnGroupedResults() {
        Map<String, Object> stats1 = Map.of("categoryId", 1L, "count", 10L, "totalQuantity", 100L);
        Map<String, Object> stats2 = Map.of("categoryId", 2L, "count", 5L, "totalQuantity", 50L);

        when(assetInfoMapper.groupByCategory()).thenReturn(List.of(stats1, stats2));

        List<Map<String, Object>> result = assetInfoMapper.groupByCategory();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("categoryId")).isEqualTo(1L);
        assertThat(result.get(0).get("count")).isEqualTo(10L);
    }

    @Test
    @DisplayName("groupByCategory 无数据时应返回空列表")
    void groupByCategory_withNoData_shouldReturnEmptyList() {
        when(assetInfoMapper.groupByCategory()).thenReturn(List.of());

        List<Map<String, Object>> result = assetInfoMapper.groupByCategory();

        assertThat(result).isEmpty();
    }
}