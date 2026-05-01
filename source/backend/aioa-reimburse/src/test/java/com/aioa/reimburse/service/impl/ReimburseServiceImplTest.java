package com.aioa.reimburse.service.impl;

import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.ocr.entity.InvoiceRecord;
import com.aioa.ocr.enums.InvoiceType;
import com.aioa.ocr.service.OcrService;
import com.aioa.reimburse.dto.CreateReimburseDTO;
import com.aioa.reimburse.dto.OcrAutoFillDTO;
import com.aioa.reimburse.dto.ReimburseActionDTO;
import com.aioa.reimburse.dto.ReimburseItemDTO;
import com.aioa.reimburse.dto.ReimburseQueryDTO;
import com.aioa.reimburse.entity.Reimburse;
import com.aioa.reimburse.entity.ReimburseItem;
import com.aioa.reimburse.enums.ReimburseActionEnum;
import com.aioa.reimburse.enums.ReimburseStatusEnum;
import com.aioa.reimburse.enums.ReimburseTypeEnum;
import com.aioa.reimburse.mapper.InvoiceMapper;
import com.aioa.reimburse.mapper.ReimburseItemMapper;
import com.aioa.reimburse.mapper.ReimburseMapper;
import com.aioa.reimburse.vo.ReimburseVO;
import com.aioa.system.entity.SysUser;
import com.aioa.system.mapper.SysUserMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ReimburseServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReimburseServiceImpl 测试")
class ReimburseServiceImplTest {

    @Mock
    private ReimburseMapper reimburseMapper;

    @Mock
    private ReimburseItemMapper reimburseItemMapper;

    @Mock
    private InvoiceMapper invoiceMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private OcrService ocrService;

    private ReimburseServiceImpl reimburseService;

    private SysUser testApplicant;
    private SysUser testApprover;
    private Reimburse testReimburse;

    @BeforeEach
    void setUp() throws Exception {
        reimburseService = new ReimburseServiceImpl(reimburseItemMapper, invoiceMapper);

        // Inject sysUserMapper via reflection
        Field sysUserMapperField = ReimburseServiceImpl.class.getDeclaredField("sysUserMapper");
        sysUserMapperField.setAccessible(true);
        sysUserMapperField.set(reimburseService, sysUserMapper);

        // Inject ocrService via reflection
        Field ocrServiceField = ReimburseServiceImpl.class.getDeclaredField("ocrService");
        ocrServiceField.setAccessible(true);
        ocrServiceField.set(reimburseService, ocrService);

        // Inject baseMapper via reflection
        Field baseMapperField = reimburseService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(reimburseService, reimburseMapper);

        // Setup test users
        testApplicant = new SysUser();
        testApplicant.setId("user001");
        testApplicant.setUsername("zhangsan");
        testApplicant.setNickname("张三");
        testApplicant.setDeptId("dept001");

        testApprover = new SysUser();
        testApprover.setId("user002");
        testApprover.setUsername("lisi");
        testApprover.setNickname("李四");
        testApprover.setDeptId("dept001");

        // Setup test reimburse
        testReimburse = new Reimburse();
        testReimburse.setId("reimb001");
        testReimburse.setTitle("差旅报销测试");
        testReimburse.setType(ReimburseTypeEnum.BUSINESS_TRIP.getCode());
        testReimburse.setStatus(ReimburseStatusEnum.PENDING.getCode());
        testReimburse.setApplicantId("user001");
        testReimburse.setApplicantName("张三");
        testReimburse.setApproverId("user002");
        testReimburse.setApproverName("李四");
        testReimburse.setTotalAmount(new BigDecimal("1000.00"));
        testReimburse.setCurrency("CNY");
        testReimburse.setPriority(1);
        testReimburse.setReimburseDate(LocalDateTime.now());
        testReimburse.setCreateBy("user001");
        testReimburse.setUpdateBy("user001");
    }

    // ==================== Helper: Spy for inherited IService methods ====================

    private ReimburseServiceImpl createSpyService() throws Exception {
        ReimburseServiceImpl spy = spy(reimburseService);

        // Re-inject dependencies into the spy
        Field f1 = ReimburseServiceImpl.class.getDeclaredField("sysUserMapper");
        f1.setAccessible(true);
        f1.set(spy, sysUserMapper);

        Field f2 = ReimburseServiceImpl.class.getDeclaredField("ocrService");
        f2.setAccessible(true);
        f2.set(spy, ocrService);

        Field f3 = spy.getClass().getSuperclass().getDeclaredField("baseMapper");
        f3.setAccessible(true);
        f3.set(spy, reimburseMapper);

        return spy;
    }

    // ==================== 正常场景测试 ====================

    @Nested
    @DisplayName("正常场景测试")
    class NormalScenarios {

        @Test
        @DisplayName("获取报销单详情 - 成功")
        void getReimburseDetail_Success() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(testReimburse).when(spy).getById("reimb001");
            when(reimburseItemMapper.selectByReimburseId("reimb001")).thenReturn(new ArrayList<>());

            // when
            ReimburseVO result = spy.getReimburseDetail("reimb001", "user001");

            // then
            assertNotNull(result);
            assertEquals("reimb001", result.getId());
            assertEquals("张三", result.getApplicantName());
        }

        @Test
        @DisplayName("查询报销单列表 - 成功")
        void queryReimburses_Success() {
            // given
            Page<Reimburse> page = new Page<>(1, 10);
            page.setRecords(Collections.singletonList(testReimburse));
            page.setTotal(1);
            when(reimburseMapper.selectByApplicantId(any(Page.class), eq("user001"))).thenReturn(page);

            ReimburseQueryDTO query = new ReimburseQueryDTO();
            query.setMode("MY_APPLY");

            // when
            var result = reimburseService.queryReimburses("user001", query);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("审批通过报销单 - 成功")
        void handleApprove_Success() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(testReimburse).when(spy).getById("reimb001");
            when(reimburseMapper.updateById(any(Reimburse.class))).thenReturn(1);
            when(reimburseItemMapper.selectByReimburseId("reimb001")).thenReturn(new ArrayList<>());

            ReimburseActionDTO dto = new ReimburseActionDTO();
            dto.setActionType(ReimburseActionEnum.APPROVE.getCode());

            // when
            ReimburseVO result = spy.doAction("reimb001", "user002", dto);

            // then
            assertNotNull(result);
            verify(reimburseMapper).updateById(any(Reimburse.class));
        }

        @Test
        @DisplayName("OCR自动填充预览 - 成功")
        void ocrAutoFill_Success() {
            // given
            InvoiceRecord ocrRecord = new InvoiceRecord();
            ocrRecord.setId("ocr001");
            ocrRecord.setInvoiceNo("INV-12345");
            ocrRecord.setInvoiceType("VAT_INVOICE");
            ocrRecord.setTotalAmount(new BigDecimal("500.00"));
            ocrRecord.setConfidence(0.95);
            ocrRecord.setInvoiceDate("2024-01-15");
            ocrRecord.setSellerName("某科技有限公司");

            when(ocrService.getInvoiceRecordById("ocr001")).thenReturn(ocrRecord);

            OcrAutoFillDTO dto = new OcrAutoFillDTO();
            dto.setOcrRecordId("ocr001");
            dto.setTitle("发票报销");

            // when
            var result = reimburseService.ocrAutoFill("user001", dto);

            // then
            assertNotNull(result);
            assertEquals("ocr001", result.getOcrRecordId());
            assertEquals("INV-12345", result.getInvoiceNo());
            assertTrue(result.getReliable());
        }

        @Test
        @DisplayName("获取统计数据 - 成功")
        void getStatistics_Success() {
            // given
            when(reimburseMapper.countByApplicantId("user001")).thenReturn(5L);
            when(reimburseMapper.countPendingByApproverId("user001")).thenReturn(2L);

            // when
            var result = reimburseService.getStatistics("user001");

            // then
            assertNotNull(result);
            assertEquals(5L, result.get("totalCount"));
            assertEquals(2L, result.get("pendingCount"));
        }

        @Test
        @DisplayName("撤回报销申请 - 成功")
        void handleCancel_Success() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(testReimburse).when(spy).getById("reimb001");
            when(reimburseMapper.updateById(any(Reimburse.class))).thenReturn(1);
            when(reimburseItemMapper.selectByReimburseId("reimb001")).thenReturn(new ArrayList<>());

            ReimburseActionDTO dto = new ReimburseActionDTO();
            dto.setActionType(ReimburseActionEnum.CANCEL.getCode());

            // when
            ReimburseVO result = spy.doAction("reimb001", "user001", dto);

            // then
            assertNotNull(result);
            verify(reimburseMapper).updateById(any(Reimburse.class));
        }

        @Test
        @DisplayName("获取待我审批列表 - 成功")
        void queryPendingForApprover_Success() {
            // given
            Page<Reimburse> page = new Page<>(1, 10);
            page.setRecords(Collections.singletonList(testReimburse));
            page.setTotal(1);
            when(reimburseMapper.selectPendingByApproverId(any(Page.class), eq("user002"))).thenReturn(page);

            ReimburseQueryDTO query = new ReimburseQueryDTO();
            query.setMode("MY_APPROVE");

            // when
            var result = reimburseService.queryReimburses("user002", query);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionScenarios {

        @Test
        @DisplayName("获取报销单详情 - 报销单不存在")
        void getReimburseDetail_NotFound() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(null).when(spy).getById("reimb999");

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> spy.getReimburseDetail("reimb999", "user001"));
            assertTrue(ex.getMessage().contains("不存在"));
        }

        @Test
        @DisplayName("删除报销单 - 无权删除")
        void deleteReimburse_NoPermission() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(testReimburse).when(spy).getById("reimb001");

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> spy.deleteReimburse("reimb001", "user999"));
            assertTrue(ex.getMessage().contains("无权"));
        }

        @Test
        @DisplayName("删除报销单 - 非草稿状态不允许删除")
        void deleteReimburse_InvalidStatus() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            testReimburse.setStatus(ReimburseStatusEnum.APPROVED.getCode());
            doReturn(testReimburse).when(spy).getById("reimb001");

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> spy.deleteReimburse("reimb001", "user001"));
            assertTrue(ex.getMessage().contains("当前状态") || ex.getMessage().contains("不允许"));
        }

        @Test
        @DisplayName("OCR自动填充 - 未找到OCR记录")
        void ocrAutoFill_RecordNotFound() {
            // given
            when(ocrService.getInvoiceRecordById("ocr999")).thenReturn(null);

            OcrAutoFillDTO dto = new OcrAutoFillDTO();
            dto.setOcrRecordId("ocr999");

            // when
            var result = reimburseService.ocrAutoFill("user001", dto);

            // then
            assertNotNull(result);
            assertEquals("ocr999", result.getOcrRecordId());
            assertFalse(result.getReliable());
            assertTrue(result.getRemark().contains("未找到"));
        }

        @Test
        @DisplayName("报销单操作 - 无效的操作类型")
        void doAction_InvalidActionType() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(testReimburse).when(spy).getById("reimb001");

            ReimburseActionDTO dto = new ReimburseActionDTO();
            dto.setActionType("INVALID_ACTION");

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> spy.doAction("reimb001", "user002", dto));
            assertTrue(ex.getMessage().contains("无效") || ex.getMessage().contains("不支持"));
        }

        @Test
        @DisplayName("报销单操作 - 非待审批状态不能审批")
        void doAction_InvalidStatusForAction() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            testReimburse.setStatus(ReimburseStatusEnum.APPROVED.getCode());
            doReturn(testReimburse).when(spy).getById("reimb001");

            ReimburseActionDTO dto = new ReimburseActionDTO();
            dto.setActionType(ReimburseActionEnum.APPROVE.getCode());

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> spy.doAction("reimb001", "user002", dto));
            assertTrue(ex.getMessage().contains("状态") || ex.getResultCode() == ResultCode.APPROVAL_STATUS_ERROR);
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryScenarios {

        @Test
        @DisplayName("查询报销单 - 默认分页参数")
        void queryReimburses_DefaultPagination() {
            // given
            Page<Reimburse> page = new Page<>(1, 10);
            page.setRecords(Collections.singletonList(testReimburse));
            page.setTotal(1);
            when(reimburseMapper.selectByApplicantId(any(Page.class), eq("user001"))).thenReturn(page);

            ReimburseQueryDTO query = new ReimburseQueryDTO();

            // when
            var result = reimburseService.queryReimburses("user001", query);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotal());
        }

        @Test
        @DisplayName("OCR自动填充 - 低置信度标记")
        void ocrAutoFill_LowConfidence() {
            // given
            InvoiceRecord ocrRecord = new InvoiceRecord();
            ocrRecord.setId("ocr002");
            ocrRecord.setInvoiceNo("INV-99999");
            ocrRecord.setInvoiceType("VAT_INVOICE");
            ocrRecord.setTotalAmount(new BigDecimal("100.00"));
            ocrRecord.setConfidence(0.5);
            ocrRecord.setInvoiceDate("2024-01-15");
            ocrRecord.setInvoiceTypeEnum(InvoiceType.VAT_INVOICE);

            when(ocrService.getInvoiceRecordById("ocr002")).thenReturn(ocrRecord);

            OcrAutoFillDTO dto = new OcrAutoFillDTO();
            dto.setOcrRecordId("ocr002");

            // when
            var result = reimburseService.ocrAutoFill("user001", dto);

            // then
            assertNotNull(result);
            assertFalse(result.getReliable());
            assertTrue(result.getRemark().contains("置信度较低"));
        }

        @Test
        @DisplayName("驳回报销单 - 成功")
        void handleReject_Success() throws Exception {
            // given
            ReimburseServiceImpl spy = createSpyService();
            doReturn(testReimburse).when(spy).getById("reimb001");
            when(reimburseMapper.updateById(any(Reimburse.class))).thenReturn(1);
            when(reimburseItemMapper.selectByReimburseId("reimb001")).thenReturn(new ArrayList<>());

            ReimburseActionDTO dto = new ReimburseActionDTO();
            dto.setActionType(ReimburseActionEnum.REJECT.getCode());
            dto.setReason("材料不全，请补充");

            // when
            ReimburseVO result = spy.doAction("reimb001", "user002", dto);

            // then
            assertNotNull(result);
            verify(reimburseMapper).updateById(any(Reimburse.class));
        }
    }

    // ==================== Helper Methods ====================

    private CreateReimburseDTO buildCreateReimburseDTO() {
        CreateReimburseDTO dto = new CreateReimburseDTO();
        dto.setTitle("差旅报销测试");
        dto.setType(ReimburseTypeEnum.BUSINESS_TRIP.getCode());
        dto.setCurrency("CNY");
        dto.setPriority(1);
        dto.setApproverId("user002");
        dto.setReimburseDate(LocalDateTime.now());
        dto.setItems(Collections.singletonList(buildItemDTO()));
        return dto;
    }

    private ReimburseItemDTO buildItemDTO() {
        ReimburseItemDTO item = new ReimburseItemDTO();
        item.setExpenseType("TRANSPORT");
        item.setDescription("出差交通费");
        item.setExpenseDate(LocalDate.now());
        item.setQuantity(new BigDecimal("2"));
        item.setUnitPrice(new BigDecimal("250"));
        item.setAmount(new BigDecimal("500"));
        return item;
    }
}
