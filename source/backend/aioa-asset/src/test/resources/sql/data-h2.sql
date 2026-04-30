-- 插入测试数据
INSERT INTO asset_category (category_code, category_name, category_type, parent_id, level_tinyint, sort_order, status, remark) VALUES
('ASSET-FIXED', '固定资产', 1, 0, 1, 1, 1, '固定资产分类'),
('ASSET-CONSUMABLE', '消耗品', 2, 0, 1, 2, 1, '消耗品分类'),
('ASSET-OFFICE', '办公用品', 3, 0, 1, 3, 1, '办公用品分类');

-- 插入一些测试资产
INSERT INTO asset_info (asset_code, asset_name, category_id, model, supplier, purchase_price, current_quantity, warning_quantity, location, asset_status, status) VALUES
('ASSET-001', '笔记本电脑', 1, 'ThinkPad X1 Carbon', '联想', 12999.00, 20, 5, 'IT仓库', 1, 1),
('ASSET-002', '办公桌', 1, '标准办公桌', '办公家具公司', 1500.00, 50, 10, '办公区', 1, 1),
('ASSET-003', 'A4打印纸', 2, 'A4 80g', '文具供应商', 25.00, 100, 20, '文印室', 1, 1),
('ASSET-004', '签字笔', 3, '黑色0.5mm', '文具供应商', 2.50, 500, 100, '文具柜', 1, 1);