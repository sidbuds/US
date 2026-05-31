# 秘密基地 — 后端健康检查脚本
param(
    [string]$PBUrl = "http://localhost:8090"
)

Write-Host ""
Write-Host "  秘密基地 · 健康检查" -ForegroundColor Cyan
Write-Host "  ====================" -ForegroundColor Cyan
Write-Host ""

# 检查 PocketBase 是否运行
try {
    $health = Invoke-RestMethod -Uri "$PBUrl/api/health" -TimeoutSec 5
    Write-Host "  ✓ PocketBase 运行中" -ForegroundColor Green
} catch {
    Write-Host "  ✗ PocketBase 未运行 ($PBUrl)" -ForegroundColor Red
    Write-Host "  请先运行: .\scripts\start.bat" -ForegroundColor Yellow
    exit 1
}

# 检查用户
try {
    $users = Invoke-RestMethod -Uri "$PBUrl/api/collections/users/records?perPage=10"
    Write-Host "  ✓ 用户数量: $($users.totalItems)" -ForegroundColor Green
    foreach ($u in $users.items) {
        Write-Host "    - $($u.username) ($($u.email))" -ForegroundColor Gray
    }
} catch {
    Write-Host "  ✗ 无法查询用户" -ForegroundColor Red
}

# 检查集合
$expectedCollections = @("couple_spaces", "checkins", "interactions", "diaries",
    "diary_likes", "diary_comments", "coins", "coin_transactions",
    "expenses", "wishlist", "wishlist_contributions")

$missing = @()
foreach ($name in $expectedCollections) {
    try {
        $null = Invoke-RestMethod -Uri "$PBUrl/api/collections/$name/records?perPage=1" -TimeoutSec 5
    } catch {
        $missing += $name
    }
}

if ($missing.Count -eq 0) {
    Write-Host "  ✓ 全部 $($expectedCollections.Count) 个集合已创建" -ForegroundColor Green
} else {
    Write-Host "  ✗ 缺少 $($missing.Count) 个集合:" -ForegroundColor Red
    foreach ($m in $missing) {
        Write-Host "    - $m" -ForegroundColor Yellow
    }
    Write-Host "  运行 .\scripts\setup.ps1 来创建" -ForegroundColor Yellow
}

# 检查情侣空间
try {
    $spaces = Invoke-RestMethod -Uri "$PBUrl/api/collections/couple_spaces/records?perPage=10"
    if ($spaces.totalItems -gt 0) {
        Write-Host "  ✓ 情侣空间已创建 (情侣码: $($spaces.items[0].code))" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ 情侣空间未创建" -ForegroundColor Yellow
    }
} catch { }

# 检查金币
try {
    $coins = Invoke-RestMethod -Uri "$PBUrl/api/collections/coins/records?perPage=10"
    if ($coins.totalItems -gt 0) {
        Write-Host "  ✓ 金币账户已创建 (余额: $($coins.items[0].balance))" -ForegroundColor Green
    }
} catch { }

# 检查 hooks
$hooksPath = Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "..\pb_hooks"
if (Test-Path $hooksPath) {
    $hookFiles = Get-ChildItem $hooksPath -Filter "*.pb.js" | Select-Object -ExpandProperty Name
    if ($hookFiles.Count -gt 0) {
        Write-Host "  ✓ JS Hooks 已加载 ($($hookFiles.Count) 个)" -ForegroundColor Green
        foreach ($f in $hookFiles) {
            Write-Host "    - $f" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "  ⚠ pb_hooks 目录不存在" -ForegroundColor Yellow
}

Write-Host ""
