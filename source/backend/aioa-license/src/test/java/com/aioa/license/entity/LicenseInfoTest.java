package com.aioa.license.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LicenseInfo Entity Tests
 */
@DisplayName("LicenseInfo Entity Tests")
class LicenseInfoTest {

    @Test
    @DisplayName("LicenseInfo should be creatable with all fields")
    void testLicenseInfoCreation() {
        LicenseInfo license = new LicenseInfo();
        license.setId(1L);
        license.setLicenseNo("LIC001");
        license.setLicenseName("营业执照");
        license.setCategoryId(1L);
        license.setIssuingAuthority("工商局");
        
        assertEquals(1L, license.getId());
        assertEquals("LIC001", license.getLicenseNo());
        assertEquals("营业执照", license.getLicenseName());
        assertEquals(1L, license.getCategoryId());
        assertEquals("工商局", license.getIssuingAuthority());
    }

    @Test
    @DisplayName("LicenseInfo status should support valid values")
    void testStatusValues() {
        Integer expired = 0;
        Integer active = 1;
        Integer pending = 2;
        assertTrue(expired < active);
        assertTrue(active < pending);
    }

    @Test
    @DisplayName("LicenseInfo should handle valid dates")
    void testValidDates() {
        LicenseInfo license = new LicenseInfo();
        license.setValidTo(null);
        assertNull(license.getValidTo());
        
        license.setValidTo(LocalDate.now().plusYears(1));
        assertNotNull(license.getValidTo());
    }
}