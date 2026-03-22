# Update Nacos configs
$nacosAddr = "http://172.26.76.190:8848"

function Update-NacosConfig {
    param($dataId, $filePath)
    
    $content = Get-Content $filePath -Raw
    $escapedContent = [System.Uri]::EscapeDataString($content)
    $body = "dataId=$dataId&group=DEFAULT_GROUP&content=$escapedContent"
    
    try {
        $response = Invoke-RestMethod -Uri "$nacosAddr/nacos/v1/cs/configs" -Method Post -ContentType "application/x-www-form-urlencoded" -Body $body
        Write-Host "Updated $dataId : $response"
    } catch {
        Write-Host "Error updating $dataId : $_"
    }
}

Update-NacosConfig -dataId "common.yaml" -filePath "D:\ai_work\cloud-platform\nacos-config\common.yaml"
Update-NacosConfig -dataId "gateway.yaml" -filePath "D:\ai_work\cloud-platform\nacos-config\gateway.yaml"
Update-NacosConfig -dataId "auth-service.yaml" -filePath "D:\ai_work\cloud-platform\nacos-config\auth-service.yaml"
Update-NacosConfig -dataId "system-service.yaml" -filePath "D:\ai_work\cloud-platform\nacos-config\system-service.yaml"

Write-Host "Done updating Nacos configs"
