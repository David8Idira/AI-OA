package com.aioa.mobile;

import com.aioa.mobile.entity.MobileDevice;
import com.aioa.mobile.entity.MobileNotification;
import com.aioa.mobile.repository.MobileDeviceRepository;
import com.aioa.mobile.repository.MobileNotificationRepository;
import com.aioa.mobile.service.MobileService;
import com.aioa.mobile.dto.DeviceRegisterDTO;
import com.aioa.mobile.dto.NotificationSendDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileServiceTest {

    @Mock
    private MobileDeviceRepository deviceRepo;

    @Mock
    private MobileNotificationRepository notificationRepo;

    @InjectMocks
    private MobileService mobileService;

    @Test
    void testRegisterDevice_New() {
        DeviceRegisterDTO dto = new DeviceRegisterDTO();
        dto.setDeviceToken("token123");
        dto.setPlatform("ios");
        dto.setAppVersion("1.0.0");

        when(deviceRepo.findByDeviceToken("token123")).thenReturn(Optional.empty());
        when(deviceRepo.save(any(MobileDevice.class))).thenAnswer(i -> i.getArgument(0));

        MobileDevice result = mobileService.registerDevice(1L, dto);
        assertNotNull(result);
        assertEquals("token123", result.getDeviceToken());
    }

    @Test
    void testGetUnreadCount() {
        when(notificationRepo.countByUserIdAndStatus(1L, 0)).thenReturn(5L);
        long count = mobileService.getUnreadCount(1L);
        assertEquals(5L, count);
    }
}
