package com.aioa.asset.service;

import com.aioa.asset.entity.AssetCategory;
import com.aioa.asset.mapper.AssetCategoryMapper;
import com.aioa.asset.service.impl.AssetCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AssetCategoryServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetCategoryServiceImpl 单元测试")
class AssetCategoryServiceImplTest {

    @Mock
    private AssetCategoryMapper assetCategoryMapper;

    private AssetCategoryServiceImpl assetCategoryService;

    @BeforeEach
    void setUp() {
        assetCategoryService = new AssetCategoryServiceImpl();
        ReflectionTestUtils.setField(assetCategoryService, "baseMapper", assetCategoryMapper);
    }

    private AssetCategory createCategory(Long id, Long parentId, String name, Integer sortOrder) {
        AssetCategory category = new AssetCategory();
        category.setId(id);
        category.setParentId(parentId);
        category.setCategoryName(name);
        category.setSortOrder(sortOrder);
        category.setStatus(1);
        category.setCategoryType(1);
        return category;
    }

    @Test
    @DisplayName("获取分类树 - 空数据")
    void getCategoryTree_withEmptyData_shouldReturnEmptyList() {
        // given
        when(assetCategoryMapper.selectList(any())).thenReturn(new ArrayList<>());

        // when
        List<AssetCategory> result = assetCategoryService.getCategoryTree();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("获取分类树 - 根分类")
    void getCategoryTree_withRootCategory_shouldReturnTree() {
        // given
        AssetCategory root = createCategory(1L, null, "根分类", 1);
        when(assetCategoryMapper.selectList(any())).thenReturn(Arrays.asList(root));

        // when
        List<AssetCategory> result = assetCategoryService.getCategoryTree();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryName()).isEqualTo("根分类");
    }

    @Test
    @DisplayName("获取分类树 - 带子分类")
    void getCategoryTree_withChildren_shouldReturnTreeWithChildren() {
        // given
        AssetCategory root = createCategory(1L, null, "根分类", 1);
        AssetCategory child1 = createCategory(2L, 1L, "子分类1", 1);
        AssetCategory child2 = createCategory(3L, 1L, "子分类2", 2);
        when(assetCategoryMapper.selectList(any())).thenReturn(Arrays.asList(root, child1, child2));

        // when
        List<AssetCategory> result = assetCategoryService.getCategoryTree();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("根据类型获取分类列表")
    void getCategoriesByType_shouldReturnFilteredCategories() {
        // given
        AssetCategory category1 = createCategory(1L, null, "类型1分类", 1);
        category1.setCategoryType(1);
        when(assetCategoryMapper.selectList(any())).thenReturn(Arrays.asList(category1));

        // when
        List<AssetCategory> result = assetCategoryService.getCategoriesByType(1);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryType()).isEqualTo(1);
    }
}
