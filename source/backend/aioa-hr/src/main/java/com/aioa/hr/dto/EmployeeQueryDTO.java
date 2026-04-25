package com.aioa.hr.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 员工查询DTO
 */
@Data
public class EmployeeQueryDTO {
    
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 职位ID
     */
    private Long positionId;
    
    /**
     * 员工状态：1-试用，2-正式，3-离职
     */
    private Integer employeeStatus;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 入职日期范围-开始
     */
    private LocalDate entryDateStart;
    
    /**
     * 入职日期范围-结束
     */
    private LocalDate entryDateEnd;
    
    /**
     * 页号
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}