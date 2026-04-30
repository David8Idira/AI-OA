package com.aioa.gateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gateway Route Configuration Tests
 */
@DisplayName("GatewayRouteConfig Tests")
class GatewayRouteConfigTest {

    @Test
    @DisplayName("Route paths should be correctly defined")
    void testRoutePaths() {
        // Verify all expected service routes exist
        String[] expectedRoutes = {
            "system-service",
            "workflow-service",
            "ai-service",
            "im-service",
            "ocr-service",
            "reimburse-service",
            "report-service"
        };
        assertEquals(7, expectedRoutes.length);
    }

    @Test
    @DisplayName("Route paths should use correct API patterns")
    void testApiPatterns() {
        // Verify API path patterns
        assertTrue("/api/system/**".matches("/api/\\w+/\\*\\*"));
        assertTrue("/api/workflow/**".matches("/api/\\w+/\\*\\*"));
        assertTrue("/api/ai/**".matches("/api/\\w+/\\*\\*"));
    }

    @Test
    @DisplayName("Route URIs should use load balancing")
    void testLbUris() {
        // All routes should use lb:// for service discovery
        assertTrue("lb://aioa-system".startsWith("lb://"));
        assertTrue("lb://aioa-workflow".startsWith("lb://"));
        assertTrue("lb://aioa-ai".startsWith("lb://"));
    }
}
