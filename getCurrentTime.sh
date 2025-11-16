#!/bin/bash
# 현재 시각을 조회하는 셸 스크립트
# Profile에 따라 다른 로직을 수행하고 로그를 저장

# UTF-8 인코딩 설정
export LANG=ko_KR.UTF-8

# 환경변수에서 Profile 확인
PROFILE=${CURRENT_PROFILE:-dev}

# Profile에 따른 분기 로직
if [ "$PROFILE" == "prod" ]; then
    echo "[PROD 환경] 배치 파일 실행"
    echo "Profile: PROD"
else
    echo "[DEV 환경] 배치 파일 실행"
    echo "Profile: DEV"
fi

# 현재 시각 출력
echo "현재 시각: $(date '+%Y-%m-%d %H:%M:%S')"

# 배치 실행 정보 수집
CURRENT_DATE=$(date '+%Y-%m-%d %H:%M:%S')
MESSAGE="배치 파일 실행 완료 - Profile: $PROFILE - 실행 시간: $CURRENT_DATE"

# 로그 파일 저장
echo ""
echo "=================================================="
echo "배치 파일에서 로그를 저장합니다."
echo "=================================================="

# prod 환경인 경우 IP 확인하여 로그 디렉토리 설정
if [ "$PROFILE" == "prod" ]; then
    echo "운영 환경(prod)에서는 IP 기반 분기를 수행합니다."
    
    # 서버 IP 확인 (Linux 환경)
    SERVER_IP=$(hostname -I | awk '{print $1}')
    if [ -z "$SERVER_IP" ]; then
        SERVER_IP="127.0.0.1"
    fi
    echo "서버 IP: $SERVER_IP"
    
    # IP 기반 분기
    if [[ "$SERVER_IP" == 192.168.1.* ]]; then
        echo "내부 네트워크로 감지됨"
        LOG_DIR="logs/batch-prod-internal"
        LOG_FILE="batch-prod-internal"
        PROFILE_TYPE="prod-internal"
    else
        echo "외부 네트워크로 감지됨"
        LOG_DIR="logs/batch-prod-external"
        LOG_FILE="batch-prod-external"
        PROFILE_TYPE="prod-external"
    fi
    
    # 로그 디렉토리 생성
    mkdir -p "$LOG_DIR"
    
    # 로그 파일 경로 생성 (타임스탬프 포함)
    TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
    LOG_PATH="$LOG_DIR/${LOG_FILE}_${TIMESTAMP}_${PROFILE_TYPE}.log"
    
    echo "분기된 Profile: $PROFILE_TYPE"
    
    # 로그 파일 작성 (UTF-8 BOM 포함)
    cat > "$LOG_PATH" << EOF
================================================
배치에서 생성한 로그
Profile: $PROFILE_TYPE
서버 IP: $SERVER_IP
저장 시간: $CURRENT_DATE
================================================

$MESSAGE
EOF
    
else
    # dev 환경 로그 디렉토리 설정
    LOG_DIR="logs/batch-dev"
    LOG_FILE="batch-dev"
    
    # 로그 디렉토리 생성
    mkdir -p "$LOG_DIR"
    
    # 로그 파일 경로 생성 (타임스탬프 포함)
    TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
    LOG_PATH="$LOG_DIR/${LOG_FILE}_${TIMESTAMP}_${PROFILE}.log"
    
    # 로그 파일 작성 (UTF-8 BOM 포함)
    cat > "$LOG_PATH" << EOF
================================================
배치에서 생성한 로그
Profile: $PROFILE
저장 시간: $CURRENT_DATE
================================================

$MESSAGE
EOF
fi

# UTF-8 인코딩 확인 및 변환
if command -v uconv &> /dev/null; then
    # uconv를 사용하여 UTF-8으로 변환
    uconv -f utf-8 -t utf-8 "$LOG_PATH" > "${LOG_PATH}.tmp" && mv "${LOG_PATH}.tmp" "$LOG_PATH"
fi

echo "[성공] 로그가 저장되었습니다: $LOG_PATH"
echo ""
echo "배치 파일 실행 완료 (Profile: $PROFILE)"
exit 0

