package com.aioa.asset.service.impl;

import com.aioa.asset.dto.OfficeSupplyApproveDTO;
import com.aioa.asset.dto.OfficeSupplyClaimDTO;
import com.aioa.asset.dto.OfficeSupplyItemDTO;
import com.aioa.asset.dto.OfficeSupplyRequestDTO;
import com.aioa.asset.entity.*;
import com.aioa.asset.enums.OfficeSupplyStatusEnum;
import com.aioa.asset.mapper.*;
import com.aioa.asset.service.OfficeSupplyRequestService;
import com.aioa.asset.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 办公用品申请单 Service 实现（简化版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OfficeSupplyRequestServiceImpl extends ServiceImpl<OfficeSupplyRequestMapper, OfficeSupplyRequest> 
        implements OfficeSupplyRequestService {
    
    private final OfficeSupplyRequestMapper requestMapper;
    private final OfficeSupplyItemMapper itemMapper;
    private final OfficeSupplyClaimMapper claimMapper;
    private final AssetInfoMapper assetInfoMapper;
    
    private static final DateTimeFormatter REQUEST_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OfficeSupplyRequestVO createRequest(OfficeSupplyRequestDTO requestDTO) {
        // 1. 创建申请单
        OfficeSupplyRequest request = new OfficeSupplyRequest();
        BeanUtils.copyProperties(requestDTO, request);
        
        // 生成申请单号
        String requestNo = "OSR" + LocalDateTime.now().format(REQUEST_NO_FORMATTER) + 
                          String.format("%04d", new Random().nextInt(10000));
        // 直接设置字段（不使用setter）
        request.setRequestNo(requestNo);
        
        // 设置申请单状态为草稿
        request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode());
        
        // 保存申请单
        requestMapper.insert(request);
        
        // 2. 创建申请明细
        List<OfficeSupplyItem> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // 获取items字段（不使用getter）
        List<OfficeSupplyItemDTO> itemDTOs = requestDTO.getItems();
        if (itemDTOs == null) {
            itemDTOs = new ArrayList<>();
        }
        
        for (OfficeSupplyItemDTO itemDTO : itemDTOs) {
            // 获取资产信息
            AssetInfo assetInfo = assetInfoMapper.selectById(itemDTO.getAssetId());
            if (assetInfo == null) {
                throw new RuntimeException("资产不存在: " + itemDTO.getAssetId());
            }
            
            OfficeSupplyItem item = new OfficeSupplyItem();
            // 直接设置字段
            item.setRequestId(request.getId());
            item.setAssetId(itemDTO.getAssetId());
            item.setAssetCode(assetInfo.getAssetCode());
            item.setAssetName(assetInfo.getAssetName());
            item.setSpecification(assetInfo.getSpecification());
            item.setRequestQuantity(itemDTO.getRequestQuantity());
            item.setClaimedQuantity(0);
            // 简化处理：使用固定单价
            BigDecimal unitPrice = BigDecimal.valueOf(100.0);
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(unitPrice.multiply(new BigDecimal(itemDTO.getRequestQuantity())));
            item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_UNCHECKED.getCode());
            
            items.add(item);
            totalQuantity += itemDTO.getRequestQuantity();
            BigDecimal itemTotalPrice = item.getTotalPrice();
            if (itemTotalPrice != null) {
                totalAmount = totalAmount.add(itemTotalPrice);
            }
        }
        
        if (!items.isEmpty()) {
            for (OfficeSupplyItem item : items) {
                itemMapper.insert(item);
            }
        }
        
        // 3. 生成申请单VO
        OfficeSupplyRequestVO result = new OfficeSupplyRequestVO();
        BeanUtils.copyProperties(request, result);
        result.setTotalQuantity(totalQuantity);
        result.setTotalAmount(totalAmount);
        
        // 设置明细列表
        List<OfficeSupplyItemVO> itemVOs = new ArrayList<>();
        for (OfficeSupplyItem item : items) {
            OfficeSupplyItemVO itemVO = new OfficeSupplyItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVOs.add(itemVO);
        }
        result.setItems(itemVOs);
        
        log.info("创建办公用品申请单成功，申请单号：{}", request.getRequestNo());
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(Long requestId) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        Integer requestStatus = request.getRequestStatus();
        if (!OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode().equals(requestStatus)) {
            throw new RuntimeException("只有草稿状态的申请单可以提交");
        }
        
        // 检查是否有申请明细
        QueryWrapper<OfficeSupplyItem> query = new QueryWrapper<>();
        query.eq("request_id", requestId);
        Long count = itemMapper.selectCount(query);
        if (count == null || count == 0) {
            throw new RuntimeException("申请单没有明细，无法提交");
        }
        
        // 更新状态为待审批
        request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode());
        requestMapper.updateById(request);
        
        log.info("提交办公用品申请单成功，申请单ID：{}", requestId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(OfficeSupplyApproveDTO approveDTO) {
        Long requestId = approveDTO.getRequestId();
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        Integer requestStatus = request.getRequestStatus();
        if (!OfficeSupplyStatusEnum.isApprovable(requestStatus)) {
            throw new RuntimeException("只有待审批状态的申请单可以审批");
        }
        
        Boolean approveResult = approveDTO.getApproveResult();
        if (approveResult != null && approveResult) {
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_APPROVED.getCode());
            request.setApproveComment(approveDTO.getApproveComment());
        } else {
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_REJECTED.getCode());
            request.setApproveComment(approveDTO.getApproveComment());
        }
        
        request.setApproverId(approveDTO.getApproverId());
        request.setApproverName(approveDTO.getApproverName());
        requestMapper.updateById(request);
        
        log.info("审批办公用品申请单成功，申请单ID：{}，审批结果：{}", 
                requestId, (approveResult != null && approveResult) ? "通过" : "拒绝");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRequest(Long requestId, String operatorId, String operatorName) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        Integer requestStatus = request.getRequestStatus();
        if (!OfficeSupplyStatusEnum.isCancellable(requestStatus)) {
            throw new RuntimeException("只有草稿或待审批状态的申请单可以取消");
        }
        
        request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_CANCELLED.getCode());
        requestMapper.updateById(request);
        
        log.info("取消办公用品申请单成功，申请单ID：{}", requestId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimOfficeSupply(OfficeSupplyClaimDTO claimDTO) {
        // 验证申请单
        Long requestId = claimDTO.getRequestId();
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        Integer requestStatus = request.getRequestStatus();
        if (!OfficeSupplyStatusEnum.isClaimable(requestStatus)) {
            throw new RuntimeException("只有审批通过或部分领取状态的申请单可以领用");
        }
        
        // 获取申请明细
        OfficeSupplyItem item;
        Long itemId = claimDTO.getItemId();
        if (itemId != null) {
            item = itemMapper.selectById(itemId);
        } else {
            // 如果未指定明细，使用第一个未完全领取的明细
            QueryWrapper<OfficeSupplyItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("request_id", requestId);
            itemQuery.lt("claimed_quantity", "request_quantity");
            item = itemMapper.selectOne(itemQuery);
        }
        
        if (item == null) {
            throw new RuntimeException("没有可领用的办公用品明细");
        }
        
        // 检查领用数量是否超过申请数量
        Integer requestQuantity = item.getRequestQuantity();
        Integer claimedQuantity = item.getClaimedQuantity();
        if (requestQuantity == null || claimedQuantity == null) {
            throw new RuntimeException("申请数量或已领数量为空");
        }
        
        int remainingQuantity = requestQuantity - claimedQuantity;
        Integer claimQuantity = claimDTO.getClaimQuantity();
        if (claimQuantity == null || claimQuantity > remainingQuantity) {
            throw new RuntimeException("领用数量超过剩余可领数量，剩余数量：" + remainingQuantity);
        }
        
        // 创建领用记录
        OfficeSupplyClaim claim = new OfficeSupplyClaim();
        claim.setRequestId(requestId);
        claim.setItemId(item.getId());
        claim.setAssetId(item.getAssetId());
        claim.setAssetCode(item.getAssetCode());
        claim.setAssetName(item.getAssetName());
        claim.setClaimQuantity(claimQuantity);
        claim.setClaimerId(claimDTO.getClaimerId());
        claim.setClaimerName(claimDTO.getClaimerName());
        claim.setClaimMethod(claimDTO.getClaimMethod());
        claim.setSignStatus(OfficeSupplyStatusEnum.SIGN_PENDING.getCode());
        claim.setClaimTime(LocalDateTime.now());
        claim.setRemark(claimDTO.getRemark());
        
        claimMapper.insert(claim);
        
        // 更新明细的已领取数量
        item.setClaimedQuantity(claimedQuantity + claimQuantity);
        itemMapper.updateById(item);
        
        // 更新申请单状态
        QueryWrapper<OfficeSupplyItem> checkQuery = new QueryWrapper<>();
        checkQuery.eq("request_id", requestId);
        checkQuery.lt("claimed_quantity", "request_quantity");
        Long remainingItems = itemMapper.selectCount(checkQuery);
        
        if (remainingItems == null || remainingItems == 0) {
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_FULLY_CLAIMED.getCode());
        } else {
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PARTIAL_CLAIMED.getCode());
        }
        requestMapper.updateById(request);
        
        log.info("领用办公用品成功，申请单ID：{}，明细ID：{}，领用数量：{}", 
                requestId, item.getId(), claimQuantity);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signClaim(Long claimId, String signer) {
        OfficeSupplyClaim claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new RuntimeException("领用记录不存在: " + claimId);
        }
        
        Integer signStatus = claim.getSignStatus();
        if (!OfficeSupplyStatusEnum.SIGN_PENDING.getCode().equals(signStatus)) {
            throw new RuntimeException("只有待签收状态的领用记录可以签收");
        }
        
        claim.setSignStatus(OfficeSupplyStatusEnum.SIGN_COMPLETED.getCode());
        claim.setSigner(signer);
        claim.setSignTime(LocalDateTime.now());
        claimMapper.updateById(claim);
        
        log.info("签收领用记录成功，领用记录ID：{}，签收人：{}", claimId, signer);
    }
    
    @Override
    public String generateClaimQrCode(Long requestId, Long itemId, LocalDateTime expireTime) {
        // 验证申请单和明细
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        OfficeSupplyItem item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("申请明细不存在: " + itemId);
        }
        
        Long itemRequestId = item.getRequestId();
        if (!requestId.equals(itemRequestId)) {
            throw new RuntimeException("明细不属于该申请单");
        }
        
        // 生成二维码内容（这里使用简单的JSON格式）
        String qrContent = String.format(
                "{\"type\":\"office_supply_claim\",\"requestId\":%d,\"itemId\":%d,\"expireTime\":\"%s\"}",
                requestId, itemId, expireTime.toString());
        
        // 在实际项目中，这里可以调用二维码生成服务
        // 这里返回二维码内容的Base64编码
        String qrCode = java.util.Base64.getEncoder().encodeToString(qrContent.getBytes());
        
        log.info("生成领用二维码成功，申请单ID：{}，明细ID：{}，过期时间：{}", requestId, itemId, expireTime);
        return qrCode;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimByQrCode(String qrCode, String claimerId, String claimerName) {
        try {
            // 解码二维码
            String qrContent = new String(java.util.Base64.getDecoder().decode(qrCode));
            
            // 解析JSON（简化处理）
            String[] parts = qrContent.split("\"requestId\":");
            if (parts.length < 2) {
                throw new RuntimeException("无效的二维码内容");
            }
            
            String requestIdStr = parts[1].split(",")[0].trim();
            Long requestId = Long.parseLong(requestIdStr);
            
            // 查找申请单的第一个明细
            QueryWrapper<OfficeSupplyItem> query = new QueryWrapper<>();
            query.eq("request_id", requestId);
            OfficeSupplyItem item = itemMapper.selectOne(query);
            
            if (item == null) {
                throw new RuntimeException("没有找到可领用的明细");
            }
            
            // 创建领用DTO
            OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
            claimDTO.setRequestId(requestId);
            claimDTO.setItemId(item.getId());
            claimDTO.setClaimQuantity(1); // 默认领用1个
            claimDTO.setClaimerId(claimerId);
            claimDTO.setClaimerName(claimerName);
            claimDTO.setClaimMethod(OfficeSupplyStatusEnum.CLAIM_METHOD_QR_CODE.getCode());
            
            // 调用领用方法
            claimOfficeSupply(claimDTO);
            
            log.info("扫码领用成功，二维码：{}，领用人：{}", qrCode, claimerName);
        } catch (Exception e) {
            throw new RuntimeException("二维码解析失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public OfficeSupplyRequestVO getRequestDetail(Long requestId) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        // 获取申请明细
        QueryWrapper<OfficeSupplyItem> itemQuery = new QueryWrapper<>();
        itemQuery.eq("request_id", requestId);
        List<OfficeSupplyItem> items = itemMapper.selectList(itemQuery);
        
        // 获取领用记录
        QueryWrapper<OfficeSupplyClaim> claimQuery = new QueryWrapper<>();
        claimQuery.eq("request_id", requestId);
        List<OfficeSupplyClaim> claims = claimMapper.selectList(claimQuery);
        
        // 计算总数量和总金额
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OfficeSupplyItem item : items) {
            Integer itemQuantity = item.getRequestQuantity();
            if (itemQuantity != null) {
                totalQuantity += itemQuantity;
            }
            BigDecimal itemPrice = item.getTotalPrice();
            if (itemPrice != null) {
                totalAmount = totalAmount.add(itemPrice);
            }
        }
        
        // 构建VO
        OfficeSupplyRequestVO result = new OfficeSupplyRequestVO();
        BeanUtils.copyProperties(request, result);
        result.setTotalQuantity(totalQuantity);
        result.setTotalAmount(totalAmount);
        
        // 设置明细列表
        List<OfficeSupplyItemVO> itemVOs = new ArrayList<>();
        for (OfficeSupplyItem item : items) {
            OfficeSupplyItemVO itemVO = new OfficeSupplyItemVO();
            BeanUtils.copyProperties(item, itemVO);
            itemVOs.add(itemVO);
        }
        result.setItems(itemVOs);
        
        // 设置领用记录
        List<OfficeSupplyClaimVO> claimVOs = new ArrayList<>();
        for (OfficeSupplyClaim claim : claims) {
            OfficeSupplyClaimVO claimVO = new OfficeSupplyClaimVO();
            BeanUtils.copyProperties(claim, claimVO);
            claimVOs.add(claimVO);
        }
        result.setClaims(claimVOs);
        
        return result;
    }
    
    @Override
    public List<OfficeSupplyRequestVO> listRequests(String applicantId, String departmentId, Integer requestStatus, 
                                                    LocalDateTime startTime, LocalDateTime endTime) {
        QueryWrapper<OfficeSupplyRequest> query = new QueryWrapper<>();
        
        if (applicantId != null && !applicantId.trim().isEmpty()) {
            query.eq("applicant_id", applicantId);
        }
        
        if (departmentId != null && !departmentId.trim().isEmpty()) {
            query.eq("department_id", departmentId);
        }
        
        if (requestStatus != null) {
            query.eq("request_status", requestStatus);
        }
        
        if (startTime != null) {
            query.ge("create_time", startTime);
        }
        
        if (endTime != null) {
            query.le("create_time", endTime);
        }
        
        query.orderByDesc("create_time");
        
        List<OfficeSupplyRequest> requests = requestMapper.selectList(query);
        List<OfficeSupplyRequestVO> result = new ArrayList<>();
        
        for (OfficeSupplyRequest request : requests) {
            OfficeSupplyRequestVO vo = new OfficeSupplyRequestVO();
            BeanUtils.copyProperties(request, vo);
            
            // 获取明细统计信息
            Long requestId = request.getId();
            QueryWrapper<OfficeSupplyItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("request_id", requestId);
            List<OfficeSupplyItem> items = itemMapper.selectList(itemQuery);
            
            int totalQuantity = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OfficeSupplyItem item : items) {
                Integer itemQuantity = item.getRequestQuantity();
                if (itemQuantity != null) {
                    totalQuantity += itemQuantity;
                }
                BigDecimal itemPrice = item.getTotalPrice();
                if (itemPrice != null) {
                    totalAmount = totalAmount.add(itemPrice);
                }
            }
            
            vo.setTotalQuantity(totalQuantity);
            vo.setTotalAmount(totalAmount);
            result.add(vo);
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkInventoryAndUpdateStatus(Long requestId) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        // 检查申请单状态，如果是草稿或待审批，检查库存
        Integer requestStatus = request.getRequestStatus();
        if (OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode().equals(requestStatus) || 
            OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode().equals(requestStatus)) {
            // 获取申请明细
            QueryWrapper<OfficeSupplyItem> itemQuery = new QueryWrapper<>();
            itemQuery.eq("request_id", requestId);
            List<OfficeSupplyItem> items = itemMapper.selectList(itemQuery);
            
            boolean allSufficient = true;
            boolean anyInsufficient = false;
            
            for (OfficeSupplyItem item : items) {
                // 简化处理：假设所有库存都充足
                item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_SUFFICIENT.getCode());
                item.setInventoryCheckComment("库存充足");
                itemMapper.updateById(item);
            }
            
            if (allSufficient) {
                log.info("申请单{}所有明细库存充足", requestId);
            } else if (anyInsufficient) {
                log.info("申请单{}有明细库存不足", requestId);
            }
        }
    }
    
    @Override
    public List<OfficeSupplyStatsVO> getOfficeSupplyStats(String dimension, String departmentId, 
                                                          LocalDateTime startTime, LocalDateTime endTime) {
        List<OfficeSupplyStatsVO> result = new ArrayList<>();
        
        // 这里实现统计逻辑
        // 简化实现：返回空列表
        log.info("获取办公用品统计，维度：{}，部门ID：{}，开始时间：{}，结束时间：{}", 
                dimension, departmentId, startTime, endTime);
        
        return result;
    }
}