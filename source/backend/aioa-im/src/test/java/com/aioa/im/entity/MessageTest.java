package com.aioa.im.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Message Entity 单元测试
 * 毛泽东思想指导：实事求是，测试消息实体
 */
@DisplayName("MessageTest 消息实体测试")
class MessageTest {

    private Message createTestMessage() {
        Message message = new Message();
        message.setId("msg-001");
        message.setConversationId("conv-001");
        message.setSenderId("user-001");
        message.setSenderNickname("张三");
        message.setType(1); // Text
        message.setContent("Hello, this is a test message");
        return message;
    }

    @Test
    @DisplayName("创建消息实体")
    void createMessage() {
        // when
        Message message = createTestMessage();

        // then
        assertThat(message.getId()).isEqualTo("msg-001");
        assertThat(message.getConversationId()).isEqualTo("conv-001");
        assertThat(message.getContent()).isEqualTo("Hello, this is a test message");
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        Message message = new Message();

        // when
        message.setId("test-id");

        // then
        assertThat(message.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取会话ID")
    void setAndGetConversationId() {
        // given
        Message message = new Message();

        // when
        message.setConversationId("conv-002");

        // then
        assertThat(message.getConversationId()).isEqualTo("conv-002");
    }

    @Test
    @DisplayName("设置和获取发送者ID")
    void setAndGetSenderId() {
        // given
        Message message = new Message();

        // when
        message.setSenderId("user-002");

        // then
        assertThat(message.getSenderId()).isEqualTo("user-002");
    }

    @Test
    @DisplayName("设置和获取发送者昵称")
    void setAndGetSenderNickname() {
        // given
        Message message = new Message();

        // when
        message.setSenderNickname("李四");

        // then
        assertThat(message.getSenderNickname()).isEqualTo("李四");
    }

    @Test
    @DisplayName("设置和获取消息类型")
    void setAndGetType() {
        // given
        Message message = new Message();

        // when
        message.setType(2); // Image

        // then
        assertThat(message.getType()).isEqualTo(2);
    }

    @Test
    @DisplayName("设置和获取内容")
    void setAndGetContent() {
        // given
        Message message = new Message();

        // when
        message.setContent("Test content");

        // then
        assertThat(message.getContent()).isEqualTo("Test content");
    }

    @Test
    @DisplayName("设置和获取额外数据")
    void setAndGetExtra() {
        // given
        Message message = new Message();

        // when
        message.setExtra("{\"key\":\"value\"}");

        // then
        assertThat(message.getExtra()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        Message msg1 = new Message();
        msg1.setId("test-id");
        
        Message msg2 = new Message();
        msg2.setId("test-id");

        // then
        assertThat(msg1).isEqualTo(msg2);
    }
}