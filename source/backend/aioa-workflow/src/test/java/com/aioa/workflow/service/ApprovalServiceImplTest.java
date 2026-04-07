package com.aioa.workflow.service;

import com.aioa.common.exception.BusinessException;
import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.entity.Approval;
import com.aioa.system.entity.SysUser;
import com.aioa.workflow.entity.ApprovalRecord;
import com.aioa.workflow.enums.ApprovalActionEnum;
import com.aioa.workflow.enums.ApprovalPriorityEnum;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.service.impl.ApprovalServiceImpl;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.workflow.vo.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApprovalServiceImpl
 */
@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private ApprovalRecordService approvalRecordService;

    @Mock
    private com.aioa.system.mapper.SysUserMapper sysUserMapper;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    private SysUser mockApplicant;
    private SysUser mockApprover;
    private CreateApprovalDTO mockCreateDTO;
    private Approval mockApproval;

    @BeforeEach
    void setUp() {
        // Setup mock applicant
        mockApplicant = new SysUser();
        mockApplicant.setId("user-001");
        mockApplicant.setUsername("zhangsan");
        mockApplicant.setNickname("张三");
        mockApplicant.setDeptId("dept-001");

        // Setup mock approver
        mockApprover = new SysUser();
        mockApprover.setId("user-002");
        mockApprover.setUsername("lisi");
        mockApprover.setNickname("李四");

        // Setup mock create DTO
        mockCreateDTO = new CreateApprovalDTO();
        mockCreateDTO.setTitle("请假申请");
        mockCreateDTO.setType("LEAVE");
        mockCreateDTO.setContent("因私请假3天");
        mockCreateDTO.setPriority(ApprovalPriorityEnum.NORMAL.getCode());
        mockCreateDTO.setApproverId("user-002");
        mockCreateDTO.setExpectFinishTime(LocalDateTime.now().plusDays(3));
        mockCreateDTO.setRemark("有急事");

        // Setup mock approval entity
        mockApproval = new Approval();
        mockApproval.setId("approval-001");
        mockApproval.setTitle("请假申请");
        mockApproval.setType("LEAVE");
        mockApproval.setContent("因私请假3天");
        mockApproval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        mockApproval.setPriority(ApprovalPriorityEnum.NORMAL.getCode());
        mockApproval.setApplicantId("user-001");
        mockApproval.setApplicantName("张三");
        mockApproval.setApproverId("user-002");
        mockApproval.setApproverName("李四");
        mockApproval.setCurrentStep(1);
        mockApproval.setTotalSteps(1);
        mockApproval.setCreateTime(LocalDateTime.now());
        mockApproval.setUpdateTime(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Create Approval Tests")
    class CreateApprovalTests {

        @Test
        @DisplayName("Should create approval successfully")
        void shouldCreateApprovalSuccessfully() {
            // Given
            when(sysUserMapper.selectById("user-001")).thenReturn(mockApplicant);
            when(sysUserMapper.selectById("user-002")).thenReturn(mockApprover);
            when(approvalService.save(any(Approval.class))).thenAnswer(inv -> {
                Approval a = inv.getArgument(0);
                a.setId("approval-001");
                return true;
            });
            when(approvalRecordService.createRecord(anyString(), anyString(), anyString(),
                    anyInt(), anyString(), anyInt(), anyInt()))
                    .thenReturn(new ApprovalRecord());

            // When
            ApprovalVO result = approvalService.createApproval("user-001", mockCreateDTO);

            // Then
            assertNotNull(result);
            assertEquals("请假申请", result.getTitle());
            assertEquals("LEAVE", result.getType());
            assertEquals("user-001", result.getApplicantId());
            assertEquals("user-002", result.getApproverId());
            assertEquals(ApprovalStatusEnum.PENDING.getCode(), result.getStatus());
            assertEquals(ApprovalStatusEnum.PENDING.getDescription(), result.getStatusDesc());

            // Verify approval was saved
            ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
            verify(approvalService).save(captor.capture());
            Approval saved = captor.getValue();
            assertEquals("请假申请", saved.getTitle());
            assertEquals("LEAVE", saved.getType());
            assertEquals("user-001", saved.getApplicantId());
            assertEquals("user-002", saved.getApproverId());
        }

        @Test
        @DisplayName("Should throw exception when applicant not found")
        void shouldThrowExceptionWhenApplicantNotFound() {
            // Given
            when(sysUserMapper.selectById("user-001")).thenReturn(null);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.createApproval("user-001", mockCreateDTO));
            assertEquals(20001, ex.getResultCode().getCode()); // USER_NOT_FOUND
        }

        @Test
        @DisplayName("Should throw exception when approver not found")
        void shouldThrowExceptionWhenApproverNotFound() {
            // Given
            when(sysUserMapper.selectById("user-001")).thenReturn(mockApplicant);
            when(sysUserMapper.selectById("user-002")).thenReturn(null);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.createApproval("user-001", mockCreateDTO));
            assertNotNull(ex.getResultCode().getCode());
            assertTrue(ex.getResultCode().getMessage().contains("审批人"));
        }
    }

    @Nested
    @DisplayName("Get Approval Detail Tests")
    class GetApprovalDetailTests {

        @Test
        @DisplayName("Should get approval detail successfully for applicant")
        void shouldGetApprovalDetailForApplicant() {
            // Given
            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(approvalRecordService.getRecordsByApprovalId("approval-001"))
                    .thenReturn(List.of());

            // When
            ApprovalVO result = approvalService.getApprovalDetail("approval-001", "user-001");

            // Then
            assertNotNull(result);
            assertEquals("approval-001", result.getId());
            assertEquals("请假申请", result.getTitle());
        }

        @Test
        @DisplayName("Should get approval detail for approver")
        void shouldGetApprovalDetailForApprover() {
            // Given
            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(approvalRecordService.getRecordsByApprovalId("approval-001"))
                    .thenReturn(List.of());

            // When
            ApprovalVO result = approvalService.getApprovalDetail("approval-001", "user-002");

            // Then
            assertNotNull(result);
            assertEquals("approval-001", result.getId());
        }

        @Test
        @DisplayName("Should throw exception when approval not found")
        void shouldThrowExceptionWhenApprovalNotFound() {
            // Given
            when(approvalService.getById("non-existent")).thenReturn(null);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.getApprovalDetail("non-existent", "user-001"));
            assertEquals(30001, ex.getResultCode().getCode()); // APPROVAL_NOT_FOUND
        }

        @Test
        @DisplayName("Should throw exception when user has no permission")
        void shouldThrowExceptionWhenNoPermission() {
            // Given
            mockApproval.setApplicantId("user-other");
            mockApproval.setApproverId("user-other");
            mockApproval.setCcUsers(null);
            when(approvalService.getById("approval-001")).thenReturn(mockApproval);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.getApprovalDetail("approval-001", "user-001"));
            assertEquals(30002, ex.getResultCode().getCode()); // APPROVAL_PERMISSION_DENIED
        }
    }

    @Nested
    @DisplayName("Approval Action Tests")
    class ApprovalActionTests {

        @Test
        @DisplayName("Should approve successfully by approver")
        void shouldApproveSuccessfully() {
            // Given
            ApprovalActionDTO actionDTO = new ApprovalActionDTO();
            actionDTO.setActionType(ApprovalActionEnum.APPROVE.getCode());
            actionDTO.setComment("同意");

            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(sysUserMapper.selectById("user-002")).thenReturn(mockApprover);
            when(approvalService.updateById(any(Approval.class))).thenReturn(true);
            when(approvalRecordService.createRecord(anyString(), anyString(), anyString(),
                    anyInt(), anyString(), anyInt(), anyInt()))
                    .thenReturn(new ApprovalRecord());
            when(approvalRecordService.getRecordsByApprovalId("approval-001"))
                    .thenReturn(List.of());

            // When
            ApprovalVO result = approvalService.doAction("approval-001", "user-002", actionDTO);

            // Then
            assertNotNull(result);
            ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
            verify(approvalService).updateById(captor.capture());
            assertEquals(ApprovalStatusEnum.APPROVED.getCode(), captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Should reject successfully by approver")
        void shouldRejectSuccessfully() {
            // Given
            ApprovalActionDTO actionDTO = new ApprovalActionDTO();
            actionDTO.setActionType(ApprovalActionEnum.REJECT.getCode());
            actionDTO.setComment("材料不全，驳回");

            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(sysUserMapper.selectById("user-002")).thenReturn(mockApprover);
            when(approvalService.updateById(any(Approval.class))).thenReturn(true);
            when(approvalRecordService.createRecord(anyString(), anyString(), anyString(),
                    anyInt(), anyString(), anyInt(), anyInt()))
                    .thenReturn(new ApprovalRecord());
            when(approvalRecordService.getRecordsByApprovalId("approval-001"))
                    .thenReturn(List.of());

            // When
            ApprovalVO result = approvalService.doAction("approval-001", "user-002", actionDTO);

            // Then
            assertNotNull(result);
            ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
            verify(approvalService).updateById(captor.capture());
            assertEquals(ApprovalStatusEnum.REJECTED.getCode(), captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Should transfer successfully by approver")
        void shouldTransferSuccessfully() {
            // Given
            SysUser newApprover = new SysUser();
            newApprover.setId("user-003");
            newApprover.setUsername("wangwu");
            newApprover.setNickname("王五");

            ApprovalActionDTO actionDTO = new ApprovalActionDTO();
            actionDTO.setActionType(ApprovalActionEnum.TRANSFER.getCode());
            actionDTO.setTransferToId("user-003");
            actionDTO.setComment("转交王五处理");

            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(sysUserMapper.selectById("user-002")).thenReturn(mockApprover);
            when(sysUserMapper.selectById("user-003")).thenReturn(newApprover);
            when(approvalService.updateById(any(Approval.class))).thenReturn(true);
            when(approvalRecordService.getRecordsByApprovalId("approval-001"))
                    .thenReturn(List.of());

            // When
            ApprovalVO result = approvalService.doAction("approval-001", "user-002", actionDTO);

            // Then
            assertNotNull(result);
            ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
            verify(approvalService).updateById(captor.capture());
            assertEquals("user-003", captor.getValue().getApproverId());
            assertEquals(ApprovalStatusEnum.TRANSFERRED.getCode(), captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Should throw exception when non-approver tries to approve")
        void shouldThrowExceptionWhenNonApproverApproves() {
            // Given
            ApprovalActionDTO actionDTO = new ApprovalActionDTO();
            actionDTO.setActionType(ApprovalActionEnum.APPROVE.getCode());

            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(sysUserMapper.selectById("user-other")).thenReturn(null);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.doAction("approval-001", "user-other", actionDTO));
            assertEquals(30002, ex.getResultCode().getCode()); // APPROVAL_PERMISSION_DENIED
        }

        @Test
        @DisplayName("Should throw exception when approving non-pending approval")
        void shouldThrowExceptionWhenApprovingNonPendingApproval() {
            // Given
            mockApproval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
            ApprovalActionDTO actionDTO = new ApprovalActionDTO();
            actionDTO.setActionType(ApprovalActionEnum.APPROVE.getCode());

            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(sysUserMapper.selectById("user-002")).thenReturn(mockApprover);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.doAction("approval-001", "user-002", actionDTO));
            assertEquals(30003, ex.getResultCode().getCode()); // APPROVAL_STATUS_ERROR
        }

        @Test
        @DisplayName("Should throw exception for invalid action type")
        void shouldThrowExceptionForInvalidActionType() {
            // Given
            ApprovalActionDTO actionDTO = new ApprovalActionDTO();
            actionDTO.setActionType(999);

            when(approvalService.getById("approval-001")).thenReturn(mockApproval);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.doAction("approval-001", "user-002", actionDTO));
            assertTrue(ex.getResultCode().getMessage().contains("无效"));
        }
    }

    @Nested
    @DisplayName("Cancel Approval Tests")
    class CancelApprovalTests {

        @Test
        @DisplayName("Should cancel approval by applicant successfully")
        void shouldCancelApprovalByApplicant() {
            // Given
            when(approvalService.getById("approval-001")).thenReturn(mockApproval);
            when(sysUserMapper.selectById("user-001")).thenReturn(mockApplicant);
            when(approvalService.updateById(any(Approval.class))).thenReturn(true);
            when(approvalRecordService.createRecord(anyString(), anyString(), anyString(),
                    anyInt(), anyString(), anyInt(), anyInt()))
                    .thenReturn(new ApprovalRecord());

            // When
            boolean result = approvalService.cancelApproval("approval-001", "user-001", "因时间冲突");

            // Then
            assertTrue(result);
            ArgumentCaptor<Approval> captor = ArgumentCaptor.forClass(Approval.class);
            verify(approvalService).updateById(captor.capture());
            assertEquals(ApprovalStatusEnum.CANCELLED.getCode(), captor.getValue().getStatus());
        }

        @Test
        @DisplayName("Should throw exception when non-applicant tries to cancel")
        void shouldThrowExceptionWhenNonApplicantCancels() {
            // Given
            when(approvalService.getById("approval-001")).thenReturn(mockApproval);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.cancelApproval("approval-001", "user-002", null));
            assertEquals(30002, ex.getResultCode().getCode());
        }

        @Test
        @DisplayName("Should throw exception when cancelling non-pending approval")
        void shouldThrowExceptionWhenCancellingNonPendingApproval() {
            // Given
            mockApproval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
            when(approvalService.getById("approval-001")).thenReturn(mockApproval);

            // When / Then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> approvalService.cancelApproval("approval-001", "user-001", null));
            assertEquals(30003, ex.getResultCode().getCode());
        }
    }

    @Nested
    @DisplayName("Query Approvals Tests")
    class QueryApprovalsTests {

        @Test
        @DisplayName("Should query approvals with default mode")
        void shouldQueryApprovalsWithDefaultMode() {
            // Given
            ApprovalQueryDTO query = new ApprovalQueryDTO();
            query.setPageNum(1);
            query.setPageSize(10);

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Approval> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(List.of(mockApproval));
            mockPage.setTotal(1);

            doReturn(mockPage).when(approvalService).page(
                    any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                    any());

            // When
            PageResult<ApprovalVO> result = approvalService.queryApprovals("user-001", query);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("Should query approvals with MY_APPLY mode")
        void shouldQueryApprovalsWithMyApplyMode() {
            // Given
            ApprovalQueryDTO query = new ApprovalQueryDTO();
            query.setPageNum(1);
            query.setPageSize(10);
            query.setMode("MY_APPLY");

            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Approval> mockPage =
                    new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10);
            mockPage.setRecords(List.of(mockApproval));
            mockPage.setTotal(1);

            doReturn(mockPage).when(approvalService).page(
                    any(com.baomidou.mybatisplus.extension.plugins.pagination.Page.class),
                    any());

            // When
            PageResult<ApprovalVO> result = approvalService.queryApprovals("user-001", query);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getRecords().size());
        }
    }

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Should get statistics correctly")
        void shouldGetStatisticsCorrectly() {
            // Given
            when(approvalService.count(any())).thenReturn(5L);

            // When
            java.util.Map<String, Object> stats = approvalService.getStatistics("user-001");

            // Then
            assertNotNull(stats);
            assertNotNull(stats.get("totalMyApply"));
            assertNotNull(stats.get("totalPendingApprove"));
        }
    }

    @Nested
    @DisplayName("Enums Tests")
    class EnumsTests {

        @Test
        @DisplayName("ApprovalStatusEnum should return correct values")
        void approvalStatusEnumShouldReturnCorrectValues() {
            assertEquals("待审批", ApprovalStatusEnum.PENDING.getDescription());
            assertEquals("已同意", ApprovalStatusEnum.APPROVED.getDescription());
            assertEquals("已驳回", ApprovalStatusEnum.REJECTED.getDescription());
            assertEquals("已撤回", ApprovalStatusEnum.CANCELLED.getDescription());
            assertEquals("已转交", ApprovalStatusEnum.TRANSFERRED.getDescription());
        }

        @Test
        @DisplayName("ApprovalActionEnum should return correct values")
        void approvalActionEnumShouldReturnCorrectValues() {
            assertEquals("同意", ApprovalActionEnum.APPROVE.getDescription());
            assertEquals("驳回", ApprovalActionEnum.REJECT.getDescription());
            assertEquals("转交", ApprovalActionEnum.TRANSFER.getDescription());
            assertEquals("撤回", ApprovalActionEnum.CANCEL.getDescription());
        }

        @Test
        @DisplayName("ApprovalPriorityEnum should return correct values")
        void approvalPriorityEnumShouldReturnCorrectValues() {
            assertEquals("低", ApprovalPriorityEnum.LOW.getDescription());
            assertEquals("普通", ApprovalPriorityEnum.NORMAL.getDescription());
            assertEquals("高", ApprovalPriorityEnum.HIGH.getDescription());
            assertEquals("紧急", ApprovalPriorityEnum.URGENT.getDescription());
        }

        @Test
        @DisplayName("Enums getByCode should return correct enum")
        void enumsGetByCodeShouldReturnCorrectEnum() {
            assertEquals(ApprovalStatusEnum.PENDING, ApprovalStatusEnum.getByCode(0));
            assertEquals(ApprovalStatusEnum.APPROVED, ApprovalStatusEnum.getByCode(1));
            assertEquals(ApprovalStatusEnum.REJECTED, ApprovalStatusEnum.getByCode(2));
            assertEquals(ApprovalActionEnum.APPROVE, ApprovalActionEnum.getByCode(1));
            assertEquals(ApprovalActionEnum.REJECT, ApprovalActionEnum.getByCode(2));
            assertEquals(ApprovalPriorityEnum.HIGH, ApprovalPriorityEnum.getByCode(2));
            assertNull(ApprovalStatusEnum.getByCode(null));
            assertNull(ApprovalStatusEnum.getByCode(99));
        }
    }
}
