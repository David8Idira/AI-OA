package com.aioa.mobile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "DeviceRegisterDTO", description = "设备注册DTO")
public class DeviceRegisterDTO {
    @NotBlank(message = "设备令牌不能为空")
    @Schema(description = "设备令牌(推送用)")
    private String deviceToken;

    @Schema(description = "平台: ios/android")
    private String platform;

    @Schema(description = "操作系统版本")
    private String osVersion;

    @Schema(description = "APP版本")
    private String appVersion;

    public String getDeviceToken() { return deviceToken; }
    public void setDeviceToken(String deviceToken) { this.deviceToken = deviceToken; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
}
