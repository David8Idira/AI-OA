package com.aioa.asset.service;

import com.aioa.asset.entity.AssetLabel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 标签打印Service接口
 */
public interface LabelService extends IService<AssetLabel> {
    
    /**
     * 生成资产标签
     */
    AssetLabel generateLabel(Long assetId, Long templateId, String createBy);
    
    /**
     * 批量生成标签
     */
    List<AssetLabel> batchGenerateLabels(List<Long> assetIds, Long templateId, String createBy);
    
    /**
     * 打印标签
     */
    boolean printLabel(Long labelId, String printer, String printerId);
    
    /**
     * 批量打印标签
     */
    boolean batchPrintLabels(List<Long> labelIds, String printer, String printerId);
    
    /**
     * 分页查询标签
     */
    Page<AssetLabel> pageLabels(Page<AssetLabel> page, AssetLabel query);
    
    /**
     * 根据标签编码查询标签
     */
    AssetLabel getByLabelCode(String labelCode);
    
    /**
     * 获取标签打印历史
     */
    List<AssetLabel> getPrintHistory(Integer limit);
    
    /**
     * 获取标签打印统计
     */
    Map<String, Object> getPrintStatistics();
    
    /**
     * 更新打印模板配置
     */
    boolean updateTemplate(Long labelId, Long templateId, String templateName);
    
    /**
     * 作废标签
     */
    boolean invalidateLabel(Long labelId, String reason);
    
    /**
     * 重新生成二维码/条码
     */
    AssetLabel regenerateCode(Long labelId);
}