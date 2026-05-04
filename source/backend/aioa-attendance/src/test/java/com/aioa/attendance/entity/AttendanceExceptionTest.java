package com.aioa.attendance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttendanceException 实体测试
 */
@DisplayName("AttendanceException 实体测试")
class AttendanceExceptionTest {

    @Nested
    @DisplayName("申请类型测试")
    class TypeTests {

        @Test
        @DisplayName("补卡申请")
        void makeupCardType() {
            AttendanceException exception = new AttendanceException();
            exception.setType(0);
            assertThat(exception.getType()).isEqualTo(0);
        }

        @Test
        @DisplayName("请假申请")
        void leaveType() {
            AttendanceException exception = new AttendanceException();
            exception.setType(1);
            assertThat(exception.getType()).isEqualTo(1);
        }

        @Test
        @DisplayName("出差申请")
        void businessTripType() {
            AttendanceException exception = new AttendanceException();
            exception.setType(2);
            assertThat(exception.getType()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("审批状态测试")
    class StatusTests {

        @Test
        @DisplayName("待审批状态")
        void pendingStatus() {
            AttendanceException exception = new AttendanceException();
            exception.setStatus(0);
            assertThat(exception.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("已批准状态")
        void approvedStatus() {
            AttendanceException exception = new AttendanceException();
            exception.setStatus(1);
            assertThat(exception.getStatus()).isEqualTo(1);
        }

        @Test
        @DisplayName("已拒绝状态")
        void rejectedStatus() {
            AttendanceException exception = new AttendanceException();
            exception.setStatus(2);
            assertThat(exception.getStatus()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("基本字段测试")
    class BasicFieldTests {

        @Test
        @DisplayName("创建请假申请并设置字段")
        void createLeaveApplication() {
            AttendanceException exception = new AttendanceException();
            exception.setId(1L);
            exception.setUserId("user-001");
            exception.setUserName("张三");
            exception.setType(1);
            exception.setApplicationDate(LocalDate.now());
            exception.setStartTime(LocalDateTime.now().plusDays(1));
            exception.setEndTime(LocalDateTime.now().plusDays(3));
            exception.setReason("个人事务");
            exception.setStatus(0);

            assertThat(exception.getId()).isEqualTo(1L);
            assertThat(exception.getUserId()).isEqualTo("user-001");
            assertThat(exception.getType()).isEqualTo(1);
            assertThat(exception.getReason()).isEqualTo("个人事务");
            assertThat(exception.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("创建出差申请并设置审批信息")
        void createBusinessTripWithApproval() {
            AttendanceException exception = new AttendanceException();
            exception.setId(2L);
            exception.setUserId("user-001");
            exception.setType(2);
            exception.setStatus(1);
            exception.setApproverId("manager-001");
            exception.setApproverName("李经理");
            exception.setApprovalTime(LocalDateTime.now());
            exception.setApprovalComment("Approved");

            assertThat(exception.getType()).isEqualTo(2);
            assertThat(exception.getStatus()).isEqualTo(1);
            assertThat(exception.getApproverId()).isEqualTo("manager-001");
        }
    }
}
