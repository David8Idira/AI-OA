package com.aioa.hr.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PageResult VO 单元测试
 * 覆盖分页返回结果的构造和计算逻辑
 */
@DisplayName("PageResult VO Tests")
class PageResultTest {

    @Test
    @DisplayName("构造函数正确设置所有字段")
    void constructor_setsAllFields() {
        PageResult<String> pageResult = new PageResult<>(
                List.of("a", "b", "c"), 100L, 1L, 20L
        );
        assertEquals(List.of("a", "b", "c"), pageResult.getRecords());
        assertEquals(100L, pageResult.getTotal());
        assertEquals(1L, pageResult.getCurrent());
        assertEquals(20L, pageResult.getSize());
    }

    @Test
    @DisplayName("total=0时 pages=0")
    void totalZero_pagesZero() {
        PageResult<String> pageResult = new PageResult<>(List.of(), 0L, 1L, 10L);
        assertEquals(0L, pageResult.getPages());
    }

    @Test
    @DisplayName("total=100, size=20时 pages=5")
    void pages_calculation() {
        PageResult<String> pageResult = new PageResult<>(List.of(), 100L, 1L, 20L);
        assertEquals(5L, pageResult.getPages());
    }

    @Test
    @DisplayName("total=101, size=20时 pages=6（向上取整）")
    void pages_ceilCalculation() {
        PageResult<String> pageResult = new PageResult<>(List.of(), 101L, 1L, 20L);
        assertEquals(6L, pageResult.getPages());
    }

    @Test
    @DisplayName("total=1, size=20时 pages=1")
    void pages_singlePage() {
        PageResult<String> pageResult = new PageResult<>(List.of("x"), 1L, 1L, 20L);
        assertEquals(1L, pageResult.getPages());
    }

    @Test
    @DisplayName("空构造函数")
    void defaultConstructor() {
        PageResult<String> pageResult = new PageResult<>();
        assertNull(pageResult.getRecords());
        assertNull(pageResult.getTotal());
        assertNull(pageResult.getCurrent());
        assertNull(pageResult.getSize());
    }

    @Test
    @DisplayName("带pages的构造函数")
    void constructor_withPages() {
        PageResult<String> pageResult = new PageResult<>(
                List.of("a"), 50L, 2L, 10L, 5L
        );
        assertEquals(5L, pageResult.getPages());
    }

    @Test
    @DisplayName("分页数据可以是复杂对象")
    void complexObjects() {
        PageResult<List<Integer>> pageResult = new PageResult<>(
                List.of(List.of(1, 2), List.of(3, 4)), 2L, 1L, 10L
        );
        assertEquals(2, pageResult.getRecords().size());
        assertEquals(List.of(1, 2), pageResult.getRecords().get(0));
    }
}