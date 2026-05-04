package com.aioa.asset.controller;

import com.aioa.asset.entity.AssetCategory;
import com.aioa.asset.service.AssetCategoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AssetCategoryController 单元测试
 */
@WebMvcTest(AssetCategoryController.class)
@DisplayName("AssetCategoryControllerTest 资产分类控制器测试")
class AssetCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetCategoryService assetCategoryService;

    private AssetCategory testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new AssetCategory();
        testCategory.setId(1L);
        testCategory.setCategoryCode("CAT-001");
        testCategory.setCategoryName("办公设备");
        testCategory.setCategoryType(1);
        testCategory.setParentId(0L);
        testCategory.setSortOrder(1);
        testCategory.setStatus(1);
        testCategory.setCreateTime(LocalDateTime.now());
    }

    // ==================== 获取分类树 ====================

    @Test
    @DisplayName("获取分类树成功")
    void getCategoryTree_success() throws Exception {
        AssetCategory child = new AssetCategory();
        child.setId(2L);
        child.setCategoryCode("CAT-002");
        child.setCategoryName("电脑");
        child.setCategoryType(1);
        child.setParentId(1L);

        when(assetCategoryService.getCategoryTree())
                .thenReturn(Arrays.asList(testCategory));

        mockMvc.perform(get("/asset/category/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("办公设备"));

        verify(assetCategoryService, times(1)).getCategoryTree();
    }

    @Test
    @DisplayName("获取分类树为空")
    void getCategoryTree_empty() throws Exception {
        when(assetCategoryService.getCategoryTree())
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/asset/category/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== 分页查询分类 ====================

    @Test
    @DisplayName("分页查询分类成功")
    void pageCategory_success() throws Exception {
        Page<AssetCategory> page = new Page<>(1, 10);
        page.setRecords(List.of(testCategory));
        page.setTotal(1);

        when(assetCategoryService.page(any(Page.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/category/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].categoryName").value("办公设备"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("分页查询分类为空")
    void pageCategory_empty() throws Exception {
        Page<AssetCategory> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(assetCategoryService.page(any(Page.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/category/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(0));
    }

    // ==================== 根据类型获取分类 ====================

    @Test
    @DisplayName("根据类型获取分类成功")
    void getByType_success() throws Exception {
        when(assetCategoryService.getCategoriesByType(1))
                .thenReturn(List.of(testCategory));

        mockMvc.perform(get("/asset/category/type/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("办公设备"));

        verify(assetCategoryService, times(1)).getCategoriesByType(1);
    }

    @Test
    @DisplayName("根据类型获取分类为空")
    void getByType_empty() throws Exception {
        when(assetCategoryService.getCategoriesByType(999))
                .thenReturn(List.of());

        mockMvc.perform(get("/asset/category/type/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== 获取分类详情 ====================

    @Test
    @DisplayName("获取分类详情成功")
    void getById_success() throws Exception {
        when(assetCategoryService.getById(1L))
                .thenReturn(testCategory);

        mockMvc.perform(get("/asset/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName").value("办公设备"));

        verify(assetCategoryService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("获取分类详情不存在")
    void getById_notFound() throws Exception {
        when(assetCategoryService.getById(999L))
                .thenReturn(null);

        mockMvc.perform(get("/asset/category/999"))
                .andExpect(status().isOk());

        verify(assetCategoryService, times(1)).getById(999L);
    }

    // ==================== 创建分类 ====================

    @Test
    @DisplayName("创建分类成功")
    void createCategory_success() throws Exception {
        when(assetCategoryService.save(any(AssetCategory.class)))
                .thenReturn(true);

        mockMvc.perform(post("/asset/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetCategoryService, times(1)).save(any(AssetCategory.class));
    }

    // ==================== 更新分类 ====================

    @Test
    @DisplayName("更新分类成功")
    void updateCategory_success() throws Exception {
        AssetCategory updateData = new AssetCategory();
        updateData.setCategoryName("更新后的分类名称");

        when(assetCategoryService.updateById(any(AssetCategory.class)))
                .thenReturn(true);

        mockMvc.perform(put("/asset/category/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetCategoryService, times(1)).updateById(any(AssetCategory.class));
    }

    @Test
    @DisplayName("更新分类失败")
    void updateCategory_failure() throws Exception {
        AssetCategory updateData = new AssetCategory();
        updateData.setCategoryName("更新后的分类名称");

        when(assetCategoryService.updateById(any(AssetCategory.class)))
                .thenReturn(false);

        mockMvc.perform(put("/asset/category/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 删除分类 ====================

    @Test
    @DisplayName("删除分类成功")
    void deleteCategory_success() throws Exception {
        when(assetCategoryService.removeById(1L))
                .thenReturn(true);

        mockMvc.perform(delete("/asset/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetCategoryService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("删除分类不存在")
    void deleteCategory_notFound() throws Exception {
        when(assetCategoryService.removeById(999L))
                .thenReturn(false);

        mockMvc.perform(delete("/asset/category/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 批量删除分类 ====================

    @Test
    @DisplayName("批量删除分类成功")
    void deleteBatch_success() throws Exception {
        when(assetCategoryService.deleteBatch(Arrays.asList(1L, 2L, 3L)))
                .thenReturn(true);

        mockMvc.perform(delete("/asset/category/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(1L, 2L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetCategoryService, times(1)).deleteBatch(anyList());
    }

    @Test
    @DisplayName("批量删除分类部分失败")
    void deleteBatch_partialFailure() throws Exception {
        when(assetCategoryService.deleteBatch(anyList()))
                .thenReturn(false);

        mockMvc.perform(delete("/asset/category/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(999L, 998L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("无效JSON格式")
    void invalidJson() throws Exception {
        mockMvc.perform(post("/asset/category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(assetCategoryService, never()).save(any(AssetCategory.class));
    }
}