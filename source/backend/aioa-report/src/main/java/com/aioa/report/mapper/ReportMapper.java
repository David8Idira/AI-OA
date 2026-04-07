package com.aioa.report.mapper;

import com.aioa.report.entity.Report;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Report Mapper Interface
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * Query reports with pagination and filters
     *
     * @param page      pagination object
     * @param keyword   keyword search
     * @param type      report type
     * @param status    report status
     * @param creatorId creator user ID
     * @param deptId    department ID
     * @param startDate start date
     * @param endDate   end date
     * @param sortField sort field
     * @param sortOrder sort order
     * @return paginated report list
     */
    IPage<Report> selectReportPage(
            Page<Report> page,
            @Param("keyword") String keyword,
            @Param("type") String type,
            @Param("status") Integer status,
            @Param("creatorId") String creatorId,
            @Param("deptId") String deptId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );
}
