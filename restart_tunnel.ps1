# restart_tunnel.ps1 - Restarts cloudflared and updates the app config
# Usage: .\restart_tunnel.ps1

Write-Host "=== Restarting Cloudflare Tunnel ===" -ForegroundColor Cyan

# Kill existing cloudflared
Get-Process -Name "cloudflared" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 2

# Start new tunnel and capture URL
$outputFile = "$PSScriptRoot\backend\cf_output.txt"
Start-Process -FilePath "$PSScriptRoot\backend\cloudflared.exe" `
    -ArgumentList "tunnel","--url","http://localhost:8090" `
    -RedirectStandardOutput $outputFile `
    -RedirectStandardError "$PSScriptRoot\backend\cf_error.txt" `
    -WindowStyle Hidden

Write-Host "Waiting for tunnel URL..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

# Extract URL
$url = Get-Content $outputFile -ErrorAction SilentlyContinue | 
    Select-String "https://.*trycloudflare.com" | 
    ForEach-Object { $_.Matches.Value }

if ($url) {
    Write-Host "New tunnel URL: $url" -ForegroundColor Green
    
    # Update build.gradle.kts
    $gradleFile = "$PSScriptRoot\app\build.gradle.kts"
    $content = Get-Content $gradleFile -Raw
    $content = $content -replace 'buildConfigField\("String", "POCKETBASE_URL", ".*?"\)', "buildConfigField(`"String`", `"POCKETBASE_URL`", `"\""$url`"`")"
    $content | Set-Content $gradleFile -Encoding UTF8
    Write-Host "Updated app/build.gradle.kts with new URL" -ForegroundColor Green
    
    # Verify health
    try {
        $health = Invoke-RestMethod -Uri "$url/api/health" -TimeoutSec 10
        Write-Host "PocketBase health: $($health.message)" -ForegroundColor Green
    } catch {
        Write-Host "Warning: Could not reach PocketBase through tunnel" -ForegroundColor Red
    }
} else {
    Write-Host "ERROR: Could not find tunnel URL!" -ForegroundColor Red
    Get-Content $outputFile
}
