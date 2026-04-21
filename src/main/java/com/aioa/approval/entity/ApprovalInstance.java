package com.aioa.approval.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 审批实例实体类（具体某一次申请）
 */
@Entity
@Table(name = "approval_instance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的流程ID
     */
    @Column(name = "process_id", nullable = false)
    private Long processId;

    /**
     * 申请人ID
     */
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    /**
     * 申请标题
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 申请表单数据（JSON）
     */
    @Column(columnDefinition = "TEXT")
    private String formData;

    /**
     * 当前审批节点
     */
    @Column(name = "current_node")
    private Integer currentNode;

    /**
     * 总审批节点数
     */
    @Column(name = "total_nodes")
    private Integer totalNodes;

    /**
     * 状态：0-草稿，1-审批中，2-已通过，3-已拒绝，4-已撤回
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
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 审批完成时间
     */
    private LocalDateTime finishTime;
}
