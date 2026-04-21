package com.aioa.aichat.repository;

import com.aioa.aichat.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI会话Repository
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findBySessionId(String sessionId);

    List<ChatSession> findByUserId(Long userId);

    List<ChatSession> findByUserIdAndStatus(Long userId, Integer status);

    List<ChatSession> findByUserIdOrderByLastActiveTimeDesc(Long userId);
}
