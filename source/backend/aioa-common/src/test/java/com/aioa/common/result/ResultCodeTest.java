package com.aioa.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ResultCode 单元测试
 * 毛泽东思想指导：实事求是，测试结果码枚举
 */
@DisplayName("ResultCode 枚举测试")
class ResultCodeTest {

    @Test
    @DisplayName("成功码应为200")
    void success_code_shouldBe200() {
        assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("成功消息应为操作成功")
    void success_message_shouldBeSuccess() {
        assertThat(ResultCode.SUCCESS.getMessage()).isEqualTo("操作成功");
    }

    @Test
    @DisplayName("错误码应有正确的值")
    void error_codes_shouldHaveCorrectValues() {
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getCode()).isEqualTo(500);
        assertThat(ResultCode.BAD_REQUEST.getCode()).isEqualTo(400);
        assertThat(ResultCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ResultCode.FORBIDDEN.getCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("预定义码消息应不为空")
    void predefined_codes_shouldHaveMessages() {
        // 测试几个预定义的错误码
        assertThat(ResultCode.SUCCESS.getMessage()).isNotEmpty();
        assertThat(ResultCode.BAD_REQUEST.getMessage()).isNotEmpty();
        assertThat(ResultCode.UNAUTHORIZED.getMessage()).isNotEmpty();
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("创建动态错误码")
    void createDynamicErrorCode() {
        ResultCode customError = ResultCode.error(99999, "自定义错误");
        assertThat(customError.getCode()).isEqualTo(99999);
        assertThat(customError.getMessage()).isEqualTo("自定义错误");
    }
}
