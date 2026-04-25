package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetLabelMapper;
import com.aioa.asset.service.LabelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 标签打印Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabelServiceImpl extends ServiceImpl<AssetLabelMapper, AssetLabel> implements LabelService {
    
    private final AssetInfoMapper assetInfoMapper;
    private final AssetLabelMapper assetLabelMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetLabel generateLabel(Long assetId, Long templateId, String createBy) {
        // 验证资产是否存在
        AssetInfo assetInfo = assetInfoMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 生成唯一标签编码
        String labelCode = generateLabelCode();
        
        // 创建标签
        AssetLabel label = new AssetLabel();
        label.setLabelCode(labelCode);
        label.setAssetId(assetId);
        label.setAssetCode(assetInfo.getAssetCode());
        label.setAssetName(assetInfo.getAssetName());
        label.setQrContent(generateQrContent(labelCode, assetInfo));
        label.setBarcodeContent(labelCode);
        label.setTemplateId(templateId);
        label.setTemplateName("默认模板");
        label.setPrintStatus(0);
        label.setPrintCount(0);
        label.setLabelStatus(1);
        label.setCreateBy(createBy);
        label.setCreateTime(LocalDateTime.now());
        label.setRemark("自动生成的标签");
        
        // 保存标签
        save(label);
        log.info("生成资产标签成功，标签编码：{}，资产ID：{}", labelCode, assetId);
        
        return label;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<AssetLabel> batchGenerateLabels(List<Long> assetIds, Long templateId, String createBy) {
        List<AssetLabel> labels = assetIds.stream()
            .map(assetId -> generateLabel(assetId, templateId, createBy))
            .toList();
        log.info("批量生成标签成功，数量：{}", labels.size());
        return labels;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean printLabel(Long labelId, String printer, String printerId) {
        AssetLabel label = getById(labelId);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        
        if (label.getLabelStatus() != 1) {
            throw new RuntimeException("标签状态异常，无法打印");
        }
        
        try {
            // TODO: 调用打印服务打印标签
            // 这里模拟打印成功
            
            // 更新标签打印状态
            label.setPrintStatus(1);
            label.setPrintCount(label.getPrintCount() + 1);
            label.setLastPrintTime(LocalDateTime.now());
            label.setPrintTime(LocalDateTime.now());
            label.setPrinter(printer);
            label.setPrinterId(printerId);
            label.setUpdateTime(LocalDateTime.now());
            
            updateById(label);
            log.info("打印标签成功，标签ID：{}，打印人：{}", labelId, printer);
            
            return true;
        } catch (Exception e) {
            log.error("打印标签失败，标签ID：{}", labelId, e);
            
            // 更新为打印失败状态
            label.setPrintStatus(2);
            label.setUpdateTime(LocalDateTime.now());
            updateById(label);
            
            return false;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchPrintLabels(List<Long> labelIds, String printer, String printerId) {
        boolean allSuccess = true;
        for (Long labelId : labelIds) {
            try {
                if (!printLabel(labelId, printer, printerId)) {
                    allSuccess = false;
                }
            } catch (Exception e) {
                log.error("批量打印标签失败，标签ID：{}", labelId, e);
                allSuccess = false;
            }
        }
        log.info("批量打印标签完成，成功：{}，总数：{}", 
                labelIds.size() - (allSuccess ? 0 : 1), labelIds.size());
        return allSuccess;
    }
    
    @Override
    public Page<AssetLabel> pageLabels(Page<AssetLabel> page, AssetLabel query) {
        LambdaQueryWrapper<AssetLabel> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            if (StringUtils.hasText(query.getLabelCode())) {
                queryWrapper.like(AssetLabel::getLabelCode, query.getLabelCode());
            }
            if (StringUtils.hasText(query.getAssetCode())) {
                queryWrapper.like(AssetLabel::getAssetCode, query.getAssetCode());
            }
            if (StringUtils.hasText(query.getAssetName())) {
                queryWrapper.like(AssetLabel::getAssetName, query.getAssetName());
            }
            if (query.getPrintStatus() != null) {
                queryWrapper.eq(AssetLabel::getPrintStatus, query.getPrintStatus());
            }
            if (query.getLabelStatus() != null) {
                queryWrapper.eq(AssetLabel::getLabelStatus, query.getLabelStatus());
            }
            if (query.getTemplateId() != null) {
                queryWrapper.eq(AssetLabel::getTemplateId, query.getTemplateId());
            }
        }
        
        queryWrapper.orderByDesc(AssetLabel::getCreateTime);
        return page(page, queryWrapper);
    }
    
    @Override
    public AssetLabel getByLabelCode(String labelCode) {
        return assetLabelMapper.selectByLabelCode(labelCode);
    }
    
    @Override
    public List<AssetLabel> getPrintHistory(Integer limit) {
        return assetLabelMapper.selectPrintHistory(limit != null ? limit : 50);
    }
    
    @Override
    public Map<String, Object> getPrintStatistics() {
        return assetLabelMapper.countPrintStatus();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(Long labelId, Long templateId, String templateName) {
        AssetLabel label = getById(labelId);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        
        label.setTemplateId(templateId);
        label.setTemplateName(templateName);
        label.setUpdateTime(LocalDateTime.now());
        
        return updateById(label);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invalidateLabel(Long labelId, String reason) {
        AssetLabel label = getById(labelId);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        
        label.setLabelStatus(2);
        label.setRemark(reason);
        label.setUpdateTime(LocalDateTime.now());
        
        return updateById(label);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AssetLabel regenerateCode(Long labelId) {
        AssetLabel label = getById(labelId);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        
        AssetInfo assetInfo = assetInfoMapper.selectById(label.getAssetId());
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 重新生成二维码内容
        label.setQrContent(generateQrContent(label.getLabelCode(), assetInfo));
        label.setUpdateTime(LocalDateTime.now());
        
        updateById(label);
        return label;
    }
    
    /**
     * 生成唯一标签编码
     */
    private String generateLabelCode() {
        // 格式：LABEL-年月日-随机码
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "LABEL-" + datePart + "-" + randomPart;
    }
    
    /**
     * 生成二维码内容
     */
    private String generateQrContent(String labelCode, AssetInfo assetInfo) {
        // 二维码包含标签编码和资产基本信息
        return String.format("标签编码：%s\n资产编码：%s\n资产名称：%s\n规格：%s\n生产厂商：%s",
                labelCode, assetInfo.getAssetCode(), assetInfo.getAssetName(),
                assetInfo.getSpecification(), assetInfo.getManufacturer());
    }
}