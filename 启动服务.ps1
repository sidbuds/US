# 秘密基地 - 一键启动脚本
# 用法: 右键 -> 以管理员身份运行

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "    秘密基地 - 一键启动后端服务" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$projectDir = "D:\projects\情侣互动"
$backendDir = "$projectDir\backend"
$logFile = "$backendDir\pocketbase_output.txt"
$urlFile = "$backendDir\current_url.txt"

# Step 1: Start PocketBase
Write-Host "`n[1/3] 启动 PocketBase..." -ForegroundColor Yellow
Get-Process -Name "pocketbase" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

Start-Process -FilePath "$backendDir\pocketbase.exe" `
    -ArgumentList "serve","--http=0.0.0.0:8090" `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError "$backendDir\pb_error.txt" `
    -WindowStyle Hidden

Start-Sleep -Seconds 3

# Step 2: Start Cloudflare Tunnel
Write-Host "[2/3] 启动 Cloudflare 隧道..." -ForegroundColor Yellow
Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

$tunnelLog = "$backendDir\cf_output.txt"
Start-Process -FilePath "$backendDir\cloudflared.exe" `
    -ArgumentList "tunnel","--url","http://localhost:8090" `
    -RedirectStandardOutput $tunnelLog `
    -RedirectStandardError "$backendDir\cf_error.txt" `
    -WindowStyle Hidden

Write-Host "等待隧道建立..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# Step 3: Extract URL and update app
$url = Get-Content $tunnelLog -ErrorAction SilentlyContinue | 
    Select-String "https://.*trycloudflare.com" | 
    ForEach-Object { $_.Matches.Value } | Select-Object -First 1

if ($url) {
    Write-Host "[3/3] 更新 App 配置..." -ForegroundColor Yellow
    
    # Save URL
    $url | Out-File $urlFile -Encoding UTF8
    
    # Update build.gradle.kts
    $gradleFile = "$projectDir\app\build.gradle.kts"
    $content = Get-Content $gradleFile -Raw
    $content = $content -replace 'buildConfigField\("String", "POCKETBASE_URL", ".*?"\)', "buildConfigField(`"String`", `"POCKETBASE_URL`", `"\""$url`"`")"
    $content | Set-Content $gradleFile -Encoding UTF8
    
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "  后端服务已启动!" -ForegroundColor Green
    Write-Host "  隧道地址: $url" -ForegroundColor Green
    Write-Host "  管理后台: $url/_/" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    
    # Test connection
    try {
        $health = Invoke-RestMethod -Uri "$url/api/health" -TimeoutSec 5
        Write-Host "`n✅ PocketBase 运行正常!" -ForegroundColor Green
    } catch {
        Write-Host "`n⚠️ PocketBase 启动中，请稍等几秒再试" -ForegroundColor Yellow
    }
    
    Write-Host "`n提示: 首次使用请在浏览器打开 $url/_/" -ForegroundColor Cyan
    Write-Host "然后创建管理员账号: longteng@love.local / love123456" -ForegroundColor Cyan
} else {
    Write-Host "`n❌ 隧道地址获取失败，请检查日志: $tunnelLog" -ForegroundColor Red
}

Write-Host "`n按任意键关闭..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")