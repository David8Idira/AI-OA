package com.aioa.asset.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OfficeSupplyStatusEnum 枚举测试
 */
@DisplayName("OfficeSupplyStatusEnum 办公用品状态枚举测试")
class OfficeSupplyStatusEnumTest {

    @Test
    @DisplayName("枚举常量的值应正确 - 申请单状态")
    void requestStatusEnumValues_shouldHaveCorrectValues() {
        assertThat(OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode()).isEqualTo(0);
        assertThat(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode()).isEqualTo(1);
        assertThat(OfficeSupplyStatusEnum.REQUEST_APPROVED.getCode()).isEqualTo(2);
        assertThat(OfficeSupplyStatusEnum.REQUEST_REJECTED.getCode()).isEqualTo(3);
        assertThat(OfficeSupplyStatusEnum.REQUEST_PARTIAL_CLAIMED.getCode()).isEqualTo(4);
        assertThat(OfficeSupplyStatusEnum.REQUEST_FULLY_CLAIMED.getCode()).isEqualTo(5);
        assertThat(OfficeSupplyStatusEnum.REQUEST_CANCELLED.getCode()).isEqualTo(6);
    }

    @Test
    @DisplayName("枚举常量的描述应正确 - 申请单状态")
    void requestStatusEnumValues_shouldHaveCorrectDescriptions() {
        assertThat(OfficeSupplyStatusEnum.REQUEST_DRAFT.getDescription()).isEqualTo("草稿");
        assertThat(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getDescription()).isEqualTo("待审批");
        assertThat(OfficeSupplyStatusEnum.REQUEST_APPROVED.getDescription()).isEqualTo("审批通过");
        assertThat(OfficeSupplyStatusEnum.REQUEST_REJECTED.getDescription()).isEqualTo("审批拒绝");
        assertThat(OfficeSupplyStatusEnum.REQUEST_PARTIAL_CLAIMED.getDescription()).isEqualTo("部分领取");
        assertThat(OfficeSupplyStatusEnum.REQUEST_FULLY_CLAIMED.getDescription()).isEqualTo("已全部领取");
        assertThat(OfficeSupplyStatusEnum.REQUEST_CANCELLED.getDescription()).isEqualTo("已取消");
    }

    @Test
    @DisplayName("枚举常量的值应正确 - 库存检查状态")
    void inventoryStatusEnumValues_shouldHaveCorrectValues() {
        assertThat(OfficeSupplyStatusEnum.INVENTORY_UNCHECKED.getCode()).isEqualTo(0);
        assertThat(OfficeSupplyStatusEnum.INVENTORY_SUFFICIENT.getCode()).isEqualTo(1);
        assertThat(OfficeSupplyStatusEnum.INVENTORY_INSUFFICIENT.getCode()).isEqualTo(2);
        assertThat(OfficeSupplyStatusEnum.INVENTORY_QUEUED.getCode()).isEqualTo(3);
    }

    @Test
    @DisplayName("枚举常量的值应正确 - 领用方式")
    void claimMethodEnumValues_shouldHaveCorrectValues() {
        assertThat(OfficeSupplyStatusEnum.CLAIM_METHOD_QR_CODE.getCode()).isEqualTo(1);
        assertThat(OfficeSupplyStatusEnum.CLAIM_METHOD_MANUAL.getCode()).isEqualTo(2);
    }

    @Test
    @DisplayName("枚举常量的值应正确 - 签收状态")
    void signStatusEnumValues_shouldHaveCorrectValues() {
        assertThat(OfficeSupplyStatusEnum.SIGN_PENDING.getCode()).isEqualTo(0);
        assertThat(OfficeSupplyStatusEnum.SIGN_COMPLETED.getCode()).isEqualTo(1);
        assertThat(OfficeSupplyStatusEnum.SIGN_CANCELLED.getCode()).isEqualTo(2);
    }

    @Test
    @DisplayName("枚举常量的值应正确 - 紧急程度")
    void urgencyEnumValues_shouldHaveCorrectValues() {
        assertThat(OfficeSupplyStatusEnum.URGENCY_NORMAL.getCode()).isEqualTo(1);
        assertThat(OfficeSupplyStatusEnum.URGENCY_URGENT.getCode()).isEqualTo(2);
        assertThat(OfficeSupplyStatusEnum.URGENCY_EMERGENCY.getCode()).isEqualTo(3);
    }

    @ParameterizedTest
    @EnumSource(OfficeSupplyStatusEnum.class)
    @DisplayName("每个枚举常量应该有有效的值和描述")
    void eachEnumConstant_shouldHaveValidCodeAndDescription(OfficeSupplyStatusEnum status) {
        assertThat(status.getCode()).isNotNull();
        assertThat(status.getDescription()).isNotBlank();
    }

    @Test
    @DisplayName("通过code获取枚举应正确")
    void getByCode_shouldReturnCorrectEnum() {
        assertThat(OfficeSupplyStatusEnum.getByCode(0)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_DRAFT);
        assertThat(OfficeSupplyStatusEnum.getByCode(1)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL);
        assertThat(OfficeSupplyStatusEnum.getByCode(2)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_APPROVED);
        assertThat(OfficeSupplyStatusEnum.getByCode(3)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_REJECTED);
        assertThat(OfficeSupplyStatusEnum.getByCode(4)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_PARTIAL_CLAIMED);
        assertThat(OfficeSupplyStatusEnum.getByCode(5)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_FULLY_CLAIMED);
        assertThat(OfficeSupplyStatusEnum.getByCode(6)).isEqualTo(OfficeSupplyStatusEnum.REQUEST_CANCELLED);
    }

    @Test
    @DisplayName("通过无效code获取枚举应返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(OfficeSupplyStatusEnum.getByCode(-1)).isNull();
        assertThat(OfficeSupplyStatusEnum.getByCode(99)).isNull();
        assertThat(OfficeSupplyStatusEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("通过code获取描述应正确")
    void getDescriptionByCode_shouldReturnCorrectDescription() {
        assertThat(OfficeSupplyStatusEnum.getDescriptionByCode(0)).isEqualTo("草稿");
        assertThat(OfficeSupplyStatusEnum.getDescriptionByCode(1)).isEqualTo("待审批");
        assertThat(OfficeSupplyStatusEnum.getDescriptionByCode(99)).isEqualTo("未知");
        assertThat(OfficeSupplyStatusEnum.getDescriptionByCode(null)).isEqualTo("未知");
    }

    @Test
    @DisplayName("isValidRequestStatus 应正确判断")
    void isValidRequestStatus_shouldReturnCorrectResult() {
        assertThat(OfficeSupplyStatusEnum.isValidRequestStatus(0)).isTrue();
        assertThat(OfficeSupplyStatusEnum.isValidRequestStatus(6)).isTrue();
        assertThat(OfficeSupplyStatusEnum.isValidRequestStatus(-1)).isFalse();
        assertThat(OfficeSupplyStatusEnum.isValidRequestStatus(7)).isFalse();
        assertThat(OfficeSupplyStatusEnum.isValidRequestStatus(null)).isFalse();
    }

    @Test
    @DisplayName("isApprovable 应正确判断")
    void isApprovable_shouldReturnCorrectResult() {
        assertThat(OfficeSupplyStatusEnum.isApprovable(1)).isTrue();
        assertThat(OfficeSupplyStatusEnum.isApprovable(0)).isFalse();
        assertThat(OfficeSupplyStatusEnum.isApprovable(2)).isFalse();
    }

    @Test
    @DisplayName("isClaimable 应正确判断")
    void isClaimable_shouldReturnCorrectResult() {
        assertThat(OfficeSupplyStatusEnum.isClaimable(2)).isTrue(); // APPROVED
        assertThat(OfficeSupplyStatusEnum.isClaimable(4)).isTrue(); // PARTIAL_CLAIMED
        assertThat(OfficeSupplyStatusEnum.isClaimable(0)).isFalse();
        assertThat(OfficeSupplyStatusEnum.isClaimable(3)).isFalse(); // REJECTED
    }

    @Test
    @DisplayName("isCancellable 应正确判断")
    void isCancellable_shouldReturnCorrectResult() {
        assertThat(OfficeSupplyStatusEnum.isCancellable(0)).isTrue(); // DRAFT
        assertThat(OfficeSupplyStatusEnum.isCancellable(1)).isTrue(); // PENDING_APPROVAL
        assertThat(OfficeSupplyStatusEnum.isCancellable(2)).isFalse();
        assertThat(OfficeSupplyStatusEnum.isCancellable(6)).isFalse(); // CANCELLED
    }
}