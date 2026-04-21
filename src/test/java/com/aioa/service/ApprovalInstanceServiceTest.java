package com.aioa.service;

import com.aioa.entity.ApprovalInstance;
import com.aioa.entity.ApprovalProcess;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.ApprovalInstanceMapper;
import com.aioa.mapper.ApprovalProcessMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ApprovalInstanceService单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalInstanceService测试")
class ApprovalInstanceServiceTest {

    @Mock
    private ApprovalInstanceMapper approvalInstanceMapper;

    @Mock
    private ApprovalProcessMapper approvalProcessMapper;

    @InjectMocks
    private ApprovalInstanceService approvalInstanceService;

    private ApprovalProcess publishedProcess;
    private ApprovalInstance runningInstance;
    private ApprovalInstance completedInstance;

    @BeforeEach
    void setUp() {
        publishedProcess = ApprovalProcess.builder()
                .id(1L)
                .processName("请假审批流程")
                .processKey("leave_approval")
                .description("员工请假审批流程")
                .category("HR")
                .formSchema("{\"fields\":[]}")
                .flowConfig("{\"nodes\":[]}")
                .version(1)
                .status("PUBLISHED")
                .createdBy("admin")
                .createdAt(LocalDateTime.now())
                .build();

        runningInstance = ApprovalInstance.builder()
                .id(1L)
                .processId(1L)
                .processKey("leave_approval")
                .instanceNo("AI2024042100001")
                .title("请假申请")
                .applicant("张三")
                .applicantId(100L)
                .currentNodeId(1L)
                .currentNodeName("部门经理审批")
                .formData("{\"days\":3}")
                .totalAmount(BigDecimal.ZERO)
                .status("RUNNING")
                .startTime(LocalDateTime.now().minusHours(2))
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        completedInstance = ApprovalInstance.builder()
                .id(2L)
                .processId(1L)
                .processKey("leave_approval")
                .instanceNo("AI2024042100002")
                .title("报销申请")
                .applicant("李四")
                .applicantId(101L)
                .currentNodeId(2L)
                .currentNodeName("财务审批")
                .formData("{\"amount\":500}")
                .totalAmount(new BigDecimal("500.00"))
                .status("COMPLETED")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now())
                .duration(86400000L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    // ============= 正常业务流程测试 =============

    @Nested
    @DisplayName("getInstanceById - 正常流程")
    class GetInstanceByIdSuccess {
        @Test
        @DisplayName("根据ID获取审批实例成功")
        void testGetInstanceById_Success() {
            // Given
            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));

            // When
            ApprovalInstance result = approvalInstanceService.getInstanceById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("请假申请");
            verify(approvalInstanceMapper, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("getInstanceByNo - 正常流程")
    class GetInstanceByNoSuccess {
        @Test
        @DisplayName("根据实例编号获取审批实例成功")
        void testGetInstanceByNo_Success() {
            // Given
            when(approvalInstanceMapper.findByInstanceNo("AI2024042100001")).thenReturn(Optional.of(runningInstance));

            // When
            ApprovalInstance result = approvalInstanceService.getInstanceByNo("AI2024042100001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getInstanceNo()).isEqualTo("AI2024042100001");
            verify(approvalInstanceMapper, times(1)).findByInstanceNo("AI2024042100001");
        }
    }

    @Nested
    @DisplayName("createInstance - 正常流程")
    class CreateInstanceSuccess {
        @Test
        @DisplayName("创建审批实例成功")
        void testCreateInstance_Success() {
            // Given
            ApprovalInstance newInstance = ApprovalInstance.builder()
                    .processId(1L)
                    .title("新的请假申请")
                    .applicant("王五")
                    .applicantId(102L)
                    .formData("{\"days\":5}")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(publishedProcess));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> {
                ApprovalInstance saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            // When
            ApprovalInstance result = approvalInstanceService.createInstance(newInstance);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("RUNNING");
            assertThat(result.getProcessKey()).isEqualTo("leave_approval");
            assertThat(result.getInstanceNo()).isNotNull();
            verify(approvalInstanceMapper, times(1)).save(any(ApprovalInstance.class));
        }

        @Test
        @DisplayName("创建时使用流程默认标题")
        void testCreateInstance_UsesProcessTitle() {
            // Given
            ApprovalInstance newInstance = ApprovalInstance.builder()
                    .processId(1L)
                    .applicant("王五")
                    .applicantId(102L)
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(publishedProcess));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.createInstance(newInstance);

            // Then
            assertThat(result.getTitle()).isEqualTo("请假审批流程");
        }
    }

    @Nested
    @DisplayName("updateInstance - 正常流程")
    class UpdateInstanceSuccess {
        @Test
        @DisplayName("更新审批实例成功")
        void testUpdateInstance_Success() {
            // Given
            ApprovalInstance updateData = ApprovalInstance.builder()
                    .id(1L)
                    .title("更新的标题")
                    .formData("{\"days\":7}")
                    .build();

            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.updateInstance(updateData);

            // Then
            assertThat(result.getTitle()).isEqualTo("更新的标题");
            verify(approvalInstanceMapper, times(1)).save(any(ApprovalInstance.class));
        }

        @Test
        @DisplayName("更新时设置完成时间")
        void testUpdateInstance_SetCompleteTime() {
            // Given
            ApprovalInstance updateData = ApprovalInstance.builder()
                    .id(1L)
                    .status("COMPLETED")
                    .build();

            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.updateInstance(updateData);

            // Then
            assertThat(result.getEndTime()).isNotNull();
            assertThat(result.getDuration()).isNotNull();
        }
    }

    @Nested
    @DisplayName("withdrawInstance - 正常流程")
    class WithdrawInstanceSuccess {
        @Test
        @DisplayName("撤回审批实例成功")
        void testWithdrawInstance_Success() {
            // Given
            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.withdrawInstance(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo("WITHDRAWN");
            assertThat(result.getEndTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("terminateInstance - 正常流程")
    class TerminateInstanceSuccess {
        @Test
        @DisplayName("终止审批实例成功")
        void testTerminateInstance_Success() {
            // Given
            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.terminateInstance(1L, "业务调整");

            // Then
            assertThat(result.getStatus()).isEqualTo("TERMINATED");
            assertThat(result.getEndTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("approveInstance - 正常流程")
    class ApproveInstanceSuccess {
        @Test
        @DisplayName("审批通过成功")
        void testApproveInstance_Success() {
            // Given
            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.approveInstance(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            assertThat(result.getEndTime()).isNotNull();
            assertThat(result.getDuration()).isNotNull();
        }
    }

    @Nested
    @DisplayName("rejectInstance - 正常流程")
    class RejectInstanceSuccess {
        @Test
        @DisplayName("审批拒绝成功")
        void testRejectInstance_Success() {
            // Given
            when(approvalInstanceMapper.findById(1L)).thenReturn(Optional.of(runningInstance));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.rejectInstance(1L, "材料不齐全");

            // Then
            assertThat(result.getStatus()).isEqualTo("REJECTED");
            assertThat(result.getEndTime()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getInstancesByProcessId - 正常流程")
    class GetInstancesByProcessIdSuccess {
        @Test
        @DisplayName("根据流程ID获取实例列表成功")
        void testGetInstancesByProcessId_Success() {
            // Given
            List<ApprovalInstance> instances = Arrays.asList(runningInstance, completedInstance);
            when(approvalInstanceMapper.findByProcessId(1L)).thenReturn(instances);

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByProcessId(1L);

            // Then
            assertThat(result).hasSize(2);
            verify(approvalInstanceMapper, times(1)).findByProcessId(1L);
        }
    }

    @Nested
    @DisplayName("getInstancesByApplicant - 正常流程")
    class GetInstancesByApplicantSuccess {
        @Test
        @DisplayName("根据申请人获取实例列表成功")
        void testGetInstancesByApplicant_Success() {
            // Given
            List<ApprovalInstance> instances = Arrays.asList(runningInstance);
            when(approvalInstanceMapper.findByApplicant("张三")).thenReturn(instances);

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByApplicant("张三");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getApplicant()).isEqualTo("张三");
            verify(approvalInstanceMapper, times(1)).findByApplicant("张三");
        }
    }

    @Nested
    @DisplayName("getInstancesByStatus - 正常流程")
    class GetInstancesByStatusSuccess {
        @Test
        @DisplayName("根据状态获取实例列表成功")
        void testGetInstancesByStatus_Success() {
            // Given
            List<ApprovalInstance> instances = Arrays.asList(runningInstance);
            when(approvalInstanceMapper.findByStatus("RUNNING")).thenReturn(instances);

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByStatus("RUNNING");

            // Then
            assertThat(result).hasSize(1);
            verify(approvalInstanceMapper, times(1)).findByStatus("RUNNING");
        }
    }

    @Nested
    @DisplayName("getPendingInstances - 正常流程")
    class GetPendingInstancesSuccess {
        @Test
        @DisplayName("获取申请人的待审批实例成功")
        void testGetPendingInstances_Success() {
            // Given
            List<ApprovalInstance> instances = Arrays.asList(runningInstance);
            when(approvalInstanceMapper.findByApplicantIdAndStatus(100L, "RUNNING")).thenReturn(instances);

            // When
            List<ApprovalInstance> result = approvalInstanceService.getPendingInstances(100L);

            // Then
            assertThat(result).hasSize(1);
            verify(approvalInstanceMapper, times(1)).findByApplicantIdAndStatus(100L, "RUNNING");
        }
    }

    // ============= 异常情况处理测试 =============

    @Nested
    @DisplayName("getInstanceById - 异常情况")
    class GetInstanceByIdException {
        @Test
        @DisplayName("实例不存在时抛出ResourceNotFoundException")
        void testGetInstanceById_NotFound_ThrowsException() {
            // Given
            when(approvalInstanceMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.getInstanceById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ApprovalInstance not found");
        }
    }

    @Nested
    @DisplayName("getInstanceByNo - 异常情况")
    class GetInstanceByNoException {
        @Test
        @DisplayName("实例编号不存在时抛出异常")
        void testGetInstanceByNo_NotFound_ThrowsException() {
            // Given
            when(approvalInstanceMapper.findByInstanceNo("NON_EXISTING")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.getInstanceByNo("NON_EXISTING"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createInstance - 异常情况")
    class CreateInstanceException {
        @Test
        @DisplayName("流程不存在时抛出ResourceNotFoundException")
        void testCreateInstance_ProcessNotFound_ThrowsException() {
            // Given
            ApprovalInstance newInstance = ApprovalInstance.builder()
                    .processId(999L)
                    .title("测试")
                    .build();

            when(approvalProcessMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.createInstance(newInstance))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("流程未发布时抛出IllegalArgumentException")
        void testCreateInstance_ProcessNotPublished_ThrowsException() {
            // Given
            ApprovalProcess draftProcess = ApprovalProcess.builder()
                    .id(1L)
                    .status("DRAFT")
                    .build();

            ApprovalInstance newInstance = ApprovalInstance.builder()
                    .processId(1L)
                    .title("测试")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.createInstance(newInstance))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unpublished");
        }
    }

    @Nested
    @DisplayName("updateInstance - 异常情况")
    class UpdateInstanceException {
        @Test
        @DisplayName("实例不存在时抛出ResourceNotFoundException")
        void testUpdateInstance_NotFound_ThrowsException() {
            // Given
            ApprovalInstance updateData = ApprovalInstance.builder()
                    .id(999L)
                    .title("更新")
                    .build();

            when(approvalInstanceMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.updateInstance(updateData))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("更新已完成的实例时抛出IllegalArgumentException")
        void testUpdateInstance_AlreadyCompleted_ThrowsException() {
            // Given
            ApprovalInstance updateData = ApprovalInstance.builder()
                    .id(2L)
                    .title("更新")
                    .build();

            when(approvalInstanceMapper.findById(2L)).thenReturn(Optional.of(completedInstance));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.updateInstance(updateData))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("completed");
        }

        @Test
        @DisplayName("更新已取消的实例时抛出IllegalArgumentException")
        void testUpdateInstance_AlreadyCancelled_ThrowsException() {
            // Given
            ApprovalInstance cancelledInstance = ApprovalInstance.builder()
                    .id(3L)
                    .status("CANCELLED")
                    .build();

            ApprovalInstance updateData = ApprovalInstance.builder()
                    .id(3L)
                    .title("更新")
                    .build();

            when(approvalInstanceMapper.findById(3L)).thenReturn(Optional.of(cancelledInstance));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.updateInstance(updateData))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("withdrawInstance - 异常情况")
    class WithdrawInstanceException {
        @Test
        @DisplayName("撤回非运行中的实例时抛出IllegalArgumentException")
        void testWithdrawInstance_NotRunning_ThrowsException() {
            // Given
            when(approvalInstanceMapper.findById(2L)).thenReturn(Optional.of(completedInstance));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.withdrawInstance(2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("running");
        }
    }

    @Nested
    @DisplayName("terminateInstance - 异常情况")
    class TerminateInstanceException {
        @Test
        @DisplayName("终止非运行中的实例时抛出IllegalArgumentException")
        void testTerminateInstance_NotRunning_ThrowsException() {
            // Given
            when(approvalInstanceMapper.findById(2L)).thenReturn(Optional.of(completedInstance));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.terminateInstance(2L, "原因"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("approveInstance - 异常情况")
    class ApproveInstanceException {
        @Test
        @DisplayName("审批非运行中的实例时抛出IllegalArgumentException")
        void testApproveInstance_NotRunning_ThrowsException() {
            // Given
            when(approvalInstanceMapper.findById(2L)).thenReturn(Optional.of(completedInstance));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.approveInstance(2L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("rejectInstance - 异常情况")
    class RejectInstanceException {
        @Test
        @DisplayName("拒绝非运行中的实例时抛出IllegalArgumentException")
        void testRejectInstance_NotRunning_ThrowsException() {
            // Given
            when(approvalInstanceMapper.findById(2L)).thenReturn(Optional.of(completedInstance));

            // When/Then
            assertThatThrownBy(() -> approvalInstanceService.rejectInstance(2L, "原因"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ============= 边界条件测试 =============

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        @Test
        @DisplayName("根据流程ID获取空列表")
        void testGetInstancesByProcessId_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalInstanceMapper.findByProcessId(999L)).thenReturn(Arrays.asList());

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByProcessId(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据申请人获取空列表")
        void testGetInstancesByApplicant_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalInstanceMapper.findByApplicant("non_existing")).thenReturn(Arrays.asList());

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByApplicant("non_existing");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据状态获取空列表")
        void testGetInstancesByStatus_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalInstanceMapper.findByStatus("COMPLETED")).thenReturn(Arrays.asList());

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByStatus("COMPLETED");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("获取待审批实例空列表")
        void testGetPendingInstances_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalInstanceMapper.findByApplicantIdAndStatus(any(), any())).thenReturn(Arrays.asList());

            // When
            List<ApprovalInstance> result = approvalInstanceService.getPendingInstances(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("金额为0的实例创建成功")
        void testCreateInstance_ZeroAmount_Success() {
            // Given
            ApprovalInstance newInstance = ApprovalInstance.builder()
                    .processId(1L)
                    .title("测试")
                    .applicant("测试用户")
                    .applicantId(100L)
                    .totalAmount(BigDecimal.ZERO)
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(publishedProcess));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.createInstance(newInstance);

            // Then
            assertThat(result.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("大金额实例创建成功")
        void testCreateInstance_LargeAmount_Success() {
            // Given
            ApprovalInstance newInstance = ApprovalInstance.builder()
                    .processId(1L)
                    .title("大额报销")
                    .applicant("测试用户")
                    .applicantId(100L)
                    .totalAmount(new BigDecimal("999999999.99"))
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(publishedProcess));
            when(approvalInstanceMapper.save(any(ApprovalInstance.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalInstance result = approvalInstanceService.createInstance(newInstance);

            // Then
            assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("999999999.99"));
        }
    }

    // ============= 并发安全性测试 =============

    @Nested
    @DisplayName("并发安全性测试")
    class ConcurrencyTests {
        @Test
        @DisplayName("多次调用countByStatus方法线程安全")
        void testCountByStatus_ThreadSafe() {
            // Given
            when(approvalInstanceMapper.countByStatus("RUNNING")).thenReturn(10L);

            // When & Then
            long count1 = approvalInstanceService.countByStatus("RUNNING");
            long count2 = approvalInstanceService.countByStatus("RUNNING");

            assertThat(count1).isEqualTo(10L);
            assertThat(count2).isEqualTo(10L);
            verify(approvalInstanceMapper, times(2)).countByStatus("RUNNING");
        }

        @Test
        @DisplayName("多次调用countByApplicantAndStatus方法线程安全")
        void testCountByApplicantAndStatus_ThreadSafe() {
            // Given
            when(approvalInstanceMapper.countByApplicantIdAndStatus(100L, "RUNNING")).thenReturn(5L);

            // When & Then
            long count1 = approvalInstanceService.countByApplicantAndStatus(100L, "RUNNING");
            long count2 = approvalInstanceService.countByApplicantAndStatus(100L, "RUNNING");

            assertThat(count1).isEqualTo(5L);
            assertThat(count2).isEqualTo(5L);
        }

        @Test
        @DisplayName("getInstancesByDateRange方法线程安全")
        void testGetInstancesByDateRange_ThreadSafe() {
            // Given
            LocalDateTime startDate = LocalDateTime.now().minusDays(7);
            LocalDateTime endDate = LocalDateTime.now();
            when(approvalInstanceMapper.findByStatusAndDateRange(eq("COMPLETED"), any(), any()))
                    .thenReturn(Arrays.asList(completedInstance));

            // When
            List<ApprovalInstance> result = approvalInstanceService.getInstancesByDateRange("COMPLETED", startDate, endDate);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}