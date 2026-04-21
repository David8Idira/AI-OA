package com.aioa.report.repository;

import com.aioa.report.entity.ReportDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportDefinitionRepository extends JpaRepository<ReportDefinition, Long> {
    Optional<ReportDefinition> findByReportKey(String reportKey);
    List<ReportDefinition> findByActiveTrue();
    boolean existsByReportKey(String reportKey);
}
