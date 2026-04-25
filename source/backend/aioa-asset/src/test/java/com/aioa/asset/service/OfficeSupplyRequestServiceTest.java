package com.aioa.asset.service;

import com.aioa.asset.dto.OfficeSupplyApproveDTO;
import com.aioa.asset.dto.OfficeSupplyClaimDTO;
import com.aioa.asset.dto.OfficeSupplyRequestDTO;
import com.aioa.asset.dto.OfficeSupplyItemDTO;
import com.aioa.asset.entity.*;
import com.aioa.asset.mapper.*;
import com.aioa.asset.service.impl.OfficeSupplyRequestServiceImpl;
import com.aioa.asset.vo.OfficeSupplyRequestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 办公用品申请服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class OfficeSupplyRequestServiceTest {
    
    @Mock
    private OfficeSupplyRequestMapper requestMapper;
    
    @Mock
    private OfficeSupplyItemMapper itemMapper;
    
    @Mock
    private OfficeSupplyClaimMapper claimMapper;
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    @InjectMocks
    private OfficeSupplyRequestServiceImpl officeSupplyRequestService;
    
    private OfficeSupplyRequestDTO requestDTO;
    private AssetInfo assetInfo;
    private OfficeSupplyRequest request;
    private OfficeSupplyItem item;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        requestDTO = new OfficeSupplyRequestDTO();
        requestDTO.setApplicantId("user001");
        requestDTO.setApplicantName("张三");
        requestDTO.setDepartmentId("dept001");
        requestDTO.setDepartmentName("技术部");
        requestDTO.setReason("日常办公需要");
        requestDTO.setClaimType(1);
        requestDTO.setUrgencyLevel(1);
        requestDTO.setRemark("测试申请");
        
        OfficeSupplyItemDTO itemDTO = new OfficeSupplyItemDTO();
        itemDTO.setAssetId(1L);
        itemDTO.setRequestQuantity(10);
        itemDTO.setRemark("测试用品");
        
        requestDTO.setItems(Collections.singletonList(itemDTO));
        
        // 资产信息
        assetInfo = new AssetInfo();
        assetInfo.setId(1L);
        assetInfo.setAssetCode("OSS001");
        assetInfo.setAssetName("A4打印纸");
        assetInfo.setSpecification("80g/包");
        assetInfo.setPurchasePrice(BigDecimal.valueOf(25.00));
        assetInfo.setCurrentQuantity(100);
        
        // 申请单
        request = new OfficeSupplyRequest();
        request.setId(1L);
        request.setRequestNo("OSS-20250425231500-123");
        request.setApplicantId("user001");
        request.setApplicantName("张三");
        request.setDepartmentId("dept001");
        request.setDepartmentName("技术部");
        request.setRequestStatus(0);
        request.setTotalQuantity(10);
        request.setClaimedQuantity(0);
        
        // 申请明细
        item = new OfficeSupplyItem();
        item.setId(1L);
        item.setRequestId(1L);
        item.setAssetId(1L);
        item.setAssetCode("OSS001");
        item.setAssetName("A4打印纸");
        item.setRequestQuantity(10);
        item.setClaimedQuantity(0);
        item.setUnitPrice(BigDecimal.valueOf(25.00));
        item.setTotalPrice(BigDecimal.valueOf(250.00));
    }
    
    @Test
    void testCreateRequest_Success() {
        // 模拟
        when(assetInfoMapper.selectById(eq(1L))).thenReturn(assetInfo);
        when(requestMapper.insert(any(OfficeSupplyRequest.class))).thenAnswer(invocation -> {
            OfficeSupplyRequest req = invocation.getArgument(0);
            req.setId(1L);
            return 1;
        });
        when(itemMapper.insert(any(OfficeSupplyItem.class))).thenReturn(1);
        
        // 执行
        OfficeSupplyRequestVO result = officeSupplyRequestService.createRequest(requestDTO);
        
        // 验证
        assertNotNull(result);
        assertNotNull(result.getRequestNo());
        assertEquals("user001", result.getApplicantId());
        assertEquals("技术部", result.getDepartmentName());
        assertEquals(0, result.getRequestStatus()); // 草稿状态
        assertEquals(10, result.getTotalQuantity());
        
        // 验证方法调用
        verify(assetInfoMapper, times(1)).selectById(eq(1L));
        verify(requestMapper, times(1)).insert(any(OfficeSupplyRequest.class));
        verify(itemMapper, atLeastOnce()).insert(any(OfficeSupplyItem.class));
    }
    
    @Test
    void testCreateRequest_AssetNotFound() {
        // 模拟资产不存在
        when(assetInfoMapper.selectById(eq(1L))).thenReturn(null);
        
        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> officeSupplyRequestService.createRequest(requestDTO));
        
        assertTrue(exception.getMessage().contains("资产不存在"));
        verify(assetInfoMapper, times(1)).selectById(eq(1L));
        verify(requestMapper, never()).insert(any());
    }
    
    @Test
    void testSubmitRequest_Success() {
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        when(itemMapper.selectList(any())).thenReturn(Collections.singletonList(item));
        
        // 执行
        officeSupplyRequestService.submitRequest(1L);
        
        // 验证
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(requestMapper, times(1)).updateById(request);
        assertEquals(1, request.getRequestStatus()); // 应该更新为待审批状态
    }
    
    @Test
    void testSubmitRequest_NotFound() {
        // 模拟申请单不存在
        when(requestMapper.selectById(eq(1L))).thenReturn(null);
        
        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> officeSupplyRequestService.submitRequest(1L));
        
        assertTrue(exception.getMessage().contains("申请单不存在"));
        verify(requestMapper, times(1)).selectById(eq(1L));
    }
    
    @Test
    void testApproveRequest_ApproveSuccess() {
        // 准备审批数据
        request.setRequestStatus(1); // 待审批状态
        
        OfficeSupplyApproveDTO approveDTO = new OfficeSupplyApproveDTO();
        approveDTO.setRequestId(1L);
        approveDTO.setApproveResult(true);
        approveDTO.setApproverId("approver001");
        approveDTO.setApproverName("李四");
        approveDTO.setApproveComment("同意");
        
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        when(itemMapper.selectList(any())).thenReturn(Collections.singletonList(item));
        
        // 执行
        officeSupplyRequestService.approveRequest(approveDTO);
        
        // 验证
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(requestMapper, times(1)).updateById(request);
        assertEquals(2, request.getRequestStatus()); // 应该更新为审批通过状态
        assertEquals("approver001", request.getApproverId());
        assertEquals("李四", request.getApproverName());
        assertNotNull(request.getApproveTime());
    }
    
    @Test
    void testApproveRequest_Reject() {
        // 准备审批数据
        request.setRequestStatus(1); // 待审批状态
        
        OfficeSupplyApproveDTO approveDTO = new OfficeSupplyApproveDTO();
        approveDTO.setRequestId(1L);
        approveDTO.setApproveResult(false);
        approveDTO.setApproverId("approver001");
        approveDTO.setApproverName("李四");
        approveDTO.setApproveComment("不同意");
        
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        
        // 执行
        officeSupplyRequestService.approveRequest(approveDTO);
        
        // 验证
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(requestMapper, times(1)).updateById(request);
        assertEquals(3, request.getRequestStatus()); // 应该更新为审批拒绝状态
    }
    
    @Test
    void testClaimOfficeSupply_Success() {
        // 准备数据
        request.setRequestStatus(2); // 审批通过状态
        item.setInventoryCheckStatus(1); // 库存充足
        
        OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
        claimDTO.setRequestId(1L);
        claimDTO.setItemId(1L);
        claimDTO.setClaimQuantity(5);
        claimDTO.setClaimerId("user001");
        claimDTO.setClaimerName("张三");
        claimDTO.setClaimMethod(2); // 手动领用
        
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        when(itemMapper.selectById(eq(1L))).thenReturn(item);
        when(assetInfoMapper.selectById(eq(1L))).thenReturn(assetInfo);
        when(claimMapper.insert(any(OfficeSupplyClaim.class))).thenReturn(1);
        
        // 执行
        officeSupplyRequestService.claimOfficeSupply(claimDTO);
        
        // 验证
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(itemMapper, times(1)).selectById(eq(1L));
        verify(assetInfoMapper, times(1)).selectById(eq(1L));
        verify(claimMapper, times(1)).insert(any(OfficeSupplyClaim.class));
        verify(itemMapper, times(1)).updateById(item);
        verify(requestMapper, times(1)).updateById(request);
        verify(assetInfoMapper, times(1)).updateById(assetInfo);
        
        // 验证数据更新
        assertEquals(5, item.getClaimedQuantity());
        assertEquals(5, request.getClaimedQuantity());
        assertEquals(4, request.getRequestStatus()); // 部分领取状态
        assertEquals(95, assetInfo.getCurrentQuantity()); // 库存减少
    }
    
    @Test
    void testClaimOfficeSupply_InsufficientInventory() {
        // 准备数据
        request.setRequestStatus(2); // 审批通过状态
        assetInfo.setCurrentQuantity(3); // 库存不足
        
        OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
        claimDTO.setRequestId(1L);
        claimDTO.setItemId(1L);
        claimDTO.setClaimQuantity(5);
        claimDTO.setClaimerId("user001");
        claimDTO.setClaimerName("张三");
        
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        when(itemMapper.selectById(eq(1L))).thenReturn(item);
        when(assetInfoMapper.selectById(eq(1L))).thenReturn(assetInfo);
        
        // 执行和验证
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> officeSupplyRequestService.claimOfficeSupply(claimDTO));
        
        assertTrue(exception.getMessage().contains("库存不足"));
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(itemMapper, times(1)).selectById(eq(1L));
        verify(assetInfoMapper, times(1)).selectById(eq(1L));
        verify(claimMapper, never()).insert(any());
    }
    
    @Test
    void testGetRequestDetail_Success() {
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        when(itemMapper.selectList(any())).thenReturn(Collections.singletonList(item));
        when(claimMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(assetInfoMapper.selectById(eq(1L))).thenReturn(assetInfo);
        
        // 执行
        OfficeSupplyRequestVO result = officeSupplyRequestService.getRequestDetail(1L);
        
        // 验证
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("OSS-20250425231500-123", result.getRequestNo());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        
        // 验证方法调用
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(itemMapper, times(1)).selectList(any());
        verify(claimMapper, times(1)).selectList(any());
    }
    
    @Test
    void testCancelRequest_Success() {
        // 模拟
        when(requestMapper.selectById(eq(1L))).thenReturn(request);
        
        // 执行
        officeSupplyRequestService.cancelRequest(1L, "user001", "张三");
        
        // 验证
        verify(requestMapper, times(1)).selectById(eq(1L));
        verify(requestMapper, times(1)).updateById(request);
        assertEquals(6, request.getRequestStatus()); // 已取消状态
        assertEquals("user001", request.getUpdateBy());
    }
    
    @Test
    void testSignClaim_Success() {
        // 准备领用记录
        OfficeSupplyClaim claim = new OfficeSupplyClaim();
        claim.setId(1L);
        claim.setSignStatus(0); // 待签收
        
        // 模拟
        when(claimMapper.selectById(eq(1L))).thenReturn(claim);
        
        // 执行
        officeSupplyRequestService.signClaim(1L, "王五");
        
        // 验证
        verify(claimMapper, times(1)).selectById(eq(1L));
        verify(claimMapper, times(1)).updateById(claim);
        assertEquals(1, claim.getSignStatus()); // 已签收状态
        assertEquals("王五", claim.getSigner());
        assertNotNull(claim.getSignTime());
    }
    
    @Test
    void testCheckInventoryAndUpdateStatus() {
        // 模拟
        when(itemMapper.selectList(any())).thenReturn(Collections.singletonList(item));
        when(assetInfoMapper.selectById(eq(1L))).thenReturn(assetInfo);
        
        // 执行
        officeSupplyRequestService.checkInventoryAndUpdateStatus(1L);
        
        // 验证
        verify(itemMapper, times(1)).selectList(any());
        verify(assetInfoMapper, times(1)).selectById(eq(1L));
        verify(itemMapper, times(1)).updateById(item);
        
        // 验证库存检查结果
        assertEquals(1, item.getInventoryCheckStatus()); // 库存充足
        assertNotNull(item.getInventoryCheckComment());
    }
}