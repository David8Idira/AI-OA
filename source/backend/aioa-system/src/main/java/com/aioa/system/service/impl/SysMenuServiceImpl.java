package com.aioa.system.service.impl;

import com.aioa.system.entity.SysMenu;
import com.aioa.system.mapper.SysMenuMapper;
import com.aioa.system.service.SysMenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Menu Service Implementation
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, 1)
                .orderByAsc(SysMenu::getSortOrder));
        return buildTree(allMenus, "0");
    }

    @Override
    public List<SysMenu> getMenuTreeByUserId(String userId) {
        List<SysMenu> menus = baseMapper.getMenusByUserId(userId);
        return buildTree(menus, "0");
    }

    @Override
    public List<String> getPermissionsByUserId(String userId) {
        return baseMapper.getPermissionsByUserId(userId);
    }

    @Override
    public List<Object> buildRouterMenus(String userId) {
        List<SysMenu> menus = baseMapper.getMenusByUserId(userId);
        return buildRouterTree(menus, "0");
    }

    /**
     * Build menu tree
     */
    private List<SysMenu> buildTree(List<SysMenu> menus, String parentId) {
        return menus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    m.setChildren(buildTree(menus, m.getId().toString()));
                    return m;
                })
                .collect(Collectors.toList());
    }

    /**
     * Build router-compatible menu tree
     */
    private List<Object> buildRouterTree(List<SysMenu> menus, String parentId) {
        List<Object> result = new ArrayList<>();
        List<SysMenu> children = menus.stream()
                .filter(m -> parentId.equals(m.getParentId()) && m.getVisible() == 1)
                .sorted(Comparator.comparing(SysMenu::getSortOrder))
                .collect(Collectors.toList());

        for (SysMenu menu : children) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("path", menu.getPath());
            item.put("name", menu.getMenuName());
            item.put("component", menu.getComponent());
            item.put("meta", buildMeta(menu));

            List<Object> childMenus = buildRouterTree(menus, menu.getId().toString());
            if (!childMenus.isEmpty()) {
                item.put("children", childMenus);
            }

            result.add(item);
        }
        return result;
    }

    private Map<String, Object> buildMeta(SysMenu menu) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("title", menu.getMenuName());
        meta.put("icon", menu.getIcon());
        meta.put("keepAlive", menu.getKeepAlive() == 1);
        return meta;
    }
}
