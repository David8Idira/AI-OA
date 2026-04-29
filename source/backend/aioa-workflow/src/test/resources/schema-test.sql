-- H2 Test Database Schema for Workflow Module
CREATE TABLE IF NOT EXISTS approval (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL,
    content TEXT,
    status INT DEFAULT 0,
    priority INT DEFAULT 1,
    applicant_id VARCHAR(64) NOT NULL,
    applicant_name VARCHAR(100),
    approver_id VARCHAR(64) NOT NULL,
    approver_name VARCHAR(100),
    dept_id VARCHAR(64),
    dept_name VARCHAR(100),
    cc_users VARCHAR(1000),
    expect_finish_time TIMESTAMP,
    finish_time TIMESTAMP,
    current_step INT DEFAULT 1,
    total_steps INT DEFAULT 1,
    attachments VARCHAR(2000),
    form_data TEXT,
    remark VARCHAR(500),
    approval_comment VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    create_by VARCHAR(64),
    update_by VARCHAR(64),
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS approval_record (
    id VARCHAR(64) PRIMARY KEY,
    approval_id VARCHAR(64) NOT NULL,
    operator_id VARCHAR(64) NOT NULL,
    operator_name VARCHAR(100),
    action_type INT NOT NULL,
    action_desc VARCHAR(50),
    comment VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_task (
    id VARCHAR(64) PRIMARY KEY,
    approval_id VARCHAR(64) NOT NULL,
    task_name VARCHAR(100) NOT NULL,
    assignee_id VARCHAR(64) NOT NULL,
    assignee_name VARCHAR(100),
    status INT DEFAULT 0,
    due_date TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS approval_cc (
    id VARCHAR(64) PRIMARY KEY,
    approval_id VARCHAR(64) NOT NULL,
    cc_user_id VARCHAR(64) NOT NULL,
    cc_user_name VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS approval_template (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    form_schema TEXT,
    approver_config TEXT,
    enable INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
