# <img src="https://github.com/user-attachments/assets/415b30b9-749d-47bd-b20d-e00b28414cc2">
## 🚀프로젝트 명 : 다함께 와인딩! (Winding!)

## 📢 프로젝트 설명
* 드라이브를 좋아하는 사람들의 드라이브 코스 공유 블로그
* 사람들이 몰리는 뻔한 드라이브 코스 뿐 아니라 스포츠 드라이빙에 적합한 코스도 공유 가능

## ⚙ 개발 환경
* 운영체제 : Windows 10 -통합개발환경(IDE) : IntelliJ
* JDK 버전 : Project SDK 17
*  데이터 베이스 : MariaDB
*  빌드 툴 : Gradle
*  관리 툴 : GitHub

## ⏱️개발 기간
- 전체 개발 기간 : 2024-08-27 ~ 2024-09-29
- UI 구현 : 2024-08-27 ~ 2024-09-27
- 기능 구현 : 2024-08-27 ~ 2024-09-29

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
* 백엔드
  : Spring Boot
  Spring Security
  Spring Data JPA

* 프론트엔드
  : HTML
  CSS
  JavaScript
  Bootstrap
  Thymeleaf
  TailWind

* 데이터베이스
  : MariaDB
  MySQL Workbench

## 🛠 DB 설계
* Member
* Post
* Product

## 🕹 구현 기능
* Naver Direction API를 사용한 지도, 경로 탐색
* 회원 로그인/회원가입
* 소셜로그인
* Daum 주소검색 API를 사용한 정확한 주소검색
* 코스, 판매상품 등록
* 관리자 기능

## 🔗 ER - Diagram
# <img src="https://github.com/user-attachments/assets/30fdbf38-ea32-47d0-aa21-3130b0107ef3">

## 📌 페이지 기능 소개
### 메인화면
# <img src="https://github.com/user-attachments/assets/d17174d7-625d-4866-adfe-c4fdba70da28">
# <img src="https://github.com/user-attachments/assets/ec29eb43-e4b0-401b-803e-9e7e65496e49">

* 로그인하지 않은 상태로 게시글 상세페이지 접근 시 로그인 페이지로 이동.

### 로그인 / 회원가입
# <img src="https://github.com/user-attachments/assets/6659a870-18be-42c0-97ea-d177370fa41b">

# <img src="https://github.com/user-attachments/assets/6f89f0ea-59b2-45fc-bce2-0c8032d6e7d7">

* 로그인 시 일반계정, 관리자 계정, 소셜 계정으로 로그인 가능.
* 회원가입 시 기본 정보와 주소 입력이 필수.
* 비밀번호와 비밀번호 확인 값이 다르면 에러메세지 출력.

# <img src="https://github.com/user-attachments/assets/f349d8a1-931f-4c63-b6ae-0a505d211203">

* 로그인 성공 시 프로필 사진 출력.
* 후에 회원정보 수정 페이지에서 수정 가능.

### 게시글 등록

