package com.aioa.mobile.repository;

import com.aioa.mobile.entity.MobileNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MobileNotificationRepository extends JpaRepository<MobileNotification, Long> {
    List<MobileNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<MobileNotification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Integer status);
    long countByUserIdAndStatus(Long userId, Integer status);
}
