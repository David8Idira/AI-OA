package com.aioa.aichat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI聊天消息实体类
 */
@Entity
@Table(name = "ai_chat_message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的会话ID
     */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    /**
     * 消息角色：user-用户，assistant-助手，system-系统
     */
    @Column(nullable = false, length = 20)
    private String role;

    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 模型名称
     */
    @Column(length = 100)
    private String modelName;

    /**
     * 令牌数量
     */
    private Integer tokenCount;

    /**
     * 扩展数据（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}
