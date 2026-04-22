-- 预约系统完善：薪资模式 + 时间段 + 实时收入
-- 1. User 表新增薪资相关字段
ALTER TABLE `user` ADD COLUMN `salary_mode` TINYINT DEFAULT 0 COMMENT '薪资模式: 0-提成制, 1-固定薪资(时薪制)';
ALTER TABLE `user` ADD COLUMN `hourly_rate` DECIMAL(10,2) DEFAULT 18.00 COMMENT '正常时薪(元/小时)';
ALTER TABLE `user` ADD COLUMN `overtime_rate` DECIMAL(10,2) DEFAULT 25.00 COMMENT '加班时薪(元/小时)';
ALTER TABLE `user` ADD COLUMN `fixed_monthly_salary` DECIMAL(10,2) DEFAULT NULL COMMENT '固定月薪(固定薪资制时使用)';

-- 2. OrderInfo 表新增时间段字段
ALTER TABLE `order_info` ADD COLUMN `time_slot` VARCHAR(20) DEFAULT NULL COMMENT '预约时间段 如 09:00-10:00';
ALTER TABLE `order_info` ADD COLUMN `start_time` VARCHAR(10) DEFAULT NULL COMMENT '预约开始时间 HH:mm';
ALTER TABLE `order_info` ADD COLUMN `end_time` VARCHAR(10) DEFAULT NULL COMMENT '预约结束时间 HH:mm';
ALTER TABLE `order_info` ADD COLUMN `auto_assigned` TINYINT DEFAULT 0 COMMENT '是否自动分配: 0-否, 1-是';
