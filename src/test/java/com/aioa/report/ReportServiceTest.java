package com.aioa.report;

import com.aioa.report.entity.ReportDefinition;
import com.aioa.report.entity.ReportExecution;
import com.aioa.report.repository.ReportDefinitionRepository;
import com.aioa.report.repository.ReportExecutionRepository;
import com.aioa.report.service.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportDefinitionRepository definitionRepo;

    @Mock
    private ReportExecutionRepository executionRepo;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ReportService reportService;

    @Test
    void testGetDefinition_Found() {
        ReportDefinition def = new ReportDefinition();
        def.setId(1L);
        def.setName("Test Report");
        when(definitionRepo.findById(1L)).thenReturn(Optional.of(def));

        ReportDefinition result = reportService.getDefinition(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetDefinition_NotFound() {
        when(definitionRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.aioa.common.BusinessException.class, () -> reportService.getDefinition(999L));
    }
}
