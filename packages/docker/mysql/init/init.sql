-- ============================================================
-- AI-OA 完整数据库初始化脚本
-- 包含所有模块表结构
-- ============================================================

USE `ai_oa`;

-- ============================================================
-- 1. 系统管理模块 (aioa-system)
-- ============================================================

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(200) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `dept_id` bigint DEFAULT NULL COMMENT '部门ID',
  `status` tinyint DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_type` varchar(20) DEFAULT 'custom' COMMENT '角色类型：system-builtin, custom',
  `data_scope` varchar(50) DEFAULT NULL COMMENT '数据范围',
  `knowledge_access_level` int DEFAULT 6 COMMENT '知识库访问等级：1-6',
  `allowed_security_levels` varchar(500) DEFAULT NULL COMMENT '允许的保密级别JSON',
  `status` tinyint DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT 0 COMMENT '父部门ID',
  `dept_name` varchar(100) NOT NULL COMMENT '部门名称',
  `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编码',
  `sort_order` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT 0,
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `menu_code` varchar(50) DEFAULT NULL COMMENT '菜单编码',
  `menu_type` varchar(10) DEFAULT NULL COMMENT '菜单类型：menu, button',
  `path` varchar(200) DEFAULT NULL COMMENT '路由路径',
  `icon` varchar(100) DEFAULT NULL COMMENT '图标',
  `sort_order` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

-- ============================================================
-- 2. 工作流模块 (aioa-workflow)
-- ============================================================

CREATE TABLE IF NOT EXISTS `approval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审批ID',
  `title` varchar(200) NOT NULL COMMENT '审批标题',
  `content` text COMMENT '审批内容',
  `type` varchar(50) DEFAULT NULL COMMENT '审批类型',
  `priority` tinyint DEFAULT 1 COMMENT '优先级：1低，2中，3高',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending, approved, rejected, cancelled',
  `applicant_id` varchar(50) NOT NULL COMMENT '申请人ID',
  `applicant_name` varchar(100) DEFAULT NULL COMMENT '申请人姓名',
  `current_node` varchar(100) DEFAULT NULL COMMENT '当前节点',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批主表';

CREATE TABLE IF NOT EXISTS `approval_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approval_id` bigint NOT NULL COMMENT '审批ID',
  `action` varchar(20) NOT NULL COMMENT '操作：approve, reject, transfer, cancel',
  `approver_id` varchar(50) NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(100) DEFAULT NULL COMMENT '审批人姓名',
  `comment` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `node_name` varchar(100) DEFAULT NULL COMMENT '节点名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_approval_id` (`approval_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ============================================================
-- 3. 知识库模块 (aioa-knowledge)
-- ============================================================

CREATE TABLE IF NOT EXISTS `knowledge_doc` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `content` text COMMENT '文档内容',
  `summary` varchar(500) DEFAULT NULL COMMENT '文档摘要',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签JSON',
  `doc_type` varchar(20) DEFAULT 'article' COMMENT '文档类型：article, faq, manual',
  `status` varchar(20) DEFAULT 'published' COMMENT '状态：draft, published',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `like_count` int DEFAULT 0 COMMENT '点赞次数',
  `security_level` varchar(20) DEFAULT 'public' COMMENT '密级：top-secret, secret, confidential, internal, project, public',
  `allowed_roles` varchar(500) DEFAULT NULL COMMENT '允许访问的角色JSON',
  `vector_id` varchar(100) DEFAULT NULL COMMENT '向量ID',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- ============================================================
-- 4. 资产模块 (aioa-asset)
-- ============================================================

CREATE TABLE IF NOT EXISTS `asset_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category_code` varchar(50) NOT NULL COMMENT '分类编码',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_type` tinyint NOT NULL COMMENT '分类类型：1固定资产，2消耗品，3办公用品',
  `parent_id` bigint DEFAULT 0 COMMENT '父分类ID',
  `level` int DEFAULT 1 COMMENT '层级',
  `sort_order` int DEFAULT 0 COMMENT '排序',
  `status` tinyint DEFAULT 1 COMMENT '状态',
  `remark` varchar(500) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产分类表';

CREATE TABLE IF NOT EXISTS `asset_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asset_code` varchar(50) NOT NULL COMMENT '资产编码',
  `asset_name` varchar(200) NOT NULL COMMENT '资产名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `specification` varchar(500) DEFAULT NULL COMMENT '规格',
  `manufacturer` varchar(200) DEFAULT NULL COMMENT '厂商',
  `supplier` varchar(200) DEFAULT NULL COMMENT '供应商',
  `purchase_date` date DEFAULT NULL COMMENT '购买日期',
  `purchase_price` decimal(10,2) DEFAULT '0.00' COMMENT '购买价格',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `current_quantity` int DEFAULT 0 COMMENT '当前数量',
  `warning_quantity` int DEFAULT 0 COMMENT '预警数量',
  `location` varchar(200) DEFAULT NULL COMMENT '存放位置',
  `responsible_person` varchar(50) DEFAULT NULL COMMENT '负责人',
  `asset_status` tinyint DEFAULT 1 COMMENT '资产状态：1正常，2领用中，3维修中，4报废',
  `status` tinyint DEFAULT 1 COMMENT '状态：0禁用，1启用',
  `remark` varchar(500) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_code` (`asset_code`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产信息表';

-- ============================================================
-- 5. 考勤模块 (aioa-attendance)
-- ============================================================

CREATE TABLE IF NOT EXISTS `attendance_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户姓名',
  `check_type` varchar(10) NOT NULL COMMENT '打卡类型：sign_in, sign_out',
  `check_time` datetime NOT NULL COMMENT '打卡时间',
  `location` varchar(200) DEFAULT NULL COMMENT '打卡位置',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '经度',
  `device` varchar(50) DEFAULT NULL COMMENT '设备',
  `status` tinyint DEFAULT 1 COMMENT '状态：0异常，1正常',
  `remark` varchar(500) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_check_time` (`check_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤记录表';

CREATE TABLE IF NOT EXISTS `leave_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(50) NOT NULL COMMENT '用户ID',
  `leave_type` varchar(20) NOT NULL COMMENT '请假类型：annual, sick, personal, other',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `reason` varchar(500) DEFAULT NULL COMMENT '请假原因',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending, approved, rejected',
  `approve_result` varchar(20) DEFAULT NULL COMMENT '审批结果',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假记录表';

-- ============================================================
-- 6. 报表模块 (aioa-report)
-- ============================================================

CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '报表标题',
  `type` varchar(50) DEFAULT NULL COMMENT '报表类型',
  `content` text COMMENT '报表内容(JSON)',
  `creator_id` varchar(50) DEFAULT NULL COMMENT '创建人ID',
  `creator_name` varchar(100) DEFAULT NULL COMMENT '创建人姓名',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft, published',
  `template_id` bigint DEFAULT NULL COMMENT '模板ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表表';

-- ============================================================
-- 7. IM模块 (aioa-im)
-- ============================================================

USE `aioa`;

CREATE TABLE IF NOT EXISTS `conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_type` varchar(20) NOT NULL COMMENT '会话类型：private, group',
  `name` varchar(100) DEFAULT NULL COMMENT '会话名称(群聊)',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `owner_id` varchar(50) DEFAULT NULL COMMENT '群主ID',
  `last_message_id` bigint DEFAULT NULL COMMENT '最后消息ID',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `unread_count` int DEFAULT 0 COMMENT '未读消息数',
  `status` tinyint DEFAULT 1 COMMENT '状态：0删除，1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';

CREATE TABLE IF NOT EXISTS `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` varchar(50) NOT NULL COMMENT '发送者ID',
  `sender_name` varchar(100) DEFAULT NULL COMMENT '发送者姓名',
  `sender_type` varchar(20) DEFAULT 'user' COMMENT '发送者类型：user, system',
  `message_type` varchar(20) NOT NULL COMMENT '消息类型：text, image, file, audio',
  `content` text COMMENT '消息内容',
  `status` tinyint DEFAULT 1 COMMENT '状态：0删除，1正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_conversation_id` (`conversation_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ============================================================
-- 8. 初始化数据
-- ============================================================

USE `ai_oa`;

-- 插入默认管理员角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `role_type`, `knowledge_access_level`, `status`, `sort_order`) VALUES
(1, 'SUPER_ADMIN', '超级管理员', 'system-builtin', 1, 1, 1),
(2, 'ADMIN', '管理员', 'system-builtin', 2, 1, 2),
(3, 'USER', '普通用户', 'custom', 6, 1, 3),
(4, 'HR', '人事专员', 'custom', 4, 1, 4),
(5, 'FINANCE', '财务专员', 'custom', 3, 1, 5);

-- 插入默认管理员用户 (密码: admin123)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `status`) VALUES
(1, 'admin', '$2a$10$XXXXX', '系统管理员', 'admin@aioa.com', 1);

-- 插入默认部门
INSERT INTO `sys_department` (`id`, `dept_name`, `dept_code`, `sort_order`, `status`) VALUES
(1, '总公司', 'HQ', 1, 1),
(2, '技术部', 'TECH', 2, 1),
(3, '市场部', 'MARKET', 3, 1),
(4, '人事部', 'HR', 4, 1),
(5, '财务部', 'FINANCE', 5, 1);

-- 插入默认知识库分类
USE `aioa_knowledge`;
INSERT INTO `knowledge_doc` (`title`, `content`, `doc_type`, `status`, `security_level`) VALUES
('欢迎使用AI-OA系统', '这是知识库的使用说明...', 'manual', 'published', 'public');

