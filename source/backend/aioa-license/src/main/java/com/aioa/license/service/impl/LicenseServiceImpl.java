package com.aioa.license.service.impl;

import com.aioa.license.entity.LicenseInfo;
import com.aioa.license.mapper.LicenseInfoMapper;
import com.aioa.license.service.LicenseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 证照信息Service实现
 */
@Slf4j
@Service
public class LicenseServiceImpl extends ServiceImpl<LicenseInfoMapper, LicenseInfo> implements LicenseService {
    
    @Override
    public List<LicenseInfo> pageLicense(int pageNum, int pageSize, LicenseInfo query) {
        LambdaQueryWrapper<LicenseInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            if (query.getLicenseNo() != null) {
                queryWrapper.like(LicenseInfo::getLicenseNo, query.getLicenseNo());
            }
            if (query.getLicenseName() != null) {
                queryWrapper.like(LicenseInfo::getLicenseName, query.getLicenseName());
            }
            if (query.getCategoryId() != null) {
                queryWrapper.eq(LicenseInfo::getCategoryId, query.getCategoryId());
            }
            if (query.getLicenseStatus() != null) {
                queryWrapper.eq(LicenseInfo::getLicenseStatus, query.getLicenseStatus());
            }
            if (query.getStatus() != null) {
                queryWrapper.eq(LicenseInfo::getStatus, query.getStatus());
            }
            if (query.getKeeper() != null) {
                queryWrapper.like(LicenseInfo::getKeeper, query.getKeeper());
            }
            if (query.getKeeperDepartment() != null) {
                queryWrapper.eq(LicenseInfo::getKeeperDepartment, query.getKeeperDepartment());
            }
        }
        
        queryWrapper.orderByDesc(LicenseInfo::getCreateTime);
        
        int offset = (pageNum - 1) * pageSize;
        queryWrapper.last("LIMIT " + offset + ", " + pageSize);
        
        return list(queryWrapper);
    }
    
    @Override
    public List<LicenseInfo> getSoonExpiringLicenses() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        
        LambdaQueryWrapper<LicenseInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LicenseInfo::getStatus, 1)
                .ne(LicenseInfo::getLicenseStatus, 4) // 不包括已过期
                .le(LicenseInfo::getValidTo, thirtyDaysLater)
                .ge(LicenseInfo::getValidTo, today);
        
        return list(queryWrapper);
    }
    
    @Override
    public List<LicenseInfo> getExpiredLicenses() {
        LambdaQueryWrapper<LicenseInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LicenseInfo::getStatus, 1)
                .lt(LicenseInfo::getValidTo, LocalDate.now());
        
        return list(queryWrapper);
    }
    
    @Override
    public boolean updateLicenseStatus(Long id, Integer status) {
        LicenseInfo license = getById(id);
        if (license == null) {
            throw new RuntimeException("证照不存在");
        }
        
        license.setLicenseStatus(status);
        return updateById(license);
    }
}