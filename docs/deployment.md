# 部署指南

## 系统要求

### 后端要求
- Java 11 或更高版本
- Maven 3.6+ 或使用内置的 Maven Wrapper
- PostgreSQL 12+ 数据库

### 前端要求
- Node.js 16+ 
- npm 或 yarn 包管理器

### 操作系统
- Windows 10/11
- macOS 10.14+
- Linux (Ubuntu 18.04+, CentOS 7+)

## 部署步骤

### 1. 数据库准备

#### 安装 PostgreSQL
1. 下载并安装 PostgreSQL: https://www.postgresql.org/download/
2. 设置数据库密码
3. 启动 PostgreSQL 服务

#### 创建数据库
```bash
# 连接到 PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE scooter_rental;

# 退出
\q
```

#### 执行初始化脚本
```bash
# 执行数据库初始化脚本
psql -U postgres -d scooter_rental -f database/init.sql
```

### 2. 后端部署

#### 配置数据库连接
编辑 `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/scooter_rental
spring.datasource.username=postgres
spring.datasource.password=your_password
```

#### 构建后端
```bash
cd backend

# 使用 Maven Wrapper (推荐)
./mvnw clean package

# 或使用系统 Maven
mvn clean package
```

#### 运行后端
```bash
# 运行打包后的应用
java -jar target/scooter-rental-1.0.0.jar

# 或使用 Maven 直接运行
./mvnw spring-boot:run
```

后端服务将在 http://localhost:8080 启动

### 3. 前端部署

#### 安装依赖
```bash
cd frontend
npm install
```

#### 开发环境运行
```bash
npm run dev
```
前端服务将在 http://localhost:5173 启动

#### 生产环境构建
```bash
npm run build
```
构建产物在 `dist` 目录

### 4. 验证部署

#### 测试后端API
```bash
# 测试健康检查
curl http://localhost:8080/api/scooters

# 应该返回滑板车列表
```

#### 测试前端访问
1. 打开浏览器访问 http://localhost:5173
2. 应该看到登录页面
3. 使用管理员账号测试: admin / admin

## 配置说明

### 后端配置

#### 应用配置 (application.properties)
```properties
# 服务器端口
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:postgresql://localhost:5432/scooter_rental
spring.datasource.username=postgres
spring.datasource.password=your_password

# JPA配置
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# JWT配置
jwt.secret=your-secret-key-change-in-production
jwt.expiration=604800

# CORS配置
cors.allowed-origins=http://localhost:5173
```

#### 生产环境配置
创建 `application-prod.properties`:
```properties
# 生产环境数据库
spring.datasource.url=jdbc:postgresql://prod-db-host:5432/scooter_rental
spring.datasource.username=prod_user
spring.datasource.password=prod_password

# 生产环境JWT密钥
jwt.secret=strong-production-secret-key

# 关闭开发功能
spring.jpa.show-sql=false
```

### 前端配置

#### 开发环境配置 (vite.config.js)
```javascript
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

#### 生产环境配置
创建生产环境 API 配置:
```javascript
// src/api/index.js
const api = axios.create({
  baseURL: process.env.NODE_ENV === 'production' 
    ? 'https://your-production-domain.com/api' 
    : '/api',
  timeout: 10000
})
```

## 生产环境部署

### Docker 部署（可选）

#### 后端 Dockerfile
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/scooter-rental-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### 前端 Dockerfile
```dockerfile
FROM node:16-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

#### docker-compose.yml
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: scooter_rental
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/scooter_rental
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
    depends_on:
      - postgres
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
```

## 故障排除

### 常见问题

#### 数据库连接失败
- 检查 PostgreSQL 服务是否启动
- 验证数据库连接配置
- 检查防火墙设置

#### 前端无法访问后端
- 检查后端服务是否正常运行
- 验证 CORS 配置
- 检查网络连接

#### JWT 认证失败
- 检查 JWT 密钥配置
- 验证令牌有效期设置
- 检查时钟同步

### 日志查看

#### 后端日志
```bash
# 查看 Spring Boot 应用日志
tail -f backend.log

# 或查看控制台输出
```

#### 前端日志
在浏览器开发者工具中查看控制台输出

### 性能优化

#### 数据库优化
- 定期清理过期预订记录
- 添加合适的数据库索引
- 考虑分区表处理大量数据

#### 应用优化
- 启用数据库连接池
- 配置合理的 JVM 参数
- 使用缓存机制

## 维护指南

### 定期任务
- 备份数据库
- 清理过期日志
- 更新系统依赖
- 监控系统性能

### 安全维护
- 定期更换 JWT 密钥
- 更新依赖包安全补丁
- 监控异常访问日志