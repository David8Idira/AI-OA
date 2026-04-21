package com.aioa.integration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "IntegrationInvokeDTO", description = "集成调用DTO")
public class IntegrationInvokeDTO {
    @NotBlank(message = "集成标识不能为空")
    @Schema(description = "集成配置标识")
    private String integrationKey;

    @NotBlank(message = "请求方法不能为空")
    @Schema(description = "HTTP方法")
    private String method;

    @NotBlank(message = "端点不能为空")
    @Schema(description = "调用端点")
    private String endpoint;

    @Schema(description = "请求体JSON")
    private String requestBody;

    @Schema(description = "请求头JSON")
    private String headers;

    public String getIntegrationKey() { return integrationKey; }
    public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }
}
