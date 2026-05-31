# ============================================================
# 秘密基地 — PocketBase 初始化脚本 v4 (PocketBase v0.22)
# 动态解析 collectionId
# ============================================================
param(
    [string]$PBUrl = "http://localhost:8090",
    [string]$AdminEmail = "admin@love.com",
    [string]$AdminPassword = "admin123456"
)

$ErrorActionPreference = "Stop"

function Invoke-PB {
    param([string]$Url, [string]$Method="GET", [object]$Body=$null, [hashtable]$Headers=@{})
    $p = @{ Uri=$Url; Method=$Method; ContentType="application/json; charset=utf-8"; Headers=$Headers }
    if ($Body) {
        $json = if ($Body -is [string]) { $Body } else { $Body | ConvertTo-Json -Depth 10 }
        $p.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }
    return Invoke-RestMethod @p
}

Write-Host ""
Write-Host "  ╔══════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "  ║      秘密基地 · 后端初始化       ║" -ForegroundColor Magenta
Write-Host "  ╚══════════════════════════════════╝" -ForegroundColor Magenta
Write-Host ""

# [1] 管理员认证
Write-Host "  [1/4] 管理员认证..." -ForegroundColor Yellow
$auth = Invoke-PB "$PBUrl/api/admins/auth-with-password" -Method POST -Body @{
    identity=$AdminEmail; password=$AdminPassword
}
$h = @{Authorization="Bearer $($auth.token)"}
Write-Host "    ✓ 已登录" -ForegroundColor Green

# [2] 创建用户
Write-Host ""
Write-Host "  [2/4] 创建用户..." -ForegroundColor Yellow

function Ensure-User($name, $email, $username) {
    $ex = Invoke-PB "$PBUrl/api/collections/users/records?filter=username%3D%22$username%22" -Headers $h
    if ($ex.items.Count -gt 0) {
        Write-Host "    ✓ $name 已存在" -ForegroundColor DarkGray
        return $ex.items[0].id
    }
    $r = Invoke-PB "$PBUrl/api/collections/users/records" -Method POST -Headers $h -Body @{
        email=$email; password="love123456"; passwordConfirm="love123456"
        username=$username; name=$name
    }
    Write-Host "    ✓ $name -> $($r.id)" -ForegroundColor Green
    return $r.id
}

$ltId = Ensure-User "隆腾" "longteng@love.local" "longteng"
$yhId = Ensure-User "闫慧鑫" "yanhuixin@love.local" "yanhuixin"

# [3] 创建集合
Write-Host ""
Write-Host "  [3/4] 创建数据集合..." -ForegroundColor Yellow

function Get-OrCreate($name, $bodyStr) {
    $ex = Invoke-PB "$PBUrl/api/collections?filter=name%3D%22$name%22" -Headers $h
    if ($ex.items.Count -gt 0) {
        Write-Host "    ✓ $name 已存在" -ForegroundColor DarkGray
        return $ex.items[0].id
    }
    $r = Invoke-PB "$PBUrl/api/collections" -Method POST -Headers $h -Body $bodyStr
    Write-Host "    ✓ $name" -ForegroundColor Green
    return $r.id
}

# 先创建 couple_spaces (无依赖)
$csId = Get-OrCreate "couple_spaces" @"
{"name":"couple_spaces","type":"base","schema":[{"system":false,"id":"cs_code","name":"code","type":"text","required":true,"options":{"min":6,"max":6,"pattern":""}},{"system":false,"id":"cs_ua","name":"user_a_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"cs_ub","name":"user_b_id","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"cs_st","name":"status","type":"select","required":false,"options":{"values":["active","disabled"],"maxSelect":1}}],"indexes":["CREATE UNIQUE INDEX idx_cs_code ON couple_spaces (code) WHERE status = 'active'"],"listRule":"user_a_id = @request.auth.id || user_b_id = @request.auth.id","viewRule":"user_a_id = @request.auth.id || user_b_id = @request.auth.id","createRule":"@request.auth.id != ''","updateRule":"user_a_id = @request.auth.id || user_b_id = @request.auth.id"}
"@

# 创建依赖 couple_spaces 的集合 (用实际 csId)
$checkinsId = Get-OrCreate "checkins" @"
{"name":"checkins","type":"base","schema":[{"system":false,"id":"ck_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"ck_ui","name":"user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"ck_tp","name":"type","type":"select","required":true,"options":{"values":["wake_up","leave_home","arrive_office","lunch","off_work","arrive_home","sleep","mood","custom"],"maxSelect":1}},{"system":false,"id":"ck_cc","name":"custom_content","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"ck_mt","name":"mood_text","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"ck_lat","name":"location_lat","type":"number","required":false,"options":{"min":null,"max":null}},{"system":false,"id":"ck_lng","name":"location_lng","type":"number","required":false,"options":{"min":null,"max":null}},{"system":false,"id":"ck_ln","name":"location_name","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}}],"listRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''"}
"@

$interId = Get-OrCreate "interactions" @"
{"name":"interactions","type":"base","schema":[{"system":false,"id":"it_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"it_fu","name":"from_user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"it_tu","name":"to_user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"it_tp","name":"type","type":"select","required":true,"options":{"values":["hug","kiss","miss"],"maxSelect":1}},{"system":false,"id":"it_rn","name":"reason","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"it_ir","name":"is_read","type":"bool","required":false,"options":{}}],"listRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''","updateRule":"to_user_id = @request.auth.id"}
"@

# diaries (depends on couple_spaces)
$diariesId = Get-OrCreate "diaries" @"
{"name":"diaries","type":"base","schema":[{"system":false,"id":"dy_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"dy_ui","name":"user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"dy_ct","name":"category","type":"select","required":false,"options":{"values":["daily","anniversary","mood"],"maxSelect":1}},{"system":false,"id":"dy_tl","name":"title","type":"text","required":true,"options":{"min":null,"max":200,"pattern":""}},{"system":false,"id":"dy_cn","name":"content","type":"text","required":true,"options":{"min":null,"max":5000,"pattern":""}},{"system":false,"id":"dy_im","name":"images","type":"json","required":false,"options":{}},{"system":false,"id":"dy_lc","name":"likes_count","type":"number","required":false,"options":{"min":0,"max":null}},{"system":false,"id":"dy_dl","name":"deleted","type":"bool","required":false,"options":{}}],"listRule":"(space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id) && deleted = false","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''","updateRule":"user_id = @request.auth.id","deleteRule":"user_id = @request.auth.id"}
"@

# diary_likes (depends on diaries)
Get-OrCreate "diary_likes" @"
{"name":"diary_likes","type":"base","schema":[{"system":false,"id":"dl_di","name":"diary_id","type":"relation","required":true,"options":{"collectionId":"$diariesId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"dl_ui","name":"user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}}],"listRule":"@request.auth.id != ''","createRule":"@request.auth.id != ''","deleteRule":"user_id = @request.auth.id"}
"@ | Out-Null

# diary_comments (depends on diaries)
Get-OrCreate "diary_comments" @"
{"name":"diary_comments","type":"base","schema":[{"system":false,"id":"dc_di","name":"diary_id","type":"relation","required":true,"options":{"collectionId":"$diariesId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"dc_ui","name":"user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"dc_cn","name":"content","type":"text","required":true,"options":{"min":1,"max":1000,"pattern":""}}],"listRule":"@request.auth.id != ''","createRule":"@request.auth.id != ''","deleteRule":"user_id = @request.auth.id"}
"@ | Out-Null

# coins
Get-OrCreate "coins" @"
{"name":"coins","type":"base","schema":[{"system":false,"id":"cn_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"cn_bl","name":"balance","type":"number","required":false,"options":{"min":0,"max":null}},{"system":false,"id":"cn_te","name":"total_earned","type":"number","required":false,"options":{"min":0,"max":null}},{"system":false,"id":"cn_ts","name":"total_spent","type":"number","required":false,"options":{"min":0,"max":null}}],"listRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''","updateRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id"}
"@ | Out-Null

# coin_transactions
Get-OrCreate "coin_transactions" @"
{"name":"coin_transactions","type":"base","schema":[{"system":false,"id":"ct_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"ct_ui","name":"user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"ct_tp","name":"type","type":"select","required":true,"options":{"values":["earn","spend"],"maxSelect":1}},{"system":false,"id":"ct_rn","name":"reason","type":"text","required":false,"options":{"min":null,"max":200,"pattern":""}},{"system":false,"id":"ct_am","name":"amount","type":"number","required":true,"options":{"min":0,"max":null}},{"system":false,"id":"ct_ri","name":"related_id","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}}],"listRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''"}
"@ | Out-Null

# expenses
Get-OrCreate "expenses" @"
{"name":"expenses","type":"base","schema":[{"system":false,"id":"ex_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"ex_am","name":"amount","type":"number","required":true,"options":{"min":0,"max":null}},{"system":false,"id":"ex_ct","name":"category","type":"select","required":true,"options":{"values":["food","shopping","travel","housing","entertainment","other","income"],"maxSelect":1}},{"system":false,"id":"ex_nt","name":"note","type":"text","required":false,"options":{"min":null,"max":500,"pattern":""}},{"system":false,"id":"ex_pb","name":"paid_by","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"ex_ii","name":"is_income","type":"bool","required":false,"options":{}},{"system":false,"id":"ex_dt","name":"date","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}}],"listRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''","updateRule":"paid_by = @request.auth.id","deleteRule":"paid_by = @request.auth.id"}
"@ | Out-Null

# wishlist
$wlId = Get-OrCreate "wishlist" @"
{"name":"wishlist","type":"base","schema":[{"system":false,"id":"wl_sp","name":"space_id","type":"relation","required":true,"options":{"collectionId":"$csId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"wl_nm","name":"name","type":"text","required":true,"options":{"min":1,"max":100,"pattern":""}},{"system":false,"id":"wl_tc","name":"target_coins","type":"number","required":true,"options":{"min":1,"max":null}},{"system":false,"id":"wl_cb","name":"current_balance","type":"number","required":false,"options":{"min":0,"max":null}},{"system":false,"id":"wl_iu","name":"image_url","type":"text","required":false,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"wl_st","name":"status","type":"select","required":false,"options":{"values":["active","completed"],"maxSelect":1}}],"listRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","viewRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","createRule":"@request.auth.id != ''","updateRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id","deleteRule":"space_id.user_a_id = @request.auth.id || space_id.user_b_id = @request.auth.id"}
"@

# wishlist_contributions (depends on wishlist)
Get-OrCreate "wishlist_contributions" @"
{"name":"wishlist_contributions","type":"base","schema":[{"system":false,"id":"wc_wi","name":"wish_id","type":"relation","required":true,"options":{"collectionId":"$wlId","cascadeDelete":true,"maxSelect":1}},{"system":false,"id":"wc_ui","name":"user_id","type":"text","required":true,"options":{"min":null,"max":null,"pattern":""}},{"system":false,"id":"wc_am","name":"amount","type":"number","required":true,"options":{"min":1,"max":null}}],"listRule":"@request.auth.id != ''","createRule":"@request.auth.id != ''"}
"@ | Out-Null

# [4] 初始化数据
Write-Host ""
Write-Host "  [4/4] 初始化数据..." -ForegroundColor Yellow

# 情侣空间
$space = Invoke-PB "$PBUrl/api/collections/couple_spaces/records?filter=user_a_id%3D%22$ltId%22" -Headers $h
if ($space.items.Count -gt 0) {
    $spaceId = $space.items[0].id
    Write-Host "    ✓ 情侣空间已存在" -ForegroundColor DarkGray
} else {
    $s = Invoke-PB "$PBUrl/api/collections/couple_spaces/records" -Method POST -Headers $h -Body @{
        code="520131"; user_a_id=$ltId; user_b_id=$yhId; status="active"
    }
    $spaceId = $s.id
    Write-Host "    ✓ 情侣空间 (码: 520131)" -ForegroundColor Green
}

# 金币余额
$coins = Invoke-PB "$PBUrl/api/collections/coins/records?filter=space_id%3D%22$spaceId%22" -Headers $h
if ($coins.items.Count -gt 0) {
    Write-Host "    ✓ 金币已存在 (余额: $($coins.items[0].balance))" -ForegroundColor DarkGray
} else {
    Invoke-PB "$PBUrl/api/collections/coins/records" -Method POST -Headers $h -Body @{
        space_id=$spaceId; balance=100; total_earned=100; total_spent=0
    } | Out-Null
    Write-Host "    ✓ 金币 (初始: 100)" -ForegroundColor Green
}

Write-Host ""
Write-Host "  ╔══════════════════════════════════╗" -ForegroundColor Green
Write-Host "  ║        初始化完成!               ║" -ForegroundColor Green
Write-Host "  ╚══════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "  隆腾:   longteng@love.local / love123456" -ForegroundColor Cyan
Write-Host "  闫慧鑫: yanhuixin@love.local / love123456" -ForegroundColor Cyan
Write-Host "  情侣码: 520131  |  初始金币: 100" -ForegroundColor Magenta
Write-Host "  管理后台: $PBUrl/_/" -ForegroundColor White
Write-Host ""
