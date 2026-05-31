#!/usr/bin/env bash
# ============================================================
# 秘密基地 — PocketBase 一键初始化脚本
# 功能: 创建管理员 → 创建用户 → 创建集合 → 初始化数据
# 用法: ./setup.sh [PB_URL] [ADMIN_EMAIL] [ADMIN_PASSWORD]
# ============================================================
set -euo pipefail

PB_URL="${1:-http://localhost:8090}"
ADMIN_EMAIL="${2:-admin@love.com}"
ADMIN_PASSWORD="${3:-admin123456}"

echo ""
echo "  ╔══════════════════════════════════╗"
echo "  ║      秘密基地 · 后端初始化       ║"
echo "  ╚══════════════════════════════════╝"
echo ""
echo "  PocketBase: $PB_URL"
echo ""

# ============================================================
# Step 1: 管理员认证
# ============================================================
echo "  [1/4] 管理员认证..."

TOKEN=$(curl -s -X POST "$PB_URL/api/collections/_superusers/auth-with-password" \
  -H "Content-Type: application/json" \
  -d "{\"identity\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
  | jq -r '.token // empty')

if [ -z "$TOKEN" ]; then
  echo "    创建管理员账号..."
  curl -s -X POST "$PB_URL/api/collections/_superusers/records" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\",\"passwordConfirm\":\"$ADMIN_PASSWORD\"}" > /dev/null

  TOKEN=$(curl -s -X POST "$PB_URL/api/collections/_superusers/auth-with-password" \
    -H "Content-Type: application/json" \
    -d "{\"identity\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}" \
    | jq -r '.token // empty')
  echo "    ✓ 管理员创建成功"
else
  echo "    ✓ 管理员已登录"
fi

AUTH="Authorization: Bearer $TOKEN"
CT="Content-Type: application/json"

# ============================================================
# Step 2: 创建用户
# ============================================================
echo ""
echo "  [2/4] 创建用户..."

create_user() {
  local name=$1 email=$2 password="${3:-love123456}"

  # 检查是否已存在
  local existing=$(curl -s "$PB_URL/api/collections/users/records?filter=email%3D%22$email%22" \
    -H "$AUTH" -H "$CT" | jq -r '.items[0].id // empty')

  if [ -n "$existing" ]; then
    echo "    ✓ $name ($email) 已存在 -> $existing"
    echo "$existing"
    return
  fi

  local result=$(curl -s -X POST "$PB_URL/api/collections/users/records" \
    -H "$AUTH" -H "$CT" \
    -d "{\"email\":\"$email\",\"password\":\"$password\",\"passwordConfirm\":\"$password\",\"username\":\"$name\",\"verified\":true}")

  local uid=$(echo "$result" | jq -r '.id // empty')
  if [ -n "$uid" ]; then
    echo "    ✓ $name ($email) -> $uid"
    echo "$uid"
  else
    echo "    ✗ 创建 $name 失败: $(echo "$result" | jq -r '.message // "unknown"')" >&2
  fi
}

LONGTENG_ID=$(create_user "隆腾" "longteng@love.local" | tail -1)
YANHUIXIN_ID=$(create_user "闫慧鑫" "yanhuixin@love.local" | tail -1)

# ============================================================
# Step 3: 创建数据集合
# ============================================================
echo ""
echo "  [3/4] 创建数据集合..."

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SCHEMA_DIR="$(dirname "$SCRIPT_DIR")/schema"

for file in couple_spaces.json checkins.json interactions.json diaries.json \
            diary_likes.json diary_comments.json coins.json coin_transactions.json \
            expenses.json wishlist.json wishlist_contributions.json; do

  FILE_PATH="$SCHEMA_DIR/$file"
  if [ ! -f "$FILE_PATH" ]; then
    echo "    ⚠ $file 不存在，跳过"
    continue
  fi

  NAME=$(jq -r '.name' "$FILE_PATH")

  # 检查是否已存在
  EXISTING=$(curl -s "$PB_URL/api/collections?filter=name%3D%22$NAME%22" \
    -H "$AUTH" -H "$CT" | jq -r '.items[0].id // empty')

  if [ -n "$EXISTING" ]; then
    echo "    ✓ $NAME 已存在"
    continue
  fi

  RESULT=$(curl -s -X POST "$PB_URL/api/collections" \
    -H "$AUTH" -H "$CT" \
    -d @"$FILE_PATH")

  CID=$(echo "$RESULT" | jq -r '.id // empty')
  if [ -n "$CID" ]; then
    echo "    ✓ $NAME -> $CID"
  else
    echo "    ✗ $NAME: $(echo "$RESULT" | jq -r '.message // "unknown"')" >&2
  fi
done

# ============================================================
# Step 4: 初始化数据
# ============================================================
echo ""
echo "  [4/4] 初始化数据..."

# 创建情侣空间
SPACE_ID=$(curl -s "$PB_URL/api/collections/couple_spaces/records?filter=user_a_id%3D%22$LONGTENG_ID%22" \
  -H "$AUTH" -H "$CT" | jq -r '.items[0].id // empty')

if [ -n "$SPACE_ID" ]; then
  echo "    ✓ 情侣空间已存在 -> $SPACE_ID"
else
  SPACE_RESULT=$(curl -s -X POST "$PB_URL/api/collections/couple_spaces/records" \
    -H "$AUTH" -H "$CT" \
    -d "{\"code\":\"520131\",\"user_a_id\":\"$LONGTENG_ID\",\"user_b_id\":\"$YANHUIXIN_ID\",\"status\":\"active\"}")
  SPACE_ID=$(echo "$SPACE_RESULT" | jq -r '.id // empty')
  if [ -n "$SPACE_ID" ]; then
    echo "    ✓ 情侣空间创建成功 -> $SPACE_ID (情侣码: 520131)"
  else
    echo "    ✗ 情侣空间创建失败" >&2
  fi
fi

# 创建金币余额
if [ -n "$SPACE_ID" ]; then
  EXISTING_COINS=$(curl -s "$PB_URL/api/collections/coins/records?filter=space_id%3D%22$SPACE_ID%22" \
    -H "$AUTH" -H "$CT" | jq -r '.items[0].id // empty')

  if [ -n "$EXISTING_COINS" ]; then
    echo "    ✓ 金币记录已存在"
  else
    curl -s -X POST "$PB_URL/api/collections/coins/records" \
      -H "$AUTH" -H "$CT" \
      -d "{\"space_id\":\"$SPACE_ID\",\"balance\":100,\"total_earned\":100,\"total_spent\":0}" > /dev/null
    echo "    ✓ 金币记录创建成功 (初始余额: 100)"
  fi
fi

echo ""
echo "  ╔══════════════════════════════════╗"
echo "  ║        初始化完成!               ║"
echo "  ╚══════════════════════════════════╝"
echo ""
echo "  用户账号:"
echo "    隆腾:   longteng@love.local / love123456"
echo "    闫慧鑫: yanhuixin@love.local / love123456"
echo ""
echo "  情侣码: 520131"
echo ""
echo "  管理后台: $PB_URL/_/"
echo ""
