package com.aioa.integration;

import com.aioa.integration.entity.IntegrationConfig;
import com.aioa.integration.entity.IntegrationLog;
import com.aioa.integration.repository.IntegrationConfigRepository;
import com.aioa.integration.repository.IntegrationLogRepository;
import com.aioa.integration.service.IntegrationService;
import com.aioa.integration.dto.IntegrationConfigDTO;
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
class IntegrationServiceTest {

    @Mock
    private IntegrationConfigRepository configRepo;

    @Mock
    private IntegrationLogRepository logRepo;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private IntegrationService integrationService;

    @Test
    void testGetConfig_Found() {
        IntegrationConfig config = new IntegrationConfig();
        config.setId(1L);
        config.setName("Test Integration");
        when(configRepo.findById(1L)).thenReturn(Optional.of(config));

        IntegrationConfig result = integrationService.getConfig(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetConfig_NotFound() {
        when(configRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.aioa.common.BusinessException.class, () -> integrationService.getConfig(999L));
    }
}
