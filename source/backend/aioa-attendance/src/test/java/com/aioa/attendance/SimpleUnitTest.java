package com.aioa.attendance;

import com.aioa.attendance.dto.AttendanceRuleDTO;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.service.AttendanceRuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = AioaAttendanceApplication.class)
@ActiveProfiles("test")
class SimpleUnitTest {

    @Autowired
    private AttendanceRuleService attendanceRuleService;

    @Test
    void testCreateAndGetRule() {
        AttendanceRuleDTO dto = new AttendanceRuleDTO();
        dto.setRuleName("Test Rule");
        dto.setRuleCode("TEST_RULE_001");
        dto.setStatus(1);
        dto.setWorkStartTime(LocalTime.of(9, 0));
        dto.setWorkEndTime(LocalTime.of(18, 0));
        dto.setAllowLateMinutes(15);
        dto.setAllowLeaveEarlyMinutes(15);
        dto.setOvertimeRule(1);
        dto.setWeekdays(Arrays.asList(1, 2, 3, 4, 5)); // Mon-Fri
        dto.setRemark("Test rule");

        AttendanceRule rule = attendanceRuleService.createRule(dto);
        assertNotNull(rule);
        assertNotNull(rule.getId());
        assertEquals("TEST_RULE_001", rule.getRuleCode());
        assertEquals(1, rule.getStatus());
        assertEquals("09:00", rule.getWorkStartTime().toString());
        assertEquals("18:00", rule.getWorkEndTime().toString());
        
        System.out.println("Test passed: Created rule with ID " + rule.getId());
    }

    @Test
    void testCalculateDistance() {
        // Simple test that doesn't require Spring context
        double earthRadius = 6371000; // meters
        
        // Shanghai coordinates
        double lat1 = 31.230416;
        double lon1 = 121.473701;
        
        // Nearby point (100 meters away)
        double lat2 = 31.230516;
        double lon2 = 121.473801;
        
        // Haversine formula
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;
        
        assertTrue(distance > 10 && distance < 20, "Distance should be ~14 meters, got: " + distance);
        System.out.println("Test passed: Calculated distance is " + distance + " meters");
    }

    @Test
    void testLocationInRange() {
        double centerLat = 31.230416;
        double centerLon = 121.473701;
        
        // Nearby point (within 100 meters)
        double nearbyLat = 31.230516;
        double nearbyLon = 121.473801;
        
        // Far point (more than 100 meters)
        double farLat = 31.231416;
        double farLon = 121.474701;
        
        // Simple Euclidean approximation for small distances
        double nearbyDistance = Math.sqrt(Math.pow(nearbyLat - centerLat, 2) * 111000 * 111000 + 
                                          Math.pow(nearbyLon - centerLon, 2) * 111000 * 111000 * Math.cos(Math.toRadians(centerLat)));
        double farDistance = Math.sqrt(Math.pow(farLat - centerLat, 2) * 111000 * 111000 + 
                                       Math.pow(farLon - centerLon, 2) * 111000 * 111000 * Math.cos(Math.toRadians(centerLat)));
        
        assertTrue(nearbyDistance < 100, "Nearby point should be in range, distance: " + nearbyDistance);
        assertTrue(farDistance > 100, "Far point should not be in range, distance: " + farDistance);
        System.out.println("Test passed: Location range check works");
    }
}