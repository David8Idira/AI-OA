package com.aioa.workflow.service;

import com.aioa.workflow.dto.N8nWorkflowDTO;
import com.aioa.workflow.service.impl.N8nWorkflowServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * N8nWorkflowServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试n8n工作流服务
 */
@DisplayName("N8nWorkflowServiceImplTest 单元测试")
class N8nWorkflowServiceImplTest {

    private N8nWorkflowServiceImpl workflowService;

    @BeforeEach
    void setUp() {
        workflowService = new N8nWorkflowServiceImpl();
        ReflectionTestUtils.setField(workflowService, "n8nBaseUrl", "http://localhost:5678");
        ReflectionTestUtils.setField(workflowService, "n8nApiKey", "test-api-key");
    }

    @Test
    @DisplayName("触发工作流 - 不存在")
    void triggerWorkflow_withNonExisting_shouldReturnError() {
        // when
        String result = workflowService.triggerWorkflow("non-existing", null);

        // then
        assertThat(result).isEqualTo("工作流不存在");
    }

    @Test
    @DisplayName("触发工作流 - 已禁用")
    void triggerWorkflow_withDisabled_shouldReturnError() {
        // given
        N8nWorkflowDTO workflow = new N8nWorkflowDTO();
        workflow.setWorkflowId("wf-001");
        workflow.setName("测试工作流");
        workflow.setWebhookUrl("http://example.com/webhook");
        workflow.setEnabled(false);
        workflowService.registerWorkflow(workflow);

        // when
        String result = workflowService.triggerWorkflow("wf-001", null);

        // then
        assertThat(result).isEqualTo("工作流已禁用");
    }

    @Test
    @DisplayName("注册工作流 - 正常场景")
    void registerWorkflow_shouldReturnTrue() {
        // given
        N8nWorkflowDTO workflow = new N8nWorkflowDTO();
        workflow.setWorkflowId("wf-001");
        workflow.setName("测试工作流");
        workflow.setWebhookUrl("http://example.com/webhook");
        workflow.setEnabled(true);

        // when
        boolean result = workflowService.registerWorkflow(workflow);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("触发审批工作流 - 正常场景")
    void triggerApprovalWorkflow_shouldReturnResult() {
        // when
        String result = workflowService.triggerApprovalWorkflow("approval-001", "approve");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取工作流状态 - 正常场景")
    void getWorkflowStatus_shouldReturnStatus() {
        // when
        String result = workflowService.getWorkflowStatus("wf-001");

        // then
        assertThat(result).isNotNull();
    }
}