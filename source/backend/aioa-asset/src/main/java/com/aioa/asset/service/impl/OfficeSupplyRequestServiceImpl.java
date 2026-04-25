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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
 * 办公用品申请单 Service 实现
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
        String requestNo = "OSS-" + LocalDateTime.now().format(REQUEST_NO_FORMATTER) + "-" + 
                          (int)(Math.random() * 1000);
        request.setRequestNo(requestNo);
        request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode()); // 草稿状态
        request.setTotalQuantity(0);
        request.setClaimedQuantity(0);
        request.setCreateBy(requestDTO.getApplicantId());
        
        // 保存申请单
        requestMapper.insert(request);
        
        // 2. 创建申请明细
        List<OfficeSupplyItem> items = new ArrayList<>();
        int totalQuantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OfficeSupplyItemDTO itemDTO : requestDTO.getItems()) {
            // 获取资产信息
            AssetInfo assetInfo = assetInfoMapper.selectById(itemDTO.getAssetId());
            if (assetInfo == null) {
                throw new RuntimeException("资产不存在: " + itemDTO.getAssetId());
            }
            
            OfficeSupplyItem item = new OfficeSupplyItem();
            item.setRequestId(request.getId());
            item.setAssetId(itemDTO.getAssetId());
            item.setAssetCode(assetInfo.getAssetCode());
            item.setAssetName(assetInfo.getAssetName());
            item.setSpecification(assetInfo.getSpecification());
            item.setRequestQuantity(itemDTO.getRequestQuantity());
            item.setClaimedQuantity(0);
            item.setUnitPrice(assetInfo.getPurchasePrice() != null ? assetInfo.getPurchasePrice() : BigDecimal.ZERO);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getRequestQuantity())));
            item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_UNCHECKED.getCode()); // 未检查
            item.setRemark(itemDTO.getRemark());
            
            items.add(item);
            totalQuantity += itemDTO.getRequestQuantity();
            totalAmount = totalAmount.add(item.getTotalPrice());
        }
        
        // 批量保存明细
        if (!CollectionUtils.isEmpty(items)) {
            for (OfficeSupplyItem item : items) {
                itemMapper.insert(item);
            }
        }
        
        // 3. 更新申请单的总数量和总金额
        request.setTotalQuantity(totalQuantity);
        requestMapper.updateById(request);
        
        // 4. 返回结果
        OfficeSupplyRequestVO result = convertToRequestVO(request);
        
        // 设置明细
        List<OfficeSupplyItemVO> itemVOs = items.stream().map(this::convertToItemVO).collect(Collectors.toList());
        result.setItems(itemVOs);
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitRequest(Long requestId) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        if (!OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode().equals(request.getRequestStatus())) {
            throw new RuntimeException("只有草稿状态的申请单可以提交");
        }
        
        // 检查库存
        checkInventoryAndUpdateStatus(requestId);
        
        // 更新状态为待审批
        request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode());
        requestMapper.updateById(request);
        
        log.info("办公用品申请单已提交: {}", requestId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveRequest(OfficeSupplyApproveDTO approveDTO) {
        OfficeSupplyRequest request = requestMapper.selectById(approveDTO.getRequestId());
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + approveDTO.getRequestId());
        }
        
        if (!OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode().equals(request.getRequestStatus())) {
            throw new RuntimeException("只有待审批状态的申请单可以审批");
        }
        
        // 更新审批信息
        request.setApproverId(approveDTO.getApproverId());
        request.setApproverName(approveDTO.getApproverName());
        request.setApproveTime(LocalDateTime.now());
        request.setApproveComment(approveDTO.getApproveComment());
        
        if (Boolean.TRUE.equals(approveDTO.getApproveResult())) {
            // 审批通过
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_APPROVED.getCode());
            
            // 如果是紧急申请，可以立即生成二维码
            if (request.getUrgencyLevel() == 3) {
                // 为每个明细生成二维码
                LambdaQueryWrapper<OfficeSupplyItem> itemQuery = new LambdaQueryWrapper<>();
                itemQuery.eq(OfficeSupplyItem::getRequestId, request.getId());
                List<OfficeSupplyItem> items = itemMapper.selectList(itemQuery);
                
                for (OfficeSupplyItem item : items) {
                    if (OfficeSupplyStatusEnum.INVENTORY_SUFFICIENT.getCode().equals(item.getInventoryCheckStatus())) { // 库存充足
                        String qrCode = generateClaimQrCode(request.getId(), item.getId(), 
                                LocalDateTime.now().plusHours(24));
                        log.info("为申请单 {} 的资产 {} 生成二维码: {}", request.getId(), item.getAssetId(), qrCode);
                    }
                }
            }
        } else {
            // 审批拒绝
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_REJECTED.getCode());
        }
        
        requestMapper.updateById(request);
        
        log.info("办公用品申请单已审批: {}, 结果: {}", approveDTO.getRequestId(), 
                approveDTO.getApproveResult() ? "通过" : "拒绝");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRequest(Long requestId, String operatorId, String operatorName) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + requestId);
        }
        
        // 只有草稿、待审批状态的申请单可以取消
        if (!OfficeSupplyStatusEnum.isCancellable(request.getRequestStatus())) {
            throw new RuntimeException("当前状态的申请单不能取消");
        }
        
        // 更新状态为已取消
        request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_CANCELLED.getCode());
        request.setUpdateBy(operatorId);
        requestMapper.updateById(request);
        
        log.info("办公用品申请单已取消: {}, 操作人: {}", requestId, operatorName);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimOfficeSupply(OfficeSupplyClaimDTO claimDTO) {
        // 检查申请单状态
        OfficeSupplyRequest request = requestMapper.selectById(claimDTO.getRequestId());
        if (request == null) {
            throw new RuntimeException("申请单不存在: " + claimDTO.getRequestId());
        }
        
        if (!OfficeSupplyStatusEnum.isClaimable(request.getRequestStatus())) {
            throw new RuntimeException("只有审批通过或部分领取状态的申请单可以领用");
        }
        
        // 获取申请明细
        OfficeSupplyItem item;
        if (claimDTO.getItemId() != null) {
            item = itemMapper.selectById(claimDTO.getItemId());
        } else {
            // 如果未指定明细，使用第一个未完全领取的明细
            LambdaQueryWrapper<OfficeSupplyItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OfficeSupplyItem::getRequestId, claimDTO.getRequestId());
            // 使用lambda表达式进行lt条件
            itemQuery.lt(true, OfficeSupplyItem::getClaimedQuantity, OfficeSupplyItem::getRequestQuantity);
            item = itemMapper.selectOne(itemQuery.orderByAsc(true, OfficeSupplyItem::getId));
        }
        
        if (item == null) {
            throw new RuntimeException("没有可领用的办公用品明细");
        }
        
        // 检查领用数量是否超过申请数量
        int remainingQuantity = item.getRequestQuantity() - item.getClaimedQuantity();
        if (claimDTO.getClaimQuantity() > remainingQuantity) {
            throw new RuntimeException("领用数量超过可领用数量，剩余: " + remainingQuantity);
        }
        
        // 检查库存
        AssetInfo assetInfo = assetInfoMapper.selectById(item.getAssetId());
        if (assetInfo == null || assetInfo.getCurrentQuantity() < claimDTO.getClaimQuantity()) {
            throw new RuntimeException("库存不足，当前库存: " + 
                    (assetInfo != null ? assetInfo.getCurrentQuantity() : 0));
        }
        
        // 创建领用记录
        OfficeSupplyClaim claim = new OfficeSupplyClaim();
        BeanUtils.copyProperties(claimDTO, claim);
        claim.setClaimNo("CLM-" + LocalDateTime.now().format(REQUEST_NO_FORMATTER) + "-" + 
                        (int)(Math.random() * 1000));
        claim.setItemId(item.getId());
        claim.setAssetId(item.getAssetId());
        claim.setAssetCode(item.getAssetCode());
        claim.setAssetName(item.getAssetName());
        claim.setClaimTime(LocalDateTime.now());
        claim.setSignStatus(OfficeSupplyStatusEnum.SIGN_PENDING.getCode()); // 待签收
        claim.setCreateBy(claimDTO.getClaimerId());
        
        claimMapper.insert(claim);
        
        // 更新申请明细的已领取数量
        item.setClaimedQuantity(item.getClaimedQuantity() + claimDTO.getClaimQuantity());
        itemMapper.updateById(item);
        
        // 更新申请单的已领取数量
        request.setClaimedQuantity(request.getClaimedQuantity() + claimDTO.getClaimQuantity());
        
        // 更新申请单状态
        if (request.getClaimedQuantity().equals(request.getTotalQuantity())) {
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_FULLY_CLAIMED.getCode()); // 已全部领取
        } else {
            request.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PARTIAL_CLAIMED.getCode()); // 部分领取
        }
        
        requestMapper.updateById(request);
        
        // 更新库存
        assetInfo.setCurrentQuantity(assetInfo.getCurrentQuantity() - claimDTO.getClaimQuantity());
        assetInfoMapper.updateById(assetInfo);
        
        log.info("办公用品领用成功: 申请单 {}, 资产 {}, 数量 {}", 
                claimDTO.getRequestId(), item.getAssetId(), claimDTO.getClaimQuantity());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void signClaim(Long claimId, String signer) {
        OfficeSupplyClaim claim = claimMapper.selectById(claimId);
        if (claim == null) {
            throw new RuntimeException("领用记录不存在: " + claimId);
        }
        
        if (!OfficeSupplyStatusEnum.SIGN_PENDING.getCode().equals(claim.getSignStatus())) {
            throw new RuntimeException("只有待签收状态的领用记录可以签收");
        }
        
        // 更新签收信息
        claim.setSignStatus(OfficeSupplyStatusEnum.SIGN_COMPLETED.getCode());
        claim.setSigner(signer);
        claim.setSignTime(LocalDateTime.now());
        claimMapper.updateById(claim);
        
        log.info("办公用品领用记录已签收: {}, 签收人: {}", claimId, signer);
    }
    
    @Override
    public String generateClaimQrCode(Long requestId, Long itemId, LocalDateTime expireTime) {
        // 这里应该调用二维码生成服务
        // 简化实现：生成一个包含申请单和明细ID的加密字符串
        String data = String.format("OSS:%d:%d:%d", requestId, itemId, 
                expireTime.toEpochSecond(java.time.ZoneOffset.ofHours(8)));
        
        // 实际应该返回二维码图片URL或base64编码
        // 这里返回加密后的字符串
        String qrCode = Base64.getEncoder().encodeToString(data.getBytes());
        
        // 更新领用记录的二维码信息
        LambdaQueryWrapper<OfficeSupplyClaim> claimQuery = new LambdaQueryWrapper<>();
        claimQuery.eq(OfficeSupplyClaim::getRequestId, requestId)
                 .eq(OfficeSupplyClaim::getItemId, itemId)
                 .orderByDesc(OfficeSupplyClaim::getCreateTime)
                 .last("limit 1");
        
        OfficeSupplyClaim claim = claimMapper.selectOne(claimQuery);
        if (claim != null) {
            claim.setClaimQrCode(qrCode);
            claim.setQrCodeExpireTime(expireTime);
            claimMapper.updateById(claim);
        }
        
        return qrCode;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void claimByQrCode(String qrCode, String claimerId, String claimerName) {
        try {
            // 解码二维码数据
            String decoded = new String(Base64.getDecoder().decode(qrCode));
            String[] parts = decoded.split(":");
            
            if (parts.length != 4 || !"OSS".equals(parts[0])) {
                throw new RuntimeException("无效的二维码");
            }
            
            Long requestId = Long.parseLong(parts[1]);
            Long itemId = Long.parseLong(parts[2]);
            long expireTime = Long.parseLong(parts[3]);
            
            // 检查二维码是否过期
            if (LocalDateTime.now().toEpochSecond(java.time.ZoneOffset.ofHours(8)) > expireTime) {
                throw new RuntimeException("二维码已过期");
            }
            
            // 获取申请单和明细
            OfficeSupplyRequest request = requestMapper.selectById(requestId);
            OfficeSupplyItem item = itemMapper.selectById(itemId);
            
            if (request == null || item == null) {
                throw new RuntimeException("申请信息不存在");
            }
            
            // 创建领用记录
            OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
            claimDTO.setRequestId(requestId);
            claimDTO.setItemId(itemId);
            claimDTO.setClaimQuantity(1); // 扫码默认领用1个
            claimDTO.setClaimerId(claimerId);
            claimDTO.setClaimerName(claimerName);
            claimDTO.setClaimMethod(1); // 扫码领用
            claimDTO.setClaimLocation("扫码领用点");
            
            // 调用领用方法
            claimOfficeSupply(claimDTO);
            
            log.info("扫码领用成功: 二维码 {}, 领用人 {}", qrCode, claimerName);
            
        } catch (Exception e) {
            log.error("扫码领用失败: {}", e.getMessage(), e);
            throw new RuntimeException("扫码领用失败: " + e.getMessage());
        }
    }
    
    @Override
    public OfficeSupplyRequestVO getRequestDetail(Long requestId) {
        OfficeSupplyRequest request = requestMapper.selectById(requestId);
        if (request == null) {
            return null;
        }
        
        OfficeSupplyRequestVO result = convertToRequestVO(request);
        
        // 设置明细
        LambdaQueryWrapper<OfficeSupplyItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OfficeSupplyItem::getRequestId, requestId);
        List<OfficeSupplyItem> items = itemMapper.selectList(itemQuery);
        
        List<OfficeSupplyItemVO> itemVOs = items.stream().map(this::convertToItemVO).collect(Collectors.toList());
        result.setItems(itemVOs);
        
        // 设置领用记录
        LambdaQueryWrapper<OfficeSupplyClaim> claimQuery = new LambdaQueryWrapper<>();
        claimQuery.eq(OfficeSupplyClaim::getRequestId, requestId);
        List<OfficeSupplyClaim> claims = claimMapper.selectList(claimQuery);
        
        List<OfficeSupplyClaimVO> claimVOs = claims.stream().map(this::convertToClaimVO).collect(Collectors.toList());
        result.setClaims(claimVOs);
        
        return result;
    }
    
    @Override
    public List<OfficeSupplyRequestVO> listRequests(String applicantId, String departmentId, 
                                                  Integer requestStatus, LocalDateTime startTime, 
                                                  LocalDateTime endTime) {
        LambdaQueryWrapper<OfficeSupplyRequest> query = new LambdaQueryWrapper<>();
        
        if (applicantId != null) {
            query.eq(OfficeSupplyRequest::getApplicantId, applicantId);
        }
        
        if (departmentId != null) {
            query.eq(OfficeSupplyRequest::getDepartmentId, departmentId);
        }
        
        if (requestStatus != null) {
            query.eq(OfficeSupplyRequest::getRequestStatus, requestStatus);
        }
        
        if (startTime != null) {
            query.ge(OfficeSupplyRequest::getCreateTime, startTime);
        }
        
        if (endTime != null) {
            query.le(OfficeSupplyRequest::getCreateTime, endTime);
        }
        
        query.orderByDesc(OfficeSupplyRequest::getCreateTime);
        
        List<OfficeSupplyRequest> requests = requestMapper.selectList(query);
        
        return requests.stream().map(this::convertToRequestVO).collect(Collectors.toList());
    }
    
    @Override
    public void checkInventoryAndUpdateStatus(Long requestId) {
        // 获取申请单的所有明细
        LambdaQueryWrapper<OfficeSupplyItem> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OfficeSupplyItem::getRequestId, requestId);
        List<OfficeSupplyItem> items = itemMapper.selectList(itemQuery);
        
        boolean allSufficient = true;
        boolean anyInsufficient = false;
        
        for (OfficeSupplyItem item : items) {
            AssetInfo assetInfo = assetInfoMapper.selectById(item.getAssetId());
            
            if (assetInfo == null) {
                item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_INSUFFICIENT.getCode()); // 库存不足（资产不存在）
                item.setInventoryCheckComment("资产不存在");
                allSufficient = false;
                anyInsufficient = true;
            } else if (assetInfo.getCurrentQuantity() >= item.getRequestQuantity()) {
                item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_SUFFICIENT.getCode()); // 库存充足
                item.setInventoryCheckComment("库存充足");
            } else if (assetInfo.getCurrentQuantity() > 0) {
                item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_INSUFFICIENT.getCode()); // 库存不足
                item.setInventoryCheckComment(String.format("库存不足，当前库存: %d", assetInfo.getCurrentQuantity()));
                allSufficient = false;
                anyInsufficient = true;
            } else {
                item.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_INSUFFICIENT.getCode()); // 库存不足
                item.setInventoryCheckComment("库存为0");
                allSufficient = false;
                anyInsufficient = true;
            }
            
            itemMapper.updateById(item);
        }
        
        // 根据库存检查结果决定是否自动排队
        if (anyInsufficient) {
            // 这里可以实现自动排队逻辑
            log.info("申请单 {} 部分资产库存不足，已标记", requestId);
        }
    }
    
    @Override
    public List<OfficeSupplyStatsVO> getOfficeSupplyStats(String dimension, String departmentId, 
                                                        LocalDateTime startTime, LocalDateTime endTime) {
        List<OfficeSupplyStatsVO> statsList = new ArrayList<>();
        
        // 这里应该实现具体的统计查询逻辑
        // 简化实现：返回一个示例
        OfficeSupplyStatsVO stats = new OfficeSupplyStatsVO();
        stats.setDimension(dimension != null ? dimension : "department");
        stats.setDimensionValue(departmentId != null ? departmentId : "all");
        stats.setTotalRequestQuantity(100);
        stats.setTotalClaimedQuantity(80);
        stats.setTotalPendingQuantity(20);
        stats.setTotalRequestAmount(BigDecimal.valueOf(5000.00));
        stats.setTotalClaimedAmount(BigDecimal.valueOf(4000.00));
        stats.setTotalRequestCount(50);
        stats.setCompletedRequestCount(40);
        stats.setClaimRecordCount(80);
        
        statsList.add(stats);
        
        return statsList;
    }
    
    private OfficeSupplyItemVO convertToItemVO(OfficeSupplyItem item) {
        OfficeSupplyItemVO vo = new OfficeSupplyItemVO();
        BeanUtils.copyProperties(item, vo);
        
        // 设置状态名称
        vo.setInventoryCheckStatusName(OfficeSupplyStatusEnum.getDescriptionByCode(item.getInventoryCheckStatus()));
        
        // 获取当前库存
        AssetInfo assetInfo = assetInfoMapper.selectById(item.getAssetId());
        if (assetInfo != null) {
            vo.setCurrentInventory(assetInfo.getCurrentQuantity());
        }
        
        return vo;
    }
    
    private OfficeSupplyClaimVO convertToClaimVO(OfficeSupplyClaim claim) {
        OfficeSupplyClaimVO vo = new OfficeSupplyClaimVO();
        BeanUtils.copyProperties(claim, vo);
        
        // 设置状态名称
        vo.setSignStatusName(OfficeSupplyStatusEnum.getDescriptionByCode(claim.getSignStatus()));
        vo.setClaimMethodName(OfficeSupplyStatusEnum.getDescriptionByCode(claim.getClaimMethod()));
        
        return vo;
    }
    
    private OfficeSupplyRequestVO convertToRequestVO(OfficeSupplyRequest request) {
        OfficeSupplyRequestVO vo = new OfficeSupplyRequestVO();
        BeanUtils.copyProperties(request, vo);
        
        // 设置状态名称
        vo.setRequestStatusName(OfficeSupplyStatusEnum.getDescriptionByCode(request.getRequestStatus()));
        vo.setClaimTypeName(OfficeSupplyStatusEnum.getDescriptionByCode(request.getClaimType()));
        vo.setUrgencyLevelName(OfficeSupplyStatusEnum.getDescriptionByCode(request.getUrgencyLevel()));
        
        return vo;
    }
}