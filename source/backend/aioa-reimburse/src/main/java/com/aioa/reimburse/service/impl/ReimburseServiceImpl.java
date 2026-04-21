package com.aioa.reimburse.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.entity.InvoiceRecord;
import com.aioa.ocr.service.OcrService;
import com.aioa.reimburse.dto.CreateReimburseDTO;
import com.aioa.reimburse.dto.OcrAutoFillDTO;
import com.aioa.reimburse.dto.ReimburseActionDTO;
import com.aioa.reimburse.dto.ReimburseItemDTO;
import com.aioa.reimburse.dto.ReimburseQueryDTO;
import com.aioa.reimburse.entity.Invoice;
import com.aioa.reimburse.entity.Reimburse;
import com.aioa.reimburse.entity.ReimburseItem;
import com.aioa.reimburse.enums.ExpenseTypeEnum;
import com.aioa.reimburse.enums.ReimburseActionEnum;
import com.aioa.reimburse.enums.ReimburseStatusEnum;
import com.aioa.reimburse.enums.ReimburseTypeEnum;
import com.aioa.reimburse.mapper.InvoiceMapper;
import com.aioa.reimburse.mapper.ReimburseItemMapper;
import com.aioa.reimburse.mapper.ReimburseMapper;
import com.aioa.reimburse.service.ReimburseService;
import com.aioa.reimburse.vo.OcrAutoFillVO;
import com.aioa.workflow.vo.PageResult;
import com.aioa.reimburse.vo.ReimburseItemVO;
import com.aioa.reimburse.vo.ReimburseVO;
import com.aioa.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reimburse Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReimburseServiceImpl extends ServiceImpl<ReimburseMapper, Reimburse> implements ReimburseService {

    private final ReimburseItemMapper reimburseItemMapper;
    private final InvoiceMapper invoiceMapper;

    @Autowired(required = false)
    private com.aioa.system.mapper.SysUserMapper sysUserMapper;

    @Autowired(required = false)
    private OcrService ocrService;

    private static final String SYSTEM_OPERATOR = "system";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReimburseVO createReimburse(String userId, CreateReimburseDTO dto) {
        log.info("Creating reimburse: userId={}, type={}, title={}", userId, dto.getType(), dto.getTitle());

        // Validate applicant
        SysUser applicant = getUserById(userId);
        if (applicant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Validate approver
        SysUser approver = getUserById(dto.getApproverId());
        if (approver == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "指定的审批人不存在");
        }

        // Calculate total amount from items
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReimburseItemDTO itemDto : dto.getItems()) {
            BigDecimal itemAmount = itemDto.getAmount();
            if (itemAmount == null) {
                itemAmount = itemDto.getQuantity().multiply(itemDto.getUnitPrice());
                if (itemDto.getTaxIncluded() != null && itemDto.getTaxIncluded() == 1 && itemDto.getTaxRate() != null) {
                    // Remove tax: amount = price * qty / (1 + taxRate/100)
                    BigDecimal divisor = BigDecimal.ONE.add(itemDto.getTaxRate().divide(BigDecimal.valueOf(100)));
                    itemAmount = itemAmount.divide(divisor, 2, java.math.RoundingMode.HALF_UP);
                }
            }
            if (itemDto.getTaxAmount() != null) {
                itemAmount = itemAmount.add(itemDto.getTaxAmount());
            }
            totalAmount = totalAmount.add(itemAmount);
        }

        // Build reimburse entity
        Reimburse reimburse = new Reimburse();
        reimburse.setTitle(dto.getTitle());
        reimburse.setType(dto.getType());
        reimburse.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CNY");
        reimburse.setStatus(ReimburseStatusEnum.PENDING.getCode());
        reimburse.setPriority(dto.getPriority() != null ? dto.getPriority() : 1);
        reimburse.setApplicantId(userId);
        reimburse.setApplicantName(getDisplayName(applicant));
        reimburse.setDeptId(applicant.getDeptId());
        reimburse.setDeptName(applicant.getDeptId()); // TODO: resolve dept name
        reimburse.setApproverId(dto.getApproverId());
        reimburse.setApproverName(getDisplayName(approver));
        reimburse.setCcUsers(dto.getCcUsers());
        reimburse.setReimburseDate(dto.getReimburseDate());
        reimburse.setExpectPayDate(dto.getExpectPayDate());
        reimburse.setPayMethod(dto.getPayMethod());
        reimburse.setBankAccount(dto.getBankAccount());
        reimburse.setBankName(dto.getBankName());
        reimburse.setDescription(dto.getDescription());
        reimburse.setAttachments(dto.getAttachments());
        reimburse.setOcrAutoFill(dto.getOcrAutoFill() != null ? dto.getOcrAutoFill() : 0);
        reimburse.setInvoiceCount(dto.getItems().size());
        reimburse.setReceiptAttached(0);
        reimburse.setTotalAmount(totalAmount);
        reimburse.setCreateBy(userId);
        reimburse.setUpdateBy(userId);

        // Save reimburse
        this.save(reimburse);

        // Save items and build invoice records
        List<Invoice> invoicesToSave = new ArrayList<>();
        int itemNo = 1;
        for (ReimburseItemDTO itemDto : dto.getItems()) {
            ReimburseItem item = buildReimburseItem(reimburse.getId(), itemDto, itemNo, userId);
            reimburseItemMapper.insert(item);

            // If OCR record ID provided, create invoice record from OCR
            if (StrUtil.isNotBlank(itemDto.getOcrRecordId())) {
                Invoice invoice = buildInvoiceFromOcr(reimburse.getId(), item.getId(), itemDto.getOcrRecordId(), userId);
                invoicesToSave.add(invoice);
            }
            itemNo++;
        }

        // Batch save invoices
        if (!invoicesToSave.isEmpty()) {
            invoiceMapper.batchInsert(invoicesToSave);
        }

        return getReimburseDetail(reimburse.getId(), userId);
    }

    @Override
    public ReimburseVO getReimburseDetail(String reimburseId, String userId) {
        Reimburse reimburse = this.getById(reimburseId);
        if (reimburse == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报销记录不存在");
        }

        ReimburseVO vo = convertToVO(reimburse);

        // Load items
        List<ReimburseItem> items = reimburseItemMapper.selectByReimburseId(reimburseId);
        vo.setItems(items.stream().map(this::convertItemToVO).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public PageResult<ReimburseVO> queryReimburses(String userId, ReimburseQueryDTO query) {
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Page<Reimburse> page = new Page<>(pageNum, pageSize);

        IPage<Reimburse> result;
        String mode = query.getMode() != null ? query.getMode() : "MY_APPLY";

        if ("MY_APPLY".equals(mode)) {
            result = baseMapper.selectByApplicantId(page, userId);
        } else if ("MY_APPROVE".equals(mode)) {
            result = baseMapper.selectPendingByApproverId(page, userId);
        } else {
            // Search with keyword
            result = baseMapper.searchByKeyword(page, query.getKeyword(), userId, userId, query.getType(), query.getStatus());
        }

        List<ReimburseVO> records = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReimburse(String reimburseId, String userId) {
        Reimburse reimburse = this.getById(reimburseId);
        if (reimburse == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报销记录不存在");
        }

        // Only applicant can delete their own reimburse, and only if it's draft or pending
        if (!reimburse.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除此报销单");
        }

        if (reimburse.getStatus() > ReimburseStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "当前状态不允许删除");
        }

        // Soft delete items
        reimburseItemMapper.deleteByReimburseId(reimburseId);

        // Soft delete reimburse
        return this.removeById(reimburseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReimburseVO doAction(String reimburseId, String userId, ReimburseActionDTO dto) {
        Reimburse reimburse = this.getById(reimburseId);
        if (reimburse == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报销记录不存在");
        }

        ReimburseActionEnum action = ReimburseActionEnum.getByCode(dto.getActionType());
        if (action == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的操作类型");
        }

        switch (action) {
            case APPROVE:
                return handleApprove(reimburse, userId, dto);
            case REJECT:
                return handleReject(reimburse, userId, dto);
            case CANCEL:
                return handleCancel(reimburse, userId, dto);
            case REQUEST_EXTRA:
                return handleRequestExtra(reimburse, userId, dto);
            default:
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不支持的操作");
        }
    }

    @Override
    public OcrAutoFillVO ocrAutoFill(String userId, OcrAutoFillDTO dto) {
        OcrAutoFillVO vo = new OcrAutoFillVO();
        vo.setOcrRecordId(dto.getOcrRecordId());

        // Fetch OCR record
        InvoiceRecord ocrRecord = null;
        if (ocrService != null) {
            try {
                ocrRecord = ocrService.getInvoiceRecordById(dto.getOcrRecordId());
            } catch (Exception e) {
                log.warn("Failed to fetch OCR record: {}", dto.getOcrRecordId(), e);
            }
        }

        if (ocrRecord == null) {
            vo.setRemark("未找到OCR识别记录，请确认记录ID正确");
            vo.setReliable(false);
            return vo;
        }

        // Map OCR data to auto-fill VO
        vo.setOcrConfidence(ocrRecord.getConfidence());
        vo.setInvoiceNo(ocrRecord.getInvoiceNo());
        vo.setInvoiceDate(ocrRecord.getInvoiceDate());
        vo.setSellerName(ocrRecord.getSellerName());
        vo.setOcrTotalAmount(ocrRecord.getTotalAmount());
        vo.setInvoiceType(ocrRecord.getInvoiceType());
        vo.setSuggestedAmount(ocrRecord.getTotalAmount());
        vo.setSuggestedExpenseDate(ocrRecord.getInvoiceDate());
        vo.setSuggestedDescription(buildDescriptionFromOcr(ocrRecord, dto.getDescriptionPrefix()));

        // Suggest expense type based on invoice type
        String expenseType = suggestExpenseType(ocrRecord.getInvoiceType(), dto.getExpenseType());
        vo.setSuggestedExpenseType(expenseType);
        vo.setSuggestedExpenseTypes(getSuggestedExpenseTypes(ocrRecord.getInvoiceType()));

        // Build suggested title
        if (StrUtil.isNotBlank(dto.getTitle())) {
            vo.setSuggestedTitle(dto.getTitle());
        } else {
            String invoiceTypeName = ocrRecord.getInvoiceTypeEnum() != null
                    ? ocrRecord.getInvoiceTypeEnum().getName() : "发票报销";
            vo.setSuggestedTitle(String.format("%s-%s", invoiceTypeName, ocrRecord.getInvoiceNo()));
        }

        // Determine reliability
        boolean reliable = ocrRecord.getConfidence() != null && ocrRecord.getConfidence() >= 0.8;
        vo.setReliable(reliable);
        if (!reliable) {
            vo.setRemark("OCR识别置信度较低(" + String.format("%.0f%%", ocrRecord.getConfidence() * 100) + ")，建议人工核对");
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReimburseVO ocrAutoFillAndCreate(String userId, OcrAutoFillDTO dto) {
        OcrAutoFillVO preview = ocrAutoFill(userId, dto);

        // Build create DTO from OCR preview
        CreateReimburseDTO createDto = new CreateReimburseDTO();
        createDto.setTitle(preview.getSuggestedTitle());
        createDto.setType(dto.getType() != null ? dto.getType() : ReimburseTypeEnum.OTHER.getCode());
        createDto.setCurrency("CNY");
        createDto.setPriority(1);
        createDto.setOcrAutoFill(1);

        ReimburseItemDTO itemDto = new ReimburseItemDTO();
        itemDto.setExpenseType(preview.getSuggestedExpenseType());
        itemDto.setDescription(preview.getSuggestedDescription());
        if (preview.getSuggestedExpenseDate() != null) {
            try {
                itemDto.setExpenseDate(LocalDate.parse(preview.getSuggestedExpenseDate()));
            } catch (Exception e) {
                itemDto.setExpenseDate(LocalDate.now());
            }
        } else {
            itemDto.setExpenseDate(LocalDate.now());
        }
        itemDto.setQuantity(BigDecimal.ONE);
        itemDto.setUnitPrice(preview.getSuggestedAmount());
        itemDto.setAmount(preview.getSuggestedAmount());
        itemDto.setOcrRecordId(dto.getOcrRecordId());
        itemDto.setInvoiceNo(preview.getInvoiceNo());
        itemDto.setReceiptAttached(1);

        createDto.setItems(Collections.singletonList(itemDto));

        return createReimburse(userId, createDto);
    }

    @Override
    public PageResult<ReimburseVO> getPendingReimburses(String userId, Integer pageNum, Integer pageSize) {
        pageNum = pageNum != null ? pageNum : 1;
        pageSize = pageSize != null ? pageSize : 10;
        Page<Reimburse> page = new Page<>(pageNum, pageSize);
        IPage<Reimburse> result = baseMapper.selectPendingByApproverId(page, userId);

        List<ReimburseVO> records = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public PageResult<ReimburseVO> getMyReimburses(String userId, Integer pageNum, Integer pageSize) {
        pageNum = pageNum != null ? pageNum : 1;
        pageSize = pageSize != null ? pageSize : 10;
        Page<Reimburse> page = new Page<>(pageNum, pageSize);
        IPage<Reimburse> result = baseMapper.selectByApplicantId(page, userId);

        List<ReimburseVO> records = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, records);
    }

    @Override
    public Long countPending(String approverId) {
        return baseMapper.countPendingByApproverId(approverId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyInvoice(String invoiceId, String userId, Integer verified, String verifyRemark) {
        Invoice invoice = invoiceMapper.selectById(invoiceId);
        if (invoice == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "发票不存在");
        }
        return invoiceMapper.updateVerifyStatus(invoiceId, verified, userId, verifyRemark) > 0;
    }

    @Override
    public Invoice getInvoiceById(String invoiceId) {
        return invoiceMapper.selectById(invoiceId);
    }

    @Override
    public List<Invoice> getInvoicesByReimburseId(String reimburseId) {
        return invoiceMapper.selectByReimburseId(reimburseId);
    }

    @Override
    public Map<String, Object> getStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", baseMapper.countByApplicantId(userId));
        stats.put("pendingCount", baseMapper.countPendingByApproverId(userId));
        return stats;
    }

    // ==================== Private Helper Methods ====================

    private ReimburseVO handleApprove(Reimburse reimburse, String userId, ReimburseActionDTO dto) {
        if (!reimburse.getApproverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权审批此报销单");
        }
        if (!ReimburseStatusEnum.PENDING.getCode().equals(reimburse.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        reimburse.setStatus(ReimburseStatusEnum.APPROVED.getCode());
        reimburse.setApprovalComment(dto.getComment());
        reimburse.setPayDate(LocalDateTime.now());
        reimburse.setUpdateBy(userId);
        reimburse.setUpdateTime(LocalDateTime.now());
        this.updateById(reimburse);

        return getReimburseDetail(reimburse.getId(), userId);
    }

    private ReimburseVO handleReject(Reimburse reimburse, String userId, ReimburseActionDTO dto) {
        if (!reimburse.getApproverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权审批此报销单");
        }
        if (!ReimburseStatusEnum.PENDING.getCode().equals(reimburse.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }
        if (StrUtil.isBlank(dto.getReason())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "驳回原因不能为空");
        }

        reimburse.setStatus(ReimburseStatusEnum.REJECTED.getCode());
        reimburse.setRejectReason(dto.getReason());
        reimburse.setApprovalComment(dto.getComment());
        reimburse.setUpdateBy(userId);
        reimburse.setUpdateTime(LocalDateTime.now());
        this.updateById(reimburse);

        return getReimburseDetail(reimburse.getId(), userId);
    }

    private ReimburseVO handleCancel(Reimburse reimburse, String userId, ReimburseActionDTO dto) {
        if (!reimburse.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权撤回此报销单");
        }
        if (!ReimburseStatusEnum.PENDING.getCode().equals(reimburse.getStatus())
                && !ReimburseStatusEnum.DRAFT.getCode().equals(reimburse.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        reimburse.setStatus(ReimburseStatusEnum.CANCELLED.getCode());
        reimburse.setCancelReason(dto.getReason());
        reimburse.setUpdateBy(userId);
        reimburse.setUpdateTime(LocalDateTime.now());
        this.updateById(reimburse);

        return getReimburseDetail(reimburse.getId(), userId);
    }

    private ReimburseVO handleRequestExtra(Reimburse reimburse, String userId, ReimburseActionDTO dto) {
        if (!reimburse.getApproverId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此报销单");
        }
        // Keep status as pending but add comment
        reimburse.setApprovalComment(dto.getComment());
        reimburse.setUpdateBy(userId);
        reimburse.setUpdateTime(LocalDateTime.now());
        this.updateById(reimburse);

        return getReimburseDetail(reimburse.getId(), userId);
    }

    private ReimburseVO convertToVO(Reimburse reimburse) {
        ReimburseVO vo = new ReimburseVO();
        vo.setId(reimburse.getId());
        vo.setTitle(reimburse.getTitle());
        vo.setType(reimburse.getType());
        vo.setTypeName(getTypeName(reimburse.getType()));
        vo.setTotalAmount(reimburse.getTotalAmount());
        vo.setCurrency(reimburse.getCurrency());
        vo.setStatus(reimburse.getStatus());
        vo.setStatusName(getStatusName(reimburse.getStatus()));
        vo.setPriority(reimburse.getPriority());
        vo.setPriorityName(getPriorityName(reimburse.getPriority()));
        vo.setApplicantId(reimburse.getApplicantId());
        vo.setApplicantName(reimburse.getApplicantName());
        vo.setDeptId(reimburse.getDeptId());
        vo.setDeptName(reimburse.getDeptName());
        vo.setApproverId(reimburse.getApproverId());
        vo.setApproverName(reimburse.getApproverName());
        vo.setReimburseDate(reimburse.getReimburseDate());
        vo.setExpectPayDate(reimburse.getExpectPayDate());
        vo.setPayDate(reimburse.getPayDate());
        vo.setPayMethod(reimburse.getPayMethod());
        vo.setDescription(reimburse.getDescription());
        vo.setApprovalComment(reimburse.getApprovalComment());
        vo.setRejectReason(reimburse.getRejectReason());
        vo.setCancelReason(reimburse.getCancelReason());
        vo.setOcrAutoFill(reimburse.getOcrAutoFill());
        vo.setInvoiceCount(reimburse.getInvoiceCount());
        vo.setReceiptAttached(reimburse.getReceiptAttached());
        vo.setCreateTime(reimburse.getCreateTime());
        vo.setUpdateTime(reimburse.getUpdateTime());
        return vo;
    }

    private ReimburseItemVO convertItemToVO(ReimburseItem item) {
        ReimburseItemVO vo = new ReimburseItemVO();
        vo.setId(item.getId());
        vo.setReimburseId(item.getReimburseId());
        vo.setItemNo(item.getItemNo());
        vo.setExpenseType(item.getExpenseType());
        vo.setExpenseTypeName(getExpenseTypeName(item.getExpenseType()));
        vo.setDescription(item.getDescription());
        vo.setExpenseDate(item.getExpenseDate());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setCurrency(item.getCurrency());
        vo.setTaxIncluded(item.getTaxIncluded());
        vo.setTaxRate(item.getTaxRate());
        vo.setTaxAmount(item.getTaxAmount());
        vo.setInvoiceId(item.getInvoiceId());
        vo.setInvoiceNo(item.getInvoiceNo());
        vo.setOriginPlace(item.getOriginPlace());
        vo.setDestination(item.getDestination());
        vo.setReceiptAttached(item.getReceiptAttached());
        vo.setAttachments(item.getAttachments());
        vo.setRemark(item.getRemark());
        vo.setCreateTime(item.getCreateTime());
        return vo;
    }

    private ReimburseItem buildReimburseItem(String reimburseId, ReimburseItemDTO dto, int itemNo, String userId) {
        ReimburseItem item = new ReimburseItem();
        item.setReimburseId(reimburseId);
        item.setItemNo(dto.getItemNo() != null ? dto.getItemNo() : itemNo);
        item.setExpenseType(dto.getExpenseType());
        item.setExpenseTypeName(getExpenseTypeName(dto.getExpenseType()));
        item.setDescription(dto.getDescription());
        item.setExpenseDate(dto.getExpenseDate());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "CNY");
        item.setTaxIncluded(dto.getTaxIncluded() != null ? dto.getTaxIncluded() : 0);
        item.setTaxRate(dto.getTaxRate());
        item.setTaxAmount(dto.getTaxAmount());
        // item.setOcrRecordId(dto.getOcrRecordId()); // TODO: Add ocrRecordId field to ReimburseItem entity
        item.setInvoiceNo(dto.getInvoiceNo());
        item.setOriginPlace(dto.getOriginPlace());
        item.setDestination(dto.getDestination());
        item.setReceiptAttached(dto.getReceiptAttached() != null ? dto.getReceiptAttached() : 0);
        item.setAttachments(dto.getAttachments());
        item.setRemark(dto.getRemark());
        item.setCreateBy(userId);
        item.setUpdateBy(userId);

        // Calculate amount
        BigDecimal amount = dto.getAmount();
        if (amount == null) {
            amount = dto.getQuantity().multiply(dto.getUnitPrice());
            if (dto.getTaxAmount() != null) {
                amount = amount.add(dto.getTaxAmount());
            }
        }
        item.setAmount(amount);

        return item;
    }

    private Invoice buildInvoiceFromOcr(String reimburseId, String itemId, String ocrRecordId, String userId) {
        Invoice invoice = new Invoice();
        invoice.setReimburseId(reimburseId);
        invoice.setReimburseItemId(itemId);
        invoice.setOcrRecordId(ocrRecordId);
        invoice.setSource("OCR");
        invoice.setVerified(0);
        invoice.setStatus("active");
        invoice.setCreateBy(userId);
        return invoice;
    }

    private SysUser getUserById(String userId) {
        if (sysUserMapper == null || StrUtil.isBlank(userId)) {
            return null;
        }
        return sysUserMapper.selectById(userId);
    }

    private String getDisplayName(SysUser user) {
        if (user == null) {
            return "";
        }
        return StrUtil.isNotBlank(user.getNickname()) ? user.getNickname() : user.getUsername();
    }

    private String getTypeName(String type) {
        ReimburseTypeEnum e = ReimburseTypeEnum.getByCode(type);
        return e != null ? e.getDesc() : type;
    }

    private String getStatusName(Integer status) {
        ReimburseStatusEnum e = ReimburseStatusEnum.getByCode(status);
        return e != null ? e.getDesc() : String.valueOf(status);
    }

    private String getPriorityName(Integer priority) {
        switch (priority) {
            case 0: return "低";
            case 1: return "普通";
            case 2: return "高";
            case 3: return "紧急";
            default: return String.valueOf(priority);
        }
    }

    private String getExpenseTypeName(String expenseType) {
        ExpenseTypeEnum e = ExpenseTypeEnum.getByCode(expenseType);
        return e != null ? e.getDesc() : expenseType;
    }

    private String suggestExpenseType(String invoiceType, String userSuggested) {
        if (StrUtil.isNotBlank(userSuggested)) {
            return userSuggested;
        }
        switch (invoiceType) {
            case "TAXI_RECEIPT":
                return ExpenseTypeEnum.TRANSPORT.getCode();
            case "TRAIN_TICKET":
                return ExpenseTypeEnum.TRANSPORT.getCode();
            case "AIR_TICKET":
                return ExpenseTypeEnum.TRANSPORT.getCode();
            case "VAT_INVOICE":
                return ExpenseTypeEnum.OTHER.getCode();
            default:
                return ExpenseTypeEnum.OTHER.getCode();
        }
    }

    private List<String> getSuggestedExpenseTypes(String invoiceType) {
        List<String> types = new ArrayList<>();
        switch (invoiceType) {
            case "TAXI_RECEIPT":
            case "TRAIN_TICKET":
            case "AIR_TICKET":
                types.add(ExpenseTypeEnum.TRANSPORT.getCode());
                break;
            default:
                types.add(ExpenseTypeEnum.OTHER.getCode());
        }
        types.add(ExpenseTypeEnum.MEAL.getCode());
        types.add(ExpenseTypeEnum.ENTERTAINMENT.getCode());
        return types;
    }

    private String buildDescriptionFromOcr(InvoiceRecord record, String prefix) {
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(prefix)) {
            sb.append(prefix).append(" - ");
        }
        if (StrUtil.isNotBlank(record.getInvoiceNo())) {
            sb.append("发票号: ").append(record.getInvoiceNo());
        }
        if (record.getTotalAmount() != null) {
            sb.append("，金额: ¥").append(record.getTotalAmount());
        }
        if (StrUtil.isNotBlank(record.getSellerName())) {
            sb.append("，销方: ").append(record.getSellerName());
        }
        if (StrUtil.isNotBlank(record.getInvoiceDate())) {
            sb.append("，日期: ").append(record.getInvoiceDate());
        }
        return sb.toString();
    }
}
