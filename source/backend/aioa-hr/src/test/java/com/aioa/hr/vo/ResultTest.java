package com.aioa.hr.vo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Result VO 单元测试
 * 覆盖统一返回结果的构造和状态码逻辑
 */
@DisplayName("Result VO Tests")
class ResultTest {

    @Test
    @DisplayName("success() 返回 code=200, message='操作成功', data=null")
    void success_noData() {
        Result<String> result = Result.success();
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("success(data) 返回正确data")
    void success_withData() {
        Result<String> result = Result.success("testData");
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("testData", result.getData());
    }

    @Test
    @DisplayName("success(data, message) 返回自定义消息")
    void success_withMessage() {
        Result<String> result = Result.success("data", "自定义消息");
        assertEquals(200, result.getCode());
        assertEquals("自定义消息", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    @DisplayName("error() 默认返回 code=500")
    void error_default() {
        Result<String> result = Result.error();
        assertEquals(500, result.getCode());
        assertEquals("操作失败", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    @DisplayName("error(message) 返回自定义错误消息")
    void error_withMessage() {
        Result<String> result = Result.error("自定义错误");
        assertEquals(500, result.getCode());
        assertEquals("自定义错误", result.getMessage());
    }

    @Test
    @DisplayName("error(code, message) 返回指定状态码")
    void error_withCodeAndMessage() {
        Result<String> result = Result.error(503, "服务不可用");
        assertEquals(503, result.getCode());
        assertEquals("服务不可用", result.getMessage());
    }

    @Test
    @DisplayName("error(code, message, data) 返回带数据的错误")
    void error_withData() {
        Result<String> result = Result.error(400, "参数错误", "fieldA");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertEquals("fieldA", result.getData());
    }

    @Test
    @DisplayName("paramError() 返回 code=400")
    void paramError_default() {
        Result<String> result = Result.paramError();
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
    }

    @Test
    @DisplayName("paramError(message) 返回自定义参数错误")
    void paramError_withMessage() {
        Result<String> result = Result.paramError("用户名不能为空");
        assertEquals(400, result.getCode());
        assertEquals("用户名不能为空", result.getMessage());
    }

    @Test
    @DisplayName("unauthorized() 返回 code=401")
    void unauthorized_default() {
        Result<String> result = Result.unauthorized();
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
    }

    @Test
    @DisplayName("unauthorized(message) 返回自定义未授权消息")
    void unauthorized_withMessage() {
        Result<String> result = Result.unauthorized("令牌过期");
        assertEquals(401, result.getCode());
        assertEquals("令牌过期", result.getMessage());
    }

    @Test
    @DisplayName("forbidden() 返回 code=403")
    void forbidden_default() {
        Result<String> result = Result.forbidden();
        assertEquals(403, result.getCode());
        assertEquals("禁止访问", result.getMessage());
    }

    @Test
    @DisplayName("forbidden(message) 返回自定义禁止访问消息")
    void forbidden_withMessage() {
        Result<String> result = Result.forbidden("无权限操作");
        assertEquals(403, result.getCode());
        assertEquals("无权限操作", result.getMessage());
    }

    @Test
    @DisplayName("notFound() 返回 code=404")
    void notFound_default() {
        Result<String> result = Result.notFound();
        assertEquals(404, result.getCode());
        assertEquals("资源未找到", result.getMessage());
    }

    @Test
    @DisplayName("notFound(message) 返回自定义未找到消息")
    void notFound_withMessage() {
        Result<String> result = Result.notFound("员工不存在");
        assertEquals(404, result.getCode());
        assertEquals("员工不存在", result.getMessage());
    }

    @Test
    @DisplayName("timestamp 在构造时自动设置")
    void timestamp_autoSet() throws InterruptedException {
        long before = System.currentTimeMillis();
        Result<String> result = Result.success("data");
        long after = System.currentTimeMillis();
        assertTrue(result.getTimestamp() >= before);
        assertTrue(result.getTimestamp() <= after);
    }

    @Test
    @DisplayName("Result 可以作为泛型容器使用")
    void genericContainer() {
        Result<List<Integer>> result = Result.success(List.of(1, 2, 3));
        assertEquals(200, result.getCode());
        assertEquals(List.of(1, 2, 3), result.getData());
    }

    @Test
    @DisplayName("Jackson序列化/deserialization正常")
    void jacksonSerialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Result<String> original = Result.success("testData", "成功");
        String json = mapper.writeValueAsString(original);
        Result<String> deserialized = mapper.readValue(json, Result.class);
        assertEquals(original.getCode(), deserialized.getCode());
        assertEquals(original.getMessage(), deserialized.getMessage());
        assertEquals(original.getData(), deserialized.getData());
    }
}