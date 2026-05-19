# 电动滑板车租赁系统 (Electric Scooter Rental System)

## 项目简介

这是一个完整的电动滑板车租赁系统，包含后端API服务和前端用户界面。系统支持用户注册、登录、双因素认证(2FA)、滑板车预订、二维码解锁、支付模拟、银行卡管理、管理员管理、设备监控、运维管理等功能。

## 技术栈

### 后端
- Java 17
- Spring Boot 2.7+
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven
- Lombok

### 前端
- Vue 3 (Composition API)
- Vite 4.4+
- Node.js 16+ (推荐 Node 18+)
- Vue Router
- Pinia
- Axios
- Element Plus
- Chart.js

## 快速启动

### 前提条件
- Java 17 环境
- Node.js 16+ (推荐 18+)
- PostgreSQL 数据库 (已安装并运行)
- Maven (已安装或使用 IDE 集成)

### 启动步骤

1. **数据库设置**
   
   创建数据库：
   ```sql
   -- 在 PostgreSQL 中执行
   CREATE DATABASE scooter;
   ```
   
   数据库连接配置在 `backend/src/main/resources/application.properties`，默认配置：
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/scooter
   spring.datasource.username=postgres
   spring.datasource.password=zjx1028
   ```
   
   **注意**：首次启动后，JPA 会自动创建所需的数据库表结构，无需手动执行 SQL 脚本。

2. **启动后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   后端默认在 http://localhost:8080 启动（可通过环境变量 `SERVER_PORT` 修改）。

3. **启动前端**
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   前端默认在 http://localhost:5173 启动（可通过环境变量 `VITE_PORT` 修改）。

4. **访问系统**
   - 前端界面: http://localhost:5173
   - 后端API: http://localhost:8080
   - 管理员账号: admin / admin
   - 如需自定义后端地址，在 `frontend` 目录创建 `.env` 文件：
     ```env
     VITE_API_URL=http://127.0.0.1:8080
     ```

## 功能特性

### 用户功能
- 用户注册与登录
- 双因素认证(2FA)
- 滑板车浏览与地图查看
- 滑板车预订（分层定价）
- 二维码扫码解锁/锁定
- 支付模拟（支持银行卡管理）
- 预订记录查看、取消、延长、提前还车
- 反馈提交
- 主题设置、保险条款查看

### 管理员功能
- 管理仪表盘（收入统计图表）
- 滑板车管理（CRUD）
- 点位管理（CRUD）
- 代用户下单（支持已注册/临时用户/访客三种模式）
- 反馈管理（设置优先级、处理状态）
- 设备监控（实时状态、低电量预警）
- 运维任务管理

### 折扣策略
| 租赁时长 | 折扣 |
|----------|:----:|
| 1-3小时 | 原价 |
| 4-8小时 | 85折 |
| 9-24小时 | 6折（最高收12小时费用） |
| 1-3天 | 5折（每天按12小时计费） |
| 3天以上 | 3折（每天按12小时计费） |

**额外折扣**（取最大折扣，不叠加）：
- 频繁用户（7天内租赁≥8小时）：9折
- 学生/老年人：9.5折

## 项目结构

```
├── backend/                    # Spring Boot后端
│   ├── src/main/java/com/scooter/
│   │   ├── config/             # 配置类 (Security, JWT, DataInit)
│   │   ├── controller/         # 13个控制器
│   │   ├── dto/                # 15个数据传输对象
│   │   ├── entity/             # 18个实体类
│   │   ├── repository/         # 15个数据访问接口
│   │   ├── service/            # 17个业务服务
│   │   └── util/               # 4个工具类
│   └── src/main/resources/
│       ├── application.properties
│       ├── application-test.properties
│       ├── application-prod.properties
│       └── templates/email/    # 6个邮件模板
├── frontend/                   # Vue 3前端
│   └── src/
│       ├── api/                # API封装
│       ├── components/         # 公共组件
│       ├── router/             # 路由配置
│       ├── stores/             # Pinia状态管理
│       ├── utils/              # 工具函数
│       └── views/              # 页面组件
├── docs/                       # 项目文档
│   └── database.md             # 数据库文档
├── database_management.sql     # 数据库管理脚本
└── README.md                   # 项目说明
```

## 常见问题

### 端口被占用
- 修改后端端口：设置环境变量 `SERVER_PORT` 或修改 `application.properties` 中的 `server.port`
- 修改前端端口：设置环境变量 `VITE_PORT` 或在 `vite.config.js` 中配置

### 数据库连接失败
- 确认 PostgreSQL 服务已启动
- 检查 `application.properties` 中的数据库连接信息
- 确认已创建名为 `scooter` 的数据库

### JWT token 过期
- 重新登录即可获取新 token
- token 默认有效期 7 天（可在 `application.properties` 中配置）