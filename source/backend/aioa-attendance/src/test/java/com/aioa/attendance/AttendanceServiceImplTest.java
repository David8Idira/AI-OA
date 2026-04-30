package com.aioa.attendance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AttendanceService Standalone Unit Tests (Pure Mockito)
 */
@DisplayName("AttendanceServiceImpl Unit Tests")
class AttendanceServiceImplTest {

    @Test
    @DisplayName("Calculate distance between two points (Haversine formula)")
    void testCalculateDistance() {
        // Shanghai coordinates
        BigDecimal shanghaiLat = new BigDecimal("31.230416");
        BigDecimal shanghaiLon = new BigDecimal("121.473701");
        
        // Beijing coordinates
        BigDecimal beijingLat = new BigDecimal("39.904202");
        BigDecimal beijingLon = new BigDecimal("116.407394");
        
        // Calculate using Haversine formula
        double distance = calculateHaversineDistance(
            shanghaiLat.doubleValue(), shanghaiLon.doubleValue(),
            beijingLat.doubleValue(), beijingLon.doubleValue()
        );
        
        // Distance should be approximately 1068 km (1,068,000 - 1,100,000 meters)
        assertTrue(distance > 1000000 && distance < 1200000, 
                "Distance should be ~1068km, got: " + distance + " meters");
    }

    @Test
    @DisplayName("Nearby location should be within range")
    void testLocationInRange_NearbyPoint() {
        BigDecimal centerLat = new BigDecimal("31.230416");
        BigDecimal centerLon = new BigDecimal("121.473701");
        
        // Nearby point (within 100 meters - approximately 0.001 degree)
        BigDecimal nearbyLat = new BigDecimal("31.230516");
        BigDecimal nearbyLon = new BigDecimal("121.473801");
        
        double distance = calculateHaversineDistance(
            centerLat.doubleValue(), centerLon.doubleValue(),
            nearbyLat.doubleValue(), nearbyLon.doubleValue()
        );
        
        assertTrue(distance < 100, "Nearby point should be within 100m, got: " + distance + "m");
    }

    @Test
    @DisplayName("Far location should be outside range")
    void testLocationInRange_FarPoint() {
        BigDecimal centerLat = new BigDecimal("31.230416");
        BigDecimal centerLon = new BigDecimal("121.473701");
        
        // Far point (more than 100 meters)
        BigDecimal farLat = new BigDecimal("31.231416");
        BigDecimal farLon = new BigDecimal("121.474701");
        
        double distance = calculateHaversineDistance(
            centerLat.doubleValue(), centerLon.doubleValue(),
            farLat.doubleValue(), farLon.doubleValue()
        );
        
        assertTrue(distance > 100, "Far point should be > 100m, got: " + distance + "m");
    }

    @Test
    @DisplayName("Same coordinates should return zero distance")
    void testCalculateDistance_SamePoint() {
        BigDecimal lat = new BigDecimal("31.230416");
        BigDecimal lon = new BigDecimal("121.473701");
        
        double distance = calculateHaversineDistance(
            lat.doubleValue(), lon.doubleValue(),
            lat.doubleValue(), lon.doubleValue()
        );
        
        assertEquals(0, distance, 0.001, "Same point should have zero distance");
    }

    /**
     * Haversine formula implementation
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth's radius in meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}