# <img src="https://github.com/user-attachments/assets/415b30b9-749d-47bd-b20d-e00b28414cc2" width="150" height="100">

## 🚗 프로젝트 명 : Winding - 길찾기 및 여행 계획 서비스

## 📢 프로젝트 설명
* Winding은 사용자가 출발지, 목적지 및 경유지를 기반으로 최적의 경로를 제공하는 여행 및 길찾기 서비스입니다.
* 네이버 API를 활용하여 경로를 검색하고, 사용자 개인화된 경로와 여행 계획 기능을 제공합니다.
* 사람들이 자신의 드라이브 코스를 공유하고 다른 사용자들과 소통할 수 있는 플랫폼을 목표로 합니다.

## 🧑‍🤝‍🧑 팀원 구성
* 안준수


## ⚙ 개발 환경
* 운영체제 : Windows 10
* 통합개발환경(IDE) : IntelliJ IDEA
* JDK 버전 : Project SDK 17
* 데이터베이스 : MariaDB
* 빌드 툴 : Gradle
* 관리 툴 : GitHub

## ⏱️ 개발 기간
- 프로젝트 전체 : 2024-08-01 ~ 현재
- 기능 구현 : 2024-08-01 ~ 현재
- UI 구현 : 2024-08-21 ~ 현재

## 🔌 Dependencies
* Spring Boot DevTools
* Lombok
* Spring Data JPA
* MariaDB Driver
* Spring Security
* Spring Web
* Oauth2-client
* Thymeleaf
* Thymeleaf Layout Dialect
* Thymeleaf Extras Spring Security6
* Spring Boot Starter Validation
* Spring Boot Starter Mail

## 💻 기술 스택
* **백엔드**
    * Spring Boot
    * Spring Security
    * Spring Data JPA
    * RestTemplate
* **프론트엔드**
    * HTML, CSS, JavaScript
    * Bootstrap
    * Thymeleaf
* **데이터베이스**
    * MariaDB

## 🛠 DB 설계
- Member
- Post
- PostImage
- Comment
- Email

## 🕹 구현 기능
* 회원 로그인 및 소셜 로그인 (네이버, 구글)
* 게시물 등록 및 삭제 기능
* 경로 설정 기능 (출발지, 목적지, 경유지)
* 게시물 추천 및 랭킹 기능
* 사용자 프로필 관리 및 정보 수정 기능
* 관리자 페이지를 통한 사용자 및 게시물 관리 기능

## 🔗 ER - Diagram
<img src="https://github.com/user-attachments/assets/1616a95f-fd5f-4176-b4fe-6781be086cc2">

## 📱 디렉토리 구조
src
├── main
│   ├── java
│   │   └── com.example.demo
│   │       ├── controller
│   │       │   ├── MapController.java
│   │       │   ├── MemberController.java
│   │       │   └── PostController.java
│   │       ├── domain
│   │       │   ├── BaseEntity.java
│   │       │   ├── Member.java
│   │       │   ├── Post.java
│   │       │   ├── PostImage.java
│   │       │   └── Role.java
│   │       ├── dto
│   │       │   ├── Location.java
│   │       │   ├── RouteRequest.java
│   │       │   └── EditForm.java
│   │       ├── repository
│   │       │   ├── MemberRepository.java
│   │       │   └── PostRepository.java
│   │       ├── service
│   │       │   ├── EmailService.java
│   │       │   ├── MapService.java
│   │       │   ├── MemberService.java
│   │       │   └── PostService.java
│   │       └── security
│   │           ├── SecurityConfig.java
│   │           └── CustomUserDetailsService.java
│   ├── resources
│   │   ├── static
│   │   │   ├── css
│   │   │   │   ├── style.css
│   │   │   ├── js
│   │   │   │   ├── main.js
│   │   ├── templates
│   │   │   ├── post
│   │   │   │   ├── postList.html
├── postCreate.html
│   │   │   │   └── postDetail.html
│   │   │   ├── member
│   │   │   │   ├── login.html
│   │   │   │   ├── signup.html
│   │   │   │   └── profile.html
│   │   │   ├── layout
│   │   │   │   ├── header.html
│   │   │   │   └── footer.html
│   │   │   └── fragments
│   │   │       └── topbar.html
│   ├── application.yml
├── application-secret.yml
│   └── application-dev.yml
└── test
└── java
└── com.example.demo
├── MemberServiceTest.java
└── PostServiceTest.java

## 📌 페이지 기능 소개

### 메인화면
> <img src="https://github.com/user-attachments/assets/1011edd9-ef97-461a-b378-d20ee2b7b8f4" alt="메인 화면">
> 1. 사용자 맞춤형 경로를 보여주는 메인 화면으로, 상단에는 검색 바와 드라이브 코스 추천 기능이 있습니다.
> 2. 사용자별 추천 코스를 보여주는 기능과 인기 코스 및 최신 코스를 확인할 수 있습니다.

### 회원가입 및 로그인
> <img src="https://github.com/user-attachments/assets/f3f64aae-0fb6-49c3-98d1-52d4dbe831dd" alt="로그인 및 회원가입">
> <img src="https://github.com/user-attachments/assets/146ff344-c868-4d1e-bc2c-544d914c6bf7" alt="로그인 및 회원가입">
> 1. 이메일 또는 소셜 로그인(네이버, 구글)을 통해 회원가입 및 로그인을 지원합니다.
> 2. 로그인 후에는 사용자 개인화된 정보를 확인하고 코스를 등록할 수 있습니다.

### 게시물 작성 및 경로 설정
> <img src="https://github.com/user-attachments/assets/0f0ac8ec-afd8-4c66-ba2b-32a88ce5546f" alt="게시물 작성">
> <img src="https://github.com/user-attachments/assets/0dc16f13-5e15-4007-906b-db0fc8863fee" alt="게시물 작성">
> 1. 사용자들은 자신만의 드라이브 코스를 작성하여 다른 사람과 공유할 수 있습니다.
> 2. Daum 주소검색 api를 사용하여 출발지, 목적지, 경유지를 설정하고 경로를 저장할 수 있습니다.

### 게시물 상세 페이지
> <img src="https://github.com/user-attachments/assets/a3e6c785-c11a-4884-8d85-5e6d372d510c" alt="게시물 작성">
> <img src="https://github.com/user-attachments/assets/07dbed93-6740-4561-bb4c-0a42db220bda" alt="게시물 작성">
> 1. 게시글의 상세 정보를 확인 할 수 있습니다.
> 2. 네이버 api를 사용해 지도와 경로까지 시각화 하였습니다.

### 관리자 페이지
> <img src="https://github.com/user-attachments/assets/429ab6a2-2a69-45c9-800a-610cfb49b38f" alt="관리자 페이지">
> <img src="https://github.com/user-attachments/assets/63b5d4c8-0f9e-42f4-a88a-5fa15c7a58db" alt="관리자 페이지">
> 1. 관리자 권한을 가진 사용자는 다른 회원들의 활동을 관리하고, 게시물을 삭제할 수 있습니다.