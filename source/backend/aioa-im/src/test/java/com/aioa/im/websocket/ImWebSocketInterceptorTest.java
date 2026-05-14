package com.aioa.im.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ImWebSocketInterceptor 单元测试
 * 覆盖 WebSocket 握手认证、用户标识提取等功能
 */
class ImWebSocketInterceptorTest {

    private ImWebSocketInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new ImWebSocketInterceptor();
    }

    @Test
    @DisplayName("beforeHandshake_有效userId参数_握手成功")
    void beforeHandshake_有效userId参数_握手成功() throws Exception {
        ServletServerHttpRequest servletRequest = mock(ServletServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler wsHandler = mock(WebSocketHandler.class);
        Map<String, Object> attributes = new HashMap<>();

        when(servletRequest.getURI()).thenReturn(new java.net.URI("/ws/im?userId=user-001"));
        when(servletRequest.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());

        boolean result = interceptor.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertTrue(result);
        assertEquals("user-001", attributes.get("userId"));
    }

    @Test
    @DisplayName("beforeHandshake_缺失userId_握手失败")
    void beforeHandshake_缺失userId_握手失败() throws Exception {
        ServletServerHttpRequest servletRequest = mock(ServletServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler wsHandler = mock(WebSocketHandler.class);
        Map<String, Object> attributes = new HashMap<>();

        when(servletRequest.getURI()).thenReturn(new java.net.URI("/ws/im"));
        when(servletRequest.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());

        boolean result = interceptor.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertFalse(result);
    }

    @Test
    @DisplayName("beforeHandshake_空userId_握手失败")
    void beforeHandshake_空userId_握手失败() throws Exception {
        ServletServerHttpRequest servletRequest = mock(ServletServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler wsHandler = mock(WebSocketHandler.class);
        Map<String, Object> attributes = new HashMap<>();

        when(servletRequest.getURI()).thenReturn(new java.net.URI("/ws/im?userId="));
        when(servletRequest.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());

        boolean result = interceptor.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertFalse(result);
    }

    @Test
    @DisplayName("beforeHandshake_包含多个参数_正确提取userId")
    void beforeHandshake_包含多个参数_正确提取userId() throws Exception {
        ServletServerHttpRequest servletRequest = mock(ServletServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler wsHandler = mock(WebSocketHandler.class);
        Map<String, Object> attributes = new HashMap<>();

        when(servletRequest.getURI()).thenReturn(new java.net.URI("/ws/im?token=abc&userId=user-002&role=admin"));
        when(servletRequest.getHeaders()).thenReturn(new org.springframework.http.HttpHeaders());

        boolean result = interceptor.beforeHandshake(servletRequest, response, wsHandler, attributes);

        assertTrue(result);
        assertEquals("user-002", attributes.get("userId"));
    }

    @Test
    @DisplayName("afterHandshake_正常握手后_无异常")
    void afterHandshake_正常握手后_无异常() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler wsHandler = mock(WebSocketHandler.class);

        // Should not throw
        interceptor.afterHandshake(request, response, wsHandler, null);
    }

    @Test
    @DisplayName("afterHandshake_握手异常_传递异常信息")
    void afterHandshake_握手异常_传递异常信息() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServerHttpResponse response = mock(ServerHttpResponse.class);
        WebSocketHandler wsHandler = mock(WebSocketHandler.class);
        Exception exception = new RuntimeException("Handshake failed");

        // Should not throw
        interceptor.afterHandshake(request, response, wsHandler, exception);
    }
}