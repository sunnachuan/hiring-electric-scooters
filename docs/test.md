# 电动滑板车租赁系统 — 测试文档

---

## 一、项目概述

### 1.1 项目简介
电动滑板车租赁系统是一个完整的B2C共享出行平台，支持用户注册登录、滑板车浏览预订、支付模拟、管理员后台管理等功能。

### 1.2 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 2.7.18 |
| 安全框架 | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA (Hibernate) |
| 数据库 | PostgreSQL (生产) / H2 (测试) |
| 前端框架 | Vue 3 (Composition API) + Vite |
| 状态管理 | Pinia |
| UI组件库 | Element Plus |
| HTTP客户端 | Axios |
| 图表 | Chart.js |
| 邮件 | Spring Mail + Thymeleaf 模板 |
| 构建工具 | Maven (后端) / npm (前端) |

### 1.3 项目结构

```
电动滑板车项目/
├── backend/                          # Spring Boot 后端
│   ├── src/main/java/com/scooter/
│   │   ├── config/                   # 配置类 (Security, JWT, DataInit)
│   │   ├── controller/               # 13个控制器
│   │   ├── dto/                      # 15个数据传输对象
│   │   ├── entity/                   # 18个实体类
│   │   ├── repository/               # 15个数据访问接口
│   │   ├── service/                  # 17个业务服务
│   │   └── util/                     # 4个工具类
│   └── src/main/resources/
│       ├── application.properties    # 主配置
│       ├── application-test.properties
│       ├── application-prod.properties
│       └── templates/email/          # 6个邮件模板
├── frontend/                         # Vue 3 前端
│   └── src/
│       ├── api/                      # API 封装
│       ├── assets/                   # 静态资源
│       ├── components/               # 公共组件
│       ├── router/                   # 路由配置 (20个路由)
│       ├── stores/                   # Pinia 状态管理
│       ├── utils/                    # 工具函数
│       └── views/                    # 页面组件 (17个)
├── database/
│   ├── init.sql                      # 数据库初始化脚本
│   └── database_management.sql       # 数据库管理脚本
└── docs/
    ├── API.md                        # API文档
    ├── database.md                   # 数据库文档
    ├── deployment.md                 # 部署文档
    └── user_manual.md                # 用户手册
```

---

## 二、测试环境配置

### 2.1 测试环境要求

| 组件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Maven | 3.6+ |
| Node.js | 16+ |
| PostgreSQL | 14+ (生产) |
| H2 Database | 内嵌 (测试) |

### 2.2 测试数据库配置

测试环境使用 H2 内存数据库，配置位于 `application-test.properties`：

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### 2.3 测试启动命令

```bash
# 后端单元测试
cd backend
mvn test

# 使用测试配置启动后端
mvn spring-boot:run -Dspring-boot.run.profiles=test

# 前端开发服务器
cd frontend
npm run dev
```

---

## 三、测试策略

### 3.1 测试层次

```
┌─────────────────────────────────────┐
│         E2E 端到端测试               │  ← 完整用户流程
├─────────────────────────────────────┤
│      集成测试 (API层)                │  ← Controller + Service + DB
├─────────────────────────────────────┤
│      服务层单元测试                   │  ← Service 业务逻辑
├─────────────────────────────────────┤
│      工具类单元测试                   │  ← Util 工具方法
└─────────────────────────────────────┘
```

### 3.2 测试范围矩阵

| 模块 | 单元测试 | 集成测试 | E2E测试 | 优先级 |
|------|:---:|:---:|:---:|:---:|
| 认证模块 (Auth) | ✅ | ✅ | ✅ | P0 |
| 预订模块 (Booking) | ✅ | ✅ | ✅ | P0 |
| 支付模块 (Payment) | ✅ | ✅ | ✅ | P0 |
| 滑板车管理 (Scooter) | ✅ | ✅ | ✅ | P1 |
| 用户管理 (User) | ✅ | ✅ | ✅ | P1 |
| 管理员功能 (Admin) | ✅ | ✅ | ✅ | P1 |
| 反馈系统 (Feedback) | ✅ | ✅ | - | P2 |
| 银行卡管理 (BankCard) | ✅ | ✅ | - | P2 |
| 设备监控 (Device) | ✅ | ✅ | - | P2 |
| 位置管理 (Location) | ✅ | ✅ | - | P2 |
| 车辆损坏 (Damage) | ✅ | ✅ | - | P2 |
| 运维任务 (Operation) | ✅ | ✅ | - | P2 |
| 二维码解锁 (QRCode) | ✅ | ✅ | - | P2 |
| 双因素认证 (2FA) | ✅ | ✅ | - | P2 |
| 安全审计 (Audit) | ✅ | - | - | P3 |
| 会话管理 (Session) | ✅ | - | - | P3 |
| 超时处理 (Overtime) | ✅ | ✅ | - | P2 |
| 计费服务 (Billing) | ✅ | ✅ | - | P1 |
| 临时用户 (Temporary) | ✅ | ✅ | - | P2 |
| 邮件服务 (Email) | ✅ | - | - | P3 |

---

## 四、单元测试用例

### 4.1 认证模块 (AuthController / UserService)

#### TC-AUTH-001: 用户注册 - 正常流程
- **前置条件**: 数据库为空
- **输入**: `username=testuser, email=test@test.com, password=Test1234, role=USER`
- **预期结果**: 返回200，包含JWT token和用户信息
- **验证点**:
  - 密码已BCrypt加密存储
  - 返回的token可解析出正确username
  - 用户角色为USER

#### TC-AUTH-002: 用户注册 - 用户名已存在
- **前置条件**: 数据库已存在用户 `testuser`
- **输入**: `username=testuser, email=other@test.com, password=Test1234`
- **预期结果**: 返回400，错误信息"用户名已存在"

#### TC-AUTH-003: 用户注册 - 邮箱已存在
- **前置条件**: 数据库已存在邮箱 `test@test.com`
- **输入**: `username=other, email=test@test.com, password=Test1234`
- **预期结果**: 返回400，错误信息"邮箱已存在"

#### TC-AUTH-004: 用户注册 - 用户名含中文
- **输入**: `username=测试用户, email=test@test.com, password=Test1234`
- **预期结果**: 返回400，错误信息"用户名不能包含中文"

#### TC-AUTH-005: 用户注册 - 弱密码
- **输入**: `username=testuser, email=test@test.com, password=123456`
- **预期结果**: 返回400，错误信息"密码过于简单"

#### TC-AUTH-006: 用户注册 - 密码长度不足
- **输入**: `username=testuser, email=test@test.com, password=ab`
- **预期结果**: 返回400，错误信息"密码长度至少需要6个字符"

#### TC-AUTH-007: 用户登录 - 正常流程
- **前置条件**: 已注册用户 `testuser / Test1234`
- **输入**: `username=testuser, password=Test1234`
- **预期结果**: 返回200，包含JWT token，`requires2FA=false`

#### TC-AUTH-008: 用户登录 - 密码错误
- **前置条件**: 已注册用户 `testuser / Test1234`
- **输入**: `username=testuser, password=WrongPass`
- **预期结果**: 返回400，"用户名或密码错误"

#### TC-AUTH-009: 用户登录 - 用户不存在
- **输入**: `username=nonexistent, password=Test1234`
- **预期结果**: 返回400，"用户名或密码错误"

#### TC-AUTH-010: 用户登录 - 多次失败锁定
- **前置条件**: 已注册用户 `testuser`
- **步骤**: 连续5次使用错误密码登录
- **预期结果**: 第6次返回400，"账户已被锁定，请30分钟后再试"

#### TC-AUTH-011: 修改密码 - 正常流程
- **前置条件**: 已登录用户 `testuser / Test1234`
- **输入**: `currentPassword=Test1234, newPassword=NewPass5678`
- **预期结果**: 返回200，"密码修改成功"

#### TC-AUTH-012: 修改密码 - 当前密码错误
- **输入**: `currentPassword=WrongPass, newPassword=NewPass5678`
- **预期结果**: 返回400，"当前密码错误"

#### TC-AUTH-013: 修改密码 - 新旧密码相同
- **输入**: `currentPassword=Test1234, newPassword=Test1234`
- **预期结果**: 返回400，"新密码不能与当前密码相同"

#### TC-AUTH-014: 更新个人信息
- **前置条件**: 已登录用户
- **输入**: `fullName=张三, email=newemail@test.com, phone=13800138000`
- **预期结果**: 返回200，用户信息已更新

#### TC-AUTH-015: 更新个人信息 - 邮箱冲突
- **前置条件**: 另一个用户已使用 `other@test.com`
- **输入**: `email=other@test.com`
- **预期结果**: 返回400，"邮箱已被其他用户使用"

---

### 4.2 预订模块 (BookingController / BookingService)

#### TC-BOOK-001: 创建预订 - 正常流程
- **前置条件**: 已登录用户，滑板车ID=1状态为AVAILABLE
- **输入**: `scooterId=1, hours=2, cardNumber=1234567890123456`
- **预期结果**: 返回200，预订状态为ACTIVE，滑板车可用数量-1

#### TC-BOOK-002: 创建预订 - 滑板车不可用
- **前置条件**: 滑板车ID=1状态为UNAVAILABLE
- **输入**: `scooterId=1, hours=2`
- **预期结果**: 返回400，"滑板车不可用"

#### TC-BOOK-003: 创建预订 - 时间冲突
- **前置条件**: 滑板车ID=1已被预订（当前时间+2小时）
- **输入**: `scooterId=1, hours=2`
- **预期结果**: 返回400，"该时间段内滑板车已被预订"

#### TC-BOOK-004: 创建预订 - 分层定价 (1-3小时)
- **输入**: `scooterId=1, hours=2` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 5.00 × 2 × 1.0 = 10.00`

#### TC-BOOK-005: 创建预订 - 分层定价 (4-8小时)
- **输入**: `scooterId=1, hours=5` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 5.00 × 5 × 0.85 = 21.25`

#### TC-BOOK-006: 创建预订 - 分层定价 (9-24小时)
- **输入**: `scooterId=1, hours=12` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 5.00 × 12 × 0.60 = 36.00`

#### TC-BOOK-007: 创建预订 - 分层定价 (1-3天)
- **输入**: `scooterId=1, hours=48` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 5.00 × 12 × 2 × 0.50 = 60.00`

#### TC-BOOK-008: 创建预订 - 分层定价 (3天以上)
- **输入**: `scooterId=1, hours=96` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 5.00 × 12 × 4 × 0.30 = 72.00`

#### TC-BOOK-009: 创建预订 - 频繁用户折扣 (9折)
- **前置条件**: 用户7天内已完成≥8小时租赁
- **输入**: `scooterId=1, hours=2` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 10.00 × 0.90 = 9.00`

#### TC-BOOK-010: 创建预订 - 学生折扣 (9.5折)
- **前置条件**: 用户 `isStudent=true`，非频繁用户
- **输入**: `scooterId=1, hours=2` (hourlyRate=5.00)
- **预期结果**: `totalPrice = 10.00 × 0.95 = 9.50`

#### TC-BOOK-011: 创建预订 - 折扣取最大 (不叠加)
- **前置条件**: 用户同时满足频繁用户(9折)和学生(9.5折)
- **预期结果**: 取9折（更优惠），`discountApplied=0.90`

#### TC-BOOK-012: 创建预订 - 使用已保存银行卡
- **前置条件**: 用户已保存银行卡ID=1
- **输入**: `scooterId=1, hours=2, bankCardId=1`
- **预期结果**: 使用已保存银行卡支付成功

#### TC-BOOK-013: 创建预订 - 使用他人银行卡
- **前置条件**: 银行卡ID=1属于其他用户
- **输入**: `scooterId=1, hours=2, bankCardId=1`
- **预期结果**: 返回400，"无权使用此银行卡"

#### TC-BOOK-014: 获取用户预订列表
- **前置条件**: 用户有3个预订记录
- **预期结果**: 返回200，列表包含3条记录

#### TC-BOOK-015: 取消预订 - 正常流程
- **前置条件**: 预订ID=1状态为PENDING
- **预期结果**: 状态变为CANCELLED，滑板车可用数量+1

#### TC-BOOK-016: 取消预订 - 非本人预订
- **前置条件**: 预订ID=1属于其他用户
- **预期结果**: 返回400，"无权取消此预订"

#### TC-BOOK-017: 取消预订 - 非PENDING状态
- **前置条件**: 预订ID=1状态为ACTIVE
- **预期结果**: 返回400，"只能取消未开始的预订"

#### TC-BOOK-018: 提前还车 - 正常流程
- **前置条件**: 预订ID=1状态为ACTIVE
- **预期结果**: 状态变为COMPLETED，endTime更新为当前时间

#### TC-BOOK-019: 提前还车 - 非ACTIVE状态
- **前置条件**: 预订ID=1状态为PENDING
- **预期结果**: 返回400，"只能归还进行中的预订"

#### TC-BOOK-020: 延长预订
- **前置条件**: 预订ID=1状态为ACTIVE
- **输入**: `hours=2`
- **预期结果**: endTime延长2小时

#### TC-BOOK-021: 获取活跃预订数量
- **预期结果**: 返回PENDING+ACTIVE状态的预订总数

---

### 4.3 滑板车管理 (ScooterController / ScooterService)

#### TC-SCOOT-001: 获取所有滑板车
- **预期结果**: 返回200，包含所有滑板车列表

#### TC-SCOOT-002: 获取可用滑板车
- **预期结果**: 返回200，仅包含status=AVAILABLE的滑板车

#### TC-SCOOT-003: 新增滑板车 (管理员)
- **前置条件**: 管理员登录
- **输入**: `model=ScooterX, totalQuantity=5, hourlyRate=8.0, dailyRate=40.0, locationId=1`
- **预期结果**: 返回200，availableQuantity=5，status=AVAILABLE

#### TC-SCOOT-004: 新增滑板车 (非管理员)
- **前置条件**: 普通用户登录
- **预期结果**: 返回403，权限不足

#### TC-SCOOT-005: 更新滑板车
- **前置条件**: 管理员登录，滑板车ID=1
- **输入**: `hourlyRate=10.0`
- **预期结果**: hourlyRate更新为10.0

#### TC-SCOOT-006: 减少可用数量 - 正常
- **前置条件**: 滑板车availableQuantity=5
- **预期结果**: availableQuantity=4

#### TC-SCOOT-007: 减少可用数量 - 库存为0
- **前置条件**: 滑板车availableQuantity=0
- **预期结果**: 抛出异常"该型号滑板车已全部租出"

#### TC-SCOOT-008: 增加可用数量 - 恢复状态
- **前置条件**: 滑板车availableQuantity=0, status=UNAVAILABLE
- **预期结果**: availableQuantity=1, status=AVAILABLE

---

### 4.4 管理员功能 (AdminController / AdminBookingController)

#### TC-ADMIN-001: 代用户下单 - 已注册用户
- **前置条件**: 管理员登录，用户 `user@test.com` 已注册
- **输入**: `userType=EXISTING, userEmail=user@test.com, scooterId=1, hours=2`
- **预期结果**: 返回200，预订创建成功

#### TC-ADMIN-002: 代用户下单 - 用户不存在
- **输入**: `userType=EXISTING, userEmail=nonexist@test.com`
- **预期结果**: 返回400，"用户不存在"

#### TC-ADMIN-003: 代用户下单 - 新临时用户
- **前置条件**: 管理员登录
- **输入**: `userType=NEW, temporaryUser.realName=李四, temporaryUser.phone=13900139000`
- **预期结果**: 返回200，临时用户创建并预订成功

#### TC-ADMIN-004: 代用户下单 - 访客模式
- **前置条件**: 管理员登录
- **输入**: `userType=GUEST, guestInfo.name=王五, guestInfo.phone=13700137000`
- **预期结果**: 返回200，访客用户创建并预订成功

#### TC-ADMIN-005: 代用户下单 - 非管理员
- **前置条件**: 普通用户登录
- **预期结果**: 返回400，"权限不足"

#### TC-ADMIN-006: 获取每周收入
- **前置条件**: 一周内有多个已完成预订
- **预期结果**: 返回totalRevenue和revenueByDuration

#### TC-ADMIN-007: 获取每日收入
- **预期结果**: 返回7天每日收入数据

#### TC-ADMIN-008: 获取所有反馈
- **预期结果**: 返回所有反馈列表，按创建时间倒序

#### TC-ADMIN-009: 更新反馈优先级
- **输入**: `priority=HIGH`
- **预期结果**: 反馈优先级更新为HIGH

#### TC-ADMIN-010: 更新反馈状态
- **输入**: `status=RESOLVED`
- **预期结果**: 反馈状态更新为RESOLVED

#### TC-ADMIN-011: 获取所有用户列表
- **前置条件**: 管理员登录
- **预期结果**: 返回所有用户列表

#### TC-ADMIN-012: 获取用户列表 - 非管理员
- **前置条件**: 普通用户登录
- **预期结果**: 返回403

---

### 4.5 支付模块 (Payment / BillingService)

#### TC-PAY-001: 模拟支付 - 正常卡号
- **输入**: `cardNumber=1234567890123456`
- **预期结果**: 支付成功，cardLastFour=3456

#### TC-PAY-002: 模拟支付 - 短卡号
- **输入**: `cardNumber=123`
- **预期结果**: cardLastFour=1234 (使用默认值)

#### TC-PAY-003: 模拟支付 - 无支付信息
- **输入**: 无cardNumber和bankCardId
- **预期结果**: 使用默认卡号123456789012，支付成功

#### TC-PAY-004: 计费 - 纯时间计费 (TIME_ONLY)
- **前置条件**: booking.billingType=TIME_ONLY, timeRate=5.00, 时长2小时
- **预期结果**: totalFee = 10.00

#### TC-PAY-005: 计费 - 纯距离计费 (DISTANCE_ONLY)
- **前置条件**: booking.billingType=DISTANCE_ONLY, distanceRate=0.50, distanceTraveled=10km
- **预期结果**: totalFee = 5.00

#### TC-PAY-006: 计费 - 时间+距离复合计费 (TIME_DISTANCE)
- **前置条件**: timeFee=10.00, distanceFee=5.00
- **预期结果**: totalFee = 15.00

#### TC-PAY-007: 计费 - 应用折扣
- **前置条件**: 基础费用=10.00, discountApplied=0.90
- **预期结果**: totalFee = 9.00

#### TC-PAY-008: 获取今日收入
- **前置条件**: 今日有已完成预订
- **预期结果**: 返回今日总收入

#### TC-PAY-009: 获取本周收入
- **预期结果**: 返回本周总收入

---

### 4.6 反馈系统 (FeedbackController / FeedbackService)

#### TC-FB-001: 提交反馈 - 正常流程
- **前置条件**: 已登录用户
- **输入**: `title=车辆故障, description=刹车不灵敏`
- **预期结果**: 返回200，priority=LOW, status=OPEN

#### TC-FB-002: 获取用户反馈
- **前置条件**: 用户有2条反馈
- **预期结果**: 返回2条反馈记录

#### TC-FB-003: 获取所有反馈 (管理员)
- **预期结果**: 返回所有反馈，按时间倒序

#### TC-FB-004: 按优先级筛选反馈
- **输入**: `priority=HIGH`
- **预期结果**: 仅返回HIGH优先级的反馈

---

### 4.7 银行卡管理 (BankCardController / BankCardService)

#### TC-CARD-001: 添加银行卡 - 正常流程
- **前置条件**: 已登录用户
- **输入**: `cardNumber=6222021234567890, bankName=工商银行, cardholderName=张三, cardType=DEBIT`
- **预期结果**: 返回200，卡号加密存储，显示卡号仅后4位

#### TC-CARD-002: 添加银行卡 - 卡号格式错误
- **输入**: `cardNumber=12345`
- **预期结果**: 返回400，"银行卡号格式不正确"

#### TC-CARD-003: 添加银行卡 - 重复卡号
- **前置条件**: 已存在相同卡号
- **预期结果**: 返回400，"该银行卡已存在"

#### TC-CARD-004: 添加银行卡 - 首卡自动设为默认
- **前置条件**: 用户无银行卡
- **预期结果**: isDefault=true

#### TC-CARD-005: 获取银行卡列表
- **前置条件**: 用户有3张银行卡
- **预期结果**: 返回3张卡，默认卡排第一

#### TC-CARD-006: 设置默认银行卡
- **输入**: `cardId=2`
- **预期结果**: cardId=2的isDefault=true，其他卡isDefault=false

#### TC-CARD-007: 删除银行卡 - 非默认卡
- **预期结果**: 状态变为INACTIVE（软删除）

#### TC-CARD-008: 删除银行卡 - 默认卡
- **前置条件**: 用户有2张卡，删除默认卡
- **预期结果**: 另一张卡自动设为默认

#### TC-CARD-009: 获取默认银行卡
- **前置条件**: 用户有默认卡
- **预期结果**: 返回默认银行卡

#### TC-CARD-010: 获取默认银行卡 - 无默认卡
- **前置条件**: 用户无默认卡
- **预期结果**: 返回404

---

### 4.8 设备监控 (DeviceController / DeviceService)

#### TC-DEV-001: 上报位置和状态
- **输入**: `scooterId=1, latitude=39.9042, longitude=116.4074, batteryLevel=85.0, speed=10.0`
- **预期结果**: 滑板车位置更新，isOnline=true，创建位置记录

#### TC-DEV-002: 获取滑板车实时状态
- **输入**: `scooterId=1`
- **预期结果**: 返回滑板车完整状态信息

#### TC-DEV-003: 获取滑板车及预订状态
- **预期结果**: 返回所有滑板车列表，包含hasActiveBooking字段

#### TC-DEV-004: 获取低电量滑板车
- **输入**: `threshold=20.0`
- **预期结果**: 返回电量低于20%的在线滑板车

#### TC-DEV-005: 计算行驶里程
- **输入**: `scooterId=1, startTime=..., endTime=...`
- **预期结果**: 返回期间行驶总里程（Haversine公式计算）

#### TC-DEV-006: 标记滑板车离线
- **输入**: `scooterId=1`
- **预期结果**: isOnline=false

---

### 4.9 二维码解锁 (QRCodeService)

#### TC-QR-001: 解锁滑板车 - 正常流程
- **前置条件**: 滑板车isLocked=true, batteryLevel>10%, availableQuantity>0
- **输入**: 正确的qrCode和unlockCode
- **预期结果**: isLocked=false, availableQuantity-1

#### TC-QR-002: 解锁滑板车 - 无效二维码
- **输入**: 不存在的qrCode
- **预期结果**: 抛出异常"无效的二维码"

#### TC-QR-003: 解锁滑板车 - 解锁码错误
- **输入**: 正确的qrCode，错误的unlockCode
- **预期结果**: 抛出异常"解锁码错误"

#### TC-QR-004: 解锁滑板车 - 已解锁
- **前置条件**: 滑板车isLocked=false
- **预期结果**: 抛出异常"滑板车已解锁"

#### TC-QR-005: 解锁滑板车 - 电量过低
- **前置条件**: batteryLevel=5.0
- **预期结果**: 抛出异常"电量过低，无法解锁"

#### TC-QR-006: 锁定滑板车 - 正常流程
- **前置条件**: 滑板车isLocked=false
- **预期结果**: isLocked=true, availableQuantity+1

#### TC-QR-007: 锁定滑板车 - 已锁定
- **前置条件**: 滑板车isLocked=true
- **预期结果**: 抛出异常"滑板车已锁定"

#### TC-QR-008: 重新生成二维码
- **输入**: `scooterId=1`
- **预期结果**: qrCode和unlockCode已更新

---

### 4.10 位置管理 (LocationController / LocationService)

#### TC-LOC-001: 获取所有点位
- **预期结果**: 返回所有点位列表

#### TC-LOC-002: 获取启用点位
- **预期结果**: 仅返回status=ACTIVE的点位

#### TC-LOC-003: 创建点位 (管理员)
- **输入**: `name=新点位, latitude=39.9, longitude=116.4, capacity=15`
- **预期结果**: 返回200，点位创建成功

#### TC-LOC-004: 创建点位 - 名称重复
- **前置条件**: 已存在同名点位
- **预期结果**: 返回400，"点位名称已存在"

#### TC-LOC-005: 更新点位
- **输入**: `capacity=20`
- **预期结果**: capacity更新为20

#### TC-LOC-006: 删除点位 - 有关联滑板车
- **前置条件**: 点位有关联滑板车
- **预期结果**: 返回400，"无法删除点位，仍有滑板车关联到此点位"

#### TC-LOC-007: 删除点位 - 无关联滑板车
- **预期结果**: 点位删除成功

#### TC-LOC-008: 搜索点位
- **输入**: `keyword=广场`
- **预期结果**: 返回名称或地址包含"广场"的点位

#### TC-LOC-009: 更新点位统计
- **预期结果**: availableCount和bookedCount已更新

---

### 4.11 车辆损坏 (VehicleDamageController / VehicleDamageService)

#### TC-DMG-001: 报告损坏 - 正常流程
- **输入**: `bookingId=1, scooterId=1, damageLevel=MINOR, damagedParts=["刹车"], description=刹车异响`
- **预期结果**: 返回200，status=REPORTED, estimatedRepairCost=100.0

#### TC-DMG-002: 报告损坏 - 重复报告
- **前置条件**: 该预订已有损坏记录
- **预期结果**: 返回400，"该预订已存在损坏记录"

#### TC-DMG-003: 报告损坏 - 中等损坏
- **输入**: `damageLevel=MODERATE`
- **预期结果**: estimatedRepairCost=500.0

#### TC-DMG-004: 报告损坏 - 严重损坏
- **输入**: `damageLevel=SEVERE`
- **预期结果**: estimatedRepairCost=1500.0

#### TC-DMG-005: 审核损坏 - 确认用户责任
- **输入**: `status=APPROVED, responsibilityType=USER_FULL, estimatedRepairCost=500.0`
- **预期结果**: userCompensation=500.0 (100%责任)

#### TC-DMG-006: 审核损坏 - 用户部分责任
- **输入**: `responsibilityType=USER_PARTIAL` (50%)
- **预期结果**: userCompensation=250.0

#### TC-DMG-007: 审核损坏 - 无用户责任
- **输入**: `responsibilityType=SYSTEM`
- **预期结果**: userCompensation=0.0

#### TC-DMG-008: 获取用户损坏记录
- **预期结果**: 返回当前用户的损坏记录列表

#### TC-DMG-009: 获取待审核记录
- **预期结果**: 返回status为REPORTED或UNDER_REVIEW的记录

---

### 4.12 运维任务 (OperationService)

#### TC-OPS-001: 创建充电任务
- **输入**: `scooterId=1, priority=HIGH`
- **预期结果**: taskType=CHARGING, estimatedDuration=120分钟

#### TC-OPS-002: 创建部署任务
- **输入**: `scooterId=1, targetLocation=市中心, targetLatitude=39.9, targetLongitude=116.4`
- **预期结果**: taskType=DEPLOYMENT, estimatedDuration=30分钟

#### TC-OPS-003: 创建收集任务
- **输入**: `scooterId=1, reason=电量耗尽`
- **预期结果**: taskType=COLLECTION, estimatedDuration=45分钟

#### TC-OPS-004: 创建维修任务
- **输入**: `scooterId=1, issueDescription=刹车故障, priority=URGENT`
- **预期结果**: taskType=MAINTENANCE, estimatedDuration=60分钟

#### TC-OPS-005: 分配任务 - 正常
- **前置条件**: 运维人员status=ACTIVE, currentTaskCount<3
- **预期结果**: status=ASSIGNED, 运维人员currentTaskCount+1

#### TC-OPS-006: 分配任务 - 运维人员不可用
- **前置条件**: 运维人员status=INACTIVE
- **预期结果**: 抛出异常"运维人员不可用"

#### TC-OPS-007: 分配任务 - 任务已满
- **前置条件**: 运维人员currentTaskCount=3
- **预期结果**: 抛出异常"运维人员任务已满"

#### TC-OPS-008: 开始任务
- **前置条件**: 任务status=ASSIGNED
- **预期结果**: status=IN_PROGRESS, startedAt已设置

#### TC-OPS-009: 完成任务 - 充电任务
- **前置条件**: 任务type=CHARGING, status=IN_PROGRESS
- **预期结果**: status=COMPLETED, 滑板车batteryLevel=100%, status=AVAILABLE

---

### 4.13 超时处理 (OvertimeBookingService)

#### TC-OT-001: 超时15分钟内 - 发送提醒
- **前置条件**: 预订已超时10分钟，lastReminderSent=null
- **预期结果**: 发送提醒邮件，reminderCount=1

#### TC-OT-002: 超时15分钟内 - 不重复提醒
- **前置条件**: 预订已超时10分钟，lastReminderSent=5分钟前
- **预期结果**: 不发送提醒（间隔不足15分钟）

#### TC-OT-003: 超时30分钟内 - 自动续费
- **前置条件**: 预订已超时20分钟，isAutoExtended=false
- **预期结果**: isAutoExtended=true

#### TC-OT-004: 超时2小时内 - 2倍费率
- **前置条件**: 预订已超时60分钟
- **预期结果**: overtimeFee按2倍费率计算

#### TC-OT-005: 超时2小时以上 - 3倍费率
- **前置条件**: 预订已超时150分钟
- **预期结果**: overtimeFee按3倍费率计算

#### TC-OT-006: 超时4小时以上 - 紧急处理
- **前置条件**: 预订已超时4小时
- **预期结果**: 触发紧急处理流程

---

### 4.14 双因素认证 (TwoFactorAuthService)

#### TC-2FA-001: 启用2FA
- **前置条件**: 用户未启用2FA
- **预期结果**: 生成secretKey和10个备用码，isEnabled=true

#### TC-2FA-002: 验证TOTP码 - 正确
- **前置条件**: 已启用2FA
- **输入**: 正确的TOTP码
- **预期结果**: 返回true

#### TC-2FA-003: 验证TOTP码 - 错误
- **输入**: 错误的验证码
- **预期结果**: 返回false

#### TC-2FA-004: 验证备用码
- **输入**: 有效的备用码
- **预期结果**: 返回true，备用码标记为已使用

#### TC-2FA-005: 验证已使用的备用码
- **输入**: 已使用的备用码
- **预期结果**: 返回false

#### TC-2FA-006: 禁用2FA
- **预期结果**: 2FA记录被删除

#### TC-2FA-007: 生成新备用码
- **预期结果**: 返回10个新备用码

#### TC-2FA-008: 登录触发2FA
- **前置条件**: 用户已启用2FA
- **预期结果**: 登录返回requires2FA=true，不返回token

#### TC-2FA-009: 2FA验证后获取token
- **前置条件**: 已完成第一步登录
- **输入**: 正确的2FA验证码
- **预期结果**: 返回JWT token

---

### 4.15 安全审计 (SecurityAuditService)

#### TC-AUDIT-001: 记录登录成功事件
- **预期结果**: 创建eventType=LOGIN_SUCCESS的审计日志

#### TC-AUDIT-002: 记录登录失败事件
- **预期结果**: 创建eventType=LOGIN_FAILED的审计日志

#### TC-AUDIT-003: 检测可疑IP
- **前置条件**: 某IP在24小时内失败≥10次
- **预期结果**: 该IP出现在可疑列表中

#### TC-AUDIT-004: 获取用户安全日志
- **预期结果**: 返回用户的安全日志，按时间倒序

#### TC-AUDIT-005: 按事件类型筛选日志
- **输入**: `eventType=LOGIN_FAILED`
- **预期结果**: 仅返回登录失败日志

---

### 4.16 会话管理 (SessionManagementService)

#### TC-SESS-001: 创建会话
- **输入**: `userId=1, deviceFingerprint=abc123`
- **预期结果**: 创建新会话，生成sessionToken

#### TC-SESS-002: 创建会话 - 相同设备复用
- **前置条件**: 已存在相同deviceFingerprint的会话
- **预期结果**: 复用已有会话，更新lastActivity

#### TC-SESS-003: 验证会话 - 有效
- **输入**: 有效的sessionToken
- **预期结果**: 返回true

#### TC-SESS-004: 验证会话 - 过期
- **前置条件**: 会话expiresAt已过期
- **预期结果**: 返回false

#### TC-SESS-005: 终止会话
- **输入**: `sessionId=1`
- **预期结果**: isActive=false

#### TC-SESS-006: 终止其他会话
- **输入**: `userId=1, currentSessionId=1`
- **预期结果**: 除当前会话外，其他会话isActive=false

#### TC-SESS-007: 清理过期会话
- **预期结果**: 过期会话被标记为非活跃

---

### 4.17 临时用户 (TemporaryUserService)

#### TC-TEMP-001: 创建临时用户 - 正常
- **输入**: `realName=张三, phone=13800138000`
- **预期结果**: 创建成功，手机号加密存储

#### TC-TEMP-002: 创建临时用户 - 手机号格式错误
- **输入**: `phone=12345`
- **预期结果**: 返回400，"手机号格式不正确"

#### TC-TEMP-003: 创建临时用户 - 身份证格式错误
- **输入**: `idCard=12345`
- **预期结果**: 返回400，"身份证号格式不正确"

#### TC-TEMP-004: 创建临时用户 - 手机号重复
- **前置条件**: 已存在相同手机号的活跃临时用户
- **预期结果**: 返回400，"该手机号已存在"

#### TC-TEMP-005: 查找临时用户
- **输入**: `phone=13800138000`
- **预期结果**: 返回匹配的临时用户

#### TC-TEMP-006: 删除临时用户 (软删除)
- **预期结果**: status=INACTIVE，关联银行卡也被删除

---

### 4.18 邮件服务 (EmailService)

#### TC-EMAIL-001: 发送预订确认邮件
- **输入**: 有效的预订信息
- **预期结果**: 邮件发送成功，记录到sentBookingIds缓存

#### TC-EMAIL-002: 防重复发送
- **前置条件**: 同一预订ID已发送过邮件
- **预期结果**: 跳过发送，日志记录"已发送过"

#### TC-EMAIL-003: 发送超时提醒
- **预期结果**: 邮件发送成功

#### TC-EMAIL-004: 发送注册成功邮件
- **预期结果**: 异步发送，不阻塞注册响应

#### TC-EMAIL-005: 发送支付确认邮件
- **预期结果**: 邮件发送成功

#### TC-EMAIL-006: 发送预订取消邮件
- **预期结果**: 邮件发送成功

---

### 4.19 工具类测试

#### TC-UTIL-001: AES加密/解密
- **输入**: `原始数据=13800138000`
- **预期结果**: 加密后解密得到原始数据

#### TC-UTIL-002: 银行卡号脱敏
- **输入**: `6222021234567890`
- **预期结果**: `**** **** **** 7890`

#### TC-UTIL-003: 身份证脱敏
- **输入**: `110101199001011234`
- **预期结果**: `110101****1234`

#### TC-UTIL-004: 手机号脱敏
- **输入**: `13800138000`
- **预期结果**: `138****8000`

#### TC-UTIL-005: 手机号格式验证 - 正确
- **输入**: `13800138000`
- **预期结果**: true

#### TC-UTIL-006: 手机号格式验证 - 错误
- **输入**: `12345678901`
- **预期结果**: false

#### TC-UTIL-007: 身份证格式验证 - 正确
- **输入**: `110101199001011234`
- **预期结果**: true

#### TC-UTIL-008: 身份证格式验证 - 错误
- **输入**: `123456789012345`
- **预期结果**: false

#### TC-UTIL-009: Luhn算法验证银行卡
- **输入**: 有效的银行卡号
- **预期结果**: true

#### TC-UTIL-010: 密码策略 - 弱密码检测
- **输入**: `password`, `123456`, `qwerty`
- **预期结果**: 均返回false，"密码过于简单"

#### TC-UTIL-011: Stream哈希一致性
- **输入**: 相同sessionId多次调用
- **预期结果**: 返回相同的streamIndex

---

## 五、集成测试用例

### 5.1 完整预订流程

```
1. 用户注册 (POST /api/auth/register)
   → 获取JWT token
2. 查看可用滑板车 (GET /api/scooters/available)
   → 选择滑板车ID=1
3. 创建预订 (POST /api/bookings)
   → 预订成功，状态ACTIVE
4. 查看我的预订 (GET /api/bookings/user)
   → 确认预订存在
5. 提前还车 (PUT /api/bookings/{id}/return)
   → 状态COMPLETED
6. 验证滑板车可用数量恢复
```

### 5.2 管理员完整流程

```
1. 管理员登录 (POST /api/auth/login)
   → admin/admin
2. 查看收入统计 (GET /api/admin/revenue/weekly)
3. 查看每日收入 (GET /api/admin/revenue/daily)
4. 代用户下单 (POST /api/admin/mixed-bookings)
5. 查看所有反馈 (GET /api/admin/feedback)
6. 更新反馈状态 (PUT /api/admin/feedback/{id}/status)
7. 查看用户列表 (GET /api/admin/users)
```

### 5.3 设备监控流程

```
1. 设备上报位置 (POST /api/device/update-location)
2. 获取滑板车状态 (GET /api/device/status/{id})
3. 获取带预订状态的滑板车列表 (GET /api/device/with-booking-status)
4. 获取低电量滑板车 (GET /api/device/low-battery)
```

### 5.4 二维码解锁流程

```
1. 扫码获取qrCode
2. 输入unlockCode解锁 (POST /api/device/unlock)
3. 骑行结束锁定 (POST /api/device/lock)
```

---

## 六、安全测试用例

### 6.1 认证与授权

#### TC-SEC-001: 未登录访问受保护接口
- **请求**: `GET /api/bookings/user` (无token)
- **预期结果**: 401 Unauthorized

#### TC-SEC-002: 使用过期token
- **请求**: 携带已过期JWT
- **预期结果**: 401 Unauthorized

#### TC-SEC-003: 使用伪造token
- **请求**: 携带随机字符串作为token
- **预期结果**: 401 Unauthorized

#### TC-SEC-004: 普通用户访问管理员接口
- **请求**: `POST /api/scooters` (USER角色)
- **预期结果**: 403 Forbidden

#### TC-SEC-005: 用户A访问用户B的预订
- **请求**: `PUT /api/bookings/{B的预订ID}/cancel` (用户A登录)
- **预期结果**: 400，"无权取消此预订"

#### TC-SEC-006: SQL注入测试
- **输入**: `username=admin' OR '1'='1`
- **预期结果**: 正常处理，不产生SQL注入

#### TC-SEC-007: XSS测试
- **输入**: `title=<script>alert('xss')</script>`
- **预期结果**: 正常存储，前端正确转义

#### TC-SEC-008: CSRF测试
- **预期结果**: CSRF保护已禁用（API设计），依赖JWT认证

---

### 6.2 数据安全

#### TC-DSEC-001: 密码BCrypt加密
- **验证**: 数据库中password_hash不以明文存储
- **预期结果**: password_hash以`$2a$10$`开头

#### TC-DSEC-002: 银行卡号加密存储
- **验证**: 数据库中card_number为密文
- **预期结果**: card_number不是原始卡号

#### TC-DSEC-003: 手机号加密存储 (临时用户)
- **验证**: 数据库中phone为密文
- **预期结果**: phone不是原始手机号

#### TC-DSEC-004: API响应不包含密码
- **验证**: 用户相关API响应
- **预期结果**: 响应中不包含passwordHash字段

---

## 七、性能测试用例

### 7.1 批量操作

#### TC-PERF-001: 批量XADD (Pipeline)
- **场景**: 50条消息同时写入
- **预期结果**: 触发批量flush，一次Pipeline完成50条XADD

#### TC-PERF-002: 定时flush
- **场景**: 消息量不足batch-produce-size，等待定时触发
- **预期结果**: 在batch-produce-interval毫秒后自动flush

#### TC-PERF-003: 应用关闭时flush
- **场景**: 应用正常关闭，buffer中有未flush消息
- **预期结果**: @PreDestroy触发flush，所有消息写入Redis

---

### 7.2 并发测试

#### TC-CONC-001: 并发预订同一滑板车
- **场景**: 10个用户同时预订最后一个可用滑板车
- **预期结果**: 仅1个成功，其余返回"已全部租出"

#### TC-CONC-002: 并发注册相同用户名
- **场景**: 2个请求同时注册相同username
- **预期结果**: 仅1个成功，另1个返回"用户名已存在"

#### TC-CONC-003: 并发银行卡操作
- **场景**: 同时添加和删除银行卡
- **预期结果**: 数据一致性保持

---

## 八、前端测试用例

### 8.1 路由导航

#### TC-FE-001: 未登录访问受保护页面
- **操作**: 直接访问 `/scooters`
- **预期结果**: 重定向到 `/login`

#### TC-FE-002: 已登录访问登录页
- **操作**: 已登录状态访问 `/login`
- **预期结果**: 重定向到 `/`

#### TC-FE-003: 普通用户访问管理页面
- **操作**: USER角色访问 `/admin/dashboard`
- **预期结果**: 重定向到 `/`

#### TC-FE-004: 管理员访问管理页面
- **操作**: ADMIN角色访问 `/admin/dashboard`
- **预期结果**: 正常显示管理页面

---

### 8.2 状态管理

#### TC-FE-005: 登录状态持久化
- **操作**: 登录后刷新页面
- **预期结果**: 仍保持登录状态（localStorage恢复）

#### TC-FE-006: 登出清理
- **操作**: 执行登出
- **预期结果**: token、userInfo、token_timestamp均被清除

#### TC-FE-007: Token过期处理
- **操作**: API返回401
- **预期结果**: 自动清除登录状态，跳转登录页

---

### 8.3 API拦截器

#### TC-FE-008: 请求自动附加token
- **验证**: 发送API请求
- **预期结果**: 请求头包含 `Authorization: Bearer {token}`

#### TC-FE-009: 请求自动附加用户信息
- **验证**: 发送API请求
- **预期结果**: 请求头包含 `X-User-Id`, `X-Username`, `X-Email`, `X-Role`

---

## 九、数据库测试

### 9.1 数据完整性

#### TC-DB-001: 用户唯一约束
- **操作**: 插入重复username
- **预期结果**: 违反唯一约束，插入失败

#### TC-DB-002: 外键约束
- **操作**: 删除有预订关联的用户
- **预期结果**: 违反外键约束，删除失败

#### TC-DB-003: CHECK约束 - 用户角色
- **操作**: 插入role=INVALID
- **预期结果**: 违反CHECK约束

#### TC-DB-004: CHECK约束 - 预订状态
- **操作**: 插入status=INVALID
- **预期结果**: 违反CHECK约束

---

### 9.2 触发器测试

#### TC-DB-005: 预订更新触发器
- **操作**: 更新预订记录
- **预期结果**: updated_at自动更新为当前时间

---

### 9.3 索引测试

#### TC-DB-006: 邮箱查询性能
- **验证**: `SELECT * FROM users WHERE email = ?`
- **预期结果**: 使用 `idx_users_email` 索引

#### TC-DB-007: 预订用户查询性能
- **验证**: `SELECT * FROM bookings WHERE user_id = ?`
- **预期结果**: 使用 `idx_bookings_user_id` 索引

---

## 十、测试用例统计

| 模块 | 用例数 | P0 | P1 | P2 | P3 |
|------|:---:|:---:|:---:|:---:|:---:|
| 认证模块 | 15 | 8 | 5 | 2 | 0 |
| 预订模块 | 21 | 12 | 7 | 2 | 0 |
| 滑板车管理 | 8 | 2 | 4 | 2 | 0 |
| 管理员功能 | 12 | 4 | 6 | 2 | 0 |
| 支付模块 | 9 | 4 | 4 | 1 | 0 |
| 反馈系统 | 4 | 0 | 2 | 2 | 0 |
| 银行卡管理 | 10 | 2 | 5 | 3 | 0 |
| 设备监控 | 6 | 0 | 3 | 3 | 0 |
| 二维码解锁 | 8 | 2 | 4 | 2 | 0 |
| 位置管理 | 9 | 0 | 4 | 5 | 0 |
| 车辆损坏 | 9 | 0 | 4 | 5 | 0 |
| 运维任务 | 9 | 0 | 3 | 6 | 0 |
| 超时处理 | 6 | 0 | 3 | 3 | 0 |
| 双因素认证 | 9 | 0 | 3 | 6 | 0 |
| 安全审计 | 5 | 0 | 0 | 2 | 3 |
| 会话管理 | 7 | 0 | 0 | 3 | 4 |
| 临时用户 | 6 | 0 | 2 | 4 | 0 |
| 邮件服务 | 6 | 0 | 0 | 3 | 3 |
| 工具类 | 11 | 2 | 5 | 4 | 0 |
| 安全测试 | 12 | 8 | 4 | 0 | 0 |
| 性能测试 | 3 | 0 | 2 | 1 | 0 |
| 前端测试 | 9 | 3 | 4 | 2 | 0 |
| 数据库测试 | 7 | 2 | 3 | 2 | 0 |
| **总计** | **216** | **49** | **77** | **65** | **10** |

---

## 十一、测试执行计划

### 阶段一：冒烟测试 (1天)
- 执行所有P0用例 (49个)
- 覆盖核心流程：注册→登录→预订→支付→还车

### 阶段二：功能测试 (3天)
- 执行所有P1用例 (77个)
- 覆盖管理功能、计费、设备监控

### 阶段三：全面测试 (2天)
- 执行所有P2用例 (65个)
- 覆盖辅助功能、边界条件

### 阶段四：专项测试 (1天)
- 执行所有P3用例 (10个)
- 安全审计、会话管理

### 阶段五：回归测试 (1天)
- 修复缺陷后重新执行相关用例

---

## 十二、缺陷管理

### 严重级别定义

| 级别 | 定义 | 示例 |
|------|------|------|
| **Blocker** | 系统无法运行 | 应用无法启动、数据库连接失败 |
| **Critical** | 核心功能不可用 | 无法登录、无法创建预订 |
| **Major** | 功能严重缺陷 | 价格计算错误、权限绕过 |
| **Minor** | 功能轻微缺陷 | UI显示问题、非关键字段缺失 |
| **Trivial** | 建议性改进 | 文案优化、代码规范 |

---

## 十三、现有测试覆盖

当前项目已有测试：
- `EmailServiceTest.java` — 邮件服务单元测试

**建议补充的测试**：
1. `BookingServiceTest.java` — 预订服务（分层定价、折扣计算）
2. `UserServiceTest.java` — 用户服务（注册验证、密码策略）
3. `BillingServiceTest.java` — 计费服务（复合计费逻辑）
4. `AuthControllerTest.java` — 认证接口集成测试
5. `BookingControllerTest.java` — 预订接口集成测试
6. `SecurityUtilsTest.java` — 安全工具类测试
7. `PasswordPolicyTest.java` — 密码策略测试
8. `TwoFactorAuthServiceTest.java` — 2FA服务测试

---

> **文档版本**: v1.0  
> **生成日期**: 2026-05-16  
> **适用项目**: 电动滑板车租赁系统 v1.0.0