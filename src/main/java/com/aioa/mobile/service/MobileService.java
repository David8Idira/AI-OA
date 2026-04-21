package com.aioa.mobile.service;

import com.aioa.common.BusinessException;
import com.aioa.mobile.dto.DeviceRegisterDTO;
import com.aioa.mobile.dto.NotificationSendDTO;
import com.aioa.mobile.entity.MobileDevice;
import com.aioa.mobile.entity.MobileNotification;
import com.aioa.mobile.repository.MobileDeviceRepository;
import com.aioa.mobile.repository.MobileNotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Tag(name = "移动端服务", description = "移动端适配接口服务")
public class MobileService {

    private final MobileDeviceRepository deviceRepo;
    private final MobileNotificationRepository notificationRepo;

    public MobileService(MobileDeviceRepository deviceRepo,
                         MobileNotificationRepository notificationRepo) {
        this.deviceRepo = deviceRepo;
        this.notificationRepo = notificationRepo;
    }

    @Operation(summary = "注册移动设备")
    @Transactional
    public MobileDevice registerDevice(Long userId, DeviceRegisterDTO dto) {
        MobileDevice device = deviceRepo.findByDeviceToken(dto.getDeviceToken())
                .orElse(new MobileDevice());
        device.setUserId(userId);
        device.setDeviceToken(dto.getDeviceToken());
        device.setPlatform(dto.getPlatform());
        device.setOsVersion(dto.getOsVersion());
        device.setAppVersion(dto.getAppVersion());
        device.setEnabled(true);
        device.setLastActiveAt(LocalDateTime.now());
        return deviceRepo.save(device);
    }

    @Operation(summary = "获取用户设备列表")
    public List<MobileDevice> getUserDevices(Long userId) {
        return deviceRepo.findByUserIdAndEnabledTrue(userId);
    }

    @Operation(summary = "更新设备活跃时间")
    @Transactional
    public void updateDeviceActive(String deviceToken) {
        deviceRepo.findByDeviceToken(deviceToken).ifPresent(device -> {
            device.setLastActiveAt(LocalDateTime.now());
            deviceRepo.save(device);
        });
    }

    @Operation(summary = "发送移动通知")
    @Transactional
    public MobileNotification sendNotification(NotificationSendDTO dto) {
        List<MobileDevice> devices = deviceRepo.findByUserIdAndEnabledTrue(dto.getUserId());
        if (devices.isEmpty()) {
            throw new BusinessException(404, "用户未注册移动设备");
        }
        MobileNotification notification = new MobileNotification();
        notification.setUserId(dto.getUserId());
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(dto.getType());
        notification.setPayload(dto.getPayload());
        notification.setStatus(0);
        return notificationRepo.save(notification);
    }

    @Operation(summary = "获取用户通知列表")
    public List<MobileNotification> getUserNotifications(Long userId, Integer status) {
        if (status != null) {
            return notificationRepo.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        }
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Operation(summary = "标记通知已读")
    @Transactional
    public MobileNotification markAsRead(Long notificationId) {
        MobileNotification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new BusinessException(404, "通知不存在"));
        notification.setStatus(1);
        notification.setReadAt(LocalDateTime.now());
        return notificationRepo.save(notification);
    }

    @Operation(summary = "获取未读通知数")
    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndStatus(userId, 0);
    }

    @Operation(summary = "注销设备")
    @Transactional
    public void unregisterDevice(Long deviceId) {
        MobileDevice device = deviceRepo.findById(deviceId)
                .orElseThrow(() -> new BusinessException(404, "设备不存在"));
        device.setEnabled(false);
        deviceRepo.save(device);
    }
}
