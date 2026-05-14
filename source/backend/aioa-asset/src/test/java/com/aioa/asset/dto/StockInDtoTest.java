package com.aioa.asset.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StockInDto 库存入库DTO测试
 */
@DisplayName("StockInDto 库存入库DTO测试")
class StockInDtoTest {

    @Test
    @DisplayName("创建DTO并设置属性应成功")
    void createDTO_andSetProperties_shouldWork() {
        StockInDto dto = new StockInDto();
        dto.setLabelCode("LABEL-001");
        dto.setAssetId(1L);
        dto.setQuantity(100);
        dto.setWarehouse("A仓");
        dto.setOperator("张三");
        dto.setOperatorId("user001");
        dto.setBatchNo("BATCH-001");
        dto.setPartner("供应商A");
        dto.setRelatedOrderNo("ORDER-001");

        assertThat(dto.getLabelCode()).isEqualTo("LABEL-001");
        assertThat(dto.getAssetId()).isEqualTo(1L);
        assertThat(dto.getQuantity()).isEqualTo(100);
        assertThat(dto.getWarehouse()).isEqualTo("A仓");
        assertThat(dto.getOperator()).isEqualTo("张三");
        assertThat(dto.getOperatorId()).isEqualTo("user001");
        assertThat(dto.getBatchNo()).isEqualTo("BATCH-001");
        assertThat(dto.getPartner()).isEqualTo("供应商A");
        assertThat(dto.getRelatedOrderNo()).isEqualTo("ORDER-001");
    }

    @Test
    @DisplayName("toString应包含关键属性")
    void toString_shouldContainKeyProperties() {
        StockInDto dto = new StockInDto();
        dto.setLabelCode("LABEL-001");
        dto.setWarehouse("A仓");

        String str = dto.toString();
        assertThat(str).contains("labelCode");
        assertThat(str).contains("A仓");
    }
}