package com.aioa.asset.controller;

import com.aioa.asset.dto.OfficeSupplyApproveDTO;
import com.aioa.asset.dto.OfficeSupplyClaimDTO;
import com.aioa.asset.dto.OfficeSupplyRequestDTO;
import com.aioa.asset.service.OfficeSupplyRequestService;
import com.aioa.asset.vo.OfficeSupplyRequestVO;
import com.aioa.asset.vo.OfficeSupplyStatsVO;
import com.aioa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 办公用品申请领用控制器
 */
@Slf4j
@RestController
@RequestMapping("/office-supply")
@RequiredArgsConstructor
@Tag(name = "办公用品申请领用管理", description = "办公用品线上申请线下领用相关接口")
public class OfficeSupplyController {
    
    private final OfficeSupplyRequestService officeSupplyRequestService;
    
    @PostMapping("/request/create")
    @Operation(summary = "创建办公用品申请")
    public Result<OfficeSupplyRequestVO> createRequest(@Valid @RequestBody OfficeSupplyRequestDTO requestDTO) {
        try {
            OfficeSupplyRequestVO result = officeSupplyRequestService.createRequest(requestDTO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("创建办公用品申请失败: {}", e.getMessage(), e);
            return Result.fail("创建办公用品申请失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/request/submit/{requestId}")
    @Operation(summary = "提交办公用品申请")
    public Result<Void> submitRequest(@PathVariable Long requestId) {
        try {
            officeSupplyRequestService.submitRequest(requestId);
            return Result.success();
        } catch (Exception e) {
            log.error("提交办公用品申请失败: {}", e.getMessage(), e);
            return Result.fail("提交办公用品申请失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/request/approve")
    @Operation(summary = "审批办公用品申请")
    public Result<Void> approveRequest(@Valid @RequestBody OfficeSupplyApproveDTO approveDTO) {
        try {
            officeSupplyRequestService.approveRequest(approveDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("审批办公用品申请失败: {}", e.getMessage(), e);
            return Result.fail("审批办公用品申请失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/request/cancel/{requestId}")
    @Operation(summary = "取消办公用品申请")
    public Result<Void> cancelRequest(@PathVariable Long requestId, 
                                 @RequestParam String operatorId,
                                 @RequestParam String operatorName) {
        try {
            officeSupplyRequestService.cancelRequest(requestId, operatorId, operatorName);
            return Result.success();
        } catch (Exception e) {
            log.error("取消办公用品申请失败: {}", e.getMessage(), e);
            return Result.fail("取消办公用品申请失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/claim")
    @Operation(summary = "领用办公用品")
    public Result<Void> claimOfficeSupply(@Valid @RequestBody OfficeSupplyClaimDTO claimDTO) {
        try {
            officeSupplyRequestService.claimOfficeSupply(claimDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("领用办公用品失败: {}", e.getMessage(), e);
            return Result.fail("领用办公用品失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/claim/sign/{claimId}")
    @Operation(summary = "签收领用记录")
    public Result<Void> signClaim(@PathVariable Long claimId, @RequestParam String signer) {
        try {
            officeSupplyRequestService.signClaim(claimId, signer);
            return Result.success();
        } catch (Exception e) {
            log.error("签收领用记录失败: {}", e.getMessage(), e);
            return Result.fail("签收领用记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/claim/qr-code")
    @Operation(summary = "生成领用二维码")
    public Result<String> generateClaimQrCode(@RequestParam Long requestId, 
                                         @RequestParam Long itemId,
                                         @RequestParam(required = false) Integer expireHours) {
        try {
            LocalDateTime expireTime = LocalDateTime.now().plusHours(expireHours != null ? expireHours : 24);
            String qrCode = officeSupplyRequestService.generateClaimQrCode(requestId, itemId, expireTime);
            return Result.success(qrCode);
        } catch (Exception e) {
            log.error("生成领用二维码失败: {}", e.getMessage(), e);
            return Result.fail("生成领用二维码失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/claim/qr-code/scan")
    @Operation(summary = "扫码领用办公用品")
    public Result<Void> claimByQrCode(@RequestParam String qrCode,
                                 @RequestParam String claimerId,
                                 @RequestParam String claimerName) {
        try {
            officeSupplyRequestService.claimByQrCode(qrCode, claimerId, claimerName);
            return Result.success();
        } catch (Exception e) {
            log.error("扫码领用失败: {}", e.getMessage(), e);
            return Result.fail("扫码领用失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/request/{requestId}")
    @Operation(summary = "获取申请单详情")
    public Result<OfficeSupplyRequestVO> getRequestDetail(@PathVariable Long requestId) {
        try {
            OfficeSupplyRequestVO result = officeSupplyRequestService.getRequestDetail(requestId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取申请单详情失败: {}", e.getMessage(), e);
            return Result.fail("获取申请单详情失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/request/list")
    @Operation(summary = "查询申请单列表")
    public Result<List<OfficeSupplyRequestVO>> listRequests(
            @RequestParam(required = false) String applicantId,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) Integer requestStatus,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            
            List<OfficeSupplyRequestVO> result = officeSupplyRequestService.listRequests(
                    applicantId, departmentId, requestStatus, start, end);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询申请单列表失败: {}", e.getMessage(), e);
            return Result.fail("查询申请单列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/request/check-inventory/{requestId}")
    @Operation(summary = "检查库存并更新状态")
    public Result<Void> checkInventoryAndUpdateStatus(@PathVariable Long requestId) {
        try {
            officeSupplyRequestService.checkInventoryAndUpdateStatus(requestId);
            return Result.success();
        } catch (Exception e) {
            log.error("检查库存失败: {}", e.getMessage(), e);
            return Result.fail("检查库存失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/stats")
    @Operation(summary = "获取办公用品统计报表")
    public Result<List<OfficeSupplyStatsVO>> getOfficeSupplyStats(
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) String departmentId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            
            List<OfficeSupplyStatsVO> result = officeSupplyRequestService.getOfficeSupplyStats(
                    dimension, departmentId, start, end);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取统计报表失败: {}", e.getMessage(), e);
            return Result.fail("获取统计报表失败: " + e.getMessage());
        }
    }
}