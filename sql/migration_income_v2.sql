USE pet_service;

-- =============================================
-- 店员收益管理系统 - 数据库迁移（固定提成+底薪模式）
-- =============================================

-- 1. User 表新增薪资配置字段
ALTER TABLE `user`
ADD COLUMN `base_salary` decimal(10,2) DEFAULT 0.00 COMMENT '月底薪（店员专属）' AFTER `staff_role`,
ADD COLUMN `commission_rate` decimal(10,2) DEFAULT 0.00 COMMENT '每单固定提成金额（店员专属）' AFTER `base_salary`;

-- 2. IncomeRecord 表新增提成计算字段
ALTER TABLE `income_record`
ADD COLUMN `commission_per_order` decimal(10,2) DEFAULT NULL COMMENT '本单固定提成金额' AFTER `order_amount`,
ADD COLUMN `calc_detail` text DEFAULT NULL COMMENT '提成计算明细(JSON)' AFTER `assigned_by`;

-- 3. 为已有店员设置默认薪资
-- 店员(所有 parent_provider_id 不为空且 staff_role=2 的用户)
UPDATE `user` SET base_salary = 3000.00, commission_rate = 15.00 
WHERE staff_role = 2 AND base_salary = 0.00;

-- 4. 更新已有 income_record 的提成信息（兼容旧数据）
UPDATE `income_record` r
JOIN `user` u ON r.staff_id = u.id
SET r.commission_per_order = r.staff_share,
    r.calc_detail = JSON_OBJECT(
      'orderAmount', r.order_amount,
      'commissionPerOrder', r.staff_share,
      'staffShare', r.staff_share,
      'shopShare', r.shop_share,
      'formula', CONCAT('员工收益 = 固定提成 ¥', r.staff_share),
      'shopFormula', CONCAT('店铺收入 = 订单金额 ¥', r.order_amount, ' - 提成 ¥', r.staff_share, ' = ¥', r.shop_share),
      'staffName', IFNULL(u.real_name, u.username)
    )
WHERE r.commission_per_order IS NULL;

SELECT '✅ 收益管理迁移完成！' AS result;
SELECT id, username, role, staff_role, base_salary, commission_rate FROM `user` WHERE role = 1 ORDER BY id;
