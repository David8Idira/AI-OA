package com.aioa.aichat.service;

import com.aioa.aichat.dto.ChatMessageDTO;
import com.aioa.aichat.dto.ChatMessageResponseDTO;
import com.aioa.aichat.entity.ChatMessage;
import com.aioa.aichat.entity.ChatSession;
import com.aioa.aichat.repository.ChatMessageRepository;
import com.aioa.aichat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI聊天消息Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;

    /**
     * 发送消息（模拟AI回复）
     */
    public ChatMessageResponseDTO sendMessage(ChatMessageDTO dto) {
        // 查找或创建会话
        ChatSession session;
        if (dto.getSessionId() != null && !dto.getSessionId().isEmpty()) {
            session = sessionRepository.findBySessionId(dto.getSessionId())
                    .orElseGet(() -> createSession(dto));
        } else {
            session = createSession(dto);
        }

        // 保存用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .sessionId(session.getId())
                .role("user")
                .content(dto.getContent())
                .build();
        messageRepository.save(userMessage);

        // 更新会话活跃时间
        session.setLastActiveTime(LocalDateTime.now());
        sessionRepository.save(session);

        // 模拟AI回复（实际项目中应调用AI服务）
        String aiResponse = generateAIResponse(dto.getContent(), session.getType());

        // 保存AI回复
        ChatMessage aiMessage = ChatMessage.builder()
                .sessionId(session.getId())
                .role("assistant")
                .content(aiResponse)
                .modelName("AI-OA-GPT")
                .tokenCount(aiResponse.length() / 4)
                .build();
        ChatMessage saved = messageRepository.save(aiMessage);

        return convertToResponseDTO(saved, session);
    }

    /**
     * 获取会话历史消息
     */
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDTO> getSessionMessages(Long sessionEntityId) {
        return messageRepository.findBySessionIdOrderByCreateTimeAsc(sessionEntityId).stream()
                .map(m -> {
                    ChatSession session = sessionRepository.findById(m.getSessionId()).orElse(null);
                    return convertToResponseDTO(m, session);
                })
                .collect(Collectors.toList());
    }

    /**
     * 模拟AI回复（实际项目中应调用外部AI服务）
     */
    private String generateAIResponse(String content, String type) {
        // 这里是模拟回复，实际项目中应调用AI服务（如OpenAI、Kimi等）
        if (content.contains("审批")) {
            return "您好！我是AI助手。关于审批的问题，我可以帮您：\n1. 查询审批进度\n2. 创建新的审批单\n3. 催办审批流程\n\n请问有什么可以帮助您？";
        } else if (content.contains("请假")) {
            return "请假审批流程如下：\n1. 填写请假申请单\n2. 选择请假类型和时长\n3. 提交给直接主管审批\n4. 主管审批通过后生效\n\n需要我帮您创建请假单吗？";
        } else {
            return "您好！我是AI-OA智能助手。请问有什么可以帮助您？我可以帮您处理：\n- 审批相关问题\n- 部门人员查询\n- 文档处理\n- 其他办公问题";
        }
    }

    private ChatSession createSession(ChatMessageDTO dto) {
        ChatSession session = ChatSession.builder()
                .sessionId(java.util.UUID.randomUUID().toString().replace("-", ""))
                .userId(dto.getUserId())
                .title("新会话")
                .type(dto.getType() != null ? dto.getType() : "general")
                .contextData(dto.getContextData())
                .status(1)
                .lastActiveTime(LocalDateTime.now())
                .build();
        return sessionRepository.save(session);
    }

    private ChatMessageResponseDTO convertToResponseDTO(ChatMessage message, ChatSession session) {
        ChatMessageResponseDTO dto = new ChatMessageResponseDTO();
        dto.setId(message.getId());
        dto.setSessionId(session != null ? session.getSessionId() : null);
        dto.setSessionEntityId(message.getSessionId());
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setModelName(message.getModelName());
        dto.setTokenCount(message.getTokenCount());
        dto.setCreateTime(message.getCreateTime() != null ? message.getCreateTime().toString() : null);
        return dto;
    }
}
