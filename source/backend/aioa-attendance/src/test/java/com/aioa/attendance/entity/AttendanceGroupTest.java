package com.aioa.attendance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttendanceGroup 实体测试
 */
@DisplayName("AttendanceGroup 实体测试")
class AttendanceGroupTest {

    @Nested
    @DisplayName("基本字段测试")
    class BasicFieldTests {

        @Test
        @DisplayName("创建考勤组并设置基本字段")
        void createAndSetFields() {
            AttendanceGroup group = new AttendanceGroup();
            group.setId(1L);
            group.setGroupName("研发部考勤组");
            group.setGroupCode("GRP-DEV-001");
            group.setStatus(1);
            group.setRuleId(1L);
            group.setScheduleType(0);
            group.setManagerId("manager-001");
            group.setManagerName("张经理");

            assertThat(group.getId()).isEqualTo(1L);
            assertThat(group.getGroupName()).isEqualTo("研发部考勤组");
            assertThat(group.getGroupCode()).isEqualTo("GRP-DEV-001");
            assertThat(group.getStatus()).isEqualTo(1);
            assertThat(group.getRuleId()).isEqualTo(1L);
            assertThat(group.getScheduleType()).isEqualTo(0);
            assertThat(group.getManagerId()).isEqualTo("manager-001");
        }

        @Test
        @DisplayName("设置排班类型")
        void setScheduleType() {
            AttendanceGroup group = new AttendanceGroup();
            group.setScheduleType(1);
            assertThat(group.getScheduleType()).isEqualTo(1);
        }

        @Test
        @DisplayName("设置弹性工作制")
        void setFlexibleSchedule() {
            AttendanceGroup group = new AttendanceGroup();
            group.setScheduleType(2);
            assertThat(group.getScheduleType()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("状态测试")
    class StatusTests {

        @Test
        @DisplayName("启用状态")
        void enabledStatus() {
            AttendanceGroup group = new AttendanceGroup();
            group.setStatus(1);
            assertThat(group.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("禁用状态")
        void disabledStatus() {
            AttendanceGroup group = new AttendanceGroup();
            group.setStatus(0);
            assertThat(group.getStatus()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("考勤地点测试")
    class CheckinLocationTests {

        @Test
        @DisplayName("设置考勤地点")
        void setCheckinLocations() {
            AttendanceGroup group = new AttendanceGroup();
            String locations = "[{\"name\":\"Office A\",\"lat\":31.23,\"lon\":121.47,\"radius\":100}]";
            group.setCheckinLocations(locations);
            assertThat(group.getCheckinLocations()).isEqualTo(locations);
        }

        @Test
        @DisplayName("设置WiFi列表")
        void setWifiList() {
            AttendanceGroup group = new AttendanceGroup();
            String wifiList = "[{\"name\":\"Office-WiFi\",\"mac\":\"00:11:22:33:44:55\"}]";
            group.setWifiList(wifiList);
            assertThat(group.getWifiList()).isEqualTo(wifiList);
        }
    }

    @Nested
    @DisplayName("远程考勤测试")
    class RemoteCheckinTests {

        @Test
        @DisplayName("允许远程考勤")
        void allowRemoteCheckin() {
            AttendanceGroup group = new AttendanceGroup();
            group.setAllowRemote(1);
            group.setMaxRemoteDistance(500);

            assertThat(group.getAllowRemote()).isEqualTo(1);
            assertThat(group.getMaxRemoteDistance()).isEqualTo(500);
        }
    }
}
