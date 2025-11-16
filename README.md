# logHome-router

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

## 기술 스택

- **Java**: 17
- **Spring Boot**: 3.5.7
- **Maven**: 빌드 도구
- **Logback**: 로깅 프레임워크
- **Windows Batch**: `.bat` 파일
- **Linux Shell**: `.sh` 파일

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

#### API 엔드포인트

| 엔드포인트 | 설명 |
|----------|------|
| `GET /api/` | 홈 (Profile 표시) |
| `GET /api/profile` | 현재 Active Profile 확인 |
| `GET /api/execute-batch` | 배치 실행 (Profile별 로그 저장) |
| `GET /api/test` | 테스트 엔드포인트 |

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
