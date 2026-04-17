package com.aioa.system.service.impl;

import com.aioa.system.entity.SysDepartment;
import com.aioa.system.mapper.SysDepartmentMapper;
import com.aioa.system.service.SysDepartmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Department Service Implementation
 */
@Service
public class SysDepartmentServiceImpl extends ServiceImpl<SysDepartmentMapper, SysDepartment> implements SysDepartmentService {

    @Override
    public List<SysDepartment> getDeptTree() {
        List<SysDepartment> allDepts = list(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getStatus, 1)
                .orderByAsc(SysDepartment::getSortOrder));
        return buildTree(allDepts, "0");
    }

    @Override
    public List<SysDepartment> getDeptTreeWithChildren() {
        return getDeptTree();
    }

    @Override
    public List<String> getSubDeptIds(String deptId) {
        List<String> result = new ArrayList<>();
        result.add(deptId);
        collectSubDeptIds(deptId, result);
        return result;
    }

    /**
     * Build department tree
     */
    private List<SysDepartment> buildTree(List<SysDepartment> depts, String parentId) {
        return depts.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .map(d -> {
                    d.setChildren(buildTree(depts, d.getId().toString()));
                    return d;
                })
                .collect(Collectors.toList());
    }

    /**
     * Recursively collect sub-department IDs
     */
    private void collectSubDeptIds(String deptId, List<String> result) {
        List<SysDepartment> children = list(new LambdaQueryWrapper<SysDepartment>()
                .eq(SysDepartment::getParentId, deptId));
        for (SysDepartment child : children) {
            result.add(child.getId().toString());
            collectSubDeptIds(child.getId().toString(), result);
        }
    }
}
