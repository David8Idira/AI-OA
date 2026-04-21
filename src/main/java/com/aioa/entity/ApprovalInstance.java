package com.aioa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审批实例实体
 */
@Entity
@Table(name = "t_approval_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "process_id", nullable = false)
    private Long processId;
    
    @Column(name = "process_key", length = 100)
    private String processKey;
    
    @Column(name = "instance_no", unique = true, length = 50)
    private String instanceNo;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Column(name = "applicant", length = 100)
    private String applicant;
    
    @Column(name = "applicant_id")
    private Long applicantId;
    
    @Column(name = "current_node_id")
    private Long currentNodeId;
    
    @Column(name = "current_node_name", length = 200)
    private String currentNodeName;
    
    @Column(name = "form_data", columnDefinition = "TEXT")
    private String formData;
    
    @Column(name = "history_data", columnDefinition = "TEXT")
    private String historyData;
    
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration")
    private Long duration;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        startTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}