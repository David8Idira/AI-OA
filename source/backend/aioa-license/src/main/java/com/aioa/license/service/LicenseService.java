package com.aioa.license.service;

import com.aioa.license.entity.LicenseInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 证照信息Service接口
 */
public interface LicenseService extends IService<LicenseInfo> {
    
    /**
     * 分页查询证照列表
     */
    java.util.List<LicenseInfo> pageLicense(int pageNum, int pageSize, LicenseInfo query);
    
    /**
     * 获取即将过期的证照（30天内）
     */
    java.util.List<LicenseInfo> getSoonExpiringLicenses();
    
    /**
     * 获取已过期的证照
     */
    java.util.List<LicenseInfo> getExpiredLicenses();
    
    /**
     * 更新证照状态
     */
    boolean updateLicenseStatus(Long id, Integer status);
}