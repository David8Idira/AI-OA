package com.aioa.ai.controller;

import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.service.AiChatService;
import com.aioa.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * AI聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {
    
    @Autowired
    private AiChatService aiChatService;
    
    /**
     * AI对话
     */
    @PostMapping("/chat")
    public Result<ChatResponseDTO> chat(@RequestBody ChatRequestDTO request) {
        log.info("收到AI聊天请求: {}", request.getMessage());
        ChatResponseDTO response = aiChatService.chat(request);
        return Result.success(response);
    }
    
    /**
     * 获取模型列表
     */
    @GetMapping("/models")
    public Result<?> getModels() {
        return Result.success(aiChatService.getAvailableModels());
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<?> health() {
        return Result.success("AI服务正常");
    }
}