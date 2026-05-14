package com.aioa.asset.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OfficeSupplyRequestVO 单元测试
 */
@DisplayName("OfficeSupplyRequestVO 办公用品申请单VO测试")
class OfficeSupplyRequestVOTest {

    @Test
    @DisplayName("创建VO并设置属性应成功")
    void createVO_andSetProperties_shouldWork() {
        OfficeSupplyRequestVO vo = new OfficeSupplyRequestVO();
        vo.setId(1L);
        vo.setRequestNo("REQ-20260514-001");
        vo.setApplicantId("user001");
        vo.setApplicantName("张三");
        vo.setDepartmentId("dept001");
        vo.setDepartmentName("技术部");
        vo.setRequestStatus(1);
        vo.setRequestStatusName("待审批");
        vo.setTotalAmount(BigDecimal.valueOf(100.00));
        vo.setCreateTime(LocalDateTime.now());

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getRequestNo()).isEqualTo("REQ-20260514-001");
        assertThat(vo.getApplicantId()).isEqualTo("user001");
        assertThat(vo.getApplicantName()).isEqualTo("张三");
        assertThat(vo.getDepartmentId()).isEqualTo("dept001");
        assertThat(vo.getDepartmentName()).isEqualTo("技术部");
        assertThat(vo.getRequestStatus()).isEqualTo(1);
        assertThat(vo.getRequestStatusName()).isEqualTo("待审批");
        assertThat(vo.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("equals和hashCode应正确工作")
    void equalsAndHashCode_shouldWorkCorrectly() {
        OfficeSupplyRequestVO vo1 = new OfficeSupplyRequestVO();
        vo1.setId(1L);
        vo1.setRequestNo("REQ-001");

        OfficeSupplyRequestVO vo2 = new OfficeSupplyRequestVO();
        vo2.setId(1L);
        vo2.setRequestNo("REQ-001");

        assertThat(vo1).isEqualTo(vo2);
        assertThat(vo1.hashCode()).isEqualTo(vo2.hashCode());
    }

    @Test
    @DisplayName("toString应包含关键属性")
    void toString_shouldContainKeyProperties() {
        OfficeSupplyRequestVO vo = new OfficeSupplyRequestVO();
        vo.setRequestNo("REQ-001");
        vo.setApplicantName("张三");

        String str = vo.toString();
        assertThat(str).contains("requestNo");
        assertThat(str).contains("REQ-001");
        assertThat(str).contains("张三");
    }
}