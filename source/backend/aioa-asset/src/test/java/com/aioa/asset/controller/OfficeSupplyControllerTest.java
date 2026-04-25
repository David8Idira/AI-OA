package com.aioa.asset.controller;

import com.aioa.asset.dto.OfficeSupplyApproveDTO;
import com.aioa.asset.dto.OfficeSupplyClaimDTO;
import com.aioa.asset.dto.OfficeSupplyRequestDTO;
import com.aioa.asset.dto.OfficeSupplyItemDTO;
import com.aioa.asset.service.OfficeSupplyRequestService;
import com.aioa.asset.vo.OfficeSupplyRequestVO;
import com.aioa.asset.vo.OfficeSupplyStatsVO;
import com.aioa.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static com.aioa.common.result.ResultCode.SUCCESS;

/**
 * 办公用品控制器单元测试
 */
@WebMvcTest(OfficeSupplyController.class)
class OfficeSupplyControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private OfficeSupplyRequestService officeSupplyRequestService;
    
    private OfficeSupplyRequestDTO requestDTO;
    private OfficeSupplyRequestVO requestVO;
    
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
        
        // 返回的VO
        requestVO = new OfficeSupplyRequestVO();
        requestVO.setId(1L);
        requestVO.setRequestNo("OSS-20250425231500-123");
        requestVO.setApplicantId("user001");
        requestVO.setApplicantName("张三");
        requestVO.setDepartmentName("技术部");
        requestVO.setRequestStatus(0);
        requestVO.setTotalQuantity(10);
        requestVO.setClaimedQuantity(0);
    }
    
    @Test
    void testCreateRequest_Success() throws Exception {
        // 模拟服务层返回
        when(officeSupplyRequestService.createRequest(any(OfficeSupplyRequestDTO.class)))
                .thenReturn(requestVO);
        
        // 执行请求
        mockMvc.perform(post("/office-supply/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requestNo").value("OSS-20250425231500-123"))
                .andExpect(jsonPath("$.data.applicantName").value("张三"))
                .andExpect(jsonPath("$.data.totalQuantity").value(10));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .createRequest(any(OfficeSupplyRequestDTO.class));
    }
    
    @Test
    void testCreateRequest_ValidationError() throws Exception {
        // 准备无效数据
        OfficeSupplyRequestDTO invalidDTO = new OfficeSupplyRequestDTO();
        // 缺少必填字段
        
        // 执行请求
        mockMvc.perform(post("/office-supply/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
        
        // 验证服务层未调用
        verify(officeSupplyRequestService, never())
                .createRequest(any(OfficeSupplyRequestDTO.class));
    }
    
    @Test
    void testSubmitRequest_Success() throws Exception {
        // 执行请求
        mockMvc.perform(post("/office-supply/request/submit/{requestId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1)).submitRequest(eq(1L));
    }
    
    @Test
    void testSubmitRequest_ServiceError() throws Exception {
        // 模拟服务层异常
        doThrow(new RuntimeException("申请单不存在"))
                .when(officeSupplyRequestService).submitRequest(eq(1L));
        
        // 执行请求
        mockMvc.perform(post("/office-supply/request/submit/{requestId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("提交办公用品申请失败: 申请单不存在"));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1)).submitRequest(eq(1L));
    }
    
    @Test
    void testApproveRequest_Success() throws Exception {
        // 准备审批数据
        OfficeSupplyApproveDTO approveDTO = new OfficeSupplyApproveDTO();
        approveDTO.setRequestId(1L);
        approveDTO.setApproveResult(true);
        approveDTO.setApproverId("approver001");
        approveDTO.setApproverName("李四");
        approveDTO.setApproveComment("同意");
        
        // 执行请求
        mockMvc.perform(post("/office-supply/request/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .approveRequest(any(OfficeSupplyApproveDTO.class));
    }
    
    @Test
    void testCancelRequest_Success() throws Exception {
        // 执行请求
        mockMvc.perform(post("/office-supply/request/cancel/{requestId}", 1L)
                        .param("operatorId", "user001")
                        .param("operatorName", "张三"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .cancelRequest(eq(1L), eq("user001"), eq("张三"));
    }
    
    @Test
    void testClaimOfficeSupply_Success() throws Exception {
        // 准备领用数据
        OfficeSupplyClaimDTO claimDTO = new OfficeSupplyClaimDTO();
        claimDTO.setRequestId(1L);
        claimDTO.setItemId(1L);
        claimDTO.setClaimQuantity(5);
        claimDTO.setClaimerId("user001");
        claimDTO.setClaimerName("张三");
        claimDTO.setClaimMethod(2);
        
        // 执行请求
        mockMvc.perform(post("/office-supply/claim")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .claimOfficeSupply(any(OfficeSupplyClaimDTO.class));
    }
    
    @Test
    void testSignClaim_Success() throws Exception {
        // 执行请求
        mockMvc.perform(post("/office-supply/claim/sign/{claimId}", 1L)
                        .param("signer", "王五"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .signClaim(eq(1L), eq("王五"));
    }
    
    @Test
    void testGenerateClaimQrCode_Success() throws Exception {
        // 模拟服务层返回
        String qrCode = "base64encodedqrcode";
        when(officeSupplyRequestService.generateClaimQrCode(eq(1L), eq(2L), any(LocalDateTime.class)))
                .thenReturn(qrCode);
        
        // 执行请求
        mockMvc.perform(get("/office-supply/claim/qr-code")
                        .param("requestId", "1")
                        .param("itemId", "2")
                        .param("expireHours", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(qrCode));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .generateClaimQrCode(eq(1L), eq(2L), any(LocalDateTime.class));
    }
    
    @Test
    void testClaimByQrCode_Success() throws Exception {
        // 执行请求
        mockMvc.perform(post("/office-supply/claim/qr-code/scan")
                        .param("qrCode", "encodedqrcode")
                        .param("claimerId", "user001")
                        .param("claimerName", "张三"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .claimByQrCode(eq("encodedqrcode"), eq("user001"), eq("张三"));
    }
    
    @Test
    void testGetRequestDetail_Success() throws Exception {
        // 模拟服务层返回
        when(officeSupplyRequestService.getRequestDetail(eq(1L)))
                .thenReturn(requestVO);
        
        // 执行请求
        mockMvc.perform(get("/office-supply/request/{requestId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.requestNo").value("OSS-20250425231500-123"))
                .andExpect(jsonPath("$.data.applicantName").value("张三"));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .getRequestDetail(eq(1L));
    }
    
    @Test
    void testGetRequestDetail_NotFound() throws Exception {
        // 模拟服务层返回空
        when(officeSupplyRequestService.getRequestDetail(eq(1L)))
                .thenReturn(null);
        
        // 执行请求
        mockMvc.perform(get("/office-supply/request/{requestId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .getRequestDetail(eq(1L));
    }
    
    @Test
    void testListRequests_Success() throws Exception {
        // 模拟服务层返回
        List<OfficeSupplyRequestVO> requestList = Arrays.asList(requestVO);
        when(officeSupplyRequestService.listRequests(any(), any(), any(), any(), any()))
                .thenReturn(requestList);
        
        // 执行请求
        mockMvc.perform(get("/office-supply/request/list")
                        .param("applicantId", "user001")
                        .param("departmentId", "dept001")
                        .param("requestStatus", "0")
                        .param("startTime", "2025-04-01T00:00:00")
                        .param("endTime", "2025-04-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].requestNo").value("OSS-20250425231500-123"));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .listRequests(eq("user001"), eq("dept001"), eq(0), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    void testCheckInventoryAndUpdateStatus_Success() throws Exception {
        // 执行请求
        mockMvc.perform(post("/office-supply/request/check-inventory/{requestId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .checkInventoryAndUpdateStatus(eq(1L));
    }
    
    @Test
    void testGetOfficeSupplyStats_Success() throws Exception {
        // 准备统计数据
        OfficeSupplyStatsVO stats = new OfficeSupplyStatsVO();
        stats.setDimension("department");
        stats.setDimensionValue("dept001");
        stats.setTotalRequestQuantity(100);
        stats.setTotalClaimedQuantity(80);
        stats.setTotalRequestAmount(BigDecimal.valueOf(5000.00));
        
        List<OfficeSupplyStatsVO> statsList = Arrays.asList(stats);
        
        // 模拟服务层返回
        when(officeSupplyRequestService.getOfficeSupplyStats(any(), any(), any(), any()))
                .thenReturn(statsList);
        
        // 执行请求
        mockMvc.perform(get("/office-supply/stats")
                        .param("dimension", "department")
                        .param("departmentId", "dept001")
                        .param("startTime", "2025-04-01T00:00:00")
                        .param("endTime", "2025-04-30T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].totalRequestQuantity").value(100))
                .andExpect(jsonPath("$.data[0].totalClaimedQuantity").value(80));
        
        // 验证服务层调用
        verify(officeSupplyRequestService, times(1))
                .getOfficeSupplyStats(eq("department"), eq("dept001"), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    void testInvalidJsonFormat() throws Exception {
        // 执行请求，发送无效的JSON
        mockMvc.perform(post("/office-supply/request/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());
        
        // 验证服务层未调用
        verify(officeSupplyRequestService, never())
                .createRequest(any(OfficeSupplyRequestDTO.class));
    }
}