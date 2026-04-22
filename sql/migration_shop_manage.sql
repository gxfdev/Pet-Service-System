USE pet_service;

-- =============================================
-- 店主管理系统 - 数据库迁移
-- 1. 订单表新增执行员工字段（支持客户指定店员）
-- 2. 创建测试客户账号
-- =============================================

-- 1. OrderInfo 表增加 staff_id 字段
ALTER TABLE `order_info`
ADD COLUMN `staff_id` BIGINT DEFAULT NULL COMMENT '指定的执行店员ID' AFTER `provider_id`;

SELECT '✅ 店主管理迁移完成！' AS result;
