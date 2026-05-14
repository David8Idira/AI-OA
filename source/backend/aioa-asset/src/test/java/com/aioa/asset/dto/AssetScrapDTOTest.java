package com.aioa.asset.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssetScrapDTO 资产报废DTO测试
 */
@DisplayName("AssetScrapDTO 资产报废DTO测试")
class AssetScrapDTOTest {

    @Test
    @DisplayName("创建DTO并设置属性应成功")
    void createDTO_andSetProperties_shouldWork() {
        AssetScrapDTO dto = new AssetScrapDTO();
        dto.setAssetId(1L);
        dto.setQuantity(1);
        dto.setOperator("李四");
        dto.setOperatorId("user002");
        dto.setReason("设备老化报废");

        assertThat(dto.getAssetId()).isEqualTo(1L);
        assertThat(dto.getQuantity()).isEqualTo(1);
        assertThat(dto.getOperator()).isEqualTo("李四");
        assertThat(dto.getOperatorId()).isEqualTo("user002");
        assertThat(dto.getReason()).isEqualTo("设备老化报废");
    }

    @Test
    @DisplayName("toString应包含关键属性")
    void toString_shouldContainKeyProperties() {
        AssetScrapDTO dto = new AssetScrapDTO();
        dto.setAssetId(1L);
        dto.setReason("报废原因");

        String str = dto.toString();
        assertThat(str).contains("assetId");
        assertThat(str).contains("报废原因");
    }
}