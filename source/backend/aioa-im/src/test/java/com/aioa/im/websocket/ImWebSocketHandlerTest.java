package com.aioa.im.websocket;

import com.alibaba.fastjson2.JSONObject;
import com.aioa.im.service.MessageService;
import com.aioa.im.service.ConversationService;
import com.aioa.im.service.impl.MessageServiceImpl;
import com.aioa.im.vo.MessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImWebSocketHandler 单元测试
 * 覆盖 WebSocket 连接、ping/pong、加入/离开会话、消息发送、已读标记等核心功能
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ImWebSocketHandlerTest WebSocket处理器测试")
class ImWebSocketHandlerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private WebSocketSession session;

    @Mock
    private ConversationService conversationService;

    private ImWebSocketHandler handler;

    private static final String USER_ID = "user-001";
    private static final String OTHER_USER_ID = "user-002";
    private static final String CONV_ID = "conv-001";

    @BeforeEach
    void setUp() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        handler = new ImWebSocketHandler(messageService, redisTemplate);

        when(session.getAttributes()).thenReturn(Map.of("userId", USER_ID));
        when(session.getId()).thenReturn("session-001");
        when(session.getUri()).thenReturn(URI.create("/ws/im?userId=" + USER_ID));
        when(session.isOpen()).thenReturn(true);

        setStaticMap(handler, "userSessions", new ConcurrentHashMap<>());
        setStaticMap(handler, "conversationUsers", new ConcurrentHashMap<>());
    }

    private void setStaticMap(Object handler, String fieldName, Map<String, Object> map) throws Exception {
        java.lang.reflect.Field field = ImWebSocketHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> typedMap = (Map<String, Object>) field.get(null);
        typedMap.clear();
        typedMap.putAll(map);
    }

    @SuppressWarnings("unchecked")
    private void putToMap(Object handler, String fieldName, String key, Object value) throws Exception {
        java.lang.reflect.Field field = ImWebSocketHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object map = field.get(null);
        if (map instanceof java.util.Map) {
            ((java.util.Map<String, Object>) map).put(key, value);
        }
    }

    private boolean isUserInMap(Object handler, String fieldName, String userId) throws Exception {
        java.lang.reflect.Field field = ImWebSocketHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object map = field.get(null);
        if (map instanceof java.util.Map) {
            return ((java.util.Map<?, ?>) map).containsKey(userId);
        }
        return false;
    }

    // ==================== 连接建立测试 ====================

    @Test
    @DisplayName("afterConnectionEstablished_有效用户_连接成功")
    void afterConnectionEstablished_有效用户_连接成功() throws Exception {
        when(session.getId()).thenReturn("session-001");

        handler.afterConnectionEstablished(session);

        verify(redisTemplate.opsForValue()).set("aioa:ws:user:" + USER_ID, "session-001");
        assertTrue(isUserInMap(handler, "userSessions", USER_ID));
    }

    @Test
    @DisplayName("afterConnectionEstablished_空用户ID_关闭连接")
    void afterConnectionEstablished_空用户ID_关闭连接() throws Exception {
        when(session.getUri()).thenReturn(URI.create("/ws/im?userId="));
        when(session.getAttributes()).thenReturn(Collections.emptyMap());

        handler.afterConnectionEstablished(session);

        verify(session).close(CloseStatus.BAD_DATA);
    }

    @Test
    @DisplayName("afterConnectionClosed_正常关闭_清理用户会话")
    void afterConnectionClosed_正常关闭_清理用户会话() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);
        Set<String> members = ConcurrentHashMap.newKeySet();
        members.add(USER_ID);
        putToMap(handler, "conversationUsers", CONV_ID, members);

        handler.afterConnectionClosed(session, CloseStatus.NORMAL);

        assertFalse(isUserInMap(handler, "userSessions", USER_ID));
        verify(redisTemplate).delete("aioa:ws:user:" + USER_ID);
    }

    // ==================== ping/pong 测试 ====================

    @Test
    @DisplayName("handleTextMessage_ping动作_返回pong响应")
    void handleTextMessage_ping动作_返回pong响应() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        JSONObject ping = new JSONObject();
        ping.put("action", "ping");
        TextMessage message = new TextMessage(ping.toJSONString());

        handler.handleTextMessage(session, message);

        verify(session).sendMessage(argThat((TextMessage msg) -> {
            JSONObject resp = JSONObject.parseObject(msg.getPayload());
            return "pong".equals(resp.getString("action")) && resp.containsKey("timestamp");
        }));
    }

    // ==================== join/leave 测试 ====================

    @Test
    @DisplayName("handleTextMessage_join动作_用户加入会话")
    void handleTextMessage_join动作_用户加入会话() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        JSONObject join = new JSONObject();
        join.put("action", "join");
        join.put("conversationId", CONV_ID);
        TextMessage msg = new TextMessage(join.toJSONString());

        handler.handleTextMessage(session, msg);

        verify(session).sendMessage(argThat((TextMessage r) -> {
            JSONObject resp = JSONObject.parseObject(r.getPayload());
            return "joined".equals(resp.getString("action")) && CONV_ID.equals(resp.getString("conversationId"));
        }));
    }

    @Test
    @DisplayName("handleTextMessage_join动作_缺失conversationId_返回错误")
    void handleTextMessage_join动作_缺失conversationId_返回错误() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        JSONObject join = new JSONObject();
        join.put("action", "join");
        TextMessage msg = new TextMessage(join.toJSONString());

        handler.handleTextMessage(session, msg);

        verify(session).sendMessage(argThat((TextMessage r) -> {
            JSONObject resp = JSONObject.parseObject(r.getPayload());
            return "error".equals(resp.getString("action"));
        }));
    }

    @Test
    @DisplayName("handleTextMessage_leave动作_用户离开会话")
    void handleTextMessage_leave动作_用户离开会话() throws Exception {
        Set<String> members = ConcurrentHashMap.newKeySet();
        members.add(USER_ID);
        putToMap(handler, "conversationUsers", CONV_ID, members);
        putToMap(handler, "userSessions", USER_ID, session);

        JSONObject leave = new JSONObject();
        leave.put("action", "leave");
        leave.put("conversationId", CONV_ID);
        TextMessage msg = new TextMessage(leave.toJSONString());

        handler.handleTextMessage(session, msg);

        assertFalse(members.contains(USER_ID));
    }

    // ==================== send 消息发送测试 ====================

    @Test
    @DisplayName("handleTextMessage_send动作_发送消息成功")
    void handleTextMessage_send动作_发送消息成功() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);
        putToMap(handler, "conversationUsers", CONV_ID, ConcurrentHashMap.newKeySet());

        MessageVO messageVO = new MessageVO();
        messageVO.setId("msg-001");
        messageVO.setContent("Hello");
        when(messageService.sendMessage(eq(USER_ID), any())).thenReturn(messageVO);

        JSONObject send = new JSONObject();
        send.put("action", "send");
        send.put("conversationId", CONV_ID);
        send.put("type", 1);
        send.put("content", "Hello");
        TextMessage msg = new TextMessage(send.toJSONString());

        handler.handleTextMessage(session, msg);

        verify(messageService).sendMessage(eq(USER_ID), any());
        verify(session).sendMessage(argThat((TextMessage r) -> {
            JSONObject resp = JSONObject.parseObject(r.getPayload());
            return "sent".equals(resp.getString("action"));
        }));
    }

    @Test
    @DisplayName("handleTextMessage_send动作_缺失参数_返回错误")
    void handleTextMessage_send动作_缺失参数_返回错误() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        JSONObject send = new JSONObject();
        send.put("action", "send");
        TextMessage msg = new TextMessage(send.toJSONString());

        handler.handleTextMessage(session, msg);

        verify(session).sendMessage(argThat((TextMessage r) -> {
            JSONObject resp = JSONObject.parseObject(r.getPayload());
            return "error".equals(resp.getString("action"));
        }));
    }

    @Test
    @DisplayName("handleTextMessage_send动作_消息服务异常_返回错误")
    void handleTextMessage_send动作_消息服务异常_返回错误() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);
        putToMap(handler, "conversationUsers", CONV_ID, ConcurrentHashMap.newKeySet());

        when(messageService.sendMessage(any(), any())).thenThrow(new RuntimeException("DB error"));

        JSONObject send = new JSONObject();
        send.put("action", "send");
        send.put("conversationId", CONV_ID);
        send.put("type", 1);
        send.put("content", "Hello");
        TextMessage msg = new TextMessage(send.toJSONString());

        handler.handleTextMessage(session, msg);

        verify(session).sendMessage(argThat((TextMessage r) -> {
            JSONObject resp = JSONObject.parseObject(r.getPayload());
            return "error".equals(resp.getString("action")) && resp.getString("message").contains("Failed");
        }));
    }

    // ==================== read 已读标记测试 ====================

    @Test
    @DisplayName("handleTextMessage_read动作_标记已读成功")
    void handleTextMessage_read动作_标记已读成功() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        java.lang.reflect.Field instanceField =
            com.aioa.im.service.ConversationServiceImplSingleton.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        Object original = instanceField.get(null);
        instanceField.set(null, conversationService);

        try {
            JSONObject read = new JSONObject();
            read.put("action", "read");
            read.put("conversationId", CONV_ID);
            read.put("lastReadMsgId", "msg-001");
            TextMessage msg = new TextMessage(read.toJSONString());

            handler.handleTextMessage(session, msg);

            verify(conversationService).markAsRead(eq(CONV_ID), eq(USER_ID), eq("msg-001"));
        } finally {
            instanceField.set(null, original);
        }
    }

    // ==================== 工具方法测试 ====================

    @Test
    @DisplayName("pushToUser_用户在线_推送成功")
    void pushToUser_用户在线_推送成功() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        handler.pushToUser(USER_ID, "test_action", Map.of("key", "value"));

        verify(session).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("pushToUser_用户不在线_无操作")
    void pushToUser_用户不在线_无操作() throws Exception {
        handler.pushToUser(USER_ID, "test_action", Map.of("key", "value"));

        verify(session, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    @DisplayName("isUserOnline_用户在线_返回true")
    void isUserOnline_用户在线_返回true() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        assertTrue(handler.isUserOnline(USER_ID));
    }

    @Test
    @DisplayName("isUserOnline_用户不在线_返回false")
    void isUserOnline_用户不在线_返回false() {
        assertFalse(handler.isUserOnline(USER_ID));
    }

    @Test
    @DisplayName("getOnlineCount_会话有成员_返回正确数量")
    void getOnlineCount_会话有成员_返回正确数量() throws Exception {
        Set<String> members = ConcurrentHashMap.newKeySet();
        members.add(USER_ID);
        members.add(OTHER_USER_ID);
        putToMap(handler, "conversationUsers", CONV_ID, members);

        assertEquals(2, handler.getOnlineCount(CONV_ID));
    }

    @Test
    @DisplayName("getOnlineCount_会话无成员_返回0")
    void getOnlineCount_会话无成员_返回0() {
        assertEquals(0, handler.getOnlineCount(CONV_ID));
    }

    @Test
    @DisplayName("handleTextMessage_未知action_无异常")
    void handleTextMessage_未知action_无异常() throws Exception {
        putToMap(handler, "userSessions", USER_ID, session);

        JSONObject unknown = new JSONObject();
        unknown.put("action", "unknown_action");
        TextMessage msg = new TextMessage(unknown.toJSONString());

        handler.handleTextMessage(session, msg);
    }

    @Test
    @DisplayName("handleTextMessage_空消息_无操作")
    void handleTextMessage_空消息_无操作() throws Exception {
        TextMessage msg = new TextMessage("");

        handler.handleTextMessage(session, msg);

        verify(session, never()).sendMessage(any(TextMessage.class));
    }
}