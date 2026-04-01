# 数据库设计文档

## 概述

电动滑板车租赁系统使用 PostgreSQL 数据库，包含以下核心表结构。

## ER 图描述

```
用户(users) -- 1:N -- 预订(bookings) -- N:1 -- 滑板车(scooters)
    |                                      |
    |                                      |
    |-- 1:N -- 反馈(feedback)             |-- 1:1 -- 支付(payments)
```

## 表结构说明

### users 表（用户表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希 |
| role | VARCHAR(20) | NOT NULL | 角色 (USER/ADMIN) |
| is_student | BOOLEAN | DEFAULT FALSE | 是否学生 |
| is_senior | BOOLEAN | DEFAULT FALSE | 是否老年人 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### scooters 表（滑板车表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 滑板车ID |
| model | VARCHAR(100) | NOT NULL | 型号 |
| hourly_rate | DECIMAL(10,2) | NOT NULL | 小时价格 |
| daily_rate | DECIMAL(10,2) | NOT NULL | 日价格 |
| status | VARCHAR(20) | DEFAULT 'AVAILABLE' | 状态 (AVAILABLE/UNAVAILABLE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### bookings 表（预订表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 预订ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| scooter_id | BIGINT | FOREIGN KEY | 滑板车ID |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| duration_type | VARCHAR(20) | NOT NULL | 时长类型 (1h/4h/1d/1w) |
| total_price | DECIMAL(10,2) | NOT NULL | 总价格 |
| discount_applied | DECIMAL(3,2) | DEFAULT 1.0 | 折扣系数 |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态 (PENDING/ACTIVE/COMPLETED/CANCELLED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### payments 表（支付表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 支付ID |
| booking_id | BIGINT | FOREIGN KEY | 预订ID |
| amount | DECIMAL(10,2) | NOT NULL | 支付金额 |
| payment_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 支付时间 |
| card_last_four | VARCHAR(4) | NOT NULL | 信用卡末四位 |

### feedback 表（反馈表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 反馈ID |
| user_id | BIGINT | FOREIGN KEY | 用户ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| description | TEXT | | 详细描述 |
| priority | VARCHAR(20) | DEFAULT 'LOW' | 优先级 (HIGH/LOW) |
| status | VARCHAR(20) | DEFAULT 'OPEN' | 状态 (OPEN/RESOLVED) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 索引设计

为提高查询性能，创建了以下索引：

- `idx_users_email` - 用户邮箱索引
- `idx_users_username` - 用户名索引
- `idx_bookings_user_id` - 预订用户ID索引
- `idx_bookings_scooter_id` - 预订滑板车ID索引
- `idx_bookings_status` - 预订状态索引
- `idx_bookings_start_time` - 预订开始时间索引
- `idx_feedback_user_id` - 反馈用户ID索引
- `idx_feedback_priority` - 反馈优先级索引

## 触发器

系统包含一个更新触发器，用于自动更新 `bookings` 表的 `updated_at` 字段：

```sql
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## 初始化数据

### 预置管理员账号
```sql
INSERT INTO users (username, email, password_hash, role) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwHvO', 'ADMIN');
```

### 示例滑板车数据
```sql
INSERT INTO scooters (model, hourly_rate, daily_rate, status) VALUES 
('Xiaomi Mi Electric Scooter 3', 5.00, 25.00, 'AVAILABLE'),
('Segway Ninebot MAX G30', 6.00, 30.00, 'AVAILABLE'),
('Hiboy S2 Pro', 4.50, 22.00, 'AVAILABLE'),
('Gotrax GXL V2', 4.00, 20.00, 'AVAILABLE'),
('Razor E300', 3.50, 18.00, 'AVAILABLE'),
('Swagtron Swagger 5', 5.50, 28.00, 'AVAILABLE'),
('Glion Dolly', 6.50, 32.00, 'AVAILABLE'),
('Unagi Model One', 7.00, 35.00, 'AVAILABLE');
```

## 数据库配置

- **数据库名**: scooter_rental
- **字符集**: UTF-8
- **时区**: 系统默认
- **连接池**: 使用 Spring Boot 默认配置