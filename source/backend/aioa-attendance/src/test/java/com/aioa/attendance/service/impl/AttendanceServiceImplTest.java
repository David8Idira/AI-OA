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
 * 毛选指导思想：实事求是，集中力量打歼灭战，覆盖正常流程、异常流程、边界值
 * 
 * 注意：以下方法因依赖MyBatis Plus内部TableInfo机制，无法在纯Mockito环境下测试：
 * - checkin() 调用 saveOrUpdate()
 * - getAttendanceRecords() 调用 page()
 * - getTodayAttendance() 调用 getOne()
 * 
 * 这些方法需要在集成测试环境（@SpringBootTest）中进行测试
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
        
        // Use reflection to set the baseMapper (MyBatis Plus)
        var baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(attendanceService, attendanceRecordMapper);
        
        // Use reflection to set the @Autowired dependencies
        var groupServiceField = AttendanceServiceImpl.class.getDeclaredField("groupService");
        groupServiceField.setAccessible(true);
        groupServiceField.set(attendanceService, groupService);
        
        var ruleServiceField = AttendanceServiceImpl.class.getDeclaredField("ruleService");
        ruleServiceField.setAccessible(true);
        ruleServiceField.set(attendanceService, ruleService);
    }

    // ========================================================================
    // checkin 异常场景测试 - 签到失败场景可以在纯Mockito下测试
    // ========================================================================

    @Nested
    @DisplayName("checkin 异常场景测试")
    class CheckinExceptionTests {

        @Test
        @DisplayName("签到失败 - 用户未分配考勤组")
        void checkin_userNoGroup_throwsException() {
            // given
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-no-group");
            dto.setCheckinType(0);
            dto.setMethod(0);

            when(groupService.getUserAttendanceGroup("user-no-group")).thenReturn(null);

            // when/then
            assertThatThrownBy(() -> attendanceService.checkin(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not belong to any attendance group");
        }

        @Test
        @DisplayName("签到失败 - 考勤规则不存在")
        void checkin_ruleNotFound_throwsException() {
            // given
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(0);
            dto.setMethod(0);

            AttendanceGroup group = createTestGroup(1L, 1L);
            when(groupService.getUserAttendanceGroup("user-001")).thenReturn(group);
            when(ruleService.getById(1L)).thenReturn(null);

            // when/then
            assertThatThrownBy(() -> attendanceService.checkin(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("rule not found");
        }
    }

    // ========================================================================
    // 工具方法测试 - 这些方法不依赖MyBatis Plus数据库操作
    // ========================================================================

    @Nested
    @DisplayName("calculateDistance 测试 - GPS距离计算")
    class CalculateDistanceTests {

        @Test
        @DisplayName("计算相同坐标距离 - 应返回0")
        void calculateDistance_sameCoordinates_returnsZero() {
            // given
            BigDecimal lat = new BigDecimal("31.230416");
            BigDecimal lon = new BigDecimal("121.473701");

            // when
            double distance = attendanceService.calculateDistance(lat, lon, lat, lon);

            // then
            assertThat(distance).isEqualTo(0.0);
        }

        @Test
        @DisplayName("计算上海到北京距离 - 应约为1068km")
        void calculateDistance_shanghaiToBeijing_approx1068km() {
            // given
            BigDecimal shanghaiLat = new BigDecimal("31.230416");
            BigDecimal shanghaiLon = new BigDecimal("121.473701");
            BigDecimal beijingLat = new BigDecimal("39.904202");
            BigDecimal beijingLon = new BigDecimal("116.407394");

            // when
            double distance = attendanceService.calculateDistance(
                    shanghaiLat, shanghaiLon, beijingLat, beijingLon);

            // then
            // Distance should be approximately 1068km (1,068,000 meters)
            assertThat(distance).isGreaterThan(1000000);
            assertThat(distance).isLessThan(1200000);
        }

        @Test
        @DisplayName("计算近距离 - 100米范围内")
        void calculateDistance_nearbyPoints_smallDistance() {
            // given - 两个非常接近的点，相距约100米
            BigDecimal lat1 = new BigDecimal("31.230416");
            BigDecimal lon1 = new BigDecimal("121.473701");
            BigDecimal lat2 = new BigDecimal("31.231316"); // 约100米
            BigDecimal lon2 = new BigDecimal("121.474701");

            // when
            double distance = attendanceService.calculateDistance(lat1, lon1, lat2, lon2);

            // then
            assertThat(distance).isLessThan(500); // Should be less than 500m
        }

        @Test
        @DisplayName("计算距离 - 含null参数")
        void calculateDistance_withNull_returnsMaxValue() {
            // given
            BigDecimal lat = new BigDecimal("31.230416");
            BigDecimal lon = new BigDecimal("121.473701");

            // when
            double distance1 = attendanceService.calculateDistance(null, lon, lat, lon);
            double distance2 = attendanceService.calculateDistance(lat, null, lat, lon);
            double distance3 = attendanceService.calculateDistance(lat, lon, null, lon);
            double distance4 = attendanceService.calculateDistance(lat, lon, lat, null);

            // then
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
            // given - 两个非常接近的点
            BigDecimal centerLat = new BigDecimal("31.230416");
            BigDecimal centerLon = new BigDecimal("121.473701");
            BigDecimal userLat = new BigDecimal("31.230516"); // 约10米差距
            BigDecimal userLon = new BigDecimal("121.473801");

            // when
            boolean result = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, 100);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("位置超出范围 - 距离超过限制")
        void isLocationInRange_outsideRange() {
            // given
            BigDecimal centerLat = new BigDecimal("31.230416");
            BigDecimal centerLon = new BigDecimal("121.473701");
            BigDecimal userLat = new BigDecimal("31.240416"); // 约1km差距
            BigDecimal userLon = new BigDecimal("121.483701");

            // when
            boolean result = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, 100);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("无距离限制 - maxDistance为null或0")
        void isLocationInRange_noRestriction() {
            // given
            BigDecimal userLat = new BigDecimal("31.240416");
            BigDecimal userLon = new BigDecimal("121.483701");
            BigDecimal centerLat = new BigDecimal("31.230416");
            BigDecimal centerLon = new BigDecimal("121.473701");

            // when
            boolean result1 = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, null);
            boolean result2 = attendanceService.isLocationInRange(userLat, userLon, centerLat, centerLon, 0);

            // then
            assertThat(result1).isTrue(); // null means no restriction
            assertThat(result2).isTrue(); // 0 also means no restriction
        }
    }

    @Nested
    @DisplayName("applyAttendanceException 测试")
    class ApplyAttendanceExceptionTests {

        @Test
        @DisplayName("申请考勤异常 - 请假")
        void applyAttendanceException_leave_success() {
            // given
            String userId = "user-001";
            Integer type = 1; // Leave
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(2);
            String reason = "Personal matters";

            // when
            var result = attendanceService.applyAttendanceException(
                    userId, type, startTime, endTime, reason, null);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("申请考勤异常 - 出差")
        void applyAttendanceException_businessTrip_success() {
            // given
            String userId = "user-001";
            Integer type = 2; // Business trip
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = LocalDateTime.now().plusDays(3);
            String reason = "Client meeting";

            // when
            var result = attendanceService.applyAttendanceException(
                    userId, type, startTime, endTime, reason, null);

            // then
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
            // given
            Long exceptionId = 1L;
            String approverId = "manager-001";
            boolean approved = true;
            String comment = "Approved";

            // when
            var result = attendanceService.approveAttendanceException(
                    exceptionId, approverId, approved, comment);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }

        @Test
        @DisplayName("审批拒绝 - 拒绝异常申请")
        void approveAttendanceException_rejected_success() {
            // given
            Long exceptionId = 1L;
            String approverId = "manager-001";
            boolean approved = false;
            String comment = "Rejected";

            // when
            var result = attendanceService.approveAttendanceException(
                    exceptionId, approverId, approved, comment);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo(200);
        }
    }

    // ========================================================================
    // 以下测试需要hasCheckedInToday和getTodayAttendance的基础方法调用
    // ========================================================================

    @Nested
    @DisplayName("hasCheckedInToday 测试")
    class HasCheckedInTodayTests {

        @Test
        @DisplayName("今日已签到 - 返回true")
        void hasCheckedInToday_alreadyCheckedIn_returnsTrue() {
            // given
            String userId = "user-001";
            when(attendanceRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            // when
            boolean result = attendanceService.hasCheckedInToday(userId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("今日未签到 - 返回false")
        void hasCheckedInToday_notCheckedIn_returnsFalse() {
            // given
            String userId = "user-001";
            when(attendanceRecordMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            // when
            boolean result = attendanceService.hasCheckedInToday(userId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getTodayAttendance 测试")
    class GetTodayAttendanceTests {

        @Test
        @DisplayName("获取今日考勤记录 - 无记录")
        void getTodayAttendance_noRecord_returnsNull() {
            // given
            String userId = "user-001";
            when(attendanceRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            // when
            AttendanceRecord result = attendanceService.getTodayAttendance(userId);

            // then
            assertThat(result).isNull();
        }
    }

    // ========================================================================
    // getUserAttendanceSummary 测试 - 调用 list() 方法
    // ========================================================================

    @Nested
    @DisplayName("getUserAttendanceSummary 测试")
    class GetUserAttendanceSummaryTests {

        @Test
        @DisplayName("汇总考勤统计 - 正常")
        void getUserAttendanceSummary_normalRecords() {
            // given
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

            // when
            Map<String, Object> summary = attendanceService.getUserAttendanceSummary(userId, startDate, endDate);

            // then
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
            // given
            String userId = "user-001";
            LocalDate startDate = LocalDate.now().minusDays(7);
            LocalDate endDate = LocalDate.now();

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

            // when
            Map<String, Object> summary = attendanceService.getUserAttendanceSummary(userId, startDate, endDate);

            // then
            assertThat(summary).isNotNull();
            assertThat(summary.get("totalDays")).isEqualTo(0);
            assertThat(summary.get("normalRate")).isEqualTo(0.0);
            assertThat(summary.get("attendanceScore")).isEqualTo(100.0);
        }
    }

    // ========================================================================
    // autoCheckoutForForgotten 测试
    // ========================================================================

    @Nested
    @DisplayName("autoCheckoutForForgotten 测试")
    class AutoCheckoutTests {

        @Test
        @DisplayName("自动签退 - 有需要处理的记录")
        void autoCheckoutForForgotten_hasRecordsToProcess() {
            // given
            AttendanceRecord record = createTestRecord("user-001", LocalDate.now());
            record.setCheckinTime(LocalDateTime.now().minusHours(13));
            record.setCheckoutTime(null);

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(record));
            when(attendanceRecordMapper.updateById(any(AttendanceRecord.class))).thenReturn(1);

            // when
            attendanceService.autoCheckoutForForgotten();

            // then
            verify(attendanceRecordMapper).updateById(any(AttendanceRecord.class));
        }

        @Test
        @DisplayName("自动签退 - 无需处理的记录")
        void autoCheckoutForForgotten_noRecords() {
            // given
            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(new ArrayList<>());

            // when
            attendanceService.autoCheckoutForForgotten();

            // then
            verify(attendanceRecordMapper, never()).updateById(any(AttendanceRecord.class));
        }
    }

    // ========================================================================
    // getMonthlyReport 测试
    // ========================================================================

    @Nested
    @DisplayName("getMonthlyReport 测试")
    class GetMonthlyReportTests {

        @Test
        @DisplayName("获取月度考勤报告 - 正常")
        void getMonthlyReport_normal() {
            // given
            String userId = "user-001";
            Integer year = 2026;
            Integer month = 5;

            List<AttendanceRecord> records = List.of(
                    createNormalRecord(userId, LocalDate.of(2026, 5, 1), 0),
                    createNormalRecord(userId, LocalDate.of(2026, 5, 2), 0),
                    createNormalRecord(userId, LocalDate.of(2026, 5, 3), 1)
            );

            when(attendanceRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

            // when
            Map<String, Object> report = attendanceService.getMonthlyReport(userId, year, month);

            // then
            assertThat(report).isNotNull();
            assertThat(report.get("year")).isEqualTo(2026);
            assertThat(report.get("month")).isEqualTo(5);
            assertThat(report.get("totalDays")).isEqualTo(3);
        }
    }
}