package com.aioa.report.service;

import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.entity.Report;
import com.aioa.report.vo.ReportVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Report Service Interface
 */
public interface ReportService extends IService<Report> {

    /**
     * Generate a new AI-powered report
     *
     * @param userId current user ID
     * @param dto    generate report DTO
     * @return generated report VO
     */
    ReportVO generateReport(String userId, GenerateReportDTO dto);

    /**
     * Get report details by ID
     *
     * @param reportId report ID
     * @param userId   current user ID
     * @return report VO
     */
    ReportVO getReportDetail(String reportId, String userId);

    /**
     * Delete a report
     *
     * @param reportId report ID
     * @param userId   current user ID
     * @return true if deleted successfully
     */
    boolean deleteReport(String reportId, String userId);

    /**
     * Export report to specified format
     *
     * @param reportId report ID
     * @param format   export format (PDF, EXCEL, HTML)
     * @param userId   current user ID
     * @return export file path
     */
    String exportReport(String reportId, String format, String userId);

    /**
     * Get user's report list with pagination
     *
     * @param userId current user ID
     * @param dto    query parameters
     * @return paginated report list
     */
    com.aioa.report.vo.PageResult<ReportVO> getReportList(String userId, com.aioa.report.dto.ReportQueryDTO dto);

    /**
     * Regenerate a failed report
     *
     * @param reportId report ID
     * @param userId   current user ID
     * @return regenerated report VO
     */
    ReportVO regenerateReport(String reportId, String userId);
}
