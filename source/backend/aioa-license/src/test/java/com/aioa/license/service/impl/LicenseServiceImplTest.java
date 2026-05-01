package com.aioa.license.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aioa.license.entity.LicenseInfo;
import com.aioa.license.mapper.LicenseInfoMapper;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LicenseServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("LicenseServiceImpl 测试")
class LicenseServiceImplTest {

    @Mock
    private LicenseInfoMapper licenseInfoMapper;

    private LicenseServiceImpl licenseService;

    private LicenseInfo testLicense;

    @BeforeEach
    void setUp() throws Exception {
        licenseService = new LicenseServiceImpl();

        // Inject baseMapper via reflection
        Field baseMapperField = licenseService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(licenseService, licenseInfoMapper);

        // Setup test license
        testLicense = new LicenseInfo();
        testLicense.setId(1L);
        testLicense.setLicenseNo("LIC-2024-001");
        testLicense.setLicenseName("营业执照");
        testLicense.setCategoryId(1L);
        testLicense.setIssuingAuthority("市场监督管理局");
        testLicense.setIssueDate(LocalDate.of(2024, 1, 1));
        testLicense.setValidFrom(LocalDate.of(2024, 1, 1));
        testLicense.setValidTo(LocalDate.of(2027, 1, 1));
        testLicense.setAnnualReviewDate(LocalDate.of(2025, 1, 1));
        testLicense.setReviewCycle(12);
        testLicense.setKeeper("张三");
        testLicense.setKeeperId("user001");
        testLicense.setKeeperDepartment("行政部");
        testLicense.setLicenseStatus(1);
        testLicense.setStatus(1);
        testLicense.setCreateBy("admin");
        testLicense.setCreateTime(LocalDateTime.now());
    }

    // ==================== 正常场景测试 ====================

    @Nested
    @DisplayName("正常场景测试")
    class NormalScenarios {

        @Test
        @DisplayName("分页查询证照列表 - 成功")
        void pageLicense_Success() {
            // given
            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(testLicense));

            LicenseInfo query = new LicenseInfo();
            query.setLicenseName("营业执照");

            // when
            List<LicenseInfo> result = licenseService.pageLicense(1, 10, query);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("营业执照", result.get(0).getLicenseName());
            verify(licenseInfoMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("分页查询证照列表 - 带分类条件")
        void pageLicense_WithCategory() {
            // given
            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(testLicense));

            LicenseInfo query = new LicenseInfo();
            query.setCategoryId(1L);
            query.setLicenseStatus(1);

            // when
            List<LicenseInfo> result = licenseService.pageLicense(1, 10, query);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取即将过期证照 - 成功")
        void getSoonExpiringLicenses_Success() {
            // given
            LicenseInfo expiringLicense = new LicenseInfo();
            expiringLicense.setId(2L);
            expiringLicense.setLicenseName("食品经营许可证");
            expiringLicense.setValidTo(LocalDate.now().plusDays(15));
            expiringLicense.setLicenseStatus(1);
            expiringLicense.setStatus(1);

            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(expiringLicense));

            // when
            List<LicenseInfo> result = licenseService.getSoonExpiringLicenses();

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getValidTo().isBefore(LocalDate.now().plusDays(31)));
        }

        @Test
        @DisplayName("获取已过期证照 - 成功")
        void getExpiredLicenses_Success() {
            // given
            LicenseInfo expiredLicense = new LicenseInfo();
            expiredLicense.setId(3L);
            expiredLicense.setLicenseName("已过期的许可证");
            expiredLicense.setValidTo(LocalDate.now().minusDays(30));
            expiredLicense.setLicenseStatus(4);
            expiredLicense.setStatus(1);

            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(expiredLicense));

            // when
            List<LicenseInfo> result = licenseService.getExpiredLicenses();

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getValidTo().isBefore(LocalDate.now()));
        }

        @Test
        @DisplayName("更新证照状态 - 成功")
        void updateLicenseStatus_Success() {
            // given
            when(licenseInfoMapper.selectById(1L)).thenReturn(testLicense);
            when(licenseInfoMapper.updateById(any(LicenseInfo.class))).thenReturn(1);

            // when
            boolean result = licenseService.updateLicenseStatus(1L, 2);

            // then
            assertTrue(result);
            verify(licenseInfoMapper, times(1)).selectById(1L);
            verify(licenseInfoMapper, times(1)).updateById(any(LicenseInfo.class));
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionScenarios {

        @Test
        @DisplayName("更新证照状态 - 证照不存在")
        void updateLicenseStatus_NotFound() {
            // given
            when(licenseInfoMapper.selectById(99L)).thenReturn(null);

            // when/then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> licenseService.updateLicenseStatus(99L, 2));
            assertTrue(ex.getMessage().contains("证照不存在"));
        }

        @Test
        @DisplayName("分页查询 - 查询条件为空")
        void pageLicense_EmptyQuery() {
            // given
            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(testLicense));

            // when
            List<LicenseInfo> result = licenseService.pageLicense(1, 10, null);

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryScenarios {

        @Test
        @DisplayName("分页查询 - 第一页")
        void pageLicense_FirstPage() {
            // given
            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(testLicense));

            // when
            List<LicenseInfo> result = licenseService.pageLicense(1, 10, new LicenseInfo());

            // then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("获取即将过期证照 - 无过期证照")
        void getSoonExpiringLicenses_Empty() {
            // given
            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // when
            List<LicenseInfo> result = licenseService.getSoonExpiringLicenses();

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("获取已过期证照 - 无已过期证照")
        void getExpiredLicenses_Empty() {
            // given
            when(licenseInfoMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            // when
            List<LicenseInfo> result = licenseService.getExpiredLicenses();

            // then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("更新证照状态 - 状态值边界")
        void updateLicenseStatus_BoundaryStatus() {
            // given
            when(licenseInfoMapper.selectById(1L)).thenReturn(testLicense);
            when(licenseInfoMapper.updateById(any(LicenseInfo.class))).thenReturn(1);

            // when - 测试状态为4（已过期）
            boolean result = licenseService.updateLicenseStatus(1L, 4);

            // then
            assertTrue(result);
        }
    }

    // ==================== Helper Methods ====================

    private LicenseInfo buildTestLicense(Long id, String name, LocalDate validTo, Integer status) {
        LicenseInfo license = new LicenseInfo();
        license.setId(id);
        license.setLicenseNo("LIC-" + id);
        license.setLicenseName(name);
        license.setValidTo(validTo);
        license.setLicenseStatus(status);
        license.setStatus(1);
        return license;
    }
}