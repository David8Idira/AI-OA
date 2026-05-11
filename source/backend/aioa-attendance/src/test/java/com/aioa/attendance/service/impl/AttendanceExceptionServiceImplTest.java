package com.aioa.attendance.service.impl;

import com.aioa.attendance.entity.AttendanceException;
import com.aioa.attendance.mapper.AttendanceExceptionMapper;
import com.aioa.attendance.service.AttendanceService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AttendanceExceptionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceExceptionServiceImpl 单元测试")
class AttendanceExceptionServiceImplTest {

    @Mock
    private AttendanceExceptionMapper attendanceExceptionMapper;

    @Mock
    private AttendanceService attendanceService;

    private AttendanceExceptionServiceImpl attendanceExceptionService;

    @BeforeEach
    void setUp() {
        attendanceExceptionService = new AttendanceExceptionServiceImpl();
        ReflectionTestUtils.setField(attendanceExceptionService, "baseMapper", attendanceExceptionMapper);
        ReflectionTestUtils.setField(attendanceExceptionService, "attendanceService", attendanceService);
    }

    private AttendanceException createException(Long id, String userId, Integer type, Integer status) {
        AttendanceException exception = new AttendanceException();
        exception.setId(id);
        exception.setUserId(userId);
        exception.setType(type);
        exception.setStatus(status);
        exception.setReason("测试原因");
        exception.setStartTime(LocalDateTime.now().minusDays(1));
        exception.setEndTime(LocalDateTime.now());
        exception.setApplicationDate(LocalDate.now());
        return exception;
    }

    @Test
    @DisplayName("申请异常 - 结束时间早于开始时间应返回错误")
    void applyException_withEndTimeBeforeStartTime_shouldReturnError() {
        // given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.minusHours(1);

        // when
        Result<?> result = attendanceExceptionService.applyException(
                "user001", 0, startTime, endTime, "测试原因", null);

        // then
        assertThat(result.getCode()).isNotEqualTo(200);
        assertThat(result.getMessage()).contains("End time");
    }

    @Test
    @DisplayName("申请异常 - 时长超过7天应返回错误")
    void applyException_withDurationExceed7Days_shouldReturnError() {
        // given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(8);

        // when
        Result<?> result = attendanceExceptionService.applyException(
                "user001", 0, startTime, endTime, "测试原因", null);

        // then
        assertThat(result.getCode()).isNotEqualTo(200);
        assertThat(result.getMessage()).contains("7 days");
    }

    @Test
    @DisplayName("申请异常 - 正常申请应成功")
    void applyException_withValidData_shouldSucceed() {
        // given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(2);
        when(attendanceExceptionMapper.insert(any())).thenReturn(1);

        // when
        Result<?> result = attendanceExceptionService.applyException(
                "user001", 0, startTime, endTime, "测试原因", null);

        // then
        assertThat(result.getCode()).isEqualTo(200);
        verify(attendanceExceptionMapper).insert(any(AttendanceException.class));
    }

    @Test
    @DisplayName("审批异常 - 异常不存在应返回错误")
    void approveException_withNotFoundException_shouldReturnError() {
        // given
        when(attendanceExceptionMapper.selectById(999L)).thenReturn(null);

        // when
        Result<?> result = attendanceExceptionService.approveException(999L, "approver001", true, "同意");

        // then
        assertThat(result.getCode()).isNotEqualTo(200);
        assertThat(result.getMessage()).contains("not found");
    }

    @Test
    @DisplayName("审批异常 - 已处理过的异常应返回错误")
    void approveException_withAlreadyProcessedException_shouldReturnError() {
        // given
        AttendanceException exception = createException(1L, "user001", 0, 1); // status=1 已审批
        when(attendanceExceptionMapper.selectById(1L)).thenReturn(exception);

        // when
        Result<?> result = attendanceExceptionService.approveException(1L, "approver001", true, "同意");

        // then
        assertThat(result.getCode()).isNotEqualTo(200);
        assertThat(result.getMessage()).contains("already processed");
    }

    @Test
    @DisplayName("审批异常 - 正常审批应成功")
    void approveException_withValidData_shouldSucceed() {
        // given
        AttendanceException exception = createException(1L, "user001", 0, 0); // status=0 待审批
        when(attendanceExceptionMapper.selectById(1L)).thenReturn(exception);
        when(attendanceExceptionMapper.updateById(any())).thenReturn(1);

        // when
        Result<?> result = attendanceExceptionService.approveException(1L, "approver001", true, "同意");

        // then
        assertThat(result.getCode()).isEqualTo(200);
        verify(attendanceExceptionMapper).updateById(any(AttendanceException.class));
    }

    @Test
    @DisplayName("审批异常 - 拒绝时应成功")
    void approveException_withReject_shouldSucceed() {
        // given
        AttendanceException exception = createException(1L, "user001", 0, 0);
        when(attendanceExceptionMapper.selectById(1L)).thenReturn(exception);
        when(attendanceExceptionMapper.updateById(any())).thenReturn(1);

        // when
        Result<?> result = attendanceExceptionService.approveException(1L, "approver001", false, "拒绝原因");

        // then
        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("获取用户异常记录")
    void getUserExceptions_shouldReturnFilteredResults() {
        // given
        AttendanceException exception = createException(1L, "user001", 0, 1);
        when(attendanceExceptionMapper.selectList(any())).thenReturn(Arrays.asList(exception));

        // when
        List<AttendanceException> result = attendanceExceptionService.getUserExceptions(
                "user001", LocalDate.now().minusDays(7), LocalDate.now());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo("user001");
    }

    @Test
    @DisplayName("根据状态获取异常分页列表")
    void getExceptionsByStatus_shouldReturnPagedResults() {
        // given
        when(attendanceExceptionMapper.selectPage(any(), any())).thenReturn(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

        // when
        PageResult<AttendanceException> result = attendanceExceptionService.getExceptionsByStatus(0, 1, 10);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取异常统计信息")
    void getExceptionStatistics_shouldReturnCorrectStats() {
        // given
        AttendanceException exception1 = createException(1L, "user001", 0, 0);
        AttendanceException exception2 = createException(2L, "user001", 1, 1);
        AttendanceException exception3 = createException(3L, "user001", 0, 2);
        when(attendanceExceptionMapper.selectList(any())).thenReturn(
                Arrays.asList(exception1, exception2, exception3));

        // when
        Map<String, Object> stats = attendanceExceptionService.getExceptionStatistics(
                "user001", LocalDate.now().minusDays(7), LocalDate.now());

        // then
        assertThat(stats.get("totalExceptions")).isEqualTo(3);
        assertThat(stats.get("pendingExceptions")).isEqualTo(1);
        assertThat(stats.get("approvedExceptions")).isEqualTo(1);
        assertThat(stats.get("rejectedExceptions")).isEqualTo(1);
    }

    @Test
    @DisplayName("自动拒绝超期异常")
    void autoRejectOverdueExceptions_shouldRejectOldPendingExceptions() {
        // given
        AttendanceException exception = createException(1L, "user001", 0, 0);
        when(attendanceExceptionMapper.selectList(any())).thenReturn(Arrays.asList(exception));
        when(attendanceExceptionMapper.updateById(any())).thenReturn(1);

        // when
        attendanceExceptionService.autoRejectOverdueExceptions();

        // then
        verify(attendanceExceptionMapper).updateById(argThat(e -> 
                e.getStatus() == 2 && e.getApprovalComment().contains("Auto rejected")));
    }
}
