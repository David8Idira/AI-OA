package com.aioa.aichat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI会话实体类
 */
@Entity
@Table(name = "ai_chat_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话唯一标识
     */
    @Column(unique = true, nullable = false, length = 64)
    private String sessionId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 会话标题
     */
    @Column(length = 200)
    private String title;

    /**
     * 会话类型：general-通用，approval-审批，document-文档等
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 上下文数据（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String contextData;

    /**
     * 状态：0-关闭，1-活跃
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 最后活跃时间
     */
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;
}
