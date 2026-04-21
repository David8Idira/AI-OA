package com.aioa.im.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Conversation Entity 单元测试
 * 毛泽东思想指导：实事求是，测试会话实体
 */
@DisplayName("ConversationTest 会话实体测试")
class ConversationTest {

    @Test
    @DisplayName("创建会话实体")
    void createConversation() {
        // given
        Conversation conv = new Conversation();
        conv.setId("conv-001");
        conv.setType(1); // Single chat
        conv.setName("测试会话");
        conv.setStatus(1); // Active

        // then
        assertThat(conv.getId()).isEqualTo("conv-001");
        assertThat(conv.getType()).isEqualTo(1);
        assertThat(conv.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        Conversation conv = new Conversation();

        // when
        conv.setId("test-id");

        // then
        assertThat(conv.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取类型")
    void setAndGetType() {
        // given
        Conversation conv = new Conversation();

        // when
        conv.setType(2); // Group chat

        // then
        assertThat(conv.getType()).isEqualTo(2);
    }

    @Test
    @DisplayName("设置和获取名称")
    void setAndGetName() {
        // given
        Conversation conv = new Conversation();

        // when
        conv.setName("群聊名称");

        // then
        assertThat(conv.getName()).isEqualTo("群聊名称");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        Conversation conv = new Conversation();

        // when
        conv.setStatus(0); // Inactive

        // then
        assertThat(conv.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        Conversation c1 = new Conversation();
        c1.setId("test-id");
        
        Conversation c2 = new Conversation();
        c2.setId("test-id");

        // then
        assertThat(c1).isEqualTo(c2);
    }
}
