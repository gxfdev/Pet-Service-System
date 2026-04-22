USE pet_service;

-- =============================================
-- 权限管理 & 收入分配系统 - 数据库迁移
-- =============================================

-- 1. User 表新增字段：店员归属、店内角色
ALTER TABLE `user` 
ADD COLUMN `parent_provider_id` bigint DEFAULT NULL COMMENT '上级/所属服务商(店长)ID' AFTER `status`,
ADD COLUMN `staff_role` tinyint DEFAULT NULL COMMENT '1=店长 2=店员' AFTER `parent_provider_id`;

-- 2. 创建收入记录表
CREATE TABLE IF NOT EXISTS `income_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `provider_id` bigint NOT NULL COMMENT '店铺(服务商)ID',
  `staff_id` bigint NOT NULL COMMENT '执行员工ID',
  `order_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `staff_share` decimal(10,2) DEFAULT NULL COMMENT '员工分成金额',
  `shop_share` decimal(10,2) DEFAULT NULL COMMENT '店铺收入金额',
  `status` tinyint DEFAULT 0 COMMENT '0-待分配 1-已分配 2-已结算',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `assigned_by` bigint DEFAULT NULL COMMENT '分配操作人(店长)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_provider_id` (`provider_id`),
  KEY `idx_staff_id` (`staff_id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收入记录表';

-- 3. 设置现有用户为默认角色（都是店长）
-- provider1(id=7): 设为店长(staff_role=1)
-- provider2(id=8): 设为店长(staff_role=1)
UPDATE `user` SET staff_role = 1 WHERE role = 1 AND staff_role IS NULL;

SELECT 'Migration completed!' AS result;
SELECT id, username, role, shop_name, staff_role, parent_provider_id FROM user WHERE role >= 1 ORDER BY id;
