-- 资产分类表
CREATE TABLE IF NOT EXISTS asset_category (
  id bigint NOT NULL AUTO_INCREMENT,
  category_code varchar(50) NOT NULL,
  category_name varchar(100) NOT NULL,
  category_type tinyint NOT NULL,
  parent_id bigint DEFAULT 0,
  level_tinyint tinyint DEFAULT 1,
  sort_order int DEFAULT 0,
  status tinyint DEFAULT 1,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (id)
);

-- 资产信息表
CREATE TABLE IF NOT EXISTS asset_info (
  id bigint NOT NULL AUTO_INCREMENT,
  asset_code varchar(50) NOT NULL,
  asset_name varchar(200) NOT NULL,
  category_id bigint NOT NULL,
  model varchar(100) DEFAULT NULL,
  specification varchar(500) DEFAULT NULL,
  manufacturer varchar(200) DEFAULT NULL,
  supplier varchar(200) DEFAULT NULL,
  purchase_date date DEFAULT NULL,
  purchase_price decimal(10,2) DEFAULT '0.00',
  unit varchar(20) DEFAULT NULL,
  current_quantity int DEFAULT 0,
  warning_quantity int DEFAULT 0,
  location varchar(200) DEFAULT NULL,
  responsible_person varchar(50) DEFAULT NULL,
  responsible_person_id varchar(50) DEFAULT NULL,
  asset_status tinyint DEFAULT 1 COMMENT '资产状态：1-正常，2-领用中，3-维修中，4-报废',
  status tinyint DEFAULT 1,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (id)
);

-- 资产标签表
CREATE TABLE IF NOT EXISTS asset_label (
  id bigint NOT NULL AUTO_INCREMENT,
  asset_id bigint NOT NULL,
  asset_code varchar(50) NOT NULL,
  asset_name varchar(200) NOT NULL,
  label_code varchar(100) NOT NULL,
  label_type tinyint DEFAULT 1,
  qr_content varchar(500) DEFAULT NULL,
  barcode_content varchar(200) DEFAULT NULL,
  print_status tinyint DEFAULT 0,
  label_status tinyint DEFAULT 1,
  print_count int DEFAULT 0,
  last_print_time datetime DEFAULT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- 库存记录表
CREATE TABLE IF NOT EXISTS stock_record (
  id bigint NOT NULL AUTO_INCREMENT,
  category_id bigint NOT NULL,
  stock_type varchar(20) NOT NULL,
  quantity int NOT NULL,
  unit_price decimal(12,2) DEFAULT NULL,
  total_amount decimal(12,2) DEFAULT NULL,
  related_order_no varchar(50) DEFAULT NULL,
  operator varchar(50) DEFAULT NULL,
  remark varchar(500) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- 办公用品申请表
CREATE TABLE IF NOT EXISTS office_supply_request (
  id bigint NOT NULL AUTO_INCREMENT,
  request_no varchar(50) NOT NULL,
  applicant_id varchar(50) NOT NULL,
  department_id bigint DEFAULT NULL,
  request_date date NOT NULL,
  total_quantity int DEFAULT 0,
  total_amount decimal(12,2) DEFAULT NULL,
  status varchar(20) DEFAULT 'PENDING',
  approve_result varchar(20) DEFAULT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);
