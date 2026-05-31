#!/bin/bash
# ============================================================
#  秘密基地 — PocketBase 启动脚本
#  用法: ./start.sh [端口号]
# ============================================================
PORT=${1:-8090}

echo ""
echo "  ╔══════════════════════════════════╗"
echo "  ║      秘密基地 · 后端服务         ║"
echo "  ╚══════════════════════════════════╝"
echo ""
echo "  端口: $PORT"
echo "  管理后台: http://localhost:$PORT/_/"
echo ""

# 查找 pocketbase 可执行文件
PB_CMD=""
if [ -f "./pocketbase" ]; then
    PB_CMD="./pocketbase"
elif [ -f "./pocketbase.exe" ]; then
    PB_CMD="./pocketbase.exe"
else
    echo "  [错误] 未找到 pocketbase"
    echo ""
    echo "  请下载 PocketBase:"
    echo "  https://github.com/pocketbase/pocketbase/releases"
    echo ""
    echo "  下载后放到 backend/ 目录下"
    exit 1
fi

# 创建数据目录
mkdir -p data

echo "  启动中... (Ctrl+C 停止)"
echo ""

$PB_CMD serve --http=0.0.0.0:$PORT --dir=data
