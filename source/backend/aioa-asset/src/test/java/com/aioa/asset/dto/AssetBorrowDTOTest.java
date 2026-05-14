package com.aioa.asset.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AssetBorrowDTO 单元测试
 */
@DisplayName("AssetBorrowDTO 资产领用DTO测试")
class AssetBorrowDTOTest {

    @Test
    @DisplayName("创建DTO并设置属性应成功")
    void createDTO_andSetProperties_shouldWork() {
        AssetBorrowDTO dto = new AssetBorrowDTO();
        dto.setAssetId(1L);
        dto.setQuantity(5);
        dto.setOperator("张三");
        dto.setOperatorId("user001");
        dto.setReason("日常工作使用");

        assertThat(dto.getAssetId()).isEqualTo(1L);
        assertThat(dto.getQuantity()).isEqualTo(5);
        assertThat(dto.getOperator()).isEqualTo("张三");
        assertThat(dto.getOperatorId()).isEqualTo("user001");
        assertThat(dto.getReason()).isEqualTo("日常工作使用");
    }

    @Test
    @DisplayName("构造函数应正确初始化")
    void constructor_shouldInitializeCorrectly() {
        AssetBorrowDTO dto = new AssetBorrowDTO();
        
        assertThat(dto.getAssetId()).isNull();
        assertThat(dto.getQuantity()).isNull();
        assertThat(dto.getOperator()).isNull();
    }

    @Test
    @DisplayName("equals和hashCode应正确工作")
    void equalsAndHashCode_shouldWorkCorrectly() {
        AssetBorrowDTO dto1 = new AssetBorrowDTO();
        dto1.setAssetId(1L);
        dto1.setQuantity(5);

        AssetBorrowDTO dto2 = new AssetBorrowDTO();
        dto2.setAssetId(1L);
        dto2.setQuantity(5);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("toString应包含关键属性")
    void toString_shouldContainKeyProperties() {
        AssetBorrowDTO dto = new AssetBorrowDTO();
        dto.setAssetId(1L);
        dto.setOperator("张三");

        String str = dto.toString();
        assertThat(str).contains("assetId");
        assertThat(str).contains("张三");
    }
}