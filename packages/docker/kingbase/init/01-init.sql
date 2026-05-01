-- ============================================================
-- AI-OA Kingbase 数据库初始化脚本
-- 适配金仓数据库 Kingbase V9
-- ============================================================

-- 创建主数据库
CREATE DATABASE ai_oa;

-- 考勤模块数据库
CREATE DATABASE ai_oa_attendance;

-- OCR模块数据库
CREATE DATABASE aioa_ocr;

-- 知识库模块数据库
CREATE DATABASE aioa_knowledge;

-- IM模块数据库
CREATE DATABASE aioa;

-- 设置默认字符集
ALTER DATABASE ai_oa SET standard_conforming_strings = on;
ALTER DATABASE ai_oa_attendance SET standard_conforming_strings = on;
ALTER DATABASE aioa_ocr SET standard_conforming_strings = on;
ALTER DATABASE aioa_knowledge SET standard_conforming_strings = on;
ALTER DATABASE aioa SET standard_conforming_strings = on;

-- 创建应用用户并授权
-- 注意：Kingbase 默认管理员为 system
GRANT ALL PRIVILEGES ON DATABASE ai_oa TO system;
GRANT ALL PRIVILEGES ON DATABASE ai_oa_attendance TO system;
GRANT ALL PRIVILEGES ON DATABASE aioa_ocr TO system;
GRANT ALL PRIVILEGES ON DATABASE aioa_knowledge TO system;
GRANT ALL PRIVILEGES ON DATABASE aioa TO system;