-- 电动滑板车租赁系统数据库初始化脚本

-- 创建表结构

-- 用户表
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('USER', 'ADMIN')),
    is_student BOOLEAN DEFAULT FALSE,
    is_senior BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 滑板车表
CREATE TABLE scooters (
    id BIGSERIAL PRIMARY KEY,
    model VARCHAR(100) NOT NULL,
    image_url VARCHAR(255),
    total_quantity INTEGER NOT NULL DEFAULT 1,
    available_quantity INTEGER NOT NULL DEFAULT 1,
    hourly_rate DECIMAL(10,2) NOT NULL,
    daily_rate DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'UNAVAILABLE')),
    location_id INTEGER NOT NULL REFERENCES locations(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 预订表
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    scooter_id BIGINT NOT NULL REFERENCES scooters(id),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    duration_type VARCHAR(20) NOT NULL CHECK (duration_type IN ('1h', '4h', '1d', '1w')),
    total_price DECIMAL(10,2) NOT NULL,
    discount_applied DECIMAL(3,2) DEFAULT 1.0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 点位管理表
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    latitude DECIMAL(10,6) NOT NULL,
    longitude DECIMAL(10,6) NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 10,
    available_count INTEGER NOT NULL DEFAULT 0,
    booked_count INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 支付表
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id),
    amount DECIMAL(10,2) NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    card_last_four VARCHAR(4) NOT NULL
);

-- 反馈表
CREATE TABLE feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    priority VARCHAR(20) DEFAULT 'LOW' CHECK (priority IN ('HIGH', 'LOW')),
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'RESOLVED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 插入预置管理员账号 (密码: admin)
INSERT INTO users (username, email, password_hash, role) VALUES 
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVwHvO', 'ADMIN');

-- 插入示例点位数据
INSERT INTO locations (name, address, latitude, longitude, capacity, available_count, booked_count) VALUES 
('市中心广场', '北京市东城区王府井大街', 39.9042, 116.4074, 20, 15, 5),
('大学城校区', '北京市海淀区中关村大街', 39.9896, 116.3509, 15, 12, 3),
('商业步行街', '北京市西城区西单北大街', 39.9138, 116.3631, 10, 8, 2),
('地铁站出口', '北京市朝阳区国贸地铁站', 39.9022, 116.3912, 12, 10, 2),
('公园入口', '北京市海淀区颐和园东门', 39.9163, 116.3972, 8, 6, 2);

-- 插入示例滑板车数据（关联到点位）
INSERT INTO scooters (model, hourly_rate, daily_rate, status, location_id) VALUES 
('Xiaomi Mi Electric Scooter 3', 5.00, 25.00, 'AVAILABLE', 1),
('Segway Ninebot MAX G30', 6.00, 30.00, 'AVAILABLE', 1),
('Hiboy S2 Pro', 4.50, 22.00, 'AVAILABLE', 2),
('Gotrax GXL V2', 4.00, 20.00, 'AVAILABLE', 2),
('Razor E300', 3.50, 18.00, 'AVAILABLE', 3),
('Swagtron Swagger 5', 5.50, 28.00, 'AVAILABLE', 3),
('Glion Dolly', 6.50, 32.00, 'AVAILABLE', 4),
('Unagi Model One', 7.00, 35.00, 'AVAILABLE', 5);

-- 创建索引以提高查询性能
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_scooter_id ON bookings(scooter_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_start_time ON bookings(start_time);
CREATE INDEX idx_feedback_user_id ON feedback(user_id);
CREATE INDEX idx_feedback_priority ON feedback(priority);

-- 创建更新触发器
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();