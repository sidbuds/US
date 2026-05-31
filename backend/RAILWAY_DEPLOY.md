# Railway 部署指南

## 步骤 1：注册 Railway
1. 访问 https://railway.app
2. 点击 "Login" → 用 GitHub 账号登录
3. 新用户每月有  免费额度

## 步骤 2：创建项目
1. 登录后点击 "New Project"
2. 选择 "Deploy from GitHub Repo" 或 "Empty Project"

## 步骤 3：部署 PocketBase
选择以下方式之一：

### 方式 A：从 Docker Hub 部署（推荐）
1. 点击 "New Service" → "Docker Image"
2. 输入镜像名：ghcr.io/muchobien/pocketbase:latest
3. 点击 "Deploy"

### 方式 B：从 GitHub 仓库部署
1. 将 backend 文件夹上传到 GitHub
2. 在 Railway 连接该仓库
3. 自动构建部署

## 步骤 4：配置环境
1. 在 Service 设置中添加 Volume：
   - Mount Path: /pb/pb_data
2. 设置端口：
   - PORT: 8080

## 步骤 5：获取域名
1. 部署完成后，点击 Service → "Settings"
2. 在 "Networking" 部分点击 "Generate Domain"
3. 获得固定域名，如：love-app.up.railway.app

## 步骤 6：初始化 PocketBase
1. 访问 https://你的域名/admin
2. 创建管理员账号
3. 创建所需的 Collections（参考项目文档）

## 步骤 7：更新 App 配置
将域名告诉我，我帮你更新到 App 中重新打包。

## 注意事项
- 免费额度 /月，PocketBase 轻量应用完全够用
- 项目 30 天不活跃会休眠，首次访问需等 1-2 分钟
- 数据会持久化保存在 Volume 中