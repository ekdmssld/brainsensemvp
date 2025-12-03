# E-Commerce Demo 프로젝트

## 프로젝트 개요
Spring Boot 기반의 전자상거래(E-Commerce) 웹 애플리케이션입니다. 상품 판매, 주문 관리, 고객 서비스 등 온라인 쇼핑몰의 핵심 기능을 제공합니다.

## 기술 스택

### Backend
- **Framework**: Spring Boot 3.3.5
- **Java Version**: 17
- **Build Tool**: Gradle
- **Database**: H2 Database (In-Memory)
- **ORM**: Spring Data JPA
- **Security**: Spring Security
- **Template Engine**: Mustache

### 주요 의존성
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter OAuth2 Client
- Spring Boot Starter Mustache
- Lombok
- H2 Database

## 주요 기능

### 1. 사용자 기능
#### 회원 관리
- 회원가입 및 로그인
- 사용자 프로필 관리
- 배송지 정보 관리
- Spring Security 기반 인증/인가

#### 상품 기능
- 상품 목록 조회 (페이징 지원)
- 상품 상세 정보 보기
- 카테고리별 상품 조회
- 상품 검색 기능
- 상품 이미지 지원

#### 장바구니
- 장바구니에 상품 추가/삭제
- 수량 변경
- 재고 확인
- 총 금액 계산

#### 위시리스트
- 관심 상품 저장
- 위시리스트에서 장바구니로 이동
- 중복 추가 방지

#### 주문/결제
- 주문서 작성
- 배송지 정보 입력
- 주문 내역 조회
- 주문 상태 추적 (대기/확인/준비중/배송중/완료/취소/환불)
- 주문 취소 및 환불 (재고 자동 복구)

#### 리뷰 시스템
- 상품 리뷰 작성 (1-5점 평점)
- 리뷰 수정/삭제
- 리뷰 댓글 기능
- 평균 평점 계산
- 구매자만 리뷰 작성 가능

#### 고객 문의
- 문의사항 등록
- 문의 유형 분류 (상품/주문/배송/교환/환불/불만/기타)
- 문의 상태 관리 (답변대기/답변완료/처리완료)
- 문의 댓글 기능

### 2. 관리자 기능
#### 대시보드
- 전체 상품 수 통계
- 전체 주문 수 통계
- 회원 수 통계
- 최근 주문 내역
- 미처리 문의 현황

#### 상품 관리
- 상품 등록/수정/삭제
- 재고 관리
- 상품 이미지 업로드
- 카테고리 관리
- 판매 가능 여부 설정

#### 주문 관리
- 전체 주문 조회
- 주문 상세 정보
- 주문 상태 변경
- 송장번호 입력

#### 회원 관리
- 회원 목록 조회
- 회원 검색
- 회원 정보 확인

#### 문의 관리
- 문의 목록 조회
- 문의 답변 달기
- 문의 상태 변경
- 관리자 댓글 작성

## 데이터베이스 구조

### 주요 엔티티
1. **User**: 사용자 정보 (일반 사용자/관리자)
2. **Product**: 상품 정보
3. **Category**: 상품 카테고리 (계층 구조 지원)
4. **Cart**: 장바구니
5. **Wishlist**: 위시리스트
6. **Order**: 주문 정보
7. **OrderItem**: 주문 상품 상세
8. **Review**: 상품 리뷰
9. **ReviewComment**: 리뷰 댓글
10. **Inquiry**: 고객 문의
11. **InquiryComment**: 문의 댓글
12. **Payment**: 결제 정보
13. **Delivery**: 배송 정보

## 프로젝트 구조

```
src/
├── main/
│   ├── java/com/example/demo/
│   │   ├── config/              # 설정 클래스
│   │   │   ├── SecurityConfig.java
│   │   │   ├── WebConfig.java
│   │   │   └── MustacheConfig.java
│   │   ├── controller/          # 컨트롤러
│   │   │   ├── MainController.java
│   │   │   ├── ProductController.java
│   │   │   ├── CartController.java
│   │   │   ├── OrderController.java
│   │   │   ├── WishlistController.java
│   │   │   ├── ReviewController.java
│   │   │   ├── InquiryController.java
│   │   │   ├── MyPageController.java
│   │   │   ├── UserController.java
│   │   │   ├── AdminController.java
│   │   │   ├── AdminProductController.java
│   │   │   └── AdminInquiryController.java
│   │   ├── dto/                 # 데이터 전송 객체
│   │   ├── entity/              # JPA 엔티티
│   │   ├── repository/          # JPA 리포지토리
│   │   ├── service/             # 비즈니스 로직
│   │   └── DemoApplication.java
│   └── resources/
│       ├── application.properties
│       ├── data.sql             # 초기 데이터
│       └── templates/           # Mustache 템플릿
│           ├── admin/
│           ├── cart/
│           ├── inquiry/
│           ├── main/
│           ├── mypage/
│           ├── order/
│           ├── products/
│           └── user/
└── test/
```

## 실행 방법

### 1. 사전 요구사항
- JDK 17 이상
- Gradle

### 2. 프로젝트 클론 및 빌드
```bash
# 프로젝트 클론
git clone [repository-url]
cd demo

# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

### 3. 접속
- 애플리케이션: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (공백)

### 4. 테스트 계정
애플리케이션 실행 시 `src/main/resources/data.sql` 파일을 통해 자동으로 테스트 계정이 생성됩니다.

#### 관리자 계정
- **Username**: `admin`
- **Password**: `admin1234`
- **Email**: admin@arduino.com
- **권한**: 관리자 (ADMIN)

#### 일반 사용자 계정
- **Username**: `test`
- **Password**: `test1234`
- **Email**: test@test.com
- **권한**: 일반 사용자 (USER)

> 초기 데이터 및 추가 테스트 계정은 `src/main/resources/data.sql` 파일을 참고하세요.

## 보안 설정

### 인증/인가
- Spring Security 기반 폼 로그인
- BCrypt 패스워드 암호화
- 역할 기반 접근 제어 (USER, ADMIN)

### 접근 권한
- **Public**: 메인 페이지, 상품 목록/상세, 로그인/회원가입
- **USER**: 장바구니, 주문, 마이페이지, 위시리스트
- **ADMIN**: 관리자 대시보드, 상품/주문/회원/문의 관리

### CSRF 설정
- H2 Console, 장바구니, 주문, 관리자 페이지 등 CSRF 비활성화
- 일반 페이지는 CSRF 보호 활성화

## 주요 특징

### 재고 관리
- 주문 시 자동 재고 차감
- 주문 취소/환불 시 자동 재고 복구
- 재고 부족 시 주문 불가

### 파일 업로드
- 상품 이미지 업로드 지원
- uploads/products/ 디렉토리에 저장
- UUID 기반 파일명 생성

### 페이징 처리
- 상품 목록, 회원 목록, 문의 목록 등 페이징 지원
- 기본 12개(상품), 20개(회원) 단위 페이징

### 검색 기능
- 상품명 기반 검색
- 카테고리 필터링
- 회원 검색 (이름, 이메일)

## 데이터베이스 설정

### H2 In-Memory Database
- 개발 환경용 인메모리 데이터베이스
- 애플리케이션 재시작 시 데이터 초기화
- data.sql 파일을 통한 초기 데이터 로드

### JPA 설정
- DDL 자동 생성: create-drop
- SQL 로깅 활성화
- 지연 로딩 사용

## 개발 도구
- Spring Boot DevTools: 자동 재시작 지원
- Lombok: 보일러플레이트 코드 감소

## 라이선스
이 프로젝트는 교육/데모 목적으로 작성되었습니다.
