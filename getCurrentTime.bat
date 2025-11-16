@echo off
setlocal enabledelayedexpansion
REM 현재 시각을 조회하는 배치 파일
REM Profile에 따라 다른 로직을 수행하고 로그를 저장

REM UTF-8 인코딩 설정
chcp 65001 >nul

REM 환경변수에서 Profile 확인
set PROFILE=%CURRENT_PROFILE%
if "%PROFILE%"=="" set PROFILE=dev

REM Profile에 따른 분기 로직
if "%PROFILE%"=="prod" (
    echo [PROD 환경] 배치 파일 실행
    echo Profile: PROD
) else (
    echo [DEV 환경] 배치 파일 실행
    echo Profile: DEV
)

REM 현재 시각 출력
echo 현재 시각: %date% %time%

REM 배치 실행 정보 수집
set MESSAGE=배치 파일 실행 완료 - Profile: %PROFILE% - 실행 시간: %date% %time%

REM 로그 파일 저장
echo.
echo ================================================
echo 배치 파일에서 로그를 저장합니다.
echo ================================================

REM prod 환경인 경우 IP 확인하여 로그 디렉토리 설정
if "%PROFILE%"=="prod" (
    echo 운영 환경^(prod^)에서는 IP 기반 분기를 수행합니다.
    REM PowerShell을 통해 IP 확인
    for /f "delims=" %%I in ('powershell -NoProfile -Command "$ip = (Get-NetIPAddress -AddressFamily IPv4 ^| Where-Object { $_.IPAddress -ne '127.0.0.1' } ^| Select-Object -First 1).IPAddress; if (!$ip^) { $ip = '127.0.0.1' }; Write-Output $ip"') do set SERVER_IP=%%I
    echo 서버 IP: !SERVER_IP!
    
    REM IP 기반 분기
    if "!SERVER_IP:~0,10!"=="192.168.1." (
        echo 내부 네트워크로 감지됨
        set LOG_DIR=logs\batch-prod-internal
        set LOG_FILE=batch-prod-internal
        set PROFILE_TYPE=prod-internal
    ) else (
        echo 외부 네트워크로 감지됨
        set LOG_DIR=logs\batch-prod-external
        set LOG_FILE=batch-prod-external
        set PROFILE_TYPE=prod-external
    )
    
    REM 로그 디렉토리 생성
    if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
    
    REM 로그 파일 경로 생성 (타임스탬프 포함)
    for /f "tokens=1-3 delims=/ " %%a in ("%date%") do (set YEAR=%%c&set MONTH=%%a&set DAY=%%b)
    for /f "tokens=1-3 delims=: " %%a in ("%time%") do (set HOUR=%%a&set MIN=%%b&set SEC=%%c)
    REM 공백 제거
    set HOUR=!HOUR: =0!
    set SEC=!SEC: =0!
    set LOG_PATH=!LOG_DIR!\!LOG_FILE!_!YEAR!!MONTH!!DAY!_!HOUR!!MIN!!SEC!_!PROFILE_TYPE!.log
    
    echo 분기된 Profile: !PROFILE_TYPE!
    goto :write_log
)

REM dev 환경 로그 디렉토리 설정
set LOG_DIR=logs\batch-dev
set LOG_FILE=batch-dev
REM 로그 디렉토리 생성
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

REM 로그 파일 경로 생성 (타임스탬프 포함)
set YEAR=%date:~0,4%
set MONTH=%date:~5,2%
set DAY=%date:~8,2%
set HOUR=%time:~0,2%
set MIN=%time:~3,2%
set SEC=%time:~6,2%
REM 공백 제거
set HOUR=!HOUR: =0!
set LOG_PATH=%LOG_DIR%\%LOG_FILE%_%YEAR%%MONTH%%DAY%_%HOUR%%MIN%%SEC%_%PROFILE%.log

:write_log
REM UTF-8 BOM을 추가하여 로그 파일 작성
REM Profile이 prod인 경우 IP 정보 포함
if "%PROFILE%"=="prod" goto :prod_log
goto :dev_log

:prod_log
powershell -NoProfile -Command "$profileType = '%PROFILE_TYPE%'; $serverIp = '%SERVER_IP%'; $content = '================================================' + [Environment]::NewLine + '배치에서 생성한 로그' + [Environment]::NewLine + 'Profile: ' + $profileType + [Environment]::NewLine + '서버 IP: ' + $serverIp + [Environment]::NewLine + '저장 시간: %date% %time%' + [Environment]::NewLine + '================================================' + [Environment]::NewLine + '' + [Environment]::NewLine + '%MESSAGE%'; [System.IO.File]::WriteAllText('%LOG_PATH%', $content, [System.Text.Encoding]::UTF8)"
goto :end_log

:dev_log
REM dev 환경은 PowerShell로 직접 로그 작성
powershell -NoProfile -Command "$content = '================================================' + [Environment]::NewLine + '배치에서 생성한 로그' + [Environment]::NewLine + 'Profile: %PROFILE%' + [Environment]::NewLine + '저장 시간: %date% %time%' + [Environment]::NewLine + '================================================' + [Environment]::NewLine + '' + [Environment]::NewLine + '%MESSAGE%'; [System.IO.File]::WriteAllText('%LOG_PATH%', $content, [System.Text.Encoding]::UTF8)"

:end_log

echo [성공] 로그가 저장되었습니다: %LOG_PATH%

echo.
echo 배치 파일 실행 완료 (Profile: %PROFILE%)
exit /b 0
