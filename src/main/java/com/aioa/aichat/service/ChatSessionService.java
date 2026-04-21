package com.aioa.aichat.service;

import com.aioa.aichat.dto.ChatSessionDTO;
import com.aioa.aichat.dto.ChatSessionResponseDTO;
import com.aioa.aichat.entity.ChatSession;
import com.aioa.aichat.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI会话Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;

    /**
     * 创建会话
     */
    public ChatSessionResponseDTO createSession(ChatSessionDTO dto) {
        ChatSession session = ChatSession.builder()
                .sessionId(UUID.randomUUID().toString().replace("-", ""))
                .userId(dto.getUserId())
                .title(dto.getTitle() != null ? dto.getTitle() : "新会话")
                .type(dto.getType() != null ? dto.getType() : "general")
                .contextData(dto.getContextData())
                .status(1)
                .lastActiveTime(LocalDateTime.now())
                .build();

        ChatSession saved = sessionRepository.save(session);
        return convertToResponseDTO(saved);
    }

    /**
     * 获取会话
     */
    @Transactional(readOnly = true)
    public ChatSessionResponseDTO getSessionById(Long id) {
        ChatSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
        return convertToResponseDTO(session);
    }

    /**
     * 获取会话（通过sessionId）
     */
    @Transactional(readOnly = true)
    public ChatSessionResponseDTO getSessionBySessionId(String sessionId) {
        ChatSession session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
        return convertToResponseDTO(session);
    }

    /**
     * 获取用户所有会话
     */
    @Transactional(readOnly = true)
    public List<ChatSessionResponseDTO> getUserSessions(Long userId) {
        return sessionRepository.findByUserIdOrderByLastActiveTimeDesc(userId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新会话活跃时间
     */
    public void updateLastActiveTime(Long id) {
        ChatSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
        session.setLastActiveTime(LocalDateTime.now());
        sessionRepository.save(session);
    }

    /**
     * 关闭会话
     */
    public void closeSession(Long id) {
        ChatSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在"));
        session.setStatus(0);
        sessionRepository.save(session);
    }

    /**
     * 删除会话
     */
    public void deleteSession(Long id) {
        if (!sessionRepository.existsById(id)) {
            throw new IllegalArgumentException("会话不存在");
        }
        sessionRepository.deleteById(id);
    }

    private ChatSessionResponseDTO convertToResponseDTO(ChatSession session) {
        ChatSessionResponseDTO dto = new ChatSessionResponseDTO();
        dto.setId(session.getId());
        dto.setSessionId(session.getSessionId());
        dto.setUserId(session.getUserId());
        dto.setTitle(session.getTitle());
        dto.setType(session.getType());
        dto.setContextData(session.getContextData());
        dto.setStatus(session.getStatus());
        dto.setCreateTime(session.getCreateTime() != null ? session.getCreateTime().toString() : null);
        dto.setUpdateTime(session.getUpdateTime() != null ? session.getUpdateTime().toString() : null);
        dto.setLastActiveTime(session.getLastActiveTime() != null ? session.getLastActiveTime().toString() : null);
        return dto;
    }
}
