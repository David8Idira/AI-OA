package com.aioa.asset.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 办公用品统计报表 VO
 */
@Data
public class OfficeSupplyStatsVO {
    
    /**
     * 统计维度
     */
    private String dimension;
    
    /**
     * 维度值
     */
    private String dimensionValue;
    
    /**
     * 申请总数量
     */
    private Integer totalRequestQuantity;
    
    /**
     * 已领取数量
     */
    private Integer totalClaimedQuantity;
    
    /**
     * 待领取数量
     */
    private Integer totalPendingQuantity;
    
    /**
     * 申请总金额
     */
    private BigDecimal totalRequestAmount;
    
    /**
     * 已领取金额
     */
    private BigDecimal totalClaimedAmount;
    
    /**
     * 申请单总数
     */
    private Integer totalRequestCount;
    
    /**
     * 已完成申请单数
     */
    private Integer completedRequestCount;
    
    /**
     * 领用记录数
     */
    private Integer claimRecordCount;
    
    /**
     * 按资产分类的统计
     */
    private List<OfficeSupplyAssetStats> assetStats;
    
    @Data
    public static class OfficeSupplyAssetStats {
        
        /**
         * 资产ID
         */
        private Long assetId;
        
        /**
         * 资产编码
         */
        private String assetCode;
        
        /**
         * 资产名称
         */
        private String assetName;
        
        /**
         * 申请数量
         */
        private Integer requestQuantity;
        
        /**
         * 已领取数量
         */
        private Integer claimedQuantity;
        
        /**
         * 申请金额
         */
        private BigDecimal requestAmount;
        
        /**
         * 已领取金额
         */
        private BigDecimal claimedAmount;
    }
}