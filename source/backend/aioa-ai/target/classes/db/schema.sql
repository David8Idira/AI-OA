-- AI Model Configuration Table
CREATE TABLE IF NOT EXISTS `ai_model_config` (
    `id` varchar(32) NOT NULL COMMENT 'Primary key ID',
    `model_code` varchar(100) NOT NULL COMMENT 'Model code (gpt-4o, claude-3.5, kimi-pro)',
    `model_name` varchar(200) NOT NULL COMMENT 'Model name',
    `provider` varchar(100) NOT NULL COMMENT 'Provider (openai, anthropic, moonshot)',
    `endpoint` varchar(500) NOT NULL COMMENT 'API endpoint',
    `api_key` varchar(500) NOT NULL COMMENT 'API Key (encrypted)',
    `default_for` varchar(100) DEFAULT NULL COMMENT 'Default functions: CHAT,REPORT,IMAGE',
    `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Enabled: 0-disabled, 1-enabled',
    `daily_limit` int DEFAULT NULL COMMENT 'Daily limit (-1 for unlimited)',
    `today_usage` int NOT NULL DEFAULT '0' COMMENT 'Today usage count',
    `model_type` varchar(50) DEFAULT NULL COMMENT 'Model type: gpt4, claude, kimi',
    `sort_order` int NOT NULL DEFAULT '0' COMMENT 'Sort order',
    `remark` varchar(500) DEFAULT NULL COMMENT 'Remark',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `create_user` varchar(100) DEFAULT NULL COMMENT 'Create user',
    `update_user` varchar(100) DEFAULT NULL COMMENT 'Update user',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_model_code` (`model_code`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_provider` (`provider`),
    KEY `idx_default_for` (`default_for`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Model Configuration';

-- Initial data for AI models
INSERT INTO `ai_model_config` (`id`, `model_code`, `model_name`, `provider`, `endpoint`, `api_key`, `default_for`, `enabled`, `daily_limit`, `model_type`, `sort_order`, `remark`) VALUES
-- OpenAI GPT-4o
('1', 'gpt-4o', 'GPT-4o', 'openai', 'https://api.openai.com/v1/chat/completions', '${OPENAI_API_KEY}', 'CHAT,REPORT', 1, 1000, 'gpt4', 1, 'OpenAI最新模型，综合能力强'),
-- Claude 3.5 Sonnet
('2', 'claude-3.5', 'Claude 3.5 Sonnet', 'anthropic', 'https://api.anthropic.com/v1/messages', '${CLAUDE_API_KEY}', 'CHAT', 1, 800, 'claude', 2, 'Anthropic最新模型，逻辑推理强'),
-- Kimi Pro
('3', 'kimi-pro', 'Kimi Pro', 'moonshot', 'https://api.moonshot.cn/v1/chat/completions', '${MOONSHOT_API_KEY}', 'CHAT', 1, 1200, 'kimi', 3, '月之暗面Kimi，中文优化'),
-- MiniMax M2.7
('4', 'mimo-v2-pro', 'MiniMax M2.7 Pro', 'minimax', 'https://api.minimax.chat/v1/text/chatcompletion_pro', '${MINIMAX_API_KEY}', 'CHAT,IMAGE', 1, 1500, 'minimax', 4, 'MiniMax最新模型，中文表现优秀');

-- AI Quota Management Table
CREATE TABLE IF NOT EXISTS `ai_user_quota` (
    `id` varchar(32) NOT NULL COMMENT 'Primary key ID',
    `user_id` varchar(100) NOT NULL COMMENT 'User ID',
    `model_code` varchar(100) NOT NULL COMMENT 'Model code',
    `daily_limit` int NOT NULL DEFAULT '100000' COMMENT 'Daily token limit',
    `today_used` int NOT NULL DEFAULT '0' COMMENT 'Today used tokens',
    `monthly_limit` int DEFAULT NULL COMMENT 'Monthly token limit',
    `monthly_used` int NOT NULL DEFAULT '0' COMMENT 'Monthly used tokens',
    `total_used` bigint NOT NULL DEFAULT '0' COMMENT 'Total used tokens',
    `last_reset_date` date NOT NULL COMMENT 'Last reset date',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_model` (`user_id`, `model_code`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_model_code` (`model_code`),
    KEY `idx_last_reset_date` (`last_reset_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI User Quota Management';

-- AI Usage History Table
CREATE TABLE IF NOT EXISTS `ai_usage_history` (
    `id` varchar(32) NOT NULL COMMENT 'Primary key ID',
    `user_id` varchar(100) NOT NULL COMMENT 'User ID',
    `model_code` varchar(100) NOT NULL COMMENT 'Model code',
    `prompt_tokens` int NOT NULL DEFAULT '0' COMMENT 'Prompt tokens',
    `completion_tokens` int NOT NULL DEFAULT '0' COMMENT 'Completion tokens',
    `total_tokens` int NOT NULL DEFAULT '0' COMMENT 'Total tokens',
    `cost_usd` decimal(10,6) DEFAULT NULL COMMENT 'Cost in USD',
    `request_time` datetime NOT NULL COMMENT 'Request time',
    `response_time` datetime DEFAULT NULL COMMENT 'Response time',
    `duration_ms` int DEFAULT NULL COMMENT 'Request duration in milliseconds',
    `success` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'Success: 0-failed, 1-success',
    `error_message` varchar(1000) DEFAULT NULL COMMENT 'Error message',
    `request_content` varchar(2000) DEFAULT NULL COMMENT .Request content (truncated).,
    `response_content` varchar(2000) DEFAULT NULL COMMENT .Response content (truncated).,
    `api_endpoint` varchar(500) DEFAULT NULL COMMENT 'API endpoint',
    `request_id` varchar(100) DEFAULT NULL COMMENT 'Request ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_model_code` (`model_code`),
    KEY `idx_request_time` (`request_time`),
    KEY `idx_success` (`success`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI Usage History';

-- Index for performance optimization
CREATE INDEX IF NOT EXISTS idx_ai_usage_date ON `ai_usage_history` (`user_id`, `model_code`, DATE(`request_time`));
CREATE INDEX IF NOT EXISTS idx_ai_quota_reset ON `ai_user_quota` (`last_reset_date`, `user_id`);