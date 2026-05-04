package com.aioa.attendance.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CheckinDTO 测试
 */
@DisplayName("CheckinDTO 测试")
class CheckinDTOTest {

    @Nested
    @DisplayName("GPS签到测试")
    class GpsCheckinTests {

        @Test
        @DisplayName("GPS签到设置所有字段")
        void setGpsCheckinFields() {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(0);
            dto.setMethod(0);
            dto.setLatitude(new BigDecimal("31.230416"));
            dto.setLongitude(new BigDecimal("121.473701"));
            dto.setAddress("上海市黄浦区");
            dto.setDeviceId("device-001");
            dto.setIp("192.168.1.100");
            dto.setRemark("正常签到");

            assertThat(dto.getUserId()).isEqualTo("user-001");
            assertThat(dto.getCheckinType()).isEqualTo(0);
            assertThat(dto.getMethod()).isEqualTo(0);
            assertThat(dto.getLatitude()).isEqualTo(new BigDecimal("31.230416"));
            assertThat(dto.getLongitude()).isEqualTo(new BigDecimal("121.473701"));
            assertThat(dto.getAddress()).isEqualTo("上海市黄浦区");
        }
    }

    @Nested
    @DisplayName("WiFi签到测试")
    class WifiCheckinTests {

        @Test
        @DisplayName("WiFi签到设置WiFi字段")
        void setWifiCheckinFields() {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(0);
            dto.setMethod(1);
            dto.setWifiMac("00:11:22:33:44:55");

            assertThat(dto.getMethod()).isEqualTo(1);
            assertThat(dto.getWifiMac()).isEqualTo("00:11:22:33:44:55");
        }
    }

    @Nested
    @DisplayName("签退测试")
    class CheckoutTests {

        @Test
        @DisplayName("签退设置")
        void setCheckoutFields() {
            CheckinDTO dto = new CheckinDTO();
            dto.setUserId("user-001");
            dto.setCheckinType(1);
            dto.setMethod(2);

            assertThat(dto.getCheckinType()).isEqualTo(1);
            assertThat(dto.getMethod()).isEqualTo(2);
        }
    }
}
