# 秘密基地 ❤️

情侣专属互动应用 — 记录我们的每一天

## 项目结构

```
情侣互动/
├── app/              # Android 前端 (Kotlin + Jetpack Compose)
├── backend/          # PocketBase 后端
│   ├── scripts/      # 启动和初始化脚本
│   ├── schema/       # 数据集合定义
│   └── hooks/        # 后端扩展钩子
├── DEVELOPMENT.md    # 完整开发文档
└── README.md         # 本文件
```

## 快速启动

### 后端

```bash
cd backend
# 1. 下载 pocketbase.exe 放到 backend/ 目录
# 2. 启动
scripts\start.bat
# 3. 初始化集合和用户
powershell -File scripts\setup.ps1
```

### 前端

```bash
# 用 Android Studio 打开项目根目录
# 修改 app/build.gradle.kts 中的 POCKETBASE_URL
# 运行到手机或模拟器
```

## 功能模块

| 模块 | 说明 |
|------|------|
| 身份选择 | 直接选人进入，无需登录 |
| 报备 | 起床/出门/到公司/心情 等 8 种状态 |
| 互动 | 抱抱/亲亲/想你 + 动画 |
| 日记 | 文字 + 图片 + 点赞评论 |
| 倒数日 | 生日/纪念日/自定义，可选主页展示 |
| 记账 | 收入/支出 + 分类统计 |
| 愿望清单 | 目标金币 + 进度追踪 |
| 金币系统 | 游戏化激励 |
