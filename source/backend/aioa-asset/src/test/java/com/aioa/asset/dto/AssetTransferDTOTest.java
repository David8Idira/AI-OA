package com.aioa.asset.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssetTransferDTO 资产调拨DTO测试
 */
@DisplayName("AssetTransferDTO 资产调拨DTO测试")
class AssetTransferDTOTest {

    @Test
    @DisplayName("创建DTO并设置属性应成功")
    void createDTO_andSetProperties_shouldWork() {
        AssetTransferDTO dto = new AssetTransferDTO();
        dto.setAssetId(1L);
        dto.setQuantity(10);
        dto.setOperator("张三");
        dto.setOperatorId("user001");
        dto.setTargetDepartment("市场部");
        dto.setReason("部门调整");

        assertThat(dto.getAssetId()).isEqualTo(1L);
        assertThat(dto.getQuantity()).isEqualTo(10);
        assertThat(dto.getOperator()).isEqualTo("张三");
        assertThat(dto.getOperatorId()).isEqualTo("user001");
        assertThat(dto.getTargetDepartment()).isEqualTo("市场部");
        assertThat(dto.getReason()).isEqualTo("部门调整");
    }

    @Test
    @DisplayName("toString应包含关键属性")
    void toString_shouldContainKeyProperties() {
        AssetTransferDTO dto = new AssetTransferDTO();
        dto.setAssetId(1L);
        dto.setOperator("张三");

        String str = dto.toString();
        assertThat(str).contains("assetId");
        assertThat(str).contains("张三");
    }
}