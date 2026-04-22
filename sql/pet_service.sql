CREATE DATABASE IF NOT EXISTS pet_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pet_service;

-- =============================================
-- 1. 用户表 user
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(128) NOT NULL COMMENT '加密密码',
  `phone` varchar(11) NOT NULL COMMENT '手机号',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '0-普通用户, 1-服务商, 2-管理员',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `real_name` varchar(32) DEFAULT NULL COMMENT '真实姓名（服务商需认证）',
  `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号（服务商）',
  `shop_name` varchar(64) DEFAULT NULL COMMENT '店铺名称（服务商）',
  `status` tinyint DEFAULT 1 COMMENT '0-禁用, 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 宠物表 pet
-- =============================================
DROP TABLE IF EXISTS `pet`;
CREATE TABLE `pet` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '宠物ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `name` varchar(32) NOT NULL COMMENT '宠物昵称',
  `type` varchar(16) DEFAULT NULL COMMENT '狗/猫/其他',
  `breed` varchar(32) DEFAULT NULL COMMENT '品种',
  `gender` tinyint DEFAULT NULL COMMENT '0-母, 1-公',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `weight` decimal(5,2) DEFAULT NULL COMMENT '体重(kg)',
  `photo` varchar(255) DEFAULT NULL COMMENT '照片URL',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物表';

-- =============================================
-- 3. 服务项目表 service_item
-- =============================================
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '服务ID',
  `name` varchar(64) NOT NULL COMMENT '服务名称',
  `category` tinyint DEFAULT NULL COMMENT '1-美容, 2-寄养, 3-医疗, 4-遛狗',
  `price` decimal(10,2) DEFAULT NULL COMMENT '参考价格（元）',
  `duration` int DEFAULT NULL COMMENT '服务时长（分钟）',
  `description` text COMMENT '服务描述',
  `image` varchar(255) DEFAULT NULL COMMENT '展示图',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- =============================================
-- 4. 服务商服务关联表 provider_service
-- =============================================
DROP TABLE IF EXISTS `provider_service`;
CREATE TABLE `provider_service` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_id` bigint NOT NULL COMMENT '服务商ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `price` decimal(10,2) DEFAULT NULL COMMENT '服务商自定义价格',
  `status` tinyint DEFAULT 1 COMMENT '0-下架, 1-上架',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_provider_service` (`provider_id`, `service_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务商服务关联表';

-- =============================================
-- 5. 订单表 order_info (order是关键字)
-- =============================================
DROP TABLE IF EXISTS `order_info`;
CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单号',
  `order_no` varchar(32) NOT NULL COMMENT '业务订单号',
  `user_id` bigint NOT NULL COMMENT '下单用户',
  `provider_id` bigint NOT NULL COMMENT '服务商ID',
  `service_id` bigint NOT NULL COMMENT '服务项目ID',
  `pet_id` bigint NOT NULL COMMENT '服务宠物',
  `appointment_time` datetime DEFAULT NULL COMMENT '预约时间',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `status` tinyint DEFAULT 0 COMMENT '0-待支付, 1-待服务, 2-服务中, 3-已完成, 4-已取消, 5-退款中',
  `remark` varchar(255) DEFAULT NULL COMMENT '用户备注',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `pay_time` datetime DEFAULT NULL,
  `complete_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_provider_id` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 6. 评价表 review
-- =============================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '评价人',
  `provider_id` bigint NOT NULL COMMENT '被评价服务商',
  `rating` tinyint NOT NULL COMMENT '评分1-5',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `images` varchar(1000) DEFAULT NULL COMMENT '图片URL列表(JSON)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_provider_id` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- =============================================
-- 7. 地址表 address
-- =============================================
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver` varchar(32) NOT NULL COMMENT '收货人/联系人',
  `phone` varchar(11) NOT NULL COMMENT '联系电话',
  `province` varchar(32) DEFAULT NULL COMMENT '省',
  `city` varchar(32) DEFAULT NULL COMMENT '市',
  `district` varchar(32) DEFAULT NULL COMMENT '区',
  `detail` varchar(128) DEFAULT NULL COMMENT '详细地址',
  `is_default` tinyint DEFAULT 0 COMMENT '0-否,1-是默认',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地址表';

-- =============================================
-- 初始化测试数据
-- =============================================

-- 管理员账号 (密码: 123456, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `phone`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13800000001', 2, 1);

-- 普通用户
INSERT INTO `user` (`username`, `password`, `phone`, `role`, `status`) VALUES
('user1', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13912345678', 0, 1),
('user2', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13923456789', 0, 1);

-- 服务商
INSERT INTO `user` (`username`, `password`, `phone`, `role`, `real_name`, `shop_name`, `status`) VALUES
('provider1', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13987654321', 1, '张三', '萌宠屋', 1),
('provider2', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13998765432', 1, '李四', '爱宠驿站', 1);

-- 服务项目
INSERT INTO `service_item` (`name`, `category`, `price`, `duration`, `description`) VALUES
('精致洗护', 1, 88.00, 60, '包含洗澡、吹干、修指甲、清洁耳朵'),
('基础洗护', 1, 58.00, 45, '基础洗澡、吹干、简单护理'),
('标准寄养', 2, 120.00, 1440, '24小时专人看护，提供狗粮'),
('豪华寄养', 2, 200.00, 1440, '独立房间、每日遛弯、视频通话'),
('基础体检', 3, 50.00, 30, '体温、心率、牙齿检查'),
('疫苗接种', 3, 80.00, 20, '狂犬疫苗、犬瘟疫苗等'),
('遛狗30分钟', 4, 35.00, 30, '小区内遛狗'),
('遛狗60分钟', 4, 60.00, 60, '公园遛狗，更长时间');

-- 服务商提供服务关联
INSERT INTO `provider_service` (`provider_id`, `service_id`, `price`, `status`) VALUES
(3, 1, 88.00, 1),
(3, 2, 110.00, 1),
(3, 3, 100.00, 1),
(3, 7, 35.00, 1),
(4, 1, 98.00, 1),
(4, 4, 220.00, 1),
(4, 5, 45.00, 1),
(4, 8, 65.00, 1);

-- 宠物档案
INSERT INTO `pet` (`user_id`, `name`, `type`, `breed`, `gender`, `birthday`, `weight`) VALUES
(2, '旺财', '狗', '金毛', 1, '2020-05-10', 25.50),
(2, '咪咪', '猫', '英短蓝猫', 0, '2021-08-15', 4.80),
(3, '大黄', '狗', '中华田园犬', 1, '2019-03-20', 18.00);
