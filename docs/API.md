# API 文档

## 概述

电动滑板车租赁系统提供 RESTful API 接口，所有接口前缀为 `/api`。

---

## 一、认证接口 (`/api/auth`)

### 用户登录
- **端点**: `POST /api/auth/login`
- **权限**: 公开
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应 (成功)**:
```json
{
  "token": "string",
  "type": "Bearer",
  "id": "number",
  "username": "string",
  "email": "string",
  "role": "string",
  "phone": "string",
  "fullName": "string",
  "requires2FA": false
}
```
- **响应 (需2FA验证)**:
```json
{
  "token": null,
  "id": "number",
  "username": "string",
  "email": "string",
  "role": "string",
  "requires2FA": true
}
```
- **错误响应**: `400` - "用户名或密码错误" / "账户已被锁定，请30分钟后再试"

### 用户注册
- **端点**: `POST /api/auth/register`
- **权限**: 公开
- **请求体**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "USER",
  "isStudent": false,
  "isSenior": false,
  "phone": "string",
  "fullName": "string"
}
```
- **响应**: 同登录成功响应
- **错误响应**: `400` - "用户名已存在" / "邮箱已存在" / "用户名不能包含中文" / "密码过于简单"

### 修改密码
- **端点**: `POST /api/auth/change-password`
- **权限**: 公开（需提供用户名和当前密码）
- **请求体**:
```json
{
  "username": "string",
  "currentPassword": "string",
  "newPassword": "string"
}
```
- **响应**: `200` - "密码修改成功"
- **错误响应**: `400` - "当前密码错误" / "新密码不能与当前密码相同"

### 更新个人信息
- **端点**: `PUT /api/auth/profile`
- **权限**: USER（需Bearer Token）
- **请求体**:
```json
{
  "fullName": "string",
  "email": "string",
  "phone": "string"
}
```
- **响应**:
```json
{
  "success": true,
  "message": "用户信息更新成功",
  "user": { ... }
}
```

### 验证2FA
- **端点**: `POST /api/auth/verify-2fa`
- **权限**: 公开
- **请求体**:
```json
{
  "username": "string",
  "code": "string"
}
```
- **响应**: 同登录成功响应（含JWT token）

### 启用2FA
- **端点**: `POST /api/auth/enable-2fa?username=xxx`
- **权限**: 公开
- **响应**:
```json
{
  "enabled": true,
  "secretKey": "string",
  "qrCodeUrl": "otpauth://totp/...",
  "message": "2FA已启用，请使用验证器应用扫描二维码"
}
```

### 禁用2FA
- **端点**: `POST /api/auth/disable-2fa?username=xxx`
- **权限**: 公开
- **响应**:
```json
{
  "enabled": false,
  "message": "2FA已禁用"
}
```

### 获取2FA状态
- **端点**: `GET /api/auth/2fa-status?username=xxx`
- **权限**: 公开
- **响应**:
```json
{
  "enabled": true/false,
  "message": "2FA已启用/未启用"
}
```

---

## 二、滑板车接口 (`/api/scooters`)

### 获取所有滑板车
- **端点**: `GET /api/scooters`
- **权限**: 公开
- **响应**: 滑板车列表（含完整字段）

### 获取可用滑板车
- **端点**: `GET /api/scooters/available`
- **权限**: 公开
- **响应**: 可用滑板车列表（status=AVAILABLE）

### 新增滑板车
- **端点**: `POST /api/scooters`
- **权限**: ADMIN
- **参数**:
  - `model`: 滑板车型号
  - `imageUrl`: 图片URL（可选）
  - `totalQuantity`: 总数量
  - `hourlyRate`: 小时价格
  - `dailyRate`: 日价格
  - `locationId`: 点位ID（可选）

### 更新滑板车
- **端点**: `PUT /api/scooters/{id}`
- **权限**: ADMIN
- **参数**: 同新增（均为可选）

---

## 三、预订接口 (`/api/bookings`)

### 创建预订
- **端点**: `POST /api/bookings`
- **权限**: USER
- **请求体**:
```json
{
  "scooterId": "number",
  "hours": "number (1-168)",
  "cardNumber": "string (可选，与bankCardId二选一)",
  "bankCardId": "number (可选，与cardNumber二选一)"
}
```
- **响应**: Booking对象（含完整预订信息）
- **错误响应**: `400` - "滑板车不可用" / "该时间段内滑板车已被预订"

### 获取用户预订
- **端点**: `GET /api/bookings/user`
- **权限**: USER
- **响应**: 当前用户的预订列表

### 取消预订
- **端点**: `PUT /api/bookings/{id}/cancel`
- **权限**: USER（仅限本人）
- **错误响应**: `400` - "无权取消此预订" / "只能取消未开始的预订"

### 提前还车
- **端点**: `PUT /api/bookings/{id}/return`
- **权限**: USER（仅限本人）
- **错误响应**: `400` - "无权操作此预订" / "只能归还进行中的预订"

### 延长预订
- **端点**: `PUT /api/bookings/{id}/extend?hours=2`
- **权限**: USER（仅限本人）
- **错误响应**: `400` - "无权延长此预订" / "只能延长进行中的预订"

### 获取活跃预订数量
- **端点**: `GET /api/bookings/active/count`
- **权限**: 公开
- **响应**: `number`（PENDING + ACTIVE状态的预订总数）

---

## 四、管理员接口 (`/api/admin`)

### 代用户下单（简单模式）
- **端点**: `POST /api/admin/bookings`
- **权限**: ADMIN
- **请求体**:
```json
{
  "userEmail": "string",
  "scooterId": "number",
  "hours": "number"
}
```
- **响应**: Booking对象

### 代用户下单（混合模式，支持三种用户类型）
- **端点**: `POST /api/admin/mixed-bookings`
- **权限**: ADMIN
- **请求体**:
```json
{
  "userType": "EXISTING | NEW | GUEST",
  "userEmail": "string (EXISTING时必填)",
  "scooterId": "number",
  "hours": "number",
  "temporaryUser": {
    "realName": "string (NEW时必填)",
    "phone": "string (NEW时必填)",
    "idCard": "string (可选)",
    "emergencyContact": "string (可选)",
    "emergencyPhone": "string (可选)",
    "bankCard": { ... }
  },
  "guestInfo": {
    "name": "string (GUEST时必填)",
    "phone": "string (GUEST时必填)"
  }
}
```
- **响应**:
```json
{
  "success": true,
  "message": "代下单成功",
  "booking": { ... },
  "userType": "EXISTING | NEW | GUEST"
}
```

### 获取每周收入
- **端点**: `GET /api/admin/revenue/weekly`
- **权限**: ADMIN
- **响应**:
```json
{
  "totalRevenue": "number",
  "revenueByDuration": {
    "1h": "number",
    "4h": "number",
    "1d": "number",
    "1w": "number"
  }
}
```

### 获取每日收入
- **端点**: `GET /api/admin/revenue/daily`
- **权限**: ADMIN
- **响应**:
```json
{
  "dailyRevenue": {
    "2026-05-10": "number",
    "2026-05-11": "number",
    ...
  }
}
```

### 获取所有反馈
- **端点**: `GET /api/admin/feedback`
- **权限**: ADMIN
- **响应**: 反馈列表（按创建时间倒序）

### 设置反馈优先级
- **端点**: `PUT /api/admin/feedback/{id}/priority?priority=HIGH|LOW`
- **权限**: ADMIN

### 更新反馈状态
- **端点**: `PUT /api/admin/feedback/{id}/status?status=OPEN|RESOLVED`
- **权限**: ADMIN

### 获取所有用户列表
- **端点**: `GET /api/admin/users`
- **权限**: ADMIN
- **响应**:
```json
{
  "success": true,
  "total": "number",
  "users": [ ... ]
}
```

---

## 五、反馈接口 (`/api/feedback`)

### 提交反馈
- **端点**: `POST /api/feedback`
- **权限**: USER
- **请求体**:
```json
{
  "title": "string",
  "description": "string"
}
```
- **响应**: Feedback对象（priority=LOW, status=OPEN）

### 获取用户反馈
- **端点**: `GET /api/feedback/user`
- **权限**: USER
- **响应**: 当前用户的反馈列表

---

## 六、点位管理 (`/api/locations`)

### 获取所有点位
- **端点**: `GET /api/locations`
- **权限**: 公开

### 获取启用点位
- **端点**: `GET /api/locations/active`
- **权限**: 公开

### 根据ID获取点位
- **端点**: `GET /api/locations/{id}`
- **权限**: 公开

### 创建点位
- **端点**: `POST /api/locations`
- **权限**: ADMIN

### 更新点位
- **端点**: `PUT /api/locations/{id}`
- **权限**: ADMIN

### 删除点位
- **端点**: `DELETE /api/locations/{id}`
- **权限**: ADMIN

### 搜索点位
- **端点**: `GET /api/locations/search?keyword=xxx`
- **权限**: 公开

---

## 七、银行卡管理 (`/api/bank-cards`)

### 获取用户银行卡列表
- **端点**: `GET /api/bank-cards`
- **权限**: USER

### 添加银行卡
- **端点**: `POST /api/bank-cards`
- **权限**: USER

### 设置默认银行卡
- **端点**: `PUT /api/bank-cards/{id}/default`
- **权限**: USER

### 删除银行卡
- **端点**: `DELETE /api/bank-cards/{id}`
- **权限**: USER

### 获取默认银行卡
- **端点**: `GET /api/bank-cards/default`
- **权限**: USER

---

## 八、设备监控 (`/api/device`)

### 上报位置和状态
- **端点**: `POST /api/device/update-location?scooterId=1&latitude=39.9&longitude=116.4&batteryLevel=85.0&speed=10.0`
- **权限**: 公开

### 获取滑板车实时状态
- **端点**: `GET /api/device/status/{scooterId}`
- **权限**: 公开

### 获取滑板车及预订状态
- **端点**: `GET /api/device/with-booking-status`
- **权限**: 公开

### 获取低电量滑板车
- **端点**: `GET /api/device/low-battery?threshold=20.0`
- **权限**: 公开

### 获取在线滑板车列表
- **端点**: `GET /api/device/online`
- **权限**: 公开
- **响应**: 在线滑板车列表

### 计算行驶里程
- **端点**: `GET /api/device/mileage/{scooterId}?startTime=...&endTime=...`
- **权限**: 公开

### 二维码解锁
- **端点**: `POST /api/device/unlock?qrCode=xxx&unlockCode=xxx`
- **权限**: 公开

### 二维码锁定
- **端点**: `POST /api/device/lock?qrCode=xxx`
- **权限**: 公开

### 通过二维码获取滑板车状态
- **端点**: `GET /api/device/status/qr/{qrCode}`
- **权限**: 公开

---

## 九、车辆损坏 (`/api/damage`)

### 报告损坏
- **端点**: `POST /api/damage/report`
- **权限**: USER
- **请求体**:
```json
{
  "bookingId": "number",
  "scooterId": "number",
  "damageLevel": "MINOR | MODERATE | SEVERE",
  "damagedParts": ["刹车", "车灯"],
  "description": "string",
  "imageUrls": ["url1", "url2"]
}
```

### 审核损坏
- **端点**: `PUT /api/damage/{id}/review`
- **权限**: ADMIN

### 获取用户损坏记录
- **端点**: `GET /api/damage/my-records`
- **权限**: USER

### 获取待审核记录
- **端点**: `GET /api/damage/pending-review`
- **权限**: ADMIN

### 获取预订的损坏记录
- **端点**: `GET /api/damage/booking/{bookingId}`
- **权限**: USER

### 检查预订是否有损坏记录
- **端点**: `GET /api/damage/booking/{bookingId}/has-damage`
- **权限**: USER

### 获取损坏记录详情
- **端点**: `GET /api/damage/{id}`
- **权限**: USER
- **响应**: 损坏记录详情

---

## 十、会话管理 (`/api/sessions`)

### 获取用户活动会话
- **端点**: `GET /api/sessions/user/{username}`
- **权限**: USER

---

## 错误码

| 状态码 | 含义 | 说明 |
|--------|------|------|
| `200` | 成功 | 请求处理成功 |
| `400` | 请求参数错误 | 参数校验失败、业务逻辑错误 |
| `401` | 未授权访问 | 缺少或无效的JWT Token |
| `403` | 权限不足 | 非管理员访问管理员接口 |
| `404` | 资源不存在 | 请求的资源不存在 |
| `500` | 服务器内部错误 | 服务器异常 |

## 通用说明

- **认证方式**: Bearer Token（JWT），在请求头中添加 `Authorization: Bearer {token}`
- **请求头附加信息**: 前端自动附加 `X-User-Id`, `X-Username`, `X-Email`, `X-Role`
- **Token有效期**: 7天（604800秒）
- **分页**: 暂未实现分页，返回全部数据
- **日期格式**: ISO 8601 (yyyy-MM-dd'T'HH:mm:ss)