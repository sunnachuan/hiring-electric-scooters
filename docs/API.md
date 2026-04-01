# API 文档

## 概述

电动滑板车租赁系统提供 RESTful API 接口，所有接口前缀为 `/api`。

## 认证接口

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
- **响应**:
```json
{
  "token": "string",
  "type": "Bearer",
  "id": "number",
  "username": "string",
  "email": "string",
  "role": "string"
}
```

### 用户注册
- **端点**: `POST /api/auth/register`
- **权限**: 公开
- **请求体**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string",
  "isStudent": "boolean",
  "isSenior": "boolean"
}
```
- **响应**: 同登录响应

## 滑板车接口

### 获取所有滑板车
- **端点**: `GET /api/scooters`
- **权限**: 公开
- **响应**: 滑板车列表

### 获取可用滑板车
- **端点**: `GET /api/scooters/available`
- **权限**: 公开
- **响应**: 可用滑板车列表

### 新增滑板车
- **端点**: `POST /api/scooters`
- **权限**: ADMIN
- **参数**:
  - `model`: 滑板车型号
  - `hourlyRate`: 小时价格
  - `dailyRate`: 日价格

### 更新滑板车
- **端点**: `PUT /api/scooters/{id}`
- **权限**: ADMIN
- **参数**: 同新增（可选）

## 预订接口

### 创建预订
- **端点**: `POST /api/bookings`
- **权限**: USER
- **请求体**:
```json
{
  "scooterId": "number",
  "durationType": "string", // 1h, 4h, 1d, 1w
  "cardNumber": "string"
}
```

### 获取用户预订
- **端点**: `GET /api/bookings/user`
- **权限**: USER
- **响应**: 用户预订列表

### 取消预订
- **端点**: `PUT /api/bookings/{id}/cancel`
- **权限**: USER

### 延长预订
- **端点**: `PUT /api/bookings/{id}/extend`
- **权限**: USER
- **参数**: `durationType`

## 管理员接口

### 代用户下单
- **端点**: `POST /api/admin/bookings`
- **权限**: ADMIN
- **请求体**:
```json
{
  "userEmail": "string",
  "scooterId": "number",
  "durationType": "string"
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
- **响应**: 7天每日收入数据

### 获取所有反馈
- **端点**: `GET /api/admin/feedback`
- **权限**: ADMIN

### 设置反馈优先级
- **端点**: `PUT /api/admin/feedback/{id}/priority`
- **权限**: ADMIN
- **参数**: `priority` (HIGH/LOW)

## 反馈接口

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

### 获取用户反馈
- **端点**: `GET /api/feedback/user`
- **权限**: USER

## 错误码

- `400`: 请求参数错误
- `401`: 未授权访问
- `403`: 权限不足
- `404`: 资源不存在
- `500`: 服务器内部错误