// layout.js

$(document).ready(function() {
    // 사용자 메뉴 토글
    $('#userIcon').click(function(event) {
        event.preventDefault();
        event.stopPropagation();
        $('.user_two_menu').toggleClass('visible');
        $('.arrow-up').toggleClass('visible');
    });

    $(document).click(function(event) {
        if (!$(event.target).closest('#userIcon, .user_two_menu').length) {
            $('.user_two_menu').removeClass('visible');
            $('.arrow-up').removeClass('visible');
        }
    });

    $('.user_two_menu').click(function(event) {
        event.stopPropagation();
    });

    // 슬라이더 및 기타 공통 기능들 초기화 코드
    function initializeSlider(section) {
        // Slider logic...
    }

    const sections = $(".drive_section");
    sections.each(function() {
        initializeSlider($(this));
    });
});
