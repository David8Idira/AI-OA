package com.aioa.hr.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 员工DTO
 */
@Data
public class EmployeeDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 员工编号
     */
    private String employeeNo;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别：1-男，2-女
     */
    private Integer gender;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 身份证号
     */
    private String idCard;
    
    /**
     * 部门ID
     */
    private Long departmentId;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 职位ID
     */
    private Long positionId;
    
    /**
     * 职位名称
     */
    private String positionName;
    
    /**
     * 入职日期
     */
    private LocalDate entryDate;
    
    /**
     * 转正日期
     */
    private LocalDate regularizationDate;
    
    /**
     * 离职日期
     */
    private LocalDate resignationDate;
    
    /**
     * 员工状态：1-试用，2-正式，3-离职
     */
    private Integer employeeStatus;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}