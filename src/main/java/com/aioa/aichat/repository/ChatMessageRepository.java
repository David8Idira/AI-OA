package com.aioa.aichat.repository;

import com.aioa.aichat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI聊天消息Repository
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findBySessionIdOrderByCreateTimeAsc(Long sessionId);

    List<ChatMessage> findBySessionId(Long sessionId);
}
