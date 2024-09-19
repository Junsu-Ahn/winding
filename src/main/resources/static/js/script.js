$(document).ready(function() {
  // 프로필을 클릭하면 서브메뉴를 토글(보이거나 숨김) 처리
  $('#userIcon').click(function(event) {
    event.preventDefault(); // 기본 동작 막기
    event.stopPropagation(); // 클릭 이벤트 전파 방지
    $(this).parent('.has_submenu').toggleClass('open');  // open 클래스 토글
  });

  // 페이지의 다른 곳을 클릭하면 서브메뉴 닫기
  $(document).click(function(event) {
    if (!$(event.target).closest('.has_submenu').length) {
      $('.has_submenu').removeClass('open');  // 서브메뉴 닫기
    }
  });
});
