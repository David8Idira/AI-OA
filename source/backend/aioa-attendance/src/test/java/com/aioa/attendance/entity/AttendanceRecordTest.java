package com.aioa.attendance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttendanceRecord 实体测试
 */
@DisplayName("AttendanceRecord 实体测试")
class AttendanceRecordTest {

    @Nested
    @DisplayName("基本字段测试")
    class BasicFieldTests {

        @Test
        @DisplayName("创建考勤记录并设置基本字段")
        void createAndSetFields() {
            AttendanceRecord record = new AttendanceRecord();
            record.setId(1L);
            record.setUserId("user-001");
            record.setUserName("张三");
            record.setAttendanceDate(LocalDate.of(2026, 5, 4));
            record.setGroupId(1L);
            record.setRuleId(1L);
            record.setStatus(0);

            assertThat(record.getId()).isEqualTo(1L);
            assertThat(record.getUserId()).isEqualTo("user-001");
            assertThat(record.getUserName()).isEqualTo("张三");
            assertThat(record.getAttendanceDate()).isEqualTo(LocalDate.of(2026, 5, 4));
            assertThat(record.getGroupId()).isEqualTo(1L);
            assertThat(record.getRuleId()).isEqualTo(1L);
            assertThat(record.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("设置打卡时间")
        void setCheckinAndCheckoutTimes() {
            AttendanceRecord record = new AttendanceRecord();
            LocalDateTime checkinTime = LocalDateTime.of(2026, 5, 4, 9, 0);
            LocalDateTime checkoutTime = LocalDateTime.of(2026, 5, 4, 18, 0);

            record.setCheckinTime(checkinTime);
            record.setCheckoutTime(checkoutTime);

            assertThat(record.getCheckinTime()).isEqualTo(checkinTime);
            assertThat(record.getCheckoutTime()).isEqualTo(checkoutTime);
        }

        @Test
        @DisplayName("设置位置信息")
        void setLocationInfo() {
            AttendanceRecord record = new AttendanceRecord();
            record.setCheckinLatitude(new BigDecimal("31.230416"));
            record.setCheckinLongitude(new BigDecimal("121.473701"));
            record.setCheckinAddress("上海市黄浦区");
            record.setCheckinMethod(0);

            assertThat(record.getCheckinLatitude()).isEqualTo(new BigDecimal("31.230416"));
            assertThat(record.getCheckinLongitude()).isEqualTo(new BigDecimal("121.473701"));
            assertThat(record.getCheckinAddress()).isEqualTo("上海市黄浦区");
            assertThat(record.getCheckinMethod()).isEqualTo(0);
        }

        @Test
        @DisplayName("设置迟到信息")
        void setLateInfo() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(1);
            record.setLateMinutes(15);
            record.setAbnormal(1);
            record.setAbnormalReason("Late: 15 minutes");

            assertThat(record.getStatus()).isEqualTo(1);
            assertThat(record.getLateMinutes()).isEqualTo(15);
            assertThat(record.getAbnormal()).isEqualTo(1);
        }

        @Test
        @DisplayName("设置早退信息")
        void setLeaveEarlyInfo() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(2);
            record.setLeaveEarlyMinutes(30);
            record.setAbnormal(1);

            assertThat(record.getStatus()).isEqualTo(2);
            assertThat(record.getLeaveEarlyMinutes()).isEqualTo(30);
        }

        @Test
        @DisplayName("设置加班信息")
        void setOvertimeInfo() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(4);
            record.setOvertimeMinutes(120);
            record.setWorkHours(10.0);

            assertThat(record.getStatus()).isEqualTo(4);
            assertThat(record.getOvertimeMinutes()).isEqualTo(120);
            assertThat(record.getWorkHours()).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("状态枚举测试")
    class StatusTests {

        @Test
        @DisplayName("正常状态")
        void normalStatus() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(0);
            assertThat(record.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("迟到状态")
        void lateStatus() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(1);
            assertThat(record.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("早退状态")
        void leaveEarlyStatus() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(2);
            assertThat(record.getStatus()).isEqualTo(2);
        }

        @Test
        @DisplayName("缺勤状态")
        void absentStatus() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(3);
            assertThat(record.getStatus()).isEqualTo(3);
        }

        @Test
        @DisplayName("加班状态")
        void overtimeStatus() {
            AttendanceRecord record = new AttendanceRecord();
            record.setStatus(4);
            assertThat(record.getStatus()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("equals和hashCode测试")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("不同ID的记录不应该equals")
        void differentIdShouldNotBeEqual() {
            AttendanceRecord record1 = new AttendanceRecord();
            record1.setId(1L);

            AttendanceRecord record2 = new AttendanceRecord();
            record2.setId(2L);

            assertThat(record1).isNotEqualTo(record2);
        }

        @Test
        @DisplayName("相同记录应该equals自身")
        void sameRecordEqualsItself() {
            AttendanceRecord record = new AttendanceRecord();
            record.setId(1L);
            record.setUserId("user-001");

            assertThat(record).isEqualTo(record);
        }
    }
}
