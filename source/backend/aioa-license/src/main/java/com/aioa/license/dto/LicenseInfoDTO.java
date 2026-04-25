package com.aioa.license.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 证照信息DTO
 */
@Data
public class LicenseInfoDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 证照编号
     */
    private String licenseNo;
    
    /**
     * 证照名称
     */
    private String licenseName;
    
    /**
     * 证照分类ID
     */
    private Long categoryId;
    
    /**
     * 证照分类名称
     */
    private String categoryName;
    
    /**
     * 颁发机构
     */
    private String issuingAuthority;
    
    /**
     * 颁发日期
     */
    private LocalDate issueDate;
    
    /**
     * 有效开始日期
     */
    private LocalDate validFrom;
    
    /**
     * 有效结束日期
     */
    private LocalDate validTo;
    
    /**
     * 年审日期
     */
    private LocalDate annualReviewDate;
    
    /**
     * 年审周期（月）
     */
    private Integer reviewCycle;
    
    /**
     * 保管人
     */
    private String keeper;
    
    /**
     * 保管人ID
     */
    private String keeperId;
    
    /**
     * 保管部门
     */
    private String keeperDepartment;
    
    /**
     * 保管部门ID
     */
    private Long keeperDepartmentId;
    
    /**
     * 证照状态：1-正常，2-借用中，3-年审中，4-已过期
     */
    private Integer licenseStatus;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}