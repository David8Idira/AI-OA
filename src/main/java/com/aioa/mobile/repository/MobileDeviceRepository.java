package com.aioa.mobile.repository;

import com.aioa.mobile.entity.MobileDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MobileDeviceRepository extends JpaRepository<MobileDevice, Long> {
    Optional<MobileDevice> findByDeviceToken(String deviceToken);
    List<MobileDevice> findByUserId(Long userId);
    List<MobileDevice> findByUserIdAndEnabledTrue(Long userId);
}
