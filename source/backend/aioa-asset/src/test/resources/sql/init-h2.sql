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

-- 资产信息表 (与MySQL/Kingbase保持一致)
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
  status tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (id)
);

-- 资产标签表 (与MySQL/Kingbase保持一致)
CREATE TABLE IF NOT EXISTS asset_label (
  id bigint NOT NULL AUTO_INCREMENT,
  label_code varchar(50) NOT NULL,
  asset_id bigint NOT NULL,
  asset_code varchar(50) NOT NULL,
  asset_name varchar(200) NOT NULL,
  qr_content text DEFAULT NULL,
  qr_image_path varchar(500) DEFAULT NULL,
  barcode_content varchar(100) DEFAULT NULL,
  barcode_image_path varchar(500) DEFAULT NULL,
  template_id bigint DEFAULT NULL,
  template_name varchar(100) DEFAULT NULL,
  print_time datetime DEFAULT NULL,
  printer varchar(50) DEFAULT NULL,
  printer_id varchar(50) DEFAULT NULL,
  print_status tinyint DEFAULT 0 COMMENT '打印状态：0-未打印，1-已打印，2-打印失败',
  print_count int DEFAULT 0,
  last_print_time datetime DEFAULT NULL,
  label_status tinyint DEFAULT 1 COMMENT '标签状态：0-禁用，1-启用，2-作废',
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  remark varchar(500) DEFAULT NULL,
  PRIMARY KEY (id)
);

-- 库存记录表 (与MySQL/Kingbase保持一致)
CREATE TABLE IF NOT EXISTS stock_record (
  id bigint NOT NULL AUTO_INCREMENT,
  record_no varchar(50) NOT NULL,
  asset_id bigint NOT NULL,
  asset_code varchar(50) NOT NULL,
  asset_name varchar(200) NOT NULL,
  operation_type tinyint NOT NULL COMMENT '操作类型：1-入库，2-出库，3-盘点，4-调拨',
  sub_type smallint DEFAULT NULL COMMENT '操作子类型：101-采购入库，102-退货入库，201-销售出库，202-领用出库，203-报废出库',
  quantity int NOT NULL,
  before_quantity int DEFAULT NULL,
  after_quantity int DEFAULT NULL,
  unit_price decimal(10,2) DEFAULT NULL,
  total_amount decimal(10,2) DEFAULT NULL,
  batch_no varchar(50) DEFAULT NULL,
  partner varchar(200) DEFAULT NULL,
  warehouse varchar(100) NOT NULL,
  target_warehouse varchar(100) DEFAULT NULL,
  operator varchar(50) NOT NULL,
  operator_id varchar(50) DEFAULT NULL,
  operation_time datetime DEFAULT CURRENT_TIMESTAMP,
  scanned_label_code varchar(50) DEFAULT NULL,
  scan_method tinyint DEFAULT NULL,
  approval_status tinyint DEFAULT 0,
  approval_comment varchar(500) DEFAULT NULL,
  related_order_no varchar(50) DEFAULT NULL,
  status tinyint DEFAULT 0,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  update_by varchar(50) DEFAULT NULL,
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  remark varchar(500) DEFAULT NULL,
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
  update_time datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

-- 资产操作记录表 (与MySQL保持一致)
CREATE TABLE IF NOT EXISTS asset_operation (
  id bigint NOT NULL AUTO_INCREMENT,
  asset_id bigint NOT NULL,
  operation_type tinyint NOT NULL COMMENT '操作类型：1-登记，2-领用，3-归还，4-调拨，5-报废',
  operation_quantity int DEFAULT NULL,
  before_quantity int DEFAULT NULL,
  after_quantity int DEFAULT NULL,
  operation_person varchar(50) DEFAULT NULL,
  operation_person_id varchar(50) DEFAULT NULL,
  operation_time datetime DEFAULT NULL,
  related_order_no varchar(50) DEFAULT NULL,
  remark varchar(500) DEFAULT NULL,
  create_by varchar(50) DEFAULT NULL,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);