package com.aioa.integration.repository;

import com.aioa.integration.entity.IntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> {
    List<IntegrationLog> findByConfigIdOrderByCreatedAtDesc(Long configId);
}
