package com.aioa.attendance.service.impl;

import com.aioa.attendance.dto.AttendanceQueryDTO;
import com.aioa.attendance.dto.CheckinDTO;
import com.aioa.attendance.entity.AttendanceGroup;
import com.aioa.attendance.entity.AttendanceRecord;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.mapper.AttendanceRecordMapper;
import com.aioa.attendance.service.AttendanceGroupService;
import com.aioa.attendance.service.AttendanceRuleService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AttendanceServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceServiceImpl 单元测试")
class AttendanceServiceImplTest {

    @Mock
    private AttendanceGroupService groupService;

    @Mock
    private AttendanceRuleService ruleService;

    @Mock
    private AttendanceRecordMapper attendanceRecordMapper;

    private AttendanceServiceImpl attendanceService;

    private AttendanceGroup createTestGroup(Long id, Long ruleId) {
        AttendanceGroup group = new AttendanceGroup();
        group.setId(id);
        group.setGroupName("测试考勤组");
        group.setGroupCode("TEST-GROUP");
        group.setStatus(1);
        group.setRuleId(ruleId);
        return group;
    }

    private AttendanceRule createTestRule(Long id) {
        AttendanceRule rule = new AttendanceRule();
        rule.setId(id);
        rule.setRuleName("标准考勤规则");
        rule.setRuleCode("STANDARD");
        rule.setStatus(1);
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setWorkEndTime(LocalTime.of(18, 0));
        rule.setAllowLateMinutes(5);
        rule.setAllowLeaveEarlyMinutes(5);
        rule.setOvertimeRule(1);
        rule.setMinOvertimeDuration(30);
        return rule;
    }

    private AttendanceRecord createTestRecord(String userId, LocalDate date) {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(1L);
        record.setUserId(userId);
        record.setAttendanceDate(date);
        record.setGroupId(1L);
        record.setRuleId(1L);
        record.setStatus(0);
        record.setDeleted(0);
        return record;
    }

    private AttendanceRecord createNormalRecord(String userId, LocalDate date, int status) {
        AttendanceRecord record = createTestRecord(userId, date);
        record.setStatus(status);
        record.setWorkHours(8.0);
        return record;
    }

    @BeforeEach
    void setUp() throws Exception {
        attendanceService = spy(new AttendanceServiceImpl());
        
        var baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(attendanceService, attendanceRecordMapper);
        
        var groupServiceField = AttendanceServiceImpl.class.getDeclaredField("groupService");
        groupServiceField.setAccessible(true);
        groupServiceField.set(attendanceService, groupService);
        
        var ruleServiceField = AttendanceServiceImpl.class.getDeclaredField("ruleService");
        ruleServiceField.setAccessible(true);
        ruleServiceField.set(attendanceService, ruleService);
    }

    @Nested
    @DisplayName("checkin 异常场景测试")
    class CheckinExceptionTests {

        @Test
        @DisplayName("签到失败 - 用户未分配考勤组")
        void checkin_userNoGroup_throwsException() {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-no-group");
            dto.setCheckinType(0);
            dto.setMethod(0);

            when(groupService.getUserAttendanceGroup("user-no-group")).thenReturn(null);

            assertThatThrownBy(() -> attendanceService.checkin(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not belong to any attendance group");
        }

        @Test
        @DisplayName("签到失败 - 考勤规则不存在")
        void checkin_ruleNotFound_throwsException() {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(0);
            dto.setMethod(0);

            AttendanceGroup group = createTestGroup(1L, 1L);
            when(groupService.getUserAttendanceGroup("user-001")).thenReturn(group);
            when(ruleService.getById(1L)).thenReturn(null);

            assertThatThrownBy(() -> attendanceService.checkin(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("rule not found");
        }
    }

    @Nested
    @DisplayName("calculateDistance 测试 - GPS距离计算")
    class CalculateDistanceTests {

        @Test
        @DisplayName("计算相同坐标距离 - 应返回0")
        void calculateDistance_sameCoordinates_returnsZero() {
            BigDecimal lat = new BigDecimal("31.230416");
            BigDecimal lon = new BigDecimal("121.473701");

            double distance = attendanceService.calculateDistance(lat, lon, lat, lon);

            assertThat(distance).isEqualTo(0.0);
        }

        @Test
        @DisplayName("计算上海到北京距离 - 应约为1068km")
        void calculateDistance_shanghaiToBeijing_approx1068km() {
            BigDecimal shanghaiLat = new BigDecimal("31.230416");
            BigDecimal shanghaiLon = new BigDecimal("121.473701");
            BigDecimal beijingLat = new BigDecimal("39.904202");
            BigDecimal beijingLon = new BigDecimal("116.407394");

            double distance = attendanceService.calculateDistance(
                    shanghaiLat, shanghaiLon, beijingLat, beijingLon);

            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(1200000);
        }

        @Test
        @DisplayName("计算近距离 - 100米范围内")
        void calculateDistance_nearbyPoints_smallDistance() {
            BigDecimal lat1 = new BigDecimal("31.230416");
            BigDecimal lon1 = new BigDecimal("121.473701");
            BigDecimal lat2 = new BigDecimal("31.231316");
            BigDecimal lon2 = new BigDecimal("121.474701");

            double distance = attendanceService.calculateDistance(lat1, lon1, lat2, lon2);

            assertThat(distance).isLessThan(500);
        }

        @Test
        @DisplayName("计算距离 - 含null参数")
        void calculateDistance_withNull_returnsMaxValue() {
            BigDecimal lat = new BigDecimal("31.230416");
            BigDecimal lon = new BigDecimal("121.473701");

            double distance1 = attendanceService.calculateDistance(null, lon, lat, lon);
            double distance2 = attendanceService.calculateDistance(lat, null, lat, lon);
            double distance3 = attendanceService.calculateDistance(lat, lon, null, lon);
            double distance4 = attendanceService.calculateDistance(lat, lon, lat, null);

            assertThat(distance1).isEqualTo(Double.MAX_VALUE);
            assertThat(distance2).isEqualTo(Double.MAX_VALUE);
            assertThat(distance3).isEqualTo(Double.MAX_VALUE);
            assertThat(distance4).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("isLocationInRange 测试")
    class IsLocationInRangeTests {

        @Test
        @DisplayName("位置在范围内 - 100米内")
        void isLocationInRange_withinRange() {
            BigDecimal centerLat = new BigDecimal("31.230416");
            BigDecimal centerLon = new BigDecimal("121.473701");
            BigDecimal userLat = new BigDecimal("31.230516");
            BigDecimal userLon = new BigDecimal("121.473801");

            boolean result = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, 100);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("位置超出范围 - 距离超过限制")
        void isLocationInRange_outsideRange() {
            BigDecimal centerLat = new BigDecimal("31.230416");
            BigDecimal centerLon = new BigDecimal("121.473701");
            BigDecimal userLat = new BigDecimal("31.240416");
            BigDecimal userLon = new BigDecimal("121.483701");

            boolean result = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, 100);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("无距离限制 - maxDistance为null或0")
        void isLocationInRange_noRestriction() {
            BigDecimal userLat = new BigDecimal("31.240416");
            BigDecimal userLon = new BigDecimal("121.483701");
            BigDecimal centerLat = new BigDecimal("31.230416");
            BigDecimal centerLon = new BigDecimal("121.473701");

            boolean result1 = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, null);
            boolean result2 = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, 0);

            assertThat(result1).isTrue();
            assertThat(result2).isTrue();
        }
    }

    @Nested
    @DisplayName("applyAttendanceException 测试")
    class ApplyAttendanceExceptionTests {

        @Test
        @DisplayName("申请考勤异常 - 请假")
        void applyAttendanceException_leave_success() {
            String userId = "user-001";
            Integer type = 1;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(2);
            String reason = "Personal matters";

            var result = attendanceService.applyAttendanceException(
                    userId, type, startTime, endTime, reason, null);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("申请考勤异常 - 出差")
        void applyAttendanceException_businessTrip_success() {
            String userId = "user-001";
            Integer type = 2;
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(3);
            String reason = "Client meeting";

            var result = attendanceService.applyAttendanceException(
                    userId, type, startTime, endTime, reason, null);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("approveAttendanceException 测试")
    class ApproveAttendanceExceptionTests {

        @Test
        @DisplayName("审批通过 - 同意异常申请")
        void approveAttendanceException_approved_success() {
            Long exceptionId = 1L;
            String approverId = "manager-001";
            boolean approved = true;
            String comment = "Approved";

            var result = attendanceService.approveAttendanceException(
                    exceptionId, approverId, approved, comment);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("审批拒绝 - 拒绝异常申请")
        void approveAttendanceException_rejected_success() {
            Long exceptionId = 1L;
            String approverId = "manager-001";
            boolean approved = false;
            String comment = "Rejected";

            var result = attendanceService.approveAttendanceException(
                    exceptionId, approverId, approved, comment);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("hasCheckedInToday 测试")
    class HasCheckedInTodayTests {

        @Test
        @DisplayName("今日已签到 - 返回true")
        void hasCheckedInToday_alreadyCheckedIn_returnsTrue() {
            String userId = "user-001";
            when(attendanceRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            boolean result = attendanceService.hasCheckedInToday(userId);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("今日未签到 - 返回false")
        void hasCheckedInToday_notCheckedIn_returnsFalse() {
            String userId = "user-001";
            when(attendanceRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            boolean result = attendanceService.hasCheckedInToday(userId);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getTodayAttendance 测试")
    class GetTodayAttendanceTests {

        @Test
        @DisplayName("获取今日考勤记录 - 无记录")
        void getTodayAttendance_noRecord_returnsNull() {
            String userId = "user-001";
            when(attendanceRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            AttendanceRecord result = attendanceService.getTodayAttendance(userId);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("getUserAttendanceSummary 测试")
    class GetUserAttendanceSummaryTests {

        @Test
        @DisplayName("汇总考勤统计 - 正常")
        void getUserAttendanceSummary_normalRecords() {
            String userId = "user-001";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            List<AttendanceRecord> records = List.of(
                    createNormalRecord(userId, LocalDate.now().minusDays(1), 0),
                    createNormalRecord(userId, LocalDate.now().minusDays(2), 1),
                    createNormalRecord(userId, LocalDate.now().minusDays(3), 2),
                    createNormalRecord(userId, LocalDate.now().minusDays(4), 0),
                    createNormalRecord(userId, LocalDate.now().minusDays(5), 0)
            );

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

            Map<String, Object> summary = attendanceService.getUserAttendanceSummary(userId, startDate, endDate);

            assertThat(summary).isNotNull();
            assertThat(summary.get("totalDays")).isEqualTo(5);
            assertThat(summary.get("normalDays")).isEqualTo(3);
            assertThat(summary.get("lateDays")).isEqualTo(1);
            assertThat(summary.get("leaveEarlyDays")).isEqualTo(1);
            assertThat(summary).containsKey("totalWorkHours");
            assertThat(summary).containsKey("normalRate");
        }

        @Test
        @DisplayName("汇总考勤统计 - 空记录")
        void getUserAttendanceSummary_noRecords() {
            String userId = "user-001";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            Map<String, Object> summary = attendanceService.getUserAttendanceSummary(userId, startDate, endDate);

            assertThat(summary).isNotNull();
            assertThat(summary.get("totalDays")).isEqualTo(0);
            assertThat(summary.get("normalRate")).isEqualTo(0.0);
            assertThat(summary.get("attendanceScore")).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("autoCheckoutForForgotten 测试")
    class AutoCheckoutTests {

        @Test
        @DisplayName("自动签退 - 有需要处理的记录")
        void autoCheckoutForForgotten_hasRecordsToProcess() {
            AttendanceRecord record = createTestRecord("user-001", LocalDate.now());
            record.setCheckinTime(LocalDateTime.now().minusHours(13));
            record.setCheckoutTime(null);

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(record));
            when(attendanceRecordMapper.updateById(any(AttendanceRecord.class))).thenReturn(1);

            attendanceService.autoCheckoutForForgotten();

            verify(attendanceRecordMapper).updateById(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("自动签退 - 无需处理的记录")
        void autoCheckoutForForgotten_noRecords() {
            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            attendanceService.autoCheckoutForForgotten();

            verify(attendanceRecordMapper, never()).updateById(any(AttendanceRecord.class));
        }
    }

    @Nested
    @DisplayName("getMonthlyReport 测试")
    class GetMonthlyReportTests {

        @Test
        @DisplayName("获取月度考勤报告 - 正常")
        void getMonthlyReport_normal() {
            String userId = "user-001";
            Integer year = 2026;
            Integer month = 5;

            List<AttendanceRecord> records = List.of(
                    createNormalRecord(userId, LocalDate.of(2026, 5, 1), 0),
                    createNormalRecord(userId, LocalDate.of(2026, 5, 2), 0),
                    createNormalRecord(userId, LocalDate.of(2026, 5, 3), 1)
            );

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

            Map<String, Object> report = attendanceService.getMonthlyReport(userId, year, month);

            assertThat(report).isNotNull();
            assertThat(report.get("year")).isEqualTo(2026);
            assertThat(report.get("month")).isEqualTo(5);
            assertThat(report.get("totalDays")).isEqualTo(3);
        }
    }
}
