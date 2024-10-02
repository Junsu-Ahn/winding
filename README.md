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
# <img src="https://github.com/user-attachments/assets/84117022-5e1d-452d-8f44-eec4f293400b">

## 📌 페이지 기능 소개
### 메인화면
# <img src="https://github.com/user-attachments/assets/d17174d7-625d-4866-adfe-c4fdba70da28">
# <img src="https://github.com/user-attachments/assets/ec29eb43-e4b0-401b-803e-9e7e65496e49">

* 로그인하지 않은 상태로 게시글 상세페이지 접근 시 로그인 페이지로 이동.
# <img src="https://github.com/user-attachments/assets/37cf5d5d-1dac-42b3-b5dc-9f34d621758a">
# <img src="https://github.com/user-attachments/assets/0697eebc-de09-441e-80cf-cdbe96b937d5">

* 제목 검색 혹은 지역별로 보기 가능.

# <img src="https://github.com/user-attachments/assets/32426fd0-acbe-4d5c-a3ca-4476654f4848">
* 지역명에 익숙하지 않은 사람들을 위한 지도로 보는 페이지.

### 로그인 / 회원가입
# <img src="https://github.com/user-attachments/assets/6659a870-18be-42c0-97ea-d177370fa41b">
# <img src="https://github.com/user-attachments/assets/6f89f0ea-59b2-45fc-bce2-0c8032d6e7d7">

* 로그인 시 일반계정, 관리자 계정, 소셜 계정으로 로그인 가능.
* 회원가입 시 기본 정보와 주소 입력이 필수.
* 비밀번호와 비밀번호 확인 값이 다르면 에러메세지 출력.

# <img src="https://github.com/user-attachments/assets/f349d8a1-931f-4c63-b6ae-0a505d211203">

* 로그인 성공 시 프로필 사진 출력.
* 후에 회원정보 수정 페이지에서 수정 가능.

### 아이디/비밀번호 찾기
# <img src="https://github.com/user-attachments/assets/6545c8e9-3817-42f8-851d-1e46ffa783d2">
* 회원가입시 입력한 이메일 입력 
* 이메일 발송
# <img src="https://github.com/user-attachments/assets/441f39b5-3438-4b75-8ced-311dd3e0724c">
* 소셜로그인 아이디 포함 가입한 아이디 전송.

# <img src="https://github.com/user-attachments/assets/26761e25-9a23-4b46-8f59-e62e53aa5d64">
* 아이디 입력 -> 해당 아이디의 이메일로 임시 비밀번호 전송.
# <img src="https://github.com/user-attachments/assets/9f8ce0eb-0179-486d-9d83-1b646728bda3">
* 임시 비밀번호로 변경되었으므로 로그인 후 비밀번호 변경 권장.

### 게시글 등록

# <img src="https://github.com/user-attachments/assets/5b647fc6-a018-456f-9c8c-300cfbb584e9">
# <img src="https://github.com/user-attachments/assets/b517a262-dd60-4773-bbb2-8773765d8018">

* Daum주소검색 API를 사용하여 Map API에 사용하기 적합한 정확한 주소를 입력받음.

# <img src="https://github.com/user-attachments/assets/c791d084-a198-4031-ab0b-a83ebb9d3e53">

* 출발지와 목적지 혹은 경유지 입력시 지도에 경로와 마커 생성.
* 썸네일 미 등록시 지도를 캡처하여 자동으로 썸네일로 저장.

### 게시글 상세
# <img src="https://github.com/user-attachments/assets/a3abc091-df05-4f80-bf1a-529383aa084e">
# <img src="https://github.com/user-attachments/assets/a669d716-fa0d-45af-a7c6-1ac074e51679">
* 썸네일과 등록된 추가 사진 및 설명, 지도 확인 가능

### 마켓
# <img src="https://github.com/user-attachments/assets/e705b38f-f915-4898-85b8-ed68a61bf57a">
# <img src="https://github.com/user-attachments/assets/f2869ff8-6d9b-4a6f-96f4-336466cf6583">

* 검색 or 카테고리 클릭 시 카테고리 별로 물건 확인 가능
# <img src="https://github.com/user-attachments/assets/8671cc8b-9ea1-40cc-8bb7-418c446c2fd1">
# <img src="https://github.com/user-attachments/assets/65e9541e-044b-495f-978d-b3ba1c99bb5f">

### 물건 상세
# <img src="https://github.com/user-attachments/assets/8015ed34-0707-40a2-a278-6b28adb7dda5">
# <img src="https://github.com/user-attachments/assets/3c2d9821-ec78-4d89-87ad-ecd29bb17e5b">

* 찜하기, 장바구니 기능 사용가능.
* 수량 조절시 가격도 자동으로 조정 됨.

### 마이페이지
# <img src="https://github.com/user-attachments/assets/79bfac14-4b14-4eb7-bc3d-1719e04112dc">
# <img src="https://github.com/user-attachments/assets/a19b16ad-17e3-4a54-82df-04691bf954a1">
* 찜목록, 장바구니, 회원정보 수정 가능.

### 회원정보 수정
# <img src="https://github.com/user-attachments/assets/a1d64775-8988-4fd2-9d70-5a5e95e6566a">
* 기본 정보 수정 가능.

### 관리자 페이지
# <img src="https://github.com/user-attachments/assets/6aec8d7e-1aff-4296-ab1a-36e6f9a36ec3">
# <img src="https://github.com/user-attachments/assets/be42f60a-1801-45ca-8e7a-8545ef85895c">

* 회원 혹은 게시물을 검색/선택하여 삭제 가능하다.

# <img src="https://github.com/user-attachments/assets/8517f990-3c20-40bd-b90f-4be321891865">
* 관리자만이 추천상품을 등록할 수 있다.

## 프로젝트 소감
혼자 모든 것을 구현하려다 보니 시간이 너무 오래 걸린 것 같다. 평소 백엔드가 어렵고 프론트엔드는 쉽다고 생각했었으나 이번 프로젝트에서 프론트가 더 어렵게 느껴졌다.
다음 프로젝트는 js를 사용한 게임사이트를 만들면서 프론트와 좀 더 친해져야겠다. 생각으로만 구현한 기능들이 많은데 여유가 된다면 실제 구매기능 등 다양한 기능들을 더 구현하고 싶다.
