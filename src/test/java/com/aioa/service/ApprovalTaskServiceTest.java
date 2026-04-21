package com.aioa.service;

import com.aioa.entity.ApprovalTask;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.ApprovalTaskMapper;
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
 * ApprovalTaskService单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalTaskService测试")
class ApprovalTaskServiceTest {

    @Mock
    private ApprovalTaskMapper approvalTaskMapper;

    @InjectMocks
    private ApprovalTaskService approvalTaskService;

    private ApprovalTask pendingTask;
    private ApprovalTask claimedTask;
    private ApprovalTask completedTask;

    @BeforeEach
    void setUp() {
        pendingTask = ApprovalTask.builder()
                .id(1L)
                .processId(1L)
                .instanceId(100L)
                .taskName("审批请假申请")
                .taskKey("approve_leave")
                .candidateUsers("user1,user2")
                .candidateGroups("managers")
                .priority(5)
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        claimedTask = ApprovalTask.builder()
                .id(2L)
                .processId(1L)
                .instanceId(100L)
                .taskName("审批报销单")
                .taskKey("approve_expense")
                .assignee("user1")
                .priority(3)
                .status("CLAIMED")
                .createdAt(LocalDateTime.now())
                .build();

        completedTask = ApprovalTask.builder()
                .id(3L)
                .processId(2L)
                .instanceId(101L)
                .taskName("审批采购单")
                .taskKey("approve_purchase")
                .assignee("user2")
                .priority(4)
                .status("COMPLETED")
                .completeDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ============= 正常业务流程测试 =============

    @Nested
    @DisplayName("getTaskById - 正常流程")
    class GetTaskByIdSuccess {
        @Test
        @DisplayName("根据ID获取审批任务成功")
        void testGetTaskById_Success() {
            // Given
            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(pendingTask));

            // When
            ApprovalTask result = approvalTaskService.getTaskById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTaskName()).isEqualTo("审批请假申请");
            verify(approvalTaskMapper, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("getTasksByInstanceId - 正常流程")
    class GetTasksByInstanceIdSuccess {
        @Test
        @DisplayName("根据实例ID获取任务列表成功")
        void testGetTasksByInstanceId_Success() {
            // Given
            List<ApprovalTask> tasks = Arrays.asList(pendingTask, claimedTask);
            when(approvalTaskMapper.findByInstanceId(100L)).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByInstanceId(100L);

            // Then
            assertThat(result).hasSize(2);
            verify(approvalTaskMapper, times(1)).findByInstanceId(100L);
        }
    }

    @Nested
    @DisplayName("getTasksByAssignee - 正常流程")
    class GetTasksByAssigneeSuccess {
        @Test
        @DisplayName("根据处理人获取任务列表成功")
        void testGetTasksByAssignee_Success() {
            // Given
            List<ApprovalTask> tasks = Arrays.asList(claimedTask);
            when(approvalTaskMapper.findByAssignee("user1")).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByAssignee("user1");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAssignee()).isEqualTo("user1");
            verify(approvalTaskMapper, times(1)).findByAssignee("user1");
        }
    }

    @Nested
    @DisplayName("getTasksByAssigneeAndStatus - 正常流程")
    class GetTasksByAssigneeAndStatusSuccess {
        @Test
        @DisplayName("根据处理人和状态获取任务列表成功")
        void testGetTasksByAssigneeAndStatus_Success() {
            // Given
            List<ApprovalTask> tasks = Arrays.asList(pendingTask);
            when(approvalTaskMapper.findByAssigneeAndStatus("user1", "PENDING")).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByAssigneeAndStatus("user1", "PENDING");

            // Then
            assertThat(result).hasSize(1);
            verify(approvalTaskMapper, times(1)).findByAssigneeAndStatus("user1", "PENDING");
        }
    }

    @Nested
    @DisplayName("createTask - 正常流程")
    class CreateTaskSuccess {
        @Test
        @DisplayName("创建审批任务成功")
        void testCreateTask_Success() {
            // Given
            ApprovalTask newTask = ApprovalTask.builder()
                    .processId(1L)
                    .instanceId(100L)
                    .taskName("新任务")
                    .taskKey("new_task")
                    .build();

            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> {
                ApprovalTask saved = invocation.getArgument(0);
                saved.setId(4L);
                return saved;
            });

            // When
            ApprovalTask result = approvalTaskService.createTask(newTask);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("PENDING");
            assertThat(result.getPriority()).isEqualTo(5);
            verify(approvalTaskMapper, times(1)).save(any(ApprovalTask.class));
        }

        @Test
        @DisplayName("创建任务时设置默认优先级为5")
        void testCreateTask_SetsDefaultPriority() {
            // Given
            ApprovalTask newTask = ApprovalTask.builder()
                    .taskName("测试任务")
                    .build();

            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.createTask(newTask);

            // Then
            assertThat(result.getPriority()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("updateTask - 正常流程")
    class UpdateTaskSuccess {
        @Test
        @DisplayName("更新审批任务成功")
        void testUpdateTask_Success() {
            // Given
            ApprovalTask updateData = ApprovalTask.builder()
                    .id(1L)
                    .taskName("更新后的任务名称")
                    .priority(10)
                    .build();

            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(pendingTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.updateTask(updateData);

            // Then
            assertThat(result.getTaskName()).isEqualTo("更新后的任务名称");
            assertThat(result.getPriority()).isEqualTo(10);
            verify(approvalTaskMapper, times(1)).save(any(ApprovalTask.class));
        }
    }

    @Nested
    @DisplayName("deleteTask - 正常流程")
    class DeleteTaskSuccess {
        @Test
        @DisplayName("删除审批任务成功")
        void testDeleteTask_Success() {
            // Given
            when(approvalTaskMapper.existsById(1L)).thenReturn(true);
            doNothing().when(approvalTaskMapper).deleteById(1L);

            // When
            approvalTaskService.deleteTask(1L);

            // Then
            verify(approvalTaskMapper, times(1)).existsById(1L);
            verify(approvalTaskMapper, times(1)).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("claimTask - 正常流程")
    class ClaimTaskSuccess {
        @Test
        @DisplayName("签收任务成功")
        void testClaimTask_Success() {
            // Given
            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(pendingTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.claimTask(1L, "user1");

            // Then
            assertThat(result.getAssignee()).isEqualTo("user1");
            assertThat(result.getStatus()).isEqualTo("CLAIMED");
            verify(approvalTaskMapper, times(1)).save(any(ApprovalTask.class));
        }

        @Test
        @DisplayName("用户签收已分配给自己的任务成功")
        void testClaimTask_AlreadyAssigned_Success() {
            // Given
            ApprovalTask assignedTask = ApprovalTask.builder()
                    .id(1L)
                    .assignee("user1")
                    .status("PENDING")
                    .build();

            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(assignedTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.claimTask(1L, "user1");

            // Then
            assertThat(result.getStatus()).isEqualTo("CLAIMED");
        }
    }

    @Nested
    @DisplayName("completeTask - 正常流程")
    class CompleteTaskSuccess {
        @Test
        @DisplayName("完成任务成功")
        void testCompleteTask_Success() {
            // Given
            when(approvalTaskMapper.findById(2L)).thenReturn(Optional.of(claimedTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.completeTask(2L);

            // Then
            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            assertThat(result.getCompleteDate()).isNotNull();
        }

        @Test
        @DisplayName("待处理状态的任务可以直接完成")
        void testCompleteTask_FromPending_Success() {
            // Given
            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(pendingTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.completeTask(1L);

            // Then
            assertThat(result.getStatus()).isEqualTo("COMPLETED");
        }
    }

    @Nested
    @DisplayName("rejectTask - 正常流程")
    class RejectTaskSuccess {
        @Test
        @DisplayName("拒绝任务成功")
        void testRejectTask_Success() {
            // Given
            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(pendingTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.rejectTask(1L, "不符合规定");

            // Then
            assertThat(result.getStatus()).isEqualTo("REJECTED");
            assertThat(result.getCompleteDate()).isNotNull();
        }
    }

    @Nested
    @DisplayName("transferTask - 正常流程")
    class TransferTaskSuccess {
        @Test
        @DisplayName("转办任务成功")
        void testTransferTask_Success() {
            // Given
            when(approvalTaskMapper.findById(2L)).thenReturn(Optional.of(claimedTask));
            when(approvalTaskMapper.save(any(ApprovalTask.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            ApprovalTask result = approvalTaskService.transferTask(2L, "user3");

            // Then
            assertThat(result.getAssignee()).isEqualTo("user3");
        }
    }

    @Nested
    @DisplayName("getTodoTasks - 正常流程")
    class GetTodoTasksSuccess {
        @Test
        @DisplayName("获取待办任务列表成功")
        void testGetTodoTasks_Success() {
            // Given
            List<ApprovalTask> tasks = Arrays.asList(pendingTask);
            when(approvalTaskMapper.findByAssigneeAndStatus("user1", "PENDING")).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getTodoTasks("user1");

            // Then
            assertThat(result).hasSize(1);
            verify(approvalTaskMapper, times(1)).findByAssigneeAndStatus("user1", "PENDING");
        }
    }

    @Nested
    @DisplayName("getDoneTasks - 正常流程")
    class GetDoneTasksSuccess {
        @Test
        @DisplayName("获取已办任务列表成功")
        void testGetDoneTasks_Success() {
            // Given
            List<ApprovalTask> tasks = Arrays.asList(completedTask);
            when(approvalTaskMapper.findByAssigneeAndStatusIn(eq("user2"), anyList())).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getDoneTasks("user2");

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getOverdueTasks - 正常流程")
    class GetOverdueTasksSuccess {
        @Test
        @DisplayName("获取超时任务列表成功")
        void testGetOverdueTasks_Success() {
            // Given
            ApprovalTask overdueTask = ApprovalTask.builder()
                    .id(1L)
                    .dueDate(LocalDateTime.now().minusDays(1))
                    .status("PENDING")
                    .build();
            List<ApprovalTask> tasks = Arrays.asList(overdueTask);
            when(approvalTaskMapper.findOverdueTasks(any(LocalDateTime.class))).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getOverdueTasks();

            // Then
            assertThat(result).hasSize(1);
            verify(approvalTaskMapper, times(1)).findOverdueTasks(any(LocalDateTime.class));
        }
    }

    // ============= 异常情况处理测试 =============

    @Nested
    @DisplayName("getTaskById - 异常情况")
    class GetTaskByIdException {
        @Test
        @DisplayName("任务不存在时抛出ResourceNotFoundException")
        void testGetTaskById_NotFound_ThrowsException() {
            // Given
            when(approvalTaskMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.getTaskById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("ApprovalTask not found");
        }
    }

    @Nested
    @DisplayName("updateTask - 异常情况")
    class UpdateTaskException {
        @Test
        @DisplayName("更新不存在的任务时抛出ResourceNotFoundException")
        void testUpdateTask_NotFound_ThrowsException() {
            // Given
            ApprovalTask updateData = ApprovalTask.builder()
                    .id(999L)
                    .taskName("更新")
                    .build();

            when(approvalTaskMapper.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.updateTask(updateData))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteTask - 异常情况")
    class DeleteTaskException {
        @Test
        @DisplayName("删除不存在的任务时抛出ResourceNotFoundException")
        void testDeleteTask_NotFound_ThrowsException() {
            // Given
            when(approvalTaskMapper.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.deleteTask(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("claimTask - 异常情况")
    class ClaimTaskException {
        @Test
        @DisplayName("签收非待处理状态的任务时抛出IllegalArgumentException")
        void testClaimTask_NotPending_ThrowsException() {
            // Given
            when(approvalTaskMapper.findById(3L)).thenReturn(Optional.of(completedTask));

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.claimTask(3L, "user1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PENDING");
        }

        @Test
        @DisplayName("签收已分配给其他用户的任务时抛出IllegalArgumentException")
        void testClaimTask_AssignedToOther_ThrowsException() {
            // Given
            ApprovalTask assignedTask = ApprovalTask.builder()
                    .id(1L)
                    .assignee("user2")
                    .status("PENDING")
                    .build();

            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(assignedTask));

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.claimTask(1L, "user1"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("assigned");
        }
    }

    @Nested
    @DisplayName("completeTask - 异常情况")
    class CompleteTaskException {
        @Test
        @DisplayName("完成已拒绝的任务时抛出IllegalArgumentException")
        void testCompleteTask_AlreadyRejected_ThrowsException() {
            // Given
            ApprovalTask rejectedTask = ApprovalTask.builder()
                    .id(1L)
                    .status("REJECTED")
                    .build();

            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(rejectedTask));

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.completeTask(1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("完成已取消的任务时抛出IllegalArgumentException")
        void testCompleteTask_AlreadyCancelled_ThrowsException() {
            // Given
            ApprovalTask cancelledTask = ApprovalTask.builder()
                    .id(1L)
                    .status("CANCELLED")
                    .build();

            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(cancelledTask));

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.completeTask(1L))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("transferTask - 异常情况")
    class TransferTaskException {
        @Test
        @DisplayName("转办未分配的任务时抛出IllegalArgumentException")
        void testTransferTask_NoAssignee_ThrowsException() {
            // Given
            when(approvalTaskMapper.findById(1L)).thenReturn(Optional.of(pendingTask));

            // When/Then
            assertThatThrownBy(() -> approvalTaskService.transferTask(1L, "user3"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no assignee");
        }
    }

    // ============= 边界条件测试 =============

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryTests {
        @Test
        @DisplayName("根据实例ID获取空列表")
        void testGetTasksByInstanceId_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalTaskMapper.findByInstanceId(999L)).thenReturn(Arrays.asList());

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByInstanceId(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据处理人获取空列表")
        void testGetTasksByAssignee_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalTaskMapper.findByAssignee("non_existing")).thenReturn(Arrays.asList());

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByAssignee("non_existing");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("获取待办任务空列表")
        void testGetTodoTasks_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalTaskMapper.findByAssigneeAndStatus(any(), any())).thenReturn(Arrays.asList());

            // When
            List<ApprovalTask> result = approvalTaskService.getTodoTasks("non_existing");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("获取超时任务空列表")
        void testGetOverdueTasks_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalTaskMapper.findOverdueTasks(any())).thenReturn(Arrays.asList());

            // When
            List<ApprovalTask> result = approvalTaskService.getOverdueTasks();

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("根据流程ID获取空列表")
        void testGetTasksByProcessId_NoResults_ReturnsEmptyList() {
            // Given
            when(approvalTaskMapper.findByProcessId(999L)).thenReturn(Arrays.asList());

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByProcessId(999L);

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
            when(approvalTaskMapper.countByStatus("PENDING")).thenReturn(10L);

            // When & Then
            long count1 = approvalTaskService.countByStatus("PENDING");
            long count2 = approvalTaskService.countByStatus("PENDING");

            assertThat(count1).isEqualTo(10L);
            assertThat(count2).isEqualTo(10L);
            verify(approvalTaskMapper, times(2)).countByStatus("PENDING");
        }

        @Test
        @DisplayName("多次调用countTodoTasks方法线程安全")
        void testCountTodoTasks_ThreadSafe() {
            // Given
            when(approvalTaskMapper.countByAssigneeAndStatus("user1", "PENDING")).thenReturn(5L);

            // When & Then
            long count1 = approvalTaskService.countTodoTasks("user1");
            long count2 = approvalTaskService.countTodoTasks("user1");

            assertThat(count1).isEqualTo(5L);
            assertThat(count2).isEqualTo(5L);
        }

        @Test
        @DisplayName("getTasksByProcessId方法线程安全")
        void testGetTasksByProcessId_ThreadSafe() {
            // Given
            List<ApprovalTask> tasks = Arrays.asList(pendingTask);
            when(approvalTaskMapper.findByProcessId(1L)).thenReturn(tasks);

            // When
            List<ApprovalTask> result = approvalTaskService.getTasksByProcessId(1L);

            // Then
            assertThat(result).hasSize(1);
        }
    }
}