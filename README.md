# 电动滑板车租赁系统 (Electric Scooter Rental System)

## 项目简介

这是一个完整的电动滑板车租赁系统，包含后端API服务和前端用户界面。系统支持用户注册、登录、滑板车预订、支付模拟、管理员管理等功能。

## 技术栈

### 后端
- Java 11+
- Spring Boot 2.7+
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok

### 前端
- Vue 3 (Composition API)
- Vite
- Vue Router
- Pinia
- Axios
- Element Plus
- Chart.js

## 快速启动

### 前提条件
- Java 11+ 环境
- Node.js 16+
- PostgreSQL 数据库

### 启动步骤

1. **数据库设置**
   ```bash
   # 创建数据库
   createdb scooter_rental
   
   # 执行初始化脚本
   psql -d scooter_rental -f database/init.sql
   ```

2. **启动后端**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   # 或使用Maven
   mvn spring-boot:run
   ```

3. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. **访问系统**
   - 前端界面: http://localhost:5173
   - 后端API: http://localhost:8080
   - 管理员账号: admin / admin

## 功能特性

### 用户功能
- 用户注册与登录
- 滑板车浏览与预订
- 支付模拟（任意信用卡号）
- 预订记录查看
- 预订取消与延长
- 反馈提交

### 管理员功能
- 滑板车管理
- 代用户下单
- 收入统计与图表展示
- 反馈管理
- 用户管理

### 折扣策略
- 频繁用户（7天内租赁≥8小时）：9折
- 学生/老年人：9.5折
- 取最大折扣（不叠加）

## 项目结构

```
├── backend/           # Spring Boot后端
├── frontend/          # Vue 3前端
├── database/          # 数据库脚本
├── docs/             # 项目文档
└── README.md         # 项目说明
```