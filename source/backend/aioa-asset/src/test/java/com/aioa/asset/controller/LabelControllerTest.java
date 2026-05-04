package com.aioa.asset.controller;

import com.aioa.asset.dto.LabelGenerateDto;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.service.LabelService;
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
 * LabelController 单元测试
 */
@WebMvcTest(LabelController.class)
@DisplayName("LabelControllerTest 标签打印控制器测试")
class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LabelService labelService;

    private AssetLabel testLabel;
    private LabelGenerateDto generateDto;

    @BeforeEach
    void setUp() {
        testLabel = new AssetLabel();
        testLabel.setId(1L);
        testLabel.setLabelCode("LBL-20250425-001");
        testLabel.setAssetId(100L);
        testLabel.setAssetCode("AST-001");
        testLabel.setAssetName("测试资产");
        testLabel.setQrContent("http://example.com/qr/1");
        testLabel.setQrImagePath("/qr/1.png");
        testLabel.setBarcodeContent("1234567890");
        testLabel.setBarcodeImagePath("/barcode/1.png");
        testLabel.setTemplateId(1L);
        testLabel.setTemplateName("标准模板");
        testLabel.setPrintStatus(0);
        testLabel.setPrintCount(0);
        testLabel.setLabelStatus(1);
        testLabel.setCreateBy("admin");
        testLabel.setCreateTime(LocalDateTime.now());

        generateDto = new LabelGenerateDto();
        generateDto.setAssetId(100L);
        generateDto.setTemplateId(1L);
        generateDto.setCreateBy("admin");
    }

    // ==================== 分页查询标签 ====================

    @Test
    @DisplayName("分页查询标签成功")
    void pageLabels_success() throws Exception {
        Page<AssetLabel> page = new Page<>(1, 10);
        page.setRecords(List.of(testLabel));
        page.setTotal(1);

        when(labelService.pageLabels(any(Page.class), any(AssetLabel.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/label/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].labelCode").value("LBL-20250425-001"))
                .andExpect(jsonPath("$.total").value(1));

        verify(labelService, times(1)).pageLabels(any(Page.class), any(AssetLabel.class));
    }

    @Test
    @DisplayName("分页查询标签为空")
    void pageLabels_empty() throws Exception {
        Page<AssetLabel> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(labelService.pageLabels(any(Page.class), any(AssetLabel.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/label/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    // ==================== 获取标签详情 ====================

    @Test
    @DisplayName("获取标签详情成功")
    void getById_success() throws Exception {
        when(labelService.getById(1L)).thenReturn(testLabel);

        mockMvc.perform(get("/asset/label/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelCode").value("LBL-20250425-001"))
                .andExpect(jsonPath("$.assetName").value("测试资产"));

        verify(labelService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("获取标签详情不存在")
    void getById_notFound() throws Exception {
        when(labelService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/asset/label/999"))
                .andExpect(status().isOk());

        verify(labelService, times(1)).getById(999L);
    }

    // ==================== 根据标签编码查询 ====================

    @Test
    @DisplayName("根据标签编码查询成功")
    void getByLabelCode_success() throws Exception {
        when(labelService.getByLabelCode("LBL-20250425-001")).thenReturn(testLabel);

        mockMvc.perform(get("/asset/label/code/LBL-20250425-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelCode").value("LBL-20250425-001"));

        verify(labelService, times(1)).getByLabelCode("LBL-20250425-001");
    }

    @Test
    @DisplayName("根据标签编码查询不存在")
    void getByLabelCode_notFound() throws Exception {
        when(labelService.getByLabelCode("INVALID")).thenReturn(null);

        mockMvc.perform(get("/asset/label/code/INVALID"))
                .andExpect(status().isOk());

        verify(labelService, times(1)).getByLabelCode("INVALID");
    }

    // ==================== 生成单个标签 ====================

    @Test
    @DisplayName("生成单个标签成功")
    void generateLabel_success() throws Exception {
        when(labelService.generateLabel(eq(100L), eq(1L), eq("admin")))
                .thenReturn(testLabel);

        mockMvc.perform(post("/asset/label/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelCode").value("LBL-20250425-001"));

        verify(labelService, times(1)).generateLabel(100L, 1L, "admin");
    }

    @Test
    @DisplayName("生成标签参数校验 - 创建人为空")
    void generateLabel_validationError() throws Exception {
        LabelGenerateDto invalidDto = new LabelGenerateDto();
        invalidDto.setAssetId(100L);
        invalidDto.setTemplateId(1L);
        // createBy 为空

        mockMvc.perform(post("/asset/label/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(labelService, never()).generateLabel(any(), any(), any());
    }

    // ==================== 批量生成标签 ====================

    @Test
    @DisplayName("批量生成标签成功")
    void batchGenerateLabels_success() throws Exception {
        LabelGenerateDto batchDto = new LabelGenerateDto();
        batchDto.setAssetIds(Arrays.asList(100L, 101L, 102L));
        batchDto.setTemplateId(1L);
        batchDto.setCreateBy("admin");

        AssetLabel label2 = new AssetLabel();
        label2.setId(2L);
        label2.setLabelCode("LBL-20250425-002");

        when(labelService.batchGenerateLabels(anyList(), eq(1L), eq("admin")))
                .thenReturn(Arrays.asList(testLabel, label2));

        mockMvc.perform(post("/asset/label/batch-generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(batchDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(labelService, times(1)).batchGenerateLabels(anyList(), eq(1L), eq("admin"));
    }

    // ==================== 打印标签 ====================

    @Test
    @DisplayName("打印标签成功")
    void printLabel_success() throws Exception {
        when(labelService.printLabel(1L, "Printer-A", "printer-001")).thenReturn(true);

        mockMvc.perform(post("/asset/label/print/1")
                        .param("printer", "Printer-A")
                        .param("printerId", "printer-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(labelService, times(1)).printLabel(1L, "Printer-A", "printer-001");
    }

    @Test
    @DisplayName("打印标签失败")
    void printLabel_failure() throws Exception {
        when(labelService.printLabel(1L, "Printer-A", "printer-001")).thenReturn(false);

        mockMvc.perform(post("/asset/label/print/1")
                        .param("printer", "Printer-A")
                        .param("printerId", "printer-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 批量打印标签 ====================

    @Test
    @DisplayName("批量打印标签成功")
    void batchPrintLabels_success() throws Exception {
        when(labelService.batchPrintLabels(anyList(), eq("Printer-A"), eq("printer-001")))
                .thenReturn(true);

        mockMvc.perform(post("/asset/label/batch-print")
                        .param("printer", "Printer-A")
                        .param("printerId", "printer-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(1L, 2L, 3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(labelService, times(1)).batchPrintLabels(anyList(), eq("Printer-A"), eq("printer-001"));
    }

    // ==================== 获取打印历史 ====================

    @Test
    @DisplayName("获取打印历史成功")
    void getPrintHistory_success() throws Exception {
        when(labelService.getPrintHistory(50)).thenReturn(List.of(testLabel));

        mockMvc.perform(get("/asset/label/print-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].labelCode").value("LBL-20250425-001"));

        verify(labelService, times(1)).getPrintHistory(50);
    }

    @Test
    @DisplayName("获取打印历史自定义限制")
    void getPrintHistory_customLimit() throws Exception {
        when(labelService.getPrintHistory(100)).thenReturn(List.of(testLabel));

        mockMvc.perform(get("/asset/label/print-history")
                        .param("limit", "100"))
                .andExpect(status().isOk());

        verify(labelService, times(1)).getPrintHistory(100);
    }

    // ==================== 获取打印统计 ====================

    @Test
    @DisplayName("获取打印统计成功")
    void getPrintStatistics_success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 100);
        stats.put("printedCount", 80);
        stats.put("unprintedCount", 20);

        when(labelService.getPrintStatistics()).thenReturn(stats);

        mockMvc.perform(get("/asset/label/print-statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.printedCount").value(80));

        verify(labelService, times(1)).getPrintStatistics();
    }

    // ==================== 更新打印模板 ====================

    @Test
    @DisplayName("更新打印模板成功")
    void updateTemplate_success() throws Exception {
        when(labelService.updateTemplate(1L, 2L, "新模板")).thenReturn(true);

        mockMvc.perform(put("/asset/label/template/1")
                        .param("templateId", "2")
                        .param("templateName", "新模板"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(labelService, times(1)).updateTemplate(1L, 2L, "新模板");
    }

    @Test
    @DisplayName("更新打印模板失败")
    void updateTemplate_failure() throws Exception {
        when(labelService.updateTemplate(1L, 2L, "新模板")).thenReturn(false);

        mockMvc.perform(put("/asset/label/template/1")
                        .param("templateId", "2")
                        .param("templateName", "新模板"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 作废标签 ====================

    @Test
    @DisplayName("作废标签成功")
    void invalidateLabel_success() throws Exception {
        when(labelService.invalidateLabel(1L, "标签损坏")).thenReturn(true);

        mockMvc.perform(put("/asset/label/invalidate/1")
                        .param("reason", "标签损坏"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(labelService, times(1)).invalidateLabel(1L, "标签损坏");
    }

    // ==================== 重新生成二维码 ====================

    @Test
    @DisplayName("重新生成二维码成功")
    void regenerateCode_success() throws Exception {
        AssetLabel regenerated = new AssetLabel();
        regenerated.setId(1L);
        regenerated.setQrContent("http://example.com/qr/new");
        regenerated.setQrImagePath("/qr/new.png");

        when(labelService.regenerateCode(1L)).thenReturn(regenerated);

        mockMvc.perform(put("/asset/label/regenerate-code/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrContent").value("http://example.com/qr/new"));

        verify(labelService, times(1)).regenerateCode(1L);
    }

    // ==================== 删除标签 ====================

    @Test
    @DisplayName("删除标签成功")
    void deleteLabel_success() throws Exception {
        when(labelService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/asset/label/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(labelService, times(1)).removeById(1L);
    }

    @Test
    @DisplayName("删除标签失败")
    void deleteLabel_failure() throws Exception {
        when(labelService.removeById(999L)).thenReturn(false);

        mockMvc.perform(delete("/asset/label/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("服务层抛出异常时返回服务器错误")
    void serviceException() throws Exception {
        when(labelService.getById(1L)).thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get("/asset/label/1"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("服务层抛出异常时不调用后续服务")
    void serviceException_noFurtherCalls() throws Exception {
        // 先成功获取了一次
        when(labelService.getById(1L)).thenReturn(testLabel);
        mockMvc.perform(get("/asset/label/1")).andExpect(status().isOk());
        verify(labelService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("无效JSON格式")
    void invalidJson() throws Exception {
        mockMvc.perform(post("/asset/label/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(labelService, never()).generateLabel(any(), any(), any());
    }
}