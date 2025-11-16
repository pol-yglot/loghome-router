param(
    [string]$message,
    [string]$profile
)

# 서버 IP 주소 확인
try {
    $ip = (Get-NetIPAddress -AddressFamily IPv4 | 
           Where-Object { $_.IPAddress -ne '127.0.0.1' -and $_.PrefixOrigin -eq 'Dhcp' } | 
           Select-Object -First 1).IPAddress
} catch {
    try {
        $ip = (Get-NetIPAddress -AddressFamily IPv4 | 
               Where-Object { $_.IPAddress -ne '127.0.0.1' } | 
               Select-Object -First 1).IPAddress
    } catch {
        $ip = "127.0.0.1"
    }
}

if (!$ip) {
    $ip = "127.0.0.1"
}

Write-Host "서버 IP: $ip"

# IP 기반 분기
if ($ip.StartsWith("192.168.1.")) {
    $profileType = "prod-internal"
    $logDir = "logs\batch-prod-internal"
} else {
    $profileType = "prod-external"
    $logDir = "logs\batch-prod-external"
}

Write-Host "분기된 Profile: $profileType"

# 로그 디렉토리 생성
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

# 로그 파일 경로 생성
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logPath = Join-Path $logDir "batch-$profileType`_$timestamp.log"

# 로그 내용 작성
$content = "================================================`n" +
           "배치에서 생성한 로그`n" +
           "Profile: $profileType`n" +
           "서버 IP: $ip`n" +
           "저장 시간: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')`n" +
           "================================================`n`n" +
           "$message"

# UTF-8 BOM으로 파일 저장
[System.IO.File]::WriteAllText($logPath, $content, [System.Text.Encoding]::UTF8)

Write-Host "[성공] 로그가 저장되었습니다: $logPath"

