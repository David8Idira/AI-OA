package com.aioa.asset.service;

import com.aioa.asset.dto.OfficeSupplyApproveDTO;
import com.aioa.asset.dto.OfficeSupplyClaimDTO;
import com.aioa.asset.dto.OfficeSupplyRequestDTO;
import com.aioa.asset.entity.OfficeSupplyRequest;
import com.aioa.asset.vo.OfficeSupplyRequestVO;
import com.aioa.asset.vo.OfficeSupplyStatsVO;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 办公用品申请单 Service
 */
public interface OfficeSupplyRequestService extends IService<OfficeSupplyRequest> {
    
    /**
     * 创建办公用品申请
     */
    OfficeSupplyRequestVO createRequest(OfficeSupplyRequestDTO requestDTO);
    
    /**
     * 提交申请（草稿→待审批）
     */
    void submitRequest(Long requestId);
    
    /**
     * 审批申请
     */
    void approveRequest(OfficeSupplyApproveDTO approveDTO);
    
    /**
     * 取消申请
     */
    void cancelRequest(Long requestId, String operatorId, String operatorName);
    
    /**
     * 领用办公用品
     */
    void claimOfficeSupply(OfficeSupplyClaimDTO claimDTO);
    
    /**
     * 签收领用记录
     */
    void signClaim(Long claimId, String signer);
    
    /**
     * 生成领用二维码
     */
    String generateClaimQrCode(Long requestId, Long itemId, LocalDateTime expireTime);
    
    /**
     * 扫码领用（通过二维码）
     */
    void claimByQrCode(String qrCode, String claimerId, String claimerName);
    
    /**
     * 获取申请单详情
     */
    OfficeSupplyRequestVO getRequestDetail(Long requestId);
    
    /**
     * 查询申请单列表
     */
    List<OfficeSupplyRequestVO> listRequests(String applicantId, String departmentId, 
                                           Integer requestStatus, LocalDateTime startTime, 
                                           LocalDateTime endTime);
    
    /**
     * 检查库存并更新申请单状态
     */
    void checkInventoryAndUpdateStatus(Long requestId);
    
    /**
     * 获取办公用品统计报表
     */
    List<OfficeSupplyStatsVO> getOfficeSupplyStats(String dimension, String departmentId, 
                                                 LocalDateTime startTime, LocalDateTime endTime);
}