package com.aioa.integration.repository;

import com.aioa.integration.entity.IntegrationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface IntegrationConfigRepository extends JpaRepository<IntegrationConfig, Long> {
    Optional<IntegrationConfig> findByIntegrationKey(String integrationKey);
    List<IntegrationConfig> findByEnabledTrue();
    boolean existsByIntegrationKey(String integrationKey);
}
