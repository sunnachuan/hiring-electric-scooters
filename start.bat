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
echo ========================================
echo   请在新窗口中手动启动以下服务：
echo ========================================
echo.
echo 步骤1: 启动后端服务
echo    cd backend
echo    mvnw spring-boot:run
echo    后端默认端口: 8080（可通过 SERVER_PORT 环境变量修改，被占用自动顺延）
echo.
echo 步骤2: 启动前端服务
echo    cd frontend
echo    npm install
echo    npm run dev
echo    前端默认端口: 5173（可通过 VITE_PORT 环境变量修改，被占用自动顺延）
echo.
echo ========================================
echo   启动后访问地址
echo ========================================
echo.
echo 前端界面: http://localhost:5173
echo 后端API: http://localhost:8080
echo.
echo 管理员账号:
echo 用户名: admin
echo 密码: admin
echo.
pause