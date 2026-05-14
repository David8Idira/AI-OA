package com.aioa.workflow.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApprovalQueryDTO测试
 */
@DisplayName("ApprovalQueryDTOTest 审批查询DTO测试")
class ApprovalQueryDTOTest {

    @Test
    @DisplayName("默认构造")
    void defaultConstructor() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        assertEquals(1, dto.getPageNum());
        assertEquals(10, dto.getPageSize());
    }

    @Test
    @DisplayName("设置查询参数")
    void setters() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setPageNum(2);
        dto.setPageSize(20);
        dto.setType("LEAVE");
        dto.setStatus(0);
        dto.setPriority(1);
        dto.setApplicantId("user-001");
        dto.setApproverId("approver-001");
        dto.setKeyword("请假");
        dto.setStartDate("2024-01-01");
        dto.setEndDate("2024-12-31");
        dto.setMode("MY_APPLY");

        assertEquals(2, dto.getPageNum());
        assertEquals(20, dto.getPageSize());
        assertEquals("LEAVE", dto.getType());
        assertEquals(0, dto.getStatus());
        assertEquals(1, dto.getPriority());
        assertEquals("user-001", dto.getApplicantId());
        assertEquals("approver-001", dto.getApproverId());
        assertEquals("请假", dto.getKeyword());
        assertEquals("2024-01-01", dto.getStartDate());
        assertEquals("2024-12-31", dto.getEndDate());
        assertEquals("MY_APPLY", dto.getMode());
    }

    @Test
    @DisplayName("模式常量-我的申请")
    void modeMyApply() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setMode("MY_APPLY");
        assertEquals("MY_APPLY", dto.getMode());
    }

    @Test
    @DisplayName("模式常量-我的审批")
    void modeMyApprove() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setMode("MY_APPROVE");
        assertEquals("MY_APPROVE", dto.getMode());
    }

    @Test
    @DisplayName("状态筛选")
    void statusFilter() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        // 0=待审批, 1=审批中, 2=已完成, 3=已取消
        dto.setStatus(0);
        assertEquals(0, dto.getStatus());
        
        dto.setStatus(1);
        assertEquals(1, dto.getStatus());
    }

    @Test
    @DisplayName("优先级筛选")
    void priorityFilter() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        // 0=低, 1=普通, 2=高, 3=紧急
        dto.setPriority(3);
        assertEquals(3, dto.getPriority());
    }

    @Test
    @DisplayName("分页参数验证")
    void paginationValidation() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        assertEquals(1, dto.getPageNum());
        assertEquals(10, dto.getPageSize());
    }

    @Test
    @DisplayName("日期范围查询")
    void dateRange() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setStartDate("2024-06-01");
        dto.setEndDate("2024-06-30");
        assertEquals("2024-06-01", dto.getStartDate());
        assertEquals("2024-06-30", dto.getEndDate());
    }

    @Test
    @DisplayName("申请人筛选")
    void applicantFilter() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setApplicantId("user-123");
        assertEquals("user-123", dto.getApplicantId());
    }

    @Test
    @DisplayName("审批人筛选")
    void approverFilter() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setApproverId("approver-456");
        assertEquals("approver-456", dto.getApproverId());
    }

    @Test
    @DisplayName("关键词搜索")
    void keywordSearch() {
        ApprovalQueryDTO dto = new ApprovalQueryDTO();
        dto.setKeyword("出差报销");
        assertEquals("出差报销", dto.getKeyword());
    }
}