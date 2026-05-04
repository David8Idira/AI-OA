package com.aioa.attendance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttendanceRule 实体测试
 */
@DisplayName("AttendanceRule 实体测试")
class AttendanceRuleTest {

    @Nested
    @DisplayName("基本字段测试")
    class BasicFieldTests {

        @Test
        @DisplayName("创建考勤规则并设置基本字段")
        void createAndSetFields() {
            AttendanceRule rule = new AttendanceRule();
            rule.setId(1L);
            rule.setRuleCode("RULE-001");
            rule.setRuleName("标准考勤规则");
            rule.setStatus(1);
            rule.setWorkStartTime(LocalTime.of(9, 0));
            rule.setWorkEndTime(LocalTime.of(18, 0));
            rule.setAllowLateMinutes(5);
            rule.setAllowLeaveEarlyMinutes(5);

            assertThat(rule.getId()).isEqualTo(1L);
            assertThat(rule.getRuleCode()).isEqualTo("RULE-001");
            assertThat(rule.getRuleName()).isEqualTo("标准考勤规则");
            assertThat(rule.getStatus()).isEqualTo(1);
            assertThat(rule.getWorkStartTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(rule.getWorkEndTime()).isEqualTo(LocalTime.of(18, 0));
            assertThat(rule.getAllowLateMinutes()).isEqualTo(5);
            assertThat(rule.getAllowLeaveEarlyMinutes()).isEqualTo(5);
        }

        @Test
        @DisplayName("设置加班规则")
        void setOvertimeRule() {
            AttendanceRule rule = new AttendanceRule();
            rule.setOvertimeRule(1);
            rule.setMinOvertimeDuration(30);

            assertThat(rule.getOvertimeRule()).isEqualTo(1);
            assertThat(rule.getMinOvertimeDuration()).isEqualTo(30);
        }

        @Test
        @DisplayName("设置工作日配置")
        void setWeekdayConfig() {
            AttendanceRule rule = new AttendanceRule();
            rule.setWeekdays("[1,2,3,4,5]");
            assertThat(rule.getWeekdays()).isEqualTo("[1,2,3,4,5]");
        }
    }

    @Nested
    @DisplayName("状态测试")
    class StatusTests {

        @Test
        @DisplayName("启用状态")
        void enabledStatus() {
            AttendanceRule rule = new AttendanceRule();
            rule.setStatus(1);
            assertThat(rule.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("禁用状态")
        void disabledStatus() {
            AttendanceRule rule = new AttendanceRule();
            rule.setStatus(0);
            assertThat(rule.getStatus()).isEqualTo(0);
        }
    }
}
