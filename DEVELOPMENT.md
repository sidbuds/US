# 情侣互动 App 安卓版 — 完整开发方案

> 基于 PocketBase + Kotlin + Jetpack Compose 的情侣专属互动应用，从零到一的完整开发指南。

---

## 目录

- [一、技术栈与部署方案](#一技术栈与部署方案)
- [二、技术架构详解](#二技术架构详解)
- [三、功能需求与数据库设计](#三功能需求与数据库设计)
  - [3.1 账号与绑定系统](#31-账号与绑定系统)
  - [3.2 互动模块：报备](#32-互动模块报备)
  - [3.3 互动模块：抱抱 / 亲亲](#33-互动模块抱抱--亲亲)
  - [3.4 日记系统](#34-日记系统)
  - [3.5 金币经济系统](#35-金币经济系统)
  - [3.6 虚拟形象与商店](#36-虚拟形象与商店)
  - [3.7 一起吃饭（虚拟饭局）](#37-一起吃饭虚拟饭局)
  - [3.8 共同记账](#38-共同记账)
  - [3.9 愿望清单](#39-愿望清单)
  - [3.10 数据关系总览（ER图）](#310-数据关系总览er图)
- [四、本地存储与离线支持](#四本地存储与离线支持)
- [五、开发环境与工具清单](#五开发环境与工具清单)
- [六、开发分期与时间计划](#六开发分期与时间计划)
- [七、成本估算](#七成本估算)
- [八、安全性与配置管理](#八安全性与配置管理)
- [九、常见问题解答](#九常见问题解答)
- [十、后续优化方向](#十后续优化方向)

---

## 一、技术栈与部署方案

### 技术选型

| 层级 | 技术选型 | 理由 |
|------|----------|------|
| **后端** | PocketBase | 单可执行文件，内置数据库 + 认证 + 文件存储 + 实时 API |
| **数据库** | SQLite（PocketBase 内置） | 零配置，数据存为单一文件 |
| **Android 前端** | Kotlin + Jetpack Compose | 现代 Android 开发主流方案 |
| **网络请求** | Retrofit + Kotlin Coroutines | 成熟稳定，与 Compose 配合丝滑 |
| **本地缓存** | Room | 离线也能查看日记和数据 |

### 为什么选 PocketBase？

PocketBase 是用 Go 语言编写的开源后端平台，将数据库、用户认证、文件存储、实时 API 全部打包进一个可执行文件中，5 分钟即可搭建完整后端服务。对于两人专用的小应用非常合适：

- **零配置**：内置 SQLite，无需安装 MySQL/PostgreSQL
- **自带管理后台**：浏览器打开 `http://localhost:8090/_/` 即可管理数据
- **实时同步**：基于 WebSocket 的实时订阅，互动消息即时推送
- **完全免费**：开源项目，部署在自有设备上，零云费用
- **单文件部署**：下载一个可执行文件，运行一条命令即可启动

### 部署步骤

**第一步：下载 PocketBase**

从 [GitHub Releases](https://github.com/pocketbase/pocketbase/releases) 下载对应操作系统的版本（Windows/Mac/Linux）。

**第二步：启动服务**

```bash
./pocketbase serve
```

首次运行自动打开浏览器安装页面，创建超级管理员账号。至此后端服务已运行在 `http://127.0.0.1:8090`。

**第三步：让外网能访问（异地场景）**

- **内网穿透（推荐）**：使用 `ngrok` 或 `cpolar`，一行命令获得公网域名：
  ```bash
  ngrok http 8090
  ```
- **云服务器**：阿里云/腾讯云轻量应用服务器（约 30-50 元/月），上传 PocketBase 运行即可。

**第四步：创建用户**

在 PocketBase 管理后台手动创建"龙腾"和"超级无敌亲亲老婆"两个用户账号。

---

## 二、技术架构详解

### 2.1 Android 应用架构（MVVM）

采用 Google 推荐的 MVVM 架构：

```
┌─────────────────────────────────────────────────────────┐
│                      View (UI层)                         │
│              Compose UI 组件 / Activity                  │
├─────────────────────────────────────────────────────────┤
│                   ViewModel (逻辑层)                      │
│              持有 State、处理用户事件、调用 Repository     │
├─────────────────────────────────────────────────────────┤
│                   Repository (数据层)                     │
│              决定从网络还是本地获取数据                    │
├─────────────────────────────────────────────────────────┤
│           RemoteDataSource (远程数据源)                   │
│                  PocketBase API 调用                      │
├─────────────────────────────────────────────────────────┤
│           LocalDataSource (本地数据源)                    │
│                Room 数据库（本地缓存）                     │
└─────────────────────────────────────────────────────────┘
```

**架构优势：**
- **职责分离**：UI 只管显示，ViewModel 只管状态，Repository 只管数据获取
- **易于测试**：每层可独立进行单元测试
- **生命周期安全**：配合 Coroutines 和 Flow，数据自动随页面生命周期管理
- **便于扩展**：新增功能不会互相影响

### 2.2 数据流设计

```
用户打开 App
  → UI → ViewModel → Repository
    → 判断本地是否有缓存
      ├─ 有缓存 → 返回本地数据 → 同时后台同步 PocketBase
      └─ 无缓存或刷新 → 从 PocketBase 获取 → 存入 Room → 返回 UI
```

### 2.3 实时同步机制

PocketBase 提供 WebSocket 实时订阅。客户端订阅关心的数据集合，数据变更时服务器自动推送更新。

**实现步骤：**

1. 建立 WebSocket 连接
2. 订阅情侣双方公共数据空间
3. 监听数据变更事件
4. 收到事件后更新 UI 和本地缓存

---

## 三、功能需求与数据库设计

### 3.1 账号与绑定系统

**功能描述：** 应用仅供两人使用，需要严格的双人绑定机制。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 登录方式 | 邮箱/密码登录（PocketBase 内置） |
| 首次启动 | 引导用户"创建情侣空间"或"加入已有空间" |
| 创建空间 | 用户 A 创建后，系统生成 6 位数字情侣码 |
| 加入空间 | 用户 B 输入情侣码，绑定后两人进入同一数据空间 |
| 唯一性限制 | 一个情侣码只能被绑定一次，防止第三人加入 |
| 密码找回 | 支持邮箱找回密码 |

**数据库表：**

```
users 表（PocketBase 内置）：
  - id          (主键)
  - email       (邮箱)
  - username    (用户名)
  - avatar      (头像URL)
  - created     (注册时间)

couple_spaces 表：
  - id          (主键)
  - code        (6位情侣码，唯一索引)
  - user_a_id   → users.id
  - user_b_id   → users.id
  - status      (active / disabled)
  - created_at  (创建时间)
```

---

### 3.2 互动模块：报备

**功能描述：** 双方互相报备实时状态（起床、到公司、到家等）。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 预设报备类型 | 起床、出门、到公司、吃午饭、下班、到家、睡觉（7 种） |
| 自定义报备 | 允许手动输入自定义内容 |
| 附带位置（可选） | 自愿附带地理位置，对方可在地图查看 |
| 时间戳 | 每条记录精确到秒 |
| 报备历史 | 按日期展示时间线，类似聊天记录 |
| 推送通知 | 对方报备时收到通知（前台/后台） |
| 金币奖励 | 每次报备给双方各 +5 金币 |

**数据库表：**

```
checkins 表：
  - id              (主键)
  - space_id        → couple_spaces.id
  - user_id         → users.id
  - type            (wake_up / leave_home / arrive_office / lunch / off_work / arrive_home / sleep / custom)
  - custom_content  (自定义内容，type=custom 时有值)
  - location_lat    (纬度，可为空)
  - location_lng    (经度，可为空)
  - location_name   (位置名称，可为空)
  - created_at      (报备时间)
```

---

### 3.3 互动模块：抱抱 / 亲亲

**功能描述：** 发送虚拟拥抱或亲吻，对方收到动画和通知，累计次数可兑换金币。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 互动类型 | 抱抱（hug）、亲亲（kiss） |
| 互动动画 | 点击后播放满屏爱心动画或漂浮特效 |
| 互动记录 | 显示"最近 x 次抱抱/亲亲"，按时间倒序 |
| 累计计数 | 分别统计总抱抱/亲亲次数 |
| 金币奖励 | 发起方 -5 金币，对方 +15 金币 |
| 每日限制 | 每日最多 10 次抱抱 + 10 次亲亲 |
| 连续互动奖励 | 连续 3 天互动，额外 +50 金币 |

**数据库表：**

```
interactions 表：
  - id            (主键)
  - space_id      → couple_spaces.id
  - from_user_id  → users.id
  - to_user_id    → users.id
  - type          (hug / kiss)
  - is_read       (对方是否已查看)
  - created_at    (互动时间)
```

**动画实现方案：**
- Compose 的 `AnimatedVisibility` + `animateFloatAsState`
- `Canvas` 绘制飘浮爱心 Particle System
- `VibrationEffect` 添加手机振动反馈

---

### 3.4 日记系统

**功能描述：** 双方各自写日记（文字 + 图片），可互相查看、点赞、评论。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 日记内容 | 文字（最多 5000 字）+ 最多 9 张图片 |
| 日记分类 | 日常 / 纪念日 / 心情 |
| 时光轴展示 | 按日期倒序，类似朋友圈 |
| 对方可见 | 双方都能看到彼此日记（不可隐藏） |
| 点赞 | 可点赞，显示数量和头像 |
| 评论 | 可写评论，显示在日记下方 |
| 编辑/删除 | 支持编辑、软删除 |
| 金币奖励 | 写一篇 +20 金币 |
| 彩蛋成就 | 连续写 7 天获"日记达人"称号，+100 金币 |

**数据库表：**

```
diaries 表：
  - id            (主键)
  - space_id      → couple_spaces.id
  - user_id       → users.id
  - category      (daily / anniversary / mood)
  - title         (标题)
  - content       (正文)
  - images        (图片URL列表，JSON格式)
  - likes_count   (点赞数)
  - created_at    (创建时间)
  - updated_at    (更新时间)
  - deleted       (软删除标志)

diary_likes 表：
  - id            (主键)
  - diary_id      → diaries.id
  - user_id       → users.id
  - created_at

diary_comments 表：
  - id            (主键)
  - diary_id      → diaries.id
  - user_id       → users.id
  - content       (评论内容)
  - created_at
```

---

### 3.5 金币经济系统

核心激励系统，所有行为货币化，增加游戏化体验。

**金币获取途径：**

| 行为 | 金币变化 | 每日上限 |
|------|----------|----------|
| 每日首次登录签到 | +10 | 1 次 |
| 写一篇日记 | +20 | 2 篇 |
| 给对方日记点赞 | +2 / 次 | 10 次 |
| 给对方日记写评论 | +5 / 次 | 5 次 |
| 进行一次报备 | +5 / 次 | 7 次 |
| 送出一个抱抱 | 发起方 -5，对方 +15 | 10 次 |
| 送出一个亲亲 | 发起方 -5，对方 +15 | 10 次 |
| 一起吃饭 | 双方各 +20（前提：各消耗 100 金币） | 3 次 |
| 连续 3 天互动 | 额外 +50 | 每 3 天 1 次 |
| 完善愿望清单 | +30 / 个愿望 | 5 个愿望 |

**金币使用途径：**

| 消费行为 | 消耗金币 | 说明 |
|----------|----------|------|
| 给对方买衣服（虚拟形象） | 100-500 | 不同价位，展示在主页 |
| 发起"一起吃饭" | 各 100 | 双方各消耗 100 金币 |
| 购买互动特效 | 50 | 下次抱抱/亲亲有特殊动画 |
| 购买每日报备提醒 | 30 | 提醒对方报备，持续一天 |

**数据库表：**

```
coins 表：
  - space_id      (主键，→ couple_spaces.id)
  - balance       (当前总余额)
  - total_earned  (累计获得)
  - total_spent   (累计消费)
  - updated_at

coin_transactions 表：
  - id            (主键)
  - space_id      → couple_spaces.id
  - user_id       → users.id
  - type          (earn / spend)
  - reason        (写日记 / 报备 / 抱抱 / 买衣服 等)
  - amount        (金币数量)
  - related_id    (关联表 ID，如 diary_id，可为空)
  - created_at
```

---

### 3.6 虚拟形象与商店

**功能描述：** 为虚拟形象购买服装，在主页展示对方当前形象。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 形象展示 | 主页上方显示两人卡通头像形象 |
| 可用部位 | 发型、上衣、裤子/裙子、鞋子、眼镜、配饰 |
| 商店 | 购买各部位新服饰 |
| 价格区间 | 基础款 50 金币，进阶款 200 金币，限量款 500 金币 |
| 穿戴/更换 | 已购买服饰可随时免费更换 |
| 对方赠送 | 可用金币给对方买衣服，对方收到通知 |
| 情侣套装 | 购买配对情侣装，展示时合并显示 |

**简化方案（MVP 阶段）：**
用"更换头像框"替代虚拟形象系统——商店出售不同样式的头像框，显示在头像周围，同样满足"买衣服"核心需求，开发成本大幅降低。

---

### 3.7 一起吃饭（虚拟饭局）

**功能描述：** 发起虚拟饭局，双方一起选菜，共同获得金币奖励。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 发起流程 | 用户 A 点击"一起吃饭"，消耗 100 金币，对方收到邀请 |
| 对方确认 | 用户 B 确认，也消耗 100 金币 |
| 美食选择 | 各自从预设菜单或自定义选一道菜 |
| 最终展示 | 生成合并美食卡片，展示两人选的菜品 |
| 卡片分享 | 可保存卡片图片，分享到微信/朋友圈 |
| 奖励 | 双方各 +20 金币 + "共进晚餐 x 次"累计 |
| 每日限制 | 最多 3 次 |

---

### 3.8 共同记账

**功能描述：** 记录共同开销，双方可见，有统计图表。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 账单条目 | 时间、金额、分类、备注、付款人 |
| 分类 | 餐饮、购物、旅行、房租水电、娱乐、其他 |
| 统计图表 | 饼图（按分类占比）、折线图（按时间趋势） |
| 标签/搜索 | 按标签分类查看、按金额范围搜索 |
| 实时同步 | 新增/修改/删除账单，对方实时同步 |
| 导出 | 支持导出 CSV 文件 |
| 金币激励 | 每笔记账 +5 金币（每日上限 5 笔） |

**数据库表：**

```
expenses 表：
  - id            (主键)
  - space_id      → couple_spaces.id
  - amount        (金额，单位：元)
  - category      (餐饮 / 购物 / 旅行 / 房租水电 / 娱乐 / 其他)
  - note          (备注)
  - paid_by       → users.id
  - date          (消费日期)
  - created_at
  - updated_at
```

**图表实现：** 使用 [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart) 库绘制饼图和条形图。

---

### 3.9 愿望清单

**功能描述：** 记录未来想一起做的事，可设定目标金币。

**详细需求：**

| 需求项 | 详细说明 |
|--------|----------|
| 愿望条目 | 愿望名称、目标金币、状态（进行中/已完成） |
| 进度条 | 显示已攒金币的进度百分比 |
| 双方认领 | 双方都可以向愿望存入金币 |
| 完成庆祝 | 达到目标时播放庆祝动画，双方收到通知 |
| 编辑/删除 | 支持修改、删除愿望 |
| 附图 | 可附参考图片（如想去的地方风景照） |
| 金币统计 | 统计"已完成愿望数"和"已花费总金币" |

**数据库表：**

```
wishlist 表：
  - id              (主键)
  - space_id        → couple_spaces.id
  - name            (愿望名称)
  - target_coins    (目标金币数)
  - current_balance (当前已存金币)
  - image_url       (参考图片URL，可为空)
  - status          (active / completed)
  - completed_at    (完成时间，可为空)
  - created_at
  - updated_at

wishlist_contributions 表：
  - id            (主键)
  - wish_id       → wishlist.id
  - user_id       → users.id
  - amount        (贡献金币数)
  - created_at
```

---

### 3.10 数据关系总览（ER 图）

```
users ──┬── (创建) ──→ couple_spaces (user_a_id / user_b_id)
        │
        ├──→ checkins (user_id)
        │
        ├──→ interactions (from_user_id / to_user_id)
        │
        ├──→ diaries (user_id)
        │     ├──→ diary_likes (diary_id)
        │     └──→ diary_comments (diary_id)
        │
        ├──→ coin_transactions (user_id)
        │
        ├──→ expenses (paid_by)
        │
        └──→ wishlist_contributions (user_id)

couple_spaces ──┬──→ checkins (space_id)
                ├──→ interactions (space_id)
                ├──→ diaries (space_id)
                ├──→ coins (space_id, 1:1)
                ├──→ coin_transactions (space_id)
                ├──→ expenses (space_id)
                └──→ wishlist (space_id)
                        └──→ wishlist_contributions (wish_id)
```

**核心关系说明：**
- `couple_spaces` 是数据隔离的核心，所有业务表通过 `space_id` 关联，实现两人数据空间的隔离
- `coins` 表与 `couple_spaces` 是 1:1 关系，存储当前金币余额
- `coin_transactions` 记录所有金币变动流水，可用于对账

---

## 四、本地存储与离线支持

**为什么需要本地存储：**
- 无网络时仍可查看日记、记账记录
- 减少网络请求，节省流量和电量
- 提供流畅的滚动和搜索体验

**实现方案：**
- 使用 Android Room 数据库
- 有网络时：同步本地数据到 PocketBase
- 无网络时：所有操作暂存本地，恢复网络后自动上传

---

## 五、开发环境与工具清单

### 5.1 开发工具

| 工具 | 用途 | 获取方式 |
|------|------|----------|
| Android Studio Ladybug 2024.1.1+ | Android 开发 IDE | [developer.android.com/studio](https://developer.android.com/studio) |
| JDK 17+ | Java 开发工具包 | Android Studio 内置 |
| PocketBase v0.22+ | 后端服务 | [github.com/pocketbase/pocketbase](https://github.com/pocketbase/pocketbase) |
| Postman / HTTPie | API 测试 | [postman.com](https://www.postman.com) |
| ngrok | 内网穿透 | [ngrok.com](https://ngrok.com) |
| Git + GitHub | 版本控制 | [github.com](https://github.com) |

### 5.2 依赖库清单（build.gradle）

```kotlin
dependencies {
    // Jetpack Compose UI
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ViewModel + Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")

    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // Room 本地数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // 图片加载
    implementation("io.coil-kt:coil-compose:2.7.0")

    // 图表库
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // WebSocket（实时同步）
    implementation("org.java-websocket:Java-WebSocket:1.5.6")

    // 权限处理
    implementation("com.google.accompanist:accompanist-permissions:0.35.0-alpha")
}
```

### 5.3 学习资源

| 资源 | 说明 | 链接 |
|------|------|------|
| Android 官方开发指南 | 从零学安卓 | [developer.android.com/docs](https://developer.android.com/docs) |
| Jetpack Compose 教程 | 现代 UI 开发 | [developer.android.com/jetpack/compose/tutorial](https://developer.android.com/jetpack/compose/tutorial) |
| Kotlin 官方文档 | Kotlin 语法 | [kotlinlang.org/docs](https://kotlinlang.org/docs/home.html) |
| PocketBase 官方文档 | 后端 API 使用 | [pocketbase.io/docs](https://pocketbase.io/docs) |

---

## 六、开发分期与时间计划

总开发周期约 **4-6 周**（业余时间），分四期推进。

### 第一期：基础框架（第 1 周，10-15 小时）

**里程碑：** 双方能登录 App，看到主框架页面，API 正常返回数据。

| 任务 | 时长 | 说明 |
|------|------|------|
| 搭建 Android Studio 项目 | 1h | 新建 Compose 项目，配置依赖 |
| 连接 PocketBase | 2h | 测试 API 连通性，写第一个网络请求 |
| 登录/绑定界面 | 3-4h | 登录页 + 情侣码绑定页 |
| 底部导航栏 | 2h | 五个 Tab 主页搭建 |
| ViewModel 基础结构 | 2-3h | MVVM 架构，Repository 和 ViewModel 基类 |
| 部署 PocketBase（可选） | 1h | 云服务器上传启动 |

### 第二期：核心互动（第 2 周，15-20 小时）

**里程碑：** 双方可以互相报备、发送抱抱和亲亲、写日记互动。

| 任务 | 时长 |
|------|------|
| 报备功能（界面 + API + 推送） | 3-4h |
| 抱抱/亲亲（按钮 + 动画 + API + 推送） | 4-5h |
| 日记（列表页、写日记页、详情页、点赞评论） | 6-8h |
| WebSocket 实时同步 | 2h |

### 第三期：经济与装扮（第 3 周，15-20 小时）

**里程碑：** 金币机制运转，双方可以互相买衣服、一起吃饭。

| 任务 | 时长 |
|------|------|
| 金币系统（获取/消费/交易记录） | 4-5h |
| 商店界面 + 虚拟形象展示 | 4-5h |
| 一起吃饭功能 | 4-5h |
| 互动事件关联金币 | 2-3h |

### 第四期：生活管理 + 完善（第 4 周，10-15 小时）

**里程碑：** 全部功能完成，可导出 APK 正式使用。

| 任务 | 时长 |
|------|------|
| 共同记账 | 3-4h |
| 愿望清单 | 2-3h |
| Room 离线缓存 | 2-3h |
| Bug 修复和体验优化 | 3-5h |
| 导出 APK 测试 | 1-2h |

---

## 七、成本估算

| 项目 | 费用 | 备注 |
|------|------|------|
| Android Studio | 免费 | 官方开发工具 |
| PocketBase | 免费 | 开源项目 |
| 云服务器（可选） | 0-50 元/月 | 不买则用 ngrok 免费穿透 |
| ngrok | 免费 | 每月 1GB 流量够用 |
| 开发者账号 | 0 元 | 双方内部使用，直接装 APK |
| **总计** | **0 元** | 完全零成本 |

> 如需长期异地且不想每次手动启动 ngrok，可租最便宜的云服务器（约 30-50 元/月），但非必须。

---

## 八、安全性与配置管理

### 8.1 安全注意事项

| 关注点 | 措施 |
|--------|------|
| API 通信 | PocketBase 默认 HTTPS（ngrok 自带 SSL），云服务器需配置 SSL 证书 |
| 用户认证 | 使用 PocketBase 内置 JWT Token，存储在 Android EncryptedSharedPreferences |
| 数据隔离 | 所有查询强制按 `space_id` 过滤，防止跨空间访问 |
| 情侣码安全 | 6 位数字码使用后即失效，设置 24 小时过期时间 |
| 敏感信息 | 不在客户端存储明文密码，Token 定期刷新 |
| 文件上传 | 限制图片大小（≤ 5MB/张），仅允许图片格式 |

### 8.2 配置管理建议

```kotlin
// 建议在 local.properties 或 BuildConfig 中管理敏感配置
object AppConfig {
    const val POCKETBASE_URL = BuildConfig.POCKETBASE_URL  // API 地址
    const val WS_URL = BuildConfig.WS_URL                   // WebSocket 地址
    const val IMAGE_MAX_SIZE_MB = 5                          // 图片大小限制
    const val DIARY_MAX_LENGTH = 5000                        // 日记字数限制
    const val COIN_HUG_COST = 5                              // 抱抱消耗金币
    const val COIN_KISS_COST = 5                             // 亲亲消耗金币
}
```

---

## 九、常见问题解答

**Q1：不想学编程，能直接用现成 App 吗？**
现有情侣 App（小恩爱、Couplete 等）难以完全匹配您的定制需求（金币系统、记账、愿望清单等）。如只想快速体验，可考虑「腾讯相册·共享空间」做日记+图片、「MoneyWiz」做共同账本、「番茄 ToDo」做清单——缺点是数据分散在不同 App 中。

**Q2：PocketBase 部署在本地，App 必须和电脑在同一网络？**
不需要。用 ngrok 等内网穿透工具，电脑联网即可，对方即使相隔千里也能访问。

**Q3：PocketBase 管理后台怎么用？**
浏览器打开 `http://localhost:8090/_/` 即可看到可视化管理界面，操作类似 Excel，可直接查看和编辑所有数据。

**Q4：对方换了手机怎么办？**
重新安装 App，用原邮箱和密码登录，所有数据自动同步回来（数据存在 PocketBase 服务器上）。

**Q5：可以先把功能跑起来再优化吗？**
完全可以！建议先实现登录和报备，让两人能"玩"起来，再根据实际体验迭代增加功能。每周一个小版本发布，持续改进。

**Q6：能上架 Google Play 吗？**
可以。需注册 Google Play 开发者账号（一次付费 25 美元），完善隐私政策后提交审核。

---

## 十、后续优化方向

| 优先级 | 功能 | 预期效果 |
|--------|------|----------|
| P1 | 纪念日倒计时 | 主页展示"在一起 x 天""距离生日 x 天" |
| P1 | 恋爱相册 | 专门的相册模块，按时间分组 |
| P2 | 每日匹配度报告 | AI 根据当日互动生成"今日默契度" |
| P2 | 情侣问答游戏 | "互相了解"题库，增加互动趣味 |
| P3 | 数据云备份 | 导出所有数据到本地文件备份 |
| P3 | 情侣套装系统 | 头像框增加"情侣套装"合集，购买后双方同时升级 |

---

## 附录：后续可补充内容

- [ ] PocketBase 完整部署配置脚本（含数据库表结构定义）
- [ ] Android 核心代码示例（登录、报备、WebSocket 实时同步）
- [ ] UI 界面设计稿建议（Figma 设计参考）
- [ ] 详细数据库 ER 图（可视化工具导出）
