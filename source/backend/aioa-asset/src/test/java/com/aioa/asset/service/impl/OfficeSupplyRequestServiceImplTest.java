package com.aioa.asset.service.impl;

import com.aioa.asset.dto.OfficeSupplyApproveDTO;
import com.aioa.asset.dto.OfficeSupplyClaimDTO;
import com.aioa.asset.dto.OfficeSupplyItemDTO;
import com.aioa.asset.dto.OfficeSupplyRequestDTO;
import com.aioa.asset.entity.*;
import com.aioa.asset.enums.OfficeSupplyStatusEnum;
import com.aioa.asset.mapper.*;
import com.aioa.asset.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * OfficeSupplyRequestServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class OfficeSupplyRequestServiceImplTest {
    
    @Mock
    private OfficeSupplyRequestMapper requestMapper;
    
    @Mock
    private OfficeSupplyItemMapper itemMapper;
    
    @Mock
    private OfficeSupplyClaimMapper claimMapper;
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    private OfficeSupplyRequestServiceImpl officeSupplyRequestService; // 使用真实实例
    
    private AssetInfo testAsset;
    private OfficeSupplyRequest testRequest;
    private OfficeSupplyItem testItem;
    
    @BeforeEach
    void setUp() {
        officeSupplyRequestService = new OfficeSupplyRequestServiceImpl(
                requestMapper, itemMapper, claimMapper, assetInfoMapper);
        
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("OFFICE-001");
        testAsset.setAssetName("中性笔");
        testAsset.setSpecification("黑色 0.5mm");
        testAsset.setManufacturer("晨光");
        testAsset.setCurrentQuantity(100);
        testAsset.setWarningQuantity(10);
        testAsset.setAssetStatus(1);
        testAsset.setStatus(1);
        
        testRequest = new OfficeSupplyRequest();
        testRequest.setId(1L);
        testRequest.setRequestNo("OSR20260513000001");
        testRequest.setApplicantId("user001");
        testRequest.setApplicantName("张三");
        testRequest.setDepartmentId("dept001");
        testRequest.setDepartmentName("研发部");
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode());
        testRequest.setCreateBy("user001");
        testRequest.setCreateTime(LocalDateTime.now());
        
        testItem = new OfficeSupplyItem();
        testItem.setId(1L);
        testItem.setRequestId(1L);
        testItem.setAssetId(1L);
        testItem.setAssetCode("OFFICE-001");
        testItem.setAssetName("中性笔");
        testItem.setRequestQuantity(10);
        testItem.setClaimedQuantity(0);
        testItem.setUnitPrice(BigDecimal.valueOf(100.0));
        testItem.setTotalPrice(BigDecimal.valueOf(1000.0));
        testItem.setInventoryCheckStatus(OfficeSupplyStatusEnum.INVENTORY_UNCHECKED.getCode());
    }
    
    @Test
    @DisplayName("创建办公用品申请单 - 成功")
    void testCreateRequest_Success() {
        OfficeSupplyRequestDTO dto = new OfficeSupplyRequestDTO();
        dto.setApplicantId("user001");
        dto.setApplicantName("张三");
        dto.setDepartmentId("dept001");
        dto.setDepartmentName("研发部");
        dto.setReason("办公需要");
        dto.setClaimType(1);
        
        OfficeSupplyItemDTO itemDTO = new OfficeSupplyItemDTO();
        itemDTO.setAssetId(1L);
        itemDTO.setRequestQuantity(10);
        dto.setItems(List.of(itemDTO));
        
        when(requestMapper.insert(any(OfficeSupplyRequest.class))).thenReturn(1);
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(itemMapper.insert(any(OfficeSupplyItem.class))).thenReturn(1);
        
        OfficeSupplyRequestVO result = officeSupplyRequestService.createRequest(dto);
        
        assertNotNull(result);
        assertNotNull(result.getRequestNo());
        assertTrue(result.getRequestNo().startsWith("OSR"));
        assertEquals("user001", result.getApplicantId());
        assertEquals(10, result.getTotalQuantity());
        assertEquals(0, result.getRequestStatus());
        
        verify(requestMapper).insert(any(OfficeSupplyRequest.class));
        verify(itemMapper, atLeastOnce()).insert(any(OfficeSupplyItem.class));
    }
    
    @Test
    @DisplayName("创建办公用品申请单 - 资产不存在")
    void testCreateRequest_AssetNotFound() {
        OfficeSupplyRequestDTO dto = new OfficeSupplyRequestDTO();
        dto.setApplicantId("user001");
        dto.setApplicantName("张三");
        dto.setDepartmentId("dept001");
        dto.setDepartmentName("研发部");
        dto.setClaimType(1);
        
        OfficeSupplyItemDTO itemDTO = new OfficeSupplyItemDTO();
        itemDTO.setAssetId(99L);
        itemDTO.setRequestQuantity(10);
        dto.setItems(List.of(itemDTO));
        
        when(assetInfoMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            officeSupplyRequestService.createRequest(dto);
        });
        
        assertEquals("资产不存在: 99", exception.getMessage());
    }
    
    @Test
    @DisplayName("提交申请单 - 成功")
    void testSubmitRequest_Success() {
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectCount(any(QueryWrapper.class))).thenReturn(1L);
        when(requestMapper.updateById(any(OfficeSupplyRequest.class))).thenReturn(1);
        
        officeSupplyRequestService.submitRequest(1L);
        
        verify(requestMapper).selectById(1L);
        verify(requestMapper).updateById(argThat(req -> 
                req.getRequestStatus().equals(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode())
        ));
    }
    
    @Test
    @DisplayName("提交申请单 - 申请单不存在")
    void testSubmitRequest_RequestNotFound() {
        when(requestMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            officeSupplyRequestService.submitRequest(99L);
        });
        
        assertEquals("申请单不存在: 99", exception.getMessage());
    }
    
    @Test
    @DisplayName("提交申请单 - 状态不是草稿")
    void testSubmitRequest_NotDraftStatus() {
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode());
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            officeSupplyRequestService.submitRequest(1L);
        });
        
        assertEquals("只有草稿状态的申请单可以提交", exception.getMessage());
    }
    
    @Test
    @DisplayName("提交申请单 - 没有明细")
    void testSubmitRequest_NoItems() {
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            officeSupplyRequestService.submitRequest(1L);
        });
        
        assertEquals("申请单没有明细，无法提交", exception.getMessage());
    }
    
    @Test
    @DisplayName("审批通过申请单 - 成功")
    void testApproveRequest_Approve_Success() {
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode());
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(requestMapper.updateById(any(OfficeSupplyRequest.class))).thenReturn(1);
        
        OfficeSupplyApproveDTO approveDTO = new OfficeSupplyApproveDTO();
        approveDTO.setRequestId(1L);
        approveDTO.setApproveResult(true);
        approveDTO.setApproveComment("同意");
        approveDTO.setApproverId("approver001");
        approveDTO.setApproverName("李四");
        
        officeSupplyRequestService.approveRequest(approveDTO);
        
        verify(requestMapper).updateById(argThat(req ->
                req.getRequestStatus().equals(OfficeSupplyStatusEnum.REQUEST_APPROVED.getCode()) &&
                req.getApproverId().equals("approver001")
        ));
    }
    
    @Test
    @DisplayName("审批拒绝申请单 - 成功")
    void testApproveRequest_Reject_Success() {
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_PENDING_APPROVAL.getCode());
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(requestMapper.updateById(any(OfficeSupplyRequest.class))).thenReturn(1);
        
        OfficeSupplyApproveDTO approveDTO = new OfficeSupplyApproveDTO();
        approveDTO.setRequestId(1L);
        approveDTO.setApproveResult(false);
        approveDTO.setApproveComment("库存不足");
        approveDTO.setApproverId("approver001");
        approveDTO.setApproverName("李四");
        
        officeSupplyRequestService.approveRequest(approveDTO);
        
        verify(requestMapper).updateById(argThat(req ->
                req.getRequestStatus().equals(OfficeSupplyStatusEnum.REQUEST_REJECTED.getCode())
        ));
    }
    
    @Test
    @DisplayName("取消申请单 - 成功")
    void testCancelRequest_Success() {
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(requestMapper.updateById(any(OfficeSupplyRequest.class))).thenReturn(1);
        
        officeSupplyRequestService.cancelRequest(1L, "user001", "张三");
        
        verify(requestMapper).updateById(argThat(req ->
                req.getRequestStatus().equals(OfficeSupplyStatusEnum.REQUEST_CANCELLED.getCode())
        ));
    }
    
    @Test
    @DisplayName("领用办公用品 - 成功")
    void testClaimOfficeSupply_Success() {
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_APPROVED.getCode());
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectById(1L)).thenReturn(testItem);
        when(claimMapper.insert(any(OfficeSupplyClaim.class))).thenReturn(1);
        when(itemMapper.updateById(any(OfficeSupplyItem.class))).thenReturn(1);
        when(itemMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(requestMapper.updateById(any(OfficeSupplyRequest.class))).thenReturn(1);
        
        OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
        claimDTO.setRequestId(1L);
        claimDTO.setItemId(1L);
        claimDTO.setClaimQuantity(5);
        claimDTO.setClaimerId("user001");
        claimDTO.setClaimerName("张三");
        claimDTO.setClaimMethod(OfficeSupplyStatusEnum.CLAIM_METHOD_MANUAL.getCode());
        
        officeSupplyRequestService.claimOfficeSupply(claimDTO);
        
        verify(claimMapper).insert(any(OfficeSupplyClaim.class));
        verify(itemMapper).updateById(argThat(item ->
                item.getClaimedQuantity() == 5
        ));
    }
    
    @Test
    @DisplayName("领用办公用品 - 数量超限")
    void testClaimOfficeSupply_QuantityExceed() {
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_APPROVED.getCode());
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectById(1L)).thenReturn(testItem);
        
        OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
        claimDTO.setRequestId(1L);
        claimDTO.setItemId(1L);
        claimDTO.setClaimQuantity(20);
        claimDTO.setClaimerId("user001");
        claimDTO.setClaimerName("张三");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            officeSupplyRequestService.claimOfficeSupply(claimDTO);
        });
        
        assertTrue(exception.getMessage().contains("领用数量超过剩余可领数量"));
    }
    
    @Test
    @DisplayName("签收领用记录 - 成功")
    void testSignClaim_Success() {
        OfficeSupplyClaim claim = new OfficeSupplyClaim();
        claim.setId(1L);
        claim.setSignStatus(OfficeSupplyStatusEnum.SIGN_PENDING.getCode());
        
        when(claimMapper.selectById(1L)).thenReturn(claim);
        when(claimMapper.updateById(any(OfficeSupplyClaim.class))).thenReturn(1);
        
        officeSupplyRequestService.signClaim(1L, "王五");
        
        verify(claimMapper).updateById(argThat(c ->
                c.getSignStatus().equals(OfficeSupplyStatusEnum.SIGN_COMPLETED.getCode()) &&
                c.getSigner().equals("王五")
        ));
    }
    
    @Test
    @DisplayName("获取申请单详情")
    void testGetRequestDetail() {
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(testItem));
        when(claimMapper.selectList(any(QueryWrapper.class))).thenReturn(new ArrayList<>());
        
        OfficeSupplyRequestVO result = officeSupplyRequestService.getRequestDetail(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("OSR20260513000001", result.getRequestNo());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
    }
    
    @Test
    @DisplayName("生成领用二维码")
    void testGenerateClaimQrCode() {
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectById(1L)).thenReturn(testItem);
        
        LocalDateTime expireTime = LocalDateTime.now().plusHours(1);
        String qrCode = officeSupplyRequestService.generateClaimQrCode(1L, 1L, expireTime);
        
        assertNotNull(qrCode);
        byte[] decoded = java.util.Base64.getDecoder().decode(qrCode);
        String content = new String(decoded);
        assertTrue(content.contains("\"requestId\":1"));
        assertTrue(content.contains("\"itemId\":1"));
    }
    
    @Test
    @DisplayName("列表查询申请单")
    void testListRequests() {
        when(requestMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(testRequest));
        when(itemMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(testItem));
        
        List<OfficeSupplyRequestVO> results = officeSupplyRequestService.listRequests(
                "user001", "dept001", null, null, null);
        
        assertEquals(1, results.size());
        verify(requestMapper).selectList(any(QueryWrapper.class));
    }
    
    @Test
    @DisplayName("库存检查并更新状态")
    void testCheckInventoryAndUpdateStatus() {
        testRequest.setRequestStatus(OfficeSupplyStatusEnum.REQUEST_DRAFT.getCode());
        when(requestMapper.selectById(1L)).thenReturn(testRequest);
        when(itemMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(testItem));
        when(itemMapper.updateById(any(OfficeSupplyItem.class))).thenReturn(1);
        
        officeSupplyRequestService.checkInventoryAndUpdateStatus(1L);
        
        verify(itemMapper, atLeastOnce()).updateById(any(OfficeSupplyItem.class));
    }
}