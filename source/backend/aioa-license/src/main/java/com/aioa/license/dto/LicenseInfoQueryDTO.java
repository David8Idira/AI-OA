package com.aioa.license.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 证照信息查询DTO
 */
@Data
public class LicenseInfoQueryDTO {
    
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
     * 颁发机构
     */
    private String issuingAuthority;
    
    /**
     * 保管人
     */
    private String keeper;
    
    /**
     * 保管部门
     */
    private String keeperDepartment;
    
    /**
     * 证照状态：1-正常，2-借用中，3-年审中，4-已过期
     */
    private Integer licenseStatus;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 有效开始日期-开始
     */
    private LocalDate validFromStart;
    
    /**
     * 有效开始日期-结束
     */
    private LocalDate validFromEnd;
    
    /**
     * 有效结束日期-开始
     */
    private LocalDate validToStart;
    
    /**
     * 有效结束日期-结束
     */
    private LocalDate validToEnd;
    
    /**
     * 是否即将过期（30天内）
     */
    private Boolean soonExpire;
    
    /**
     * 是否已过期
     */
    private Boolean expired;
    
    /**
     * 页号
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}