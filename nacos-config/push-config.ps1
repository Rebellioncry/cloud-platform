$NACOS_HOST = if ($env:NACOS_HOST) { $env:NACOS_HOST } else { "lyz.iot.com" }
$NACOS_PORT = if ($env:NACOS_PORT) { $env:NACOS_PORT } else { "8848" }
$NACOS_URL = "http://${NACOS_HOST}:${NACOS_PORT}"
$NACOS_USER = if ($env:NACOS_USER) { $env:NACOS_USER } else { "nacos" }
$NACOS_PASS = if ($env:NACOS_PASS) { $env:NACOS_PASS } else { "nacos" }
$Group = "DEFAULT_GROUP"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Nacos Config Pusher" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Server: $NACOS_URL"
Write-Host "User:   $NACOS_USER"
Write-Host ""

# Step 1: Check Nacos is alive
Write-Host "Checking Nacos connection..." -NoNewline
try {
    $null = Invoke-RestMethod -Uri "$NACOS_URL/nacos/" -Method Head -TimeoutSec 5 -ErrorAction Stop
    Write-Host " OK" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq 404 -or $_.Exception.Response.StatusCode -eq 200 -or $_.Exception.Response.StatusCode -eq 302) {
        Write-Host " OK" -ForegroundColor Green
    } else {
        Write-Host " FAILED" -ForegroundColor Red
        Write-Host "Cannot reach $NACOS_URL - is Nacos running?" -ForegroundColor Red
        exit 1
    }
}

# Step 2: Login and get access token
Write-Host "Logging in..." -NoNewline
try {
    $loginResp = Invoke-RestMethod -Uri "$NACOS_URL/nacos/v1/auth/login" -Method Post -Body @{
        username = $NACOS_USER
        password = $NACOS_PASS
    } -ContentType "application/x-www-form-urlencoded" -TimeoutSec 10 -ErrorAction Stop
    $accessToken = $loginResp.accessToken
    if (-not $accessToken) {
        Write-Host " FAILED (no token)" -ForegroundColor Red
        Write-Host "Response: $($loginResp | ConvertTo-Json -Compress)" -ForegroundColor Yellow
        exit 1
    }
    Write-Host " OK" -ForegroundColor Green
} catch {
    Write-Host " FAILED" -ForegroundColor Red
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "Tip: If Nacos has no auth, set NACOS_USER/NACOS_PASS env vars or check Nacos config." -ForegroundColor Yellow
    exit 1
}

# Step 3: Upload configs
$configFiles = @(
    "common.yaml",
    "gateway.yaml",
    "auth-service.yaml",
    "system-service.yaml",
    "resource-service.yaml",
    "sms-service.yaml",
    "iot-service.yaml",
    "emqx-auth.yaml"
)

$successCount = 0
$failCount = 0

foreach ($fileName in $configFiles) {
    $filePath = Join-Path $ScriptDir $fileName
    if (-not (Test-Path $filePath)) {
        Write-Host "  SKIP  $fileName (file not found)" -ForegroundColor Yellow
        $failCount++
        continue
    }

    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
    $url = "$NACOS_URL/nacos/v1/cs/configs?accessToken=$accessToken"

    Write-Host "  PUSH  $fileName..." -NoNewline

    try {
        $response = Invoke-RestMethod -Uri $url -Method Post -Body @{
            dataId  = $fileName
            group   = $Group
            content = $content
            type    = "yaml"
        } -ContentType "application/x-www-form-urlencoded" -TimeoutSec 30 -ErrorAction Stop

        if ($response -eq "true") {
            Write-Host " OK" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host " WARN ($response)" -ForegroundColor Yellow
            $successCount++
        }
    } catch {
        Write-Host " FAILED" -ForegroundColor Red
        Write-Host "    $_" -ForegroundColor Red
        $failCount++
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Done: $successCount succeeded, $failCount failed" -ForegroundColor $(if ($failCount -eq 0) { "Green" } else { "Yellow" })
Write-Host "  Console: $NACOS_URL" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
