-- =========================================================
-- AI-OA Approval Workflow Module Database Schema
-- Version: 1.0.0
-- =========================================================

-- Approval Main Table
CREATE TABLE IF NOT EXISTS `approval` (
    `id` VARCHAR(64) NOT NULL COMMENT 'Primary key (Snowflake ID)',
    `title` VARCHAR(200) NOT NULL COMMENT 'Approval title',
    `type` VARCHAR(50) NOT NULL COMMENT 'Approval type (LEAVE, EXPENSE, PURCHASE, OVERTIME, TRAVEL, etc.)',
    `content` TEXT COMMENT 'Approval content/description',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0-Pending, 1-Approved, 2-Rejected, 3-Cancelled, 4-Transferred',
    `priority` TINYINT NOT NULL DEFAULT 1 COMMENT 'Priority: 0-Low, 1-Normal, 2-High, 3-Urgent',
    `applicant_id` VARCHAR(64) NOT NULL COMMENT 'Applicant user ID',
    `applicant_name` VARCHAR(100) COMMENT 'Applicant name (denormalized)',
    `approver_id` VARCHAR(64) NOT NULL COMMENT 'Current approver user ID',
    `approver_name` VARCHAR(100) COMMENT 'Approver name (denormalized)',
    `dept_id` VARCHAR(64) COMMENT 'Department ID',
    `dept_name` VARCHAR(100) COMMENT 'Department name (denormalized)',
    `cc_users` VARCHAR(1000) COMMENT 'CC users, comma-separated user IDs',
    `expect_finish_time` DATETIME COMMENT 'Expected completion date',
    `finish_time` DATETIME COMMENT 'Actual completion time',
    `current_step` INT NOT NULL DEFAULT 1 COMMENT 'Current approval step (for multi-step flow)',
    `total_steps` INT NOT NULL DEFAULT 1 COMMENT 'Total steps in approval flow',
    `attachments` VARCHAR(2000) COMMENT 'Attachment URLs (comma-separated)',
    `form_data` TEXT COMMENT 'Flexible form data (JSON)',
    `remark` VARCHAR(500) COMMENT 'Applicant remark',
    `approval_comment` VARCHAR(500) COMMENT 'Approver result comment',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `create_by` VARCHAR(64) COMMENT 'Creator user ID',
    `update_by` VARCHAR(64) COMMENT 'Updater user ID',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT 'Logical delete: 0-not deleted, 1-deleted',
    PRIMARY KEY (`id`),
    KEY `idx_applicant_id` (`applicant_id`),
    KEY `idx_approver_id` (`approver_id`),
    KEY `idx_status` (`status`),
    KEY `idx_type` (`type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval main table';

-- Approval Record Table (audit trail)
CREATE TABLE IF NOT EXISTS `approval_record` (
    `id` VARCHAR(64) NOT NULL COMMENT 'Primary key (Snowflake ID)',
    `approval_id` VARCHAR(64) NOT NULL COMMENT 'Approval ID reference',
    `operator_id` VARCHAR(64) NOT NULL COMMENT 'Operator user ID',
    `operator_name` VARCHAR(100) COMMENT 'Operator name (denormalized)',
    `action_type` TINYINT NOT NULL COMMENT 'Action type: 1-Approve, 2-Reject, 3-Transfer, 4-Cancel',
    `action_desc` VARCHAR(50) COMMENT 'Action description',
    `comment` VARCHAR(500) COMMENT 'Action comment/reason',
    `status_after` TINYINT COMMENT 'Status after this action',
    `step` INT DEFAULT 0 COMMENT 'Step number in multi-step flow',
    `transfer_to_id` VARCHAR(64) COMMENT 'Transfer target user ID (for transfer action)',
    `transfer_to_name` VARCHAR(100) COMMENT 'Transfer target user name (for transfer action)',
    `previous_approver_id` VARCHAR(64) COMMENT 'Previous approver ID (for transfer tracking)',
    `next_approver_id` VARCHAR(64) COMMENT 'Next approver ID (for multi-step tracking)',
    `operator_ip` VARCHAR(50) COMMENT 'Operator IP address',
    `user_agent` VARCHAR(500) COMMENT 'Operator user agent',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    `attachments` VARCHAR(2000) COMMENT 'Attachments related to this record',
    PRIMARY KEY (`id`),
    KEY `idx_approval_id` (`approval_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_action_type` (`action_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Approval record table (audit trail)';

-- =========================================================
-- Common Approval Types Reference Data
-- =========================================================
-- LEAVE     - 请假
-- EXPENSE   - 报销
-- PURCHASE  - 采购
-- OVERTIME  - 加班
-- TRAVEL    - 出差
-- RECRUIT   - 招聘
-- CONTRACT  - 合同
-- =========================================================
