$(document).ready(function() {
    let currentSlide = 0;
    const slides = $('.slide');
    const slideCount = slides.length;

    function showSlide(n) {
        slides.hide();
        slides.eq(n).show();
    }

    function nextSlide() {
        currentSlide = (currentSlide + 1) % slideCount;
        showSlide(currentSlide);
    }

    function prevSlide() {
        currentSlide = (currentSlide - 1 + slideCount) % slideCount;
        showSlide(currentSlide);
    }

    showSlide(currentSlide);
    setInterval(nextSlide, 5000);

    // 슬라이더 초기화는 layout.js에서 수행됨

    // 슬라이더 외의 추가 초기화 코드
    const sections = $(".drive_section");
    sections.each(function() {
        initializeSlider($(this));
    });
});
