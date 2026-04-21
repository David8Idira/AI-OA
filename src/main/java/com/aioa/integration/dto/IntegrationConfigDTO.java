package com.aioa.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "IntegrationConfigDTO", description = "集成配置DTO")
public class IntegrationConfigDTO {
    @Schema(description = "ID")
    private Long id;

    @NotBlank(message = "集成标识不能为空")
    @Schema(description = "集成唯一标识")
    private String integrationKey;

    @NotBlank(message = "集成名称不能为空")
    @Schema(description = "集成名称")
    private String name;

    @Schema(description = "提供商")
    private String provider;

    @Schema(description = "配置JSON")
    private String configJson;

    @Schema(description = "是否启用")
    private Boolean enabled;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
