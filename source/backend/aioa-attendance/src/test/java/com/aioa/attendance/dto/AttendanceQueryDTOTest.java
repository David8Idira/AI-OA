package com.aioa.attendance.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AttendanceQueryDTO 测试
 */
@DisplayName("AttendanceQueryDTO 测试")
class AttendanceQueryDTOTest {

    @Nested
    @DisplayName("默认分页测试")
    class DefaultPaginationTests {

        @Test
        @DisplayName("默认分页参数")
        void defaultPagination() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            assertThat(dto.getPageNum()).isEqualTo(1);
            assertThat(dto.getPageSize()).isEqualTo(20);
        }

        @Test
        @DisplayName("自定义分页参数")
        void customPagination() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            dto.setPageNum(5);
            dto.setPageSize(50);
            assertThat(dto.getPageNum()).isEqualTo(5);
            assertThat(dto.getPageSize()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("用户筛选测试")
    class UserFilterTests {

        @Test
        @DisplayName("按用户ID筛选")
        void filterByUserId() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            dto.setUserId("user-001");
            assertThat(dto.getUserId()).isEqualTo("user-001");
        }

        @Test
        @DisplayName("按部门筛选")
        void filterByDeptId() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            dto.setDeptId("dept-001");
            assertThat(dto.getDeptId()).isEqualTo("dept-001");
        }
    }

    @Nested
    @DisplayName("日期范围筛选测试")
    class DateRangeFilterTests {

        @Test
        @DisplayName("设置日期范围")
        void setDateRange() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 31);

            dto.setStartDate(startDate);
            dto.setEndDate(endDate);

            assertThat(dto.getStartDate()).isEqualTo(startDate);
            assertThat(dto.getEndDate()).isEqualTo(endDate);
        }
    }

    @Nested
    @DisplayName("状态筛选测试")
    class StatusFilterTests {

        @Test
        @DisplayName("筛选正常记录")
        void filterNormal() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            dto.setStatus(0);
            assertThat(dto.getStatus()).isEqualTo(0);
        }

        @Test
        @DisplayName("筛选异常记录")
        void filterAbnormal() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            dto.setAbnormal(1);
            assertThat(dto.getAbnormal()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("排序测试")
    class OrderByTests {

        @Test
        @DisplayName("默认排序")
        void defaultOrderBy() {
            AttendanceQueryDTO dto = new AttendanceQueryDTO();
            assertThat(dto.getOrderBy()).isEqualTo("attendance_date desc, checkin_time desc");
        }
    }
}
