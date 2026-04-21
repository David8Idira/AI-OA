package com.aioa.integration.controller;

import com.aioa.common.ApiResponse;
import com.aioa.integration.dto.IntegrationConfigDTO;
import com.aioa.integration.dto.IntegrationInvokeDTO;
import com.aioa.integration.entity.IntegrationConfig;
import com.aioa.integration.entity.IntegrationLog;
import com.aioa.integration.service.IntegrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/integrations")
@Tag(name = "集成模块", description = "第三方系统集成API")
public class IntegrationController {

    private final IntegrationService integrationService;

    public IntegrationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping("/configs")
    @Operation(summary = "创建集成配置")
    public ApiResponse<IntegrationConfig> createConfig(@Valid @RequestBody IntegrationConfigDTO dto) {
        return ApiResponse.created(integrationService.createConfig(dto));
    }

    @PutMapping("/configs/{id}")
    @Operation(summary = "更新集成配置")
    public ApiResponse<IntegrationConfig> updateConfig(
            @Parameter(description = "配置ID") @PathVariable Long id,
            @Valid @RequestBody IntegrationConfigDTO dto) {
        return ApiResponse.success(integrationService.updateConfig(id, dto));
    }

    @DeleteMapping("/configs/{id}")
    @Operation(summary = "删除集成配置")
    public ApiResponse<Void> deleteConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        integrationService.deleteConfig(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/configs")
    @Operation(summary = "获取集成配置列表")
    public ApiResponse<List<IntegrationConfig>> listConfigs() {
        return ApiResponse.success(integrationService.listConfigs());
    }

    @GetMapping("/configs/{id}")
    @Operation(summary = "获取单个集成配置")
    public ApiResponse<IntegrationConfig> getConfig(@Parameter(description = "配置ID") @PathVariable Long id) {
        return ApiResponse.success(integrationService.getConfig(id));
    }

    @GetMapping("/configs/by-key/{key}")
    @Operation(summary = "获取集成配置ByKey")
    public ApiResponse<IntegrationConfig> getConfigByKey(@Parameter(description = "集成标识") @PathVariable String key) {
        return ApiResponse.success(integrationService.getConfigByKey(key));
    }

    @PostMapping("/invoke")
    @Operation(summary = "调用第三方系统")
    public ApiResponse<IntegrationLog> invoke(@Valid @RequestBody IntegrationInvokeDTO dto) {
        return ApiResponse.success(integrationService.invoke(dto));
    }

    @GetMapping("/configs/{id}/logs")
    @Operation(summary = "获取调用日志")
    public ApiResponse<List<IntegrationLog>> getLogs(@Parameter(description = "配置ID") @PathVariable Long id) {
        return ApiResponse.success(integrationService.getLogs(id));
    }
}
