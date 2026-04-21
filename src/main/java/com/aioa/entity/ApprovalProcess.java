package com.aioa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 审批流程实体
 */
@Entity
@Table(name = "t_approval_process")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalProcess {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;
    
    @Column(name = "process_key", nullable = false, unique = true, length = 100)
    private String processKey;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "form_schema", columnDefinition = "TEXT")
    private String formSchema;
    
    @Column(name = "flow_config", columnDefinition = "TEXT")
    private String flowConfig;
    
    @Column
    private Integer version;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) version = 1;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}