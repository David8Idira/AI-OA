package com.aioa.im.websocket;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.aioa.im.service.MessageService;
import com.aioa.im.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket Handler for real-time IM
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final StringRedisTemplate redisTemplate;

    /**
     * User ID -> WebSocket Session
     */
    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * Conversation ID -> Set of user IDs (online members)
     */
    private static final Map<String, Set<String>> conversationUsers = new ConcurrentHashMap<>();

    private static final String USER_KEY_PREFIX = "aioa:ws:user:";
    private static final String HEARTBEAT_INTERVAL_KEY = "aioa:ws:heartbeat:";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        if (StrUtil.isBlank(userId)) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        userSessions.put(userId, session);
        redisTemplate.opsForValue().set(USER_KEY_PREFIX + userId, session.getId());
        log.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if (StrUtil.isBlank(payload)) {
            return;
        }

        JSONObject json = JSON.parseObject(payload);
        String action = json.getString("action");

        switch (action != null ? action : "") {
            case "ping":
                handlePing(session);
                break;
            case "join":
                handleJoin(session, json);
                break;
            case "leave":
                handleLeave(session, json);
                break;
            case "send":
                handleSend(session, json);
                break;
            case "read":
                handleRead(session, json);
                break;
            default:
                log.warn("Unknown WebSocket action: {}", action);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = getUserId(session);
        if (StrUtil.isNotBlank(userId)) {
            userSessions.remove(userId);
            redisTemplate.delete(USER_KEY_PREFIX + userId);
            // Remove from all conversation rooms
            conversationUsers.values().forEach(set -> set.remove(userId));
            log.info("WebSocket disconnected: userId={}, reason={}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: sessionId={}", session.getId(), exception);
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    // ==================== Action Handlers ====================

    private void handlePing(WebSocketSession session) throws IOException {
        JSONObject resp = new JSONObject();
        resp.put("action", "pong");
        resp.put("timestamp", System.currentTimeMillis());
        session.sendMessage(new TextMessage(resp.toJSONString()));
    }

    private void handleJoin(WebSocketSession session, JSONObject json) throws IOException {
        String userId = getUserId(session);
        String conversationId = json.getString("conversationId");

        if (StrUtil.isBlank(conversationId)) {
            sendError(session, "conversationId is required");
            return;
        }

        conversationUsers.computeIfAbsent(conversationId, k -> new CopyOnWriteArraySet<>()).add(userId);

        JSONObject resp = new JSONObject();
        resp.put("action", "joined");
        resp.put("conversationId", conversationId);
        resp.put("onlineCount", conversationUsers.get(conversationId).size());
        session.sendMessage(new TextMessage(resp.toJSONString()));

        log.debug("User {} joined conversation {}", userId, conversationId);
    }

    private void handleLeave(WebSocketSession session, JSONObject json) throws IOException {
        String userId = getUserId(session);
        String conversationId = json.getString("conversationId");

        if (conversationId != null && conversationUsers.containsKey(conversationId)) {
            conversationUsers.get(conversationId).remove(userId);
            log.debug("User {} left conversation {}", userId, conversationId);
        }
    }

    private void handleSend(WebSocketSession session, JSONObject json) throws IOException {
        String userId = getUserId(session);
        String conversationId = json.getString("conversationId");
        Integer type = json.getInteger("type");
        String content = json.getString("content");

        if (StrUtil.isBlank(conversationId) || type == null || StrUtil.isBlank(content)) {
            sendError(session, "conversationId, type and content are required");
            return;
        }

        // Send via message service
        com.aioa.im.dto.SendMessageDTO dto = new com.aioa.im.dto.SendMessageDTO();
        dto.setConversationId(conversationId);
        dto.setType(type);
        dto.setContent(content);

        try {
            MessageVO message = messageService.sendMessage(userId, dto);

            // Send back confirmation
            JSONObject resp = new JSONObject();
            resp.put("action", "sent");
            resp.put("message", JSON.toJSONString(message));
            session.sendMessage(new TextMessage(resp.toJSONString()));

            // Broadcast to other members in the conversation
            broadcastToConversation(conversationId, userId, "new_message", message);

        } catch (Exception e) {
            sendError(session, "Failed to send message: " + e.getMessage());
        }
    }

    private void handleRead(WebSocketSession session, JSONObject json) throws IOException {
        String userId = getUserId(session);
        String conversationId = json.getString("conversationId");
        String lastReadMsgId = json.getString("lastReadMsgId");

        if (StrUtil.isBlank(conversationId)) {
            sendError(session, "conversationId is required");
            return;
        }

        try {
            conversationService().markAsRead(conversationId, userId, lastReadMsgId);

            // Notify other members
            JSONObject notification = new JSONObject();
            notification.put("action", "read_receipt");
            notification.put("conversationId", conversationId);
            notification.put("userId", userId);
            notification.put("lastReadMsgId", lastReadMsgId);
            broadcastToConversation(conversationId, userId, "read_receipt", notification);

        } catch (Exception e) {
            sendError(session, "Failed to mark as read: " + e.getMessage());
        }
    }

    // ==================== Broadcast Utilities ====================

    private void broadcastToConversation(String conversationId, String excludeUserId, String action, Object data) {
        Set<String> members = conversationUsers.get(conversationId);
        if (members == null || members.isEmpty()) {
            return;
        }

        JSONObject message = new JSONObject();
        message.put("action", action);
        message.put("conversationId", conversationId);
        message.put("data", data);

        for (String userId : members) {
            if (userId.equals(excludeUserId)) {
                continue;
            }
            WebSocketSession session = userSessions.get(userId);
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message.toJSONString()));
                } catch (IOException e) {
                    log.error("Failed to send message to user {}: {}", userId, e.getMessage());
                }
            }
        }
    }

    private void sendError(WebSocketSession session, String message) throws IOException {
        JSONObject resp = new JSONObject();
        resp.put("action", "error");
        resp.put("message", message);
        session.sendMessage(new TextMessage(resp.toJSONString()));
    }

    // ==================== Session Utilities ====================

    private String getUserId(WebSocketSession session) {
        // Try to get from query parameter
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        for (String param : query.split("&")) {
            if (param.startsWith("userId=")) {
                return param.substring(7);
            }
        }
        // Try from session attributes (set by interceptor)
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : null;
    }

    private com.aioa.im.service.ConversationService conversationService() {
        // Lazy load to avoid circular dependency
        return com.aioa.im.service.ConversationServiceImplSingleton.INSTANCE;
    }

    /**
     * Send push notification to a specific user via WebSocket
     */
    public void pushToUser(String userId, String action, Object data) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                JSONObject message = new JSONObject();
                message.put("action", action);
                message.put("data", data);
                session.sendMessage(new TextMessage(message.toJSONString()));
            } catch (IOException e) {
                log.error("Failed to push message to user {}: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * Check if user is online
     */
    public boolean isUserOnline(String userId) {
        return userSessions.containsKey(userId);
    }

    /**
     * Get online count for a conversation
     */
    public int getOnlineCount(String conversationId) {
        Set<String> members = conversationUsers.get(conversationId);
        return members != null ? members.size() : 0;
    }
}
