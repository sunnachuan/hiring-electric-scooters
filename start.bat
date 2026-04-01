@echo off
echo ========================================
echo   电动滑板车租赁系统启动脚本
echo ========================================
echo.

echo 请确保已安装以下环境：
echo 1. Java 11+ 环境
echo 2. Node.js 16+ 环境
echo 3. PostgreSQL 数据库
echo.

set /p choice=是否继续启动系统？(y/n): 
if /i "%choice%" neq "y" (
    echo 启动已取消
    pause
    exit /b
)

echo.
echo 步骤1: 启动后端服务...
cd backend
call mvnw spring-boot:run
if %errorlevel% neq 0 (
    echo 后端启动失败，请检查Java和Maven环境
    pause
    exit /b
)

echo.
echo 步骤2: 启动前端服务...
cd ..\frontend
call npm install
if %errorlevel% neq 0 (
    echo 前端依赖安装失败
    pause
    exit /b
)

call npm run dev
if %errorlevel% neq 0 (
    echo 前端启动失败
    pause
    exit /b
)

echo.
echo ========================================
echo   系统启动完成！
echo ========================================
echo.
echo 访问地址:
echo 前端: http://localhost:5173
echo 后端: http://localhost:8080
echo.
echo 管理员账号:
echo 用户名: admin
echo 密码: admin
echo.
pause