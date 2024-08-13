$(document).ready(function () {
    // 페이지네이션 링크 클릭 시 페이지 변경
    $(".page-link").on("click", function () {
        $("#page").val($(this).data("page"));
        $("#searchForm").submit();
    });

    // 검색 버튼 클릭 시 검색 폼 제출
    $("#btn_search").on("click", function () {
        $("#kw").val($("#search_kw").val());
        $("#page").val(0); // 검색 시 첫 페이지로 이동
        $("#searchForm").submit();
    });

    // 사용자 메뉴 토글
    $('#userIcon').click(function(event) {
        event.preventDefault();
        event.stopPropagation();
        $('.user_two_menu').toggleClass('visible');
        $('.arrow-up').toggleClass('visible');
    });

    // 사용자 메뉴 외부 클릭 시 닫기
    $(document).click(function(event) {
        if (!$(event.target).closest('#userIcon, .user_two_menu').length) {
            $('.user_two_menu').removeClass('visible');
            $('.arrow-up').removeClass('visible');
        }
    });

    $('.user_two_menu').click(function(event) {
        event.stopPropagation();
    });
});
