package com.aioa.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 认证过滤器 - 验证JWT Token
 * 功能：
 * 1. 验证 JWT Token 的签名和有效性
 * 2. 检查 Token 是否在黑名单中（登出/踢出）
 * 3. 将用户信息传递到下游服务
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ReactiveStringRedisTemplate redisTemplate;
    
    @Value("${aioa.jwt.secret}")
    private String jwtSecret;
    
    @Value("${aioa.jwt.issuer:aioa-system}")
    private String jwtIssuer;
    
    // 跳过认证的路径（支持通配符）
    private static final List<String> SKIP_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/user/register",
        "/actuator/**",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/api/public/**"
    );
    
    // WebSocket 路径（需要特殊处理）
    private static final List<String> WS_PATHS = List.of(
        "/api/im/ws/**",
        "/ws/**"
    );
    
    public AuthFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 1. 检查是否需要跳过认证
        if (shouldSkipAuth(path)) {
            log.debug("跳过认证: {}", path);
            return chain.filter(exchange);
        }
        
        // 2. 获取 Token
        String token = extractToken(request);
        if (token == null || token.isEmpty()) {
            log.warn("缺少认证Token: {}", path);
            return unauthorized(exchange.getResponse(), "缺少认证Token");
        }
        
        // 3. 验证 Token
        try {
            Claims claims = validateToken(token);
            
            // 4. 检查 Token 是否在黑名单中
            String jti = claims.getId(); // Token ID
            if (jti != null) {
                String blacklisted = redisTemplate.opsForValue()
                    .get("jwt:blacklist:" + jti)
                    .block();
                if ("1".equals(blacklisted) || "true".equalsIgnoreCase(blacklisted)) {
                    log.warn("Token已被加入黑名单: jti={}", jti);
                    return unauthorized(exchange.getResponse(), "Token已失效");
                }
            }
            
            // 5. 将用户信息注入请求头，传递给下游服务
            String userId = claims.getSubject();
            String username = claims.get("username", String.class);
            String roles = claims.get("roles", String.class);
            
            ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-Username", username != null ? username : "")
                .header("X-User-Roles", roles != null ? roles : "")
                .header("X-Auth-Token", token)
                .build();
            
            log.debug("认证成功: userId={}, username={}, path={}", userId, username, path);
            
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
            
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return unauthorized(exchange.getResponse(), "Token已过期，请重新登录");
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage());
            return unauthorized(exchange.getResponse(), "无效的认证Token");
        }
    }
    
    /**
     * 判断是否跳过认证
     */
    private boolean shouldSkipAuth(String path) {
        return SKIP_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path))
            || WS_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
    
    /**
     * 从请求中提取 Token
     */
    private String extractToken(ServerHttpRequest request) {
        // 优先从 Authorization Header 获取
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        
        // 从 Query Parameter 获取（用于 WebSocket 连接等场景）
        List<String> tokenParams = request.getQueryParams().get("token");
        if (tokenParams != null && !tokenParams.isEmpty()) {
            return tokenParams.get(0);
        }
        
        return null;
    }
    
    /**
     * 验证 JWT Token 并解析 Claims
     */
    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.parser()
            .verifyWith(key)
            .requireIssuer(jwtIssuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
    
    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        // 构建 JSON 响应
        String body = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }
    
    @Override
    public int getOrder() {
        return -100; // 高优先级，确保在其他过滤器之前执行
    }
}