# 秘密基地 — 后端服务

基于 **PocketBase v0.22+** 的后端服务。一个可执行文件包含数据库、API、认证、实时推送。

## 快速开始

### 1. 下载 PocketBase

从 [GitHub Releases](https://github.com/pocketbase/pocketbase/releases) 下载 Windows/Linux/macOS 版本，放到 `backend/` 目录：

```
backend/
├── pocketbase.exe     ← 放这里 (Windows)
├── pb_hooks/          ← JS 钩子 (自动加载)
├── schema/            ← 集合定义
├── scripts/           ← 脚本
└── data/              ← 运行时数据 (自动创建)
```

### 2. 启动服务

```powershell
# Windows
cd backend
.\scripts\start.bat

# Linux/Mac
cd backend
chmod +x scripts/start.sh
./scripts/start.sh
```

首次启动后打开 **http://localhost:8090/_/** 创建管理员：
- 邮箱: `admin@love.com`
- 密码: `admin123456`

### 3. 初始化数据

保持 PocketBase 运行，另开终端执行：

```powershell
# Windows PowerShell
.\scripts\setup.ps1

# Linux/Mac
chmod +x scripts/setup.sh
./scripts/setup.sh
```

自动创建：
- ✅ 11 个数据集合
- ✅ 2 个用户账号
- ✅ 情侣空间 (情侣码: 520131)
- ✅ 金币账户 (初始余额: 100)

## 用户账号

| 用户 | 邮箱 | 密码 |
|------|------|------|
| 隆腾 🐷 | `longteng@love.local` | `love123456` |
| 闫慧鑫 💍 | `yanhuixin@love.local` | `love123456` |

## 数据集合

| 集合 | 说明 | Hook |
|------|------|------|
| `couple_spaces` | 情侣空间 (两人绑定) | — |
| `checkins` | 报备记录 | +5 金币/次, 每日7次 |
| `interactions` | 互动 (抱抱/亲亲/想你) | 抱抱/亲亲: -5/+15 金币; 想你: +10 |
| `diaries` | 日记 | +20 金币/篇, 每日2篇 |
| `diary_likes` | 日记点赞 | +2 金币/次, 每日10次 |
| `diary_comments` | 日记评论 | +5 金币/次, 每日5次 |
| `coins` | 金币余额 | — |
| `coin_transactions` | 金币交易流水 | — |
| `expenses` | 共同记账 | +5 金币/笔, 每日5笔 |
| `wishlist` | 愿望清单 | 达成 +30 金币 |
| `wishlist_contributions` | 愿望贡献 | 自动扣款+更新进度 |

## 业务逻辑 (pb_hooks/)

### coin_rewards.pb.js — 金币自动奖励

各种行为触发时自动发放/扣除金币：

```
报备    → 双方各 +5  (每日7次)
写日记  → 作者 +20   (每日2篇)
点赞    → 操作者 +2  (每日10次)
评论    → 操作者 +5  (每日5次)
记账    → 记账者 +5  (每日5笔)
登录    → 用户 +10   (每日1次)
想你    → 发起方 +10 (每日20次)
抱抱    → 发起方 -5, 接收方 +15
亲亲    → 发起方 -5, 接收方 +15
```

### validation.pb.js — 服务端校验

- 每日互动次数上限
- 每日报备次数上限
- 每日日记数量上限
- 愿望贡献金币余额检查

### achievements.pb.js — 成就系统

- 愿望达成自动标记完成 + 奖励
- 连续3天互动奖励 +50 金币
- 愿望贡献自动扣款

## 外网访问

两人异地时需要外网访问：

### 方案1: ngrok (推荐，免费)

```bash
ngrok http 8090
# 得到 https://xxx.ngrok.io
```

### 方案2: 云服务器

```bash
# 上传 pocketbase 到服务器
scp pocketbase user@server:~/backend/
ssh user@server
cd ~/backend
nohup ./pocketbase serve --http=0.0.0.0:8090 --dir=data &
```

### 方案3: frp / cpolar

类似 ngrok 的内网穿透工具。

## Android 端配置

修改 `app/build.gradle.kts` 中的 `POCKETBASE_URL`：

```kotlin
// 模拟器 (默认)
buildConfigField("String", "POCKETBASE_URL", "\"http://10.0.2.2:8090\"")

// 真机 (同一 WiFi)
buildConfigField("String", "POCKETBASE_URL", "\"http://192.168.x.x:8090\"")

// 外网 (ngrok / 云服务器)
buildConfigField("String", "POCKETBASE_URL", "\"https://xxx.ngrok.io\"")
```

同时确保 `network_security_config.xml` 允许明文通信 (开发环境)。

## 管理后台

PocketBase 自带可视化管理界面：

- 地址: `http://localhost:8090/_/`
- 功能: 查看/编辑所有数据、管理用户、查看日志
- 操作: 类似 Excel，可直接增删改查

## 目录结构

```
backend/
├── pocketbase.exe           # PocketBase 可执行文件 (需下载)
├── README.md                # 本文档
├── pb_hooks/                # JavaScript 业务钩子 (自动加载)
│   ├── coin_rewards.pb.js   # 金币奖励逻辑
│   ├── validation.pb.js     # 数据校验规则
│   └── achievements.pb.js   # 成就/连续奖励
├── schema/                  # 集合 schema 定义
│   ├── collections.json     # 完整 schema (合并版)
│   ├── couple_spaces.json   # 单独 schema 文件
│   ├── checkins.json
│   ├── interactions.json
│   ├── diaries.json
│   ├── diary_likes.json
│   ├── diary_comments.json
│   ├── coins.json
│   ├── coin_transactions.json
│   ├── expenses.json
│   ├── wishlist.json
│   └── wishlist_contributions.json
├── scripts/
│   ├── start.bat            # Windows 启动
│   ├── start.sh             # Linux/Mac 启动
│   ├── setup.ps1            # Windows 初始化
│   └── setup.sh             # Linux/Mac 初始化
└── data/                    # PocketBase 运行时数据 (自动创建)
    ├── data.db              # SQLite 数据库
    └── logs/                # 日志
```

## 常见问题

**Q: pocketbase.exe 放哪里？**
放到 `backend/` 目录下，和 `pb_hooks/`、`schema/` 同级。

**Q: 如何重置所有数据？**
停止 PocketBase → 删除 `data/` 文件夹 → 重新启动 → 重新运行 setup 脚本。

**Q: 如何备份数据？**
复制 `data/data.db` 文件即可。SQLite 单文件数据库。

**Q: 手机连不上 PocketBase？**
确保手机和电脑在同一 WiFi，使用电脑的局域网 IP (如 `192.168.1.100:8090`)。

**Q: 如何查看金币交易记录？**
打开管理后台 → coin_transactions 集合 → 按 created 排序。
