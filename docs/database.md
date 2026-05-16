# 数据库设计文档

## 概述

电动滑板车租赁系统使用 PostgreSQL 数据库，数据库名为 `scooter`（开发环境），包含以下核心表结构。

## ER 图描述

```
用户(users) -- 1:N -- 预订(bookings) -- N:1 -- 滑板车(scooters)
    |                          |                          |
    |                          |                          |-- N:1 -- 点位(locations)
    |-- 1:N -- 反馈(feedback)  |-- 1:1 -- 支付(payments)  |
    |                          |                          |-- 1:N -- 损坏记录(vehicle_damage_records)
    |-- 1:1 -- 2FA(two_factor_auth)                       |
    |                          |                          |-- 1:N -- 运维任务(operation_tasks)
    |-- 1:N -- 会话(user_sessions)                        |
    |                          |                          |-- N:1 -- 运维人员(operators)
    |-- 1:N -- 银行卡(bank_cards)                         |
    |                          |
    |-- 1:N -- 安全审计(security_audit_log)
    |
    |-- 1:N -- 临时用户(temporary_users) -- 1:1 -- 银行卡(bank_cards)
```

---

## 一、核心表结构

### users 表（用户表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| password_hash | VARCHAR(255) | NOT NULL | 密码哈希（BCrypt） |
| role | VARCHAR(20) | NOT NULL, CHECK(USER,ADMIN) | 角色 |
| is_student | BOOLEAN | DEFAULT FALSE | 是否学生 |
| is_senior | BOOLEAN | DEFAULT FALSE | 是否老年人 |
| phone | VARCHAR(20) | | 手机号 |
| full_name | VARCHAR(50) | | 真实姓名 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### scooters 表（滑板车表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 滑板车ID |
| model | VARCHAR(100) | NOT NULL | 型号 |
| image_url | VARCHAR(255) | | 图片URL |
| total_quantity | INTEGER | NOT NULL DEFAULT 1 | 总数量 |
| available_quantity | INTEGER | NOT NULL DEFAULT 1 | 可用数量 |
| hourly_rate | DECIMAL(10,2) | NOT NULL | 小时价格 |
| daily_rate | DECIMAL(10,2) | NOT NULL | 日价格 |
| status | VARCHAR(20) | DEFAULT 'AVAILABLE', CHECK(AVAILABLE,UNAVAILABLE) | 状态 |
| location_id | INTEGER | FOREIGN KEY → locations(id) | 所属点位ID |
| location_name | VARCHAR(100) | | 点位名称 |
| latitude | DECIMAL(10,6) | | 纬度 |
| longitude | DECIMAL(10,6) | | 经度 |
| battery_level | DECIMAL(5,2) | DEFAULT 100.0 | 电量百分比 |
| total_mileage | DECIMAL(10,2) | DEFAULT 0.0 | 总行驶里程(km) |
| is_locked | BOOLEAN | DEFAULT true | 是否锁定 |
| is_online | BOOLEAN | DEFAULT false | 是否在线 |
| qr_code | VARCHAR(255) | | 二维码标识 |
| unlock_code | VARCHAR(50) | | 解锁码 |
| last_update_time | TIMESTAMP | | 最后更新时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### bookings 表（预订表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 预订ID |
| user_id | BIGINT | FOREIGN KEY → users(id) | 用户ID（可为null，临时用户时为空） |
| temporary_user_id | BIGINT | | 临时用户ID |
| user_type | VARCHAR(20) | NOT NULL DEFAULT 'REGULAR' | 用户类型(REGULAR/TEMPORARY) |
| user_info | VARCHAR(100) | | 用户显示信息 |
| scooter_id | BIGINT | FOREIGN KEY → scooters(id) | 滑板车ID |
| start_time | TIMESTAMP | NOT NULL | 开始时间 |
| end_time | TIMESTAMP | NOT NULL | 结束时间 |
| duration_type | VARCHAR(20) | NOT NULL | 时长类型（如 "2h", "5h"） |
| total_price | DECIMAL(10,2) | NOT NULL | 总价格 |
| discount_applied | DECIMAL(3,2) | DEFAULT 1.0 | 折扣系数 |
| status | VARCHAR(20) | DEFAULT 'PENDING', CHECK(PENDING,ACTIVE,COMPLETED,CANCELLED) | 状态 |
| start_latitude | DECIMAL(10,8) | | 起始纬度 |
| start_longitude | DECIMAL(11,8) | | 起始经度 |
| end_latitude | DECIMAL(10,8) | | 结束纬度 |
| end_longitude | DECIMAL(11,8) | | 结束经度 |
| distance_traveled | DECIMAL(10,2) | | 行驶距离(km) |
| overtime_minutes | INTEGER | DEFAULT 0 | 超时分钟数 |
| overtime_fee | DECIMAL(10,2) | DEFAULT 0.00 | 超时费用 |
| is_auto_extended | BOOLEAN | DEFAULT FALSE | 是否自动续费 |
| billing_type | VARCHAR(20) | | 计费类型(TIME_ONLY/DISTANCE_ONLY/TIME_DISTANCE) |
| time_rate | DECIMAL(10,2) | | 时间费率 |
| distance_rate | DECIMAL(10,2) | | 距离费率 |
| actual_end_time | TIMESTAMP | | 实际结束时间 |
| last_reminder_sent | TIMESTAMP | | 最后提醒发送时间 |
| reminder_count | INTEGER | DEFAULT 0 | 提醒次数 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### payments 表（支付表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 支付ID |
| booking_id | BIGINT | FOREIGN KEY → bookings(id) | 预订ID |
| amount | DECIMAL(10,2) | NOT NULL | 支付金额 |
| payment_date | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 支付时间 |
| card_last_four | VARCHAR(4) | NOT NULL | 信用卡末四位 |

### feedback 表（反馈表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 反馈ID |
| user_id | BIGINT | FOREIGN KEY → users(id) | 用户ID |
| title | VARCHAR(200) | NOT NULL | 标题 |
| description | TEXT | | 详细描述（最长1000字） |
| priority | VARCHAR(20) | DEFAULT 'LOW', CHECK(HIGH,LOW) | 优先级 |
| status | VARCHAR(20) | DEFAULT 'OPEN', CHECK(OPEN,RESOLVED) | 状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

---

## 二、扩展表结构

### locations 表（点位管理表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 点位ID |
| name | VARCHAR(100) | NOT NULL | 点位名称 |
| address | VARCHAR(255) | | 地址 |
| latitude | DECIMAL(10,6) | NOT NULL | 纬度 |
| longitude | DECIMAL(10,6) | NOT NULL | 经度 |
| capacity | INTEGER | NOT NULL DEFAULT 10 | 容量 |
| available_count | INTEGER | NOT NULL DEFAULT 0 | 可用数量 |
| booked_count | INTEGER | NOT NULL DEFAULT 0 | 已预订数量 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE', CHECK(ACTIVE,INACTIVE) | 状态 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### bank_cards 表（银行卡表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 银行卡ID |
| user_id | BIGINT | NOT NULL, FOREIGN KEY → users(id) | 用户ID |
| card_number | VARCHAR(200) | NOT NULL | 银行卡号（AES加密存储） |
| card_number_display | VARCHAR(20) | NOT NULL | 显示卡号（仅后4位） |
| bank_name | VARCHAR(50) | NOT NULL | 银行名称 |
| cardholder_name | VARCHAR(50) | NOT NULL | 持卡人姓名 |
| card_type | VARCHAR(10) | NOT NULL, CHECK(DEBIT,CREDIT) | 卡片类型 |
| expiry_date | VARCHAR(10) | | 有效期(MM/YY) |
| is_default | BOOLEAN | NOT NULL DEFAULT FALSE | 是否默认卡 |
| status | VARCHAR(10) | NOT NULL DEFAULT 'ACTIVE' | 状态(ACTIVE/INACTIVE) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### temporary_users 表（临时用户表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 临时用户ID |
| username | VARCHAR(50) | NOT NULL | 临时用户名（自动生成） |
| real_name | VARCHAR(50) | NOT NULL | 真实姓名 |
| phone | VARCHAR(20) | NOT NULL | 手机号（AES加密存储） |
| id_card | VARCHAR(200) | | 身份证号（AES加密存储） |
| id_card_display | VARCHAR(20) | | 身份证号显示（部分隐藏） |
| emergency_contact | VARCHAR(50) | | 紧急联系人 |
| emergency_phone | VARCHAR(20) | | 紧急联系人电话 |
| bank_card_id | BIGINT | FOREIGN KEY → bank_cards(id) | 关联银行卡ID |
| created_by | BIGINT | NOT NULL | 创建店员ID |
| created_by_name | VARCHAR(50) | NOT NULL | 创建店员姓名 |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | 状态(ACTIVE/INACTIVE) |
| last_used_time | TIMESTAMP | | 最后使用时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### two_factor_auth 表（双因素认证表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | ID |
| user_id | BIGINT | NOT NULL, FOREIGN KEY → users(id) | 用户ID |
| secret_key | VARCHAR(32) | | TOTP密钥 |
| backup_codes | VARCHAR(500) | | 备用码（JSON格式） |
| is_enabled | BOOLEAN | DEFAULT FALSE | 是否启用 |
| last_verified | TIMESTAMP | | 最后验证时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### user_sessions 表（用户会话表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 会话ID |
| user_id | BIGINT | NOT NULL, FOREIGN KEY → users(id) | 用户ID |
| session_token | VARCHAR(255) | NOT NULL | 会话令牌 |
| device_fingerprint | VARCHAR(255) | | 设备指纹 |
| user_agent | VARCHAR(500) | | 用户代理 |
| ip_address | VARCHAR(45) | | IP地址 |
| location | VARCHAR(100) | | 位置 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否活跃 |
| last_activity | TIMESTAMP | | 最后活动时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| expires_at | TIMESTAMP | | 过期时间（默认30天） |

### security_audit_logs 表（安全审计日志表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 日志ID |
| event_type | VARCHAR(50) | NOT NULL | 事件类型 |
| event_description | VARCHAR(500) | | 事件描述 |
| user_id | BIGINT | | 用户ID |
| username | VARCHAR(50) | | 用户名 |
| ip_address | VARCHAR(45) | | IP地址 |
| user_agent | VARCHAR(500) | | 用户代理 |
| device_fingerprint | VARCHAR(255) | | 设备指纹 |
| success | BOOLEAN | | 是否成功 |
| failure_reason | VARCHAR(200) | | 失败原因 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### vehicle_damage_records 表（车辆损坏记录表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 记录ID |
| booking_id | BIGINT | NOT NULL | 预订ID |
| scooter_id | BIGINT | NOT NULL | 滑板车ID |
| reported_by_user_id | BIGINT | NOT NULL | 报告人ID |
| damage_level | VARCHAR(20) | NOT NULL | 损坏等级(MINOR/MODERATE/SEVERE) |
| damaged_parts | VARCHAR(1000) | | 损坏部位（JSON数组） |
| description | VARCHAR(2000) | | 损坏描述 |
| image_urls | VARCHAR(2000) | | 图片URL（JSON数组） |
| estimated_repair_cost | DOUBLE | | 预估维修费用 |
| actual_repair_cost | DOUBLE | | 实际维修费用 |
| user_compensation | DOUBLE | | 用户赔偿金额 |
| responsibility_type | VARCHAR(20) | | 责任类型(USER_FULL/USER_PARTIAL/PLATFORM_FULL/UNKNOWN/NO_FAULT) |
| status | VARCHAR(20) | NOT NULL | 状态(REPORTED/UNDER_REVIEW/APPROVED/REJECTED/RESOLVED) |
| reviewed_by_user_id | BIGINT | | 审核人ID |
| reviewer_notes | VARCHAR(2000) | | 审核备注 |
| reported_at | TIMESTAMP | | 报告时间 |
| reviewed_at | TIMESTAMP | | 审核时间 |
| resolved_at | TIMESTAMP | | 解决时间 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 更新时间 |

### operators 表（运维人员表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 人员ID |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| phone | VARCHAR(20) | NOT NULL | 电话 |
| email | VARCHAR(50) | NOT NULL | 邮箱 |
| role | VARCHAR(20) | NOT NULL | 角色(CHARGER/DEPLOYER/MAINTENANCE) |
| status | VARCHAR(20) | DEFAULT 'ACTIVE' | 状态(ACTIVE/INACTIVE) |
| assigned_area | VARCHAR(100) | | 负责区域 |
| current_task_count | INTEGER | DEFAULT 0 | 当前任务数 |
| total_tasks_completed | INTEGER | DEFAULT 0 | 完成任务总数 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| last_active_time | TIMESTAMP | | 最后活跃时间 |

### operation_tasks 表（运维任务表）

| 字段名 | 类型 | 约束 | 描述 |
|--------|------|------|------|
| id | BIGSERIAL | PRIMARY KEY | 任务ID |
| scooter_id | BIGINT | NOT NULL | 滑板车ID |
| assigned_operator_id | BIGINT | | 运维人员ID |
| task_type | VARCHAR(20) | NOT NULL | 任务类型(CHARGING/DEPLOYMENT/COLLECTION/MAINTENANCE) |
| priority | VARCHAR(20) | DEFAULT 'NORMAL' | 优先级(LOW/NORMAL/HIGH/URGENT) |
| status | VARCHAR(20) | DEFAULT 'PENDING' | 状态(PENDING/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELLED) |
| description | VARCHAR(500) | | 任务描述 |
| estimated_duration | INTEGER | | 预估时长(分钟) |
| actual_duration | INTEGER | | 实际时长(分钟) |
| target_location | VARCHAR(100) | | 目标位置 |
| target_latitude | DECIMAL(10,8) | | 目标纬度 |
| target_longitude | DECIMAL(11,8) | | 目标经度 |
| completion_notes | VARCHAR(1000) | | 完成备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| assigned_at | TIMESTAMP | | 分配时间 |
| started_at | TIMESTAMP | | 开始时间 |
| completed_at | TIMESTAMP | | 完成时间 |

---

## 三、索引设计

### 核心表索引
- `idx_users_email` — 用户邮箱索引
- `idx_users_username` — 用户名索引
- `idx_bookings_user_id` — 预订用户ID索引
- `idx_bookings_scooter_id` — 预订滑板车ID索引
- `idx_bookings_status` — 预订状态索引
- `idx_bookings_start_time` — 预订开始时间索引
- `idx_feedback_user_id` — 反馈用户ID索引
- `idx_feedback_priority` — 反馈优先级索引

### 扩展表索引（建议添加）
- `idx_locations_status` — 点位状态索引
- `idx_bank_cards_user_id` — 银行卡用户ID索引
- `idx_temporary_users_phone` — 临时用户手机号索引
- `idx_temporary_users_status` — 临时用户状态索引
- `idx_user_sessions_user_id` — 会话用户ID索引
- `idx_security_audit_user_id` — 审计日志用户ID索引
- `idx_security_audit_event_type` — 审计日志事件类型索引
- `idx_vehicle_damage_booking_id` — 损坏记录预订ID索引
- `idx_operation_tasks_status` — 运维任务状态索引
- `idx_operation_tasks_operator_id` — 运维任务人员ID索引

---

## 四、触发器

系统包含一个更新触发器，用于自动更新 `bookings` 表的 `updated_at` 字段：

```sql
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

---

## 五、初始化数据

### 预置管理员账号
```sql
INSERT INTO users (username, email, password_hash, role) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwHvO', 'ADMIN');
-- 密码: admin
```

### 示例点位数据（10个点位）
```sql
INSERT INTO locations (name, address, latitude, longitude, capacity) VALUES 
('市中心广场', '北京市东城区王府井大街', 39.9042, 116.4074, 20),
('大学城校区', '北京市海淀区中关村大街', 39.9896, 116.3509, 15),
('商业步行街', '北京市西城区西单北大街', 39.9138, 116.3631, 10),
('地铁站出口', '北京市朝阳区国贸地铁站', 39.9022, 116.3912, 12),
('公园入口', '北京市海淀区颐和园东门', 39.9163, 116.3972, 8),
('火车站北广场', '北京市西城区北京站', 39.9028, 116.4278, 15),
('科技园区', '北京市海淀区上地信息产业基地', 40.0412, 116.2981, 12),
('体育中心', '北京市朝阳区奥林匹克公园', 39.9924, 116.3912, 10),
('购物中心', '北京市朝阳区三里屯', 39.9334, 116.4526, 8),
('医院门口', '北京市西城区协和医院', 39.9048, 116.4076, 6);
```

### 示例滑板车数据（关联到点位）
```sql
INSERT INTO scooters (model, hourly_rate, daily_rate, status, location_id) VALUES 
('Xiaomi Mi Electric Scooter 3', 5.00, 25.00, 'AVAILABLE', 1),
('Segway Ninebot MAX G30', 6.00, 30.00, 'AVAILABLE', 1),
('Hiboy S2 Pro', 4.50, 22.00, 'AVAILABLE', 2),
('Gotrax GXL V2', 4.00, 20.00, 'AVAILABLE', 2),
('Razor E300', 3.50, 18.00, 'AVAILABLE', 3),
('Swagtron Swagger 5', 5.50, 28.00, 'AVAILABLE', 3),
('Glion Dolly', 6.50, 32.00, 'AVAILABLE', 4),
('Unagi Model One', 7.00, 35.00, 'AVAILABLE', 5);
```

---

## 六、数据库配置

| 配置项 | 开发环境 | 测试环境 | 生产环境 |
|--------|----------|----------|----------|
| 数据库名 | scooter | testdb (H2) | scooter_rental |
| 端口 | 5432 | 内嵌 | 5432 |
| 字符集 | UTF-8 | UTF-8 | UTF-8 |
| DDL策略 | update | create-drop | validate |
| 连接池 | HikariCP (10) | HikariCP (5) | HikariCP (20) |

### 配置文件
- **开发环境**: `application.properties` — 连接 PostgreSQL `scooter` 库
- **测试环境**: `application-test.properties` — 使用 H2 内存数据库
- **生产环境**: `application-prod.properties` — 连接生产 PostgreSQL 库