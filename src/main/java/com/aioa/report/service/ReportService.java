package com.aioa.report.service;

import com.aioa.common.BusinessException;
import com.aioa.report.dto.ReportDefinitionDTO;
import com.aioa.report.dto.ReportExecuteDTO;
import com.aioa.report.entity.ReportDefinition;
import com.aioa.report.entity.ReportExecution;
import com.aioa.report.repository.ReportDefinitionRepository;
import com.aioa.report.repository.ReportExecutionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Tag(name = "报表服务", description = "报表定义与执行服务")
public class ReportService {

    private final ReportDefinitionRepository definitionRepo;
    private final ReportExecutionRepository executionRepo;
    private final ObjectMapper objectMapper;

    public ReportService(ReportDefinitionRepository definitionRepo,
                         ReportExecutionRepository executionRepo,
                         ObjectMapper objectMapper) {
        this.definitionRepo = definitionRepo;
        this.executionRepo = executionRepo;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "创建报表定义")
    @Transactional
    public ReportDefinition createDefinition(ReportDefinitionDTO dto) {
        if (definitionRepo.existsByReportKey(dto.getReportKey())) {
            throw new BusinessException(409, "报表标识已存在");
        }
        ReportDefinition entity = new ReportDefinition();
        entity.setName(dto.getName());
        entity.setReportKey(dto.getReportKey());
        entity.setDescription(dto.getDescription());
        entity.setSqlTemplate(dto.getSqlTemplate());
        entity.setChartConfig(dto.getChartConfig());
        entity.setActive(dto.getActive() != null ? dto.getActive() : true);
        return definitionRepo.save(entity);
    }

    @Operation(summary = "更新报表定义")
    @Transactional
    public ReportDefinition updateDefinition(Long id, ReportDefinitionDTO dto) {
        ReportDefinition entity = definitionRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "报表定义不存在"));
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setSqlTemplate(dto.getSqlTemplate());
        entity.setChartConfig(dto.getChartConfig());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        return definitionRepo.save(entity);
    }

    @Operation(summary = "删除报表定义")
    @Transactional
    public void deleteDefinition(Long id) {
        if (!definitionRepo.existsById(id)) {
            throw new BusinessException(404, "报表定义不存在");
        }
        definitionRepo.deleteById(id);
    }

    @Operation(summary = "获取报表定义列表")
    public List<ReportDefinition> listDefinitions() {
        return definitionRepo.findByActiveTrue();
    }

    @Operation(summary = "获取单个报表定义")
    public ReportDefinition getDefinition(Long id) {
        return definitionRepo.findById(id)
                .orElseThrow(() -> new BusinessException(404, "报表定义不存在"));
    }

    @Operation(summary = "执行报表")
    @Transactional
    public ReportExecution executeReport(ReportExecuteDTO dto) {
        ReportDefinition definition = definitionRepo.findById(dto.getReportId())
                .orElseThrow(() -> new BusinessException(404, "报表定义不存在"));

        ReportExecution execution = new ReportExecution();
        execution.setReportId(dto.getReportId());
        execution.setParameters(serializeParams(dto.getParameters()));
        execution.setStatus(0);

        long start = System.currentTimeMillis();
        try {
            String resultData = simulateReportExecution(definition.getSqlTemplate(), dto.getParameters());
            execution.setResultData(resultData);
            execution.setStatus(1);
            execution.setExecutionTime(System.currentTimeMillis() - start);
            return executionRepo.save(execution);
        } catch (Exception e) {
            execution.setStatus(-1);
            execution.setErrorMessage(e.getMessage());
            execution.setExecutionTime(System.currentTimeMillis() - start);
            return executionRepo.save(execution);
        }
    }

    @Operation(summary = "获取报表执行历史")
    public List<ReportExecution> getExecutionHistory(Long reportId) {
        return executionRepo.findByReportIdOrderByCreatedAtDesc(reportId);
    }

    private String serializeParams(Map<String, Object> params) {
        try {
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }

    private String simulateReportExecution(String sqlTemplate, Map<String, Object> params) {
        return "{\"columns\":[\"name\",\"value\"],\"data\":[[\"项目A\",120],[\"项目B\",85],[\"项目C\",200]]}";
    }
}
