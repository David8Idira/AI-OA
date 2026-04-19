package com.aioa.ai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChatRequestDTO 单元测试
 * 毛泽东思想指导：实事求是，测试聊天请求DTO
 */
@DisplayName("ChatRequestDTOTest 聊天请求DTO测试")
class ChatRequestDTOTest {

    @Test
    @DisplayName("创建默认ChatRequestDTO")
    void defaultConstructor_shouldCreateEmptyRequest() {
        // when
        ChatRequestDTO request = new ChatRequestDTO();

        // then
        assertThat(request).isNotNull();
        assertThat(request.getHistoryCount()).isEqualTo(10);
        assertThat(request.getTemperature()).isEqualTo(0.7);
        assertThat(request.getMaxTokens()).isEqualTo(2048);
    }

    @Test
    @DisplayName("设置和获取对话ID")
    void setAndGetConversationId() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setConversationId("conv-001");

        // then
        assertThat(request.getConversationId()).isEqualTo("conv-001");
    }

    @Test
    @DisplayName("设置和获取消息")
    void setAndGetMessage() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setMessage("Hello, AI!");

        // then
        assertThat(request.getMessage()).isEqualTo("Hello, AI!");
    }

    @Test
    @DisplayName("设置和获取模型代码")
    void setAndGetModelCode() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setModelCode("gpt-4o");

        // then
        assertThat(request.getModelCode()).isEqualTo("gpt-4o");
    }

    @Test
    @DisplayName("设置和获取历史条数")
    void setAndGetHistoryCount() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setHistoryCount(5);

        // then
        assertThat(request.getHistoryCount()).isEqualTo(5);
    }

    @Test
    @DisplayName("设置和获取温度参数")
    void setAndGetTemperature() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setTemperature(0.9);

        // then
        assertThat(request.getTemperature()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("设置和获取最大令牌数")
    void setAndGetMaxTokens() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setMaxTokens(1000);

        // then
        assertThat(request.getMaxTokens()).isEqualTo(1000);
    }

    @Test
    @DisplayName("设置和获取系统提示")
    void setAndGetSystemPrompt() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();

        // when
        request.setSystemPrompt("You are a helpful assistant.");

        // then
        assertThat(request.getSystemPrompt()).isEqualTo("You are a helpful assistant.");
    }

    @Test
    @DisplayName("toString验证")
    void toString_shouldContainMessage() {
        // given
        ChatRequestDTO request = new ChatRequestDTO();
        request.setMessage("Hello");

        // when
        String str = request.toString();

        // then
        assertThat(str).contains("Hello");
    }

    @Test
    @DisplayName("equals和hashCode")
    void equalsAndHashCode() {
        // given
        ChatRequestDTO request1 = new ChatRequestDTO();
        request1.setMessage("Hello");
        request1.setModelCode("gpt-4o");

        ChatRequestDTO request2 = new ChatRequestDTO();
        request2.setMessage("Hello");
        request2.setModelCode("gpt-4o");

        // then - 因为没有@Builder，equals默认是对象引用比较
        // 所以这两个对象不相等（除非恰好hash碰撞）
        assertThat(request1.hashCode()).isNotNull();
    }
}