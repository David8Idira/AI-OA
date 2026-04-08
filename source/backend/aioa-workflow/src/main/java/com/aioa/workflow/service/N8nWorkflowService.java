package com.aioa.workflow.service;

import com.aioa.workflow.dto.N8nWorkflowDTO;

/**
 * n8n工作流服务接口
 */
public interface N8nWorkflowService {
    
    /**
     * 触发n8n工作流
     * @param workflowId 工作流ID
     * @param data 触发数据
     * @return 执行结果
     */
    String triggerWorkflow(String workflowId, Object data);
    
    /**
     * 触发审批工作流
     * @param approvalId 审批ID
     * @param action 审批动作
     * @return 结果
     */
    String triggerApprovalWorkflow(String approvalId, String action);
    
    /**
     * 获取工作流状态
     * @param workflowId 工作流ID
     * @return 状态信息
     */
    String getWorkflowStatus(String workflowId);
    
    /**
     * 注册工作流
     * @param dto 配置
     * @return 是否成功
     */
    boolean registerWorkflow(N8nWorkflowDTO dto);
    
    /**
     * 发送webhook通知
     * @param url webhook地址
     * @param data 数据
     * @return 响应
     */
    String sendWebhook(String url, Object data);
}