package com.aioa.asset.controller;

import com.aioa.asset.dto.LabelGenerateDto;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.service.LabelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签打印Controller
 */
@Tag(name = "物料标签打印")
@RestController
@RequestMapping("/asset/label")
public class LabelController {
    
    @Autowired
    private LabelService labelService;
    
    @Operation(summary = "分页查询标签")
    @GetMapping("/page")
    public Page<AssetLabel> pageLabels(Page<AssetLabel> page, AssetLabel query) {
        return labelService.pageLabels(page, query);
    }
    
    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public AssetLabel getById(@PathVariable Long id) {
        return labelService.getById(id);
    }
    
    @Operation(summary = "根据标签编码查询")
    @GetMapping("/code/{labelCode}")
    public AssetLabel getByLabelCode(@PathVariable String labelCode) {
        return labelService.getByLabelCode(labelCode);
    }
    
    @Operation(summary = "生成单个标签")
    @PostMapping("/generate")
    public AssetLabel generateLabel(@Validated @RequestBody LabelGenerateDto dto) {
        return labelService.generateLabel(dto.getAssetId(), dto.getTemplateId(), dto.getCreateBy());
    }
    
    @Operation(summary = "批量生成标签")
    @PostMapping("/batch-generate")
    public List<AssetLabel> batchGenerateLabels(@Validated @RequestBody LabelGenerateDto dto) {
        return labelService.batchGenerateLabels(dto.getAssetIds(), dto.getTemplateId(), dto.getCreateBy());
    }
    
    @Operation(summary = "打印标签")
    @PostMapping("/print/{id}")
    public boolean printLabel(@PathVariable Long id,
                             @RequestParam String printer,
                             @RequestParam String printerId) {
        return labelService.printLabel(id, printer, printerId);
    }
    
    @Operation(summary = "批量打印标签")
    @PostMapping("/batch-print")
    public boolean batchPrintLabels(@RequestBody List<Long> labelIds,
                                   @RequestParam String printer,
                                   @RequestParam String printerId) {
        return labelService.batchPrintLabels(labelIds, printer, printerId);
    }
    
    @Operation(summary = "获取打印历史")
    @GetMapping("/print-history")
    public List<AssetLabel> getPrintHistory(@RequestParam(required = false, defaultValue = "50") Integer limit) {
        return labelService.getPrintHistory(limit);
    }
    
    @Operation(summary = "获取打印统计")
    @GetMapping("/print-statistics")
    public Map<String, Object> getPrintStatistics() {
        return labelService.getPrintStatistics();
    }
    
    @Operation(summary = "更新打印模板")
    @PutMapping("/template/{id}")
    public boolean updateTemplate(@PathVariable Long id,
                                 @RequestParam Long templateId,
                                 @RequestParam String templateName) {
        return labelService.updateTemplate(id, templateId, templateName);
    }
    
    @Operation(summary = "作废标签")
    @PutMapping("/invalidate/{id}")
    public boolean invalidateLabel(@PathVariable Long id,
                                  @RequestParam String reason) {
        return labelService.invalidateLabel(id, reason);
    }
    
    @Operation(summary = "重新生成二维码")
    @PutMapping("/regenerate-code/{id}")
    public AssetLabel regenerateCode(@PathVariable Long id) {
        return labelService.regenerateCode(id);
    }
    
    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public boolean deleteLabel(@PathVariable Long id) {
        return labelService.removeById(id);
    }
}