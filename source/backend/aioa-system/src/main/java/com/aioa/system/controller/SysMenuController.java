package com.aioa.system.controller;

import com.aioa.common.result.Result;
import com.aioa.system.entity.SysMenu;
import com.aioa.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Menu Controller
 */
@RestController
@RequestMapping("/api/v1/menus")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * Get menu tree
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> getTree() {
        return Result.success(sysMenuService.getMenuTree());
    }

    /**
     * Get menu tree by user ID
     */
    @GetMapping("/user/{userId}")
    public Result<List<SysMenu>> getByUserId(@PathVariable String userId) {
        return Result.success(sysMenuService.getMenuTreeByUserId(userId));
    }

    /**
     * Get router menus by user ID
     */
    @GetMapping("/user/{userId}/router")
    public Result<List<Object>> getRouterMenus(@PathVariable String userId) {
        return Result.success(sysMenuService.buildRouterMenus(userId));
    }

    /**
     * Get permissions by user ID
     */
    @GetMapping("/user/{userId}/permissions")
    public Result<List<String>> getPermissions(@PathVariable String userId) {
        return Result.success(sysMenuService.getPermissionsByUserId(userId));
    }

    /**
     * Get menu list
     */
    @GetMapping
    public Result<List<SysMenu>> list() {
        return Result.success(sysMenuService.list());
    }

    /**
     * Get menu detail
     */
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable String id) {
        return Result.success(sysMenuService.getById(id));
    }

    /**
     * Create menu
     */
    @PostMapping
    public Result<String> create(@RequestBody SysMenu menu) {
        sysMenuService.save(menu);
        return Result.success(menu.getId().toString());
    }

    /**
     * Update menu
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @RequestBody SysMenu menu) {
        menu.setId(id);
        sysMenuService.updateById(menu);
        return Result.success();
    }

    /**
     * Delete menu
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        sysMenuService.removeById(id);
        return Result.success();
    }
}
