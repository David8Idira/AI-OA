package com.aioa.asset.service;

import com.aioa.asset.entity.AssetOperation;
import com.aioa.asset.mapper.AssetOperationMapper;
import com.aioa.asset.service.impl.AssetOperationServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AssetOperationServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssetOperationServiceImpl 单元测试")
class AssetOperationServiceImplTest {

    @Mock
    private AssetOperationMapper assetOperationMapper;

    private AssetOperationServiceImpl assetOperationService;

    @BeforeEach
    void setUp() {
        assetOperationService = new AssetOperationServiceImpl();
        ReflectionTestUtils.setField(assetOperationService, "baseMapper", assetOperationMapper);
    }

    private AssetOperation createOperation(Long id, Long assetId, Integer operationType) {
        AssetOperation operation = new AssetOperation();
        operation.setId(id);
        operation.setAssetId(assetId);
        operation.setOperationType(operationType);
        operation.setOperatorId("user001");
        operation.setOperator("测试用户");
        operation.setOperationTime(LocalDateTime.now());
        return operation;
    }

    @Test
    @DisplayName("分页查询操作记录")
    void pageOperations_shouldReturnPagedResults() {
        // given
        Page<AssetOperation> page = new Page<>(1, 10);
        AssetOperation operation = createOperation(1L, 1L, 1);
        Page<AssetOperation> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(operation));
        resultPage.setTotal(1);
        when(assetOperationMapper.selectPage(any(), any())).thenReturn(resultPage);

        // when
        Page<AssetOperation> result = assetOperationService.pageOperations(page, new AssetOperation());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("根据资产ID获取操作记录")
    void getOperationsByAssetId_shouldReturnFilteredResults() {
        // given
        Page<AssetOperation> page = new Page<>(1, 10);
        AssetOperation operation = createOperation(1L, 1L, 2);
        Page<AssetOperation> resultPage = new Page<>(1, 10);
        resultPage.setRecords(Arrays.asList(operation));
        when(assetOperationMapper.selectPage(any(), any())).thenReturn(resultPage);

        // when
        Page<AssetOperation> result = assetOperationService.getOperationsByAssetId(page, 1L);

        // then
        assertThat(result).isNotNull();
        verify(assetOperationMapper).selectPage(any(), any());
    }
}
