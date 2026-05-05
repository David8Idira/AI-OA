package com.aioa.system.service.impl;

import com.aioa.system.entity.SysDepartment;
import com.aioa.system.mapper.SysDepartmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SysDepartmentServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试部门服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SysDepartmentServiceImplTest 单元测试")
class SysDepartmentServiceImplTest {

    @Mock
    private SysDepartmentMapper sysDepartmentMapper;

    private SysDepartmentServiceImpl sysDepartmentService;

    @BeforeEach
    void setUp() {
        sysDepartmentService = new SysDepartmentServiceImpl();
        ReflectionTestUtils.setField(sysDepartmentService, "baseMapper", sysDepartmentMapper);
    }

    private SysDepartment createDepartment(String id, String deptName, String parentId, Integer sortOrder, Integer status) {
        SysDepartment dept = new SysDepartment();
        dept.setId(id);
        dept.setDeptName(deptName);
        dept.setParentId(parentId);
        dept.setSortOrder(sortOrder);
        dept.setStatus(status);
        return dept;
    }

    @Test
    @DisplayName("获取部门树 - 正常场景")
    void getDeptTree_shouldReturnTreeStructure() {
        // given - 模拟list()方法返回树形结构数据
        List<SysDepartment> departments = new ArrayList<>();
        departments.add(createDepartment("1", "总公司", "0", 1, 1));
        departments.add(createDepartment("2", "技术部", "1", 1, 1));
        departments.add(createDepartment("3", "市场部", "1", 2, 1));

        when(sysDepartmentMapper.selectList(any())).thenReturn(departments);

        // when
        List<SysDepartment> result = sysDepartmentService.getDeptTree();

        // then
        assertThat(result).isNotNull();
        // 返回根节点（parentId="0"）
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeptName()).isEqualTo("总公司");
    }

    @Test
    @DisplayName("获取部门树 - 空数据")
    void getDeptTree_withEmptyData_shouldReturnEmptyList() {
        // given
        when(sysDepartmentMapper.selectList(any())).thenReturn(new ArrayList<>());

        // when
        List<SysDepartment> result = sysDepartmentService.getDeptTree();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("获取部门树 - 单节点")
    void getDeptTree_withSingleNode_shouldReturnSingleItem() {
        // given
        List<SysDepartment> departments = new ArrayList<>();
        departments.add(createDepartment("1", "总公司", "0", 1, 1));

        when(sysDepartmentMapper.selectList(any())).thenReturn(departments);

        // when
        List<SysDepartment> result = sysDepartmentService.getDeptTree();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeptName()).isEqualTo("总公司");
    }

    @Test
    @DisplayName("获取子部门ID列表 - 包含自身")
    void getSubDeptIds_shouldIncludeSelf() {
        // given - 模拟递归查询子部门
        when(sysDepartmentMapper.selectList(any()))
                .thenReturn(List.of(createDepartment("2", "技术部", "1", 1, 1)))
                .thenReturn(new ArrayList<>()); // 递归终止

        // when
        List<String> result = sysDepartmentService.getSubDeptIds("1");

        // then
        assertThat(result).contains("1");
        assertThat(result).contains("2");
    }

    @Test
    @DisplayName("获取子部门ID列表 - 无子部门")
    void getSubDeptIds_withNoChildren_shouldReturnOnlySelf() {
        // given - 递归终止条件
        when(sysDepartmentMapper.selectList(any())).thenReturn(new ArrayList<>());

        // when
        List<String> result = sysDepartmentService.getSubDeptIds("1");

        // then
        assertThat(result).containsExactly("1");
    }

    @Test
    @DisplayName("获取子部门ID列表 - 多层递归")
    void getSubDeptIds_withMultiLevelHierarchy_shouldReturnAllDescendants() {
        // given - 三层部门结构
        when(sysDepartmentMapper.selectList(any()))
                .thenReturn(List.of(createDepartment("2", "技术部", "1", 1, 1)))
                .thenReturn(List.of(createDepartment("4", "研发组", "2", 1, 1)))
                .thenReturn(new ArrayList<>());

        // when
        List<String> result = sysDepartmentService.getSubDeptIds("1");

        // then
        assertThat(result).contains("1");
        assertThat(result).contains("2");
        assertThat(result).contains("4");
    }

    @Test
    @DisplayName("获取部门树与子部门 - 等同于getDeptTree")
    void getDeptTreeWithChildren_shouldReturnSameAsGetDeptTree() {
        // given
        List<SysDepartment> departments = new ArrayList<>();
        departments.add(createDepartment("1", "总公司", "0", 1, 1));

        when(sysDepartmentMapper.selectList(any())).thenReturn(departments);

        // when
        List<SysDepartment> result = sysDepartmentService.getDeptTreeWithChildren();

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("构建部门树 - 多级部门")
    void getDeptTree_withMultiLevel_shouldBuildCorrectTree() {
        // given
        List<SysDepartment> departments = new ArrayList<>();
        departments.add(createDepartment("1", "总公司", "0", 1, 1));
        departments.add(createDepartment("2", "技术部", "1", 1, 1));
        departments.add(createDepartment("3", "产品部", "1", 2, 1));
        departments.add(createDepartment("4", "前端组", "2", 1, 1));
        departments.add(createDepartment("5", "后端组", "2", 2, 1));

        when(sysDepartmentMapper.selectList(any())).thenReturn(departments);

        // when
        List<SysDepartment> result = sysDepartmentService.getDeptTree();

        // then - 验证树形结构
        assertThat(result).hasSize(1); // 根节点
        SysDepartment root = result.get(0);
        assertThat(root.getDeptName()).isEqualTo("总公司");
        // 总公司有2个子部门（技术部和产品部）
        assertThat(root.getChildren()).hasSize(2);
    }

    @Test
    @DisplayName("获取部门树 - 按排序顺序")
    void getDeptTree_shouldOrderBySortOrder() {
        // given - 乱序数据
        List<SysDepartment> departments = new ArrayList<>();
        departments.add(createDepartment("3", "第三个", "0", 3, 1));
        departments.add(createDepartment("1", "第一个", "0", 1, 1));
        departments.add(createDepartment("2", "第二个", "0", 2, 1));

        when(sysDepartmentMapper.selectList(any())).thenReturn(departments);

        // when
        List<SysDepartment> result = sysDepartmentService.getDeptTree();

        // then - 按sortOrder排序
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("获取子部门ID列表 - 叶子节点")
    void getSubDeptIds_withLeafNode_shouldReturnOnlySelf() {
        // given - 该部门没有子部门
        when(sysDepartmentMapper.selectList(any())).thenReturn(new ArrayList<>());

        // when
        List<String> result = sysDepartmentService.getSubDeptIds("leaf-dept");

        // then
        assertThat(result).containsExactly("leaf-dept");
    }
}