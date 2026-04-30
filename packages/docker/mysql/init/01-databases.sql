-- ============================================================
-- AI-OA Database Initialization Script
-- Creates all databases required by microservices
-- ============================================================

-- Main application database
CREATE DATABASE IF NOT EXISTS `ai_oa`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- Attendance module database
CREATE DATABASE IF NOT EXISTS `ai_oa_attendance`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- IM module database (short name as per im application.yml)
CREATE DATABASE IF NOT EXISTS `aioa`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- OCR module database
CREATE DATABASE IF NOT EXISTS `aioa_ocr`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- Knowledge base module database (can share ai_oa)
CREATE DATABASE IF NOT EXISTS `aioa_knowledge`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- Set default character set for all future tables
ALTER DATABASE `ai_oa` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE `ai_oa_attendance` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE `aioa` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE `aioa_ocr` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER DATABASE `aioa_knowledge` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant all privileges to root user for docker convenience
-- In production, create dedicated app users per database
GRANT ALL PRIVILEGES ON `ai_oa`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `ai_oa_attendance`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `aioa`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `aioa_ocr`.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON `aioa_knowledge`.* TO 'root'@'%';
FLUSH PRIVILEGES;
