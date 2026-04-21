package com.aioa.workflow.controller;

import com.aioa.workflow.dto.N8nWorkflowDTO;
import com.aioa.workflow.service.N8nWorkflowService;
import com.aioa.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * n8n工作流控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/workflow/n8n")
public class N8nWorkflowController {
    
    @Autowired
    private N8nWorkflowService n8nWorkflowService;
    
    /**
     * 注册工作流
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody N8nWorkflowDTO dto) {
        boolean success = n8nWorkflowService.registerWorkflow(dto);
        return success ? Result.success("注册成功") : Result.fail("注册失败");
    }
    
    /**
     * 触发工作流
     */
    @PostMapping("/trigger/{workflowId}")
    public Result<?> trigger(
            @PathVariable String workflowId,
            @RequestBody(required = false) Object data) {
        String result = n8nWorkflowService.triggerWorkflow(workflowId, data);
        return Result.success(result);
    }
    
    /**
     * 获取工作流状态
     */
    @GetMapping("/status/{workflowId}")
    public Result<?> getStatus(@PathVariable String workflowId) {
        String status = n8nWorkflowService.getWorkflowStatus(workflowId);
        return Result.success(status);
    }
    
    /**
     * 测试Webhook
     */
    @PostMapping("/webhook/test")
    public Result<?> testWebhook(@RequestBody Object data) {
        String result = n8nWorkflowService.sendWebhook(
            "https://your-n8n-instance.com/webhook/test", 
            data
        );
        return Result.success(result);
    }
}