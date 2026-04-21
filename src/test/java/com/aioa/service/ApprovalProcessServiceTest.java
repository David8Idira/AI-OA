package com.aioa.service;

import com.aioa.entity.ApprovalProcess;
import com.aioa.exception.ResourceConflictException;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.ApprovalProcessMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ApprovalProcessService单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalProcessService测试")
class ApprovalProcessServiceTest {

    @Mock
    private ApprovalProcessMapper approvalProcessMapper;

    @InjectMocks
    private ApprovalProcessService approvalProcessService;

    private ApprovalProcess draftProcess;
    private ApprovalProcess publishedProcess;

    @BeforeEach
    void setUp() {
        draftProcess = ApprovalProcess.builder()
                .id(1L)
                .processName("请假审批流程")
                .processKey("leave_approval")
                .description("员工请假审批流程")
                .category("HR")
                .formSchema("{\"fields\":[]}")
                .flowConfig("{\"nodes\":[]}")
                .version(1)
                .status("DRAFT")
                .createdBy("admin")
                .createdAt(LocalDateTime.now())
                .build();

        publishedProcess = ApprovalProcess.builder()
                .id(2L)
                .processName("报销审批流程")
                .processKey("expense_approval")
                .description("费用报销审批流程")
                .category("FINANCE")
                .formSchema("{\"fields\":[{\"name\":\"amount\"}]}")
                .flowConfig("{\"nodes\":[{\"id\":\"start\"}]}")
                .version(2)
                .status("PUBLISHED")
                .createdBy("admin")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ============= 正常业务流程测试 =============

    @Nested
    @DisplayName("getProcessById - 正常流程")
    class GetProcessByIdSuccess {
        @Test
        @DisplayName("根据ID获取审批流程成功")
        void testGetProcessById_Success() {
            // Given
            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));

            // When
            ApprovalProcess result = approvalProcessService.getProcessById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProcessName()).isEqualTo("请假审批流程");
            verify(approvalProcessMapper, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("getProcessByKey - 正常流程")
    class GetProcessByKeySuccess {
        @Test
        @DisplayName("根据流程Key获取审批流程成功")
        void testGetProcessByKey_Success() {
            // Given
            when(approvalProcessMapper.findByProcessKey("leave_approval")).thenReturn(Optional.of(draftProcess));

            // When
            ApprovalProcess result = approvalProcessService.getProcessByKey("leave_approval");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProcessKey()).isEqualTo("leave_approval");
            verify(approvalProcessMapper, times(1)).findByProcessKey("leave_approval");
        }
    }

    @Nested
    @DisplayName("createProcess - 正常流程")
    class CreateProcessSuccess {
        @Test
        @DisplayName("创建审批流程成功")
        void testCreateProcess_Success() {
            // Given
            ApprovalProcess newProcess = ApprovalProcess.builder()
                    .processName("新审批流程")
                    .processKey("new_approval")
                    .description("新创建的审批流程")
                    .category("IT")
                    .build();

            when(approvalProcessMapper.existsByProcessKey("new_approval")).thenReturn(false);
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> {
                ApprovalProcess saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            // When
            ApprovalProcess result = approvalProcessService.createProcess(newProcess);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProcessName()).isEqualTo("新审批流程");
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getVersion()).isEqualTo(1);
            verify(approvalProcessMapper, times(1)).existsByProcessKey("new_approval");
            verify(approvalProcessMapper, times(1)).save(any(ApprovalProcess.class));
        }

        @Test
        @DisplayName("创建时自动设置默认状态为DRAFT")
        void testCreateProcess_SetsDefaultStatus() {
            // Given
            ApprovalProcess newProcess = ApprovalProcess.builder()
                    .processName("测试流程")
                    .processKey("test_approval")
                    .build();

            when(approvalProcessMapper.existsByProcessKey("test_approval")).thenReturn(false);
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalProcess result = approvalProcessService.createProcess(newProcess);

            // Then
            assertThat(result.getStatus()).isEqualTo("DRAFT");
            assertThat(result.getVersion()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("updateProcess - 正常流程")
    class UpdateProcessSuccess {
        @Test
        @DisplayName("更新审批流程成功")
        void testUpdateProcess_Success() {
            // Given
            ApprovalProcess updateData = ApprovalProcess.builder()
                    .id(1L)
                    .processName("更新的流程名称")
                    .description("更新的描述")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalProcess result = approvalProcessService.updateProcess(updateData);

            // Then
            assertThat(result.getProcessName()).isEqualTo("更新的流程名称");
            assertThat(result.getVersion()).isEqualTo(2); // 版本号增加
            verify(approvalProcessMapper, times(1)).findById(1L);
            verify(approvalProcessMapper, times(1)).save(any(ApprovalProcess.class));
        }

        @Test
        @DisplayName("更新流程Key成功")
        void testUpdateProcess_ChangeKey_Success() {
            // Given
            ApprovalProcess updateData = ApprovalProcess.builder()
                    .id(1L)
                    .processKey("new_key")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));
            when(approvalProcessMapper.existsByProcessKey("new_key")).thenReturn(false);
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalProcess result = approvalProcessService.updateProcess(updateData);

            // Then
            assertThat(result.getProcessKey()).isEqualTo("new_key");
        }
    }

    @Nested
    @DisplayName("deleteProcess - 正常流程")
    class DeleteProcessSuccess {
        @Test
        @DisplayName("删除审批流程成功")
        void testDeleteProcess_Success() {
            // Given
            when(approvalProcessMapper.existsById(1L)).thenReturn(true);
            doNothing().when(approvalProcessMapper).deleteById(1L);

            // When
            approvalProcessService.deleteProcess(1L);

            // Then
            verify(approvalProcessMapper, times(1)).existsById(1L);
            verify(approvalProcessMapper, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("publishProcess - 正常流程")
    class PublishProcessSuccess {
        @Test
        @DisplayName("发布审批流程成功")
        void testPublishProcess_Success() {
            // Given
            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalProcess result = approvalProcessService.publishProcess(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo("PUBLISHED");
            verify(approvalProcessMapper, times(1)).save(any(ApprovalProcess.class));
        }
    }

    @Nested
    @DisplayName("deactivateProcess - 正常流程")
    class DeactivateProcessSuccess {
        @Test
        @DisplayName("停用审批流程成功")
        void testDeactivateProcess_Success() {
            // Given
            when(approvalProcessMapper.findById(2L)).thenReturn(Optional.of(publishedProcess));
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalProcess result = approvalProcessService.deactivateProcess(2L);

            // Then
            assertThat(result.getStatus()).isEqualTo("INACTIVE");
        }
    }

    @Nested
    @DisplayName("getProcessesByCategory - 正常流程")
    class GetProcessesByCategorySuccess {
        @Test
        @DisplayName("根据分类获取审批流程列表成功")
        void testGetProcessesByCategory_Success() {
            // Given
            List<ApprovalProcess> processes = Arrays.asList(draftProcess);
            when(approvalProcessMapper.findByCategory("HR")).thenReturn(processes);

            // When
            List<ApprovalProcess> result = approvalProcessService.getProcessesByCategory("HR");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCategory()).isEqualTo("HR");
            verify(approvalProcessMapper, times(1)).findByCategory("HR");
        }
    }

    @Nested
    @DisplayName("getProcessesByStatus - 正常流程")
    class GetProcessesByStatusSuccess {
        @Test
        @DisplayName("根据状态获取审批流程列表成功")
        void testGetProcessesByStatus_Success() {
            // Given
            List<ApprovalProcess> processes = Arrays.asList(draftProcess);
            when(approvalProcessMapper.findByStatus("DRAFT")).thenReturn(processes);

            // When
            List<ApprovalProcess> result = approvalProcessService.getProcessesByStatus("DRAFT");

            // Then
            assertThat(result).hasSize(1);
            verify(approvalProcessMapper, times(1)).findByStatus("DRAFT");
        }
    }

    @Nested
    @DisplayName("searchProcesses - 正常流程")
    class SearchProcessesSuccess {
        @Test
        @DisplayName("搜索审批流程成功")
        void testSearchProcesses_Success() {
            // Given
            List<ApprovalProcess> processes = Arrays.asList(draftProcess);
            when(approvalProcessMapper.searchProcesses("请假", "HR", "DRAFT")).thenReturn(processes);

            // When
            List<ApprovalProcess> result = approvalProcessService.searchProcesses("请假", "HR", "DRAFT");

            // Then
            assertThat(result).hasSize(1);
            verify(approvalProcessMapper, times(1)).searchProcesses("请假", "HR", "DRAFT");
        }

        @Test
        @DisplayName("空条件搜索返回所有流程")
        void testSearchProcesses_AllNull_ReturnsAll() {
            // Given
            List<ApprovalProcess> processes = Arrays.asList(draftProcess, publishedProcess);
            when(approvalProcessMapper.searchProcesses(null, null, null)).thenReturn(processes);

            // When
            List<ApprovalProcess> result = approvalProcessService.searchProcesses(null, null, null);

            // Then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("copyProcess - 正常流程")
    class CopyProcessSuccess {
        @Test
        @DisplayName("复制审批流程成功")
        void testCopyProcess_Success() {
            // Given
            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));
            when(approvalProcessMapper.existsByProcessKey("copied_approval")).thenReturn(false);
            when(approvalProcessMapper.save(any(ApprovalProcess.class))).thenAnswer(invocation -> {
                ApprovalProcess saved = invocation.getArgument(0);
                saved.setId(3L);
                return saved;
            });

            // When
            ApprovalProcess result = approvalProcessService.copyProcess(1L, "copied_approval", "复制的请假流程");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getProcessKey()).isEqualTo("copied_approval");
            assertThat(result.getProcessName()).isEqualTo("复制的请假流程");
            assertThat(result.getVersion()).isEqualTo(1);
            assertThat(result.getStatus()).isEqualTo("DRAFT");
        }
    }

    // ============= 异常情况处理测试 =============

    @Nested
    @DisplayName("getProcessById - 异常情况")
    class GetProcessByIdException {
        @Test
        @DisplayName("流程不存在时抛出ResourceNotFoundException")
        void testGetProcessById_NotFound_ThrowsException() {
            // Given
            when(approvalProcessMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.getProcessById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ApprovalProcess not found");
        }
    }

    @Nested
    @DisplayName("getProcessByKey - 异常情况")
    class GetProcessByKeyException {
        @Test
        @DisplayName("流程Key不存在时抛出异常")
        void testGetProcessByKey_NotFound_ThrowsException() {
            // Given
            when(approvalProcessMapper.findByProcessKey("non_existing")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.getProcessByKey("non_existing"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createProcess - 异常情况")
    class CreateProcessException {
        @Test
        @DisplayName("流程Key已存在时抛出ResourceConflictException")
        void testCreateProcess_KeyExists_ThrowsException() {
            // Given
            ApprovalProcess newProcess = ApprovalProcess.builder()
                    .processName("新流程")
                    .processKey("leave_approval")
                    .build();

            when(approvalProcessMapper.existsByProcessKey("leave_approval")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.createProcess(newProcess))
                    .isInstanceOf(ResourceConflictException.class)
                    .hasMessageContaining("processKey");
        }
    }

    @Nested
    @DisplayName("updateProcess - 异常情况")
    class UpdateProcessException {
        @Test
        @DisplayName("流程不存在时抛出ResourceNotFoundException")
        void testUpdateProcess_NotFound_ThrowsException() {
            // Given
            ApprovalProcess updateData = ApprovalProcess.builder()
                    .id(999L)
                    .processName("更新")
                    .build();

            when(approvalProcessMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.updateProcess(updateData))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("更新流程Key冲突时抛出ResourceConflictException")
        void testUpdateProcess_KeyConflict_ThrowsException() {
            // Given
            ApprovalProcess updateData = ApprovalProcess.builder()
                    .id(1L)
                    .processKey("expense_approval")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));
            when(approvalProcessMapper.existsByProcessKey("expense_approval")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.updateProcess(updateData))
                    .isInstanceOf(ResourceConflictException.class);
        }
    }

    @Nested
    @DisplayName("deleteProcess - 异常情况")
    class DeleteProcessException {
        @Test
        @DisplayName("删除不存在的流程时抛出ResourceNotFoundException")
        void testDeleteProcess_NotFound_ThrowsException() {
            // Given
            when(approvalProcessMapper.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.deleteProcess(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("publishProcess - 异常情况")
    class PublishProcessException {
        @Test
        @DisplayName("已发布的流程再次发布时抛出IllegalArgumentException")
        void testPublishProcess_AlreadyPublished_ThrowsException() {
            // Given
            when(approvalProcessMapper.findById(2L)).thenReturn(Optional.of(publishedProcess));

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.publishProcess(2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already published");
        }

        @Test
        @DisplayName("流程缺少表单配置时发布失败")
        void testPublishProcess_MissingFormSchema_ThrowsException() {
            // Given
            ApprovalProcess processWithoutForm = ApprovalProcess.builder()
                    .id(1L)
                    .processName("测试")
                    .processKey("test")
                    .status("DRAFT")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(processWithoutForm));

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.publishProcess(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("form schema");
        }

        @Test
        @DisplayName("流程缺少流程配置时发布失败")
        void testPublishProcess_MissingFlowConfig_ThrowsException() {
            // Given
            ApprovalProcess processWithoutFlow = ApprovalProcess.builder()
                    .id(1L)
                    .processName("测试")
                    .processKey("test")
                    .status("DRAFT")
                    .formSchema("{\"fields\":[]}")
                    .build();

            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(processWithoutFlow));

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.publishProcess(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("flow config");
        }
    }

    @Nested
    @DisplayName("deactivateProcess - 异常情况")
    class DeactivateProcessException {
        @Test
        @DisplayName("停用草稿状态的流程时抛出IllegalArgumentException")
        void testDeactivateProcess_Draft_ThrowsException() {
            // Given
            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.deactivateProcess(1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("draft");
        }
    }

    @Nested
    @DisplayName("copyProcess - 异常情况")
    class CopyProcessException {
        @Test
        @DisplayName("复制不存在的流程时抛出ResourceNotFoundException")
        void testCopyProcess_NotFound_ThrowsException() {
            // Given
            when(approvalProcessMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.copyProcess(999L, "new_key", "新名称"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("复制的流程Key已存在时抛出ResourceConflictException")
        void testCopyProcess_KeyExists_ThrowsException() {
            // Given
            when(approvalProcessMapper.findById(1L)).thenReturn(Optional.of(draftProcess));
            when(approvalProcessMapper.existsByProcessKey("leave_approval")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> approvalProcessService.copyProcess(1L, "leave_approval", "新名称"))
                    .isInstanceOf(ResourceConflictException.class);
        }
    }

    // ============= 边界条件测试 =============

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        @Test
        @DisplayName("搜索返回空列表")
        void testSearchProcesses_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalProcessMapper.searchProcesses(any(), any(), any())).thenReturn(Arrays.asList());

            // When
            List<ApprovalProcess> result = approvalProcessService.searchProcesses("不存在的流程", "HR", "DRAFT");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据分类获取空列表")
        void testGetProcessesByCategory_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalProcessMapper.findByCategory("NON_EXISTING")).thenReturn(Arrays.asList());

            // When
            List<ApprovalProcess> result = approvalProcessService.getProcessesByCategory("NON_EXISTING");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据状态获取空列表")
        void testGetProcessesByStatus_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalProcessMapper.findByStatus("NON_EXISTING")).thenReturn(Arrays.asList());

            // When
            List<ApprovalProcess> result = approvalProcessService.getProcessesByStatus("NON_EXISTING");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("获取所有流程返回空列表")
        void testGetAllProcesses_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalProcessMapper.findAll()).thenReturn(Arrays.asList());

            // When
            List<ApprovalProcess> result = approvalProcessService.getAllProcesses();

            // Then
            assertThat(result).isEmpty();
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
            when(approvalProcessMapper.countByStatus("PUBLISHED")).thenReturn(5L);

            // When & Then
            long count1 = approvalProcessService.countByStatus("PUBLISHED");
            long count2 = approvalProcessService.countByStatus("PUBLISHED");

            assertThat(count1).isEqualTo(5L);
            assertThat(count2).isEqualTo(5L);
            verify(approvalProcessMapper, times(2)).countByStatus("PUBLISHED");
        }

        @Test
        @DisplayName("getAllProcesses方法线程安全")
        void testGetAllProcesses_ThreadSafe() {
            // Given
            List<ApprovalProcess> processes = Arrays.asList(draftProcess, publishedProcess);
            when(approvalProcessMapper.findAll()).thenReturn(processes);

            // When
            List<ApprovalProcess> result = approvalProcessService.getAllProcesses();

            // Then
            assertThat(result).hasSize(2);
        }
    }
}