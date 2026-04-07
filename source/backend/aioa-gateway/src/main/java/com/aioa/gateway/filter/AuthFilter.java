package com.aioa.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 认证过滤器 - 验证JWT Token
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    // 跳过认证的路径
    private static final List<String> SKIP_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/user/register",
        "/actuator/health",
        "/swagger-ui",
        "/v3/api-docs"
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 跳过不需要认证的路径
        if (skipAuth(path)) {
            return chain.filter(exchange);
        }
        
        // 获取Token
        String token = getToken(request);
        if (token == null || token.isEmpty()) {
            return unauthorized(exchange.getResponse(), "缺少认证Token");
        }
        
        // TODO: 验证Token有效性
        // 这里可以调用Redis或JWT服务验证Token
        
        return chain.filter(exchange);
    }
    
    /**
     * 判断是否跳过认证
     */
    private boolean skipAuth(String path) {
        return SKIP_PATHS.stream().anyMatch(path::startsWith);
    }
    
    /**
     * 从请求中获取Token
     */
    private String getToken(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers != null && !headers.isEmpty()) {
            String authHeader = headers.get(0);
            if (authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }
    
    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        return response.writeWith(Mono.just(response.bufferFactory()
            .wrap(("{\"code\":401,\"message\":\"" + message + "\"}").getBytes())));
    }
    
    @Override
    public int getOrder() {
        return -100; // 高优先级
    }
}