package com.aioa.hr.service;

import com.aioa.hr.dto.DepartmentDTO;
import com.aioa.hr.dto.DepartmentQueryDTO;
import com.aioa.hr.entity.Department;
import com.aioa.hr.mapper.DepartmentMapper;
import com.aioa.hr.service.impl.DepartmentServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * DepartmentServiceImpl 单元测试
 * 覆盖部门增删改查核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("部门服务测试")
class DepartmentServiceImplTest {

    @Mock
    private DepartmentMapper departmentMapper;

    /** Spy on real service so we can stub internal baseMapper calls */
    @Spy
    private DepartmentServiceImpl departmentService;

    private Department rootDepartment;
    private Department childDepartment;

    @BeforeEach
    void setUp() {
        // Inject the mock mapper into the spied service's baseMapper field
        ReflectionTestUtils.setField(departmentService, "baseMapper", departmentMapper);

        // 构造根部门
        rootDepartment = new Department();
        rootDepartment.setId(1L);
        rootDepartment.setDepartmentCode("DEPT001");
        rootDepartment.setDepartmentName("技术部");
        rootDepartment.setParentId(0L);
        rootDepartment.setLevel(1);
        rootDepartment.setManager("张三");
        rootDepartment.setStatus(1);
        rootDepartment.setSortOrder(1);
        rootDepartment.setCreateBy("system");
        rootDepartment.setCreateTime(LocalDateTime.now());

        // 构造子部门
        childDepartment = new Department();
        childDepartment.setId(2L);
        childDepartment.setDepartmentCode("DEPT002");
        childDepartment.setDepartmentName("研发组");
        childDepartment.setParentId(1L);
        childDepartment.setLevel(2);
        childDepartment.setManager("李四");
        childDepartment.setStatus(1);
        childDepartment.setSortOrder(1);
        childDepartment.setCreateBy("system");
        childDepartment.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("addDepartment - 新增根部门成功")
    void testAddDepartment_addRootDepartment_success() {
        // Given
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentName("财务部");
        dto.setManager("王五");
        dto.setStatus(1);
        dto.setSortOrder(2);

        when(departmentMapper.insert(any(Department.class))).thenReturn(1);

        // When
        boolean result = departmentService.addDepartment(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper).insert(captor.capture());

        Department saved = captor.getValue();
        assertNotNull(saved.getDepartmentCode());
        assertTrue(saved.getDepartmentCode().startsWith("DEPT"));
        assertEquals(0L, saved.getParentId());
        assertEquals(1, saved.getLevel());
        assertEquals("system", saved.getCreateBy());
    }

    @Test
    @DisplayName("addDepartment - 新增子部门成功，level自动设置为父部门+1")
    void testAddDepartment_addChildDepartment_correctLevel() {
        // Given: 父部门存在
        when(departmentMapper.selectById(1L)).thenReturn(rootDepartment);
        when(departmentMapper.insert(any(Department.class))).thenReturn(1);

        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentName("测试组");
        dto.setParentId(1L);
        dto.setStatus(1);
        dto.setSortOrder(1);

        // When
        boolean result = departmentService.addDepartment(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper).insert(captor.capture());

        Department saved = captor.getValue();
        assertEquals(2, saved.getLevel()); // 父级为1，当前应为2
    }

    @Test
    @DisplayName("addDepartment - 父部门不存在时，level降级为1")
    void testAddDepartment_parentNotFound_levelFallbackToOne() {
        // Given: 父部门不存在
        when(departmentMapper.selectById(999L)).thenReturn(null);
        when(departmentMapper.insert(any(Department.class))).thenReturn(1);

        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentName("孤儿部门");
        dto.setParentId(999L);
        dto.setStatus(1);

        // When
        boolean result = departmentService.addDepartment(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getLevel());
    }

    @Test
    @DisplayName("updateDepartment - 更新部门信息成功")
    void testUpdateDepartment_success() {
        // Given
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(1L);
        dto.setDepartmentName("技术中心");
        dto.setManager("赵六");
        dto.setParentId(0L);

        when(departmentMapper.selectById(0L)).thenReturn(null);
        when(departmentMapper.updateById(any(Department.class))).thenReturn(1);

        // When
        boolean result = departmentService.updateDepartment(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper).updateById(captor.capture());

        Department updated = captor.getValue();
        assertEquals("技术中心", updated.getDepartmentName());
        assertEquals("赵六", updated.getManager());
        assertEquals("system", updated.getUpdateBy());
        assertEquals(1, updated.getLevel());
    }

    @Test
    @DisplayName("deleteDepartment - 删除有子部门的部门应抛出异常")
    void testDeleteDepartment_hasChildren_throwsException() {
        // Given: 存在子部门
        when(departmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> departmentService.deleteDepartment(1L));

        assertTrue(exception.getMessage().contains("该部门下有子部门"));
        // removeById should NOT be called
        verify(departmentMapper, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("deleteDepartment - 删除无子部门的部门成功")
    void testDeleteDepartment_noChildren_success() {
        // Given: 无子部门，stub removeById to bypass MyBatis Plus tableInfo check
        when(departmentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        doReturn(true).when(departmentService).removeById(1L);

        // When
        boolean result = departmentService.deleteDepartment(1L);

        // Then
        assertTrue(result);
        verify(departmentService).removeById(1L);
    }

    @Test
    @DisplayName("getDepartmentById - 部门存在时返回DTO")
    void testGetDepartmentById_found() {
        // Given
        when(departmentMapper.selectById(1L)).thenReturn(rootDepartment);

        // When
        DepartmentDTO result = departmentService.getDepartmentById(1L);

        // Then
        assertNotNull(result);
        assertEquals("DEPT001", result.getDepartmentCode());
        assertEquals("技术部", result.getDepartmentName());
        assertEquals(1, result.getLevel());
    }

    @Test
    @DisplayName("getDepartmentById - 部门不存在时返回null")
    void testGetDepartmentById_notFound() {
        // Given
        when(departmentMapper.selectById(999L)).thenReturn(null);

        // When
        DepartmentDTO result = departmentService.getDepartmentById(999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("queryDepartmentPage - 分页查询成功")
    void testQueryDepartmentPage_success() {
        // Given
        DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setDepartmentName("技术");

        Page<Department> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(rootDepartment, childDepartment));
        page.setTotal(2);

        when(departmentMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        IPage<DepartmentDTO> result = departmentService.queryDepartmentPage(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotal());
        assertEquals(2, result.getRecords().size());
        assertEquals("技术部", result.getRecords().get(0).getDepartmentName());
    }

    @Test
    @DisplayName("queryDepartmentList - 条件查询成功")
    void testQueryDepartmentList_success() {
        // Given
        DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();
        queryDTO.setStatus(1);

        when(departmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(rootDepartment, childDepartment));

        // When
        List<DepartmentDTO> result = departmentService.queryDepartmentList(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("queryDepartmentList - 查询条件为空时返回所有记录")
    void testQueryDepartmentList_emptyQuery() {
        // Given
        DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(rootDepartment));

        // When
        List<DepartmentDTO> result = departmentService.queryDepartmentList(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getDepartmentTree - 查询启用的部门树成功")
    void testGetDepartmentTree_success() {
        // Given
        rootDepartment.setStatus(1);
        childDepartment.setStatus(1);
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(rootDepartment, childDepartment));

        // When
        List<DepartmentDTO> result = departmentService.getDepartmentTree();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("updateDepartmentStatus - 更新部门状态成功")
    void testUpdateDepartmentStatus_success() {
        // Given
        when(departmentMapper.updateById(any(Department.class))).thenReturn(1);

        // When
        boolean result = departmentService.updateDepartmentStatus(1L, 0);

        // Then
        assertTrue(result);
        ArgumentCaptor<Department> captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
        assertEquals("system", captor.getValue().getUpdateBy());
    }

    @Test
    @DisplayName("getDepartmentsByParentId - 根据父ID查询子部门成功")
    void testGetDepartmentsByParentId_success() {
        // Given
        when(departmentMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(childDepartment));

        // When
        List<DepartmentDTO> result = departmentService.getDepartmentsByParentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("研发组", result.get(0).getDepartmentName());
    }

    @Test
    @DisplayName("generateDepartmentCode - 生成格式正确的部门编码")
    void testGenerateDepartmentCode_correctFormat() {
        // When
        String code = departmentService.generateDepartmentCode();

        // Then
        assertNotNull(code);
        assertTrue(code.startsWith("DEPT"));
        assertTrue(code.length() > 4);
    }

    @Test
    @DisplayName("addDepartment - 数据库异常时抛出RuntimeException")
    void testAddDepartment_exception() {
        // Given
        DepartmentDTO dto = new DepartmentDTO();
        dto.setDepartmentName("测试部门");
        when(departmentMapper.insert(any(Department.class)))
                .thenThrow(new RuntimeException("DB error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> departmentService.addDepartment(dto));
        assertTrue(exception.getMessage().contains("新增部门失败"));
    }
}
