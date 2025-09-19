# UOS 영화 예매 서비스 백엔드

## 1. 프로젝트 개요

본 프로젝트는 2025년 1학기 서울시립대학교 '데이터베이스 설계' 강의의 팀 프로젝트로 진행된 영화 예매 서비스의 백엔드 API 서버입니다.

사용자는 영화 정보를 조회하고, 원하는 좌석을 선택하여 예매할 수 있으며, 리뷰를 작성하는 등의 활동을 할 수 있습니다. 백엔드는 Spring Boot를 기반으로 구축되었으며, 데이터베이스로 Oracle21C 버전을 사용합니다.
[데이터 베이스 설계 문서 바로가기]([https://drive.google.com/file/d/1z7xc4EzItsYpopO6_EwVMd-Hvc3nt1yU/view?usp=sharing](https://drive.google.com/file/d/1jcEwRmVn2VV1ybDrWKATp61RlE2-myfk/view?usp=sharing))

## 2. 주요 기능

- **회원 및 비회원 관리**
  - JWT(JSON Web Token)를 이용한 회원가입, 로그인, 인증/인가 처리
  - 비회원 예매 및 예매 확인 기능

- **영화 및 상영 정보**
  - 영화, 영화관, 상영관, 상영 스케줄 정보 관리 (CRUD)
  - 관리자 전용 데이터 등록 및 수정 기능

- **예매 시스템**
  - 좌석 등급 및 좌석 선택 기능
  - 예매 생성, 조회, 취소 기능
  - 스케줄러를 통해 특정 시간 동안 결제되지 않은 예매 자동 취소

- **결제 및 포인트**
  - 가상 은행 연동 API를 통한 결제 처리
  - 회원 대상 포인트 적립 및 사용 내역 관리

- **리뷰 관리**
  - 영화 관람 후 리뷰 작성, 조회, 수정, 삭제 기능

## 3. 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3
- **보안**: Spring Security, JWT
- **데이터베이스**: Oracle Database 21C
- **ORM**: Spring Data JPA
- **빌드 도구**: Gradle
- **API 문서화**: Swagger (Springdoc)

## 4. API 문서

서버 실행 후, 아래의 주소로 접속하면 Swagger UI를 통해 API 명세를 확인하고 직접 테스트해볼 수 있습니다.

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

**주요 API Endpoints:**
- `AdminController`: 관리자 기능
- `MemberController`: 회원 관련 기능
- `NonMemberController`: 비회원 관련 기능
- `MovieController`: 영화 정보
- `CinemaController`: 영화관 정보
- `ReservationController`: 예매 처리
- `ReviewController`: 리뷰 관리
- `BankApiController`: 결제 연동

## 5. 데이터베이스 스키마

JPA Entity를 기준으로 주요 테이블은 다음과 같습니다.

- `MEMBER`: 회원 정보
- `NON_MEMBER`: 비회원 정보
- `MOVIE`: 영화 정보
- `CINEMA`: 영화관 정보
- `SCREEN`: 상영관 정보
- `SCHEDULE`: 상영 스케줄
- `SEAT_GRADE`: 좌석 등급
- `SEAT`: 좌석 정보
- `RESERVATION`: 예매 내역
- `PAYMENT`: 결제 내역
- `POINT_HISTORY`: 포인트 사용 내역
- `REVIEW`: 리뷰 정보

## 6. 실행 방법

1. **데이터베이스 설정**
   - Oracle DB 서버를 준비합니다.
   - `src/main/resources/application.properties` (또는 `.yml`) 파일에 본인의 DB 연결 정보(URL, username, password)를 올바르게 입력합니다.

2. **프로젝트 빌드**
   - 프로젝트 루트 디렉토리에서 아래 명령어를 실행합니다.
   ```bash
   ./gradlew build
   ```

3. **서버 실행**
   - 빌드가 완료되면 아래 명령어를 통해 서버를 시작합니다.
   ```bash
   java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
   ```
   - 서버는 기본적으로 `8080` 포트에서 실행됩니다.

## 7. 프로젝트 구조

```
src
└── main
    ├── java/com/example/backend
    │   ├── config       # Security, Swagger 등 설정 파일
    │   ├── constants    # 비즈니스 로직 관련 상수
    │   ├── controller   # API 엔드포인트 컨트롤러
    │   ├── dto          # 데이터 전송 객체
    │   ├── entity       # JPA 엔티티 (DB 테이블)
    │   ├── repository   # Spring Data JPA 리포지토리
    │   ├── service      # 비즈니스 로직 서비스
    │   └── util         # JWT, ID 생성 등 유틸리티 클래스
    └── resources
        └── templates    # (서버 사이드 렌더링용) HTML 템플릿
```
