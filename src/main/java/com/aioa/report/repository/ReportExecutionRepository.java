package com.aioa.report.repository;

import com.aioa.report.entity.ReportExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {
    List<ReportExecution> findByReportIdOrderByCreatedAtDesc(Long reportId);
    List<ReportExecution> findByExecutedByOrderByCreatedAtDesc(Long executedBy);
}
