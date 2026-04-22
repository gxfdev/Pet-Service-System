-- 员工排班表
CREATE TABLE IF NOT EXISTS `schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `staff_id` bigint NOT NULL COMMENT '店员ID (user.id)',
  `provider_id` bigint NOT NULL COMMENT '店长ID (user.id)',
  `shift_date` date NOT NULL COMMENT '排班日期',
  `start_time` varchar(10) NOT NULL COMMENT '上班时间 HH:mm',
  `end_time` varchar(10) NOT NULL COMMENT '下班时间 HH:mm',
  `shift_type` tinyint DEFAULT 0 COMMENT '班次: 0-早班, 1-中班, 2-晚班, 3-全天',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `status` tinyint DEFAULT 1 COMMENT '0-已取消, 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_staff_date` (`staff_id`, `shift_date`),
  KEY `idx_provider_date` (`provider_id`, `shift_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工排班表';
