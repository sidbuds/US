@echo off
chcp 65001 >nul
REM ============================================================
REM  秘密基地 — PocketBase 启动脚本
REM  用法: start.bat [端口号]
REM ============================================================
set PORT=%1
if "%PORT%"=="" set PORT=8090

echo.
echo   ╔══════════════════════════════════╗
echo   ║      秘密基地 · 后端服务         ║
echo   ╚══════════════════════════════════╝
echo.
echo   端口: %PORT%
echo   管理后台: http://localhost:%PORT%/_/
echo.

REM 检查 pocketbase.exe
if not exist "pocketbase.exe" (
    echo   [错误] 未找到 pocketbase.exe
    echo.
    echo   请下载 PocketBase:
    echo   https://github.com/pocketbase/pocketbase/releases
    echo.
    echo   下载后将 pocketbase.exe 放到 backend\ 目录下
    echo.
    pause
    exit /b 1
)

REM 创建数据目录
if not exist "data" mkdir data

echo   启动中... (Ctrl+C 停止)
echo.

pocketbase.exe serve --http=0.0.0.0:%PORT% --dir=data
