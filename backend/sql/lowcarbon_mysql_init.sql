-- 社区低碳平台 MySQL 初始化脚本
-- 建议版本：MySQL 8.x

CREATE DATABASE IF NOT EXISTS lowcarbon
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE lowcarbon;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  nickname VARCHAR(50) NOT NULL,
  full_name VARCHAR(50) DEFAULT NULL,
  gender VARCHAR(20) DEFAULT NULL,
  email VARCHAR(120) DEFAULT NULL,
  phone VARCHAR(30) DEFAULT NULL,
  address VARCHAR(255) DEFAULT NULL,
  bio VARCHAR(500) DEFAULT NULL,
  city VARCHAR(80) DEFAULT NULL,
  organization VARCHAR(120) DEFAULT NULL,
  tags VARCHAR(300) DEFAULT NULL,
  role VARCHAR(20) NOT NULL DEFAULT 'RESIDENT',
  total_points INT NOT NULL DEFAULT 0,
  total_carbon_reduction DOUBLE NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS behavior_rules (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(300) DEFAULT NULL,
  points_per_action INT NOT NULL,
  carbon_reduction_per_action DOUBLE NOT NULL,
  daily_limit INT NOT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_behavior_rules_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS mall_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  description VARCHAR(300) DEFAULT NULL,
  points_cost INT NOT NULL,
  stock INT NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY uk_mall_items_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS behavior_reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  rule_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  proof_text VARCHAR(500) DEFAULT NULL,
  proof_image_url VARCHAR(500) DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  audited_at DATETIME DEFAULT NULL,
  auditor_id BIGINT DEFAULT NULL,
  audit_remark VARCHAR(300) DEFAULT NULL,
  granted_points INT DEFAULT NULL,
  granted_carbon DOUBLE DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_behavior_reports_user_id (user_id),
  KEY idx_behavior_reports_rule_id (rule_id),
  CONSTRAINT fk_behavior_reports_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_behavior_reports_rule FOREIGN KEY (rule_id) REFERENCES behavior_rules (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS points_ledgers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  change_points INT NOT NULL,
  type VARCHAR(20) NOT NULL,
  related_id BIGINT DEFAULT NULL,
  description VARCHAR(300) DEFAULT NULL,
  balance_after INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_points_ledgers_user_id (user_id),
  CONSTRAINT fk_points_ledgers_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS redemption_orders (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  item_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  total_points INT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  completed_at DATETIME DEFAULT NULL,
  remark VARCHAR(300) DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_redemption_orders_user_id (user_id),
  KEY idx_redemption_orders_item_id (item_id),
  CONSTRAINT fk_redemption_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_redemption_orders_item FOREIGN KEY (item_id) REFERENCES mall_items (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_role_points
  ON users (role, total_points, total_carbon_reduction);
CREATE INDEX idx_behavior_reports_user_rule_time
  ON behavior_reports (user_id, rule_id, submitted_at);
CREATE INDEX idx_behavior_reports_status_submitted_at
  ON behavior_reports (status, submitted_at);
CREATE INDEX idx_behavior_reports_status_audited_at
  ON behavior_reports (status, audited_at);
CREATE INDEX idx_points_ledgers_user_created_at
  ON points_ledgers (user_id, created_at);
CREATE INDEX idx_redemption_orders_status_created_at
  ON redemption_orders (status, created_at);
CREATE INDEX idx_redemption_orders_status_completed_at
  ON redemption_orders (status, completed_at);

-- 初始化演示账号
INSERT INTO users (username, password, nickname, role)
VALUES
  ('admin', 'admin123', '社区管理员', 'ADMIN'),
  ('alice', '123456', '张三', 'RESIDENT'),
  ('bob', '123456', '李四', 'RESIDENT'),
  ('carol', '123456', '王五', 'RESIDENT'),
  ('david', '123456', '赵六', 'RESIDENT')
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  nickname = VALUES(nickname),
  role = VALUES(role);

-- 初始化规则
INSERT INTO behavior_rules (name, description, points_per_action, carbon_reduction_per_action, daily_limit, active)
VALUES
  ('步行或骑行出行', '日常通勤选择步行或骑行等低碳方式', 10, 0.8, 5, 1),
  ('垃圾分类投放', '完成一次垃圾分类并正确投放', 6, 0.3, 6, 1),
  ('自带水杯', '外出消费时使用自带水杯', 8, 0.4, 4, 1),
  ('空调26度设置', '空调温度设置在26度及以上', 5, 0.2, 3, 1)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  points_per_action = VALUES(points_per_action),
  carbon_reduction_per_action = VALUES(carbon_reduction_per_action),
  daily_limit = VALUES(daily_limit),
  active = VALUES(active);

-- 初始化商品
INSERT INTO mall_items (name, description, points_cost, stock, enabled)
VALUES
  ('环保帆布袋', '可重复使用购物袋', 100, 120, 1),
  ('不锈钢吸管套装', '可循环使用吸管套装', 180, 60, 1),
  ('再生纸笔记本', '再生纸环保记事本', 60, 80, 1),
  ('竹纤维牙刷', '可降解日用品', 20, 200, 1),
  ('太阳能小台灯', 'USB 充电太阳能台灯', 220, 40, 1)
ON DUPLICATE KEY UPDATE
  description = VALUES(description),
  points_cost = VALUES(points_cost),
  stock = VALUES(stock),
  enabled = VALUES(enabled);
