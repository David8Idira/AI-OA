-- OCR Invoice Record Table
-- Invoice OCR recognition records storage

CREATE TABLE IF NOT EXISTS `ocr_invoice_record` (
    `id` VARCHAR(32) NOT NULL COMMENT 'Record ID',
    `user_id` VARCHAR(32) DEFAULT NULL COMMENT 'User ID',
    `invoice_type` VARCHAR(32) DEFAULT NULL COMMENT 'Invoice Type Code',
    `file_name` VARCHAR(255) DEFAULT NULL COMMENT 'Original File Name',
    `file_url` VARCHAR(512) DEFAULT NULL COMMENT 'File URL or Path',
    `confidence` DECIMAL(5,4) DEFAULT NULL COMMENT 'Recognition Confidence (0-1)',
    `status` VARCHAR(20) DEFAULT NULL COMMENT 'Status: pending/processing/success/failed',
    `invoice_no` VARCHAR(64) DEFAULT NULL COMMENT 'Invoice Number',
    `invoice_date` VARCHAR(32) DEFAULT NULL COMMENT 'Invoice Date',
    `total_amount` DECIMAL(16,2) DEFAULT NULL COMMENT 'Total Amount',
    `tax_amount` DECIMAL(16,2) DEFAULT NULL COMMENT 'Tax Amount',
    `invoice_code` VARCHAR(32) DEFAULT NULL COMMENT 'Invoice Code',
    `seller_name` VARCHAR(255) DEFAULT NULL COMMENT 'Seller Name',
    `seller_tax_id` VARCHAR(32) DEFAULT NULL COMMENT 'Seller Tax ID',
    `buyer_name` VARCHAR(255) DEFAULT NULL COMMENT 'Buyer Name',
    `buyer_tax_id` VARCHAR(32) DEFAULT NULL COMMENT 'Buyer Tax ID',
    `raw_result` TEXT COMMENT 'Raw OCR Result JSON',
    `error_message` VARCHAR(512) DEFAULT NULL COMMENT 'Error Message',
    `process_time` BIGINT DEFAULT NULL COMMENT 'Processing Time (ms)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create Time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update Time',
    `create_by` VARCHAR(64) DEFAULT NULL COMMENT 'Creator',
    `update_by` VARCHAR(64) DEFAULT NULL COMMENT 'Updater',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Logical Delete (0=normal, 1=deleted)',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_invoice_type` (`invoice_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='OCR Invoice Record Table';
