CREATE TABLE `user` (
  id bigint NOT NULL AUTO_INCREMENT,
  username varchar(32) NOT NULL,
  password varchar(128) NOT NULL,
  phone varchar(11) NOT NULL,
  role tinyint NOT NULL DEFAULT 0,
  avatar varchar(255) DEFAULT NULL,
  real_name varchar(32) DEFAULT NULL,
  id_card varchar(18) DEFAULT NULL,
  shop_name varchar(64) DEFAULT NULL,
  status tinyint DEFAULT 1,
  create_time datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO `user` (username, password, phone, role, status) VALUES
('admin', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13800000001', 2, 1);

INSERT INTO `user` (username, password, phone, role, status) VALUES
('user1', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13912345678', 0, 1),
('user2', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13923456789', 0, 1);

INSERT INTO `user` (username, password, phone, role, real_name, shop_name, status) VALUES
('provider1', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13987654321', 1, '张三', '萌宠屋', 1),
('provider2', '$2a$10$N.zmYkGksJF4ZJfQ0rGZUeBQjF5V7L8X2nWqE6RtY9uI0oP3sA5c', '13998765432', 1, '李四', '爱宠驿站', 1);
