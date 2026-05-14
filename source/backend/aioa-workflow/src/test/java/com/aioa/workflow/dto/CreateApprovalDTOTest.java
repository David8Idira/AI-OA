package com.aioa.workflow.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CreateApprovalDTO测试
 */
@DisplayName("CreateApprovalDTOTest 创建审批DTO测试")
class CreateApprovalDTOTest {

    @Test
    @DisplayName("默认构造")
    void defaultConstructor() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        assertNotNull(dto);
    }

    @Test
    @DisplayName("设置基础字段")
    void basicFields() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setTitle("请假申请");
        dto.setType("LEAVE");
        dto.setContent("因病需要请假2天");
        dto.setPriority(1);
        dto.setApproverId("approver-001");

        assertEquals("请假申请", dto.getTitle());
        assertEquals("LEAVE", dto.getType());
        assertEquals("因病需要请假2天", dto.getContent());
        assertEquals(1, dto.getPriority());
        assertEquals("approver-001", dto.getApproverId());
    }

    @Test
    @DisplayName("设置可选字段")
    void optionalFields() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setCcUsers("user-001,user-002");
        dto.setExpectFinishTime(LocalDateTime.of(2024, 6, 15, 18, 0));
        dto.setRemark("请尽快审批");
        dto.setAttachments("https://example.com/file.pdf");

        assertEquals("user-001,user-002", dto.getCcUsers());
        assertEquals(LocalDateTime.of(2024, 6, 15, 18, 0), dto.getExpectFinishTime());
        assertEquals("请尽快审批", dto.getRemark());
        assertEquals("https://example.com/file.pdf", dto.getAttachments());
    }

    @Test
    @DisplayName("表单数据")
    void formData() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        Map<String, Object> formData = new HashMap<>();
        formData.put("days", 2);
        formData.put("reason", "身体不适");
        dto.setFormData(formData);

        assertEquals(2, dto.getFormData().get("days"));
        assertEquals("身体不适", dto.getFormData().get("reason"));
    }

    @Test
    @DisplayName("优先级枚举")
    void priorityLevels() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        // 0=低优先级
        dto.setPriority(0);
        assertEquals(0, dto.getPriority());

        // 1=普通优先级
        dto.setPriority(1);
        assertEquals(1, dto.getPriority());

        // 2=高优先级
        dto.setPriority(2);
        assertEquals(2, dto.getPriority());

        // 3=紧急
        dto.setPriority(3);
        assertEquals(3, dto.getPriority());
    }

    @Test
    @DisplayName("审批类型")
    void approvalTypes() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        
        dto.setType("LEAVE");
        assertEquals("LEAVE", dto.getType());
        
        dto.setType("EXPENSE");
        assertEquals("EXPENSE", dto.getType());
        
        dto.setType("TRAVEL");
        assertEquals("TRAVEL", dto.getType());
    }

    @Test
    @DisplayName("完整审批请求")
    void fullApprovalRequest() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setTitle("出差申请");
        dto.setType("TRAVEL");
        dto.setContent("需要前往北京出差一周");
        dto.setPriority(2);
        dto.setApproverId("manager-001");
        dto.setCcUsers("hr-001,finance-001");
        dto.setExpectFinishTime(LocalDateTime.now().plusDays(3));
        dto.setRemark("紧急项目需要");
        dto.setAttachments("https://example.com/approval.pdf");
        
        Map<String, Object> formData = new HashMap<>();
        formData.put("destination", "北京");
        formData.put("duration", 7);
        formData.put("budget", 5000);
        dto.setFormData(formData);

        assertEquals("出差申请", dto.getTitle());
        assertEquals("TRAVEL", dto.getType());
        assertEquals("需要前往北京出差一周", dto.getContent());
        assertEquals(2, dto.getPriority());
        assertEquals("manager-001", dto.getApproverId());
        assertEquals("hr-001,finance-001", dto.getCcUsers());
        assertEquals("紧急项目需要", dto.getRemark());
        assertEquals(7, dto.getFormData().get("duration"));
    }

    @Test
    @DisplayName("空表单数据")
    void emptyFormData() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setFormData(new HashMap<>());
        assertNotNull(dto.getFormData());
        assertTrue(dto.getFormData().isEmpty());
    }

    @Test
    @DisplayName("多个抄送人")
    void multipleCcUsers() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setCcUsers("user1,user2,user3");
        assertEquals("user1,user2,user3", dto.getCcUsers());
    }
}