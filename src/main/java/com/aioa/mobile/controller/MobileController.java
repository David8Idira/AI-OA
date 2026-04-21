package com.aioa.mobile.controller;

import com.aioa.common.ApiResponse;
import com.aioa.mobile.dto.DeviceRegisterDTO;
import com.aioa.mobile.dto.NotificationSendDTO;
import com.aioa.mobile.entity.MobileDevice;
import com.aioa.mobile.entity.MobileNotification;
import com.aioa.mobile.service.MobileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mobile")
@Tag(name = "移动端模块", description = "移动端适配接口API")
public class MobileController {

    private final MobileService mobileService;

    public MobileController(MobileService mobileService) {
        this.mobileService = mobileService;
    }

    @PostMapping("/devices")
    @Operation(summary = "注册移动设备")
    public ApiResponse<MobileDevice> registerDevice(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Valid @RequestBody DeviceRegisterDTO dto) {
        return ApiResponse.success(mobileService.registerDevice(userId, dto));
    }

    @GetMapping("/devices")
    @Operation(summary = "获取用户设备列表")
    public ApiResponse<List<MobileDevice>> getUserDevices(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        return ApiResponse.success(mobileService.getUserDevices(userId));
    }

    @DeleteMapping("/devices/{id}")
    @Operation(summary = "注销设备")
    public ApiResponse<Void> unregisterDevice(@Parameter(description = "设备ID") @PathVariable Long id) {
        mobileService.unregisterDevice(id);
        return ApiResponse.success("设备已注销", null);
    }

    @PostMapping("/notifications")
    @Operation(summary = "发送移动通知")
    public ApiResponse<MobileNotification> sendNotification(@Valid @RequestBody NotificationSendDTO dto) {
        return ApiResponse.created(mobileService.sendNotification(dto));
    }

    @GetMapping("/notifications")
    @Operation(summary = "获取用户通知列表")
    public ApiResponse<List<MobileNotification>> getUserNotifications(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "状态: 0未读, 1已读") @RequestParam(required = false) Integer status) {
        return ApiResponse.success(mobileService.getUserNotifications(userId, status));
    }

    @PutMapping("/notifications/{id}/read")
    @Operation(summary = "标记通知已读")
    public ApiResponse<MobileNotification> markAsRead(@Parameter(description = "通知ID") @PathVariable Long id) {
        return ApiResponse.success(mobileService.markAsRead(id));
    }

    @GetMapping("/notifications/unread-count")
    @Operation(summary = "获取未读通知数")
    public ApiResponse<Long> getUnreadCount(@Parameter(description = "用户ID") @RequestParam Long userId) {
        return ApiResponse.success(mobileService.getUnreadCount(userId));
    }
}
