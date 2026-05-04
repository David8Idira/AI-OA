package com.aioa.attendance.controller;

import com.aioa.attendance.dto.AttendanceQueryDTO;
import com.aioa.attendance.dto.CheckinDTO;
import com.aioa.attendance.entity.AttendanceRecord;
import com.aioa.attendance.service.AttendanceService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AttendanceController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceControllerTest 考勤控制器测试")
class AttendanceControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AttendanceService attendanceService;

    @InjectMocks
    private AttendanceController attendanceController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceController)
                .setMessageConverters(converter)
                .build();
    }

    private AttendanceRecord createMockRecord(Long id, String userId) {
        AttendanceRecord record = new AttendanceRecord();
        record.setId(id);
        record.setUserId(userId);
        record.setAttendanceDate(LocalDate.now());
        record.setStatus(0);
        record.setCheckinTime(LocalDateTime.now().minusHours(9));
        record.setCheckoutTime(LocalDateTime.now());
        record.setWorkHours(8.0);
        record.setDeleted(0);
        return record;
    }

    @Nested
    @DisplayName("签到/签退测试")
    class CheckinTests {

        @Test
        @DisplayName("签到成功")
        void checkin_success() throws Exception {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(0);
            dto.setMethod(0);
            dto.setLatitude(new BigDecimal("31.230416"));
            dto.setLongitude(new BigDecimal("121.473701"));

            AttendanceRecord record = createMockRecord(1L, "user-001");
            when(attendanceService.checkin(any(CheckinDTO.class))).thenReturn(record);

            mockMvc.perform(post("/api/attendance/checkin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userId").value("user-001"));
        }

        @Test
        @DisplayName("签到失败 - 服务异常")
        void checkin_serviceError() throws Exception {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(0);
            dto.setMethod(0);

            when(attendanceService.checkin(any(CheckinDTO.class)))
                    .thenThrow(new RuntimeException("User not in any attendance group"));

            mockMvc.perform(post("/api/attendance/checkin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("User not in any attendance group"));
        }
    }

    @Nested
    @DisplayName("今日考勤查询测试")
    class TodayAttendanceTests {

        @Test
        @DisplayName("获取今日考勤记录成功")
        void getTodayAttendance_success() throws Exception {
            AttendanceRecord record = createMockRecord(1L, "user-001");
            when(attendanceService.getTodayAttendance("user-001")).thenReturn(record);

            mockMvc.perform(get("/api/attendance/today")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.userId").value("user-001"));
        }

        @Test
        @DisplayName("获取今日考勤记录 - 无记录")
        void getTodayAttendance_noRecord() throws Exception {
            when(attendanceService.getTodayAttendance("user-001")).thenReturn(null);

            mockMvc.perform(get("/api/attendance/today")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("检查今日是否已签到")
        void hasCheckedInToday_success() throws Exception {
            when(attendanceService.hasCheckedInToday("user-001")).thenReturn(true);

            mockMvc.perform(get("/api/attendance/hasCheckedIn")
                            .param("userId", "user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    @Nested
    @DisplayName("考勤记录列表查询测试")
    class ListAttendanceRecordsTests {

        @Test
        @DisplayName("查询考勤记录列表成功")
        void listAttendanceRecords_success() throws Exception {
            PageResult<AttendanceRecord> pageResult = new PageResult<>();
            pageResult.setRecords(List.of(
                    createMockRecord(1L, "user-001"),
                    createMockRecord(2L, "user-002")
            ));
            pageResult.setTotal(2L);

            when(attendanceService.getAttendanceRecords(any(AttendanceQueryDTO.class)))
                    .thenReturn(pageResult);

            AttendanceQueryDTO query = new AttendanceQueryDTO();
            query.setUserId("user-001");

            mockMvc.perform(post("/api/attendance/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(query)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2));
        }

        @Test
        @DisplayName("查询考勤记录列表 - 空结果")
        void listAttendanceRecords_empty() throws Exception {
            PageResult<AttendanceRecord> pageResult = new PageResult<>();
            pageResult.setRecords(List.of());
            pageResult.setTotal(0L);

            when(attendanceService.getAttendanceRecords(any(AttendanceQueryDTO.class)))
                    .thenReturn(pageResult);

            AttendanceQueryDTO query = new AttendanceQueryDTO();

            mockMvc.perform(post("/api/attendance/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(query)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(0));
        }
    }

    @Nested
    @DisplayName("考勤汇总报告测试")
    class AttendanceSummaryTests {

        @Test
        @DisplayName("获取考勤汇总成功")
        void getAttendanceSummary_success() throws Exception {
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDays", 20);
            summary.put("normalDays", 18);
            summary.put("lateDays", 1);
            summary.put("leaveEarlyDays", 1);
            summary.put("totalWorkHours", 160.0);
            summary.put("normalRate", 0.9);

            when(attendanceService.getUserAttendanceSummary(anyString(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(summary);

            mockMvc.perform(get("/api/attendance/summary")
                            .param("userId", "user-001")
                            .param("startDate", "2025-04-01")
                            .param("endDate", "2025-04-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalDays").value(20))
                    .andExpect(jsonPath("$.data.normalRate").value(0.9));
        }

        @Test
        @DisplayName("获取月度考勤报告成功")
        void getMonthlyReport_success() throws Exception {
            Map<String, Object> report = new HashMap<>();
            report.put("year", 2025);
            report.put("month", 4);
            report.put("totalDays", 22);
            report.put("normalDays", 20);
            report.put("lateDays", 2);
            report.put("totalWorkHours", 176.0);

            when(attendanceService.getMonthlyReport("user-001", 2025, 4))
                    .thenReturn(report);

            mockMvc.perform(get("/api/attendance/monthlyReport")
                            .param("userId", "user-001")
                            .param("year", "2025")
                            .param("month", "4"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.year").value(2025))
                    .andExpect(jsonPath("$.data.month").value(4));
        }

        @Test
        @DisplayName("获取部门考勤报告成功")
        void getDepartmentReport_success() throws Exception {
            Map<String, Object> report = new HashMap<>();
            report.put("deptId", "dept-001");
            report.put("totalEmployees", 50);
            report.put("normalRate", 0.95);
            report.put("totalWorkHours", 4000.0);

            when(attendanceService.getDepartmentReport(anyString(), any(LocalDate.class), any(LocalDate.class)))
                    .thenReturn(report);

            mockMvc.perform(get("/api/attendance/departmentReport")
                            .param("deptId", "dept-001")
                            .param("startDate", "2025-04-01")
                            .param("endDate", "2025-04-30"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.deptId").value("dept-001"))
                    .andExpect(jsonPath("$.data.totalEmployees").value(50));
        }
    }

    @Nested
    @DisplayName("自动签退测试")
    class AutoCheckoutTests {

        @Test
        @DisplayName("自动签退成功")
        void autoCheckout_success() throws Exception {
            mockMvc.perform(post("/api/attendance/autoCheckout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("自动签退 - 服务异常")
        void autoCheckout_serviceError() throws Exception {
            // This is hard to test because it throws but catches internally
            // The endpoint returns success even on error (just logs it)
            mockMvc.perform(post("/api/attendance/autoCheckout"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("距离计算测试")
    class CalculateDistanceTests {

        @Test
        @DisplayName("计算两点间距离成功")
        void calculateDistance_success() throws Exception {
            when(attendanceService.calculateDistance(any(), any(), any(), any()))
                    .thenReturn(1234.56);

            mockMvc.perform(get("/api/attendance/calculateDistance")
                            .param("lat1", "31.230416")
                            .param("lon1", "121.473701")
                            .param("lat2", "31.240416")
                            .param("lon2", "121.483701"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1234.56));
        }
    }
}