package com.aioa.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Check-in DTO
 */
@Data
@Schema(description = "Check-in DTO")
public class CheckinDTO {

    @NotNull(message = "User ID cannot be null")
    @Schema(description = "User ID", required = true)
    private String userId;

    @Schema(description = "Check-in type: 0-In, 1-Out, 2-Both")
    private Integer checkinType = 0;

    @Schema(description = "Latitude")
    private BigDecimal latitude;

    @Schema(description = "Longitude")
    private BigDecimal longitude;

    @Schema(description = "Address")
    private String address;

    @Schema(description = "WiFi MAC address")
    private String wifiMac;

    @Schema(description = "Device ID")
    private String deviceId;

    @Schema(description = "IP address")
    private String ip;

    @Schema(description = "Check-in method: 0-GPS, 1-WiFi, 2-Manual, 3-Remote")
    private Integer method = 0;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Photo URL (for facial recognition)")
    private String photoUrl;

    @Schema(description = "Target location ID (if checking in to specific location)")
    private Long locationId;
}