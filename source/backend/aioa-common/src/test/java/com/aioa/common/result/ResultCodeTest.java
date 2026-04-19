package com.aioa.common.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ResultCode 单元测试
 * 毛泽东思想指导：实事求是，测试结果码
 */
@DisplayName("ResultCodeTest 结果码测试")
class ResultCodeTest {

    // ==================== 成功码测试 ====================
    @Test
    @DisplayName("SUCCESS码应为200")
    void success_code_shouldBe200() {
        assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("SUCCESS消息应为操作成功")
    void success_message_shouldBeSuccess() {
        assertThat(ResultCode.SUCCESS.getMessage()).isEqualTo("操作成功");
    }

    // ==================== 4xx客户端错误测试 ====================
    @Test
    @DisplayName("BAD_REQUEST码应为400")
    void badRequest_code_shouldBe400() {
        assertThat(ResultCode.BAD_REQUEST.getCode()).isEqualTo(400);
        assertThat(ResultCode.BAD_REQUEST.getMessage()).isEqualTo("请求参数错误");
    }

    @Test
    @DisplayName("UNAUTHORIZED码应为401")
    void unauthorized_code_shouldBe401() {
        assertThat(ResultCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ResultCode.UNAUTHORIZED.getMessage()).isEqualTo("未授权，请登录");
    }

    @Test
    @DisplayName("FORBIDDEN码应为403")
    void forbidden_code_shouldBe403() {
        assertThat(ResultCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ResultCode.FORBIDDEN.getMessage()).isEqualTo("无权限访问");
    }

    @Test
    @DisplayName("NOT_FOUND码应为404")
    void notFound_code_shouldBe404() {
        assertThat(ResultCode.NOT_FOUND.getCode()).isEqualTo(404);
        assertThat(ResultCode.NOT_FOUND.getMessage()).isEqualTo("资源不存在");
    }

    @Test
    @DisplayName("METHOD_NOT_ALLOWED码应为405")
    void methodNotAllowed_code_shouldBe405() {
        assertThat(ResultCode.METHOD_NOT_ALLOWED.getCode()).isEqualTo(405);
        assertThat(ResultCode.METHOD_NOT_ALLOWED.getMessage()).isEqualTo("请求方法不支持");
    }

    // ==================== 5xx服务器错误测试 ====================
    @Test
    @DisplayName("INTERNAL_SERVER_ERROR码应为500")
    void internalServerError_code_shouldBe500() {
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getCode()).isEqualTo(500);
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getMessage()).isEqualTo("服务器内部错误");
    }

    @Test
    @DisplayName("SERVICE_UNAVAILABLE码应为503")
    void serviceUnavailable_code_shouldBe503() {
        assertThat(ResultCode.SERVICE_UNAVAILABLE.getCode()).isEqualTo(503);
        assertThat(ResultCode.SERVICE_UNAVAILABLE.getMessage()).isEqualTo("服务不可用");
    }

    // ==================== 业务错误码测试 ====================
    @Test
    @DisplayName("认证错误码")
    void auth_error_codes() {
        assertThat(ResultCode.USERNAME_PASSWORD_ERROR.getCode()).isEqualTo(10001);
        assertThat(ResultCode.TOKEN_EXPIRED.getCode()).isEqualTo(10002);
        assertThat(ResultCode.TOKEN_INVALID.getCode()).isEqualTo(10003);
        assertThat(ResultCode.ACCOUNT_DISABLED.getCode()).isEqualTo(10004);
    }

    @Test
    @DisplayName("用户错误码")
    void user_error_codes() {
        assertThat(ResultCode.USER_NOT_FOUND.getCode()).isEqualTo(20001);
        assertThat(ResultCode.USER_ALREADY_EXISTS.getCode()).isEqualTo(20002);
    }

    @Test
    @DisplayName("审批错误码")
    void approval_error_codes() {
        assertThat(ResultCode.APPROVAL_NOT_FOUND.getCode()).isEqualTo(30001);
        assertThat(ResultCode.APPROVAL_PERMISSION_DENIED.getCode()).isEqualTo(30002);
        assertThat(ResultCode.APPROVAL_STATUS_ERROR.getCode()).isEqualTo(30003);
    }

    @Test
    @DisplayName("AI服务错误码")
    void ai_error_codes() {
        assertThat(ResultCode.AI_SERVICE_UNAVAILABLE.getCode()).isEqualTo(40001);
        assertThat(ResultCode.AI_MODEL_QUOTA_EXCEEDED.getCode()).isEqualTo(40002);
        assertThat(ResultCode.AI_REQUEST_TIMEOUT.getCode()).isEqualTo(40003);
    }

    @Test
    @DisplayName("文件错误码")
    void file_error_codes() {
        assertThat(ResultCode.FILE_UPLOAD_ERROR.getCode()).isEqualTo(50001);
        assertThat(ResultCode.FILE_SIZE_EXCEEDED.getCode()).isEqualTo(50002);
        assertThat(ResultCode.FILE_TYPE_NOT_SUPPORTED.getCode()).isEqualTo(50003);
    }

    // ==================== 动态创建测试 ====================
    @Test
    @DisplayName("error静态方法创建自定义错误码")
    void error_static_method() {
        ResultCode customError = ResultCode.error(99999, "自定义错误");
        assertThat(customError.getCode()).isEqualTo(99999);
        assertThat(customError.getMessage()).isEqualTo("自定义错误");
    }

    @Test
    @DisplayName("create静态方法创建自定义错误码")
    void create_static_method() {
        ResultCode created = ResultCode.create(88888, "动态创建");
        assertThat(created.getCode()).isEqualTo(88888);
        assertThat(created.getMessage()).isEqualTo("动态创建");
    }

    // ==================== 边界测试 ====================
    @Test
    @DisplayName("所有预定义码消息应不为空")
    void all_predefined_codes_shouldHaveMessages() {
        assertThat(ResultCode.SUCCESS.getMessage()).isNotEmpty();
        assertThat(ResultCode.BAD_REQUEST.getMessage()).isNotEmpty();
        assertThat(ResultCode.UNAUTHORIZED.getMessage()).isNotEmpty();
        assertThat(ResultCode.FORBIDDEN.getMessage()).isNotEmpty();
        assertThat(ResultCode.NOT_FOUND.getMessage()).isNotEmpty();
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("错误码范围验证")
    void error_code_ranges() {
        // 4xx 客户端错误
        assertThat(ResultCode.BAD_REQUEST.getCode()).isBetween(400, 499);
        // 5xx 服务器错误
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getCode()).isBetween(500, 599);
        // 1xxxx 认证错误
        assertThat(ResultCode.USERNAME_PASSWORD_ERROR.getCode()).isBetween(10000, 19999);
        // 2xxxx 用户错误
        assertThat(ResultCode.USER_NOT_FOUND.getCode()).isBetween(20000, 29999);
        // 3xxxx 审批错误
        assertThat(ResultCode.APPROVAL_NOT_FOUND.getCode()).isBetween(30000, 39999);
        // 4xxxx AI错误
        assertThat(ResultCode.AI_SERVICE_UNAVAILABLE.getCode()).isBetween(40000, 49999);
        // 5xxxx 文件错误
        assertThat(ResultCode.FILE_UPLOAD_ERROR.getCode()).isBetween(50000, 59999);
    }
}