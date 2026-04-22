USE pet_service;

-- 清理所有旧测试数据
DELETE FROM `order_info` WHERE order_no LIKE 'ORD%';
DELETE FROM `provider_service`;

-- 正确的 provider_service 关联
-- provider1(id=7, 萌宠屋)
INSERT INTO `provider_service` (`provider_id`, `service_id`, `price`, `status`) VALUES
(7, 1, 88.00, 1),
(7, 2, 110.00, 1),
(7, 3, 100.00, 1),
(7, 7, 35.00, 1);

-- provider2(id=8, 爱宠驿站)
INSERT INTO `provider_service` (`provider_id`, `service_id`, `price`, `status`) VALUES
(8, 1, 98.00, 1),
(8, 4, 220.00, 1),
(8, 5, 45.00, 1),
(8, 8, 65.00, 1);

-- 插入正确的测试订单
-- user1(id=10) 的宠物: pet 4(啊啊), pet 5(庞)
-- 下单给 provider1(id=7)

SET NAMES utf8mb4;
INSERT INTO `order_info` (`order_no`, `user_id`, `provider_id`, `service_id`, `pet_id`, `appointment_time`, `total_amount`, `status`, `remark`, `create_time`, `pay_time`) VALUES
('ORD20260414001', 10, 7, 1, 4, '2026-04-15 10:00:00', 88.00, 1, 'gentle', '2026-04-12 09:30:00', '2026-04-12 09:35:00'),
('ORD20260414002', 10, 7, 3, 5, '2026-04-16 14:00:00', 100.00, 2, 'shy cat', '2026-04-13 11:20:00', '2026-04-13 11:25:00'),
('ORD20260414003', 10, 7, 7, 4, '2026-04-14 17:00:00', 35.00, 3, '', '2026-04-10 15:00:00', '2026-04-10 15:05:00'),
('ORD20260414004', 10, 8, 1, 4, '2026-04-18 09:00:00', 98.00, 0, '', '2026-04-14 08:00:00', NULL),
('ORD20260414005', 10, 7, 2, 5, '2026-04-19 10:00:00', 110.00, 1, '', '2026-04-13 16:45:00', NULL);

-- user2(id=3) 下单给 provider2(id=8)，宠物用大黄(pet_id=3，但user_id=3不是user2的...用4吧)
INSERT INTO `order_info` (`order_no`, `user_id`, `provider_id`, `service_id`, `pet_id`, `appointment_time`, `total_amount`, `status`, `remark`, `create_time`, `pay_time`) VALUES
('ORD20260414006', 3, 8, 4, 1, '2026-04-20 09:00:00', 220.00, 1, '', '2026-04-14 07:30:00', '2026-04-14 07:35:00'),
('ORD20260414007', 3, 8, 8, 1, '2026-04-15 16:00:00', 65.00, 4, '', '2026-04-08 14:00:00', '2026-04-08 14:05:00');

SELECT 'DONE' AS result;
SELECT CONCAT('provider1(id=',id,', name=',username) FROM user WHERE username='provider1';
SELECT CONCAT('provider2(id=',id,', name=',username) FROM user WHERE username='provider2';
SELECT CONCAT('user1(id=',id,', name=',username) FROM user WHERE username='user1';

SELECT o.id, o.order_no, u.username AS customer, p.username AS provider,
       si.name AS service_name, o.total_amount,
       CASE o.status WHEN 0 THEN 'pending' WHEN 1 THEN 'waiting' WHEN 2 THEN 'serving' 
            WHEN 3 THEN 'done' WHEN 4 THEN 'cancelled' END AS status_text
FROM order_info o
LEFT JOIN `user` u ON o.user_id = u.id
LEFT JOIN `user` p ON o.provider_id = p.id
LEFT JOIN service_item si ON o.service_id = si.id;

SELECT COUNT(*) AS provider1_orders FROM order_info WHERE provider_id = 7;
SELECT COUNT(*) AS provider2_orders FROM order_info WHERE provider_id = 8;
