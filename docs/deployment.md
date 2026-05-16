# 部署指南

## 系统要求

### 后端要求
- Java 11 或更高版本（推荐 Java 17+）
- Maven 3.6+ 或使用内置的 Maven Wrapper
- PostgreSQL 12+ 数据库

### 前端要求
- Node.js 16+ 
- npm 或 yarn 包管理器

### 操作系统
- Windows 10/11
- macOS 10.14+
- Linux (Ubuntu 18.04+, CentOS 7+)

---

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

# 创建数据库（开发环境）
CREATE DATABASE scooter;

# 退出
\q
```

#### 执行初始化脚本
```bash
# 执行数据库初始化脚本
psql -U postgres -d scooter -f database/init.sql
```

> **注意**: 开发环境数据库名为 `scooter`（端口5432），生产环境建议使用 `scooter_rental`。测试环境使用 H2 内存数据库，无需手动创建。

---

### 2. 后端部署

#### 配置数据库连接
编辑 `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/scooter
spring.datasource.username=postgres
spring.datasource.password=your_password
```

#### 构建后端
```bash
cd backend

# 使用 Maven Wrapper (推荐)
./mvnw clean package -DskipTests

# 或使用系统 Maven
mvn clean package -DskipTests
```

#### 运行后端
```bash
# 运行打包后的应用（默认端口8080）
java -jar target/scooter-rental-1.0.0.jar

# 或使用 Maven 直接运行
./mvnw spring-boot:run

# 指定端口运行（示例：覆盖默认端口8080）
java -jar target/scooter-rental-1.0.0.jar --server.port=9090

# 使用生产配置运行
java -jar target/scooter-rental-1.0.0.jar --spring.profiles.active=prod
```

后端服务默认在 http://localhost:8080 启动（可通过 `SERVER_PORT` 环境变量修改，端口被占用会自动顺延）。

---

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
前端服务将在 http://localhost:5173 启动，自动代理 `/api` 请求到后端（端口被占用会自动顺延）。

#### 生产环境构建
```bash
npm run build
```
构建产物在 `dist` 目录，可部署到 Nginx 等 Web 服务器。

---

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

---

## 配置说明

### 后端配置

#### 应用配置 (application.properties)
```properties
# 服务器端口（可通过环境变量 SERVER_PORT 覆盖）
server.port=${SERVER_PORT:8080}
server.address=0.0.0.0

# 数据库配置
spring.datasource.url=jdbc:postgresql://localhost:5432/scooter
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.hikari.connection-timeout=10000
spring.datasource.hikari.maximum-pool-size=10

# JPA配置
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# 允许循环引用和Bean覆盖
spring.main.allow-circular-references=true
spring.main.allow-bean-definition-overriding=true

# JWT配置（可通过环境变量 JWT_SECRET 覆盖）
jwt.secret=${JWT_SECRET:dev-jwt-secret-key-change-in-production}
jwt.expiration=604800

# 日志配置
logging.level.com.scooter=DEBUG
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN

# 邮件配置（QQ邮箱示例）
spring.mail.host=smtp.qq.com
spring.mail.port=587
spring.mail.username=your-email@qq.com
spring.mail.password=your-auth-code
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Thymeleaf邮件模板
spring.thymeleaf.prefix=classpath:/templates/email/
spring.thymeleaf.suffix=.html
spring.thymeleaf.mode=HTML5
spring.thymeleaf.cache=false
```

#### 生产环境配置 (application-prod.properties)
```properties
# 生产环境数据库（使用环境变量）
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:scooter}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:}

# 生产环境JWT密钥
jwt.secret=${JWT_SECRET:}
jwt.expiration=2592000

# 关闭开发功能
spring.jpa.show-sql=false
spring.jpa.hibernate.ddl-auto=validate

# 安全配置
security.enable-csrf=true
security.require-ssl=false
```

#### 测试环境配置 (application-test.properties)
```properties
# 测试环境使用H2内存数据库
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

# JPA配置
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# 测试邮件配置（使用GreenMail）
spring.mail.host=localhost
spring.mail.port=3025
spring.mail.username=test@scooter.com
spring.mail.password=test
spring.mail.properties.mail.smtp.auth=false
spring.mail.properties.mail.smtp.starttls.enable=false
```

### 前端配置

#### 开发环境配置 (vite.config.js)
```javascript
export default defineConfig({
  plugins: [vue()],
  server: {
    port: process.env.VITE_PORT || 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_URL || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

#### 生产环境配置
```javascript
// src/api/index.js
const api = axios.create({
  baseURL: process.env.NODE_ENV === 'production' 
    ? 'https://your-production-domain.com/api' 
    : '/api',
  timeout: 10000
})
```

---

## 生产环境部署

### Docker 部署（可选）

#### 后端 Dockerfile
```dockerfile
FROM openjdk:17-jre-slim
WORKDIR /app
COPY target/scooter-rental-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

#### 前端 Dockerfile
```dockerfile
FROM node:18-alpine as build
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
    image: postgres:15
    environment:
      POSTGRES_DB: scooter_rental
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"

  backend:
    build: ./backend
    environment:
      SERVER_PORT: 8080
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/scooter_rental
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
      JWT_SECRET: production-secret-key-change-this
      SPRING_PROFILES_ACTIVE: prod
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

---

## 故障排除

### 常见问题

#### 数据库连接失败
- 检查 PostgreSQL 服务是否启动
- 验证数据库连接配置（数据库名是否为 `scooter`）
- 检查防火墙设置
- 确认连接超时设置（默认10秒）

#### 前端无法访问后端
- 检查后端服务是否正常运行（默认端口8080）
- 验证 Vite 代理配置中的 target 端口
- 检查 CORS 配置

#### JWT 认证失败
- 检查 JWT 密钥配置（生产环境务必修改默认密钥）
- 验证令牌有效期设置（默认7天）
- 检查时钟同步

#### 邮件发送失败
- 检查 SMTP 服务器配置
- 验证邮箱授权码是否正确
- QQ邮箱需使用授权码而非登录密码

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
- 启用数据库连接池（HikariCP）
- 配置合理的 JVM 参数
- 使用缓存机制

---

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
- 定期审查安全审计日志