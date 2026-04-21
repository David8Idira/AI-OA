package com.aioa.approval.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 审批任务实体类（审批节点记录）
 */
@Entity
@Table(name = "approval_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的审批实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    /**
     * 审批人ID
     */
    @Column(name = "approver_id", nullable = false)
    private Long approverId;

    /**
     * 节点序号
     */
    @Column(name = "node_order", nullable = false)
    private Integer nodeOrder;

    /**
     * 审批意见
     */
    @Column(length = 500)
    private String comment;

    /**
     * 审批结果：1-通过，2-拒绝，3-转交
     */
    private Integer result;

    /**
     * 状态：0-待审批，1-已审批
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
     * 审批时间
     */
    private LocalDateTime approveTime;
}
