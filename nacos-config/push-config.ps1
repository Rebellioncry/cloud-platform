$NACOS_URL = "http://172.26.76.190:8848"
$ScriptDir = "D:\ai_work\cloud-platform\nacos-config\"

function Upload-Config {
    param($fileName)
    $filePath = $ScriptDir + $fileName
    if (-not (Test-Path $filePath)) {
        Write-Host "File not found: $filePath"
        return
    }
    
    # Read file content preserving encoding
    $content = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
    $url = "$NACOS_URL/nacos/v1/cs/configs"
    
    Write-Host "Uploading $fileName..."
    
    # Use Invoke-RestMethod for better handling
    try {
        $response = Invoke-RestMethod -Uri $url -Method Post -Body @{
            dataId = $fileName
            group = "DEFAULT_GROUP"
            content = $content
        } -ContentType "application/x-www-form-urlencoded" -TimeoutSec 30
        
        if ($response -eq "true") {
            Write-Host "  OK" -ForegroundColor Green
        } else {
            Write-Host "  Result: $response" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  Error: $_" -ForegroundColor Red
    }
}

Upload-Config "common.yaml"
Upload-Config "gateway.yaml"
Upload-Config "auth-service.yaml"
Upload-Config "system-service.yaml"
Upload-Config "resource-service.yaml"

Write-Host ""
Write-Host "Done!" -ForegroundColor Green
