package com.aioa.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审批任务实体
 */
@Entity
@Table(name = "t_approval_task")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "process_id", nullable = false)
    private Long processId;
    
    @Column(name = "instance_id")
    private Long instanceId;
    
    @Column(name = "task_name", nullable = false, length = 200)
    private String taskName;
    
    @Column(name = "task_key", length = 100)
    private String taskKey;
    
    @Column(name = "assignee", length = 100)
    private String assignee;
    
    @Column(name = "candidate_users", length = 500)
    private String candidateUsers;
    
    @Column(name = "candidate_groups", length = 500)
    private String candidateGroups;
    
    @Column
    private Integer priority;
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;
    
    @Column(name = "complete_date")
    private LocalDateTime completeDate;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "task_vars", columnDefinition = "TEXT")
    private String taskVars;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}