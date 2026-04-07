package com.aioa.im.websocket;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket Interceptor for authentication and user identification
 */
@Slf4j
public class ImWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Get userId from query parameter or header
        String userId = null;

        if (request instanceof ServletServerHttpRequest servletRequest) {
            String query = servletRequest.getURI().getQuery();
            if (StrUtil.isNotBlank(query)) {
                for (String param : query.split("&")) {
                    if (param.startsWith("userId=")) {
                        userId = param.substring(7);
                        break;
                    }
                }
            }
            // Try token-based auth header
            String authHeader = servletRequest.getHeaders().getFirst("Authorization");
            if (StrUtil.isNotBlank(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // TODO: Validate token and extract userId
                // userId = jwtUtil.extractUserId(token);
            }
        }

        if (StrUtil.isBlank(userId)) {
            log.warn("WebSocket handshake rejected: userId not found");
            return false;
        }

        attributes.put("userId", userId);
        log.debug("WebSocket handshake accepted: userId={}", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // No-op
    }
}
