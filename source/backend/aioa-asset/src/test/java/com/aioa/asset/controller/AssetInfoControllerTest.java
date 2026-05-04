package com.aioa.asset.controller;

import com.aioa.asset.dto.AssetBorrowDTO;
import com.aioa.asset.dto.AssetReturnDTO;
import com.aioa.asset.dto.AssetTransferDTO;
import com.aioa.asset.dto.AssetScrapDTO;
import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.service.AssetInfoService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AssetInfoController 单元测试
 */
@WebMvcTest(AssetInfoController.class)
@DisplayName("AssetInfoControllerTest 资产信息控制器测试")
class AssetInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssetInfoService assetInfoService;

    private AssetInfo testAsset;

    @BeforeEach
    void setUp() {
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("AST-001");
        testAsset.setAssetName("ThinkPad T490");
        testAsset.setCategoryId(1L);
        testAsset.setPurchaseDate(java.time.LocalDate.now().minusMonths(6));
        testAsset.setPurchasePrice(BigDecimal.valueOf(8000.00));
        testAsset.setStatus(1);
        testAsset.setCreateTime(LocalDateTime.now());
    }

    // ==================== 分页查询资产 ====================

    @Test
    @DisplayName("分页查询资产成功")
    void pageAssets_success() throws Exception {
        Page<AssetInfo> page = new Page<>(1, 10);
        page.setRecords(List.of(testAsset));
        page.setTotal(1);

        when(assetInfoService.pageAssets(any(Page.class), any(AssetInfo.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/info/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].assetName").value("ThinkPad T490"))
                .andExpect(jsonPath("$.total").value(1));

        verify(assetInfoService, times(1)).pageAssets(any(Page.class), any(AssetInfo.class));
    }

    @Test
    @DisplayName("分页查询资产为空")
    void pageAssets_empty() throws Exception {
        Page<AssetInfo> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(assetInfoService.pageAssets(any(Page.class), any(AssetInfo.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/info/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(0));
    }

    // ==================== 获取资产详情 ====================

    @Test
    @DisplayName("获取资产详情成功")
    void getById_success() throws Exception {
        when(assetInfoService.getById(1L)).thenReturn(testAsset);

        mockMvc.perform(get("/asset/info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetName").value("ThinkPad T490"));

        verify(assetInfoService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("获取资产详情不存在")
    void getById_notFound() throws Exception {
        when(assetInfoService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/asset/info/999"))
                .andExpect(status().isOk());

        verify(assetInfoService, times(1)).getById(999L);
    }

    // ==================== 创建资产 ====================

    @Test
    @DisplayName("创建资产成功")
    void createAsset_success() throws Exception {
        when(assetInfoService.save(any(AssetInfo.class))).thenReturn(true);

        mockMvc.perform(post("/asset/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAsset)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).save(any(AssetInfo.class));
    }

    // ==================== 更新资产 ====================

    @Test
    @DisplayName("更新资产成功")
    void updateAsset_success() throws Exception {
        AssetInfo updateData = new AssetInfo();
        updateData.setAssetName("ThinkPad T490 更新版");

        when(assetInfoService.updateById(any(AssetInfo.class))).thenReturn(true);

        mockMvc.perform(put("/asset/info/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).updateById(any(AssetInfo.class));
    }

    @Test
    @DisplayName("更新资产不存在")
    void updateAsset_notFound() throws Exception {
        AssetInfo updateData = new AssetInfo();
        updateData.setAssetName("不存在的资产");

        when(assetInfoService.updateById(any(AssetInfo.class))).thenReturn(false);

        mockMvc.perform(put("/asset/info/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 删除资产 ====================

    @Test
    @DisplayName("删除资产成功")
    void deleteAsset_success() throws Exception {
        when(assetInfoService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/asset/info/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("删除资产不存在")
    void deleteAsset_notFound() throws Exception {
        when(assetInfoService.removeById(999L)).thenReturn(false);

        mockMvc.perform(delete("/asset/info/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 获取资产预警列表 ====================

    @Test
    @DisplayName("获取资产预警列表成功")
    void getWarningAssets_success() throws Exception {
        when(assetInfoService.getWarningAssets())
                .thenReturn(List.of(testAsset));

        mockMvc.perform(get("/asset/info/warning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetName").value("ThinkPad T490"));

        verify(assetInfoService, times(1)).getWarningAssets();
    }

    @Test
    @DisplayName("获取资产预警列表为空")
    void getWarningAssets_empty() throws Exception {
        when(assetInfoService.getWarningAssets())
                .thenReturn(List.of());

        mockMvc.perform(get("/asset/info/warning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== 领用资产 ====================

    @Test
    @DisplayName("领用资产成功")
    void borrowAsset_success() throws Exception {
        AssetBorrowDTO dto = new AssetBorrowDTO();
        dto.setAssetId(1L);
        dto.setQuantity(1);
        dto.setOperator("张三");
        dto.setOperatorId("user001");
        dto.setReason("日常工作使用");

        when(assetInfoService.borrowAsset(eq(1L), eq(1), eq("张三"), eq("user001"), eq("日常工作使用")))
                .thenReturn(true);

        mockMvc.perform(post("/asset/info/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).borrowAsset(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("领用资产参数校验")
    void borrowAsset_validationFail() throws Exception {
        AssetBorrowDTO dto = new AssetBorrowDTO();
        // 缺少必填字段

        mockMvc.perform(post("/asset/info/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(assetInfoService, never()).borrowAsset(any(), any(), any(), any(), any());
    }

    // ==================== 归还资产 ====================

    @Test
    @DisplayName("归还资产成功")
    void returnAsset_success() throws Exception {
        AssetReturnDTO dto = new AssetReturnDTO();
        dto.setAssetId(1L);
        dto.setQuantity(1);
        dto.setOperator("张三");

        when(assetInfoService.returnAsset(eq(1L), eq(1), eq("张三")))
                .thenReturn(true);

        mockMvc.perform(post("/asset/info/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).returnAsset(any(), any(), any());
    }

    // ==================== 调拨资产 ====================

    @Test
    @DisplayName("调拨资产成功")
    void transferAsset_success() throws Exception {
        AssetTransferDTO dto = new AssetTransferDTO();
        dto.setAssetId(1L);
        dto.setQuantity(1);
        dto.setOperator("李四");
        dto.setOperatorId("user002");
        dto.setTargetDepartment("市场部");
        dto.setReason("部门调拨");

        when(assetInfoService.transferAsset(eq(1L), eq(1), eq("李四"), eq("user002"), eq("市场部"), eq("部门调拨")))
                .thenReturn(true);

        mockMvc.perform(post("/asset/info/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).transferAsset(any(), any(), any(), any(), any(), any());
    }

    // ==================== 报废资产 ====================

    @Test
    @DisplayName("报废资产成功")
    void scrapAsset_success() throws Exception {
        AssetScrapDTO dto = new AssetScrapDTO();
        dto.setAssetId(1L);
        dto.setQuantity(1);
        dto.setOperator("王五");
        dto.setOperatorId("user003");
        dto.setReason("设备老化报废");

        when(assetInfoService.scrapAsset(eq(1L), eq(1), eq("王五"), eq("user003"), eq("设备老化报废")))
                .thenReturn(true);

        mockMvc.perform(post("/asset/info/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(assetInfoService, times(1)).scrapAsset(any(), any(), any(), any(), any());
    }

    // ==================== 获取资产统计 ====================

    @Test
    @DisplayName("获取资产统计成功")
    void getAssetStatistics_success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 100);
        stats.put("normalCount", 80);
        stats.put("warningCount", 15);
        stats.put("scrappedCount", 5);
        stats.put("totalValue", BigDecimal.valueOf(500000.00));

        when(assetInfoService.getAssetStatistics()).thenReturn(stats);

        mockMvc.perform(get("/asset/info/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.normalCount").value(80))
                .andExpect(jsonPath("$.totalValue").value(500000.00));

        verify(assetInfoService, times(1)).getAssetStatistics();
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("无效JSON格式")
    void invalidJson() throws Exception {
        mockMvc.perform(post("/asset/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(assetInfoService, never()).save(any(AssetInfo.class));
    }
}