package com.aioa.reimburse.controller;

import com.aioa.common.result.Result;
import com.aioa.reimburse.dto.CreateReimburseDTO;
import com.aioa.reimburse.dto.OcrAutoFillDTO;
import com.aioa.reimburse.dto.ReimburseActionDTO;
import com.aioa.reimburse.dto.ReimburseQueryDTO;
import com.aioa.reimburse.dto.ReimburseItemDTO;
import com.aioa.reimburse.entity.Invoice;
import com.aioa.reimburse.enums.ReimburseActionEnum;
import com.aioa.reimburse.enums.ReimburseTypeEnum;
import com.aioa.reimburse.service.ReimburseService;
import com.aioa.reimburse.vo.OcrAutoFillVO;
import com.aioa.reimburse.vo.ReimburseVO;
import com.aioa.common.vo.PageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReimburseController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReimburseController 测试")
class ReimburseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReimburseService reimburseService;

    @InjectMocks
    private ReimburseController reimburseController;

    private ObjectMapper objectMapper;
    private ReimburseVO testReimburseVO;
    private CreateReimburseDTO testCreateDTO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reimburseController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup test ReimburseVO
        testReimburseVO = new ReimburseVO();
        testReimburseVO.setId("reimb001");
        testReimburseVO.setTitle("差旅报销测试");
        testReimburseVO.setType(ReimburseTypeEnum.BUSINESS_TRIP.getCode());
        testReimburseVO.setTypeName("差旅报销");
        testReimburseVO.setTotalAmount(new BigDecimal("1000.00"));
        testReimburseVO.setCurrency("CNY");
        testReimburseVO.setStatus(1);
        testReimburseVO.setStatusName("待审批");
        testReimburseVO.setPriority(1);
        testReimburseVO.setPriorityName("普通");
        testReimburseVO.setApplicantId("user001");
        testReimburseVO.setApplicantName("张三");
        testReimburseVO.setApproverId("user002");
        testReimburseVO.setApproverName("李四");
        testReimburseVO.setReimburseDate(LocalDateTime.now());
        testReimburseVO.setCreateTime(LocalDateTime.now());
        testReimburseVO.setUpdateTime(LocalDateTime.now());
        testReimburseVO.setItems(new ArrayList<>());

        // Setup test CreateReimburseDTO
        testCreateDTO = new CreateReimburseDTO();
        testCreateDTO.setTitle("差旅报销测试");
        testCreateDTO.setType(ReimburseTypeEnum.BUSINESS_TRIP.getCode());
        testCreateDTO.setCurrency("CNY");
        testCreateDTO.setPriority(1);
        testCreateDTO.setApproverId("user002");
        testCreateDTO.setReimburseDate(LocalDateTime.now());

        ReimburseItemDTO itemDTO = new ReimburseItemDTO();
        itemDTO.setExpenseType("TRANSPORT");
        itemDTO.setDescription("出差交通费");
        itemDTO.setExpenseDate(LocalDate.now());
        itemDTO.setQuantity(new BigDecimal("2"));
        itemDTO.setUnitPrice(new BigDecimal("250"));
        itemDTO.setAmount(new BigDecimal("500"));
        testCreateDTO.setItems(Collections.singletonList(itemDTO));
    }

    // ==================== Helper: set userId in request attribute ====================

    private void setUserIdAttribute(String userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(userId);
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
    }

    // ==================== POST /api/v1/reimburse ====================

    @Nested
    @DisplayName("POST /api/v1/reimburse - 创建报销单")
    class CreateReimburseTests {

        @Test
        @DisplayName("创建报销单 - 成功")
        void createReimburse_Success() throws Exception {
            setUserIdAttribute("user001");
            when(reimburseService.createReimburse(eq("user001"), any(CreateReimburseDTO.class)))
                    .thenReturn(testReimburseVO);

            mockMvc.perform(post("/api/v1/reimburse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCreateDTO))
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value("reimb001"))
                    .andExpect(jsonPath("$.data.title").value("差旅报销测试"));

            verify(reimburseService).createReimburse(eq("user001"), any(CreateReimburseDTO.class));
        }

        @Test
        @DisplayName("创建报销单 - 失败（Service异常）")
        void createReimburse_ServiceException() throws Exception {
            setUserIdAttribute("user001");
            when(reimburseService.createReimburse(eq("user001"), any(CreateReimburseDTO.class)))
                    .thenThrow(new RuntimeException("Service error"));

            mockMvc.perform(post("/api/v1/reimburse")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testCreateDTO))
                            .requestAttr("userId", "user001"))
                    .andExpect(status().is5xxServerError());

            verify(reimburseService).createReimburse(eq("user001"), any(CreateReimburseDTO.class));
        }
    }

    // ==================== GET /api/v1/reimburse ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse - 查询报销列表")
    class ListReimbursesTests {

        @Test
        @DisplayName("查询报销列表 - 成功")
        void listReimburses_Success() throws Exception {
            setUserIdAttribute("user001");
            PageResult<ReimburseVO> pageResult = PageResult.of(1, 1, 10,
                    Collections.singletonList(testReimburseVO));
            when(reimburseService.queryReimburses(eq("user001"), any(ReimburseQueryDTO.class)))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/v1/reimburse")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1))
                    .andExpect(jsonPath("$.data.records[0].id").value("reimb001"));

            verify(reimburseService).queryReimburses(eq("user001"), any(ReimburseQueryDTO.class));
        }

        @Test
        @DisplayName("查询报销列表 - 带过滤条件")
        void listReimburses_WithFilters() throws Exception {
            setUserIdAttribute("user001");
            PageResult<ReimburseVO> pageResult = PageResult.of(0, 1, 10, Collections.emptyList());
            when(reimburseService.queryReimburses(eq("user001"), any(ReimburseQueryDTO.class)))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/v1/reimburse")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .param("type", "BUSINESS_TRIP")
                            .param("status", "1")
                            .param("mode", "MY_APPLY")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(reimburseService).queryReimburses(eq("user001"), any(ReimburseQueryDTO.class));
        }
    }

    // ==================== GET /api/v1/reimburse/{id} ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse/{id} - 获取报销详情")
    class GetReimburseDetailTests {

        @Test
        @DisplayName("获取报销详情 - 成功")
        void getReimburseDetail_Success() throws Exception {
            setUserIdAttribute("user001");
            when(reimburseService.getReimburseDetail("reimb001", "user001"))
                    .thenReturn(testReimburseVO);

            mockMvc.perform(get("/api/v1/reimburse/reimb001")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value("reimb001"))
                    .andExpect(jsonPath("$.data.applicantName").value("张三"));

            verify(reimburseService).getReimburseDetail("reimb001", "user001");
        }
    }

    // ==================== DELETE /api/v1/reimburse/{id} ====================

    @Nested
    @DisplayName("DELETE /api/v1/reimburse/{id} - 删除报销单")
    class DeleteReimburseTests {

        @Test
        @DisplayName("删除报销单 - 成功")
        void deleteReimburse_Success() throws Exception {
            setUserIdAttribute("user001");
            when(reimburseService.deleteReimburse("reimb001", "user001")).thenReturn(true);

            mockMvc.perform(delete("/api/v1/reimburse/reimb001")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            verify(reimburseService).deleteReimburse("reimb001", "user001");
        }

        @Test
        @DisplayName("删除报销单 - 失败返回false")
        void deleteReimburse_Failure() throws Exception {
            setUserIdAttribute("user001");
            when(reimburseService.deleteReimburse("reimb001", "user001")).thenReturn(false);

            mockMvc.perform(delete("/api/v1/reimburse/reimb001")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("删除失败"));

            verify(reimburseService).deleteReimburse("reimb001", "user001");
        }
    }

    // ==================== POST /api/v1/reimburse/{id}/action ====================

    @Nested
    @DisplayName("POST /api/v1/reimburse/{id}/action - 审批操作")
    class DoActionTests {

        @Test
        @DisplayName("审批通过 - 成功")
        void doAction_Approve_Success() throws Exception {
            setUserIdAttribute("user002");
            ReimburseActionDTO actionDTO = new ReimburseActionDTO();
            actionDTO.setActionType(ReimburseActionEnum.APPROVE.getCode());
            actionDTO.setComment("同意报销");

            when(reimburseService.doAction("reimb001", "user002", actionDTO))
                    .thenReturn(testReimburseVO);

            mockMvc.perform(post("/api/v1/reimburse/reimb001/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(actionDTO))
                            .requestAttr("userId", "user002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value("reimb001"));

            verify(reimburseService).doAction("reimb001", "user002", actionDTO);
        }

        @Test
        @DisplayName("审批驳回 - 成功")
        void doAction_Reject_Success() throws Exception {
            setUserIdAttribute("user002");
            ReimburseActionDTO actionDTO = new ReimburseActionDTO();
            actionDTO.setActionType(ReimburseActionEnum.REJECT.getCode());
            actionDTO.setReason("材料不全");
            actionDTO.setComment("请补充发票");

            ReimburseVO rejectedVO = new ReimburseVO();
            rejectedVO.setId("reimb001");
            rejectedVO.setStatus(3);
            rejectedVO.setRejectReason("材料不全");

            when(reimburseService.doAction("reimb001", "user002", actionDTO))
                    .thenReturn(rejectedVO);

            mockMvc.perform(post("/api/v1/reimburse/reimb001/action")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(actionDTO))
                            .requestAttr("userId", "user002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            verify(reimburseService).doAction("reimb001", "user002", actionDTO);
        }
    }

    // ==================== POST /api/v1/reimburse/ocr-auto-fill ====================

    @Nested
    @DisplayName("POST /api/v1/reimburse/ocr-auto-fill - OCR自动填充预览")
    class OcrAutoFillTests {

        @Test
        @DisplayName("OCR自动填充预览 - 成功")
        void ocrAutoFill_Success() throws Exception {
            setUserIdAttribute("user001");
            OcrAutoFillVO ocrVO = new OcrAutoFillVO();
            ocrVO.setOcrRecordId("ocr001");
            ocrVO.setInvoiceNo("INV-12345");
            ocrVO.setSuggestedAmount(new BigDecimal("500.00"));
            ocrVO.setSuggestedExpenseType("TRANSPORT");
            ocrVO.setReliable(true);
            ocrVO.setSuggestedTitle("增值税发票-INV-12345");

            when(reimburseService.ocrAutoFill(eq("user001"), any(OcrAutoFillDTO.class)))
                    .thenReturn(ocrVO);

            OcrAutoFillDTO dto = new OcrAutoFillDTO();
            dto.setOcrRecordId("ocr001");
            dto.setTitle("发票报销");

            mockMvc.perform(post("/api/v1/reimburse/ocr-auto-fill")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.ocrRecordId").value("ocr001"))
                    .andExpect(jsonPath("$.data.invoiceNo").value("INV-12345"))
                    .andExpect(jsonPath("$.data.reliable").value(true));

            verify(reimburseService).ocrAutoFill(eq("user001"), any(OcrAutoFillDTO.class));
        }
    }

    // ==================== POST /api/v1/reimburse/ocr-auto-fill/create ====================

    @Nested
    @DisplayName("POST /api/v1/reimburse/ocr-auto-fill/create - OCR自动填充并创建")
    class OcrAutoFillCreateTests {

        @Test
        @DisplayName("OCR自动填充并创建 - 成功")
        void ocrAutoFillAndCreate_Success() throws Exception {
            setUserIdAttribute("user001");
            when(reimburseService.ocrAutoFillAndCreate(eq("user001"), any(OcrAutoFillDTO.class)))
                    .thenReturn(testReimburseVO);

            OcrAutoFillDTO dto = new OcrAutoFillDTO();
            dto.setOcrRecordId("ocr001");
            dto.setTitle("OCR自动填单");

            mockMvc.perform(post("/api/v1/reimburse/ocr-auto-fill/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto))
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value("reimb001"));

            verify(reimburseService).ocrAutoFillAndCreate(eq("user001"), any(OcrAutoFillDTO.class));
        }
    }

    // ==================== GET /api/v1/reimburse/pending ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse/pending - 获取待我审批列表")
    class GetPendingReimbursesTests {

        @Test
        @DisplayName("获取待我审批列表 - 成功")
        void getPendingReimburses_Success() throws Exception {
            setUserIdAttribute("user002");
            PageResult<ReimburseVO> pageResult = PageResult.of(1, 1, 10,
                    Collections.singletonList(testReimburseVO));
            when(reimburseService.getPendingReimburses("user002", 1, 10))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/v1/reimburse/pending")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .requestAttr("userId", "user002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1));

            verify(reimburseService).getPendingReimburses("user002", 1, 10);
        }
    }

    // ==================== GET /api/v1/reimburse/my ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse/my - 获取我的报销列表")
    class GetMyReimbursesTests {

        @Test
        @DisplayName("获取我的报销列表 - 成功")
        void getMyReimburses_Success() throws Exception {
            setUserIdAttribute("user001");
            PageResult<ReimburseVO> pageResult = PageResult.of(1, 1, 10,
                    Collections.singletonList(testReimburseVO));
            when(reimburseService.getMyReimburses("user001", 1, 10))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/v1/reimburse/my")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.total").value(1));

            verify(reimburseService).getMyReimburses("user001", 1, 10);
        }
    }

    // ==================== GET /api/v1/reimburse/pending/count ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse/pending/count - 获取待审批数量")
    class GetPendingCountTests {

        @Test
        @DisplayName("获取待审批数量 - 成功")
        void getPendingCount_Success() throws Exception {
            setUserIdAttribute("user002");
            when(reimburseService.countPending("user002")).thenReturn(5L);

            mockMvc.perform(get("/api/v1/reimburse/pending/count")
                            .requestAttr("userId", "user002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(5));

            verify(reimburseService).countPending("user002");
        }
    }

    // ==================== GET /api/v1/reimburse/{id}/invoices ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse/{id}/invoices - 获取发票列表")
    class GetInvoicesTests {

        @Test
        @DisplayName("获取发票列表 - 成功")
        void getInvoices_Success() throws Exception {
            setUserIdAttribute("user001");
            Invoice invoice = new Invoice();
            invoice.setId("inv001");
            invoice.setReimburseId("reimb001");
            invoice.setOcrRecordId("ocr001");

            when(reimburseService.getInvoicesByReimburseId("reimb001"))
                    .thenReturn(Collections.singletonList(invoice));

            mockMvc.perform(get("/api/v1/reimburse/reimb001/invoices")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data[0].id").value("inv001"));

            verify(reimburseService).getInvoicesByReimburseId("reimb001");
        }
    }

    // ==================== POST /api/v1/reimburse/invoices/{invoiceId}/verify ====================

    @Nested
    @DisplayName("POST /api/v1/reimburse/invoices/{invoiceId}/verify - 发票核验")
    class VerifyInvoiceTests {

        @Test
        @DisplayName("发票核验 - 成功")
        void verifyInvoice_Success() throws Exception {
            setUserIdAttribute("user002");
            when(reimburseService.verifyInvoice("inv001", "user002", 1, "核验通过"))
                    .thenReturn(true);

            mockMvc.perform(post("/api/v1/reimburse/invoices/inv001/verify")
                            .param("verified", "1")
                            .param("remark", "核验通过")
                            .requestAttr("userId", "user002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            verify(reimburseService).verifyInvoice("inv001", "user002", 1, "核验通过");
        }

        @Test
        @DisplayName("发票核验 - 失败")
        void verifyInvoice_Failure() throws Exception {
            setUserIdAttribute("user002");
            when(reimburseService.verifyInvoice("inv001", "user002", 0, "发票伪造"))
                    .thenReturn(false);

            mockMvc.perform(post("/api/v1/reimburse/invoices/inv001/verify")
                            .param("verified", "0")
                            .param("remark", "发票伪造")
                            .requestAttr("userId", "user002"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.message").value("发票核验失败"));

            verify(reimburseService).verifyInvoice("inv001", "user002", 0, "发票伪造");
        }
    }

    // ==================== GET /api/v1/reimburse/statistics ====================

    @Nested
    @DisplayName("GET /api/v1/reimburse/statistics - 获取统计")
    class GetStatisticsTests {

        @Test
        @DisplayName("获取统计 - 成功")
        void getStatistics_Success() throws Exception {
            setUserIdAttribute("user001");
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", 10L);
            stats.put("pendingCount", 3L);

            when(reimburseService.getStatistics("user001")).thenReturn(stats);

            mockMvc.perform(get("/api/v1/reimburse/statistics")
                            .requestAttr("userId", "user001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.totalCount").value(10))
                    .andExpect(jsonPath("$.data.pendingCount").value(3));

            verify(reimburseService).getStatistics("user001");
        }
    }
}