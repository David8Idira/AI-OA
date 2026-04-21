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

    private N8nWorkflowDTO createTestWorkflow() {
        N8nWorkflowDTO workflow = new N8nWorkflowDTO();
        workflow.setWorkflowId("wf-001");
        workflow.setName("测试工作流");
        workflow.setWebhookUrl("http://example.com/webhook");
        workflow.setEnabled(true);
        return workflow;
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
        N8nWorkflowDTO workflow = createTestWorkflow();
        workflow.setEnabled(false);
        workflowService.registerWorkflow(workflow);

        // when
        String result = workflowService.triggerWorkflow("wf-001", null);

        // then
        assertThat(result).isEqualTo("工作流已禁用");
    }

    @Test
    @DisplayName("触发工作流 - 已注册且启用")
    void triggerWorkflow_withEnabled_shouldCallWebhook() {
        // given
        N8nWorkflowDTO workflow = createTestWorkflow();
        workflowService.registerWorkflow(workflow);

        // when
        String result = workflowService.triggerWorkflow("wf-001", "test-data");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("注册工作流 - 正常场景")
    void registerWorkflow_shouldReturnTrue() {
        // given
        N8nWorkflowDTO workflow = createTestWorkflow();

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
    @DisplayName("触发审批工作流 - 驳回")
    void triggerApprovalWorkflow_withReject_shouldReturnResult() {
        // when
        String result = workflowService.triggerApprovalWorkflow("approval-002", "reject");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取工作流状态 - 已注册")
    void getWorkflowStatus_withRegisteredWorkflow_shouldReturnStatus() {
        // given
        N8nWorkflowDTO workflow = createTestWorkflow();
        workflowService.registerWorkflow(workflow);

        // when
        String result = workflowService.getWorkflowStatus("wf-001");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取工作流状态 - 未注册")
    void getWorkflowStatus_withUnregisteredWorkflow_shouldReturnNotFound() {
        // when
        String result = workflowService.getWorkflowStatus("non-existing");

        // then
        assertThat(result).isEqualTo("工作流不存在");
    }

    @Test
    @DisplayName("发送Webhook - 正常URL")
    void sendWebhook_withValidUrl_shouldReturnResponse() {
        // when
        String result = workflowService.sendWebhook("http://example.com/webhook", "test-data");

        // then
        assertThat(result).isNotNull();
    }
}