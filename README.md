# LogHome Project

## 프로젝트 개요

LogHome Project는 Spring Boot 기반의 로깅 시스템으로, Profile별(dev/prod) 로그 관리와 배치 파일을 통한 로그 적재 기능을 제공합니다.

### 주요 기능

- **Profile 기반 로깅**: 개발(dev) 및 운영(prod) 환경에 따른 로그 분리
- **배치 파일 실행**: Windows 배치 파일 및 Linux 셸 스크립트 지원
- **IP 기반 분기**: 운영 환경에서 서버 IP에 따른 추가 로그 분기
  - 내부 네트워크 (192.168.1.x) → `logs/batch-prod-internal/`
  - 외부 네트워크 → `logs/batch-prod-external/`
- **UTF-8 인코딩**: 한글 로그 정상 표시
- **MVP 디자인 패턴**: Controller-Presenter-Model 구조

---

## 프로젝트 구조

```
logHome_project/
├── src/main/java/com/example/loghome_project/
│   ├── controller/           # View (REST API)
│   │   └── LogController.java
│   ├── model/                # Model (데이터 모델)
│   │   └── BatchResult.java
│   ├── presenter/            # Presenter (비즈니스 로직)
│   │   └── BatchPresenter.java
│   ├── service/              # Service
│   │   └── LogService.java
│   ├── LogWriter.java        # 커맨드라인 실행 클래스
│   └── LogHomeProjectApplication.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   └── application-prod.properties
├── getCurrentTime.bat        # Windows 배치 파일
├── getCurrentTime.sh         # Linux 셸 스크립트
└── logs/                     # 로그 파일 저장 디렉토리
    ├── batch-dev/
    ├── batch-prod-internal/
    └── batch-prod-external/
```

---

## 배치 실행 방법

### 방법 1: Windows 배치 파일 실행

#### 개발 환경
```cmd
REM 배치 파일 직접 실행
getCurrentTime.bat

REM 또는 명령어로
cmd /c getCurrentTime.bat
```

#### 운영 환경
```cmd
REM 환경변수 설정 후 실행
cmd /c "set CURRENT_PROFILE=prod&&call getCurrentTime.bat"
```

### 방법 2: Spring Boot API를 통한 실행

#### 애플리케이션 실행
```bash
# 개발 환경 (기본)
mvn spring-boot:run

# 운영 환경
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### API 엔드포인트

| 엔드포인트 | 설명 |
|----------|------|
| `GET /api/` | 홈 (Profile 표시) |
| `GET /api/profile` | 현재 Active Profile 확인 |
| `GET /api/execute-batch` | 배치 실행 (Profile별 로그 저장) |
| `GET /api/test` | 테스트 엔드포인트 |

#### 실행 예시
```bash
# 브라우저 접속
http://localhost:8080/api/execute-batch

# 또는 curl 사용
curl http://localhost:8080/api/execute-batch
```

### Linux 환경 (셸 스크립트)

```bash
# 실행 권한 부여
chmod +x getCurrentTime.sh

# 개발 환경
./getCurrentTime.sh

# 운영 환경
CURRENT_PROFILE=prod ./getCurrentTime.sh
```

---

## 로그 파일 구조

```
logs/
├── application.log              # 기본 애플리케이션 로그
├── application-dev.log         # 개발 환경 애플리케이션 로그
├── application-prod.log        # 운영 환경 애플리케이션 로그
├── batch-dev/                  # 개발 환경 배치 로그
│   └── batch-dev_YYYYMMDD_HHMMSS_dev.log
├── batch-prod-internal/        # 운영 - 내부 네트워크
│   └── batch-prod-internal_YYYYMMDD_HHMMSS_prod-internal.log
└── batch-prod-external/        # 운영 - 외부 네트워크
    └── batch-prod-external_YYYYMMDD_HHMMSS_prod-external.log
```

---

## 개발 및 Fix 이력

### 초기 설정 (로그 기반)

**날짜**: 2025-10-27  
**작업**: 기본 로그 설정  
**파일**: `application.properties`

```properties
# 로그 레벨 설정
logging.level.root=INFO
logging.level.com.example.loghome_project=DEBUG

# 로그 파일 출력 경로
logging.file.name=logs/application.log
```

**문제**: 기본적인 로깅 기능만 구현  
**해결**: Profile별 로그 파일 분리 필요

---

### Profile별 로그 설정 (로그 기반)

**날짜**: 2025-10-27  
**작업**: dev/prod 환경별 설정 파일 생성  
**파일**: `application-dev.properties`, `application-prod.properties`

```properties
# application-dev.properties
logging.level.root=DEBUG
logging.file.name=logs/application-dev.log

# application-prod.properties
logging.level.root=INFO
logging.file.name=logs/application-prod.log
```

**로그 예시**:
```
2025-10-27 12:00:00 INFO  LogController - 인포 레벨 로그 - Profile: dev
2025-10-27 12:00:00 DEBUG LogController - 디버그 레벨 로그
```

---

### MVP 패턴 적용 (로그 기반)

**날짜**: 2025-10-27  
**작업**: 패키지 구조 재구성 (Controller-Presenter-Model)  
**파일**: 
- `controller/LogController.java`
- `presenter/BatchPresenter.java`
- `model/BatchResult.java`

**로그 기반 확인**:
```
INFO  BatchPresenter - 배치 파일 실행 시작: getCurrentTime.bat
INFO  BatchPresenter - 현재 Active Profile: dev
INFO  BatchPresenter - [배치 실행 결과] [DEV 환경] 배치 파일 실행
```

---

### 배치 파일 실행 및 로그 저장 구현 (로그 기반)

**날짜**: 2025-10-27  
**작업**: 배치 파일 생성 및 실행  
**파일**: `getCurrentTime.bat`, `BatchPresenter.java`

**로그 예시**:
```
INFO  BatchPresenter - 배치 파일 실행 시작: getCurrentTime.bat (Profile: dev)
INFO  BatchPresenter - 배치 프로세스가 실행되었습니다. PID: 12345
INFO  BatchPresenter - [배치 실행 결과] [DEV 환경] 배치 파일 실행
INFO  BatchPresenter - 배치 파일이 성공적으로 실행되었습니다. 종료 코드: 0
INFO  BatchPresenter - 배치 실행 결과가 파일로 저장되었습니다: logs/batch-dev/batch-dev_20251027_120000_dev.log
```

---

### 한글 인코딩 문제 해결 (로그 기반)

**날짜**: 2025-10-27  
**문제**: 로그 파일에서 한글이 깨짐  
**로그 예시 (문제)**:
```
??곗튂?먯꽌 ?앹꽦??濡쒓렇
?쒕쾭 IP: 192.168.35.46
```

**작업**: UTF-8 인코딩 설정  
**파일**: `application.properties`, `getCurrentTime.bat`

**수정 내용**:
```properties
# UTF-8 인코딩 설정 추가
logging.charset.console=UTF-8
logging.charset.file=UTF-8
```

**로그 예시 (해결)**:
```
배치에서 생성한 로그
Profile: prod-external
서버 IP: 192.168.35.46
저장 시간: 2025-10-27 23:25:28
```

---

### IP 기반 분기 구현 (로그 기반)

**날짜**: 2025-10-27  
**작업**: prod 환경에서 서버 IP 확인하여 로그 디렉토리 분기  
**파일**: `LogWriter.java`, `getCurrentTime.bat`, `write_log_with_ip.ps1`

**로그 예시**:
```
INFO  LogWriter - 서버 IP: 192.168.35.46
INFO  LogWriter - 로그 디렉토리 생성: logs/batch-prod-external
INFO  LogWriter - 로그가 파일로 저장되었습니다: logs/batch-prod-external/batch-prod-external_20251027_232528.log
```

**IP 분기 로직**:
- `192.168.1.x` → `logs/batch-prod-internal/`
- 기타 IP → `logs/batch-prod-external/`

---

### 배치 파일에 IP 분기 추가 (로그 기반)

**날짜**: 2025-10-27  
**작업**: 배치 파일 내 IP 확인 로직 추가  
**파일**: `getCurrentTime.bat`

**실행 로그**:
```
[PROD 환경] 배치 파일 실행
Profile: PROD
현재 시각: 2025-10-27 23:28:43.90
운영 환경(prod)에서는 IP 기반 분기를 수행합니다.
서버 IP: 192.168.35.46
외부 네트워크로 감지됨
분기된 Profile: prod-external
[성공] 로그가 저장되었습니다: logs\batch-prod-external\batch-prod-external_2025-10-27_232853_prod-external.log
```

---

## 배치 엔드포인트 처리 순서

### GET /api/execute-batch 호출 시 처리 흐름

#### 1. LogController (View)
```
[1단계] 배치 실행 엔드포인트 요청 수신
[2단계] Active Profile 확인
[3단계] BatchPresenter.executeBatch() 메서드 호출
```

#### 2. BatchPresenter (Presenter)
```
[A단계] BatchPresenter.executeBatch() 메서드 진입
[B단계] Environment에서 Active Profile 조회
    └─ [B단계 완료] Profile 확인됨
[C단계] Profile 기반 로그 디렉토리 설정
    └─ [C단계 완료] 로그 디렉토리, 파일명 설정
[D단계] 로그 디렉토리 생성 시도
    └─ [D단계 완료] 로그 디렉토리 존재 확인
[E단계] 배치 파일 실행 준비
[F단계] ProcessBuilder 생성 및 환경변수 설정
    └─ [F단계 완료] CURRENT_PROFILE 환경변수 설정
[G단계] 배치 파일 프로세스 시작
    └─ [G단계 완료] 프로세스 PID 확인
[H단계] 배치 실행 결과 스트림 읽기 시작
    └─ [H단계] 결과 라인 읽음 (각 라인마다)
    └─ [H단계 완료] 스트림 읽기 완료
[I단계] 에러 스트림 읽기 시작
    └─ [I단계] 에러 라인 읽음 (각 라인마다)
    └─ [I단계 완료] 에러 스트림 읽기 완료
[J단계] 프로세스 종료 대기
    └─ [J단계 완료] 종료 코드 확인, 성공여부 판정
[K단계] 배치 실행 결과 파일 저장 시작
    └─ [K-1] saveBatchResultToFile() 메서드 진입
    └─ [K-2] 타임스탬프 생성
    └─ [K-3] 로그 파일 경로 생성
    └─ [K-4] 로그 내용 생성
    └─ [K-5] 파일 저장 시작
    └─ [K-6] 파일 저장 완료
    └─ [K단계 완료] 파일 저장 완료
[L단계] BatchResult 객체 생성 및 반환
```

#### 3. LogController (View - 응답)
```
[4단계] 배치 실행 결과 수신 (성공여부, 종료코드 확인)
[5단계] HTTP 응답 생성 및 반환
```

### Debug 로그 출력 예시

```
DEBUG LogController - [1단계] 배치 실행 엔드포인트 요청 수신
DEBUG LogController - [2단계] Active Profile 확인: dev
INFO  LogController - 배치 파일 실행 엔드포인트 호출됨 - Profile: dev
DEBUG LogController - [3단계] BatchPresenter.executeBatch() 메서드 호출

DEBUG BatchPresenter - [A단계] BatchPresenter.executeBatch() 메서드 진입
DEBUG BatchPresenter - [B단계] Environment에서 Active Profile 조회
INFO  BatchPresenter - 현재 Active Profile: dev
DEBUG BatchPresenter - [B단계 완료] Profile 확인됨: dev
DEBUG BatchPresenter - [C단계] Profile 기반 로그 디렉토리 설정
DEBUG BatchPresenter - [C단계 완료] 로그 디렉토리: logs/batch-dev, 파일명: batch-dev
DEBUG BatchPresenter - [D단계] 로그 디렉토리 생성 시도
DEBUG BatchPresenter - [D단계 완료] 로그 디렉토리 이미 존재: logs/batch-dev
DEBUG BatchPresenter - [E단계] 배치 파일 실행 준비: getCurrentTime.bat
DEBUG BatchPresenter - [F단계] ProcessBuilder 생성 및 환경변수 설정
DEBUG BatchPresenter - [F단계 완료] CURRENT_PROFILE 환경변수 설정: dev
DEBUG BatchPresenter - [G단계] 배치 파일 프로세스 시작
INFO  BatchPresenter - 배치 프로세스가 실행되었습니다. PID: 12345
DEBUG BatchPresenter - [G단계 완료] 프로세스 PID: 12345
DEBUG BatchPresenter - [H단계] 배치 실행 결과 스트림 읽기 시작
DEBUG BatchPresenter - [H단계] 결과 라인 읽음: [DEV 환경] 배치 파일 실행
DEBUG BatchPresenter - [H단계 완료] 스트림 읽기 완료
DEBUG BatchPresenter - [I단계] 에러 스트림 읽기 시작
DEBUG BatchPresenter - [I단계 완료] 에러 스트림 읽기 완료
DEBUG BatchPresenter - [J단계] 프로세스 종료 대기
DEBUG BatchPresenter - [J단계 완료] 종료 코드: 0, 성공여부: true
DEBUG BatchPresenter - [K단계] 배치 실행 결과 파일 저장 시작
DEBUG BatchPresenter - [K-1] saveBatchResultToFile() 메서드 진입
DEBUG BatchPresenter - [K-2] 타임스탬프 생성
DEBUG BatchPresenter - [K-3] 로그 파일 경로: logs/batch-dev/batch-dev_20251027_120000_dev.log
DEBUG BatchPresenter - [K-4] 로그 내용 생성
DEBUG BatchPresenter - [K-5] 파일 저장 시작
DEBUG BatchPresenter - [K-6] 파일 저장 완료
INFO  BatchPresenter - 배치 실행 결과가 파일로 저장되었습니다
DEBUG BatchPresenter - [K단계 완료] 파일 저장 완료
DEBUG BatchPresenter - [L단계] BatchResult 객체 생성 및 반환

DEBUG LogController - [4단계] 배치 실행 결과 수신 - 성공여부: true, 종료코드: 0
DEBUG LogController - [5단계] HTTP 응답 생성 및 반환
```

---

## 환경별 로그 설정

### 개발 환경 (dev)

```properties
# application-dev.properties
logging.level.root=DEBUG
logging.file.name=logs/application-dev.log
logging.charset.console=UTF-8
logging.charset.file=UTF-8
logging.file.max-size=10MB
logging.file.max-history=30
```

### 운영 환경 (prod)

```properties
# application-prod.properties
logging.level.root=INFO
logging.file.name=logs/application-prod.log
logging.charset.console=UTF-8
logging.charset.file=UTF-8
logging.file.max-size=50MB
logging.file.max-history=60
```

---

## 기술 스택

- **Java**: 17
- **Spring Boot**: 3.5.7
- **Maven**: 빌드 도구
- **Logback**: 로깅 프레임워크
- **Windows Batch**: `.bat` 파일
- **Linux Shell**: `.sh` 파일

---

## 빌드 및 실행

### Maven 빌드
```bash
mvn clean package
```

### 애플리케이션 실행
```bash
# JAR 파일 실행
java -jar target/logHome_project-0.0.1-SNAPSHOT.jar

# Profile 지정하여 실행
java -jar -Dspring.profiles.active=prod target/logHome_project-0.0.1-SNAPSHOT.jar
```

---

## 로그 파일 확인

### 현재 서버 IP 확인
```bash
# Windows
ipconfig | findstr /i "IPv4"

# Linux
hostname -I
```

### 생성된 로그 파일 확인
```bash
# Windows
dir logs\batch-dev
dir logs\batch-prod-external

# Linux
ls -lh logs/batch-dev/
ls -lh logs/batch-prod-external/
```

---

## 트러블슈팅

### 배치 파일 실행 시 Maven 오류
```bash
# Maven 설치 확인
mvn --version

# Maven이 없으면 API를 통한 실행 사용
http://localhost:8080/api/execute-batch
```

### 로그 파일이 생성되지 않는 경우
- `logs/` 디렉토리 권한 확인
- 디스크 공간 확인
- 애플리케이션 로그 확인: `logs/application.log`

### Profile이 인식되지 않는 경우
- `application.properties`의 `spring.profiles.active` 설정 확인
- IDE에서 Active Profile 설정 확인

---

## IP 분기 문제 해결 가이드

### IP에 따라 분기가 안되는 경우 확인해야 할 부분

#### 1. 서버 IP 확인
```bash
# Windows - PowerShell
Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.IPAddress -ne '127.0.0.1'}

# Windows - CMD
ipconfig | findstr /i "IPv4"

# Linux
hostname -I
ip addr show | grep "inet "
```

**문제**: IP가 127.0.0.1로 표시되는 경우
- 활성화된 네트워크 어댑터가 없음
- DHCP가 작동하지 않음

#### 2. 배치 파일 실행 환경 확인
```cmd
REM 배치 파일에서 환경변수 확인
echo CURRENT_PROFILE=%CURRENT_PROFILE%

REM IP 확인 테스트
powershell -Command "Get-NetIPAddress -AddressFamily IPv4"
```

**문제**: 환경변수가 설정되지 않음
- `CURRENT_PROFILE` 환경변수 미설정
- 배치 파일 실행 시 환경변수 전달 실패

#### 3. PowerShell 스크립트 권한 문제
```powershell
# PowerShell 실행 정책 확인
Get-ExecutionPolicy

# 실행 정책 변경 (관리자 권한 필요)
Set-ExecutionPolicy RemoteSigned
```

**문제**: PowerShell 스크립트 실행이 차단됨
- ExecutionPolicy가 Restricted로 설정됨

#### 4. 로그 파일 생성 위치 확인
```bash
# Windows
dir logs\batch-prod-internal
dir logs\batch-prod-external

# 로그 파일이 어느 디렉토리에 생성되었는지 확인
```

**예상 위치**:
- `192.168.1.x` → `logs/batch-prod-internal/`
- 기타 IP → `logs/batch-prod-external/`

#### 5. IP 분기 로직 점검

**getCurrentTime.bat (42번째 라인)**
```batch
REM 현재 설정
if "!SERVER_IP:~0,10!"=="192.168.1." (
    set PROFILE_TYPE=prod-internal
) else (
    set PROFILE_TYPE=prod-external
)
```

**확인 사항**:
- IP 범위 조건 (`192.168.1.`)이 올바른가?
- 서버 IP가 예상 범위와 일치하는가?
- 배치 파일에서 IP 확인이 정상적으로 되는가?

#### 6. NetworkInterface 확인 (Java 코드)
```java
// LogWriter.java - getServerIp() 메서드
// IP 주소 반환 로직 확인

// 디버깅을 위한 로그 추가
System.out.println("Detected IP: " + serverIp);
System.out.println("Profile Type: " + profile);
```

**확인 사항**:
- NetworkInterface에서 IP를 제대로 가져오는가?
- 예외가 발생하지 않는가?
- IP 주소가 UNKNOWN으로 표시되는가?

#### 7. 프로세스 빌더 환경변수 전달 확인
```java
// BatchPresenter.java
ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", BATCH_FILE_PATH);
processBuilder.environment().put("CURRENT_PROFILE", profile);

// 디버깅 로그 추가
logger.debug("Environment variables: {}", processBuilder.environment());
```

**확인 사항**:
- CURRENT_PROFILE 환경변수가 제대로 전달되는가?
- 배치 파일에서 환경변수를 읽을 수 있는가?

#### 8. 배치 파일 실행 결과 확인
```cmd
REM 배치 파일 직접 실행하여 IP 확인
set CURRENT_PROFILE=prod
getCurrentTime.bat

REM 출력 확인
- 서버 IP가 표시되는가?
- "내부 네트워크로 감지됨" 또는 "외부 네트워크로 감지됨" 메시지가 나오는가?
- "분기된 Profile" 메시지가 나오는가?
```

#### 9. 일반적인 문제 해결 방법

##### 문제 1: IP가 127.0.0.1로 표시됨
```
해결:
1. 네트워크 어댑터 활성화 확인
2. DHCP 서버 연결 확인
3. 수동으로 IP 설정
```

##### 문제 2: 내부 IP로 분기되지 않음
```
현재 서버 IP 확인:
- 예: 192.168.35.46 (외부 네트워크로 인식)

IP 범위 조건 변경:
if "!SERVER_IP:~0,11!"=="192.168.35." (

또는 여러 범위 지원:
if "!SERVER_IP:~0,10!"=="192.168.1." (
    set PROFILE_TYPE=prod-internal-dev
) else if "!SERVER_IP:~0,10!"=="172.16.0." (
    set PROFILE_TYPE=prod-internal-dmz
) else (
    set PROFILE_TYPE=prod-external
)
```

##### 문제 3: 프로세스가 실행되지 않음
```
해결:
1. cmd.exe 경로 확인
2. 배치 파일 권한 확인
3. 다른 프로세스가 배치 파일을 사용 중인지 확인
```

##### 문제 4: 로그 파일이 생성되지 않음
```
해결:
1. 로그 디렉토리 권한 확인
2. 디스크 공간 확인
3. 파일 경로에 특수문자 확인
```

#### 10. 디버깅 로그 활성화

**application.properties**에 로그 레벨 설정:
```properties
# 모든 DEBUG 로그 활성화
logging.level.com.example.loghome_project=DEBUG
logging.level.com.example.loghome_project.presenter.BatchPresenter=DEBUG
logging.level.com.example.loghome_project.controller.LogController=DEBUG
```

**로그 파일 확인**:
```bash
tail -f logs/application-dev.log
```

#### 11. 테스트 시나리오

**시나리오 1: 내부 네트워크 테스트**
```cmd
REM 가상 IP 설정 (테스트용)
set SERVER_IP=192.168.1.100
getCurrentTime.bat

예상 결과: logs\batch-prod-internal\ 에 파일 생성
```

**시나리오 2: 외부 네트워크 테스트**
```cmd
REM 외부 IP로 테스트
set SERVER_IP=203.0.113.100
getCurrentTime.bat

예상 결과: logs\batch-prod-external\ 에 파일 생성
```

#### 12. IP 분기 로직 수정 방법

**getCurrentTime.bat 수정 (42-52번째 라인)**:
```batch
REM 현재 설정: 192.168.1.x만 내부로 분기
if "!SERVER_IP:~0,10!"=="192.168.1." (
    set PROFILE_TYPE=prod-internal
) else (
    set PROFILE_TYPE=prod-external
)

REM 수정 예시 1: 여러 내부 IP 범위 지원
if "!SERVER_IP:~0,10!"=="192.168.1." (
    set PROFILE_TYPE=prod-internal-dev
) else if "!SERVER_IP:~0,11!"=="10.0.0.1" (
    set PROFILE_TYPE=prod-internal-dmz
) else if "!SERVER_IP:~0,7!"=="172.16." (
    set PROFILE_TYPE=prod-internal-dmz
) else (
    set PROFILE_TYPE=prod-external
)

REM 수정 예시 2: 특정 IP만 외부로 분기
if "!SERVER_IP:~0,7!"=="203.0." (
    set PROFILE_TYPE=prod-external-cdn
) else (
    set PROFILE_TYPE=prod-internal
)
```

---

## 라이선스

이 프로젝트는 교육 및 학습 목적으로 사용됩니다.

