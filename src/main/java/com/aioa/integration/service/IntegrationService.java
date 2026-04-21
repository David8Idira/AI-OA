package com.aioa.integration.service;

import com.aioa.common.BusinessException;
import com.aioa.integration.dto.IntegrationConfigDTO;
import com.aioa.integration.dto.IntegrationInvokeDTO;
import com.aioa.integration.entity.IntegrationConfig;
import com.aioa.integration.entity.IntegrationLog;
import com.aioa.integration.repository.IntegrationConfigRepository;
import com.aioa.integration.repository.IntegrationLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Tag(name = "集成服务", description = "第三方系统集成服务")
public class IntegrationService {

    private final IntegrationConfigRepository configRepo;
    private final IntegrationLogRepository logRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public IntegrationService(IntegrationConfigRepository configRepo,
                                IntegrationLogRepository logRepo,
                                ObjectMapper objectMapper) {
        this.configRepo = configRepo;
        this.logRepo = logRepo;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "创建集成配置")
    @Transactional
    public IntegrationConfig createConfig(IntegrationConfigDTO dto) {
        if (configRepo.existsByIntegrationKey(dto.getIntegrationKey())) {
            throw new BusinessException(409, "集成标识已存在");
        }
        IntegrationConfig config = new IntegrationConfig();
        config.setIntegrationKey(dto.getIntegrationKey());
        config.setName(dto.getName());
        config.setProvider(dto.getProvider());
        config.setConfigJson(dto.getConfigJson());
        config.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        return configRepo.save(config);
    }

    @Operation(summary = "更新集成配置")
    @Transactional
    public IntegrationConfig updateConfig(Long id, IntegrationConfigDTO dto) {
        IntegrationConfig config = configRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "集成配置不存在"));
        config.setName(dto.getName());
        config.setProvider(dto.getProvider());
        config.setConfigJson(dto.getConfigJson());
        if (dto.getEnabled() != null) config.setEnabled(dto.getEnabled());
        return configRepo.save(config);
    }

    @Operation(summary = "删除集成配置")
    @Transactional
    public void deleteConfig(Long id) {
        if (!configRepo.existsById(id)) {
            throw new BusinessException(404, "集成配置不存在");
        }
        configRepo.deleteById(id);
    }

    @Operation(summary = "获取集成配置列表")
    public List<IntegrationConfig> listConfigs() {
        return configRepo.findByEnabledTrue();
    }

    @Operation(summary = "获取单个集成配置")
    public IntegrationConfig getConfig(Long id) {
        return configRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "集成配置不存在"));
    }

    @Operation(summary = "获取集成配置ByKey")
    public IntegrationConfig getConfigByKey(String integrationKey) {
        return configRepo.findByIntegrationKey(integrationKey)
                .orElseThrow(() -> new BusinessException(404, "集成配置不存在"));
    }

    @Operation(summary = "调用第三方系统")
    @Transactional
    public IntegrationLog invoke(IntegrationInvokeDTO dto) {
        IntegrationConfig config = configRepo.findByIntegrationKey(dto.getIntegrationKey())
                .orElseThrow(() -> new BusinessException(404, "集成配置不存在"));

        if (!config.getEnabled()) {
            throw new BusinessException(400, "集成配置已禁用");
        }

        IntegrationLog log = new IntegrationLog();
        log.setConfigId(config.getId());
        log.setMethod(dto.getMethod().toUpperCase());
        log.setEndpoint(dto.getEndpoint());
        log.setRequestBody(dto.getRequestBody());

        long start = System.currentTimeMillis();
        try {
            String baseUrl = extractBaseUrl(config.getConfigJson());
            String fullUrl = baseUrl + dto.getEndpoint();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            if (dto.getHeaders() != null) {
                Map<String, String> headerMap = objectMapper.readValue(dto.getHeaders(), Map.class);
                headerMap.forEach(headers::add);
            }

            org.springframework.http.HttpEntity<String> entity =
                new org.springframework.http.HttpEntity<>(dto.getRequestBody(), headers);

            org.springframework.http.ResponseEntity<String> response;
            switch (dto.getMethod().toUpperCase()) {
                case "GET" -> response = restTemplate.exchange(fullUrl, org.springframework.http.HttpMethod.GET, entity, String.class);
                case "POST" -> response = restTemplate.exchange(fullUrl, org.springframework.http.HttpMethod.POST, entity, String.class);
                case "PUT" -> response = restTemplate.exchange(fullUrl, org.springframework.http.HttpMethod.PUT, entity, String.class);
                case "DELETE" -> response = restTemplate.exchange(fullUrl, org.springframework.http.HttpMethod.DELETE, entity, String.class);
                default -> throw new BusinessException(400, "不支持的HTTP方法");
            }

            log.setResponseBody(response.getBody());
            log.setStatusCode(response.getStatusCode().value());
            log.setDuration(System.currentTimeMillis() - start);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.setStatusCode(-1);
            log.setErrorMessage(e.getMessage());
            log.setDuration(System.currentTimeMillis() - start);
        }

        return logRepo.save(log);
    }

    @Operation(summary = "获取调用日志")
    public List<IntegrationLog> getLogs(Long configId) {
        return logRepo.findByConfigIdOrderByCreatedAtDesc(configId);
    }

    private String extractBaseUrl(String configJson) {
        try {
            if (configJson == null || configJson.isEmpty()) return "";
            Map<String, String> config = objectMapper.readValue(configJson, Map.class);
            return config.getOrDefault("baseUrl", "");
        } catch (Exception e) {
            return "";
        }
    }
}
