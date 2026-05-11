package com.aioa.attendance.service.impl;

import com.aioa.attendance.dto.AttendanceRuleDTO;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.mapper.AttendanceRuleMapper;
import com.aioa.attendance.service.impl.AttendanceRuleServiceImpl;
import com.aioa.common.vo.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AttendanceRuleServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AttendanceRuleServiceImpl 单元测试")
class AttendanceRuleServiceImplTest {

    @Mock
    private AttendanceRuleMapper attendanceRuleMapper;

    private AttendanceRuleServiceImpl attendanceRuleService;

    @BeforeEach
    void setUp() {
        attendanceRuleService = new AttendanceRuleServiceImpl();
        ReflectionTestUtils.setField(attendanceRuleService, "baseMapper", attendanceRuleMapper);
    }

    private AttendanceRule createRule(Long id, String ruleCode, String ruleName, Integer status) {
        AttendanceRule rule = new AttendanceRule();
        rule.setId(id);
        rule.setRuleCode(ruleCode);
        rule.setRuleName(ruleName);
        rule.setStatus(status);
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setWorkEndTime(LocalTime.of(18, 0));
        return rule;
    }

    private AttendanceRuleDTO createRuleDTO(String ruleCode, String ruleName) {
        AttendanceRuleDTO dto = new AttendanceRuleDTO();
        dto.setRuleCode(ruleCode);
        dto.setRuleName(ruleName);
        dto.setStatus(1);
        dto.setWorkStartTime(LocalTime.of(9, 0));
        dto.setWorkEndTime(LocalTime.of(18, 0));
        dto.setAllowLateMinutes(5);
        dto.setAllowLeaveEarlyMinutes(5);
        return dto;
    }

    @Test
    @DisplayName("创建考勤规则 - 正常创建应成功")
    void createRule_withValidData_shouldSucceed() {
        // given
        AttendanceRuleDTO dto = createRuleDTO("RULE001", "测试规则");
        when(attendanceRuleMapper.selectList(any())).thenReturn(Arrays.asList());
        when(attendanceRuleMapper.insert(any())).thenReturn(1);

        // when
        AttendanceRule result = attendanceRuleService.createRule(dto);

        // then
        assertThat(result).isNotNull();
        verify(attendanceRuleMapper).insert(any(AttendanceRule.class));
    }

    @Test
    @DisplayName("更新考勤规则 - 规则不存在应抛出异常")
    void updateRule_withNotFoundRule_shouldThrowException() {
        // given
        AttendanceRuleDTO dto = createRuleDTO("RULE001", "测试规则");
        when(attendanceRuleMapper.selectById(999L)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> attendanceRuleService.updateRule(999L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("更新考勤规则 - 正常更新应成功")
    void updateRule_withValidData_shouldSucceed() {
        // given
        AttendanceRule existingRule = createRule(1L, "RULE001", "现有规则", 1);
        AttendanceRuleDTO dto = createRuleDTO("RULE001", "更新后规则");
        
        when(attendanceRuleMapper.selectById(1L)).thenReturn(existingRule);
        when(attendanceRuleMapper.selectList(any())).thenReturn(Arrays.asList());
        when(attendanceRuleMapper.updateById(any())).thenReturn(1);

        // when
        AttendanceRule result = attendanceRuleService.updateRule(1L, dto);

        // then
        assertThat(result).isNotNull();
        verify(attendanceRuleMapper).updateById(any(AttendanceRule.class));
    }

    @Test
    @DisplayName("删除考勤规则 - 规则不存在应返回false")
    void deleteRule_withNotFoundRule_shouldReturnFalse() {
        // given
        when(attendanceRuleMapper.selectById(999L)).thenReturn(null);

        // when
        boolean result = attendanceRuleService.deleteRule(999L);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("根据ID获取考勤规则")
    void getRuleById_shouldReturnRule() {
        // given
        AttendanceRule rule = createRule(1L, "RULE001", "测试规则", 1);
        when(attendanceRuleMapper.selectById(1L)).thenReturn(rule);

        // when
        AttendanceRule result = attendanceRuleService.getRuleById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRuleCode()).isEqualTo("RULE001");
    }

    @Test
    @DisplayName("分页查询考勤规则 - 无查询条件")
    void listRules_withoutCondition_shouldReturnAllRules() {
        // given
        when(attendanceRuleMapper.selectPage(any(), any())).thenReturn(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

        // when
        PageResult<AttendanceRule> result = attendanceRuleService.listRules(1, 10, null, null);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("分页查询考勤规则 - 带关键字查询")
    void listRules_withKeyword_shouldFilterRules() {
        // given
        when(attendanceRuleMapper.selectPage(any(), any())).thenReturn(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 10));

        // when
        PageResult<AttendanceRule> result = attendanceRuleService.listRules(1, 10, "测试", 1);

        // then
        assertThat(result).isNotNull();
        verify(attendanceRuleMapper).selectPage(any(), any());
    }

    @Test
    @DisplayName("获取适用规则")
    void getApplicableRules_shouldReturnActiveRules() {
        // given
        when(attendanceRuleMapper.selectList(any())).thenReturn(Arrays.asList(createRule(1L, "RULE001", "规则1", 1)));

        // when
        List<AttendanceRule> result = attendanceRuleService.getApplicableRules("user001", "dept001", "pos001");

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("切换规则状态")
    void toggleRuleStatus_shouldUpdateStatus() {
        // given
        AttendanceRule rule = createRule(1L, "RULE001", "测试规则", 1);
        when(attendanceRuleMapper.selectById(1L)).thenReturn(rule);
        when(attendanceRuleMapper.updateById(any())).thenReturn(1);

        // when
        boolean result = attendanceRuleService.toggleRuleStatus(1L, 0);

        // then
        assertThat(result).isTrue();
        verify(attendanceRuleMapper).updateById(argThat(r -> r.getStatus() == 0));
    }

    @Test
    @DisplayName("切换规则状态 - 规则不存在")
    void toggleRuleStatus_withNotFoundRule_shouldReturnFalse() {
        // given
        when(attendanceRuleMapper.selectById(999L)).thenReturn(null);

        // when
        boolean result = attendanceRuleService.toggleRuleStatus(999L, 0);

        // then
        assertThat(result).isFalse();
    }
}
