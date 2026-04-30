package com.aioa.knowledge.enums;

/**
 * 知识库文档密级枚举
 */
public enum SecurityLevel {
    
    TOP_SECRET("绝密级", 1, "top-secret"),
    SECRET("机密级", 2, "secret"),
    CONFIDENTIAL("秘密级", 3, "confidential"),
    INTERNAL("内部文件", 4, "internal"),
    PROJECT("项目文件", 5, "project"),
    PUBLIC("公开", 6, "public");
    
    private final String label;
    private final int level;
    private final String code;
    
    SecurityLevel(String label, int level, String code) {
        this.label = label;
        this.level = level;
        this.code = code;
    }
    
    public String getLabel() {
        return label;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getCode() {
        return code;
    }
    
    public static SecurityLevel fromCode(String code) {
        for (SecurityLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return PUBLIC;
    }
    
    /**
     * 判断用户角色是否可访问该密级
     * 密级数字越小越敏感，高密级需要更高角色权限
     */
    public static boolean canAccess(String docLevel, String userRoleCode, int userRoleLevel) {
        SecurityLevel required = fromCode(docLevel);
        // 用户角色等级需要 >= 文档密级要求的等级才能访问
        // 绝密级(1)需要角色等级1，公开(6)需要角色等级任意
        return userRoleLevel >= required.level;
    }
}